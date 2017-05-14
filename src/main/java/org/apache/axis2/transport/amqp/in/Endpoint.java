package org.apache.axis2.transport.amqp.in;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.amqp.builders.ConfigurationParser;
import org.apache.axis2.transport.amqp.builders.RabbitUrlParser;
import org.apache.axis2.transport.amqp.common.Constants;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.apache.axis2.transport.amqp.data.RabbitUrl;
import org.apache.axis2.transport.amqp.out.ref.ChannelReferenceStore;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endpoint extends ProtocolEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(Endpoint.class);
    private static final ConfigurationParamCallback listenerUrlCallback = new ConfigurationParamCallback() {
        public String get(Configuration configuration) {
            return configuration.getListenerUrl();
        }
    };
    private static final ConfigurationParamCallback replyToUrlCallback = new ConfigurationParamCallback() {
        public String get(Configuration configuration) {
            return configuration.getReplyToUri();
        }
    };
    private static final ConfigurationParamCallback faultUrlCallback = new ConfigurationParamCallback() {
        public String get(Configuration configuration) {
            return configuration.getFaultUri();
        }
    };
    
//    private static final EndpointReference[] emptyReferences = {};
    private final Listener listener;
    private final WorkerPool workerPool;
    private List<ListenerWorker> workers;
    private CountDownLatch pausedEvent;
    private EndpointReference[] eprs;
    private ListenerProxy proxy = new ListenerProxy(this);
    private Context context;
    private Context replyToContext;
    private Context faultContext;
    private ChannelReferenceStore channelReferenceStore;
    private String proxyName;
    
    
    Endpoint(Listener listener, WorkerPool workerPool) {
        this.listener = listener;
        this.workerPool = workerPool;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        if (logger.isDebugEnabled()) {
            logger.debug("loadConfiguration");
        }
        if (!(params instanceof AxisService)) {
            return false;
        }
        proxyName = ((AxisService) params).getName();
        final ArrayList<Parameter> parameters = params.getParameters();
        this.context = createContext(parameters, Constants.LISTENER_URI, listenerUrlCallback, true, null, false, false);
        RabbitUrl contextUrl = context != null ? context.getUrl() : null;
        this.replyToContext = createContext(parameters, Constants.REPLY_TO_URI, replyToUrlCallback, false, context.getUrl(), false, false);
        RabbitUrl replyToUrl = replyToContext != null ? replyToContext.getUrl() : null;
        final RabbitUrl faultBaseUrl;
        final boolean faultBaseUrlSameDirection;
        if (replyToUrl != null) {
            faultBaseUrl = replyToUrl;
            faultBaseUrlSameDirection = true;
        } else {
            faultBaseUrl = contextUrl;
            faultBaseUrlSameDirection = false;
        }
        this.faultContext = createContext(parameters, Constants.FAULT_URI, faultUrlCallback, false, faultBaseUrl, faultBaseUrlSameDirection, false);
        
        this.eprs = buildEprs(listener, context.getUrl());
        logger.info("loaded " + context.getUrl());
        return true;
    }
    
    private Context createContext(final ArrayList<Parameter> parameters, String parametersKey, final ConfigurationParamCallback callback, 
            final boolean isIn, RabbitUrl baseUrl, boolean baseUrlSameDirection, boolean exceptionOnNotFound) throws AxisFault {
        String transportName = listener.getTransportName();
        String serviceName = this.getServiceName();
        
        boolean found = false;
        final String paramUri = findParameter(parameters, parametersKey);
        if (paramUri == "" || paramUri == null) {
            return null;
        }

        found |= paramUri != null;
        
        RabbitUrl url = new RabbitUrl();
        RabbitUrl serviceUrl = RabbitUrlParser.parseSingle(paramUri, transportName);
        url.mergeFrom(serviceUrl, true);
        url.mergeFrom(baseUrl, baseUrlSameDirection);
        
        Configuration configuration = listener.getConfiguration(url.getConfigurationName(), isIn);
        if (configuration != null) {
            String configurationUrlStr = callback.get(configuration);
            found |= configurationUrlStr != null;
            RabbitUrl configurationUrl = RabbitUrlParser.parseSingle(configurationUrlStr, transportName);
            url.mergeFrom(configurationUrl, true);
        }
        if (url == null || !found) {
            if (exceptionOnNotFound) {
                throw new IllegalArgumentException("parameter " + parametersKey + " not found");
            } else {
                return null;
            }
        }
        RabbitUrlParser.parseFinished(url, transportName, serviceName);
        logger.info(parametersKey + "=" + url);
        String configurationName = url.getConfigurationName();
        Configuration commonConfiguration = ConfigurationParser.parse(parameters, true);
        
        final Configuration systemConfiguration = listener.getConfiguration(configurationName, isIn);
        Configuration resultConfiguration = commonConfiguration.merge(systemConfiguration);
        Context result = new Context(url, resultConfiguration);
        return result;
    }
    
    private String findParameter(ArrayList<Parameter> parameters, String name) {
        for (Parameter parameter : parameters) {
            if (name.equals(parameter.getName())) {
                String result = (String) parameter.getValue();
                return result;
            }
        }
        return null;
    }

    private static EndpointReference[] buildEprs(Listener listener, RabbitUrl url) throws AxisFault {
        EndpointReference reference = new EndpointReference(url.getUrl());
        return new EndpointReference[] {reference};
    }

    
    @Override
    public EndpointReference[] getEndpointReferences(AxisService service, String ip) throws AxisFault {
        return eprs;
    }
    
    public ListenerProxy getProxy() {
        return proxy;
    }

    public ChannelReferenceStore getChannelReferenceStore() {
        return channelReferenceStore;
    }

    
    public synchronized void pause() {
        doPause();
    }
    
    private void doPause() {
        if (workers == null) {
            logger.warn("cannot pause. not started");
        } else if (pausedEvent == null) {
            pausedEvent = new CountDownLatch(1);
            for (ListenerWorker worker : workers) {
                worker.pauseUntil(pausedEvent);
            }
        } else {
            logger.warn("already paused");
        }
    }
    
    public synchronized void resume(boolean shouldBePaused) {
        doResume(shouldBePaused);
    }

    private void doResume(boolean shouldBePaused) {
        if (pausedEvent != null) {
            if (!shouldBePaused) {
                logger.warn("Unexpected paused state. resuming");
            }
            pausedEvent.countDown();
            pausedEvent = null;
        } else if (shouldBePaused) {
            logger.warn("not paused");
        }
    }

    public synchronized void start() {
        channelReferenceStore = new ChannelReferenceStore();
        if (workers == null) {
            final int workerCount = context.getConfiguration().getListenerThreadPool();
            workers = new ArrayList<ListenerWorker>(workerCount);
            for (int i = 0; i < workerCount; i++) {
                ListenerWorker worker = new ListenerWorker(this, context, replyToContext, faultContext);
                workers.add(worker);
            }
            for (ListenerWorker worker : workers) {
                worker.starting();
                workerPool.execute(worker);
            }
        } else {
            logger.warn("already started");
        }
    }
    
    public synchronized void stop() {
        if (workers == null) {
            logger.warn("not started");
        } else {
            doPause();
            logger.info("store inside stop " + channelReferenceStore);
            channelReferenceStore.stop(context.getConfiguration().getChannelReferenceTimeoutMillis());
            for (ListenerWorker worker : workers) {
                worker.markStopping();
            }
            doResume(true);
            for (ListenerWorker worker : workers) {
                worker.stop();
            }
            workers = null;
        }
        Context.close(context);
        Context.close(replyToContext);
        Context.close(faultContext);
    }

    public synchronized void restart() {
        for (ListenerWorker worker : workers) {
            worker.markStopping();
        }
        workers = null;
        Context.close(context);
        Context.close(replyToContext);
        Context.close(faultContext);
        logger.info("Restarting listener workers");
        start();
    }

    private interface ConfigurationParamCallback {
        String get(Configuration configuration);
    }

    public String getProxyName(){
        return proxyName;
    }
}

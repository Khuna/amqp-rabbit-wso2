package org.apache.axis2.transport.amqp.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.amqp.builders.ConfigurationParser;
import org.apache.axis2.transport.amqp.common.Utils;
import org.apache.axis2.transport.amqp.data.Configuration;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends AbstractTransportListenerEx<Endpoint> {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);
    private List<Endpoint> startedEndpoints = new ArrayList<Endpoint>();
    private Map<String, Configuration> inConfigurations;
    private Map<String, Configuration> outConfigurations;

    @Override
    protected void doInit() throws AxisFault {
        if (logger.isDebugEnabled()) {
            logger.debug("init");
        }
        ArrayList<Parameter> inParameters = getTransportInDescription().getParameters();
        this.inConfigurations = parseConfigurations(inParameters);
        ArrayList<Parameter> outParameters = getTransportOutDescription().getParameters();
        this.outConfigurations = parseConfigurations(outParameters);
        logger.info("loaded");
    }

    @Override
    protected Endpoint createEndpoint() {
        Endpoint result = new Endpoint(this, workerPool);
        if (logger.isDebugEnabled()) {
            logger.debug("createEndpoint.result=" + result);
        }
        return result;
    }

    @Override
    protected void startEndpoint(Endpoint endpoint) throws AxisFault {
        if (logger.isDebugEnabled()) {
            logger.debug("startEndpoint.endpoint=" + endpoint);
        }
        endpoint.start();
        startedEndpoints.add(endpoint);
    }
    
    @Override
    public void maintenenceShutdown(long millis) throws AxisFault {
        super.maintenenceShutdown(millis);
        pause();
    }

    @Override
    protected void stopEndpoint(Endpoint endpoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("stopEndpoint.endpoint=" + endpoint);
        }
        startedEndpoints.remove(endpoint);
        endpoint.stop();
    }
    
    @Override
    public void pause() throws AxisFault {
        super.pause();
        for (Endpoint endpoint : startedEndpoints) {
            endpoint.pause();
        }
    }
    
    @Override
    public void resume() throws AxisFault {
        super.resume();
        for (Endpoint endpoint : startedEndpoints) {
            endpoint.resume(true);
        }
    }

    private static final Map<String, Configuration> parseConfigurations(List<Parameter> parameters) throws AxisFault {
        Map<String, Configuration> result = ConfigurationParser.parseList(parameters, false);
        result = Collections.unmodifiableMap(result);
        return result;
    }
    
    private TransportOutDescription getTransportOutDescription() {
        String transportName = getTransportName();
        ConfigurationContext cfgCtx = getConfigurationContext();
        AxisConfiguration axisConfiguration = cfgCtx.getAxisConfiguration();
        TransportOutDescription result = axisConfiguration.getTransportOut(transportName);
        return result;
    }
    
    public Configuration getDefaultInConfiguration() {
        return getInConfiguration(null);
    }

    public Configuration getInConfiguration(String name) {
        return Utils.getConfiguration(name, inConfigurations);
    }
    
    public Configuration getOutConfiguration(String name) {
        return Utils.getConfiguration(name, outConfigurations);
    }
    
    public Configuration getConfiguration(String name, boolean isIn) {
        if (isIn) {
            return getInConfiguration(name);
        } else {
            return getOutConfiguration(name);
        }
    }
}

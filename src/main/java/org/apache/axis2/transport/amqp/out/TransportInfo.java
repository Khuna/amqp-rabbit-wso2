package org.apache.axis2.transport.amqp.out;

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.amqp.connection.Context;
import org.apache.axis2.transport.amqp.in.Endpoint;
import org.apache.axis2.transport.amqp.out.ref.AckReference;

public class TransportInfo implements OutTransportInfo {
    private final Context context;
    private final Context faultContext;
    private String contentType;
    private final AckReference ackReference;
    private Endpoint endpoint;
    
    public TransportInfo(Context context, Context faultContext, AckReference ackReference, Endpoint endpoint) {
        this.context = context;
        this.faultContext = faultContext;
        this.ackReference = ackReference;
        this.endpoint = endpoint;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public Context getcontext() {
        return context;
    }

    public Context getFaultContext() {
        return faultContext;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public AckReference getAckReference() {
        return ackReference;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

}

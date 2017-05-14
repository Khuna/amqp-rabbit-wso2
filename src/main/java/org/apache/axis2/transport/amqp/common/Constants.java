/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.amqp.common;


public class Constants {
    public static final String CONNECTION_THREAD_POOL = "amqp.connection.thread.pool";
    public static final String LISTENER_THREAD_POOL = "amqp.listener.thread.pool";
    public static final String QUEUE_AUTO_ACK = "amqp.queue.auto.ack";
    public static final String EXCHANGE_TYPE = "amqp.exchange.type";
    public static final String QUEUE_EXCLUSIVE = "amqp.queue.exclusive";
    public static final String QUEUE_DURABLE = "amqp.queue.durable";
    public static final String QUEUE_AUTO_DELETE = "amqp.queue.auto.delete";
    public static final String RECONNECT_INTERVAL_MILLIS = "amqp.worker.reconnect.interval.millis";
    public static final String WAIT_MESSAGE_TIMEOUT_MILLIS = "amqp.worker.wait.message.timeout.millis";
    public static final String FINISH_TIMEOUT_MILLIS = "amqp.worker.finish.timeout.millis";
    public static final String CHANNEL_REFERENCE_TIMEOUT_MILLIS = "amqp.worker.channel.reference.timeout.millis";
    public static final String RECEIVER_REPLY_TO_ENABLED = "amqp.receiver.reply.to.enabled";
    public static final String SYNC_RESPONSE_ENABLED = "amqp.sync.response.enabled";
    public static final String ASYNC_RESPONSE_ENABLED = "amqp.async.response.enabled";

    public static final String CORRELATION_ID = "amqp.message.correlation.id";
    public static final String CONTENT_TYPE = "amqp.message.content.type";
    public static final String CONTENT_ENCODING = "amqp.message.content.encoding";
    public static final String MESSAGE_ID = "amqp.message.id";
    public static final String REPLY_TO = "amqp.message.replyto";
    public static final String QUEUE_DELIVERY_MODE = "amqp.queue.delivery.mode";
    

    public static final String DEFAULT_CONFIGURATION_NAME = "default";
    public static final String CONFIGURATION_NAME = "conf";

    public static final String LISTENER_URI = "amqp.listener.uri";
    public static final String REPLY_TO_URI = "amqp.reply.to.uri";
    public static final String FAULT_URI = "amqp.fault.uri";
    public static final String SENDER_URI = "amqp.sender.uri";
    
    public static final int DEFAULT_CONNECTION_THREAD_POOL = 5;
    public static final int DEFAULT_LISTENER_THREAD_POOL = 5;
    public static final boolean DEFAULT_AUTO_ACK = true;
    public static final String DEFAULT_EXCHANGE_TYPE = "direct";
    public static final Boolean DEFAULT_EXCLUSIVE = false;
    public static final Boolean DEFAULT_DURABLE = true; 
    public static final Boolean DEFAULT_AUTO_DELETE = false; 
    public static final long DEFAULT_RECONNECT_INTERVAL_MILLIS = 10000;
    public static final long DEFAULT_WAIT_MESSAGE_TIMEOUT_MILLIS = 2000; 
    public static final long DEFAULT_FINISH_TIMEOUT_MILLIS = 20000;
    public static final long DEFAULT_CHANNEL_REFERENCE_TIMEOUT_MILLIS = 20000;
    public static final Boolean DEFAULT_RECEIVER_REPLY_TO_ENABLED = true;
    public static final Boolean DEFAULT_SYNC_RESPONSE_ENABLED = true;
    public static final Boolean DEFAULT_ASYNC_RESPONSE_ENABLED = true;
    
    
    public static final String SOAP_ACTION = "SOAP_ACTION";
    public static final String DEFAULT_SOAP_ACTION = "GREET";
    public static final String CHARACTER_SET_ENCODING = org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING;
    public static final String DEFAULT_CONTENT_TYPE = org.apache.axis2.namespace.Constants.MIME_CT_APPLICATION_XML;
    public static final String OUT_TRANSPORT_INFO = org.apache.axis2.Constants.OUT_TRANSPORT_INFO;

    public static final String CONSUMER_TAG = "tag";
    
    
}

package org.apache.axis2.transport.amqp.out.cache;

public interface ValueVisitor<T> {
    void visit(T value);
}

package org.apache.axis2.transport.amqp;

public interface TestConsts {
    public static final String transport = "amqp";
    public static final String login = "Вася :Пупкин";
    public static final String password = "Пароль с пробелами @:";
    public static final String virtualHost = " $%@virtual/host/ ";
    public static final String queue = " $%@queue1/queue2/ ";
    public static final String exchange = " $%@exchange1/exchange2/ ";
    public static final String configurationName = "default conf";
    public static final String loginEncoded = "%D0%92%D0%B0%D1%81%D1%8F+%3A%D0%9F%D1%83%D0%BF%D0%BA%D0%B8%D0%BD";
    public static final String passwordEncoded = "%D0%9F%D0%B0%D1%80%D0%BE%D0%BB%D1%8C+%D1%81+%D0%BF%D1%80%D0%BE%D0%B1%D0%B5%D0%BB%D0%B0%D0%BC%D0%B8+%40%3A";
    public static final String virtualHostEncoded = "+%24%25%40virtual%2Fhost%2F+";
    public static final String queueEncoded = "+%24%25%40queue1%2Fqueue2%2F+";
    public static final String exchangeEncoded = "+%24%25%40exchange1%2Fexchange2%2F+";
    public static final String configurationNameEncoded = "default+conf";
    public static final String urlStr = String.format("%s://%s:%s@localhost/%s/%s/%s/%s?conf=%s", 
            transport, loginEncoded, passwordEncoded, queueEncoded, exchangeEncoded, 
            virtualHostEncoded, queueEncoded, configurationNameEncoded);
}

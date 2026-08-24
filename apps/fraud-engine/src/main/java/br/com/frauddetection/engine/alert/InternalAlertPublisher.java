package br.com.frauddetection.engine.alert;

public interface InternalAlertPublisher {

    void publish(FraudAlert alert);
}
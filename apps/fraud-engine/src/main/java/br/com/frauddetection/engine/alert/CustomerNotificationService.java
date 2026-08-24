package br.com.frauddetection.engine.alert;

public interface CustomerNotificationService {

    void notify(FraudAlert alert);
}
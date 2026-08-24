package br.com.frauddetection.engine.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogCustomerNotificationService
        implements CustomerNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(LogCustomerNotificationService.class);

    @Override
    public void notify(FraudAlert alert) {

        log.info(
                "Notificação ao cliente gerada. idAlerta={} idEvento={} idCliente={}",
                alert.idAlerta(),
                alert.idEvento(),
                alert.idCliente()
        );
    }
}
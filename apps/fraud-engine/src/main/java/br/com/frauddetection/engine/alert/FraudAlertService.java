package br.com.frauddetection.engine.alert;

import br.com.frauddetection.engine.rules.FraudRuleResult;
import br.com.frauddetection.events.TransactionEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FraudAlertService {

    private final InternalAlertPublisher internalAlertPublisher;
    private final CustomerNotificationService customerNotificationService;

    public FraudAlertService(
            InternalAlertPublisher internalAlertPublisher,
            CustomerNotificationService customerNotificationService
    ) {
        this.internalAlertPublisher = internalAlertPublisher;
        this.customerNotificationService = customerNotificationService;
    }

    public void enviarAlertas(
            TransactionEvent event,
            List<FraudRuleResult> resultados
    ) {

        List<String> regras =
                resultados.stream()
                        .map(FraudRuleResult::regra)
                        .toList();

        FraudAlert alert =
                new FraudAlert(
                        UUID.randomUUID().toString(),
                        event.getIdEvento(),
                        event.getIdTransacao(),
                        event.getIdCliente(),
                        regras,
                        Instant.now()
                );

        internalAlertPublisher.publish(alert);

        customerNotificationService.notify(alert);
    }
}
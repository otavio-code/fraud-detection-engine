package br.com.frauddetection.engine.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailCustomerNotificationService
        implements CustomerNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    EmailCustomerNotificationService.class
            );

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String destinatario;

    public EmailCustomerNotificationService(
            JavaMailSender mailSender,
            @Value("${fraud.notification.email.from}")
            String remetente,
            @Value("${fraud.notification.email.to}")
            String destinatario
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.destinatario = destinatario;
    }

    @Override
    public void notify(FraudAlert alert) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(remetente);
        message.setTo(destinatario);

        message.setSubject(
                "Alerta de transação suspeita"
        );

        message.setText(
                """
                Uma transação suspeita foi identificada.

                Id do alerta: %s
                Id do evento: %s
                Id da transação: %s
                Id do cliente: %s
                Regras identificadas: %s
                Data/hora: %s
                """.formatted(
                        alert.idAlerta(),
                        alert.idEvento(),
                        alert.idTransacao(),
                        alert.idCliente(),
                        String.join(", ", alert.regras()),
                        alert.dataHora()
                )
        );

        mailSender.send(message);

        log.info(
                "Notificação externa enviada. idAlerta={} idEvento={}",
                alert.idAlerta(),
                alert.idEvento()
        );
    }
}
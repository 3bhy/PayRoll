package ca.uhn.fhir.jpa.subscription.channel.subscription;

import ca.uhn.fhir.jpa.subscription.match.deliver.email.IEmailSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class SubscriptionDeliveryHandlerFactory {

    private final Optional<IEmailSender> myEmailSender;

    public SubscriptionDeliveryHandlerFactory(
            @Autowired(required = false) IEmailSender theEmailSender) {

        myEmailSender = Optional.ofNullable(theEmailSender);
    }
}	

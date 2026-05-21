package com.pragma.msgservice;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class TwilioSmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone-number}")
    private String fromNumber;

    @Value("${twilio.whatsapp-phone-number}")
    private String whatsappNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized successfully.");
    }

    public void sendSms(SmsRequest request){
        String destination;
        String from;
        try{
            if (request.getDestinationPhoneNumber().contains("whatsapp:")) {
                destination = request.getDestinationPhoneNumber();
                from = whatsappNumber;
            } else {
                destination = request.getDestinationPhoneNumber();
                from = fromNumber;
            }
            Message message = Message.creator(
                    new PhoneNumber(destination),
                    new PhoneNumber(from),
                    request.getMessage()
            ).create();
            log.info("SMS sent successfully. SID: {}", message.getSid());
        }catch (Exception e){
            log.error("Failed to send SMS: {}", e.getMessage());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }
}

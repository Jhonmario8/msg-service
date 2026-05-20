package com.pragma.msgservice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sms")
@RequiredArgsConstructor
public class SmsController {

    private final TwilioSmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<String> sendSms(@Valid @RequestBody SmsRequest request){
        smsService.sendSms(request);
        return ResponseEntity.ok(Constants.SMS_SENT_SUCCESS);
    }
}

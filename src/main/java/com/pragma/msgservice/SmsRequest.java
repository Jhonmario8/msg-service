package com.pragma.msgservice;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequest {

    @NotBlank(message = Constants.PHONE_NUMBER_REQUIRED)
    private String destinationPhoneNumber;

    @NotBlank(message = Constants.MESSAGE_REQUIRED )
    private String message;


}

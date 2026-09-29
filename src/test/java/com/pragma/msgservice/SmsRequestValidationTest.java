package com.pragma.msgservice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmsRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static SmsRequest request(String phone, String message) {
        SmsRequest request = new SmsRequest();
        request.setDestinationPhoneNumber(phone);
        request.setMessage(message);
        return request;
    }

    private static List<String> messages(SmsRequest request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessage).toList();
    }

    @Test
    @DisplayName("una solicitud con teléfono y mensaje es válida")
    void validRequest() {
        assertThat(validator.validate(request("+573001234567", "hola"))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("el teléfono de destino es obligatorio")
    void phoneIsRequired(String phone) {
        assertThat(messages(request(phone, "hola"))).containsExactly(Constants.PHONE_NUMBER_REQUIRED);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("el mensaje es obligatorio")
    void messageIsRequired(String message) {
        assertThat(messages(request("+573001234567", message))).containsExactly(Constants.MESSAGE_REQUIRED);
    }
}

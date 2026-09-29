package com.pragma.msgservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsControllerTest {

    @Mock
    private TwilioSmsService smsService;

    @InjectMocks
    private SmsController smsController;

    private static SmsRequest request() {
        SmsRequest request = new SmsRequest();
        request.setDestinationPhoneNumber("whatsapp:+573001234567");
        request.setMessage("Your order has been delivered! Enjoy your meal!");
        return request;
    }

    @Test
    @DisplayName("delega el envío al servicio y responde 200 con el mensaje de éxito")
    void sendsSmsAndReturnsOk() {
        // given
        SmsRequest request = request();

        // when
        ResponseEntity<String> response = smsController.sendSms(request);

        // then
        verify(smsService).sendSms(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Constants.SMS_SENT_SUCCESS);
    }

    @Test
    @DisplayName("propaga la excepción del servicio cuando Twilio falla")
    void propagatesServiceErrors() {
        // given
        SmsRequest request = request();
        doThrow(new RuntimeException("Failed to send SMS: timeout")).when(smsService).sendSms(request);

        // when / then
        assertThatThrownBy(() -> smsController.sendSms(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send SMS: timeout");
    }
}

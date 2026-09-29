package com.pragma.msgservice;

import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TwilioSmsService llama al SDK de Twilio de forma estática (Message.creator(...).create()),
 * por eso aquí se usa Mockito.mockStatic sobre Message: ninguna llamada sale a la red.
 * El servicio se instancia con new, así que el @PostConstruct (Twilio.init) nunca se ejecuta
 * y no se necesitan credenciales.
 */
class TwilioSmsServiceTest {

    private static final String SMS_FROM = "+15550000001";
    private static final String WHATSAPP_FROM = "whatsapp:+15550000002";

    private TwilioSmsService service;
    private MockedStatic<Message> messageStatic;
    private MessageCreator creator;

    @BeforeEach
    void setUp() {
        service = new TwilioSmsService();
        ReflectionTestUtils.setField(service, "fromNumber", SMS_FROM);
        ReflectionTestUtils.setField(service, "whatsappNumber", WHATSAPP_FROM);

        creator = mock(MessageCreator.class);
        messageStatic = mockStatic(Message.class);
        messageStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
                .thenReturn(creator);
    }

    @AfterEach
    void tearDown() {
        messageStatic.close();
    }

    private static SmsRequest request(String destination, String message) {
        SmsRequest request = new SmsRequest();
        request.setDestinationPhoneNumber(destination);
        request.setMessage(message);
        return request;
    }

    @Test
    @DisplayName("envía SMS normal desde el número SMS configurado con el mensaje recibido")
    void sendsPlainSmsFromSmsNumber() {
        // given
        Message message = mock(Message.class);
        when(creator.create()).thenReturn(message);

        // when
        service.sendSms(request("+573001234567", "Tu pedido está listo"));

        // then
        messageStatic.verify(() -> Message.creator(
                new PhoneNumber("+573001234567"),
                new PhoneNumber(SMS_FROM),
                "Tu pedido está listo"));
        verify(creator).create();
    }

    @Test
    @DisplayName("si el destino empieza por whatsapp: usa el número de WhatsApp como origen")
    void sendsWhatsappFromWhatsappNumber() {
        // given
        when(creator.create()).thenReturn(mock(Message.class));

        // when
        service.sendSms(request("whatsapp:+573001234567", "Your order is ready for pickup!1234"));

        // then
        messageStatic.verify(() -> Message.creator(
                new PhoneNumber("whatsapp:+573001234567"),
                new PhoneNumber(WHATSAPP_FROM),
                "Your order is ready for pickup!1234"));
        verify(creator).create();
    }

    @Test
    @DisplayName("si Twilio lanza ApiException la envuelve en RuntimeException con el detalle")
    void wrapsTwilioErrors() {
        // given
        when(creator.create()).thenThrow(new ApiException("The 'To' number is not a valid phone number."));

        // when / then
        assertThatThrownBy(() -> service.sendSms(request("123", "hola")))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send SMS: The 'To' number is not a valid phone number.");
    }

    @Test
    @DisplayName("si el destino es null también termina en RuntimeException (no llega a Twilio)")
    void wrapsNullDestination() {
        // when / then
        assertThatThrownBy(() -> service.sendSms(request(null, "hola")))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Failed to send SMS:");
        messageStatic.verifyNoInteractions();
    }
}

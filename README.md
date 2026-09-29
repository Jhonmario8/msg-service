# Msg Service

Microservicio de mensajería del proyecto Reto Pragma (plazoleta de comidas). Expone un endpoint REST que recibe un número de destino y un texto, y los envía como SMS o como mensaje de WhatsApp usando el SDK de Twilio para Java. `plazoleta-service` lo llama por OpenFeign de forma síncrona cuando un pedido pasa a listo, se entrega o se cancela.

Forma parte del repositorio [Reto-Pragma](https://github.com/Jhonmario8/Reto-Pragma).

## Tabla de contenidos

- [Tecnologías](#tecnologías)
- [Estructura](#estructura)
- [Endpoint](#endpoint)
- [Requisitos: cuenta de Twilio](#requisitos-cuenta-de-twilio)
- [Variables de entorno](#variables-de-entorno)
- [Ejecución en local](#ejecución-en-local)
- [Tests](#tests)
- [Limitaciones conocidas](#limitaciones-conocidas)

## Tecnologías

- Java 17, Spring Boot 3.3.5 (Web, Validation)
- SDK de Twilio para Java 8.31.1
- Lombok
- Gradle 9.4.1 (wrapper incluido)
- JUnit 5, Mockito y AssertJ

No tiene base de datos ni Spring Security.

## Estructura

A diferencia de los otros servicios, no sigue la arquitectura hexagonal: son cuatro clases en un solo paquete.

```
src/main/java/com/pragma/msgservice/
  SmsController.java      POST /sms/send
  SmsRequest.java         DTO con destinationPhoneNumber y message (@NotBlank)
  TwilioSmsService.java   Inicializa Twilio y envía el mensaje con Message.creator(...).create()
  Constants.java          Mensajes de validación y de éxito
```

## Endpoint

`POST /sms/send`

```json
{
  "destinationPhoneNumber": "whatsapp:+573001234567",
  "message": "Your order is ready for pickup!4821"
}
```

- Si `destinationPhoneNumber` contiene `whatsapp:`, el mensaje sale desde `TWILIO_WHATSAPP_FROM_PHONE_NUMBER`. En cualquier otro caso sale como SMS desde `TWILIO_FROM_PHONE_NUMBER`.
- `plazoleta-service` siempre envía el destino como `whatsapp:+57<teléfono>`.

Respuestas:

| Código | Cuándo |
|---|---|
| 200 | Twilio aceptó el mensaje. Cuerpo: `SMS sent successfully.` |
| 400 | Falta el número o el mensaje. |
| 500 | Twilio devolvió un error. El servicio lo envuelve en `RuntimeException("Failed to send SMS: ...")` y no hay un manejador de errores propio. |

## Requisitos: cuenta de Twilio

Para enviar mensajes reales se necesita una cuenta de Twilio. Sirve una cuenta de prueba (trial), con estas restricciones de Twilio:

- Solo se puede enviar a números verificados en la consola de Twilio.
- Los mensajes llevan un prefijo de cuenta de prueba.
- Para WhatsApp hay que usar el sandbox de Twilio y unir el número de destino al sandbox antes de enviarle mensajes.

## Variables de entorno

Referenciadas en `src/main/resources/application.yml`. Las cuatro son obligatorias para arrancar, porque `Twilio.init` se ejecuta en `@PostConstruct` y Spring no resuelve los placeholders vacíos.

| Variable | Descripción |
|---|---|
| `TWILIO_ACCOUNT_SID` | Account SID de la consola de Twilio. |
| `TWILIO_AUTH_TOKEN` | Auth Token de la consola de Twilio. |
| `TWILIO_FROM_PHONE_NUMBER` | Número de Twilio para SMS, formato E.164 (`+1...`). |
| `TWILIO_WHATSAPP_FROM_PHONE_NUMBER` | Número de WhatsApp del sandbox, con prefijo (`whatsapp:+1...`). |

El puerto es `8082` (`server.port`).

## Ejecución en local

```bash
export TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
export TWILIO_AUTH_TOKEN=<auth_token>
export TWILIO_FROM_PHONE_NUMBER=+15550000000
export TWILIO_WHATSAPP_FROM_PHONE_NUMBER=whatsapp:+14155238886

./gradlew bootRun
```

Prueba manual:

```bash
curl -X POST http://localhost:8082/sms/send \
  -H "Content-Type: application/json" \
  -d '{"destinationPhoneNumber":"+573001234567","message":"Prueba"}'
```

## Tests

```bash
./gradlew test
```

Los tests unitarios no envían SMS reales ni hacen peticiones a Twilio, y no necesitan credenciales:

| Clase | Qué cubre |
|---|---|
| `SmsControllerTest` | El controlador delega en `TwilioSmsService` (mockeado) y responde 200; si el servicio falla, propaga la excepción. |
| `TwilioSmsServiceTest` | Número de origen SMS vs WhatsApp, texto enviado y conversión de `ApiException` de Twilio en `RuntimeException`. |
| `SmsRequestValidationTest` | Mensajes de validación cuando falta el número o el texto. |

`TwilioSmsService` llama al SDK con métodos estáticos (`Message.creator(...)`) y no recibe un cliente inyectable. Por eso el test usa `Mockito.mockStatic(Message.class)`, que viene con el mock maker inline de `spring-boot-starter-test` y no requiere dependencias extra. El servicio se instancia con `new`, así que `Twilio.init` no se ejecuta.

## Limitaciones conocidas

- Sin credenciales de Twilio la aplicación no arranca.
- Los errores de Twilio llegan al cliente como un 500 genérico.
- El endpoint no requiere autenticación.
- El acoplamiento directo con las clases estáticas de Twilio complica los tests. Una interfaz propia (por ejemplo, un `SmsSender`) permitiría mockear el envío con `@Mock`.

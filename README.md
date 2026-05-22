# Msg Service

Microservicio para el envío de mensajes SMS y WhatsApp utilizando Twilio como proveedor, desarrollado en Java 17 y Spring Boot 3. Proporciona una API REST sencilla para integrarse con otros componentes de una plataforma o ser usado standalone.

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Variables de entorno](#variables-de-entorno)
- [Configuración inicial](#configuración-inicial)
- [Uso de la API](#uso-de-la-api)
- [Testing](#testing)
- [Contribuciones](#contribuciones)

---

## Descripción

Este microservicio expone un endpoint `/sms/send` para el envío de mensajes SMS tradicionales o vía WhatsApp, centralizando la lógica de validación y despacho sobre la plataforma Twilio. Puede ser integrado en arquitecturas de microservicios para notificaciones, autenticación por SMS, alertas, etc.

---

## Tecnologías

- **Java 17**
- **Spring Boot 3** (`spring-boot-starter-web`, `spring-boot-starter-validation`)
- **Lombok** (para reducir boilerplate)
- **Twilio SDK Java**
- **Gradle 9.4.1** (wrapper incluido)
- **JUnit 5 y Spring Boot Test**

---

## Estructura del proyecto

```
src/
 ├─ main/
 │   ├─ java/com/pragma/msgservice/
 │   │   ├─ Constants.java            # Mensajes de validación y éxito
 │   │   ├─ SmsController.java        # Controlador REST principal
 │   │   ├─ SmsRequest.java           # DTO de entrada para solicitudes de SMS
 │   │   └─ TwilioSmsService.java     # Lógica para el uso de API de Twilio
 │   └─ resources/
 │       └─ application.yml           # Configuración de Twilio y server
 └─ test/
     └─ java/com/pragma/msgservice/
         └─ MsgServiceApplicationTests.java # Prueba básica de contexto
```

---

## Variables de entorno

Para un funcionamiento seguro, las credenciales de Twilio deben configurarse vía variables de entorno, referenciadas en `src/main/resources/application.yml`:

- `TWILIO_ACCOUNT_SID` — SID de cuenta Twilio.
- `TWILIO_AUTH_TOKEN` — Token de autenticación Twilio.
- `TWILIO_FROM_PHONE_NUMBER` — Número telefónico registrado en Twilio para enviar SMS.
- `TWILIO_WHATSAPP_FROM_PHONE_NUMBER` — Número configurado en Twilio para WhatsApp (formato: `whatsapp:+...`).

Opcional: cambia el puerto en `application.yml` si lo requieres (por defecto 8082).

---

## Configuración inicial

1. Clona este repo:
    ```sh
    git clone https://github.com/Jhonmario8/msg-service.git
    cd msg-service
    ```
2. Prepara el entorno Java 17/Gradle y exporta las variables de entorno de Twilio según corresponda.
3. Ejecuta el servicio:
    ```sh
    ./gradlew bootRun
    ```
   El microservicio quedará disponible en: http://localhost:8082

---

## Uso de la API

### Endpoint principal

`POST /sms/send`

#### Request Body

```json
{
  "destinationPhoneNumber": "+573001112233",
  "message": "Tu código de verificación es 123456"
}
```

- Para WhatsApp, el número debe ir así: `"destinationPhoneNumber": "whatsapp:+573001112233"`

#### Respuesta exitosa

```json
"SMS sent successfully."
```

- Status HTTP: **200 OK**

#### Posibles errores

- 400 Bad Request (cuando falta número/mensaje)
- 500 Internal Server Error (si Twilio falla)

---

## Testing

Para correr todos los tests:

```sh
./gradlew test
```

---

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o haz un fork y PR siguiendo las prácticas estándar de la comunidad.

---

> Servicio desarrollado por [Jhonmario8](https://github.com/Jhonmario8) como pieza de mensajería versátil y desacoplada para microservicios.
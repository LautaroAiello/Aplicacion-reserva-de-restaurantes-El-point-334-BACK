🍽️ Sistema de Gestión y Reservas de Restaurantes (Backend)

Este repositorio contiene el código fuente del backend de una plataforma distribuida para la gestión integral de restaurantes y reservas en tiempo real. Construido con una arquitectura de microservicios escalable, prioriza la consistencia de datos, la seguridad y la eficiencia operativa.

🚀 Características Principales

Arquitectura Distribuida: Sistema modular basado en microservicios independientes.

Gestión de Transacciones Distribuidas: Implementación de patrones para garantizar la integridad de datos entre servicios (ej: Creación de Restaurante + Admin).

Comunicación Híbrida:

Síncrona (Feign Clients): Para operaciones bloqueantes y consultas directas entre servicios.

Asíncrona (RabbitMQ): Para procesos desacoplados como el envío de notificaciones y correos electrónicos.

Seguridad Robusta: Autenticación y autorización centralizada mediante Spring Security y JWT (JSON Web Tokens) con manejo de roles (ADMIN, GESTOR, CLIENTE).

Persistencia Políglota: Uso de PostgreSQL para datos transaccionales y MongoDB para logs de auditoría y notificaciones.

Service Discovery & Routing: Orquestación dinámica con Eureka Server y Spring Cloud Gateway.

🛠️ Tech Stack

Lenguaje: Java 17

Framework: Spring Boot 3

Bases de Datos: PostgreSQL, MongoDB

Mensajería: RabbitMQ

Seguridad: Spring Security, JWT (jjwt)

Infraestructura: Docker (Contenerización de servicios y BDs), Spring Cloud (Gateway, Eureka, OpenFeign)

Herramientas: Maven, Postman (Testing de API), Lombok

🏗️ Arquitectura de Microservicios

El sistema está compuesto por los siguientes servicios:

Servicio

Puerto

Descripción

API Gateway

8080

Punto de entrada único. Enruta peticiones y maneja CORS.

Eureka Server

8761

Registro y descubrimiento de servicios.

Auth Service

8081

Gestión de usuarios, roles y generación/validación de tokens JWT.

Restaurant Service

8082

Catálogo de restaurantes, mesas, menú y configuración.

Reserva Service

8083

Lógica core de reservas, validación de disponibilidad y horarios.

Notification Service

8084

Envío de emails (Gmail SMTP) y registro de notificaciones.

⚙️ Instalación y Ejecución

Prerrequisitos

Java 17 JDK

Docker & Docker Compose (Recomendado para BDs y RabbitMQ)

Maven

Pasos para levantar el entorno

Clonar el repositorio:

git clone [https://github.com/tu-usuario/reservas-restaurante-back.git](https://github.com/tu-usuario/reservas-restaurante-back.git)
cd reservas-restaurante-back


Levantar infraestructura (Docker):
Asegúrate de tener corriendo los contenedores necesarios (Postgres, Mongo, RabbitMQ).

docker-compose up -d


Configuración:

Revisar los archivos application.properties de cada servicio para asegurar que las credenciales de base de datos y RabbitMQ coincidan con tu entorno local.

Configurar la App Password de Gmail en notification-service para el envío de correos.

Compilar y Ejecutar:
Se recomienda iniciar los servicios en el siguiente orden:

Eureka Server

API Gateway

Auth Service, Restaurant Service, Reserva Service, Notification Service (en cualquier orden).

# Ejemplo para un servicio
cd auth-service
mvn spring-boot:run


🔌 Endpoints Principales

Todos los endpoints son accesibles a través del API Gateway (http://localhost:8080).

Autenticación (/api/auth)

POST /login: Iniciar sesión (Devuelve JWT + Roles).

POST /usuarios: Registrar nuevo cliente.

GET /usuarios/me: Obtener perfil del usuario actual (Requiere Token).

Restaurantes (/api/restaurant)

GET /restaurantes: Listar todos los restaurantes.

POST /restaurantes: Crear restaurante (SAGA: Crea local + Usuario Admin).

GET /restaurantes/{id}/menu: Ver menú público.

POST /restaurantes/{id}/mesas: Gestión de mesas (Solo Admin).

Reservas (/api/reserva)

POST /reservas: Crear nueva reserva (Valida disponibilidad).

GET /reservas/mias: Ver historial de reservas del usuario.

PUT /reservas/{id}: Confirmar/Rechazar reserva (Dispara notificación).

👥 Equipo de Desarrollo

Este proyecto fue desarrollado como Trabajo Final de la carrera de Desarrollo de Software.

Lautaro Aiello - Backend & DevOps Architect - GitHub

[Nombre de tu compañero] - Frontend Developer & Integración

📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

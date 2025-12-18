# 🍽️ Sistema de Gestión y Reservas de Restaurantes (Backend)

Backend de una plataforma distribuida para la **gestión integral de restaurantes y reservas en tiempo real**.  
El sistema está construido con una **arquitectura de microservicios escalable**, priorizando la consistencia de datos, la seguridad y la eficiencia operativa.

---

## 🚀 Características Principales

- Arquitectura distribuida basada en microservicios independientes.
- Gestión de transacciones distribuidas para garantizar la integridad de datos  
  (ej. creación de Restaurante + Usuario Administrador).
- Comunicación híbrida entre servicios:
  - **Síncrona**: OpenFeign para consultas directas y operaciones bloqueantes.
  - **Asíncrona**: RabbitMQ para procesos desacoplados como notificaciones y envío de emails.
- Seguridad robusta con Spring Security y JWT (JSON Web Tokens):
  - Autenticación y autorización centralizada.
  - Manejo de roles: `ADMIN`, `GESTOR`, `CLIENTE`.
- Persistencia políglota:
  - PostgreSQL para datos transaccionales.
  - MongoDB para logs de auditoría y notificaciones.
- Service Discovery & Routing con Eureka Server y Spring Cloud Gateway.

---

## 🛠️ Tech Stack

- **Lenguaje**: Java 17  
- **Framework**: Spring Boot 3  
- **Bases de Datos**: PostgreSQL, MongoDB  
- **Mensajería**: RabbitMQ  
- **Seguridad**: Spring Security, JWT (jjwt)  
- **Infraestructura**: Docker, Docker Compose, Spring Cloud  
- **Herramientas**: Maven, Postman, Lombok  

---

## 🏗️ Arquitectura de Microservicios

| Servicio             | Puerto | Descripción |
|----------------------|--------|-------------|
| API Gateway          | 8080   | Punto de entrada único. Enrutamiento y CORS |
| Eureka Server        | 8761   | Registro y descubrimiento de servicios |
| Auth Service         | 8081   | Gestión de usuarios, roles y JWT |
| Restaurant Service   | 8082   | Restaurantes, mesas, menú y configuración |
| Reserva Service      | 8083   | Lógica core de reservas |
| Notification Service | 8084   | Envío de emails y notificaciones |

---

## ⚙️ Instalación y Ejecución

### Prerrequisitos

- Java 17 JDK  
- Docker y Docker Compose (recomendado)  
- Maven  

### Pasos para levantar el entorno

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/reservas-restaurante-back.git
   cd reservas-restaurante-back


2. **Levantar infraestructura (Docker):**
Asegúrate de tener corriendo los contenedores necesarios (Postgres, Mongo, RabbitMQ).

docker-compose up -d
 

Configuración:

- Revisar los archivos application.properties de cada servicio para asegurar que las credenciales de base de datos y RabbitMQ coincidan con tu entorno local.
- Verificar credenciales de bases de datos y RabbitMQ.
- Configurar la App Password de Gmail en notification-service para el envío de correos.

Compilar y Ejecutar los servicios:
Se recomienda iniciar los servicios en el siguiente orden:

1. Eureka Server

2. API Gateway

3. Auth Service, Restaurant Service, Reserva Service, Notification Service

# Ejemplo para un servicio
cd auth-service
mvn spring-boot:run


🔌 Endpoints Principales

Todos los endpoints son accesibles a través del API Gateway
📍 http://localhost:8080

🔐 Autenticación (/api/auth)

POST /login – Iniciar sesión (JWT + roles)

POST /usuarios – Registrar nuevo cliente

GET /usuarios/me – Obtener perfil del usuario autenticado

🍴 Restaurantes (/api/restaurant)

GET /restaurantes – Listar restaurantes

POST /restaurantes – Crear restaurante
(SAGA: Restaurante + Usuario Admin)

GET /restaurantes/{id}/menu – Ver menú público

POST /restaurantes/{id}/mesas – Gestión de mesas (Admin)

📅 Reservas (/api/reserva)

POST /reservas – Crear reserva

GET /reservas/mias – Historial de reservas del usuario

PUT /reservas/{id} – Confirmar / Rechazar reserva (dispara notificación)

👥 Equipo de Desarrollo  

Este proyecto fue desarrollado como Trabajo Final de la carrera de Desarrollo de Software.

Lautaro Aiello - FullStack - GitHub

Santiago Cacciabue - FullStack - GitHub


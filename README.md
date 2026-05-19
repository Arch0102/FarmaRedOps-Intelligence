# FarmaRed: Ops-Intelligence

**FarmaRed: Ops-Intelligence** es una aplicación backend desarrollada para la materia **Desarrollo Empresarial** de la Universidad Santo Tomás, sede Villavicencio.

El proyecto consiste en un gestor de inventario farmacéutico y abastecimiento predictivo orientado al control de medicamentos, lotes, proveedores, movimientos de inventario, alertas de stock crítico y métricas operativas para dashboard.

## Integrantes

- Santiago Ortiz Ochoa
- Nicolás Mancera León
- Juan Felipe Rocha
- Juan Felipe Cardona

## Objetivo del proyecto

Desarrollar una plataforma que permita optimizar la gestión de inventarios farmacéuticos mediante trazabilidad de medicamentos, control de lotes, registro de movimientos, alertas de abastecimiento y métricas de apoyo para la toma de decisiones.

## Stack tecnológico

- Java 21 LTS
- Spring Boot 3.5.13
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Security 6
- JWT
- Bean Validation / Jakarta Validation
- MapStruct
- SpringDoc / OpenAPI
- Maven
- Git y GitHub
- IntelliJ IDEA

## Base de datos

Motor de base de datos:

PostgreSQL

Base de datos local esperada:

farmared_db

## Variables de entorno

El proyecto usa variables de entorno para evitar subir credenciales reales al repositorio.

Crear un archivo `.env` local basado en `.env.example`:

DB_URL=jdbc:postgresql://localhost:5432/farmared_db
DB_USER=postgres
DB_PASSWORD=tu_password_local
SERVER_PORT=8080
JWT_SECRET=clave_jwt_de_desarrollo

El archivo `.env` no debe subirse a GitHub.

## Ejecución local

1. Crear la base de datos en PostgreSQL:

CREATE DATABASE farmared_db;

2. Configurar las variables de entorno locales.

3. Ejecutar el proyecto desde IntelliJ IDEA o con Maven:

./mvnw spring-boot:run

En Windows:

mvnw.cmd spring-boot:run

## Estado actual

Configuración inicial del backend con:

- Proyecto Spring Boot limpio.
- Java 21 configurado.
- Spring Boot 3.x configurado.
- Conexión inicial a PostgreSQL.
- Estructura base preparada para arquitectura por capas.

## Ramas principales

main
dev
feature/backend-architecture

## Flujo de trabajo Git

El equipo trabajará con ramas por funcionalidad:

feature/nombre-funcionalidad

Los cambios deben integrarse mediante Pull Request hacia `dev`.

La rama `main` se reserva para versiones estables del proyecto.

## Convención de commits

Ejemplos de commits válidos:

chore: initialize clean Spring Boot backend
feat: add inventory movement module
feat: implement jwt authentication
docs: update README setup instructions
fix: correct database configuration

Evitar commits como:

avance
cambios
prueba
final

# BFF — Sistema de Gestión de Usuarios y Roles

## 1. Descripción

Backend for Frontend (BFF) desarrollado con Spring Boot y Java 17 para centralizar el acceso a las operaciones de gestión de usuarios y roles del sistema de veterinaria.

El BFF expone endpoints REST y GraphQL, y se integra con Azure Functions desarrolladas en Java. La solución incorpora Azure Event Grid para el procesamiento orientado a eventos y utiliza Oracle como base de datos principal de la gestión de usuarios y roles.

## 2. Arquitectura

La solución utiliza los siguientes componentes:

- **BFF:** aplicación Spring Boot desplegada en AWS EC2.
- **REST:** endpoints para consultar y administrar usuarios y roles.
- **GraphQL:** consultas y mutaciones para recuperar y modificar datos.
- **Azure Functions Java:** ejecutan las operaciones de gestión de usuarios y roles.
- **Azure Event Grid:** permite publicar y procesar eventos relacionados con la asignación predeterminada y la eliminación de roles.
- **Oracle:** persistencia principal de usuarios y roles.
- **Azure Cosmos DB:** almacenamiento de registros de trazabilidad en un flujo complementario de eventos.

El BFF centraliza el acceso a las funcionalidades expuestas por las Azure Functions. No se considera que cada carpeta del repositorio represente un microservicio desplegado.

## 3. Tecnologías

- Java 17
- Spring Boot
- Spring for GraphQL
- Maven
- Docker
- AWS EC2
- Azure Functions Java
- Azure Event Grid
- Oracle Database
- Azure Cosmos DB

## 4. Despliegue

El BFF se encuentra desplegado en una instancia AWS EC2 y ejecuta la aplicación mediante Docker.

**Puerto de la aplicación:** `8080`

**Imagen Docker de la versión desplegada:** `ms-bff:2.2`

**Contenedor:** `ms-bff-2.2`

### URL base

```text
http://13.216.7.174:8080
```

La disponibilidad de la URL depende del estado de la instancia EC2, el contenedor, las reglas de red y la configuración del entorno.

### Construcción local

Desde la raíz del proyecto:

```bash
./mvnw clean package -DskipTests
```

En Windows PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### Construcción de la imagen Docker

```bash
docker build -t ms-bff:2.2 .
```

### Ejecución local mediante Docker

```bash
docker run -d --name ms-bff-2.2 --restart unless-stopped -p 8080:8080 ms-bff:2.2
```

El Dockerfile activa el perfil Spring `ec2`. Antes de ejecutar una nueva instancia, verifica que el puerto 8080 esté disponible y que no exista otro contenedor con el mismo nombre.

### Verificación del contenedor

```bash
docker ps
docker logs --tail 40 ms-bff-2.2
```

## 5. Configuración por entorno

La aplicación utiliza archivos de configuración de Spring para separar las direcciones locales de las direcciones de los servicios desplegados.

### Entorno local

El archivo `application.properties` utiliza direcciones locales para las Azure Functions:

```properties
server.port=8080
azure.functions.roles.url=http://localhost:7071/api/rolesJava
azure.functions.usuarios.url=http://localhost:7071/api/usuariosJava
azure.functions.usuarios.graphql.url=http://localhost:7071/api/usuariosGraphQL
azure.functions.roles.graphql.url=http://localhost:7071/api/rolesGraphQL
```

### Entorno EC2

El archivo `application-ec2.properties` utiliza los endpoints publicados de Azure Functions:

```properties
azure.functions.roles.url=https://fn-veterinaria-citas-java.azurewebsites.net/api/rolesJava
azure.functions.usuarios.url=https://fn-veterinaria-citas-java.azurewebsites.net/api/usuariosJava
azure.functions.usuarios.graphql.url=https://fn-veterinaria-citas-java.azurewebsites.net/api/usuariosGraphQL
azure.functions.roles.graphql.url=https://fn-veterinaria-citas-java.azurewebsites.net/api/rolesGraphQL
```

Las configuraciones pueden cambiar según el entorno. No se deben almacenar contraseñas, claves privadas, tokens ni cadenas de conexión con credenciales en el repositorio.

## 6. Endpoints REST

Todos los endpoints REST descritos en esta sección utilizan la siguiente base:

```text
http://13.216.7.174:8080
```

### 6.1 Usuarios

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/bff/usuarios` | Consulta los usuarios registrados. |
| POST | `/bff/usuarios` | Crea un usuario. |
| PUT | `/bff/usuarios` | Actualiza los datos de un usuario mediante el identificador incluido en el cuerpo. |

#### Consultar usuarios

```http
GET /bff/usuarios
```

Ejemplo de respuesta:

```json
[
  {
    "id": 93,
    "nombre": "Prueba GraphQL Actualizada",
    "email": "prueba.consumidor.20261010@test.cl",
    "rolId": 3,
    "rol": "Veterinario"
  }
]
```

Los registros devueltos dependen de los datos existentes en la base de datos.

#### Crear usuario

```http
POST /bff/usuarios
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "nombre": "Usuario Demo REST",
  "email": "demo.rest@example.com",
  "password": "CAMBIAR_POR_UNA_CLAVE_DE_PRUEBA"
}
```

Cuando se crea un usuario mediante el flujo REST probado, el sistema asigna el rol predeterminado `ROL_PRIMARIO`, con identificador `88`, sin necesidad de incluir `rolId` en el cuerpo.

#### Actualizar usuario

```http
PUT /bff/usuarios
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "id": 93,
  "nombre": "Usuario Demo Actualizado",
  "email": "demo.rest@example.com",
  "password": "CAMBIAR_POR_UNA_CLAVE_DE_PRUEBA",
  "rolId": 3
}
```

El identificador y los demás valores deben corresponder a un usuario existente. La respuesta debe verificarse mediante una nueva consulta GET.

### 6.2 Roles

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/bff/roles` | Consulta los roles disponibles. |
| POST | `/bff/roles` | Crea un rol. |
| DELETE | `/bff/roles/{id}` | Solicita la eliminación de un rol mediante Event Grid. |

#### Consultar roles

```http
GET /bff/roles
```

#### Crear rol

```http
POST /bff/roles
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "nombre": "ROL_DEMO"
}
```

#### Solicitar eliminación de rol

```http
DELETE /bff/roles/92
```

La operación probada devuelve una respuesta que indica que la solicitud fue enviada a Event Grid y que el procesamiento es asíncrono.

La eliminación del rol y la reasignación de los usuarios asociados deben comprobarse consultando posteriormente los usuarios y los roles.

**Importante:** no eliminar el rol `ROL_PRIMARIO`, identificado con el ID `88`, ya que constituye el rol predeterminado de la solución.

## 7. Endpoint GraphQL

El endpoint GraphQL del BFF es:

```text
POST /graphql
```

URL completa:

```text
http://13.216.7.174:8080/graphql
```

Las solicitudes se envían mediante HTTP POST con un cuerpo JSON que contiene la consulta o mutación GraphQL.

### 7.1 Consultar usuarios

```json
{
  "query": "{ usuarios { id nombre email rolId rol } }"
}
```

Esta consulta solicita el identificador, nombre, correo y rol de cada usuario.

### 7.2 Actualizar usuario

```json
{
  "query": "mutation { actualizarUsuario(id: \"93\", nombre: \"Prueba GraphQL Video\", email: \"prueba.consumidor.20261010@test.cl\", password: \"CAMBIAR_POR_UNA_CLAVE_DE_PRUEBA\", rolId: \"3\") { id nombre email rolId rol } }"
}
```

Esta mutación actualiza el usuario indicado y devuelve los campos solicitados. El resultado debe verificarse con una consulta posterior.

### 7.3 Operaciones disponibles en el esquema

El esquema GraphQL contempla las siguientes mutaciones:

| Mutación | Propósito |
|---|---|
| `crearUsuario` | Crear un usuario. |
| `actualizarUsuario` | Actualizar un usuario. |
| `eliminarUsuario` | Eliminar un usuario. |
| `crearRol` | Crear un rol. |
| `actualizarRol` | Actualizar un rol. |
| `eliminarRol` | Eliminar un rol. |

Las firmas y argumentos requeridos se definen en `src/main/resources/graphql/schema.graphqls`.

La mutación `crearUsuario` requiere el argumento `rolId` según el esquema actual. Por lo tanto, la creación GraphQL no demuestra por sí sola la asignación automática por omisión del rol. La asignación automática se verificó mediante el endpoint REST de creación.

## 8. Arquitectura orientada a eventos

La solución integra Azure Event Grid para publicar y procesar eventos relacionados con la gestión de roles.

### Asignación del rol predeterminado

El flujo REST de creación de usuarios asigna el rol predeterminado `ROL_PRIMARIO` (ID `88`) en Oracle y publica un evento asociado al proceso.

La publicación y la entrega del evento se supervisan mediante las métricas y suscripciones configuradas en Azure Event Grid.

### Eliminación de un rol y reasignación

El flujo probado es:

1. Crear un rol de prueba.
2. Asociar el rol a un usuario existente.
3. Solicitar la eliminación del rol desde el BFF.
4. Publicar el evento en Azure Event Grid.
5. Procesar la eliminación de manera asíncrona.
6. Reasignar al usuario afectado al rol predeterminado `ROL_PRIMARIO` (ID `88`).
7. Consultar usuarios y roles para comprobar el resultado.

Las métricas de Event Grid aportan evidencia de actividad y entrega de eventos. La comprobación del estado final de usuarios y roles permite validar el resultado funcional.

## 9. Trazabilidad con Azure Cosmos DB

La solución contempla un flujo complementario de trazabilidad mediante eventos y Azure Cosmos DB.

Las Azure Functions de procesamiento de eventos registran información de trazabilidad, incluyendo el tipo de evento, su estado de procesamiento, el origen y los datos relevantes.

Este flujo de trazabilidad es complementario a la persistencia principal de usuarios y roles en Oracle. No debe confundirse con el proceso de reasignación de roles.

## 10. Pruebas realizadas

Durante la validación de la solución se realizaron las siguientes pruebas:

- Consulta de usuarios mediante REST.
- Creación de usuarios mediante REST.
- Asignación automática del rol predeterminado durante la creación REST.
- Consulta de roles mediante REST.
- Creación de un rol de prueba.
- Asignación del rol de prueba a un usuario.
- Solicitud de eliminación de un rol mediante Event Grid.
- Verificación de la reasignación del usuario al rol predeterminado.
- Consulta GraphQL de usuarios.
- Actualización de un usuario mediante GraphQL.
- Verificación de persistencia mediante consulta REST.
- Revisión de las métricas de las suscripciones de Azure Event Grid.

Las evidencias visuales y capturas de pantalla deben conservarse en el directorio de documentación correspondiente.

## 11. Estructura principal del proyecto

```text
ms-bff/
├── .mvn/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           ├── application.properties
│           ├── application-ec2.properties
│           └── graphql/
│               └── schema.graphqls
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

Los directorios de compilación, archivos de registro, imágenes Docker exportadas y archivos con credenciales no forman parte del código fuente que debe publicarse en el repositorio.

## 12. Recomendaciones de seguridad

- No versionar archivos `.pem`, `.key`, wallets ni credenciales.
- Mantener los archivos `.env` y configuraciones locales sensibles fuera de Git.
- No publicar contraseñas de bases de datos ni claves de Azure.
- Usar credenciales de prueba durante las demostraciones.
- No incluir archivos de registro sin revisar su contenido.
- Evitar publicar datos personales reales en ejemplos.
- Rotar cualquier credencial que haya sido expuesta accidentalmente.

## 13. Repositorio

Repositorio GitHub:

https://github.com/CrisDebug/bff-usuariosyroles-cloudnative2

Este repositorio contiene el código fuente del BFF, la configuración de construcción y el esquema GraphQL asociado al sistema de gestión de usuarios y roles.
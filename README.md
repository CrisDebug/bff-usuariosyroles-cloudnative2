# BFF - Usuarios y Roles Cloud Native

Backend for Frontend (BFF) desarrollado con **Spring Boot** para centralizar el acceso a las operaciones de **Usuarios y Roles**.

El BFF actúa como una capa intermedia entre el cliente y las Azure Functions, ocultando las URLs internas y permitiendo además realizar operaciones de composición entre dominios.

## Arquitectura

```text
Cliente
   |
   v
BFF - Spring Boot
   |
   +-------------> usuariosJava ---> Oracle
   |
   +-------------> rolesJava ------> Oracle
   |
   +-------------> Composición Usuarios + Roles
```

## Responsabilidades

* Centralizar el acceso a Usuarios y Roles.
* Delegar las operaciones CRUD a las Azure Functions correspondientes.
* Adaptar las solicitudes externas a las interfaces internas de las Functions.
* Componer información de Usuarios y Roles en una única respuesta.
* Mantener las URLs de las Functions mediante configuración externa.

## Tecnologías

* Java
* Spring Boot
* Spring Web / RestClient
* Maven
* Docker
* Azure Functions
* Oracle Database
* Amazon EC2

## Endpoints disponibles

### Usuarios

| Método | Endpoint             | Descripción              |
| ------ | -------------------- | ------------------------ |
| GET    | `/bff/usuarios`      | Lista todos los usuarios |
| POST   | `/bff/usuarios`      | Crea un usuario          |
| PUT    | `/bff/usuarios`      | Actualiza un usuario     |
| DELETE | `/bff/usuarios/{id}` | Elimina un usuario       |

### Roles

| Método | Endpoint          | Descripción           |
| ------ | ----------------- | --------------------- |
| GET    | `/bff/roles`      | Lista todos los roles |
| POST   | `/bff/roles`      | Crea un rol           |
| PUT    | `/bff/roles`      | Actualiza un rol      |
| DELETE | `/bff/roles/{id}` | Elimina un rol        |

### Usuarios + Roles

| Método | Endpoint                  | Descripción                            |
| ------ | ------------------------- | -------------------------------------- |
| GET    | `/bff/usuarios-con-roles` | Obtiene usuarios y sus roles asociados |

Este endpoint permite al BFF consultar la información de Usuarios y Roles y entregar una respuesta compuesta.

### Ejemplo de respuesta

```json
{
  "usuarios": [
    {
      "id": 1,
      "nombre": "Juan Perez",
      "email": "juan.perez@test.cl",
      "rolId": 1,
      "rol": "Administrador Principal"
    },
    {
      "id": 4,
      "nombre": "Maria Lopez Actualizada",
      "email": "maria.actualizada@test.cl",
      "rolId": 1,
      "rol": "Administrador Principal"
    }
  ],
  "roles": [
    {
      "id": 1,
      "nombre": "Administrador Principal"
    },
    {
      "id": 3,
      "nombre": "Veterinario"
    },
    {
      "id": 21,
      "nombre": "Asistente Veterinario Senior"
    },
    {
      "id": 23,
      "nombre": "Administrador de Agenda"
    }
  ]
}
```

## Decisión de arquitectura

Las operaciones se agrupan por **dominio funcional** y no por operación individual.

Por este motivo se utilizan:

* `usuariosJava` para las operaciones relacionadas con Usuarios.
* `rolesJava` para las operaciones relacionadas con Roles.

Esta organización permite mantener **contextos acotados**, reducir la complejidad y evitar una proliferación innecesaria de Azure Functions.

El BFF funciona como una capa de:

* Integración.
* Adaptación.
* Composición.
* Exposición de APIs orientadas al cliente.

## Configuración

Las URLs de las Azure Functions no están escritas directamente en los controladores.

Se configuran mediante propiedades de Spring:

```text
application.properties
application-ec2.properties
```

Esto permite utilizar diferentes configuraciones para ambientes locales y desplegados.

Ejemplo conceptual:

```properties
azure.functions.usuarios.url=...
azure.functions.roles.url=...
```

Las URLs reales y credenciales sensibles no se incluyen en el repositorio.

## Ejecución local

Para ejecutar el proyecto:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

El BFF queda disponible en:

```text
http://localhost:8080
```

## Docker

Construir la imagen:

```bash
docker build -t ms-bff:1.0 .
```

Ejecutar el contenedor:

```bash
docker run -d -p 8080:8080 --name ms-bff ms-bff:1.0
```

## Despliegue

El BFF se ejecuta como un **contenedor Docker sobre Amazon EC2**.

La comunicación con las operaciones de Usuarios y Roles se realiza mediante las Azure Functions correspondientes.

Flujo general:

```text
Cliente
   |
   v
Amazon EC2
   |
   v
Docker - ms-bff
   |
   +----> Azure Function usuariosJava ----> Oracle
   |
   +----> Azure Function rolesJava -------> Oracle
```

## Estado del proyecto

* [x] CRUD de Usuarios implementado.
* [x] CRUD de Roles implementado.
* [x] Persistencia en Oracle verificada.
* [x] Eliminación de usuarios mediante `?id={id}`.
* [x] Eliminación de roles mediante `?id={id}`.
* [x] Endpoint de composición Usuarios + Roles implementado.
* [x] Endpoint `/bff/usuarios-con-roles` probado localmente.
* [x] BFF empaquetado con Maven.
* [x] Imagen Docker generada.
* [x] BFF desplegado en EC2.
* [x] Endpoint de composición probado en EC2.
* [x] Código respaldado en GitHub.

## Repositorio

Repositorio GitHub:

`https://github.com/CrisDebug/bff-usuariosyroles-cloudnative2`

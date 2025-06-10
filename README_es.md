# API REST para Gestión de Usuarios y Roles

**Descripción del proyecto:**  
API REST desarrollada con Spring Boot 3.5.0, Java 24 y MongoDB 8.0.10 para gestionar Usuarios y Roles con funcionalidad CRUD completa.


- Se aprovechó el modelo documental de MongoDB para almacenar directamente los IDs de roles como un arreglo dentro del documento Usuario.

- Esto elimina la necesidad de manejar una tabla o colección adicional para la relación muchos a muchos.

- Simplifica las consultas y operaciones CRUD, haciendo uso de las ventajas de MongoDB para manejar datos embebidos o referencias.

- Este diseño facilita la escalabilidad y flexibilidad, especialmente cuando la cantidad de roles por usuario puede variar.

---

Para la versión en ingles, ve a [README.md](README.md)

## 📋 Funcionalidades

- **Entidades:**
  - `User`  
    - `Id` (generado automáticamente)  
    - `Firstname`  
    - `Lastname paternal`  
    - `Lastname Maternal`  
    - `Roles` (Lista de ids de roles)

  - `Role`  
    - `Id` (generado automáticamente)  
    - `Key` (Usado para facilitar inserciones sin tener que memorizar id's numericos)  
    - `Name`  

- **Endpoints:**
  - **GET**  
    - Consultar todos los user/roles
    - Consultar user/role por ID
    - Consultar user/role por Nombre  

  - **POST**  
    - Insertar user o role con ID automático  

  - **PUT**  
    - Actualizar user o role  

  - **DELETE**  
    - Eliminar user o role  

---

## 🚀 Tecnologías Usadas

- Spring Boot 3.5.0  
- Java 24  
- MongoDB 8.0.10  

---

## 📦 Estructura del Proyecto

spring-mongo-api/
├── src/

    Se aprovechó el modelo documental de MongoDB para almacenar directamente los IDs de roles como un arreglo dentro del documento Usuario.

    Esto elimina la necesidad de manejar una tabla o colección adicional para la relación muchos a muchos.

    Simplifica las consultas y operaciones CRUD, haciendo uso de las ventajas de MongoDB para manejar datos embebidos o referencias.

    Este diseño facilita la escalabilidad y flexibilidad, especialmente cuando la cantidad de roles por usuario puede variar.

├── db/ # Archivos JSON para importar datos iniciales
├── postman/ # Colecciones Postman para pruebas
├── README.md # Información del proyecto en inglés
├── README_es.md # Información del proyecto en español
└── ...


---

## ⚙️ Ejecutando el Proyecto

1. Asegúrate de tener MongoDB 8.0.10 corriendo localmente o configura la URI en `application.yml`.

2. Compila y ejecuta la aplicación:
```bash
./mvnw clean install
./mvnw spring-boot:run

3.La API estará disponible en http://localhost:8080.

---

## 🧪 Importar datos Iniciales

Ejecuta estos comandos para cargar roles y usuarios:

mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=user --file=db/user.json --jsonArray
mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=role --file=db/role.json --jsonArray
mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=collectionSequence --file=db/collectionSequence.json --jsonArray

## 📬 Colección Postman

Importa la colección Postman desde:

`postman/spring-mongo-api.postman_collection.json`

Para probar los endpoints de la API.

---

## 📌 Resumen de Endpoints

Documentación más extensa de la API corriendo el proyecto y navegando a:  
[text](http://localhost:8080/swagger-ui/index.html)

---
| Método | Endpoint                           | Descripción                  |
| ------ | ---------------------------------- | ---------------------------- |
| GET    | `api/users`                        | Consultar todos los usuarios |
| GET    | `api/users/{id}`                   | Consultar usuario por ID     |
| GET    | `api/users/filter?name ={nombre}`  | Consultar usuario por nombre |
| POST   | `api/users`                        | Crear usuario                |
| PUT    | `api/users/{id}`                   | Actualizar usuario           |
| DELETE | `api/users/{id}`                   | Eliminar usuario             |
| GET    | `api/roles`                        | Consultar todos los roles    |
| GET    | `api/roles/{id}`                   | Consultar rol por ID         |
| GET    | `api/roles/filter?name ={nombre}`  | Consultar role por nombre    |
| POST   | `api/roles`                        | Crear rol                    |
| PUT    | `api/roles/{id}`                   | Actualizar rol               |
| DELETE | `api/roles/{id}`                   | Eliminar rol                 |



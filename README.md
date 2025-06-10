# API REST to manage Users and ROles

**Project description:**  
 REST API developed with Spring Boot 3.5.0, Java 24, and MongoDB 8.0.10 to manage Users and Roles with full CRUD functionality.

- The document model of MongoDB was leveraged to store role IDs directly as an array within the User document.
- This removes the need to manage an additional table or collection for the many-to-many relationship.
- It simplifies queries and CRUD operations by taking advantage of MongoDB’s ability to handle embedded data or references.
- This design improves scalability and flexibility, especially when the number of roles per user may vary.
---

For spanish version, go to [README_es.md](README_es.md)

## 📋 Features

- **Entities:**
  - `User`  
    - `Id` (auto-generated)  
    - `Firstname`  
    - `Lastname (paternal)`  
    - `Lastname (maternal)`  
    - `Roles` (List of role IDs)

  - `Role`  
    - `Id` (auto-generated)  
    - `Key` (Used to simplify insertions without needing to memorize numeric IDs)  
    - `Name`  

- **Endpoints:**
  - **GET**  
    - Retrieve all users/roles
    - Retrieve user/role by ID
    - Retrieve user/role by name

  - **POST**  
    - Insert a user or role with auto-generated ID  

  - **PUT**  
    - Update a user or role  

  - **DELETE**  
    - Delete user or role  

---

## 🚀 Technologies Used

- Spring Boot 3.5.0  
- Java 24  
- MongoDB 8.0.10  

---

## 📦 Project Structure
```
spring-mongo-api/
├── src/
|
├── db/ # JSON files to import initial data
|
├── postman/ # Colecciones Postman para pruebas
|
├── README.md # Project information (English)
|
├── README_es.md # Project information (Spanish)
|
└── ...
```

---

## ⚙️ Project execution

1. Run MongoDB 8.0.10 locally or configure the URI in `application.yml`.

2. Compile and execute the app:
```bash
./mvnw clean install
./mvnw spring-boot:run

```
Or running '.../src/main/java/com/josegomez/spring_mongo_api/SpringMongoApiApplication.java' in your favourite IDE.

The API will be available at http://localhost:8080.

---

## 🧪 Import init data

To seed the database with test data, a DataInitializer class was created that runs automatically at application startup to load the necessary data. However, JSON files are also provided in the db/ folder so you can manually import the data if you prefer or want to easily reset the database using mongoimport.

Execute the following commands to import roles, users, and the ID sequence from JSON files:

```
mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=user --file=db/user.json --jsonArray
mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=role --file=db/role.json --jsonArray
mongoimport --uri="mongodb://localhost:27017/spring_mongo_api_db" --collection=collectionSequence --file=db/collectionSequence.json --jsonArray
```

Were created 5 roles, 15 users, and a collection named collectionSequence to manage auto-generated IDs.

It is not possible to delete roles that are currently in use. FOr this reason, the roles with keys "monitor" and "editor" were not assigned to any user, allowing effective testing of role deletion.

## 📬 Postman collection

Load the following collection on your Potsman app:

`postman/spring-mongo-api.postman_collection.json`

to test theAPI endpoints.

---

## 📌 Endpoints Summary

Swagger is integrated to provide a more detailed API documentation.  

[text](http://localhost:8080/swagger-ui/index.html)

---
| Method | Endpoint                          | Description               |
| ------ | --------------------------------  | ------------------------- |
| GET    | `api/users`                       | Retrieve all users        |
| GET    | `api/users/{id}`                  | Retrieve user by ID       |
| GET    | `api/users/filter?name={name}`    | Retrieve user by name     |
| POST   | `api/users`                       | Create user               |
| PUT    | `api/users/{id}`                  | Update user               |
| DELETE | `api/users/{id}`                  | Delete user               |
| GET    | `api/roles`                       | Retrieve all roles        |
| GET    | `api/roles/{id}`                  | Retrieve role by ID       |
| GET    | `api/roles/filter?name={name}`    | Retrieve role by name     |
| POST   | `api/roles`                       | Create role               |
| PUT    | `api/roles/{id}`                  | Update role               |
| DELETE | `api/roles/{id}`                  | Delete role               |


# 🛒 Spring Boot E-Commerce Backend (JWT + Role Based Auth)

A production-style REST API for an e-commerce platform built using **Spring Boot 3**, **Spring Security**, **JWT Authentication**, and **MySQL**.

This project demonstrates how modern backend systems handle authentication, authorization and protected APIs.

---

## 🚀 Features

* User Registration & Login
* JWT Authentication
* Role Based Authorization (CUSTOMER / SELLER / ADMIN)
* Secure APIs using Spring Security Filter Chain
* Product Creation (Protected Route)
* MySQL Database Integration
* Password Encryption using BCrypt
* Stateless Authentication (No Sessions)

---

## 🧱 Tech Stack

* Java 17
* Spring Boot 3
* Spring Security
* JWT (io.jsonwebtoken)
* Spring Data JPA (Hibernate)
* MySQL
* Lombok
* Maven

---

## 🔐 Authentication Flow

1. User registers
2. User logs in
3. Server generates JWT token
4. Client sends token in header

```
Authorization: Bearer <token>
```

5. Protected APIs become accessible

---

## 📡 API Endpoints

### Auth

POST `/api/auth/register`
POST `/api/auth/login`

### Test Protected Route

GET `/api/test/hello`

### Product (Requires SELLER role)

POST `/api/products`

---

## 🧪 Example Request

Register:

```
POST /api/auth/register
{
  "name": "shaan",
  "email": "shaan@gmail.com",
  "password": "123456",
  "role": "CUSTOMER"
}
```

---

## 🧠 What This Project Demonstrates

* How JWT authentication actually works internally
* How Spring Security filters requests
* How stateless authentication replaces sessions
* How real backend APIs protect endpoints

---

## ▶ Run Locally

1. Configure MySQL in `application.yml`
2. Create database `ecommerce`
3. Run:

```
mvn spring-boot:run
```

Server starts at:

```
http://localhost:8080
```

---

## 👨‍💻 Author

Muhammed Shaan

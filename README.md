# 📘 Nexus Portfolio Platform

**A Scalable, Microservices-Based Portfolio Management System.**

This project is a backend platform designed to manage professional developer portfolios. It uses a **Microservices Architecture** with Spring Boot, Spring Cloud, and PostgreSQL, featuring a centralized API Gateway and Service Discovery.

---

## **🚀 Tech Stack**

* **Language:** Java 21
* **Framework:** Spring Boot 3.4.x
* **Microservices:** Spring Cloud Gateway (Reactive/Netty), Netflix Eureka
* **Database:** PostgreSQL (Relational Schema)
* **ORM:** Hibernate / Spring Data JPA
* **Build Tool:** Maven

---

## **🏗 Architecture**

The system follows a **Client-Side Load Balanced** architecture.

### **1. Service Registry (The "Phonebook")**

* **Port:** `8761`
* **Role:** Auto-discovery server. All microservices register themselves here.

### **2. API Gateway (The "Front Door")**

* **Port:** `8080`
* **Role:** Reactive, non-blocking Gateway (Netty). Handles routing and load balancing.
* **Route:** `/api/portfolio/**` → `lb://PORTFOLIO-SERVICE`

### **3. Portfolio Service (The "Core Logic")**

* **Port:** `8081` (Dynamic)
* **Role:** Manages Users, Projects, Skills, Education, etc.
* **Architecture:** Layered (Controller -> Service -> Repository -> Database).

---

## **💾 Database Schema (Relational)**

We use a **Profile-Centric** model. The `Profile` table is the root, and all other tables are children linked via Foreign Keys.

* **`profile`**: The root user (Name, Bio, Theme, Social Links).
* **`project`**: Linked to Profile (Title, TechStack, URLs).
* **`skill`**: Linked to Profile (Name, Rating, Category).
* **`education`**: Linked to Profile (Degree, Institution, Dates).
* **`experience`**: Linked to Profile (Company, Role, Dates).
* **`certification`**: Linked to Profile (Name, Url, Issue Date).

> **Note:** All entities extend a `BaseEntity` which automatically tracks `createdAt` and `updatedAt` timestamps.

---

## **🔌 API Documentation**

All requests must go through the **Gateway (Port 8080)**.
**Base URL:** `http://localhost:8080/api/portfolio`

### **1. Profile Management**

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/profiles` | **Create User.** Returns the new Profile ID. |
| `GET` | `/profiles/{id}` | **Get Full Portfolio.** Returns Profile + all Projects/Skills. |
| `GET` | `/profiles/user/{username}` | **Public Lookup.** Find a portfolio by friendly username. |
| `PATCH` | `/profiles/{id}` | **Partial Update.** Update bio, theme, or links only. |
| `DELETE` | `/profiles/{id}` | **Delete User.** Cascades delete to all dependent data. |

### **2. Content Management (Add Items)**

All these endpoints require the `profileId` in the URL.

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/profiles/{id}/projects` | Add a Project. |
| `POST` | `/profiles/{id}/skills` | Add a Skill (Rated 1-10). |
| `POST` | `/profiles/{id}/education` | Add Education history. |
| `POST` | `/profiles/{id}/experience` | Add Job Experience. |
| `POST` | `/profiles/{id}/certifications` | Add Certifications. |

---

## **🛠 Setup & Run Instructions**

### **Prerequisites**

1. **PostgreSQL** installed and running. Create a DB named `nexus_db`.
2. **Java 21** installed.
3. **Maven** installed.

### **Step 1: Start the Database**

Ensure PostgreSQL is running on port `5432`.

* *Config:* `src/main/resources/application.properties` inside `portfolio-service`.

### **Step 2: Run the Microservices (In Order)**

Open 3 separate terminals:

1. **Service Registry:**
```bash
cd service-registry
mvn spring-boot:run

```


*Wait for "Started ServiceRegistryApplication"*
2. **Portfolio Service:**
```bash
cd portfolio-service
mvn spring-boot:run

```


*Wait for "Registered with Eureka"*
3. **API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run

```


*Wait for "Netty started on port 8080"*

---

## **✅ Best Practices Implemented**

1. **Standardized API Response:**
Every API returns a consistent JSON structure:
```json
{
  "success": true,
  "message": "Operation Successful",
  "data": { ... },
  "timestamp": "2026-01-16T..."
}

```


2. **Global Exception Handling:**
Centralized handler (`@RestControllerAdvice`) converts Java exceptions into clean 400/500 JSON errors.
3. **DTO/JSON Safety:**
Used `@JsonIgnore` to prevent infinite recursion loops between Parent and Child entities.
4. **Transactional Logic:**
`@Transactional` services ensure data integrity during complex save operations.

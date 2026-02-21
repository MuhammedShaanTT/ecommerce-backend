# 🛍️ ShopVerse — E-Commerce Platform

A full-stack e-commerce platform built with **Spring Boot** (backend) and **React + Vite** (frontend).

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────┐
│  React Frontend │────▶│  Spring Boot API  │────▶│  MySQL  │
│  (Port 5173)    │     │  (Port 4000)      │     │         │
└─────────────────┘     └──────────────────┘     └─────────┘
                              │
                   ┌──────────┴──────────┐
                   │   JWT Auth + RBAC   │
                   │  BUYER│SELLER│ADMIN  │
                   └─────────────────────┘
```

## ⚡ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3, Spring Security, JPA/Hibernate |
| Frontend | React 18, Vite, React Router, Axios |
| Database | MySQL 8 |
| Auth | JWT (JSON Web Tokens) |
| Docs | Swagger / OpenAPI |
| DevOps | Docker, GitHub Actions, Prometheus, Grafana |

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+
- Maven

### Backend
```bash
cd ecommerce
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend
```bash
cd ecommerce-frontend
npm install
npm run dev
```

### Docker (Full Stack)
```bash
docker-compose up -d
```

## 📡 API Endpoints

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |
| PUT | `/api/auth/profile` | Authenticated |

### Products
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| GET | `/api/products/search?query=` | Public |
| GET | `/api/products/category/{id}` | Public |
| POST | `/api/products` | SELLER |
| PUT | `/api/products/{id}` | SELLER |
| DELETE | `/api/products/{id}` | SELLER |
| GET | `/api/products/my-products` | SELLER |

### Cart
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/cart` | Authenticated |
| POST | `/api/cart` | Authenticated |
| PUT | `/api/cart/{id}` | Authenticated |
| DELETE | `/api/cart/{id}` | Authenticated |
| DELETE | `/api/cart` | Authenticated |

### Orders
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/orders` | Authenticated |
| GET | `/api/orders` | Authenticated |
| PUT | `/api/orders/{id}/cancel` | Authenticated |

### Wishlist & Reviews
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/wishlist/{productId}` | Authenticated |
| GET | `/api/wishlist` | Authenticated |
| POST | `/api/reviews/{productId}` | Authenticated |
| GET | `/api/reviews/{productId}` | Public |

### Admin
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/admin/stats` | ADMIN |
| GET | `/api/admin/orders` | ADMIN |
| PUT | `/api/admin/orders/{id}/status` | ADMIN |
| GET | `/api/admin/users` | ADMIN |
| POST | `/api/admin/categories` | ADMIN |
| DELETE | `/api/admin/categories/{id}` | ADMIN |

## 👥 User Roles

| Role | Capabilities |
|------|-------------|
| **BUYER** | Browse, cart, orders, wishlist, reviews |
| **SELLER** | Add/edit/delete own products |
| **ADMIN** | Manage categories, users, orders, view stats |

## 📊 Monitoring

- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3001`
- **Actuator**: `http://localhost:4000/actuator`

## 📄 License

MIT
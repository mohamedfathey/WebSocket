# 🔌 WebSocket Projects Overview

This repo contains **two WebSocket-based projects** built with **Spring Boot**, demonstrating different approaches to real-time communication.

---

## 📁 Project 1 — Simple WebSocket Chat

📡 A minimal WebSocket chat server with no authentication.



### 📂 Path:
 [Simple WebSocket Chat](https://github.com/mohamedfathey/WebSocket/tree/main/webSocket)


---

## 🔐 Project 2 — Authenticated WebSocket Chat (JWT)

🛡️ A secure WebSocket server where clients must authenticate using JWT tokens before connecting.

### 🔧 Features:
- JWT-based authentication during WebSocket handshake
- Supports both **user ↔ merchant** and **merchant ↔ merchant** chats
- Stores undelivered messages in Map for offline users
- Integrated with Spring Security

### 📂 Path:
 [Authenticated WebSocket Chat](https://github.com/mohamedfathey/WebSocket/tree/main/webSocketWithToken)
---

## 🚀 How to Run

Each project has its own README inside its folder with setup instructions.

To run either:
```bash
cd <project-folder>
./mvnw spring-boot:run

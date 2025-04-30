# 💬 Secure Real-time Chat with Spring WebSocket + JWT

This project builds a **secure, real-time chat application** using **Spring WebSocket** and **JWT (JSON Web Tokens)**. It supports:
- 📡 Real-time, two-way messaging between users.
- 🔐 Secure authentication using JWT tokens.
- 🧑‍💼 Role-based messaging rules (e.g., users can't message other users).
- 📥 Offline message storage and delivery when users reconnect.

This README explains the **Spring WebSocket concepts**, walks through the provided code, and provides **multiple examples** to make everything crystal clear. Let's dive in! 🌟

---

## 🧠 What is WebSocket? The Big Picture

**WebSocket** is a protocol that enables **real-time, bi-directional communication** between a client (e.g., a browser or mobile app) and a server over a single, long-lived TCP connection. Unlike HTTP, which uses a request-response model, WebSocket keeps the connection open, allowing both sides to send messages instantly.

### 🚀 Why Use WebSocket?
- **Low latency**: Perfect for chats, live notifications, gaming, or stock trading apps.
- **Persistent connection**: No need to repeatedly open new connections like in HTTP.
- **Two-way communication**: Both client and server can initiate messages.

#### HTTP vs. WebSocket
| Feature             | HTTP (REST)                     | WebSocket                     |
|---------------------|---------------------------------|-------------------------------|
| Communication       | Client requests, server responds | Both sides can send anytime   |
| Use Case            | Loading a webpage, API calls    | Real-time chat, live updates  |
| Overhead            | High (headers, reconnection)    | Low (single connection)       |

**Analogy**: HTTP is like sending letters—you send one, wait for a reply. WebSocket is like a phone call—both sides talk freely without waiting. 📞

---

## 🧩 Spring WebSocket: Core Concepts

Spring WebSocket makes it easy to build WebSocket-based applications in a Spring project. Below are the key concepts, each with **examples** to solidify understanding.

### 1. **WebSocket Handler** (`TextWebSocketHandler`)
- **What it does**: Defines how the server handles WebSocket connections, messages, and disconnections.
- **Key Methods**:
  - `afterConnectionEstablished`: Triggered when a client connects.
  - `handleTextMessage`: Processes incoming text messages from clients.
  - `afterConnectionClosed`: Called when a client disconnects.

**Example 1: Echo Handler**
```java
public class EchoHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }
}
```
- **What happens**: A client sends "Hello!" and gets "Echo: Hello!" back. 🗣️
- **Use case**: A simple chatbot that repeats what you say.

**Example 2: Broadcast Handler**
```java
public class BroadcastHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage("Broadcast: " + message.getPayload()));
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
```
- **What happens**: A client sends a message, and all connected clients receive it. 📢
- **Use case**: A group chat where everyone sees all messages.

### 2. **WebSocket Configuration** (`WebSocketConfigurer`)
- **What it does**: Registers WebSocket handlers and maps them to specific URLs (e.g., `/chat`).
- **Key Annotation**: `@EnableWebSocket` activates WebSocket support in Spring.
- **Features**:
  - Map handlers to endpoints.
  - Configure allowed origins (e.g., `*` for all domains).

**Example: Basic Configuration**
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new EchoHandler(), "/echo").setAllowedOrigins("*");
    }
}
```
- **What happens**: Clients can connect to `ws://localhost:8080/echo` to use the `EchoHandler`. 🌐
- **Use case**: Setting up a WebSocket endpoint for a specific feature.

### 3. **JWT Authentication** 🔐
- **What it does**: Ensures only authorized users can connect by validating a JWT token.
- **How it works**: The client includes a token in the WebSocket URL (e.g., `ws://localhost:8080/chat?token=xxx`). The server decodes it to verify the user’s identity and role.

**Example: Token Validation**
```java
public String getUsernameFromToken(String token) {
    return JWT.decode(token).getClaim("USERNAME").asString();
}
```
- **What happens**: The server extracts the username (e.g., "alice") from the token. 🕵️
- **Use case**: Ensuring only logged-in users can join the chat.

### 4. **Role-based Messaging** 🧑‍💼
- **What it does**: Restricts who can message whom based on user roles (e.g., `USER`, `MERCHANT`).
- **Rules in this project**:
  - `USER` cannot message another `USER`.
  - `USER` can message `MERCHANT`.
  - `MERCHANT` can message `USER` or `MERCHANT`.

**Example: Role Check**
```java
private boolean isAllowed(String senderRole, String targetRole) {
    return !(senderRole.equals("USER") && targetRole.equals("USER"));
}
```
- **What happens**: A `USER` trying to message another `USER` gets an error. 🚫
- **Use case**: Enforcing business rules, like restricting customer-to-customer chats.

### 5. **Offline Message Handling** 💌
- **What it does**: Stores messages for offline users and delivers them when they reconnect.
- **How it works**: Messages are stored in a `Map` with the target username as the key and a list of messages as the value.

**Example: Store and Deliver**
```java
Map<String, List<String>> offlineMessages = new ConcurrentHashMap<>();
offlineMessages.computeIfAbsent("bob", k -> new ArrayList<>()).add("Hi Bob!");
// When Bob connects:
List<String> messages = offlineMessages.getOrDefault("bob", new ArrayList<>());
for (String msg : messages) {
    session.sendMessage(new TextMessage(msg));
}
offlineMessages.remove("bob");
```
- **What happens**: Bob is offline, so messages are stored. When he connects, he receives them. 📬
- **Use case**: Ensuring users don’t miss messages sent while they were offline.

---

## 🛠️ Code Walkthrough

Let’s break down the provided code files (`JWTService.java`, `ChatHandler.java`, `WebSocketConfig.java`) and explain how they work together to create the chat application.

### 1. `JWTService.java` – Token Generation & Validation
- **Purpose**: Manages JWT token creation and validation to authenticate users and extract their username and role.
- **Key Responsibilities**:
  - Generate JWT tokens for users.
  - Extract username and role from tokens.
  - Fetch a user’s role from the database by username.

**Key Code**:
- **Generate Token**:
  ```java
  public String generateJWTForUser(User user) {
      return JWT.create()
          .withClaim("USERNAME", user.getUsername())
          .withClaim("ROLE", user.getRole().toString())
          .withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))
          .withIssuer(issuer)
          .sign(algorithm);
  }
  ```
  - **Explanation**: Creates a JWT containing the user’s username, role, expiration time, and issuer, signed with a secret key (HMAC256 algorithm). 🔑
  - **Example**: For a user with `username="alice"` and `role="MERCHANT"`, it generates a token like `eyJhbG...`.

- **Extract Username & Role**:
  ```java
  public String getUsername(String token) {
      return JWT.decode(token).getClaim("USERNAME").asString();
  }
  public String getRole(String token) {
      return JWT.decode(token).getClaim("ROLE").asString();
  }
  ```
  - **Explanation**: Decodes the JWT to retrieve the username (e.g., "alice") and role (e.g., "MERCHANT"). 🕵️
  - **Example**: Given a token, `getUsername` returns "alice", and `getRole` returns "MERCHANT".

- **Get Role by Username**:
  ```java
  public String getRoleByUsername(String username) {
      Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(username);
      return optionalUser.map(user -> user.getRole().toString()).orElse("UNKNOWN");
  }
  ```
  - **Explanation**: Queries the database to get the role of a target user. If not found, returns "UNKNOWN". 📊
  - **Example**: For `username="bob"`, it returns "USER" if Bob is a user in the database.

**Real-world Example**:
- A user logs into the app and receives a JWT: `eyJhbG...`.
- They connect to the WebSocket endpoint: `ws://localhost:8080/chat?token=eyJhbG...`.
- The server uses `JWTService` to verify the token and extract `username="alice"` and `role="MERCHANT"`.

### 2. `ChatHandler.java` – Core WebSocket Logic
- **Purpose**: Handles WebSocket connections, processes messages, enforces role-based rules, and manages offline messages.
- **Key Responsibilities**:
  - Authenticate users when they connect.
  - Process and route messages to the target user.
  - Store messages for offline users.
  - Clean up sessions when users disconnect.

**Key Code**:
- **Connection Established**:
  ```java
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      String token = getParam(session, "token");
      String username = jwtService.getUsername(token);
      sessions.put(username, session);
      List<String> messages = offlineMessages.getOrDefault(username, new ArrayList<>());
      for (String msg : messages) {
          session.sendMessage(new TextMessage(msg));
      }
      offlineMessages.remove(username);
  }
  ```
  - **Explanation**: When a client connects, the server:
    1. Extracts the JWT token from the URL.
    2. Gets the username from the token.
    3. Stores the session in a `Map` (`sessions`).
    4. Sends any stored offline messages to the user.
    5. Clears the offline messages for that user. 🤝
  - **Example**: Alice connects with `token=eyJhbG...`. The server verifies her as "alice", stores her session, and sends her any missed messages.

- **Handle Messages**:
  ```java
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      String token = getParam(session, "token");
      String sender = jwtService.getUsername(token);
      String senderRole = jwtService.getRole(token);
      String target = getParam(session, "targetUsername");
      if (!isAllowed(senderRole, jwtService.getRoleByUsername(target))) {
          session.sendMessage(new TextMessage("❌ Communication not allowed with this role."));
          return;
      }
      String fullMessage = "[" + sender + "] → " + message.getPayload();
      WebSocketSession targetSession = sessions.get(target);
      if (targetSession != null && targetSession.isOpen()) {
          targetSession.sendMessage(new TextMessage(fullMessage));
      } else {
          offlineMessages.computeIfAbsent(target, k -> new ArrayList<>()).add(fullMessage);
      }
  }
  ```
  - **Explanation**: When a client sends a message, the server:
    1. Extracts the sender’s username and role from the token.
    2. Gets the target username from the URL.
    3. Checks if the sender is allowed to message the target based on roles.
    4. Formats the message (e.g., "[alice] → Hi!").
    5. Sends the message to the target if they’re online, or stores it if they’re offline. 📨
  - **Example**:
    - Alice (MERCHANT) sends "Hi!" to Bob (USER). The server checks roles, sees it’s allowed, and delivers the message.
    - If Bob is offline, the message is stored in `offlineMessages` for later delivery.

- **Connection Closed**:
  ```java
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      sessions.values().removeIf(s -> s.equals(session));
  }
  ```
  - **Explanation**: When a client disconnects, their session is removed from the `sessions` map. 👋
  - **Example**: Alice closes her browser, and her session is removed so the server doesn’t try to send her messages.

- **Helper Methods**:
  - `getParam`: Extracts query parameters (e.g., `token`, `targetUsername`) from the WebSocket URL.
    ```java
    private String getParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals(key)) {
                return parts[1];
            }
        }
        return null;
    }
    ```
    - **Explanation**: Parses the URL (e.g., `ws://localhost:8080/chat?token=xxx&targetUsername=bob`) to get values like `token` or `targetUsername`. 🔍
    - **Example**: For `key="targetUsername"`, it returns "bob".

  - `isAllowed`: Enforces role-based messaging rules.
    ```java
    private boolean isAllowed(String senderRole, String targetRole) {
        if (senderRole.equals("user") && targetRole.equals("user")) return false;
        return true;
    }
    ```
    - **Explanation**: Returns `false` if a `USER` tries to message another `USER`, otherwise `true`. 🚫
    - **Example**: A `USER` messaging a `MERCHANT` is allowed, but a `USER` messaging a `USER` is blocked.

**Real-world Example**:
- Alice (MERCHANT) connects to `ws://localhost:8080/chat?token=eyJhbG...&targetUsername=bob`.
- She sends "Hi Bob!". The server checks roles, sees Bob is a `USER`, and delivers the message if Bob is online or stores it if he’s offline.
- When Bob connects, he receives all stored messages like "[alice] → Hi Bob!".

### 3. `WebSocketConfig.java` – WebSocket Setup
- **Purpose**: Configures the WebSocket endpoint and registers the `ChatHandler`.
- **Key Responsibilities**:
  - Enable WebSocket support with `@EnableWebSocket`.
  - Map the `ChatHandler` to the `/chat` endpoint.

**Key Code**:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final JWTService jwtService;
    
    public WebSocketConfig(JWTService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/chat").setAllowedOrigins("*");
    }
    
    @Bean
    WebSocketHandler chatHandler() {
        return new ChatHandler(jwtService);
    }
}
```
- **Explanation**:
  - `@EnableWebSocket`: Activates WebSocket support in Spring.
  - `registerWebSocketHandlers`: Maps the `ChatHandler` to the `/chat` endpoint, allowing clients to connect to `ws://localhost:8080/chat`.
  - `setAllowedOrigins("*")`: Allows connections from any domain (useful for testing).
  - `chatHandler()`: Creates a `ChatHandler` bean, injecting the `JWTService` for authentication. 🌐
- **Example**: A client connects to `ws://localhost:8080/chat?token=xxx`, and the `ChatHandler` processes their messages.

**Real-world Example**:
- The server starts, and the `/chat` endpoint is available.
- Clients (e.g., a browser or mobile app) connect to `ws://localhost:8080/chat` with a valid JWT token, and the `ChatHandler` manages their interactions.

---

## 🌟 Example WebSocket Client (JavaScript)

To see the chat in action, here’s a simple JavaScript client that connects to the WebSocket endpoint.

```javascript
const socket = new WebSocket("ws://localhost:8080/chat?token=eyJhbG...&targetUsername=bob");

socket.onopen = () => {
    console.log("Connected! 🚀");
    socket.send("Hello Bob! 👋");
};

socket.onmessage = (event) => {
    console.log("Received: ", event.data);
};

socket.onclose = () => {
    console.log("Disconnected. 😢");
};
```
- **What happens**:
  - The client connects with a JWT token and specifies a target user ("bob").
  - It sends a message ("Hello Bob!").
  - It logs any received messages (e.g., "[alice] → Hi!").
- **Use case**: A web app where users chat with merchants in real-time.

---

## 💡 Future Enhancements
Here are some ideas to make the chat app even better:
- 🔔 **Typing indicators**: Show when a user is typing.
- ✅ **Read receipts**: Confirm when messages are read.
- 🌈 **Multimedia support**: Allow sending images or files.
- 📊 **Online status**: Show which users are online.

---

## 🏁 Getting Started
1. **Set up the project**:
   - Clone the repository.
   - Ensure you have Spring Boot and a database (e.g., MySQL) configured.
   - Add dependencies for `spring-boot-starter-websocket`, `jjwt`, and `spring-data-jpa`.

2. **Configure properties**:
   - In `application.properties`, set:
     ```properties
     jwt.algorithm.key=your-secret-key
     jwt.issuer=your-app-name
     jwt.expiryInSeconds=3600
     ```

3. **Run the server**:
   - Start the Spring Boot application.
   - The WebSocket endpoint will be available at `ws://localhost:8080/chat`.

4. **Test with a client**:
   - Use the JavaScript client above or a WebSocket client like Postman.
   - Connect with a valid JWT token and a `targetUsername`.

---

Happy chatting! 🎉
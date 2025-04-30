# Spring WebSocket: A Complete Guide with Code Breakdown

- ### 1️⃣ What is WebSocket?
    ➡ **`WebSocket`** is a *communication protocol* that provides
     **full-duplex** communication *over* a single, long-lived TCP connection. 
     `Unlike` HTTP, which follows a request-response model, **WebSockets** 
     allow real-timebidirectional communication between the *`client`* and *`server`*.
- ### 2️⃣ Why Spring WebSocket?
    ➡ **Spring** provides a **`powerful`** and **`flexible`** way to implement 
    WebSocket in your applications. It abstracts away the 
    low-level details and provides a clean API for working 
    with WebSocket. 

## 💥Key Components of Spring WebSocket
- ### 1️⃣ WebSocketHandler:
    ➖This is where you define the **logic** for handling 
    WebSocket **`messages`**. 
    You can think of it as the "`controller`" for WebSocket communication.

-  ### 2️⃣ WebSocketConfigurer:
    ➖Used to **`configure`** WebSocket **endpoints**.
You register your *`WebSocket handlers`* **here**.

- ### 3️⃣ STOMP (Simple Text Oriented Messaging Protocol):
    A sub-protocol that runs on **top of WebSocket**. 
It defines a **format** for `sending` and `receiving` messages. 
**STOMP** makes it `easier` to work with WebSocket by **providing** 
a `messaging model` similar to **HTTP**.
- ### 4️⃣ SockJS:
    A fallback **option** for browsers that **don't** support **`WebSocket`**.
 It **simulates** WebSocket behavior using techniques like long polling.
- ### 5️⃣ Message Broker:
    A **middleware** component that **`handles`** message routing 
    between `clients` and the `server`.
- ### 6️⃣ WebSocketSession
    **Represents** a WebSocket connection between the client
 and the server. It provides **methods** for sending messages
 , closing the connection, and accessing session attributes.

## 💫 How Spring WebSocket Works

🟠The client establishes a WebSocket connection with the server.

🔵The server and client can send messages to each other at any time.

🟣If using STOMP, messages are routed through a message broker.

🔴  The connection remains open until either the client or server closes it.
## 🚀 Code Explanation
- ### 1️⃣ ChatHandler Class
    This class extends **`TextWebSocketHandler`** 
    and **handles** WebSocket `connections`, `messages`, and `disconnections`.
    ```java 
    package com.example.webSocket.handler;

    import org.springframework.web.socket.*;
    import org.springframework.web.socket.handler.TextWebSocketHandler;
    import java.io.IOException;
    import java.util.*;

    public class ChatHandler extends TextWebSocketHandler {

    // Maps to store user and admin sessions
    private static final Map<String, WebSocketSession> userSessions = new HashMap<>(); // Key: userId
    private static final Map<String, WebSocketSession> adminSessions = new HashMap<>(); // Key: adminId
    private static final Map<String, String> userToAdminMapping = new HashMap<>(); // Key: userId, Value: adminId
    ```
    **`userSessions`**: **Stores** WebSocket sessions for `users`. The key is the **`userId`**.
    **`adminSessions`**: **Stores** WebSocket sessions for `admins`. The key is the **`adminId`**.
    **`userToAdminMapping`**: Maps a `user` to an `admin`. The `key` is the **`userId`**, and the `value` is the **`adminId`**.
- ### 2️⃣ afterConnectionEstablished
    This method is **called** when a new WebSocket **connection** is **established**.
    ```java
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String role = getRoleFromSession(session);
        String userIdOrAdminId = getUserIdOrAdminIdFromSession(session);

        System.out.println("New connection established. Role: " + role + ", ID: " + userIdOrAdminId);

        if ("user".equals(role)) {
            String adminId = getAdminIdFromSession(session);
            System.out.println("User connected. Admin ID: " + adminId);
            System.out.println("Current admin sessions: " + adminSessions);

            if (adminId != null && adminSessions.containsKey(adminId)) {
                userToAdminMapping.put(userIdOrAdminId, adminId); // Map user to admin
                userSessions.put(userIdOrAdminId, session); // Store user session
                session.sendMessage(new TextMessage("Connected as USER. Assigned to ADMIN: " + adminId));
                System.out.println("User assigned to admin: " + adminId);
            } else {
                session.sendMessage(new TextMessage("Admin not found or not available."));
                session.close();
                System.out.println("Admin not found or not available. Closing user session.");
            }
        } else if ("admin".equals(role)) {
            adminSessions.put(userIdOrAdminId, session); // Store admin session
            session.sendMessage(new TextMessage("Connected as ADMIN. Waiting for users."));
            System.out.println("Admin connected. ID: " + userIdOrAdminId);
        }
    }
    ```
    🔵 When a user connects, the code **checks** if the assigned **admin** is **`available`**. 
    
    ✅ If yes, the user is mapped to the admin, and the session is stored.
    
    ❎ If the **admin** is `not available`, the **user** session is **closed**.

    🟦 When an admin **connects**, their session is **`stored`**, and they wait for users.

- ### 3️⃣ handleTextMessage
    This method **processes** incoming text messages.
    ```java
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String senderId = getUserIdOrAdminIdFromSession(session);
        String role = getRoleFromSession(session);
        String payload = message.getPayload();

        if ("user".equals(role)) {
            String assignedAdmin = userToAdminMapping.get(senderId);
            if (assignedAdmin != null && adminSessions.containsKey(assignedAdmin)) {
                adminSessions.get(assignedAdmin).sendMessage(new TextMessage("User " + senderId + ": " + payload));
            } else {
                session.sendMessage(new TextMessage("No assigned admin is available."));
            }
        } else if ("admin".equals(role)) {
            String assignedUser = getUserAssignedToAdmin(senderId);
            if (assignedUser != null && userSessions.containsKey(assignedUser)) {
                userSessions.get(assignedUser).sendMessage(new TextMessage("Admin " + senderId + ": " + payload));
            } else {
                session.sendMessage(new TextMessage("No user is assigned to you currently."));
            }
        }
    }
    ```
    ✅ If a **user** `sends` a message, it’s forwarded to their assigned **admin**.
    
    ✅If an **admin** `sends` a message, it’s forwarded to their assigned **user**.

    ❎ If **no** `admin` or `user` is **available**, an **`error`** message is sent.


- ### 4️⃣ afterConnectionClosed
    This method is **called** when a WebSocket **`connection`** is **closed**.
    
    ```java
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userIdOrAdminId = getUserIdOrAdminIdFromSession(session);
        if (userSessions.containsKey(userIdOrAdminId)) {
            userToAdminMapping.remove(userIdOrAdminId);
            userSessions.remove(userIdOrAdminId);
        } else if (adminSessions.containsKey(userIdOrAdminId)) {
            adminSessions.remove(userIdOrAdminId);
            userToAdminMapping.values().remove(userIdOrAdminId);
        }
        System.out.println("Connection closed. ID: " + userIdOrAdminId);
    }
    ```
    When a `user` or `admin` **disconnects**, their **session** is **removed** from the maps.

- ### 5️⃣ Helper Methods
    These methods **extract** **information** from the *WebSocket* *session*.
    ```java
    private String getRoleFromSession(WebSocketSession session) {
    String uri = session.getUri().toString();
    return uri.contains("role=admin") ? "admin" : "user";
    }

    private String getAdminIdFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        if (uri.contains("adminId=")) {
            return uri.split("adminId=")[1].split("&")[0]; // Extract adminId from URI
        }
        return null;
    }

    private String getUserIdOrAdminIdFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        if (uri.contains("userId=")) {
            return uri.split("userId=")[1].split("&")[0]; // Extract userId from URI
        } else if (uri.contains("adminId=")) {
            return uri.split("adminId=")[1].split("&")[0]; // Extract adminId from URI
        }
        return session.getId(); // Fallback to session ID
    }

    private String getUserAssignedToAdmin(String adminId) {
        for (Map.Entry<String, String> entry : userToAdminMapping.entrySet()) {
            if (entry.getValue().equals(adminId)) {
                return entry.getKey();
            }
        }
        return null;
    }
    ```
    These methods **extract** the **`role`**, **`userId`**, and **`adminId`** from the **WebSocket** session URI.

- ### 6️⃣ WebSocketConfig Class
    This class **configures** the WebSocket **endpoint**.
    ```java
    package com.example.webSocket.config;

    import com.example.webSocket.handler.ChatHandler;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.socket.WebSocketHandler;
    import org.springframework.web.socket.config.annotation.EnableWebSocket;
    import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
    import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

    @Configuration
    @EnableWebSocket
    public class WebSocketConfig implements WebSocketConfigurer {
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(chatHandler(), "/chat")
                    .setAllowedOrigins("*");
        }

        @Bean
        WebSocketHandler chatHandler() {
            return new ChatHandler();
        }
    }
    ```
    **`@EnableWebSocket`** ➡ **Enables** WebSocket support in the application.
    **`registerWebSocketHandlers`**  ➡ **Registers** the `/chat` endpoint and maps it to the **ChatHandler**.
    **`setAllowedOrigins("*")`** ➡ **Allows** `connections` from any origin (for development purposes).









<!-- 🖌 1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ ➡ ⬅⬇↗☑🔴🟠🔵🟣🟣🟪🟦🟩 
💫💥🚀 
6️⃣7️⃣8️⃣9️⃣
❌💯❎✅⏩➖
-->
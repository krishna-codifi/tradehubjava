# TradeHub Java Sample
This project demonstrates how to use the **TradeHub Java SDK** (`in.codifi.tradehub`) from a separate Java application.  
It shows how to call all supported TradeHub REST APIs and how to connect to the live WebSocket feed.

![Java Version](https://img.shields.io/badge/Java-17%2B-blue)
![Status](https://img.shields.io/badge/Status-Test-yellow)
![WebSocket](https://img.shields.io/badge/WebSocket-Live%20Feed-success)
![Build](https://img.shields.io/badge/Build-Maven%20%2F%20Manual-lightgrey)

The code in this project resides in:

```
in.codifi.client.Main
```

This consumer depends on the SDK JAR:

```
tradehub-<version>-all.jar
```

(placed inside a `/libs` folder or added in Maven/Gradle.)

---

## 📦 Project Purpose

This consumer application demonstrates:

### 🔹 REST API Usage
- Get Session  
- Get Contract Master  
- Script & F&O Search  
- Get Profile  
- Get Funds  
- Get Positions  
- Get Holdings  
- Place Order  
- Modify Order  
- Cancel Order  
- Position Square-Off  
- Exit Bracket Order  
- Single Order Margin  
- GTT Orders (Place, Modify, Cancel)  
- Orderbook, Tradebook, Order History  

### 🔹 WebSocket Feed
- Connect WebSocket  
- Auto-authenticate  
- Subscribe to market tokens  
- Receive live feed  
- Keep running until **Ctrl + C**  
- Gracefully stop on shutdown  

---

## 🏗 Required Dependency (SDK JAR)

Place your TradeHub JAR inside:

```
/libs/tradehub-1.0.0-all.jar
```

This JAR must contain:

```
in.codifi.tradehub.TradeHub
in.codifi.tradehub.ClientWS
```

---

## 🔧 Setup Your Credentials

Open `Main.java` and update:

```java
private static final String USER_ID    = "";
private static final String AUTH_CODE  = "";
private static final String SECRET_KEY = "";
private static final String API_KEY    = "";
```

These values are required for REST and WebSocket.

---

## ▶️ Running the Consumer

### 1. Compile

```bash
javac -cp libs/tradehub-1.0.0-all.jar -d out src/main/java/in/codifi/client/Main.java
```

### 2. Run

```bash
java -cp out:libs/tradehub-1.0.0-all.jar in.codifi.client.Main
```

(Windows users → replace `:` with `;`)

---

## 🎛 Enabling Specific Features

In `Main.java`, toggle features by changing flags:

```java
private static final boolean RUN_GET_FUNDS = true;
private static final boolean RUN_PLACE_ORDER = true;
private static final boolean RUN_GTT_PLACE = true;
...
```

Each enabled block will:

1. Perform the API action  
2. Print the response  
3. Save it inside:

```
static/DDMMYYYY/ID-<USER_ID>/
```

---

## 📡 WebSocket Usage

WebSocket section is automatically triggered at the bottom of `Main.java`.

#### Flow inside consumer:

1. Create WS client:

```java
ClientWS client = ClientWS.SetCredentials(USER_ID, API_KEY);
```

2. Fetch session for WS:

```java
client.GetSessionID();
```

3. Attach event handlers:

```java
client.OnOpen = () -> System.out.println("Connected");
client.SubscribeHandler = msg -> System.out.println("Feed: " + msg);
```

4. Start WebSocket:

```java
client.StartWebsocket(true, false);
```

5. Subscribe to a token:

```java
ClientWS.InstrumentWS inst = client.GetInstrumentByTokenWS("INDICES", "26000");
client.Subscribe(List.of(inst));
```

6. Keep running until **Ctrl+C**:

```java
while (true) Thread.sleep(1000);
```

7. Shutdown hook automatically stops WebSocket:

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> client.StopWebsocket()));
```

---

## 📁 Output Files

All results (session, funds, orders, etc.) are saved here:

```
static/<DDMMYYYY>/ID-<USER_ID>/*.txt
```

Everything is timestamp-organized for easy tracking.

---

## 🚀 Customization

You can extend this consumer to:

- Integrate into your own backend  
- Listen to multiple tokens  
- Add database or logging storage  
- Turn this into a scheduled job or microservice  

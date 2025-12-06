package tradehubjava;

//The package from the JAR file (dependency)
import in.codifi.tradehub.ClientWS;
import in.codifi.tradehub.TradeHub;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    // =======================
    //  CONFIGURATION
    // =======================

    // Put your real credentials here (or load from env/config)
    private static final String USER_ID    = "";
    private static final String AUTH_CODE  = "";
    private static final String SECRET_KEY = "";
    private static final String API_KEY    = "";

    // What actions to run
    private static final boolean RUN_GET_CONTRACT_MASTER  = false;
    private static final boolean RUN_SCRIPTS              = false;
    private static final boolean RUN_GET_PROFILE          = false;
    private static final boolean RUN_GET_FUNDS            = false;
    private static final boolean RUN_GET_POSITIONS        = false;
    private static final boolean RUN_GET_HOLDINGS         = false;
    private static final boolean RUN_PLACE_ORDER          = false;

    // NEW: flag equivalents you asked to convert
    private static final boolean RUN_MODIFY_ORDER         = false;
    private static final boolean RUN_CANCEL_ORDER         = false;
    private static final boolean RUN_POSITION_SQROFF      = false;
    private static final boolean RUN_EXIT_BRACKET         = false;
    private static final boolean RUN_SINGLE_ORDER_MARGIN  = false;

    private static final boolean RUN_GTT_PLACE            = false;
    private static final boolean RUN_GTT_MODIFY           = false;
    private static final boolean RUN_GTT_CANCEL           = false;

    private static final boolean RUN_GET_ORDERBOOK        = false;
    private static final boolean RUN_GET_TRADEBOOK        = false;
    private static final boolean RUN_GET_ORDER_HISTORY    = false;

    public static void main(String[] args) {
        try {
            // 1) Build TradeHub client
            TradeHub trade = new TradeHub(USER_ID, AUTH_CODE, SECRET_KEY, null);

            // 2) Build storage directory
            Path storageDir = buildStorageDir(USER_ID);
            System.out.println("Storage directory: " + storageDir);

            // 3) Get Session
            System.out.println("1. Get Session...");
            Map<String, Object> sessionResp = trade.getSessionID("", "");
            System.out.println("Session Response:");
            prettyPrint(sessionResp);
            saveResult(storageDir, "Session_" + USER_ID + ".txt", sessionResp);

            // 4) Optional: Get Contract Master (NSE, NFO, etc.)
            if (RUN_GET_CONTRACT_MASTER) {
                System.out.println("2. Get Contract Master files...");
                String[] exchanges = {"NSE", "NFO", "CDS", "BSE", "BFO", "MCX", "INDICES"};
                for (String exch : exchanges) {
                    Object res = trade.getContractMaster(exch);
                    System.out.printf("GetContractMaster(%s): %s%n", exch, res);
                }
            }

            // 5) Optional: Scripts (instruments + F&O search)
            if (RUN_SCRIPTS) {
                System.out.println("3. Scripts / Instrument search...");

                // YESBANK on NSE
                Object instYesBank = trade.getInstrument("NSE", "YESBANK", "");
                System.out.println("Instrument YESBANK NSE:");
                prettyPrint(instYesBank);

                // Token 200306 on BSE
                Object instBseToken = trade.getInstrument("BSE", "", "200306");
                System.out.println("Instrument token 200306 BSE:");
                prettyPrint(instBseToken);

                // F&O example
                String strike = "30000";
                Object fnoInst = trade.getInstrumentForFNO(
                        "NFO",
                        "NIFTY",
                        "2026-06-25",
                        false,   // isFut = false => options
                        strike,
                        false    // isCE = false => PE
                );
                System.out.println("FNO instrument search:");
                prettyPrint(fnoInst);
            }

            // 6) Optional: Get Profile (InitGet("getProfile"))
            if (RUN_GET_PROFILE) {
                System.out.println("4. Get Profile...");
                Map<String, Object> profile = trade.initGet("getProfile", null, "");
                System.out.println("Profile:");
                prettyPrint(profile);
                saveResult(storageDir, "Profile_" + USER_ID + ".txt", profile);
            }

            // 7) Get Funds
            if (RUN_GET_FUNDS) {
                System.out.println("5. Get Funds...");
                Map<String, Object> funds = trade.initGet("getFunds", null, "");
                System.out.println("Funds:");
                prettyPrint(funds);
                saveResult(storageDir, "Funds_" + USER_ID + ".txt", funds);
            }

            // 8) Get Positions
            if (RUN_GET_POSITIONS) {
                System.out.println("6. Get Positions...");
                Map<String, Object> positions = trade.initGet("getPositions", null, "");
                System.out.println("Positions:");
                prettyPrint(positions);
                saveResult(storageDir, "Positions_" + USER_ID + ".txt", positions);
            }

            // 9) Get Holdings
            if (RUN_GET_HOLDINGS) {
                System.out.println("7. Get Holdings...");
                Map<String, Object> holdings = trade.getHoldings("CNC");
                System.out.println("Holdings:");
                prettyPrint(holdings);
                saveResult(storageDir, "Holdings_" + USER_ID + ".txt", holdings);
            }

            // 10) Place Order example
            if (RUN_PLACE_ORDER) {
                System.out.println("8. Place Order example...");

                // Get instrument for token 14366 on NSE
                Object instRaw = trade.getInstrument("NSE", "", "14366");
                String instrumentId = extractTokenFromInstrument(instRaw);

                if (instrumentId == null || instrumentId.isEmpty()) {
                    System.out.println("Could not extract instrumentId from GetInstrument result");
                } else {
                    Map<String, Object> placeOrderResp = trade.placeOrder(
                            instrumentId,   // instrumentId
                            "NSE",          // exchange
                            "Buy",          // transactionType
                            "2",            // quantity
                            "AMO",          // orderComplexity
                            "Longterm",     // product
                            "Limit",        // orderType
                            "6.2",          // price
                            "0",            // slTriggerPrice
                            "0",            // slLegPrice
                            "0",            // targetLegPrice
                            "DAY"           // validity
                    );
                    System.out.println("PlaceOrder response:");
                    prettyPrint(placeOrderResp);
                    saveResult(storageDir, "PlaceOrder_" + USER_ID + ".txt", placeOrderResp);
                }
            }

            // 11) Modify Order
            if (RUN_MODIFY_ORDER) {
                System.out.println("9. Modify Order example...");

                String brokerOrderId = ""; // Order Id
                String price = "6.5";
                String slTriggerPrice = "0";
                String slLegPrice = "";
                String targetLegPrice = "";
                String quantity = "5";
                String orderType = "LIMIT";
                String trailingSLAmount = "";
                String validity = "DAY";
                String disclosedQuantity = "";
                String marketProtectionPercent = "";
                String orderComplexity = "";
                String deviceId = "180a894d3ce7be4349c4139bd13377f5";

                Map<String, Object> resp = trade.modifyOrder(
                        brokerOrderId,
                        price,
                        slTriggerPrice,
                        slLegPrice,
                        targetLegPrice,
                        quantity,
                        orderType,
                        trailingSLAmount,
                        validity,
                        disclosedQuantity,
                        marketProtectionPercent,
                        deviceId
                );

                System.out.println("ModifyOrder response:");
                prettyPrint(resp);
                saveResult(storageDir, "ModifyOrder_" + USER_ID + ".txt", resp);
            }

            // 12) Cancel Order
            if (RUN_CANCEL_ORDER) {
                System.out.println("10. Cancel Order example...");

                String brokerOrderId = ""; // Order Id
                Map<String, Object> resp = trade.cancelOrder(brokerOrderId);
                System.out.println("CancelOrder response:");
                prettyPrint(resp);
                saveResult(storageDir, "CancelOrder_" + USER_ID + ".txt", resp);
            }

            // 13) Position Square Off (place SELL MARKET)
            if (RUN_POSITION_SQROFF) {
                System.out.println("11. Position Square Off example...");

                Object instRaw = trade.getInstrument("NSE", "", "14366");
                String instrumentId = extractTokenFromInstrument(instRaw);

                if (instrumentId == null || instrumentId.isEmpty()) {
                    System.out.println("Could not extract instrumentId for PositionSqrOff");
                } else {
                    Map<String, Object> resp = trade.placeOrder(
                            instrumentId,
                            "NSE",
                            "Sell",
                            "1",
                            "REGULAR",
                            "INTRADAY",
                            "MARKET",
                            "0",
                            "0",
                            "0",
                            "0",
                            "DAY"
                    );
                    System.out.println("PositionSqrOff response:");
                    prettyPrint(resp);
                    saveResult(storageDir, "PositionSqrOff_" + USER_ID + ".txt", resp);
                }
            }

            // 14) Exit Bracket Order
            if (RUN_EXIT_BRACKET) {
                System.out.println("12. Exit Bracket Order example...");

                String brokerOrderId = ""; // Order Id
                String orderComplexity =  "CO"; // Cover order; adjust if you use "BO" etc.

                Map<String, Object> resp = trade.exitBracketOrder(brokerOrderId, orderComplexity);
                System.out.println("ExitBracketOrder response:");
                prettyPrint(resp);
                saveResult(storageDir, "ExitBracketOrder_" + USER_ID + ".txt", resp);
            }

            // 15) Single Order Margin
            if (RUN_SINGLE_ORDER_MARGIN) {
                System.out.println("13. Single Order Margin example...");

                Object instRaw = trade.getInstrument("NSE", "HFCL", "");
                String transactionType = "BUY";
                String quantity = "1";
                String orderComplexity = "REGULAR";
                String product = "INTRADAY";
                String orderType = "MARKET";
                String price = "82.99";
                String slTriggerPrice = "1215";
                String slLegPrice = "0";

                Map<String, Object> resp = trade.singleOrderMargin(
                        instRaw,
                        transactionType,
                        quantity,
                        orderComplexity,
                        product,
                        orderType,
                        price,
                        slTriggerPrice,
                        slLegPrice
                );
                System.out.println("SingleOrderMargin response:");
                prettyPrint(resp);
                saveResult(storageDir, "SingleOrderMargin_" + USER_ID + ".txt", resp);
            }

            // 16) GTT Place
            if (RUN_GTT_PLACE) {
                System.out.println("14. GTT Place example...");

                Object inst = trade.getInstrument("NFO", "", "35167");

                Map<String, Object> resp = trade.GTT_placeOrder(
                        "SELL",      // transactionType
                        "3100",      // quantity
                        "REGULAR",   // orderComplexity
                        "INTRADAY",  // product
                        "LIMIT",     // orderType
                        "100",       // price
                        "10",        // gttValue
                        "DAY",       // validity
                        inst,        // instrument (optional)
                        "",          // instrumentId (optional)
                        null,        // exchange (optional)
                        ""           // tradingSymbol (optional)
                );
                System.out.println("GTT Place response:");
                prettyPrint(resp);
                saveResult(storageDir, "GTTPlace_" + USER_ID + ".txt", resp);
            }

            // 17) GTT Modify
            if (RUN_GTT_MODIFY) {
                System.out.println("15. GTT Modify example...");

                String brokerOrderId = "25120600000944";
                Object inst = trade.getInstrument("NFO", "", "35167");

                Map<String, Object> resp = trade.GTT_modifyOrder(
                        brokerOrderId, // brokerOrderId
                        inst,          // instrument
                        "3100",        // quantity
                        "REGULAR",     // orderComplexity
                        "INTRADAY",    // product
                        "LIMIT",       // orderType
                        "100",         // price
                        "10",          // gttValue
                        "DAY",         // validity
                        null,          // exchange
                        ""             // tradingSymbol
                );
                System.out.println("GTT Modify response:");
                prettyPrint(resp);
                saveResult(storageDir, "GTTModify_" + USER_ID + ".txt", resp);
            }

            // 18) GTT Cancel
            if (RUN_GTT_CANCEL) {
                System.out.println("16. GTT Cancel example...");

                String brokerOrderId = ""; // Provide order Id
                Map<String, Object> resp = trade.GTT_cancelOrder(brokerOrderId);
                System.out.println("GTT Cancel response:");
                prettyPrint(resp);
                saveResult(storageDir, "GTTCancel_" + USER_ID + ".txt", resp);
            }

            // 19) Get Orderbook
            if (RUN_GET_ORDERBOOK) {
                System.out.println("17. Get Orderbook...");
                Map<String, Object> resp = trade.initGet("getOrderbook", null, "");
                System.out.println("Orderbook:");
                prettyPrint(resp);
                saveResult(storageDir, "Orderbook_" + USER_ID + ".txt", resp);
            }

            // 20) Get Tradebook
            if (RUN_GET_TRADEBOOK) {
                System.out.println("18. Get Tradebook...");
                Map<String, Object> resp = trade.initGet("getTradebook", null, "");
                System.out.println("Tradebook:");
                prettyPrint(resp);
                saveResult(storageDir, "Tradebook_" + USER_ID + ".txt", resp);
            }

            // 21) Get Order History
            if (RUN_GET_ORDER_HISTORY) {
                System.out.println("19. Get Order History...");
                String brokerOrderId = ""; //Provide order Id
                Map<String, Object> resp = trade.getOrderHistory(brokerOrderId);
                System.out.println("OrderHistory:");
                prettyPrint(resp);
                saveResult(storageDir, "OrderHistory_" + USER_ID + ".txt", resp);
            }

            ////////////////////////////////////////////////////
            // ** Websocket connection
            ////////////////////////////////////////////////////
            System.out.println("******* Web Socket Flow ******");

            ClientWS client = ClientWS.SetCredentials(USER_ID, API_KEY);

            // Get Session ID for WS
            System.out.println("Get Websocket Session...");
            Map<String, Object> wsSession = client.GetSessionID();
            System.out.println(wsSession);

            // Handlers
            client.OnOpen  = () -> System.out.println("Connected");
            client.OnClose = () -> System.out.println("Closed");
            client.OnError = (err) -> System.out.println("Error: " + err.getMessage());

            // NOTE: in ClientWS, SubscribeHandler is Consumer<String>,
            // so msg is already a String from WebSocket onText.
            client.SubscribeHandler = (msg) -> {
                try {
                    System.out.println("Feed: " + msg);
                } catch (Exception e) {
                    System.out.println("Feed error: " + e.getMessage());
                }
            };

            // Start websocket
            client.StartWebsocket(true, false);

            // Wait for websocket to become active (optional sleep)
            Thread.sleep(3000);

            // Subscribe to token 26000
            ClientWS.InstrumentWS instIdx = client.GetInstrumentByTokenWS("INDICES", "26000");
            List<ClientWS.InstrumentWS> subscribeList = new ArrayList<>();
            subscribeList.add(instIdx);

            try {
                client.Subscribe(subscribeList);
                System.out.println("Subscribed to INDICES 26000");
            } catch (Exception e) {
                System.out.println("Subscribe failed: " + e.getMessage());
            }

            // ---- WAIT FOR FEED UNTIL CTRL+C ----
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCtrl+C detected. Shutting down websocket...");
                client.StopWebsocket();
            }));

            System.out.println("Websocket is running... Press Ctrl+C to stop.");

            // Infinite loop (until Ctrl+C)
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in TradeHubConsumer: " + e.getMessage());
        }
    }

    // =======================
    //  Helpers
    // =======================

    private static Path buildStorageDir(String userId) throws IOException {
        String currDate = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        Path storageDir = Paths.get("static", currDate, "ID-" + userId);
        Files.createDirectories(storageDir);
        return storageDir;
    }

    private static void saveResult(Path storageDir, String fileName, Object obj) {
        try {
            Path dest = storageDir.resolve(fileName);
            String text = String.valueOf(obj); // simple text; you can use Jackson if you want JSON
            Files.writeString(dest, text, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved: " + dest);
        } catch (IOException e) {
            System.out.println("Failed to save " + fileName + " : " + e.getMessage());
        }
    }

    private static void prettyPrint(Object obj) {
        System.out.println(obj);
    }

    private static String extractTokenFromInstrument(Object instRaw) {
        if (instRaw == null) return "";

        if (instRaw instanceof TradeHub.Instrument ins) {
            return ins.token;
        }

        if (instRaw instanceof Map<?, ?> m) {
            Object t = m.get("Token");
            if (t == null) t = m.get("token");
            return t != null ? t.toString() : "";
        }

        return "";
    }
}

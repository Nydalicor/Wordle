import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The WordleServer class represents a server for the Wordle game. It handles
 * client requests and manages game sessions.
 */
public class WordleServer {

    private final static Set<String> wordSet = WordleWordSet.WORD_SET;
    private static Map<String, String> sessionMap = new HashMap<>();
    private static Map<String, List<String>> attemptsMap = new HashMap<>();

    private static String imagePath = "logo.png";

    /**
     * The main method starts the Wordle server and listens for client connections.
     * 
     * @param args The command line arguments.
     * @throws InterruptedException If the server is interrupted while waiting for
     *                              client connections.
     */
    public static void main(String[] args) throws InterruptedException {

        final int port = 8021; // Specify the port number for the server
        ExecutorService threadPool = Executors.newFixedThreadPool(Integer.parseInt(args[0]));
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleRequest(clientSocket));
                clientSocket.setSoTimeout(300000);
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } finally {
            System.out.println("Server stopped");
            threadPool.shutdown();
        }
    }

    /**
     * Handles a client request by reading the request, processing it, and sending
     * the response.
     * 
     * @param clientSocket The socket representing the client connection.
     */
    private static void handleRequest(Socket clientSocket) {
        String rightWord = null;
        boolean noCookie = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()) {
            char[] buffer = new char[1024];
            String request = reader.readLine();
            String body = "";
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(": ");
                if (headerParts.length == 2) {
                    String headerName = headerParts[0];
                    String headerValue = headerParts[1];
                    headers.put(headerName, headerValue);
                }
            }
            int bufferSize = 0;
            if (headers.containsKey("Content-Length")) {
                bufferSize = Integer.parseInt(headers.get("Content-Length"));
                buffer = new char[bufferSize];
            }
            String sessionCookie = null;
            if (headers.containsKey("Cookie")) {
                sessionCookie = getSessionCookie(headers.get("Cookie"));
            }
            // Read the rest of the request
            int i = 0;
            if (bufferSize > 0) {
                while (i < bufferSize) {
                    int read = reader.read(buffer, i, bufferSize - i);
                    if (read == -1) {
                        System.err.println("read == -1");
                    }
                    i += read;
                }
            }
            body = new String(buffer);
            if (request != null) {
                String[] requestParts = request.split(" ");
                String method = requestParts[0];
                String path = requestParts[1];
                if (sessionCookie == null) {
                    noCookie = true;
                    sessionCookie = java.util.UUID.randomUUID().toString();
                }
                if (!sessionMap.containsKey(sessionCookie)) {
                    attemptsMap.put(sessionCookie, new java.util.ArrayList<>());
                    sessionMap.put(sessionCookie,
                            wordSet.stream().skip((int) (wordSet.size() * Math.random())).findFirst().get()
                                    .toUpperCase());
                }

                rightWord = sessionMap.get(sessionCookie);
                String query = "";
                if (path.contains("?")) {
                    String[] pathParts = path.split("\\?");
                    path = pathParts[0];
                    query = pathParts[1];
                }
                if (method.equals("POST")) {
                    // Get the body of the post method and search for the guess parameter
                    // substring what's atfter guess=, which is the guess of 5 letters
                    query = body;
                    handlePostRequest(outputStream, sessionCookie, query, rightWord, noCookie);
                } else if (method.equals("GET") && (path.equals("/") || path.equals("/index.html"))) {
                    handleRedirect(outputStream, sessionCookie);
                } else if (method.equals("GET") && path.equals("/play.html")
                        && query.toLowerCase().contains("guess=")) {
                    playWordle(outputStream, sessionCookie, query, rightWord, "GET", noCookie);
                } else if (method.equals("GET")) {
                    handleGetRequest(outputStream, path);
                } else {
                    sendResponse(outputStream, "Invalid request : not a GET nor a POST method", 400);
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Problem : " + e.getMessage());
        }
    }

    /**
     * Plays the Wordle game by processing the client's guess and sending the
     * response.
     * 
     * @param outputStream  The output stream to send the response to the client.
     * @param sessionCookie The session cookie associated with the game session.
     * @param query         The query string containing the client's guess.
     * @param rightWord     The correct word for the game session.
     * @param method        The HTTP method used in the request.
     * @param noCookie      Indicates if the client has a session cookie.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    private static void playWordle(OutputStream outputStream, String sessionCookie, String query, String rightWord,
            String method, boolean noCookie)
            throws IOException {
        String guess = query.split("=")[1].toUpperCase();
        if (guess.length() != 5) {
            if (method.equals("GET"))
                sendResponse(outputStream, "Invalid request : guess must be 5 letters", 400);
            else if (method.equals("POST")) {
                HtmlContainer htmlContainer = new HtmlContainer(imagePath);
                htmlContainer.updateGuessSection("BBBBB", guess);
                String response = htmlContainer.getHtml();
                String httpResponse = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "Content-Length: " + response.length() + "\r\n";
                httpResponse += "Connection: close\r\n\r\n" + response;
                outputStream.write(httpResponse.getBytes());
                outputStream.flush();
            }
            return;
        }
        if (attemptsMap.get(sessionCookie).size() >= 6) {
            sendResponse(outputStream, "Invalid request : you already tried too many Words", 400);
            sessionMap.remove(sessionCookie);
            attemptsMap.remove(sessionCookie);
            return;
        }
        attemptsMap.get(sessionCookie).add(guess);
        char[] result = new char[5];
        Map<Character, Integer> map = new HashMap<>();
        /*
         * System.out.println("----------------------");
         * System.out.println("Right word: |" + rightWord + "|");
         * System.out.println("Query: |" + guess + "|");
         * System.out.println("Session cookie: " + sessionCookie);
         */
        String response = "";
        StringBuilder stringBuilder = new StringBuilder();
        if (!guess.equals(rightWord)) {
            // Add the letters of the word to the map
            // If a letter is already in the map, increment the value
            for (int i = 0; i < rightWord.length(); i++) {
                char c = rightWord.charAt(i);
                if (map.containsKey(c)) {
                    map.put(c, map.get(c) + 1);
                } else {
                    map.put(c, 1);
                }
            }
            // Loop on the guess
            // If the letter is in the right place, add G to the array of char at his
            // location
            // If the letter is not in the word, add B to the array of char at his location
            for (int i = 0; i < guess.length(); i++) {
                char c = guess.charAt(i);
                if (rightWord.charAt(i) == c) {
                    result[i] = 'G';
                    map.put(c, map.get(c) - 1);
                } else {
                    result[i] = 'B';
                }
            }
            // Loop on the guess
            // If the letter is not in the right place, check in the map has a value > 0
            // If yes, add Y to the array of char at his location and decrement the value in
            // the map
            // If no, do nothing
            for (int i = 0; i < guess.length(); i++) {
                char c = guess.charAt(i);
                if (rightWord.charAt(i) != c) {
                    if (map.containsKey(c) && map.get(c) > 0) {
                        result[i] = 'Y';
                        map.put(c, map.get(c) - 1);
                    }
                }
            }
            stringBuilder = new StringBuilder();
            for (char c : result) {
                stringBuilder.append(c);
            }

            // Create response for the client, composed by the result and all attempts
            response = "{\"result\":\"" + stringBuilder.toString() + "\",\"attempts\":" + attemptsMap.get(sessionCookie)
                    + "}";
        } else {
            response = "{\"result\":\"" + "GGGGG GAMEOVER" + "\",\"attempts\":" + attemptsMap.get(sessionCookie)
                    + "}";
            sessionMap.remove(sessionCookie);
            attemptsMap.remove(sessionCookie);
        }

        String httpResponse = "";
        if (method.equals("GET")) {
            httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: " + response.length() + "\r\n";
        } else if (method.equals("POST")) {
            // send the response to the client without using javascript
            HtmlContainer htmlContainer = new HtmlContainer(imagePath);
            htmlContainer.updateGuessSection(stringBuilder.toString(), guess);
            /*
             * CHUNKED CODE :
             * response = getChunkResponse(htmlContainer.getHtml());
             * httpResponse = "HTTP/1.1 200 OK\r\n"
             * + "Content-Type: text/html\r\n"
             * + "Transfer-Encoding: chunked" + "\r\n";
             */
            response = htmlContainer.getHtml();
            httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: " + response.length() + "\r\n";
        }
        if (noCookie) {
            httpResponse += "Set-Cookie: SESSIONID=" + sessionCookie + "; Max-Age=1800; SameSite=Strict\r\n"
                    + "Connection: close"
                    + "\r\n\r\n"
                    + response;
        } else {
            httpResponse += "Connection: close\r\n\r\n" + response;
        }
        try {
            // System.out.println("Response: " + httpResponse);
            outputStream.write(httpResponse.getBytes());
            outputStream.flush();
            // outputStream.close();

        } catch (Exception e) {
            System.out.println("Problem       " + e.getMessage());
        }
    }

    /**
     * Handles the redirect request by sending a 302 response to the client.
     *
     * @param outputStream  The output stream used to send the response to the
     *                      client.
     * @param sessionCookie The session cookie for the current user.
     * @throws IOException If an I/O error occurs while handling the request.
     */
    private static void handleRedirect(OutputStream outputStream, String sessionCookie) throws IOException {
        String httpResponse = "HTTP/1.1 302 Found\r\n"
                + "Location: /play.html\r\n";
        if (sessionCookie != null)
            httpResponse += "Set-Cookie: SESSIONID=" + sessionCookie + "; Max-Age=1800; SameSite=Strict\r\n\r\n";
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    /**
     * Handles the GET request by generating and sending an HTML response to the
     * client.
     *
     * @param outputStream The output stream used to send the response to the
     *                     client.
     * @param path         The path of the requested resource.
     * @throws IOException If an I/O error occurs while handling the request.
     */
    private static void handleGetRequest(OutputStream outputStream, String path) throws IOException {
        try {
            // byte[] bytes = readAllBytes(filePath);
            // String mimeType = getMimeType(filePath);
            HtmlContainer container = new HtmlContainer(imagePath);
            String html = container.getHtml();
            /*
             * String httpResponse = "HTTP/1.1 200 OK\r\n"
             * + "Content-Type: text/html\r\n"
             * + "Transfer-Encoding: chunked\r\n"
             * + "Connection: close"
             * + "\r\n\r\n" + getChunkResponse(html);
             */
            String httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: " + html.length() + "\r\n"
                    + "Connection: close"
                    + "\r\n\r\n" + html;

            outputStream.write(httpResponse.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            sendResponse(outputStream, "File not found", 404);
        }
    }

    /**
     * Handles a POST request by playing the Wordle game.
     *
     * @param outputStream  The output stream to write the response to.
     * @param sessionCookie The session cookie for the current user.
     * @param query         The query string of the request.
     * @param rightWord     The correct word for the Wordle game.
     * @param noCookie      Indicates whether the request has a session cookie or
     *                      not.
     * @throws IOException If an I/O error occurs while handling the request.
     */
    private static void handlePostRequest(OutputStream outputStream, String sessionCookie, String query,
            String rightWord, boolean noCookie) throws IOException {
        // print all method
        playWordle(outputStream, sessionCookie, query, rightWord, "POST", noCookie);
    }

    /**
     * Sends an HTTP response to the output stream.
     *
     * @param outputStream The output stream to send the response to.
     * @param response     The response message to be sent.
     * @param statusCode   The status code of the response.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    private static void sendResponse(OutputStream outputStream, String response, int statusCode) throws IOException {
        /*
         * CHUNKED CODE :
         * String httpResponse = "HTTP/1.1 " + statusCode + " " +
         * getStatusCodeMessage(statusCode) + "\r\n"
         * + "Content-Type: text/html;charset=UTF-8\r\n"
         * + "Transfer-Encoding: chunked\r\n\r\n"
         * + getChunkResponse(response);
         * outputStream.write(httpResponse.getBytes());
         */
        String httpResponse = "HTTP/1.1 " + statusCode + " " + getStatusCodeMessage(statusCode) + "\r\n"
                + "Content-Type: text/html;charset=UTF-8\r\n"
                + "Content-Length: " + response.length() + "\r\n\r\n"
                + response;
        outputStream.write(httpResponse.getBytes());
        outputStream.flush();
    }

    /**
     * CHUNKED CODE :
     * Generates a chunked response for the HTML content.
     * 
     * @param response The HTML content to be sent in chunks.
     * @return The formatted chunked response.
     */
    private static String getChunkResponse(String response) {
        StringBuilder formattedResponse = new StringBuilder();

        final int SIZE = 128; // Size of each chunk

        for (int i = 0; i < response.length(); i += SIZE) {
            // Calculate the size of the current chunk
            int currentChunkSize = Math.min(SIZE, response.length() - i);

            // Add the current chunk size in hexadecimal format, followed by a newline
            formattedResponse.append(Integer.toHexString(currentChunkSize)).append("\r\n");

            // Add the current chunk itself, followed by a newline
            formattedResponse.append(response.substring(i, i + currentChunkSize)).append("\r\n");
        }

        // Add the last chunk with a size of 0 (indicating the end), followed by two
        // newlines
        formattedResponse.append("0").append("\r\n").append("\r\n");

        // Convert the result to a string
        return formattedResponse.toString();
    }

    /**
     * Retrieves the session cookie from the given request.
     *
     * @param request the request string containing the cookies
     * @return the session cookie value, or null if not found
     */
    private static String getSessionCookie(String request) {
        String sessionCookie = null;
        String[] cookies = request.split("; ");
        for (String cookie : cookies) {
            String[] parts = cookie.split("=");
            if (parts.length == 2 && parts[0].equals("SESSIONID")) {
                sessionCookie = parts[1];
                break;
            }
        }
        return sessionCookie;
    }

    /**
     * Returns the status code message for the given status code.
     *
     * @param statusCode the status code
     * @return the status code message
     */
    private static String getStatusCodeMessage(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 302:
                return "Found";
            case 303:
                return "See Other";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 411:
                return "Length Required";
            case 501:
                return "Not Implemented";
            case 505:
                return "HTTP Version Not Supported";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }
}
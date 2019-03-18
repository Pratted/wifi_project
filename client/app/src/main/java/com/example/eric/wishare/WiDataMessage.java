package com.example.eric.wishare;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WiDataMessage {

    private JSONObject mJSONObect;
    private static URL url;
    private static final String queryString = "?token=abc123";


    public WiDataMessage(JSONObject json) {
        mJSONObect = json;
    }

    public static void send() {
//        try {
//            String hostname = "192.3.135.177" + queryString;
//            int port = 3000;
//
//            InetAddress addr = InetAddress.getByName(hostname);
//            Socket socket = new Socket(addr, port);
//            String path = "/myapp";
//
//            // Send headers
//            BufferedWriter wr =
//                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
//            System.out.println("SENDING MSG TO SERVER");
//            wr.write("[\"123\",\"2\",\"3\"]" );
////            wr.write("Content-Length: "+params.length()+"rn");
//            wr.write("Content-Type: application/json");
//            // Send parameters
////            wr.write(params);
//            wr.flush();
//            System.out.println("MSG SENT TO SERVER");
//            // Get response
//            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String line;
//            System.out.println("READING FROM SERVER");
//            while ((line = rd.readLine()) != null) {
//                System.out.println(line);
//            }
//            wr.close();
//            rd.close();
//
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            String token = "?token=cba123";
            String urlAddress = "http://192.3.135.177:3000/" + token;
            url = new URL(urlAddress);
            URLConnection conn = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            String message = "[\"123\",\"2\",\"3\"]";
            byte[] out = message.getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(out.length);
            http.setRequestProperty("Content-Type", "application/json");
            http.connect();
            System.out.println("SENDING MSG TO SERVER");
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            System.out.println("MSG SENT TO SERVER");
//            try(InputStream is = http.getInputStream()) {
//                byte[] buffer = new byte[2048];
//                int i = is.read(buffer);
//                System.err.println("THE SERVER SAYS: ");
//                System.out.println(buffer);
//            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String line;
            System.out.println("READING FROM SERVER");
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            rd.close();
            ((HttpURLConnection) conn).disconnect();
        } catch (IOException e) {
            System.err.println("Send message expetion: " + e.getMessage());
        }
    }
}

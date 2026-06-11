package src.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class AttendanceWebServer {

    public static void start() {

        try {

            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(8080),
                            0);

            // Attendance Form Page
            server.createContext("/attendance",
                    exchange -> {

                        String response =
                                "<html>" +
                                "<head><title>Attendance</title></head>" +
                                "<body>" +

                                "<h2>Student Attendance</h2>" +

                                "<form action='/submitAttendance' method='POST'>" +

                                "Student ID:<br>" +
                                "<input type='text' name='studentId'><br><br>" +

                                "Student Name:<br>" +
                                "<input type='text' name='studentName'><br><br>" +

                                "<button type='submit'>" +
                                "Mark Attendance" +
                                "</button>" +

                                "</form>" +

                                "</body>" +
                                "</html>";

                        exchange.sendResponseHeaders(
                                200,
                                response.getBytes().length);

                        OutputStream os =
                                exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();
                    });

            // Handle Form Submission
            server.createContext("/submitAttendance",
                    exchange -> {

                        if ("POST".equals(exchange.getRequestMethod())) {

                            InputStream input =
                                    exchange.getRequestBody();

                            String formData =
                                    new String(
                                            input.readAllBytes(),
                                            StandardCharsets.UTF_8);

                            String studentId = "";
                            String studentName = "";

                            String[] pairs =
                                    formData.split("&");

                            for (String pair : pairs) {

                                String[] kv =
                                        pair.split("=");

                                if (kv.length == 2) {

                                    String key =
                                            URLDecoder.decode(
                                                    kv[0],
                                                    "UTF-8");

                                    String value =
                                            URLDecoder.decode(
                                                    kv[1],
                                                    "UTF-8");

                                    if (key.equals("studentId")) {
                                        studentId = value;
                                    }

                                    if (key.equals("studentName")) {
                                        studentName = value;
                                    }
                                }
                            }

                            System.out.println(
                                    "Student ID = "
                                            + studentId);

                            System.out.println(
                                    "Student Name = "
                                            + studentName);

                            String response =
                                    "<html>" +
                                    "<body>" +
                                    "<h2>Attendance Request Received</h2>" +
                                    "<p>ID: "
                                    + studentId +
                                    "</p>" +
                                    "<p>Name: "
                                    + studentName +
                                    "</p>" +
                                    "</body>" +
                                    "</html>";

                            exchange.sendResponseHeaders(
                                    200,
                                    response.getBytes().length);

                            OutputStream os =
                                    exchange.getResponseBody();

                            os.write(response.getBytes());

                            os.close();
                        }
                    });

            server.start();

            System.out.println(
                    "Attendance Server Started");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
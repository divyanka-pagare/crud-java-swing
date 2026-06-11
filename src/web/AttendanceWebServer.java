package src.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class AttendanceWebServer {

    public static void start() {

        try {

            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(8080),
                            0);

            server.createContext("/attendance",
                    (HttpExchange exchange) -> {

                        String response =
                            "<html>" +
                            "<head>" +
                            "<title>Attendance</title>" +
                            "</head>" +
                            "<body>" +

                            "<h2>Student Attendance</h2>" +

                            "<form>" +

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
                                response.length());

                        OutputStream os =
                                exchange.getResponseBody();

                        os.write(response.getBytes());

                        os.close();
                    });

            server.start();

            System.out.println(
                    "Attendance Server Started on Port 8080");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
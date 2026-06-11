package src.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import src.db.DBConnection;

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

                        String query =
                                exchange.getRequestURI().getQuery();
                        
                        String token = "";
                        
                        if(query != null &&
                           query.startsWith("token="))
                        {
                            token =
                                query.substring(6);
                        }

                        String response =
                                "<html>" +
                                "<head><title>Attendance</title></head>" +
                                "<body>" +

                                "<h2>Student Attendance</h2>" +

                                "<form action='/submitAttendance' method='POST'>" +

                                "<input type='hidden' " + "name='token' " + "value='" + token + "'>" +

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
                            try {

                            InputStream input =
                                    exchange.getRequestBody();

                            String formData =
                                    new String(
                                            input.readAllBytes(),
                                            StandardCharsets.UTF_8);

                            String studentId = "";
                            String studentName = "";
                            String token = "";

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

                                    if (key.equals("token")) {
                                        token = value;
                                    }
                                }
                            }

                            System.out.println(
                                    "Student ID = "
                                            + studentId);

                            System.out.println(
                                    "Student Name = "
                                            + studentName);

                            Connection con = DBConnection.getConnection();
                                    
                            PreparedStatement pst = con.prepareStatement(
                                "SELECT * FROM students " +
                                "WHERE id = ? AND name = ?"
                            );
                            
                            pst.setInt(
                                    1,
                                    Integer.parseInt(studentId)
                            );
                            
                            pst.setString(
                                    2,
                                    studentName
                            );
                            
                            

                            ResultSet rs = pst.executeQuery();
                            
                                if (!rs.next()) {

                                    

                                    String response =
                                            "<html><body>" +
                                            "<h2 style='color:red'>" +
                                            "Invalid Student ID or Name" +
                                            "</h2>" +
                                            "</body></html>";
                                
                                    exchange.sendResponseHeaders(
                                            200,
                                            response.getBytes().length);
                                
                                    OutputStream os =
                                            exchange.getResponseBody();
                                
                                    os.write(response.getBytes());
                                
                                    os.close();
                                
                                    return;
                                }

                                

                            PreparedStatement pst2 =
                                    con.prepareStatement(
                                            "SELECT * FROM qr_sessions " +
                                            "WHERE token = ? " +
                                            "AND active = 1"
                                    );

                            pst2.setString(
                                    1,
                                    token
                            );

                            ResultSet rs2 = pst2.executeQuery();

                            if (!rs2.next()) {

                                String response =
                                        "<html><body>" +
                                        "<h2 style='color:red'>" +
                                        "QR Session Not Found or Expired" +
                                        "</h2>" +
                                        "</body></html>";

                                exchange.sendResponseHeaders(
                                        200,
                                        response.getBytes().length);

                                OutputStream os =
                                        exchange.getResponseBody();

                                os.write(response.getBytes());

                                os.close();

                                return;
                            }

                            int courseId = rs2.getInt("course_id");

                            PreparedStatement checkAttendance =
                                    con.prepareStatement(
                                            "SELECT * FROM attendance " +
                                            "WHERE student_id=? " +
                                            "AND course_id=? " +
                                            "AND attendance_date=CURDATE()"
                                    );

                            checkAttendance.setInt(
                                    1,
                                    Integer.parseInt(studentId)
                            );

                            checkAttendance.setInt(
                                    2,
                                    courseId
                            );

                            ResultSet attendanceRs =
                                    checkAttendance.executeQuery();

                            if(attendanceRs.next()) {

                                String response =
                                        "<html><body>" +
                                        "<h2 style='color:orange'>" +
                                        "Attendance Already Marked" +
                                        "</h2>" +
                                        "</body></html>";

                                exchange.sendResponseHeaders(
                                        200,
                                        response.getBytes().length);

                                OutputStream os =
                                        exchange.getResponseBody();

                                os.write(response.getBytes());

                                os.close();

                                return;
                            }
                            PreparedStatement pst3 =
                                    con.prepareStatement(
                                            "INSERT INTO attendance " +
                                            "(student_id, course_id, attendance_date, status) " +
                                            "VALUES (?, ?, CURDATE(), 'Present')"
                                    );

                            pst3.setInt(
                                    1,
                                    Integer.parseInt(studentId)
                            );

                            pst3.setInt(
                                    2,
                                    courseId
                            );

                            pst3.executeUpdate();

                            String response =
                                    "<html><body>" +
                                    "<h2 style='color:green'>" +
                                    "Attendance Marked Successfully" +
                                    "</h2>" +
                                    "</body></html>";

                            exchange.sendResponseHeaders(
                                    200,
                                    response.getBytes().length);

                            OutputStream os =
                                    exchange.getResponseBody();

                            os.write(response.getBytes());

                            os.close();

                        } catch (Exception e) {

                            e.printStackTrace();
                        
                            String response =
                                    "<html><body>" +
                                    "<h2 style='color:red'>" +
                                    "Server Error: " + e.getMessage() +
                                    "</h2>" +
                                    "</body></html>";
                        
                            exchange.sendResponseHeaders(
                                    500,
                                    response.getBytes().length);
                        
                            OutputStream os =
                                    exchange.getResponseBody();
                        
                            os.write(response.getBytes());
                        
                            os.close();
                        }
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
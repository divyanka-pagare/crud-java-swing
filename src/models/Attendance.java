package src.models;

import java.time.LocalDate;

public class Attendance {

    private int       id;
    private int       studentId;
    private int       courseId;
    private LocalDate attendanceDate;
    private String    status;
    private String    remarks;

    public Attendance() {}

    public Attendance(int studentId, int courseId,
                      LocalDate date, String status, String remarks) {
        this.studentId      = studentId;
        this.courseId       = courseId;
        this.attendanceDate = date;
        this.status         = status;
        this.remarks        = remarks;
    }

    public int       getId()             { return id; }
    public int       getStudentId()      { return studentId; }
    public int       getCourseId()       { return courseId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String    getStatus()         { return status; }
    public String    getRemarks()        { return remarks; }

    public void setId(int id)                        { this.id = id; }
    public void setStudentId(int sid)                { this.studentId = sid; }
    public void setCourseId(int cid)                 { this.courseId = cid; }
    public void setAttendanceDate(LocalDate d)       { this.attendanceDate = d; }
    public void setStatus(String status)             { this.status = status; }
    public void setRemarks(String remarks)           { this.remarks = remarks; }
}
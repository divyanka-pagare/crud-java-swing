package src.models;

public class Course {

    private int    id;
    private String courseName;
    private double fees;
    private String duration;

    public Course() {}

    public Course(int id, String courseName,
                  double fees, String duration) {
        this.id         = id;
        this.courseName = courseName;
        this.fees       = fees;
        this.duration   = duration;
    }

    public int    getId()           { return id; }
    public String getCourseName()   { return courseName; }
    public double getFees()         { return fees; }
    public String getDuration()     { return duration; }

    public void setId(int id)                    { this.id = id; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setFees(double fees)             { this.fees = fees; }
    public void setDuration(String duration)     { this.duration = duration; }

    @Override
    public String toString() {
        return courseName;
    }
}
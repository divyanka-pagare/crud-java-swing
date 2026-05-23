package src.models;

public class Teacher {

    private int    id;
    private String name;
    private int    experience;
    private String specialization;
    private String availableTime;

    public Teacher() {}

    public Teacher(int id, String name, int experience,
                   String specialization, String availableTime) {
        this.id             = id;
        this.name           = name;
        this.experience     = experience;
        this.specialization = specialization;
        this.availableTime  = availableTime;
    }

    public int    getId()               { return id; }
    public String getName()             { return name; }
    public int    getExperience()       { return experience; }
    public String getSpecialization()   { return specialization; }
    public String getAvailableTime()    { return availableTime; }

    public void setId(int id)                         { this.id = id; }
    public void setName(String name)                  { this.name = name; }
    public void setExperience(int experience)         { this.experience = experience; }
    public void setSpecialization(String s)           { this.specialization = s; }
    public void setAvailableTime(String availableTime){ this.availableTime = availableTime; }

    @Override
    public String toString() { return name; }
}
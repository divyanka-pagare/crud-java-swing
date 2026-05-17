package src.models;

public class Student {

    private int    id;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String skills;
    private String country;
    private int    age;
    private String address;
    private String bio;

    public Student() {}

    public Student(int id, String name, String email,
                   String phone, String gender, String skills,
                   String country, int age,
                   String address, String bio) {
        this.id      = id;
        this.name    = name;
        this.email   = email;
        this.phone   = phone;
        this.gender  = gender;
        this.skills  = skills;
        this.country = country;
        this.age     = age;
        this.address = address;
        this.bio     = bio;
    }

    // Getters
    public int    getId()      { return id; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getPhone()   { return phone; }
    public String getGender()  { return gender; }
    public String getSkills()  { return skills; }
    public String getCountry() { return country; }
    public int    getAge()     { return age; }
    public String getAddress() { return address; }
    public String getBio()     { return bio; }

    // Setters
    public void setId(int id)              { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setEmail(String email)     { this.email = email; }
    public void setPhone(String phone)     { this.phone = phone; }
    public void setGender(String gender)   { this.gender = gender; }
    public void setSkills(String skills)   { this.skills = skills; }
    public void setCountry(String country) { this.country = country; }
    public void setAge(int age)            { this.age = age; }
    public void setAddress(String address) { this.address = address; }
    public void setBio(String bio)         { this.bio = bio; }

    @Override
    public String toString() {
        return name; // shown in JComboBox
    }
}
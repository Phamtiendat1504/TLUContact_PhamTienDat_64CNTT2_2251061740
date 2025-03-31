package com.example.tlu;

public class User {
    private String name;
    private String email;
    private String studentId;
    private String phone;

    // Constructor mặc định (cần thiết cho Firebase)
    public User() {}

    public User(String name, String email, String studentId, String phone) {
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public String getPhone() { return phone; }
}

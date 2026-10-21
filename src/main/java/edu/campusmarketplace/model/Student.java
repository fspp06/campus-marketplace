package edu.campusmarketplace.model;

import java.util.UUID;

/** A campus account. It inherits common login details from User. */
public final class Student extends User {
    private final String studentId;
    private final String department;
    private final int year;
    private final String phoneNumber;
    private final Wishlist wishlist;

    public Student(String name, String email, String password, String studentId, String department, int year, String phoneNumber) {
        this(UUID.randomUUID(), name, email, password, studentId, department, year, phoneNumber);
    }

    public Student(UUID id, String name, String email, String password, String studentId, String department, int year, String phoneNumber) {
        super(id, name, email, password);
        if (studentId == null || studentId.isBlank()) throw new IllegalArgumentException("Student ID is required.");
        if (department == null || department.isBlank()) throw new IllegalArgumentException("Department is required.");
        if (year < 1 || year > 6) throw new IllegalArgumentException("Year must be between 1 and 6.");
        this.studentId = studentId.trim().toUpperCase();
        this.department = department.trim();
        this.year = year;
        this.phoneNumber = (phoneNumber == null || phoneNumber.isBlank()) ? "+91 98765 00000" : phoneNumber.trim();
        this.wishlist = new Wishlist(this);
    }

    public Student(String name, String email, String password, String studentId, String department, int year) {
        this(name, email, password, studentId, department, year, "+91 98765 00000");
    }

    public String getStudentId()   { return studentId; }
    public String getDepartment()  { return department; }
    public int getYear()           { return year; }
    public String getPhoneNumber() { return phoneNumber; }
    public Wishlist getWishlist()  { return wishlist; }
    @Override public String toString() { return getName(); }
}

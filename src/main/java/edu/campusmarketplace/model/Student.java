package edu.campusmarketplace.model;

/** A campus account. It inherits common login details from User. */
public final class Student extends User {
    private final String studentId;
    private final String department;
    private final int year;
    private final Wishlist wishlist;

    public Student(String name, String email, String password, String studentId, String department, int year) {
        super(name, email, password);
        if (studentId == null || studentId.isBlank()) throw new IllegalArgumentException("Student ID is required.");
        if (department == null || department.isBlank()) throw new IllegalArgumentException("Department is required.");
        if (year < 1 || year > 6) throw new IllegalArgumentException("Year must be between 1 and 6.");
        this.studentId = studentId.trim().toUpperCase();
        this.department = department.trim();
        this.year = year;
        this.wishlist = new Wishlist(this);
    }

    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
    public Wishlist getWishlist() { return wishlist; }
    @Override public String toString() { return getName(); }
}

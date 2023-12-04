package com.SOA.universityproject.Model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


//This Class is the model of Student has all student attribute , Setters and Getters
@XmlRootElement(name = "Student")
public class StudentRequest {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private double gpa;
    private int level;
    private String address;

    @XmlElement(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @XmlElement(name = "FirstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @XmlElement(name = "LastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    @XmlElement(name = "Gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @XmlElement(name = "Gpa")
    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }
    @XmlElement(name = "Level")
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    @XmlElement(name = "Address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}

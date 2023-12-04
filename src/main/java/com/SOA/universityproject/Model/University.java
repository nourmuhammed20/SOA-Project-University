package com.SOA.universityproject.Model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "University")
public class University {

    private List<StudentRequest> students;
    public University() {
        this.students = new ArrayList<>();
    }
    @XmlElementWrapper(name = "Students")
    @XmlElement(name = "Student")
    public List<StudentRequest> getStudents() {
        return students;
    }

    public void setStudents(List<StudentRequest> students) {
        this.students = students;
    }
}

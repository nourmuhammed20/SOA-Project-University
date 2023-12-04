package com.SOA.universityproject.Controllers;

import com.SOA.universityproject.Model.StudentRequest;
import com.SOA.universityproject.Model.University;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


//This Controller handle the XML Whole Logic and make API for Each Function

@RestController
@RequestMapping("/students")
public class StudentController {

    private static final String XML_FILE_PATH = "src/main/java/com/SOA/universityproject/university.xml";


    @GetMapping("/allStudents")
    public List<StudentRequest> getAllStudents() {
        List<StudentRequest> result = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new File(XML_FILE_PATH));

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);

                StudentRequest studentResponse = new StudentRequest();
                studentResponse.setId(studentElement.getAttribute("ID"));
                studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
                studentResponse.setLastName(getElementValue(studentElement, "LastName"));
                studentResponse.setGender(getElementValue(studentElement, "Gender"));
                studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
                studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
                studentResponse.setAddress(getElementValue(studentElement, "Address"));

                result.add(studentResponse);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @PostMapping("/saveStudents")
    public String saveStudents(@RequestBody List<StudentRequest> studentRequests) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
        try {
            //            used for parsing xml
            DocumentBuilder builder = dbf.newDocumentBuilder();

            // Load existing XML document if it exists
            Document doc;
            File xmlFile = new File(XML_FILE_PATH);
            if (xmlFile.exists()) {
                doc = builder.parse(xmlFile);
            } else {
                doc = builder.newDocument();
                Element root = doc.createElement("University");
                doc.appendChild(root);
            }

            Element root = doc.getDocumentElement();

            for (StudentRequest studentRequest : studentRequests) {
                // Check if student with given ID already exists
                if (isStudentAlreadySaved(doc, studentRequest.getId())) {
                    // System.out.println("Student with ID " + studentRequest.getId() + " is already saved.");
                    continue; // Skip to the next student
                }
                if (!validateStudentData(studentRequest)) {
                    continue;
                }

                Element student = doc.createElement("Student");
                student.setAttribute("ID", studentRequest.getId());

                Element firstName = doc.createElement("FirstName");
                Text firstNameVal = doc.createTextNode(studentRequest.getFirstName());
                firstName.appendChild(firstNameVal);

                Element lastName = doc.createElement("LastName");
                Text lastNameVal = doc.createTextNode(studentRequest.getLastName());
                lastName.appendChild(lastNameVal);

                Element gender = doc.createElement("Gender");
                Text genderVal = doc.createTextNode(studentRequest.getGender());
                gender.appendChild(genderVal);

                Element gpa = doc.createElement("GPA");
                Text gpaVal = doc.createTextNode(String.valueOf(studentRequest.getGpa()));
                gpa.appendChild(gpaVal);

                Element level = doc.createElement("Level");
                Text levelVal = doc.createTextNode(String.valueOf(studentRequest.getLevel()));
                level.appendChild(levelVal);

                Element address = doc.createElement("Address");
                Text addressVal = doc.createTextNode(studentRequest.getAddress());
                address.appendChild(addressVal);

                student.appendChild(firstName);
                student.appendChild(lastName);
                student.appendChild(gender);
                student.appendChild(gpa);
                student.appendChild(level);
                student.appendChild(address);

                root.appendChild(student);
            }

            DOMSource source = new DOMSource(doc);
            Result result = new StreamResult(xmlFile);
            //          used to  transform the source object into the result object
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            // System.out.println("OK" + XML_FILE_PATH);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving students: " + e.getMessage();
        }

        return "Students saved successfully.";
    }




    @DeleteMapping("/deleteById/{id}")
    public String deleteStudentById(@PathVariable String id) {
        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                return "No students have been saved yet.";
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);

                if (studentElement.getAttribute("ID").equals(id)) {
                    // Remove the matching student element
                    Node parentNode = studentElement.getParentNode();
                    parentNode.removeChild(studentElement);

                    // Save the updated XML without introducing extra spaces
                    saveUpdatedXml(doc);

                    return "Student with ID " + id + " deleted successfully.";
                }
            }

            return "Student with ID " + id + " not found.";

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return "An error occurred during the deletion process.";
        }
    }


    private void saveUpdatedXml(Document doc) throws TransformerException {
        try {
            // Use a StringWriter to prevent extra spaces when saving
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            // Use the transformer to write the XML content to the StringWriter
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), result);

            // Update the file with the content from the StringWriter
            try (FileWriter fileWriter = new FileWriter(new File(XML_FILE_PATH))) {
                fileWriter.write(sw.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String getElementValue(Element parentElement, String elementName) {
        NodeList nodeList = parentElement.getElementsByTagName(elementName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    @GetMapping("/validate")
    private boolean isStudentAlreadySaved(Document doc, String studentId) {
        Element root = doc.getDocumentElement();
        Element[] students = getChildElements(root, "Student");
        for (Element student : students) {
            if (studentId.equals(student.getAttribute("ID"))) {
                return true;
            }
        }
        return false;
    }
    // Helper method to get child elements by tag name
    private Element[] getChildElements(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }

        return elements.toArray(new Element[0]);
    }
//////////////////////////////////// Sort Function //////////////////////////////////////////////////////////////////////
@GetMapping("/sort")
public List<StudentRequest> sortStudents(@RequestParam String sortBy,
                           @RequestParam boolean ascending,
                           Model model) {
    List<StudentRequest> sortedStudents =sortStudents(sortBy, ascending);

    model.addAttribute("sortedStudents", sortedStudents);
    return sortedStudents;
}
    private University university = new University();

    public List<StudentRequest> sortStudents(String sortBy, boolean ascending) {
        List<StudentRequest> students = getAllStudents();
        if (students != null) {

            Comparator<StudentRequest> comparator = getComparator(sortBy);
            if (ascending) {

                students.sort(comparator);
            } else {

                students.sort(comparator.reversed());
            }
            university.setStudents(students);

            writeUniversity(university); // Overwrite the file with sorted students
        }

        return students;
    }
    public University readUniversity() {
        return (University) XmlParser.readXml(University.class);
    }
    public void writeUniversity(University university) {
        XmlParser.writeXml(university);
    }
    private Comparator<StudentRequest> getComparator(String sortBy) {

        sortBy = sortBy.toLowerCase();

        switch (sortBy) {
            case "id":
                return Comparator.comparing(StudentRequest::getId);
            case "firstname":
                return Comparator.comparing(StudentRequest::getFirstName);
            case "lastname":
                return Comparator.comparing(StudentRequest::getLastName);
            case "gender":
                return Comparator.comparing(StudentRequest::getGender);
            case "gpa":
                return Comparator.comparing(StudentRequest::getGpa);
            case "level":
                return Comparator.comparing(StudentRequest::getLevel);
            case "address":
                return Comparator.comparing(StudentRequest::getAddress);
            default:
                throw new IllegalArgumentException("Invalid attribute for sorting.");
        }
    }

//////////////////////////////////// Search Function ////////////////////////////////////////////////////////////////////
    @GetMapping("/search")
    public ResponseEntity<?> searchStudents(@RequestParam(required = false) String query) {
        List<StudentRequest> result = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new File(XML_FILE_PATH));

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);

                String studentId = studentElement.getAttribute("ID").toLowerCase();
                String studentFirstName = getElementValue(studentElement, "FirstName").toLowerCase();
                String studentLastName = getElementValue(studentElement, "LastName").toLowerCase();
                double studentGPA = Double.parseDouble(getElementValue(studentElement, "GPA"));
                int studentLevel = Integer.parseInt(getElementValue(studentElement, "Level"));
                String studentAddress = getElementValue(studentElement, "Address").toLowerCase();

                String lowercaseQuery = query.toLowerCase();


                // Check if the query matches any of the attributes
                if (studentId.contains(lowercaseQuery)
                        || studentFirstName.contains(lowercaseQuery)
                        || studentLastName.contains(lowercaseQuery)
                        || String.valueOf(studentGPA).contains(lowercaseQuery)
                        || String.valueOf(studentLevel).contains(lowercaseQuery)
                        || studentAddress.contains(lowercaseQuery)) {

                    StudentRequest studentResponse = new StudentRequest();
                    studentResponse.setId(studentId);
                    studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
                    studentResponse.setGpa(studentGPA);
                    studentResponse.setLevel(studentLevel);
                    studentResponse.setAddress(getElementValue(studentElement, "Address"));

                    result.add(studentResponse);
                }
            }

            // Check if the result list is empty
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Student Found");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(result);
    }

///////////////////////////////////// update Function ///////////////////////////////////////////
    public String updateStudent(@RequestBody List<StudentRequest> studentRequests) {
        try {
                if (!validateStudentData(studentRequests.get(0))) {
                    return "invalid Values";
                }
            File xmlFile = new File(XML_FILE_PATH);
            Document doc;
    
            // Load existing XML document if it exists
            if (xmlFile.exists()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
                DocumentBuilder builder = dbf.newDocumentBuilder();
                doc = builder.parse(xmlFile);
            } else {
                // Create a new document if it doesn't exist
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
                DocumentBuilder builder = dbf.newDocumentBuilder();
                doc = builder.newDocument();
                Element root = doc.createElement("University");
                doc.appendChild(root);
            }
    
            Element root = doc.getDocumentElement();
            

            for (StudentRequest studentRequest : studentRequests) {
                NodeList studentNodes = doc.getElementsByTagName("Student");

                for (int i = 0; i < studentNodes.getLength(); i++) {
                    Element studentElement = (Element) studentNodes.item(i);

                    if (studentElement.getAttribute("ID").equals(studentRequest.getId())) {
                        // Remove the matching student element
                        Node parentNode = studentElement.getParentNode();
                        parentNode.removeChild(studentElement);

                        // Save the updated XML without introducing extra spaces
                        saveUpdatedXml(doc);
                    }
                }
                updateStudentInXml(doc, root, studentRequest);
            }
    
            // Save the updated XML without introducing extra spaces
            saveUpdatedXml(doc);
    
            return "Students saved successfully.";
    
        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return "Error saving students: " + e.getMessage();
        }
    }
    
    private void updateStudentInXml(Document doc, Element root, StudentRequest studentRequest) {

        Element student = doc.createElement("Student");
        student.setAttribute("ID", studentRequest.getId());
    
        Element firstName = createElementWithTextContent(doc, "FirstName", studentRequest.getFirstName());
        Element lastName = createElementWithTextContent(doc, "LastName", studentRequest.getLastName());
        Element gender = createElementWithTextContent(doc, "Gender", studentRequest.getGender());
        Element gpa = createElementWithTextContent(doc, "GPA", String.valueOf(studentRequest.getGpa()));
        Element level = createElementWithTextContent(doc, "Level", String.valueOf(studentRequest.getLevel()));
        Element address = createElementWithTextContent(doc, "Address", studentRequest.getAddress());
    
        student.appendChild(firstName);
        student.appendChild(lastName);
        student.appendChild(gender);
        student.appendChild(gpa);
        student.appendChild(level);
        student.appendChild(address);
    
        root.appendChild(student);
    }
    
    private Element createElementWithTextContent(Document doc, String elementName, String textContent) {
        Element element = doc.createElement(elementName);
        Text text = doc.createTextNode(textContent);
        element.appendChild(text);
        return element;
    }

    public boolean validateStudentData(StudentRequest student) {
        boolean isValid = true;

        if ((Integer.parseInt(student.getId()) <= 0)) {
            System.out.println("Invalid ID for Student");
            isValid = false;
        }

        if (!(student.getFirstName().matches("^[a-zA-Z]+$"))) {
            System.out.println("Invalid first name for Student");
            isValid = false;
        }

        if (!(student.getLastName().matches("^[a-zA-Z]+$"))) {
            System.out.println("Invalid last name for Student");
            isValid = false;
        }

        if (!(student.getAddress().matches("^[a-zA-Z0-9,.\s]+$"))) {
            System.out.println("Invalid address for Student");
            isValid = false;
        }

        if (!(student.getGpa() >= 0 && student.getGpa() <= 4)) {
            System.out.println("Invalid GPA for Student");
            isValid = false;
        }

        if (!(student.getLevel() >= 1 && student.getLevel() <= 4)) {
            System.out.println("Invalid level for Student");
            isValid = false;
        }

        if (!(student.getGender().equalsIgnoreCase("male") || student.getGender().equalsIgnoreCase("female"))) {
            System.out.println("Invalid gender for Student");
            isValid = false;
        }

        return isValid;
    }

//    @GetMapping("/searchById")
//    public List<StudentRequest> searchById(@RequestParam double id) {
//        List<StudentRequest> result = new ArrayList<>();
//
//        try {
//            File xmlFile = new File(XML_FILE_PATH);
//
//            // Check if the file exists before attempting to parse it
//            if (!xmlFile.exists()) {
//                System.out.println("No students have been saved yet.");
//                return result;
//            }
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
//            DocumentBuilder builder = dbf.newDocumentBuilder();
//            Document doc = builder.parse(xmlFile);
//
//            NodeList studentNodes = doc.getElementsByTagName("Student");
//
//            for (int i = 0; i < studentNodes.getLength(); i++) {
//                Element studentElement = (Element) studentNodes.item(i);
//                String studentId = studentElement.getAttribute("ID");
//
//                // Check if the studentId matches the provided double ID
//                if (Double.parseDouble(studentId) == id) {
//                    StudentRequest studentResponse = new StudentRequest();
//                    studentResponse.setId(studentId);
//                    studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
//                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
//                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
//                    studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
//                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
//                    studentResponse.setAddress(getElementValue(studentElement, "Address"));
//
//                    result.add(studentResponse);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return result;
//    }
//    @GetMapping("/searchByGPA")
//    public List<StudentRequest> searchByGPA(@RequestParam double gpa) {
//        List<StudentRequest> result = new ArrayList<>();
//
//        try {
//            File xmlFile = new File(XML_FILE_PATH);
//
//            // Check if the file exists before attempting to parse it
//            if (!xmlFile.exists()) {
//                System.out.println("No students have been saved yet.");
//                return result;
//            }
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
//            DocumentBuilder builder = dbf.newDocumentBuilder();
//            Document doc = builder.parse(xmlFile);
//
//            NodeList studentNodes = doc.getElementsByTagName("Student");
//
//            for (int i = 0; i < studentNodes.getLength(); i++) {
//                Element studentElement = (Element) studentNodes.item(i);
//                String studentId = studentElement.getAttribute("ID");
//                double studentGPA = Double.parseDouble(getElementValue(studentElement, "GPA"));
//
//                if (studentGPA == gpa) {
//                    StudentRequest studentResponse = new StudentRequest();
//                    studentResponse.setId(studentId);
//                    studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
//                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
//                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
//                    studentResponse.setGpa(studentGPA);
//                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
//                    studentResponse.setAddress(getElementValue(studentElement, "Address"));
//
//                    result.add(studentResponse);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return result;
//    }
//
//    public List<StudentRequest> searchByLevel(@RequestParam int Level) {
//        List<StudentRequest> result = new ArrayList<>();
//
//        try {
//            File xmlFile = new File(XML_FILE_PATH);
//
//            // Check if the file exists before attempting to parse it
//            if (!xmlFile.exists()) {
//                System.out.println("No students have been saved yet.");
//                return result;
//            }
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
//            DocumentBuilder builder = dbf.newDocumentBuilder();
//            Document doc = builder.parse(xmlFile);
//
//            NodeList studentNodes = doc.getElementsByTagName("Student");
//
//            for (int i = 0; i < studentNodes.getLength(); i++) {
//                Element studentElement = (Element) studentNodes.item(i);
//                String studentId = studentElement.getAttribute("ID");
//                double studentGPA = Double.parseDouble(getElementValue(studentElement, "GPA"));
//
//                if (studentGPA == Level) {
//                    StudentRequest studentResponse = new StudentRequest();
//                    studentResponse.setId(studentId);
//                    studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
//                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
//                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
//                    studentResponse.setGpa(studentGPA);
//                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
//                    studentResponse.setAddress(getElementValue(studentElement, "Address"));
//
//                    result.add(studentResponse);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return result;
//    }
//
//    @GetMapping("/searchByFirstName")
//    public List<StudentRequest> searchByFirstName(@RequestParam String firstName) {
//        List<StudentRequest> result = new ArrayList<>();
//
//        try {
//            File xmlFile = new File(XML_FILE_PATH);
//
//            // Check if the file exists before attempting to parse it
//            if (!xmlFile.exists()) {
//                System.out.println("No students have been saved yet.");
//                return result; // Return an empty list indicating no students found
//            }
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
//            DocumentBuilder builder = dbf.newDocumentBuilder();
//            Document doc = builder.parse(xmlFile);
//
//            NodeList studentNodes = doc.getElementsByTagName("Student");
//
//            for (int i = 0; i < studentNodes.getLength(); i++) {
//                Element studentElement = (Element) studentNodes.item(i);
//                String studentId = studentElement.getAttribute("ID");
//                String studentFirstName = getElementValue(studentElement, "FirstName");
//
//                if (studentFirstName != null && studentFirstName.equals(firstName)) {
//                    StudentRequest studentResponse = new StudentRequest();
//                    studentResponse.setId(studentId);
//                    studentResponse.setFirstName(studentFirstName);
//                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
//                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
//                    studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
//                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
//                    studentResponse.setAddress(getElementValue(studentElement, "Address"));
//
//                    result.add(studentResponse);
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return result;
//    }

}

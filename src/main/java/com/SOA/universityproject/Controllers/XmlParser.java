package com.SOA.universityproject.Controllers;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class XmlParser {
    private static final String UNIVERSITY_XML_FILE ="src/main/java/com/SOA/universityproject/university.xml";

    public static void writeXml(Object object) {
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();

            // Ensure that the necessary directories exist
            Path filePath = Paths.get(UNIVERSITY_XML_FILE);
            Files.createDirectories(filePath.getParent());

            // If the file doesn't exist, create it
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            // Copy the input stream to the destination file
            try (InputStream inputStream = Files.newInputStream(filePath);
                 FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                FileCopyUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            marshaller.marshal(object, filePath.toFile());
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readXml(Class<?> obj) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            Path filePath = Paths.get(UNIVERSITY_XML_FILE);

            // If the file doesn't exist, return null
            if (!Files.exists(filePath)) {
                return null;
            }

            try (InputStream inputStream = Files.newInputStream(filePath)) {
                return unmarshaller.unmarshal(inputStream);
            } catch (IOException | JAXBException e) {
                e.printStackTrace();
                return null;
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}

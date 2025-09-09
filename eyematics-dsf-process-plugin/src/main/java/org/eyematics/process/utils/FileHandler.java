package org.eyematics.process.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    public static void getWorkingDirectory() {
        logger.info("Working Directory = {}", System.getProperty("user.dir"));
    }

    public static Optional<String> readFile(String fileName) {
        StringBuilder output = new StringBuilder();
        try {
            File jsonFile = new File(fileName);
            Scanner myReader = new Scanner(jsonFile);
            while (myReader.hasNextLine()) {
                String tmp = myReader.nextLine();
                if(tmp != null) output.append(tmp);
            }
            myReader.close();
            logger.info("File ({}) read successfully.", fileName);
        } catch (FileNotFoundException e) {
            logger.info("File ({}) could not be read: {}", fileName, e.toString());
            return Optional.empty();
        }
        return Optional.of(output.toString());
    }

    public static boolean saveFile(String fileName, String fileContent) {
        // 1.) Create new File
        try {
            File jsonFile = new File(fileName);
            if (jsonFile.createNewFile()) {
                logger.info("File created: {}", jsonFile.getName());
            } else {
                logger.info("File already exists.");
            }
        } catch (IOException e) {
            logger.info("File ({}) could not be created: {}", fileName, e.toString());
            return false;
        }
        // 2.) Write to File
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(fileContent);
            myWriter.close();
            logger.info("Successfully wrote to the file.");
        } catch (IOException e) {
            logger.info("File ({}) could not be written: {}", fileName, e.toString());
            return false;
        }
        return true;
    }
}

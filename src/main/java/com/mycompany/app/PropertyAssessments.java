package com.mycompany.app;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

public class PropertyAssessments {
    // Instance variables, these will be accessible with each instance of the class:
    private final String fileName;
    private List<PropertyAssessment> properties = new ArrayList<>();

    // Constructor:
    // Creates a new instance of the class with a given fileName and loads the data
    public PropertyAssessments(String fileName) throws IOException {
        this.fileName = fileName;
        loadData(fileName);
    }

    public PropertyAssessments(List<PropertyAssessment> properties) {
        this.fileName = null;
        this.properties = properties;
    }

    // Getters:
    public String getfileName() {
        return fileName;
    }
    public List<PropertyAssessment> getProperties() {return properties;}

    // Methods:
    private void loadData(String fileName) throws IOException {
        // Check the file, open it and start reading:
        fileName = checkFile(fileName);
        FileReader CSVFile = new FileReader(fileName);
        BufferedReader CSVParser = new BufferedReader(CSVFile);

        String line = CSVParser.readLine(); // Skip the first header line
        while ((line = CSVParser.readLine()) != null) {
            String[] CSVTokens = line.split(",", -1);
            addProperty(CSVTokens);
        }
    }

    private String checkFile(String fileName) throws FileNotFoundException {
        if (!fileName.contains(".csv")) {
            fileName = fileName + ".csv";
        }
        File CSVFile = new File(fileName);
        if (!CSVFile.exists()) {
            throw new FileNotFoundException("Error: " + fileName + " not found.");
        }
        return fileName;
    }

    private void addProperty(String[] CSVTokens) {
        int accountID = parseInt(CSVTokens[0]);

        int suite = parseInt(CSVTokens[1]);
        int houseNumber = parseInt(CSVTokens[2]);
        String streetName = CSVTokens[3];
        Address newAddress = new Address(suite, houseNumber, streetName);

        String garage = CSVTokens[4];

        int neighborhoodID = parseInt(CSVTokens[5]);
        String neighborhoodName = CSVTokens[6];
        String ward = CSVTokens[7];
        Neighborhood newNeighborhood = new Neighborhood(neighborhoodID, neighborhoodName, ward);

        long assessedValue = parseLong(CSVTokens[8]);

        double lat = parseDouble(CSVTokens[9]);
        double lng = parseDouble(CSVTokens[10]);
        String point = CSVTokens[11];
        Location newLocation = new Location(lat, lng, point);

        int assessmentPercentage1 = parseInt(CSVTokens[12]);
        int assessmentPercentage2 = parseInt(CSVTokens[13]);
        int assessmentPercentage3 = parseInt(CSVTokens[14]);
        String assessmentClass1 = CSVTokens[15];
        String assessmentClass2 = CSVTokens[16];
        String assessmentClass3 = CSVTokens[17];
        AssessmentClass newAssessmentClass = new AssessmentClass(assessmentPercentage1, assessmentPercentage2, assessmentPercentage3, assessmentClass1, assessmentClass2, assessmentClass3);

        PropertyAssessment newProperty = new PropertyAssessment(accountID, newAddress, garage, newNeighborhood, assessedValue, newLocation, newAssessmentClass);
        properties.add(newProperty);
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getNumberOfRecords() {
        return properties.size();
    }

    public long getMinValue() {
        long minValue = properties.get(0).getAssessedValue();

        for (PropertyAssessment property : properties) {
            if (property.getAssessedValue() < minValue) {
                minValue = property.getAssessedValue();
            }
        }

        return minValue;
    }

    public long getMaxValue() {
        long maxValue = properties.get(0).getAssessedValue();

        for (PropertyAssessment property : properties) {
            if (property.getAssessedValue() > maxValue) {
                maxValue = property.getAssessedValue();
            }
        }

        return maxValue;
    }

    public long getRange() {
        return getMaxValue() - getMinValue();
    }

    public long getMean() {
        long sum = 0;

        for (PropertyAssessment property : properties) {
            sum += property.getAssessedValue();
        }

        return sum / properties.size();
    }

    public long getMedian() {
        // Create a mutable list of assessed values
        List<Long> assessedValues = properties.stream()
                .map(PropertyAssessment::getAssessedValue)
                .sorted() // Sort the values
                .collect(Collectors.toList());

        int size = assessedValues.size();
        if (size == 0) {
            throw new IllegalStateException("No properties available to calculate median.");
        }

        if (size % 2 == 0) {
            // For even-sized lists, take the average of the two middle elements
            return (assessedValues.get(size / 2) + assessedValues.get(size / 2 - 1)) / 2;
        } else {
            // For odd-sized lists, take the middle element
            return assessedValues.get(size / 2);
        }
    }


}
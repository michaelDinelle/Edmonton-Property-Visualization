package com.mycompany.app;

import java.util.Objects;
import java.util.function.Predicate;

public class Address {
    // Instance variables:
    private final int suite;
    private final int houseNumber;
    private final String streetName;

    // Constructor:
    public Address(int suite, int houseNumber, String streetName) {
        this.suite = suite;
        this.houseNumber = houseNumber;
        this.streetName = streetName;
    }

    // Getters:
    public int getSuite() {return suite;}
    public int getHouseNumber() {return houseNumber;}
    public String getStreetName() {return streetName;}

    // Predicates:
    private static final Predicate<Integer> validHouseNumber = number -> number != -1;
    private static final Predicate<String> validStreetName = name -> name != null && !name.isEmpty();

    // Methods:
    @Override
    public String toString() {
        StringBuilder addressStr = new StringBuilder();

        if (validHouseNumber.test(houseNumber)) {
            addressStr.append(houseNumber);
        }

        if (validStreetName.test(streetName)) {
            addressStr.append(" ").append(streetName);
        }

        return addressStr.isEmpty() ? "N/A" : addressStr.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Address address = (Address) other;

        if (suite != address.suite) return false;
        if (houseNumber != address.houseNumber) return false;
        return Objects.equals(streetName, address.streetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suite, houseNumber, streetName);
    }
}


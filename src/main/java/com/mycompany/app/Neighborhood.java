package com.mycompany.app;

import java.util.function.Predicate;

public class Neighborhood {
    // Instance variables:
    public final int neighborhoodID;
    public final String neighborhoodName;
    public final String ward;

    // Constructor:
    public Neighborhood(int neighborhoodID, String neighborhoodName, String ward) {
        this.neighborhoodID = neighborhoodID;
        this.neighborhoodName = neighborhoodName;
        this.ward = ward;
    }

    // Getters:
    public int getNeighborhoodID() {return neighborhoodID;}
    public String getNeighborhoodName() {return neighborhoodName;}
    public String getWard() {return ward;}

    // Predicates:
    private static final Predicate<String> validString = str -> str != null && !str.isEmpty();

    // Methods:
    @Override
    public String toString() {
        StringBuilder neighborhoodStr = new StringBuilder();

        if (validString.test(neighborhoodName)) {
            neighborhoodStr.append(neighborhoodName);
        }

        if (validString.test(ward)) {
            neighborhoodStr.append(" (").append(ward).append(")");
        }

        return neighborhoodStr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Neighborhood neighborhood = (Neighborhood) o;
        if (neighborhoodID != neighborhood.neighborhoodID) return false;
        if (!neighborhoodName.equals(neighborhood.neighborhoodName)) return false;
        return ward.equals(neighborhood.ward);
    }

    @Override
    public int hashCode() {
        int result = neighborhoodID;
        result = 31 * result + neighborhoodName.hashCode();
        result = 31 * result + ward.hashCode();
        return result;
    }
}

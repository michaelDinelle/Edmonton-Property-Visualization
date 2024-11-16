package com.mycompany.app;

import java.util.function.Predicate;

public class Location {
    // Instance variables:
    private final double lat;
    private final double lng;
    private final String point;

    // Constructor:
    public Location(double lat, double lng, String point) {
        this.lat = lat;
        this.lng = lng;
        this.point = point;
    }

    // Getters:
    public double getLat() {return lat;}
    public double getLng() {return lng;}
    public String getPoint() {return point;}

    // Predicates:
    private static final Predicate<Double> validCoord = coord -> coord >= -90 && coord <= 180 && coord != -1;
    private static final Predicate<String> validPoint = point -> point != null && !point.isEmpty();

    // Methods:
    @Override
    public String toString() {
        StringBuilder locationStr = new StringBuilder();

        if (validCoord.test(lat)) {
            locationStr.append("(").append(lat).append(", ");
        }

        if (validCoord.test(lng)) {
            locationStr.append(lng).append(")");
        }

        return locationStr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        if (Double.compare(location.lat, lat) != 0) return false;
        if (Double.compare(location.lng, lng) != 0) return false;
        return point.equals(location.point);
    }

    @Override
    public int hashCode() {
        int result;
        result = Double.hashCode(lat);
        result = 31 * result + Double.hashCode(lng);
        result = 31 * result + point.hashCode();
        return result;
    }
}

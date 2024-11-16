package com.mycompany.app;

public class PropertyAssessment implements Comparable<PropertyAssessment> {
    // Instance variables:
    private final int accountID;
    private final Address address;
    private final String garage;
    private final Neighborhood neighborhood;
    private final long assessedValue;
    private final Location location;
    private final AssessmentClass assessmentClass;

    // Constructor:
    public PropertyAssessment(int accountID, Address address, String garage, Neighborhood neighborhood, long assessedValue, Location location, AssessmentClass assessmentClass) {
        this.accountID = accountID;
        this.address = address;
        this.garage = garage;
        this.neighborhood = neighborhood;
        this.assessedValue = assessedValue;
        this.location = location;
        this.assessmentClass = assessmentClass;
    }

    // Getters:
    public int getAccountID() {return accountID;}
    public Address getAddress() {return address;}
    public String getGarage() {return garage;}
    public Neighborhood getNeighborhood() {return neighborhood;}
    public long getAssessedValue() {return assessedValue;}
    public Location getLocation() {return location;}
    public AssessmentClass getAssessmentClass() {return assessmentClass;}

    // Methods:
    @Override
    public int compareTo(PropertyAssessment otherProperty) {
        return Long.compare(this.assessedValue, otherProperty.assessedValue);
    }
}

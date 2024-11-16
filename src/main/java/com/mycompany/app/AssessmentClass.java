package com.mycompany.app;

import java.util.function.Predicate;

public class AssessmentClass {
    // Instance Variables:
    private final int assessmentPercentage1;
    private final int assessmentPercentage2;
    private final int assessmentPercentage3;
    private final String assessmentClass1;
    private final String assessmentClass2;
    private final String assessmentClass3;

    // Constructor:
    public AssessmentClass(int assessmentPercentage1, int assessmentPercentage2, int assessmentPercentage3, String assessmentClass1, String assessmentClass2, String assessmentClass3) {
        this.assessmentPercentage1 = assessmentPercentage1;
        this.assessmentPercentage2 = assessmentPercentage2;
        this.assessmentPercentage3 = assessmentPercentage3;
        this.assessmentClass1 = assessmentClass1;
        this.assessmentClass2 = assessmentClass2;
        this.assessmentClass3 = assessmentClass3;
    }

    // Getters:
    public int getAssessmentPercentage1() {return assessmentPercentage1;}
    public int getAssessmentPercentage2() {return assessmentPercentage2;}
    public int getAssessmentPercentage3() {return assessmentPercentage3;}
    public String getAssessmentClass1() {return assessmentClass1;}
    public String getAssessmentClass2() {return assessmentClass2;}
    public String getAssessmentClass3() {return assessmentClass3;}

    // Predicates:
    private static final Predicate<String> validAssessmentClass = assessmentClass -> assessmentClass != null && !assessmentClass.isEmpty();
    private static final Predicate<Integer> validAssessmentPercentage = assessmentPercentage -> assessmentPercentage != -1;

    // Methods:
    @Override
    public String toString() {
        StringBuilder assessmentClassesStr = new StringBuilder();
        assessmentClassesStr.append("[");

        if (validAssessmentClass.test(assessmentClass1) && validAssessmentPercentage.test(assessmentPercentage1)) {
            assessmentClassesStr.append(assessmentClass1).append(" ").append(assessmentPercentage1).append("%");
        }

        if (validAssessmentClass.test(assessmentClass2) && validAssessmentPercentage.test(assessmentPercentage2)) {
            if (assessmentClassesStr.length() > 1) {
                assessmentClassesStr.append(", ");
            }
            assessmentClassesStr.append(assessmentClass2).append(" ").append(assessmentPercentage2).append("%");
        }

        if (validAssessmentClass.test(assessmentClass3) && validAssessmentPercentage.test(assessmentPercentage3)) {
            if (assessmentClassesStr.length() > 1) {
                assessmentClassesStr.append(", ");
            }
            assessmentClassesStr.append(assessmentClass3).append(" ").append(assessmentPercentage3).append("%");
        }

        assessmentClassesStr.append("]");
        return assessmentClassesStr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssessmentClass that = (AssessmentClass) o;

        if (assessmentPercentage1 != that.assessmentPercentage1) return false;
        if (assessmentPercentage2 != that.assessmentPercentage2) return false;
        if (assessmentPercentage3 != that.assessmentPercentage3) return false;
        if (!assessmentClass1.equals(that.assessmentClass1)) return false;
        if (!assessmentClass2.equals(that.assessmentClass2)) return false;
        return assessmentClass3.equals(that.assessmentClass3);
    }

    @Override
    public int hashCode() {
        int result = assessmentPercentage1;
        result = 31 * result + assessmentPercentage2;
        result = 31 * result + assessmentPercentage3;
        result = 31 * result + (assessmentClass1 != null ? assessmentClass1.hashCode() : 0);
        result = 31 * result + (assessmentClass2 != null ? assessmentClass2.hashCode() : 0);
        result = 31 * result + (assessmentClass3 != null ? assessmentClass3.hashCode() : 0);
        return result;
    }
}

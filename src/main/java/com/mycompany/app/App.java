/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {

    private MapView mapView;
    private GraphicsOverlay graphicsOverlay;

    // Instance Variables
    private PropertyAssessments propertiesClass;
    private TextArea propertyInfoArea;
    private TextArea propertyStatisticsArea;

    private TitledPane propertyGroupPane;
    private TitledPane accountNumberPane;

    private Button filterButton;
    private ComboBox<String> filterDropdown;
    private ComboBox<String> valueDropdown;
    private TextField accountSearchInput;
    private Button removeFilterButton;
    private Button accountSearchButton;




    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Set API key for ArcGIS
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurFJ2xCDXvFC-uJSDrIEtVkCMkq-W26QCtQCgZQipt1lUwR3Pm3yRRKbeYBv6kCefEqVXMAsnQ4rVMkNdiFC5DLtXmhx_Daydix9ND6gKSfXvNdbEQeQwWhhHluGF5DXHa496Q77CndgVM7EY_nadJd-0J9bw5HiOrqsb4as3xU5lBVtBAp2G1FB5WYInTpK_0C6_6_reJIkqqnHUoC6Ez_o.AT1_V1QXZfZZ";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // Load property data
        loadPropertyData();

        // Set the title and size of the stage and show it
        stage.setTitle("My Map App");
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Create a JavaFX scene with a BorderPane layout
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();

        // Initialize the accordion filter panel and set it to the left
        Accordion accordionFilterPanel = createAccordionFilterPanel();

        // Initialize the statistics Panel
        VBox statisticsPanel = createStatisticsPanel();
        borderPane.setRight(statisticsPanel);

        //Add Buttons to Accordion sub panels
        addButtonsToPropertyGroupPane();
        addButtonsToAccountNumberPane();

        borderPane.setLeft(accordionFilterPanel);

        //Add Button Functionality
        accountSearchButtonFunctionality();
        filterButtonFunctionality();
        removeFilterButtonFunctionality();

        // Create the mapview and graphics overlay
        initializeMap(borderPane);

        // Add all properties to the map initially
        addPropertiesToMap(propertiesClass.getProperties());

        // Center the map on Edmonton
        Point edmontonViewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
        mapView.setViewpointCenterAsync(edmontonViewPoint, 15000);

    }

    private void loadPropertyData() {
        // Load property data
        try {
            propertiesClass = new PropertyAssessments("Property_Assessment_Data_2024.csv");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return; // Exit if data loading fails
        }
    }

    private void initializeMap(BorderPane borderPane) {
        // Create a MapView to display the map
        mapView = new MapView();
        // Create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);
        map.setReferenceScale(10000);
        mapView.setMap(map);

        // Create a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        // Enable scaling of symbols in the graphics overlay
        graphicsOverlay.setScaleSymbols(true); // This will scale the map symbols when zooming in and out
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        borderPane.setCenter(mapView);
    }

    private VBox createStatisticsPanel() {
        VBox statisticsPanel = new VBox(10);

        statisticsPanel.setPadding(new Insets(15));
        statisticsPanel.setAlignment(Pos.TOP_LEFT);
        statisticsPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        statisticsPanel.setPrefWidth(300);

        Label statisticsLabel = new Label("Property Overview");
        statisticsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statisticsLabel.setStyle("-fx-text-fill: #2b5b84;");

        VBox legend = createLegend(); // Add the legend to the panel

        // Initialize the text areas
        propertyInfoArea = new TextArea();
        propertyInfoArea.setEditable(false);
        propertyInfoArea.setWrapText(true);
        propertyInfoArea.setPromptText("Property Info");

        propertyStatisticsArea = new TextArea();
        propertyStatisticsArea.setEditable(false);
        propertyStatisticsArea.setWrapText(true);
        propertyStatisticsArea.setPromptText("Property Group Statistics");



        statisticsPanel.getChildren().addAll(
                legend,
                statisticsLabel,
                propertyInfoArea,        // Add property info area for displaying details
                propertyStatisticsArea  // Add statistics area for group data
        );
        return statisticsPanel;
    }


    private VBox createLegend() {
        VBox legend = new VBox(5);
        legend.setPadding(new Insets(10));
        legend.setStyle("-fx-background-color: rgba(240, 240, 240, 0.8); -fx-background-radius: 5;");
        legend.setAlignment(Pos.TOP_LEFT);

        Label legendLabel = new Label("Legend:");
        legendLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        legendLabel.setStyle("-fx-text-fill: #2b5b84;");

        // Define legend items
        legend.getChildren().addAll(
                createLegendItem("Zero Value", Color.BLACK),
                createLegendItem("Below $50,000", Color.DARKBLUE),
                createLegendItem("$50,000 - $200,000", Color.BLUE),
                createLegendItem("$200,000 - $500,000", Color.YELLOW),
                createLegendItem("$500,000 - $1,000,000", Color.ORANGE),
                createLegendItem("$1,000,000 - $5,000,000", Color.RED),
                createLegendItem("Above $5,000,000", Color.DARKRED)
        );

        return legend;
    }

    private HBox createLegendItem(String labelText, Color color) {
        HBox legendItem = new HBox(5);
        legendItem.setAlignment(Pos.CENTER_LEFT);

        Label colorBox = new Label();
        colorBox.setPrefSize(15, 15);
        colorBox.setStyle("-fx-background-color: " + toHexString(color) + "; -fx-border-color: black;");

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 12));

        legendItem.getChildren().addAll(colorBox, label);
        return legendItem;
    }

    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }



    private Accordion createAccordionFilterPanel() {
        Accordion accordion = new Accordion();

        // Neighborhood filter
        propertyGroupPane = new TitledPane();
        propertyGroupPane.setText("Property Group Search");
        propertyGroupPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        propertyGroupPane.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        propertyGroupPane.setStyle("-fx-text-fill: #2b5b84;");

        // Account Number Filter
        accountNumberPane = new TitledPane();
        accountNumberPane.setText("Account Search");
        accountNumberPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        accountNumberPane.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        accountNumberPane.setStyle("-fx-text-fill: #2b5b84;");



        accordion.getPanes().addAll(propertyGroupPane, accountNumberPane);
        return accordion;
    }

    private Button createButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: white; -fx-background-radius: 5;");
        return button;
    }


    private void addButtonsToPropertyGroupPane(){
        VBox propertyGroupContent = new VBox(10);

        Label filterLabel = new Label("Filter Properties");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        filterLabel.setStyle("-fx-text-fill: #2b5b84;");

        filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll("Neighborhood", "Assessment Class", "Ward");
        filterDropdown.setPromptText("Select a filter");

        valueDropdown = new ComboBox<>();
            valueDropdown.setPromptText("Select a value");

            filterDropdown.setOnAction(event -> {
                String selectedFilter = filterDropdown.getValue();
                populateValues(selectedFilter);
        });

        filterButton = createButton("Apply Filter", "#4CAF50");

        removeFilterButton = createButton("Remove Filters", "#ca072f");

        propertyGroupContent.getChildren().addAll(filterLabel, filterDropdown, valueDropdown, filterButton, removeFilterButton);
        propertyGroupPane.setContent(propertyGroupContent);

    }

    private void addButtonsToAccountNumberPane(){
        VBox accountGroupContent = new VBox(10);
        Label accountSearchLabel = new Label("Search by Account Number:");

        accountSearchInput = new TextField();
        accountSearchInput.setPromptText("Enter the account number");
        accountSearchButton = createButton("Search", "#007ACC");

        accountGroupContent.getChildren().addAll(accountSearchLabel, accountSearchInput, accountSearchButton);
        accountNumberPane.setContent(accountGroupContent);

    }

    private void populateValues(String selectedFilter) {
        valueDropdown.getItems().clear();

        switch (selectedFilter) {
            case "Neighborhood" -> {
                List<String> neighborhoods = propertiesClass.getProperties().stream()
                        .map(p -> p.getNeighborhood().getNeighborhoodName())
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                valueDropdown.getItems().addAll(neighborhoods);

            }
            case "Ward" -> {
                List<String> wards = propertiesClass.getProperties().stream()
                        .map(p -> p.getNeighborhood().getWard())
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                valueDropdown.getItems().addAll(wards);
            }
            case "Assessment Class" -> {
                List<String> assessmentClasses = propertiesClass.getProperties().stream()
                        //.flatMap(p -> Stream.of(p.getAssessmentClass().getAssessmentClass1(), p.getAssessmentClass().getAssessmentClass2()))
                        // .map(p -> p.getAssessmentClass().getAssessmentClass2())
                        .map(p -> Arrays.asList(p.getAssessmentClass().getAssessmentClass1(), p.getAssessmentClass().getAssessmentClass2()))
                        .flatMap(List::stream)
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                valueDropdown.getItems().addAll(assessmentClasses);
            }
        }
    }


    private void accountSearchButtonFunctionality() {
        // Add functionality to Account Search button
        accountSearchButton.setOnAction(event -> {
            String accountNumber = accountSearchInput.getText().trim();
            if (accountNumber.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter an account number.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            PropertyAssessment property = propertiesClass.getProperties().stream()
                    .filter(p -> Integer.toString(p.getAccountID()).equalsIgnoreCase(accountNumber))
                    .findFirst()
                    .orElse(null);
            if (property == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No property found with the given account number.", ButtonType.OK);
                alert.showAndWait();
            } else {
                displayPropertyInfo(property, propertyInfoArea);
                highlightSelectedProperty(property);
            }
        });
    }

    private void filterButtonFunctionality() {
        // Add functionality to the filter button
        filterButton.setOnAction(event -> {
            String selectedFilter = filterDropdown.getValue();
            String filterValue = valueDropdown.getValue();

            if (selectedFilter == null || filterValue.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a filter and enter a value.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            List<PropertyAssessment> filteredProperties;

            if (selectedFilter.equals("Neighborhood")) {
                filteredProperties = propertiesClass.getProperties().stream()
                        .filter(property -> property.getNeighborhood().getNeighborhoodName().equals(filterValue))
                        .collect(Collectors.toList());

                displayPropertyStatisticsInfo(filteredProperties, propertyStatisticsArea, filterValue);
            } else if (selectedFilter.equals("Assessment Class")) {
                filteredProperties = propertiesClass.getProperties().stream()
                        .filter(property -> property.getAssessmentClass().toString().contains(filterValue))
                        .collect(Collectors.toList());

                displayPropertyStatisticsInfo(filteredProperties, propertyStatisticsArea, filterValue);
            }
            else if (selectedFilter.equals("Ward")) {
                filteredProperties = propertiesClass.getProperties().stream()
                        .filter(property -> property.getNeighborhood().getWard().contains(filterValue))
                        .collect(Collectors.toList());

                displayPropertyStatisticsInfo(filteredProperties, propertyStatisticsArea, filterValue);
            }

            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid filter selected.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Update the map with filtered properties
            updateMapWithFilteredProperties(filteredProperties);
        });
    }

    private void removeFilterButtonFunctionality() {
        //Add functionality to remove filters button
        removeFilterButton.setOnAction(event ->{

            graphicsOverlay.getGraphics().clear();
            addPropertiesToMap(propertiesClass.getProperties());
            Point edmontonViewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
            mapView.setViewpointCenterAsync(edmontonViewPoint, 15000);

        });
    }


    private void updateMapWithFilteredProperties(List<PropertyAssessment> filteredProperties) {
        graphicsOverlay.getGraphics().clear();
        addPropertiesToMap(filteredProperties);
    }

    private Color getAssesmentColor(long assessedValue){

        if (assessedValue == 0){
            return Color.BLACK;
        }

        if (assessedValue < 50000){
            return Color.DARKBLUE;

        }
        else if (assessedValue < 200000) {
            return Color.BLUE;
        }
        else if (assessedValue < 500000) {
            return Color.YELLOW;
        }
        else if (assessedValue < 1000000) {
            return Color.ORANGE;
        }
        else if (assessedValue < 50000000)  {
            return Color.RED;
        } else {
            return Color.DARKRED;
        }
    }

    // Display property information
    private void displayPropertyInfo(PropertyAssessment property, TextArea propertyInfoArea) {
        if (property == null) {
            propertyInfoArea.setText("No property information available.");
        } else {

            //For formatting assessed value into a currency
            DecimalFormat numberFormat = new DecimalFormat("#,###");

            propertyInfoArea.setText(String.format(
                            "Account Number: %s%n" +
                            "Address: %s%n%n" +
                            "Garage: %s%n%n"  +
                            "Assessment Value: $%s %n%n" +
                            "Neighborhood: %s%n%n" +
                            "Assessment Class: %s%n%n" +
                            "Latitude: %f%n" +
                            "Longitude %f%n",
                    property.getAccountID(),
                    property.getAddress(),
                    property.getGarage(),
                    numberFormat.format(property.getAssessedValue()),
                    property.getNeighborhood().getNeighborhoodName(),
                    property.getAssessmentClass(),
                    property.getLocation().getLat(),
                    property.getLocation().getLng()
            ));
        }
    }


    // Display property information
    private void displayPropertyStatisticsInfo(List<PropertyAssessment> properties, TextArea propertyInfoArea, String filterValue) {

        PropertyAssessments propertyAssessments = new PropertyAssessments(properties);

        if (properties == null) {
            propertyInfoArea.setText("No property statistics available.");
        } else {

            //For formatting assessed value into a currency
            DecimalFormat numberFormat = new DecimalFormat("#,###");

            propertyInfoArea.setText("Statistics: " + filterValue + "\n" + String.format(
                            "n: %s%n" +
                            "min: $%s%n" +
                            "max: $%s%n"  +
                            "range: $%s%n" +
                            "mean: $%s%n" +
                            "median: $%s%n",
                    propertyAssessments.getNumberOfRecords(),
                    numberFormat.format(propertyAssessments.getMinValue()),
                    numberFormat.format(propertyAssessments.getMaxValue()),
                    numberFormat.format(propertyAssessments.getRange()),
                    numberFormat.format(propertyAssessments.getMean()),
                    numberFormat.format(propertyAssessments.getMedian())
            ));
        }
    }



    // Highlight selected property
    private void highlightSelectedProperty(PropertyAssessment property) {

        // Clear all graphics prior to highlighting
        graphicsOverlay.getGraphics().clear();

        // Highlight the selected property
        SimpleMarkerSymbol selectedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.MAGENTA, 20);
        Point point = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
        // Center the map on the Highlighted property
        mapView.setViewpointCenterAsync(point, 3000);

        Graphic highlightedGraphic = new Graphic(point, selectedSymbol);
        graphicsOverlay.getGraphics().add(highlightedGraphic);

        // Fade surrounding properties by adding semi-transparent markers
        propertiesClass.getProperties().stream()
                .filter(otherProperty -> otherProperty != property) // Exclude the selected property
                .forEach(otherProperty -> {
                    Color fadedColor = getAssesmentColor(otherProperty.getAssessedValue()).deriveColor(0, 1, 1, 0.3); // Reduce opacity
                    SimpleMarkerSymbol fadedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, fadedColor, 15);
                    Point fadedPoint = new Point(otherProperty.getLocation().getLng(), otherProperty.getLocation().getLat(), SpatialReferences.getWgs84());
                    Graphic fadedGraphic = new Graphic(fadedPoint, fadedSymbol);
                    graphicsOverlay.getGraphics().add(fadedGraphic);
                });


    }

    private void addPropertiesToMap(List<PropertyAssessment> properties) {
        for (PropertyAssessment property : properties) {
            Color color = getAssesmentColor(property.getAssessedValue());
            SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, 15);

            Point point = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
            Graphic graphic = new Graphic(point, symbol);
            graphicsOverlay.getGraphics().add(graphic);
        }
    }



    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}

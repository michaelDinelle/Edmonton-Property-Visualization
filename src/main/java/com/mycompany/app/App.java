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
import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {

    private MapView mapView;
    private GraphicsOverlay graphicsOverlay;

    // Instance Variables
    private PropertyAssessments propertiesClass;
    private TextArea propertyInfoArea;
    private Button filterButton;
    private ComboBox<String> filterDropdown;
    private TextField filterInput;
    private Button removeFilterButton;
    private Button accountSearchButton;
    private TextField accountSearchInput;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Set API key for ArcGIS
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurFJ2xCDXvFC-uJSDrIEtVkCMkq-W26QCtQCgZQipt1lUwR3Pm3yRRKbeYBv6kCefEqVXMAsnQ4rVMkNdiFC5DLtXmhx_Daydix9ND6gKSfXvNdbEQeQwWhhHluGF5DXHa496Q77CndgVM7EY_nadJd-0J9bw5HiOrqsb4as3xU5lBVtBAp2G1FB5WYInTpK_0C6_6_reJIkqqnHUoC6Ez_o.AT1_V1QXZfZZ";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // Load property data
        // NOTE: Do this first because loading all our UI is pointless if we have no data loaded
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

        // Create the mapview and graphics overlay
        initializeMap(borderPane);

        // Initialize the filter panel
        VBox filterPanel = createFilterPanel();
        borderPane.setLeft(filterPanel);

        // Add all properties to the map initially
        addPropertiesToMap(propertiesClass.getProperties());

        // Center the map on Edmonton
        Point edmontonViewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
        mapView.setViewpointCenterAsync(edmontonViewPoint, 15000);

        // Add the functionality to the account search button, filter button, and remove filter button
        accountSearchButtonFunctionality();
        filterButtonFunctionality();
        removeFilterButtonFunctionality(edmontonViewPoint);
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

    private VBox createFilterPanel() {
        VBox filterPanel = new VBox(10);
        filterPanel.setPadding(new Insets(15));
        filterPanel.setAlignment(Pos.TOP_LEFT);
        filterPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        filterPanel.setPrefWidth(300);

        Label filterLabel = new Label("Filter Properties");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        filterLabel.setStyle("-fx-text-fill: #2b5b84;");

        filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll("Neighborhood", "Assessment Class");
        filterDropdown.setPromptText("Select a filter");

        filterInput = new TextField();
        filterInput.setPromptText("Enter the filter value");

        filterButton = createButton("Apply Filter", "#4CAF50");
        removeFilterButton = createButton("Remove Filters", "#ca072f");

        Label accountSearchLabel = new Label("Search by Account Number:");
        accountSearchInput = new TextField();
        accountSearchInput.setPromptText("Enter the account number");
        accountSearchButton = createButton("Search", "#007ACC");

        Label propertyInfoLabel = new Label("Property Information");
        propertyInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        propertyInfoLabel.setStyle("-fx-text-fill: #2b5b84;");

        propertyInfoArea = new TextArea();
        propertyInfoArea.setEditable(false);
        propertyInfoArea.setWrapText(true);

//        VBox propertyInfoPanel = createPropertyInfoPanel();

        filterPanel.getChildren().addAll(
                filterLabel,
                new Label("Filter by:"),
                filterDropdown,
                new Label("Filter value:"),
                filterInput,
                filterButton,
                accountSearchLabel,
                accountSearchInput,
                accountSearchButton,
                propertyInfoLabel,
                propertyInfoArea,
                removeFilterButton
        );
        return filterPanel;
    }

    private Button createButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: white; -fx-background-radius: 5;");
        return button;
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
            String filterValue = filterInput.getText().trim();

            if (selectedFilter == null || filterValue.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a filter and enter a value.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            List<PropertyAssessment> filteredProperties;

            if (selectedFilter.equals("Neighborhood")) {
                filteredProperties = propertiesClass.getProperties().stream()
                        .filter(property -> property.getNeighborhood().getNeighborhoodName().equalsIgnoreCase(filterValue))
                        .collect(Collectors.toList());
            } else if (selectedFilter.equals("Assessment Class")) {
                filteredProperties = propertiesClass.getProperties().stream()
                        .filter(property -> property.getAssessmentClass().toString().toLowerCase().contains(filterValue.toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid filter selected.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Update the map with filtered properties
            updateMapWithFilteredProperties(filteredProperties);
        });
    }

    private void removeFilterButtonFunctionality(Point edmontonViewPoint) {
        //Add functionality to remove filters button
        removeFilterButton.setOnAction(event ->{

            graphicsOverlay.getGraphics().clear();
            addPropertiesToMap(propertiesClass.getProperties());

            mapView.setViewpointCenterAsync(edmontonViewPoint, 15000);

        });
    }

    private Color getAssesmentColor(long assessedValue){

        if (assessedValue == 0){
            return Color.BLACK;
        }

        if (assessedValue < 50000){
            return Color.DARKBLUE;

        }
        else if (assessedValue < 100000) {
            return Color.BLUE;
        }

        else if (assessedValue < 200000) {
            return Color.YELLOW;
        }
        else if (assessedValue < 500000) {
            return Color.ORANGE;
        }
        else if (assessedValue < 1000000) {
            return Color.RED;
        }
        else {
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
    private void displayPropertyStatisticsInfo(List<PropertyAssessment> properties, TextArea propertyInfoArea) {

        PropertyAssessments propertyAssessments = new PropertyAssessments(properties);

        if (properties == null) {
            propertyInfoArea.setText("No property statistics available.");
        } else {

            //For formatting assessed value into a currency
            DecimalFormat numberFormat = new DecimalFormat("#,###");

            propertyInfoArea.setText("Statistics: %n%n" + String.format(
                            "n: %s%n" +
                            "min: %s%n" +
                            "max: %s%n"  +
                            "range: $%s%n" +
                            "mean: %s%n" +
                            "median: %s%n",
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
//        graphicsOverlay.getGraphics().clear();
        SimpleMarkerSymbol selectedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.MAGENTA, 20);
        Point point = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
        Graphic highlightedGraphic = new Graphic(point, selectedSymbol);
        graphicsOverlay.getGraphics().add(highlightedGraphic);

        // Center the map on the Highlighted property
        mapView.setViewpointCenterAsync(point, 10000);

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

    private void updateMapWithFilteredProperties(List<PropertyAssessment> filteredProperties) {
        graphicsOverlay.getGraphics().clear();
        if (filteredProperties.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No properties match the filter criteria.", ButtonType.OK);
            alert.showAndWait();
        } else {
            addPropertiesToMap(filteredProperties);
        }
    }

    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}

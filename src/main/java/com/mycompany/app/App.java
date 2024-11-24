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
    private TextArea propertyStatisticsArea;


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

        // Initialize the accordion filter panel and set it to the left
        Accordion accordionFilterPanel = createAccordionFilterPanel();
        borderPane.setLeft(accordionFilterPanel);

        // Create the mapview and graphics overlay
        initializeMap(borderPane);

        // Initialize the statistics Panel
        VBox statisticsPanel = createStatisticsPanel();
        borderPane.setRight(statisticsPanel);

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

    private VBox createStatisticsPanel(){

        VBox statisticsPanel = new VBox(10);

        statisticsPanel.setPadding(new Insets(15));
        statisticsPanel.setAlignment(Pos.TOP_LEFT);
        statisticsPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");
        statisticsPanel.setPrefWidth(300);

        Label statisticsLabel = new Label("Property Group Statistics");
        statisticsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statisticsLabel.setStyle("-fx-text-fill: #2b5b84;");

        propertyStatisticsArea = new TextArea();
        propertyStatisticsArea.setEditable(false);
        propertyStatisticsArea.setWrapText(true);

        VBox legend = createLegend(); // Add the legend to the panel

//        VBox propertyInfoPanel = createPropertyInfoPanel();

        statisticsPanel.getChildren().addAll(
                statisticsLabel,
                propertyStatisticsArea,
                legend
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
        TitledPane neighborhoodPane = createFilterPane("Filter by Neighborhood", "Enter neighborhood name",
                filterValue -> propertiesClass.getProperties().stream()
                        .filter(property -> property.getNeighborhood().getNeighborhoodName().equalsIgnoreCase(filterValue))
                        .collect(Collectors.toList()));

        // Assessment Class Filter
        TitledPane assessmentClassPane = createFilterPane("Filter by Assessment Class", "Enter assessment class",
                filterValue -> propertiesClass.getProperties().stream()
                        .filter(property -> property.getAssessmentClass().toString().toLowerCase().contains(filterValue.toLowerCase()))
                        .collect(Collectors.toList()));

        // Account Number Filter
        TitledPane accountNumberPane = createAccountNumberFilterPane();

        accordion.getPanes().addAll(neighborhoodPane, assessmentClassPane, accountNumberPane);
        return accordion;
    }


    private TitledPane createFilterPane(String title, String prompt, java.util.function.Function<String, List<PropertyAssessment>> filterFunction) {
        TitledPane pane = new TitledPane();
        pane.setText(title);

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        TextField input = new TextField();
        input.setPromptText(prompt);

        Button applyButton = new Button("Apply Filter");
        applyButton.setOnAction(event -> {
            String filterValue = input.getText().trim();
            if (filterValue.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a value.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            List<PropertyAssessment> filteredProperties = filterFunction.apply(filterValue);
            updateMapWithFilteredProperties(filteredProperties);
        });

        box.getChildren().addAll(input, applyButton);
        pane.setContent(box);
        return pane;
    }

    private TitledPane createAccountNumberFilterPane() {
        TitledPane pane = new TitledPane();
        pane.setText("Filter by Account Number");

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        TextField accountInput = new TextField();
        accountInput.setPromptText("Enter account number");

        Button applyButton = new Button("Search");
        applyButton.setOnAction(event -> {
            String accountNumber = accountInput.getText().trim();
            if (accountNumber.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter an account number.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            try {
                // Use findPropertyByAccountID to locate the property
                PropertyAssessment property = propertiesClass.findPropertyByAccountID(accountNumber);

                if (property == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No property found with the given account number.", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    System.out.println("Property Found: " + property);
                    // Highlight and display statistics for the selected property
                    displayPropertyStatisticsInfo(List.of(property), propertyStatisticsArea);
                    highlightSelectedProperty(property);
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid account number format. Please enter a numeric value.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        box.getChildren().addAll(accountInput, applyButton);
        pane.setContent(box);
        return pane;
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
    private void displayPropertyStatisticsInfo(List<PropertyAssessment> properties, TextArea propertyInfoArea) {

        PropertyAssessments propertyAssessments = new PropertyAssessments(properties);

        if (properties == null) {
            propertyInfoArea.setText("No property statistics available.");
        } else {

            //For formatting assessed value into a currency
            DecimalFormat numberFormat = new DecimalFormat("#,###");

            propertyInfoArea.setText("Statistics: \n" + String.format(
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



    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}

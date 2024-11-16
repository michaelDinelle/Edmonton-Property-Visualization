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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {

    private MapView mapView;
    private GraphicsOverlay graphicsOverlay;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        // Set the title and size of the stage and show it
        stage.setTitle("My Map App");
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.show();

        // Create a JavaFX scene with a BorderPane layout
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        // Set API key for ArcGIS
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurFJ2xCDXvFC-uJSDrIEtVkCMkq-W26QCtQCgZQipt1lUwR3Pm3yRRKbeYBv6kCefEqVXMAsnQ4rVMkNdiFC5DLtXmhx_Daydix9ND6gKSfXvNdbEQeQwWhhHluGF5DXHa496Q77CndgVM7EY_nadJd-0J9bw5HiOrqsb4as3xU5lBVtBAp2G1FB5WYInTpK_0C6_6_reJIkqqnHUoC6Ez_o.AT1_V1QXZfZZ";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // Create a MapView to display the map
        mapView = new MapView();
        borderPane.setCenter(mapView);

        // Create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);

        // Set the reference scale for the map
        map.setReferenceScale(10000);

        mapView.setMap(map);

        // Create a graphics overlay
        graphicsOverlay = new GraphicsOverlay();

        // Enable scaling of symbols in the graphics overlay
        graphicsOverlay.setScaleSymbols(true);

        mapView.getGraphicsOverlays().add(graphicsOverlay);


        // Load property data
        final PropertyAssessments propertiesClass;
        try {
            propertiesClass = new PropertyAssessments("Property_Assessment_Data_2024.csv");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return; // Exit if data loading fails
        }

        // Center the map on Edmonton
        Point viewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
        mapView.setViewpointCenterAsync(viewPoint, 15000);

        // Add all properties to the map initially
        addPropertiesToMap(propertiesClass.getProperties());


        // Create a filter panel using VBox
        VBox filterPanel = new VBox(10);
        filterPanel.setPadding(new Insets(15));
        filterPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10;");

        // Filter label
        Label filterLabel = new Label("Filter Properties:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        filterLabel.setStyle("-fx-text-fill: #2b5b84;");

        // Dropdown for filtering options
        ComboBox<String> filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll("Neighborhood", "Assessment Class");
        filterDropdown.setPromptText("Select a filter");

        // Input field for filter value
        TextField filterInput = new TextField();
        filterInput.setPromptText("Enter filter value");

        // Button to apply the filter
        Button filterButton = new Button("Apply Filter");
        filterButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        filterButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");

        // Add elements to filter panel
        filterPanel.getChildren().addAll(filterLabel, new Label("Filter by:"), filterDropdown, new Label("Filter value:"), filterInput, filterButton);

        // Wrap the filter panel in a StackPane to position it
        StackPane filterContainer = new StackPane(filterPanel);
        filterContainer.setPadding(new Insets(20));
        filterContainer.setPrefWidth(300);

        // Position the filter panel to the top-left corner
        StackPane.setMargin(filterPanel, new Insets(10, 0, 0, 10));
        borderPane.setLeft(filterContainer);


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

    private Color getAssesmentColor(long assessedValue){

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

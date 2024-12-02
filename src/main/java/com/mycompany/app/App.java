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
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class App extends Application {
    // Global variables that might need to change
    private final Integer initialScreenWidth = 1000;
    private final Integer initialScreenHeight = 800;
    private final Integer minScreenWidth = 800;
    private final Integer minScreenHeight = 600;

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

    private VBox statisticsPanel;
    private Label statisticsLabel;
    private StackPane rootStackPane;

    private VBox legend;
    private HBox legendItem;

    private Button toggleStatsButton;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initializeArcGISRuntime();

        initializeStage(stage);

        // Create a StackPane as the root layout
        rootStackPane = new StackPane();

        // Load property data
        loadPropertyData();

        // Add all properties to the map initially
        addPropertiesToMap(propertiesClass.getProperties());

        // Initialize all UI components
        mapView = createMapLayout();
        Accordion accordionFilterPanel = createAccordionFilterPanel();
        VBox statisticsPanel = createStatisticsPanel();
        Button toggleStatsButton = createToggleButton();

        // Add all components to the StackPane in the correct order
        setupStackPane(mapView, accordionFilterPanel, statisticsPanel, toggleStatsButton);

        //Add Button Functionality
        accountSearchButtonFunctionality();
        filterButtonFunctionality();
        removeFilterButtonFunctionality();

        // Add click functionality to each point on the map
        setupClickHandler();

        // Create the scene, apply the styling and show it on the screen
        Scene scene = new Scene(rootStackPane);
        applyStylesToScene(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void applyStylesToScene(Scene scene){
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        propertyGroupPane.getStyleClass().add("property-group-pane");
        accountNumberPane.getStyleClass().add("account-number-pane");

        statisticsPanel.getStyleClass().add("statistics-panel");
        statisticsLabel.getStyleClass().add("statistics-label");

        legend.getStyleClass().add("legend");
        legendItem.getStyleClass().add("legend-item");

        toggleStatsButton.getStyleClass().add("toggle-stats-button");
        filterButton.getStyleClass().add("filter-button");
        removeFilterButton.getStyleClass().add("remove-filter-button");
        accountSearchButton.getStyleClass().add("account-search-button");
    }

    private void initializeArcGISRuntime() {
        // Set API key for ArcGIS
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurFJ2xCDXvFC-uJSDrIEtVkCMkq-W26QCtQCgZQipt1lUwR3Pm3yRRKbeYBv6kCefEqVXMAsnQ4rVMkNdiFC5DLtXmhx_Daydix9ND6gKSfXvNdbEQeQwWhhHluGF5DXHa496Q77CndgVM7EY_nadJd-0J9bw5HiOrqsb4as3xU5lBVtBAp2G1FB5WYInTpK_0C6_6_reJIkqqnHUoC6Ez_o.AT1_V1QXZfZZ";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);
    }

    private void initializeStage(Stage stage) {
        // Set the title and size of the stage and show it
        stage.setTitle("Edmonton Properties Map");
        stage.setWidth(initialScreenWidth);
        stage.setHeight(initialScreenHeight);
        stage.setMinWidth(minScreenWidth);
        stage.setMinHeight(minScreenHeight);
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

    private void addPropertiesToMap(List<PropertyAssessment> properties) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Graphic> graphics = new ArrayList<>();

                for (PropertyAssessment property : properties) {
                    // Generate color and symbol
                    Color color = getAssesmentColor(property.getAssessedValue());
                    SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, 15);

                    // Create the graphic
                    Point point = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
                    Graphic graphic = new Graphic(point, symbol);

                    graphic.getAttributes().put("accountID", property.getAccountID());

                    graphics.add(graphic);

                    // Update progress
                    updateProgress(graphics.size(), properties.size());
                }

                // Add graphics to the overlay on the JavaFX thread
                Platform.runLater(() -> graphicsOverlay.getGraphics().addAll(graphics));
                return null;
            }
        };

        VBox loadingContainer = createLoadingContainer("Loading Data", task);

        // Add the loading container to the StackPane
        Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer)); // rootStackPane is the root of your Scene

        // Remove the loading container once the task is complete
        task.setOnSucceeded(e -> Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer)));

        // Remove the loading container in case of failure
        task.setOnFailed(e -> {
            Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
            task.getException().printStackTrace();
        });

        // Run the task in a background thread
        new Thread(task).start();
    }

    private MapView createMapLayout() {
        MapView mapView = new MapView();
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY); // Creates a new ArcGIS map
        map.setReferenceScale(10000);
        mapView.setMap(map);// Sets the scaling for when the symbols on the map should appear

        // Center the map on Edmonton
        Point edmontonViewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
        mapView.setViewpointCenterAsync(edmontonViewPoint, 15000);

        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.setScaleSymbols(true);
        mapView.getGraphicsOverlays().add(graphicsOverlay);
        return mapView;
    }

    private Accordion createAccordionFilterPanel() {
        Accordion accordion = new Accordion();

        // Neighborhood filter
        propertyGroupPane = new TitledPane();
        propertyGroupPane.setText("Property Group Search");

        // Account Number Filter
        accountNumberPane = new TitledPane();
        accountNumberPane.setText("Account Search");

        //Add Buttons to Accordion sub panes
        addButtonsToPropertyGroupPane();
        addButtonsToAccountNumberPane();

        accordion.getPanes().addAll(propertyGroupPane, accountNumberPane);

        accordion.setPrefWidth(250);

        accordion.setMinWidth(200);
        accordion.setMaxWidth(300);
        accordion.setMaxHeight(Region.USE_PREF_SIZE);
        accordion.setPrefHeight(Region.USE_COMPUTED_SIZE);
        return accordion;
    }

    private VBox createStatisticsPanel() {
        statisticsPanel = new VBox(10);

        statisticsPanel.setPrefWidth(300);
        statisticsPanel.setMaxWidth(300);

        statisticsLabel = new Label("Property Overview");
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

        statisticsPanel.setMaxHeight(Region.USE_PREF_SIZE);
        statisticsPanel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        return statisticsPanel;
    }

    private VBox createLegend() {
        legend = new VBox(5);

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
        legendItem = new HBox(5);

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

    private Button createToggleButton() {
        toggleStatsButton = new Button("Hide Statistics");
        toggleStatsButtonFunctionality();
        return toggleStatsButton;
    }

    private void toggleStatsButtonFunctionality() {
        toggleStatsButton.setOnAction(event -> {
            if (rootStackPane.getChildren().contains(statisticsPanel)) {
                // Hide the statistics panel
                rootStackPane.getChildren().remove(statisticsPanel);
                StackPane.setMargin(toggleStatsButton, new Insets(10));
                toggleStatsButton.setText("Show Statistics");
            } else {
                // Show the statistics panel
                rootStackPane.getChildren().add(statisticsPanel);
                StackPane.setAlignment(statisticsPanel, Pos.TOP_RIGHT);
                StackPane.setMargin(statisticsPanel, new Insets(10));
                StackPane.setMargin(toggleStatsButton, new Insets(10, 320, 0, 320));
                toggleStatsButton.setText("Hide Statistics");
            }
        });
    }

    private void setupStackPane(MapView mapLayout, Accordion accordionFilterPanel, VBox statisticsPanel, Button toggleStatsButton) {
        // Add the map layout (background layer)
        rootStackPane.getChildren().add(mapLayout);

        // Add the filter panel (top-left corner)
        StackPane.setAlignment(accordionFilterPanel, Pos.TOP_LEFT);
        StackPane.setMargin(accordionFilterPanel, new Insets(10));
        rootStackPane.getChildren().add(accordionFilterPanel);

        // Add the statistics panel (top-right corner)
        StackPane.setAlignment(statisticsPanel, Pos.TOP_RIGHT);
        StackPane.setMargin(statisticsPanel, new Insets(10));
        rootStackPane.getChildren().add(statisticsPanel);

        // Add the statistics panel toggle button
        StackPane.setAlignment(toggleStatsButton, Pos.TOP_RIGHT);
        StackPane.setMargin(toggleStatsButton, new Insets(10, 320, 0, 320));
        rootStackPane.getChildren().add(toggleStatsButton);
    }

    private Button createButton(String text) {
        Button button = new Button(text);
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

        filterButton = createButton("Apply Filter");

        removeFilterButton = createButton("Remove Filters");

        propertyGroupContent.getChildren().addAll(filterLabel, filterDropdown, valueDropdown, filterButton, removeFilterButton);
        propertyGroupPane.setContent(propertyGroupContent);

    }

    private void addButtonsToAccountNumberPane(){
        VBox accountGroupContent = new VBox(10);
        Label accountSearchLabel = new Label("Search by Account Number:");

        accountSearchInput = new TextField();
        accountSearchInput.setPromptText("Enter the account number");
        accountSearchButton = createButton("Search");

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
            String accountNumberStr = accountSearchInput.getText().trim();
            if (accountNumberStr.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter an account number.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            try {
                int accountNumber = Integer.parseInt(accountNumberStr);
                PropertyAssessment property = propertiesClass.getPropertyByAccountID(accountNumber);

                if (property == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No property found with the given account number.", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    displayPropertyInfo(property, propertyInfoArea);
                    highlightSelectedProperty(property);
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Account number must be a valid number", ButtonType.OK);
            }
        });
    }

    private void filterButtonFunctionality() {
        filterButton.setOnAction(event -> {
            String selectedFilter = filterDropdown.getValue();
            String filterValue = valueDropdown.getValue();

            if (selectedFilter == null || filterValue.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a filter and enter a value.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Background task for filtering
            Task<List<PropertyAssessment>> task = new Task<>() {
                @Override
                protected List<PropertyAssessment> call() {
                    List<PropertyAssessment> filteredProperties;
                    if (selectedFilter.equals("Neighborhood")) {
                        filteredProperties = propertiesClass.getProperties().stream()
                                .filter(property -> property.getNeighborhood().getNeighborhoodName().equals(filterValue))
                                .collect(Collectors.toList());
                    } else if (selectedFilter.equals("Assessment Class")) {
                        filteredProperties = propertiesClass.getProperties().stream()
                                .filter(property -> property.getAssessmentClass().toString().contains(filterValue))
                                .collect(Collectors.toList());
                    } else if (selectedFilter.equals("Ward")) {
                        filteredProperties = propertiesClass.getProperties().stream()
                                .filter(property -> property.getNeighborhood().getWard().contains(filterValue))
                                .collect(Collectors.toList());
                    } else {
                        return null; // Invalid filter
                    }

                    // Simulate progress
                    for (int i = 0; i < 10; i++) {
                        updateProgress(i + 1, 10);
                        try {
                            Thread.sleep(50); // Simulated delay
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return filteredProperties;
                }
            };

            VBox loadingContainer = createLoadingContainer("Applying Filter", task);

            // Add the loading container to the rootStackPane
            Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer));

            task.setOnSucceeded(e -> {
                // Remove the loading container
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));

                // Update map and display statistics
                List<PropertyAssessment> filteredProperties = task.getValue();
                if (filteredProperties != null) {
                    displayPropertyStatisticsInfo(filteredProperties, propertyStatisticsArea, filterValue);
                    updateMapWithFilteredProperties(filteredProperties);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid filter selected.", ButtonType.OK);
                    alert.showAndWait();
                }
            });

            task.setOnFailed(e -> {
                // Remove the loading container in case of failure
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                task.getException().printStackTrace();
            });

            // Start the task in a background thread
            new Thread(task).start();
        });
    }

    private void removeFilterButtonFunctionality() {
        removeFilterButton.setOnAction(event -> {
            // Create a background task to simulate progress
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Simulate progress
                    for (int i = 0; i <= 10; i++) {
                        updateProgress(i, 10);
                        Thread.sleep(50); // Simulated delay
                    }
                    return null;
                }
            };

            // Create a loading container with a progress bar and label
            VBox loadingContainer = createLoadingContainer("Removing Filters...", task);

            // Add the loading container to the root stack pane
            Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer));

            // When the task succeeds, clear the filters and reset the map
            task.setOnSucceeded(e -> {
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                graphicsOverlay.getGraphics().clear(); // Clear all graphics
                addPropertiesToMap(propertiesClass.getProperties()); // Re-add all properties
                Point edmontonViewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
                mapView.setViewpointCenterAsync(edmontonViewPoint, 15000); // Reset the view
            });

            // Handle task failure
            task.setOnFailed(e -> {
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                e.getSource().getException().printStackTrace();
            });

            // Run the task in a background thread
            new Thread(task).start();
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
                            "Number of properties: %s%n" +
                            "Minimum property value: $%s%n" +
                            "Maximum property value: $%s%n"  +
                            "Property value Range: $%s%n" +
                            "Mean Property Value: $%s%n" +
                            "Median Property Value: $%s%n",
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
        // Background task for preparing graphics
        Task<List<Graphic>> task = new Task<>() {
            @Override
            protected List<Graphic> call() {
                List<Graphic> fadedGraphics = new ArrayList<>();
                List<PropertyAssessment> properties = propertiesClass.getProperties();

                for (int i = 0; i < properties.size(); i++) {
                    PropertyAssessment otherProperty = properties.get(i);

                    if (otherProperty != property) { // Exclude the selected property
                        Color fadedColor = getAssesmentColor(otherProperty.getAssessedValue()).deriveColor(0, 1, 1, 0.3);
                        SimpleMarkerSymbol fadedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, fadedColor, 15);
                        Point fadedPoint = new Point(otherProperty.getLocation().getLng(), otherProperty.getLocation().getLat(), SpatialReferences.getWgs84());
                        Graphic fadedGraphic = new Graphic(fadedPoint, fadedSymbol);
                        fadedGraphics.add(fadedGraphic);
                    }

                    // Update progress
                    updateProgress(i + 1, properties.size());
                }

                // Prepare the highlighted graphic
                Point highlightedPoint = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
                SimpleMarkerSymbol highlightedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.MAGENTA, 20);
                fadedGraphics.add(new Graphic(highlightedPoint, highlightedSymbol));

                return fadedGraphics;
            }
        };

        VBox loadingContainer = createLoadingContainer("Loading Data", task);

        // Add the loading container to the StackPane
        Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer)); // rootStackPane is the root of your Scene

        task.setOnSucceeded(e -> {
            // Remove the loading container
            Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));

            // Update graphics overlay and map viewpoint
            graphicsOverlay.getGraphics().clear();
            graphicsOverlay.getGraphics().addAll(task.getValue()); // Add all graphics in one batch

            // Center the map on the selected property
            Point centerPoint = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
            mapView.setViewpointCenterAsync(centerPoint, 3000);
        });

        task.setOnFailed(e -> {
            // Remove the progress bar in case of failure
            Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
            e.getSource().getException().printStackTrace();
        });

        // Start the task in a background thread
        new Thread(task).start();
    }

    private VBox createLoadingContainer(String loadingMessage, Task<?> task) {
        // Create ProgressBar and Loading Label
        ProgressBar progressBar = new ProgressBar();
        Label loadingLabel = new Label(loadingMessage);

        // Apply CSS class to the loading label
        loadingLabel.getStyleClass().add("loading-label");

        // Create a container for the loading UI
        VBox loadingContainer = new VBox(10, loadingLabel, progressBar);

        // Apply CSS class to the loading container
        loadingContainer.getStyleClass().add("loading-container");
        loadingContainer.setAlignment(Pos.CENTER);

        // Bind the task progress to the ProgressBar
        progressBar.progressProperty().bind(task.progressProperty());

        return loadingContainer;
    }

    private void setupClickHandler() {
        mapView.setOnMouseClicked(event -> {
            if (event.isStillSincePress()) { // Ensure it's not a drag
                Point2D screenPoint = new Point2D(event.getX(), event.getY()); // Screen coordinates where the user clicked

                // Perform a hit test on the GraphicsOverlay
                ListenableFuture<IdentifyGraphicsOverlayResult> future = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10, false, 1);

                // Add a listener to process the result once it's available
                future.addDoneListener(() -> {
                    try {
                        // Get the result of the identify operation
                        IdentifyGraphicsOverlayResult result = future.get();

                        // Retrieve the list of identified graphics
                        List<Graphic> graphics = result.getGraphics();

                        if (!graphics.isEmpty()) {
                            // Get the first graphic that was clicked
                            Graphic clickedGraphic = graphics.get(0);

                            // Retrieve the accountID attribute
                            Integer accountID = (Integer) clickedGraphic.getAttributes().get("accountID");

                            if (accountID != null) {
                                // Use the accountID to find the PropertyAssessment object
                                PropertyAssessment property = propertiesClass.getPropertyByAccountID(accountID);

                                // Display the property info in the info area
                                if (property != null) {
                                    displayPropertyInfo(property, propertyInfoArea);
                                    highlightSelectedProperty(property);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // Handle exceptions such as InterruptedException or ExecutionException
                    }
                });
            }
        });
    }

    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}
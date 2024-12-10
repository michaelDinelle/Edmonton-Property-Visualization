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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.chart.PieChart;

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
    private Label legendLabel;

    private PieChart classesPieChart;

    private Button toggleStatsButton;
    private Button toggleLegendButton;

    private long assessedValueCenter;

    private VBox legendPanel;

    private NumberFormat numberFormat;

    private ToggleGroup garageFilterGroup;

    private TextField centerInputField;


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {


        //For formatting assessed value into a currency
        numberFormat = new DecimalFormat("#,###");

        initializeArcGISRuntime();

        initializeStage(stage);

        // Create a StackPane as the root layout
        rootStackPane = new StackPane();

        // Load property data
        loadPropertyData();

        //Choose Median to be center
        assessedValueCenter = propertiesClass.getMedian();

        // Add all properties to the map initially
        addPropertiesToMap(propertiesClass.getProperties());

        // Initialize all UI components
        mapView = createMapLayout();
        Accordion accordionFilterPanel = createAccordionFilterPanel();
        statisticsPanel = createStatisticsPanel();
        toggleStatsButton = createStatsToggleButton();
        legendPanel = createLegendPanel();
        toggleLegendButton = createLegendToggleButton();

        // Add all components to the StackPane in the correct order
        setupStackPane(mapView, accordionFilterPanel, statisticsPanel, toggleStatsButton, legendPanel, toggleLegendButton);

        //Add Button Functionality
        accountSearchButtonFunctionality();
        filterButtonFunctionality();
        removeFilterButtonFunctionality();
        centerInputFieldFunctionality();

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
        legendLabel.getStyleClass().add("legend-label");
        toggleLegendButton.getStyleClass().add("toggle-legend-button");

        legendPanel.getStyleClass().add("legend-panel");

        classesPieChart.getStyleClass().add("pie-chart");


        toggleStatsButton.getStyleClass().add("toggle-stats-button");
        filterButton.getStyleClass().add("filter-button");
        removeFilterButton.getStyleClass().add("remove-filter-button");
        accountSearchButton.getStyleClass().add("account-search-button");
    }

    public static String readApiKey(String fileName) {
        try {
            Path filePath = Path.of(fileName);
            System.out.println("Looking for API key file at: " + filePath.toAbsolutePath());
            return Files.readString(filePath).trim();
        } catch (IOException e) {
            System.err.println("Error reading the API key: " + e.getMessage());
            return null;
        }
    }

    private void initializeArcGISRuntime() {
        // Set API key for ArcGIS
        // Please paste your key into a file named secret.txt
        String yourApiKey = readApiKey("secret.txt");
        System.out.println(yourApiKey);
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

    private VBox createLegendPanel(){
        legendPanel = new VBox();

        legendLabel = new Label("Legend");
        legendLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        legendLabel.setStyle("-fx-text-fill: #2b5b84;");

        centerInputField = new TextField();
        centerInputField.setPromptText("Enter a new value to center the map around");

        legend = createLegend(); // Add the legend to the panel
        legendPanel.getChildren().addAll(
                legendLabel,
                legend,
                centerInputField

        );

        legendPanel.setPrefWidth(300);
        legendPanel.setMaxWidth(300);

        legendPanel.setMaxHeight(Region.USE_PREF_SIZE);
        legendPanel.setPrefHeight(Region.USE_COMPUTED_SIZE);

        return legendPanel;


    }

    private VBox createStatisticsPanel() {
        statisticsPanel = new VBox(10);

        statisticsPanel.setPrefWidth(300);
        statisticsPanel.setMaxWidth(300);


        statisticsLabel = new Label("Property Overview");
        statisticsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statisticsLabel.setStyle("-fx-text-fill: #2b5b84;");


        // Initialize the text areas
        propertyInfoArea = new TextArea();
        propertyInfoArea.setEditable(false);
        propertyInfoArea.setWrapText(true);
        propertyInfoArea.setPromptText("Property Info");

        propertyStatisticsArea = new TextArea();
        propertyStatisticsArea.setEditable(false);
        propertyStatisticsArea.setWrapText(true);
        propertyStatisticsArea.setPromptText("Property Group Statistics");

        // Initialize pie chart
        classesPieChart = new PieChart();
        classesPieChart.setVisible(false);
        classesPieChart.setManaged(false);
        classesPieChart.setLegendVisible(false);
        classesPieChart.setMinHeight(0);
        classesPieChart.setMinWidth(0);



        statisticsPanel.getChildren().addAll(

                statisticsLabel,
                propertyInfoArea,        // Add property info area for displaying details
                classesPieChart,        // Add pie chart displaying property assessment classes
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
        refreshLegend();

        return legend;
    }

    private void refreshLegend(){
        legend.getChildren().clear();
        // Define legend items
        legend.getChildren().addAll(
                createLegendItem("Zero Value: $0", Color.BLACK),
                createLegendItem(String.format("50%% Below Center: $%s", numberFormat.format(assessedValueCenter*0.5)), Color.web("#4b2ca3")),
                createLegendItem(String.format("$30%% Below Center: $%s", numberFormat.format(assessedValueCenter * 0.7)), Color.web("#0077bb")),
                createLegendItem(String.format("$15%% Below Center: $%s", numberFormat.format(assessedValueCenter * 0.85)), Color.web("#00b891")),
                createLegendItem(String.format("$5%% Below Center: $%s", numberFormat.format(assessedValueCenter * 0.95)), Color.web("#6ccc63")),
                createLegendItem(String.format("$2%% Below Center: $%s", numberFormat.format(assessedValueCenter * 0.98)), Color.web("#d9ed4c")),
                createLegendItem(String.format("Center: $%s",numberFormat.format(assessedValueCenter)), Color.web("#ffff66")),
                createLegendItem(String.format("$2%% Above Center: $%s", numberFormat.format(assessedValueCenter * 1.02)),Color.web("#ffcc33")),
                createLegendItem(String.format("$5%% Above Center: $%s",numberFormat.format(assessedValueCenter * 1.05)) ,Color.web("#ff8c00")),
                createLegendItem(String.format("$15%% Above Center: $%s", numberFormat.format(assessedValueCenter * 1.15)), Color.web("#e64a19")),
                createLegendItem(String.format("$30%% Above Center: $%s", numberFormat.format(assessedValueCenter * 1.3)) ,Color.web("#c70039")),
                createLegendItem(String.format("$50%% Above Center: $%s",numberFormat.format(assessedValueCenter * 1.5)) ,Color.web("#800026")),
                createLegendItem("Selected", Color.MAGENTA)
        );

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

    private Button createStatsToggleButton() {
        toggleStatsButton = new Button("Hide Statistics");
        toggleStatsButtonFunctionality();
        return toggleStatsButton;
    }

    private Button createLegendToggleButton() {
        toggleLegendButton = new Button("Hide Legend");
        toggleLegendButtonFunctionality();
        return toggleLegendButton;
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

    private void toggleLegendButtonFunctionality() {
        toggleLegendButton.setOnAction(event -> {
            if (rootStackPane.getChildren().contains(legendPanel)) {
                // Hide the statistics panel
                rootStackPane.getChildren().remove(legendPanel);
                StackPane.setMargin(toggleLegendButton, new Insets(10, 320, 20, 320));
                toggleLegendButton.setText("Show Legend");
            } else {
                // Show the statistics panel
                rootStackPane.getChildren().add(legendPanel);
                StackPane.setAlignment(legendPanel, Pos.BOTTOM_LEFT);
                StackPane.setMargin(legendPanel, new Insets(170, 10,20 ,0));
                StackPane.setMargin(toggleLegendButton, new Insets(10, 320, 20, 320));
                toggleLegendButton.setText("Hide Legend");
            }
        });
    }


    private void setupStackPane(MapView mapLayout, Accordion accordionFilterPanel, VBox statisticsPanel, Button toggleStatsButton, VBox legendPanel, Button toggleLegendButton) {
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

        // Add the legend panel
        StackPane.setAlignment(legendPanel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(legendPanel, new Insets(170, 10,20 ,0));
        rootStackPane.getChildren().add(legendPanel);

        // Add the legend toggle button
        StackPane.setAlignment(toggleLegendButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(toggleLegendButton, new Insets(10, 320, 20, 320));
        rootStackPane.getChildren().add(toggleLegendButton);


    }

    private Button createButton(String text) {
        Button button = new Button(text);
        return button;
    }

    private TextField priceInputField;
    private ComboBox<String> priceComparisonDropdown;

    private void addButtonsToPropertyGroupPane() {
        VBox propertyGroupContent = new VBox(10);

        Label filterLabel = new Label("Filter Properties");
        filterLabel.getStyleClass().add("filter-label");

        filterDropdown = new ComboBox<>();
        filterDropdown.getItems().addAll("Neighborhood", "Assessment Class", "Ward");
        filterDropdown.setPromptText("Select a filter");

        valueDropdown = new ComboBox<>();
        valueDropdown.setPromptText("Select a value");

        filterDropdown.setOnAction(event -> {
            String selectedFilter = filterDropdown.getValue();
            populateValues(selectedFilter);
        });

        // Garage filter group
        garageFilterGroup = new ToggleGroup();

        RadioButton garageAllButton = new RadioButton("All");
        garageAllButton.setToggleGroup(garageFilterGroup);
        garageAllButton.setSelected(true);

        RadioButton garageYesButton = new RadioButton("Yes");
        garageYesButton.setToggleGroup(garageFilterGroup);

        RadioButton garageNoButton = new RadioButton("No");
        garageNoButton.setToggleGroup(garageFilterGroup);

        VBox garageFilterBox = new VBox(5);
        garageFilterBox.getChildren().addAll(new Label("Garage Filter:"), garageAllButton, garageYesButton, garageNoButton);

        // Price filtering controls
        Label priceFilterLabel = new Label("Price Filter:");
        priceInputField = new TextField();
        priceInputField.setPromptText("Enter price (e.g., 100000)");

        priceComparisonDropdown = new ComboBox<>();
        priceComparisonDropdown.getItems().addAll("Under", "Equal", "Above");
        priceComparisonDropdown.setPromptText("Select comparison");

        VBox priceFilterBox = new VBox(5);
        priceFilterBox.getChildren().addAll(priceFilterLabel, priceInputField, priceComparisonDropdown);

        filterButton = createButton("Apply Filter");
        removeFilterButton = createButton("Remove Filters");

        propertyGroupContent.getChildren().addAll(filterLabel, filterDropdown, valueDropdown, garageFilterBox, priceFilterBox, filterButton, removeFilterButton);
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
                        .map(p -> Arrays.asList(p.getAssessmentClass().getAssessmentClass1(), p.getAssessmentClass().getAssessmentClass2()))
                        .flatMap(List::stream)
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList());
                valueDropdown.getItems().addAll(assessmentClasses);
            }
        }
    }

    private void centerInputFieldFunctionality(){
        centerInputField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode().equals(KeyCode.ENTER)){

                    //Get value from input field -> need to alert upon invalid entry

                    String centerString = centerInputField.getText().trim();


                    final Long newCenter; // Declare priceValue as final
                    if (centerString.isEmpty()) {
                        try {
                            newCenter = Long.parseLong(centerString);
                        } catch (NumberFormatException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid price value. Please enter a valid number.", ButtonType.OK);
                            alert.showAndWait();
                            return;
                        }
                    }else {
                        newCenter = null;
                    }

                    assessedValueCenter = Long.parseLong(centerString);

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
                    VBox loadingContainer = createLoadingContainer("recentering", task);

                    // Add the loading container to the root stack pane
                    Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer));

                    // When the task succeeds, clear the filters and reset the map
                    task.setOnSucceeded(e -> {
                        Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                        graphicsOverlay.getGraphics().clear(); // Clear all graphics
                        addPropertiesToMap(propertiesClass.getProperties()); // Re-add all properties
                        //Redraw legend
                        refreshLegend();

                    });

                    // Handle task failure
                    task.setOnFailed(e -> {
                        Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                        e.getSource().getException().printStackTrace();
                    });

                    // Run the task in a background thread
                    new Thread(task).start();


                }

            }
        });
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
                    displayPropertyInfo(property);
                    displayPieChart(property);
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

            RadioButton selectedGarageButton = (RadioButton) garageFilterGroup.getSelectedToggle();
            String garageFilter = selectedGarageButton.getText();

            String priceComparison = priceComparisonDropdown.getValue();
            String priceInput = priceInputField.getText().trim();

            final Long priceValue; // Declare priceValue as final
            if (!priceInput.isEmpty()) {
                try {
                    priceValue = Long.parseLong(priceInput);
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid price value. Please enter a valid number.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            } else {
                priceValue = null; // No price filtering if input is empty
            }

            Task<List<PropertyAssessment>> task = new Task<>() {
                @Override
                protected List<PropertyAssessment> call() {
                    List<PropertyAssessment> filteredProperties = propertiesClass.getProperties();

                    // Apply primary filter
                    if (selectedFilter != null && filterValue != null && !filterValue.isEmpty()) {
                        filteredProperties = filteredProperties.stream()
                                .filter(property -> {
                                    switch (selectedFilter) {
                                        case "Neighborhood":
                                            return property.getNeighborhood().getNeighborhoodName().equals(filterValue);
                                        case "Assessment Class":
                                            return property.getAssessmentClass().toString().contains(filterValue);
                                        case "Ward":
                                            return property.getNeighborhood().getWard().contains(filterValue);
                                        default:
                                            return true;
                                    }
                                })
                                .collect(Collectors.toList());
                    }

                    // Apply garage filter
                    if (!garageFilter.equals("All")) {
                        boolean hasGarage = garageFilter.equals("Yes");
                        filteredProperties = filteredProperties.stream()
                                .filter(property -> property.getGarage().equalsIgnoreCase(hasGarage ? "Y" : "N"))
                                .collect(Collectors.toList());
                    }

                    // Apply price filter if input is valid
                    if (priceValue != null && priceComparison != null && !priceComparison.isEmpty()) {
                        switch (priceComparison) {
                            case "Under":
                                filteredProperties = filteredProperties.stream()
                                        .filter(property -> property.getAssessedValue() < priceValue)
                                        .collect(Collectors.toList());
                                break;
                            case "Equal":
                                filteredProperties = filteredProperties.stream()
                                        .filter(property -> property.getAssessedValue() == priceValue)
                                        .collect(Collectors.toList());
                                break;
                            case "Above":
                                filteredProperties = filteredProperties.stream()
                                        .filter(property -> property.getAssessedValue() > priceValue)
                                        .collect(Collectors.toList());
                                break;
                        }
                    }

                    return filteredProperties;
                }
            };

            VBox loadingContainer = createLoadingContainer("Applying Filter", task);
            Platform.runLater(() -> rootStackPane.getChildren().add(loadingContainer));

            task.setOnSucceeded(e -> {
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));

                List<PropertyAssessment> filteredProperties = task.getValue();
                if (filteredProperties != null && !filteredProperties.isEmpty()) {
                    // Update the legend dynamically based on filtered properties
                    updateLegend(filteredProperties);

                    // Update other UI components
                    displayPropertyStatisticsInfo(filteredProperties, "Custom Filter");

                    PropertyAssessment property = filteredProperties.get(0) ;
                    Point groupPoint = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
                    //Zoom out further than normal to show the entire group
                    mapView.setViewpointCenterAsync(groupPoint, 10000);

                    updateMapWithFilteredProperties(filteredProperties);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "No properties match the selected filters.", ButtonType.OK);
                    alert.showAndWait();

                    // Reset the legend if no properties match
                    updateLegend(null);
                }
            });

            task.setOnFailed(e -> {
                Platform.runLater(() -> rootStackPane.getChildren().remove(loadingContainer));
                e.getSource().getException().printStackTrace();
            });

            new Thread(task).start();
        });
    }

    private void updateLegend(List<PropertyAssessment> filteredProperties) {
        if (filteredProperties != null && !filteredProperties.isEmpty()) {
            // Update the assessed value center (median) based on the filtered properties
            assessedValueCenter = new PropertyAssessments(filteredProperties).getMedian();
        } else {
            // Reset to the original center if no properties match
            assessedValueCenter = propertiesClass.getMedian();
        }

        // Refresh the legend with the new median
        refreshLegend();
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
                assessedValueCenter = propertiesClass.getMedian();
                //Redraw legend
                refreshLegend();
                // Reset text area's text & pie chart
                propertyInfoArea.setText("");
                propertyStatisticsArea.setText("");
                classesPieChart.setVisible(false);
                classesPieChart.setManaged(false);
                classesPieChart.setMinHeight(0);
                classesPieChart.setMinWidth(0);
                filterDropdown.getSelectionModel().clearSelection();
                valueDropdown.getSelectionModel().clearSelection();
                priceComparisonDropdown.getSelectionModel().clearSelection();
                priceInputField.setText("");
                centerInputField.setText("");
                accountSearchInput.setText("");
                garageFilterGroup.selectToggle(garageFilterGroup.getToggles().get(0));

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

    // Altered version of the Spectral 11 Color Palette
    private Color getAssesmentColor(long currentAssessedValue){

        if (currentAssessedValue == 0){
            return Color.BLACK;
        }
        //-50% off of center
        else if (currentAssessedValue <= assessedValueCenter * 0.5) {
            return Color.web("#4b2ca3"); // Royal Blue
        }
        //-30% off of center
        else if (currentAssessedValue <= assessedValueCenter * 0.70){
            return Color.web("#0077bb"); // Bright Azure
        }

        //-15% off of center
        else if (currentAssessedValue <= assessedValueCenter * 0.85) {
            return Color.web("#00b891"); // Vivid Turquoise
        }
        //-5 % off of center
        else if (currentAssessedValue <= assessedValueCenter * 0.95) {
            return Color.web("#6ccc63"); // Spring Green
        }
        //-2% off of center
        else if (currentAssessedValue <= assessedValueCenter * 0.98) {

            return Color.web("#d9ed4c"); // Bright Lime
        }

        //At Center
        else if (currentAssessedValue == assessedValueCenter) {
            return Color.web("#ffff66"); // Pure Yellow
        }

        //+2% off of center
        else if (currentAssessedValue <= assessedValueCenter * 1.02) {
            return Color.web("#ffcc33"); // Bright Amber
        }
        //+5% off of center
        else if (currentAssessedValue <= assessedValueCenter * 1.05 ) {
            return Color.web("#ff8c00"); // Vivid Orange
        }
        //+15% of center
        else if (currentAssessedValue <= assessedValueCenter * 1.15 )  {
            return Color.web("#e64a19"); // Deep Coral
        }
        //+30%  of center
        else if (currentAssessedValue <= assessedValueCenter * 1.30) {

            return Color.web("#c70039"); // Crimson
        }
        // +50% of center
        else{

            return Color.web("#800026"); // Dark Burgundy
        }

    }




    // Display property information
    private void displayPropertyInfo(PropertyAssessment property) {
        if (property == null) {
            propertyInfoArea.setText("No property information available.");
        } else {

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
    private void displayPropertyStatisticsInfo(List<PropertyAssessment> properties , String filterValue) {

        PropertyAssessments propertyAssessments = new PropertyAssessments(properties);

        if (properties == null) {
            propertyStatisticsArea.setText("No property statistics available.");
        } else {

            //For formatting assessed value into a currency
            DecimalFormat numberFormat = new DecimalFormat("#,###");

            propertyStatisticsArea.setText("Statistics: " + filterValue + "\n" + String.format(
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

    // Display pie chart of property assessment classes
    private void displayPieChart(PropertyAssessment property) {
        classesPieChart.getData().clear();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(property.getAssessmentClass().getAssessmentClass1(), property.getAssessmentClass().getAssessmentPercentage1()),
                new PieChart.Data(property.getAssessmentClass().getAssessmentClass2(), property.getAssessmentClass().getAssessmentPercentage2()),
                new PieChart.Data(property.getAssessmentClass().getAssessmentClass3(), property.getAssessmentClass().getAssessmentPercentage3())
        );
        // Only add valid entries
        pieChartData.removeIf(data -> data.getPieValue() == 0 || data.getName().isEmpty() || data.getName() == null);

        classesPieChart.setData(pieChartData);

        classesPieChart.setClockwise(true);
        classesPieChart.setStartAngle(180);
        classesPieChart.setVisible(true);
        classesPieChart.setManaged(true);
        classesPieChart.setTitle("Assessment Classes");
        classesPieChart.setLabelLineLength(8);
        classesPieChart.setMaxHeight(200);
        classesPieChart.setStyle("-fx-padding: 0");

        setupPieChartValues();
    }

     // Add tool tips to each slice on the pie chart
    private void setupPieChartValues() {
        classesPieChart.getData().forEach(data -> {
            String percentage = String.format("%.0f%%", data.getPieValue());
            Tooltip tooltip = new Tooltip(percentage);
            Tooltip.install(data.getNode(), tooltip);
        });
    }

    // Highlight selected property
    private void highlightSelectedProperty(PropertyAssessment property) {

        assessedValueCenter = property.getAssessedValue();
        refreshLegend();
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
                                    displayPropertyInfo(property);
                                    displayPieChart(property);
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
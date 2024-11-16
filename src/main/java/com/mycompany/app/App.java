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
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class App extends Application {

    private MapView mapView;

    public static void main(String[] args) {

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {

        // set the title and size of the stage and show it
        stage.setTitle("My Map App");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        String yourApiKey = "AAPTxy8BH1VEsoebNVZXo8HurFJ2xCDXvFC-uJSDrIEtVkCMkq-W26QCtQCgZQipt1lUwR3Pm3yRRKbeYBv6kCefEqVXMAsnQ4rVMkNdiFC5DLtXmhx_Daydix9ND6gKSfXvNdbEQeQwWhhHluGF5DXHa496Q77CndgVM7EY_nadJd-0J9bw5HiOrqsb4as3xU5lBVtBAp2G1FB5WYInTpK_0C6_6_reJIkqqnHUoC6Ez_o.AT1_V1QXZfZZ";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);

        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);

        // display the map by setting the map on the map view
        mapView.setMap(map);

        GraphicsOverlay m_graphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(m_graphicsOverlay);

        SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 10);

        PropertyAssessments propertiesClass = null;
        try {
            propertiesClass = new PropertyAssessments("Property_Assessment_Data_2024.csv");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        List<PropertyAssessment> properties = propertiesClass.getProperties();


//        Point viewPoint = new Point(properties.get(0).getLocation().getLng(), properties.get(0).getLocation().getLat(), SpatialReferences.getWgs84());
        Point viewPoint = new Point(-113.4938, 53.5461, SpatialReferences.getWgs84());
        mapView.setViewpointCenterAsync(viewPoint, 15000);
        for (PropertyAssessment property : properties) {
            Point point = new Point(property.getLocation().getLng(), property.getLocation().getLat(), SpatialReferences.getWgs84());
            Graphic graphic = new Graphic(point, symbol);
            m_graphicsOverlay.getGraphics().add(graphic);
        }

//        stackPane.getChildren().add(mapView);
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {

        if (mapView != null) {
            mapView.dispose();
        }
    }
}

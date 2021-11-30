/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.examples.extension.javafx.aut;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PieChartExampleAUT extends Application {

    /** current amount of data */
    int dataAmount = 0;

    /** default amount of data */
    int defaultAmount = 5;
    
    /** data list */
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        Pane root = new VBox();
        Scene scene = new Scene(root);
        stage.setTitle("Pie Chart Example"); //$NON-NLS-1$
        stage.setWidth(500);
        stage.setHeight(500);
        
        while (dataAmount < defaultAmount) {            
            addData();
        }
        
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Random Data"); //$NON-NLS-1$
        chart.setId("pieChart"); //$NON-NLS-1$
        
        Pane buttonArea = new HBox();
        root.getChildren().addAll(chart, buttonArea);
        
        Button plus = new Button("+"); //$NON-NLS-1$
        plus.setId("plusButton"); //$NON-NLS-1$
        plus.setPrefWidth(50);
        plus.setOnMouseClicked(e -> {
            addData();
        });

        Button minus = new Button("-"); //$NON-NLS-1$
        minus.setId("minusButton"); //$NON-NLS-1$
        minus.setPrefWidth(50);
        minus.setOnMouseClicked(e -> {
            removeData();
        });
        
        buttonArea.getChildren().addAll(plus, minus);

        stage.setScene(scene);
        stage.show();
    }

    /** adds a data set */
    private void addData() {
        pieChartData.add(new PieChart.Data("Data" + dataAmount, Math.random())); //$NON-NLS-1$
        dataAmount++;
    }

    /** removes one data set */
    private void removeData() {
        if (dataAmount > 0) {            
            pieChartData.remove(dataAmount - 1);
            dataAmount--;
        }
    }
}
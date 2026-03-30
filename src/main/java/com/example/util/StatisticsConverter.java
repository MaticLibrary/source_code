package com.example.util;

import javafx.scene.chart.XYChart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StatisticsConverter {
    static public void exportStats(File file,
                                   XYChart.Series<Number, Number> supporting,
                                   XYChart.Series<Number, Number> notSupporting) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("krok,za_atakiem,za_odwrotem\n");
        int max = Math.max(supporting.getData().size(), notSupporting.getData().size());
        for (int i = 0; i < max; i++) {
            String supportValue = i < supporting.getData().size() ? supporting.getData().get(i).getYValue().toString() : "";
            String opposeValue = i < notSupporting.getData().size() ? notSupporting.getData().get(i).getYValue().toString() : "";
            writer.write(i + "," + supportValue + "," + opposeValue + "\n");
        }
        writer.close();
    }
}

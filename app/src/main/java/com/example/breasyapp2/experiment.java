package com.example.breasyapp2;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class experiment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the LineChart view
        LineChart lineChart = findViewById(R.id.lineChart);

        // Data Points (Heart Rate BPM)
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 72));    // 0s
        entries.add(new Entry(30f, 75));
        entries.add(new Entry(60f, 78));   // 1 mins
        entries.add(new Entry(90f, 80));
        entries.add(new Entry(120f, 82));  // 2 mins
        entries.add(new Entry(150f, 79));
        entries.add(new Entry(180f, 85));  // 3 mins
        entries.add(new Entry(210f, 88));
        entries.add(new Entry(240f, 90));  // 4 mins
        entries.add(new Entry(270f, 87));
        entries.add(new Entry(300f, 85));  // 5 mins
        entries.add(new Entry(330f, 88));
        entries.add(new Entry(360f, 90));  // 6 mins


        // Line DataSet Configuration
        LineDataSet lineDataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleColor(getResources().getColor(android.R.color.holo_green_light));
        lineDataSet.setCircleRadius(2f);
        lineDataSet.setValueTextSize(7f);

        // Add the dataset to a list (in case you want to add more datasets later)
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        // Set LineData to chart
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        // Chart Appearance Settings
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        // X Axis Customization
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "s"; // Format as seconds
            }
        });
        // Left Y Axis Customization
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(60f); // Minimum for heart rate
        // Right Y Axis as a Border
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);               // Enable axis
        rightAxis.setDrawAxisLine(true);          // Show axis line
        rightAxis.setDrawLabels(false);           // Hide text
        rightAxis.setDrawGridLines(false);        // No grid lines
        rightAxis.setAxisLineColor(Color.GRAY);   // Optional styling
        rightAxis.setAxisLineWidth(1f);           // Optional thickness

        lineChart.invalidate(); // Refresh chart to apply changes


    }
}
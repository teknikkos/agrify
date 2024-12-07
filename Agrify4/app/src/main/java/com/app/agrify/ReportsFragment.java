package com.app.agrify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReportsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        WebView chartWebView = view.findViewById(R.id.chartWebView);

        // Enable JavaScript
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load and parse JSON, then load the chart
        try {
            String jsonData = loadJSONFromAsset();
            if (jsonData != null) {
                String chartHTML = getChartHTML(jsonData);
                chartWebView.loadDataWithBaseURL("file:///android_asset/", chartHTML, "text/html", "UTF-8", null);
            } else {
                chartWebView.loadData("Error loading data", "text/html", "UTF-8");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = requireContext().getAssets().open("yearlyYieldReport.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private String getChartHTML(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray citiesArray = jsonObject.getJSONArray("yields");
        StringBuilder dataString = new StringBuilder("['Yield', 'Financial Report'],");

        // Loop through the JSON array to extract field and reports
        for (int i = 0; i < citiesArray.length(); i++) {
            JSONObject city = citiesArray.getJSONObject(i);
            String cityName = city.getString("name");
            int population = city.getInt("Financial Report");
            dataString.append("['").append(cityName).append("', ").append(population).append("],");
        }

        // HTML with embedded Google Charts JavaScript to render the pie chart
        return "<html>" +
                "<head>" +
                "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                "    <script type=\"text/javascript\">" +
                "        google.charts.load('current', {'packages':['corechart']});" +
                "        google.charts.setOnLoadCallback(drawChart);" +
                "        function drawChart() {" +
                "            var data = google.visualization.arrayToDataTable([" +
                dataString.toString() +
                "            ]);" +
                "            var options = {" +
                "                title: 'Yearly Yield Report (TEST)'," +
                "                pieHole: 0.1," + // Pie
                "            };" +
                "            var chart = new google.visualization.PieChart(document.getElementById('donutchart'));" +
                "            chart.draw(data, options);" +
                "        }" +
                "    </script>" +
                "</head>" +
                "<body>" +
                "    <div id=\"donutchart\" style=\"width: auto; height: 500px;\"></div>" +
                "</body>" +
                "</html>";
    }
}

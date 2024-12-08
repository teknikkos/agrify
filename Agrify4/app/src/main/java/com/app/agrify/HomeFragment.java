package com.app.agrify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HomeFragment extends Fragment {

    private Button logOutButton;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        logOutButton = view.findViewById(R.id.logOutButton);
        // Set onClick listener for the logout button
        logOutButton.setOnClickListener(v -> logoutUser());
        // Enable JavaScript

        // Find the WebView for the chart
        WebView chartWebView = view.findViewById(R.id.chartWebView);

        // Enable JavaScript for the WebView
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load and parse the saved JSON file, then load the chart
        try {
            String jsonData = loadJSONFromFile(); // Load the JSON data from internal storage
            if (jsonData != null) {
                String chartHTML = getChartHTML(jsonData); // Generate the HTML for the bar chart
                chartWebView.loadDataWithBaseURL("file:///android_asset/", chartHTML, "text/html", "UTF-8", null);
            } else {
                chartWebView.loadData("Error loading data", "text/html", "UTF-8");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    private String loadJSONFromFile() {
        String json;
        try {
            // Load the JSON file from internal storage where it's saved
            File file = new File(requireContext().getFilesDir(), "yearlyYieldReport.json");
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
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
        StringBuilder dataString = new StringBuilder("['Field', 'Financial Report'],"); // Changed label for a bar chart

        // Loop through the JSON array to extract field and reports
        for (int i = 0; i < citiesArray.length(); i++) {
            JSONObject field = citiesArray.getJSONObject(i);
            String fieldName = field.getString("name");
            int financialReport = field.getInt("Financial Report");
            dataString.append("['").append(fieldName).append("', ").append(financialReport).append("],");
        }

        // HTML with embedded Google Charts JavaScript to render the bar chart
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
                "                title: 'Financial Report by Field'," +
                "                hAxis: {title: 'Field Name'}," +
                "                vAxis: {title: 'Financial Report'}," +
                "                bars: 'vertical'," + // Bar chart style
                "            };" +
                "            var chart = new google.visualization.ColumnChart(document.getElementById('barchart'));" +
                "            chart.draw(data, options);" +
                "        }" +
                "    </script>" +
                "</head>" +
                "<body>" +
                "    <div id=\"barchart\" style=\"width: auto; height: 500px;\"></div>" +
                "</body>" +
                "</html>";
    }

    private void logoutUser() {
        // Clear user login state from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_prefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("user_login", false);
        editor.apply();

        // Show a confirmation message
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginProcess activity
        Intent intent = new Intent(getActivity(), LoginProcess.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);

        requireActivity().finish(); // Close the current activity
    }
}
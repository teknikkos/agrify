package com.app.agrify;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FieldsFragment extends Fragment {
    private LinearLayout fieldsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fields, container, false);
        fieldsContainer = view.findViewById(R.id.fieldsContainer);
        FloatingActionButton fabAdd = view.findViewById(R.id.floatingActionButton);

        // Load field data when fragment starts
        loadFieldsFromServer();

        fabAdd.setOnClickListener(v -> {
            // Add your dialog logic here
        });

        fabAdd.setOnClickListener(v -> {
            // Instantiate the AddFieldDialog and set the listener
            AddFieldDialog addFieldDialog = new AddFieldDialog(new AddFieldDialog.AddFieldListener() {
                @Override
                public void onFieldAdded() {
                    // This will be called when the field is added, refresh the field list
                    loadFieldsFromServer(); // Call your method to refresh the data
                }
            });
            addFieldDialog.show(getChildFragmentManager(), "AddFieldDialog");
        });

        return view;
    }

    private String getCropType(String cropId) {
        String cropType;
        switch (cropId) {
            case "1":
                cropType = "Sugarcane (18 months)";
                break;
            case "2":
                cropType = "Rice (3-6 months)";
                break;
            case "3":
                cropType = "Corn (3-5 months)";
                break;
            default:
                cropType = "Unknown crop";  // Default case if cropId is not recognized
                break;
        }
        return cropType;
    }



    private void loadFieldsFromServer() {
        fieldsContainer.removeAllViews();

        // Define your API URL
        String apiUrl = "https://just1ncantiler0.heliohost.us/read.php";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to fetch data from server", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Parse JSON data
                String responseData = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    // Update the yearly yield report with crop counts for the current year
                    updateYearlyYieldReport(responseData);
                    populateFields(responseData); // Populate the fields as usual
                });
            }
        });
    }

    private void updateYearlyYieldReport(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Current year
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            // A map to keep track of crops and their counts for the current year
            HashMap<String, Integer> cropCountMap = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject field = jsonArray.getJSONObject(i);
                String datePlanted = field.getString("Date_Planted");
                String cropId = field.getString("Crop_ID");

                // Extract the year from the datePlanted
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date plantedDate = dateFormat.parse(datePlanted);
                Calendar plantedCalendar = Calendar.getInstance();
                plantedCalendar.setTime(plantedDate);
                int plantedYear = plantedCalendar.get(Calendar.YEAR);

                // Check if the crop was planted this year
                if (plantedYear == currentYear) {
                    // Update the crop count map for the current year
                    String cropType = getCropType(cropId);  // Get crop type based on cropId
                    cropCountMap.put(cropType, cropCountMap.getOrDefault(cropType, 0) + 1);
                }
            }

            // Now we have the count of crops planted this year, you can create your report
            JSONArray yieldReport = new JSONArray();
            for (Map.Entry<String, Integer> entry : cropCountMap.entrySet()) {
                JSONObject reportItem = new JSONObject();
                reportItem.put("name", entry.getKey());  // Crop type
                reportItem.put("Financial Report", entry.getValue());  // Number of crops
                yieldReport.put(reportItem);
            }

            // Now that the report is generated, save it to a JSON file
            saveYearlyYieldReportToFile(yieldReport);

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse or process data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveYearlyYieldReportToFile(JSONArray yieldReport) {
        try {
            // Prepare the data to be written to the file
            JSONObject updatedData = new JSONObject();
            updatedData.put("yields", yieldReport);

            // Get the app's directory or a specific file path to save the report
            File outputFile = new File(getActivity().getFilesDir(), "yearlyYieldReport.json");

            // Write the updated data to the file
            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(updatedData.toString());
            bufferedWriter.close();

            // Notify the user that the file has been updated
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Yearly yield report saved successfully", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Failed to save the report", Toast.LENGTH_SHORT).show();
            });
        }
    }




    private void populateFields(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject field = jsonArray.getJSONObject(i);
                String fieldName = field.getString("Name");
                String datePlanted = field.getString("Date_Planted");
                String cropId = field.getString("Crop_ID");
                String cropType = getCropType(cropId);
                String monthsLeft = calculateMonthsLeft(datePlanted, cropId);

                // Create and display CardView for each field with margin for spacing
                CardView cardView = createCardView(fieldName, cropType, monthsLeft, datePlanted);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                layoutParams.setMargins(0, 20, 0, 20);  // Set margin (top, bottom)
                cardView.setLayoutParams(layoutParams);

                fieldsContainer.addView(cardView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse server response", Toast.LENGTH_SHORT).show();
        }
    }

    private CardView createCardView(String fieldName, String cropType, String monthsLeft, String datePlanted) {
        CardView cardView = new CardView(getActivity());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setCardBackgroundColor(getResources().getColor(R.color.secondary_color));
        cardView.setRadius(12f);
        cardView.setCardElevation(8f);

        // Create a new linear layout for the card's content
        LinearLayout cardLayout = new LinearLayout(getActivity());
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardLayout.setOrientation(LinearLayout.VERTICAL); // Change to VERTICAL to stack the elements
        cardLayout.setPadding(30, 30, 30, 30); // Add padding to the card's content

        // Create the TextView for the field name
        TextView tvFieldName = new TextView(getActivity());
        tvFieldName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvFieldName.setText(fieldName);
        tvFieldName.setTextSize(18);
        tvFieldName.setTextColor(getResources().getColor(R.color.background_color));

        // Add spacing below the field name
        LinearLayout.LayoutParams fieldNameLayoutParams = (LinearLayout.LayoutParams) tvFieldName.getLayoutParams();
        fieldNameLayoutParams.setMargins(0, 0, 0, 16); // Add margin at the bottom
        tvFieldName.setLayoutParams(fieldNameLayoutParams);

        // Create the TextView for the date planted
        TextView tvDatePlanted = new TextView(getActivity());
        tvDatePlanted.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvDatePlanted.setText("Date Planted: " + datePlanted);
        tvDatePlanted.setTextSize(16);
        tvDatePlanted.setTextColor(getResources().getColor(R.color.background_color));

        // Add spacing below the date planted
        LinearLayout.LayoutParams datePlantedLayoutParams = (LinearLayout.LayoutParams) tvDatePlanted.getLayoutParams();
        datePlantedLayoutParams.setMargins(0, 0, 0, 16); // Add margin at the bottom
        tvDatePlanted.setLayoutParams(datePlantedLayoutParams);

        // Create the TextView for the crop type
        TextView tvCropType = new TextView(getActivity());
        tvCropType.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvCropType.setText("Crop: " + cropType);
        tvCropType.setTextSize(16);
        tvCropType.setTextColor(getResources().getColor(R.color.background_color));

        // Add spacing below the crop type
        LinearLayout.LayoutParams cropTypeLayoutParams = (LinearLayout.LayoutParams) tvCropType.getLayoutParams();
        cropTypeLayoutParams.setMargins(0, 0, 0, 16); // Add margin at the bottom
        tvCropType.setLayoutParams(cropTypeLayoutParams);

        // Create the TextView for the months left
        TextView tvMonthsLeft = new TextView(getActivity());
        tvMonthsLeft.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvMonthsLeft.setText("Months Left: " + monthsLeft);
        tvMonthsLeft.setTextSize(16);
        tvMonthsLeft.setTextColor(getResources().getColor(R.color.background_color));

        // Add all TextViews to the layout
        cardLayout.addView(tvFieldName);
        cardLayout.addView(tvDatePlanted);
        cardLayout.addView(tvCropType);
        cardLayout.addView(tvMonthsLeft);

        // Add the layout to the CardView
        cardView.addView(cardLayout);

        return cardView;
    }






    private String calculateMonthsLeft(String datePlanted, String cropId) {
        if (datePlanted == null || datePlanted.isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date plantedDate = dateFormat.parse(datePlanted);

            Calendar plantedCalendar = Calendar.getInstance();
            plantedCalendar.setTime(plantedDate);

            Calendar currentCalendar = Calendar.getInstance();
            int monthsPassed = currentCalendar.get(Calendar.MONTH) - plantedCalendar.get(Calendar.MONTH);

            // Adjust for crop growth cycle
            int monthsLeft = getMonthsLeftBasedOnCrop(cropId) - monthsPassed;
            return Math.max(monthsLeft, 0) + " months";
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    private int getMonthsLeftBasedOnCrop(String cropId) {
        // Adjust the growth period based on cropId
        switch (cropId) {
            case "1":  // Sugarcane
                return 18;
            case "2":  // Rice
                return 6;  // Assuming rice takes about 6 months
            case "3":  // Corn
                return 5;  // Assuming corn takes about 5 months
            default:
                return 0;  // Unknown crop, no months left
        }
    }
}

package com.app.agrify;

import android.os.Bundle;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
                requireActivity().runOnUiThread(() -> populateFields(responseData));
            }
        });
    }

    private void populateFields(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject field = jsonArray.getJSONObject(i);
                String fieldName = field.getString("Name");
                String datePlanted = field.getString("Date_Planted");
                String cropId = field.getString("Crop_ID");  // Assuming Crop_ID is a number representing the crop type
                String cropType = getCropType(cropId);  // Get crop type based on crop ID
                String monthsLeft = calculateMonthsLeft(datePlanted, cropId); // Pass both datePlanted and cropId

                // Create and display CardView for each field, now with the datePlanted
                CardView cardView = createCardView(fieldName, cropType, monthsLeft, datePlanted);
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
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);  // Make it horizontal to align name and date side by side
        cardLayout.setPadding(20, 20, 20, 20);

        // Create the TextView for the field name
        TextView tvFieldName = new TextView(getActivity());
        tvFieldName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1  // Weight to make it take the available space
        ));
        tvFieldName.setText(fieldName);
        tvFieldName.setTextSize(16);
        tvFieldName.setTextColor(getResources().getColor(R.color.background_color));

        // Create the TextView for the date planted
        TextView tvDatePlanted = new TextView(getActivity());
        tvDatePlanted.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvDatePlanted.setText(datePlanted);  // Show the actual date planted
        tvDatePlanted.setTextSize(14);  // You can adjust the size as needed
        tvDatePlanted.setTextColor(getResources().getColor(R.color.background_color));
        tvDatePlanted.setGravity(Gravity.CENTER);  // Center the date if necessary

        // Create the TextView for the crop type (middle)
        TextView tvCropType = new TextView(getActivity());
        tvCropType.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1  // Use weight to make it evenly distributed
        ));
        tvCropType.setText(cropType);
        tvCropType.setTextSize(16);
        tvCropType.setTextColor(getResources().getColor(R.color.background_color));
        tvCropType.setGravity(Gravity.CENTER);  // To center the crop type text

        // Create the TextView for the months left
        TextView tvMonthsLeft = new TextView(getActivity());
        tvMonthsLeft.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvMonthsLeft.setText(monthsLeft);
        tvMonthsLeft.setTextSize(16);
        tvMonthsLeft.setTextColor(getResources().getColor(R.color.background_color));

        // Add the TextViews to the layout
        cardLayout.addView(tvFieldName);
        cardLayout.addView(tvDatePlanted);  // Add the date planted here beside the field name
        cardLayout.addView(tvCropType);
        cardLayout.addView(tvMonthsLeft);
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

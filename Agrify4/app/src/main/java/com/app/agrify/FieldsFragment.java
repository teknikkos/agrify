package com.app.agrify;

import android.os.Bundle;
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

        return view;
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
                String monthsLeft = calculateMonthsLeft(datePlanted);

                // Create and display CardView for each field
                CardView cardView = createCardView(fieldName, monthsLeft);
                fieldsContainer.addView(cardView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to parse server response", Toast.LENGTH_SHORT).show();
        }
    }

    private CardView createCardView(String fieldName, String monthsLeft) {
        CardView cardView = new CardView(getActivity());
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setCardBackgroundColor(getResources().getColor(R.color.secondary_color));
        cardView.setRadius(12f);
        cardView.setCardElevation(8f);

        LinearLayout cardLayout = new LinearLayout(getActivity());
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLayout.setPadding(20, 20, 20, 20);

        TextView tvFieldName = new TextView(getActivity());
        tvFieldName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        tvFieldName.setText(fieldName);
        tvFieldName.setTextSize(16);
        tvFieldName.setTextColor(getResources().getColor(R.color.background_color));

        TextView tvMonthsLeft = new TextView(getActivity());
        tvMonthsLeft.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvMonthsLeft.setText(monthsLeft);
        tvMonthsLeft.setTextSize(16);
        tvMonthsLeft.setTextColor(getResources().getColor(R.color.background_color));

        cardLayout.addView(tvFieldName);
        cardLayout.addView(tvMonthsLeft);
        cardView.addView(cardLayout);

        return cardView;
    }

    private String calculateMonthsLeft(String datePlanted) {
        if (datePlanted == null || datePlanted.isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date plantedDate = dateFormat.parse(datePlanted);

            Calendar plantedCalendar = Calendar.getInstance();
            plantedCalendar.setTime(plantedDate);

            Calendar currentCalendar = Calendar.getInstance();
            int monthsLeft = plantedCalendar.get(Calendar.MONTH) - currentCalendar.get(Calendar.MONTH);
            return Math.max(monthsLeft, 0) + " months";
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}

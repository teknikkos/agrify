package com.app.agrify;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddFieldDialog extends DialogFragment {

    private EditText editTextFieldName, editTextSoilType, editTextCrop, editTextDatePlanted, editTextLatitude, editTextLongitude;
    private final AddFieldListener listener;
    private ApiService apiService;

    public interface AddFieldListener {
        void onFieldAdded();
    }

    public AddFieldDialog(AddFieldListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_field_layout, null);

        // Initialize EditText fields
        editTextFieldName = view.findViewById(R.id.etFieldName);
        editTextSoilType = view.findViewById(R.id.etSoilType);
        editTextCrop = view.findViewById(R.id.etCrop);
        editTextDatePlanted = view.findViewById(R.id.etDatePlanted);
        editTextLatitude = view.findViewById(R.id.etLatitude);
        editTextLongitude = view.findViewById(R.id.etLongitude);

        // Set up the DatePickerDialog for the DatePlanted field
        editTextDatePlanted.setOnClickListener(v -> showDatePickerDialog());

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://just1ncantiler0.heliohost.us/") // Replace with your server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Build the dialog
        builder.setView(view)
                .setTitle("Add Field")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Add Field", (dialog, which) -> handleAddField());

        AlertDialog dialog = builder.create();

        // Adjust dialog width
        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getWindow() != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(layoutParams);
            }
        });

        return dialog;
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date as YYYY-MM-DD
                    String formattedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    editTextDatePlanted.setText(formattedDate);
                },
                year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void handleAddField() {
        String fieldName = editTextFieldName.getText().toString().trim();
        String soilType = editTextSoilType.getText().toString().trim();
        String crop = editTextCrop.getText().toString().trim();
        String datePlanted = editTextDatePlanted.getText().toString().trim();
        String latitude = editTextLatitude.getText().toString().trim();
        String longitude = editTextLongitude.getText().toString().trim();

        // Validate inputs
        if (fieldName.isEmpty() || soilType.isEmpty() || crop.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int soilTypeId = Integer.parseInt(soilType);
            int cropId = Integer.parseInt(crop);
            double fieldLatitude = Double.parseDouble(latitude);
            double fieldLongitude = Double.parseDouble(longitude);

            addFieldToServer(fieldName, soilTypeId, cropId, datePlanted, fieldLatitude, fieldLongitude);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid number input", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFieldToServer(String fieldName, int soilTypeId, int cropId, String datePlanted, double fieldLatitude, double fieldLongitude) {
        Call<ResponseBody> call = apiService.createField(fieldName, soilTypeId, cropId, datePlanted, fieldLatitude, fieldLongitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (getActivity() != null) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Field added successfully", Toast.LENGTH_SHORT).show();
                        listener.onFieldAdded();
                    } else {
                        Toast.makeText(getActivity(), "Error adding field", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("AddFieldDialog", "Activity is null, cannot show Toast");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error adding field", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("AddFieldDialog", "Activity is null, cannot show Toast");
                }
                t.printStackTrace();
            }
        });
    }
}

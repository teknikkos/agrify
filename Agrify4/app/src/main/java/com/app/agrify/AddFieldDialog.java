package com.app.agrify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddFieldDialog extends DialogFragment {

    private EditText editTextFieldName, editTextSoilType, editTextCrop, editTextDatePlanted, editTextLatitude, editTextLongtitude;
    private DatabaseHelper myDb;
    private final AddFieldListener listener;

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

        editTextFieldName = view.findViewById(R.id.etFieldName);
        editTextSoilType = view.findViewById(R.id.etSoilType);
        editTextCrop = view.findViewById(R.id.etCrop);
        editTextDatePlanted = view.findViewById(R.id.etDatePlanted);
        editTextLatitude = view.findViewById(R.id.etLatitude);
        editTextLongtitude = view.findViewById(R.id.etLongitude);

        myDb = new DatabaseHelper(getActivity());

        // Build dialog with "Cancel" and "Add Product" buttons styled similarly
        builder.setView(view)
                .setTitle("Add Field")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();  // Cancel the dialog
                    }
                })
                .setPositiveButton("Add Field", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Add product logic
                        String fieldName = editTextFieldName.getText().toString();
                        String soilType = editTextSoilType.getText().toString();
                        String Crop = editTextCrop.getText().toString();
                        String DatePlanted = editTextDatePlanted.getText().toString();
                        String Latitude = editTextLatitude.getText().toString();
                        String Longtitude = editTextLongtitude.getText().toString();

                        if (fieldName.isEmpty() || soilType.isEmpty() || Crop.isEmpty()) {
                            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean isInserted = myDb.insertData(fieldName, soilType, Crop, DatePlanted, Latitude, Longtitude);
                            if (isInserted) {
                                Toast.makeText(getActivity(), "Product added", Toast.LENGTH_SHORT).show();
                                listener.onFieldAdded();  // Notify listener (FragmentProducts) that a product was added
                            } else {
                                Toast.makeText(getActivity(), "Error adding product", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        AlertDialog dialog = builder.create();

        // Set the dialog width to fill the entire screen width
        dialog.setOnShowListener(dialogInterface -> {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(layoutParams);
        });

        return dialog;
    }
}

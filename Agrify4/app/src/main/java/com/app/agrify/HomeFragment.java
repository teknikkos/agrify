package com.app.agrify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomeFragment extends Fragment {

    private Button logOutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        logOutButton = view.findViewById(R.id.logOutButton);

        // Set onClick listener for the logout button
        logOutButton.setOnClickListener(v -> logoutUser());

        return view;
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
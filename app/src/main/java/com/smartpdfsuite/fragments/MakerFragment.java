package com.smartpdfsuite.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.smartpdfsuite.R;
import com.smartpdfsuite.viewmodels.MakerViewModel; // You would create this ViewModel

public class MakerFragment extends Fragment {

    private MakerViewModel makerViewModel; // Assuming you'll have a ViewModel for this

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maker, container, false);
        final TextView textView = root.findViewById(R.id.text_maker);

        // Initialize ViewModel if needed
        makerViewModel = new ViewModelProvider(this).get(MakerViewModel.class);
        makerViewModel.getText().observe(getViewLifecycleOwner(), s -> {
            textView.setText(s);
        });

        return root;
    }

    // You will add methods here for:
    // - Handling image selection (using ActivityResultLauncher)
    // - Creating PDF from selected images
    // - Handling text input and creating PDF from text
    // - Saving the created PDF
}
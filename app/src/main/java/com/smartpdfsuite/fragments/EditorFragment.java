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

//import com.smartpdfsuite.R;
import com.example.pdfviewer_editor_scanner.R;
import com.smartpdfsuite.viewmodels.EditorViewModel; // You would create this ViewModel

public class EditorFragment extends Fragment {

    private EditorViewModel editorViewModel; // Assuming you'll have a ViewModel for this

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editor, container, false);
        final TextView textView = root.findViewById(R.id.text_editor);

        // Initialize ViewModel if needed
        editorViewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        editorViewModel.getText().observe(getViewLifecycleOwner(), s -> {
            textView.setText(s);
        });

        return root;
    }

    // You will add methods here for:
    // - Handling PDF selection for merging/splitting/editing
    // - Implementing UI for page reordering (drag & drop)
    // - Calling PDF library functions for merge, split, delete, compress
}
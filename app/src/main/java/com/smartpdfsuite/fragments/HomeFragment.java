package com.smartpdfsuite.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*import com.smartpdfsuite.R;*/
import com.example.pdfviewer_editor_scanner.R;
import com.smartpdfsuite.adapters.PdfListAdapter;
import com.smartpdfsuite.models.PdfDocument;
import com.smartpdfsuite.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements PdfListAdapter.OnPdfClickListener {

    private HomeViewModel homeViewModel;
    private PdfListAdapter recentPdfsAdapter;
    private RecyclerView recentPdfsRecyclerView;
    private TextView noPdfsText; // This is the TextView for your "No recent PDFs found" message

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        recentPdfsRecyclerView = root.findViewById(R.id.recent_pdfs_recycler_view);
        noPdfsText = root.findViewById(R.id.no_pdfs_text); // Link to the TextView in your layout
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the HomeViewModel, scoped to this fragment's lifecycle
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecentPdfsRecyclerView();
        observeRecentPdfs(); // Start observing LiveData
        // Trigger loading of PDFs from storage when the fragment is created/viewed
        homeViewModel.loadPdfsFromStorage(requireContext());
    }

    private void setupRecentPdfsRecyclerView() {
        // Initialize the adapter with an empty list initially
        recentPdfsAdapter = new PdfListAdapter(new ArrayList<>(), this);
        recentPdfsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentPdfsRecyclerView.setAdapter(recentPdfsAdapter);
    }

    private void observeRecentPdfs() {
        // Observe changes in the list of recent PDFs from the ViewModel
        homeViewModel.getRecentPdfs().observe(getViewLifecycleOwner(), pdfDocuments -> {
            if (pdfDocuments != null && !pdfDocuments.isEmpty()) {
                // If PDFs are found, update the adapter and show the RecyclerView
                recentPdfsAdapter.updatePdfList(pdfDocuments);
                recentPdfsRecyclerView.setVisibility(View.VISIBLE);
                noPdfsText.setVisibility(View.GONE); // Hide the "No PDFs found" message
            } else {
                // If no PDFs are found, hide the RecyclerView and show the message
                recentPdfsRecyclerView.setVisibility(View.GONE);
                noPdfsText.setVisibility(View.VISIBLE); // Show the "No PDFs found" message
            }
        });
    }

    @Override
    public void onPdfClick(PdfDocument pdfDocument) {
        // Handle PDF click: navigate to the ReaderFragment
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putParcelable("pdfUri", pdfDocument.getUri());
        navController.navigate(R.id.navigation_reader, bundle);
    }

    @Override
    public void onPdfLongClick(PdfDocument pdfDocument) {
        // Handle long press on a PDF item.
        // This is where you might show a context menu or a BottomSheetDialog
        // with options like delete, move, rename, share, bookmark, view properties.
        Toast.makeText(getContext(), "Long pressed: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
    }
}
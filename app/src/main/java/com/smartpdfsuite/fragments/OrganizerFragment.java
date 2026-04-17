package com.smartpdfsuite.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.smartpdfsuite.R;
import com.smartpdfsuite.adapters.PdfListAdapter;
import com.smartpdfsuite.models.PdfDocument;
import com.smartpdfsuite.viewmodels.OrganizerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrganizerFragment extends Fragment implements PdfListAdapter.OnPdfClickListener {

    private OrganizerViewModel viewModel;

    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;
    private MaterialButton scanPdfButton;
    private TextInputEditText searchEditText;
    private ChipGroup sortChipGroup;

    private PdfListAdapter adapter;

    private List<PdfDocument> allPdfs = new ArrayList<>();
    private String searchQuery = "";
    private SortType currentSort = SortType.DATE;

    private enum SortType {
        DATE, NAME, SIZE
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_organizer, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupSorting();
        setupActions();
        observeViewModel();

        viewModel.loadAllPdfs(requireContext());


        if (requireActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .hide();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.all_pdfs_recycler_view);
        emptyLayout = view.findViewById(R.id.no_all_pdfs_layout);
        scanPdfButton = view.findViewById(R.id.btn_scan_pdf);
        searchEditText = view.findViewById(R.id.search_pdf_edit_text);
        sortChipGroup = view.findViewById(R.id.sort_chip_group);

        viewModel = new ViewModelProvider(this)
                .get(OrganizerViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new PdfListAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilterAndSort();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSorting() {
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip == null) return;

            String text = chip.getText().toString().toLowerCase(Locale.getDefault());

            if (text.contains("name")) {
                currentSort = SortType.NAME;
            } else if (text.contains("size")) {
                currentSort = SortType.SIZE;
            } else {
                currentSort = SortType.DATE;
            }

            applyFilterAndSort();
        });
    }

    private void setupActions() {
        scanPdfButton.setOnClickListener(v ->
                Toast.makeText(
                        getContext(),
                        "Scanner coming soon 🚀",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void observeViewModel() {
        viewModel.getAllPdfs().observe(getViewLifecycleOwner(), pdfs -> {
            allPdfs = pdfs != null ? pdfs : new ArrayList<>();
            applyFilterAndSort();
        });
    }

    private void applyFilterAndSort() {
        List<PdfDocument> result = new ArrayList<>();

        // Filter
        for (PdfDocument pdf : allPdfs) {
            if (searchQuery.isEmpty() ||
                    pdf.getName().toLowerCase(Locale.getDefault())
                            .contains(searchQuery.toLowerCase(Locale.getDefault()))) {
                result.add(pdf);
            }
        }

        // Sort
        result.sort((p1, p2) -> {
            switch (currentSort) {
                case NAME:
                    return p1.getName().compareToIgnoreCase(p2.getName());
                case SIZE:
                    return Long.compare(p1.getSize(), p2.getSize());
                case DATE:
                default:
                    return Long.compare(p2.getDateModified(), p1.getDateModified());
            }
        });

        updateUi(result);
    }

    private void updateUi(List<PdfDocument> list) {
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updatePdfList(list);
        }
    }

    @Override
    public void onPdfClick(PdfDocument pdf) {
//        NavController navController =
//                Navigation.findNavController(requireView());
//
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("pdfUri", pdf.getUri());
//
//        navController.navigate(R.id.navigation_reader, bundle);
/*update on 7_03_26*/
        /*NavController navController =
                Navigation.findNavController(requireView());

        Bundle bundle = new Bundle();
        bundle.putParcelable(
                ReaderFragment.ARG_PDF_URI,
                pdf.getUri()
        );
        bundle.putString(
                ReaderFragment.ARG_PDF_NAME,
                pdf.getName()
        );

        navController.navigate(R.id.navigation_reader, bundle);*/

        NavController navController =
                Navigation.findNavController(requireView());

        Bundle bundle = new Bundle();

        bundle.putString(
                ReaderFragment.ARG_PDF_URI,
                pdf.getUri().toString()
        );

        bundle.putString(
                ReaderFragment.ARG_PDF_NAME,
                pdf.getName()
        );

        navController.navigate(R.id.navigation_reader, bundle);
    }

    @Override
    public void onPdfLongClick(PdfDocument pdf) {
        Toast.makeText(
                getContext(),
                "Long press: " + pdf.getName(),
                Toast.LENGTH_SHORT
        ).show();

        // TODO: Bottom sheet (Rename / Share / Delete / Properties)
    }

    //add below code 20/03/2026
    @Override
    public void onRenameClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Rename: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMoveClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Move: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Delete: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
    }


}





/*
package com.smartpdfsuite.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.smartpdfsuite.R;

import com.smartpdfsuite.adapters.PdfListAdapter;
import com.smartpdfsuite.models.PdfDocument;
import com.smartpdfsuite.viewmodels.OrganizerViewModel; // You would create this ViewModel

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OrganizerFragment extends Fragment implements PdfListAdapter.OnPdfClickListener {

    private OrganizerViewModel organizerViewModel;
    private PdfListAdapter allPdfsAdapter;
    private RecyclerView allPdfsRecyclerView;
    private TextView noAllPdfsText;
    private TextInputEditText searchEditText;
    private ChipGroup sortChipGroup;

    private List<PdfDocument> currentPdfs = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentSortOrder = "date"; // Default sort by date

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_organizer, container, false);
        allPdfsRecyclerView = root.findViewById(R.id.all_pdfs_recycler_view);
        noAllPdfsText = root.findViewById(R.id.no_all_pdfs_text);
        searchEditText = root.findViewById(R.id.search_pdf_edit_text);
        sortChipGroup = root.findViewById(R.id.sort_chip_group);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);

        setupAllPdfsRecyclerView();
        setupSearch();
        setupSorting();
        observeAllPdfs();

        // Trigger loading of PDFs from storage
        organizerViewModel.loadAllPdfs(requireContext());
    }

    private void setupAllPdfsRecyclerView() {
        allPdfsAdapter = new PdfListAdapter(new ArrayList<>(), this);
        allPdfsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allPdfsRecyclerView.setAdapter(allPdfsAdapter);
    }

    private void observeAllPdfs() {
        organizerViewModel.getAllPdfs().observe(getViewLifecycleOwner(), pdfDocuments -> {
            currentPdfs = pdfDocuments; // Store the original list
            updatePdfList(); // Apply current search and sort
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { */
/* Not used *//*
 }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                updatePdfList();
            }

            @Override
            public void afterTextChanged(Editable s) { */
/* Not used *//*
 }
        });
    }

    private void setupSorting() {
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedChipId = checkedIds.get(0);
                Chip checkedChip = group.findViewById(checkedChipId);
                if (checkedChip != null) {
                    currentSortOrder = checkedChip.getText().toString().toLowerCase(Locale.getDefault());
                    updatePdfList();
                }
            }
        });
    }

    private void updatePdfList() {
        List<PdfDocument> filteredAndSortedPdfs = new ArrayList<>(currentPdfs);

        // 1. Apply Search Filter
        if (!currentSearchQuery.isEmpty()) {
            filteredAndSortedPdfs.removeIf(pdf -> !pdf.getName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery.toLowerCase(Locale.getDefault())));
        }

        // 2. Apply Sorting
        filteredAndSortedPdfs.sort((pdf1, pdf2) -> {
            switch (currentSortOrder) {
                case "name":
                    return pdf1.getName().compareToIgnoreCase(pdf2.getName());
                case "size":
                    return Long.compare(pdf1.getSize(), pdf2.getSize());
                case "date": // Default and fall-through
                default:
                    return Long.compare(pdf2.getDateModified(), pdf1.getDateModified()); // Newest first
            }
        });

        if (filteredAndSortedPdfs.isEmpty()) {
            allPdfsRecyclerView.setVisibility(View.GONE);
            noAllPdfsText.setVisibility(View.VISIBLE);
        } else {
            allPdfsAdapter.updatePdfList(filteredAndSortedPdfs);
            allPdfsRecyclerView.setVisibility(View.VISIBLE);
            noAllPdfsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPdfClick(PdfDocument pdfDocument) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putParcelable("pdfUri", pdfDocument.getUri());
        navController.navigate(R.id.navigation_reader, bundle);
    }

    @Override
    public void onPdfLongClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Long pressed (Organizer): " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
        // Implement context menu or bottom sheet for actions like delete, move, rename, share, tag, properties
    }
}*/

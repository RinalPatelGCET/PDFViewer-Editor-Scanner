package com.smartpdfsuite.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.smartpdfsuite.R;

import com.smartpdfsuite.viewmodels.HomeViewModel;
import com.smartpdfsuite.viewmodels.OrganizerViewModel;
import com.smartpdfsuite.viewmodels.ScannerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ImageProcessorFragment extends Fragment {

    private static final String TAG = "ImageProcessorFragment";
    private Uri imageUri; // The initial captured image URI received from ScannerFragment
    private ImageView capturedImageView;
    private Button processImageButton;
    private Button createPdfButton;
    private ProgressBar processingProgressBar;

    // ViewModels for data management and operations
    private ScannerViewModel scannerViewModel;
    private HomeViewModel homeViewModel;
    private OrganizerViewModel organizerViewModel;

    // This URI will represent the image currently displayed and being processed.
    // It starts with `imageUri` and gets updated to `outputProcessedUri` after processing.
    private Uri currentDisplayImageUri;

    // ActivityResultLauncher for Storage Access Framework (SAF) to let user choose PDF save location
    private ActivityResultLauncher<String> createPdfLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the initial image URI passed from ScannerFragment
        if (getArguments() != null) {
            String uriString = getArguments().getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
                currentDisplayImageUri = imageUri;
            }
            else {
                Log.e(TAG, "No image string received for processing.");
                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
            }
        }
        /*if (getArguments() != null) {
            imageUri = getArguments().getParcelable("imageUri");
            if (imageUri == null) {
                Log.e(TAG, "No image URI received for processing.");
                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
                // Safely pop back to the previous fragment if no image is provided
                if (isAdded()) {
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }
            currentDisplayImageUri = imageUri; // Initially, the displayed image is the captured one
        }
*/
        // Initialize the SAF launcher for creating PDF documents
        createPdfLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
            if (uri != null) {
                // User successfully selected a save location and filename
                Log.d(TAG, "User selected PDF save location: " + uri.toString());
                showProcessingUi(true); // Show progress UI
                // Call ViewModel to convert the current image to PDF and save to the user-selected URI
                scannerViewModel.convertImageToPdf(currentDisplayImageUri, uri);
            } else {
                // User cancelled the PDF save operation
                Toast.makeText(getContext(), "PDF save cancelled by user.", Toast.LENGTH_SHORT).show();
                // Clear any pending PDF URI in ViewModel if user cancels, to prevent stale data
                scannerViewModel.clearLastCreatedPdfUri();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_processor, container, false);
        capturedImageView = root.findViewById(R.id.captured_image_view);
        processImageButton = root.findViewById(R.id.process_image_button);
        createPdfButton = root.findViewById(R.id.create_pdf_button);
        processingProgressBar = root.findViewById(R.id.processing_progress_bar);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels.
        // ScannerViewModel is scoped to this Fragment's lifecycle.
        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        // HomeViewModel and OrganizerViewModel are scoped to the Activity's lifecycle
        // so they are shared across all fragments of MainActivity.
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        organizerViewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);

        // Display the initial captured image
        if (currentDisplayImageUri != null) {
            Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
        }

        // --- Set up UI element listeners ---

        processImageButton.setOnClickListener(v -> {
            if (currentDisplayImageUri != null) {
                // Generate a URI for the output of the processed image.
                // This is a temporary file in the app's cache directory.
                Uri outputProcessedUri = getProcessedImageOutputUri();
                if (outputProcessedUri != null) {
                    showProcessingUi(true); // Show progress
                    // Trigger image processing in the ViewModel
                    scannerViewModel.processScannedImage(currentDisplayImageUri, outputProcessedUri);
                    // Update the URI for the currently displayed image to the new processed one.
                    currentDisplayImageUri = outputProcessedUri;
                } else {
                    Toast.makeText(getContext(), "Could not create output file for processed image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No image to process.", Toast.LENGTH_SHORT).show();
            }
        });

        createPdfButton.setOnClickListener(v -> {
            if (currentDisplayImageUri != null) {
                // Ask the user to choose the save location for the PDF using SAF.
                String defaultFileName = "scan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".pdf";
                createPdfLauncher.launch(defaultFileName); // Launch the SAF document creation intent
            } else {
                Toast.makeText(getContext(), "No image to convert to PDF.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Observe ViewModel LiveData for state changes and updates ---

        scannerViewModel.getProcessingState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return; // Guard against null state

            switch (state) {
                case IDLE:
                    showProcessingUi(false);
                    break;
                case PROCESSING:
                    showProcessingUi(true);
                    break;
                case SUCCESS:
                    showProcessingUi(false);
                    Toast.makeText(getContext(), "Operation successful!", Toast.LENGTH_SHORT).show();

                    // If image processing was successful, reload the image into the ImageView
                    // to show the processed version.
                    if (currentDisplayImageUri != null) {
                        Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
                    }

                    // Check if the successful operation was PDF creation
                    Uri newlyCreatedPdfUri = scannerViewModel.getLastCreatedPdfUri().getValue();
                    if (newlyCreatedPdfUri != null) {
                        Log.d(TAG, "PDF created successfully at: " + newlyCreatedPdfUri.toString());

                        // 1. Trigger ViewModel reloads to update PDF lists in Home/Organizer Fragments
                        homeViewModel.loadPdfsFromStorage(requireContext());
                        organizerViewModel.loadAllPdfs(requireContext());

                        // 2. Navigate to ReaderFragment to automatically open the newly created PDF
                        NavController navController = Navigation.findNavController(requireView());
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("pdfUri", newlyCreatedPdfUri);
                        navController.navigate(R.id.navigation_reader, bundle);

                        // Clear the lastCreatedPdfUri in ViewModel immediately after consumption
                        // to prevent accidental re-navigation or stale data on subsequent operations.
                        scannerViewModel.clearLastCreatedPdfUri();
                    }
                    break;
                case FAILURE:
                    showProcessingUi(false);
                    Toast.makeText(getContext(), "Operation failed.", Toast.LENGTH_LONG).show(); // Longer toast for failures
                    // Clear the lastCreatedPdfUri in ViewModel on failure as well
                    scannerViewModel.clearLastCreatedPdfUri();
                    break;
            }
        });
    }

    /**
     * Generates a Uri for a temporary processed image file in the app's cache directory.
     * This file is intended for intermediate storage and can be deleted later.
     */
    private Uri getProcessedImageOutputUri() {
        File cacheDir = new File(requireContext().getCacheDir(), "scanned_images");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        String fileName = "processed_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(cacheDir, fileName);
        return Uri.fromFile(outputFile);
    }

    /**
     * Controls the visibility of the processing UI elements (ProgressBar, buttons).
     * @param show true to show progress UI and disable buttons, false to hide progress and enable buttons.
     */
    private void showProcessingUi(boolean show) {
        if (show) {
            processingProgressBar.setVisibility(View.VISIBLE);
            processImageButton.setEnabled(false);
            createPdfButton.setEnabled(false);
            // Optionally, disable other UI interactions or show a modal barrier
        } else {
            processingProgressBar.setVisibility(View.GONE);
            processImageButton.setEnabled(true);
            createPdfButton.setEnabled(true);
        }
    }
}


//package com.smartpdfsuite.fragments;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//
//import com.bumptech.glide.Glide;
///*import com.smartpdfsuite.R;*/
//import com.example.pdfviewer_editor_scanner.R;
//import com.smartpdfsuite.viewmodels.HomeViewModel;      // NEW import
//import com.smartpdfsuite.viewmodels.OrganizerViewModel; // NEW import
//import com.smartpdfsuite.viewmodels.ScannerViewModel;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Locale;
//
//public class ImageProcessorFragment extends Fragment {
//
//    private static final String TAG = "ImageProcessorFragment";
//    private Uri imageUri;
//    private ImageView capturedImageView;
//    private Button processImageButton;
//    private Button createPdfButton;
//    private ProgressBar processingProgressBar;
//    private ScannerViewModel scannerViewModel;
//    private HomeViewModel homeViewModel;       // NEW ViewModel instance
//    private OrganizerViewModel organizerViewModel; // NEW ViewModel instance
//
//    private Uri currentDisplayImageUri;
//    private ActivityResultLauncher<String> createPdfLauncher; // For SAF save
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            imageUri = getArguments().getParcelable("imageUri");
//            if (imageUri == null) {
//                Log.e(TAG, "No image URI received for processing.");
//                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
//                if (isAdded()) {
//                    Navigation.findNavController(requireView()).popBackStack();
//                }
//            }
//            currentDisplayImageUri = imageUri;
//        }
//
//        // Initialize SAF launcher
//        createPdfLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
//            if (uri != null) {
//                Log.d(TAG, "User selected PDF save location: " + uri.toString());
//                showProcessingUi(true);
//                // Call ViewModel to convert and save to the user-selected URI
//                scannerViewModel.convertImageToPdf(currentDisplayImageUri, uri);
//            } else {
//                Toast.makeText(getContext(), "PDF save cancelled by user.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_image_processor, container, false);
//        capturedImageView = root.findViewById(R.id.captured_image_view);
//        processImageButton = root.findViewById(R.id.process_image_button);
//        createPdfButton = root.findViewById(R.id.create_pdf_button);
//        processingProgressBar = root.findViewById(R.id.processing_progress_bar);
//        return root;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
//        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);       // Get shared ViewModel
//        organizerViewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class); // Get shared ViewModel
//
//        if (currentDisplayImageUri != null) {
//            Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
//        }
//
//        processImageButton.setOnClickListener(v -> {
//            if (currentDisplayImageUri != null) {
//                Uri outputProcessedUri = getProcessedImageOutputUri();
//                if (outputProcessedUri != null) {
//                    showProcessingUi(true);
//                    scannerViewModel.processScannedImage(currentDisplayImageUri, outputProcessedUri);
//                    currentDisplayImageUri = outputProcessedUri;
//                } else {
//                    Toast.makeText(getContext(), "Could not create output file for processed image.", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(getContext(), "No image to process.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        createPdfButton.setOnClickListener(v -> {
//            if (currentDisplayImageUri != null) {
//                // Trigger SAF to let user choose save location
//                String defaultFileName = "scan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".pdf";
//                createPdfLauncher.launch(defaultFileName);
//            } else {
//                Toast.makeText(getContext(), "No image to convert to PDF.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        scannerViewModel.getProcessingState().observe(getViewLifecycleOwner(), state -> {
//            if (state == null) return;
//
//            switch (state) {
//                case IDLE:
//                    showProcessingUi(false);
//                    break;
//                case PROCESSING:
//                    showProcessingUi(true);
//                    break;
//                case SUCCESS:
//                    showProcessingUi(false);
//                    Toast.makeText(getContext(), "Operation successful!", Toast.LENGTH_SHORT).show();
//                    if (currentDisplayImageUri != null) {
//                        Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
//                    }
//
//                    // --- NEW LOGIC FOR PDF CREATION SUCCESS ---
//                    Uri newlyCreatedPdfUri = scannerViewModel.getLastCreatedPdfUri().getValue();
//                    if (newlyCreatedPdfUri != null) {
//                        Log.d(TAG, "PDF created successfully at: " + newlyCreatedPdfUri.toString());
//
//                        // 1. Trigger ViewModel reloads to update PDF lists
//                        homeViewModel.loadPdfsFromStorage(requireContext());
//                        organizerViewModel.loadAllPdfs(requireContext());
//
//                        // 2. Navigate to ReaderFragment to open the new PDF
//                        NavController navController = Navigation.findNavController(requireView());
//                        Bundle bundle = new Bundle();
//                        bundle.putParcelable("pdfUri", newlyCreatedPdfUri);
//                        navController.navigate(R.id.navigation_reader, bundle);
//
//                        // Clear the lastCreatedPdfUri in ViewModel to prevent re-navigation
//                        scannerViewModel.getLastCreatedPdfUri().postValue(null);
//                    }
//                    // --- END NEW LOGIC ---
//                    break;
//                case FAILURE:
//                    showProcessingUi(false);
//                    Toast.makeText(getContext(), "Operation failed.", Toast.LENGTH_LONG).show();
//                    // Clear the lastCreatedPdfUri in ViewModel on failure as well
//                    scannerViewModel.getLastCreatedPdfUri().postValue(null);
//                    break;
//            }
//        });
//    }
//
//    private Uri getProcessedImageOutputUri() {
//        File cacheDir = new File(requireContext().getCacheDir(), "scanned_images");
//        if (!cacheDir.exists()) {
//            cacheDir.mkdirs();
//        }
//        String fileName = "processed_" + System.currentTimeMillis() + ".jpg";
//        File outputFile = new File(cacheDir, fileName);
//        return Uri.fromFile(outputFile);
//    }
//
//    private void showProcessingUi(boolean show) {
//        if (show) {
//            processingProgressBar.setVisibility(View.VISIBLE);
//            processImageButton.setEnabled(false);
//            createPdfButton.setEnabled(false);
//        } else {
//            processingProgressBar.setVisibility(View.GONE);
//            processImageButton.setEnabled(true);
//            createPdfButton.setEnabled(true);
//        }
//    }
//}
//
//
//
////package com.smartpdfsuite.fragments;
////
////import android.net.Uri;
////import android.os.Bundle;
////import android.util.Log;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.Button;
////import android.widget.ImageView;
////import android.widget.ProgressBar;
////import android.widget.Toast;
////
////import androidx.activity.result.ActivityResultLauncher;
////import androidx.activity.result.contract.ActivityResultContracts;
////import androidx.annotation.NonNull;
////import androidx.annotation.Nullable;
////import androidx.fragment.app.Fragment;
////import androidx.lifecycle.ViewModelProvider;
////import androidx.navigation.NavController;
////import androidx.navigation.Navigation;
////
////import com.bumptech.glide.Glide;
//////import com.smartpdfsuite.R;
////import com.example.pdfviewer_editor_scanner.R;
////import com.smartpdfsuite.viewmodels.HomeViewModel;      // NEW import
////import com.smartpdfsuite.viewmodels.OrganizerViewModel; // NEW import
////import com.smartpdfsuite.viewmodels.ScannerViewModel;
////
////import java.io.File;
////import java.text.SimpleDateFormat;
////import java.util.Locale;
////
////public class ImageProcessorFragment extends Fragment {
////
////    private static final String TAG = "ImageProcessorFragment";
////    private Uri imageUri;
////    private ImageView capturedImageView;
////    private Button processImageButton;
////    private Button createPdfButton;
////    private ProgressBar processingProgressBar;
////    private ScannerViewModel scannerViewModel;
////    private HomeViewModel homeViewModel;       // NEW ViewModel instance
////    private OrganizerViewModel organizerViewModel; // NEW ViewModel instance
////
////    private Uri currentDisplayImageUri;
////    private ActivityResultLauncher<String> createPdfLauncher; // For SAF save
////
////    @Override
////    public void onCreate(@Nullable Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        if (getArguments() != null) {
////            imageUri = getArguments().getParcelable("imageUri");
////            if (imageUri == null) {
////                Log.e(TAG, "No image URI received for processing.");
////                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
////                if (isAdded()) {
////                    Navigation.findNavController(requireView()).popBackStack();
////                }
////            }
////            currentDisplayImageUri = imageUri;
////        }
////
////        // Initialize SAF launcher
////        createPdfLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
////            if (uri != null) {
////                Log.d(TAG, "User selected PDF save location: " + uri.toString());
////                showProcessingUi(true);
////                // Call ViewModel to convert and save to the user-selected URI
////                scannerViewModel.convertImageToPdf(currentDisplayImageUri, uri);
////            } else {
////                Toast.makeText(getContext(), "PDF save cancelled by user.", Toast.LENGTH_SHORT).show();
////            }
////        });
////    }
////
////    @Override
////    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
////        View root = inflater.inflate(R.layout.fragment_image_processor, container, false);
////        capturedImageView = root.findViewById(R.id.captured_image_view);
////        processImageButton = root.findViewById(R.id.process_image_button);
////        createPdfButton = root.findViewById(R.id.create_pdf_button);
////        processingProgressBar = root.findViewById(R.id.processing_progress_bar);
////        return root;
////    }
////
////    @Override
////    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
////        super.onViewCreated(view, savedInstanceState);
////        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
////        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);       // Get shared ViewModel
////        organizerViewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class); // Get shared ViewModel
////
////        if (currentDisplayImageUri != null) {
////            Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
////        }
////
////        processImageButton.setOnClickListener(v -> {
////            if (currentDisplayImageUri != null) {
////                Uri outputProcessedUri = getProcessedImageOutputUri();
////                if (outputProcessedUri != null) {
////                    showProcessingUi(true);
////                    scannerViewModel.processScannedImage(currentDisplayImageUri, outputProcessedUri);
////                    currentDisplayImageUri = outputProcessedUri;
////                } else {
////                    Toast.makeText(getContext(), "Could not create output file for processed image.", Toast.LENGTH_SHORT).show();
////                }
////            } else {
////                Toast.makeText(getContext(), "No image to process.", Toast.LENGTH_SHORT).show();
////            }
////        });
////
////        createPdfButton.setOnClickListener(v -> {
////            if (currentDisplayImageUri != null) {
////                // Trigger SAF to let user choose save location
////                String defaultFileName = "scan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".pdf";
////                createPdfLauncher.launch(defaultFileName);
////            } else {
////                Toast.makeText(getContext(), "No image to convert to PDF.", Toast.LENGTH_SHORT).show();
////            }
////        });
////
////        scannerViewModel.getProcessingState().observe(getViewLifecycleOwner(), state -> {
////            if (state == null) return;
////
////            switch (state) {
////                case IDLE:
////                    showProcessingUi(false);
////                    break;
////                case PROCESSING:
////                    showProcessingUi(true);
////                    break;
////                case SUCCESS:
////                    showProcessingUi(false);
////                    Toast.makeText(getContext(), "Operation successful!", Toast.LENGTH_SHORT).show();
////                    if (currentDisplayImageUri != null) {
////                        Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
////                    }
////
////                    // --- NEW LOGIC FOR PDF CREATION SUCCESS ---
////                    Uri newlyCreatedPdfUri = scannerViewModel.getLastCreatedPdfUri().getValue();
////                    if (newlyCreatedPdfUri != null) {
////                        Log.d(TAG, "PDF created successfully at: " + newlyCreatedPdfUri.toString());
////
////                        // 1. Trigger ViewModel reloads to update PDF lists
////                        homeViewModel.loadPdfsFromStorage(requireContext());
////                        organizerViewModel.loadAllPdfs(requireContext());
////
////                        // 2. Navigate to ReaderFragment to open the new PDF
////                        NavController navController = Navigation.findNavController(requireView());
////                        Bundle bundle = new Bundle();
////                        bundle.putParcelable("pdfUri", newlyCreatedPdfUri);
////                        navController.navigate(R.id.navigation_reader, bundle);
////
////                        // Clear the lastCreatedPdfUri in ViewModel to prevent re-navigation
////                        scannerViewModel.getLastCreatedPdfUri().postValue(null);
////                    }
////                    // --- END NEW LOGIC ---
////                    break;
////                case FAILURE:
////                    showProcessingUi(false);
////                    Toast.makeText(getContext(), "Operation failed.", Toast.LENGTH_LONG).show();
////                    // Clear the lastCreatedPdfUri in ViewModel on failure as well
////                    scannerViewModel.getLastCreatedPdfUri().postValue(null);
////                    break;
////            }
////        });
////    }
////
////    private Uri getProcessedImageOutputUri() {
////        File cacheDir = new File(requireContext().getCacheDir(), "scanned_images");
////        if (!cacheDir.exists()) {
////            cacheDir.mkdirs();
////        }
////        String fileName = "processed_" + System.currentTimeMillis() + ".jpg";
////        File outputFile = new File(cacheDir, fileName);
////        return Uri.fromFile(outputFile);
////    }
////
////    private void showProcessingUi(boolean show) {
////        if (show) {
////            processingProgressBar.setVisibility(View.VISIBLE);
////            processImageButton.setEnabled(false);
////            createPdfButton.setEnabled(false);
////        } else {
////            processingProgressBar.setVisibility(View.GONE);
////            processImageButton.setEnabled(true);
////            createPdfButton.setEnabled(true);
////        }
////    }
////}
////
////
////
//////package com.smartpdfsuite.fragments;
//////
//////import android.net.Uri;
//////import android.os.Bundle;
//////import android.util.Log;
//////import android.view.LayoutInflater;
//////import android.view.View;
//////import android.view.ViewGroup;
//////import android.widget.Button;
//////import android.widget.ImageView;
//////import android.widget.ProgressBar;
//////import android.widget.Toast;
//////
//////import androidx.annotation.NonNull;
//////import androidx.annotation.Nullable;
//////import androidx.fragment.app.Fragment;
//////import androidx.lifecycle.ViewModelProvider;
//////import androidx.navigation.NavController;
//////import androidx.navigation.Navigation;
//////
//////import com.bumptech.glide.Glide;
////////import com.smartpdfsuite.R;
//////import com.example.pdfviewer_editor_scanner.R;
//////import com.smartpdfsuite.viewmodels.ScannerViewModel;
//////
//////import java.io.File;
//////import java.text.SimpleDateFormat;
//////import java.util.Locale;
//////
//////public class ImageProcessorFragment extends Fragment {
//////
//////    private static final String TAG = "ImageProcessorFragment";
//////    private Uri imageUri; // The initial captured image URI
//////    private ImageView capturedImageView;
//////    private Button processImageButton;
//////    private Button createPdfButton;
//////    private ProgressBar processingProgressBar;
//////    private ScannerViewModel scannerViewModel;
//////
//////    // This URI will be updated to point to the processed image after ImageUtils.processScannedImage
//////    private Uri currentDisplayImageUri;
//////    private Uri outputPdfUri;
//////
//////    @Override
//////    public void onCreate(@Nullable Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        if (getArguments() != null) {
//////            imageUri = getArguments().getParcelable("imageUri");
//////            if (imageUri == null) {
//////                Log.e(TAG, "No image URI received for processing.");
//////                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
//////                // Safe navigation back to previous fragment if no image
//////                if (isAdded()) { // Check if fragment is attached
//////                    Navigation.findNavController(requireView()).popBackStack();
//////                }
//////            }
//////            currentDisplayImageUri = imageUri; // Initially display the captured image
//////        }
//////    }
//////
//////    @Override
//////    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//////        View root = inflater.inflate(R.layout.fragment_image_processor, container, false);
//////        capturedImageView = root.findViewById(R.id.captured_image_view);
//////        processImageButton = root.findViewById(R.id.process_image_button);
//////        createPdfButton = root.findViewById(R.id.create_pdf_button);
//////        processingProgressBar = root.findViewById(R.id.processing_progress_bar);
//////        return root;
//////    }
//////
//////    @Override
//////    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//////        super.onViewCreated(view, savedInstanceState);
//////        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
//////
//////        // Display the captured image
//////        if (currentDisplayImageUri != null) {
//////            Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
//////        }
//////
//////        processImageButton.setOnClickListener(v -> {
//////            if (currentDisplayImageUri != null) {
//////                Uri outputProcessedUri = getProcessedImageOutputUri();
//////                if (outputProcessedUri != null) {
//////                    showProcessingUi(true);
//////                    scannerViewModel.processScannedImage(currentDisplayImageUri, outputProcessedUri);
//////                    // Update the URI that will be displayed and used for next step
//////                    currentDisplayImageUri = outputProcessedUri;
//////                } else {
//////                    Toast.makeText(getContext(), "Could not create output file for processed image.", Toast.LENGTH_SHORT).show();
//////                }
//////            } else {
//////                Toast.makeText(getContext(), "No image to process.", Toast.LENGTH_SHORT).show();
//////            }
//////        });
//////
//////        createPdfButton.setOnClickListener(v -> {
//////            if (currentDisplayImageUri != null) {
//////                outputPdfUri = getPdfOutputUri();
//////                if (outputPdfUri != null) {
//////                    showProcessingUi(true);
//////                    scannerViewModel.convertImageToPdf(currentDisplayImageUri, outputPdfUri);
//////                } else {
//////                    Toast.makeText(getContext(), "Could not create output file for PDF.", Toast.LENGTH_SHORT).show();
//////                }
//////            } else {
//////                Toast.makeText(getContext(), "No image to convert to PDF.", Toast.LENGTH_SHORT).show();
//////            }
//////        });
//////
//////        // Observe the new ProcessingState from the ViewModel
//////        scannerViewModel.getProcessingState().observe(getViewLifecycleOwner(), state -> {
//////            if (state == null) return;
//////
//////            switch (state) {
//////                case IDLE:
//////                    showProcessingUi(false);
//////                    break;
//////                case PROCESSING:
//////                    showProcessingUi(true);
//////                    break;
//////                case SUCCESS:
//////                    showProcessingUi(false);
//////                    Toast.makeText(getContext(), "Operation successful!", Toast.LENGTH_SHORT).show();
//////                    // If image processing was successful, reload the image into the ImageView
//////                    // to show the processed version.
//////                    if (currentDisplayImageUri != null) {
//////                        Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
//////                    }
//////                    if (outputPdfUri != null) { // outputPdfUri needs to be accessible here
//////                        Log.d(TAG, "PDF saved at: " + outputPdfUri.getPath());
//////                        // You might want to update a LiveData in the ViewModel with the final PDF URI
//////                        // so this Fragment can display it or pass it to the ReaderFragment.
//////                    }
//////                    // TODO: If PDF created, you might want to navigate to the ReaderFragment
//////                    // Example:
//////                    // NavController navController = Navigation.findNavController(requireView());
//////                    // Bundle bundle = new Bundle();
//////                    // bundle.putParcelable("pdfUri", outputPdfUriFromTheViewModel); // You'd get this from ViewModel
//////                    // navController.navigate(R.id.navigation_reader, bundle);
//////                    break;
//////                case FAILURE:
//////                    showProcessingUi(false);
//////                    Toast.makeText(getContext(), "Operation failed.", Toast.LENGTH_LONG).show(); // Longer toast for failure
//////                    break;
//////            }
//////        });
//////    }
//////
//////    private Uri getProcessedImageOutputUri() {
//////        // Use app-specific cache directory for temporary processed images.
//////        File cacheDir = new File(requireContext().getCacheDir(), "scanned_images");
//////        if (!cacheDir.exists()) {
//////            cacheDir.mkdirs();
//////        }
//////        String fileName = "processed_" + System.currentTimeMillis() + ".jpg";
//////        File outputFile = new File(cacheDir, fileName);
//////        return Uri.fromFile(outputFile);
//////    }
//////
//////    private Uri getPdfOutputUri() {
//////        // For saving the final PDF, it's best to use SAF (Storage Access Framework)
//////        // to let the user choose the location. For simplicity in this example,
//////        // we're saving to app-specific files directory.
//////        File filesDir = new File(requireContext().getFilesDir(), "SmartPdfSuite_PDFs");
//////        if (!filesDir.exists()) {
//////            filesDir.mkdirs();
//////        }
//////        String fileName = "scan_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".pdf";
//////        File outputFile = new File(filesDir, fileName);
//////        return Uri.fromFile(outputFile);
//////    }
//////
//////
//////    private void showProcessingUi(boolean show) {
//////        if (show) {
//////            processingProgressBar.setVisibility(View.VISIBLE);
//////            processImageButton.setEnabled(false);
//////            createPdfButton.setEnabled(false);
//////            // Optionally, disable other UI interactions
//////        } else {
//////            processingProgressBar.setVisibility(View.GONE);
//////            processImageButton.setEnabled(true);
//////            createPdfButton.setEnabled(true);
//////        }
//////    }
//////}
package com.smartpdfsuite.fragments;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.button.MaterialButton;
import com.smartpdfsuite.R;

import com.smartpdfsuite.viewmodels.HomeViewModel;
import com.smartpdfsuite.viewmodels.OrganizerViewModel;
import com.smartpdfsuite.viewmodels.ScannerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpdfsuite.adapters.FilterAdapter;
import com.smartpdfsuite.models.FilterModel;
import com.smartpdfsuite.utils.ImageUtils;
import com.smartpdfsuite.views.CropOverlayView;

public class ImageProcessorFragment extends Fragment {


    private RecyclerView filterRecycler;
    private FilterAdapter filterAdapter;
    private static final String TAG = "ImageProcessorFragment";
    private Uri imageUri; // The initial captured image URI received from ScannerFragment
    private ImageView capturedImageView;
   // private Button processImageButton;
    private MaterialButton createPdfButton;
    private ProgressBar processingProgressBar;

    // ViewModels for data management and operations
    private ScannerViewModel scannerViewModel;
    private HomeViewModel homeViewModel;
    private OrganizerViewModel organizerViewModel;

    // This URI will represent the image currently displayed and being processed.
    // It starts with `imageUri` and gets updated to `outputProcessedUri` after processing.
    private Uri currentDisplayImageUri;
    private Uri originalImageUri;
    private Bitmap bitmap;
    private ImageView imageView;
    private CropOverlayView cropOverlayView;

    // ActivityResultLauncher for Storage Access Framework (SAF) to let user choose PDF save location
    private ActivityResultLauncher<String> createPdfLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for filter 12_03_2026
        if (getArguments() != null) {

            String uriString = getArguments().getString("imageUri");

            if (uriString != null) {

                originalImageUri = Uri.parse(uriString);

                // initially show original image
                currentDisplayImageUri = originalImageUri;

            } else {

                Log.e(TAG, "No image received");
                Toast.makeText(getContext(), "Error: No image to process", Toast.LENGTH_SHORT).show();
            }
        }
        // Retrieve the initial image URI passed from ScannerFragment
        /*if (getArguments() != null) {
            String uriString = getArguments().getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
                currentDisplayImageUri = imageUri;
            }
            else {
                Log.e(TAG, "No image string received for processing.");
                Toast.makeText(getContext(), "Error: No image to process.", Toast.LENGTH_SHORT).show();
            }
        }*/
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
     //   processImageButton = root.findViewById(R.id.process_image_button);
        createPdfButton = root.findViewById(R.id.create_pdf_button);
        processingProgressBar = root.findViewById(R.id.processing_progress_bar);


        LinearLayout rotateLeft = root.findViewById(R.id.btn_rotate_left);
        LinearLayout rotateRight = root.findViewById(R.id.btn_rotate_right);


        rotateLeft.setOnClickListener(v -> rotateImage(-90));
        rotateRight.setOnClickListener(v -> rotateImage(90));




        ImageButton backButton = root.findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });


        return root;
    }

    private void rotateImage(int angle) {

        if (bitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );

        imageView.setImageBitmap(bitmap);

        // 🔥 Reset overlay after rotation
        imageView.post(() -> {
            RectF rect = getBitmapRect();
            cropOverlayView.setImageRect(rect);
        });

    }

    private RectF getBitmapRect() {
        Matrix m = imageView.getImageMatrix();
        float[] v = new float[9];
        m.getValues(v);

        float scaleX = v[Matrix.MSCALE_X];
        float scaleY = v[Matrix.MSCALE_Y];
        float transX = v[Matrix.MTRANS_X];
        float transY = v[Matrix.MTRANS_Y];

        float w = bitmap.getWidth() * scaleX;
        float h = bitmap.getHeight() * scaleY;

        return new RectF(
                transX,
                transY,
                transX + w,
                transY + h
        );
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

        // add filters 12_03_2026

        filterRecycler = view.findViewById(R.id.filter_recycler);

        filterRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false)
        );
        // Display the initial captured image
        if (currentDisplayImageUri != null) {
            Glide.with(this).load(currentDisplayImageUri).into(capturedImageView);
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().getContentResolver(),
                        originalImageUri
                );

                generateFilterPreviews(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }




       /* List<FilterModel> filters = new ArrayList<>();

        filters.add(new FilterModel("Original"));
        filters.add(new FilterModel("Auto"));
        filters.add(new FilterModel("Docs"));
        filters.add(new FilterModel("No Shadow"));
        filters.add(new FilterModel("Color"));
        filters.add(new FilterModel("Enhance2"));
        filters.add(new FilterModel("Black & White"));
        filters.add(new FilterModel("Gray"));
        filters.add(new FilterModel("Invert"));

        filterAdapter = new FilterAdapter(filters, filterName -> applyFilter(filterName));

        filterRecycler.setAdapter(filterAdapter);*/



        // --- Set up UI element listeners ---

       /* processImageButton.setOnClickListener(v -> {
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
        });*/

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
                    Toast.makeText(getContext(), "PDF created successfully!", Toast.LENGTH_SHORT).show();

                    // Reload PDF list
                    homeViewModel.loadPdfsFromStorage(requireContext());
                    organizerViewModel.loadAllPdfs(requireContext());

                    // Navigate to Home screen
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack(R.id.navigation_home, false);
                    navController.navigate(R.id.navigation_home);

                    // Clear URI
                    scannerViewModel.clearLastCreatedPdfUri();
                    break;
/*
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

                    break;*/
                case FAILURE:
                    showProcessingUi(false);
                    Toast.makeText(getContext(), "Operation failed.", Toast.LENGTH_LONG).show(); // Longer toast for failures
                    // Clear the lastCreatedPdfUri in ViewModel on failure as well
                    scannerViewModel.clearLastCreatedPdfUri();
                    break;
            }
        });
    }

    private void applyFilter(String filterName) {

        try{

            // ⭐ ALWAYS load original image
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    originalImageUri
            );

            Bitmap result;

            switch (filterName){
                case "Original":
                    result = bitmap;
                    currentDisplayImageUri = originalImageUri;
                    break;

                case "Auto":
                    result = ImageUtils.autoEnhance(bitmap);
                    break;

                case "Docs":
                    result = ImageUtils.documentMode(bitmap);
                    break;

                case "No Shadow":
                    result = ImageUtils.removeShadow(bitmap);
                    break;

                case "Color":
                    result = ImageUtils.colorBoost(bitmap);
                    break;

                case "Enhance2":
                    result = ImageUtils.enhance2(bitmap);
                    break;

                case "Black & White":
                    result = ImageUtils.blackWhite(bitmap);
                    break;

                case "Gray":
                    result = ImageUtils.gray(bitmap);
                    break;

                case "Invert":
                    result = ImageUtils.invert(bitmap);
                    break;

                default:
                    result = bitmap;
            }

            // show preview
            capturedImageView.setImageBitmap(result);

            // Save filtered bitmap (except original)
            if (!filterName.equals("Original")) {

                Uri filteredUri = ImageUtils.saveBitmapToCache(requireContext(), result);

                if (filteredUri != null) {
                    currentDisplayImageUri = filteredUri;
                }
            }
            /*// save filtered bitmap
            Uri filteredUri = ImageUtils.saveBitmapToCache(requireContext(), result);

            if (filteredUri != null) {
                currentDisplayImageUri = filteredUri;
            }*/

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void generateFilterPreviews(Bitmap originalBitmap) {

        List<FilterModel> filters = new ArrayList<>();

        Bitmap thumb = Bitmap.createScaledBitmap(originalBitmap, 200, 200, false);

        filters.add(new FilterModel("Original", thumb));
        filters.add(new FilterModel("Auto", ImageUtils.autoEnhance(thumb)));
        filters.add(new FilterModel("Docs", ImageUtils.documentMode(thumb)));
        filters.add(new FilterModel("No Shadow", ImageUtils.removeShadow(thumb)));
        filters.add(new FilterModel("Color", ImageUtils.colorBoost(thumb)));
        filters.add(new FilterModel("Enhance2", ImageUtils.enhance2(thumb)));
        filters.add(new FilterModel("Black & White", ImageUtils.blackWhite(thumb)));
        filters.add(new FilterModel("Gray", ImageUtils.gray(thumb)));
        filters.add(new FilterModel("Invert", ImageUtils.invert(thumb)));

        filterAdapter = new FilterAdapter(filters, filterName -> applyFilter(filterName));

       /* filterRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );*/

        filterRecycler.setAdapter(filterAdapter);
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
           // processImageButton.setEnabled(false);
            createPdfButton.setEnabled(false);
            // Optionally, disable other UI interactions or show a modal barrier
        } else {
            processingProgressBar.setVisibility(View.GONE);
          //  processImageButton.setEnabled(true);
            createPdfButton.setEnabled(true);
        }
    }
}



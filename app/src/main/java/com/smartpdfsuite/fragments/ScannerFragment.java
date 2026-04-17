package com.smartpdfsuite.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;
import com.smartpdfsuite.R;
import com.smartpdfsuite.viewmodels.ScannerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private static final String TAG = "ScannerFragment";

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPermissionLauncher();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scanner, container, false);

        previewView = root.findViewById(R.id.camera_preview_view);
        ImageButton captureButton = root.findViewById(R.id.capture_button);

        cameraExecutor = Executors.newSingleThreadExecutor();
        captureButton.setOnClickListener(v -> takePhoto());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN);


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // 🔥 Hide Toolbar
        if (requireActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .hide();
        }

        // 🔥 Hide Bottom Navigation
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) startCamera();
                            else Toast.makeText(
                                    requireContext(),
                                    "Camera permission denied",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File outputDir = new File(requireContext().getCacheDir(), "camera");
        if (!outputDir.exists()) outputDir.mkdirs();

        String fileName =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(System.currentTimeMillis()) + ".jpg";

        File photoFile = new File(outputDir, fileName);

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                options,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults results) {

                        requireActivity().runOnUiThread(() -> {

                            Uri imageUri = Uri.fromFile(photoFile);

                            Bundle bundle = new Bundle();
                            // ✅ CRITICAL FIX
                            bundle.putString("imageUri", imageUri.toString());

                            NavController navController =
                                    Navigation.findNavController(requireView());

                            navController.navigate(
                                    R.id.action_scannerFragment_to_edgeCropFragment,
                                    bundle
                            );
                        });
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Capture failed", exception);
                    }
                });
    }

    /*public void onResume() {
        super.onResume();

        if (getActivity() != null && getActivity().getSupportActionBar() != null) {
            getActivity().getSupportActionBar().hide();
        }
    }*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        // 🔥 Show Toolbar again
        if (requireActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .show();
        }

        // 🔥 Show Bottom Navigation again
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
    }
}


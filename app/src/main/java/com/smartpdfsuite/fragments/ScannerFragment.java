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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
/*import com.smartpdfsuite.R;*/
import com.example.pdfviewer_editor_scanner.R;
import com.smartpdfsuite.viewmodels.ScannerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private static final String TAG = "ScannerFragment";
    private ScannerViewModel scannerViewModel;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        previewView = root.findViewById(R.id.camera_preview_view);
        Button captureButton = root.findViewById(R.id.capture_button);

        cameraExecutor = Executors.newSingleThreadExecutor();

        captureButton.setOnClickListener(v -> takePhoto());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);

        // Check for Camera permission specifically when the fragment view is created
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(); // Start camera if permission is already granted
        } else {
            // Request Camera permission if not granted
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission granted, start camera
                startCamera();
            } else {
                // Permission denied, inform the user
                Toast.makeText(requireContext(), "Camera permission denied. Cannot scan.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startCamera() {
        // Get an instance of ProcessCameraProvider. This is a singleton.
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                // Bind the lifecycle of the camera to the lifecycle of this fragment
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create a Preview use case and set its surface provider
                Preview preview = new Preview.Builder().build();
                // Ensure previewView is not null before setting the surface provider
                if (previewView != null) {
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } else {
                    Log.e(TAG, "PreviewView is null, cannot set SurfaceProvider.");
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Camera preview not available.", Toast.LENGTH_LONG).show());
                    return;
                }


                // Create an ImageCapture use case
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // Optimize for low latency
                        .build();

                // Select the back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind any previously bound use cases from the cameraProvider
                cameraProvider.unbindAll();
                // Bind the camera to the lifecycle of this fragment, with selected use cases
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

                Log.d(TAG, "Camera started successfully.");

            } catch (ExecutionException | InterruptedException e) {
                // Handle errors during camera startup (e.g., no camera, camera in use, permissions issue)
                Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }, ContextCompat.getMainExecutor(requireContext())); // Ensure callback runs on main thread
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "Image capture not initialized.");
            Toast.makeText(requireContext(), "Camera not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        File outputDirectory = getPhotoOutputDirectory();
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg";
        File photoFile = new File(outputDirectory, fileName);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();


        imageCapture.takePicture(outputFileOptions,cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
                Log.d(TAG, msg);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

                // Navigate to ImageProcessorFragment on the main thread
                requireActivity().runOnUiThread(() -> {
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("imageUri", Uri.fromFile(photoFile));
                    navController.navigate(R.id.action_scannerFragment_to_imageProcessorFragment, bundle);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private File getPhotoOutputDirectory() {
        // Using app's cache directory for temporary files after capture
        File cacheDir = new File(requireContext().getCacheDir(), "camera_captures");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ensure the camera executor is shut down to prevent leaks
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}

/*
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.pdfviewer_editor_scanner.R;
import com.smartpdfsuite.viewmodels.ScannerViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private static final String TAG = "ScannerFragment";
    private ScannerViewModel scannerViewModel;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        previewView = root.findViewById(R.id.camera_preview_view);
        Button captureButton = root.findViewById(R.id.capture_button);

        cameraExecutor = Executors.newSingleThreadExecutor();

        captureButton.setOnClickListener(v -> takePhoto());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "Image capture not initialized.");
            return;
        }

        File outputDirectory = getPhotoOutputDirectory();
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg";
        File photoFile = new File(outputDirectory, fileName);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
                Log.d(TAG, msg);
                // The Toast needs to be on the UI thread
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

                // **** CRITICAL FIX: Wrap navigation in runOnUiThread() ****
                requireActivity().runOnUiThread(() -> {
                    NavController navController = Navigation.findNavController(requireView());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("imageUri", Uri.fromFile(photoFile));
                    navController.navigate(R.id.action_scannerFragment_to_imageProcessorFragment, bundle);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                // Toast needs to be on the UI thread
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File getPhotoOutputDirectory() {
        File cacheDir = new File(requireContext().getCacheDir(), "camera_captures");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}*/

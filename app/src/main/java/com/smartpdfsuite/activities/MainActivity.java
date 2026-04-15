
package com.smartpdfsuite.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpdfsuite.R;
import com.smartpdfsuite.fragments.ReaderFragment;
import com.smartpdfsuite.utils.PermissionUtils;
import com.smartpdfsuite.utils.ThemeUtils;
import com.smartpdfsuite.viewmodels.HomeViewModel;
//import android.view.View;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NavController navController;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private boolean permissionsHandled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.nav_view);

        // NavHostFragment (SAFE WAY)
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            Toast.makeText(this, "Navigation error", Toast.LENGTH_LONG).show();
            return;
        }

        navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        R.id.navigation_home,
                        R.id.navigation_maker,
                        R.id.navigation_editor,
                        R.id.navigation_scanner,
                        R.id.navigation_files
                ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);


        // 🔥 ADD THIS HERE
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            if (destination.getId() == R.id.navigation_home ||
                    destination.getId() == R.id.navigation_scanner ||
                    destination.getId() == R.id.navigation_files) {

                bottomNav.setVisibility(View.VISIBLE);

            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });
        setupPermissionLauncher();

        if (savedInstanceState == null) {
            checkAndRequestPermissions();
        } else {
            permissionsHandled = savedInstanceState.getBoolean("permissionsHandled", false);
            if (PermissionUtils.hasRequiredPermissions(this)) {
                loadInitialPdfData();
            }
        }

        handleIncomingIntent(getIntent());
    }

   /* private void handleExternalPdfIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri pdfUri = intent.getData();

            if (pdfUri != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("pdf_uri", pdfUri);

                NavController navController =
                        Navigation.findNavController(this, R.id.nav_host_fragment);

                navController.navigate(R.id.navigation_reader, bundle);
            }
        }
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null || navController == null) return;

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri pdfUri = intent.getData();
            if (pdfUri != null) {
                /*Bundle bundle = new Bundle();
                bundle.putParcelable("pdfUri", pdfUri);
                navController.navigate(R.id.navigation_reader, bundle);*/
                Bundle bundle = new Bundle();
                bundle.putString(ReaderFragment.ARG_PDF_URI, pdfUri.toString());
                bundle.putString(
                        ReaderFragment.ARG_PDF_NAME,
                        "External PDF"
                );
                navController.navigate(R.id.navigation_reader, bundle);
            }
        }

    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    permissionsHandled = true;
                    boolean allGranted = true;

                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, entry.getKey())) {
                                showPermissionPermanentlyDeniedDialog();
                                return;
                            }
                        }
                    }

                    if (allGranted) {
                        loadInitialPdfData();
                    } else {
                        Toast.makeText(this,
                                R.string.permission_denied_message,
                                Toast.LENGTH_LONG).show();
                        loadInitialPdfData();
                    }
                }
        );
    }

    private void checkAndRequestPermissions() {
        if (!PermissionUtils.hasRequiredPermissions(this)) {
            permissionLauncher.launch(PermissionUtils.getRequiredPermissions());
        } else {
            permissionsHandled = true;
            loadInitialPdfData();
        }
    }

    private void showPermissionPermanentlyDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("Please enable permissions manually from Settings to access PDF files.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void loadInitialPdfData() {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.loadPdfsFromStorage(this);
        Log.d(TAG, "PDF data loading triggered");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp()
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("permissionsHandled", permissionsHandled);
    }
}



//hide on 18-01-2026
//package com.smartpdfsuite.activities;

/*import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartpdfsuite.R;

import com.smartpdfsuite.fragments.ReaderFragment;
import com.smartpdfsuite.utils.PermissionUtils;
import com.smartpdfsuite.utils.ThemeUtils;
import com.smartpdfsuite.viewmodels.HomeViewModel;
import com.smartpdfsuite.viewmodels.OrganizerViewModel;


import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private boolean permissionsHandled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the Toolbar from your layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set it as the Activity's ActionBar

        BottomNavigationView navView = findViewById(R.id.nav_view);

        findViewById(R.id.nav_host_fragment).post(() -> {
            try {
                navController = Navigation.findNavController(this, R.id.nav_host_fragment);

                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.navigation_home, R.id.navigation_maker, R.id.navigation_editor,
                        R.id.navigation_scanner, R.id.navigation_files)
                        .build();

                // Now setupActionBarWithNavController will find the Toolbar we just set
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(navView, navController);

                Log.d(TAG, "NavController successfully initialized and set up.");

                if (savedInstanceState == null) {
                    checkAndRequestPermissions();
                } else {
                    permissionsHandled = savedInstanceState.getBoolean("permissionsHandled", false);
                    if (PermissionUtils.hasRequiredPermissions(this)) {
                        loadInitialPdfData();
                    }
                }

                handleIncomingIntent(getIntent());

            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to find NavController or set it up: " + e.getMessage(), e);
                Toast.makeText(this, "Navigation setup error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        setupPermissionLauncher();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("permissionsHandled", permissionsHandled);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (navController == null) {
            Log.w(TAG, "NavController not yet initialized, deferring intent handling.");
            // Consider re-trying in onResume or using a LiveData to trigger after NavController is ready
            return;
        }

        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri pdfUri = intent.getData();
            if (pdfUri != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("pdfUri", pdfUri);
                runOnUiThread(() -> {
                    navController.navigate(R.id.navigation_reader, bundle);
                    Toast.makeText(this, "Opening PDF: " + pdfUri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            permissionsHandled = true;
            boolean allGranted = true;
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    allGranted = false;
                    Log.w(TAG, "Permission denied: " + entry.getKey());
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, entry.getKey())) {
                        showPermissionPermanentlyDeniedDialog();
                        return;
                    }
                }
            }

            if (allGranted) {
                Log.d(TAG, "All necessary permissions granted.");
                Toast.makeText(MainActivity.this, "Permissions granted. Loading PDFs...", Toast.LENGTH_SHORT).show();
                loadInitialPdfData();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Some permissions were denied. App functionality may be limited.");
                loadInitialPdfData();
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (!PermissionUtils.hasRequiredPermissions(this)) {
            Log.d(TAG, "Missing permissions. Checking for rationale...");
            boolean shouldShowRationale = false;
            for (String permission : PermissionUtils.getRequiredPermissions()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                showPermissionRationaleDialog();
            } else {
                Log.d(TAG, "No rationale needed, launching system permission dialog directly.");
                requestPermissionLauncher.launch(PermissionUtils.getRequiredPermissions());
            }
        } else {
            Log.d(TAG, "All required permissions already granted.");
            permissionsHandled = true;
            loadInitialPdfData();
        }
    }

    private void showPermissionRationaleDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Required")
                .setMessage("This app needs Camera permission for PDF scanning and Storage/Media access to read/write PDF files. Please grant these permissions to use all features.")
                .setPositiveButton("Grant Permissions", (dialog, which) -> {
                    Log.d(TAG, "User accepted rationale, requesting permissions.");
                    requestPermissionLauncher.launch(PermissionUtils.getRequiredPermissions());
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "User denied rationale.");
                    Toast.makeText(MainActivity.this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                    permissionsHandled = true;
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionPermanentlyDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Permanently Denied")
                .setMessage("It looks like you've permanently denied some essential permissions. To use all features of Smart PDF Suite, please enable them manually in app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Log.d(TAG, "User clicked to go to settings.");
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "User cancelled from permanently denied dialog.");
                    Toast.makeText(MainActivity.this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                    permissionsHandled = true;
                })
                .setCancelable(false)
                .show();
    }

    private void loadInitialPdfData() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        HomeViewModel homeViewModel = viewModelProvider.get(HomeViewModel.class);
        homeViewModel.loadPdfsFromStorage(this);

        // OrganizerViewModel organizerViewModel = viewModelProvider.get(OrganizerViewModel.class);
        // organizerViewModel.loadAllPdfs(this);
        Log.d(TAG, "Triggered initial PDF data loading.");
    }

    public void toggleTheme() {
        ThemeUtils.toggleTheme(this);
        recreate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the Up button in the ActionBar/Toolbar for navigation
        if (navController != null) {
            // Check if navController is ready before using it
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}*/

//





/*
package com.smartpdfsuite.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pdfviewer_editor_scanner.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.smartpdfsuite.fragments.ReaderFragment;
import com.smartpdfsuite.utils.PermissionUtils;
import com.smartpdfsuite.utils.ThemeUtils;

import java.util.Map;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;

    private AppBarConfiguration appBarConfiguration;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate to apply dark/light mode
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 1️⃣ Get NavHostFragment SAFELY
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            throw new RuntimeException("NavHostFragment not found");
        }

        // 2️⃣ Get NavController
        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_maker, R.id.navigation_editor, R.id.navigation_scanner, R.id.navigation_files)
                .build();
        // 4️⃣ Setup ActionBar
        NavigationUI.setupActionBarWithNavController(
                this, navController, appBarConfiguration);

        // 5️⃣ Setup BottomNavigationView
        NavigationUI.setupWithNavController(navView, navController);

//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_maker, R.id.navigation_editor, R.id.navigation_scanner, R.id.navigation_files)
//                .build();
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        setupPermissionLauncher();
        checkAndRequestPermissions();

        handleIncomingIntent(getIntent());
    }


    // 🔙 Handle Up button
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri pdfUri = intent.getData();
            if (pdfUri != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("pdfUri", pdfUri);
                navController.navigate(R.id.navigation_reader, bundle);
                Toast.makeText(this, "Opening PDF: " + pdfUri.getLastPathSegment(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Log.d(TAG, "All necessary permissions granted.");
                // You can trigger refresh or initial data loading here
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Some permissions were denied.");
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (!PermissionUtils.hasRequiredPermissions(this)) {
            requestPermissionLauncher.launch(PermissionUtils.getRequiredPermissions());
        }
    }

    // You might want to handle theme changes from a settings menu or similar
    public void toggleTheme() {
        ThemeUtils.toggleTheme(this);
        recreate(); // Recreate activity to apply new theme
    }
}*/


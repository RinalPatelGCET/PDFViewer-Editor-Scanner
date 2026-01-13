package com.smartpdfsuite.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    /**
     * Returns an array of permissions required for the app, tailored for different Android versions.
     * Includes CAMERA and storage read permissions.
     */
    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA); // For PDF Scanner

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33+)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            // Consider READ_MEDIA_VIDEO/AUDIO if your app processes them too.
        } else { // Below Android 13 (API 32 and below)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            // WRITE_EXTERNAL_STORAGE generally not needed for reading MediaStore PDFs or writing via SAF.
        }
        return permissions.toArray(new String[0]);
    }

    /**
     * Checks if all required permissions have been granted.
     * @param context The application context.
     * @return true if all permissions are granted, false otherwise.
     */
    public static boolean hasRequiredPermissions(Context context) {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
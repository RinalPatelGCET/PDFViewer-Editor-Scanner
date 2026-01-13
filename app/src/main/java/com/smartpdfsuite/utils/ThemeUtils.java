package com.smartpdfsuite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    private static final String PREFS_NAME = "SmartPdfSuitePrefs";
    private static final String THEME_KEY = "app_theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2; // Follow system default

    public static void applyTheme(Context context) {
        int theme = getSavedTheme(context);
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void toggleTheme(Context context) {
        int currentTheme = getSavedTheme(context);
        int newTheme;
        if (currentTheme == THEME_LIGHT) {
            newTheme = THEME_DARK;
        } else {
            newTheme = THEME_LIGHT; // Default to light if dark or system
        }
        saveTheme(context, newTheme);
        applyTheme(context);
    }

    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(THEME_KEY, THEME_SYSTEM); // Default to system theme
    }

    public static void saveTheme(Context context, int theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(THEME_KEY, theme).apply();
    }
}
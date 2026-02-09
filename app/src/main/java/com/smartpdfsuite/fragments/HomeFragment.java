package com.smartpdfsuite.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpdfsuite.R;
import com.smartpdfsuite.adapters.PdfListAdapter;
import com.smartpdfsuite.models.PdfDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements PdfListAdapter.OnPdfClickListener {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private RecyclerView pdfRecyclerView;
    private TextView noPdfsText;
    private PdfListAdapter pdfListAdapter;
    private final List<PdfDocument> pdfList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        pdfRecyclerView = view.findViewById(R.id.recent_pdfs_recycler_view);
        noPdfsText = view.findViewById(R.id.no_pdfs_text);

        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pdfListAdapter = new PdfListAdapter(pdfList, this);
        pdfRecyclerView.setAdapter(pdfListAdapter);

        checkStoragePermissionAndLoad();
        return view;
    }

    // =========================
    // Permission Handling
    // =========================
    private void checkStoragePermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ requires MANAGE_EXTERNAL_STORAGE for full file access
            if (!Environment.isExternalStorageManager()) {
                // Ask user to grant "All Files Access"
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                Toast.makeText(getContext(), "Please grant All Files Access in Settings", Toast.LENGTH_LONG).show();
                return;
            } else {
                loadPdfs(requireContext());
            }
        } else {
            // Android <= 10
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                loadPdfs(requireContext());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPdfs(requireContext());
            } else {
                Toast.makeText(getContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
                pdfRecyclerView.setVisibility(View.GONE);
                noPdfsText.setVisibility(View.VISIBLE);
            }
        }
    }

    // =========================
    // Load PDFs (MediaStore + Folder Scan)
    // =========================
    private void loadPdfs(Context context) {
        pdfList.clear();

        // 1️⃣ Load PDFs via MediaStore
        loadPdfsFromMediaStore(context);

        // 2️⃣ Fallback scan Downloads/Documents folders
        if (pdfList.isEmpty()) {
            loadPdfsFromFileSystem();
        }

        updateUI();
    }

    private void loadPdfsFromMediaStore(Context context) {
        Uri collection = MediaStore.Files.getContentUri("external");
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
        };

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String[] selectionArgs = {"application/pdf"};
        String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
                    long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED));
                    Uri uri = Uri.withAppendedPath(collection, id);

                    pdfList.add(new PdfDocument(id, name, uri, null, size, dateModified));
                    Log.d("HomeFragment", "MediaStore PDF: " + name);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPdfsFromFileSystem() {
        // Scan Downloads and Documents folders
        String[] paths = {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()
        };

        int counter = 0;
        for (String path : paths) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                            pdfList.add(new PdfDocument(
                                    String.valueOf(counter++),
                                    file.getName(),
                                    Uri.fromFile(file),
                                    file.getAbsolutePath(),
                                    file.length(),
                                    file.lastModified()
                            ));
                            Log.d("HomeFragment", "Fallback PDF: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    private void updateUI() {
        if (pdfList.isEmpty()) {
            pdfRecyclerView.setVisibility(View.GONE);
            noPdfsText.setVisibility(View.VISIBLE);
        } else {
            pdfRecyclerView.setVisibility(View.VISIBLE);
            noPdfsText.setVisibility(View.GONE);
            pdfListAdapter.notifyDataSetChanged();
        }
    }

    // =========================
    // Adapter Callbacks
    // =========================
    @Override
    /*public void onPdfClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Clicked: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to ReaderFragment here
    }
*/

    public void onPdfClick(PdfDocument pdfDocument) {

        ReaderFragment readerFragment =
                ReaderFragment.newInstance(
                        pdfDocument.getUri(),
                        pdfDocument.getName()
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, readerFragment)
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void onPdfLongClick(PdfDocument pdfDocument) {
        Toast.makeText(getContext(), "Long pressed: " + pdfDocument.getName(), Toast.LENGTH_SHORT).show();
    }
}

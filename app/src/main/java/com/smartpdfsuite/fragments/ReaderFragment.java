package com.smartpdfsuite.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfPasswordException;
import com.smartpdfsuite.R;

import java.io.InputStream;

public class ReaderFragment extends Fragment
        implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    public static final String ARG_PDF_URI = "pdf_uri";
    public static final String ARG_PDF_NAME = "pdf_name";

    private PDFView pdfView;
    private Uri pdfUri;
    private String pdfName;
    private int pageNumber = 0;

    private String currentPassword = null; // 🔥 important

    // 🔥 Dialog variables
    private AlertDialog passwordDialog;
    private com.google.android.material.textfield.TextInputLayout inputLayout;
    private EditText input;

    public static ReaderFragment newInstance(Uri pdfUri, String pdfName) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PDF_URI, pdfUri);
        args.putString(ARG_PDF_NAME, pdfName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        pdfView = view.findViewById(R.id.pdfView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ From inside app (OrganizerFragment)
        if (getArguments() != null) {
            //pdfUri = getArguments().getParcelable("pdf_uri");
            String uriString =
                    getArguments().getString(ARG_PDF_URI);

            pdfName =
                    getArguments().getString(ARG_PDF_NAME);

            if (uriString != null) {
                pdfUri = Uri.parse(uriString);
            }
           /* String uriString = getArguments().getString("pdfUri");
            if (uriString != null) {
                pdfUri = Uri.parse(uriString);
            }
            pdfName = getArguments().getString("pdf_name");*/
        }

        // 2️⃣ From external apps (WhatsApp, Files, etc.)
        if (pdfUri == null && getActivity() != null) {
            Intent intent = getActivity().getIntent();
           /* if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                pdfUri = intent.getData();
            }*/
            if (intent != null) {

                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    pdfUri = intent.getData();
                } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
                    pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                }

               /* if (pdfUri != null) {
                    requireActivity().getContentResolver().takePersistableUriPermission(
                            pdfUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                }*/
                if (pdfUri != null) {

                    try {

                        InputStream inputStream =
                                requireContext()
                                        .getContentResolver()
                                        .openInputStream(pdfUri);

                        pdfView.fromStream(inputStream)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .defaultPage(0)
                                .load();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (pdfUri == null) {
            Toast.makeText(getContext(), "PDF not found", Toast.LENGTH_SHORT).show();
//            requireActivity().onBackPressed();
            requireActivity().runOnUiThread(() ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed());
            return;
        }

        if (pdfName != null) {
            requireActivity().setTitle(pdfName);
        }

        openPdf(pdfUri, null);
    }

    // ============================
    // 🔥 OPEN PDF (FIXED)
    // ============================
    private void openPdf(Uri uri, String password) {

        pdfView.fromUri(uri)
                .password(password)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .onPageChange(this)
                .onLoad(nbPages -> {
                    // ✅ SUCCESS → close dialog
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }
                    // ✅ Reset title after successful load
                    if (pdfName != null) {
                        requireActivity().setTitle(pdfName);
                    }
                })
                .onError(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(8)
                .load();
    }

    // ============================
    // 🔥 PASSWORD DIALOG
    // ============================
    private void showPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("This file is protected");

        inputLayout = new com.google.android.material.textfield.TextInputLayout(requireContext());
        inputLayout.setPadding(40, 20, 40, 0);
        inputLayout.setEndIconMode(
                com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
        );

        input = new EditText(requireContext());
        input.setHint("Password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        inputLayout.addView(input);
        builder.setView(inputLayout);

        builder.setCancelable(false);

        builder.setPositiveButton("Open", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        passwordDialog = builder.create();
        passwordDialog.show();

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String password = input.getText().toString().trim();

            if (password.isEmpty()) {
                inputLayout.setError("Enter password");
                return;
            }

            inputLayout.setError(null);
            currentPassword = password;

            openPdf(pdfUri, password); // 🔥 retry
        });

        // remove error while typing
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ============================
    // 🔥 ERROR HANDLER (MAIN FIX)
    // ============================
    @Override
    public void onError(Throwable t) {

        String errorMsg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";

        if (errorMsg.contains("password") || errorMsg.contains("security")) {

            if (passwordDialog != null && passwordDialog.isShowing()) {

                inputLayout.setError("Password incorrect"); // 🔥 show error
                input.setText("");

            } else {
                showPasswordDialog();
            }

        } else {
            Toast.makeText(getContext(), "Failed to open PDF", Toast.LENGTH_LONG).show();
        }

        t.printStackTrace();
    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        /* requireActivity().setTitle((page + 1) + " / " + pageCount);*/
        // ✅ Always keep PDF name in title
        if (pdfName != null) {
            requireActivity().setTitle(pdfName);
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        // PDF loaded successfully
    }


    public void onPause() {
        super.onPause();
        requireActivity().setTitle("Smart PDF");
    }
}





/*
package com.smartpdfsuite.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfPasswordException;
import com.smartpdfsuite.R;

import java.io.InputStream;

public class ReaderFragment extends Fragment
        implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    public static final String ARG_PDF_URI = "pdf_uri";
    public static final String ARG_PDF_NAME = "pdf_name";

    private PDFView pdfView;
    private Uri pdfUri;
    private String pdfName;
    private int pageNumber = 0;

    private String currentPassword = null; // 🔥 important

    // 🔥 Dialog variables
    private AlertDialog passwordDialog;
    private com.google.android.material.textfield.TextInputLayout inputLayout;
    private EditText input;

    public static ReaderFragment newInstance(Uri pdfUri, String pdfName) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PDF_URI, pdfUri);
        args.putString(ARG_PDF_NAME, pdfName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        pdfView = view.findViewById(R.id.pdfView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ From inside app (OrganizerFragment)
        if (getArguments() != null) {
            //pdfUri = getArguments().getParcelable("pdf_uri");
            String uriString =
                    getArguments().getString(ARG_PDF_URI);

            pdfName =
                    getArguments().getString(ARG_PDF_NAME);

            if (uriString != null) {
                pdfUri = Uri.parse(uriString);
            }
           */
/* String uriString = getArguments().getString("pdfUri");
            if (uriString != null) {
                pdfUri = Uri.parse(uriString);
            }
            pdfName = getArguments().getString("pdf_name");*//*

        }

        // 2️⃣ From external apps (WhatsApp, Files, etc.)
        if (pdfUri == null && getActivity() != null) {
            Intent intent = getActivity().getIntent();
           */
/* if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                pdfUri = intent.getData();
            }*//*

            if (intent != null) {

                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    pdfUri = intent.getData();
                } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
                    pdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                }

               */
/* if (pdfUri != null) {
                    requireActivity().getContentResolver().takePersistableUriPermission(
                            pdfUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                }*//*

                if (pdfUri != null) {

                    try {

                        InputStream inputStream =
                                requireContext()
                                        .getContentResolver()
                                        .openInputStream(pdfUri);

                        pdfView.fromStream(inputStream)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .defaultPage(0)
                                .load();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (pdfUri == null) {
            Toast.makeText(getContext(), "PDF not found", Toast.LENGTH_SHORT).show();
//            requireActivity().onBackPressed();
            requireActivity().runOnUiThread(() ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed());
            return;
        }

        if (pdfName != null) {
            requireActivity().setTitle(pdfName);
        }

        openPdf(pdfUri, null);
    }

    // ============================
    // 🔥 OPEN PDF (FIXED)
    // ============================
    private void openPdf(Uri uri, String password) {

        pdfView.fromUri(uri)
                .password(password)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .onPageChange(this)
                .onLoad(nbPages -> {
                    // ✅ SUCCESS → close dialog
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }
                    // ✅ Reset title after successful load
                    if (pdfName != null) {
                        requireActivity().setTitle(pdfName);
                    }
                })
                .onError(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(8)
                .load();
    }

    // ============================
    // 🔥 PASSWORD DIALOG
    // ============================
    private void showPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("This file is protected");

        inputLayout = new com.google.android.material.textfield.TextInputLayout(requireContext());
        inputLayout.setPadding(40, 20, 40, 0);
        inputLayout.setEndIconMode(
                com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
        );

        input = new EditText(requireContext());
        input.setHint("Password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        inputLayout.addView(input);
        builder.setView(inputLayout);

        builder.setCancelable(false);

        builder.setPositiveButton("Open", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        passwordDialog = builder.create();
        passwordDialog.show();

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String password = input.getText().toString().trim();

            if (password.isEmpty()) {
                inputLayout.setError("Enter password");
                return;
            }

            inputLayout.setError(null);
            currentPassword = password;

            openPdf(pdfUri, password); // 🔥 retry
        });

        // remove error while typing
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayout.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ============================
    // 🔥 ERROR HANDLER (MAIN FIX)
    // ============================
    @Override
    public void onError(Throwable t) {

        String errorMsg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";

        if (errorMsg.contains("password") || errorMsg.contains("security")) {

            if (passwordDialog != null && passwordDialog.isShowing()) {

                inputLayout.setError("Password incorrect"); // 🔥 show error
                input.setText("");

            } else {
                showPasswordDialog();
            }

        } else {
            Toast.makeText(getContext(), "Failed to open PDF", Toast.LENGTH_LONG).show();
        }

        t.printStackTrace();
    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        //requireActivity().setTitle((page + 1) + " / " + pageCount);
        // ✅ Always keep PDF name in title
        if (pdfName != null) {
            requireActivity().setTitle(pdfName);
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        // PDF loaded successfully
    }


    public void onPause() {
        super.onPause();

        requireActivity().setTitle("Smart PDF");
    }
}
*/

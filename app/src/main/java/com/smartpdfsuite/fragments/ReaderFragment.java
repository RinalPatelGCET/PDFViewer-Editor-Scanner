
// latest working code....

package com.smartpdfsuite.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.smartpdfsuite.R;

public class ReaderFragment extends Fragment
        implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    public static final String ARG_PDF_URI = "pdf_uri";
    public static final String ARG_PDF_NAME = "pdf_name";

    private PDFView pdfView;
    private Uri pdfUri;
    private String pdfName;
    private int pageNumber = 0;

    public static ReaderFragment newInstance(Uri pdfUri, String pdfName) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PDF_URI, pdfUri);
        args.putString(ARG_PDF_NAME, pdfName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            pdfUri = getArguments().getParcelable(ARG_PDF_URI);
            pdfName = getArguments().getString(ARG_PDF_NAME);
        }
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

        if (pdfUri == null) {
            Toast.makeText(getContext(), "PDF not found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        if (pdfName != null) {
            requireActivity().setTitle(pdfName);
        }

        openPdf(pdfUri);
    }

    private void openPdf(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .onPageChange(this)
                .onLoad(this)
                .onError(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(8)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        requireActivity().setTitle((page + 1) + " / " + pageCount);
    }

    @Override
    public void loadComplete(int nbPages) {
        // Silent success (no toast spam)
    }

    @Override
    public void onError(Throwable t) {
        Toast.makeText(getContext(), "Failed to open PDF", Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.reader_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_share && pdfUri != null) {
            sharePdf(pdfUri);
            return true;
        }

        if (item.getItemId() == R.id.action_bookmark) {
            Toast.makeText(getContext(), "Bookmark feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sharePdf(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share PDF"));
    }
}



/*
package com.smartpdfsuite.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.smartpdfsuite.R;

public class ReaderFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    private PDFView pdfView;
    private Uri pdfUri;
    private int pageNumber = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            pdfUri = getArguments().getParcelable("pdfUri");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        pdfView = view.findViewById(R.id.pdfView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (pdfUri != null) openPdf(pdfUri);
        else Toast.makeText(getContext(), "No PDF selected", Toast.LENGTH_SHORT).show();
    }

    private void openPdf(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange(this)
                .onLoad(this)
                .onError(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(8)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        if (getActivity() != null) getActivity().setTitle((page + 1) + " / " + pageCount);
    }

    @Override
    public void loadComplete(int nbPages) {
        Toast.makeText(getContext(), "PDF Loaded (" + nbPages + " pages)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable t) {
        Toast.makeText(getContext(), "Failed to open PDF", Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.reader_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share && pdfUri != null) {
            sharePdf(pdfUri);
            return true;
        }
        if (item.getItemId() == R.id.action_bookmark) {
            Toast.makeText(getContext(), "Bookmark coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePdf(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share PDF"));
    }
}
*/

package com.smartpdfsuite.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//import com.github.mhiew.android.pdfviewer.PDFView;
import com.example.pdfviewer_editor_scanner.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
//import com.smartpdfsuite.R;
import com.smartpdfsuite.viewmodels.ReaderViewModel; // You'd create this ViewModel

public class ReaderFragment extends Fragment implements OnPageChangeListener, OnLoadCompleteListener, OnErrorListener {

    private PDFView pdfView;
    private Uri pdfUri;
    private Integer pageNumber = 0;
    private ReaderViewModel readerViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Indicate that this fragment has an options menu
        if (getArguments() != null) {
            pdfUri = getArguments().getParcelable("pdfUri");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reader, container, false);
        pdfView = root.findViewById(R.id.pdfView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readerViewModel = new ViewModelProvider(this).get(ReaderViewModel.class);

        if (pdfUri != null) {
            displayPdf(pdfUri);
        } else {
            Toast.makeText(getContext(), "No PDF to display.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPdf(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .onLoad(this)
                .onError(this)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(10) // in dp
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        if (getActivity() != null) {
            getActivity().setTitle(String.format("%s %s / %s", pdfUri.getLastPathSegment(), page + 1, pageCount));
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        Toast.makeText(getContext(), "PDF Loaded (" + nbPages + " pages)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable t) {
        Toast.makeText(getContext(), "Error loading PDF: " + t.getMessage(), Toast.LENGTH_LONG).show();
        t.printStackTrace();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.reader_menu, menu);
        // You might want to dynamically show/hide bookmark icon based on current PDF's bookmark status
        // MenuItem bookmarkItem = menu.findItem(R.id.action_bookmark);
        // bookmarkItem.setIcon(readerViewModel.isBookmarked(pdfUri) ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_bookmark) {
            if (pdfUri != null) {
                // Handle bookmarking logic here, update UI accordingly
                Toast.makeText(getContext(), "Bookmark clicked for " + pdfUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
                // readerViewModel.toggleBookmark(pdfUri, pageNumber);
                // getActivity().invalidateOptionsMenu(); // To update icon
            }
            return true;
        } else if (id == R.id.action_share) {
            if (pdfUri != null) {
                sharePdf(pdfUri);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePdf(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Optionally, if sharing with other apps and using FileProvider:
        // shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Uri contentUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", new File(uri.getPath()));
        // shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(shareIntent, "Share PDF using..."));
    }
}

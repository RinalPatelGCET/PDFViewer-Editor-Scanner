package com.smartpdfsuite.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpdfsuite.R;

import com.smartpdfsuite.models.PdfDocument;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

    public class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.PdfViewHolder> {

    private List<PdfDocument> pdfList;
    private OnPdfClickListener listener;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public PdfListAdapter(List<PdfDocument> pdfList, OnPdfClickListener listener) {
        this.pdfList = pdfList;
        this.listener = listener;
    }

    public void updatePdfList(List<PdfDocument> newPdfList) {
        this.pdfList.clear();
        this.pdfList.addAll(newPdfList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_document, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfDocument pdf = pdfList.get(position);
        holder.pdfName.setText(pdf.getName());
        holder.pdfSize.setText(formatFileSize(pdf.getSize()));
        holder.pdfDateModified.setText(dateFormat.format(pdf.getDateModified()));

        holder.itemView.setOnClickListener(v -> listener.onPdfClick(pdf));

        // ✅ SHARE BUTTON
        holder.shareBtn.setOnClickListener(v -> sharePdf(pdf));

        // ✅ THREE DOT MENU
        holder.moreBtn.setOnClickListener(v -> showPopupMenu(v, pdf));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onPdfLongClick(pdf);
            return true;
        });
    }

    private void sharePdf(PdfDocument pdf) {
        try {
            Uri uri = pdf.getUri();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopupMenu(View view, PdfDocument pdf) {

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_pdf_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_rename) {
                listener.onRenameClick(pdf);
                return true;

            } else if (id == R.id.menu_move) {
                listener.onMoveClick(pdf);
                return true;

            } else if (id == R.id.menu_delete) {
                listener.onDeleteClick(pdf);
                return true;
            }

            return false;
        });

        popup.show();
    }
        @Override
    public int getItemCount() {
        return pdfList.size();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView pdfName, pdfSize, pdfDateModified;
        ImageView shareBtn, moreBtn;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);
            pdfSize = itemView.findViewById(R.id.pdf_size);
            pdfDateModified = itemView.findViewById(R.id.pdf_date_modified);

            shareBtn = itemView.findViewById(R.id.btn_share);
            moreBtn = itemView.findViewById(R.id.btn_more);
        }
    }

    public interface OnPdfClickListener {
        void onPdfClick(PdfDocument pdfDocument);
        void onPdfLongClick(PdfDocument pdfDocument);
        void onRenameClick(PdfDocument pdfDocument);
        void onMoveClick(PdfDocument pdfDocument);
        void onDeleteClick(PdfDocument pdfDocument);
    }
}
package com.smartpdfsuite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.itemView.setOnLongClickListener(v -> {
            listener.onPdfLongClick(pdf);
            return true;
        });
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

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.pdf_name);
            pdfSize = itemView.findViewById(R.id.pdf_size);
            pdfDateModified = itemView.findViewById(R.id.pdf_date_modified);
        }
    }

    public interface OnPdfClickListener {
        void onPdfClick(PdfDocument pdfDocument);
        void onPdfLongClick(PdfDocument pdfDocument);
    }
}
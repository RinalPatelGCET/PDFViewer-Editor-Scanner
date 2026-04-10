
package com.smartpdfsuite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpdfsuite.R;
import com.smartpdfsuite.models.FilterModel;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    public interface FilterClickListener {
        void onFilterClick(String filterName);
    }

    private List<FilterModel> filterList;
    private FilterClickListener listener;

    public FilterAdapter(List<FilterModel> filterList, FilterClickListener listener) {
        this.filterList = filterList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FilterModel model = filterList.get(position);

        holder.filterName.setText(model.getName());
        holder.filterImage.setImageBitmap(model.getPreview());

        holder.itemView.setOnClickListener(v ->
                listener.onFilterClick(model.getName())
        );
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView filterImage;
        TextView filterName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            filterImage = itemView.findViewById(R.id.filter_thumbnail);
            filterName = itemView.findViewById(R.id.filter_name);
        }
    }
}
/*
package com.smartpdfsuite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpdfsuite.R;
import com.smartpdfsuite.models.FilterModel;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder>{


    private List<FilterModel> filterList;
    private OnFilterClick listener;

    public interface OnFilterClick{
        void onClick(String filterName);
    }

    public FilterAdapter(List<FilterModel> list, OnFilterClick listener){
        this.filterList = list;
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        public ViewHolder(View itemView){
            super(itemView);

            name = itemView.findViewById(R.id.filter_name);

            itemView.setOnClickListener(v -> {
                listener.onClick(filterList.get(getAdapterPosition()).getName());
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(filterList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }
}
*/

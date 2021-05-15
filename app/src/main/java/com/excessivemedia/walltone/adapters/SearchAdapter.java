package com.excessivemedia.walltone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchVH> {
    private final ArrayList<String> searchResults;
    private OnSearchClickListener listener;
    public SearchAdapter(ArrayList<String> searchResults) {
        this.searchResults = searchResults;
    }

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        this.listener = listener;
    }

    public interface OnSearchClickListener{
        void onSearchResultClickListener(String searchText);
    }

    @NonNull
    @Override
    public SearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchVH(
                LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.card_search,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchVH holder, final int position) {
        holder.tv.setText(searchResults.get(position));
        holder.itemView.setOnClickListener(v->{
            if(listener!=null){
                listener.onSearchResultClickListener(searchResults.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    static class SearchVH extends RecyclerView.ViewHolder{
        TextView tv;
        public SearchVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.searchTV);
        }
    }
}

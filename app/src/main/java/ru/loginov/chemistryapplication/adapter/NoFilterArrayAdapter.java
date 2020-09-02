package ru.loginov.chemistryapplication.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.List;

public class NoFilterArrayAdapter<T> extends ArrayAdapter<T> {

    private final Filter filter;

    public NoFilterArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
        filter = new NoFilter();
    }



    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class NoFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
        }
    }
}

package com.cyrilmottier.android.queryhighlight;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import com.cyrilmottier.android.queryhighlight.text.Normalizer;
import com.cyrilmottier.android.queryhighlight.text.TextWatcherAdapter;
import com.cyrilmottier.android.queryhighlight.text.format.QueryHighlighter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MainActivity extends AppCompatActivity {

    private CheesesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new CheesesAdapter();

        final ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.search_view);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        final EditText editText = (EditText) actionBar.getCustomView();
        editText.addTextChangedListener(mTextWatcher);
        editText.requestFocus();
    }

    public final class CheesesAdapter extends BaseAdapter implements Filterable {

        private final QueryHighlighter mQueryHighlighter;
        private final Filter mFilter;

        private List<String> mCheeses;
        private String mQuery;

        public CheesesAdapter() {
            mCheeses = Cheeses.ALL;
            mFilter = new WordFilter(mCheeses);
            mQueryHighlighter = new QueryHighlighter().
                    setQueryNormalizer(QueryHighlighter.QueryNormalizer.FOR_SEARCH);
        }

        @Override
        public int getCount() {
            return mCheeses.size();
        }

        @Override
        public String getItem(int position) {
            return mCheeses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_text, parent, false);
            }

            mQueryHighlighter.setText((TextView) convertView, getItem(position), mQuery);

            return convertView;
        }

        // NOTE Cyril
        //   - ArrayAdapter has built-in filtering by default but normalization
        //     is only case-based.
        //   - CursorAdapter has built-int filtering too. However, try to favor
        //     restarting a query (with a Loader for instance) instead.
        //   - When data comes from the network, it's up to you to restart the
        //     query.
        @Override
        public Filter getFilter() {
            return mFilter;
        }

        public void setQuery(String query) {
            if (!Objects.equals(query, mQuery)) {
                getFilter().filter(query);
            }
        }

        private final class WordFilter extends Filter {

            private final List<String> mItems;

            private WordFilter(List<String> items) {
                mItems = new ArrayList<>(items);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final List<String> list;

                if (TextUtils.isEmpty(constraint)) {
                    list = mItems;
                } else {
                    list = new ArrayList<>();
                    final String normalizedConstraint = Normalizer.forSearch(constraint);
                    for (String item : mItems) {
                        final String normalizedItem = Normalizer.forSearch(item);
                        if (normalizedItem.startsWith(normalizedConstraint) || //
                                normalizedItem.contains(" " + normalizedConstraint)) {
                            list.add(item);
                        }
                    }
                }

                final FilterResults results = new FilterResults();
                results.values = list;
                results.count = list.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mCheeses = (List<String>) results.values;
                mQuery = constraint != null ? constraint.toString() : null;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private final TextWatcher mTextWatcher = new TextWatcherAdapter() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.setQuery(s != null ? s.toString() : null);
        }
    };

}

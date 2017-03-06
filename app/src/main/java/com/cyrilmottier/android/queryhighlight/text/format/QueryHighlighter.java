package com.cyrilmottier.android.queryhighlight.text.format;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.widget.TextView;
import com.cyrilmottier.android.queryhighlight.text.Normalizer;

import java.util.Locale;
import java.util.Objects;

public final class QueryHighlighter {

    public enum Mode {
        CHARACTERS, WORDS
    }

    public static abstract class QueryNormalizer {

        public static final QueryNormalizer FOR_SEARCH = new QueryNormalizer() {
            @Override
            public CharSequence normalize(CharSequence source) {
                return Normalizer.forSearch(source);
            }
        };

        public static final QueryNormalizer CASE = new QueryNormalizer() {
            @Override
            public CharSequence normalize(CharSequence source) {
                if (TextUtils.isEmpty(source)) {
                    return source;
                }
                return source.toString().toUpperCase(Locale.ROOT);
            }
        };

        public static final QueryNormalizer NONE = new QueryNormalizer() {
            @Override
            public CharSequence normalize(CharSequence source) {
                return source;
            }
        };

        public abstract CharSequence normalize(CharSequence source);
    }

    private CharacterStyle mHighlightStyle = new StyleSpan(Typeface.BOLD);
    private QueryNormalizer mQueryNormalizer = QueryNormalizer.NONE;
    private Mode mMode = Mode.WORDS;

    public QueryHighlighter setHighlightStyle(CharacterStyle highlightStyle) {
        mHighlightStyle = Objects.requireNonNull(highlightStyle, "highlightStyle cannot be null");
        return this;
    }

    public QueryHighlighter setQueryNormalizer(QueryNormalizer queryNormalizer) {
        mQueryNormalizer = Objects.requireNonNull(queryNormalizer, "queryNormalizer cannot be null");
        return this;
    }

    public QueryHighlighter setMode(Mode mode) {
        mMode = Objects.requireNonNull(mode, "mode cannot be null");
        return this;
    }

    public CharSequence apply(CharSequence text, CharSequence wordPrefix) {
        final CharSequence normalizedText = mQueryNormalizer.normalize(text);
        final CharSequence normalizedWordPrefix = mQueryNormalizer.normalize(wordPrefix);

        final int index = indexOfQuery(normalizedText, normalizedWordPrefix);
        if (index != -1) {
            final SpannableString result = new SpannableString(text);
            result.setSpan(mHighlightStyle, index, index + normalizedWordPrefix.length(), 0);
            return result;
        } else {
            return text;
        }
    }

    public void setText(TextView view, CharSequence text, CharSequence query) {
        view.setText(apply(text, query));
    }

    private int indexOfQuery(CharSequence text, CharSequence query) {
        if (query == null || text == null) {
            return -1;
        }

        final int textLength = text.length();
        final int queryLength = query.length();

        if (queryLength == 0 || textLength < queryLength) {
            return -1;
        }

        for (int i = 0; i <= textLength - queryLength; i++) {
            // Only match word prefixes
            if (mMode == Mode.WORDS && i > 0 && text.charAt(i - 1) != ' ') {
                continue;
            }

            int j;
            for (j = 0; j < queryLength; j++) {
                if (text.charAt(i + j) != query.charAt(j)) {
                    break;
                }
            }
            if (j == queryLength) {
                return i;
            }
        }

        return -1;
    }

}

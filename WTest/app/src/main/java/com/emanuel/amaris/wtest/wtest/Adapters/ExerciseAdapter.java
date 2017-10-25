package com.emanuel.amaris.wtest.wtest.Adapters;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.emanuel.amaris.wtest.wtest.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emanuel on 25-10-2017.
 */

public class ExerciseAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_TEXT = 1;
    public static final int VIEW_TYPE_LOADING = 2;
    public static final int VIEW_TYPE_NO_DATA = 3;
    public static final int VIEW_TYPE_PLACEHOLDER = 4;

    private Context recyclerContext;

    private List<itemTemplate> adapterContent;

    private boolean isLoadingData;
    private String loadingString;

    public ExerciseAdapter(Context context) {
        this.recyclerContext = context;
        this.adapterContent = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                View loadingView = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_loading, parent, false);
                return new LoadingViewHolder(loadingView);
            case VIEW_TYPE_NO_DATA:
            case VIEW_TYPE_TEXT:
            default:
                View layoutView = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_layout, parent, false);
                return new ExerciseViewHolder(layoutView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (adapterContent.size() == 0 && !isLoadingData) {
            ExerciseViewHolder exerciseHolder = (ExerciseViewHolder) holder;

            exerciseHolder.itemText.setText(recyclerContext.getString(R.string.no_data_loaded));

            exerciseHolder.itemEdit.setVisibility(TextInputEditText.GONE);
            exerciseHolder.itemEditListener.updateItem(null);
            return;
        } else if (isLoadingData) {
            LoadingViewHolder loadingHolder = (LoadingViewHolder) holder;
            if (loadingString != null && !loadingString.isEmpty()) {
                loadingHolder.progressText.setText(loadingString);
                loadingHolder.progressText.setVisibility(TextView.VISIBLE);
            } else {
                loadingHolder.progressText.setVisibility(TextView.GONE);
            }
            return;
        }

        switch (adapterContent.get(position).getViewType()) {
            case VIEW_TYPE_TEXT:
                ExerciseViewHolder exerciseHolder = (ExerciseViewHolder) holder;

                exerciseHolder.itemText.setText(adapterContent.get(position).getContentToShow());
                if (adapterContent.get(position).shouldShowEditText()) {
                    switch (adapterContent.get(position).getContentType()) {
                        case itemTemplate.CONTENT_NORMAL:
                            //Only Accept Text
                            exerciseHolder.itemEdit.setFilters(new InputFilter[]{new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                    String filtered = "";
                                    for (int i = start; i < end; i++) {
                                        char character = source.charAt(i);
                                        if (!Character.isWhitespace(character) && Character.isLetter(character)) {
                                            filtered += character;
                                        }
                                    }
                                    return filtered;
                                }
                            }});
                            exerciseHolder.itemEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);
                            break;
                        case itemTemplate.CONTENT_NUMERIC:
                            exerciseHolder.itemEdit.setFilters(new InputFilter[]{});
                            exerciseHolder.itemEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
                            break;
                        case itemTemplate.CONTENT_ALL_CAPS:
                            //Use Android native ALL CAPS filter and use a new InputFilter to only accept Text
                            exerciseHolder.itemEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter() {
                                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                    String filtered = "";
                                    for (int i = start; i < end; i++) {
                                        char character = source.charAt(i);
                                        if (!Character.isWhitespace(character) && Character.isLetter(character)) {
                                            filtered += character;
                                        }
                                    }
                                    return filtered;
                                }
                            }});

                            exerciseHolder.itemEdit.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_CLASS_TEXT);
                            break;
                    }
                    exerciseHolder.itemEditListener.updateItem(adapterContent.get(position));
                    exerciseHolder.itemEdit.setText(adapterContent.get(position).getContentBeingEdited());
                    exerciseHolder.itemEditLayout.setVisibility(TextInputLayout.VISIBLE);
                } else {
                    exerciseHolder.itemEditLayout.setVisibility(TextInputLayout.GONE);
                    exerciseHolder.itemEditListener.updateItem(null);
                }
                break;
            case VIEW_TYPE_LOADING:
                LoadingViewHolder loadingHolder = (LoadingViewHolder) holder;
                loadingHolder.progressText.setVisibility(TextView.VISIBLE);
                loadingHolder.progressText.setText(R.string.pls_wait_data_loading);
                //Do nothing
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingData) {
            return VIEW_TYPE_LOADING;
        }

        if (adapterContent.size() == 0) {
            return VIEW_TYPE_NO_DATA;
        }

        return adapterContent.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        if (isLoadingData) {
            return 1;
        }

        if (adapterContent.size() == 0) {
            return 1;
        }

        return adapterContent.size();
    }

    public void setLoading(boolean loading) {
        this.isLoadingData = loading;
        notifyDataSetChanged();
    }

    public void setLoadingString(String loading) {
        this.loadingString = loading;
        notifyDataSetChanged();
    }

    public boolean isLoading() {
        return this.isLoadingData;
    }

    public List<itemTemplate> getAdapterContent() {
        return adapterContent;
    }

    public void setAdapterContent(List<itemTemplate> adapterContent, boolean resetScroll) {
        this.adapterContent = adapterContent;
        if (resetScroll)
            notifyDataSetChanged();
        else
            notifyItemRangeChanged(0, this.adapterContent.size());
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        TextView itemText;
        EditText itemEdit;
        TextInputLayout itemEditLayout;

        ItemEditTextChangedListener itemEditListener;


        public ExerciseViewHolder(View itemView) {
            super(itemView);

            if (itemView != null) {
                itemText = itemView.findViewById(R.id.item_text);
                itemEdit = itemView.findViewById(R.id.item_edit);
                itemEditLayout = itemView.findViewById(R.id.item_edit_layout);

                itemEditListener = new ItemEditTextChangedListener();
                itemEdit.addTextChangedListener(itemEditListener);
            }
        }

        public class ItemEditTextChangedListener implements TextWatcher {

            itemTemplate item;

            public void updateItem(itemTemplate item) {
                this.item = item;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (item != null)
                    item.setContentBeingEdited((editable.toString()));
            }
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        TextView progressText;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressText = itemView.findViewById(R.id.progress_text);
        }
    }

    public static class itemTemplate {

        public final static int CONTENT_NORMAL = 0;
        public final static int CONTENT_NUMERIC = 1;
        public final static int CONTENT_ALL_CAPS = 2;

        private String contentToShow;
        private String contentBeingEdited;
        private int contentType = -1;
        private boolean shouldShowEditText = false;
        private int viewType;

        public boolean shouldShowEditText() {
            return shouldShowEditText;
        }

        public void setShowEditText(boolean shouldShowEditText) {
            this.shouldShowEditText = shouldShowEditText;
        }

        public String getContentToShow() {
            return contentToShow;
        }

        public void setContentToShow(String contentToShow) {
            this.contentToShow = contentToShow;
        }

        public String getContentBeingEdited() {
            return contentBeingEdited;
        }

        public void setContentBeingEdited(String contentBeingEdited) {
            this.contentBeingEdited = contentBeingEdited;
        }

        public int getContentType() {
            return contentType;
        }

        public void setContentType(int contentType) {
            this.contentType = contentType;
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }
    }
}

package com.emanuel.amaris.wtest.wtest.Adapters;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.emanuel.amaris.wtest.wtest.R;

import java.util.List;

/**
 * Created by emanuel on 25-10-2017.
 */

public class ExerciseAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_DIVISOR = 1;
    public static final int VIEW_TYPE_TEXT = 2;

    private int itemCount = 0;
    private Context recyclerContext;

    private List<itemTemplate> adapterContent;

    public ExerciseAdapter(Context context) {
        this.recyclerContext = context;
    }

    public ExerciseAdapter(Context context, List<itemTemplate> content) {
        this.recyclerContext = context;
        this.adapterContent = content;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DIVISOR:
                View divisorView = View.inflate(recyclerContext, R.layout.list_item_divisor, parent);
                return new DivisorViewHolder(divisorView);
            case VIEW_TYPE_TEXT:
            default:
                View layoutView = View.inflate(recyclerContext, R.layout.list_item_layout, parent);
                return new ExerciseViewHolder(layoutView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (adapterContent.get(position).getViewType() == VIEW_TYPE_DIVISOR) {
            DivisorViewHolder divisorHolder = (DivisorViewHolder) holder;

            divisorHolder.divisorText.setText(adapterContent.get(position).getDivisorTitle());
        } else {
            ExerciseViewHolder exerciseHolder = (ExerciseViewHolder) holder;

            exerciseHolder.itemText.setText(adapterContent.get(position).getContentToShow());
            if (adapterContent.get(position).shouldShowEditText()) {
                exerciseHolder.itemEditListener.updateItem(adapterContent.get(position));
                exerciseHolder.itemEdit.setText(adapterContent.get(position).getContentBeingEdited());
                exerciseHolder.itemEdit.setVisibility(TextInputEditText.VISIBLE);
            } else {
                exerciseHolder.itemEdit.setVisibility(TextInputEditText.GONE);
                exerciseHolder.itemEditListener.updateItem(null);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return adapterContent.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return adapterContent.size();
    }

    public List<itemTemplate> getAdapterContent() {
        return adapterContent;
    }

    public void setAdapterContent(List<itemTemplate> adapterContent) {
        this.adapterContent = adapterContent;
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        TextView itemText;
        EditText itemEdit;

        ItemEditTextChangedListener itemEditListener;


        public ExerciseViewHolder(View itemView) {
            super(itemView);

            if (itemView != null) {
                itemText = itemView.findViewById(R.id.item_text);
                itemEdit = itemView.findViewById(R.id.item_edit);

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

    public class DivisorViewHolder extends RecyclerView.ViewHolder {

        TextView divisorText;

        public DivisorViewHolder(View itemView) {
            super(itemView);

            if (itemView != null) {
                divisorText = itemView.findViewById(R.id.text_divisor);
                divisorText.setAllCaps(true);
            }
        }
    }

    public class itemTemplate {

        private String divisorTitle;
        private String contentToShow;
        private String contentBeingEdited;
        private boolean shouldShowEditText = false;
        private int viewType;

        public boolean shouldShowEditText() {
            return shouldShowEditText;
        }

        public void setShowEditText(boolean shouldShowEditText) {
            this.shouldShowEditText = shouldShowEditText;
        }

        public String getDivisorTitle() {
            return divisorTitle;
        }

        public void setDivisorTitle(String divisorTitle) {
            this.divisorTitle = divisorTitle;
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

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }
    }
}

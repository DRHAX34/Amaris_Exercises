package com.emanuel.amaris.wtest.wtest.Adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emanuel.amaris.wtest.wtest.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emanuel on 25-10-2017.
 */

/**
 * An all-purpose ExerciseAdapter that will fulfill the need for every exercise
 * Since I'm a fan of non-repeated code, and I believe it is a good practice to generalise things, I have made this unique class than is very adaptable
 */
public class ExerciseAdapter extends RecyclerView.Adapter {

    //Constants so we can determine which view type should be shown
    public static final int VIEW_TYPE_TEXT = 1;
    public static final int VIEW_TYPE_LOADING = 2;
    public static final int VIEW_TYPE_LOADING2 = 3;
    public static final int VIEW_TYPE_NO_DATA = 4;
    public static final int VIEW_TYPE_PLACEHOLDER = 5;

    //The context needed to inflate Views or get other display metrics
    private Context recyclerContext;

    //Our Adapter content and further in the class file, it's getter and setter methods
    private List<itemTemplate> adapterContent;

    //Status variables so we can control the adapter state
    private boolean isLoadingData;
    private String loadingString;

    //Adapter Constructor
    public ExerciseAdapter(Context context) {
        this.recyclerContext = context;
        this.adapterContent = new ArrayList<>();
    }

    //In here, we check which viewtype is being requested and inflate the respective layout.
    //We pass it to a ViewHolder so the RecyclerView can recycle the inflated layouts as it wishes.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                View loadingView = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_loading, parent, false);
                return new LoadingViewHolder(loadingView);
            case VIEW_TYPE_LOADING2:
                View loadingView2 = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_loading2, parent, false);
                return new LoadingViewHolder2(loadingView2);
            case VIEW_TYPE_PLACEHOLDER:
                View placeHolderView = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_placeholder, parent, false);
                return new PlaceHolderViewHolder(placeHolderView);
            case VIEW_TYPE_NO_DATA:
            case VIEW_TYPE_TEXT:
            default:
                View layoutView = LayoutInflater.from(recyclerContext).inflate(R.layout.list_item_layout, parent, false);
                return new ExerciseViewHolder(layoutView);
        }
    }


    //In here, we bind all values that need to be binded to each respectful view type
    //Since we are dealing with multiple view types and we have a loading state, the is also this if at the beggining that deals if items that are not
    //In the adapter view contents
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (adapterContent.size() == 0 && !isLoadingData) {
            ExerciseViewHolder exerciseHolder = (ExerciseViewHolder) holder;

            exerciseHolder.itemText.setText(recyclerContext.getString(R.string.no_data_loaded));

            exerciseHolder.itemEdit.setVisibility(TextInputEditText.GONE);
            exerciseHolder.itemEditListener.updateItem(null);
            return;
        } else if (isLoadingData) {
            if (position == 1) {
                LoadingViewHolder2 loadingHolder = (LoadingViewHolder2) holder;
                if (loadingString != null && !loadingString.isEmpty()) {
                    loadingHolder.progressText.setText(loadingString);
                    loadingHolder.progressText.setVisibility(TextView.VISIBLE);
                } else {
                    loadingHolder.progressText.setVisibility(TextView.GONE);
                }
            } else if (position == 0) {
                LoadingViewHolder loadingHolder = (LoadingViewHolder) holder;
                loadingHolder.progressText.setVisibility(TextView.GONE);
            }
            return;
        }

        //Check each view type and deliver the data accordingly
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
        }
    }

    //Method executed when the Recycler View need to know what the view type is
    //It will use this data either to Create a view holder or get an existing one
    @Override
    public int getItemViewType(int position) {
        if (isLoadingData) {
            if (position == 0)
                return VIEW_TYPE_LOADING;
            else
                return VIEW_TYPE_LOADING2;
        }

        if (adapterContent.size() == 0) {
            return VIEW_TYPE_NO_DATA;
        }

        return adapterContent.get(position).getViewType();
    }

    //Get the content item count, simple
    @Override
    public int getItemCount() {
        if (isLoadingData) {
            return 2;
        }

        if (adapterContent.size() == 0) {
            return 1;
        }

        return adapterContent.size();
    }

    //Set the adapter to a loading data state
    public void setLoading(boolean loading) {
        this.isLoadingData = loading;
        notifyDataSetChanged();
    }

    //Personalize the loading string as we wish
    public void setLoadingString(String loading) {
        this.loadingString = loading;
        notifyItemChanged(1);
    }

    //Return if the adapter is in a loading state
    public boolean isLoading() {
        return this.isLoadingData;
    }

    //Return the adapter content
    public List<itemTemplate> getAdapterContent() {
        return adapterContent;
    }

    //Set tje adapter content
    public void setAdapterContent(List<itemTemplate> adapterContent, boolean resetScroll) {
        int previousAdapterCount = this.adapterContent.size();

        if (previousAdapterCount != 0) {
            previousAdapterCount--;
        }

        this.adapterContent = adapterContent;

        //Since it's in our interest not to always reset the scroll position, just notify the item range changed
        if (resetScroll)
            notifyDataSetChanged();
        else
            notifyItemRangeChanged(previousAdapterCount, this.adapterContent.size());
    }

    //View Holder for our Exercise Adapter, this will fetch all the views from the inflated layout and keep it so the recyclerview can recycle the views.
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

        //Implement this listener so the edittext keeps the data entered onto it
        //Wouldn't want to lose the data of course
        public class ItemEditTextChangedListener implements TextWatcher {

            itemTemplate item;

            //This updates the item this listener belongs to.
            public void updateItem(itemTemplate item) {
                this.item = item;
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Not needed, but as with any other interface, it has to be implemented
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Not needed, but as with any other interface, it has to be implemented
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (item != null)
                    item.setContentBeingEdited((editable.toString()));
            }
        }
    }

    //Simple view holder for the loading item
    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        TextView progressText;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressText = itemView.findViewById(R.id.progress_text);
        }
    }

    //Simple view holder for the loading item
    public class LoadingViewHolder2 extends RecyclerView.ViewHolder {

        TextView progressText;

        public LoadingViewHolder2(View itemView) {
            super(itemView);

            progressText = itemView.findViewById(R.id.progress_text);
        }
    }

    //Simple viewholder for the placeholder on exercise 2
    public class PlaceHolderViewHolder extends RecyclerView.ViewHolder {

        LinearLayout stub;

        public PlaceHolderViewHolder(View itemView) {
            super(itemView);

            //Set the place holder to the same size as the image view in exercise 2
            WindowManager wm = (WindowManager) recyclerContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;

            stub = itemView.findViewById(R.id.imageStub);
            ViewGroup.LayoutParams params = stub.getLayoutParams();

            //Ratio 2:1 means width 300, height 150, which means that the image has to be half of the screen height
            params.height = height / 2;
            stub.setLayoutParams(params);

        }
    }

    //Item Template so we can show our data with this adapter
    public static class itemTemplate {

        //Necessary constants for exercise 3
        public final static int CONTENT_NORMAL = 0;
        public final static int CONTENT_NUMERIC = 1;
        public final static int CONTENT_ALL_CAPS = 2;


        //All properties needed so we can show what we want in the adapter
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

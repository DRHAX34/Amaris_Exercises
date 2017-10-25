package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.emanuel.amaris.wtest.wtest.Adapters.ExerciseAdapter;
import com.emanuel.amaris.wtest.wtest.R;
import com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper.WTestDbContract;
import com.emanuel.amaris.wtest.wtest.WebCode.PostalCodeManager;

import java.util.ArrayList;
import java.util.List;

//TODO: Add RecyclerView to layout and add items to RecyclerView Adapter

public class FragmentExercise1 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise1Tag";
    private RecyclerView fragmentRecyclerView;
    private ExerciseAdapter recyclerAdapter;

    private EditText searchText;

    //Little extra for pull to refresh
    private boolean dataWasForced;
    private boolean dataIsLoading;
    private PostalCodeManager manager;

    public FragmentExercise1() {
        //Default Empty Constructor
    }

    public static FragmentExercise1 newInstance() {
        FragmentExercise1 fragment = new FragmentExercise1();
        fragment.setFragmentLayout(R.layout.fragment_exercises_exercise1);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewAvailable(Bundle savedInstanceState) {
        fragmentRecyclerView = getFragmentView().findViewById(R.id.recyclerView);
        searchText = getFragmentView().findViewById(R.id.search_box);
        if (fragmentRecyclerView != null) {
            fragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            fragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerAdapter = new ExerciseAdapter(getContext());
            fragmentRecyclerView.setAdapter(recyclerAdapter);

            manager = PostalCodeManager.getInstance(getContext(), new PostalCodeManager.PostalCodeEventListener() {
                @Override
                public void onDataLoaded(final List<WTestDbContract.DbItem> postalCodes) {
                    ArrayList<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();
                    List<ExerciseAdapter.itemTemplate> adapterContent = recyclerAdapter.getAdapterContent();

                    for (int i = 0; i < postalCodes.size(); i++) {
                        WTestDbContract.DbItem item = postalCodes.get(i);
                        ExerciseAdapter.itemTemplate newItemToShow = new ExerciseAdapter.itemTemplate();
                        newItemToShow.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                        newItemToShow.setContentToShow(item.toString());

                        itemsToShow.add(newItemToShow);
                    }

                    if (postalCodes.size() >= 15) {
                        ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();

                        item.setViewType(ExerciseAdapter.VIEW_TYPE_LOADING);

                        itemsToShow.add(item);
                    }


                    if (dataWasForced) {
                        recyclerAdapter.setAdapterContent(itemsToShow, true);
                        recyclerAdapter.setLoading(false);
                        dataWasForced = false;
                    } else {
                        if (adapterContent.size() != 0 && adapterContent.get(adapterContent.size() - 1).getViewType() == ExerciseAdapter.VIEW_TYPE_LOADING) {
                            adapterContent.remove(adapterContent.size() - 1);
                        }
                        adapterContent.addAll(itemsToShow);
                        if (recyclerAdapter.isLoading()) {
                            recyclerAdapter.setLoading(false);
                        }
                        recyclerAdapter.setAdapterContent(adapterContent, false);
                    }
                    dataIsLoading = false;
                    searchText.setEnabled(true);
                }

                @Override
                public void onPreLoadData(boolean isCaching) {
                    if (!dataIsLoading) {
                        recyclerAdapter.setLoading(true);
                    }
                    if (isCaching) {
                        recyclerAdapter.setLoadingString(getContext().getString(R.string.pls_wait_data_caching));
                    } else {
                        recyclerAdapter.setLoadingString(getContext().getString(R.string.pls_wait_data_loading));
                    }
                }

                @Override
                public void onExceptionData() {
                    ArrayList<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();

                    ExerciseAdapter.itemTemplate newItemToShow = new ExerciseAdapter.itemTemplate();
                    newItemToShow.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                    newItemToShow.setContentToShow(getContext().getString(R.string.error_loading_data));
                    newItemToShow.setShowEditText(false);

                    itemsToShow.add(newItemToShow);

                    recyclerAdapter.setAdapterContent(itemsToShow, true);
                    recyclerAdapter.setLoading(false);
                    dataIsLoading = false;
                }
            });

            manager.loadPostalCodes(false, 0);

            fragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (!recyclerView.canScrollVertically(1) && (!recyclerAdapter.isLoading() && !dataIsLoading)) {
                        dataIsLoading = true;

                        manager.loadPostalCodes(false, recyclerAdapter.getAdapterContent().size());
                    }
                }
            });

            if (manager.getFilter() != null && !manager.getFilter().getValue().isEmpty()) {
                searchText.setText(manager.getFilter().getValue());
            }

            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //No need for this method, but Android make us implement it
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //No need for this method, but Android make us implement it
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    manager.setFilter(new WTestDbContract.Filter(editable.toString()));
                    dataWasForced = true;
                    dataIsLoading = false;
                    manager.loadPostalCodes(false, 0);
                }
            });
        }
    }
}

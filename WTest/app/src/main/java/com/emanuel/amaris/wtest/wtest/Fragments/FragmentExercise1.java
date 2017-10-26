package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
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

/**
 * Welcome to the fragment for exercise 1, this fragment is here with the purpose of demonstrating my skills in Android Development.
 */
public class FragmentExercise1 extends AppFragment {

    //Constant necessary to be able to identify this fragment
    public static final String FRAGMENT_TAG = "Exercise1Tag";

    //View required for this fragment/exercise
    private RecyclerView fragmentRecyclerView;
    private ExerciseAdapter recyclerAdapter;
    private EditText searchText;

    //This boolean distinguishes if data is being fetched for the first time or not
    private boolean dataWasForced;

    //This boolean tells the code that data is being loaded into adapter
    private boolean dataIsLoading;

    //This manages all the data from the web repository. A more proficcient way to be able to add more values is to use a service, so we are able to get a notification
    //telling the user the progress of the caching (There are about 32000 entries in the .csv file we're getting)
    private PostalCodeManager manager;

    public FragmentExercise1() {
        //Default Empty Constructor
    }

    //This is needed to instanciate the fragment, it follows Google's guidelines for Fragment instantiation
    public static FragmentExercise1 newInstance() {
        FragmentExercise1 fragment = new FragmentExercise1();

        //This is needed to tell AppFragment which view to load
        fragment.setFragmentLayout(R.layout.fragment_exercises_exercise1);

        return fragment;
    }

    //Method to be able to access the fragment views without repeating the same boilerplate code all over again
    //Saves time and makes a better code organization
    @Override
    public void onViewAvailable(Bundle savedInstanceState) {

        //We fetch all our views from the inflated layout
        fragmentRecyclerView = getFragmentView().findViewById(R.id.recyclerView);
        searchText = getFragmentView().findViewById(R.id.search_box);

        //On Rotation, the views might be null, so we check them and prevent exceptions from happening.
        //After rotation the views are fine
        if (fragmentRecyclerView != null) {

            //Set the default LayoutManager and ItemAnimator for the RecyclerView
            fragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            fragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());

            //Here we instantiate our ExerciseAdapter. The general-purpose adapter built specifically for this app. More info on it's class file
            recyclerAdapter = new ExerciseAdapter(getContext());

            //Setting the adapter to the recyclerview...
            fragmentRecyclerView.setAdapter(recyclerAdapter);

            //Here we instantiate or get the running instance of our PostalCodeManager, this is to prevent the AsyncTasks and data contained from being either repeated
            //Or lost. The AsyncTasks are needed to obtain all our data either from web or db
            //It'll take a long time for the data to be cached
            manager = PostalCodeManager.getInstance(getContext(), new PostalCodeManager.PostalCodeEventListener() {

                //This method in this listener is executed as soon as the PostalCodeManager gets it's data
                //Since we are caching from a 32000 line file, when this is caching with the web, it may take a while
                @Override
                public void onDataLoaded(final List<WTestDbContract.DbItem> postalCodes) {

                    //We initialize the list we'll be passing to the recyclerView adapter
                    //so we can start filling it up
                    ArrayList<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();

                    //Here we get what's already in the adapter, as we may just be adding more data to it
                    //The data is lazily fetched, which means, from the db we only fetch 15 lines at a time
                    //So the recyclerview won't take long to populate
                    List<ExerciseAdapter.itemTemplate> adapterContent = recyclerAdapter.getAdapterContent();

                    //Go through each postalCode and add it to the arrayList for the adapter
                    for (int i = 0; i < postalCodes.size(); i++) {

                        //Get the data from the database item and convert it to the object to be shown in the recycler view
                        //This piece of code is pretty self-explanatory
                        WTestDbContract.DbItem item = postalCodes.get(i);
                        ExerciseAdapter.itemTemplate newItemToShow = new ExerciseAdapter.itemTemplate();
                        newItemToShow.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                        newItemToShow.setContentToShow(item.toString());

                        itemsToShow.add(newItemToShow);
                    }

                    //Check if there will be more data coming from the Postal Code Manager
                    //If we're getting 15 items, more data will come, so there's a need to show a loading icon
                    if (postalCodes.size() >= 15) {
                        ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();

                        item.setViewType(ExerciseAdapter.VIEW_TYPE_LOADING);

                        itemsToShow.add(item);
                    }

                    //If the data is being fetched for the first time, we don't need to maintain scroll state, just set the list to the adapter
                    //and hide the loading screen
                    if (dataWasForced) {
                        recyclerAdapter.setAdapterContent(itemsToShow, true);
                        recyclerAdapter.setLoading(false);
                        dataWasForced = false;
                    } else {

                        // If the data is being added to the adapter, we need to maintain scroll state so the user doesn't get jerked up to the start
                        // Remove the icon for loading and add the items
                        if (adapterContent.size() != 0 && adapterContent.get(adapterContent.size() - 1).getViewType() == ExerciseAdapter.VIEW_TYPE_LOADING) {
                            adapterContent.remove(adapterContent.size() - 1);
                        }

                        //Add the items to the existing adapter content
                        adapterContent.addAll(itemsToShow);

                        //Hide the loading screen
                        if (recyclerAdapter.isLoading()) {
                            recyclerAdapter.setLoading(false);
                        }

                        //Set the content + newly added items as the adapter content
                        recyclerAdapter.setAdapterContent(adapterContent, false);
                    }

                    //Inform the recyclerview listener that data is no longer loading
                    dataIsLoading = false;

                    //Enable search, in case we disabled it
                    searchText.setEnabled(true);
                }

                //This method is executed before the data is fetched from either the DB or the web
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

                //Uh oh, this method should not be executed, but there could by any exception, so let's just process it and show to the user
                @Override
                public void onExceptionData() {

                    //Since in this layout we don't have any separate TextView, because there's no need for it,
                    //Just add a stub to the adapter with the error
                    ArrayList<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();

                    ExerciseAdapter.itemTemplate newItemToShow = new ExerciseAdapter.itemTemplate();
                    newItemToShow.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                    newItemToShow.setContentToShow(getContext().getString(R.string.error_loading_data));
                    newItemToShow.setShowEditText(false);

                    itemsToShow.add(newItemToShow);

                    //Like before, set the adapter content...
                    recyclerAdapter.setAdapterContent(itemsToShow, true);
                    //... turn off the loading screen...
                    recyclerAdapter.setLoading(false);
                    //... and inform the listener that it can update data again
                    dataIsLoading = false;
                }
            });

            //Call the manager to load the Postal Codes to show on the RecyclerView
            manager.loadPostalCodes(false, 0);

            //This is needed in order to load more data as soon as the user arrived at the end of the recyclerview
            //We don't need to load all data at once, so we load dynamically 15 items each time
            fragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    //Check if we arrived at the end of the recycler view and if data is already loading,
                    //if not, load the data
                    if (!recyclerView.canScrollVertically(1) && (!recyclerAdapter.isLoading() && !dataIsLoading)) {
                        dataIsLoading = true;

                        manager.loadPostalCodes(false, recyclerAdapter.getAdapterContent().size());
                    }
                }
            });

            //Set the search text field in case of screen rotation or activity restart and filter was set before it happened
            if (manager.getFilter() != null && !manager.getFilter().getValue().isEmpty()) {
                searchText.setText(manager.getFilter().getValue());
            }

            //Add the TextWatcher to search on afterText is changed or user type his query
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
                    //Set the filter on the PostalCodeManager
                    manager.setFilter(new WTestDbContract.Filter(editable.toString()));
                    //Set that we are searching for fresh data from db
                    dataWasForced = true;
                    //Inform the listener that we are loading fresh data, so no need for it to load any
                    dataIsLoading = false;

                    //Just call this method with filter set and the items will be loaded according to the filter
                    manager.loadPostalCodes(false, 0);
                }
            });
        }
    }
}

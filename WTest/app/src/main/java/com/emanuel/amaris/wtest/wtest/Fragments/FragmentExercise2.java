package com.emanuel.amaris.wtest.wtest.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emanuel.amaris.wtest.wtest.Adapters.ExerciseAdapter;
import com.emanuel.amaris.wtest.wtest.MainActivity;
import com.emanuel.amaris.wtest.wtest.R;
import com.emanuel.amaris.wtest.wtest.WebCode.ImageManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Welcome to the fragment for exercise 2, this fragment is here with the purpose of demonstrating my skills in Android Development.
 * For me, this was the hardest one to make, and I couldn't complete it due to time shortage. I couldn't make the recyclerView scroll listener work in the way
 * it would need to in order to achieve the Toolbar transparency
 */
public class FragmentExercise2 extends AppFragment {


    //Constant necessary to be able to identify this fragment
    public static final String FRAGMENT_TAG = "Exercise2Tag";

    //View required for this fragment
    private ImageView headerImage;
    private RelativeLayout loadingLayout;
    private TextView loadingText;
    private RecyclerView fragmentRecyclerView;
    private ExerciseAdapter recyclerAdapter;
    private ImageManager manager;

    public FragmentExercise2() {
        //Required fragment empty constructor
    }

    //This is needed to instanciate the fragment, it follows Google's guidelines for Fragment instantiation
    public static FragmentExercise2 newInstance() {
        FragmentExercise2 fragment = new FragmentExercise2();
        fragment.setFragmentLayout(R.layout.fragment_fragment_exercise2);
        return fragment;
    }


    //Method to be able to access the fragment views without repeating the same boilerplate code all over again
    //Saves time and makes a better code organization
    @Override
    public void onViewAvailable(Bundle savedInstanceState) {
        //First, we fetch all the views from the inflated layout of the fragment
        fragmentRecyclerView = getFragmentView().findViewById(R.id.recyclerView);
        headerImage = getFragmentView().findViewById(R.id.imageHeader);
        loadingLayout = getFragmentView().findViewById(R.id.loadingLayout);
        loadingText = getFragmentView().findViewById(R.id.loadingMessage);

        //As always, like in the other fragments, we need to check for null views because of the way Android handles configuration changes
        if (headerImage != null) {

            //This code was needed in order for the headerImage view to have a height that corresponds to the requested aspect ratio, 2:1
            //Since 2:1 is exactly width:(height/2) and Android doesn't let me set the height/2 from the XML files, this code is needed
            ViewGroup.LayoutParams params = headerImage.getLayoutParams();

            //We get the WindowManager, so we can get the Display so we can get the display size and resize our view accordingly
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int height = size.y;

            //Set the new height...
            params.height = height / 2;
            //... and apply it to the headerImage
            headerImage.setLayoutParams(params);

            //Get our ImageManager so we can fetch the header image for our recyclerview
            manager = ImageManager.getInstance(getContext(), new ImageManager.ImageEventListener() {

                //This method in this listener is executed as soon as the ImageManager gets the image from the web
                //I'm caching the image in a temp file just for the sake of fastness
                @Override
                public void onDataLoaded(Bitmap postalCodes) {
                    loadingLayout.setVisibility(LinearLayout.GONE);
                    headerImage.setImageBitmap(postalCodes);
                    headerImage.setScaleType(ImageView.ScaleType.CENTER);
                }

                //Method executed before the ImageManager fetches the image
                @Override
                public void onPreLoadData() {
                    loadingLayout.setVisibility(LinearLayout.VISIBLE);
                    loadingText.setText(R.string.pls_wait_image_loading);
                }

                //Oops, I personally hope this never runs
                @Override
                public void onExceptionData() {
                    loadingText.setText(R.string.error_loading_image);
                }

            });

            //As explicit as it can be, tells the manager to load the image
            manager.loadImage();

            //Here we set the default stuff for the RecyclerView
            fragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            fragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
            //If I had a bit more time, I would enjoy tuning this stuff for a better UI Experience

            //Here we create our recyclerView adapter and initialize it with the 50 items asked
            recyclerAdapter = new ExerciseAdapter(getContext());
            List<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();
            if (itemsToShow.size() == 0) {

                //Create a placeholder to allow the image to be seen. This reaches the same effect as asked,
                //But if I had a bit more time, there would be a better approach to this
                ExerciseAdapter.itemTemplate placeholder = new ExerciseAdapter.itemTemplate();
                placeholder.setViewType(ExerciseAdapter.VIEW_TYPE_PLACEHOLDER);
                itemsToShow.add(placeholder);

                //Add the 50 items....
                for (int i = 0; i < 50; ++i) {
                    ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();
                    item.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                    item.setShowEditText(false);
                    item.setContentToShow("Item number: " + i);
                    itemsToShow.add(item);
                }
            }

            //... and set them to our Adapter
            fragmentRecyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.setAdapterContent(itemsToShow, false);

            fragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                private int firstVisibleInListview = 0;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int currentFirstVisible = ((LinearLayoutManager) fragmentRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (currentFirstVisible == firstVisibleInListview)
                        ((MainActivity) getContext()).changeToolbarVisibility(false);
                    else
                        ((MainActivity) getContext()).changeToolbarVisibility(true);

                }
            });

        }
    }
}

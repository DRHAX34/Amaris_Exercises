package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.emanuel.amaris.wtest.wtest.Adapters.ExerciseAdapter;
import com.emanuel.amaris.wtest.wtest.MainActivity;
import com.emanuel.amaris.wtest.wtest.R;
import com.emanuel.amaris.wtest.wtest.WTestApplication;

import java.util.ArrayList;

public class FragmentExercise3 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise3Tag";

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_NUMBERS = 1;
    public static final int TYPE_ALL_CAPS = 2;

    private RecyclerView fragmentRecyclerView;
    private ExerciseAdapter recyclerAdapter;

    WTestApplication app;

    public FragmentExercise3() {
        //Default Empty Constructor
    }

    public static FragmentExercise3 newInstance() {
        FragmentExercise3 fragment = new FragmentExercise3();
        fragment.setFragmentLayout(R.layout.fragment_exercises_exercise3);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        app.setExercise3Items((ArrayList<ExerciseAdapter.itemTemplate>) recyclerAdapter.getAdapterContent());
    }

    @Override
    public void onViewAvailable(Bundle savedInstanceState) {

        ArrayList<ExerciseAdapter.itemTemplate> itemsToShow = new ArrayList<>();

        if (getContext() != null) {
            MainActivity activity = (MainActivity) getContext();
            app = (WTestApplication) activity.getApplication();
            if (app.getExercise3Items() != null) {
                itemsToShow = app.getExercise3Items();
            }
        }

        fragmentRecyclerView = getFragmentView().findViewById(R.id.recyclerView);
        if (fragmentRecyclerView != null) {
            fragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            fragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerAdapter = new ExerciseAdapter(getContext());
            if (itemsToShow.size() == 0) {
                int currentType = 0;

                for (int i = 0; i < 50; ++i) {
                    if (currentType == TYPE_TEXT) {
                        ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();
                        item.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                        item.setShowEditText(true);
                        item.setContentToShow(getContext().getString(R.string.third_exercise_normal));
                        item.setContentType(ExerciseAdapter.itemTemplate.CONTENT_NORMAL);
                        itemsToShow.add(item);
                        currentType = TYPE_NUMBERS;

                    } else if (currentType == TYPE_NUMBERS) {
                        ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();
                        item.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                        item.setShowEditText(true);
                        item.setContentToShow(getContext().getString(R.string.third_exercise_numeric));
                        item.setContentType(ExerciseAdapter.itemTemplate.CONTENT_NUMERIC);
                        itemsToShow.add(item);
                        currentType = TYPE_ALL_CAPS;

                    } else if (currentType == TYPE_ALL_CAPS) {
                        ExerciseAdapter.itemTemplate item = new ExerciseAdapter.itemTemplate();
                        item.setViewType(ExerciseAdapter.VIEW_TYPE_TEXT);
                        item.setShowEditText(true);
                        item.setContentToShow(getContext().getString(R.string.third_exercise_all_caps));
                        item.setContentType(ExerciseAdapter.itemTemplate.CONTENT_ALL_CAPS);
                        itemsToShow.add(item);
                        currentType = TYPE_TEXT;
                    }
                }
            }

            fragmentRecyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.setAdapterContent(itemsToShow, false);
        }
    }
}

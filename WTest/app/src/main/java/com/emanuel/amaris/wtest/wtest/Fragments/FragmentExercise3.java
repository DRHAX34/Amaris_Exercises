package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.emanuel.amaris.wtest.wtest.Adapters.ExerciseAdapter;
import com.emanuel.amaris.wtest.wtest.R;

public class FragmentExercise3 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise3Tag";

    private RecyclerView fragmentRecyclerView;
    private ExerciseAdapter recyclerAdapter;

    public FragmentExercise3() {
        //Default Empty Constructor
    }

    public static FragmentExercise3 newInstance() {
        FragmentExercise3 fragment = new FragmentExercise3();
        fragment.setFragmentLayout(R.layout.fragment_exercises_recyclerview);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewAvailable(Bundle savedInstanceState) {
        fragmentRecyclerView = getFragmentView().findViewById(R.id.recyclerView);
        fragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        fragmentRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //TODO: Set items to Adapter
        //fragmentRecyclerView.setAdapter()
    }
}

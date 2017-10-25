package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.emanuel.amaris.wtest.wtest.R;

//TODO: Why do I feel this will be the hardest one...?

public class FragmentExercise2 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise2Tag";

    public FragmentExercise2() {

    }

    public static FragmentExercise2 newInstance() {
        FragmentExercise2 fragment = new FragmentExercise2();
        fragment.setFragmentLayout(R.layout.fragment_fragment_exercise2);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewAvailable(Bundle savedInstanceState) {

    }
}

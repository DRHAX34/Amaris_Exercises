package com.emanuel.amaris.wtest.wtest.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emanuel.amaris.wtest.wtest.R;

/**
 * Created by emanuel on 24-10-2017.
 */

/* This is a base class for Fragment to eliminate boilerplate code from the main Fragment classes
   with the purpose of code organization */

public abstract class AppFragment extends Fragment {

    //The view of the fragment, we need to hold on to this so we can get to the views inside of the fragment layout
    //This is needed because for this exercise we will use the fragment class to hold the logic for each exercise
    protected View fragmentView;

    //This is a hint so Android Studio warns the developer that the code is expecting a layout id for this variable
    @LayoutRes
    protected int fragmentLayoutResId = -1;

    //Allows the activity to interact with the Fragment or vice-versa
    private AppFragment.OnFragmentInteractionListener mListener;

    //This is a method that was created so the main fragments can access the View
    public View getFragmentView() {
        return fragmentView;
    }

    public void setFragmentView(View view) {
        this.fragmentView = view;
    }

    /*This method was created to set the layout res id that we will use to inflate the layout.
      No getter was created since we don't need to access the layout res id in the main fragment.
      Once again, this is the same hint needed for Android Studio to recognize this int is expected to hold a layout id
     */
    public void setFragmentLayout(@LayoutRes int resId) {
        this.fragmentLayoutResId = resId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int layoutToInflate;

        if (fragmentLayoutResId != -1) {
            layoutToInflate = fragmentLayoutResId;
        } else {
            layoutToInflate = R.layout.layout_not_specified;
        }

        View fragmentView = inflater.inflate(layoutToInflate, container, false);
        setFragmentView(fragmentView);

        if (fragmentView != null) {
            onViewAvailable(savedInstanceState);
        }

        return getFragmentView();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public abstract void onViewAvailable(Bundle savedInstanceState);

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri webst);
    }
}

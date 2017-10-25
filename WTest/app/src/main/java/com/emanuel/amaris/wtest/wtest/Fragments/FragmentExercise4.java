package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.emanuel.amaris.wtest.wtest.BuildConfig;
import com.emanuel.amaris.wtest.wtest.R;


public class FragmentExercise4 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise4Tag";

    private WebView fragmentWebView;
    private WebViewClient webviewClient;

    public FragmentExercise4() {
    }

    public static FragmentExercise4 newInstance() {
        FragmentExercise4 fragment = new FragmentExercise4();
        fragment.setFragmentLayout(R.layout.fragment_fragment_exercise4);
        return fragment;
    }

    @Override
    public void onViewAvailable(Bundle savedInstanceState) {
        fragmentWebView = getFragmentView().findViewById(R.id.exercise_4_webview);
        if (webviewClient == null) {
            webviewClient = new WebViewClient();
            fragmentWebView.setWebViewClient(new WebViewClient());
            fragmentWebView.loadUrl(BuildConfig.WEBSITE_TO_SHOW);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentWebView.saveState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            fragmentWebView.restoreState(savedInstanceState);
    }

}

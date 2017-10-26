package com.emanuel.amaris.wtest.wtest.Fragments;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.emanuel.amaris.wtest.wtest.BuildConfig;
import com.emanuel.amaris.wtest.wtest.MainActivity;
import com.emanuel.amaris.wtest.wtest.R;
import com.emanuel.amaris.wtest.wtest.WTestApplication;


/**
 * Welcome to the fragment for exercise 4, this fragment is here with the purpose of demonstrating my skills in Android Development.
 * For me, this was the easiest fragment to make, and I hope I didn't misinterpreted the requisites
 * The two flavours should display different websites as requested
 */
public class FragmentExercise4 extends AppFragment {

    public static final String FRAGMENT_TAG = "Exercise4Tag";

    private WebView fragmentWebView;
    private WebViewClient webviewClient;
    private ProgressBar progressbar;

    private boolean hasFinishedLoading = false;

    WTestApplication app;

    public FragmentExercise4() {
    }

    public static FragmentExercise4 newInstance() {
        FragmentExercise4 fragment = new FragmentExercise4();
        fragment.setFragmentLayout(R.layout.fragment_fragment_exercise4);
        return fragment;
    }

    //Method to be able to access the fragment views without repeating the same boilerplate code all over again
    //Saves time and makes a better code organization
    @Override
    public void onViewAvailable(Bundle savedInstanceState) {
        fragmentWebView = getFragmentView().findViewById(R.id.exercise_4_webview);
        progressbar = getFragmentView().findViewById(R.id.progressbar);

        if (fragmentWebView != null) {
            Bundle webState = null;

            if (getContext() != null) {
                MainActivity activity = (MainActivity) getContext();
                app = (WTestApplication) activity.getApplication();
                webState = app.getWebStateBundle();
            }

            if (webviewClient == null) {
                //Load the webview and set the webview client
                webviewClient = new WebViewClient();
                fragmentWebView.setWebViewClient(new Callback());
                fragmentWebView.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        //Make the bar disappear after URL is loaded, and changes string to Loading...
                        progressbar.setProgress(progress); //Make the bar disappear after URL is loaded

                        // Return the app name after finish loading
                        if (progress == 100)
                            progressbar.setVisibility(ProgressBar.GONE);
                        else
                            progressbar.setVisibility(ProgressBar.VISIBLE);
                    }
                });
                fragmentWebView.getSettings().setJavaScriptEnabled(true);
                if (webState == null)
                    //If there is no saved state from before, just load the URL
                    fragmentWebView.loadUrl(BuildConfig.WEBSITE_TO_SHOW);
                else
                    fragmentWebView.restoreState(webState);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save the webstate is it was loaded
        if (fragmentWebView != null) {
            Bundle webstate = new Bundle();
            if (hasFinishedLoading) {
                fragmentWebView.saveState(webstate);
                app.setWebStateBundle(webstate);
            }
        }
    }

    //Callback needed so we can know if page was loaded
    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hasFinishedLoading = true;
        }


    }
}

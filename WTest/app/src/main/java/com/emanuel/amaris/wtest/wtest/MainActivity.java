package com.emanuel.amaris.wtest.wtest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.emanuel.amaris.wtest.wtest.Fragments.AppFragment;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise1;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise2;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise3;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise4;

/**
 * Created by emanuel on 25-10-2017.
 */

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView navigation;

    Toolbar toolbar;

    AlphaAnimation animation1;

    ValueAnimator anim;

    FragmentManager fragmentManager;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_exercise_1:
                    setFragmentTo(FragmentExercise1.FRAGMENT_TAG);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    return true;
                case R.id.navigation_exercise_2:
                    setFragmentTo(FragmentExercise2.FRAGMENT_TAG);
                    toolbar.setBackgroundColor(Color.WHITE);
                    toolbar.setTitleTextColor(Color.BLACK);
                    return true;
                case R.id.navigation_exercise_3:
                    setFragmentTo(FragmentExercise3.FRAGMENT_TAG);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    return true;
                case R.id.navigation_exercise_4:
                    setFragmentTo(FragmentExercise4.FRAGMENT_TAG);
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_exercise_1);
    }

    @Override
    public void onResume() {
        super.onResume();

        navigation.setSelectedItemId(navigation.getSelectedItemId());
    }

    private FragmentManager getAppFragmentManager() {
        if (fragmentManager == null) {
            fragmentManager = getSupportFragmentManager();
        }

        return fragmentManager;
    }

    private void setFragmentTo(String fragmentTag) {

        FragmentTransaction transaction = getAppFragmentManager().beginTransaction();

        transaction.replace(R.id.content, returnFragmentWithTag(fragmentTag), fragmentTag).commit();
    }

    private AppFragment returnFragmentWithTag(String tag) {
        switch (tag) {
            case FragmentExercise1.FRAGMENT_TAG:
                return FragmentExercise1.newInstance();
            case FragmentExercise2.FRAGMENT_TAG:
                return FragmentExercise2.newInstance();
            case FragmentExercise3.FRAGMENT_TAG:
                return FragmentExercise3.newInstance();
            case FragmentExercise4.FRAGMENT_TAG:
                return FragmentExercise4.newInstance();
            default:
                return null;
        }
    }

    //Catch all the touch events from every view, if it's a EditText that currently has the touch focus, remove the focus.
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}

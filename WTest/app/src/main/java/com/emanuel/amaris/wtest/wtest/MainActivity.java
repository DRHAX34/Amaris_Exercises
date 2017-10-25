package com.emanuel.amaris.wtest.wtest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.emanuel.amaris.wtest.wtest.Fragments.AppFragment;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise1;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise2;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise3;
import com.emanuel.amaris.wtest.wtest.Fragments.FragmentExercise4;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigation;

    private final String CURRENT_FRAGMENT = "currentFragment";

    FragmentManager fragmentManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_exercise_1:
                    setFragmentTo(FragmentExercise1.FRAGMENT_TAG);
                    return true;
                case R.id.navigation_exercise_2:
                    setFragmentTo(FragmentExercise2.FRAGMENT_TAG);
                    return true;
                case R.id.navigation_exercise_3:
                    setFragmentTo(FragmentExercise3.FRAGMENT_TAG);
                    return true;
                case R.id.navigation_exercise_4:
                    setFragmentTo(FragmentExercise4.FRAGMENT_TAG);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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

}

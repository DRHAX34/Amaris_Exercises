package com.emanuel.amaris.wtest.wtest;

import android.app.Application;
import android.os.Bundle;

import com.emanuel.amaris.wtest.wtest.Adapters.ExerciseAdapter;

import java.util.ArrayList;

/**
 * Created by emanuel on 25-10-2017.
 */

/**
 * This Application class only holds some items since Google never enjoys having a stable OnSaveInstanceState experience
 */
public class WTestApplication extends Application {

    private Bundle WebStateBundle;

    private ArrayList<ExerciseAdapter.itemTemplate> Exercise3Items;

    public Bundle getWebStateBundle() {
        return WebStateBundle;
    }

    public void setWebStateBundle(Bundle webstate) {
        WebStateBundle = webstate;
    }

    public ArrayList<ExerciseAdapter.itemTemplate> getExercise3Items() {
        return Exercise3Items;
    }

    public void setExercise3Items(ArrayList<ExerciseAdapter.itemTemplate> Exercise3Items) {
        this.Exercise3Items = Exercise3Items;
    }

}

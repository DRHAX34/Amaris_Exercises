package com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * Created by emanuel on 25-10-2017.
 */

public final class WTestDbContract {
    // To prevent someone from instantiating the contract class,
    // we make the constructor private.
    private WTestDbContract() {
    }

    /* Inner class that defines the table contents */
    public static class WTestDbEntry implements BaseColumns {
        public static final String TABLE_NAME = "POSTAL_CODES";
        public static final String COLUMN_LOCAL = "PLACE";
        public static final String COLUMN_LOCAL_ASCII = "PLACE_ASCII";
        public static final String COLUMN_VALUE = "VALUE";
    }

    public static class Filter {

        private String value;

        public Filter() {
            this.value = "";
        }

        public Filter(@NonNull String value) {
            this.value = value;
        }


        public String getValue() {
            return value;
        }

        public void setValue(@NonNull String whereClause) {
            this.value = whereClause;
        }
    }

    public static class DbItem {
        private String Place;
        private String Value;

        public DbItem() {
            Place = "";
            Value = "";
        }

        public DbItem(@NonNull String place, @NonNull String value) {
            Place = place;
            Value = value;
        }

        @NonNull
        public String getPlace() {
            return Place;
        }

        public void setPlace(@NonNull String place) {
            Place = place;
        }

        @NonNull
        public String getValue() {
            return Value;
        }

        public void setValue(@NonNull String value) {
            Value = value;
        }

        @Override
        public String toString() {
            if (this.isEmpty()) {
                return "";
            }

            if (Place.isEmpty()) {
                return Value;
            }

            if (Value.isEmpty()) {
                return Place;
            }

            return Value + ", " + Place;
        }

        public boolean isEmpty() {
            return (Value.isEmpty() && Place.isEmpty());
        }
    }
}

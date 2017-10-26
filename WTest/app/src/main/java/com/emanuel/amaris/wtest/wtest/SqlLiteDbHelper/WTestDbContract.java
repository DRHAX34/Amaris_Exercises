package com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * Created by emanuel on 25-10-2017.
 */

//This class is here to bring stability and to make a good and stable connection between all the database stuff and the other activities
public final class WTestDbContract {
    // To prevent someone from instantiating the contract class,
    // we make the constructor private.
    private WTestDbContract() {
    }

    /* Inner class that defines the table contents for our db */
    public static class WTestDbEntry implements BaseColumns {
        public static final String TABLE_NAME = "POSTAL_CODES";
        public static final String COLUMN_LOCAL = "PLACE";
        public static final String COLUMN_LOCAL_ASCII = "PLACE_ASCII";
        public static final String COLUMN_VALUE = "VALUE";
    }

    //Here we define the blueprint of a Filter so we can later use it in the PostalCodeManagerClass
    public static class Filter {

        private String value;

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

    //Here is the class that will hold the data when it's fetched from the DB
    public static class DbItem {
        private String Place;
        private String Value;

        public DbItem(@NonNull String place, @NonNull String value) {
            Place = place;
            Value = value;
        }

        @NonNull
        public String getPlace() {
            return Place;
        }

        @NonNull
        public String getValue() {
            return Value;
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

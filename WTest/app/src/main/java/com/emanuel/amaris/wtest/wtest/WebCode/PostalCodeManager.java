package com.emanuel.amaris.wtest.wtest.WebCode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper.WTestDbContract;
import com.emanuel.amaris.wtest.wtest.SqlLiteDbHelper.WTestDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by emanuel on 25-10-2017.
 */

public class PostalCodeManager {

    private final String GITHUB_URL = "https://github.com/centraldedados/codigos_postais";

    private final String RESOURCE_NAME = "codigos_postais";

    protected Handler uiThread;

    protected GetPostalCodesFromDb taskDb;

    protected GetPostalCodesFromWeb taskWeb;

    public static PostalCodeManager manager;

    private PostalCodeEventListener listener;

    private WTestDbHelper dbHelper;

    private WTestDbContract.Filter filter;

    private Context context;

    private PostalCodeManager(Context context) {
        this.context = context;
    }

    //Force the programmer to use the getInstance method by making the constructor private
    //This way, we're not putting stuff to do on the constructor and making the code organized
    //Constructor should only be used to gather all necessary stuff so the rest of the class works as it should
    //Also, since this is accessing the db and we do not want to constantly reinstanciate the DB connection, we call this method to only get
    //a single Instance
    public static PostalCodeManager getInstance(Context context, PostalCodeEventListener listener) {
        if (manager == null) {
            manager = new PostalCodeManager(context);
        }
        manager.setPostalCodeEventListener(listener);
        manager.uiThread = new Handler(Looper.getMainLooper());

        return manager;
    }

    //This method initializes the DbHelper if it's not already initialized and returns it to the caller
    private WTestDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new WTestDbHelper(context);
        }

        return dbHelper;
    }

    public long getDbCount() {
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();

        long cnt = DatabaseUtils.queryNumEntries(dbRead, WTestDbContract.WTestDbEntry.TABLE_NAME);
        dbRead.close();

        return cnt;
    }

    //This method will force the update of data from the web repository
    private void forceLoadPostalCodes(boolean firstTime) {
        taskWeb = new GetPostalCodesFromWeb();

        taskWeb.isFirstStart = firstTime;

        taskWeb.execute(GITHUB_URL);
    }

    //This method will set the event listener, so we can update progress and know when to show updated data to the user
    public void setPostalCodeEventListener(PostalCodeEventListener listener) {
        this.listener = listener;
    }

    public WTestDbContract.Filter getFilter() {
        return this.filter;
    }

    public void setFilter(WTestDbContract.Filter filter) {
        this.filter = filter;
    }

    public void loadPostalCodes(boolean forceLoadFromWeb, final int indexToStart) {
        if (taskWeb == null || !taskWeb.isRunning) {
            if (forceLoadFromWeb) {
                forceLoadPostalCodes(false);
            } else {
                taskDb = new GetPostalCodesFromDb();
                taskDb.execute(indexToStart);
            }
        } else {
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onPreLoadData(true);
                    }
                }
            });
        }
    }

    private void updateDatabaseWithNewData(ArrayList<WTestDbContract.DbItem> postalCodes, boolean clearDatabase) {
        SQLiteDatabase wtestDb = getDbHelper().getWritableDatabase();

        //Clear the db (cache)
        if (clearDatabase)
            try {
                wtestDb.delete(WTestDbContract.WTestDbEntry.TABLE_NAME, "", null);
            } catch (IllegalStateException exception) {
                //Try to re-open the db helper again
                wtestDb = getDbHelper().getWritableDatabase();
                wtestDb.delete(WTestDbContract.WTestDbEntry.TABLE_NAME, "", null);
            }

        ContentValues values = new ContentValues();

        //Insert new values on the db
        for (WTestDbContract.DbItem postalCode : postalCodes) {
            values.put(WTestDbContract.WTestDbEntry.COLUMN_VALUE, postalCode.getValue());
            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL, postalCode.getPlace());
            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII, Normalizer.normalize(postalCode.getPlace(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", ""));
            try {
                wtestDb.insert(WTestDbContract.WTestDbEntry.TABLE_NAME, null, values);
            } catch (IllegalStateException exception) {
                //Try to re-open the db helper again
                wtestDb = getDbHelper().getWritableDatabase();
                wtestDb.insert(WTestDbContract.WTestDbEntry.TABLE_NAME, null, values);
            }
            values.clear();
        }

        postalCodes.clear();
    }

    public class GetPostalCodesFromDb extends AsyncTask<Integer, Integer, List<WTestDbContract.DbItem>> {

        boolean hasError = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (listener != null) {
                listener.onPreLoadData(false);
            }
        }

        @Override
        protected List<WTestDbContract.DbItem> doInBackground(Integer... ints) {
            SQLiteDatabase dbWTest = getDbHelper().getReadableDatabase();

            String sortOrder = " ORDER BY " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " ASC";

            String whereClause = "";

            if (filter != null && !filter.getValue().isEmpty()) {
                String words = filter.getValue().replaceAll("[0-9,;]+", "").replaceAll("\\s", "%");

                //Remove all accents and weird characters from search
                String wordsNormalized = Normalizer.normalize(words, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");

                if (words.equals("-")) {
                    words = "";
                }

                String numbers = filter.getValue().replaceAll("[a-zA-Z,;]+", "");

                if (numbers.trim().length() == 0) {
                    numbers = "";
                } else {
                    numbers = numbers.replaceAll("\\s", "%");
                }

                if (words.isEmpty()) {
                    if (!numbers.isEmpty()) {
                        whereClause = "WHERE " + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " LIKE '%" + numbers + "%' ";
                    }
                } else {
                    whereClause = "WHERE " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " LIKE '%" + words + "%' OR " +
                            WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII + " LIKE '%" + wordsNormalized + "%' ";

                    String[] wordArraySearch = words.split("%");

                    if (wordArraySearch.length > 1) {

                        whereClause += " OR (";

                        for (int i = 0; i < wordArraySearch.length; i++) {
                            String word = wordArraySearch[i];
                            String wordNormalized = Normalizer.normalize(word, Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", "");

                            whereClause += "(" + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " LIKE '%" + word + "%' OR " +
                                    WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII + " LIKE '%" + wordNormalized + "%')";

                            if (i != wordArraySearch.length - 1) {
                                whereClause += " AND ";
                            } else {
                                whereClause += ") ";
                            }
                        }

                    }

                    if (!numbers.isEmpty()) {
                        whereClause += " OR " + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " LIKE '%" + numbers + "%' ";
                    }
                }
            }

            hasError = false;

            Cursor cursor;
            try {
                cursor = dbWTest.rawQuery("SELECT DISTINCT " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + "," + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " FROM " +
                        WTestDbContract.WTestDbEntry.TABLE_NAME + " " + whereClause + " " + sortOrder + " LIMIT 15 OFFSET " + ints[0], null);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
                hasError = true;

                //Wrong query, no need to alert the user since it only happens if user is messing with search and trying to be naughty
                return new ArrayList<>();
            }

            List<WTestDbContract.DbItem> items = new ArrayList<>();
            while (cursor.moveToNext()) {
                WTestDbContract.DbItem item = new WTestDbContract.DbItem(cursor.getString(cursor.getColumnIndex(WTestDbContract.WTestDbEntry.COLUMN_LOCAL)),
                        cursor.getString(cursor.getColumnIndex(WTestDbContract.WTestDbEntry.COLUMN_VALUE)));
                items.add(item);
            }
            cursor.close();

            return items;
        }

        @Override
        protected void onPostExecute(List<WTestDbContract.DbItem> items) {
            super.onPostExecute(items);

            if (items.size() == 0) {
                if (!hasError) {
                    if (filter == null || filter.getValue().isEmpty()) {
                        forceLoadPostalCodes(true);
                    } else {
                        if (listener != null) {
                            listener.onDataLoaded(items);
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onDataLoaded(items);
                    }
                }
            } else {
                if (listener != null) {
                    listener.onDataLoaded(items);
                }
            }
        }
    }


    public class GetPostalCodesFromWeb extends AsyncTask<String, Integer, List<WTestDbContract.DbItem>> {

        HttpURLConnection urlConnection;

        boolean isFirstStart = false;

        boolean isRunning = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (listener != null) {
                listener.onPreLoadData(true);
            }
        }

        @Override
        protected List<WTestDbContract.DbItem> doInBackground(String... strings) {
            /**
             * Doing this in a certain dynamic way, I'm accessing the datapackage on the github repository so I can get the path to
             * the .csv file that contains the postal codes.
             *
             * I could do it the direct way, but if the owner of the github changes the path or name of file, the app would stop working,
             * if we code it like this, at least until the owner of the github repository changes the name of the datapackage file, the app will find it's way around
             */
            isRunning = true;
            int progress;

            //Use stringbuilders to increase performance, since Java takes longer with simple "Add" operations to a String
            StringBuilder result = new StringBuilder();

            //Get the raw content, we don't need all the extra html stuff, only the file
            String urlString = strings[0].replace("github.com", "raw.githubusercontent.com") + "/master/datapackage.json";

            try {

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();

                Log.d(this.getClass().getSimpleName(), "Connecting to " + urlString);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                progress = 10;
                publishProgress(progress);

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                    if (progress <= 30) {
                        publishProgress(progress++);
                    }
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.onExceptionData();
                }
                e.printStackTrace();
                isRunning = false;
                //STOP METHOD EXECUTION HERE, THERE HAS BEEN AN ERROR CONTACTING THE SYSTEM
                return null;
            } finally {
                urlConnection.disconnect();
            }


            String pathToPostalCodes = "";

            try {
                JSONObject datapackage = new JSONObject(result.toString());

                JSONArray arrayData = datapackage.getJSONArray("resources");

                for (int i = 0; i < arrayData.length(); ++i) {
                    if (arrayData.getJSONObject(i).getString("name").equals(RESOURCE_NAME)) {
                        pathToPostalCodes = arrayData.getJSONObject(i).getString("path");
                        break;
                    }
                }

                Log.d(this.getClass().getSimpleName(), pathToPostalCodes.isEmpty() ? "No path obtained" : "Path obtained: " + pathToPostalCodes);
                progress = 35;
                publishProgress(progress);
            } catch (JSONException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onExceptionData();
                }
                isRunning = false;
                //STOP METHOD EXECUTION HERE, WRONG TYPE OF DATA
                return null;
            }

            ArrayList<WTestDbContract.DbItem> postalCodes = new ArrayList<>();

            if (!pathToPostalCodes.isEmpty()) {
                StringBuilder realResult = new StringBuilder();

                urlString = strings[0].replace("github.com", "raw.githubusercontent.com") + "/master/" + pathToPostalCodes;

                try {
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    progress = 40;
                    publishProgress(progress);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    int indexPlace = -1;
                    int firstPostalCode = -1;
                    int secondPostalCode = -1;

                    final ArrayList<WTestDbContract.DbItem> itemsToAdd = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] columns = line.split(",");
                        if (columns.length == 0) {
                            columns = realResult.toString().split(";");
                        }

                        if (indexPlace != -1) {
                            WTestDbContract.DbItem item = new WTestDbContract.DbItem(columns[indexPlace], columns[firstPostalCode] + "-" + columns[secondPostalCode]);
                            postalCodes.add(item);
                            itemsToAdd.add(item);
                            if (itemsToAdd.size() == 30) {
                                boolean clearDb = postalCodes.size() == 30;

                                //Update the database in the background, taking advantage of all the cores in the device and making already available the data
                                //Since the data to fetch is too big and it would take too long to show
                                updateDatabaseWithNewData(itemsToAdd, clearDb);

                                itemsToAdd.clear();
                            }
                        } else {
                            for (int j = 0; j < columns.length; ++j) {
                                //Search the header for the positions
                                if (columns[j].equals("localidade")) {
                                    indexPlace = j;
                                } else if (columns[j].equals("cod_postal")) {
                                    firstPostalCode = j;
                                } else if (columns[j].equals("extensao_cod_postal")) {
                                    secondPostalCode = j;
                                }
                            }
                        }
                    }

                    if (itemsToAdd.size() > 0) {
                        boolean clearDb = itemsToAdd.size() == postalCodes.size();
                        //Update the database in the background, taking advantage of all the cores in the device
                        updateDatabaseWithNewData(itemsToAdd, clearDb);
                    }

                    postalCodes.clear();

                    loadPostalCodes(false, 0);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onExceptionData();
                    }
                    isRunning = false;
                    //STOP METHOD EXECUTION HERE, WRONG PATH
                    return null;
                } finally {
                    urlConnection.disconnect();
                }
            }

            return postalCodes;
        }
    }

    //Always close the db connection
    @Override
    protected void finalize() throws Throwable {
        try {
            getDbHelper().close();
        } finally {
            super.finalize();
        }
    }

    public interface PostalCodeEventListener {

        void onDataLoaded(List<WTestDbContract.DbItem> postalCodes);

        void onPreLoadData(boolean isCaching);

        void onExceptionData();

    }
}

package com.emanuel.amaris.wtest.wtest.WebCode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    //Necessary constants
    private final String GITHUB_URL = "https://github.com/centraldedados/codigos_postais";
    private final String RESOURCE_NAME = "codigos_postais";
    private final int UPDATE_INTERVAL = 10;

    //Properties
    protected Handler uiThread;

    protected GetPostalCodesFromDb taskDb;

    protected GetPostalCodesFromWeb taskWeb;

    public static PostalCodeManager manager;

    private PostalCodeEventListener listener;

    private WTestDbHelper dbHelper;

    private SQLiteDatabase wtestDbWritable;

    private WTestDbContract.Filter filter;

    private Context context;

    public int itemCount;

    public int counter;

    private final Object threadLock = new Object();
    private final Object threadA = new Object();
    private final Object threadB = new Object();
    private final Object threadC = new Object();
    private boolean[] threadFinished = {false, false, false};

    private PostalCodeManager(Context context) {
        this.context = context;
    }

    //Force the programmer to use the getInstance method by making the constructor private
    //This way, we're not putting stuff to do on the constructor and making the code organized
    //Constructor should only be used to gather all necessary stuff so the rest of the class works as it should
    //Also, since this is accessing the db and we do not want to constantly reinstanciate the DB connection, we call this method to only get
    //a single Instance
    public static PostalCodeManager getInstance(Context context, PostalCodeEventListener listener) {
        boolean startLoadingData = false;
        if (manager == null) {
            manager = new PostalCodeManager(context);
            Log.d("PostalCodeManager", "Instantiate PostalCodeManager");
            startLoadingData = true;
        }

        manager.setPostalCodeEventListener(listener);
        manager.uiThread = new Handler(Looper.getMainLooper());

        if (startLoadingData) {
            manager.loadPostalCodes(0);
        }

        return manager;
    }

    //This method initializes the DbHelper if it's not already initialized and returns it to the caller
    private WTestDbHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new WTestDbHelper(context);
        }

        return dbHelper;
    }


    //This method will force the update of data from the web repository
    private void forceLoadPostalCodes(boolean firstTime) {
        if (taskWeb != null && taskWeb.isRunning) {
            return;
        }
        taskWeb = new GetPostalCodesFromWeb();

        taskWeb.isFirstStart = firstTime;

        counter = 0;

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

    //Method executed by activities/fragments
    public void loadPostalCodes(final int indexToStart) {
        if (taskWeb == null || !taskWeb.isRunning) {
                taskDb = new GetPostalCodesFromDb();
                taskDb.execute(indexToStart);
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

    //Update the Db with data from the web repository
    private void updateDatabaseWithNewData(ArrayList<String> lines) {
        wtestDbWritable = getDbHelper().getWritableDatabase();

        //Clear the db because it's basically a cache
        try {
            wtestDbWritable.delete(WTestDbContract.WTestDbEntry.TABLE_NAME, "", null);
        } catch (IllegalStateException exception) {
            //Try to re-open the db helper again
            wtestDbWritable = getDbHelper().getWritableDatabase();
            wtestDbWritable.delete(WTestDbContract.WTestDbEntry.TABLE_NAME, "", null);
        }

        int indexPlace = -1;
        int firstPostalCode = -1;
        int secondPostalCode = -1;

        //For audit puposes
        counter = 0;
        itemCount = lines.size();

        String[] columns = lines.get(0).split(",");
        if (columns.length == 0) {
            columns = lines.get(0).toString().split(";");
        }

        for (int j = 0; j < columns.length; ++j) {
            //Search the header for the positions of each data we need
            if (columns[j].equals("localidade")) {
                indexPlace = j;
            } else if (columns[j].equals("cod_postal")) {
                firstPostalCode = j;
            } else if (columns[j].equals("extensao_cod_postal")) {
                secondPostalCode = j;
            }
        }

        final int indexPlaceFinal = indexPlace;
        final int firstPostalCodeFinal = firstPostalCode;
        final int secondPostalCodeFinal = secondPostalCode;

        threadFinished = new boolean[]{false, false, false};

        final List<String> itemsToAddPart1 = lines.subList(1, (itemCount / 3) - 1);
        final List<String> itemsToAddPart2 = lines.subList(itemCount / 3, ((itemCount / 3) * 2) - 1);
        final List<String> itemsToAddPart3 = lines.subList(((itemCount / 3) * 2), itemCount - 1);

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();

                synchronized (threadA) {
                    ContentValues values = new ContentValues();
                    int progressUpdateCounter = 0;

                    //Insert new values on the db
                    for (int i = 0; i < itemsToAddPart1.size(); ++i) {
                        String postalCode = itemsToAddPart1.get(i);

                        String[] columns = postalCode.split(",");
                        if (columns.length == 0) {
                            columns = postalCode.toString().split(";");
                        }

                        if (indexPlaceFinal != -1) {
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_VALUE, columns[firstPostalCodeFinal] + "-" + columns[secondPostalCodeFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL, columns[indexPlaceFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII, Normalizer.normalize(columns[indexPlaceFinal], Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", ""));

                            wtestDbWritable.insert(WTestDbContract.WTestDbEntry.TABLE_NAME, null, values);
                            values.clear();
                            counter++;
                            progressUpdateCounter++;

                            if (listener != null && progressUpdateCounter == UPDATE_INTERVAL) {
                                uiThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onProgressUpdate(counter, itemCount);
                                    }
                                });
                                progressUpdateCounter = 0;
                            }
                        }
                    }
                }
                synchronized (threadB) {
                    synchronized (threadC) {
                        threadFinished[0] = true;

                        Log.d("DbWTest", "Thread 1 finished");

                        if (threadFinished[0] && threadFinished[1] && threadFinished[2]) {
                            if (taskWeb != null) {
                                taskWeb.isRunning = false;
                            }
                            uiThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadPostalCodes(0);
                                }
                            });

                            Log.d("DbWTest", "inserted in database successfully");
                        }
                    }
                }
            }
        };


        Thread thread2 = new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (threadB) {
                    ContentValues values = new ContentValues();
                    int progressUpdateCounter = 0;

                    //Insert new values on the db
                    for (int i = 0; i < itemsToAddPart2.size(); ++i) {
                        String postalCode = itemsToAddPart2.get(i);

                        String[] columns = postalCode.split(",");
                        if (columns.length == 0) {
                            columns = postalCode.toString().split(";");
                        }

                        if (indexPlaceFinal != -1) {
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_VALUE, columns[firstPostalCodeFinal] + "-" + columns[secondPostalCodeFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL, columns[indexPlaceFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII, Normalizer.normalize(columns[indexPlaceFinal], Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", ""));
                            wtestDbWritable.insert(WTestDbContract.WTestDbEntry.TABLE_NAME, null, values);
                            values.clear();
                            counter++;
                            progressUpdateCounter++;

                            if (listener != null && progressUpdateCounter == UPDATE_INTERVAL) {
                                uiThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onProgressUpdate(counter, itemCount);
                                    }
                                });
                                progressUpdateCounter = 0;
                            }
                        }
                    }
                }

                synchronized (threadA) {
                    synchronized (threadC) {
                        threadFinished[1] = true;

                        Log.d("DbWTest", "Thread 2 finished");

                        if (threadFinished[0] && threadFinished[1] && threadFinished[2]) {
                            if (taskWeb != null) {
                                taskWeb.isRunning = false;
                            }
                            uiThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadPostalCodes(0);
                                }
                            });

                            Log.d("DbWTest", "inserted in database successfully");
                        }
                    }
                }
            }
        };


        Thread thread3 = new Thread() {
            @Override
            public void run() {
                super.run();

                synchronized (threadC) {
                    ContentValues values = new ContentValues();
                    int progressUpdateCounter = 0;

                    //Insert new values on the db
                    for (int i = 0; i < itemsToAddPart3.size(); ++i) {
                        String postalCode = itemsToAddPart3.get(i);

                        String[] columns = postalCode.split(",");
                        if (columns.length == 0) {
                            columns = postalCode.toString().split(";");
                        }

                        if (indexPlaceFinal != -1) {
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_VALUE, columns[firstPostalCodeFinal] + "-" + columns[secondPostalCodeFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL, columns[indexPlaceFinal]);
                            values.put(WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII, Normalizer.normalize(columns[indexPlaceFinal], Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", ""));

                            wtestDbWritable.insert(WTestDbContract.WTestDbEntry.TABLE_NAME, null, values);
                            values.clear();
                            counter++;
                            progressUpdateCounter++;

                            if (listener != null && progressUpdateCounter == UPDATE_INTERVAL) {
                                uiThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onProgressUpdate(counter, itemCount);
                                    }
                                });
                                progressUpdateCounter = 0;
                            }
                        }
                    }
                }

                synchronized (threadA) {
                    synchronized (threadB) {
                        threadFinished[2] = true;

                        Log.d("DbWTest", "Thread 3 finished");

                        if (threadFinished[0] && threadFinished[1] && threadFinished[2]) {
                            if (taskWeb != null) {
                                taskWeb.isRunning = false;
                            }
                            uiThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadPostalCodes(0);
                                }
                            });

                            Log.d("DbWTest", "inserted in database successfully");
                        }
                    }
                }
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();
    }

    //AsyncTask to interface with the database in an asyncronous way
    public class GetPostalCodesFromDb extends AsyncTask<Integer, Integer, List<WTestDbContract.DbItem>> {

        boolean hasError = false;

        boolean isRunning = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Inform the activity/fragment that we are about to do magic
            if (listener != null) {
                listener.onPreLoadData(false);
            }
            isRunning = true;
        }

        @Override
        protected List<WTestDbContract.DbItem> doInBackground(Integer... ints) {
            isRunning = true;

            //Get the database in read only mode
            SQLiteDatabase dbWTest = getDbHelper().getReadableDatabase();

            //Sort by cities in a ascending order
            String sortOrder = " ORDER BY " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " ASC";

            String whereClause = "";

            if (filter != null && !filter.getValue().isEmpty()) {

                //Separate all stuff into their own string, words, normalizedWords and numbers
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
                        //Only Search numbers then
                        whereClause = "WHERE " + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " LIKE '%" + numbers + "%' ";
                    }

                    //Or don't search nothing at all
                } else {
                    //search the name of the cities
                    whereClause = "WHERE " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + " LIKE '%" + words + "%' OR " +
                            WTestDbContract.WTestDbEntry.COLUMN_LOCAL_ASCII + " LIKE '%" + wordsNormalized + "%' ";

                    //Word search, so we can enter the words without order
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

                    //if there were numbers in the filter, search the postal codes as well
                    if (!numbers.isEmpty()) {
                        whereClause += " OR " + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " LIKE '%" + numbers + "%' ";
                    }
                }
            }

            hasError = false;

            Cursor cursor;
            try {
                //Fetch the data from the DataBase
                cursor = dbWTest.rawQuery("SELECT DISTINCT " + WTestDbContract.WTestDbEntry.COLUMN_LOCAL + "," + WTestDbContract.WTestDbEntry.COLUMN_VALUE + " FROM " +
                        WTestDbContract.WTestDbEntry.TABLE_NAME + " " + whereClause + " " + sortOrder + " LIMIT 15 OFFSET " + ints[0], null);
            } catch (SQLiteException exception) {
                exception.printStackTrace();
                hasError = true;

                //Wrong query, no need to alert the user since it only happens if user is messing with search and trying to be naughty
                return new ArrayList<>();
            }

            //Fetch the data from the cursor
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

            //If we have no items, check if there was an error and check if we should load from web
            //If it was a bad filter, don't load nothing at all and inform the Activity/fragment that there's no data
            if (items.size() == 0) {
                if (!hasError) {
                    if (filter == null || filter.getValue().isEmpty()) {
                        if (taskWeb == null || !taskWeb.isRunning) {
                            forceLoadPostalCodes(true);
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
            } else {
                if (listener != null) {
                    listener.onDataLoaded(items);
                }
            }
            isRunning = false;
        }
    }


    public class GetPostalCodesFromWeb extends AsyncTask<String, Integer, List<WTestDbContract.DbItem>> {

        HttpURLConnection urlConnection;

        boolean isFirstStart = false;

        boolean isRunning = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Inform the activity/fragment that we're about to do magic
            if (listener != null) {
                listener.onPreLoadData(true);
            }
            isRunning = true;
        }

        @Override
        protected List<WTestDbContract.DbItem> doInBackground(String... strings) {
            synchronized (threadLock) {
                if (counter != 0) {
                    return new ArrayList<>();
                }
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

                    //Open the connection
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    //Get the connection inputStream
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    //With a bit more time, I would validate the HTTP codes correctly of course

                    progress = 10;
                    publishProgress(progress);

                    //Read the connection data
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
                        uiThread.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onExceptionData();
                            }
                        });
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
                    //Obtain the path to the postal code file from the received JSON Object
                    JSONObject datapackage = new JSONObject(result.toString());

                    JSONArray arrayData = datapackage.getJSONArray("resources");

                    for (int i = 0; i < arrayData.length(); ++i) {
                        if (arrayData.getJSONObject(i).getString("name").equals(RESOURCE_NAME)) {
                            pathToPostalCodes = arrayData.getJSONObject(i).getString("path");
                            break;
                        }
                    }
                    progress = 35;
                    publishProgress(progress);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        uiThread.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onExceptionData();
                            }
                        });
                    }
                    isRunning = false;
                    //STOP METHOD EXECUTION HERE, WRONG TYPE OF DATA
                    return null;
                }

                ArrayList<WTestDbContract.DbItem> postalCodes = new ArrayList<>();

                //Check if we have a path, if we do have a path, connect once again to the repository and download the file that contains all postal codes
                //It's a big file, to be expected
                if (!pathToPostalCodes.isEmpty()) {
                    StringBuilder realResult = new StringBuilder();

                    urlString = strings[0].replace("github.com", "raw.githubusercontent.com") + "/master/" + pathToPostalCodes;

                    InputStream in;
                    ArrayList<String> lines = new ArrayList<>();
                    try {

                        //Open the connection
                        URL url = new URL(urlString);
                        urlConnection = (HttpURLConnection) url.openConnection();

                        progress = 40;
                        publishProgress(progress);
                        in = new BufferedInputStream(urlConnection.getInputStream());

                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (listener != null) {
                            uiThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onExceptionData();
                                }
                            });
                        }
                        isRunning = false;
                        //STOP METHOD EXECUTION HERE, WRONG PATH
                        return null;
                    } finally {
                        //Always disconnect from the server;
                        urlConnection.disconnect();
                    }

                    updateDatabaseWithNewData(lines);

                    postalCodes.clear();

                }

                return postalCodes;
            }
        }
    }

    //Always close the db connection
    //We're running it in this method as a last resort
    @Override
    protected void finalize() throws Throwable {
        try {
            getDbHelper().close();
        } finally {
            super.finalize();
        }
    }

    //This interface is needed to send the events to the fragment/activity that subscribes to them
    //Each method name is self-explanatory
    public interface PostalCodeEventListener {

        void onDataLoaded(List<WTestDbContract.DbItem> postalCodes);

        void onPreLoadData(boolean isCaching);

        void onExceptionData();

        void onProgressUpdate(int progress, int itemCount);

    }
}

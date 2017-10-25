package com.emanuel.amaris.wtest.wtest.WebCode;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by emanuel on 25-10-2017.
 */

public class PostalCodeManager {

    private final String GITHUB_URL = "https://github.com/centraldedados/codigos_postais";

    private final String RESOURCE_NAME = "codigos_postais";

    public PostalCodeManager() {
        //TODO: Inicializar a base de dados, obter dados da mesma. Caso nao haja dados, ir buscar ao serviço
    }


    public void updateDataFromServer() {

    }


    public class GetPostalCodes extends AsyncTask<String, Integer, String[]> {

        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... strings) {

            //TODO: Publish progress

            /**
             *I'm doing this in a certain dynamic way, I'm accessing the datapackage on the github repository so I can get the path to
             * the .csv file that contains the postal codes.
             *
             * I could do it the direct way, but if the owner of the github changes the path or name of file, the app would stop working,
             * if we code it like this, at least until the owner of the github repository changes the name of the datapackage file, the app will find it's way
             */

            //Use stringbuilders to increase performance, since Java takes longer with simple "Add" operations to a String
            StringBuilder result = new StringBuilder();

            //Get the raw content, we don't need all the extra html stuff, only the file
            String urlString = strings[0].replace("github.com", "raw.githubusercontent.com") + "/master/datapackage.json";

            try {

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            String pathToPostalCodes = "";

            try {
                JSONObject datapackage = new JSONObject(result.toString());

                JSONArray arrayData = datapackage.getJSONArray("resources");

                for (int i = 0; i < arrayData.length(); ++i) {
                    if (arrayData.getJSONObject(i).getString("name") == RESOURCE_NAME) {
                        pathToPostalCodes = arrayData.getJSONObject(i).getString("path");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //TODO: Inform the user there has been an error
            }

            ArrayList<String> postalCodes = new ArrayList<>();

            if (!pathToPostalCodes.isEmpty()) {
                StringBuilder realResult = new StringBuilder();

                urlString = strings[0].replace("github.com", "raw.githubusercontent.com") + "/master/" + pathToPostalCodes;

                try {
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        realResult.append(line);
                    }

                    String[] rows = realResult.toString().split("\n");

                    for (String row : rows) {
                        String[] columns = realResult.toString().split(",");

                        for (int i = 0; i < columns.length; ++i) {
                            if (columns[i].matches("[0-9]{3}") && columns[i + 1].matches("[0-9]{4}")) {
                                //we check if there's the postal code extension next to the normal postal code
                                postalCodes.add(columns[i] + "-" + columns[i + 1]);
                                i++;
                            } else if (columns[i].matches("[0-9]{4}-[0-9]{3}")) {
                                //Here, the column fullfils the regex, which means it has a full postal code
                                postalCodes.add(columns[i]);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            }

            return (String[]) postalCodes.toArray();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            //TODO: Obtain the Postal Codes and parse them to the RecyclerView
        }
    }
}

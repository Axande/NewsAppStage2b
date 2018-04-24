package com.example.andrei.newsappstage2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrei on 13.04.2018.
 * <p>
 * Loader for an async task to an API
 */

public class NewsLoader extends AsyncTaskLoader<ArrayList<News>> {

    private static final String NEWS_REQUEST_URL = "https://content.guardianapis.com/search";

    public NewsLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<News> loadInBackground() {
        String TAG = MainActivity.class.getSimpleName();            //TAG used for logs

        ArrayList<News> loadedNews = new ArrayList<>();             //an array for retrieved news
        HttpHandler sh = new HttpHandler();

        //request to URL
        String jsonString;
        try {
            jsonString = sh.makeHttpRequest(createUrl());   //connect to the API's URL
        } catch (IOException e) {                           //there was a problem retrieving data
            Log.e(TAG, "Failed to get string of data");
            return null;
        }

        if (jsonString != null) {                           //there is a string of data
            try {
                final JSONObject jsonObj = new JSONObject(jsonString); //Convert the string to Json object
                final JSONArray rawNews = jsonObj.getJSONObject("response").getJSONArray("results"); //get the json node with results
                JSONArray tempArray;
                JSONObject tempObj;

                //save all nodes into News objects
                for (int i = 0; i < rawNews.length(); i++) {
                    JSONObject c = rawNews.getJSONObject(i);
                    News oneEntry = new News();

                    oneEntry.setDate(formatDate(c.optString("webPublicationDate"), 1));
                    oneEntry.setTime(formatDate(c.optString("webPublicationDate"), 2));
                    oneEntry.setTitle(c.optString("webTitle"));
                    oneEntry.setWebUrl(c.optString("webUrl"));
                    oneEntry.setCategory(c.optString("sectionName"));

                    //get the author from the requested tags
                    tempArray = c.getJSONArray("tags");
                    if (tempArray.length() != 0) {
                        tempObj = tempArray.getJSONObject(0);
                        oneEntry.setAuthor(tempObj.optString("webTitle"));
                    }
                    loadedNews.add(oneEntry);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
        }
        return loadedNews;
    }

    /**
     * @param s    input string to be formated
     * @param part which part of the string to be returned
     * @return either date or time from a given string s
     */
    private String formatDate(String s, int part) {
        String[] parts = s.split("T");                  //the date part
        parts[1] = parts[1].substring(0, parts[1].length() - 1); //cut Z from the time part
        return parts[part - 1];
    }

    /**
     * Create an Url for API connection
     *
     * @return an URL object
     */
    private URL createUrl() {

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(NEWS_REQUEST_URL);
        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        //get the context
        Context context = getContext();

        //get all the preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //get individual values for all preferences
        String nrPages = preferences.getString(context.getString(R.string.settings_page_size_to_load), "");
        String containingWord = preferences.getString(context.getString(R.string.settings_find), "");
        String category = preferences.getString(context.getString(R.string.settings_category), "");
        String orderBy = preferences.getString(context.getString(R.string.settings_order), "");

        // Append query parameter and its value. For example, the `format=geojson`
        uriBuilder.appendQueryParameter("q", containingWord);
        uriBuilder.appendQueryParameter("section", category);
        uriBuilder.appendQueryParameter("page-size", nrPages);
        uriBuilder.appendQueryParameter("api-key", "f410ec8e-d4be-419c-b77d-dfb3d818a0d7");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("order-by", orderBy);

        URL url;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException exception) {
            return null;
        }
        return url;
    }
}

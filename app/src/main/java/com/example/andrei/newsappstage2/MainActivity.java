package com.example.andrei.newsappstage2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<News>>{

    private int currentDisplay = 1; //which part of the app to display: 1 = help, 200 = list of items, 404 = error

    SharedPreferences.Editor editor;
    public ArrayList<News> allNewsList = new ArrayList<>();

    private ConnectivityManager cm;         //object to manage connectivity
    private NetworkInfo activeNetwork;

    //all layouts that change visibility
    private LinearLayout layout_error;
    private LinearLayout layout_info;
    private ListView layout_list;
    private TextView layout_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the preferences
        editor = getPreferences(MODE_PRIVATE).edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton btnRefresh = (FloatingActionButton) findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentDisplay != 200) updateScreen(200, "");

                allNewsList.clear();

                activeNetwork = cm.getActiveNetworkInfo();
                //check if there is an internet connection or not
                if (!(activeNetwork != null && activeNetwork.isConnectedOrConnecting())) {
                    updateScreen(404, "No internet access! :(");
                } else {
                    getSupportLoaderManager().initLoader(1, null, MainActivity.this).forceLoad();
                }
            }
        });

        //find layouts
        layout_info = findViewById(R.id.info_wrapper);
        layout_error = findViewById(R.id.error_wrapper);
        layout_list = findViewById(R.id.news_feed);
        layout_loading = findViewById(R.id.loading_bar);

        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<ArrayList<News>> onCreateLoader(int id, Bundle args) {
        triggerLoadingBar(1);
        return new NewsLoader(MainActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<News>> loader, ArrayList<News> data) {
        triggerLoadingBar(0);
        if (data.size() == 0) {
            //an error occurred
            updateScreen(404, "Could not correctly parse data. :(");
        } else {
            allNewsList.addAll(data);
            refreshListOfItems();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<News>> loader) {
    }

    /**
     * refresh the list displayed on the main screen
     */
    private void refreshListOfItems() {
        if (allNewsList != null) {
            NewsAdapter newsAdapter = new NewsAdapter(this, allNewsList);

            ListView newsFeed = findViewById(R.id.news_feed);
            newsFeed.setAdapter(newsAdapter);

            //add a click listener for the list of items and launch the intent
            newsFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                    News item = (News) parent.getItemAtPosition(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(item.getWebUrl()));
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * open/close the loading bar
     */
    private void triggerLoadingBar(int theCase) {
        if(theCase == 1)
            layout_loading.setVisibility(View.VISIBLE);
        else
            layout_loading.setVisibility(View.GONE);
    }

    private void updateScreen(int val, String message) {
        currentDisplay = val;

        //close all tabs
        layout_info.setVisibility(View.GONE);
        layout_error.setVisibility(View.GONE);
        layout_list.setVisibility(View.GONE);

        //write message to error tab in case it is present
        if (!message.equals("")) {
            TextView tw = findViewById(R.id.error_text);
            tw.setText(message);
        }
        //open the right tab
        if (val == 404) layout_error.setVisibility(View.VISIBLE);
        else if (val == 200) layout_list.setVisibility(View.VISIBLE);
        else layout_info.setVisibility(View.VISIBLE);
    }
}



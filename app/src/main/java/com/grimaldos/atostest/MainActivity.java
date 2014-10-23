package com.grimaldos.atostest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.grimaldos.atostest.util.CheckConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


public class MainActivity extends Activity implements SearchView.OnQueryTextListener {

    private static String url = "http://t21services.herokuapp.com/points";
    ListView list;
    ArrayList<HashMap<String, String>> oslist;
    private static SimpleAdapter adapter;

    // Key tags from the JSON message
    private static final String TAG_LIST = "list";
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_GEO = "geocoordinates";
    JSONArray jArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        oslist = new ArrayList<HashMap<String, String>>();

        // Download the info from the url and fills the ListView
        new JSONparser().execute();
    }

    protected void onRestart() {
        // Activity restart (so it doesn't need to download the info again)
        super.onRestart();
        Log.d("RESTART", "Restarting MainActivity");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Configure the Search functionality
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Refresh functionality
            case R.id.action_refresh:
                new JSONparser().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    private class JSONparser extends AsyncTask<Void, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            // Set and show the "Loading" dialog
            this.dialog.setMessage("Getting data...");
            this.dialog.setIndeterminate(false);
            this.dialog.setCancelable(true);
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // Check Internet connection
            JSONObject data = null;
            ConnectivityManager conMgr = (ConnectivityManager) MainActivity.this.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            Log.d("QUESTION", "Should we try to download the data?");

            if (CheckConnection.checkConnection(conMgr)) {
                // If Internet connection is OK, get the data from the url
                Log.d("ANSWER", "YES");
                data = new com.grimaldos.atostest.util.JSONparser().getJSONfromURL(url);
            } else {
                // If Internet connection is not working, data keeps being null
                Log.d("ANSWER", "NO");
                Log.d("NETWORK_ERROR", "No internet connection");
            }
            return data;
        }

        protected void onPostExecute(JSONObject data) {
            dialog.dismiss();
            try {
                // If Internet connection wasn't working, warn the user
                if (data == null)
                    throw new Exception("Server unreachable. " +
                        "Check your internet connection and try again.");

                // Get JSON Array from URL
                jArray = data.getJSONArray(TAG_LIST);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject c = jArray.getJSONObject(i);

                    // Store JSON items in corresponding variables
                    String id = c.getString(TAG_ID);
                    String title = c.getString(TAG_TITLE);
                    //String geo = c.getString(TAG_GEO);

                    // Add value HashMap (key, value)
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TAG_ID, id);
                    map.put(TAG_TITLE, title);
                    //map.put(TAG_GEO, geo);

                    // Add map to ArrayList
                    oslist.add(map);

                    // Fill ListView with data
                    list = (ListView) findViewById(R.id.listView);
                    adapter = new SimpleAdapter(MainActivity.this, oslist, R.layout.item,
                            new String[]{TAG_TITLE}, new int[]{R.id.title});
                    list.setAdapter(adapter);

                    // Set listener onClick -> Show item details
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            // Get id from the actual item clicked
                            StringTokenizer itemInfo = new StringTokenizer(adapter.getItem(position).toString(), "=,");
                            itemInfo.nextToken();

                            // Start new activity
                            Intent detail = new Intent(view.getContext(), Detail.class);
                            detail.putExtra("id", itemInfo.nextToken());
                            detail.putExtra("url", url);
                            startActivity(detail);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
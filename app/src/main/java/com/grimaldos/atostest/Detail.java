package com.grimaldos.atostest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.grimaldos.atostest.db.PointOfInterestDBOpenHelper;
import com.grimaldos.atostest.util.CheckConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Detail extends Activity {

    // Key tags from the JSON message
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_TRANSPORT = "transport";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_GEO = "geocoordinates";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_PHONE = "phone";
    private static String url = null;
    private static SQLiteOpenHelper helper = null;
    TextView titleView, addressView, transportView, emailView, geoView, descriptionView, phoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);

        // Get id and setting detail url
        String id = getIntent().getStringExtra("id");
        url = getIntent().getStringExtra("url");
        url = url + "/" + id;

        titleView = (TextView) findViewById(R.id.title);
        addressView = (TextView) findViewById(R.id.address);
        transportView = (TextView) findViewById(R.id.transport);
        emailView = (TextView) findViewById(R.id.email);
        geoView = (TextView) findViewById(R.id.geo);
        descriptionView = (TextView) findViewById(R.id.description);
        phoneView = (TextView) findViewById(R.id.phone);

        //Check whether the PoI already exists in the DB
        helper = new PointOfInterestDBOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PointOfInterest WHERE ID = " + id, null);

        if(cursor.getCount() <= 0) {
            // If it doesn't exist in the DB, get the info from the url
            new JSONparser().execute();
        } else {
            // If it exists in the DB, get the info from there
            cursor.moveToNext();
            Log.d("TITLE", cursor.getString(cursor.getColumnIndex("TITLE")));
            titleView.setText(cursor.getString(cursor.getColumnIndex("TITLE")));
            addressView.setText(cursor.getString(cursor.getColumnIndex("ADDRESS")));
            transportView.setText(cursor.getString(cursor.getColumnIndex("TRANSPORT")));
            emailView.setText(cursor.getString(cursor.getColumnIndex("EMAIL")));
            geoView.setText(cursor.getString(cursor.getColumnIndex("GEO")));
            descriptionView.setText(cursor.getString(cursor.getColumnIndex("DESCRIPTION")));
            phoneView.setText(cursor.getString(cursor.getColumnIndex("PHONE")));
        }
        cursor.close();
        db.close();

        // If transport or email are empty, reduce the amount of space used by their TextViews
        // If not, set them to "WRAP_CONTENT"
        if(transportView.getText().toString().equals("")) {
            ViewGroup.LayoutParams params = transportView.getLayoutParams();
            params.height = 0;
        } else {
            ViewGroup.LayoutParams params = transportView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        if(emailView.getText().toString().equals("")) {
            ViewGroup.LayoutParams params = emailView.getLayoutParams();
            params.height = 0;
        } else {
            ViewGroup.LayoutParams params = emailView.getLayoutParams();
            params.height = params.WRAP_CONTENT;
        }

        // Set addressView handler, onClick -> open the geocoordinates with gmaps (or any other app)
        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ADDRESS", "Clicked");
                Log.d("GEO:", geoView.getText().toString());
                try {
                    Intent viewMap = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + geoView.getText()
                            + "?q=" + geoView.getText() + "(" + titleView.getText() + ")"));
                    startActivity(viewMap);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /*
        // Set emailView handler, onClick -> open gmail with the email address as destination
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[] {emailView.getText().toString()});
                //Intent email = new Intent(Intent.ACTION_VIEW, Uri.parse(emailView.getText().toString()));
                startActivity(email);
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            // Refresh functionality
            new JSONparser().execute();
            return true;
        } /*else if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private class JSONparser extends AsyncTask<Void, Void, JSONObject> {
        private final ProgressDialog dialog = new ProgressDialog(Detail.this);

        protected void onPreExecute() {
            // Set and show the "Loading" dialog
            this.dialog.setMessage("Getting item data...");
            this.dialog.setIndeterminate(false);
            this.dialog.setCancelable(true);
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // Check Internet connection
            JSONObject data = null;
            ConnectivityManager conMgr = (ConnectivityManager) Detail.this.
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

                // Store JSON items in corresponding variables
                String id = data.getString(TAG_ID);
                Log.d("itemID", id);

                String title = data.getString(TAG_TITLE);
                Log.d("itemTitle", title);

                String address = data.getString(TAG_ADDRESS);
                Log.d("itemAddress", address);

                // Check if transport is not a valid field
                String transport = "";
                if (!data.getString(TAG_TRANSPORT).toLowerCase().equals("null")
                        && !data.getString(TAG_TRANSPORT).toLowerCase().equals("undefined")) {
                    transport = data.getString(TAG_TRANSPORT);
                }
                Log.d("itemTransport", transport);

                // Check if email is not a valid field
                String email = "";
                if (!data.getString(TAG_EMAIL).toLowerCase().equals("null")
                        && !data.getString(TAG_EMAIL).toLowerCase().equals("undefined")) {
                    email = data.getString(TAG_EMAIL);
                }
                Log.d("itemEmail", email);

                final String geo = data.getString(TAG_GEO);
                Log.d("itemGeo", geo);

                String description = data.getString(TAG_DESCRIPTION);
                Log.d("itemDescription", description);

                // Check if phone is not a valid field
                String phone = "";
                if (!data.getString(TAG_PHONE).toLowerCase().equals("null")
                        && !data.getString(TAG_PHONE).toLowerCase().equals("undefined")) {
                    phone = data.getString(TAG_PHONE);
                }
                Log.d("itemPhone", phone);

                // Add value HashMap (key, value)
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TAG_ID, id);
                map.put(TAG_TITLE, title);
                map.put(TAG_ADDRESS, address);
                map.put(TAG_TRANSPORT, transport);
                map.put(TAG_EMAIL, email);
                map.put(TAG_GEO, geo);
                map.put(TAG_DESCRIPTION, description);
                map.put(TAG_PHONE, phone);

                titleView = (TextView) findViewById(R.id.title);
                addressView = (TextView) findViewById(R.id.address);
                transportView = (TextView) findViewById(R.id.transport);
                emailView = (TextView) findViewById(R.id.email);
                geoView = (TextView) findViewById(R.id.geo);
                descriptionView = (TextView) findViewById(R.id.description);
                phoneView = (TextView) findViewById(R.id.phone);

                // Fill the TextViews
                titleView.setText(title);
                addressView.setText(address);
                transportView.setText(transport);
                emailView.setText(email);
                geoView.setText(geo);
                descriptionView.setText(description);
                phoneView.setText(phone);

                // Update or create new row in the DB with the info
                SQLiteDatabase db = helper.getReadableDatabase();

                try {
                    db.execSQL("DELETE FROM PointOfInterest WHERE ID = ?;", new String[]{id});
                } catch (Exception e) {
                    e.printStackTrace();
                }

                db.execSQL("INSERT INTO PointOfInterest (ID, TITLE, ADDRESS, TRANSPORT, EMAIL, " +
                                "GEO, DESCRIPTION, PHONE) values (?, ?, ?, ?, ?, ?, ?, ?);",
                        new String[]{
                                id,
                                title,
                                address,
                                transport,
                                email,
                                geo,
                                description,
                                phone});
                db.close();

            } catch(JSONException e) {
                e.printStackTrace();
            } catch(Exception e) {
                Toast.makeText(Detail.this, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}

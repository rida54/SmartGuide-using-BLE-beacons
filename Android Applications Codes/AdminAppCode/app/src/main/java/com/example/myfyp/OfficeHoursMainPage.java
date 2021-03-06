package com.example.myfyp;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfficeHoursMainPage extends AppCompatActivity {

    ImageButton buttonR;
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private AlertDialog.Builder build;

    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;



    ArrayList<HashMap<String, String>> roomsList;
    List<String> IdList = new ArrayList<>();
    SimpleAdapter simpleAdapter;
    SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_hours_main_page);

        getSupportActionBar().setTitle("Office Hours");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        buttonR = findViewById(R.id.iBtnAdd);

        buttonR.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent(getApplicationContext(),  AddOfficeHours.class);
                startActivity(i);
            }
        });
        roomsList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.lvTimings);
        new OfficeHoursMainPage.GetRooms().execute();
        registerForContextMenu(lv);
        sv=(SearchView) findViewById(R.id.searchviewTm);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {

                simpleAdapter.getFilter().filter(text);

                return false;
            }
        });
        // ListView on item selected listener.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // TODO Auto-generated method stub

                //invoking AlertDialog box
                build = new androidx.appcompat.app.AlertDialog.Builder(OfficeHoursMainPage.this);
                build.setTitle("Update/Delete OfficeHours ");
                build.setMessage("Do you want to delete/update the record?");

                //user select UPDATE
                build.setNegativeButton("UPDATE",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent intent = new Intent(OfficeHoursMainPage.this, ViewOfficeHours.class);

                                // Sending ListView clicked value using intent.
                                intent.putExtra("ListViewValue2", IdList.get(position).toString());

                                startActivity(intent);

                                //Finishing current activity after open next activity.
                                finish();
                            }
                        });

                //user select DELETE
                build.setPositiveButton("DELETE",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,int which) {
                                String abc = "";
                                String rid =   IdList.get(position);

                                deleteOfficeHours(Integer.parseInt(rid));
                                // Toast.makeText(getApplicationContext() , "delete successfully", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        });//end DELETE
                AlertDialog alert = build.create();
                alert.show();

            }
        });

    }

    private void deleteOfficeHours(int id) {
        OfficeHoursMainPage.PerformNetworkRequest request = new OfficeHoursMainPage.PerformNetworkRequest(URLs.URL_DELETE_TIMINGS + id, null, CODE_GET_REQUEST);
        request.execute();
    }
    private class GetRooms extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(OfficeHoursMainPage.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(URLs.URL_READ_TIMINGS);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray roomsArray = jsonObj.getJSONArray("officetimings");

                    // looping through All roomsArray
                    for (int i = 0; i < roomsArray.length(); i++) {
                        JSONObject c = roomsArray.getJSONObject(i);
                        String stfmemName = c.getString("stfMemName");

                        String tmid = c.getString("tmid");

                        // Adding tmId TO IdList Array.
                        IdList.add(tmid);


                        // tmp hash map for single contact
                        HashMap<String, String> room = new HashMap<>();

                        // adding each child node to HashMap key => value
                        room.put("stfMemName",stfmemName);



                        // adding contact to contact list
                        roomsList.add(room);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            // ListAdapter
            //    ListAdapter    adapter = new SimpleAdapter(
            //     Testing.this, roomsList,
            //  R.layout.list_item, new String[]{ "RoomNo"}, new int[]{R.id.lblroomno});

             simpleAdapter=new SimpleAdapter(OfficeHoursMainPage.this,roomsList,R.layout.list_item, new String[]{"stfMemName"},new int[]{R.id.lblroomno});
            lv.setAdapter(simpleAdapter);
            //  lv.setAdapter(adapter);
        }

    }
    private void refreshRoomList()  {
        roomsList.clear();
        new OfficeHoursMainPage.GetRooms().execute();

    }
    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {
        String url;
        HashMap<String, String> params;
        int requestCode;

        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OfficeHoursMainPage.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (pDialog.isShowing())
                pDialog.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    //refreshHeroList(object.getJSONArray("heroes"));
                    refreshRoomList();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);


            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }

}


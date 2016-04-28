package com.example.hlt04.pyquiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.hlt04.pyquiz.helper.AlertDialogManager;
import com.example.hlt04.pyquiz.helper.ConnectionDetector;

public class AlbumsActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    //JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> albumsList;

    // albums JSONArray
    JSONArray albums = null;

    // albums JSON url
    private static final String URL_ALBUMS = /*"http://api.androidhive.info/songs/albums.php";*/"http://adapt2.sis.pitt.edu/aggregate/GetContentLevels?usr=adl01&grp=ADL&sid=generate_a_session_id&cid=23&mod=all&models=0";

    // ALL JSON node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_SONGS_COUNT = "songs_count";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(AlbumsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Hashmap for ListView
        albumsList = new ArrayList<HashMap<String, String>>();

        // Loading Albums JSON in Background Thread
        new LoadAlbums().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview item click listener
         * TrackListActivity will be lauched by passing album id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // on selecting a single album
                // TrackListActivity will be launched to show tracks inside the album
                Intent i = new Intent(getApplicationContext(), TrackListActivity.class);

                // send album id to tracklist activity to get list of songs under that album
                String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
                i.putExtra("album_id", album_id);

                startActivity(i);
            }
        });
    }

    /**
     * Background Async Task to Load all Albums by making http request
     * */
    class LoadAlbums extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AlbumsActivity.this);
            pDialog.setMessage("Listing Albums ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Albums JSON
         * */
        protected String doInBackground(String... args) {
            String json = "";

            URL url = null;
            try {
                url = new URL(URL_ALBUMS);
                Log.d("url lalala: ", url.toString());
                URLConnection uc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        uc.getInputStream()));
                String line = "";
                while((line = in.readLine()) != null){
                    json += line;
                }
                in.close();
            } catch (MalformedURLException e) {
                Log.d("MalformedURL lala ", e.toString());

            } catch (IOException io) {
                Log.d("ioexception lala ", io.toString());
            }

            Log.d("Albums JSON lala ", "> " + json);

            //json = "[{\"id\":1,\"name\":\"127 Hours\",\"songs_count\":14},{\"id\":2,\"name\":\"Adele 21\",\"songs_count\":11},{\"id\":3,\"name\":\"Lana Del Rey - Born to Die\",\"songs_count\":12},{\"id\":4,\"name\":\"Once\",\"songs_count\":13},{\"id\":5,\"name\":\"Away We Go\",\"songs_count\":13},{\"id\":6,\"name\":\"Eminem Curtain Call\",\"songs_count\":14},{\"id\":7,\"name\":\"Bad Meets Evil Eminem\",\"songs_count\":11},{\"id\":8,\"name\":\"Safe Trip Home\",\"songs_count\":11},{\"id\":9,\"name\":\"No Angel\",\"songs_count\":12}]";

            // Check your log cat for JSON reponse
            Log.d("substring lala", "hi" + json.substring(98190) + "hi");
            Log.d("json length", "" + json.length());
            json = json.replace("function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); }", "hi");
            Log.d("substring lala", "hi" + json.substring(98190) + "hi");
            Log.d("json length", "" + json.length());

            try {
                albums = new JSONObject(json).getJSONArray("topics");

                if (albums != null) {
                    // looping through All albums
                    Log.d("within json loop", "in loop lala");
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject c = albums.getJSONObject(i);

                        // Storing each json item values in variable
                        String id = c.getString("id");
                        String name = c.getString("name");
                        String songs_count = c.getString("difficulty");

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_SONGS_COUNT, songs_count);

                        // adding HashList to ArrayList
                        albumsList.add(map);
                    }
                }else{
                    Log.d("Albums: ", "null");
                }

            } catch (JSONException e) {
                Log.d("parse lala", e.toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all albums
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AlbumsActivity.this, albumsList,
                            R.layout.list_item_albums, new String[] { TAG_ID,
                            TAG_NAME, TAG_SONGS_COUNT }, new int[] {
                            R.id.album_id, R.id.album_name, R.id.songs_count });

                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
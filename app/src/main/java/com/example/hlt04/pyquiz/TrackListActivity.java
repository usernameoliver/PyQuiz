package com.example.hlt04.pyquiz;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.hlt04.pyquiz.helper.AlertDialogManager;
import com.example.hlt04.pyquiz.helper.ConnectionDetector;
import com.example.hlt04.pyquiz.helper.HttpGet;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrackListActivity extends ListActivity {
    private ArrayList<String> durationString = new ArrayList<String>();
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    //Creating http get object
    HttpGet hg = new HttpGet();

    ArrayList<HashMap<String, String>> tracksList;

    // tracks JSONArray
    JSONObject albums = null;
    JSONObject state = null;

    // Album id
    String userId, album_index, album_id, album_name;

    private static String URL_USER = "";//


    // ALL JSON node names
    private static final String TAG_SONGS = "songs";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ALBUM = "album";
    private static final String TAG_DURATION = "duration";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TrackListActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        userId = i.getStringExtra("userId");
        album_index = i.getStringExtra("album_index");
        album_name = i.getStringExtra("album_name");

        URL_USER = "http://adapt2.sis.pitt.edu/aggregate/GetContentLevels?usr=" + userId + "&grp=ADL&sid=abcd&cid=23&mod=user";

        try {
            albums = new JSONObject(i.getStringExtra("albums"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Hashmap for ListView
        tracksList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), SingleTrackActivity.class);

                // to get song information
                // both album id and song is needed
                String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
                String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();
                String track_no = ((TextView) view.findViewById(R.id.track_no)).getText().toString();
                //Toast.makeText(getApplicationContext(), "Album Id: " + album_id  + ", Song Id: " + song_id, Toast.LENGTH_SHORT).show();

                i.putExtra("album_id", album_id + "&usr=" + userId + "&grp=ADL&sid=abcd&cid=23");
                i.putExtra("song_id", song_id);
                i.putExtra("track_no", track_no);
                i.putExtra("userId", userId);//such as adl04
                startActivity(i);
            }
        });

    }




    /**
     * Background Async Task to Load all tracks under one album
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TrackListActivity.this);
            pDialog.setMessage("Loading Questions ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        protected String doInBackground(String... args) {
            String jsonState = hg.getJson(URL_USER);
            try {
                JSONObject jsState = new JSONObject(jsonState);
                state = jsState.getJSONObject("learner").getJSONObject("state");

                if (albums != null) {
                    album_id = albums.getString("id");//actually album_id string
                    JSONArray album_question = albums.getJSONObject("activities").getJSONArray("qp");
                    if (albums != null) {
                        // looping through All songs
                        for (int i = 0; i < album_question.length(); i++) {
                            JSONObject c = album_question.getJSONObject(i);

                            // Storing each json item in variable
                            String song_url = c.getString("url");
                            String song_id = c.getString("id");
                            // track no - increment i value
                            String track_no = String.valueOf(i + 1);
                            String name = c.getString("name");
                            String duration = state.getJSONObject("activities").getJSONObject(album_id).getJSONObject("qp").getJSONObject(song_id).getJSONObject("values").getString("p");

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("album_id", song_url);//song url
                            map.put(TAG_ID, song_id);// song id
                            map.put("track_no", track_no + "");//song index starting from 1
                            map.put(TAG_NAME, name);//song name
                            map.put(TAG_DURATION, duration);//song progress
                            durationString.add(duration);

                            // adding HashList to ArrayList
                            tracksList.add(map);
                        }
                    } else {
                        Log.d("Albums: ", "null");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new TrackListAdapter(
                            TrackListActivity.this, tracksList,
                            R.layout.list_item_tracks, new String[] { "album_id", TAG_ID, "track_no",
                            TAG_NAME, TAG_DURATION }, new int[] {
                            R.id.album_id, R.id.song_id, R.id.track_no, R.id.album_name, R.id.song_duration, },durationString);
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(album_name);
                }
            });

        }

        public class TrackListAdapter extends SimpleAdapter {
            private ArrayList<String> gradeStringHere;
            private int[] colors = new int[] { 0x30ffffff, 0x30f2ffcc,0x30dfff80,0x30ccff33,0x30bfff00,0x3099cc00,0x3000e600 };

            public TrackListAdapter(Context context, ArrayList<HashMap<String, String>> items, int resource, String[] from, int[] to,ArrayList<String> gradeStringnew) {
                super(context, items, resource, from, to);
                gradeStringHere = gradeStringnew;
                //Log.d("String",gradeStringnew + "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");


            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int colorPos;
                //TextView currentAlbumnSongsCount =(TextView) findViewById(R.id.songs_count);
                //gradeString = currentAlbumnSongsCount.getText().toString();
                //float grade =
                //Log.d("String",gradeStringHere.get(position) + "______________________________________________________________________");
                float grade = Float.parseFloat(gradeStringHere.get(position));
                Log.d("String",grade + "______________________________________________________________________");
                if (grade < 0.21)
                    colorPos = 0;
                else {
                    if (grade < 0.41)
                        colorPos = 1;
                    else {
                        if (grade < 0.61)
                            colorPos = 2;
                        else {
                            if (grade < 0.81)
                                colorPos = 4;
                            else
                                colorPos = 6;
                        }
                    }
                }


                view.setBackgroundColor(colors[colorPos]);
                return view;
            }
        }

    }
}
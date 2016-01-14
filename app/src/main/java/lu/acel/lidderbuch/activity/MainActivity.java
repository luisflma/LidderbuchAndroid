package lu.acel.lidderbuch.activity;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.Settings;
import lu.acel.lidderbuch.design.SongbookAdapter;
import lu.acel.lidderbuch.model.LBSong;
import lu.acel.lidderbuch.model.LBSongbook;
import lu.acel.lidderbuch.network.LBFetchSongs;
import lu.acel.lidderbuch.network.LBFetchSongsCallback;

public class MainActivity extends AppCompatActivity {

    private LBSongbook songbook;

    private ListView songbookListview;
    private SongbookAdapter songbookAdapter;
    private View footerView;

    // Fetch songs
    Handler handler = new Handler();

    private Runnable runnableSongs = new Runnable() {
        @Override
        public void run() {

            //String arg0 = String.valueOf(songbook.updateTime().getTime() / 1000);
            String arg0 = String.valueOf(1440754883); // for test
            String url = MessageFormat.format(Settings.SONGBOOK_API, arg0);
            Log.i("MainActivity", "url:" + url);
            new FetchSongsTask().execute(url);

            handler.postDelayed(runnableSongs, 30000000); //300000
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list_selection);

        footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);

        songbookListview = (ListView) findViewById(R.id.singleChoiceListView);
        songbookListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleClickOnSong(view);
            }
        });

        songbook = new LBSongbook(this);

        handler.post(runnableSongs);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshFooter(songbook.updateTime());

        if(songbook.getSongs() != null) {
            songbookAdapter = new SongbookAdapter(this, R.layout.list_item_songbook, songbook.getSongs());
            songbookListview.addFooterView(footerView);
            songbookListview.setAdapter(songbookAdapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(songbook.isHasChangesToSave()) {
            songbook.save(this);
        }
    }

    private void handleClickOnSong(View view) {

    }

    private void refreshFooter(Date updateTime) {
        if(updateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String dateFormatted = sdf.format(updateTime);
            TextView updateTimeTv = (TextView) footerView.findViewById(R.id.tvUpdateTime);
            updateTimeTv.setText(getString(R.string.update_time) + " " + dateFormatted);
        }
    }

    private void refreshSongsList(ArrayList<LBSong> songs) {
        songbook.integrateSongs(songs, false);
        songbookAdapter.notifyDataSetChanged();
        refreshFooter(songbook.updateTime());
    }

    class FetchSongsTask extends AsyncTask<String, Void, Void> {

        JSONArray songsArray;
        boolean refreshSongs;

        protected Void doInBackground(String... urls) {
            songsArray = LBFetchSongs.requestWebService(urls[0]);

            if(songsArray != null && songsArray.length() > 0)
                refreshSongs = true;

            return null;
        }

        protected void onPostExecute(Void vo) {

            if(refreshSongs) {
                ArrayList<LBSong> songs = LBSongbook.songsWithData(songsArray.toString());

                refreshSongsList(songs);
            }
        }
    }

}


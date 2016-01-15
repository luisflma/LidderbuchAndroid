package lu.acel.lidderbuch.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.Settings;
import lu.acel.lidderbuch.design.SongbookAdapter;
import lu.acel.lidderbuch.model.LBSong;
import lu.acel.lidderbuch.model.LBSongbook;
import lu.acel.lidderbuch.network.LBFetchSongs;

public class MainActivity extends AppCompatActivity {

    private LBSongbook songbook;
    private SearchView toolbarSearchButton;

    private ListView songbookListview;
    private SongbookAdapter songbookAdapter;
    private View footerView;

    Parcelable state; // saves the scroll position of the listview

    private Thread thread;

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

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            LBSong songEdited = (LBSong) intent.getSerializableExtra("song");
            Log.d("MainActivity", "Receive song - name: " + songEdited.getName());
            songbook.integrateSong(songEdited, true, true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list_selection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);
        toolbarSearchButton = (SearchView) findViewById(R.id.toolbarSearchButton);

        toolbarSearchButton.clearFocus();
        setupSearchView(toolbarSearchButton);
//        ((EditText)toolbarSearchButton.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.BLACK);
//        // icon
//        ImageView searchIcon = (ImageView) toolbarSearchButton.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
//        searchIcon.setImageResource(R.drawable.search_icon);

        toolbarSearchButton.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchInSongbook(newText);
                return true;
            }
        });
//        int id = toolbarSearchButton.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        TextView textView = (TextView) toolbarSearchButton.findViewById(id);
//        textView.setTextColor(Color.WHITE);

        songbookListview = (ListView) findViewById(R.id.singleChoiceListView);
        songbookListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleClickOnSong(view);
            }
        });

        songbook = new LBSongbook(this);

        handler.post(runnableSongs);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("song-edited-event"));

    }
    private void setupSearchView(SearchView searchView)
    {
        // search hint
        searchView.setQueryHint("Rechercher");
        searchView.clearFocus();

        // background
        View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        //searchPlate.setBackgroundResource(R.drawable.searchview_bg);

        // icon
        ImageView searchIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageResource(R.drawable.search_icon);

        // clear button
        ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.close_icon);

        // text color
        AutoCompleteTextView searchText = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextColor(getResources().getColor(R.color.darkGray));
        searchText.setHintTextColor(getResources().getColor(R.color.hintGray));
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

        if(state != null)
            songbookListview.onRestoreInstanceState(state);

        toolbarSearchButton.clearFocus();
    }

    @Override
    protected void onPause() {


        if(songbook.isHasChangesToSave()) {
            songbook.save(this);
        }

        // save the scroll position of the listview
        state = songbookListview.onSaveInstanceState();

        super.onPause();
    }

    @Override
    protected void onStop() {
        if(thread != null)
            thread.interrupt();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);

        super.onDestroy();
    }

    private void handleClickOnSong(View view) {
        Intent i = new Intent(this, SongActivity.class);
        LBSong song = songbook.songWithId((int)view.getTag());
        i.putExtra("song", song);
        startActivity(i);
    }

    private void searchInSongbook(String newText) {
        final String text = newText;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                ArrayList<LBSong> songsResult = songbook.search(text);

                Log.i("MainActivity", "result count:" + songsResult.size());

                for (LBSong song : songsResult) {
                    Log.i("MainActivity", "song name:" + song.getName());
                }

                if(!Thread.currentThread().isInterrupted()) {
                    // display welcome UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // run on UI : refresh Listview
                        }
                    });
                }
            }
        };

        thread = new Thread(runnable);
        thread.start();
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


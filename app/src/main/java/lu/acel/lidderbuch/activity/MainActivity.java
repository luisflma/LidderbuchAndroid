package lu.acel.lidderbuch.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.Settings;
import lu.acel.lidderbuch.design.SongbookAdapter;
import lu.acel.lidderbuch.helper.FontHelper;
import lu.acel.lidderbuch.model.LBSong;
import lu.acel.lidderbuch.model.LBSongbook;
import lu.acel.lidderbuch.network.LBFetchSongs;

public class MainActivity extends AppCompatActivity {

    String credits = "<a href='http://www.acel.lu/publications/lidderbuch'>Lidderbuch</a> ass eng App vun der <a href='http://acel.lu/'>ACEL</a>, dem Daachverband vun iwwer 40 lëtzebuergesch Studentencercelen. De Projet gouf vum <a href='http://2f.lt/1GioavM'>Fränz Friederes</a> entwéckelt an ass op <a href='https://github.com/AcelLuxembourg/LidderbuchApp'>GitHub</a> ze fannen. <a href='http://acel.lu/about/contact'>Schreif eis</a>, wann däi Lidd feelt.";

    private ArrayList<LBSong> songsWithBookmarked;

    private LBSongbook songbook;

    private RelativeLayout creditsLayout;
    private TextView tvMenu;
    private ImageView toolbarInfoButton;
    private SearchView toolbarSearchButton;

    private ListView songbookListview;
    private SongbookAdapter songbookAdapter;
    private View footerView;

    private Animation animShow, animHide;

    Parcelable state; // saves the scroll position of the listview

    private Thread thread;

    // Fetch songs
    Handler handler = new Handler();

    private Runnable runnableSongs = new Runnable() {
        @Override
        public void run() {

            String arg0 = String.valueOf(songbook.updateTime().getTime() / 1000);
            //String arg0 = String.valueOf(1440754883); // for test
            String url = MessageFormat.format(Settings.SONGBOOK_API, arg0);
            new FetchSongsTask().execute(url);

            handler.postDelayed(runnableSongs, 300000); //300000
        }
    };

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LBSong songEdited = (LBSong) intent.getSerializableExtra("song");
            boolean bookmarked = intent.getBooleanExtra("bookmarked", false);
            songbook.integrateSong(songEdited, true, true);

            if(bookmarked)
                songbook.integrateSongBookmarked(songEdited, MainActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list_selection);

        initAnimation();

        creditsLayout = (RelativeLayout) findViewById(R.id.creditsLayout);
        tvMenu = (TextView) findViewById(R.id.tvMenu);
        tvMenu.setClickable(true);
        tvMenu.setMovementMethod(LinkMovementMethod.getInstance());
        tvMenu.setText(Html.fromHtml(credits));
        tvMenu.setTypeface(FontHelper.georgia);

        toolbarInfoButton = (ImageView) findViewById(R.id.toolbarInfoButton);
        toolbarInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideCredits();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);

        footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);
        toolbarSearchButton = (SearchView) findViewById(R.id.toolbarSearchButton);

        toolbarSearchButton.clearFocus();
        setupSearchView(toolbarSearchButton);

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

        songbookListview = (ListView) findViewById(R.id.singleChoiceListView);
        songbookListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleClickOnSong(view);
            }
        });

        songbook = new LBSongbook(this);

        songbookListview.addFooterView(footerView);
        refreshFooter(songbook.updateTime());

        handler.post(runnableSongs);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("song-edited-event"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(songbook.getSongs() != null) {
            songbookAdapter = new SongbookAdapter(this, R.layout.list_item_songbook, getSongsWithBookmarked());
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

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation( this, R.anim.view_show);
        animHide = AnimationUtils.loadAnimation( this, R.anim.view_hide);
    }

    private void showHideCredits() {
        if(creditsLayout.getVisibility() == View.VISIBLE) {
            //creditsLayout.startAnimation(animHide);
            creditsLayout.setVisibility(View.GONE);
        }
        else {
            creditsLayout.setVisibility(View.VISIBLE);
            //creditsLayout.startAnimation(animShow);
        }

    }

    private void setupSearchView(SearchView searchView) {
        // search hint
        searchView.setQueryHint(getString(R.string.search));
        searchView.clearFocus();

        // background
        View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        //searchPlate.setBackgroundResource(R.drawable.searchview_bg);

        // icon
        ImageView searchIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageResource(R.drawable.ic_search);

        // clear button
        ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.ic_clear);

        // text color
        AutoCompleteTextView searchText = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextColor(getResources().getColor(R.color.darkGray));
        searchText.setHintTextColor(getResources().getColor(R.color.hintGray));
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

                final ArrayList<LBSong> songsResult = songbook.search(text);

                if(!Thread.currentThread().isInterrupted()) {
                    // display welcome UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // run on UI : refresh Listview
                            if(songsResult.size() != 0)
                                refreshSongsList(songsResult);
                            else
                                refreshSongsList(getSongsWithBookmarked());
                        }
                    });
                }
            }
        };

        thread = new Thread(runnable);
        thread.start();
    }

    private ArrayList<LBSong> getSongsWithBookmarked() {
        songsWithBookmarked = new ArrayList<>(songbook.getSongsBookmarked());
        songsWithBookmarked.addAll(songbook.getSongs());

        return songsWithBookmarked;
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
        songbookAdapter.setSongs(songs);
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
                songbook.integrateSongs(songs, false);
                refreshSongsList(getSongsWithBookmarked());
            }
        }
    }

}


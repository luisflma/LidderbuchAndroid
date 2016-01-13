package lu.acel.lidderbuch.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.design.SongbookAdapter;
import lu.acel.lidderbuch.model.LBSong;
import lu.acel.lidderbuch.model.LBSongbook;

public class MainActivity extends AppCompatActivity {

    private LBSongbook songbook;

    private ListView songbookListview;
    private SongbookAdapter songbookAdapter;
    private View footerView;

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

        for(LBSong so : songbook.getSongs()) {
            Log.i("MainActivity", "number:" + so.getNumber() + " updateTime:" + so.getUpdateTime().toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Date updateTime = songbook.updateTime();
        if(updateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String dateFormatted = sdf.format(updateTime);
            TextView updateTimeTv = (TextView) footerView.findViewById(R.id.tvUpdateTime);
            updateTimeTv.setText(getString(R.string.update_time) + " " + dateFormatted);
        }
        songbookAdapter = new SongbookAdapter(this, R.layout.list_item_songbook, songbook.getSongs());
        songbookListview.addFooterView(footerView);
        songbookListview.setAdapter(songbookAdapter);
    }

    private void handleClickOnSong(View view) {

    }
}

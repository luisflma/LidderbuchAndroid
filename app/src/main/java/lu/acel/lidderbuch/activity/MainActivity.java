package lu.acel.lidderbuch.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.design.SongbookAdapter;

public class MainActivity extends AppCompatActivity {

    private ListView songbookListview;
    private SongbookAdapter songbookAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_list_selection);

        songbookListview = (ListView) findViewById(R.id.singleChoiceListView);
        songbookListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleClickOnSong(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get songs
        ArrayList<String> songs = new ArrayList<>();
        songs.add("one");
        songs.add("two");
        songs.add("three");
        songs.add("four");
        songs.add("five");
        songs.add("six");
        songs.add("seven");
        songs.add("height");

        songbookAdapter = new SongbookAdapter(this, R.layout.list_item_songbook, songs);
        songbookListview.setAdapter(songbookAdapter);
    }

    private void handleClickOnSong(View view) {

    }
}

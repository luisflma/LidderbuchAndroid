package lu.acel.lidderbuch.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lu.acel.lidderbuch.FileHelper;
import lu.acel.lidderbuch.Settings;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class LBSongbook {

    private boolean hasChangesToSave;
    private ArrayList<LBSong> songs;
    private ArrayList<String> categories;

    public ArrayList<LBSong> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<LBSong> songs) {
        this.songs = songs;
    }

    public LBSongbook(Context context) {
        songs = load(context);
    }

    private ArrayList<LBSong> load(Context context) {

        ArrayList<LBSong> songsList = new ArrayList<>();
        // try loading from local songs file
//        File songsFile = new File(context.getFilesDir(), Settings.SONGS_FILE);
//        if(!songsFile.exists()) {
//            try {
//                songsFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        String songsStr = FileHelper.readFromFile(context);
        if(!TextUtils.isEmpty(songsStr)) {
            Log.i("Songbook", "songs string from file is not empty");
            songsList = songsWithData(songsStr);
        } else {
            // try loading songs delivered json file from asset folder
            Log.i("Songbook", "try load songs from json file from assets");
            songsList = songsWithData(loadJSONFromAsset(context));
        }

        return songsList;
    }

    private void save(Context context) {
        JSONArray songsJson = new JSONArray();

        for(LBSong s : songs) {
            songsJson.put(s.json());
        }

        FileHelper.writeToFile(songsJson.toString(), context);
        hasChangesToSave = false;
    }

    private void upload() {
        try {
            URL url = new URL(Settings.SONGBOOK_API);

            //todo call http request
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("songs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private ArrayList<LBSong> songsWithData(String[] songsTxt) {
        // one rows contains the complete json
        ArrayList<LBSong> songsList = new ArrayList<>();
        for(int i = 0 ; i < songsTxt.length ; i++) {
            try {
                JSONArray jsonSongs = new JSONArray(songsTxt[i]);

                for(int j = 0 ; j < jsonSongs.length() ; j++) {
                    JSONObject songJson = jsonSongs.getJSONObject(j);
                    songsList.add(new LBSong(songJson));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songsList;
    }

    private ArrayList<LBSong> songsWithData(String songsTxt) {

        Log.i("Songbook", "songs json : " + songsTxt);
        ArrayList<LBSong> songsList = new ArrayList<>();
        try {
            JSONArray jsonSongs = new JSONArray(songsTxt);

            for(int j = 0 ; j < jsonSongs.length() ; j++) {
                JSONObject songJson = jsonSongs.getJSONObject(j);
                songsList.add(new LBSong(songJson));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return songsList;
    }

    public Date updateTime() {
        Date updateTime = null;

        for(int i = 0 ; i < songs.size() ; i++) {
            if(updateTime == null || songs.get(i).getUpdateTime().getTime() > updateTime.getTime()) {
                Log.i("Songbook", "song updateTime:" + songs.get(i).getUpdateTime());
                updateTime = songs.get(i).getUpdateTime();
            }
        }

        return updateTime;
    }


    private void reloadMeta() {
        // todo reload meta
    }

    public void search(String keyword) {
        // todo search method in songbook class
    }

    public LBSong songWithId(int id) {
        for(LBSong s : songs){
            if(s.getId() == id) {
                return s;
            }
        }

        return null;
    }

    public LBSong songWithNumber(int number) {
        for(LBSong s : songs) {
            if(s.getNumber() == number)
                return s;
        }

        return null;
    }

    public LBSong songWithUrl(URL url) {
        for(LBSong s : songs) {
            if(s.getUrl().getPath() == url.getPath())
                return s;
        }

        return null;
    }

}

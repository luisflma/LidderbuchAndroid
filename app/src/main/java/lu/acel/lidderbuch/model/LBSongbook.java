package lu.acel.lidderbuch.model;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.helper.FileHelper;
import lu.acel.lidderbuch.Settings;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class LBSongbook {

    private boolean hasChangesToSave;
    private ArrayList<LBSong> songs;
    private ArrayList<LBSong> songsBookmarked;

    public boolean isHasChangesToSave() {
        return hasChangesToSave;
    }

    public void setHasChangesToSave(boolean hasChangesToSave) {
        this.hasChangesToSave = hasChangesToSave;
    }

    public ArrayList<LBSong> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<LBSong> songs) {
        this.songs = songs;
    }

    public ArrayList<LBSong> getSongsBookmarked() {
        return songsBookmarked;
    }

    public void setSongsBookmarked(ArrayList<LBSong> songsBookmarked) {
        this.songsBookmarked = songsBookmarked;
    }

    public LBSongbook(Context context) {
        songs = load(context);
        songsBookmarked = loadBookmarked(context);
    }

    private ArrayList<LBSong> load(Context context) {

        ArrayList<LBSong> songsList = new ArrayList<>();

        // try loading from shared preferences
        String songsStr = FileHelper.getKey(context, "songsJson");

        if(!TextUtils.isEmpty(songsStr)) {
            songsList = songsWithData(songsStr);
        } else {
            // try loading songs delivered json file from asset folder
            songsList = songsWithData(loadJSONFromAsset(context));
        }

        if(songsList == null)
            return new ArrayList<LBSong>();

        return songsList;
    }

    public ArrayList<LBSong> loadBookmarked(Context context) {
        ArrayList<LBSong> bookmarked = new ArrayList<>();

        if(songs.size() == 0)
            return bookmarked;

        for(LBSong so : songs) {
            if(so.isBookmarked())
                bookmarked.add(bookmarkSong(so, context));
        }

        return bookmarked;
    }

    public void save(Context context) {

        Gson gson = new Gson();
        String jsonStr = gson.toJson(songs);

        FileHelper.storeKey(context, "songsJson", jsonStr);
        hasChangesToSave = false;
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(Settings.SONGS_JSON);
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

    public static ArrayList<LBSong> songsWithData(String songsTxt) {

        if(TextUtils.isEmpty(songsTxt))
            return null;

        ArrayList<LBSong> songsList = new ArrayList<>();
        try {
            JSONArray jsonSongs = new JSONArray(songsTxt);

            for(int i = 0 ; i < jsonSongs.length() ; i++) {

                JSONObject songJson = jsonSongs.getJSONObject(i);
                songsList.add(new LBSong(songJson));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return songsList;
    }

    public Date updateTime() {

        if(songs == null)
            return null;

        Date updateTime = null;
        for(int i = 0 ; i < songs.size() ; i++) {
            if(updateTime == null || songs.get(i).getUpdate_time().getTime() > updateTime.getTime()) {
                updateTime = songs.get(i).getUpdate_time();
            }
        }

        return updateTime;
    }

    public void integrateSongs(ArrayList<LBSong> newSongs, boolean replaceMeta) {
        for(int i = 0; i < newSongs.size() ; i++) {
            integrateSong(newSongs.get(i), replaceMeta, (i == newSongs.size() - 1));
        }

    }

    public void integrateSong(LBSong newSong, boolean replaceMeta, boolean propagate) {

        // is the song already included
        int idx = songs.indexOf(newSong);
        if(idx > -1) {
            LBSong oldSong = songs.get(idx);
            if(newSong.getUpdate_time().getTime() > oldSong.getUpdate_time().getTime() || (replaceMeta && newSong.getUpdate_time().getTime() == oldSong.getUpdate_time().getTime())) {
                if(!replaceMeta) {
                    newSong.setBookmarked(oldSong.isBookmarked());
                    newSong.setViews(oldSong.getViews());
                    newSong.setViewTime(oldSong.getViewTime());
                }

                // replace song
                songs.set(idx, newSong);
            }
        }
        else {
            // add song to library
            songs.add(newSong);
        }

        if(propagate) {
            hasChangesToSave = true;
        }
    }

    public void integrateSongBookmarked(LBSong song, Context context) {

        if(songsBookmarked == null)
            return;

        if(song.isBookmarked()) {
            songsBookmarked.add(bookmarkSong(song, context));
        } else {
            songsBookmarked.remove(song);
        }
    }

    public LBSong bookmarkSong(LBSong song, Context context) {
        LBSong copySong = new LBSong(song);
        copySong.setCategory(context.getString(R.string.bookmarked));

        return copySong;
    }

    public ArrayList<LBSong> search(String keyword) { //may be callback

        ArrayList<LBSong> songsResult = new ArrayList<>();

        // handle song number
        int number;
        try {
            number = Integer.parseInt(keyword);
            LBSong song = songWithNumber(number);

            if(song != null) {
                songsResult.add(song);
                return songsResult;
            }
        } catch(NumberFormatException nfe) {
            //nfe.printStackTrace();
        }

        // return no results when query too short
        if(keyword.length() <= 2) {
            return songsResult;
        }

        // search in songs
        for(LBSong so : songs) {
            if(so.search(keyword) > 0) {
                songsResult.add(so);
            }
        }

        Collections.sort(songsResult);

        return songsResult;
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

package lu.acel.lidderbuch.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.network.StringHelper;

import com.google.common.base.Joiner;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class LBSong implements Serializable{
    private int id;
    private String name;
    private String language;
    private URL url;
    private String category;
    private int position;
    private ArrayList<LBParagraph> paragraphs;
    private Date update_time;

    private boolean bookmarked = false;
    private int views = 0;
    private Date viewTime;

    private int number;
    private String way;
    private int year;
    private String lyrics_author;
    private String melody_author;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<LBParagraph> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(ArrayList<LBParagraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public Date getViewTime() {
        return viewTime;
    }

    public void setViewTime(Date viewTime) {
        this.viewTime = viewTime;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LBSong(JSONObject jsonSong) {

        try {
            // retrieve required attributes
            id = jsonSong.getInt("id");
            name = jsonSong.getString("name");
            language = jsonSong.getString("language");
            category = jsonSong.getString("category");
            position = jsonSong.getInt("position");

            String timeStampStr = jsonSong.getString("update_time");
            if(!TextUtils.isEmpty(timeStampStr)){
                try{
                    long timeStamp = Long.parseLong(timeStampStr);
                    update_time = new Date(timeStamp * 1000);
                } catch(NumberFormatException nfe) {
                    //SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm:ss a");
                    try {
                        Log.i("Song", "timeStampStr:" + timeStampStr);
                        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
                        update_time = format.parse(timeStampStr);
                    } catch (ParseException e) {
                        Log.i("Song", "ParseException::");
                        e.printStackTrace();
                    }
                }
            }

            paragraphs = new ArrayList<>();
            JSONArray paragraphsJson = jsonSong.getJSONArray("paragraphs");
            for (int i = 0; i < paragraphsJson.length(); i++) {
                paragraphs.add(new LBParagraph(paragraphsJson.getJSONObject(i)));
            }

            String urlStr = jsonSong.getString("url");
            url = new URL(urlStr);

            // optional attributes
            number = jsonSong.optInt("number");
            way = StringHelper.optString(jsonSong, "way");
            year = jsonSong.optInt("year");
            lyrics_author = StringHelper.optString(jsonSong, "lyrics_author");
            melody_author = StringHelper.optString(jsonSong, "melody_author");

            bookmarked = jsonSong.optBoolean("bookmarked");
            views = jsonSong.optInt("views");

            String viewTimeStampStr = StringHelper.optString(jsonSong, "viewTime");
            if(!TextUtils.isEmpty(viewTimeStampStr)) {
                try{
                    long viewTimeStamp = Long.parseLong(viewTimeStampStr);
                    update_time = new Date(viewTimeStamp * 1000);
                } catch(NumberFormatException nfe) {
                    //SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy  h:mm:ss a");
                    try {
                        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
                        update_time = format.parse(timeStampStr);
                    } catch (ParseException e) {
                        Log.i("Song", "ParseException:ViewTimeStamp:");
                        e.printStackTrace();
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public String preview() {
        // grab the two first lines of a song
        // if a paragraph has only one line, the next one will be included too

        String preview = "";
        int lines = 0;
        int i = -1;

        while (++i < paragraphs.size() && lines < 2) {
            String paragraphLines[] = paragraphs.get(i).getContent().split("\\r?\\n");
            int j = -1;
            while (++j < paragraphLines.length && lines < 2) {
                preview += (lines > 0 ? "\n" : "") + paragraphLines[j];
                lines++;
            }
        }

        return preview;
    }

    public String detail(Context context) {

        ArrayList<String> parts = new ArrayList<>();

        if(this.number > 0) {
            parts.add(context.getString(R.string.no) + " " + number);
        }

        if(!TextUtils.isEmpty(way)) {
            parts.add(context.getString(R.string.tune) + ": " + way);
        }

        if(!TextUtils.isEmpty(lyrics_author) && lyrics_author.equals(melody_author)) {
            parts.add(context.getString(R.string.text_and_melodie) + ": " + lyrics_author);
        }
        else {
            if(!TextUtils.isEmpty(lyrics_author)) {
                parts.add(context.getString(R.string.text) + ": " + lyrics_author);
            }

            if(!TextUtils.isEmpty(melody_author)) {
                parts.add(context.getString(R.string.melodie) + ": " + melody_author);
            }
        }

        if(year > 0) {
            parts.add(String.valueOf(year));
        }

        return Joiner.on(" · ").join(parts);
    }

    public String json() {

        // prepare paragraphs json object
        JSONArray paragraphsJson = new JSONArray();

        for( LBParagraph para : paragraphs) {
            paragraphsJson.put(para.json());
        }

        // prepare json object
        JSONObject jsonSong = new JSONObject();
        try {
            jsonSong.put("id", id);
            jsonSong.put("type", name);
            jsonSong.put("content", language);
            jsonSong.put("url", url.toString());
            jsonSong.put("category", category);
            jsonSong.put("position", position);
            jsonSong.put("update_time", update_time.getTime());

            jsonSong.put("paragraphs", paragraphsJson);

            // meta
            jsonSong.put("bookmarked", bookmarked);
            jsonSong.put("views", views);
            jsonSong.put("viewTime", viewTime != null ? viewTime.getTime() : null);

            // optional details
            jsonSong.put("number", number);
            jsonSong.put("way", !TextUtils.isEmpty(way) ? way : null);
            jsonSong.put("year", year);
            jsonSong.put("lyrics_author", !TextUtils.isEmpty(lyrics_author) ? lyrics_author : null);
            jsonSong.put("melody_author", !TextUtils.isEmpty(melody_author) ? melody_author : null);

            return jsonSong.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LBSong))
            return false;

        return this.id == ((LBSong) o).id;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }


    private String lastSearchKeywords;
    private int lastSearchScore;

    public int search(String keywords) {
        // TODO LBSONG implement search method
         return 0;
    }

}

package lu.acel.lidderbuch.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import lu.acel.lidderbuch.R;

import com.google.common.base.Joiner;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class LBSong {
    private int id;
    private String name;
    private String language;
    private URL url;
    private String category;
    private int position;
    private ArrayList<LBParagraph> paragraphs;
    private Date updateTime;

    private boolean bookmarked = false;
    private int views = 0;
    private Date viewTime;

    private int number;
    private String way;
    private int year;
    private String lyricsAuthor;
    private String melodyAuthor;

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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
                long timeStamp = Long.parseLong(timeStampStr);
                updateTime = new Date(timeStamp * 1000);
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
            way = jsonSong.optString("way");
            year = jsonSong.optInt("year");
            lyricsAuthor = jsonSong.optString("lyrics_author");
            melodyAuthor = jsonSong.optString("melody_author");

            bookmarked = jsonSong.optBoolean("bookmarked");
            views = jsonSong.optInt("views");

            String viewTimeStampStr = jsonSong.optString("viewTime");
            if(!TextUtils.isEmpty(viewTimeStampStr)) {
                long viewTimeStamp = Long.parseLong(viewTimeStampStr);
                viewTime = new Date(viewTimeStamp * 1000);
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

        if(!TextUtils.isEmpty(lyricsAuthor) && lyricsAuthor.equals(melodyAuthor)) {
            parts.add(context.getString(R.string.text_and_melodie) + ": " + lyricsAuthor);
        }
        else {
            if(!TextUtils.isEmpty(lyricsAuthor)) {
                parts.add(context.getString(R.string.text) + ": " + lyricsAuthor);
            }

            if(!TextUtils.isEmpty(melodyAuthor)) {
                parts.add(context.getString(R.string.melodie) + ": " + melodyAuthor);
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
            jsonSong.put("update_time", updateTime.getTime());

            jsonSong.put("paragraphs", paragraphsJson);

            // meta
            jsonSong.put("bookmarked", bookmarked);
            jsonSong.put("views", views);
            jsonSong.put("viewTime", viewTime != null ? viewTime.getTime() : null);

            // optional details
            jsonSong.put("number", number);
            jsonSong.put("way", !TextUtils.isEmpty(way) ? way : null);
            jsonSong.put("year", year);
            jsonSong.put("lyrics_author", !TextUtils.isEmpty(lyricsAuthor) ? lyricsAuthor : null);
            jsonSong.put("melody_author", !TextUtils.isEmpty(melodyAuthor) ? melodyAuthor : null);

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

package lu.acel.lidderbuch.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import lu.acel.lidderbuch.helper.StringHelper;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class LBParagraph implements Serializable{
    private int id;
    private String type;
    private String content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LBParagraph(JSONObject jsonParagraph) {
        try {

            id = jsonParagraph.getInt("id");
            type = jsonParagraph.getString("type");
            content = jsonParagraph.getString("content");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String json() {
        JSONObject jsonPara = new JSONObject();

        try {
            jsonPara.put("id", id);
            jsonPara.put("type", type);
            jsonPara.put("content", content);

            return jsonPara.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int search(String keywords) {

        // search score is determined by occurence count
        return StringHelper.countOccurrences(content, keywords);

    }

    public boolean isRefrain() {
        return type.equals("refrain");
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LBParagraph))
            return false;

        return this.id == ((LBParagraph) o).id;
    }
}

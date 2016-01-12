package lu.acel.lidderbuch.design;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lu.acel.lidderbuch.R;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class SongbookAdapter extends ArrayAdapter<String> {

    private ArrayList<String> songs;

    public SongbookAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);

        this.songs = (ArrayList<String>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_songbook, null);
        }

        String song = songs.get(position);

        if(!TextUtils.isEmpty(song)) {
//            TextView addressTextView = (TextView) view.findViewById(R.id.addressTextView);
//
//            addressTextView.setText(song);
        }

        return view;
    }
}

package lu.acel.lidderbuch.design;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lu.acel.lidderbuch.helper.FontHelper;
import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.model.LBSong;

/**
 * Created by luis-fleta on 12/01/16.
 */
public class SongbookAdapter extends ArrayAdapter<LBSong> {

    private ArrayList<LBSong> songs;

    public ArrayList<LBSong> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<LBSong> songs) {
        this.songs = songs;
    }

    public SongbookAdapter(Context context, int resource, List<LBSong> objects) {
        super(context, resource, objects);

        this.songs = (ArrayList<LBSong>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_songbook, null);
        }

        LBSong song = this.songs.get(position);

        if(song != null) {
            view.setTag(song.getId());

            TextView headerTv = (TextView) view.findViewById(R.id.tvHeader);
            TextView numberTv = (TextView) view.findViewById(R.id.tvNumber);
            TextView titleTv = (TextView) view.findViewById(R.id.tvTitle);
            TextView previewTv = (TextView) view.findViewById(R.id.tvPreview);

            headerTv.setTypeface(FontHelper.georgia);
            numberTv.setTypeface(FontHelper.georgia);
            titleTv.setTypeface(FontHelper.georgia);
            previewTv.setTypeface(FontHelper.georgia);

            // category header
            if(position > 0) {
                LBSong previousSong = songs.get(position - 1);

                if(song.getCategory().equals(previousSong.getCategory())) {
                    headerTv.setVisibility(View.GONE);
                } else {
                    headerTv.setText(song.getCategory());
                    headerTv.setVisibility(View.VISIBLE);
                }
            } else {
                headerTv.setText(song.getCategory());
                headerTv.setVisibility(View.VISIBLE);
            }

            if(song.getNumber() > 0) {
                numberTv.setText(String.valueOf(song.getNumber()));
            }
            titleTv.setText(song.getName());
            previewTv.setText(song.preview());
        }

        return view;
    }

    @Override
    public int getCount() {
        return this.songs.size();
    }
}

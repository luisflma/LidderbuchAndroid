package lu.acel.lidderbuch.helper;

import java.util.Comparator;
import java.util.Date;

import lu.acel.lidderbuch.model.LBSong;

/**
 * Created by luis-fleta on 15/01/16.
 */
public class SongComparator implements Comparator<LBSong> {
    @Override
    public int compare(LBSong song1, LBSong song2) {

        if(song1.lastSearchScore > song2.lastSearchScore) {
            return 1;
        }else if (song1.lastSearchScore < song2.lastSearchScore) {
            return -1;
        } else {
            return 0;
        }

    }
}
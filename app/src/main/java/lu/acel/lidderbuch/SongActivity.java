package lu.acel.lidderbuch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import lu.acel.lidderbuch.model.LBParagraph;
import lu.acel.lidderbuch.model.LBSong;

public class SongActivity extends AppCompatActivity {

    private TextView tvName, tvDetail, tvLyrics;
    private LBSong song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        prepareLayout();

        song = (LBSong) getIntent().getSerializableExtra("song");

        updateLayout();
    }

    private void prepareLayout() {
        tvName = (TextView) findViewById(R.id.tvName);
        tvDetail = (TextView) findViewById(R.id.tvDetail);
        tvLyrics = (TextView) findViewById(R.id.tvLyrics);

        tvName.setTypeface(FontHelper.georgia);
        tvDetail.setTypeface(FontHelper.georgia);
    }


    private void updateLayout() {
        tvName.setText(song.getName());
        tvDetail.setText(song.detail(this));

        String lyricsOriginal = "";
        for(LBParagraph para : song.getParagraphs()) {
            lyricsOriginal += para.getContent() + "\n\n";
        }

        SpannableStringBuilder SS = new SpannableStringBuilder(lyricsOriginal);

        int charCount = 0;
        for(LBParagraph para : song.getParagraphs()) {

            if(para.isRefrain()) {
                SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgiaItalic), charCount, charCount + para.getContent().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            } else {
                SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgia), charCount, charCount + para.getContent().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            charCount += para.getContent().length() + 2;
        }

        tvLyrics.setText(SS);
    }
}

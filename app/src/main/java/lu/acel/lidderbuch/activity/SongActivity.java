package lu.acel.lidderbuch.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import lu.acel.lidderbuch.CustomTypefaceSpan;
import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.design.ObservableScrollView;
import lu.acel.lidderbuch.design.ScrollViewListener;
import lu.acel.lidderbuch.helper.FontHelper;
import lu.acel.lidderbuch.model.LBParagraph;
import lu.acel.lidderbuch.model.LBSong;

public class SongActivity extends AppCompatActivity {

    private static final String TAG = SongActivity.class.getName();

    private String lyricsOriginal;
    private String textSplited[];
    private int lineCount = 0;

    private ObservableScrollView svLyrics;
    private Toolbar toolbar;
    private ImageView toolbarBackButton, toolbarShareButton, toolbarBookmarkButon;
    private TextView tvName, tvDetail, tvLyrics;
    private LBSong song;
    // tracking view
    private boolean viewTracked = false;
    Handler handler = new Handler();
    private Runnable runnableTrackView = new Runnable() {
        @Override
        public void run() {
            trackView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        prepareLayout();

        song = (LBSong) getIntent().getSerializableExtra("song");

        updateLayout();

    }

    @Override
    protected void onResume() {
        super.onResume();

        startTrackingView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTrackingView();
    }

    private void prepareLayout() {
        // scrollview
        svLyrics = (ObservableScrollView) findViewById(R.id.svLyrics);
        svLyrics.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

            }
        });

        // Prepare toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // menu button
        toolbarBackButton = (ImageView) findViewById(R.id.toolbarBackButton);
        toolbarShareButton = (ImageView) findViewById(R.id.toolbarSharetButton);
        toolbarBookmarkButon = (ImageView) findViewById(R.id.toolbarBookmarktButton);

        toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity();
            }
        });

        toolbarShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(v);
            }
        });

        toolbarBookmarkButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarkSong();
            }
        });

        // prepare song's info
        tvName = (TextView) findViewById(R.id.tvName);
        tvDetail = (TextView) findViewById(R.id.tvDetail);
        tvLyrics = (TextView) findViewById(R.id.tvLyrics);

        tvLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineCount < textSplited.length) {

                    svLyrics.post(new Runnable() {
                        @Override
                        public void run() {
                            int y = tvLyrics.getLayout().getLineBottom(lineCount) - 200;
                            svLyrics.smoothScrollTo(0, y);
                        }
                    });
                    highlightLine();
                    lineCount++;

                } else {
                    lineCount = 0;
                    svLyrics.smoothScrollTo(0, 0);
                    refreshLyrics();
                }

            }
        });

        tvName.setTypeface(FontHelper.georgia);
        tvDetail.setTypeface(FontHelper.georgia);
    }

    private void highlightLine() {

        if(TextUtils.isEmpty(textSplited[lineCount]))
            lineCount++;

        SpannableStringBuilder SS = new SpannableStringBuilder(lyricsOriginal);
        int charCount = 0;
        int i = 0;
        LBParagraph para = song.getParagraphs().get(i);
        for(int j = 0 ; j < textSplited.length ; j++) {

            if(!TextUtils.isEmpty(textSplited[j])) {
                if(j == lineCount) {
                    if(para.isRefrain()) {
                        SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgiaItalic), charCount, charCount + textSplited[j].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    } else {
                        SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgia), charCount, charCount + textSplited[j].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                } else {
                    if(para.isRefrain()) {
                        SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgiaItalic), charCount, charCount + textSplited[j].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        SS.setSpan(new ForegroundColorSpan(Color.parseColor("#A5ABAC")), charCount, charCount + textSplited[j].length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    } else {
                        SS.setSpan (new CustomTypefaceSpan("", FontHelper.georgia), charCount, charCount + textSplited[j].length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        SS.setSpan(new ForegroundColorSpan(Color.parseColor("#A5ABAC")), charCount, charCount + textSplited[j].length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }

                if(lyricsOriginal.charAt(charCount + textSplited[j].length()) == '\r') {
                    charCount += textSplited[j].length() + 2;
                }
                else {
                    charCount += textSplited[j].length() + 1;
                }
            }
            else {
                charCount += 2;
                i++;
                para = song.getParagraphs().get(i);
            }
        }

        tvLyrics.setText(SS);

    }

    private void updateLayout() {
        // set is bookmarked image
        toolbarBookmarkButon.setImageResource(song.isBookmarked() ? R.drawable.ic_bookmark_enabled : R.drawable.ic_bookmark_disabled);

        // set song's info
        tvName.setText(song.getName());
        tvDetail.setText(song.detail(this));

        // set lyrics (refrain is in italic)
        lyricsOriginal = "";
        for(LBParagraph para : song.getParagraphs()) {
            lyricsOriginal += para.getContent() + "\r\n\r\n";
        }

        textSplited = lyricsOriginal.split("(\\r\\n|\\n)");

        refreshLyrics();
    }

    private void refreshLyrics() {
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

    private void openMenu(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
    }

    private void closeActivity() {
        finish();
    }

    private void bookmarkSong() {

        sendMessageSongEdited(true);

        if(song.isBookmarked()) {
            song.setBookmarked(!song.isBookmarked()); // false
            toolbarBookmarkButon.setImageResource(R.drawable.ic_bookmark_disabled);
        } else {
            song.setBookmarked(!song.isBookmarked()); // true
            toolbarBookmarkButon.setImageResource(R.drawable.ic_bookmark_enabled);
        }

    }

    private void sendMessageSongEdited(boolean bookmarked) {
        Intent intent = new Intent("song-edited-event");
        intent.putExtra("song", song);
        intent.putExtra("bookmarked", bookmarked);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // View Tracking
    private void startTrackingView() {

        if(!viewTracked) {
            handler.postDelayed(runnableTrackView, 15000);
        }
    }

    private void stopTrackingView() {
        handler.removeCallbacks(runnableTrackView);
    }

    private void trackView() {
        // track view
        song.setViews(song.getViews() + 1);
        song.setViewTime(new Date());

        sendMessageSongEdited(false);

        viewTracked = true;
        stopTrackingView();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_song, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.copy_into_clipboard:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", song.getUrl().toString());
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.send_by_sms:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("sms:"));
                i.putExtra("sms_body", song.getUrl().toString());
                startActivity(i);
                return true;
            case R.id.send_by_email:
                Intent i2 = new Intent(Intent.ACTION_SEND);
                i2.setType("message/rfc822");
                i2.putExtra(Intent.EXTRA_SUBJECT, song.getName());
                i2.putExtra(Intent.EXTRA_TEXT, song.getUrl().toString());
                try {
                    startActivity(Intent.createChooser(i2, getString(R.string.send_by_email)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, getString(R.string.cant_use_mail), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}

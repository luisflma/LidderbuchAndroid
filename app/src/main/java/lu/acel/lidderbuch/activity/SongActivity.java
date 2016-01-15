package lu.acel.lidderbuch.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lu.acel.lidderbuch.CustomTypefaceSpan;
import lu.acel.lidderbuch.R;
import lu.acel.lidderbuch.helper.FontHelper;
import lu.acel.lidderbuch.model.LBParagraph;
import lu.acel.lidderbuch.model.LBSong;

public class SongActivity extends AppCompatActivity {

    private ImageView toolbarBackButton, toolbarShareButton, toolbarBookmarkButon;
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
        // Prepare toolbar
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

        tvName.setTypeface(FontHelper.georgia);
        tvDetail.setTypeface(FontHelper.georgia);
    }

    private void updateLayout() {
        // set is bookmarked image
        toolbarBookmarkButon.setImageResource(song.isBookmarked() ? R.drawable.bookmarked_icon : R.drawable.bookmark_icon);

        // set song's info
        tvName.setText(song.getName());
        tvDetail.setText(song.detail(this));

        // set lyrics (refrain is in italic)
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

    private void openMenu(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
    }

    private void closeActivity() {
        finish();
    }

    private void bookmarkSong() {

        sendMessage();

        if(song.isBookmarked()) {
            song.setBookmarked(!song.isBookmarked()); // false
            toolbarBookmarkButon.setImageResource(R.drawable.bookmark_icon);
        } else {
            song.setBookmarked(!song.isBookmarked()); // true
            toolbarBookmarkButon.setImageResource(R.drawable.bookmarked_icon);
        }

    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("song-edited-event");
        // You can also include some extra data.
        intent.putExtra("song", song);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_song, menu);
    }


    protected void setMenuBackground(){
        // Log.d(TAG, "Enterting setMenuBackGround");
        getLayoutInflater().setFactory( new LayoutInflater.Factory() {
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                if ( name.equalsIgnoreCase( "com.android.internal.view.menu.IconMenuItemView" ) ) {
                    try { // Ask our inflater to create the view
                        LayoutInflater f = getLayoutInflater();
                        final View view = f.createView( name, null, attrs );
                        /* The background gets refreshed each time a new item is added the options menu.
                        * So each time Android applies the default background we need to set our own
                        * background. This is done using a thread giving the background change as runnable
                        * object */
                        new Handler().post(new Runnable() {
                            public void run () {
                                // sets the background color
                                view.setBackgroundResource( R.color.lightGray);
                                // sets the text color
                                ((TextView) view).setTextColor(Color.BLACK);
                                // sets the text size
                                ((TextView) view).setTextSize(18);
                            }
                        } );
                        return view;
                    }
                    catch ( InflateException e ) {}
                    catch ( ClassNotFoundException e ) {}
                }
                return null;
            }});
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

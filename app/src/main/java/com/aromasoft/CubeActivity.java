package com.aromasoft;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
public class CubeActivity extends Activity implements OnClickListener
{
    public final static String EXACTLYPOSITION = "exactly_position";

    private CubeSurfaceView mView;

    String[] MP3_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
    };

    private final int MUSIC_ID_INDEX = 0;
    private final int MUSIC_DISPLAY_NAME_INDEX = 1;

    private HashMap<Integer, String> displayNames = new HashMap<Integer, String>();
    private HashMap<Integer, String> musicIds = new HashMap<Integer, String>();

    TextView title;
    ImageView playimage;
    MediaPlayer mp = null;
    boolean musicPlay = false;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(mp == null)
        {
            mp = new MediaPlayer();
        }

        Cursor cur = null;
        int cubeCount = 0;

        try
        {
            cur = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MP3_PROJECTION, null,null,null);
            if(cur != null && cur.moveToFirst())
            {
                Log.i("JH", "cur.getCount() = " + cur.getCount());
                cubeCount = cur.getCount();

                int i = 0;

                do
                {
                    displayNames.put(i, cur.getString(MUSIC_DISPLAY_NAME_INDEX));
                    musicIds.put(i++, cur.getString(MUSIC_ID_INDEX));
                }
                while(cur.moveToNext());
            }

        }finally
        {
            if(cur != null)
            {
                cur.close();
            }
        }

        setContentView(R.layout.main);

        mView = CubeSurfaceView.getCubeSurfaceView();
        mView.setCubeRenderer(this, cubeCount);

        title = (TextView) findViewById(R.id.title);

        ImageView rew = (ImageView) findViewById(R.id.control_rew);
        rew.setOnClickListener(this);

        ImageView play = (ImageView) findViewById(R.id.control_play);
        play.setOnClickListener(this);

        ImageView ff = (ImageView) findViewById(R.id.control_ff);
        ff.setOnClickListener(this);

        playimage = (ImageView) findViewById(R.id.control_play_icon);

        IntentFilter setTitle =new IntentFilter();
        setTitle.addAction(EXACTLYPOSITION);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                int position = intent.getIntExtra("position", -1);
                Log.i(">>>>>", "position = " + position);
                String titleText = displayNames.get(position);
                title.setText(titleText);

                Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicIds.get(position));

                mp.stop();
                mp.reset();
                try
                {
                    mp.setDataSource(context, uri);
                }
                catch (IllegalArgumentException e1)
                {
                    e1.printStackTrace();
                }
                catch (SecurityException e1)
                {
                    e1.printStackTrace();
                }
                catch (IllegalStateException e1)
                {
                    e1.printStackTrace();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }

                try
                {
                    mp.prepare();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                if(musicPlay)
                {
                    mp.start();
                }

            }
        };

        getApplicationContext().registerReceiver(broadcastReceiver, setTitle);
    }

    public void onPause()
    {
        super.onPause();

        mp.stop();
        mp.reset();
        try
        {
            mp.prepare();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.control_rew)
        {
            mView.showPrevious();
        }
        else if(v.getId() == R.id.control_play)
        {
            musicPlay = !musicPlay;
            if(mp.isPlaying())
            {
                mp.pause();

                try
                {
                    mp.prepare();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                playimage.setImageResource(R.drawable.widget_music_icon_play);
            }
            else
            {
                try
                {
                    mp.prepare();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                mp.start();

                playimage.setImageResource(R.drawable.widget_music_icon_pause);
            }

        }
        else if(v.getId() == R.id.control_ff)
        {
            mView.showNext();
        }
    }
}
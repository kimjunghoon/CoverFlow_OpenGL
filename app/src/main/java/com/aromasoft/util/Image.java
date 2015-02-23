package com.aromasoft.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.aromasoft.R;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Image
{
    private final static String TAG = "Image";
    final static int IMAGE_MAX_SIZE = 1280;

    public static Bitmap loadImage(View view, int resID)
    {
        if (view == null)
            return null;

        Bitmap bitmap = null;
        Bitmap resize = null;

        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            if (options.outHeight * options.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE)
            {
                options.inSampleSize =
                    (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(options.outHeight, options.outWidth))
                            / Math.log(0.5)));
            }

            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeResource(view.getResources(), resID);
            if(bitmap != null) {
                resize = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            }

        }
        catch (Exception e)
        {
        }

        return resize;
    }

    static String[] MP3_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID
    };

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private final static int MUSIC_ID_INDEX = 0;
    private final static int MUSIC_DISPLAY_NAME_INDEX = 1;
    private final static int ALBUM_ID_INDEX = 2;


    public static Bitmap loadImageByMp3(Context context, View view, int index)
    {
        if (view == null)
            return null;

        Cursor cur = null;
        Bitmap bitmap = null;

        try
        {
            cur = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MP3_PROJECTION, null,null,null);
            if(cur != null && cur.moveToFirst())
            {
                cur.moveToPosition(index);
                long album_id = cur.getLong(ALBUM_ID_INDEX);
                Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

                try
                {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        catch (Exception e)
        {
            Log.i("JH","index = " + index);
        }
        finally
        {
            if(cur != null)
            {
                cur.close();
            }
        }

        Bitmap resize = null;

        if (bitmap != null)
        {
            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                if(options.outHeight * options.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE)
                {
                    options.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }

                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                resize = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            }
            catch (Exception e)
            {
                Log.d("JH", e.toString(), e);
            }
        }
        else
        {
            bitmap = loadImage(view, R.drawable.albumart_mp_unknown);
            if(bitmap != null)
            {
                resize = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
            }
        }

        return resize;
    }
}

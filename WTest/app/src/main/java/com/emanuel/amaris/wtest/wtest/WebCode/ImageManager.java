package com.emanuel.amaris.wtest.wtest.WebCode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by emanuel on 25-10-2017.
 */

public class ImageManager {

    public final String IMAGE_URL = "https://i.stack.imgur.com/dqZo3.jpg";

    private ImageEventListener listener;

    public static ImageManager manager;

    private String tempImagePath;

    private Handler uiThread;

    private Context context;

    private ImageManager(Context context) {
        this.context = context;

        uiThread = new Handler(Looper.getMainLooper());
    }

    public static ImageManager getInstance(Context context, ImageEventListener listener) {
        if (manager == null) {
            manager = new ImageManager(context);
        }
        manager.setListener(listener);

        return manager;
    }

    public ImageEventListener getListener() {
        return listener;
    }

    public void setListener(ImageEventListener listener) {
        this.listener = listener;
    }

    public void writeImageToCache(Bitmap imageData) {
        String filename = "imageToShow.jpg";
        File file;
        FileOutputStream outputStream = null;

        try {
            file = File.createTempFile(filename, null, context.getCacheDir());
            tempImagePath = file.getPath();
            outputStream = new FileOutputStream(file);
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (IOException e) {
            // Error while creating file
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap readImageFromCache() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(tempImagePath, options);

            return bitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void loadImage() {
        GetImageFromWeb imageTask = new GetImageFromWeb();

        imageTask.execute(0);
    }

    public class GetImageFromWeb extends AsyncTask<Integer, Integer, Bitmap> {
        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null) {
                listener.onPreLoadData();
            }
        }

        @Override
        protected Bitmap doInBackground(Integer... ints) {
            Bitmap image = readImageFromCache();

            if (image == null) {


                URL url;
                try {
                    url = new URL(IMAGE_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    image = BitmapFactory.decodeStream(in);

                    writeImageToCache(image);

                    final Bitmap imageToReturn = image;

                    uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onDataLoaded(imageToReturn);
                            }
                        }
                    });

                    return image;
                } catch (MalformedURLException e) {
                    e.printStackTrace();

                    if (listener != null) {
                        uiThread.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onExceptionData();
                            }
                        });
                    }

                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        uiThread.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onExceptionData();
                            }
                        });
                    }

                    return null;
                } finally {
                    urlConnection.disconnect();
                }
            }

            final Bitmap imageToReturn = image;

            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onDataLoaded(imageToReturn);
                    }
                }
            });

            return image;
        }

    }

    public interface ImageEventListener {

        void onDataLoaded(Bitmap postalCodes);

        void onPreLoadData();

        void onExceptionData();

    }
}

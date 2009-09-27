package com.markupartist.iglaset.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

/**
 * ImageLoaded, modified from excellent class posted at wu-media.com 
 * http://wu-media.com/2009/06/android-imageloader-load-images-sequencially-in-the-background/
 */
public class ImageLoader {
    static private ImageLoader sInstance;

    static public ImageLoader getInstance() {
        if (sInstance == null) {
            sInstance = new ImageLoader();
        }
        return sInstance;
    }

    private HashMap<String, Bitmap> mUrlToBitmap;
    private Queue<Group> mQueue;
    private DownloadThread mThread;
    private Bitmap mMissing;
    private boolean mBusy;

    /**
     * Constructor
     */
    private ImageLoader() {
        mUrlToBitmap = new HashMap<String, Bitmap>();
        mQueue = new LinkedList<Group>();
        mBusy = false;
    }

    public Bitmap get(String url) {
        return mUrlToBitmap.get(url);
    }

    public void load(ImageView image, String url) {
        load(image, url, false);
    }

    public void load(ImageView image, String url, boolean cache) {
        load(image, url, cache, 0);
    }

    public void load(ImageView image, String url, boolean cache, 
            int defaultImageResource) {
        if (mUrlToBitmap.get(url) != null) {
            if (image != null) {
                image.setImageBitmap(mUrlToBitmap.get(url));
            }
        } else {
            if (defaultImageResource != 0) {
                image.setImageResource(defaultImageResource);
            } else {
                image.setImageBitmap(null);
            }
            queue(image, url, cache);
        }        
    }
 
    public void queue(ImageView image, String url, boolean cache) {
        Iterator<Group> it = mQueue.iterator();
        if (image != null) {
            while (it.hasNext()) {
                if (it.next().image.equals(image)) {
                    it.remove();
                    break;
                }
            }
        } else if (url != null) {
            while (it.hasNext()) {
                if (it.next().url.equals(url)) {
                    it.remove();
                    break;
                }
            }
        }
        mQueue.add(new Group(image, url, null, cache));
        loadNext();
    }

    public void clearQueue() {
        mQueue = new LinkedList<Group>();
    }

    public void clearCache() {
        mUrlToBitmap = new HashMap<String, Bitmap>();
    }

    public void cancel() {
        clearQueue();
        if (mThread != null) {
            mThread.disconnect();
            mThread = null;
        }
    }

    public void setMissingBitmap(Bitmap bitmap) {
        mMissing = bitmap;
    }

    private void loadNext() {
        Iterator<Group> it = mQueue.iterator();
        if (!mBusy && it.hasNext()) {
            mBusy = true;
            Group group = it.next();
            it.remove();
            // double check image availability
            if (mUrlToBitmap.get(group.url) != null) {
                if (group.image != null) {
                    group.image.setImageBitmap(mUrlToBitmap.get(group.url));
                }
                mBusy = false;
                loadNext();
            } else {
                mThread = new DownloadThread(group);
                mThread.start();
            }
        }
    }

    private void onLoad() {
        if (mThread != null) {
            Group group = mThread.group;
            if (group.bitmap != null) {
                if (group.cache) {
                    mUrlToBitmap.put(group.url, group.bitmap);
                }
                if (group.image != null) {
                    group.image.setImageBitmap(group.bitmap);
                }
            } else if (mMissing != null) {
                if (group.image != null) {
                    group.image.setImageBitmap(mMissing);
                }
            }
        }
        mThread = null;
        mBusy = false;
        loadNext();
    }

    private class Group {
        public Group(ImageView image, String url, Bitmap bitmap, boolean cache) {
            this.image = image;
            this.url = url;
            this.bitmap = bitmap;
            this.cache = cache;
        }

        public ImageView image;
        public String url;
        public Bitmap bitmap;
        public boolean cache;

    }

    private class DownloadThread extends Thread {
        final Handler threadHandler = new Handler();
        final Runnable threadCallback = new Runnable() {
            public void run() {
                onLoad();
            }
        };
        private HttpURLConnection mConn;
        public Group group;

        public DownloadThread(Group group) {
            this.group = group;
        }

        @Override
        public void run() {
            InputStream inStream = null;
            mConn = null;
            try {
                mConn = (HttpURLConnection) new URL(group.url).openConnection();
                mConn.setDoInput(true);
                mConn.connect();
                inStream = mConn.getInputStream();
                group.bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
                mConn.disconnect();
                inStream = null;
                mConn = null;
            } catch (Exception ex) {
                // nothing
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ex) {
                }
            }
            disconnect();
            inStream = null;
            mConn = null;
            threadHandler.post(threadCallback);
        }

        public void disconnect() {
            if (mConn != null) {
                mConn.disconnect();
            }
        }
    }
}

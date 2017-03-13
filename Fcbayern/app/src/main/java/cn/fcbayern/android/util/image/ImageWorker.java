/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.fcbayern.android.util.image;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;


import java.lang.ref.WeakReference;

import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.AsyncTask;
import cn.fcbayern.android.util.LogUtils;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 150;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = false;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected final Context mContext;
    protected final Resources mResources;

    private static final int MESSAGE_CLEAR_ALL = 0;
    private static final int MESSAGE_CLEAR_MEM = 1;
    private static final int MESSAGE_TRIM_MEM_TO_PERCENT = 2;
    private static final int MESSAGE_INIT_DISK_CACHE = 3;
    private static final int MESSAGE_FLUSH = 4;
    private static final int MESSAGE_CLOSE = 5;

    protected ImageWorker(Context context) {
        mContext = context;
        mResources = context.getResources();
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link cn.fcbayern.android.util.image.ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link cn.fcbayern.android.util.image.ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(Object data, ImageView imageView) {
        if (data == null) {
            return;
        }

        LogUtils.v(TAG, "data = %s", data);
        BitmapDrawable value = null;

        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (value != null) {
            // Bitmap found in memory cache
            if (mImageCacheParams.checkDrawableBounds) {
//                Rect imageContentBounds = new Rect(0, 0, imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight(), imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom());
//                if (!imageContentBounds.equals(value.getBounds())) {
//                    //Fix image size bug when two activities/fragments/sth share the same loader when resume back. @yonnielu
//                    LogUtils.v(TAG, "loadImage(), force reset image bounds when drawable in memory cache");
//    //                imageView.setImageDrawable(null);
//                    LogUtils.v(TAG, "loadImage(), old bounds = %s", value.getBounds());
//                    value.setBounds(imageContentBounds);
//                    LogUtils.v(TAG, "loadImage(), new bounds = %s", imageContentBounds);
//                }
                LogUtils.v(TAG, "force reset image bounds when drawable in memory cache");
                imageView.setImageDrawable(null);
            }
            LogUtils.v(TAG, "loadImage(), set image using memory value");
            if(mImageCacheParams.clearBackground) {
            	imageView.setBackgroundResource(0);
            }
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.

            task.executeOnExecutor(AsyncTask.getDualThreadExecutor(), data);
            LogUtils.v(TAG, "data = %s, execute the task", data);
        }
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
    	Bitmap old = mLoadingBitmap;
        mLoadingBitmap = bitmap;
        BitmapUtils.recycle(old);
        System.gc();
    }
    
    public void setClearBackground(boolean clear) {
    	mImageCacheParams.clearBackground = clear;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
    	Bitmap old = mLoadingBitmap;
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
        BitmapUtils.recycle(old);
        System.gc();
    }

    /**
     * Adds an {@link ImageCache} to this {@link cn.fcbayern.android.util.image.ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(FragmentManager fragmentManager,
            ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        if (fragmentManager == null) {
            mImageCache = ImageCache.getInstance(mImageCacheParams);
        } else {
            mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
        }
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * Adds an {@link ImageCache} to this {@link cn.fcbayern.android.util.image.ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param activity
     * @param diskCacheDirectoryName See
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(android.content.Context, String)}.
     */
    public void addImageCache(FragmentActivity activity, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
        mImageCache = ImageCache.getInstance(activity.getSupportFragmentManager(), mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *            {@link cn.fcbayern.android.util.image.ImageWorker#loadImage(Object, android.widget.ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(Object data);

    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (AppConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.data;
                LogUtils.d(TAG, "cancelWork - cancelled work for %s", bitmapData);
            }
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                LogUtils.d(TAG, "cancelPotentialWork - cancelled work for %s", data);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Object... params) {
            data = params[0];

            final String dataString = String.valueOf(data);

            LogUtils.d(TAG, "doInBackground - starting work, data = %s", dataString);

            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                LogUtils.v(TAG, "doInBackground - processBitmap, data = %s", dataString);
                bitmap = processBitmap(params[0]);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                LogUtils.v(TAG, "doInBackground - data = %s, bitmap size = %f", dataString, BitmapUtils.getBitmapSize(bitmap) / 1024.0f);
//                if (Utils.hasHoneycomb()) {
//                    // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
//                    drawable = new BitmapDrawable(mResources, bitmap);
//                } else {
                    // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                    // which will recycle automagically
                    drawable = new RecyclingBitmapDrawable(mResources, bitmap);
//                }

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(dataString, drawable);
                }
            }

            LogUtils.d(TAG, "doInBackground - finished work, data = %s", dataString);

            return drawable;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                LogUtils.d(TAG, "onPostExecute - setting bitmap, data = %s, bitmap = %s, imageView = %s", data, value.getBitmap(), imageView);
                if(mImageCacheParams.clearBackground) {
                	imageView.setBackgroundResource(0);
                }
                setImageDrawable(imageView, value);
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be 
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            // Set background to loading bitmap
//            imageView.setBackgroundDrawable(
//                    new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR_ALL:
                    clearCacheInternal();
                    break;
                case MESSAGE_CLEAR_MEM:
                    clearMemCacheInternal();
                    break;
                case MESSAGE_TRIM_MEM_TO_PERCENT:
                    int size = (Integer)params[1];
                    trimMemCacheToPercentInternal(size);
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void clearMemCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearMemCache();
        }
    }

    protected void trimMemCacheToPercentInternal(int percent) {
        if (mImageCache != null) {
            mImageCache.trimMemCacheToPercent(percent);
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearAllCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR_ALL);
    }

    public void clearMemCache() {
        LogUtils.v(TAG, "clearMemCache()");
        new CacheAsyncTask().execute(MESSAGE_CLEAR_MEM);
    }

    /**
     *
     * @param percent  0 - 100
     */
    public void trimMemCacheToPercent(int percent) {
        LogUtils.v(TAG, "trimMemCacheToPercent(), size = %d", percent);
        new CacheAsyncTask().execute(MESSAGE_TRIM_MEM_TO_PERCENT, percent);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
}

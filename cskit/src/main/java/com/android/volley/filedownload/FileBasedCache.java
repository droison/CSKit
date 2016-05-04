package com.android.volley.filedownload;

import com.android.volley.Cache;
import com.android.volley.VolleyLog;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by song on 16/4/6.
 */
public class FileBasedCache implements Cache{

    /** The root directory to use for the cache. */
    private final File mRootDirectory;

    /** The maximum size of the cache in bytes. */
    private final int mMaxCacheSizeInBytes;

    /** Default maximum disk usage in bytes. */
    private static final int DEFAULT_DISK_USAGE_BYTES = 100 * 1024 * 1024;

    /** High water mark percentage for the cache */
    private static final float HYSTERESIS_FACTOR = 0.75f;

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory.
     * @param rootDirectory The root directory of the cache.
     * @param maxCacheSizeInBytes The maximum size of the cache in bytes.
     */
    public FileBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
        mRootDirectory = rootDirectory;
        mMaxCacheSizeInBytes = maxCacheSizeInBytes;
        if (!mRootDirectory.exists()) {
            if (!mRootDirectory.mkdirs()) {
                VolleyLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
            }
        }
    }

    /**
     * Constructs an instance of the DiskBasedCache at the specified directory using
     * the default maximum cache size of 5MB.
     * @param rootDirectory The root directory of the cache.
     */
    public FileBasedCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    //只要拿出来的 就不过期
    @Override
    public synchronized FileEntry get(String url) {
        File file = getFileForKey(url);
        if (file.exists()){
            FileEntry entry = new FileEntry();
            entry.file = file;
            file.setLastModified(System.currentTimeMillis());
            return entry;
        }
        return null;
    }

    @Override
    public synchronized void put(String url, Entry entry) { }

    @Override
    public synchronized void initialize() {
        pruneIfNeeded();
    }

    @Override
    public void invalidate(String url, boolean fullExpire) {
        remove(url);
    }

    @Override
    public synchronized void remove(String url) {
        File file = getFileForKey(url);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public synchronized void clear() {
        File newFile = new File(mRootDirectory.getAbsolutePath()+"tmp");
        mRootDirectory.renameTo(newFile);
        if (!mRootDirectory.exists()) {
            mRootDirectory.mkdir();
        }
        deleteFile(newFile);
    }

    /**
     * Returns a file object for the given cache key.
     */
    public File getFileForKey(String url) {
        return new File(mRootDirectory, HttpUtils.md5(url) + "." + HttpUtils.parseSuffix(url));
    }

    /**
     * 用于外面主动调用,不要在主线程执行
     */
    protected synchronized void pruneIfNeeded() {
        if (VolleyLog.DEBUG) {
            VolleyLog.v("Pruning old cache entries.");
        }
        if (!mRootDirectory.exists() || !mRootDirectory.isDirectory())
        {
            VolleyLog.e("mRootDirectory is not exist or mRootDirectory is not directory");
            return;
        }
        long before = 0;
        File[] files = mRootDirectory.listFiles();
        for (File file : files) {
            if (!file.isDirectory())
                before += file.length();
        }
        if (before > mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) { //超过上限的75%
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return (int) (lhs.lastModified() - rhs.lastModified());
                }
            });
            for (File file : files) {
                if (!file.isDirectory())
                {
                    long size = file.length();
                    boolean deleted = file.delete();
                    if (deleted) {
                        before -= size;
                    } else {
                        VolleyLog.d("Could not delete filename=%s", file.getAbsoluteFile());
                    }
                    if (before < mMaxCacheSizeInBytes * HYSTERESIS_FACTOR) {
                        break;
                    }
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return true;
        }
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f);
            }
        }
        return file.delete();
    }

    public static class FileEntry extends Entry {
        public File file;

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public boolean refreshNeeded() {
            return false;
        }
    }
}

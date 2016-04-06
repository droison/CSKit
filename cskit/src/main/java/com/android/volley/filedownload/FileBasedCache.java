package com.android.volley.filedownload;

import com.android.volley.Cache;
import com.android.volley.VolleyLog;

import java.io.File;

/**
 * Created by song on 16/4/6.
 */
public class FileBasedCache implements Cache{

    /** Total amount of space currently used by the cache in bytes. */
    private long mTotalSize = 0;

    /** The root directory to use for the cache. */
    private final File mRootDirectory;

    /** The maximum size of the cache in bytes. */
    private final int mMaxCacheSizeInBytes;

    /** Default maximum disk usage in bytes. */
    private static final int DEFAULT_DISK_USAGE_BYTES = 100 * 1024 * 1024;

    /** High water mark percentage for the cache */
    private static final float HYSTERESIS_FACTOR = 0.9f;

    /** Magic number for current version of cache file format. */
    private static final int CACHE_MAGIC = 0x20150306;

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
            return entry;
        }
        return null;
    }

    @Override
    public synchronized void put(String url, Entry entry) { }

    @Override
    public synchronized void initialize() {}

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
        return new File(mRootDirectory, HttpUtils.md5(url));
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param file
     * @return
     */
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

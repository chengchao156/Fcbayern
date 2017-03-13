package cn.fcbayern.android.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Created by chenzhan on 15/5/26.
 */
public class FileUtils {

    public static String getCachePath(String fileName) {
        return Global.sContext.getFilesDir() + File.separator + fileName;
    }

    public static void writeFile(Context context, String data, String path, boolean append) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file, append);
            byte[] bytes = data.getBytes();
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    public static void accessWriteFile(Context context, String data, String path, long dstPos) {
        RandomAccessFile accessFile = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(dstPos);
            byte[] bytes = data.getBytes();
            accessFile.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(accessFile);
        }
    }

    public static String readFile(Context context, String name) {
        String path = context.getFilesDir() + File.separator + name;
        String data = "";
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = stream.read()) != -1) {
                sb.append((char) c);
            }
            data = sb.toString();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return data;
    }

    public static long getDirSize(File dir) {
        long size = 0;
        if (null != dir && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (null != files && files.length > 0) {
                for(File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }

}

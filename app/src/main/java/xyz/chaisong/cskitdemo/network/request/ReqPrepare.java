package xyz.chaisong.cskitdemo.network.request;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by song on 15/11/27.
 * 暂时只支持图片上传，还没有别的需求
 */
public interface ReqPrepare {
    void formImageData(ArrayList<FormImage> formImages);

    public class FormImage {
        //参数的名称
        private String name;
        //文件名
        private String fileName;
        //文件的 mime，需要根据文档查询
        private String mime;
        //需要上传的图片资源，因为这里测试为了方便起见，直接把 bigmap 传进来，真正在项目中一般不会这般做，而是把图片的路径传过来，在这里对图片进行二进制转换
        private Bitmap bitmap;
        private File imageFile; //优先读取mBitMap，为空读取file

        public FormImage(Bitmap mBitmap) {
            this.bitmap = mBitmap;
        }

        public FormImage(File imageFile) {
            this.imageFile = imageFile;
        }

        public String getName() {
            //        return mName;
            //测试，把参数名称写死
            return name;
        }

        public String getFileName() {
            if (fileName == null)
                return "upload.png";
            return fileName;
        }

        //对图片进行二进制转换
        public byte[] getValue() {
            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                return bos.toByteArray();
            }
            if (imageFile != null) {
                try {
                    FileInputStream stream = new FileInputStream(imageFile);
                    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = stream.read(b)) != -1)
                        out.write(b, 0, n);
                    stream.close();
                    out.close();
                    return out.toByteArray();
                } catch (IOException e) {
                    Log.e("FormImage", "getValue" + imageFile.toString(), e);
                }
            }
            return null;
        }

        //因为我知道是 png 文件，所以直接根据文档查的
        public String getMime() {
            if (mime == null)
                return "image/png";
            return mime;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setMime(String mime) {
            this.mime = mime;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public File getImageFile() {
            return imageFile;
        }

        public void setImageFile(File imageFile) {
            this.imageFile = imageFile;
        }
    }
}

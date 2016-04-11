package xyz.chaisong.cskitdemo.network.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by song on 15/11/27.
 */
public class RespBaseMeta {
    private Meta meta;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public class Meta implements Parcelable {
        private String msg = "";
        private int status = 0;
        private String cookie = "";

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.msg);
            dest.writeInt(this.status);
            dest.writeString(this.cookie);
        }

        public Meta() {
        }

        private Meta(Parcel in) {
            this.msg = in.readString();
            this.status = in.readInt();
            this.cookie = in.readString();
        }

        public final Creator<Meta> CREATOR = new Creator<Meta>() {
            public Meta createFromParcel(Parcel source) {
                return new Meta(source);
            }

            public Meta[] newArray(int size) {
                return new Meta[size];
            }
        };

        @Override
        public String toString() {
            return "Meta{" +
                    "cookie='" + cookie + '\'' +
                    ", msg='" + msg + '\'' +
                    ", status=" + status +
                    '}';
        }
    }



    @Override
    public String toString() {
        return "BaseEntity{" +
                "meta=" + meta +
                '}';
    }
}

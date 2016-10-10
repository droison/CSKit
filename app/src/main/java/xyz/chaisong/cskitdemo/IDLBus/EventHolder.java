package xyz.chaisong.cskitdemo.idlBus;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by song on 16/10/10.
 */

public class EventHolder implements Parcelable {
    private static final String TAG = "EventHolder";

    private String mClassName;
    private String mMethodName;
    private String[] mParameterTypesName;
    private Object[] mArgs;


    public EventHolder(String className, Method method, Object[] args) {
        this.mClassName = className;
        this.mMethodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes != null && parameterTypes.length > 0) {
            this.mParameterTypesName = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                mParameterTypesName[i] = parameterTypes[i].getName();
            }
        }

        this.mArgs = args;
    }

    protected EventHolder(Parcel in) {
        mClassName = in.readString();
        mMethodName = in.readString();

        int length = in.readInt();
        if(length > 0){
            mParameterTypesName = new String[length];
            in.readStringArray(mParameterTypesName);
        }

        int argLength = in.readInt();
        if (argLength > 0 && argLength == length) { //必须一致才能传参,并且进行确认是否能使用
            mArgs = new Object[length];
            for (int i = 0; i < argLength; i++) {
                try {
                    mArgs[i] = in.readParcelable(Class.forName(mParameterTypesName[i]).getClassLoader());
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "EventHolder: ", e);
                }
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mClassName);
        dest.writeString(mMethodName);

        if (mParameterTypesName == null || mParameterTypesName.length == 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(mParameterTypesName.length);
            for (String parameterTypeName: mParameterTypesName) {
                dest.writeString(parameterTypeName);
            }
        }

        if (mArgs == null || mArgs.length == 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(mArgs.length);
            for (Object arg: mArgs) {
                dest.writeParcelable((Parcelable) arg, 0);
            }
        }
    }

    public String getClassName() {
        return mClassName;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public String[] getParameterTypesName() {
        return mParameterTypesName;
    }

    public Object[] getArgs() {
        return mArgs;
    }

    public static final Creator<EventHolder> CREATOR = new Creator<EventHolder>() {
        @Override
        public EventHolder createFromParcel(Parcel in) {
            return new EventHolder(in);
        }

        @Override
        public EventHolder[] newArray(int size) {
            return new EventHolder[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }
}

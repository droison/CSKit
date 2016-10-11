package xyz.chaisong.cskitdemo.idlbus;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
            for (int i=0; i<length; i++) {
                mParameterTypesName[i] = in.readString();
            }
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
                writeValue(dest, arg);
            }
        }
    }

    private void writeValue(Parcel dest, Object v) {
        if (v == null) {
            dest.writeInt(0);
        } else if (v instanceof String) {
            dest.writeString((String) v);
        } else if (v instanceof Integer) {
            dest.writeInt((Integer) v);
        } else if (v instanceof Map) {
            dest.writeMap((Map) v);
        } else if (v instanceof Parcelable) {
            dest.writeParcelable((Parcelable) v, 0);
        } else if (v instanceof Short) {
            dest.writeInt(((Short) v).intValue());
        } else if (v instanceof Long) {
            dest.writeLong((Long) v);
        } else if (v instanceof Float) {
            dest.writeFloat((Float) v);
        } else if (v instanceof Double) {
            dest.writeDouble((Double) v);
        } else if (v instanceof Boolean) {
            dest.writeInt((Boolean) v ? 1 : 0);
        } else if (v instanceof List) {
            dest.writeList((List) v);
        } else if (v instanceof SparseArray) {
            dest.writeSparseArray((SparseArray) v);
        } else if (v instanceof boolean[]) {
            dest.writeBooleanArray((boolean[]) v);
        } else if (v instanceof byte[]) {
            dest.writeByteArray((byte[]) v);
        } else if (v instanceof String[]) {
            dest.writeStringArray((String[]) v);
        } else if (v instanceof Parcelable[]) {
            dest.writeParcelableArray((Parcelable[]) v, 0);
        } else if (v instanceof int[]) {
            dest.writeIntArray((int[]) v);
        } else if (v instanceof long[]) {
            dest.writeLongArray((long[]) v);
        } else if (v instanceof Byte) {
            dest.writeInt((Byte) v);
        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                dest.writeArray((Object[]) v);
            } else if (v instanceof Serializable) {
                dest.writeSerializable((Serializable) v);
            } else {
                throw new RuntimeException("Parcel: unable to marshal value " + v);
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

    @Override
    public String toString() {
        return "EventHolder{" +
                "mClassName=" + mClassName +
                ", mMethodName=" + mMethodName +
                ", mParameterTypesName=" + mParameterTypesName +
                ", mArgs=" + mArgs +
                '}';
    }
}

package xyz.chaisong.mmbus.aidl;

import android.os.Parcel;
import android.os.Parcelable;

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

    public EventHolder(String className, String methodName, Class<?>[] parameterTypes, Object[] args) {
        this.mClassName = className;
        this.mMethodName = methodName;

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
            mArgs = in.readArray(EventHolder.class.getClassLoader());
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
            dest.writeArray(mArgs);
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

    public Class<?>[] getParametersType() throws ClassNotFoundException{
        if (mParameterTypesName != null && mParameterTypesName.length > 0) {
            Class<?>[] result = new Class[mParameterTypesName.length];
            for (int i = 0; i < mParameterTypesName.length; i++) {
                result[i] = getType(mParameterTypesName[i]);
            }
            return result;
        }
        return null;
    }

    private Class getType(String className) throws ClassNotFoundException{
        if (!className.contains(".")) {
            return getPrimitiveType(className);
        } else {
            return Class.forName(className);
        }
    }

    private Class getPrimitiveType(String name) {
        if (name.equals("byte")) return byte.class;
        if (name.equals("short")) return short.class;
        if (name.equals("int")) return int.class;
        if (name.equals("long")) return long.class;
        if (name.equals("char")) return char.class;
        if (name.equals("float")) return float.class;
        if (name.equals("double")) return double.class;
        if (name.equals("boolean")) return boolean.class;
        if (name.equals("void")) return void.class;

        return Object.class;
    }
}

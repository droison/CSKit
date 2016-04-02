package xyz.chaisong.mmanagercenter;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

/**
 * Created by song on 15/6/14.
 */
public class MManagerCenter {
    private static MManagerCenter defaultManagerCenter;
    private static boolean isDebugMode = true;
    private final ReentrantLock lock;
    private HashMap<String, MManagerInterface> hashMapManager;
    private Context context;

    private MManagerCenter()
    {
        log("MManagerCenter init");
        lock = new ReentrantLock();
        hashMapManager = new HashMap<>();
    }

    public static MManagerCenter init(Context context)
    {
        if (defaultManagerCenter == null)
        {
            defaultManagerCenter = new MManagerCenter();
            defaultManagerCenter.context = context;
        }
        return defaultManagerCenter;
    }

    public static void configDebug(boolean isDebug)
    {
        isDebugMode = isDebug;
    }

    @Nullable
    public static MManagerCenter defaultManagerCenter()
    {
        return defaultManagerCenter;
    }

    public static <T extends MManagerInterface> T getManager(Class<T> cls)
    {
        defaultManagerCenter.lock.lock();

        T obj = (T) defaultManagerCenter.hashMapManager.get(cls.getName());
        if (obj == null)
        {
            try {
                obj = cls.newInstance();
                log("getManager:Create service object:" + obj);
            } catch (InstantiationException e) {
                log("getManager:cls.newInstance()", e);
            } catch (IllegalAccessException e)
            {
                log("getManager:cls.newInstance()", e);
            }

            if (obj != null)
            {
                defaultManagerCenter.hashMapManager.put(cls.getName(), obj);
                obj.onManagerInit(defaultManagerCenter.context);
            }

        }
        defaultManagerCenter.lock.unlock();

        return obj;
    }

    /**
     * 除了没有返回值，和上面的getManager效果基本一致，用于初始化
     * @param cls
     *        需要初始化的Manager类
     * @param context
     *        初始化的Manager时传入的context，如果为空会传入ManagerCenter默认的Context
     * @param <T>
     *        泛型，限制cls需要implements MManagerInterface
     */
    public static <T extends MManagerInterface> void callInitManager(Class<T> cls, Context context)
    {
        defaultManagerCenter.lock.lock();

        T obj = (T) defaultManagerCenter.hashMapManager.get(cls.getName());
        if (obj == null)
        {
            try {
                obj = cls.newInstance();
                log("getManager:Create service object:" + obj);
            } catch (InstantiationException e) {
                log("getManager:cls.newInstance()", e);
            } catch (IllegalAccessException e)
            {
                log("getManager:cls.newInstance()", e);
            }
            if (obj != null)
            {
                defaultManagerCenter.hashMapManager.put(cls.getName(), obj);
                obj.onManagerInit(context != null ? context : defaultManagerCenter.context);
            }
        }
        defaultManagerCenter.lock.unlock();
    }

    /**
     * 在MManagerCenter中移除对该Manager的持有
     * @param cls
     *        需要remove的Manager类
     * @param <T>
     *        泛型，限制cls需要implements MManagerInterface
     */
    public static <T extends MManagerInterface> void removeManager(Class<T> cls)
    {
        defaultManagerCenter.lock.lock();

        MManagerInterface obj = defaultManagerCenter.hashMapManager.get(cls.getName());

        if (obj == null)
        {
            defaultManagerCenter.lock.unlock();
            return ;
        }

        defaultManagerCenter.hashMapManager.remove(cls.getName());

        obj.getManagerState().isManagerRemoved = true;

        defaultManagerCenter.lock.unlock();
    }

    /**
     * 程序后台转前台调用
     */
    public static void callEnterForeground()
    {
        defaultManagerCenter.lock.lock();
        Collection<MManagerInterface> arrayCopy = defaultManagerCenter.hashMapManager.values();
        defaultManagerCenter.lock.unlock();

        Iterator<MManagerInterface> iterator = arrayCopy.iterator();

        while (iterator.hasNext())
        {
            MManagerInterface service = iterator.next();
            service.onManagerEnterForeground();
        }
    }

    /**
     * 程序进入后台调用
     */
    public static void callEnterBackground()
    {
        defaultManagerCenter.lock.lock();
        Collection<MManagerInterface> arrayCopy = defaultManagerCenter.hashMapManager.values();
        defaultManagerCenter.lock.unlock();

        Iterator<MManagerInterface> iterator = arrayCopy.iterator();

        while (iterator.hasNext())
        {
            MManagerInterface service = iterator.next();
            service.onManagerEnterBackground();
        }
    }

    /**
     * 退出app使用，程序会取消对所有service的持有，并将自己制空
     */
    public static void callTerminate()
    {
        defaultManagerCenter.lock.lock();
        Collection<MManagerInterface> arrayCopy = defaultManagerCenter.hashMapManager.values();
        defaultManagerCenter.lock.unlock();

        Iterator<MManagerInterface> iterator = arrayCopy.iterator();

        List<Class<? extends MManagerInterface>> classArrayList = new ArrayList<>();
        while (iterator.hasNext())
        {
            MManagerInterface service = iterator.next();
            service.onManagerTerminate();
            if (!service.getManagerState().isManagerPersistent)
            {
                // remove
                classArrayList.add(service.getClass());
            }
        }
        for (Class<? extends MManagerInterface> serviceClass : classArrayList)
        {
            removeManager(serviceClass);
        }
    }

    public static void callReloadData()
    {
        defaultManagerCenter.lock.lock();
        Collection<MManagerInterface> arrayCopy = defaultManagerCenter.hashMapManager.values();
        defaultManagerCenter.lock.unlock();

        Iterator<MManagerInterface> iterator = arrayCopy.iterator();

        while (iterator.hasNext())
        {
            MManagerInterface service = iterator.next();
            service.onManagerReloadData();
        }
    }

    /**
     * 方法的会先调用每个Manager的onManagerClearData方法
     * 如果该Manager中的state.isManagerPersistent==false，则之后会取消对该Manager的持有
     */
    public static void callClearData()
    {
        defaultManagerCenter.lock.lock();
        Collection<MManagerInterface> arrayCopy = defaultManagerCenter.hashMapManager.values();
        defaultManagerCenter.lock.unlock();
        Iterator<MManagerInterface> iterator = arrayCopy.iterator();
        while (iterator.hasNext())
        {
            MManagerInterface service = iterator.next();
            service.onManagerClearData();
        }
    }

    public static void log(String msg)
    {
        if (isDebugMode)
        {
            Log.d("MManagerCenter", msg);
        }
    }

    public static void log(String msg, Exception e)
    {
        if (e == null )
        {
           if (isDebugMode)Log.d("MManagerCenter", msg);
        }
        else
        {
            Log.e("MManagerCenter", msg, e);
        }
    }
}

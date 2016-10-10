package xyz.chaisong.mmbus;

/**
 * Created by song on 16/10/9.
 */

public interface IMMBus {
    <T> void register(Class<T> targetInterface, T receiver);

    <T> void unregister(Class<T> targetInterface, T receiver);

    <T> void unregister(T receiver);

    void addRegisterListener(Object listener);

    void removeRegisterListener(Object listener);

    <T> T getReceiver(Class<T> targetInterface);
}

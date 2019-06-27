package cf.xmon.chat.database;

public interface CallBack<T>
{
    T done(final T p0);

    void error(final Throwable p0);
}

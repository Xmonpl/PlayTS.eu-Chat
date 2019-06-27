package cf.xmon.chat.database;

import java.sql.Connection;
import java.sql.ResultSet;

public interface Store
{
    Connection getConnection();

    boolean connect();

    void disconnect();

    void reconnect();

    boolean isConnected();

    ResultSet query(final String p0);

    void query(final String p0, final CallBack<ResultSet> p1);

    void update(final boolean p0, final String p1);

    ResultSet update(final String p0);

    StoreMode getStoreMode();
}



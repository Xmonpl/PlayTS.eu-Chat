package cf.xmon.chat.config;

import com.google.gson.annotations.SerializedName;

public class Instance {
    @SerializedName("virutal-server-id")
    private int virtualServerId;
    @SerializedName("query-password")
    private String password;
    @SerializedName("query-address")
    private String ip;
    @SerializedName("query-port")
    private int port;
    @SerializedName("query-login")
    private String queryLogin;
    @SerializedName("debug")
    private boolean debug;
    @SerializedName("database-mode")
    private String databasemode;
    @SerializedName("database-tableprefix")
    private String databasetableprefix;
    @SerializedName("database-mysql-host")
    private String databasemysqlhost;
    @SerializedName("database-mysql-port")
    private Integer databasemysqlport;
    @SerializedName("database-mysql-user")
    private String databasemysqluser;
    @SerializedName("database-mysql-password")
    private String databasemysqlpassword;
    @SerializedName("database-mysql-name")
    private String databasemysqlname;
    public Instance() {
        this.ip = "127.0.0.1";
        this.queryLogin = "serveradmin";
        this.password = "1234567890";
        this.virtualServerId = 1;
        this.port = 10011;
        this.debug = false;
        this.databasemode = "SQLITE";
        this.databasetableprefix = "chat";
        this.databasemysqlhost = "123.123.123.123";
        this.databasemysqlport = 3036;
        this.databasemysqluser = "root";
        this.databasemysqlpassword = "toor";
        this.databasemysqlname = "chat";

    }
    public int getVirtualServerId() {
        return this.virtualServerId;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean getDebug() {
        return this.debug;
    }

    public String getQueryIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getQueryLogin() {
        return this.queryLogin;
    }

    public String getIp(){return this.ip;}

    public String getDatabasemode() {return this.databasemode;}

    public String getDatabasetableprefix() {return this.databasetableprefix;}

    public String getDatabasemysqlhost() {return this.databasemysqlhost;}

    public Integer getDatabasemysqlport(){return this.databasemysqlport;}

    public String getDatabasemysqluser(){return this.databasemysqluser;}

    public String getDatabasemysqlpassword(){return this.databasemysqlpassword;}

    public String getDatabasemysqlname() {return this.databasemysqlname;}
}

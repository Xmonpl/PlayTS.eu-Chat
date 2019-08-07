package cf.xmon.chat.object;

import cf.xmon.chat.Main;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String name;
    private String uuid;
    private Integer dbid;
    //do ustawienia
    private String select;
    private String channels;
    private String color;
    private String username;
    private String password;
    private long mute;
    private long timeout;
    private Integer money;

    public User(@NotNull Client c){
        new User(c.getUniqueIdentifier());
    }

    public User(String uuid){
        this.name = TeamSpeakUtils.api.getClientByUId(uuid).getNickname().replace("')", "").replace("\")", "").replace("\\\\", "").replace("Ctrl+Z", "").replace("DROP DATABASE", "");
        this.uuid = uuid;
        this.dbid = TeamSpeakUtils.api.getClientByUId(uuid).getDatabaseId();
        this.select = "playts";
        this.channels = "playts@";
        this.color = "null";
        this.username = "null";
        this.password = "null";
        this.mute = System.currentTimeMillis();
        this.timeout = 0;
        this.money = 200;
        UserUtils.addUser(this);
        this.insert();
    }
    public User(@NotNull final ResultSet rs) throws SQLException {
        this.name = rs.getString("name");
        this.uuid = rs.getString("uuid");
        this.dbid = rs.getInt("dbid");
        this.select = rs.getString("select");
        this.channels = rs.getString("channels");
        this.color = rs.getString("color");
        this.mute = rs.getLong("mute");
        this.username = rs.getString("username");
        this.password = rs.getString("password");
        this.timeout = rs.getLong("timeout");
        this.money = rs.getInt("money");
        UserUtils.addUser(this);
    }
    public String getName(){return this.name;}
    public String getUuid(){return this.uuid;}
    public Integer getDbid(){return this.dbid;}
    public String getSelect(){return this.select;}
    public String getChannels(){return this.channels;}
    public String getColor(){return this.color;}
    public long getMute(){return this.mute;}
    public String getUsername(){return this.username;}
    public String getPassword(){return this.password;}
    public long getTimeout(){return this.timeout;}

    public void setSelect(String select){
        this.select = select;
        Main.getStore().update(false, "UPDATE `{P}users` SET `select`='" + this.getSelect() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setChannels(String channels){
        this.channels = channels;
        Main.getStore().update(false, "UPDATE `{P}users` SET `channels`='" + this.getChannels() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setColor(String color){
        this.color = color;
        Main.getStore().update(false, "UPDATE `{P}users` SET `color`='" + this.getColor() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setTimeout(long timeout){
        this.timeout = timeout;
        Main.getStore().update(false, "UPDATE `{P}users` SET `timeout`='" + this.getTimeout() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setMute(long mute){
        this.mute = mute;
        Main.getStore().update(false, "UPDATE `{P}users` SET `mute`='" + this.getMute() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setUsername(String username){
        this.username = username;
        Main.getStore().update(false, "UPDATE `{P}users` SET `username`='" + this.getUsername().replace("')", "").replace("\")", "").replace("\\\\", "").replace("Ctrl+Z", "").replace("DROP DATABASE", "") + "' WHERE `uuid`='" + this.getUuid() + "'");
    }

    public void setPassword(String password){
        this.password = password;
        Main.getStore().update(false, "UPDATE `{P}users` SET `password`='" + this.getPassword().replace("')", "").replace("\")", "").replace("\\\\", "").replace("Ctrl+Z", "").replace("DROP DATABASE", "") + "' WHERE `uuid`='" + this.getUuid() + "'");
    }
    private void insert() {
        Main.getStore().update(false, "INSERT INTO `{P}users`(`id`, `name`, `uuid`, `dbid`, `select`, `channels`, `color`, `mute`, `username`, `password`, `timeout`, `money`) VALUES (NULL, '"
                + this.getName() + "','" + this.getUuid() + "','" + this.getDbid() + "','" + this.getSelect()  + "','" + this.getChannels()  + "','" + this.getColor()  + "','" + this.getMute() + "','" + this.getUsername() + "','" + this.getPassword() + "','" + this.getTimeout() + "','" + this.getMoney() + "')");
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
        Main.getStore().update(false, "UPDATE `{P}users` SET `money`='" + this.getMoney() + "' WHERE `uuid`='" + this.getUuid() + "'");
    }
}

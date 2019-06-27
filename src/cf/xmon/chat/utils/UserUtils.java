package cf.xmon.chat.utils;

import cf.xmon.chat.Main;
import cf.xmon.chat.object.User;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static cf.xmon.chat.Main.channels;

public class UserUtils {
    public static Map<String, Integer> max = new HashMap<>();
    public static Map<String, Integer> online = new HashMap<>();
    private static List<User> users = new ArrayList<User>();
    public static List<User> getUsers() {
        return users;
    }
    public static void addUser(User u) {
        users.add(u);
    }
    public static void remUser(User u) { users.remove(u); }
    public static User get(Client c) {
        return get(c.getUniqueIdentifier());
    }

    public static User get(String uuid) {
        User u = UserUtils.users.stream().filter(user -> user.getUuid().toLowerCase().equals(uuid.toLowerCase())).findFirst().orElse(null);
        return (u == null) ? new User(uuid) : u;
    }
    public static void load() {
        try {
            ResultSet rs = Main.getStore().query("SELECT * FROM `{P}users`");
            while (rs.next()) {
                User u = new User(rs);
                UserUtils.users.add(u);
            }
            rs.close();
            Logger.info("Loaded " + UserUtils.users.size() + " users");
        }
        catch (SQLException e) {
            Logger.info("Can not load players Error " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static boolean oneUsername(String nick){
        User u = users.stream().filter(user -> user.getUsername().equals(nick)).findFirst().orElse(null);
        if (u == null){
            return true;
        }else{
            return false;
        }
    }
    public static String ehkem = "-";
    public static void loadOnline(){
        online.clear();
        max.clear();
        Arrays.stream(channels.split("@")).forEach(x ->{
            if (!x.equals("")) {
                if (online.get(x) == null) {
                    online.put(x.toLowerCase(), 0);
                }
                if (max.get(x) == null) {
                    max.put(x.toLowerCase(), 0);
                }
            }
        });
        users.forEach(u ->{
            if (!ehkem.equals(u.getName())) {
                if (!u.getChannels().equals("")) {
                    Arrays.stream(u.getChannels().split("@")).forEach(y -> {
                        if (!y.equals("")) {
                            if (TeamSpeakUtils.api.isClientOnline(u.getUuid())) {
                                online.put(y.toLowerCase(), online.get(y.toLowerCase()) + 1);
                            }
                            max.put(y.toLowerCase(), max.get(y.toLowerCase()) + 1);
                        }
                    });
                    ehkem = u.getName();
                }
            }
        });
    }
}

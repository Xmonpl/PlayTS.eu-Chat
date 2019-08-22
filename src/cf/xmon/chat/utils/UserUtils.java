package cf.xmon.chat.utils;

import cf.xmon.chat.Main;
import cf.xmon.chat.object.User;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static cf.xmon.chat.Main.channels;

public class UserUtils {
    //public static Map<String, Integer> max = new ConcurrentHashMap<>();
    public static Object2ObjectMap max = new Object2ObjectOpenHashMap();
    //public static Map<String, Integer> maxnew = new ConcurrentHashMap<>();
    public static Object2ObjectMap maxnew = new Object2ObjectOpenHashMap();
    //public static Map<String, Integer> online = new ConcurrentHashMap<>();
    public static Object2ObjectMap online = new Object2ObjectOpenHashMap();
    //public static Map<String, Integer> onlinenew = new ConcurrentHashMap<>();
    public static Object2ObjectMap onlinenew = new Object2ObjectOpenHashMap();
    private static ObjectList<User> users = new ObjectArrayList<User>();
    public static ObjectList<User> getUsers() {
        return users;
    }
    public static void addUser(User u) {
        users.add(u);
    }
    public static void remUser(User u) { users.remove(u); }
    public static User get(@NotNull Client c) {
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
            Logger.info("Loaded " + UserUtils.users.size()/2 + " users");
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
    public static User getUserByUsername(String username){
        //User u = UserUtils.users.stream().filter(user -> user.getUuid().toLowerCase().equals(uuid.toLowerCase())).findFirst().orElse(null);
        User u = UserUtils.users.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);
        if (u == null){
            return null;
        }else{
            return u;
        }
    }
    public static void loadOnline(){
        long start = System.currentTimeMillis();
        onlinenew.clear();
        maxnew.clear();
        online.clear();
        max.clear();
        System.out.println("Czyszczenie online oraz max: " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        Arrays.stream(channels.split("@")).forEach(x ->{
            if (!x.equals("")) {
                if (onlinenew.get(x) == null) {
                    onlinenew.put(x.toLowerCase(), 0);
                }
                if (maxnew.get(x) == null) {
                    maxnew.put(x.toLowerCase(), 0);
                }
            }
        });
        System.out.println("Pierwszy forEach(channel == null? online 0; max 0): " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        // Teraz mnie kurwa nie wkurwiaj, ze to nie dziala. EDIT DZIALA !!!!!!!!
        String chujcieto = "-";
        for (User u : new ObjectArrayList<User>(users)){
            if (!chujcieto.equals(u.getName())) {
                chujcieto = u.getName();
                if (!u.getChannels().equals("")) {
                    Arrays.stream(u.getChannels().split("@")).forEach(y -> {
                        if (!y.equals("")) {
                            if (TeamSpeakUtils.api.isClientOnline(u.getUuid())) {
                                onlinenew.put(y.toLowerCase(), (Integer) onlinenew.get(y.toLowerCase()) + 1);
                            }
                            maxnew.put(y.toLowerCase(), (Integer) maxnew.get(y.toLowerCase()) + 1);
                        }
                    });
                }
            }
        }
        online = onlinenew;
        max = maxnew;
        System.out.println("For: " + (System.currentTimeMillis() - start)  + "ms");

    }
}

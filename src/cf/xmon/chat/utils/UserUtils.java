package cf.xmon.chat.utils;

import cf.xmon.chat.Main;
import cf.xmon.chat.object.User;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cf.xmon.chat.Main.channels;

public class UserUtils {
    public static Map<String, Integer> maxnew = new ConcurrentHashMap<>();
    public static Map<String, Integer> onlinenew = new ConcurrentHashMap<>();
    private static LinkedHashSet<User> users = new LinkedHashSet<User>();
    public static LinkedHashSet<User> getUsers() {
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
        } catch (Exception ex) {
            TeamSpeakUtils.error(ex);
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
    public static User getUserByNickName(String username){
        //User u = UserUtils.users.stream().filter(user -> user.getUuid().toLowerCase().equals(uuid.toLowerCase())).findFirst().orElse(null);
        User u = UserUtils.users.stream().filter(user -> user.getName().equals(username)).findFirst().orElse(null);
        if (u == null){
            u = UserUtils.users.stream().filter(user -> user.getUuid().equals(username)).findFirst().orElse(null);
            if (u == null) {
                return null;
            }else{
                return u;
            }
        }else{
            return u;
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
        System.out.println("Czyszczenie online oraz max: " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        /*
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
         */
        Arrays.stream(channels.split("@")).filter(x -> !x.isEmpty()).forEach(x ->{
            onlinenew.put(x.toLowerCase(), 0);
            maxnew.put(x.toLowerCase(), 0);
        });
        System.out.println("Pierwszy forEach(channel == null? online 0; max 0): " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        // Teraz mnie kurwa nie wkurwiaj, ze to nie dziala. EDIT DZIALA !!!!!!!!
        //String chujcieto = "-";
        LinkedHashSet<User> uu = new LinkedHashSet<User>(users);
        //List<User> uu = new ArrayList<User>(users);
        uu.stream().filter(x -> !(x.getChannels().equals(""))).forEach(u ->{
            Arrays.stream(u.getChannels().split("@")).filter(y -> !y.isEmpty()).forEach(y -> {
                if (TeamSpeakUtils.query.getApi().isClientOnline(u.getUuid())) {
                    onlinenew.put(y.toLowerCase(), (Integer) onlinenew.get(y.toLowerCase()) + 1);
                }
                maxnew.put(y.toLowerCase(), (Integer) maxnew.get(y.toLowerCase()) + 1);
            });
        });

        /*

                uu.stream().filter(x -> !(x.getChannels().equals(""))).forEach(u ->{
            Arrays.stream(u.getChannels().split("@")).filter(y -> !y.isEmpty()).forEach(y -> {
                    if (TeamSpeakUtils.query.getApi().isClientOnline(u.getUuid())) {
                        onlinenew.put(y.toLowerCase(), (Integer) onlinenew.get(y.toLowerCase()) + 1);
                    }
                    maxnew.put(y.toLowerCase(), (Integer) maxnew.get(y.toLowerCase()) + 1);
            });
        });

        uu.parallelStream()
                .filter(user -> !user.getChannels().isEmpty())
                .forEach(user -> {
                    System.out.println(user.getName());
                    Arrays.stream(user.getChannels().split("@"))
                            .parallel()
                            .filter(channel -> !channel.isEmpty())
                            .forEach(channel -> {
                                if (TeamSpeakUtils.query.getApi().isClientOnline(user.getUuid())){
                                    onlinenew.put(channel.toLowerCase(), onlinenew.get(channel.toLowerCase()) + 1);
                                }
                                maxnew.put(channel.toLowerCase(), (Integer) maxnew.get(channel.toLowerCase()) + 1);
                            });
                });
        for (User u : new ArrayList<User>(users)){
            if (!chujcieto.equals(u.getName())) {
                chujcieto = u.getName();
                if (!u.getChannels().equals("")) {
                    Arrays.stream(u.getChannels().split("@")).forEach(y -> {
                        if (!y.equals("")) {
                            if (TeamSpeakUtils.query.getApi().isClientOnline(u.getUuid())) {
                                onlinenew.put(y.toLowerCase(), (Integer) onlinenew.get(y.toLowerCase()) + 1);
                            }
                            maxnew.put(y.toLowerCase(), (Integer) maxnew.get(y.toLowerCase()) + 1);
                        }
                    });
                }
            }
        }
         */
        System.out.println("For: " + (System.currentTimeMillis() - start)  + "ms");

    }

}

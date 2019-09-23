package cf.xmon.chat;

import cf.xmon.chat.config.Config;
import cf.xmon.chat.config.ConfigManager;
import cf.xmon.chat.database.Store;
import cf.xmon.chat.database.StoreMode;
import cf.xmon.chat.database.StoreMySQL;
import cf.xmon.chat.database.StoreSQLITE;
import cf.xmon.chat.object.User;
import cf.xmon.chat.tasks.AntyCrash;
import cf.xmon.chat.tasks.AutoClearChannelsTask;
import cf.xmon.chat.tasks.OnlineTask;
import cf.xmon.chat.utils.MessageUtils;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static Store store;
    private static ConfigManager cm = new ConfigManager();
    public static Config c = ConfigManager.getConfig();
    public static String channels = null;
    public static Map<String, Integer> max = new ConcurrentHashMap<>();
    public static Map<String, Integer> online = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        final long start = System.currentTimeMillis();
        registerDatabase();
        registerChannels();
        UserUtils.load();
        createFiles();
        TeamSpeakUtils.TeamSpeakConnect(c.getInstance().getQueryIp(), c.getInstance().getPort(), c.getInstance().getDebug(), c.getInstance().getQueryLogin(), c.getInstance().getPassword(), c.getInstance().getVirtualServerId());
        UserUtils.loadOnline();
        online.putAll(UserUtils.onlinenew);
        max.putAll(UserUtils.maxnew);
        onload(args);
        //ServerCreator.createServer();
        OnlineTask.update();
        AutoClearChannelsTask.update();
        AntyCrash.update();
        AntyCrash.update1();
        System.out.println("Uruchomiono w " + (System.currentTimeMillis() - start) + "ms!");
        System.out.println("x-Chat created by Xmon for PlayTS.eu (https://github.com/xmonpl)");
    }

    private static void onload(String[] args) throws IOException {
        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
        TeamSpeakUtils.api.getClients().forEach(x ->{
            if (x.isRegularClient() && !x.isInServerGroup(36) && !x.isInServerGroup(92)) {
                System.out.println(x.getNickname());
                try {
                    User u = UserUtils.getUsers().stream().filter(user -> user.getUuid().toLowerCase().equals(x.getUniqueIdentifier().toLowerCase())).findFirst().orElse(null);
                    if (u == null) {
                        new User(x.getUniqueIdentifier());
                        if (args.length == 0) {
                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                                    "Automatycznie dołączono" +
                                    " do kanału [color=#f4511e]#playts[/color] ([color=#43a047]" + online.get("playts") + "/" + max.get("playts") + " online[/color]). [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#playts[/color]:[/i][/b]\n");
                            File file = new File(jsonObject.getJSONObject("playts").getString("file"));
                            int n_lines = 5;
                            int counter = 0;
                            ReversedLinesFileReader object = null;
                            object = new ReversedLinesFileReader(file);
                            List<String> s = new ArrayList();
                            while (counter < n_lines) {
                                s.add(counter, object.readLine() + "\n");
                                counter++;
                            }
                            String ss = "";
                            StringBuilder sb = new StringBuilder(ss);
                            MessageUtils.reverseList(s).forEach(y -> {
                                sb.append(y);
                            });
                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "\n" + sb.toString());
                        }
                    } else {
                        if (args.length == 0) {
                            String sads = "";
                            StringBuilder sbb = new StringBuilder(sads);
                            Arrays.stream(u.getChannels().split("@")).forEach(y -> {
                                sbb.append("[color=#f4511e]#" + y.toLowerCase() + "[/color] ([color=#43a047]" + online.get(y.toLowerCase()) + "/" + max.get(y.toLowerCase()) + " online[/color]), ");
                            });
                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                                    "Znajdujesz się w kanałach: " + sbb.toString() + " [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#" + u.getSelect().toLowerCase() + "[/color]:[/i][/b]\n");
                            File file = new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file"));
                            int n_lines = 5;
                            int counter = 0;
                            ReversedLinesFileReader object = null;
                            object = new ReversedLinesFileReader(file);
                            List<String> s = new ArrayList();
                            while (counter < n_lines) {
                                s.add(counter, object.readLine() + "\n");
                                counter++;
                            }
                            String ss = "";
                            StringBuilder sb = new StringBuilder(ss);
                            MessageUtils.reverseList(s).forEach(z -> {
                                sb.append(z);
                            });
                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "\n" + sb.toString());
                        }
                    }
                } catch (Exception ex) {
                    TeamSpeakUtils.error(ex);
                }
            }
        });
    }

    private static boolean registerDatabase() {
        switch (StoreMode.getByName(c.getInstance().getDatabasemode())) {
            case MYSQL: {
                Main.store = new StoreMySQL(c.getInstance().getDatabasemysqlhost(), c.getInstance().getDatabasemysqlport(), c.getInstance().getDatabasemysqluser(), c.getInstance().getDatabasemysqlpassword(), c.getInstance().getDatabasemysqlname(), c.getInstance().getDatabasetableprefix());
                break;
            }
            case SQLITE: {
                Main.store = new StoreSQLITE("xmon-chat.db", c.getInstance().getDatabasetableprefix());
                break;
            }
            default: {
                System.err.println("Value of databse mode is not valid! Using SQLITE as database!");
                Main.store = new StoreSQLITE("xmon-chat.db", c.getInstance().getDatabasetableprefix());
                break;
            }
        }
        final boolean conn = Main.store.connect();
        if (conn) {
            Main.store.update(true, "CREATE TABLE IF NOT EXISTS `{P}users` (" + ((Main.store.getStoreMode() == StoreMode.MYSQL) ? "`id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT," : "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ") + "`name` varchar(30) NOT NULL, `uuid` varchar(29) NOT NULL, `dbid` int(11) NOT NULL, `select` varchar(32) NOT NULL, `channels` varchar(2048) NOT NULL, `color` varchar(32) NOT NULL, `mute` TIMESTAMP NOT NULL, `username` varchar(32) NOT NULL, `password` varchar(128) NOT NULL, `timeout` TIMESTAMP NOT NULL, `money` INT(2147483646) NOT NULL);");
            return conn;
        }
        return conn;
    }
    public static void registerChannels() {
        try {
            JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
            Iterator<String> keys = jsonObject.keys();
            String add = new String();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    add += "@" + key;
                }
            }
            channels = add;
            System.out.println(channels);
        }catch (Exception e){
            TeamSpeakUtils.error(e);
        }
    }
    @NotNull
    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
    }
    @Contract(pure = true)
    public static Store getStore() {
        return store;
    }

    public static void createFiles(){
        try {
            JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
            String write = "\uD83D\uDCAC [color=#5e6165]" + MessageUtils.getTime() + "[/color][color=#2580c3][b]Maniek[/b][/color]: Witam co tam u was?\n\uD83D\uDCAC [color=#5e6165]" + MessageUtils.getTime() + "[/color][color=#2580c3][b]Eustachy[/b][/color]: a dobrze a u ciebie?\n\uD83D\uDCAC [color=#5e6165]" + MessageUtils.getTime() + "[/color][color=#2580c3][b]Maniek[/b][/color]: super, własnie pije sobie wode z lodem.\n\uD83D\uDCAC [color=#5e6165]" + MessageUtils.getTime() + "[/color][color=#2580c3][b]Eustachy[/b][/color]: to fajnie xd\n\uD83D\uDCAC [color=#5e6165]" + MessageUtils.getTime() + "[/color] \uD83D\uDD27 [color=#2580c3][b]Xmon[/b][/color]: Witam wszystkich!\n";
            Arrays.stream(channels.split("@")).forEach(x ->{
                if (!x.equals("")) {
                    File f = new File(jsonObject.getJSONObject(x).getString("file"));
                    if (!f.exists()) {
                        try {
                            Files.write(Paths.get(f.getName(), new String[0]), write.getBytes(), StandardOpenOption.CREATE_NEW);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            if (!new File("rules_PL.txt").exists()){
                Files.write(Paths.get("rules_PL.txt", new String[0]), "[b]1. nie ma zasad[/b]{NEW}[b]Edit. jednak są ale Xmonowi się nie chce pisać (Matis ma napisac)[/b]".getBytes(), StandardOpenOption.CREATE_NEW);
            }
            if (!new File("rules_DE.txt").exists()){
                Files.write(Paths.get("rules_DE.txt", new String[0]), "[b]1. nie ma zasad[/b]{NEW}[b]Edit. jednak są ale Xmonowi się nie chce pisać (Matis ma napisac)[/b]".getBytes(), StandardOpenOption.CREATE_NEW);
            }
            if (!new File("rules_EN.txt").exists()){
                Files.write(Paths.get("rules_EN.txt", new String[0]), "[b]1. nie ma zasad[/b]{NEW}[b]Edit. jednak są ale Xmonowi się nie chce pisać (Matis ma napisac)[/b]".getBytes(), StandardOpenOption.CREATE_NEW);
            }
            if (!new File("help_PL.txt").exists()){
                Files.write(Paths.get("help_PL.txt", new String[0]), "[b]Dostępne komendy:[/b]{NEW}[b]!channels[/b] — Wyświetla wszystkie dostępne kanały.{NEW}[b]!join <nazwa>[/b] — Dołącza do wybranego kanału{NEW}[b]!leave <nazwa>[/b] — Wychodzi z wybranego kanału tekstowego.{NEW}[b]!mute <3s/5m/6h>[/b] — Wycisza chat na podaną ilość sekund/minut/godzin.{NEW}[b]!switch <nazwa>[/b] — Przełącza twoje odpowiedzi na wybrany kanał.{NEW}[b]!check <nazwa>[/b] — Sprawdza ostatnie 15 wiadomości, bez dołączania do kanału.{NEW}[b]!rainbow on/off[/b] — Zmienia kolor twoich wiadomosci na tęczowy. [Premium]{NEW}[b]!admin[/b] — Wyświetla pomoc dostępną tylko dla Administratorów.{NEW}[b]!botinfo[/b] — Wyswietla informacje dotyczące autora.".getBytes(), StandardOpenOption.CREATE_NEW);
            }
            if (!new File("help_EN.txt").exists()){
                Files.write(Paths.get("help_EN.txt", new String[0]), "[b]Available commands:[/b]{NEW}[b]!channels[/b] — Displays all available channels.{NEW}[b]!join <nazwa>[/b] — Joins the selected channel.{NEW}[b]!leave <nazwa>[/b] — Exits from the selected text channel.{NEW}[b]!mute <3s/5m/6h>[/b] — Mute the chat to the specified number of seconds / minutes / hours.{NEW}[b]!switch <nazwa>[/b] — Switches your answers to the selected channel.{NEW}[b]!check <nazwa>[/b] — Checks the last 15 messages without joining the channel.{NEW}[b]!color <#ffea00>[/b] — Changes the color of your messages. [Plus/Premium]{NEW}[b]!rainbow on/off[/b] — Changes the color of your messages to the rainbow. [Premium]{NEW}[b]!admin[/b] — Displays help available only to Administrators.{NEW}[b]!botinfo[/b] — Displays information about the author.".getBytes(), StandardOpenOption.CREATE_NEW);
            }
            if (!new File("help_DE.txt").exists()){
                Files.write(Paths.get("help_DE.txt", new String[0]), "[b]Dostępne komendy:[/b]{NEW}[b]!channels[/b] — Wyświetla wszystkie dostępne kanały.{NEW}[b]!join <nazwa>[/b] — Dołącza do wybranego kanału{NEW}[b]!leave <nazwa>[/b] — Wychodzi z wybranego kanału tekstowego.{NEW}[b]!mute <3s/5m/6h>[/b] — Wycisza chat na podaną ilość sekund/minut/godzin.{NEW}[b]!switch <nazwa>[/b] — Przełącza twoje odpowiedzi na wybrany kanał.{NEW}[b]!check <nazwa>[/b] — Sprawdza ostatnie 15 wiadomości, bez dołączania do kanału.{NEW}[b]!rainbow on/off[/b] — Zmienia kolor twoich wiadomosci na tęczowy. [Premium]{NEW}[b]!admin[/b] — Wyświetla pomoc dostępną tylko dla Administratorów.{NEW}[b]!botinfo[/b] — Wyswietla informacje dotyczące autora.".getBytes(), StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception ex) {
            TeamSpeakUtils.error(ex);
        }
    }
}

package cf.xmon.chat.utils;

import cf.xmon.chat.config.Config;
import cf.xmon.chat.config.ConfigManager;
import cf.xmon.chat.events.ChatEvent;
import cf.xmon.chat.events.JoinEvent;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.sun.istack.internal.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cf.xmon.chat.Main.parseJSONFile;

public class TeamSpeakUtils {
    public static TS3Query query;
    public static void TeamSpeakConnector(){
        System.out.println("Loading config...");
        final TS3Config config = new TS3Config();
        Config c = ConfigManager.getConfig();
        config.setHost(c.getInstance().getQueryIp());
        config.setQueryPort(c.getInstance().getPort());
        config.setFloodRate(TS3Query.FloodRate.UNLIMITED);
        config.setEnableCommunicationsLogging(c.getInstance().getDebug());
        config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff());
        config.setConnectionHandler(new ConnectionHandler() {
            @Override
            public void onConnect(TS3Api ts3Api) {
                connect(ts3Api, c);
            }

            @Override
            public void onDisconnect(TS3Query ts3Query) {

            }
        });
        System.out.println("Config loaded.");
        System.out.println("Query connecting...");
        query = new TS3Query(config);
        query.connect();
        System.out.println("Query connected.");
        query.getApi().addTS3Listeners(new ChatEvent());
        query.getApi().addTS3Listeners(new JoinEvent());
        System.out.println("Loaded all events!");
    }
    private static void connect (TS3Api ts3Api, Config c){
        ts3Api.login(c.getInstance().getQueryLogin(), c.getInstance().getPassword());
        ts3Api.selectVirtualServerByPort(9987,"Chat");
        ts3Api.moveClient(ts3Api.whoAmI().getId(), 1216);
        query.getApi().registerEvent(TS3EventType.TEXT_PRIVATE);
        query.getApi().registerEvent(TS3EventType.SERVER);
    }
    /*
    public static void TeamSpeakConnect(@NotNull String queryip, @NotNull int queryport, @NotNull boolean debug, @NotNull String querylogin, @NotNull String querypassword, @NotNull int virtualserverid) {
        System.out.println("Trwa Łączenie..");
        (TeamSpeakUtils.config = new TS3Config()).setHost(queryip);
        TeamSpeakUtils.config.setQueryPort(queryport);
        TeamSpeakUtils.config.setEnableCommunicationsLogging(debug);
        TeamSpeakUtils.config.setFloodRate(TS3Query.FloodRate.UNLIMITED);
        TeamSpeakUtils.config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff(10, 10));
        (TeamSpeakUtils.query = new TS3Query(TeamSpeakUtils.config)).connect();
        System.out.println("Połączono poprawnie");
        System.out.println("Trwa logowanie..");
        (TeamSpeakUtils.api = TeamSpeakUtils.query.getApi()).login(querylogin, querypassword);
        TeamSpeakUtils.api.selectVirtualServerById(virtualserverid, "Chat");
        TeamSpeakUtils.api.moveClient(TeamSpeakUtils.api.whoAmI().getId(), 5270);
        System.out.println("Logowanie przebiegło pomyślnie!");
        api.registerEvent(TS3EventType.TEXT_PRIVATE);
        api.registerEvent(TS3EventType.SERVER);
        TeamSpeakUtils.api.addTS3Listeners(new ChatEvent());
        TeamSpeakUtils.api.addTS3Listeners(new JoinEvent());
    }
     */
    public static String getTimeFromLong(Long time){
        Date d = new Date(time);
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
        return df2.format(d);
    }
    public static void error (Exception e){
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Date now = new Date();
        final String strDate = sdfDate.format(now);
        error("[x-Chat]");
        error(" Java: " + System.getProperty("java.version"));
        error(" Thread: " + Thread.currentThread());
        error(" Time: " + strDate);
        error(" Błąd: " + e.toString());
        for (int i = 0; i < e.getStackTrace().length; i++){
            String[] splited = e.getStackTrace()[i].toString().split("\\(");
            if (splited[0].contains("cf.xmon")) {
                String line = splited[1];
                line = line.replace(":",  " | Linijka: ");
                line = line.replace("\\)", "");
                error(" Klasa: " + line);
            }
        }
    }
    private static void error(String message){
        System.err.println(message);
        if (query.isConnected()) {
            query.getApi().getClients().stream().filter(client -> client.isInServerGroup(6)).filter(client -> client.isInServerGroup(16)).forEach(client -> {
                query.getApi().sendPrivateMessage(client.getId(), message);
            });
        }
    }
    public static String getRainbowColor(){
        Random random = new Random();
        int nextInt = random.nextInt(0xffffff + 1);
        String colorCode = String.format("#%06x", nextInt);
        return colorCode;
    }
    public static void sendMultiLanguagePrivateMessage(@NotNull String[] Poland$English$Niemcy, @NotNull Client c){
        String country = c.getCountry().toLowerCase();
        if (country.equalsIgnoreCase("pl")){
            query.getApi().sendPrivateMessage(c.getId(), Poland$English$Niemcy[0]);
        }else if(country.equalsIgnoreCase("de")){
            query.getApi().sendPrivateMessage(c.getId(), Poland$English$Niemcy[1]);
        }else{
            query.getApi().sendPrivateMessage(c.getId(), Poland$English$Niemcy[1]);
        }
    }
    public static String sendMultiLanguagePrivateMessage(@NotNull String[] Poland$English$Niemcy, @NotNull String c){
        if (c.equalsIgnoreCase("pl")){
            return Poland$English$Niemcy[0];
        }else if(c.equalsIgnoreCase("de")){
            return Poland$English$Niemcy[1];
        }else{
            return Poland$English$Niemcy[1];
        }
    }
    public static boolean canUse(final long saveTime, final long time) {
        return System.currentTimeMillis() - saveTime >= time;
    }
    public static boolean getRequired(Client c, String channel) throws IOException {
        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
        String required = jsonObject.getJSONObject(channel).getString("required");
        Logger.info(required);
        if (required.equals("none")){
            return true;
        }else if (required.equals("age18")){
            if (c.isInServerGroup(51) || c.isInServerGroup(52) || c.isInServerGroup(53) || c.isInServerGroup(93)){
                return true;
            }else{
                return false;
            }
        }else if (required.equals("age16")){
            if (c.isInServerGroup(50) || c.isInServerGroup(51) || c.isInServerGroup(52) || c.isInServerGroup(53) || c.isInServerGroup(93)){
                return true;
            }else{
                return false;
            }
        }else if (required.equals("admin")) {
            if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(17) || c.isInServerGroup(26)) {
                return true;
            } else {
                return false;
            }
        }else if (required.equals("plus")){
            if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(122) || c.isInServerGroup(123)){
                return true;
            }else{
                return false;
            }
        }else if (required.equals("premium")){
            if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(123)){
                return true;
            }else{
                return false;
            }
        }else if(required.contains("groupid-")){
            if (c.isInServerGroup(Integer.parseInt(required.split("-")[1]))){
                return true;
            }else{
                return false;
            }
        } else{
            return false;
        }
    }
    @NotNull
    public static String getDate(long czas)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
        return sdf.format(new Date(czas));
    }
    public static long getTimeWithString(String s)
    {
        Pattern pattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[, \\s]*)?(?:([0-9]+)\\s*mo[a-z]*[, \\s]*)?(?:([0-9]+)\\s*d[a-z]*[, \\s]*)?(?:([0-9]+)\\s*h[a-z]*[, \\s]*)?(?:([0-9]+)\\s*m[a-z]*[, \\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
        Matcher matcher = pattern.matcher(s);
        long czas = 0L;
        boolean found = false;
        while (matcher.find()) {
            if ((matcher.group() != null) && (!matcher.group().isEmpty()))
            {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    if ((matcher.group(i) != null) && (!matcher.group(i).isEmpty()))
                    {
                        found = true;
                        break;
                    }
                }
                if ((matcher.group(1) != null) && (!matcher.group(1).isEmpty())) {
                    czas += 31536000 * Integer.valueOf(matcher.group(1)).intValue();
                }
                if ((matcher.group(2) != null) && (!matcher.group(2).isEmpty())) {
                    czas += 2592000 * Integer.valueOf(matcher.group(2)).intValue();
                }
                if ((matcher.group(3) != null) && (!matcher.group(3).isEmpty())) {
                    czas += 86400 * Integer.valueOf(matcher.group(3)).intValue();
                }
                if ((matcher.group(4) != null) && (!matcher.group(4).isEmpty())) {
                    czas += 3600 * Integer.valueOf(matcher.group(4)).intValue();
                }
                if ((matcher.group(5) != null) && (!matcher.group(5).isEmpty())) {
                    czas += 60 * Integer.valueOf(matcher.group(5)).intValue();
                }
                if ((matcher.group(6) != null) && (!matcher.group(6).isEmpty())) {
                    czas += Integer.valueOf(matcher.group(6)).intValue();
                }
            }
        }
        if (!found) {
            return -1L;
        }
        return czas * 1000L;
    }
}

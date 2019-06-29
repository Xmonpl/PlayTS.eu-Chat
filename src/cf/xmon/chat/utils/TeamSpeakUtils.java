package cf.xmon.chat.utils;

import cf.xmon.chat.events.ChatEvent;
import cf.xmon.chat.events.JoinEvent;
import cf.xmon.chat.tasks.OnlineTask;
import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
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
    public static TS3Config config;
    public static TS3Query query;
    public static TS3Api api;
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
        //TeamSpeakUtils.api.moveClient(TeamSpeakUtils.api.whoAmI().getId(), 5270);
        System.out.println("Logowanie przebiegło pomyślnie!");
        api.registerEvent(TS3EventType.TEXT_PRIVATE);
        api.registerEvent(TS3EventType.SERVER);
        TeamSpeakUtils.api.addTS3Listeners(new ChatEvent());
        TeamSpeakUtils.api.addTS3Listeners(new JoinEvent());
        OnlineTask.update();
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
            api.sendPrivateMessage(c.getId(), Poland$English$Niemcy[0]);
        }else if(country.equalsIgnoreCase("de")){
            api.sendPrivateMessage(c.getId(), Poland$English$Niemcy[2]);
        }else{
            api.sendPrivateMessage(c.getId(), Poland$English$Niemcy[1]);
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

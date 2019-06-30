package cf.xmon.chat.events;

import cf.xmon.chat.Main;
import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.Logger;
import cf.xmon.chat.utils.MessageUtils;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static cf.xmon.chat.Main.parseJSONFile;

public class ChatEvent extends TS3EventAdapter {
    private static Integer charlimit = 256;
    private static Integer slowdowntime = 5;
    public static Pattern URL_PATTERN;
    public static Pattern IPPATTERN;
    public static Pattern uppercase;
    private Map<String, Long> slowdown = new HashMap<>();
    public static Pattern BANNED_WORDS;
    static {
        URL_PATTERN = Pattern.compile("(.*(www\\.|http://|https://|ftp://).*|.*\\.(com|pl|eu|org|net|yt|tk|ga|cf|me|ml|gq|xyz|online).*|.*ser[vw]er.*|.*s e r [vw] e r.*|.*info.*)");
        IPPATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        BANNED_WORDS = Pattern.compile("(.*chuj.*|.*huj.*|.*cip.*|.*jeb[aicny].*|.*pieprz[oyan].*|.*pierd[aoz].*|.*kurw.*|.*kurews.*|.*kutas.*|.*matkojebc.*|.*pizd.*|.*piczka.*|.*qrwa.*|.*pojeb.*|.*sukinsy.*|.*sraj.*|.*sram.*|.*ssij.*|.*gnoju.*|.*lamus.*|.*pipa.*|.*dupa.*|.*rucha.*|.*dild[o0].*)");
        uppercase = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    }
    @Override
    public void onTextMessage(@NotNull TextMessageEvent e){
        if(!e.getInvokerName().equals("PlayTS.eu @ Chat")) {
            Client c = TeamSpeakUtils.api.getClientInfo(e.getInvokerId());
            if (!c.isServerQueryClient()){
                User u = UserUtils.get(c.getUniqueIdentifier());
                String message = e.getMessage();
                String[] args = message.split(" ");
                if (!c.isInServerGroup(6) && slowdown.containsKey(c.getUniqueIdentifier()) && !TeamSpeakUtils.canUse(slowdown.get(c.getUniqueIdentifier()), slowdowntime * 1000)) {
                    String time = Long.toString(slowdowntime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - slowdown.get(c.getUniqueIdentifier())));
                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Następną wiadomość możesz wysłać za " + time + " sekund.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You can send the next message in " + time + " seconds.", "brak"}, c);
                }else {
                    //join command
                    if (args[0].equalsIgnoreCase("!join") || args[0].equalsIgnoreCase("!dolacz") || args[0].equalsIgnoreCase("!beitreten")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Znajdujesz się już w tym kanale.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are already in this channel.", "brak"}, c);
                                } else {
                                    try {
                                        if (TeamSpeakUtils.getRequired(c, args[1])) {
                                            u.setChannels(u.getChannels() + args[1].toLowerCase() + "@");
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]dołączyłeś/aś do kanału [color=#f4511e][B]#" + args[1].toLowerCase() +"[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]). [b][i]Ostatnie 5 wiadomości:[/i][/b]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you've joined the channel [color=#f4511e][B]#" + args[1].toLowerCase() +"[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]). [b][i]Last 5 messages:[/i][/b]", "brak"}, c);
                                            JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                            File file = new File(jsonObject.getJSONObject(args[1]).getString("file"));
                                            int n_lines = 5;
                                            int counter = 0;
                                            ReversedLinesFileReader object = null;
                                            object = new ReversedLinesFileReader(file);
                                            List<String> s = new ArrayList();
                                            while(counter < n_lines) {
                                                s.add(counter ,object.readLine() + "\n");
                                                counter++;
                                            }
                                            String ss = "";
                                            StringBuilder sb = new StringBuilder(ss);
                                            MessageUtils.reverseList(s).forEach(x -> {
                                                sb.append(x);
                                            });
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "\n" + sb.toString());
                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                        } else {
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie możesz dołączyć do kanału #" + args[1].toLowerCase() + ".", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You can not join the channel #" + args[1].toLowerCase() + ".", "brak"}, c);
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.",  "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !join <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !join <channel_name>", "brak"}, c);
                        }
                    }
                    /*@TODO do zrobienia, dodanie hashu oraz panelu www
                    if (args[0].equalsIgnoreCase("!register") || args[0].equalsIgnoreCase("!registrieren") || args[0].equalsIgnoreCase("!zarejestruj")){
                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "balblablaballablbalbalablalalbalbalblablal");
                    }
                    //registerby
                    if (args[0].equalsIgnoreCase("!registerby")){
                        if (args.length == 1){
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !registerby <teamspeak/www>");
                        }else {
                            if (args[1].equalsIgnoreCase("www")) {
                                if (args.length == 2) {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "przez www");
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !registerby www");
                                }
                            } else {
                                if (args[1].equalsIgnoreCase("teamspeak")) {
                                    if (args.length == 3) {
                                        if (UserUtils.oneUsername(c.getNickname())){
                                            if (args[2].length() > 16){
                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Twoje hasło jest za długie! (max 16 znaków)");
                                            }else{
                                                if (uppercase.matcher(args[2]).matches()){
                                                    u.setUsername(c.getNickname());

                                                }else{
                                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Hasło musi posiadać duże, małe litery oraz cyfre");
                                                }
                                            }
                                        }else{
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Zmień nick! Taki nick jak twój zawiera już nasza baza danych!");
                                        }
                                    } else {
                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !registerby teamspeak <password>");
                                    }
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !registerby <teamspeak/www>");
                                }
                            }
                        }
                    }
                    */
                    if (args[0].equalsIgnoreCase("!timeout")){
                        if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(17) || c.isInServerGroup(26) || c.isInServerGroup(75)) {

                            if (args[1].equalsIgnoreCase("revoke")){
                                if (args.length == 3) {
                                    String uuid = args[2].split("/")[3].replaceAll("~.+", "");
                                    User ua = UserUtils.get(uuid);
                                    ua.setTimeout(System.currentTimeMillis());
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Użytkownik " + ua.getName() + " został odciszony!");
                                }else{
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !timeout revoke <przenies klienta>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !timeout revoke <move client>", "brak"}, c);
                                }
                            }else {
                                if (args.length == 3) {
                                    try {
                                        long czas = System.currentTimeMillis() + TeamSpeakUtils.getTimeWithString(args[2]);
                                        String uuid = args[1].split("/")[3].replaceAll("~.+", "");
                                        User ua = UserUtils.get(uuid);
                                        ua.setTimeout(czas);
                                        slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                        String parse = MessageUtils.parserMessage("System", "[color=#00bcd4]Użytkownik " + ua.getName() + " został [color=#d50000]wyciszony[/color], do [B]" + TeamSpeakUtils.getDate(czas) + " przez [url=" + c.getClientURI() + "]" + c.getNickname() + "[/url]", new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                        TeamSpeakUtils.api.getClients().forEach(x -> {
                                            if (x.isRegularClient()) {
                                                User ux = UserUtils.get(x.getUniqueIdentifier());
                                                if (System.currentTimeMillis() > ux.getMute()) {
                                                    if (!x.isInServerGroup(115)) {
                                                        if (ux.getChannels().contains(u.getSelect())) {
                                                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), parse);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        MessageUtils.saveMessageToFile("System", "[color=#00bcd4]Użytkownik " + ua.getName() + " został [color=#d50000]wyciszony[/color], do [B]" + TeamSpeakUtils.getDate(czas) + " przez [url=" + c.getClientURI() + "]" + c.getNickname() + "[/url]", new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                    } catch (Exception ee) {
                                        Logger.warning(ee.getMessage());
                                    }
                                } else {
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !timeout <przenies klienta> <czas>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !timeout <move client> <time>", "brak"}, c);
                                }
                            }
                        }else{
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie jesteś moim szefem! 🤓");
                        }
                    }
                    //switch kanał
                    if (args[0].equalsIgnoreCase("!switch") || args[0].equalsIgnoreCase("!przelacz") || args[0].equalsIgnoreCase("!select") || args[0].equalsIgnoreCase("!schalter")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    if (!u.getSelect().equalsIgnoreCase(args[1].toLowerCase())) {
                                        slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                        u.setSelect(args[1]);
                                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#00bcd4]Wybrano kanał[/color] [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]).", "[color=#00bcd4]Selected channel[/color] [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]).", "brak"}, c);
                                    }else{
                                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Należysz już do tego kanału.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You already belong to this channel.", "brak"}, c);
                                    }
                                } else {
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie subskrybujesz tego kanału. Dołącz do niego za pomocą !join <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are not subscribed to this channel. Join to that channel by !join <channel_name>", "brak"}, c);
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !switch <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !switch <channel_name>", "brak"}, c);
                        }
                    }
                    if (args[0].equalsIgnoreCase("!check") || args[0].equalsIgnoreCase("!sprawdz") || args[0].equalsIgnoreCase("!prüfen")){
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                try {
                                    if (TeamSpeakUtils.getRequired(c, args[1])){
                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                        File file = new File(jsonObject.getJSONObject(args[1]).getString("file"));
                                        int n_lines = 12;
                                        int counter = 0;
                                        ReversedLinesFileReader object = null;
                                        object = new ReversedLinesFileReader(file);
                                        List<String> s = new ArrayList();
                                        while(counter < n_lines) {
                                            if (counter == 12){
                                                //sendto.append("\n" + object.readLine() + "\n");
                                                s.add(counter ,"\n" + object.readLine() + "\n");
                                            }else{
                                                //sendto.append(object.readLine() + "\n");
                                                s.add(counter ,object.readLine() + "\n");
                                            }
                                            counter++;
                                        }
                                        String ss = "";
                                        StringBuilder sb = new StringBuilder(ss);
                                        MessageUtils.reverseList(s).forEach(x -> {
                                            sb.append(x);
                                        });
                                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[b]Ostatnie wiadomości z kanału [color=#f4511e]#" + args[1].toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/color]):[/b]\n" + sb.toString(), "[b]Last messages from the channel [color=#f4511e]#" + args[1].toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/color]):[/b]\n" + sb.toString(), "brak"}, c);
                                    }else{
                                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie masz uprawnień, aby przeglądać wiadomości z tego kanału!", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You do not have permission to view messages from this channel!", "brak"}, c);
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !check <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !check <channel_name>", "brak"}, c);
                        }
                    }
                    if (args[0].equalsIgnoreCase("!botreload")){
                        if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(17) || c.isInServerGroup(26) || c.isInServerGroup(75)) {
                            try {
                                Main.registerChannels();
                                Main.createFiles();
                                UserUtils.loadOnline();
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Bot został zreloadowany!");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }else{
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie jesteś moim szefem! 🤓");
                        }
                    }
                    if (args[0].equalsIgnoreCase("!botinfo") || args[0].equalsIgnoreCase("!info") || args[0].equalsIgnoreCase("!autor")){
                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b][color=orange]Cześć 🖐\n Zostałem napisany przez Xmon'a. Xmon kazał mi wysłać do Ciebie następujące linki: \n [url=https://github.com/Xmonpl]GitHub[/url]\n [url=https://Xmon.cf]Xmon.cf[/url]\n [url=https://steamcommunity.com/id/xmonofficial]Steam[/url]");
                    }
                    //get channels
                    if (args[0].equalsIgnoreCase("!channels") || args[0].equalsIgnoreCase("!kanaly") || args[0].equalsIgnoreCase("!kanäle")) {
                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[b]Znaczek [[color=#00e676]✔[/color]] oznacza że już subskrybujesz ten kanał, natomiast [[color=#ff1744]✖[/color]] oznacza że nie subskrybujesz tego kanału. Podkreślony kanał to ten w którym obecnie rozmawiasz. Zmiana komendą !switch[/b]", "[b]Badge [[color=#00e676]✔[/color]] means that you've subscribed to this channel, but [[color=#ff1744]✖[/color]] means that you do not subscribe to this channel. The underlined channel is the one you are currently talking to. Change with the command !switch[/b]", "bral"}, c);
                        String s = "";
                        StringBuilder channel = new StringBuilder(s);
                        try {
                            JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                            Arrays.stream(Main.channels.split("@")).forEach(x -> {
                                if (!x.equals("")) {
                                    if (u.getSelect().equalsIgnoreCase(x)) {
                                        if (c.isInServerGroup(6) || c.isInServerGroup(16)) {
                                            channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private") + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ?[u] " + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b][/color]\n" : "[b]➡️[[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private") + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b][/color]\n");
                                        } else {
                                            if (jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private")) {
                                                if (u.getChannels().contains(x.toLowerCase())) {
                                                    channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b]➡️[[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                                }
                                            } else {
                                                channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b]➡️[[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                            }
                                        }
                                    }else{
                                        if (c.isInServerGroup(6) || c.isInServerGroup(16)) {
                                            channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private") + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ?[u] " + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b][/color]\n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private") + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b][/color]\n");
                                        } else {
                                            if (jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private")) {
                                                if (u.getChannels().contains(x.toLowerCase())) {
                                                    channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                                }
                                            } else {
                                                channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                            }
                                        }
                                    }
                                }
                            });
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[b]Dostępne kanały:[/b]\n" + channel.toString(), "[b]Avalible channels:[/b]\n" + channel.toString(), "brak"}, c);
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //leave
                    if (args[0].equalsIgnoreCase("!leave") || args[0].equalsIgnoreCase("!wyjdz") || args[0].equalsIgnoreCase("!verlassen")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                    u.setChannels(u.getChannels().replace(args[1] + "@", ""));
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]wyszedłeś/aś z kanału [color=#f4511e][B]#" + args[1].toLowerCase() +"[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) +" online[/B][/color]). [B]Aby dołączyć do innego kanału wpisz [u][color=#8d6e63]!channels[/color][/u].[/b]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you've left the channel [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + "[/B][/color]). [B]To join another channel, enter [u][color=#8d6e63]!channels[/color][/u].[/b]", "bral"}, c);
                                } else {
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie znajdujesz się w tym kanale.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are not in this channel.", "brak"}, c);
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !leave <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !leave <channel_name>", "brak"}, c);
                        }
                    }
                    //limit znaków
                    if (args[0].equalsIgnoreCase("!char")) {
                        if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(17) || c.isInServerGroup(26) || c.isInServerGroup(75)) {
                            if (args.length == 2) {
                                try {
                                    charlimit = Integer.parseInt(args[1]);
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Zmieniono charlimit na " + args[1] + " a dla rang premium " + Integer.parseInt(args[1]) * 2);
                                } catch (NumberFormatException ee) {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Argument[1] nie jest liczbą!");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !char <limit>");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie jesteś moim szefem! 🤓");
                        }
                    }
                    //wycisznie czatu
                    if (args[0].equalsIgnoreCase("!mute") || args[0].equalsIgnoreCase("!zmutuj") || args[0].equalsIgnoreCase("!stumm")) {
                        if (args.length == 2) {
                            long czas = System.currentTimeMillis() + TeamSpeakUtils.getTimeWithString(args[1]);
                            u.setMute(czas);
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#00bcd4]Chat został wyciszony do [B]" + TeamSpeakUtils.getDate(czas), "[color=#00bcd4]Chat has been muted to [B]" + TeamSpeakUtils.getDate(czas), "brak"}, c);
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !mute <czas>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !mute <time>", "brak"}, c);
                        }
                    }
                    //odciszanie chatu
                    if (args[0].equalsIgnoreCase("!unmute") || args[0].equalsIgnoreCase("!odcisz")) {
                        if (System.currentTimeMillis() < u.getMute()) {
                            u.setMute(System.currentTimeMillis());
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]chat został odciszony.", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]chat was unmuted.", "brak"}, c);
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Chat nie jest wyciszony.", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]chat was unmuted.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Chat is not muted.", "brak"}, c);
                        }
                    }
                    //ustawienie koloru
                    if (args[0].equalsIgnoreCase("!color") || args[0].equalsIgnoreCase("!kolor") || args[0].equalsIgnoreCase("!farbe")) {
                        if (c.isInServerGroup(122) || c.isInServerGroup(123) || c.isInServerGroup(6) || c.isInServerGroup(16)) {
                            if (args.length == 2) {
                                if (!args[1].contains("[")) {
                                    if (args[1].contains("#")) {
                                        if (args[1].length() <= 7) {
                                            u.setColor(args[1]);
                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]pomyślnie ustawiłeś/aś [color=" + args[1] + "]kolor[/color]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you have set successfully [color=" + args[1] + "]color[/color]", "brak"}, c);
                                        } else {
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, c);
                                        }
                                    } else {
                                        if (args[1].length() <= 6) {
                                            u.setColor(args[1]);
                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]pomyślnie ustawiłeś/aś [color=#" + args[1] + "]kolor[/color]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you have set successfully [color=" + args[1] + "]color[/color]", "brak"}, c);
                                        } else {
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, c);
                                        }
                                    }
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Chciałoby się nie? 😪");
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Aby użyć tego polecenia, musisz posiadać rangę Plus lub Premium!", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]To use this command, you must have the Plus or Premium rank!", "brak"}, c);
                        }
                    }
                    if (args[0].equalsIgnoreCase("!rainbow") || args[0].equalsIgnoreCase("!tencza") || args[0].equalsIgnoreCase("!regenbogen")) {
                        if (c.isInServerGroup(123) || c.isInServerGroup(6) || c.isInServerGroup(16)) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("on")) {
                                    u.setColor("rainbow");
                                    slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b][color=#24BACB]P[/color][color=#26EA70]o[/color][color=#BD5C64]m[/color][color=#F478B1]y[/color][color=#74CE81]ś[/color][color=#C1FB82]l[/color][color=#EFDA01]n[/color][color=#B7B465]i[/color][color=#E776CF]e[/color] [color=#E0BD82]w[/color][color=#CCDF8B]ł[/color][color=#FAB0DF]ą[/color][color=#31950E]c[/color][color=#4389DC]z[/color][color=#DBF6AB]y[/color][color=#731F47]ł[/color][color=#457ECE]e[/color][color=#E22F6C]ś[/color][color=#7B3D8F]/[/color][color=#532EB8]a[/color][color=#6EC6B2]ś[/color] [color=#5BD855]t[/color][color=#158315]r[/color][color=#312AB0]y[/color][color=#6A0EBA]b[/color] [color=#00DC51]r[/color][color=#2BD84F]a[/color][color=#56FE3F]i[/color][color=#88FF77]n[/color][color=#FDCA07]b[/color][color=#3DF702]o[/color][color=#341617]w[/color][color=#3B3B6F]![/color][/b]");
                                } else if (args[1].equalsIgnoreCase("off")) {
                                    u.setColor(TeamSpeakUtils.getRainbowColor());
                                    slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b][color=#914C3B]P[/color][color=#4213CB]o[/color][color=#A0DC07]m[/color][color=#FE24EF]y[/color][color=#EF696A]ś[/color][color=#9A3BF9]l[/color][color=#022B1E]n[/color][color=#28DFFF]i[/color][color=#308277]e[/color] [color=#2FAC6C]w[/color][color=#585D9A]y[/color][color=#661641]ł[/color][color=#96EBD8]ą[/color][color=#67B125]c[/color][color=#DBD02A]z[/color][color=#2EF831]y[/color][color=#8A183A]ł[/color][color=#183590]e[/color][color=#915CD3]ś[/color][color=#E6FA0A]/[/color][color=#55A571]a[/color][color=#514DFF]ś[/color] [color=#B598DC]t[/color][color=#C50538]r[/color][color=#E8FD1C]y[/color][color=#F8F3E7]b[/color] [color=#70A7E2]r[/color][color=#A701F1]a[/color][color=#4E4A33]i[/color][color=#3CC287]n[/color][color=#F99F54]b[/color][color=#7C6BF1]o[/color][color=#DCC32A]w[/color][color=#07ED7F]![/color][/b]");
                                } else {
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !rainbow on/off", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !rainbow on/off", "brral"}, c);
                                }
                            } else {
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !rainbow on/off", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !rainbow on/off", "brral"}, c);
                            }
                        } else {
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Aby użyć tego polecenia, musisz posiadać rangę Plus lub Premium!", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]To use this command, you must have the Plus or Premium rank!", "brak"}, c);
                        }
                    }
                    //help
                    if (args[0].equalsIgnoreCase("!help") || args[0].equalsIgnoreCase("!pomoc") || args[0].equalsIgnoreCase("!komendy") || args[0].equalsIgnoreCase("!hilfe")){
                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[b]Dostępne komendy:[/b]\n" +
                        "[b]!channels[/b] — Wyświetla wszystkie dostępne kanały.\n" +
                                "[b]!join <nazwa>[/b] — Dołącza do wybranego kanału\n" +
                                "[b]!leave <nazwa>[/b] — Wychodzi z wybranego kanału tekstowego.\n" +
                                "[b]!mute <3s/5m/6h>[/b] — Wycisza chat na podaną ilość sekund/minut/godzin.\n" +
                                "[b]!switch <nazwa>[/b] — Przełącza twoje odpowiedzi na wybrany kanał.\n" +
                                "[b]!check <nazwa>[/b] — Sprawdza ostatnie 15 wiadomości, bez dołączania do kanału.\n" +
                                "[b]!color <#ffea00>[/b] — Zmienia kolor twoich wiadomości. [Plus/Premium]\n" +
                                "[b]!rainbow on/off[/b] — Zmienia kolor twoich wiadomosci na tęczowy. [Premium]\n" +
                                "[b]!admin[/b] — Wyświetla pomoc dostępną tylko dla Administratorów.\n[b]!botinfo — Wyswietla informacje dotyczące autora.[/b]", "[b]Available commands:[/b]\n" +
                        "[b]!channels[/b] — Displays all available channels.\n" +
                                "[b]!join <nazwa>[/b] — Joins the selected channel.\n" +
                                "[b]!leave <nazwa>[/b] — Exits from the selected text channel.\n" +
                                "[b]!mute <3s/5m/6h>[/b] — Mute the chat to the specified number of seconds / minutes / hours.\n" +
                                "[b]!switch <nazwa>[/b] — Switches your answers to the selected channel.\n" +
                                "[b]!check <nazwa>[/b] — Checks the last 15 messages without joining the channel.\n" +
                                "[b]!color <#ffea00>[/b] — Changes the color of your messages. [Plus/Premium]\n" +
                                "[b]!rainbow on/off[/b] — Changes the color of your messages to the rainbow. [Premium]\n" +
                                "[b]!admin[/b] — Displays help available only to Administrators.\n[b]!botinfo[/b] — Displays information about the author.", "brak"}, c);
                    }
                    if (args[0].equalsIgnoreCase("!slowdown") && args[0].equalsIgnoreCase("!slowmode")){
                        if (c.isInServerGroup(6) || c.isInServerGroup(16) || c.isInServerGroup(17) || c.isInServerGroup(26) || c.isInServerGroup(75)) {
                            if (args.length == 2) {
                                try {
                                    slowdowntime = Integer.parseInt(args[1]);
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Zmieniono slowdowntime na " + args[1]);
                                } catch (NumberFormatException ee) {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Argument[1] nie jest liczbą!");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !slowdown <limit>");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie jesteś moim szefem! 🤓");
                        }
                    }
                    if (args[0].equalsIgnoreCase("!rules") || args[0].equalsIgnoreCase("!regulamin") || args[0].equalsIgnoreCase("!zasady") || args[0].equalsIgnoreCase("!regeln")){
                        try {
                            if (c.getCountry().toUpperCase().equalsIgnoreCase("PL")){
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), Files.readAllLines(new File("rules_PL.txt").toPath()).get(0).replace("\n", "\n"));
                            }else if (c.getCountry().toUpperCase().equalsIgnoreCase("DE")){
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), Files.readAllLines(new File("rules_DE.txt").toPath()).get(0).replace("\n", "\n"));
                            }else{
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), Files.readAllLines(new File("rules_EN.txt").toPath()).get(0).replace("\n", "\n"));
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (args[0].startsWith("#")){
                        if (Main.channels.contains(args[0].replace("#", "").toLowerCase())){
                            if (u.getChannels().contains(args[0].replace("#", "").toLowerCase())){
                                if (!u.getSelect().equalsIgnoreCase(args[0].replace("#", "").toLowerCase())){
                                    u.setSelect(args[0].replace("#", "").toLowerCase());
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#00bcd4]Wybrano kanał[/color] [color=#f4511e][B]#" + args[0].replace("#", "").toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[0].replace("#", "").toLowerCase()) + "/" + UserUtils.max.get(args[0].replace("#", "").toLowerCase()) + " online[/B][/color]).", "[color=#00bcd4]Selected channel[/color] [color=#f4511e][B]#" + args[0].replace("#", "").toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[0].replace("#", "").toLowerCase()) + "/" + UserUtils.max.get(args[0].replace("#", "").toLowerCase()) + " online[/B][/color]).", "brak"}, c);
                                }else{
                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Należysz już do tego kanału.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You already belong to this channel.", "brak"}, c);
                                }
                            }else{
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie subskrybujesz tego kanału. Dołącz do niego za pomocą !join <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are not subscribed to this channel. Join to that channel by !join <channel_name>", "brak"}, c);
                            }
                        }else{
                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, c);
                        }
                    }
                    //Wiadomosci
                    //if (!args[0].contains("!") && !args[0].contains("#")) {
                    if (!args[0].startsWith("!") && !args[0].startsWith("#")) {
                        try {
                            if (c.isInServerGroup(133)) {
                                //blokada ranga (BAN)
                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Ostrzeżenie:[/B][/color] [color=#00bcd4]Administrator nałożył na Ciebie blokadę!", "[color=#d50000][B]Warning:[/B][/color] [color=#00bcd4]Administrator has blocked you!", "brak"}, c);
                            } else {
                                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                if (c.isInServerGroup(7) || c.isInServerGroup(9)) {
                                    //z ranga premium/plus
                                    if (c.isInServerGroup(122) || c.isInServerGroup(123) || c.isInServerGroup(6) || c.isInServerGroup(16)) {
                                        if (message.length() > charlimit * 2) {
                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Ostrzeżenie:[/B][/color] [color=#00bcd4]Twoja wiadomość zawiera zbyt dużą ilość znaków! (" + message.length() + "/" + charlimit * 2 + ")", "[color=#d50000][B]Warning:[/B][/color] [color=#00bcd4]Your message contains too many characters! (" + message.length() + "/" + charlimit * 2 + ")", "brak"}, c);
                                        } else {
                                            if (System.currentTimeMillis() > u.getTimeout()) {
                                                if (URL_PATTERN.matcher(message).find() || IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Ostrzeżenie:[/B][/color] [color=#00bcd4]Twoja wiadomość zawiera reklamę!", "[color=#d50000][B]Warning:[/B][/color] [color=#00bcd4]Your message contains an advertisement!", "brak"}, c);
                                                } else {
                                                    if (BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                        TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Ostrzeżenie:[/B][/color] [color=#00bcd4]Przeklinanie nie jest tutaj dozwolone!", "[color=#d50000][B]Warning:[/B][/color] [color=#00bcd4]Swearing is not allowed here!", "brak"}, c);
                                                    } else {
                                                        if (Main.channels.contains(u.getSelect())) {
                                                            String parse = MessageUtils.parserMessage(c, u, message, new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                                            if (System.currentTimeMillis() > u.getMute()) {
                                                                TeamSpeakUtils.api.getClients().forEach(x -> {
                                                                    if (x.isRegularClient()) {
                                                                        User ux = UserUtils.get(x.getUniqueIdentifier());
                                                                        if (System.currentTimeMillis() > ux.getMute()) {
                                                                            if (!x.isInServerGroup(115)) {
                                                                                if (ux.getChannels().contains(u.getSelect())) {
                                                                                    TeamSpeakUtils.api.sendPrivateMessage(x.getId(), parse.replace("@" + x.getNickname(), "[b][color=orange]@" + x.getNickname() + "[/color][/b]").replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"));
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                                MessageUtils.saveMessageToFile(c, u, message.replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                            } else {
                                                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Ostrzeżenie:[/B][/color] [color=#00bcd4]Masz wyciszony chat użyj komendy !unmute aby go odsiczyć!", "[color=#d50000][B]Warning:[/B][/color] [color=#00bcd4]You have muted chat, please use the !unmute command to unmute!", "brak"}, c);
                                                            }
                                                        } else {
                                                            TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Spróbuj jeszcze raz wybrać kanał oraz dołączyć do kanału, który wybrałeś/aś.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Try again to choose a channel and join the channel you have chosen.", "bral"}, c);
                                                        }
                                                    }
                                                }
                                            }else{
                                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Zostałeś wyciszony do " + TeamSpeakUtils.getDate(u.getTimeout()), "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You have been silenced for " + TeamSpeakUtils.getDate(u.getTimeout()), "bral"}, c);
                                            }
                                        }
                                    } else {
                                        //bez rangi premium/plus
                                        if (message.length() > charlimit) {
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Błąd! Zbyt duża ilośc znaków! (" + message.length() + "/" + charlimit + ")");
                                        } else {
                                            if (System.currentTimeMillis() > u.getTimeout()) {
                                                if (URL_PATTERN.matcher(message).find() || IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie reklamuj się!");
                                                } else {
                                                    if (BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Przeklinanie jest dowolone wyłącznie na kanale #NSFW");
                                                    } else {
                                                        if (Main.channels.contains(u.getSelect())) {
                                                            String parse = MessageUtils.parserMessage(c, u, message.replace("[", "[ ").replace("]", " ]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                                            if (System.currentTimeMillis() > u.getMute()) {
                                                                TeamSpeakUtils.api.getClients().forEach(x -> {
                                                                    if (x.isRegularClient()) {
                                                                        User ux = UserUtils.get(x.getUniqueIdentifier());
                                                                        if (System.currentTimeMillis() > ux.getMute()) {
                                                                            if (!x.isInServerGroup(115)) {
                                                                                if (ux.getChannels().contains(u.getSelect())) {
                                                                                    TeamSpeakUtils.api.sendPrivateMessage(x.getId(), parse.replace("@" + x.getNickname(), "[b][color=orange]@" + x.getNickname() + "[/color][/b]").replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"));
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                                MessageUtils.saveMessageToFile(c, u, message.replace("[", "[ ").replace("]", " ]").replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                            } else {
                                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Masz wyciszony chat użyj komendy !unmute aby go odsiczyć!");
                                                            }
                                                        } else {
                                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Wystąpił błąd, spróbuj jeszcze raz wybrać kanał oraz dołączyć do kanału, który wybrałeś/aś.");
                                                        }
                                                    }
                                                }
                                            }else {
                                                TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Zostałeś wyciszony do " + TeamSpeakUtils.getDate(u.getTimeout()), "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You have been silenced for " + TeamSpeakUtils.getDate(u.getTimeout()), "bral"}, c);
                                            }
                                        }
                                    }
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Musisz być zarejestrowany!");
                                }
                            }
                        }catch (Exception ee){
                            Logger.warning(ee.getMessage());
                        }
                    }
                }
            }
        }
    }
}

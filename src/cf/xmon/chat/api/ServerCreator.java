package cf.xmon.chat.api;

import cf.xmon.chat.Main;
import cf.xmon.chat.events.ChatEventOld;
import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.MessageUtils;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClient;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import static cf.xmon.chat.Main.parseJSONFile;

public class ServerCreator {
    private static String apikey = "GHW$gjDB0-j4n42i0gj-PMFO9234j90hyw";
    public static void createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(5669), 0);
        server.createContext("/send", new MyHandler());
        server.createContext("/getmessage", new getmessage());
        server.setExecutor(null);
        server.start();
        System.out.println("Uruchomiono serwer!");
    }
    static class getmessage implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Błąd wewnetrzny api!";
            if (t.getRequestHeaders().get("apikey").toString().replace("[", "").replace("]", "").equals(apikey)){

            }else{
                response = "incorrect-apikey";
            }
            t.sendResponseHeaders(0, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            t.close();
        }
    }
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Błąd wewnetrzny api!";
            if (t.getRequestHeaders().get("apikey").toString().replace("[", "").replace("]", "").equals(apikey)){
                String username = t.getRequestHeaders().get("username").toString().replace("[", "").replace("]", "");
                if (username != null) {
                    if (t.getRequestHeaders().get("method").toString().replace("[", "").replace("]", "").equalsIgnoreCase("message")) {
                        User u = UserUtils.getUserByUsername(username);
                        if (u != null){
                            if (t.getRequestHeaders().get("password").toString().replace("[", "").replace("]", "").equals(u.getPassword())) {
                                DatabaseClientInfo dbc = TeamSpeakUtils.api.getDatabaseClientByUId(u.getUuid());
                                if (!isinSG(dbc, 133)) {
                                    if (isinServerGroup(dbc)) {
                                        String message = t.getRequestHeaders().get("message").toString().replace("[", "").replace("]", "");
                                        if (message != null) {
                                            if (isinPremium(dbc)) {
                                                if (message.length() > ChatEventOld.charlimit * 2) {
                                                    response = "Charlimit Premium " + message.length() + "/" + ChatEventOld.charlimit *2;
                                                } else {
                                                    if (System.currentTimeMillis() > u.getTimeout()) {
                                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                        if (ChatEventOld.URL_PATTERN.matcher(message).find() || ChatEventOld.IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                            response = "advertisement";
                                                        }else{
                                                            if (ChatEventOld.BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                                response = "imprecation";
                                                            }else {
                                                                if (Main.channels.contains(u.getSelect())) {
                                                                    String parse = MessageUtils.parserMessage(dbc, u, message, new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                    ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
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
                                                                        MessageUtils.saveMessageToFile(dbc, u, message.replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                        response = "success";
                                                                    }else{
                                                                        response = "mute";
                                                                    }
                                                                }else {
                                                                    response = "idiot error#1";
                                                                }
                                                            }
                                                        }
                                                    }else{
                                                        response = "timeout " + TeamSpeakUtils.getDate(u.getTimeout());
                                                    }
                                                }
                                            } else {
                                                if (message.length() > ChatEventOld.charlimit) {
                                                    response = "Charlimit " + message.length() + "/" + ChatEventOld.charlimit;
                                                } else {
                                                    if (System.currentTimeMillis() > u.getTimeout()) {
                                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                        if (ChatEventOld.URL_PATTERN.matcher(message).find() || ChatEventOld.IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                            response = "advertisement";
                                                        }else{
                                                            if (ChatEventOld.BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                                response = "imprecation";
                                                            }else {
                                                                if (Main.channels.contains(u.getSelect())) {
                                                                    String parse = MessageUtils.parserMessage(dbc, u, message, new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                    ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
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
                                                                        MessageUtils.saveMessageToFile(dbc, u, message.replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                        response = "success";
                                                                    }else{
                                                                        response = "mute";
                                                                    }
                                                                }else {
                                                                    response = "idiot error#1";
                                                                }
                                                            }
                                                        }
                                                    }else{
                                                        response = "timeout " + TeamSpeakUtils.getDate(u.getTimeout());
                                                    }
                                                }
                                            }
                                        } else {
                                            response = "no-message";
                                        }
                                    } else {
                                        response = "no-register";
                                    }
                                }else{
                                    response = "global-block";
                                }
                            }else{
                                response = "incorrect-password";
                            }
                        }else {
                            response = "incorrect-username";
                        }
                    }else if (t.getRequestHeaders().get("method").toString().replace("[", "").replace("]", "").equalsIgnoreCase("command")) {
                        User u = UserUtils.getUserByUsername(username);
                        if (u != null) {
                            if (t.getRequestHeaders().get("password").toString().replace("[", "").replace("]", "").equals(u.getPassword())) {
                                DatabaseClientInfo dbc = TeamSpeakUtils.api.getDatabaseClientByUId(u.getUuid());
                                String command = t.getRequestHeaders().get("command").toString().replace("[", "").replace("]", "");
                                if (command != null){
                                    String[] args = command.split(" ");
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 1; i < args.length; i++) {
                                        sb.append(args[i]).append(" ");
                                    }
                                    System.out.println(args[0]);
                                    String language = t.getRequestHeaders().get("language").toString().replace("[", "").replace("]", "");

                                    if (args[0].equalsIgnoreCase("!switch") || args[0].equalsIgnoreCase("!przelacz") || args[0].equalsIgnoreCase("!select") || args[0].equalsIgnoreCase("!schalter")) {
                                        if (args.length == 2) {
                                            if (Main.channels.contains(args[1])) {
                                                if (u.getChannels().contains(args[1])) {
                                                    if (!u.getSelect().equalsIgnoreCase(args[1].toLowerCase())) {
                                                        ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                        u.setSelect(args[1]);
                                                        response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#00bcd4]Wybrano kanał[/color] [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]).", "[color=#00bcd4]Selected channel[/color] [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + " online[/B][/color]).", "brak"}, language);
                                                    }else{
                                                        response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Należysz już do tego kanału.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You already belong to this channel.", "brak"}, language);
                                                    }
                                                } else {
                                                    response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie subskrybujesz tego kanału. Dołącz do niego za pomocą !join <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are not subscribed to this channel. Join to that channel by !join <channel_name>", "brak"}, language);
                                                }
                                            } else {
                                                response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, language);
                                            }
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !switch <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !switch <channel_name>", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!leave") || args[0].equalsIgnoreCase("!wyjdz") || args[0].equalsIgnoreCase("!verlassen")) {
                                        if (args.length == 2) {
                                            if (Main.channels.contains(args[1])) {
                                                if (u.getChannels().contains(args[1])) {
                                                    ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                    u.setChannels(u.getChannels().replace(args[1].toLowerCase() + "@", ""));
                                                    response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]wyszedłeś/aś z kanału [color=#f4511e][B]#" + args[1].toLowerCase() +"[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) +" online[/B][/color]). [B]Aby dołączyć do innego kanału wpisz [u][color=#8d6e63]!channels[/color][/u].[/b]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you've left the channel [color=#f4511e][B]#" + args[1].toLowerCase() + "[/B][/color] ([color=#43a047][B]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + "[/B][/color]). [B]To join another channel, enter [u][color=#8d6e63]!channels[/color][/u].[/b]", "bral"}, language);
                                                } else {
                                                    response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Nie znajdujesz się w tym kanale.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]You are not in this channel.", "brak"}, language);
                                                }
                                            } else {
                                                response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Taki kanał nie istnieje.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Such a channel does not exist.", "brak"}, language);
                                            }
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !leave <nazwa_kanału>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !leave <channel_name>", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!mute") || args[0].equalsIgnoreCase("!zmutuj") || args[0].equalsIgnoreCase("!stumm")) {
                                        if (args.length == 2) {
                                            long czas = System.currentTimeMillis() + TeamSpeakUtils.getTimeWithString(args[1]);
                                            u.setMute(czas);
                                            ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#00bcd4]Chat został wyciszony do [B]" + TeamSpeakUtils.getDate(czas), "[color=#00bcd4]Chat has been muted to [B]" + TeamSpeakUtils.getDate(czas), "brak"}, language);
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !mute <czas>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !mute <time>", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!unmute") || args[0].equalsIgnoreCase("!odcisz")) {
                                        if (System.currentTimeMillis() < u.getMute()) {
                                            u.setMute(System.currentTimeMillis());
                                            ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]chat został odciszony.", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]chat was unmuted.", "brak"}, language);
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Chat nie jest wyciszony.", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]chat was unmuted.", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Chat is not muted.", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!color") || args[0].equalsIgnoreCase("!kolor") || args[0].equalsIgnoreCase("!farbe")) {
                                        if (isinSG(dbc, 122) || isinSG(dbc, 123) || isinSG(dbc, 6) || isinSG(dbc, 16)) {
                                            if (args.length == 2) {
                                                if (!args[1].contains("[")) {
                                                    if (args[1].contains("#")) {
                                                        if (args[1].length() <= 7) {
                                                            u.setColor(args[1]);
                                                            ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]pomyślnie ustawiłeś/aś [color=" + args[1] + "]kolor[/color]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you have set successfully [color=" + args[1] + "]color[/color]", "brak"}, language);
                                                        } else {
                                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, language);
                                                        }
                                                    } else {
                                                        if (args[1].length() <= 6) {
                                                            u.setColor(args[1]);
                                                            ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=lightgreen][B]Gratulacje[/B][/color], [color=#00bcd4]pomyślnie ustawiłeś/aś [color=#" + args[1] + "]kolor[/color]", "[color=lightgreen][B]Congratulations[/B][/color], [color=#00bcd4]you have set successfully [color=" + args[1] + "]color[/color]", "brak"}, language);
                                                        } else {
                                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, language);
                                                        }
                                                    }
                                                } else {
                                                    response = "[color=red]Blad! [color=pink]Chciałoby się nie? 😪";
                                                }
                                            } else {
                                                response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !color <#ffffff>", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !color <#ffffff>", "brral"}, language);
                                            }
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Aby użyć tego polecenia, musisz posiadać rangę Plus lub Premium!", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]To use this command, you must have the Plus or Premium rank!", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!rainbow") || args[0].equalsIgnoreCase("!tencza") || args[0].equalsIgnoreCase("!regenbogen")) {
                                        if (isinSG(dbc, 123) || isinSG(dbc, 6) || isinSG(dbc, 16)) {
                                            if (args.length == 2) {
                                                if (args[1].equalsIgnoreCase("on")) {
                                                    u.setColor("rainbow");
                                                    ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                    response = "[b][color=#24BACB]P[/color][color=#26EA70]o[/color][color=#BD5C64]m[/color][color=#F478B1]y[/color][color=#74CE81]ś[/color][color=#C1FB82]l[/color][color=#EFDA01]n[/color][color=#B7B465]i[/color][color=#E776CF]e[/color] [color=#E0BD82]w[/color][color=#CCDF8B]ł[/color][color=#FAB0DF]ą[/color][color=#31950E]c[/color][color=#4389DC]z[/color][color=#DBF6AB]y[/color][color=#731F47]ł[/color][color=#457ECE]e[/color][color=#E22F6C]ś[/color][color=#7B3D8F]/[/color][color=#532EB8]a[/color][color=#6EC6B2]ś[/color] [color=#5BD855]t[/color][color=#158315]r[/color][color=#312AB0]y[/color][color=#6A0EBA]b[/color] [color=#00DC51]r[/color][color=#2BD84F]a[/color][color=#56FE3F]i[/color][color=#88FF77]n[/color][color=#FDCA07]b[/color][color=#3DF702]o[/color][color=#341617]w[/color][color=#3B3B6F]![/color][/b]";
                                                } else if (args[1].equalsIgnoreCase("off")) {
                                                    u.setColor(TeamSpeakUtils.getRainbowColor());
                                                    ChatEventOld.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
                                                    response = "[b][color=#914C3B]P[/color][color=#4213CB]o[/color][color=#A0DC07]m[/color][color=#FE24EF]y[/color][color=#EF696A]ś[/color][color=#9A3BF9]l[/color][color=#022B1E]n[/color][color=#28DFFF]i[/color][color=#308277]e[/color] [color=#2FAC6C]w[/color][color=#585D9A]y[/color][color=#661641]ł[/color][color=#96EBD8]ą[/color][color=#67B125]c[/color][color=#DBD02A]z[/color][color=#2EF831]y[/color][color=#8A183A]ł[/color][color=#183590]e[/color][color=#915CD3]ś[/color][color=#E6FA0A]/[/color][color=#55A571]a[/color][color=#514DFF]ś[/color] [color=#B598DC]t[/color][color=#C50538]r[/color][color=#E8FD1C]y[/color][color=#F8F3E7]b[/color] [color=#70A7E2]r[/color][color=#A701F1]a[/color][color=#4E4A33]i[/color][color=#3CC287]n[/color][color=#F99F54]b[/color][color=#7C6BF1]o[/color][color=#DCC32A]w[/color][color=#07ED7F]![/color][/b]";
                                                } else {
                                                    response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !rainbow on/off", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !rainbow on/off", "brral"}, language);
                                                }
                                            } else {
                                                response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Poprawne użycie: !rainbow on/off", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]Correct use: !rainbow on/off", "brral"}, language);
                                            }
                                        } else {
                                            response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[color=#d50000][B]Błąd:[/B][/color] [color=#00bcd4]Aby użyć tego polecenia, musisz posiadać rangę Plus lub Premium!", "[color=#d50000][B]Error:[/B][/color] [color=#00bcd4]To use this command, you must have the Plus or Premium rank!", "brak"}, language);
                                        }
                                    }
                                    if (args[0].equalsIgnoreCase("!help") || args[0].equalsIgnoreCase("!pomoc") || args[0].equalsIgnoreCase("!komendy") || args[0].equalsIgnoreCase("!hilfe")){
                                        response = TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"[b]Dostępne komendy:[/b]\n" +
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
                                                "[b]!admin[/b] — Displays help available only to Administrators.\n[b]!botinfo[/b] — Displays information about the author.", "brak"}, language);
                                    }
                                    if (args[0].equalsIgnoreCase("!rules") || args[0].equalsIgnoreCase("!regulamin") || args[0].equalsIgnoreCase("!zasady") || args[0].equalsIgnoreCase("!regeln")){
                                        try {
                                            if (language.equalsIgnoreCase("pl")) {
                                                response = Files.readAllLines(new File("rules_PL.txt").toPath()).get(0).replace("\n", "\n");
                                            }else if (language.equalsIgnoreCase("de")){
                                                response = Files.readAllLines(new File("rules_DE.txt").toPath()).get(0).replace("\n", "\n");
                                            }else{
                                                response = Files.readAllLines(new File("rules_EN.txt").toPath()).get(0).replace("\n", "\n");
                                            }
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }else {
                                    response = "Stary... podaj mi argumenty";
                                }
                            }else{
                                response = "Stary... zle haslo";
                            }
                        }else{
                            response = "Stary... taki username nie istnieje";
                        }
                    } else {
                        response = "Stary... Nie znam tej metody co mi podales kurde blaszka.";
                    }
                }else{
                    response = "Stary... podaj mi username.";
                }
            }else{
                response = "Api key jest nie poprawny!";
            }
            //response += "<br>API KEY: " + t.getRequestHeaders().get("apikey").toString() + "<br>username: " + t.getRequestHeaders().get("username").toString() + "<br>method: "  + t.getRequestHeaders().get("method").toString();
            t.sendResponseHeaders(0, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            t.close();
        }
    }

    private static boolean isinServerGroup(DatabaseClient db){
        ServerGroup sg = TeamSpeakUtils.api.getServerGroupsByClientId(db.getDatabaseId()).stream().filter(serverGroup -> (serverGroup.getId() == 7)).findFirst().orElse(TeamSpeakUtils.api.getServerGroupsByClientId(db.getDatabaseId()).stream().filter(serverGroup -> (serverGroup.getId() == 9)).findFirst().orElse(null));
        if (sg != null) {
            if (sg.getId() == 7 || sg.getId() == 9) {
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }

    private static boolean isinPremium(DatabaseClient db){
        ServerGroup sg = TeamSpeakUtils.api.getServerGroupsByClientId(db.getDatabaseId()).stream().filter(serverGroup -> (serverGroup.getId() == 122)).findFirst().orElse(TeamSpeakUtils.api.getServerGroupsByClientId(db.getDatabaseId()).stream().filter(serverGroup -> (serverGroup.getId() == 123)).findFirst().orElse(null));
        if (sg != null) {
            if (sg.getId() == 122 || sg.getId() == 123) {
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }

    private static boolean isinSG(DatabaseClient db, Integer id){
        ServerGroup sg = TeamSpeakUtils.api.getServerGroupsByClientId(db.getDatabaseId()).stream().filter(serverGroup -> (serverGroup.getId() == id)).findFirst().orElse(null);
        if (sg != null) {
            if (sg.getId() == id) {
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }
}

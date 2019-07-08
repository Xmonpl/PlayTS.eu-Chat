package cf.xmon.chat.api;

import cf.xmon.chat.Main;
import cf.xmon.chat.events.ChatEvent;
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

import static cf.xmon.chat.Main.parseJSONFile;

public class ServerCreator {
    private static String apikey = "GHW$gjDB0-j4n42i0gj-PMFO9234j90hyw";
    public static void createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(5669), 0);
        server.createContext("/send", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Uruchomiono serwer!");
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
                                                if (message.length() > ChatEvent.charlimit * 2) {
                                                    response = "Stary... zbyt dluga ta twoja wiadmosc!";
                                                } else {
                                                    if (System.currentTimeMillis() > u.getTimeout()) {
                                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                        if (ChatEvent.URL_PATTERN.matcher(message).find() || ChatEvent.IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                            response = "Stary.. twoja wiadomosci zawiera reklame";
                                                        }else{
                                                            if (ChatEvent.BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                                response = "Stary.. twoja wiadomosci zawiera przeklensta";
                                                            }else {
                                                                if (Main.channels.contains(u.getSelect())) {
                                                                    String parse = MessageUtils.parserMessage(dbc, u, message, new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                    ChatEvent.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
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
                                                                    }else{
                                                                        response = "Stary.. masz zmutowany czat! !unmute";
                                                                    }
                                                                }else {
                                                                    response = "Stary.. sprobuj jeszcze raz wybrac kanał!";
                                                                }
                                                            }
                                                        }
                                                    }else{
                                                        response = "Stary... zostales wyciszony do " + TeamSpeakUtils.getDate(u.getTimeout());
                                                    }
                                                }
                                            } else {
                                                if (message.length() > ChatEvent.charlimit) {
                                                    response = "Stary... zbyt dluga ta twoja wiadmosc!";
                                                } else {
                                                    if (System.currentTimeMillis() > u.getTimeout()) {
                                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                        if (ChatEvent.URL_PATTERN.matcher(message).find() || ChatEvent.IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                            response = "Stary.. twoja wiadomosci zawiera reklame";
                                                        }else{
                                                            if (ChatEvent.BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                                response = "Stary.. twoja wiadomosci zawiera przeklensta";
                                                            }else {
                                                                if (Main.channels.contains(u.getSelect())) {
                                                                    String parse = MessageUtils.parserMessage(dbc, u, message, new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
                                                                    ChatEvent.slowdown.put(dbc.getUniqueIdentifier(), System.currentTimeMillis());
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
                                                                    }else{
                                                                        response = "Stary.. masz zmutowany czat";
                                                                    }
                                                                }else {
                                                                    response = "Stary.. sprobuj jeszcze raz wybrac kanal!";
                                                                }
                                                            }
                                                        }
                                                    }else{
                                                        response = "Stary... zostales wyciszony do " + TeamSpeakUtils.getDate(u.getTimeout());
                                                    }
                                                }
                                            }
                                        } else {
                                            response = "Stary... Musisz wyslac mi wiadomosc ktora ma porosylac!";
                                        }
                                    } else {
                                        response = "Uzytkownik musi byc zarejestrowany!";
                                    }
                                }else{
                                    response = "Administrator naloxyl na ciebie blokade";
                                }
                            }else{
                                response = "Stary... Podales mi zle haslo do tego konta!";
                            }
                        }else {
                            response = "Stary... Taki username nie istnieje!";
                        }
                    } else if (t.getRequestHeaders().get("method").toString().replace("[", "").replace("]", "").equalsIgnoreCase("command")) {
                        response = "Stary... Nie znam tej metody co mi podales kurde blaszka.";
                    } else {
                        response = "Stary... Nie znam tej metody co mi podales kurde blaszka.";
                    }
                }else{
                    response = "Stary... podaj mi username.";
                }
            }else{
                response = "Api key jest nie poprawny!";
            }
            response += "<br>API KEY: " + t.getRequestHeaders().get("apikey").toString() + "<br>username: " + t.getRequestHeaders().get("username").toString() + "<br>method: "  + t.getRequestHeaders().get("method").toString();
            t.sendResponseHeaders(404, response.length());
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

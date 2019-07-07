package cf.xmon.chat.api;

import cf.xmon.chat.events.ChatEvent;
import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClient;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

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
                                if (isinServerGroup(dbc)){
                                    String selectchannel = u.getSelect();
                                    String message = t.getRequestHeaders().get("message").toString().replace("[", "").replace("]", "");
                                    if (message != null) {
                                        if (isinPremium(dbc)){
                                            if (message.length() > ChatEvent.charlimit * 2){
                                                response = "Stary... zbyt długa ta twoja wiadmosc!";
                                            }else{
                                                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                response = "porpawne";
                                            }
                                        }else{
                                            if (message.length() > ChatEvent.charlimit){
                                                response = "Stary... zbyt długa ta twoja wiadmosc!";
                                            }else{
                                                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                                response = "porpawne";
                                            }
                                        }
                                    }else{
                                        response = "Stary... Musisz wysłać mi wiadomosc która ma porosyłąc!";
                                    }
                                }else{
                                    response = "Uzytkownik musi byc zarejestrowany!";
                                }
                            }else{
                                response = "Stary... Podales mi zle haslo do tego konta!";
                            }
                        }else {
                            response = "Stary... Taki username nie istnieje!";
                        }
                    } else if (t.getRequestHeaders().get("method").toString().replace("[", "").replace("]", "").equalsIgnoreCase("command")) {
                        response = "Stary... Nie znam tej metody co mi podałeś kurde blaszka.";
                    } else {
                        response = "Stary... Nie znam tej metody co mi podałeś kurde blaszka.";
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
}

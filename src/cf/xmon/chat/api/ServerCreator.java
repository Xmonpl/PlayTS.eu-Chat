package cf.xmon.chat.api;

import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.MessageUtils;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
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
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = null;
            if (t.getRequestHeaders().get("apikey").toString().replace("[", "").replace("]", "").equals(apikey)){
                response = "UUID=" + t.getRequestHeaders().get("uuid").toString() + " | MESSAGE=" + t.getRequestHeaders().get("message").toString();
                User u = UserUtils.get(t.getRequestHeaders().get("uuid").toString().replace("[", "").replace("]", ""));
                Client c = TeamSpeakUtils.api.getClientByUId(t.getRequestHeaders().get("uuid").toString().replace("[", "").replace("]", ""));
                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                String parse = MessageUtils.parserMessage(c, u, t.getRequestHeaders().get("message").toString().replace("[", "").replace("]", ""), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
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
                MessageUtils.saveMessageToFile(c, u, t.getRequestHeaders().get("message").toString().replace("[", "").replace("]", "").replace(":shrug:", "¯\\_(ツ)_/¯").replace(":lenny:", "( ͡° ͜ʖ ͡°)").replace(":take:", "༼ つ ◕_◕ ༽つ").replace(":dolar:", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]").replace(":lennydolar:", "[̲̅$̲̅(̲̅ ͡° ͜ʖ ͡°̲̅)̲̅$̲̅]").replace("<3", "[b][color=red]❤[/color][/b]"), new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file")));
            }else{
                response = "Api key jest nie poprawny!";
            }
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            t.close();
        }
    }
}

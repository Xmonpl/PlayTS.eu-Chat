package cf.xmon.chat.events;

import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.Logger;
import cf.xmon.chat.utils.MessageUtils;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cf.xmon.chat.Main.parseJSONFile;

public class JoinEvent extends TS3EventAdapter {
    @Override
    public void onClientJoin(@NotNull ClientJoinEvent e){
        Client c = TeamSpeakUtils.api.getClientByUId(e.getUniqueClientIdentifier());
        if (c.isRegularClient()) {
            try {
                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                User u = UserUtils.getUsers().stream().filter(user -> user.getUuid().toLowerCase().equals(e.getUniqueClientIdentifier().toLowerCase())).findFirst().orElse(null);
                if (u == null){
                    Logger.info("New User[" + e.getClientNickname() + "]");
                    new User(e.getUniqueClientIdentifier());
                    /*
                    TeamSpeakUtils.api.sendPrivateMessage(e.getClientId(), "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                            "Automatycznie dołączono" +
                            " do kanału [color=#f4511e]#playts[/color] ([color=#43a047]" + UserUtils.online.get("playts") + "/" + UserUtils.max.get("playts") + "[/color]). [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#playts[/color]:[/i][/b]\n");
                     */
                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                            "Automatycznie dołączono" +
                            " do kanału [color=#f4511e]#playts[/color] ([color=#43a047]" + UserUtils.online.get("playts") + "/" + UserUtils.max.get("playts") + " online[/color]). [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#playts[/color]:[/i][/b]\n", "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]News![/color] Text channels as on [color=#7289da]Discord[/color]. Select the channel by typing [u][color=#8d6e63]!channels[/color][/u] or [u][color=#795548]!help[/color][/u].\n" +
                            "Automatically joined" +
                            " to the channel [color=#f4511e]#playts[/color] ([color=#43a047]" + UserUtils.online.get("playts") + "/" + UserUtils.max.get("playts") + " online[/color]). [i]Last 5 messages from the channel [color=#f4511e]#playts[/color]:[/i][/b]\n", ""}, c);
                    File file = new File(jsonObject.getJSONObject("playts").getString("file"));
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
                    TeamSpeakUtils.api.sendPrivateMessage(e.getClientId(), "\n" + sb.toString());
                }else{
                    String sads = "";
                    StringBuilder sbb = new StringBuilder(sads);
                    Arrays.stream(u.getChannels().split("@")).forEach(x ->{
                        sbb.append("[color=#f4511e]#" + x.toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]), ");
                    });
                    /*
                    TeamSpeakUtils.api.sendPrivateMessage(e.getClientId(), "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                            "Znajdujesz się w kanałach: " + sbb.toString() + " [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#" + u.getSelect().toLowerCase() + "[/color]:[/i][/b]\n");
                                   */
                    TeamSpeakUtils.sendMultiLanguagePrivateMessage(new String[]{"\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]Nowość![/color] Kanały tekstowe jak na [color=#7289da]Discord[/color]zie. Wybierz kanał wpisując [u][color=#8d6e63]!channels[/color][/u] lub [u][color=#795548]!help[/color][/u].\n" +
                            "Znajdujesz się w kanałach: " + sbb.toString() + " [i]Ostatnie 5 wiadomości z kanału [color=#f4511e]#" + u.getSelect().toLowerCase() + "[/color]:[/i][/b]\n", "\n " + MessageUtils.getTime() + " ⚙️ [color=#2580c3][b]\"System\"[/b][/color]: [b][color=#76ff03]News![/color] Text channels as on [color=#7289da]Discord[/color]. Select the channel by typing [u][color=#8d6e63]!channels[/color][/u] or [u][color=#795548]!help[/color][/u].\n" +
                            "You are in the channels: " + sbb.toString() + " [i]Last 5 messages from the channel [color=#f4511e]#" + u.getSelect().toLowerCase() + "[/color]:[/i][/b]\n", "brak"}, c);
                    File file = new File(jsonObject.getJSONObject(u.getSelect().toLowerCase()).getString("file"));
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
                    TeamSpeakUtils.api.sendPrivateMessage(e.getClientId(), "\n" + sb.toString());
                }
                if (!u.getChannels().contains("staff")) {
                    u.setChannels(u.getChannels() + "staff@");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

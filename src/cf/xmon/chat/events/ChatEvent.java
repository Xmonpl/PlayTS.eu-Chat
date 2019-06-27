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
    private static Integer charlimit = 128;
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
                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Następną wiadomość możesz wysłąć za " + Long.toString(slowdowntime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - slowdown.get(c.getUniqueIdentifier()))));
                }else {
                    //join command
                    if (args[0].equalsIgnoreCase("!join") || args[0].equalsIgnoreCase("!dolacz")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Znajdujesz się już w tym kanale!");
                                } else {
                                    try {
                                        if (TeamSpeakUtils.getRequired(c, args[1])) {
                                            u.setChannels(u.getChannels() + args[1].toLowerCase() + "@");
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b]Gratulacje, dołączyłeś do kanału [color=#f4511e]#" + args[1].toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + "[/color]). [i]Ostatnie 5 wiadomości:[/i][/b]");
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
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Kanał #" + args[1].toLowerCase() + " nie jest dla ciebie!");
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Taki kanał nie istnieje!");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !join <Channel>");
                        }
                    }
                    if (args[0].equalsIgnoreCase("!register")){
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
                    //switch kanał
                    if (args[0].equalsIgnoreCase("!switch") || args[0].equalsIgnoreCase("!przelacz") || args[0].equalsIgnoreCase("!select")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                    u.setSelect(args[1]);
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=green]Gratulacje! Wybrałeś/aś do kanał [color=orange]#" + args[1].toUpperCase());
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Najpierw dołącz do tego kanału! (!join <Channel>)");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Taki kanał nie istnieje!");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !switch <Channel>");
                        }
                    }
                    if (args[0].equalsIgnoreCase("!check") || args[0].equalsIgnoreCase("!sprawdz")){
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                try {
                                    if (TeamSpeakUtils.getRequired(c, args[1])){
                                        JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                        File file = new File(jsonObject.getJSONObject(args[1]).getString("file"));
                                        int n_lines = 15;
                                        int counter = 0;
                                        ReversedLinesFileReader object = null;
                                        object = new ReversedLinesFileReader(file);
                                        List<String> s = new ArrayList();
                                        while(counter < n_lines) {
                                            if (counter == 15){
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
                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "\n[b]Ostatnie wiadomości z kanału: [color=#f4511e]#" + args[1].toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + "[/color]).[/b]\n" + sb.toString());

                                    }else{
                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Nie masz na tyle uprawnien aby przeglądać wiadomości z tego kanału!");
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Taki kanał nie istnieje!");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !check <Channel>");
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
                    if (args[0].equalsIgnoreCase("!channels") || args[0].equalsIgnoreCase("!kanaly")) {
                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b]Znaczek [[color=#00e676]✔[/color]] oznacza że już subskrybujesz ten kanał, natomiast [[color=#ff1744]✖[/color]] oznacza że nie subskrybujesz tego kanału. Podkreślony kanał to ten w którym obecnie rozmawiasz. Zmiana komendą !switch[/b]");
                        String s = "";
                        StringBuilder channel = new StringBuilder(s);
                        try {
                            JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                            Arrays.stream(Main.channels.split("@")).forEach(x -> {
                                if (!x.equals("")) {
                                    if (c.isInServerGroup(6) || c.isInServerGroup(16)){
                                        channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private")  + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ?[u] " + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b]\n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] [color=orange][b](private ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private")  + "[/u], imprecation ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("imprecation") + "[/u], advertising ? [u]" + jsonObject.getJSONObject(x.toLowerCase()).getBoolean("advertising") + "[/u])[/b]\n");
                                    }else {
                                        if (jsonObject.getJSONObject(x.toLowerCase()).getBoolean("private")) {
                                            if (u.getChannels().contains(x.toLowerCase())) {
                                                channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                            }
                                        }else{
                                            channel.append(!u.getChannels().contains(x) ? "[b][[color=#ff1744]✖[/color]]  [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b] \n" : "[b][[color=#00e676]✔[/color]] [color=#f4511e]#" + x + "[/color] ([color=#43a047]" + UserUtils.online.get(x.toLowerCase()) + "/" + UserUtils.max.get(x.toLowerCase()) + " online[/color]) - " + jsonObject.getJSONObject(x.toLowerCase()).getString("description") + "[/b]\n");
                                        }
                                    }
                                }
                            });
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b]Dostępne kanały:[/b]\n" + channel.toString());
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //leave
                    if (args[0].equalsIgnoreCase("!leave") || args[0].equalsIgnoreCase("!wyjdz")) {
                        if (args.length == 2) {
                            if (Main.channels.contains(args[1])) {
                                if (u.getChannels().contains(args[1])) {
                                    slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                    u.setChannels(u.getChannels().replace(args[1] + "@", ""));
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b]Gratulacje, wyszedłeś z kanału [color=#f4511e]#" + args[1].toLowerCase() + "[/color] ([color=#43a047]" + UserUtils.online.get(args[1].toLowerCase()) + "/" + UserUtils.max.get(args[1].toLowerCase()) + "[/color]). Aby dołączyć do innego kanału wpisz [u][color=#8d6e63]!join[/color][/u].[/b]");
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Nie znajdujesz się w tym kanale!");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Taki kanał nie istnieje!");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !leave <Channel>");
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
                    if (args[0].equalsIgnoreCase("!mute") || args[0].equalsIgnoreCase("!zmutuj")) {
                        if (args.length == 2) {
                            long czas = System.currentTimeMillis() + TeamSpeakUtils.getTimeWithString(args[1]);
                            u.setMute(czas);
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Czat został wyciszony do " + TeamSpeakUtils.getDate(czas));
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !mute <czas>");
                        }
                    }
                    //odciszanie chatu
                    if (args[0].equalsIgnoreCase("!unmute") || args[0].equalsIgnoreCase("!odcisz")) {
                        if (System.currentTimeMillis() < u.getMute()) {
                            u.setMute(System.currentTimeMillis());
                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=Green]Gratulacje! Pomyślnie odciszyleś chat!");
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Chat nie jest wyciszony!");
                        }
                    }
                    //ustawienie koloru
                    if (args[0].equalsIgnoreCase("!color") || args[0].equalsIgnoreCase("!kolor")) {
                        if (c.isInServerGroup(122) || c.isInServerGroup(123) || c.isInServerGroup(6) || c.isInServerGroup(16)) {
                            if (args.length == 2) {
                                if (!args[1].contains("[")) {
                                    if (args[1].contains("#")) {
                                        if (args[1].length() <= 7) {
                                            u.setColor(args[1]);
                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=Green]Gratulacje! Pomyślnie ustawiłeś [color=" + args[1] + "]kolor[/color]");
                                        } else {
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !color <#ffffff>");
                                        }
                                    } else {
                                        if (args[1].length() <= 6) {
                                            u.setColor(args[1]);
                                            slowdown.put(c.getUniqueIdentifier(), System.currentTimeMillis());
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=Green]Gratulacje! Pomyślnie ustawiłeś [color=#" + args[1] + "]kolor[/color]");
                                        } else {
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !color <#ffffff>");
                                        }
                                    }
                                } else {
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Chciałoby się nie? 😪");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !color <#ffffff>");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Aby użyć tej komedy wymagana jest ranga plus lub premium!");
                        }
                    }
                    if (args[0].equalsIgnoreCase("!rainbow") || args[0].equalsIgnoreCase("!tencza")) {
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
                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !rainbow on/off");
                                }
                            } else {
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Poprawne użycie: !rainbow on/off");
                            }
                        } else {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Aby użyć tej komedy wymagana jest ranga plus lub premium!");
                        }
                    }
                    //help
                    if (args[0].equalsIgnoreCase("!help") || args[0].equalsIgnoreCase("!pomoc") || args[0].equalsIgnoreCase("!komendy")){
                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[b]Dostępne komendy:[/b]\n" +
                                "[b]!channels[/b] — Wyświetla wszystkie dostępne kanały.\n" +
                                "[b]!join <nazwa>[/b] — Dołącza do wybranego kanału\n" +
                                "[b]!leave <nazwa>[/b] — Wychodzi z wybranego kanału tekstowego.\n" +
                                "[b]!mute <3s/5m/6h>[/b] — Wycisza chat na podaną ilość sekund/minut/godzin.\n" +
                                "[b]!switch <nazwa>[/b] — Przełącza twoje odpowiedzi na wybrany kanał.\n" +
                                "[b]!check <nazwa>[/b] — Sprawdza ostatnie 15 wiadomości, bez dołączania do kanału.\n" +
                                "[b]!color <#ffea00>[/b] — Zmienia kolor twoich wiadomości. [Plus/Premium]\n" +
                                "[b]!rainbow on/off[/b] — Zmienia kolor twoich wiadomosci na tęczowy. [Premium]\n" +
                                "[b]!admin[/b] — Wyświetla pomoc dostępną tylko dla Administratorów.\n" +
                                "[b]!botinfo — Wyświetla informacje o bocie.");
                    }
                    if (args[0].equalsIgnoreCase("!slowdown")){
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
                    if (args[0].equalsIgnoreCase("!rules") || args[0].equalsIgnoreCase("!regulamin") || args[0].equalsIgnoreCase("!zasady")){
                        try {
                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), Files.readAllLines(new File("rules.txt").toPath()).get(0).replace("\n", "\n"));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //Wiadomosci
                    if (!args[0].contains("!")) {
                        try {
                            if (c.isInServerGroup(133)) {
                                //blokada ranga (BAN)
                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Administrator nałożył na Ciebie blokade!");
                            } else {
                                JSONObject jsonObject = (JSONObject) parseJSONFile("channelconfig.json");
                                if (c.isInServerGroup(7) || c.isInServerGroup(9)) {
                                    //z ranga premium/plus
                                    if (c.isInServerGroup(122) || c.isInServerGroup(123)) {
                                        if (message.length() > charlimit * 2) {
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Błąd! Zbyt duża ilośc znaków! (" + message.length() + "/" + charlimit * 2 + ")");
                                        } else {
                                            if (URL_PATTERN.matcher(message).find() || IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie reklamuj się!");
                                            } else {
                                                if (BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Przeklinanie jest dowolone wyłącznie na kanale #NSFW");
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
                                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Masz wyciszony chat użyj komendy !unmute aby go odsiczyć!");
                                                            }
                                                    } else {
                                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Wystąpił błąd, spróbuj jeszcze raz wybrać kanał oraz dołączyć do kanału, który wybrałeś/aś.");
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        //bez rangi premium/plus
                                        if (message.length() > charlimit) {
                                            TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Błąd! Zbyt duża ilośc znaków! (" + message.length() + "/" + charlimit + ")");
                                        } else {
                                            if (URL_PATTERN.matcher(message).find() || IPPATTERN.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("advertising")) {
                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Nie reklamuj się!");
                                            } else {
                                                if (BANNED_WORDS.matcher(message).find() && !jsonObject.getJSONObject(u.getSelect().toLowerCase()).getBoolean("imprecation")) {
                                                    TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "Przeklinanie jest dowolone wyłącznie na kanale #NSFW");
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
                                                                TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Masz wyciszony chat użyj komendy !unmute aby go odsiczyć!");
                                                            }
                                                    } else {
                                                        TeamSpeakUtils.api.sendPrivateMessage(c.getId(), "[color=red]Blad! [color=pink]Wystąpił błąd, spróbuj jeszcze raz wybrać kanał oraz dołączyć do kanału, który wybrałeś/aś.");
                                                    }
                                                }
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

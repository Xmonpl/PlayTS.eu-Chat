package cf.xmon.chat.utils;


import cf.xmon.chat.object.User;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageUtils {
    public static void saveMessageToFile(@NotNull Client c, @NotNull User u, @NotNull String message, @NotNull File channel){
        String write = null;
        if (c.isInServerGroup(6) || c.isInServerGroup(16)) {
                write = "\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color] \uD83D\uDD27 [b][URL=client://0/" +
                        c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]\n";
        }else if(c.isInServerGroup(122)){
            write = "\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color] \uD83D\uDCB2 [b][URL=client://0/" +
                    c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]\n";
        }else if(c.isInServerGroup(123)){
            write = "\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color] \uD83E\uDD11 [b][URL=client://0/" +
                    c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]\n";
        } else{
                write = "\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color][b][URL=client://0/" +
                        c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]\n";
        }
        try {
            if (!channel.exists()) {
                Files.write(Paths.get(channel.getName(), new String[0]), write.getBytes(), StandardOpenOption.CREATE_NEW);
            } else {
                Files.write(Paths.get(channel.getName(), new String[0]), write.getBytes(), StandardOpenOption.APPEND);
            }
        }catch (IOException e){
            Logger.warning(e.getMessage());
        }
    }
    public static<T> List<T> reverseList(List<T> list)
    {
        List<T> reverse = new ArrayList<>(list);
        Collections.reverse(reverse);
        return reverse;
    }
    public static String parserMessage(@NotNull Client c, @NotNull User u, @NotNull String message, @NotNull File channel){
        if (c.isInServerGroup(6) || c.isInServerGroup(16)){
            return "[b]Kanał: [color=#f4511e]#" + channel.getName().replace(".txt", "") + "[/color] ([color=#43a047]" + UserUtils.online.get(channel.getName().replace(".txt", "").toLowerCase()) + "/" + UserUtils.max.get(channel.getName().replace(".txt", "").toLowerCase()) + "[/color])[/b]\n\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color] \uD83D\uDD27 [b][URL=client://0/" +
                    c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]";

        }else {
            return "[b]Kanał: [color=#f4511e]#" + channel.getName().replace(".txt", "") + "[/color] ([color=#43a047]" + UserUtils.online.get(channel.getName().replace(".txt", "").toLowerCase()) + "/" + UserUtils.max.get(channel.getName().replace(".txt", "").toLowerCase()) + "[/color])[/b]\n\uD83D\uDCAC [color=#5e6165]" + getTime() + "[/color] [b][URL=client://0/" +
                    c.getUniqueIdentifier() + "~" + c.getNickname().replace(" ", "%20").replace("/", "%2F").replace("[", "%5C%5B").replace("]", "%5C%5D") + "]\"" + c.getNickname() + "\"[/URL][/b]: [color=" + setColor(u) + "]" + message + "[/color]";
        }
    }
    public static String getTime(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("<HH;mm;ss>");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    public static String setColor(@NotNull User u){
        if (u.getColor().equals("null")){
            return "#FFFFFF";
        }else if (u.getColor().equalsIgnoreCase("rainbow")){
            return TeamSpeakUtils.getRainbowColor();
        } else{
            return u.getColor();
        }
    }
}

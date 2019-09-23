package cf.xmon.chat.tasks;


import cf.xmon.chat.Main;
import cf.xmon.chat.utils.TeamSpeakUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoClearChannelsTask {
    public static Timer timer;
    public static void update() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Arrays.stream(Main.channels.split("@")).forEach(x ->{
                    if (!x.equals("")) {
                        try {
                            File path = new File(x + ".txt");
                            Scanner scanner = new Scanner(path);
                            ArrayList<String> coll = new ArrayList<String>();
                            while (scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                coll.add(line);
                            }
                            scanner.close();
                            if (coll.size() >= 8192) {
                                FileWriter writer = new FileWriter(path);
                                Integer lines = 0;
                                for (String line : coll) {
                                    if (lines >= 2048) {
                                        writer.write(line);
                                    }
                                    lines++;
                                }
                                writer.close();
                            }
                        } catch (Exception ex) {
                            TeamSpeakUtils.error(ex);
                        }
                    }
                });
            }
        }, TimeUnit.HOURS.toMillis(2) + 12, TimeUnit.HOURS.toMillis(2) + 12);
    }
}

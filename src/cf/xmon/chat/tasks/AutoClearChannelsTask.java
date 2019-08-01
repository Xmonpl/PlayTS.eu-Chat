package cf.xmon.chat.tasks;


import cf.xmon.chat.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoClearChannelsTask {
    public static Timer timer;
    public static void update() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Arrays.stream(Main.channels.split("@")).forEach(x ->{
                    if (!x.equals("")){
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
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, TimeUnit.HOURS.toMillis(2) + 12, TimeUnit.HOURS.toMillis(2) + 12);
    }
}

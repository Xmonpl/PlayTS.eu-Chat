package cf.xmon.chat.tasks;

import cf.xmon.chat.utils.TeamSpeakUtils;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AntyCrash {
    public static Timer timer;
    public static void update() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!TeamSpeakUtils.query.isConnected()){
                    Random rand = new Random();
                    String cmd = "screen -dmS chatts3-" + rand.nextInt(9) + " java -Xmx128M -Xms64M -jar x-Chat.jar";
                    try {
                        Runtime run = Runtime.getRuntime();
                        Process pr = run.exec(cmd);
                        pr.waitFor();
                        System.exit(-1);
                    }catch (Exception e){ }
                }
            }
        }, TimeUnit.MINUTES.toMillis(1) + 52, TimeUnit.MINUTES.toMillis(1) + 52);
    }
}

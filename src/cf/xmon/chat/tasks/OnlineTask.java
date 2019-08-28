package cf.xmon.chat.tasks;

import cf.xmon.chat.Main;
import cf.xmon.chat.utils.UserUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OnlineTask {
    public static Timer timer;
    public static void update() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                UserUtils.loadOnline();
                Main.online.putAll(UserUtils.onlinenew);
                Main.max.putAll(UserUtils.maxnew);
                System.out.println("Suma " + (System.currentTimeMillis() - start)  + "ms");
            }
        }, TimeUnit.MINUTES.toMillis(2) + 15, TimeUnit.MINUTES.toMillis(2) + 15);
    }
}

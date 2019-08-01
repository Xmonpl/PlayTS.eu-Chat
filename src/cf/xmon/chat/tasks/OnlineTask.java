package cf.xmon.chat.tasks;

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
                System.out.println("Załadowano online w " + (System.currentTimeMillis() - start) + "ms!");
            }
        }, TimeUnit.SECONDS.toMillis(30) + 15, TimeUnit.SECONDS.toMillis(30) + 15);
    }
}

package cf.xmon.chat.tasks;

import cf.xmon.chat.utils.UserUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OnlineTask {
    public static void update() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                UserUtils.loadOnline();
            }
        }, TimeUnit.SECONDS.toMillis(30) + 15, TimeUnit.SECONDS.toMillis(30) + 15);
    }
}

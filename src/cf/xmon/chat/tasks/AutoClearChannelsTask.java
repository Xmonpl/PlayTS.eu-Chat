package cf.xmon.chat.tasks;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AutoClearChannelsTask {
    public static void update() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //@TODO dodać
            }
        }, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1));
    }
}

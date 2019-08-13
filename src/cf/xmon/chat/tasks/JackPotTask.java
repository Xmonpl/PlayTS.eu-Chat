package cf.xmon.chat.tasks;

import cf.xmon.chat.events.ChatEvent;
import cf.xmon.chat.object.User;
import cf.xmon.chat.utils.RandomUtil;
import cf.xmon.chat.utils.TeamSpeakUtils;
import cf.xmon.chat.utils.UserUtils;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class JackPotTask {
    public static Timer timer;
    private static Integer timetoEnd = 0;
    public static void update() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ChatEvent.jackpot != null){
                    if (ChatEvent.jackpot.size() == 1){
                        if (timetoEnd == 3) {
                            TeamSpeakUtils.api.getClients().forEach(x ->{
                                if (x.isRegularClient()) {
                                    User ux = UserUtils.get(x);
                                    if (ux.getChannels().toLowerCase().contains("gaming")) {
                                        if (System.currentTimeMillis() > ux.getMute()) {
                                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "[b][color=gray][[color=gold]JACKPOT[color=gray]] [color=red]Przepraszamy, nikt nie dołączył do puli. Pięniądze wróciły do ");
                                        }
                                    }
                                }
                            });
                            ChatEvent.jackpot.forEach((c, kwota) -> {
                                User u = UserUtils.get(c);
                                u.setMoney(u.getMoney() + ChatEvent.jackpotKwota);
                            });
                            timetoEnd = 0;
                            ChatEvent.jackpotKwota = 0;
                            ChatEvent.jackpot.clear();
                            timer.cancel();
                            timer.purge();
                        }
                    }else{
                        if (timetoEnd == 3) {
                            List<Client> winnerList = new ArrayList<>();
                            ChatEvent.jackpot.forEach((c, kwota) -> {
                                if (!winnerList.contains(c)) {
                                    winnerList.add(c);
                                }
                            });
                            int ii = RandomUtil.getNextInt(winnerList.size());
                            Client win = winnerList.get(ii);
                            User u = UserUtils.get(win);
                            u.setMoney(u.getMoney() + ChatEvent.jackpotKwota);
                            TeamSpeakUtils.api.getClients().forEach(x ->{
                                if (x.isRegularClient()) {
                                    User ux = UserUtils.get(x);
                                    if (ux.getChannels().toLowerCase().contains("gaming")) {
                                        if (System.currentTimeMillis() > ux.getMute()) {
                                            TeamSpeakUtils.api.sendPrivateMessage(x.getId(), "[b][color=gray][[color=gold]JACKPOT[color=gray]] [color=green]Szcześliwym zwycieżcą puli o łącznej wartości " + ChatEvent.jackpotKwota + "$ jest " + win.getNickname());
                                        }
                                    }
                                }
                            });
                            ChatEvent.jackpotKwota = 0;
                            ChatEvent.jackpot.clear();
                            timetoEnd = 0;
                            timer.cancel();
                            timer.purge();
                        }
                    }
                    ++timetoEnd;
                }else{
                    ChatEvent.jackpotKwota = 0;
                    ChatEvent.jackpot.clear();
                    timetoEnd = 0;
                    timer.cancel();
                    timer.purge();
                }
            }
        }, TimeUnit.SECONDS.toMillis(20) + 52, TimeUnit.SECONDS.toMillis(20) + 52);
    }
}

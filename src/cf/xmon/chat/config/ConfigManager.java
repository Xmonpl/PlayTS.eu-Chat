package cf.xmon.chat.config;

import cf.xmon.chat.utils.JsonUtil;
import cf.xmon.chat.utils.TeamSpeakUtils;
import org.jetbrains.annotations.Contract;

import java.io.File;

public class ConfigManager
{
    private static Config botConfig;

    public ConfigManager() {
        try {
            ConfigManager.botConfig = JsonUtil.readConfiguration(Config.class, new File("config.json"));
        } catch (Exception ex) {
            TeamSpeakUtils.error(ex);
            ConfigManager.botConfig = null;
        }
    }

    @Contract(pure = true)
    public static Config getConfig() {
        return ConfigManager.botConfig;
    }
}


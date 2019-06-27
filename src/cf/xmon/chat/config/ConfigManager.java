package cf.xmon.chat.config;

import cf.xmon.chat.utils.JsonUtil;
import org.jetbrains.annotations.Contract;

import java.io.File;

public class ConfigManager
{
    private static Config botConfig;

    public ConfigManager() {
        try {
            ConfigManager.botConfig = JsonUtil.readConfiguration(Config.class, new File("config.json"));
        }
        catch (Exception e) {
            e.printStackTrace();
            ConfigManager.botConfig = null;
        }
    }

    @Contract(pure = true)
    public static Config getConfig() {
        return ConfigManager.botConfig;
    }
}


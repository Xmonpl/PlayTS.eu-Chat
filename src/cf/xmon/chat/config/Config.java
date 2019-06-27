package cf.xmon.chat.config;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Contract;

public class Config
{
    @SerializedName("Config")
    private Instance Instance;

    @Contract(pure = true)
    public Config() {
        this.Instance = new Instance();
    }

    public Instance getInstance() {
        return this.Instance;
    }
}
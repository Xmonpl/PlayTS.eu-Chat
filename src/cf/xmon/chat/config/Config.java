package cf.xmon.chat.config;

import com.google.gson.annotations.SerializedName;

public class Config
{
    @SerializedName("Config")
    private Instance Instance;

    public Config() {
        this.Instance = new Instance();
    }

    public Instance getInstance() {
        return this.Instance;
    }
}
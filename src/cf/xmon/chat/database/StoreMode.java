package cf.xmon.chat.database;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum StoreMode
{
    SQLITE("sqlite"),
    MYSQL("mysql");

    private String name;

    @Contract(pure = true)
    private StoreMode(final String name) {
        this.name = name;
    }

    @Nullable
    public static StoreMode getByName(final String name) {
        for (final StoreMode sm : values()) {
            if (sm.getName().equalsIgnoreCase(name)) {
                return sm;
            }
        }
        return null;
    }

    @Contract(pure = true)
    public String getName() {
        return this.name;
    }
}
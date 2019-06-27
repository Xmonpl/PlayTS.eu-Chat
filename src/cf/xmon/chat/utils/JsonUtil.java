package cf.xmon.chat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;

public class JsonUtil
{
    private static final Gson gson;

    public static <T> T readConfiguration(final Class<T> configurationClass, @NotNull final File file) throws Exception {
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("Generowanie configu..");
            Files.write(file.toPath(), JsonUtil.gson.toJson(configurationClass.newInstance()).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        return JsonUtil.gson.fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), configurationClass);
    }

    @Nullable
    public static <T> T readObjectFromFile(final Class<T> object, @NotNull final File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        return JsonUtil.gson.fromJson(reader, object);
    }

    public static void writeObjectToFile(final Object object, @NotNull final File file) throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileWriter writer = new FileWriter(file);
        writer.write(JsonUtil.gson.toJson(object));
        writer.close();
    }

    static {
        gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }
}

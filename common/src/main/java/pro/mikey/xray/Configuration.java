package pro.mikey.xray;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Neoforge like builder based configuration system. This is a relatively simple and thus limited and lightweight
 * implementation of a "configuration system" that allows for easy reading and writing of configuration values
 */
public enum Configuration {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger("XRay Configuration");

    private final static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final static String CONFIG_FILE_NAME = "xray-client.json";

    private final List<ConfigValue<?>> values = new ArrayList<>();

    public final ConfigValue<Boolean> showOverlay = bool("showOverlay", () -> true)
            .comment("This allows you hide or show the overlay in the top right of the screen when XRay is enabled")
            .build();

    public final ConfigValue<Integer> radius = integer("radius", () -> 2)
            .comment("The radius area (in chunks in each direction) that XRay will search within.")
                    .comment("This is in each direction so a radius of 1 will be a 3x3 area, a radius of 2 will be a 5x5 area, etc.")
                    .build();

    public final ConfigValue<Boolean> lavaActive = bool("lavaActive", () -> false)
            .comment("When true, lava will automatically be added to the scan list when XRay is enabled.")
                    .build();

    Configuration() {}

    public void load() {
        JsonObject rootData;
        try {
            var configPath = XRay.XPLAT.configPath().get().resolve(CONFIG_FILE_NAME);
            if (Files.notExists(configPath.getParent())) {
                // If the config directory does not exist, create it
                Files.createDirectories(configPath.getParent());
            }

            if (Files.notExists(configPath)) {
                // If the config file does not exist, create it with default values
                save();
            }

            String jsonContent = Files.readString(configPath);
            rootData = GSON.fromJson(jsonContent, JsonObject.class);
        } catch (Exception e) {
            // If there is an error reading the file, log it and create a new config
            LOGGER.error("Failed to load configuration file", e);
            rootData = new JsonObject();
        }

        // Init all values
        for (ConfigValue<?> value : values) {
            value.read(rootData);
        }
    }

    private void save() {
        JsonObject root = new JsonObject();

        // Write all values
        for (ConfigValue<?> value : values) {
            value.write(root);
        }

        try {
            var configPath = XRay.XPLAT.configPath().get().resolve(CONFIG_FILE_NAME);
            // Ensure the parent directory exists
            if (Files.notExists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }

            Files.writeString(configPath, GSON.toJson(root));
        } catch (Exception e) {
            LOGGER.error("Failed to save configuration file: {}", e.getMessage());
        }
    }

    private ValueBuilder<Boolean> bool(String name, Supplier<Boolean> defaultValue) {
        return new ValueBuilder<>(name, defaultValue, JsonElement::getAsBoolean, JsonPrimitive::new);
    }

    private ValueBuilder<Integer> integer(String name, Supplier<Integer> defaultValue) {
        return new ValueBuilder<>(name, defaultValue, JsonElement::getAsInt, JsonPrimitive::new);
    }

    public static class ConfigValue<T> {
        private final Configuration parent;

        private T value;
        private final String name;
        private final List<String> comment;
        private final Supplier<T> defaultValue;

        private final Function<JsonElement, T> reader;
        private final Function<T, JsonElement> writer;

        public ConfigValue(Configuration parent, String name, Supplier<T> defaultValue, List<String> comment,
                           Function<JsonElement, T> reader, Function<T, JsonElement> writer) {
            this.parent = parent;
            this.name = name;
            this.defaultValue = defaultValue;
            this.comment = comment;
            this.reader = reader;
            this.writer = writer;
            this.value = null;
        }

        public T get() {
            if (value == null) {
                value = defaultValue.get();
            }
            return value;
        }

        public void set(T value) {
            this.value = value;
            parent.save(); // Save the configuration whenever a value is set
        }

        public void write(JsonObject root) {
            JsonObject entryObj = new JsonObject();

            if (!comment.isEmpty()) {
                var comments = new JsonArray();
                for (String line : comment) {
                    comments.add(new JsonPrimitive(line));
                }

                entryObj.add("comments", comments);
            }

            JsonElement jsonValue = writer.apply(get());
            entryObj.add("value", jsonValue);

            root.add(name, entryObj);
        }

        public void read(JsonObject rootData) {
            if (rootData.has(name)) {
                JsonObject element = rootData.get(name).getAsJsonObject();
                if (element.has("value")) {
                    value = reader.apply(element.get("value"));
                }
            }
        }
    }

    private class ValueBuilder<T> {
        private final String name;
        private final Supplier<T> defaultValue;
        private final List<String> comment = new ArrayList<>();

        private final Function<JsonElement, T> reader;
        private final Function<T, JsonElement> writer;

        public ValueBuilder(String name, Supplier<T> defaultValue, Function<JsonElement, T> reader, Function<T, JsonElement> writer) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.reader = reader;
            this.writer = writer;
        }

        public ValueBuilder<T> comment(String comment) {
            this.comment.add(comment);
            return this;
        }

        public ConfigValue<T> build() {
            ConfigValue<T> configValue = new ConfigValue<>(Configuration.this, name, defaultValue, comment, reader, writer);
            Configuration.this.values.add(configValue);
            return configValue;
        }
    }
}

package com.readutf.inari.core.utils.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    @SuppressWarnings("EmptyClass")
    private final Type objectStringMapType = new TypeToken<LinkedHashMap<String, Object>>() {
    }.getType();

    /**
     * Deserialize a ConfigurationSerializable object from a JsonElement.
     *
     * @param json The JsonElement to deserialize
     * @param type The type of the object to deserialize
     * @param context The context of the deserialization
     * @return The deserialized ConfigurationSerializable object
     * @throws JsonParseException If the JsonElement is not a valid ConfigurationSerializable object
     */
    @Override
    public ConfigurationSerializable deserialize(@NotNull JsonElement json, Type type, JsonDeserializationContext context) {
        Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            String name = entry.getKey();

            if (value.isJsonObject()) {
                JsonObject jsonObject = value.getAsJsonObject();

                if (jsonObject.has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    map.put(name, deserialize(value, value.getClass(), context));
                } else {
                    Map<String, Object> mapInMap = new LinkedHashMap<>();

                    for (Map.Entry<String, JsonElement> secondEntry : jsonObject.entrySet()) {
                        JsonElement element = secondEntry.getValue();

                        if (element.isJsonObject()) {
                            mapInMap.put(secondEntry.getKey(), deserialize(secondEntry.getValue(), secondEntry.getClass(), context));
                        } else if (element.isJsonArray()) {
                            JsonArray array = element.getAsJsonArray();
                            List<Object> objectsList = new ArrayList<>();

                            for (JsonElement arrayElement : array) {
                                objectsList.add(deserialize(arrayElement, arrayElement.getClass(), context));
                            }

                            mapInMap.put(secondEntry.getKey(), objectsList);
                        } else {
                            mapInMap.put(secondEntry.getKey(), context.deserialize(element, Object.class));
                        }
                    }

                    map.put(name, mapInMap);
                }
            } else if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<Object> objectsList = new ArrayList<>();

                for (JsonElement element : array) {
                    if (element.isJsonObject() && element.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        objectsList.add(deserialize(element, element.getClass(), context));
                    } else {
                        objectsList.add(context.deserialize(element, Object.class));
                    }
                }

                map.put(name, objectsList);
            } else {
                map.put(name, context.deserialize(value, Object.class));
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    @Override
    public JsonElement serialize(@NotNull ConfigurationSerializable src, Type type, @NotNull JsonSerializationContext context) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
        map.putAll(src.serialize());
        return context.serialize(map, objectStringMapType);
    }
}

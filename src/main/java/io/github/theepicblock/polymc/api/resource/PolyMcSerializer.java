package io.github.theepicblock.polymc.api.resource;

import com.google.gson.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * A Gson serializer that can serialize objects
 * that implement the JsonSerializable interface
 */
public class PolyMcSerializer implements JsonSerializer<PolyMcSerializer.JsonSerializable>
{

    /**
     * Create a new Gson instance with the PolyMcSerializer registered
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeHierarchyAdapter(JsonSerializable.class, new PolyMcSerializer())
                .create();
    }

    /**
     * Deserialize using the `fromJson` method
     */
    public static <T extends JsonDeserializable> T deserialize(JsonElement json, Class<T> classOfT) {

        T instance;

        try {
            instance = classOfT.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            return null;
        }

        instance.fromJson(json);
        return instance;
    }

    /**
     * The actual serialization logic that will tell Gson
     * to use the toJson method of the JsonSerializable object
     */
    @Override
    public JsonElement serialize(JsonSerializable src, Type typeOfSrc, JsonSerializationContext context) {

        if (src == null) {
            return null;
        }

        return src.toJson(context);
    }

    /**
     * The actual interface classes should implement in order
     * to use custom serialization logic
     */
    public interface JsonSerializable {
        JsonElement toJson(JsonSerializationContext context);
    }

    /**
     * The actual interface classes should implement in order
     * to use custom deserialization logic
     */
    public interface JsonDeserializable {
        void fromJson(JsonElement json);
    }
}

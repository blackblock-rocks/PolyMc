package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.PolyMcSerializer;
import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateMultipart;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateVariant;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;

@ApiStatus.Internal
public class JBlockStateImpl implements JBlockState, PolyMcSerializer.JsonSerializable {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    @SerializedName(value = "credit", alternate = "__comment")
    private String credit;

    public final Map<String, JsonElement> variants = new TreeMap<>();
    public List<JsonElement> multipart = null;
    private Map<String, JsonElement> vanillaMultipartIds = new TreeMap<>();

    private String namespace = null;
    private String name = null;
    private Identifier blockId = null;

    public JBlockStateImpl() {
    }

    public void setBlock(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
        this.blockId = new Identifier(namespace, name);
    }

    @Nullable
    private Block getBlock() {

        if (!"minecraft".equals(this.namespace)) {
            return null;
        }

        return Registries.BLOCK.get(this.blockId);

    }

    public void convertToMultipart() {

        if (this.variants.isEmpty()) {
            return;
        }

        Block block = this.getBlock();

        if (block == null) {
            return;
        }

        Map<String, JsonElement> variants = new TreeMap<>(this.variants);
        this.variants.clear();

        // The variants might not target all properties,
        // so we have to duplicate them to make sure that they do
        StateManager<Block, BlockState> manager = block.getStateManager();
        List<BlockState> allStates = manager.getStates();
        Collection<Property<?>> allProperties = manager.getProperties();

        // Iterate over all the variants
        variants.forEach((propertyString, jsonElement) -> {

            List<BlockState> matchingStates = new ArrayList<>();
            Map<String, String> properties = Util.getPropertyMap(propertyString);

            // Iterate over all the blockstates and find the ones that match
            for (BlockState state : allStates) {
                // See if this state matches the property string
                boolean matches = true;

                for (var entry : properties.entrySet()) {
                    Property<?> property = manager.getProperty(entry.getKey());

                    if (property == null) {
                        matches = false;
                        break;
                    }

                    Comparable<?> value = state.get(property);

                    // If the value is null, return false
                    if (value == null) {
                        matches = false;
                        break;
                    }

                    if (!value.toString().equals(entry.getValue())) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    matchingStates.add(state);
                }
            }

            // Create multipart entries out of all the matching states
            for (BlockState matchingState : matchingStates) {
                String stateId = Util.getPropertiesFromBlockState(matchingState);
                JsonElement stateMultipartEntry = JBlockStateMultipart.jsonElementFrom(stateId, jsonElement);
                this.setMultipart(stateId, stateMultipartEntry);
                this.vanillaMultipartIds.put(stateId, stateMultipartEntry);
            }
        });
    }

    @ApiStatus.Internal
    public static JBlockStateImpl of(InputStream inputStream, @Nullable String name) {
        try (var jsonReader = new JsonReader(new InputStreamReader(inputStream));) {
            jsonReader.setLenient(true);

            return Util.GSON.fromJson(jsonReader, JBlockStateImpl.class);
        } catch (JsonSyntaxException | IOException e) {
            throw new ResourceGenerationException("Error reading block state definition for "+name, e);
        }
    }

    @Override
    @Nullable
    public String getMultipartVariantId(BlockState state) {
        return Util.getPropertiesFromBlockState(state);
    }

    /**
     * Try to get all the multipart variants that match the given blockstate.
     * If none are found, return null.
     */
    @Override
    @Nullable
    public JBlockStateVariant[] getMultipartVariantsBestMatching(BlockState state) {

        if (this.multipart == null || this.multipart.isEmpty()) {
            return null;
        }

        List<JBlockStateVariant> matching_multiparts = new ArrayList<>();

        // If no variants were found, check multipart
        for (JsonElement entry : this.multipart) {

            JBlockStateMultipart multipart = JBlockStateMultipart.from(entry);

            if (multipart == null) {
                continue;
            }

            if (multipart.matches(state)) {
                JBlockStateVariant apply = multipart.getApply();

                if (apply != null) {
                    matching_multiparts.add(apply);
                }
            }
        }

        if (!matching_multiparts.isEmpty()) {
            return matching_multiparts.toArray(new JBlockStateVariant[0]);
        }

        return null;
    }

    /**
     * The json can contain either a single variant object or a list of them. This normalizes them to a list.
     */
    private static JBlockStateVariant[] getVariantsFromJsonElement(JsonElement input) {
        if (input instanceof JsonObject jsonObject) {
            var variant = PolyMcSerializer.deserialize(jsonObject, JBlockStateVariant.class);
            var returnArray = new JBlockStateVariant[1];
            returnArray[0] = variant;
            return returnArray;
        }
        if (input instanceof JsonArray jsonArray) {
            return new Gson().fromJson(jsonArray, JBlockStateVariant[].class);
        }
        return new JBlockStateVariant[0];
    }

    private static JsonElement variantsToJsonElement(JBlockStateVariant[] variants) {
        if (variants.length == 0) {
            return null;
        } else if (variants.length == 1) {
            new Gson().toJsonTree(variants[0]);
        }
        return new Gson().toJsonTree(variants);
    }

    @Override
    public void setVariant(String propertyString, JBlockStateVariant[] variants) {
        //this.variants.put(propertyString, variantsToJsonElement(variants));
        this.setMultipart(propertyString, JBlockStateMultipart.jsonElementFrom(propertyString, variants));
    }

    @Override
    public void setMultipart(String propertyString, JBlockStateVariant[] variants) {
        this.setMultipart(propertyString, JBlockStateMultipart.jsonElementFrom(propertyString, variants));
    }

    protected void setMultipart(String propertyString, JsonElement multipart) {

        if (this.multipart == null) {
            this.multipart = new ArrayList<>();
        }

        // Make sure to delete any possible variant matching this property string,
        // since variants take precedence over multipart
        this.variants.remove(propertyString);
        this.multipart.add(multipart);

        if (this.vanillaMultipartIds.containsKey(propertyString)) {
            JsonElement originalEntry = this.vanillaMultipartIds.get(propertyString);

            // Remove the element from the multipart list
            this.multipart.remove(originalEntry);
            this.vanillaMultipartIds.remove(propertyString);
        }
    }

    @Override
    public JBlockStateVariant[] getVariants(String variantString) {
        JBlockStateVariant[] variants = getVariantsFromJsonElement(this.variants.get(variantString));

        // If variants is not null or empty, return it
        if (variants != null && variants.length != 0) {
            return variants;
        }

        // Return empty array if no variants were found
        return new JBlockStateVariant[0];
    }

    @Override
    public Set<String> getPropertyStrings() {
        return this.variants.keySet();
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        Util.writeJsonToStream(stream, gson, this);
    }

    @Override
    public JsonElement toJson(JsonSerializationContext context) {

        JsonObject result = new JsonObject();

        if (this.credit != null) {
            result.addProperty("credit", this.credit);
        }

        if (!this.variants.isEmpty()) {
            JsonObject variantObject = new JsonObject();

            for (var entry : this.variants.entrySet()) {
                variantObject.add(entry.getKey(), entry.getValue());
            }

            result.add("variants", variantObject);
        }

        if (this.multipart != null) {
            JsonArray multipartArray = new JsonArray();

            for (JsonElement entry : this.multipart) {
                multipartArray.add(entry);
            }

            result.add("multipart", multipartArray);
        }

        return result;
    }
}

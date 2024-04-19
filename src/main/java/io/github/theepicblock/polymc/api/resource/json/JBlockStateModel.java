package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.PolyMcSerializer;

public class JBlockStateModel implements
        AssetWithDependencies,
        PolyMcSerializer.JsonDeserializable {

    private String model;
    private Integer x;
    private Integer y;
    private Boolean uvlock;


    @Override
    public void fromJson(JsonElement json) {

    }
}

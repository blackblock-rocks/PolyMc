package io.github.theepicblock.polymc.api.resource.json;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JElement {
    private final double[] from;
    private final double[] to;
    private final JElementRotation rotation;
    private final Boolean shade;
    private final Integer light_emission;
    private final Map<JDirection, JElementFace> faces;

    public JElement(double[] from, double[] to, JElementRotation rotation, Map<JDirection,JElementFace> faces, @Nullable Boolean shade, @Nullable Integer light_emission) {
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.shade = shade;
        this.faces = faces;
        this.light_emission = light_emission;
    }

    public double[] from() {
        return from;
    }

    public double[] to() {
        return to;
    }

    public JElementRotation rotation() {
        return rotation;
    }

    public Boolean shade() {
        return shade;
    }

    public Integer light_emission() {
        return light_emission;
    }

    public Map<JDirection,JElementFace> faces() {
        return faces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JElement that = (JElement)o;
        return ((this.light_emission == null || that.light_emission == null) ? this.light_emission == that.light_emission : this.light_emission.intValue() == that.light_emission.intValue()) &&
                ((this.shade == null || that.shade == null) ? this.shade == that.shade : this.shade.booleanValue() == that.shade.booleanValue()) &&
                this.faces.equals(that.faces) && Arrays.equals(this.from, that.from) && Arrays.equals(this.to, that.to) && this.rotation.equals(that.rotation);
    }

    @Override
    public int hashCode() {
        int result;
        if (shade != null) {
            if (light_emission != null) result = Objects.hash(rotation, faces, shade, light_emission);
            else result = Objects.hash(rotation, faces, shade);
        }
        else {
            if (light_emission != null) result = Objects.hash(rotation, faces, light_emission);
            else result = Objects.hash(rotation, faces);
        }
        result = 31 * result + Arrays.hashCode(from);
        result = 31 * result + Arrays.hashCode(to);
        return result;
    }
}

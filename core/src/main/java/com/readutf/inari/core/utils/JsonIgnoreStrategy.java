package com.readutf.inari.core.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.jetbrains.annotations.NotNull;

public class JsonIgnoreStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(@NotNull FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(JsonIgnore.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}

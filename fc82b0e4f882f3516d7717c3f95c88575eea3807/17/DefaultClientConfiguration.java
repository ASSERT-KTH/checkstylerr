package com.piksel.sequoia.clientsdk.configuration;

import com.google.common.collect.ForwardingList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.clientsdk.resource.*;
import com.piksel.sequoia.clientsdk.resource.json.*;

import java.util.Arrays;
import java.util.Collection;

@Internal
public class DefaultClientConfiguration {

    public static Gson getDefaultGson() {
        return getDefaultGsonBuilder().create();
    }

    /**
     * Produce the default set of type adapters.
     */
    public static Collection<TypeAdapter> getDefaultTypeAdapters() {
        return Arrays.asList(
                TypeAdapter.builder().type(ResourceMap.class)
                        .typeAdapter(new ResourceMapSerializer()).build(),
                TypeAdapter.builder().type(ResourceMap.class)
                        .typeAdapter(new ResourceMapDeserializer()).build(),
                TypeAdapter.builder().type(ResourceList.class)
                        .typeAdapter(new ResourceListSerializer()).build(),
                TypeAdapter.builder().type(ResourceList.class)
                        .typeAdapter(new ResourceListDeserializer()).build(),
                TypeAdapter.builder().type(Reference.class)
                        .typeAdapter(new ReferenceSerializer()).build(),
                TypeAdapter.builder().type(Reference.class)
                        .typeAdapter(new ReferenceDeserializer()).build(),
                TypeAdapter.builder().superClass(ForwardingList.class)
                        .typeAdapter(new ForwardingListDeserializer()).build(),
                TypeAdapter.builder().superClass(ForwardingList.class)
                        .typeAdapter(new ForwardingListSerializer()).build(),
                TypeAdapter.builder().superClass(MetadataLockFieldValue.class)
                        .typeAdapter(new MetadataLockFieldValueSerializer()).build(),
                TypeAdapter.builder().superClass(MetadataLockFieldValue.class)
                        .typeAdapter(new MetadataLockFieldValueDeserializer()).build(),
                TypeAdapter.builder().superClass(MetadataLockField.class)
                        .typeAdapter(new MetadataLockFieldSerializer()).build(),
                TypeAdapter.builder().superClass(MetadataLockField.class)
                        .typeAdapter(new MetadataLockFieldDeserializer()).build()
                );
    }

    public static GsonBuilder getDefaultGsonBuilder() {
        return addDefaulTypeAdatpers(new GsonBuilder());
    }

    /**
     * Add a type adapter to the default set.
     */
    public static GsonBuilder addDefaulTypeAdatpers(GsonBuilder gsonBuilder) {
        getDefaultTypeAdapters().forEach((e) -> addAdapter(gsonBuilder, e));
        return gsonBuilder;
    }

    /**
     * Add a type adapter to the {@link GsonBuilder}.
     */
    public static GsonBuilder addTypeAdapters(GsonBuilder gsonBuilder,
            Collection<TypeAdapter> typeAdapters) {
        typeAdapters.forEach((e) -> addAdapter(gsonBuilder, e));
        return gsonBuilder;
    }

    private static void addAdapter(GsonBuilder gsonBuilder, TypeAdapter e) {
        if (e.isHierarchyAdapter()) {
            gsonBuilder.registerTypeHierarchyAdapter(e.getSuperClass(),
                    e.getTypeAdapter());
        } else {
            gsonBuilder.registerTypeAdapter(e.getType(), e.getTypeAdapter());
        }
    }

    public static Gson createGson(GsonBuilder gsonBuilder,
            Collection<TypeAdapter> typeAdapters) {
        return addTypeAdapters(addDefaulTypeAdatpers(gsonBuilder), typeAdapters)
                .create();
    }
}

package com.piksel.sequoia.clientsdk.resource.json;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.json.Json;
import com.google.api.client.util.Preconditions;
import com.google.gson.Gson;
import com.piksel.sequoia.annotations.Internal;

/**
 * This class has been designed in order to use Gson rather than JsonFactory.
 */
@Internal
public class JsonHttpContent extends AbstractHttpContent {

    private final Object data;

    private final Gson gson;

    public JsonHttpContent(Gson gson, Object data) {
        super(Json.MEDIA_TYPE);
        this.gson = Preconditions.checkNotNull(gson);
        this.data = Preconditions.checkNotNull(data);
    }

    public void writeTo(OutputStream out) throws IOException {
        BufferedOutputStream bf = new BufferedOutputStream(out);
        bf.write(gson.toJson(data).getBytes(UTF_8));
        bf.flush();
    }

    @Override
    public JsonHttpContent setMediaType(HttpMediaType mediaType) {
        super.setMediaType(mediaType);
        return this;
    }

    public final Object getData() {
        return data;
    }

}
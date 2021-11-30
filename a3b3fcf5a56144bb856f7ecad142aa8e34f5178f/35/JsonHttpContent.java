package com.piksel.sequoia.clientsdk.resource.json;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.translate.spi;

import static com.google.cloud.translate.spi.TranslateRpc.Option.SOURCE_LANGUAGE;
import static com.google.cloud.translate.spi.TranslateRpc.Option.TARGET_LANGUAGE;
import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.DetectionsResourceItems;
import com.google.api.services.translate.model.LanguagesResource;
import com.google.api.services.translate.model.TranslationsResource;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultTranslateRpc implements TranslateRpc {

  private final TranslateOptions options;
  private final Translate translate;

  public DefaultTranslateRpc(TranslateOptions options) {
    HttpTransport transport = options.getHttpTransportFactory().create();
    HttpRequestInitializer initializer = options.getHttpRequestInitializer();
    this.options = options;
    translate = new Translate.Builder(transport, new JacksonFactory(), initializer)
        .setRootUrl(options.getHost())
        .setApplicationName(options.getApplicationName())
        .build();
  }

  private static TranslateException translate(IOException exception) {
    return new TranslateException(exception);
  }

  @Override
  public List<List<DetectionsResourceItems>> detect(List<String> texts) {
    try {
      List<List<DetectionsResourceItems>> detections =
          translate.detections().list(texts).setKey(options.getApiKey()).execute().getDetections();
      return detections != null ? detections : ImmutableList.<List<DetectionsResourceItems>>of();
    } catch (IOException ex) {
      throw translate(ex);
    }
  }

  @Override
  public List<LanguagesResource> listSupportedLanguages(Map<Option, ?> optionMap) {
    try {
      List<LanguagesResource> languages = translate.languages()
          .list()
          .setKey(options.getApiKey())
          .setTarget(
              firstNonNull(TARGET_LANGUAGE.getString(optionMap), options.getTargetLanguage()))
          .execute().getLanguages();
      return languages != null ? languages : ImmutableList.<LanguagesResource>of();
    } catch (IOException ex) {
      throw translate(ex);
    }
  }

  @Override
  public List<TranslationsResource> translate(List<String> texts, Map<Option, ?> optionMap) {
    try {
      String targetLanguage =
          firstNonNull(TARGET_LANGUAGE.getString(optionMap), options.getTargetLanguage());
      final String sourceLanguage = SOURCE_LANGUAGE.getString(optionMap);
      List<TranslationsResource> translations =
          translate.translations()
              .list(texts, targetLanguage)
              .setSource(sourceLanguage)
              .setKey(options.getApiKey())
              .execute()
              .getTranslations();
      return Lists.transform(
          translations != null ? translations : ImmutableList.<TranslationsResource>of(),
          new Function<TranslationsResource, TranslationsResource>() {
            @Override
            public TranslationsResource apply(TranslationsResource translationsResource) {
              if (translationsResource.getDetectedSourceLanguage() == null) {
                translationsResource.setDetectedSourceLanguage(sourceLanguage);
              }
              return translationsResource;
            }
          });
    } catch (IOException ex) {
      throw translate(ex);
    }
  }
}

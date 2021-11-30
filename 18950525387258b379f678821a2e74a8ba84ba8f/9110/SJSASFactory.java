/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.deployment.annotation.factory;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessor;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.factory.Factory;
import org.glassfish.apf.impl.AnnotationProcessorImpl;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory is responsible for initializing a ready to use
 * AnnotationProcessor.
 *
 * @author Shing Wai Chan
 */
@Service
@Singleton
public class SJSASFactory extends Factory {

    @Inject
    private ServiceLocator locator;

    private Set<String> annotationClassNames = new HashSet<String>();
    private Set<String> annotationClassNamesMetaDataComplete = new HashSet<String>();

    // we have two system processors to process annotations:
    // one to process all JavaEE annotations when metadata-complete is false,
    // another to process a subset of JavaEE annotations when
    // metadata-complete is true
    private AnnotationProcessorImpl systemProcessor=null;
    private AnnotationProcessorImpl systemProcessorMetaDataComplete=null;

    public AnnotationProcessor getAnnotationProcessor(boolean isMetaDataComplete) {
        AnnotationProcessorImpl processor =
            Factory.getDefaultAnnotationProcessor();
        if (!isMetaDataComplete) {
            processor.setDelegate(systemProcessor);
        } else {
            processor.setDelegate(systemProcessorMetaDataComplete);
        }
        return processor;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAnnotations(boolean isMetaDataComplete) {
        if (!isMetaDataComplete) {
            return (HashSet<String>)((HashSet<String>)annotationClassNames).clone();
        } else {
            return (HashSet<String>)((HashSet<String>)annotationClassNamesMetaDataComplete).clone();
        }
    }

    private static String getAnnotationHandlerForStringValue(ActiveDescriptor<AnnotationHandler> onMe) {
        Map<String, List<String>> metadata = onMe.getMetadata();
        List<String> answers = metadata.get(AnnotationHandler.ANNOTATION_HANDLER_METADATA);
        if (answers == null || answers.isEmpty()) return null;

        return answers.get(0);
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @PostConstruct
    private void postConstruct() {
        if (systemProcessor != null &&
            systemProcessorMetaDataComplete != null) return;

        // initialize our system annotation processor...
        systemProcessor = new AnnotationProcessorImpl();
        systemProcessorMetaDataComplete = new AnnotationProcessorImpl();
        for (ActiveDescriptor<?> i : locator.getDescriptors(BuilderHelper.createContractFilter(
                AnnotationHandler.class.getName()))) {
            ActiveDescriptor<AnnotationHandler> descriptor = (ActiveDescriptor<AnnotationHandler>) i;

            String annotationTypeName = getAnnotationHandlerForStringValue(descriptor);
            if (annotationTypeName == null) continue;

            systemProcessor.pushAnnotationHandler(annotationTypeName, new LazyAnnotationHandler(descriptor));
            annotationClassNames.add("L" +
                    annotationTypeName.
                    replace('.', '/') + ";");

            // In the current set of the annotations processed by the
            // deployment layer, the only annotation that should be
            // processed even when metadata-complete atribute value is true
            // is jakarta.annotation.ManagedBean. If there are more annotations
            // falling in this category in the future, add them to this list
            if (annotationTypeName.equals("jakarta.annotation.ManagedBean")) {
                systemProcessorMetaDataComplete.pushAnnotationHandler(annotationTypeName, new LazyAnnotationHandler(descriptor));
                annotationClassNamesMetaDataComplete.add("L" +
                    annotationTypeName.
                    replace('.', '/') + ";");
            }
        }
    }

    private class LazyAnnotationHandler implements AnnotationHandler {
        private final ActiveDescriptor<AnnotationHandler> descriptor;
        private AnnotationHandler handler;

        private LazyAnnotationHandler(ActiveDescriptor<AnnotationHandler> descriptor) {
            this.descriptor = descriptor;
        }

        private AnnotationHandler getHandler() {
            if (handler != null) return handler;

            handler = locator.getServiceHandle(descriptor).getService();
            return handler;
        }

        @Override
        public Class<? extends Annotation> getAnnotationType() {
            return getHandler().getAnnotationType();
        }

        @Override
        public HandlerProcessingResult processAnnotation(
                AnnotationInfo element) throws AnnotationProcessorException {
            return getHandler().processAnnotation(element);
        }

        @Override
        public Class<? extends Annotation>[] getTypeDependencies() {
            return getHandler().getTypeDependencies();
        }

    }
}

/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.annotation.handlers;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.Local;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 * This handler is responsible for handling the jakarta.ejb.EJB
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(EJB.class)
public class EJBHandler extends AbstractResourceHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(EJBHandler.class);

    public EJBHandler() {
    }

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        EJB ejbAn = (EJB)ainfo.getAnnotation();
        return processEJB(ainfo, rcContexts, ejbAn);
    }


    /**
     * Process a particular annotation whose type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @param ejbAn
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processEJB(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts, EJB ejbAn)
            throws AnnotationProcessorException {
        EjbReferenceDescriptor ejbRefs[] = null;

        String defaultLogicalName = null;
        Class defaultBeanInterface = null;
        InjectionTarget target = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field)ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            defaultLogicalName = targetClassName + "/" + f.getName();

            defaultBeanInterface = f.getType();

            target = new InjectionTarget();
            target.setClassName(targetClassName);
            target.setFieldName(f.getName());
            target.setMetadataSource(MetadataSource.ANNOTATION);

        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method)ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            validateInjectionMethod(m, ainfo);

            // Derive javabean property name.
            String propertyName = getInjectionMethodPropertyName(m, ainfo);

            defaultLogicalName = targetClassName + "/" + propertyName;

            defaultBeanInterface = m.getParameterTypes()[0];

            target = new InjectionTarget();
            target.setClassName(targetClassName);
            target.setMethodName(m.getName());
            target.setMetadataSource(MetadataSource.ANNOTATION);

        } else if( ElementType.TYPE.equals(ainfo.getElementType()) ) {
            // name() and beanInterface() are required for TYPE-level @EJB
            // if either of them not set, fail fast.  See issue 17284
            if (ejbAn.name().equals("") ||
                    ejbAn.beanInterface() == Object.class ) {
                Class c = (Class) ainfo.getAnnotatedElement();
                AnnotationProcessorException fatalException =
                    new AnnotationProcessorException(localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidtypelevelejb",
                    "Invalid TYPE-level @EJB with name() = [{0}] and " +
                    "beanInterface = [{1}] in {2}.  Each TYPE-level @EJB " +
                    "must specify both name() and beanInterface().",
                    new Object[] { ejbAn.name(), ejbAn.beanInterface(), c }),
                    ainfo);
                fatalException.setFatal(true);
                throw fatalException;
            }
        } else {
            // can't happen
            return getDefaultFailedResult();
        }

        // NOTE that default value is Object.class, not null
        Class beanInterface = (ejbAn.beanInterface() == Object.class) ?
            defaultBeanInterface : ejbAn.beanInterface();
        String logicalName = ejbAn.name().equals("") ?
            defaultLogicalName : ejbAn.name();

        ejbRefs = getEjbReferenceDescriptors(logicalName, rcContexts);
        for (EjbReferenceDescriptor ejbRef : ejbRefs) {
            if (target != null)
                ejbRef.addInjectionTarget(target);

            if (!ok(ejbRef.getName()))  // a new one
                ejbRef.setName(logicalName);

            // merge type information
            setEjbType(ejbRef, beanInterface);

            // merge description
            if (!ok(ejbRef.getDescription()) && ok(ejbAn.description()))
                ejbRef.setDescription(ejbAn.description());

            // merge lookup-name and mapped-name
            if (!ejbRef.hasLookupName() && ok(ejbAn.lookup()))
                ejbRef.setLookupName(ejbAn.lookup());
            if (!ok(ejbRef.getMappedName()) && ok(ejbAn.mappedName()))
                ejbRef.setMappedName(ejbAn.mappedName());

            // merge beanName/linkName
            if (!ok(ejbRef.getLinkName()) && ok(ejbAn.beanName()))
                ejbRef.setLinkName(ejbAn.beanName());
        }

        return getDefaultProcessedResult();
    }

    /**
     * Return EjbReferenceDescriptors with given name if exists or a new
     * one without name being set.
     * @param logicalName
     * @param rcContexts
     * @return an array of EjbReferenceDescriptor
     */
    private EjbReferenceDescriptor[] getEjbReferenceDescriptors(
            String logicalName, ResourceContainerContext[] rcContexts) {
        EjbReferenceDescriptor ejbRefs[] =
                new EjbReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            EjbReferenceDescriptor ejbRef =
                (EjbReferenceDescriptor)rcContexts[i].getEjbReference(logicalName);
            if (ejbRef == null) {
                ejbRef = new EjbReferenceDescriptor();
                rcContexts[i].addEjbReferenceDescriptor(ejbRef);
            }
            ejbRefs[i] = ejbRef;
        }

        return ejbRefs;
    }

    /**
     * Set the type information for the EJB, but only if it hasn't
     * already been set by the deployment descriptor.
     */
    private void setEjbType(EjbReferenceDescriptor ejbRef,
                                         Class beanInterface) {
        if (EJBHome.class.isAssignableFrom(beanInterface) ||
                EJBLocalHome.class.isAssignableFrom(beanInterface)) {
            setEjbHomeType(ejbRef, beanInterface);
        } else {
            setEjbIntfType(ejbRef, beanInterface);
        }
    }

    /**
     * Set the type information for the EJB starting with the EJB business
     * interface, but only if it hasn't already been set.
     */
    private void setEjbIntfType(EjbReferenceDescriptor ejbRef,
                                        Class beanInterface) {
        if (ejbRef.getEjbInterface() != null)
            return;

        // only set it if not already set by DD
        ejbRef.setEjbInterface(beanInterface.getName());

        if (beanInterface.getAnnotation(Local.class) != null) {
            ejbRef.setLocal(true);
        } else {
            // If beanInterface has @Remote annotation, setLocal(false);
            // If beanInterface has neither @Local nor @Remote,
            // assume remote for now. We can't know for sure until the
            // post-validation stage.  Even though local business will
            // probably be more common than remote business, defaulting
            // to remote business simplifies the post-application
            // validation logic considerably.  See
            // EjbBundleValidator.accept(EjbReferenceDescriptor)
            // for more details.
            ejbRef.setLocal(false);
        }
        ejbRef.setType(EjbSessionDescriptor.TYPE);
    }

    /**
     * Set the type information for the EJB starting with the EJB Home
     * interface, but only if it hasn't already been set.
     */
    private void setEjbHomeType(EjbReferenceDescriptor ejbRef,
                                        Class beanInterface) {

        if (ejbRef.getHomeClassName() != null)
            return;

        // default is Session bean
        String targetBeanType = EjbSessionDescriptor.TYPE;
        ejbRef.setHomeClassName(beanInterface.getName());

        try {
            // Set bean Interface as well so we have all
            // the info that would have been in an ejb-ref/
            // ejb-local-ref
            Method[] methods = beanInterface.getMethods();
            for (Method m : methods) {
                if (m.getName().equals("create")) {
                    ejbRef.setEjbInterface(m.getReturnType().getName());
                    break;
                }
            }
            // Use existence of findByPrimaryKey method on Home to
            // determine target bean type
            for (Method m : methods) {
                if (m.getName().equals("findByPrimaryKey")) {
                    targetBeanType = EjbEntityDescriptor.TYPE;
                    break;
                }
            }
        } catch(Exception e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                "component intf / ejb type annotation processing error", e);
            }
        }

        ejbRef.setLocal(EJBLocalHome.class.isAssignableFrom(beanInterface));
        ejbRef.setType(targetBeanType);
    }
}

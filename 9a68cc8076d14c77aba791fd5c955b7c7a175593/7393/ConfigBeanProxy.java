/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.reflect.Proxy;

import org.glassfish.hk2.api.Customize;
import org.glassfish.hk2.api.Customizer;

/**
 * Marker interface that signifies that the interface
 * is meant to be used as a strongly-typed proxy to
 * {@link Dom}.
 *
 * <p>
 * To obtain the Dom object, use {@link Dom#unwrap(ConfigBeanProxy)}.
 * This design allows the interfaces to be implemented by other code
 * outside DOM more easily.
 *
 * @author Kohsuke Kawaguchi
 * @see Dom#unwrap(ConfigBeanProxy)
 * @see DuckTyped
 * @see Element
 * @see Attribute
 */
@Customizer(ConfigBeanProxyCustomizer.class)
public interface ConfigBeanProxy {

    /**
     * Returns the parent element of this configuration element.
     *
     * It is possible to return a not null parent while the parent knows nothing of this
     * child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @return the parent configuration node.
     */
    @DuckTyped @Customize
    public ConfigBeanProxy getParent();

    /**
     * Returns the typed parent element of this configuration element.
     *
     * It is possible to return a not null parent while the parent knows nothing of this
     * child element. This could happen when the child element was removed
     * from the configuration tree, yet it's parent would not have been reset.
     *
     * @param type parent's type
     * @return the parent configuration node.
     */
    @DuckTyped @Customize
    public <T extends ConfigBeanProxy> T getParent(Class<T> type);

    /**
     * Creates a child element of this configuration element
     *
     * @param type the child element type
     * @return the newly created child instance
     * @throws TransactionFailure when called outside the boundaries of a transaction
     */
    @DuckTyped @Customize
    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure;


    /**
     * Performs a deep copy of this configuration element and returns it.
     * The parent of this configuration must be locked in a transaction and the newly created
     * child will be automatically enrolled in the parent's transaction.
     *
     * @param parent the writable copy of the parent
     * @return a deep copy of itself.
     * @throws TransactionFailure if the transaction cannot be completed.
     */
    @DuckTyped @Customize
    public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) throws TransactionFailure;

    public class Duck {

        public static ConfigBeanProxy getParent(ConfigBeanProxy self) {
            Dom dom = Dom.unwrap(self);
            if (dom.parent()!=null) {
                return dom.parent().createProxy();
            } else {
                return null;
            }
        }

        public static <T extends ConfigBeanProxy> T getParent(ConfigBeanProxy self, Class<T> c) {
             Dom dom = Dom.unwrap(self);
            if (dom.parent()!=null) {
                return dom.parent().createProxy(c);
            } else {
                return null;
            }

        }

        public static <T extends ConfigBeanProxy> T createChild(ConfigBeanProxy self, Class<T> c)
            throws TransactionFailure {

             try {
                 WriteableView bean = WriteableView.class.cast(Proxy.getInvocationHandler(Proxy.class.cast(self)));
                 return bean.allocateProxy(c);
             } catch (ClassCastException e) {
                 throw new TransactionFailure("Must use a locked parent config object for instantiating new config object", e);
             }

        }

        public static ConfigBeanProxy deepCopy(ConfigBeanProxy self, ConfigBeanProxy parent) throws TransactionFailure {
            ConfigBean configBean = (ConfigBean) Dom.unwrap(self);
            // ensure the parent is locked
            Transaction t = Transaction.getTransaction(parent);

            if (t==null) {
                throw new TransactionFailure("Must use a locked parent config object for copying new config object");
            }

            ConfigBean copy = configBean.copy(configBean.parent());
            return t.enroll(copy.createProxy());
        }

    }

}

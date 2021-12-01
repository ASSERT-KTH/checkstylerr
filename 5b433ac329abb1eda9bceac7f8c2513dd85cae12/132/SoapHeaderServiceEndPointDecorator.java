/*
 * Copyright (c) 2009-2015, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.messaging.service.decorator.cxf;

import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import gov.hhs.fha.nhinc.messaging.service.decorator.ServiceEndpointDecorator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author achidamb
 *
 *
 */
public class SoapHeaderServiceEndPointDecorator<T> extends ServiceEndpointDecorator<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SoapHeaderServiceEndPointDecorator.class);

    private String subscriptionId = null;

    private BindingProvider bindingProviderPort;

    public SoapHeaderServiceEndPointDecorator(ServiceEndpoint<T> decoratoredEndpoint, String subscriptionId) {
        super(decoratoredEndpoint);
        this.subscriptionId = subscriptionId;
        this.bindingProviderPort = (BindingProvider) decoratedEndpoint.getPort();
    }

    @Override
    public void configure() {
        super.configure();

        if (subscriptionId != null) {
            List<Header> headers = new ArrayList<Header>();
            Header SoapHeader;
            try {
                SoapHeader = new Header(new QName("http://www.hhs.gov/healthit/nhin", "SubscriptionId"),
                        subscriptionId, new JAXBDataBinding(String.class));
                headers.add(SoapHeader);
            } catch (JAXBException e) {
                LOG.error("Failed to set subscription id to header", e);
            }

            bindingProviderPort.getRequestContext().put(Header.HEADER_LIST, headers);
        }
    }
}

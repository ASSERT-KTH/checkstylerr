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
package gov.hhs.fha.nhinc.docsubmission.adapter.deferred.request.proxy;

import gov.hhs.fha.nhinc.adapterxdrrequestsecured.AdapterXDRRequestSecuredPortType;
import gov.hhs.fha.nhinc.aspect.AdapterDelegationEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterProvideAndRegisterDocumentSetSecuredRequestType;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.request.proxy.service.AdapterDocSubmissionDeferredRequestSecuredServicePortDescriptor;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionArgTransformerBuilder;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionBaseEventDescriptionBuilder;
import gov.hhs.fha.nhinc.messaging.client.CONNECTCXFClientFactory;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Visu Patlolla
 */
public class AdapterDocSubmissionDeferredRequestProxyWebServiceSecuredImpl implements
    AdapterDocSubmissionDeferredRequestProxy {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterDocSubmissionDeferredRequestProxyWebServiceSecuredImpl.class);
    private WebServiceProxyHelper oProxyHelper = null;

    public AdapterDocSubmissionDeferredRequestProxyWebServiceSecuredImpl() {
        oProxyHelper = createWebServiceProxyHelper();
    }

    protected WebServiceProxyHelper createWebServiceProxyHelper() {
        return new WebServiceProxyHelper();
    }

    protected CONNECTClient<AdapterXDRRequestSecuredPortType> getCONNECTClientSecured(
        ServicePortDescriptor<AdapterXDRRequestSecuredPortType> portDescriptor, String url, AssertionType assertion) {
        return CONNECTCXFClientFactory.getInstance().getCONNECTClientSecured(portDescriptor,
            url, assertion);
    }

    /**
     *
     * @param request
     * @param assertion
     * @return
     */
    @AdapterDelegationEvent(beforeBuilder = DocSubmissionBaseEventDescriptionBuilder.class,
    afterReturningBuilder = DocSubmissionArgTransformerBuilder.class,
    serviceType = "Document Submission Deferred Request",
    version = "")
    @Override
    public XDRAcknowledgementType provideAndRegisterDocumentSetBRequest(
        ProvideAndRegisterDocumentSetRequestType request, AssertionType assertion) {
        LOG.debug("Begin AdapterDocSubmissionDeferredRequestProxyWebServiceSecuredImpl.provideAndRegisterDocumentSetBRequest");
        XDRAcknowledgementType response = null;
        String serviceName = NhincConstants.ADAPTER_XDR_REQUEST_SECURED_SERVICE_NAME;

        try {
            LOG.debug("Before target system destination URL look up.");
            String destURL = oProxyHelper.getAdapterEndPointFromConnectionManager(serviceName);
            LOG.debug("After target system URL look up. URL for service: " + serviceName + " is: " + destURL);

            if (NullChecker.isNotNullish(destURL)) {
                AdapterProvideAndRegisterDocumentSetSecuredRequestType wsRequest = new AdapterProvideAndRegisterDocumentSetSecuredRequestType();
                wsRequest.setProvideAndRegisterDocumentSetRequest(request);
                wsRequest.setUrl(destURL);

                ServicePortDescriptor<AdapterXDRRequestSecuredPortType> portDescriptor = new AdapterDocSubmissionDeferredRequestSecuredServicePortDescriptor();
                CONNECTClient<AdapterXDRRequestSecuredPortType> client = getCONNECTClientSecured(portDescriptor,
                    destURL, assertion);
                client.enableMtom();
                response = (XDRAcknowledgementType) client.invokePort(AdapterXDRRequestSecuredPortType.class,
                    "provideAndRegisterDocumentSetBRequest", wsRequest);
            } else {
                LOG.error("Failed to call the web service (" + serviceName + ").  The URL is null.");
            }
        } catch (Exception ex) {
            LOG.error("Error: Failed to retrieve url for service: " + serviceName + " for local home community");
            LOG.error(ex.getMessage(), ex);
        }

        LOG.debug("End AdapterDocSubmissionDeferredRequestProxyWebServiceSecuredImpl.provideAndRegisterDocumentSetBRequest");
        return response;
    }
}

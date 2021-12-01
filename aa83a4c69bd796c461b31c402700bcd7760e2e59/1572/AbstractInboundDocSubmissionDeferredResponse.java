/*
 * Copyright (c) 2009-2018, United States Government, as represented by the Secretary of Health and Human Services.
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
package gov.hhs.fha.nhinc.docsubmission.inbound.deferred.response;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxy;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.docsubmission.audit.DSDeferredResponseAuditLogger;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import java.util.Properties;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

public abstract class AbstractInboundDocSubmissionDeferredResponse implements InboundDocSubmissionDeferredResponse {

    public abstract XDRAcknowledgementType processDocSubmissionResponse(RegistryResponseType body, AssertionType assertion);

    private DSDeferredResponseAuditLogger auditLogger = null;
    private AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory = null;

    public AbstractInboundDocSubmissionDeferredResponse(
        AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory, DSDeferredResponseAuditLogger auditLogger) {
        this.adapterFactory = adapterFactory;
        this.auditLogger = auditLogger;
    }

    @Override
    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(RegistryResponseType body,
        AssertionType assertion, Properties webContextProperties) {

        XDRAcknowledgementType response = processDocSubmissionResponse(body, assertion);

        auditResponse(body, response, assertion, webContextProperties);

        return response;
    }

    protected XDRAcknowledgementType sendToAdapter(RegistryResponseType body, AssertionType assertion) {
        AdapterDocSubmissionDeferredResponseProxy proxy = adapterFactory.getAdapterDocSubmissionDeferredResponseProxy();
        return proxy.provideAndRegisterDocumentSetBResponse(body, assertion);
    }

    protected void auditResponse(RegistryResponseType body, XDRAcknowledgementType response, AssertionType assertion,
        Properties webContextProperties) {
        auditLogger.auditResponseMessage(body, response, assertion, null, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
            NhincConstants.AUDIT_LOG_NHIN_INTERFACE, Boolean.FALSE, webContextProperties,
            NhincConstants.NHINC_XDR_RESPONSE_SERVICE_NAME);
    }

    protected DSDeferredResponseAuditLogger getAuditLogger() {
        return new DSDeferredResponseAuditLogger();
    }
}

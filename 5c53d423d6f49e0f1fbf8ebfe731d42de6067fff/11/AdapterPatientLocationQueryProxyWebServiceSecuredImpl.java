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
package gov.hhs.fha.nhinc.patientlocationquery.adapter.proxy;

//import gov.hhs.fha.nhinc.adapterxdssecured.AdapterXDSSecuredPortType;
import gov.hhs.fha.nhinc.adapterpatientlocationquerysecured.AdapterPatientLocationQuerySecuredPortType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterPatientLocationQueryRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.AdapterPatientLocationQueryResponseType;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClientFactory;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
//import gov.hhs.fha.nhinc.docdatasubmission.MessageGeneratorUtilsDocData;
import gov.hhs.fha.nhinc.patientlocationquery.adapter.descriptor.AdapterPatientLocationQuerySecuredServicePortDescriptor;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterPatientLocationQueryProxyWebServiceSecuredImpl implements AdapterPatientLocationQueryProxy {

    private static final Logger LOG = LoggerFactory
        .getLogger(AdapterPatientLocationQueryProxyWebServiceSecuredImpl.class);
    private WebServiceProxyHelper oProxyHelper = new WebServiceProxyHelper();

    @Override
    public AdapterPatientLocationQueryResponseType AdapterPatientLocationQueryResponse(
        AdapterPatientLocationQueryRequestType msg, AssertionType assertion) {
        AdapterPatientLocationQueryResponseType response = null;

        try {
            String url = oProxyHelper
                .getAdapterEndPointFromConnectionManager(NhincConstants.ADAPTER_PLQ_SECURED_SERVICE_NAME);
            if (NullChecker.isNotNullish(url)) {

                ServicePortDescriptor<AdapterPatientLocationQuerySecuredPortType> portDescriptor = new AdapterPatientLocationQuerySecuredServicePortDescriptor();

                CONNECTClient<AdapterPatientLocationQuerySecuredPortType> client = CONNECTClientFactory.getInstance()
                    .getCONNECTClientSecured(portDescriptor, url, assertion);
                response = (AdapterPatientLocationQueryResponseType) client
                    .invokePort(AdapterPatientLocationQuerySecuredPortType.class, "registerDocumentSetb", msg);
            } else {
                LOG.error("Failed to call the web service ({}).  The URL is null.",
                    NhincConstants.ADAPTER_PLQ_SECURED_SERVICE_NAME);
                throw new IllegalArgumentException("Failed to call the webservice. The service URL was null.");
            }
        } catch (Exception ex) {
            LOG.error("Error sending Patient Location Query Secured message: " + ex.getMessage(), ex);
            // response = MessageGeneratorUtilsDocData.getInstance().createRegistryErrorResponse();
            // response = "";
        }
        return response;
    }
}

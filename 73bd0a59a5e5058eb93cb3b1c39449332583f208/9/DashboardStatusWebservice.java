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
package gov.hhs.fha.nhinc.admingui.dashboard;

import static gov.hhs.fha.nhinc.util.CoreHelpUtils.getXMLGregorianCalendarFrom;

import gov.hhs.fha.nhinc.admingui.services.StatusEvent;
import gov.hhs.fha.nhinc.admingui.services.StatusService;
import gov.hhs.fha.nhinc.admingui.services.impl.StatusEventImpl;
import gov.hhs.fha.nhinc.adminguimanagement.AdminGUIManagementPortType;
import gov.hhs.fha.nhinc.common.adminguimanagement.AdminGUIRequestMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.DashboardStatusMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.EventLogMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.GetSearchFilterRequestMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.ListErrorLogRequestMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.LogEventSimpleResponseMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.LogEventType;
import gov.hhs.fha.nhinc.common.adminguimanagement.ViewErrorLogRequestMessageType;
import gov.hhs.fha.nhinc.event.model.EventCount;
import java.util.Date;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;


public class DashboardStatusWebservice implements AdminGUIManagementPortType{


    @Autowired
    StatusService service;

    private final StatusEvent eventService = new StatusEventImpl();

    @Override
    public DashboardStatusMessageType dashboardStatus(AdminGUIRequestMessageType request) {
        request.isIncludeEventMessages();
        DashboardStatusMessageType resp = new DashboardStatusMessageType();
        resp.setMemory(service.getMemory());
        resp.setOS(service.getOperatingSystem());
        resp.setServer(service.getApplicationServer());
        resp.setVersion(service.getJavaVersion());

        eventService.setCounts();

        for (Entry<String, EventCount> event : eventService.getEventCounts().entrySet())
        {
            EventLogMessageType eventType = new EventLogMessageType();
            eventType.setEvent(event.getKey());
            eventType.setInbound(event.getValue().getInbound());
            eventType.setOutbound(event.getValue().getOutbound());

            resp.getEvent().add(eventType);
        }

        return resp;
    }

    @Override
    public LogEventSimpleResponseMessageType getSearchFilter(GetSearchFilterRequestMessageType arg0) {
        LogEventSimpleResponseMessageType response = new LogEventSimpleResponseMessageType();
        response.getServiceList().add("service-type-1");
        response.getServiceList().add("service-type-2");
        response.getExceptionList().add("UserException");
        response.getExceptionList().add("SystemException");
        return response;
    }

    @Override
    public LogEventSimpleResponseMessageType listErrorLog(ListErrorLogRequestMessageType arg0) {
        LogEventSimpleResponseMessageType response = new LogEventSimpleResponseMessageType();
        response.getEventLogList().add(buildLogEventType(1, null, "CustomException"));
        response.getEventLogList().add(buildLogEventType(2, null, "MyCustomException"));
        return response;
    }

    @Override
    public LogEventSimpleResponseMessageType viewErrorLog(ViewErrorLogRequestMessageType arg0) {
        LogEventSimpleResponseMessageType response = new LogEventSimpleResponseMessageType();
        response.getEventLogList()
        .add(buildLogEventType(4,
                "{\"failedMethod\": \"respondingGatewayCrossGatewayQuery\", \"service_type\": \"Document Query\", \"exceptionClass\": \"class gov.hhs.fha.nhinc.event.error.ErrorEventException\", \"stackTrace\": [\"gov.hhs.fha.nhinc.docquery.adapter.proxy.AdapterDocQueryProxyWebServiceSecuredImpl.respondingGatewayCrossGatewayQuery(AdapterDocQueryProxyWebServiceSecuredImpl.java:104)\",\"gov.hhs.fha.nhinc.docquery.adapter.proxy.AdapterDocQueryProxyWebServiceSecuredImpl.respondingGatewayCrossGatewayQuery(AdapterDocQueryProxyWebServiceSecuredImpl.java:104)\"], \"failedClass\": \"class gov.hhs.fha.nhinc.docquery.adapter.proxy.AdapterDocQueryProxyWebServiceSecuredImpl\", \"exceptionMessage\": \"Unable to call Doc Query Adapter\"}",
            "CustomException"));
        return response;
    }

    private LogEventType buildLogEventType(long id, String detail, String classException) {
        LogEventType retObj = new LogEventType();
        retObj.setId(id);
        retObj.setEventTime(getXMLGregorianCalendarFrom(new Date()));
        retObj.setServiceType("Service-Type");
        retObj.setVersion("1.1");
        retObj.setDescription(detail);
        retObj.setExceptionType(classException);
        return retObj;
    }

}

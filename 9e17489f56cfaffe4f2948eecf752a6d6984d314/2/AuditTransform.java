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
package gov.hhs.fha.nhinc.audit.transform;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;
import gov.hhs.fha.nhinc.audit.AuditTransformConstants;
import gov.hhs.fha.nhinc.audit.AuditTransformDataBuilder;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformHelper;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.log4j.Logger;

/**
 *
 * @author achidamb
 * @param <T> request Object Type
 * @param <K> response Object Type
 */
public abstract class AuditTransform<T, K> {

    private static final Logger LOG = Logger.getLogger(AuditTransform.class);

    /**
     * Build AuditLog Request Message from Request
     * 
     * This abstract class follows Template design pattern. EventIdentification, ActivePartiicpant(HumanRequestor,Source,
     * Destination), AuditSourceIdentification are same across all the exchange services (PD,DQ,DR,AD,DS) and X12.
     * The ParticipantObjectIdentification is built using the requests and response types received from Inbound and 
     * Outbound for any specific service. Therefore an abstract method is defined here to build 
     * ParticipantObjectIdentification and this can be implemented by specific Core services AuditTransformer. In order 
     * to build ParticipantObjectIdentification the generic request of type <T> and generic response of type <K> are 
     * used here. These Generic types can be extended in the subclasses to their respective request and response types
     * objects.
     * 
     * @param request Request Object
     * @param assertion Assertion Object
     * @param target  Target Community
     * @param direction Inbound/Outbound
     * @param _interface Entity, Adapter and Nwhin
     * @param isRequesting Initiating/Responding Gateway
     * @param webContextProperties Properties hold destination Url and Remote IP
     * @param serviceName Service Name
     * @param instance
     * @return Audit Request
     * 
     */
    public final LogEventRequestType transformRequestToAuditMsg(T request, AssertionType assertion,
        NhinTargetSystemType target, String direction, String _interface, boolean isRequesting, Properties 
            webContextProperties, String serviceName, AuditTransformDataBuilder instance) {

        return createAuditMessage(request, assertion, target, direction, _interface, isRequesting, 
            webContextProperties, serviceName, instance);
    }
    
    /**
     * Build AuditLog Request Message from Response
     * 
     * This abstract class follows Template design pattern. EventIdentification, ActivePartiicpant(HumanRequestor,Source,
     * Destination), AuditSourceIdentification are same across all the exchange services (PD,DQ,DR,AD,DS) and X12.
     * The ParticipantObjectIdentification is built using the requests and response types received from Inbound and 
     * Outbound for any specific service. Therefore an abstract method is defined here to build 
     * ParticipantObjectIdentification and this can be implemented by specific Core services AuditTransformer. In order 
     * to build ParticipantObjectIdentification the generic request of type <T> and generic response of type <K> are 
     * used here. These Generic types can be extended in the subclasses to their respective request and response types
     * objects.
     * 
     * @param response Response Object
     * @param assertion Assertion Object
     * @param target  Target Community
     * @param direction Inbound/Outbound
     * @param _interface Entity, Adapter and Nwhin
     * @param isRequesting Initiating/Responding Gateway
     * @param webContextProperties Properties hold destination Url and Remote IP
     * @param serviceName Service Name
     * @param instance
     * @return Audit Request
     * 
     */
    public final LogEventRequestType transformResponseToAuditMsg(K response, AssertionType assertion,
        NhinTargetSystemType target, String direction, String _interface, boolean isRequesting, Properties 
            webContextProperties, String serviceName, AuditTransformDataBuilder instance) {

        return createAuditMessage(response, assertion, target, direction, _interface, isRequesting, 
            webContextProperties, serviceName, instance);
    }

    /**
     * ParticipantObjectIdentifiaction is specific to each service and the implementation can be carried out in 
     * specific service core transformer.
     * @param msg
     * @param assertion
     * @param auditMsg
     * @return
     */
    protected abstract AuditMessageType getParticipantObjectIdentification(Object msg, AssertionType assertion, 
        AuditMessageType auditMsg);
    

    /**
     *
     * @return
     */
    protected AuditSourceIdentificationType getAuditSourceIdentificationType() {
        String hcid = HomeCommunityMap.getLocalHomeCommunityId();
        String homeCommunityName = HomeCommunityMap.getHomeCommunityName(hcid);
        return createAuditSourceIdentification(hcid, homeCommunityName);
    }

    /**
     *
     * @param oUserInfo
     * @param isRequesting
     * @return
     */
    protected AuditMessageType.ActiveParticipant getActiveParticipant(UserType oUserInfo, Boolean isRequesting) {
        // Create Active Participant Section
        // create a method to call the AuditDataTransformHelper - one expectation
        AuditMessageType.ActiveParticipant participant = createActiveParticipantFromUser(
            oUserInfo, isRequesting);
        if (oUserInfo.getRoleCoded() != null) {
            participant.getRoleIDCode().add(AuditDataTransformHelper.createCodeValueType(oUserInfo.getRoleCoded().
                getCode(), "",
                oUserInfo.getRoleCoded().getCodeSystemName(), oUserInfo.getRoleCoded().getDisplayName()));
        }
        return participant;
    }

    /**
     *
     * @param serviceName
     * @param isRequesting
     * @param instance
     * @return
     */
    protected EventIdentificationType createEventIdentification(String serviceName, boolean isRequesting, 
        AuditTransformDataBuilder instance) {
        CodedValueType eventID = createCodeValueType(instance.getServiceEvenIdCode(), null,
            instance.getServiceEventCodeSystem(), isRequesting ? instance.getServiceEventDisplayRequestor()
            : instance.getServiceEventDisplayResponder());

        EventIdentificationType oEventIdentificationType = getEventIdentificationType(eventID, isRequesting,
            serviceName, instance);
        oEventIdentificationType.getEventTypeCode().add(AuditDataTransformHelper.
            createCodeValueType(instance.getServiceEventTypeCode(), null,
                instance.getServiceEventTypeCodeSystem(), instance.getServiceEventTypeCodeDisplayName()));
        return oEventIdentificationType;
    }

    /**
     * Create the ActiveParticipant for an audit log record.
     *
     * @param userInfo
     * @param userIsReq
     * @return
     */
    protected AuditMessageType.ActiveParticipant createActiveParticipantFromUser(UserType userInfo,
        Boolean userIsReq) {
        AuditMessageType.ActiveParticipant participant = new AuditMessageType.ActiveParticipant();

        // Set the User Id
        String userId = null;
        if (userInfo != null && userInfo.getUserName() != null && userInfo.getUserName().length() > 0) {
            userId = userInfo.getUserName();
            participant.setUserID(userId);
        }

        // If specified, set the User Name
        String userName = null;
        if (userInfo != null && userInfo.getPersonName() != null) {
            if (userInfo.getPersonName().getGivenName() != null && userInfo.getPersonName().getGivenName().
                length() > 0) {
                userName = userInfo.getPersonName().getGivenName();
            }

            if (userInfo.getPersonName().getFamilyName() != null
                && userInfo.getPersonName().getFamilyName().length() > 0) {
                if (userName != null) {
                    userName += (" " + userInfo.getPersonName().getFamilyName());
                } else {
                    userName = userInfo.getPersonName().getFamilyName();
                }
            }
            if (userName != null) {
                participant.setUserName(userName);
            }
        }

        participant.setUserIsRequestor(userIsReq);
        return participant;
    }

    /**
     *
     * @param eventID
     * @param isRequesting
     * @param serviceName
     * @param instance
     * @return
     */
    protected EventIdentificationType getEventIdentificationType(CodedValueType eventID, boolean isRequesting,
        String serviceName, AuditTransformDataBuilder instance) {

        return createEventIdentification(
            isRequesting ? instance.getServiceEventActionCodeRequestor() : instance.getServiceEventActionCodeResponder(),
            AuditTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventID);
    }

    /**
     *
     * @param isRequesting
     * @param webContextProperties
     * @param serviceName
     * @return
     */
    protected AuditMessageType.ActiveParticipant getActiveParticipantSource(boolean isRequesting, Properties webContextProperties, String serviceName) {
        AuditMessageType.ActiveParticipant participant = new AuditMessageType.ActiveParticipant();
        participant.setUserID(AuditTransformConstants.ACTIVE_PARTICPANT_USER_ID_SOURCE);
        participant.setAlternativeUserID(ManagementFactory.getRuntimeMXBean().getName());
        String hostAddress = null;
        hostAddress = isRequesting ? getLocalHostAddress() : getRemoteHostAddress(webContextProperties);
        participant.setNetworkAccessPointID(hostAddress);
        participant.setNetworkAccessPointTypeCode(getNetworkAccessPointTypeCode(hostAddress));

        participant.getRoleIDCode().add(AuditDataTransformHelper.createCodeValueType(AuditTransformConstants.ACTIVE_PARTICIPANT_ROLE_CODE_SOURCE, null,
            AuditTransformConstants.ACTIVE_PARTICIPANT_CODE_SYSTEM_NAME, AuditTransformConstants.ACTIVE_PARTICPANT_ROLE_CODE_SOURCE_DISPLAY_NAME));
        participant.setUserIsRequestor(isRequesting);
        return participant;
    }

    /**
     *
     * @param target
     * @param isRequesting
     * @param webContextProprties
     * @param serviceName
     * @return
     */
    protected AuditMessageType.ActiveParticipant getActiveParticipantDestination(NhinTargetSystemType target, boolean isRequesting, Properties webContextProprties, String serviceName) {
        String strUrl = null;
        String strHost = null;
        boolean setDefaultValue = true;

        AuditMessageType.ActiveParticipant participant = new AuditMessageType.ActiveParticipant();

        strUrl = isRequesting ? getWebServiceUrlFromRemoteObject(target, serviceName)
            : getWebServiceRequestURL(webContextProprties);
        if (strUrl != null) {
            try {
                URL url = new URL(strUrl);
                participant.setUserID(strUrl);
                strHost = url.getHost();
                participant.setNetworkAccessPointID(strHost);
                participant.setNetworkAccessPointTypeCode(getNetworkAccessPointTypeCode(strHost));
                setDefaultValue = false;
            } catch (MalformedURLException ex) {
                LOG.error(ex);
                //if the url is null or not a valid url
                //for now set the user id to anonymouns
                participant.setUserID(AuditTransformConstants.ACTIVE_PARTICPANT_USER_ID_SOURCE);
                //for now hardcode the value to localhost, need to find out if this needs to be set
                participant.setNetworkAccessPointTypeCode(AuditTransformConstants.NETWORK_ACCESSOR_PT_TYPE_CODE_NAME);
                participant.setNetworkAccessPointID(AuditTransformConstants.ACTIVE_PARTICPANT_UNKNOWN_IP_ADDRESS);
            }
        }

        participant.setUserIsRequestor(Boolean.FALSE);
        participant.getRoleIDCode().add(AuditDataTransformHelper.createCodeValueType(AuditTransformConstants.
            ACTIVE_PARTICIPANT_ROLE_CODE_DEST, null,
            AuditTransformConstants.ACTIVE_PARTICIPANT_CODE_SYSTEM_NAME, AuditTransformConstants.
                ACTIVE_PARTICPANT_ROLE_CODE_DESTINATION_DISPLAY_NAME));
        return participant;
    }

    /**
     *
     * @param webContextProperties
     * @return
     */
    protected String getRemoteHostAddress(Properties webContextProperties) {
        if (webContextProperties != null && !webContextProperties.isEmpty() && webContextProperties.getProperty(
            NhincConstants.REMOTE_HOST_ADDRESS) != null) {
            return webContextProperties.getProperty(NhincConstants.REMOTE_HOST_ADDRESS);
        }
        return AuditTransformConstants.ACTIVE_PARTICPANT_UNKNOWN_IP_ADDRESS;
    }

    /**
     *
     * @param hostAddress
     * @return
     */
    protected Short getNetworkAccessPointTypeCode(String hostAddress) {
        if (InetAddressValidator.getInstance().isValid(hostAddress)) {
            return AuditTransformConstants.NETWORK_ACCESSOR_PT_TYPE_CODE_IP;
        }
        return AuditTransformConstants.NETWORK_ACCESSOR_PT_TYPE_CODE_NAME;

    }

    /**
     *
     * @param webContextProeprties
     * @return
     */
    protected String getWebServiceRequestURL(Properties webContextProeprties) {
        if (webContextProeprties != null && !webContextProeprties.isEmpty() && webContextProeprties.getProperty(
            NhincConstants.WEB_SERVICE_REQUEST_URL) != null) {
            return webContextProeprties.getProperty(NhincConstants.WEB_SERVICE_REQUEST_URL);
        }
        return AuditTransformConstants.ACTIVE_PARTICPANT_USER_ID_SOURCE;
    }

    /**
     *
     * @param target
     * @param serviceName
     * @return
     */
    protected String getWebServiceUrlFromRemoteObject(NhinTargetSystemType target, String serviceName) {
        if (target != null && serviceName != null) {
            try {
                return getConnectionManagerCache().getEndpointURLFromNhinTarget(target, serviceName);
            } catch (ConnectionManagerException ex) {
                LOG.error(ex);
            }
        }
        return AuditTransformConstants.ACTIVE_PARTICPANT_USER_ID_SOURCE;
    }

    /**
     *
     * @return
     */
    protected ConnectionManagerCache getConnectionManagerCache() {
        return ConnectionManagerCache.getInstance();
    }

    /**
     *
     * @return
     */
    protected String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LOG.error("Error while returning Local Host Address: " + ex.getLocalizedMessage(), ex);
            return AuditTransformConstants.ACTIVE_PARTICPANT_UNKNOWN_IP_ADDRESS;
        }
    }

    /**
     *
     * @return
     */
    protected String createUUID() {

        return UUID.randomUUID().toString();

    }

    /**
     *
     * @param assertion
     * @param target
     * @param isRequesting
     * @return
     */
    protected String getMessageCommunityId(AssertionType assertion, NhinTargetSystemType target, boolean isRequesting) {
        String communityId = null;
        if (isRequesting) {
            communityId = HomeCommunityMap.getCommunityIdFromTargetSystem(target);
        } else {
            communityId = HomeCommunityMap.getHomeCommunityIdFromAssertion(assertion);
        }
        return HomeCommunityMap.formatHomeCommunityId(communityId);
    }

    /**
     *
     * @param participantObjectCode
     * @param participantObjectCodeRole
     * @param participantObjectIdCode
     * @param particpantObjectIdCodeSystem
     * @param participantObjectIdDisplayName
     * @return
     */
    protected ParticipantObjectIdentificationType createParticipantObjectIdentification(short participantObjectCode,
        short participantObjectCodeRole, String participantObjectIdCode, String particpantObjectIdCodeSystem,
        String participantObjectIdDisplayName) {
        ParticipantObjectIdentificationType participantObject = new ParticipantObjectIdentificationType();

        // Set the Participation Object Typecode
        participantObject.setParticipantObjectTypeCode(participantObjectCode);

        // Set the Participation Object Typecode Role
        participantObject.setParticipantObjectTypeCodeRole(participantObjectCodeRole);

        // Set the Participation Object Id Type code
        CodedValueType partObjIdTypeCode = new CodedValueType();
        partObjIdTypeCode.setCode(participantObjectIdCode);
        partObjIdTypeCode.setCodeSystemName(particpantObjectIdCodeSystem);
        partObjIdTypeCode.setDisplayName(participantObjectIdDisplayName);
        participantObject.setParticipantObjectIDTypeCode(partObjIdTypeCode);
        return participantObject;
    }

    /**
     * Create the <code>EventIdentificationType</code> for an audit log record.
     *
     * @param actionCode
     * @param eventOutcome
     * @param eventId
     * @return <code>EventIdentificationType</code>
     */
    private static EventIdentificationType createEventIdentification(String actionCode, Integer eventOutcome,
        CodedValueType eventId) {
        EventIdentificationType eventIdentification = new EventIdentificationType();

        // Set the Event Action Code
        eventIdentification.setEventActionCode(actionCode);

        // Set the Event Action Time
        try {
            GregorianCalendar today = new java.util.GregorianCalendar(TimeZone.getTimeZone("GMT"));
            DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
            XMLGregorianCalendar calendar = factory.newXMLGregorianCalendar(
                today.get(java.util.GregorianCalendar.YEAR), today.get(java.util.GregorianCalendar.MONTH) + 1,
                today.get(java.util.GregorianCalendar.DAY_OF_MONTH),
                today.get(java.util.GregorianCalendar.HOUR_OF_DAY), today.get(java.util.GregorianCalendar.MINUTE),
                today.get(java.util.GregorianCalendar.SECOND), today.get(java.util.GregorianCalendar.MILLISECOND),
                0);
            eventIdentification.setEventDateTime(calendar);
        } catch (DatatypeConfigurationException e) {
            LOG.error("DatatypeConfigurationException when creating XMLGregorian Date");
            LOG.error(" message: " + e.getMessage(), e);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("ArrayIndexOutOfBoundsException when creating XMLGregorian Date");
            LOG.error(" message: " + e.getMessage(), e);
        }
        // Set the Event Outcome Indicator
        BigInteger eventOutcomeBig = BigInteger.ZERO;
        if (eventOutcome != null) {
            eventOutcomeBig = new BigInteger(eventOutcome.toString());
        }
        eventIdentification.setEventOutcomeIndicator(eventOutcomeBig);

        // Set the Event Id
        eventIdentification.setEventID(eventId);

        return eventIdentification;
    }

    /**
     * Create the <code>CodedValueType</code> for an audit log record.
     *
     * @param code
     * @param codeSys
     * @param codeSysName
     * @param dispName
     * @return <code>CodedValueType</code>
     */
    private static CodedValueType createCodeValueType(String code, String codeSys, String codeSysName, String dispName) {
        CodedValueType codeValueType = new CodedValueType();

        // Set the Code
        if (NullChecker.isNotNullish(code)) {
            codeValueType.setCode(code);
        }

        // Set the Codesystem
        if (NullChecker.isNotNullish(codeSys)) {
            codeValueType.setCodeSystem(codeSys);
        }

        // Set the Codesystem Name
        if (NullChecker.isNotNullish(codeSysName)) {
            codeValueType.setCodeSystemName(codeSysName);
        }

        // Set the Display Name
        if (NullChecker.isNotNullish(dispName)) {
            codeValueType.setDisplayName(dispName);
        }

        return codeValueType;
    }

    /**
     * Create an <code>AuditSourceIdentificationType</code> based on the community id and name for an audit log record.
     *
     * @param communityId
     * @param communityName
     * @return <code>AuditSourceIdentificationType</code>
     */
    private static AuditSourceIdentificationType createAuditSourceIdentification(String communityId, String communityName) {
        AuditSourceIdentificationType auditSrcId = new AuditSourceIdentificationType();

        // Set the Audit Source Id (community id)
        if (communityId != null) {
            if (communityId.startsWith("urn:oid:")) {
                auditSrcId.setAuditSourceID(communityId.substring(8));
            } else {
                auditSrcId.setAuditSourceID(communityId);
            }
        }

        // If specified, set the Audit Enterprise Site Id (community name)
        if (communityName != null) {
            auditSrcId.setAuditEnterpriseSiteID(communityName);
        }

        return auditSrcId;
    }
    
    private LogEventRequestType createAuditMessage(Object msg, AssertionType assertion,
        NhinTargetSystemType target, String direction, String _interface, boolean isRequesting, Properties 
            webContextProperties, String serviceName, AuditTransformDataBuilder instance) {
        LogEventRequestType result = new LogEventRequestType();

        AuditMessageType auditMsg = new AuditMessageType();
        //****************************Construct Event Identification**************************

        auditMsg.setEventIdentification(createEventIdentification(serviceName, isRequesting, instance));

        //*********************************Construct Active Participant************************
        //Active Participant for human requester only required for requesting gateway
        if (isRequesting) {
            AuditMessageType.ActiveParticipant participantHumanFactor = getActiveParticipant(assertion.getUserInfo(), isRequesting);
            auditMsg.getActiveParticipant().add(participantHumanFactor);
        }
        AuditMessageType.ActiveParticipant participantSource = getActiveParticipantSource(isRequesting,
            webContextProperties, serviceName);
        AuditMessageType.ActiveParticipant participantDestination = getActiveParticipantDestination(target,
            isRequesting, webContextProperties, serviceName);
        auditMsg.getActiveParticipant().add(participantSource);
        auditMsg.getActiveParticipant().add(participantDestination);

        //******************************Constuct Participation Object Identification*************
        // Assign ParticipationObjectIdentification
        auditMsg = getParticipantObjectIdentification(msg, assertion, auditMsg);

        //Create the AuditSourceIdentifierType object
        String communityId = getMessageCommunityId(assertion, target, isRequesting);
        AuditSourceIdentificationType auditSource = getAuditSourceIdentificationType();

        //******************************Constuct Audit Source Identification**********************
        auditMsg.getAuditSourceIdentification().add(auditSource);

        //Set the all the required data
        result.setAuditMessage(auditMsg);
        result.setDirection(direction);
        result.setInterface(_interface);
        //set the target community identifier
        result.setCommunityId(communityId);

        LOG.trace("End AuditDataTransform transformMsgToAuditMsg() ----");
        return result;
    }
}

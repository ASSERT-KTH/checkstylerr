/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.extvar.ExternalVariableConf;
import org.apache.ode.bpel.engine.extvar.ExternalVariableManager;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.MessageExchange.AckType;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.iapi.Scheduler.JobProcessorException;
import org.apache.ode.bpel.intercept.FailMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.bpel.memdao.ProcessInstanceDaoImpl;
import org.apache.ode.bpel.rapi.ConstantsModel;
import org.apache.ode.bpel.rapi.FaultInfo;
import org.apache.ode.bpel.rapi.OdeRTInstance;
import org.apache.ode.bpel.rapi.OdeRuntime;
import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.bpel.rapi.Serializer;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.bpel.runtime.InvalidInstanceException;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.Properties;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Entry point into the runtime of a BPEL process.
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ODEProcess {
    static final Log __log = LogFactory.getLog(ODEProcess.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private volatile Map<PartnerLinkModel, PartnerLinkPartnerRoleImpl> _partnerRoles;

    private volatile Map<PartnerLinkModel, PartnerLinkMyRoleImpl> _myRoles;

    /**
     * Mapping from {"Service Name" (QNAME) / port} to a myrole.
     */
    private volatile Map<Endpoint, PartnerLinkMyRoleImpl> _endpointToMyRoleMap;

    // Backup hashmaps to keep initial endpoints handy after dehydration
    private Map<Endpoint, EndpointReference> _myEprs = new HashMap<Endpoint, EndpointReference>();

    private Map<Endpoint, EndpointReference> _partnerEprs = new HashMap<Endpoint, EndpointReference>();

    private Map<Endpoint, PartnerRoleChannel> _partnerChannels = new HashMap<Endpoint, PartnerRoleChannel>();

    /**
     * Mapping from a potentially shared endpoint to its EPR
     */
    private SharedEndpoints _sharedEps;

    final QName _pid;

    private volatile ProcessModel _processModel;

    // Has the process already been hydrated before?
    private boolean _hydratedOnce = false;

    /**
     * Last time the process was used.
     */
    private volatile long _lastUsed;

    volatile OdeRuntime _runtime;

    public DebuggerSupport _debugger;

    final ProcessConf _pconf;

    /**
     * {@link MessageExchangeInterceptor}s registered for this process.
     */
    private final List<MessageExchangeInterceptor> _mexInterceptors = new ArrayList<MessageExchangeInterceptor>();

    /**
     * Latch-like thing to control hydration/dehydration.
     */
    HydrationLatch _hydrationLatch;

    protected Contexts _contexts;

    final BpelInstanceWorkerCache _instanceWorkerCache = new BpelInstanceWorkerCache(this);

    private final Set<InvocationStyle> _invocationStyles;

    private final BpelDAOConnectionFactoryImpl _inMemDao;

    final BpelServerImpl _server;

    private MyRoleMessageExchangeCache _myRoleMexCache;

    /**
     * Deploy-time configuraton for external variables.
     */
    private ExternalVariableConf _extVarConf;

    private ExternalVariableManager _evm;

    ODEProcess(BpelServerImpl server, ProcessConf conf, BpelEventListener debugger, MyRoleMessageExchangeCache mexCache) {
        _server = server;
        _pid = conf.getProcessId();
        _pconf = conf;
        _hydrationLatch = new HydrationLatch();
        _contexts = server._contexts;
        _inMemDao = new BpelDAOConnectionFactoryImpl(_contexts.txManager);
        _myRoleMexCache = mexCache;

        // TODO : do this on a per-partnerlink basis, support transacted styles.
        HashSet<InvocationStyle> istyles = new HashSet<InvocationStyle>();
        istyles.add(InvocationStyle.UNRELIABLE);

        if (!conf.isTransient()) istyles.add(InvocationStyle.RELIABLE);
        else istyles.add(InvocationStyle.TRANSACTED);
        _invocationStyles = Collections.unmodifiableSet(istyles);
    }

    /**
     * Retrives the base URI to use for local resource resolution.
     *
     * @return URI - instance representing the absolute file path to the physical location of the process definition folder.
     */
    public URI getBaseResourceURI() {
        return this._pconf.getBaseURI();
    }

    /**
     * Intiialize the external variable configuration/engine manager. This is called from hydration logic, so it
     * is possible to change the external variable configuration at runtime.
     *
     */
    void initExternalVariables() {
        List<Element> conf = _pconf.getExtensionElement(ExternalVariableConf.EXTVARCONF_ELEMENT);
        _extVarConf = new ExternalVariableConf(conf);
        _evm = new ExternalVariableManager(_pid, _extVarConf, _contexts.externalVariableEngines);
    }

    public OdeConfigProperties getProperties() {
        return _server.getConfigProperties();
    }

    public String toString() {
        return "ODEProcess[" + _pid + "]";
    }

    public ExternalVariableManager getEVM() {
        return _evm;
    }

    public void recoverActivity(ProcessInstanceDAO instanceDAO, final String channel, final long activityId, final String action,
                                final FaultInfo fault) {
        if (__log.isDebugEnabled())
            __log.debug("Recovering activity in process " + instanceDAO.getInstanceId() + " with action " + action);

        _hydrationLatch.latch(1);
        try {
            markused();
            BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceDAO.getInstanceId());
            final OdeRTInstance rti = _runtime.newInstance(getState(iworker, instanceDAO));
            final BpelRuntimeContextImpl processInstance = new BpelRuntimeContextImpl(iworker, instanceDAO, rti);
            try {
                iworker.execInCurrentThread(new Callable<Void>() {
                    public Void call() throws Exception {
                        processInstance.recoverActivity(channel, activityId, action, fault);
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new BpelEngineException(e);
            }
        } finally {
            _hydrationLatch.release(1);
        }
    }

    /**
     * Entry point for message exchanges aimed at the my role.
     *
     * @param mexdao Message Exchange DAO.
     */
    void invokeProcess(final MessageExchangeDAO mexdao) {
        InvocationStyle istyle = mexdao.getInvocationStyle();
        ConstantsModel constants = null;

        _hydrationLatch.latch(1);
        try {
            // The following check is mostly for sanity purposes. MexImpls should prevent this from
            // happening.
            PartnerLinkMyRoleImpl target = getMyRoleForService(mexdao.getCallee());
            constants = target._process.getProcessModel().getConstantsModel();
            Status oldstatus = mexdao.getStatus();
            if (target == null) {
                String errmsg = __msgs.msgMyRoleRoutingFailure(mexdao.getMessageExchangeId());
                __log.error(errmsg);
                MexDaoUtil.setFailed(mexdao, MessageExchange.FailureType.UNKNOWN_ENDPOINT, errmsg);
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            Operation op = target._plinkDef.getMyRoleOperation(mexdao.getOperation());
            if (op == null) {
                String errmsg = __msgs.msgMyRoleRoutingFailure(mexdao.getMessageExchangeId());
                __log.error(errmsg);
                MexDaoUtil.setFailed(mexdao, MessageExchange.FailureType.UNKNOWN_OPERATION, errmsg);
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            mexdao.setPattern((op.getOutput() == null) ? MessageExchangePattern.REQUEST_ONLY
                    : MessageExchangePattern.REQUEST_RESPONSE);
            if (!processInterceptors(mexdao, InterceptorInvoker.__onProcessInvoked)) {
                __log.debug("Aborting processing of mex " + mexdao.getMessageExchangeId() + " due to interceptors.");
                onMyRoleMexAck(mexdao, oldstatus);
                return;
            }

            // "Acknowledge" any one-way invokes
            if (op.getOutput() == null) {
                if(__log.isDebugEnabled()){
                    __log.debug("Acknowledge one-way invokes....");
                }
                mexdao.setStatus(Status.ACK);
                mexdao.setAckType(AckType.ONEWAY);
                onMyRoleMexAck(mexdao, oldstatus);
            }

            mexdao.setProcess(getProcessDAO());

            markused();
            CorrelationStatus cstatus = target.invokeMyRole(mexdao);
            if (cstatus == null) {
                ; // do nothing
            } else if (cstatus == CorrelationStatus.CREATE_INSTANCE) {
                doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                    public Void call() {
                        executeCreateInstance(mexdao);
                        return null;
                    }
                });

            } else if (cstatus == CorrelationStatus.MATCHED) {
                // This should not occur for in-memory processes, since they are technically not allowed to
                // have any <receive>/<pick> elements that are not start activities.
                if (isInMemory())
                    __log.warn("In-memory process " + _pid + " is participating in a non-createinstance exchange!");

                // We don't like to do the work in the same TX that did the matching, since this creates fertile
                // conditions for deadlock in the correlation tables. However if invocation style is transacted,
                // we need to do the work right then and there.
                if (mexdao.getInstance().getState() == ProcessState.STATE_TERMINATED) {
                    throw new InvalidInstanceException("Trying to invoke terminated process instance",
                            InvalidInstanceException.TERMINATED_CAUSE_CODE);
                }

                if (op.getOutput() != null) {
                    // If the invoked operation is request-response type it's not good to store the request in
                    // database until suspended instance is resumed. It's good to throw and exception in this case.
                    // Then the client will know that this process is suspended.
                    if (mexdao.getInstance().getState() == ProcessState.STATE_SUSPENDED) {
                        throw new InvalidInstanceException("Trying to invoke suspended instance",
                                InvalidInstanceException.SUSPENDED_CAUSE_CODE);
                    }
                }

                if (istyle == InvocationStyle.TRANSACTED) {
                    doInstanceWork(mexdao.getInstance().getInstanceId(), new Callable<Void>() {
                        public Void call() {
                            executeContinueInstanceMyRoleRequestReceived(mexdao);
                            return null;
                        }
                    });
                } else if (istyle == InvocationStyle.P2P_TRANSACTED) /* transact p2p invoke in the same thread */ {
                    executeContinueInstanceMyRoleRequestReceived(mexdao);
                } else /* non-transacted style */ {
                    WorkEvent we = new WorkEvent();
                    we.setType(WorkEvent.Type.MYROLE_INVOKE);
                    we.setIID(mexdao.getInstance().getInstanceId());
                    we.setMexId(mexdao.getMessageExchangeId());
                    // Could be different to this pid when routing to an older version
                    we.setProcessId(mexdao.getInstance().getProcess().getProcessId());

                    scheduleWorkEvent(we, null);
                }
            } else if (cstatus == CorrelationStatus.QUEUED) {
                ; // do nothing
            }
        } catch (InvalidProcessException ipe) {
            QName faultQName = null;
            if (constants != null) {
                Document document = DOMUtils.newDocument();
                Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
                Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
                faultElement.appendChild(faultDetail);
                switch (ipe.getCauseCode()) {
                    case InvalidProcessException.DUPLICATE_CAUSE_CODE:
                        faultQName = constants.getDuplicateInstance();
                        faultDetail.setTextContent("Found a duplicate instance with the same message key");
                        break;
                    case InvalidProcessException.RETIRED_CAUSE_CODE:
                        faultQName = constants.getRetiredProcess();
                        faultDetail.setTextContent("The process you're trying to instantiate has been retired");
                        break;
                    case InvalidProcessException.DEFAULT_CAUSE_CODE:
                    default:
                        faultQName = constants.getUnknownFault();
                        break;
                }
                MexDaoUtil.setFaulted(mexdao, faultQName, faultElement);
            }
        } catch (InvalidInstanceException iie) {
            QName faultQname = null;
            if (constants != null) {
                Document document = DOMUtils.newDocument();
                Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
                Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
                faultElement.appendChild(faultDetail);
                switch (iie.getCauseCode()) {
                    case InvalidInstanceException.TERMINATED_CAUSE_CODE:
                        faultQname = new QName("ode", "TerminatedInstance");
                        faultDetail.setTextContent(iie.getMessage());
                        break;
                    case InvalidInstanceException.SUSPENDED_CAUSE_CODE:
                        faultQname = new QName("ode", "SuspendedInstance");
                        faultDetail.setTextContent(iie.getMessage());
                        break;
                    default:
                        faultQname = constants.getUnknownFault();
                        break;
                }
                MexDaoUtil.setFaulted(mexdao, faultQname, faultElement);
            }
        } catch (BpelEngineException bee) {
            QName faultQname = null;
            Document document = DOMUtils.newDocument();
            Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
            Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
            faultElement.appendChild(faultDetail);
            faultQname = new QName("ode", "BpelEngineException");
            faultDetail.setTextContent(bee.getMessage());

            MexDaoUtil.setFaulted(mexdao, faultQname, faultElement);
        } finally {
            _hydrationLatch.release(1);

            // If we did not get an ACK during this method, then mark this MEX as needing an ASYNC wake-up
            if (mexdao.getStatus() != Status.ACK) mexdao.setStatus(Status.ASYNC);

            assert mexdao.getStatus() == Status.ACK || mexdao.getStatus() == Status.ASYNC;
        }

    }

    void executeCreateInstance(MessageExchangeDAO mexdao) {
        assert _hydrationLatch.isLatched(1);

        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();
        BpelRuntimeContextImpl rtictx = new BpelRuntimeContextImpl(
                worker, mexdao.getInstance(), _runtime.newInstance(getState(worker, mexdao.getInstance())));
        rtictx.executeCreateInstance(mexdao);
    }

    void executeContinueInstanceMyRoleRequestReceived(MessageExchangeDAO mexdao) {
        assert _hydrationLatch.isLatched(1);

        assert mexdao != null;
        assert mexdao.getInstance() != null;

        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
        BpelRuntimeContextImpl instance = new BpelRuntimeContextImpl(worker, mexdao.getInstance(), rti);
        int amp = mexdao.getChannel().indexOf('&');
        String groupId = mexdao.getChannel().substring(0, amp);
        int idx = Integer.valueOf(mexdao.getChannel().substring(amp + 1));
        instance.injectMyRoleMessageExchange(groupId, idx, mexdao);
        instance.execute();
    }

    void executeContinueInstanceResume(ProcessInstanceDAO instanceDao, int retryCount) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, instanceDao));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
        brc.setRetryCount(retryCount);
        brc.execute();

    }

    void executeContinueInstanceTimerReceived(ProcessInstanceDAO instanceDao, String timerChannel) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, instanceDao));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
        if (brc.injectTimerEvent(timerChannel)) brc.execute();

    }

    private void executeContinueInstanceMatcherEvent(ProcessInstanceDAO instanceDao, String correlatorId,
                                                     CorrelationKey correlationKey) {

        if (__log.isDebugEnabled()) {
            __log.debug("MatcherEvent handling: correlatorId=" + correlatorId + ", ckey=" + correlationKey);
        }

        CorrelatorDAO correlator = instanceDao.getProcess().getCorrelator(correlatorId);

        // Find the route first, this is a SELECT FOR UPDATE on the "selector" row,
        // So we want to acquire the lock before we do anthing else.
        MessageRouteDAO mroute = correlator.findRoute(correlationKey);
        if (mroute == null) {
            // Ok, this means that a message arrived before we did, so nothing to do.
            __log.debug("MatcherEvent handling: nothing to do, route no longer in DB");
            return;
        }

        // Now see if there is a message that matches this selector.
        MessageExchangeDAO mexdao = correlator.dequeueMessage(correlationKey);
        if (mexdao != null) {
            __log.debug("MatcherEvent handling: found matching message in DB (i.e. message arrived before <receive>)");

            // We have a match, so we can get rid of the routing entries.
            correlator.removeRoutes(mroute.getGroupId(), instanceDao);
            mexdao.setInstance(instanceDao);

            // Found message matching one of our selectors.
            if (__log.isDebugEnabled()) {
                __log.debug("SELECT: " + mroute.getGroupId() + ": matched to MESSAGE " + mexdao + " on CKEY " + correlationKey);
            }

            BpelInstanceWorker worker = _instanceWorkerCache.get(instanceDao.getInstanceId());
            assert worker.isWorkerThread();

            OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
            BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, instanceDao, rti);
            brc.injectMyRoleMessageExchange(mroute.getGroupId(), mroute.getIndex(), mexdao);
            brc.execute();

            mexdao.release(true);
        } else {
            __log.debug("MatcherEvent handling: nothing to do, no matching message in DB");

        }
    }

    void executeContinueInstancePartnerRoleResponseReceived(MessageExchangeDAO mexdao) {
        assert _hydrationLatch.isLatched(1);
        ProcessInstanceDAO instanceDao = mexdao.getInstance();
        if (instanceDao == null)
            throw new BpelEngineException("InternalError: No instance for partner mex " + mexdao);

        BpelInstanceWorker worker = _instanceWorkerCache.get(mexdao.getInstance().getInstanceId());
        assert worker.isWorkerThread();

        OdeRTInstance rti = _runtime.newInstance(getState(worker, mexdao.getInstance()));
        BpelRuntimeContextImpl brc = new BpelRuntimeContextImpl(worker, mexdao.getInstance(), rti);
        // Canceling invoke check
        String jobId = mexdao.getProperty("invokeCheckJobId");
        if (jobId != null)
            _contexts.scheduler.cancelJob(jobId);

        brc.injectPartnerResponse(mexdao.getMessageExchangeId(), mexdao.getChannel());
        brc.execute();
    }

    void enqueueInstanceTransaction(Long instanceId, final Runnable runnable) {
        if (instanceId == null)
            throw new NullPointerException("instanceId was null!");

        BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceId);
        iworker.enqueue(_server.new TransactedRunnable(runnable));
    }

    private <T> T doInstanceWork(Long instanceId, final Callable<T> callable) {
        try {
            BpelInstanceWorker iworker = _instanceWorkerCache.get(instanceId);
            return iworker.execInCurrentThread(new ProcessCallable<T>(callable));

        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        }
    }

    private PartnerLinkMyRoleImpl getMyRoleForService(QName serviceName) {
        assert _hydrationLatch.isLatched(1);

        for (Map.Entry<Endpoint, PartnerLinkMyRoleImpl> e : _endpointToMyRoleMap.entrySet()) {
            if (e.getKey().serviceName.equals(serviceName))
                return e.getValue();
        }
        return null;
    }

    /**
     * Process the message-exchange interceptors.
     *
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    boolean processInterceptors(MessageExchangeDAO mexdao, InterceptorInvoker invoker) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_contexts.dao.getConnection(), mexdao, getProcessDAO(), _pconf);

        try {
            for (MessageExchangeInterceptor interceptor : _mexInterceptors)
                invoker.invoke(interceptor, ictx);

            for (MessageExchangeInterceptor interceptor : _server._contexts.globalIntereceptors)
                invoker.invoke(interceptor, ictx);
        } catch (FailMessageExchangeException e) {
            MexDaoUtil.setFailed(mexdao, FailureType.ABORTED, e.getMessage());
            return false;
        } catch (FaultMessageExchangeException e) {
            MexDaoUtil.setFaulted(mexdao, e.getFaultName(), e.getFaultData());
            return false;
        }

        return true;
    }

    /**
     * Handle a work event; this method is called from the scheduler thread and should be very quick, i.e. any serious work needs to
     * be handed off to a separate thread.
     *
     * @throws JobProcessorException
     */
    void handleWorkEvent(final JobInfo jobInfo) throws JobProcessorException {
        assert !_contexts.isTransacted() : "work events must be received outside of a transaction";

        markused();

        final WorkEvent we = new WorkEvent(jobInfo.jobDetail);
        if (__log.isDebugEnabled()) {
            __log.debug(ObjectPrinter.stringifyMethodEnter("handleWorkEvent", new Object[]{"jobInfo", jobInfo}));
        }

        enqueueInstanceTransaction(we.getIID(), new Runnable() {
            public void run() {
                _contexts.scheduler.jobCompleted(jobInfo.jobName);
                execInstanceEvent(we);
            }
        });

    }

    /**
     * Enqueue a transaction for execution by the engine.
     *
     * @param tx the transaction
     */
    <T> Future<T> enqueueTransaction(final Callable<T> tx) {
        // We have to wrap our transaction to make sure that we are hydrated when the transaction runs.
        return _server.enqueueTransaction(new ProcessCallable<T>(tx));
    }

    private Object getState(BpelInstanceWorker worker, ProcessInstanceDAO instanceDAO) {
        ExecutionQueueImpl state = (ExecutionQueueImpl) worker.getCachedState(instanceDAO.getExecutionStateCounter());
        if (state != null) return state;

        if (isInMemory()) {
            ProcessInstanceDaoImpl inmem = (ProcessInstanceDaoImpl) instanceDAO;
            if (inmem.getSoup() != null) {
                state = (ExecutionQueueImpl) inmem.getSoup();
            }
        } else {
            byte[] daoState = instanceDAO.getExecutionState();
            if (daoState != null) {
                state = new ExecutionQueueImpl(getClass().getClassLoader());
                state.setReplacementMap((ReplacementMap) _runtime.getReplacementMap(instanceDAO.getProcess().getProcessId()));

                ByteArrayInputStream iis = new ByteArrayInputStream(daoState);
                try {
                    state.read(iis);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return state;
    }

    private void execInstanceEvent(WorkEvent we) {
        BpelInstanceWorker worker = _instanceWorkerCache.get(we.getIID());
        assert worker.isWorkerThread();

        ProcessInstanceDAO instanceDAO = getProcessDAO().getInstance(we.getIID());
        MessageExchangeDAO mexDao = we.getMexId() == null ? null : loadMexDao(we.getMexId());

        if (instanceDAO == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("handleWorkEvent: no ProcessInstance found with iid " + we.getIID() + "; ignoring.");
            }
            return;
        }

        if (__log.isDebugEnabled()) {
            __log.debug("handleWorkEvent: " + we.getType() + " event for process instance " + we.getIID());
        }

        switch (we.getType()) {
            case MYROLE_INVOKE:
                executeContinueInstanceMyRoleRequestReceived(mexDao);
                break;
            case TIMER:
                executeContinueInstanceTimerReceived(instanceDAO, we.getChannel());
                break;
            case RESUME:
                executeContinueInstanceResume(instanceDAO, we.getRetryCount());
                break;
            case PARTNER_RESPONSE:
                executeContinueInstancePartnerRoleResponseReceived(mexDao);
                break;
            case MATCHER:
                executeContinueInstanceMatcherEvent(instanceDAO, we.getCorrelatorId(), we.getCorrelationKey());
                break;
        }
    }

    MessageExchangeDAO loadMexDao(String mexId) {
        return isInMemory() ? _inMemDao.getConnection().getMessageExchange(mexId) : _contexts.dao.getConnection()
                .getMessageExchange(mexId);
    }

    private void setRoles(ProcessModel oprocess) {
        _partnerRoles = new HashMap<PartnerLinkModel, PartnerLinkPartnerRoleImpl>();
        _myRoles = new HashMap<PartnerLinkModel, PartnerLinkMyRoleImpl>();
        _endpointToMyRoleMap = new HashMap<Endpoint, PartnerLinkMyRoleImpl>();

        // Create myRole endpoint name mapping (from deployment descriptor)
        HashMap<PartnerLinkModel, Endpoint> myRoleEndpoints = new HashMap<PartnerLinkModel, Endpoint>();
        for (Map.Entry<String, Endpoint> provide : _pconf.getProvideEndpoints().entrySet()) {
            PartnerLinkModel plink = oprocess.getPartnerLink(provide.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid + "; reference to unknown partner link "
                        + provide.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            myRoleEndpoints.put(plink, provide.getValue());
        }

        // Create partnerRole initial value mapping
        for (Map.Entry<String, Endpoint> invoke : _pconf.getInvokeEndpoints().entrySet()) {
            PartnerLinkModel plink = oprocess.getPartnerLink(invoke.getKey());
            if (plink == null) {
                String errmsg = "Error in deployment descriptor for process " + _pid + "; reference to unknown partner link "
                        + invoke.getKey();
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }
            __log.debug("Processing <invoke> element for process " + _pid + ": partnerlink " + invoke.getKey() + " --> "
                    + invoke.getValue());
        }

        for (PartnerLinkModel pl : oprocess.getAllPartnerLinks()) {
            if (pl.hasMyRole()) {
                Endpoint endpoint = myRoleEndpoints.get(pl);
                if (endpoint == null)
                    throw new IllegalArgumentException("No service name for myRole plink " + pl.getName());
                PartnerLinkMyRoleImpl myRole = new PartnerLinkMyRoleImpl(this, pl, endpoint);
                _myRoles.put(pl, myRole);
                _endpointToMyRoleMap.put(endpoint, myRole);
            }

            if (pl.hasPartnerRole()) {
                Endpoint endpoint = _pconf.getInvokeEndpoints().get(pl.getName());
                if (endpoint == null && pl.isInitializePartnerRoleSet())
                    throw new IllegalArgumentException(pl.getName() + " must be bound to an endpoint in deloy.xml");
                PartnerLinkPartnerRoleImpl partnerRole = new PartnerLinkPartnerRoleImpl(this, pl, endpoint);
                _partnerRoles.put(pl, partnerRole);
            }
        }
    }

    ProcessDAO getProcessDAO() {
        return isInMemory() ? _inMemDao.getConnection().getProcess(_pid) : _contexts.dao.getConnection().getProcess(_pid);
    }

    static String genCorrelatorId(PartnerLinkModel plink, String opName) {
        return plink.getId() + "." + opName;
    }

    /**
     * Get all the services that are implemented by this process.
     *
     * @return list of qualified names corresponding to the myroles.
     */
    public Set<Endpoint> getServiceNames() {
        Set<Endpoint> endpoints = new HashSet<Endpoint>();
        for (Endpoint provide : _pconf.getProvideEndpoints().values()) {
            endpoints.add(provide);
        }
        return endpoints;
    }

    void activate(Contexts contexts) {
        _contexts = contexts;
        _debugger = new DebuggerSupport(this);

        __log.debug("Activating " + _pid);
        // Activate all the my-role endpoints.
        for (Map.Entry<String, Endpoint> entry : _pconf.getProvideEndpoints().entrySet()) {
            Endpoint endpoint = entry.getValue();
            EndpointReference initialEPR = null;
            if (isShareable(endpoint)) {
                // Check if the EPR already exists for the given endpoint
                initialEPR = _sharedEps.getEndpointReference(endpoint);
                if (initialEPR == null) {
                    // Create an EPR by physically activating the endpoint
                    initialEPR = _contexts.bindingContext.activateMyRoleEndpoint(_pid, entry.getValue());
                    _sharedEps.addEndpoint(endpoint, initialEPR);
                    __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
                }
                // Increment the reference count on the endpoint
                _sharedEps.incrementReferenceCount(endpoint);
            } else {
                // Create an EPR by physically activating the endpoint
                initialEPR = _contexts.bindingContext.activateMyRoleEndpoint(_pid, entry.getValue());
                __log.debug("Activated " + _pid + " myrole " + entry.getKey() + ": EPR is " + initialEPR);
            }
            _myEprs.put(endpoint, initialEPR);
        }
        __log.debug("Activated " + _pid);

        markused();
    }

    void deactivate() {
        // Deactivate all the my-role endpoints.
        for (Endpoint endpoint : _myEprs.keySet()) {
            // Deactivate the EPR only if there are no more references
            // to this endpoint from any (active) BPEL process.
            if (isShareable(endpoint)) {
                __log.debug("deactivating shared endpoint " + endpoint);
                if (!_sharedEps.decrementReferenceCount(endpoint)) {
                    _contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
                    _sharedEps.removeEndpoint(endpoint);
                }
            } else {
                __log.debug("deactivating non-shared endpoint " + endpoint);
                _contexts.bindingContext.deactivateMyRoleEndpoint(endpoint);
            }
        }
        // TODO Deactivate all the partner-role channels
    }

    private boolean isShareable(Endpoint endpoint) {
        if (!_pconf.isSharedService(endpoint.serviceName)) return false;

//    	PartnerLinkMyRoleImpl partnerLink = _endpointToMyRoleMap.get(endpoint);
//        return partnerLink != null && partnerLink.isOneWayOnly();
        return false;
    }

    public EndpointReference getInitialPartnerRoleEPR(PartnerLinkModel link) {
        _hydrationLatch.latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole.getInitialEPR();
        } finally {
            _hydrationLatch.release(1);
        }
    }

    Endpoint getInitialPartnerRoleEndpoint(PartnerLinkModel link) {
        _hydrationLatch.latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(link);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + link);
            return prole._initialPartner;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    EndpointReference getInitialMyRoleEPR(PartnerLinkModel link) {
        _hydrationLatch.latch(1);
        try {
            PartnerLinkMyRoleImpl myRole = _myRoles.get(link);
            if (myRole == null) throw new IllegalStateException("Unknown partner link " + link);
            return myRole.getInitialEPR();
        } finally {
            _hydrationLatch.release(1);
        }
    }

    QName getPID() {
        return _pid;
    }

    QName getProcessType() {
        return _pconf.getType();
    }

    PartnerRoleChannel getPartnerRoleChannel(PartnerLinkModel partnerLink) {
        _hydrationLatch.latch(1);
        try {
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(partnerLink);
            if (prole == null)
                throw new IllegalStateException("Unknown partner link " + partnerLink);
            return prole._channel;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao) {
        saveEvent(event, instanceDao, null);
    }

    public void saveEvent(ProcessInstanceEvent event, ProcessInstanceDAO instanceDao, List<String> scopeNames) {
        markused();
        if (_pconf.isEventEnabled(scopeNames, event.getType())) {
            // notify the listeners
            _server.fireEvent(event);
            if (instanceDao != null)
                instanceDao.insertBpelEvent(event);
            else
                __log.debug("Couldn't find instance to save event, no event generated!");
        }
    }


    /**
     * Ask the process to dehydrate.
     */
    void dehydrate() {
        _hydrationLatch.latch(0);
        try {
            // We don't actually need to do anything, the latch will run the doDehydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(0);
        }
    }

    void hydrate() {
        _hydrationLatch.latch(1);
        try {
            // We don't actually need to do anything, the latch will run the doHydrate method
            // when necessary..
        } finally {
            _hydrationLatch.release(1);
        }
    }

    ProcessModel getProcessModel() {
        _hydrationLatch.latch(1);
        try {
            return _processModel;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    private MyRoleMessageExchangeImpl newMyRoleMex(InvocationStyle istyle, String mexId, QName target,
                                                   PartnerLinkModel mplink, Operation operation) {
        MyRoleMessageExchangeImpl mex;
        switch (istyle) {
            case RELIABLE:
                mex = new ReliableMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
                break;
            case TRANSACTED:
                mex = new TransactedMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
                break;
            case UNRELIABLE:
                mex = new UnreliableMyRoleMessageExchangeImpl(this, mexId, mplink, operation, target);
                break;
            default:
                throw new AssertionError("Unexpected invocation style: " + istyle);
        }

        _myRoleMexCache.put(mex);
        return mex;
    }

    /**
     * Lookup a {@link MyRoleMessageExchangeImpl} object in the cache, re-creating it if not found.
     *
     * @param mexdao DB representation of the mex.
     * @return client representation
     */
    MyRoleMessageExchangeImpl lookupMyRoleMex(MessageExchangeDAO mexdao) {
        return _myRoleMexCache.get(mexdao, this); // this will re-create if necessary
    }

    /**
     * Create (or recreate) a {@link MyRoleMessageExchangeImpl} object from data in the db. This method is used by the
     * {@link MyRoleMessageExchangeCache} to re-create objects when they are not found in the cache.
     *
     * @param mexdao
     * @return
     */
    MyRoleMessageExchangeImpl recreateMyRoleMex(MessageExchangeDAO mexdao) {
        InvocationStyle istyle = mexdao.getInvocationStyle();

        _hydrationLatch.latch(1);
        try {
            PartnerLinkModel plink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
            if (plink == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced unknown pLinkModelId " + mexdao.getPartnerLinkModelId());
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            Operation op = plink.getMyRoleOperation(mexdao.getOperation());
            if (op == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced unknown operation " + mexdao.getOperation());
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            PartnerLinkMyRoleImpl myRole = _myRoles.get(plink);
            if (myRole == null) {
                String errmsg = __msgs.msgDbConsistencyError("MexDao #" + mexdao.getMessageExchangeId()
                        + " referenced non-existant myrole");
                __log.error(errmsg);
                throw new BpelEngineException(errmsg);
            }

            MyRoleMessageExchangeImpl mex = newMyRoleMex(istyle, mexdao.getMessageExchangeId(), myRole._endpoint.serviceName,
                    plink, op);
            mex.load(mexdao);
            return mex;
        } finally {
            _hydrationLatch.release(1);
        }
    }

    PartnerRoleMessageExchangeImpl createPartnerRoleMex(MessageExchangeDAO mexdao) {

        _hydrationLatch.latch(1);
        try {
            PartnerLinkModel plink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
            PartnerLinkPartnerRoleImpl prole = _partnerRoles.get(plink);
            return prole.createPartnerRoleMex(mexdao);
        } finally {
            _hydrationLatch.release(1);
        }

    }

    Set<InvocationStyle> getSupportedInvocationStyle(QName serviceId) {
        return _invocationStyles;
    }

    /**
     * Find the partner-link-my-role that corresponds to the given service name.
     *
     * @param serviceName name of service
     * @return corresponding {@link PartnerLinkMyRoleImpl}
     */
    private PartnerLinkMyRoleImpl getPartnerLinkForService(QName serviceName) {
        assert _hydrationLatch.isLatched(1);

        PartnerLinkMyRoleImpl target = null;
        for (Endpoint endpoint : _endpointToMyRoleMap.keySet())
            if (endpoint.serviceName.equals(serviceName))
                target = _endpointToMyRoleMap.get(endpoint);

        return target;

    }

    public boolean isInMemory() {
        return _pconf.isTransient();
    }

    public long getLastUsed() {
        return _lastUsed;
    }

    /**
     * Get a hint as to whether this process is hydrated. Note this is only a hint, since things could change.
     */
    public boolean hintIsHydrated() {
        return _processModel != null;
    }

    /**
     * Keep track of the time the process was last used.
     */
    private final void markused() {
        _lastUsed = System.currentTimeMillis();
    }

    /**
     * If necessary, create an object in the data store to represent the process. We'll re-use an existing object if it already
     * exists and matches the GUID.
     */
    private void bounceProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        deleteProcessDAO(conn, pid, version, mprocess);
        createProcessDAO(conn, pid, version, mprocess);
    }

    private void deleteProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        __log.debug("Creating process DAO for " + pid + " (guid=" + mprocess.getGuid() + ")");
        try {
            ProcessDAO old = conn.getProcess(pid);
            if (old != null) {
                __log.debug("Found ProcessDAO for " + pid + " with GUID " + old.getGuid());
                if (mprocess.getGuid() == null) {
                    // No guid, old version assume its good
                } else {
                    if (old.getGuid().equals(mprocess.getGuid())) {
                        // Guids match, no need to create
                    } else {
                        // GUIDS dont match, delete and create new
                        String errmsg = "ProcessDAO GUID " + old.getGuid() + " does not match " + mprocess.getGuid() + "; replacing.";
                        __log.debug(errmsg);
                        old.delete();
                    }
                }
            }
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("DbError", dce);
            throw new BpelEngineException("DbError", dce);
        }
    }

    private void createProcessDAO(BpelDAOConnection conn, final QName pid, final long version, final ProcessModel mprocess) {
        __log.debug("Creating process DAO for " + pid + " (guid=" + mprocess.getGuid() + ")");
        try {
            boolean create = true;
            ProcessDAO old = conn.getProcess(pid);
            if (old != null) {
                __log.debug("Found ProcessDAO for " + pid + " with GUID " + old.getGuid());
                if (mprocess.getGuid() == null) {
                    // No guid, old version assume its good
                    create = false;
                } else {
                    if (old.getGuid().equals(mprocess.getGuid())) {
                        // Guids match, no need to create
                        create = false;
                    } else {
                        // GUIDS dont match, delete and create new
                        String errmsg = "ProcessDAO GUID " + old.getGuid() + " does not match " + mprocess.getGuid() + "; replacing.";
                        __log.debug(errmsg);
                    }
                }
            }

            if (create) {
                ProcessDAO newDao = conn.createProcess(pid, mprocess.getQName(), mprocess.getGuid(), (int) version);
                for (String correlator : mprocess.getCorrelators()) {
                    newDao.addCorrelator(correlator);
                }
            }
        } catch (BpelEngineException ex) {
            throw ex;
        } catch (Exception dce) {
            __log.error("DbError", dce);
            throw new BpelEngineException("DbError", dce);
        }
    }

    MessageExchangeDAO createMessageExchange(String mexId, final char dir) {
        if (isInMemory()) {
            return _inMemDao.getConnection().createMessageExchange(mexId, dir);
        } else {
            return _contexts.dao.getConnection().createMessageExchange(mexId, dir);
        }
    }

    MessageExchangeDAO getInMemMexDAO(String mexId) {
        return _inMemDao.getConnection().getMessageExchange(mexId);
    }

    public void releaseMessageExchange(final String mexId) {
        if (isInMemory()) {
            _inMemDao.getConnection().releaseMessageExchange(mexId);
        } else {
            if (_contexts.isTransacted()) {
                _contexts.dao.getConnection().releaseMessageExchange(mexId);
            } else {
                // ATT-MRIOU; what's the right way without creating its own transaction for releasing my role mex?
                try {
                    _contexts.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            _contexts.dao.getConnection().releaseMessageExchange(mexId);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Schedule process-level work. This method defers to the server to do the scheduling and wraps the {@link Runnable} in a
     * try-finally block that ensures that the process is hydrated.
     *
     * @param runnable
     */
    void scheduleRunnable(final Runnable runnable) {
        if (__log.isDebugEnabled()) __log.debug("schedulingRunnable for process " + _pid + ": " + runnable);

        _server.scheduleRunnable(new ProcessRunnable(runnable));
    }

    void enqueueRunnable(BpelInstanceWorker worker) {
        if (__log.isDebugEnabled()) __log.debug("enqueuRunnable for process " + _pid + ": " + worker);

        _server.enqueueRunnable(new ProcessRunnable(worker));
    }

    MyRoleMessageExchange createNewMyRoleMex(final InvocationStyle istyle, final QName targetService, final String operation) {
        final String mexId = new GUID().toString();
        _hydrationLatch.latch(1);
        try {
            final PartnerLinkMyRoleImpl target = getPartnerLinkForService(targetService);
            if (target == null)
                throw new BpelEngineException("NoSuchService: " + targetService);
            final Operation op = target._plinkDef.getMyRoleOperation(operation);
            if (op == null)
                throw new BpelEngineException("NoSuchOperation: " + operation);

            return newMyRoleMex(istyle, mexId, target._endpoint.serviceName, target._plinkDef, op);
        } finally {
            _hydrationLatch.release(1);
        }
    }

    void onMyRoleMexAck(MessageExchangeDAO mexdao, Status old) {
        if (mexdao.getPipedMessageExchangeId() != null) /* p2p */ {
            ODEProcess caller = _server.getBpelProcess(mexdao.getPipedPID());
            // process no longer deployed....
            if (caller == null) return;

            MessageExchangeDAO pmex = caller.loadMexDao(mexdao.getPipedMessageExchangeId());
            // Mex no longer there.... odd..
            if (pmex == null) return;

            // Need to copy the response and state from myrolemex --> partnerrolemex
            boolean compat = !(caller.isInMemory() ^ isInMemory());
            if (compat) {
                // both processes are in-mem or both are persisted, can share the message
                pmex.setResponse(mexdao.getResponse());
            } else /* one process in-mem, other persisted */ {
                if(mexdao.getAckType() != AckType.ONEWAY){
                    MessageDAO presponse = pmex.getConnection().createMessage(mexdao.getResponse().getType());
                    presponse.setData(mexdao.getResponse().getData());
                    presponse.setHeader(mexdao.getResponse().getHeader());
                    pmex.setResponse(presponse);
                }else{
                    pmex.setResponse(null);
                }
            }
            pmex.setFault(mexdao.getFault());
            pmex.setStatus(mexdao.getStatus());
            pmex.setAckType(mexdao.getAckType());
            pmex.setFailureType(mexdao.getFailureType());

            if (old == Status.ASYNC) caller.p2pWakeup(pmex);
        } else /* not p2p */ {
            // Do an Async wakeup if we are in the ASYNC state. If we're not, we'll pick up the ACK when we unwind
            // the stack.
           if(__log.isDebugEnabled()){
               __log.debug("ODEProcess#onMyRoleMexAck not p2p block.");
           }
            if (old == Status.ASYNC) {
                if(__log.isDebugEnabled()){
                    __log.debug("ODEProcess#onMyRoleMexAck not p2p block, old status is async.");
                }
                MyRoleMessageExchangeImpl mymex = _myRoleMexCache.get(mexdao, this);
                mymex.onAsyncAck(mexdao);
                try {
                    _contexts.mexContext.onMyRoleMessageExchangeStateChanged(mymex);
                } catch (Throwable t) {
                    __log.error("Integration layer threw an unexepcted exception.", t);
                }
            }
        }
    }

    /**
     * Read an {@link org.apache.ode.bpel.rtrep.v2.OProcess} representation from a stream.
     *
     * @param is input stream
     * @return deserialized process representation
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private ProcessModel deserializeCompiledProcess(InputStream is) throws IOException, ClassNotFoundException {
        ProcessModel compiledProcess;
        Serializer ofh = new Serializer(is);
        compiledProcess = (ProcessModel) ofh.readPModel();
        return compiledProcess;
    }


    class ProcessRunnable implements Runnable {
        Runnable _work;

        ProcessRunnable(Runnable work) {
            _work = work;
        }

        public void run() {
            _hydrationLatch.latch(1);
            try {
                _work.run();
            } finally {
                _hydrationLatch.release(1);
            }
        }
    }

    class ProcessCallable<T> implements Callable<T> {
        Callable<T> _work;

        ProcessCallable(Callable<T> work) {
            _work = work;
        }

        public T call() throws Exception {
            _hydrationLatch.latch(1);
            try {
                return _work.call();
            } finally {
                _hydrationLatch.release(1);
            }

        }

    }

    class HydrationLatch extends NStateLatch {

        HydrationLatch() {
            super(new Runnable[2]);
            _transitions[0] = new Runnable() {
                public void run() {
                    doDehydrate();
                }
            };
            _transitions[1] = new Runnable() {
                public void run() {
                    doHydrate();
                }
            };
        }

        private void doDehydrate() {
            _processModel = null;
            _partnerRoles = null;
            _myRoles = null;
            _endpointToMyRoleMap = null;
        }

        private void doHydrate() {
            markused();
            try {
                InputStream inputStream = _pconf.getCBPInputStream();
                try {
                    _processModel = deserializeCompiledProcess(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                String errmsg = "Error reloading compiled process " + _pconf.getProcessId() + "; the file appears to be corrupted.";
                __log.error(errmsg);
                throw new BpelEngineException(errmsg, e);
            }
            _runtime = buildRuntime(_processModel.getModelVersion());
            _runtime.init(_pconf, _processModel);

            setRoles(_processModel);
            initExternalVariables();

            if (!_hydratedOnce) {
                for (PartnerLinkPartnerRoleImpl prole : _partnerRoles.values()) {
                    if (prole._initialPartner != null) {
                        PartnerRoleChannel channel = _contexts.bindingContext.createPartnerRoleChannel(_pid,
                                prole._plinkDef.getPartnerRolePortType(), prole._initialPartner);
                        prole._channel = channel;
                        _partnerChannels.put(prole._initialPartner, prole._channel);
                        EndpointReference epr = channel.getInitialEndpointReference();
                        if (epr != null) {
                            prole._initialEPR = epr;
                            _partnerEprs.put(prole._initialPartner, epr);
                        }
                        __log.debug("Activated " + _pid + " partnerrole " + prole.getPartnerLinkName() + ": EPR is "
                                + prole._initialEPR);
                    }
                }
                _hydratedOnce = true;
            }

            for (PartnerLinkMyRoleImpl myrole : _myRoles.values()) {
                myrole._initialEPR = _myEprs.get(myrole._endpoint);
            }

            for (PartnerLinkPartnerRoleImpl prole : _partnerRoles.values()) {
                prole._channel = _partnerChannels.get(prole._initialPartner);
                if (_partnerEprs.get(prole._initialPartner) != null) {
                    prole._initialEPR = _partnerEprs.get(prole._initialPartner);
                }
            }

            if (isInMemory()) {
                bounceProcessDAO(_inMemDao.getConnection(), _pid, _pconf.getVersion(), _processModel);
            } else if (_contexts.isTransacted()) {
                // If we have a transaction, we do this in the current transaction.
                bounceProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
            } else {
                // If we do not have a transaction we need to create one.
                try {
                    _contexts.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            deleteProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
                            return null;
                        }
                    });
                    _contexts.execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            createProcessDAO(_contexts.dao.getConnection(), _pid, _pconf.getVersion(), _processModel);
                            return null;
                        }
                    });
                } catch (Exception ex) {
                    String errmsg = "DbError";
                    __log.error(errmsg, ex);
                    throw new BpelEngineException(errmsg, ex);
                }
            }
        }

    }

    public String scheduleWorkEvent(WorkEvent we, Date timeToFire) {
        // if (isInMemory())
        // throw new InvalidProcessException("In-mem process execution resulted in event scheduling.");

        return _contexts.scheduler.schedulePersistedJob(we.getDetail(), timeToFire);
    }

    void invokePartner(MessageExchangeDAO mexdao) {
        PartnerLinkModel oplink = _processModel.getPartnerLink(mexdao.getPartnerLinkModelId());
        PartnerLinkPartnerRoleImpl partnerRole = _partnerRoles.get(oplink);
        Endpoint partnerEndpoint = getInitialPartnerRoleEndpoint(oplink);
        List<ODEProcess> p2pProcesses = null;
        if (partnerEndpoint != null)
            p2pProcesses = _server.route(partnerEndpoint.serviceName, new DbBackedMessageImpl(mexdao.getRequest()));

        Operation operation = oplink.getPartnerRoleOperation(mexdao.getOperation());

        if (!processInterceptors(mexdao, InterceptorInvoker.__onPartnerInvoked)) {
            __log.debug("Partner invocation intercepted.");
            return;
        }

        mexdao.setStatus(Status.REQ);
        try {
            if (p2pProcesses != null && p2pProcesses.size() != 0) {
                /* P2P (process-to-process) invocation, special logic */
                // First, make a copy of the original request message
                MessageDAO request = mexdao.getRequest();
                // Then, iterate over each subscribing process
                for (ODEProcess p2pProcess : p2pProcesses) {
                    // Clone the request message for this subscriber
                    MessageDAO clone = mexdao.getConnection().createMessage(request.getType());
                    clone.setData((Element) request.getData().cloneNode(true));
                    clone.setHeader((Element) request.getHeader().cloneNode(true));
                    // Set the request on the MEX to the clone
                    mexdao.setRequest(clone);
                    // Send the cloned message to the subscribing process
                    invokeP2P(p2pProcess, partnerEndpoint.serviceName, operation, mexdao);
                }
            } else {
                partnerRole.invokeIL(mexdao);
                // Scheduling a verification to see if the invoke has really been processed. Otherwise
                // we put it in activity recovery mode (case of a server crash during invocation).
                scheduleInvokeCheck(mexdao);
            }
        } finally {
            if (mexdao.getStatus() != Status.ACK)
                mexdao.setStatus(Status.ASYNC);

        }

        assert mexdao.getStatus() == Status.ACK || mexdao.getStatus() == Status.ASYNC;
    }

    private void scheduleInvokeCheck(MessageExchangeDAO mex) {
        boolean isTwoWay = mex.getPattern() ==
                org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
        if (!isInMemory() && isTwoWay) {
            if (__log.isDebugEnabled())
                __log.debug("Creating invocation check event for mexid " + mex.getMessageExchangeId());
            WorkEvent event = new WorkEvent();
            event.setMexId(mex.getMessageExchangeId());
            event.setProcessId(getPID());
            event.setType(WorkEvent.Type.INVOKE_CHECK);
            // use a greater timeout to make sure the check job does not get executed while the service invocation is still waiting for a response
            PartnerLinkModel model = _processModel.getPartnerLink(mex.getPartnerLinkModelId());
            long timeout = (long) (getTimeout(model) * 1.5);
            Date future = new Date(System.currentTimeMillis() + timeout);
            String jobId = scheduleWorkEvent(event, future);
            mex.setProperty("invokeCheckJobId", jobId);
        }
    }

    /**
     * Invoke a partner process directly (via the engine), bypassing the Integration Layer. Obviously this can only be used when an
     * process is partners with another process hosted on the same engine.
     *
     * @param operation
     * @param serviceName
     * @param operation
     * @param partnerRoleMex
     */
    private void invokeP2P(ODEProcess target, QName serviceName, Operation operation, MessageExchangeDAO partnerRoleMex) {
        if (ODEProcess.__log.isDebugEnabled())
            __log.debug("Invoking in a p2p interaction, partnerrole " + partnerRoleMex.getMessageExchangeId()
                    + " target=" + target);

        partnerRoleMex.setInvocationStyle(
                Boolean.parseBoolean(
                        partnerRoleMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_TRANSACTED))
                        ? InvocationStyle.P2P_TRANSACTED
                        : InvocationStyle.P2P);

        // Plumbing
        MessageExchangeDAO myRoleMex = target.createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        myRoleMex.setStatus(Status.REQ);
        myRoleMex.setCallee(serviceName);

        myRoleMex.setOperation(partnerRoleMex.getOperation());
        myRoleMex.setPattern(partnerRoleMex.getPattern());
        myRoleMex.setTimeout(partnerRoleMex.getTimeout());
        myRoleMex.setRequest(partnerRoleMex.getRequest());
        myRoleMex.setInvocationStyle(partnerRoleMex.getInvocationStyle());

        // Piped cross-references.
        myRoleMex.setPipedMessageExchangeId(partnerRoleMex.getMessageExchangeId());
        myRoleMex.setPipedPID(getPID());
        partnerRoleMex.setPipedPID(target.getPID());
        partnerRoleMex.setPipedMessageExchangeId(myRoleMex.getMessageExchangeId());

        setStatefulEPRs(partnerRoleMex, myRoleMex);

        // A classic P2P interaction is considered reliable. The invocation should take place
        // in the local transaction but the invoked process is not supposed to hold our thread
        // and the reply should come in a separate transaction.
        target.invokeProcess(myRoleMex);
        if(myRoleMex.getStatus() != Status.ACK){
            MexDaoUtil.setFailed(partnerRoleMex, FailureType.NO_RESPONSE, "No Response");    
        } else {
            MexDaoUtil.copyMyRoleMexDAOToPartnerRoleMexDAOInP2PInvoke(myRoleMex, partnerRoleMex);
        }
    }

    private OdeRuntime buildRuntime(int modelVersion) {
        // Relying on package naming conventions to find our runtime
        String qualifiedName = "org.apache.ode.bpel.rtrep.v" + modelVersion + ".RuntimeImpl";
        try {
            OdeRuntime runtime = (OdeRuntime) Class.forName(qualifiedName).newInstance();
            runtime.setExtensionRegistry(_contexts.extensionRegistry);
            return runtime;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't instantiate ODE runtime version " + modelVersion +
                    ", either your process definition version is outdated or we have a bug.");
        }
    }

    void setStatefulEPRs(MessageExchangeDAO partnerRoleMex) {
        setStatefulEPRs(partnerRoleMex, null);
    }

    private void setStatefulEPRs(MessageExchangeDAO partnerRoleMex, MessageExchangeDAO myRoleMex) {
        // Properties used by stateful-exchange protocol.
        String mySessionId = partnerRoleMex.getPartnerLink().getMySessionId();
        String partnerSessionId = partnerRoleMex.getPartnerLink().getPartnerSessionId();

        if (ODEProcess.__log.isDebugEnabled())
            __log.debug("Setting myRoleMex session ids for p2p interaction, mySession " + partnerSessionId
                    + " - partnerSess " + mySessionId);

        if (mySessionId != null) {
            partnerRoleMex.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, mySessionId);
            if (myRoleMex != null)
                myRoleMex.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, mySessionId);
        }
        if (partnerSessionId != null) {
            partnerRoleMex.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, partnerSessionId);
            if (myRoleMex != null)
                myRoleMex.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, partnerSessionId);
        }

        if (__log.isDebugEnabled())
            __log.debug("INVOKE PARTNER (SEP): sessionId=" + mySessionId + " partnerSessionId=" + partnerSessionId);
    }

    /**
     * Handle in-line P2P responses. Called from the child's transaction.
     *
     * @param prolemex
     */
    private void p2pWakeup(final MessageExchangeDAO prolemex) {
        try {
            doInstanceWork(prolemex.getInstance().getInstanceId(), new Callable<Void>() {
                public Void call() throws Exception {
                    executeContinueInstancePartnerRoleResponseReceived(prolemex);
                    return null;
                }
            });
        } catch (Exception ex) {
            throw new BpelEngineException(ex);
        }
    }

    public boolean isCleanupCategoryEnabled(boolean instanceSucceeded, CLEANUP_CATEGORY category) {
        return _pconf.isCleanupCategoryEnabled(instanceSucceeded, category);
    }

    public Set<CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
        return _pconf.getCleanupCategories(instanceSucceeded);
    }

    public Node getProcessProperty(QName propertyName) {
        Map<QName, Node> properties = _pconf.getProcessProperties();
        if (properties != null) {
            return properties.get(propertyName);
        }
        return null;
    }

    public long getTimeout(PartnerLinkModel partnerLink) {
        // OPartnerLink, PartnerLinkPartnerRoleImpl
        final PartnerLinkPartnerRoleImpl linkPartnerRole = _partnerRoles.get(partnerLink);
        long timeout = Properties.DEFAULT_MEX_TIMEOUT;
        String timeout_property = _pconf.getEndpointProperties(linkPartnerRole._initialEPR).get(Properties.PROP_MEX_TIMEOUT);
        if (timeout_property != null) {
            try {
                timeout = Long.parseLong(timeout_property);
            } catch (NumberFormatException e) {
                if (__log.isWarnEnabled())
                    __log.warn("Mal-formatted Property: [" + Properties.PROP_MEX_TIMEOUT + "=" + timeout_property + "] Default value (" + timeout + ") will be used");
            }
        }
        return timeout;
    }
}

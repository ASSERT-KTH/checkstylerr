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

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.ProcessCountThrottler;
import org.apache.ode.bpel.intercept.ProcessSizeThrottler;
import org.apache.ode.bpel.o.OConstants;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the {@link BpelEngine} interface: provides the server methods that should be invoked in the context of a
 * transaction.
 *
 * @author mszefler
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelEngineImpl implements BpelEngine {
    private static final Log __log = LogFactory.getLog(BpelEngineImpl.class);

    /**
     * RNG, for delays
     */
    private Random _random = new Random(System.currentTimeMillis());

    private static double _delayMean = 0;

    static {
        try {
            String delay = System.getenv("ODE_DEBUG_TX_DELAY");
            if (delay != null && delay.length() > 0) {
                _delayMean = Double.valueOf(delay);
                __log.info("Stochastic debugging delay activated. Delay (Mean)=" + _delayMean + "ms.");
            }
        } catch (Throwable t) {
            if (__log.isDebugEnabled()) {
                __log.debug("Could not read ODE_DEBUG_TX_DELAY environment variable; assuming 0 (mean) delay", t);
            } else {
                __log.info("Could not read ODE_DEBUG_TX_DELAY environment variable; assuming 0 (mean) delay");
            }
        }
    }

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private static final double PROCESS_OVERHEAD_MEMORY_FACTOR = 1.2;

    /**
     * Active processes, keyed by process id.
     */
    public final HashMap<QName, BpelProcess> _activeProcesses = new HashMap<QName, BpelProcess>();

    /**
     * Mapping from myrole service name to active process.
     */
    private final HashMap<QName, List<BpelProcess>> _serviceMap = new HashMap<QName, List<BpelProcess>>();

    private final MultiKeyMap serviceMap = new MultiKeyMap();

    /**
     * Mapping from a potentially shared endpoint to its EPR
     */
    private SharedEndpoints _sharedEps;

    /**
     * Manage instance-level locks.
     */
    private final InstanceLockManager _instanceLockManager = new InstanceLockManager();

    final Contexts _contexts;

    private final Map<QName, Long> _hydratedSizes = new HashMap<QName, Long>();
    private final Map<QName, Long> _unhydratedSizes = new HashMap<QName, Long>();

    public BpelEngineImpl(Contexts contexts) {
        _contexts = contexts;
        _sharedEps = new SharedEndpoints();
        _sharedEps.init();
    }

    public SharedEndpoints getSharedEndpoints() {
        return _sharedEps;
    }

    public MyRoleMessageExchange createMessageExchange(String clientKey, QName targetService,
                                                       String operation, String pipedMexId, String deployer)
            throws BpelEngineException {

        List<BpelProcess> targets = route(targetService, null, deployer);
        List<BpelProcess> activeTargets = new ArrayList<BpelProcess>();
        for (BpelProcess target : targets) {
            if (target.getConf().getState() == ProcessState.ACTIVE) {
                activeTargets.add(target);
            }
        }

        if (targets == null || targets.size() == 0)
            throw new BpelEngineException("No Such Process: " + targetService);

        if (targets.size() == 1 || activeTargets.size() == 1) {
            // If the number of targets is one, create and return a simple MEX
            BpelProcess target;
            if (activeTargets.size() == 1) {
                target = activeTargets.get(0);
            } else {
                target = targets.get(0);
            }
            return createNewMyRoleMex(target, clientKey, targetService, operation, pipedMexId);
        } else {
            // If the number of targets is greater than one, create and return
            // a brokered MEX that embeds the simple MEXs for each of the targets
            BpelProcess template = activeTargets.get(0);
            ArrayList<MyRoleMessageExchange> meps = new ArrayList<MyRoleMessageExchange>();
            for (BpelProcess target : activeTargets) {
                meps.add(createNewMyRoleMex(target, clientKey, targetService, operation, pipedMexId));
            }
            return createNewMyRoleMex(template, meps);
        }
    }

    public MyRoleMessageExchange createMessageExchange(String clientKey, QName targetService,
                                                       String operation, String pipedMexId)
            throws BpelEngineException {

        List<BpelProcess> targets = route(targetService, null);
        List<BpelProcess> activeTargets = new ArrayList<BpelProcess>();
        for (BpelProcess target : targets) {
            if (target.getConf().getState() == ProcessState.ACTIVE) {
                activeTargets.add(target);
            }
        }

        if (targets == null || targets.size() == 0)
            throw new BpelEngineException("No Such Process: " + targetService);

        if (targets.size() == 1 || activeTargets.size() == 1) {
            // If the number of targets is one, create and return a simple MEX
            BpelProcess target;
            if (activeTargets.size() == 1) {
                target = activeTargets.get(0);
            } else {
                target = targets.get(0);
            }
            return createNewMyRoleMex(target, clientKey, targetService, operation, pipedMexId);
        } else {
            // If the number of targets is greater than one, create and return
            // a brokered MEX that embeds the simple MEXs for each of the targets
            BpelProcess template = activeTargets.get(0);
            ArrayList<MyRoleMessageExchange> meps = new ArrayList<MyRoleMessageExchange>();
            for (BpelProcess target : activeTargets) {
                meps.add(createNewMyRoleMex(target, clientKey, targetService, operation, pipedMexId));
            }
            return createNewMyRoleMex(template, meps);
        }
    }

    private MyRoleMessageExchange createNewMyRoleMex(BpelProcess target, String clientKey, QName targetService, String operation, String pipedMexId) {
        MessageExchangeDAO dao;
        if (target == null || target.isInMemory()) {
            dao = _contexts.inMemDao.getConnection().createMessageExchange(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        } else {
            dao = _contexts.dao.getConnection().createMessageExchange(MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        }
        dao.setCorrelationId(clientKey);
        dao.setCorrelationStatus(CorrelationStatus.UKNOWN_ENDPOINT.toString());
        dao.setPattern(MessageExchangePattern.UNKNOWN.toString());
        dao.setCallee(targetService);
        dao.setStatus(Status.NEW.toString());
        dao.setOperation(operation);
        dao.setPipedMessageExchangeId(pipedMexId);
        MyRoleMessageExchangeImpl mex = new MyRoleMessageExchangeImpl(target, this, dao);

        if (target != null) {
            target.initMyRoleMex(mex);
        }
        return mex;
    }

    /**
     * Return a brokered MEX that delegates invocations to each of the embedded
     * MEXs contained in the <code>meps</code> list, using the appropriate style.
     *
     * @param target
     * @param meps
     * @return
     * @throws BpelEngineException
     */
    private MyRoleMessageExchange createNewMyRoleMex(BpelProcess target, List<MyRoleMessageExchange> meps)
            throws BpelEngineException {
        MyRoleMessageExchangeImpl templateMex = (MyRoleMessageExchangeImpl) meps.get(0);
        MessageExchangeDAO templateMexDao = templateMex.getDAO();
        return new BrokeredMyRoleMessageExchangeImpl(target, this, meps, templateMexDao, templateMex);
    }


    public MyRoleMessageExchange createMessageExchange(String clientKey, QName targetService, String operation) {
        return createMessageExchange(clientKey, targetService, operation, null, "0");
    }

    private void setMessageExchangeProcess(String mexId, ProcessDAO processDao) {
        MessageExchangeDAO mexdao = _contexts.inMemDao.getConnection().getMessageExchange(mexId);
        if (mexdao == null) mexdao = _contexts.dao.getConnection().getMessageExchange(mexId);
        if (mexdao != null)
            mexdao.setProcess(processDao);
    }

    public MessageExchange getMessageExchange(String mexId) {
        MessageExchangeDAO mexdao = _contexts.inMemDao.getConnection().getMessageExchange(mexId);
        if (mexdao == null) mexdao = _contexts.dao.getConnection().getMessageExchange(mexId);
        if (mexdao == null)
            return null;

        ProcessDAO pdao = mexdao.getProcess();
        BpelProcess process = pdao == null ? null : _activeProcesses.get(pdao.getProcessId());

        MessageExchangeImpl mex;
        switch (mexdao.getDirection()) {
            case MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE:
                if (process == null) {
                    String errmsg = __msgs.msgProcessNotActive(pdao.getProcessId());
                    __log.error(errmsg);
                    // TODO: Perhaps we should define a checked exception for this
                    // condition.
                    throw new BpelEngineException(errmsg);
                }
            {
                OPartnerLink plink = (OPartnerLink) process.getOProcess().getChild(mexdao.getPartnerLinkModelId());
                PortType ptype = plink.partnerRolePortType;
                Operation op = plink.getPartnerRoleOperation(mexdao.getOperation());
                // TODO: recover Partner's EPR
                mex = createPartnerRoleMessageExchangeImpl(mexdao, ptype, op, plink, process);
            }
            break;
            case MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE:
                mex = new MyRoleMessageExchangeImpl(process, this, mexdao);
                if (process != null) {
                    Object child = process.getOProcess().getChild(mexdao.getPartnerLinkModelId());
                    if (child instanceof OPartnerLink) {
                        OPartnerLink plink = (OPartnerLink) child;
                        // the partner link might not be hydrated
                        if (plink != null) {
                            PortType ptype = plink.myRolePortType;
                            Operation op = plink.getMyRoleOperation(mexdao.getOperation());
                            mex.setPortOp(ptype, op);
                        }
                    }
                }
                break;
            default:
                String errmsg = "BpelEngineImpl: internal error, invalid MexDAO direction: " + mexId;
                __log.fatal(errmsg);
                throw new BpelEngineException(errmsg);
        }

        return mex;
    }

    // enable extensibility
    protected PartnerRoleMessageExchangeImpl createPartnerRoleMessageExchangeImpl(
            MessageExchangeDAO mexdao, PortType ptype, Operation op, OPartnerLink plink, BpelProcess process) {
        return new PartnerRoleMessageExchangeImpl(this, mexdao, ptype, op, null, plink.hasMyRole() ? process
                .getInitialMyRoleEPR(plink) : null, process.getPartnerRoleChannel(plink));
    }

    BpelProcess unregisterProcess(QName process) {
        BpelProcess p = _activeProcesses.remove(process);
        __log.debug("Unregister process: serviceId=" + process + ", process=" + p);
        if (p != null) {
            if (__log.isDebugEnabled())
                __log.debug("Deactivating process " + p.getPID());

            Iterator<List<BpelProcess>> serviceIter = serviceMap.values().iterator();
            while (serviceIter.hasNext()) {
                Iterator<BpelProcess> entryProcesses = serviceIter.next().iterator();
                while (entryProcesses.hasNext()) {
                    BpelProcess entryProcess = entryProcesses.next();
                    if (entryProcess.getPID().equals(process)) {
                        entryProcesses.remove();
                    }
                }
            }

            // unregister the services provided by the process
            p.deactivate();
            // release the resources held by this process
            p.dehydrate();
            // update the process footprints list
            _hydratedSizes.remove(p.getPID());
        }
        return p;
    }

    boolean isProcessRegistered(QName pid) {
        return _activeProcesses.containsKey(pid);
    }

    public BpelProcess getProcess(QName pid) {
        return _activeProcesses.get(pid);
    }

    /**
     * Register a process with the engine.
     *
     * @param process the process to register
     */
    void registerProcess(BpelProcess process) {
        _activeProcesses.put(process.getPID(), process);
        for (Endpoint e : process.getServiceNames()) {
            __log.debug("Register process: serviceId=" + e + ", process=" + process);
            List<BpelProcess> processes = null;
            if (serviceMap.get(e.serviceName, process.getConf().getDeployer()) != null) {
                processes = (List<BpelProcess>) serviceMap.get(e.serviceName, process.getConf().getDeployer());
            }
            if (processes == null) {
                processes = new ArrayList<BpelProcess>();
                serviceMap.put(e.serviceName, process.getConf().getDeployer(), processes);
            }
            // Remove any older version of the process from the list
            Iterator<BpelProcess> processesIter = processes.iterator();
            while (processesIter.hasNext()) {
                BpelProcess cachedVersion = processesIter.next();
                __log.debug("cached version " + cachedVersion.getPID() + " vs registering version " + process.getPID());
                if (cachedVersion.getProcessType().equals(process.getProcessType()) && cachedVersion.getConf().getDeployer().equals(process.getConf().getDeployer())) {
                    //Check for versions to retain newer one
                    if (cachedVersion.getVersion() > process.getVersion()) {
                        __log.debug("removing current version");
                        process.activate(this);
                        process.deactivate();
                        return;
                    } else {
                        __log.debug("removing cached older version");
                        processesIter.remove();
                        cachedVersion.deactivate();
                    }

                }
            }
            processes.add(process);
        }
        process.activate(this);
    }

    /**
     * Route to a process using the service id. Note, that we do not need the endpoint name here, we are assuming that two processes
     * would not be registered under the same service qname but different endpoint.
     *
     * @param service target service id
     * @param request request message
     * @return process corresponding to the targetted service, or <code>null</code> if service identifier is not recognized.
     */
    List<BpelProcess> route(QName service, Message request) {
        // TODO: use the message to route to the correct service if more than
        // one service is listening on the same endpoint.
        List<BpelProcess> routed = _serviceMap.get(service);
        if (__log.isDebugEnabled())
            __log.debug("Routed: svcQname " + service + " --> " + routed);
        return routed;

    }

    List<BpelProcess> route(QName service, Message request, String deployer) {
        // TODO: use the message to route to the correct service if more than
        // one service is listening on the same endpoint.
        if (serviceMap.get(service, deployer) != null) {
            List<BpelProcess> routed = (List<BpelProcess>) serviceMap.get(service, deployer);
            if (__log.isDebugEnabled())
                __log.debug("Routed: svcQname " + service + " --> " + routed);
            return routed;
        }

        return null;

    }


    OProcess getOProcess(QName processId) {
        BpelProcess process = _activeProcesses.get(processId);

        if (process == null) return null;
        return process.getOProcess();
    }

    private List<BpelProcess> getAllProcesses(QName processId) {
        String qName = processId.toString();
        if (qName.lastIndexOf("-") > 0) {
            qName = qName.substring(0, qName.lastIndexOf("-"));
        }
        List<BpelProcess> ret = new ArrayList<BpelProcess>();
        Iterator<Map.Entry<QName, BpelProcess>> it = _activeProcesses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<QName, BpelProcess> pairs = it.next();
            if (pairs.getKey().toString().startsWith(qName)) {
                ret.add(pairs.getValue());
            }
        }
        return ret;
    }

    private List<BpelProcess> getAllProcesses(QName processId, String processDeployer) {
        String qName = processId.toString();
        if (qName.lastIndexOf("-") > 0) {
            qName = qName.substring(0, qName.lastIndexOf("-"));
        }
        List<BpelProcess> ret = new ArrayList<BpelProcess>();
        Iterator<Map.Entry<QName, BpelProcess>> it = _activeProcesses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<QName, BpelProcess> pairs = it.next();
            if (pairs.getKey().toString().startsWith(qName) && pairs.getValue().getConf().getDeployer().equals(processDeployer)) {
                ret.add(pairs.getValue());
            }
        }
        return ret;
    }

    public void acquireInstanceLock(final Long iid) {
        // We lock the instance to prevent concurrent transactions and prevent unnecessary rollbacks,
        // Note that we don't want to wait too long here to get our lock, since we are likely holding
        // on to scheduler's locks of various sorts.
        try {
            _instanceLockManager.lock(iid, 1, TimeUnit.MICROSECONDS);
            _contexts.scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
                public void afterCompletion(boolean success) {
                    _instanceLockManager.unlock(iid);
                }

                public void beforeCompletion() {
                }
            });
        } catch (InterruptedException e) {
            // Retry later.
            __log.debug("Thread interrupted, job will be rescheduled");
            throw new Scheduler.JobProcessorException(true);
        } catch (org.apache.ode.bpel.engine.InstanceLockManager.TimeoutException e) {
            __log.debug("Instance " + iid + " is busy, rescheduling job.");
            throw new Scheduler.JobProcessorException(true);
        }
    }

    public void onScheduledJob(Scheduler.JobInfo jobInfo) throws Scheduler.JobProcessorException {
        final JobDetails we = jobInfo.jobDetail;

        if (__log.isTraceEnabled()) __log.trace("[JOB] onScheduledJob " + jobInfo + "" + we.getInstanceId());

        acquireInstanceLock(we.getInstanceId());

        // DONT PUT CODE HERE-need this method real tight in a try/catch block, we need to handle
        // all types of failure here, the scheduler is not going to know how to handle our errors,
        // ALSO we have to release the lock obtained above (IMPORTANT), lest the whole system come
        // to a grinding halt.
        BpelProcess process = null;
        try {
            if (we.getProcessId() != null) {
                process = _activeProcesses.get(we.getProcessId());
            } else {
                ProcessInstanceDAO instance;
                if (we.getInMem()) instance = _contexts.inMemDao.getConnection().getInstance(we.getInstanceId());
                else instance = _contexts.dao.getConnection().getInstance(we.getInstanceId());

                if (instance == null) {
                    __log.debug(__msgs.msgScheduledJobReferencesUnknownInstance(we.getInstanceId()));
                    // nothing we can do, this instance is not in the database, it will always fail, not
                    // exactly an error since can occur in normal course of events.
                    return;
                }
                ProcessDAO processDao = instance.getProcess();
                process = _activeProcesses.get(processDao.getProcessId());
            }

            if (process == null) {
                // The process is not active, there's nothing we can do with this job
                __log.debug("Process " + we.getProcessId() + " can't be found, job abandoned.");
                return;
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(process._classLoader);
                if (we.getType().equals(JobType.INVOKE_CHECK)) {
                    if (__log.isDebugEnabled())
                        __log.debug("handleJobDetails: InvokeCheck event for mexid " + we.getMexId());

                    sendPartnerRoleFailure(we, MessageExchange.FailureType.COMMUNICATION_ERROR);
                    return;
                } else if (we.getType().equals(JobType.INVOKE_INTERNAL)) {
                    if (__log.isDebugEnabled())
                        __log.debug("handleJobDetails: InvokeInternal event for mexid " + we.getMexId());

                    setMessageExchangeProcess(we.getMexId(), process.getProcessDAO());
                    MyRoleMessageExchangeImpl mex = (MyRoleMessageExchangeImpl) getMessageExchange(we.getMexId());
                    if (!process.processInterceptors(mex, InterceptorInvoker.__onJobScheduled)) {
                        boolean isTwoWay = Boolean.valueOf(mex.getProperty("isTwoWay"));
                        if (isTwoWay) {
                            String causeCodeValue = mex.getProperty("causeCode");
                            mex.getDAO().setProcess(process.getProcessDAO());
                            sendMyRoleFault(process, we, causeCodeValue != null ?
                                    Integer.valueOf(causeCodeValue) : InvalidProcessException.DEFAULT_CAUSE_CODE);
                            return;
                        } else {
                            throw new Scheduler.JobProcessorException(checkRetry(we));
                        }
                    }
                }
                if (we.getType() == JobType.INVOKE_INTERNAL || we.getType() == JobType.MEX_MATCHER) {
                    List<BpelProcess> processes = getAllProcesses(we.getProcessId(), process.getConf().getDeployer());
                    boolean routed = false;
                    jobInfo.jobDetail.detailsExt.put("enqueue", false);
                    for (BpelProcess proc : processes) {
                        routed = routed || proc.handleJobDetails(jobInfo.jobDetail);
                    }
                    if (!routed && we.getType() == JobType.INVOKE_INTERNAL) {
                        jobInfo.jobDetail.detailsExt.put("enqueue", true);
                        process.handleJobDetails(jobInfo.jobDetail);
                    }
                } else {
                    process.handleJobDetails(jobInfo.jobDetail);
                }
                debuggingDelay();
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        } catch (Scheduler.JobProcessorException e) {
            throw e;
        } catch (BpelEngineException bee) {
            __log.error(__msgs.msgScheduledJobFailed(we), bee);
            throw new Scheduler.JobProcessorException(bee, checkRetry(we));
        } catch (ContextException ce) {
            __log.error(__msgs.msgScheduledJobFailed(we), ce);
            throw new Scheduler.JobProcessorException(ce, checkRetry(we));
        } catch (InvalidProcessException ipe) {
            //__log.error(__msgs.msgScheduledJobFailed(we), ipe);
            sendMyRoleFault(process, we, ipe);
        } catch (RuntimeException rte) {
            __log.error(__msgs.msgScheduledJobFailed(we), rte);
            throw new Scheduler.JobProcessorException(rte, checkRetry(we));
        } catch (Throwable t) {
            __log.error(__msgs.msgScheduledJobFailed(we), t);
            throw new Scheduler.JobProcessorException(t, checkRetry(we));
        }
    }

    private boolean checkRetry(JobDetails we) {
        // Only retry if the job is NOT in memory. Not that this does not guaranty that a retry will be scheduled.
        // Actually events are not retried if not persisted and the scheduler might choose to discard the event if it has been retried too many times.
        return !we.getInMem();
    }

    /**
     * Block the thread for random amount of time. Used for testing for races and the like. The delay generated is exponentially
     * distributed with the mean obtained from the <code>ODE_DEBUG_TX_DELAY</code> environment variable.
     */
    private void debuggingDelay() {
        // Do a delay for debugging purposes.
        if (_delayMean != 0)
            try {
                long delay = randomExp(_delayMean);
                // distribution
                // with mean
                // _delayMean
                __log.warn("Debugging delay has been activated; delaying transaction for " + delay + "ms.");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                ; // ignore
            }
    }

    private long randomExp(double mean) {
        double u = _random.nextDouble(); // Uniform
        long delay = (long) (-Math.log(u) * mean); // Exponential
        return delay;
    }

    void fireEvent(BpelEvent event) {
        // Note that the eventListeners list is a copy-on-write array, so need
        // to mess with synchronization.
        for (org.apache.ode.bpel.iapi.BpelEventListener l : _contexts.eventListeners) {
            l.onEvent(event);
        }
    }

    /**
     * Get the list of globally-registered message-exchange interceptors.
     *
     * @return list
     */
    List<MessageExchangeInterceptor> getGlobalInterceptors() {
        return _contexts.globalInterceptors;
    }


    public void registerMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        _contexts.globalInterceptors.add(interceptor);
    }

    public void unregisterMessageExchangeInterceptor(MessageExchangeInterceptor interceptor) {
        _contexts.globalInterceptors.remove(interceptor);
    }

    public void unregisterMessageExchangeInterceptor(Class interceptorClass) {
        MessageExchangeInterceptor candidate = null;
        for (MessageExchangeInterceptor interceptor : _contexts.globalInterceptors) {
            if (interceptor.getClass().isAssignableFrom(interceptorClass)) {
                candidate = interceptor;
                break;
            }
        }
        if (candidate != null) {
            _contexts.globalInterceptors.remove(candidate);
        }
    }

    public long getTotalBpelFootprint() {
        long bpelFootprint = 0;
        for (BpelProcess process : _activeProcesses.values()) {
            Long size = _hydratedSizes.get(process.getPID());
            if (size == null) {
                size = _unhydratedSizes.get(process.getPID());
            }
            if (size != null && size.longValue() > 0) {
                bpelFootprint += size;
            }
        }
        return bpelFootprint;
    }

    public long getHydratedFootprint() {
        long hydratedFootprint = 0;
        for (BpelProcess process : _activeProcesses.values()) {
            if (!process.hintIsHydrated()) {
                continue;
            }
            Long size = _hydratedSizes.get(process.getPID());
            if (size == null) {
                size = _unhydratedSizes.get(process.getPID());
            }
            if (size != null && size.longValue() > 0) {
                hydratedFootprint += size;
            }
        }
        return hydratedFootprint;
    }

    public long getHydratedProcessSize(QName processName) {
        return getHydratedProcessSize(_activeProcesses.get(processName));
    }

    private long getHydratedProcessSize(BpelProcess process) {
        long potentialGrowth = 0;
        if (!process.hintIsHydrated()) {
            Long mySize = _hydratedSizes.get(process.getPID());
            if (mySize == null) {
                mySize = _unhydratedSizes.get(process.getPID());
            }
            if (mySize != null && mySize.longValue() > 0) {
                potentialGrowth = mySize.longValue();
            }
        }
        return getHydratedProcessSize(potentialGrowth);
    }

    private long getHydratedProcessSize(long potentialGrowth) {
        long processMemory = (long)
                ((getHydratedFootprint() + potentialGrowth) *
                        PROCESS_OVERHEAD_MEMORY_FACTOR);
        return processMemory;
    }

    public int getHydratedProcessCount(QName processName) {
        int processCount = 0;
        for (BpelProcess process : _activeProcesses.values()) {
            if (process.hintIsHydrated() || process.getPID().equals(processName)) {
                processCount++;
            }
        }
        return processCount;
    }

    private long _processThrottledMaximumSize = Long.MAX_VALUE;
    private int _processThrottledMaximumCount = Integer.MAX_VALUE;
    private int _instanceThrottledMaximumCount = Integer.MAX_VALUE;
    private boolean _hydrationThrottled = false;

    public void setInstanceThrottledMaximumCount(
            int instanceThrottledMaximumCount) {
        this._instanceThrottledMaximumCount = instanceThrottledMaximumCount;
    }

    public int getInstanceThrottledMaximumCount() {
        return _instanceThrottledMaximumCount;
    }

    public void setProcessThrottledMaximumCount(
            int hydrationThrottledMaximumCount) {
        this._processThrottledMaximumCount = hydrationThrottledMaximumCount;
        if (hydrationThrottledMaximumCount < Integer.MAX_VALUE) {
            registerMessageExchangeInterceptor(new ProcessCountThrottler());
        } else {
            unregisterMessageExchangeInterceptor(ProcessCountThrottler.class);
        }
    }

    public int getProcessThrottledMaximumCount() {
        return _processThrottledMaximumCount;
    }

    public void setProcessThrottledMaximumSize(
            long hydrationThrottledMaximumSize) {
        this._processThrottledMaximumSize = hydrationThrottledMaximumSize;
        if (hydrationThrottledMaximumSize < Long.MAX_VALUE) {
            registerMessageExchangeInterceptor(new ProcessSizeThrottler());
        } else {
            unregisterMessageExchangeInterceptor(ProcessSizeThrottler.class);
        }
    }

    public long getProcessThrottledMaximumSize() {
        return _processThrottledMaximumSize;
    }

    public void setProcessSize(QName processId, boolean hydratedOnce) {
        BpelProcess process = _activeProcesses.get(processId);
        long processSize = process.sizeOf();
        if (hydratedOnce) {
            _hydratedSizes.put(process.getPID(), processSize);
            _unhydratedSizes.remove(process.getPID());
        } else {
            _hydratedSizes.remove(process.getPID());
            _unhydratedSizes.put(process.getPID(), processSize);
        }
    }

    /**
     * Returns true if the last used process was dehydrated because it was not in-use.
     */
    public boolean dehydrateLastUnusedProcess() {
        BpelProcess lastUnusedProcess = null;
        long lastUsedMinimum = Long.MAX_VALUE;
        for (BpelProcess process : _activeProcesses.values()) {
            if (process.hintIsHydrated()
                    && process.getLastUsed() < lastUsedMinimum
                    && process.getInstanceInUseCount() == 0) {
                lastUsedMinimum = process.getLastUsed();
                lastUnusedProcess = process;
            }
        }
        if (lastUnusedProcess != null) {
            lastUnusedProcess.dehydrate();
            return true;
        }
        return false;
    }

    public void sendMyRoleFault(BpelProcess process, JobDetails we, InvalidProcessException ipe) {
        MessageExchange mex = (MessageExchange) getMessageExchange(we.getMexId());
        if (!(mex instanceof MyRoleMessageExchange)) {
            return;
        }
        QName faultQName = null;
        OConstants constants = process.getOProcess().constants;
        if (constants != null) {
            Document document = DOMUtils.newDocument();
            Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
            Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
            faultElement.appendChild(faultDetail);
            switch (ipe.getCauseCode()) {
                case InvalidProcessException.TOO_MANY_PROCESSES_CAUSE_CODE:
                    faultQName = constants.qnTooManyProcesses;
                    faultDetail.setTextContent("The total number of processes in use is over the limit.");
                    break;
                case InvalidProcessException.TOO_HUGE_PROCESSES_CAUSE_CODE:
                    faultQName = constants.qnTooHugeProcesses;
                    faultDetail.setTextContent("The total size of processes in use is over the limit");
                    break;
                case InvalidProcessException.TOO_MANY_INSTANCES_CAUSE_CODE:
                    faultQName = constants.qnTooManyInstances;
                    faultDetail.setTextContent("No more instances of the process allowed at start at this time.");
                    break;
                case InvalidProcessException.RETIRED_CAUSE_CODE:
                    // we're invoking a target process, trying to see if we can retarget the message
                    // to the current version (only applies when it's a new process creation)
                    for (BpelProcess activeProcess : _activeProcesses.values()) {
                        if (activeProcess.getConf().getState().equals(org.apache.ode.bpel.iapi.ProcessState.ACTIVE)
                                && activeProcess.getConf().getType().equals(process.getConf().getType())) {
                            we.setProcessId(activeProcess._pid);
                            ((MyRoleMessageExchangeImpl) mex)._process = activeProcess;
                            process.handleJobDetails(we);
                            return;
                        }
                    }
                    __log.error(__msgs.msgScheduledJobFailed(we), ipe);
                    faultQName = constants.qnRetiredProcess;
                    faultDetail.setTextContent("The process you're trying to instantiate has been retired.");
                    break;
                case InvalidProcessException.DEFAULT_CAUSE_CODE:
                default:
                    faultQName = constants.qnUnknownFault;
                    break;
            }
            MexDaoUtil.setFaulted((MessageExchangeImpl) mex, faultQName, faultElement);
        }
    }

    public void sendMyRoleFault(BpelProcess process, JobDetails we, int causeCode) {
        MessageExchange mex = (MessageExchange) getMessageExchange(we.getMexId());
        if (!(mex instanceof MyRoleMessageExchange)) {
            return;
        }
        QName faultQName = null;
        OConstants constants = process.getOProcess().constants;
        if (constants != null) {
            Document document = DOMUtils.newDocument();
            Element faultElement = document.createElementNS(Namespaces.SOAP_ENV_NS, "Fault");
            Element faultDetail = document.createElementNS(Namespaces.ODE_EXTENSION_NS, "fault");
            faultElement.appendChild(faultDetail);
            switch (causeCode) {
                case InvalidProcessException.TOO_MANY_PROCESSES_CAUSE_CODE:
                    faultQName = constants.qnTooManyProcesses;
                    faultDetail.setTextContent("The total number of processes in use is over the limit.");
                    break;
                case InvalidProcessException.TOO_HUGE_PROCESSES_CAUSE_CODE:
                    faultQName = constants.qnTooHugeProcesses;
                    faultDetail.setTextContent("The total size of processes in use is over the limit");
                    break;
                case InvalidProcessException.TOO_MANY_INSTANCES_CAUSE_CODE:
                    faultQName = constants.qnTooManyInstances;
                    faultDetail.setTextContent("No more instances of the process allowed at start at this time.");
                    break;
                case InvalidProcessException.RETIRED_CAUSE_CODE:
                    // we're invoking a target process, trying to see if we can retarget the message
                    // to the current version (only applies when it's a new process creation)
                    for (BpelProcess activeProcess : _activeProcesses.values()) {
                        if (activeProcess.getConf().getState().equals(org.apache.ode.bpel.iapi.ProcessState.ACTIVE)
                                && activeProcess.getConf().getType().equals(process.getConf().getType())) {
                            we.setProcessId(activeProcess._pid);
                            ((MyRoleMessageExchangeImpl) mex)._process = activeProcess;
                            process.handleJobDetails(we);
                            return;
                        }
                    }
                    faultQName = constants.qnRetiredProcess;
                    faultDetail.setTextContent("The process you're trying to instantiate has been retired.");
                    break;
                case InvalidProcessException.DEFAULT_CAUSE_CODE:
                default:
                    faultQName = constants.qnUnknownFault;
                    break;
            }
            MexDaoUtil.setFaulted((MessageExchangeImpl) mex, faultQName, faultElement);
        }
    }

    private void sendPartnerRoleFailure(JobDetails we, FailureType failureType) {
        MessageExchange mex = (MessageExchange) getMessageExchange(we.getMexId());
        if (mex instanceof PartnerRoleMessageExchange) {
            if (mex.getStatus() == MessageExchange.Status.ASYNC || mex.getStatus() == MessageExchange.Status.REQUEST) {
                String msg = "No response received for invoke (mexId=" + we.getMexId() + "), forcing it into a failed state.";
                if (__log.isDebugEnabled()) __log.debug(msg);
                MexDaoUtil.setFailure((PartnerRoleMessageExchangeImpl) mex, failureType, msg, null);
            }
        }
    }

    public BpelProcess getNewestProcessByType(QName processType) {
        int v = -1;
        BpelProcess q = null;
        for (BpelProcess p : _activeProcesses.values()) {
            if (p.getProcessType().equals(processType) && v < p.getVersion()) {
                v = p.getVersion();
                q = p;
            }
        }
        return q;
    }
}


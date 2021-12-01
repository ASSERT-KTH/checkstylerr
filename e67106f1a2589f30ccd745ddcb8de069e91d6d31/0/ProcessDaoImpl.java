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
package org.apache.ode.daohib.bpel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.DeferredProcessInstanceCleanable;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HActivityRecovery;
import org.apache.ode.daohib.bpel.hobj.HBpelEvent;
import org.apache.ode.daohib.bpel.hobj.HCorrelationProperty;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HCorrelator;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;
import org.apache.ode.daohib.bpel.hobj.HFaultData;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HMessageExchangeProperty;
import org.apache.ode.daohib.bpel.hobj.HPartnerLink;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.daohib.bpel.hobj.HScope;
import org.apache.ode.daohib.bpel.hobj.HVariableProperty;
import org.apache.ode.daohib.bpel.hobj.HXmlData;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.StaleStateException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

/**
 * Hibernate-based {@link ProcessDAO} implementation.
 */
public class ProcessDaoImpl extends HibernateDao implements ProcessDAO, DeferredProcessInstanceCleanable {
    private static final Log __log = LogFactory.getLog(ProcessDaoImpl.class);

    private static final String QRY_CORRELATOR = "where this.correlatorId = ?";

    private HProcess _process;

    public ProcessDaoImpl(SessionManager sm, HProcess process) {
        super(sm,process);
        entering("ProcessDaoImpl.ProcessDaoImpl");
        _process = process;
    }
    
    public Long getPidId() {
        return (Long) getId();
    }

    public QName getProcessId() {
        return QName.valueOf(_process.getProcessId());
    }

    public ProcessInstanceDAO getInstance(Long iid) {
        entering("ProcessDaoImpl.getInstance");
        ProcessInstanceDAO instance = BpelDAOConnectionImpl._getInstance(_sm, getSession(), iid);
        if (instance == null || !instance.getProcess().getProcessId().equals(getProcessId()))
            return null;
        return instance;
    }

    @SuppressWarnings("unchecked")
    public CorrelatorDAO getCorrelator(String  corrId) {
        entering("ProcessDaoImpl.getCorrelator");
        Iterator results;
        Query q = getSession().createFilter(_process.getCorrelators(),
                QRY_CORRELATOR);
        results = q.setString(0, corrId).iterate();

        if(!results.hasNext()){
            String msg = "no such correlator: corrId = " + corrId;
            throw new IllegalArgumentException(msg);
        }
        try {
            return new CorrelatorDaoImpl(_sm, (HCorrelator)results.next());
        } finally {
            Hibernate.close(results);
        }
    }

    public Set<String> getCorrelatorsSet() {
        Set<HCorrelator> correlators = _process.getCorrelators();
        Set<String> correlatorIDs = new HashSet<String>();
        for (HCorrelator correlator:correlators)
        {
            correlatorIDs.add(correlator.getCorrelatorId());
        }
        return correlatorIDs;
    }


    public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        entering("ProcessDaoImpl.removeRoutes");
        for (HCorrelator hCorrelator : _process.getCorrelators()) {
            new CorrelatorDaoImpl(_sm, hCorrelator).removeRoutes(routeId, target);
        }
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO correlator) {
        entering("ProcessDaoImpl.createInstance");
        HProcessInstance instance = new HProcessInstance();
        instance.setInstantiatingCorrelator((HCorrelator)((CorrelatorDaoImpl)correlator).getHibernateObj());
        instance.setProcess(_process);
        instance.setCreated(new Date());
        getSession().save(instance);
//        _process.addInstance(instance);

        return new ProcessInstanceDaoImpl(_sm,instance);
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessDAO#findInstance(CorrelationKey)
     */
    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey ckeyValue) {
        entering("ProcessDaoImpl.findInstance");
        Criteria criteria = getSession().createCriteria(HCorrelationSet.class);
        criteria.add(Expression.eq("scope.instance.process.id",_process.getId()));
        criteria.add(Expression.eq("value", ckeyValue.toCanonicalString()));
        criteria.addOrder(Order.desc("scope.instance.created"));
        return criteria.list();
    }

    /**
     * @see org.apache.ode.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
     */
    public void instanceCompleted(ProcessInstanceDAO instance) {
        // nothing to do here (yet?)
    }

    @SuppressWarnings("unchecked")
    public void deleteProcessAndRoutes() {
        // delete routes
//        deleteByIds(HCorrelatorSelector.class, getSession().getNamedQuery(HCorrelatorSelector.SELECT_MESSAGE_ROUTE_IDS_BY_PROCESS).setParameter("process", _process).list());

        // delete process dao
  //      deleteByIds(HCorrelator.class, getSession().getNamedQuery(HCorrelator.SELECT_CORRELATOR_IDS_BY_PROCESS).setParameter("process", _process).list());
        try {
            getSession().refresh(_process);
            getSession().delete(_process); // this deletes HCorrelator -> HCorrelatorSelector

            // after this delete, we have a use case that creates the process with the same procid.
            // for hibernate to work without the database deferred constraint check, let's just flush the session.
            getSession().flush();
        } catch( UnresolvableObjectException sse ) {
            __log.debug("Process: " + getProcessId() + " has been already deleted.");
            // don't sweat, they already deleted by another thread or process
        } catch( StaleStateException sse ) {
            __log.debug("Process: " + getProcessId() + " has been already deleted.");
            // don't sweat, they already deleted by another thread or process
        }
    }

    @SuppressWarnings("unchecked")
    public int deleteInstances(int transactionSize) {
        entering("ProcessDaoImpl.delete");

        if( transactionSize < 1 ) {
               if(__log.isWarnEnabled()) __log.warn("A zero or negative value was given for the transaction size of process dao deletion; overriding to '1'. Not using bulk deletion of rows may result in performance degradation.");
               transactionSize = 1;
        }

        Collection<HProcessInstance> instances = getSession().getNamedQuery(HProcessInstance.SELECT_INSTANCES_BY_PROCESS).setParameter("process", _process).setMaxResults(transactionSize).list();
        if( !instances.isEmpty() ) {
            deleteEvents(instances);
            deleteCorrelations(instances);
            deleteMessages(instances);
            deleteVariables(instances);
            deleteProcessInstances(instances);
        }

        return instances.size();
    }

    public int deleteInstances(Collection<HProcessInstance> instances, Set<CLEANUP_CATEGORY> categories) {
        entering("ProcessDaoImpl.deleteInstances");

        if( !instances.isEmpty() ) {
            if( categories.contains(CLEANUP_CATEGORY.EVENTS)) {
                deleteEvents(instances);
            }
            if( categories.contains(CLEANUP_CATEGORY.CORRELATIONS)) {
                deleteCorrelations(instances);
            }
            if( categories.contains(CLEANUP_CATEGORY.MESSAGES)) {
                deleteMessages(instances);
            }
            if( categories.contains(CLEANUP_CATEGORY.VARIABLES)) {
                deleteVariables(instances);
            }
            if( categories.contains(CLEANUP_CATEGORY.INSTANCE)) {
                deleteProcessInstances(instances);
            }
        }

        return instances.size();
    }

    @SuppressWarnings("unchecked")
    private void deleteProcessInstances(Collection<HProcessInstance> instances) {
        deleteByIds(HActivityRecovery.class, getSession().getNamedQuery(HActivityRecovery.SELECT_ACTIVITY_RECOVERY_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        deleteByIds(HFaultData.class, getSession().getNamedQuery(HFaultData.SELECT_FAULT_IDS_BY_INSTANCES).setParameterList("instances", instances).list());

        List<Long> instanceIds = new ArrayList<Long>();
        for( HProcessInstance instance : instances ) {
            instanceIds.add(instance.getId());
        }
        deleteByIds(HProcessInstance.class, instanceIds);
    }

    @SuppressWarnings("unchecked")
    private void deleteVariables(Collection<HProcessInstance> instances) {
        deleteByIds(HVariableProperty.class, getSession().getNamedQuery(HVariableProperty.SELECT_VARIABLE_PROPERTY_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        deleteByIds(HXmlData.class, getSession().getNamedQuery(HXmlData.SELECT_XMLDATA_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        deleteByIds(HPartnerLink.class, getSession().getNamedQuery(HPartnerLink.SELECT_PARTNER_LINK_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        deleteByIds(HScope.class, getSession().getNamedQuery(HScope.SELECT_SCOPE_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
    }

    @SuppressWarnings("unchecked")
    private void deleteMessages(Collection<HProcessInstance> instances) {
        deleteByIds(HActivityRecovery.class, getSession().getNamedQuery(HCorrelatorMessage.SELECT_CORMESSAGE_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        deleteByIds(HMessage.class, getSession().getNamedQuery(HMessage.SELECT_MESSAGE_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
        List<Long> mex = getSession().getNamedQuery(HMessageExchange.SELECT_MEX_IDS_BY_INSTANCES).setParameterList("instances", instances).list();
        deleteByColumn(HMessageExchangeProperty.class, "mex.id", mex);
        deleteByIds(HMessageExchange.class, mex);
    }

    @SuppressWarnings("unchecked")
    private void deleteCorrelations(Collection<HProcessInstance> instances) {
        deleteByIds(HCorrelationProperty.class, getSession().getNamedQuery(HCorrelationProperty.SELECT_CORPROP_IDS_BY_INSTANCES).setParameterList ("instances", instances).list());
        deleteByIds(HCorrelationSet.class, getSession().getNamedQuery(HCorrelationSet.SELECT_CORSET_IDS_BY_INSTANCES).setParameterList ("instances", instances).list());
    }

    @SuppressWarnings("unchecked")
    private void deleteEvents(Collection<HProcessInstance> instances) {
        deleteByIds(HBpelEvent.class, getSession().getNamedQuery(HBpelEvent.SELECT_EVENT_IDS_BY_INSTANCES).setParameterList("instances", instances).list());
    }

    public QName getType() {
        return new QName(_process.getTypeNamespace(), _process.getTypeName());
    }

    public long getVersion() {
        return _process.getVersion();
    }

    public CorrelatorDAO addCorrelator(String corrid) {
        entering("ProcessDaoImpl.addCorrelator");
        HCorrelator correlator = new HCorrelator();
        correlator.setCorrelatorId(corrid);
        correlator.setProcess(_process);
        correlator.setCreated(new Date());
//        _process.addCorrelator(correlator);
        getSession().save(correlator);
        getSession().saveOrUpdate(_process);
        return new CorrelatorDaoImpl(_sm, correlator);
    }

    @SuppressWarnings("unchecked")
    public Collection<ProcessInstanceDAO> getActiveInstances() {
        ArrayList<ProcessInstanceDAO> instDaos = new ArrayList<ProcessInstanceDAO>();
        Collection<HProcessInstance> insts = getSession().getNamedQuery(HProcessInstance.SELECT_INSTANCES_BY_PROCESS_AND_STATES)
                .setParameter("process", _process).setParameterList("states", new Object[] {ProcessState.STATE_ACTIVE}).list();
        for (HProcessInstance inst : insts)
            instDaos.add(new ProcessInstanceDaoImpl(_sm, inst));
        return instDaos;
    }

    public int getNumInstances() {
        entering("ProcessDaoImpl.getNumInstances");
        // this should be efficient if the relation is tagged as extra-lazy.
        // If the collection is not initialized yet, Hibernate will do a count(*) and the whole collection will not be fetched.
        return _process.getInstances().size();
    }

    public String getGuid() {
        return _process.getGuid();
    }

}

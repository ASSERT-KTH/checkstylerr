/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.griddynamics.jagger.monitoring.reporting;

import com.griddynamics.jagger.dbapi.entity.PerformedMonitoring;
import com.griddynamics.jagger.dbapi.entity.WorkloadData;
import com.griddynamics.jagger.reporting.AbstractMappedReportProvider;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public abstract class AbstractMonitoringReportProvider<T> extends AbstractMappedReportProvider<T> {

    public void clearCache() {
    }

    @Deprecated
    protected Map<String, String> loadMonitoringMap() {
        

        String sessionId = getSessionIdProvider().getSessionId();
        List<PerformedMonitoring> list = (List<PerformedMonitoring>) getHibernateTemplate().find("select pf from PerformedMonitoring pf where pf.sessionId =? and pf.parentId is not null", sessionId);
        Map<String, String> result = Maps.newTreeMap();

        for (PerformedMonitoring performedMonitoring : list) {
            result.put(performedMonitoring.getParentId(), performedMonitoring.getMonitoringId());
        }

        return result;
    }

    @Deprecated
    protected String relatedMonitoringTask(String taskId, Map<String, String> monitoringMap) {
        String parentId = parentOf(taskId);

        if (parentId == null) {
            return null;
        }

        return monitoringMap.get(parentId);

    }


    protected String parentOf(String workloadTaskId) {
        String sessionId = getSessionIdProvider().getSessionId();
        List<WorkloadData> list = (List<WorkloadData>) getHibernateTemplate().find("select wd from WorkloadData wd where wd.sessionId =? and wd.taskId =? and wd.parentId is not null", sessionId, workloadTaskId);

        if (list.size() == 1) {
            return list.get(0).getParentId();
        }

        return null;
    }
}

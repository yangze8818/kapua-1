/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.job.engine.commons.wrappers;

import com.google.common.base.Strings;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.job.engine.commons.exception.ReadJobPropertyException;
import org.eclipse.kapua.job.engine.commons.logger.JobLogger;
import org.eclipse.kapua.job.engine.commons.model.JobTargetSublist;
import org.eclipse.kapua.job.engine.commons.model.JobTransientUserData;
import org.eclipse.kapua.model.id.KapuaId;
import org.xml.sax.SAXException;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.Properties;

public class JobContextWrapper {

    private JobContext jobContext;

    public JobContextWrapper(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    public KapuaId getScopeId() {
        String scopeIdString = getProperties().getProperty(JobContextPropertyNames.JOB_SCOPE_ID);
        return scopeIdString != null ? KapuaEid.parseCompactId(scopeIdString) : null;
    }

    public KapuaId getJobId() {
        String jobIdString = getProperties().getProperty(JobContextPropertyNames.JOB_ID);
        return jobIdString != null ? KapuaEid.parseCompactId(jobIdString) : null;
    }

    public JobTargetSublist getTargetSublist() {
        String jobTargetSublistString = getProperties().getProperty(JobContextPropertyNames.JOB_TARGET_SUBLIST);

        try {
            return XmlUtil.unmarshal(jobTargetSublistString, JobTargetSublist.class);
        } catch (JAXBException | XMLStreamException | SAXException e) {
            throw new ReadJobPropertyException(e, JobContextPropertyNames.JOB_TARGET_SUBLIST, jobTargetSublistString);
        }
    }

    public KapuaId getResumedJobExecutionId() {
        String resumedKapuaExecutionIdString = getProperties().getProperty(JobContextPropertyNames.RESUMED_KAPUA_EXECUTION_ID);
        return Strings.isNullOrEmpty(resumedKapuaExecutionIdString) ? null : KapuaEid.parseCompactId(resumedKapuaExecutionIdString);
    }

    public Integer getFromStepIndex() {
        String fromStepIndexString = getProperties().getProperty(JobContextPropertyNames.JOB_STEP_FROM_INDEX);
        return Strings.isNullOrEmpty(fromStepIndexString) ? null : Integer.valueOf(fromStepIndexString);
    }

    public boolean getEnqueue() {
        String enqueueString = getProperties().getProperty(JobContextPropertyNames.ENQUEUE);
        return enqueueString != null && Boolean.valueOf(enqueueString);
    }

    public JobTransientUserData getJobTransientUserData() {
        JobTransientUserData transientUserData = (JobTransientUserData) getTransientUserData();

        if (transientUserData == null) {
            transientUserData = new JobTransientUserData();
            setTransientUserData(transientUserData);
        }
        return transientUserData;
    }

    public JobLogger getJobLogger() {

        JobLogger jobLogger = getJobTransientUserData().getJobLogger();

        if (jobLogger == null) {
            jobLogger = new JobLogger(getScopeId(), getJobId(), getJobName());
            getJobTransientUserData().setJobLogger(jobLogger);
        }

        return jobLogger;
    }

    public String getJobName() {
        return jobContext.getJobName();
    }

    public Object getTransientUserData() {
        return jobContext.getTransientUserData();
    }

    public void setTransientUserData(Object data) {
        jobContext.setTransientUserData(data);
    }

    public long getInstanceId() {
        return jobContext.getInstanceId();
    }

    public long getExecutionId() {
        return jobContext.getExecutionId();
    }

    public Properties getProperties() {
        return jobContext.getProperties();
    }

    public BatchStatus getBatchStatus() {
        return jobContext.getBatchStatus();
    }

    public String getExitStatus() {
        return jobContext.getExitStatus();
    }

    public void setExitStatus(String status) {
        jobContext.setExitStatus(status);
    }

    public KapuaId getKapuaExecutionId() {
        return (KapuaId) getProperties().get(JobContextPropertyNames.KAPUA_EXECUTION_ID);
    }

    public void setKapuaExecutionId(KapuaId kapuaExecutionId) {
        getProperties().put(JobContextPropertyNames.KAPUA_EXECUTION_ID, kapuaExecutionId);
    }
}

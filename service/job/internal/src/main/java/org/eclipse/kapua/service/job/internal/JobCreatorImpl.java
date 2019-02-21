/*******************************************************************************
 * Copyright (c) 2017, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.job.internal;

import org.eclipse.kapua.commons.model.AbstractKapuaNamedEntityCreator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.step.JobStep;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link JobCreator} implementation
 *
 * @since 1.0.0
 */
@KapuaProvider
public class JobCreatorImpl extends AbstractKapuaNamedEntityCreator<Job> implements JobCreator {

    private static final long serialVersionUID = 3119071638220738358L;

    private List<JobStep> jobSteps;
    private String jobXmlDefinition;

    public JobCreatorImpl(KapuaId scopeId) {
        super(scopeId);
    }

    @Override
    public List<JobStep> getJobSteps() {
        if (jobSteps == null) {
            jobSteps = new ArrayList<>();
        }

        return jobSteps;
    }

    @Override
    public void setJobSteps(List<JobStep> jobSteps) {
        this.jobSteps = jobSteps;
    }

    @Override
    public String getJobXmlDefinition() {
        return jobXmlDefinition;
    }

    @Override
    public void setJobXmlDefinition(String jobXmlDefinition) {
        this.jobXmlDefinition = jobXmlDefinition;
    }
}

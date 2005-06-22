/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/jobs/CmsPublishJob.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.Map;

/**
 * Scheduled job for time based publishing.<p>
 *
 * This class is called via the scheduled job backoffice to publish a project at a given time.<p>
 * 
 * Per default, it publishes all new, edited and deleted resources in the project which are not locked.
 * To unlock all resources in the project before publishing, add the parameter <code>unlock=true</code>
 * in the scheduled job configuration.<p>
 * 
 * @author Michael Emmerich 
 * @version $Revision: 1.4 $
 * 
 * @since 6.0
 */
public class CmsPublishJob implements I_CmsScheduledJob {

    /** Unlock parameter. */
    public static final String PARAM_UNLOCK = "unlock";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        String finishMessage;
        String unlock = (String)parameters.get(PARAM_UNLOCK);
        CmsProject project = cms.getRequestContext().currentProject();

        try {
            // check if the unlock parameter was given
            if (Boolean.valueOf(unlock).booleanValue()) {
                cms.unlockProject(project.getId());
            }
            // publish the project, the publish output will be put in the logfile
            cms.publishProject(new CmsLogReport(this.getClass()));
            finishMessage = Messages.get().key(Messages.LOG_PUBLISH_FINISHED_1, project.getName());
        } catch (CmsException e) {
            // there was an error, so create an output for the logfile
            finishMessage = Messages.get().key(
                Messages.LOG_PUBLISH_FAILD_2,
                project.getName(),
                e.getMessageContainer().key());
        }

        return finishMessage;
    }
}

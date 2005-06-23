/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/scheduler/Attic/CmsActionActivateJob.java,v $
 * Date   : $Date: 2005/06/23 09:05:03 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.scheduler;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

/**
 * List action to activate a scheduled job, can be used as direct action for a single selected item.<p>
 * 
 * @author Michael Moossen 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsActionActivateJob extends A_CmsListTwoStatesAction {

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param cms the cms context
     */
    protected CmsActionActivateJob(String id, CmsObject cms) {

        super(id, cms);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        if (getItem() != null) {
            String jobId = getItem().getId();
            CmsScheduledJobInfo job = OpenCms.getScheduleManager().getJob(jobId);
            if (!job.isActive()) {
                // activate job action
                return getFirstAction();
            }
        }
        // deactivate job action
        return getSecondAction();
    }

}

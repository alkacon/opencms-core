/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronScheduleJob.java,v $
 * Date   : $Date: 2003/11/13 16:32:30 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cron;

import org.opencms.main.OpenCms;

import com.opencms.file.CmsObject;

/**
 * This thread launches one job in its own thread.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.2 $ $Date: 2003/11/13 16:32:30 $
 * @since 5.1.12
 */
public class CmsCronScheduleJob extends Thread {

    /** The CmsObject to get access to the system */
    private CmsObject m_cms;

    /** The cron entry for this job */
    private CmsCronEntry m_entry;

    /**
     * Creates a new CmsCronScheduleJob.<p>
     * 
     * @param cms the CmsObject with an logged in user.
     * @param entry the entry to launch.
     */
    public CmsCronScheduleJob(CmsObject cms, CmsCronEntry entry) {
        super("OpenCms: Cron job " + entry);
        m_cms = cms;
        m_entry = entry;
    }

    /**
     * The run method of this thread loads the module-class and launches the Method
     * launch() on this module.
     */
    public void run() {
        try {
            // load the job class
            Class module = getClass().getClassLoader().loadClass(m_entry.getModuleName());
            // create an instance
            I_CmsCronJob job = (I_CmsCronJob)module.newInstance();
            // invoke method launch
            String retValue = job.launch(m_cms, m_entry.getModuleParameter());
            // log the returnvalue to the logfile
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info("Successful launch of job " + m_entry + (((retValue != null) && (! "".equals(retValue))) ? " Message: " + retValue : ""));
            }
        } catch (Exception exc) {
            // log the exception
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error running job for " + m_entry, exc);
            }
        }
        m_cms = null;
        m_entry = null;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/threads/CmsSynchronizeThread.java,v $
 * Date   : $Date: 2005/06/23 07:58:47 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.threads;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.synchronize.CmsSynchronize;
import org.opencms.synchronize.CmsSynchronizeSettings;

/**
 * Synchronizes a VFS folder with a folder form the "real" file system.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSynchronizeThread extends A_CmsReportThread {

    /** An error that occured during the report. */
    private Throwable m_error;

    /** The current users synchonize settings. */
    private CmsSynchronizeSettings m_settings;

    /**
     * Creates the synchronize Thread.<p>
     * 
     * @param cms the current OpenCms context object
     */
    public CmsSynchronizeThread(CmsObject cms) {

        super(cms, Messages.get().key(
            cms.getRequestContext().getLocale(),
            Messages.GUI_SYNCHRONIZE_THREAD_NAME_1,
            new Object[] {cms.getRequestContext().currentProject().getName()}));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = new CmsUserSettings(cms.getRequestContext().currentUser()).getSynchronizeSettings();
        start();
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    public Throwable getError() {

        return m_error;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        I_CmsReport report = getReport();

        report.println(Messages.get().container(Messages.RPT_SYNCHRONIZE_BEGIN_0), I_CmsReport.C_FORMAT_HEADLINE);
        try {
            new CmsSynchronize(getCms(), m_settings, getReport());
        } catch (CmsException e) {
            report.println(e);
        }
        report.println(Messages.get().container(Messages.RPT_SYNCHRONIZE_END_0), I_CmsReport.C_FORMAT_HEADLINE);
    }
}
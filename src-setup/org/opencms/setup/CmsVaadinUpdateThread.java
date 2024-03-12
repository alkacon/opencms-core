/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.setup;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.ui.report.CmsStreamReportWidget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class CmsVaadinUpdateThread extends Thread {

    /** System.out and System.err are redirected to this stream. */
    private PipedOutputStream m_pipedOut;

    /** The cms shell to import the workplace with. */
    private CmsShell m_shell;

    /** The report widget. */
    private CmsStreamReportWidget m_reportWidget;

    /** The output stream for the CmsShell. */
    private PrintStream m_out;

    /** The additional shell commands, i.e. the setup bean. */
    private CmsUpdateBean m_updateBean;

    /**
     * Constructor.<p>
     *
     * @param updateBean the initialized update bean
     */
    public CmsVaadinUpdateThread(CmsUpdateBean updateBean, CmsStreamReportWidget reportWidget) {

        super("OpenCms: Workplace update");

        // store setup bean
        m_updateBean = updateBean;
        m_out = reportWidget.getStream();
        m_reportWidget = reportWidget;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {

            // create a shell that will start importing the workplace
            m_shell = new CmsShell(
                m_updateBean.getWebAppRfsPath() + "WEB-INF" + File.separator,
                m_updateBean.getServletMapping(),
                m_updateBean.getDefaultWebApplication(),
                "${user}@${project}>",
                Arrays.asList(m_updateBean),
                m_out,
                m_out,
                false);

            try {
                try {
                    if (CmsLog.INIT.isInfoEnabled()) {
                        // log welcome message, the full package name is required because
                        // two different Message classes are used
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.setup.Messages.get().getBundle().key(
                                org.opencms.setup.Messages.INIT_WELCOME_UPDATE_0));
                        CmsLog.INIT.info(
                            org.opencms.setup.Messages.get().getBundle().key(
                                org.opencms.setup.Messages.INIT_UPDATE_WORKPLACE_START_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        for (int i = 0; i < org.opencms.main.Messages.COPYRIGHT_BY_ALKACON.length; i++) {
                            CmsLog.INIT.info(". " + org.opencms.main.Messages.COPYRIGHT_BY_ALKACON[i]);
                        }
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(
                            org.opencms.main.Messages.get().getBundle().key(org.opencms.main.Messages.INIT_LINE_0));

                    }
                    m_shell.execute(
                        new FileInputStream(
                            new File(m_updateBean.getWebAppRfsPath() + CmsUpdateBean.FOLDER_UPDATE + "cmsupdate.txt")));
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(
                            org.opencms.setup.Messages.get().getBundle().key(
                                org.opencms.setup.Messages.INIT_UPDATE_WORKPLACE_FINISHED_0));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                m_pipedOut.close();
            } catch (Throwable t) {
                // ignore
            }
        } finally {
            m_reportWidget.finish();
        }
    }
}
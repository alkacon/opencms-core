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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Used for the workplace setup in the OpenCms setup wizard.<p>
 *
 * @since 6.0.0
 */
public class CmsVaadinSetupWorkplaceImportThread extends Thread {

    /** The additional shell commands, i.e. the setup bean. */
    private CmsSetupBean m_setupBean;

    /** The cms shell to import the workplace with. */
    private CmsShell m_shell;

    /** Flag to signalize if a workplace import is needed or not. */
    private boolean m_workplaceImportNeeded;

    /** The output stream for the CmsShell. */
    private PrintStream m_out;

    /** The report widget. */
    private CmsStreamReportWidget m_reportWidget;

    /**
     * Constructor.<p>
     *
     * @param setupBean the initialized setup bean
     */
    public CmsVaadinSetupWorkplaceImportThread(CmsSetupBean setupBean, CmsStreamReportWidget reportWidget) {

        super("OpenCms: Setup workplace import");

        // store setup bean
        m_setupBean = setupBean;
        m_out = reportWidget.getStream();
        m_reportWidget = reportWidget;
        m_workplaceImportNeeded = !setupBean.getModulesToInstall().isEmpty();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if (m_workplaceImportNeeded) {
            // create a shell that will start importing the workplace

            m_shell = new CmsShell(
                m_setupBean.getWebAppRfsPath() + "WEB-INF" + File.separator,
                m_setupBean.getServletMapping(),
                m_setupBean.getDefaultWebApplication(),
                "${user}@${project}>",
                Arrays.asList(m_setupBean),
                m_out,
                m_out,
                false);

        }

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
                            org.opencms.setup.Messages.INIT_WELCOME_SETUP_0));
                    CmsLog.INIT.info(
                        org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_IMPORT_WORKPLACE_START_0));
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
                if (m_workplaceImportNeeded) {
                    m_shell.execute(
                        new FileInputStream(
                            new File(m_setupBean.getWebAppRfsPath() + CmsSetupDb.SETUP_DATA_FOLDER + "cmssetup.txt")));
                } else {
                    System.out.println(
                        org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_NO_WORKPLACE_IMPORT_NEEDED_0));
                }
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_IMPORT_WORKPLACE_FINISHED_0));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // stop the logging thread
        } catch (Throwable t) {
            // ignore
        } finally {
            m_reportWidget.finish();
        }

    }
}
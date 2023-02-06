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

package org.opencms.ui.apps.shell;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsShell;
import org.opencms.module.Messages;
import org.opencms.report.A_CmsReportThread;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Thread for run shell script
 */
public class CmsShellScriptThread extends A_CmsReportThread {

    /** Object used as lock to prevent multiple shell script threads from running at the same time. */
    private static final Object LOCK = new Object();

    /**Script to run.*/
    private String m_script;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param script Script to run
     */
    public CmsShellScriptThread(CmsObject cms, String script) {

        super(cms, "shellscript");
        initHtmlReport(cms.getRequestContext().getLocale());
        m_script = script;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        synchronized (LOCK) {
            String script = "echo on\n" + m_script;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer);
            final CmsShell shell = new CmsShell(getCms(), "${user}@${project}:${siteroot}|${uri}>", null, out, out);

            String[] subscripts = script.split("\n");
            int stringPos = 0;
            for (String subscript : subscripts) {
                shell.execute(subscript);
                out.flush();
                String res = buffer.toString();
                getReport().println(
                    Messages.get().container(Messages.RPT_IMPORT_SCRIPT_OUTPUT_1, res.substring(stringPos)));
                stringPos = res.length();
            }
        }
    }

}

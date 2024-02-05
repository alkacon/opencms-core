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

package org.opencms.staticexport;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

/**
 * Abstract implementation for the <code>{@link I_CmsStaticExportHandler}</code> interface.<p>
 *
 * This handler is most suitable for dynamic sites that use the static export
 * as optimization for non-dynamic content.<p>
 *
 * @since 6.0.0
 *
 * @see I_CmsStaticExportHandler
 */
public abstract class A_CmsOnDemandStaticExportHandler extends A_CmsStaticExportHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsOnDemandStaticExportHandler.class);

    /**
     * @see org.opencms.staticexport.I_CmsStaticExportHandler#performEventPublishProject(org.opencms.util.CmsUUID, org.opencms.report.I_CmsReport)
     */
    @Override
    public void performEventPublishProject(CmsUUID publishHistoryId, I_CmsReport report) {

        int count = 0;
        // if the handler is still running, we must wait up to 30 secounds until it is finished
        while ((count < CmsStaticExportManager.HANDLER_FINISH_TIME) && isBusy()) {
            count++;
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_WAITING_STATIC_EXPORT_3,
                            getClass().getName(),
                            Integer.valueOf(count),
                            Integer.valueOf(CmsStaticExportManager.HANDLER_FINISH_TIME)));
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // if interrupted we ignore the handler, this will produce some log messages but should be ok
                count = CmsStaticExportManager.HANDLER_FINISH_TIME;
            }
        }

        if (isBusy()) {
            // if the handler is still busy write a warning to the log and exit
            Object[] arguments = new Object[] {
                publishHistoryId,
                Integer.valueOf(CmsStaticExportManager.HANDLER_FINISH_TIME)};
            LOG.error(Messages.get().getBundle().key(Messages.LOG_SCRUBBING_FOLDER_FAILED_2, arguments));

            return;
        }

        final CmsUUID id = publishHistoryId;

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // only perform scrubbing if OpenCms is still running
            m_busy = true;
            Thread t = new Thread(new Runnable() {

                public void run() {

                    try {
                        scrubExportFolders(id);
                    } finally {
                        m_busy = false;
                    }
                }
            }, Messages.get().getBundle().key(Messages.GUI_THREAD_NAME_SCRUB_EXPORT_FOLDERS_1, String.valueOf(id)));
            t.start();
        }
    }
}
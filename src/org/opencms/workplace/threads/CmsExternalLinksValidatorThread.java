/*
 * File   :
 * Date   :
 * Version:
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.threads;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsExternalLinksValidator;
import org.opencms.report.A_CmsReportThread;
import org.opencms.workplace.threads.Messages;

import org.apache.commons.logging.Log;

/**
 * Thread for extern link validation. <p>
 *
 * @since 6.0.0
 */
public class CmsExternalLinksValidatorThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExternalLinksValidatorThread.class);

    /** the CmsObject to use. */
    private CmsObject m_cms;

    /** reference to the HtmlImport. */
    private CmsExternalLinksValidator m_externLinkValidator;

    /**
     * Constructor, creates a new CmsExternLinkValidationThread.<p>
     *
     * @param cms the current CmsObject
     */
    public CmsExternalLinksValidatorThread(CmsObject cms) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_POINTER_VALIDATION_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_externLinkValidator = new CmsExternalLinksValidator();
        m_externLinkValidator.setReport(getReport());
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * The run method which starts the import process.<p>
     */
    @Override
    public void run() {

        try {
            // do the validation
            m_externLinkValidator.validateLinks(m_cms);
        } catch (Throwable e) {
            getReport().println(e);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }
}

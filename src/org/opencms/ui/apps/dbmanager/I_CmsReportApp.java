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

package org.opencms.ui.apps.dbmanager;

import org.opencms.report.A_CmsReportThread;

/**
 * Interface for Apps, which run an import thread for files.<p>
 */
public interface I_CmsReportApp {

    /**
     * Method to open main view.<p>
     */
    void goToMainView();

    /**
     * Method to open the report view for the import thread.<p>
     *
     * @param path (state) in the app
     * @param thread which gets started and for which the report gets displayed
     * @param title to be shown for the report (title is caption of panel)
     */
    void openReport(String path, A_CmsReportThread thread, String title);
}

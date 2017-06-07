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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.xml.CmsXmlException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Holds the functionality to import resources from the file system
 * or a zip file into the OpenCms VFS.<p>
 *
 * @since 6.0.0
 */
public class CmsImport {

    /** The cms context. */
    protected CmsObject m_cms;

    /** The output report. */
    protected I_CmsReport m_report;

    /** Stores all import interface implementations .*/
    protected List<I_CmsImport> m_importImplementations;

    /**
     * Constructs a new uninitialized import, required for special subclass data import.<p>
     */
    public CmsImport() {

        // empty
        super();
    }

    /**
     * Constructs a new import object which imports the resources from an OpenCms
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the cms context
     * @param report the output report
     *
     * @throws CmsRoleViolationException if the current user dies not have role permissions to import the database
     */
    public CmsImport(CmsObject cms, I_CmsReport report)
    throws CmsRoleViolationException {

        // check the role permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        // set member variables
        m_importImplementations = OpenCms.getImportExportManager().getImportVersionClasses();
        m_cms = cms;
        m_report = report;
    }

    /**
     * Imports the resources and writes them to the cms VFS, even if there
     * already exist files with the same name.<p>
     *
     * @param parameters the import parameters
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the manifest of the import file could not be unmarshalled
     */
    public void importData(CmsImportParameters parameters) throws CmsImportExportException, CmsXmlException {

        boolean run = false;

        try {
            // now find the correct import implementation
            Iterator<I_CmsImport> i = m_importImplementations.iterator();
            while (i.hasNext()) {
                I_CmsImport importVersion = i.next();
                if (importVersion.matches(parameters)) {
                    m_report.println(
                        Messages.get().container(
                            Messages.RPT_IMPORT_VERSION_1,
                            String.valueOf(importVersion.getVersion())),
                        I_CmsReport.FORMAT_NOTE);
                    // this is the correct import version, so call it for the import process
                    importVersion.importData(m_cms, m_report, parameters);
                    OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, null));
                    run = true;
                    break;
                }
            }
            if (!run) {
                m_report.println(
                    Messages.get().container(Messages.RPT_IMPORT_DB_NO_CLASS_1, parameters.getPath()),
                    I_CmsReport.FORMAT_WARNING);
            }
        } finally {
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES, Collections.<String, Object> emptyMap()));
        }
    }
}
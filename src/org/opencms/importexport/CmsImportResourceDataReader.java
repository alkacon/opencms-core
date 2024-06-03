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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleImportData;
import org.opencms.module.CmsResourceImportData;
import org.opencms.report.I_CmsReport;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Subclass which doesn't actually import anything, but just reads the module data into a
 * data structure which can then be used by the module updater.<p>
 */
public class CmsImportResourceDataReader extends CmsImportVersion10 {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportResourceDataReader.class);

    /** The module data object to be filled. */
    private CmsModuleImportData m_moduleData;

    /**
     * Creates a new instance.<p>
     *
     * @param moduleData the module data object to be filled
     */
    public CmsImportResourceDataReader(CmsModuleImportData moduleData) {

        super();
        m_moduleData = moduleData;
    }

    /**
     * @see org.opencms.importexport.CmsImportVersion10#importAccessControlEntries()
     */
    @Override
    public void importAccessControlEntries() {

        // do nothing, ACLS handled by module updater
    }

    /**
     * @see org.opencms.importexport.CmsImportVersion10#importData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport, org.opencms.importexport.CmsImportParameters)
     */
    @Override
    public void importData(CmsObject cms, I_CmsReport report, CmsImportParameters parameters) {

        try {
            // iniitializes the import helper, but we aren't interested in the method return value
            matches(parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.importData(cms, report, parameters);
    }

    /**
     * @see org.opencms.importexport.CmsImportVersion10#importRelations()
     */
    @Override
    public void importRelations() {
        // do nothing, relations handled by module updater

    }

    /**
     * @see org.opencms.importexport.CmsImportVersion10#importResource()
     */
    @Override
    public void importResource() {

        try {
            if (m_throwable != null) {
                getReport().println(m_throwable);
                getReport().addError(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;
                m_importACEs = false;
                m_resource = null;
                return;
            }
            // apply name translation and import path
            String translatedName = getRequestContext().addSiteRoot(m_parameters.getDestinationPath() + m_destination);
            boolean resourceImmutable = checkImmutable(translatedName);
            translatedName = getRequestContext().removeSiteRoot(translatedName);
            boolean isExistingParent = !m_hasStructureId && isFolderType(m_typeName) && getCms().existsResource(translatedName, CmsResourceFilter.ALL);
            if (!resourceImmutable && !isExistingParent) {
                byte[] content = null;
                if (m_source != null) {
                    content = m_helper.getFileBytes(m_source);
                }
                int size = 0;
                if (content != null) {
                    size = content.length;
                }
                setDefaultsForEmptyResourceFields();
                // create a new CmsResource
                CmsResource resource = createResourceObjectFromFields(translatedName, size);
                if (!OpenCms.getResourceManager().hasResourceType(m_typeName)) {
                    CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT_TYPE, null, m_typeName);
                    m_properties.put(CmsPropertyDefinition.PROPERTY_EXPORT_TYPE, prop);
                }
                CmsResourceImportData resData = new CmsResourceImportData(
                    resource,
                    translatedName,
                    content,
                    Lists.newArrayList(m_properties.values()),
                    m_aces,
                    m_relationsForResource,
                    m_hasStructureId,
                    m_hasDateLastModified,
                    m_typeName);
                m_moduleData.addResource(resData);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_report.println(e);
        }

    }

    /**
     * @see org.opencms.importexport.CmsImportVersion10#rewriteParseables()
     */
    @Override
    public void rewriteParseables() {

        // do nothing , parseables handled by module updater
    }

}

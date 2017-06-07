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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * Helper class for locating configuration files by looking up their location in properties of another resource.<p>
 *
 * @since 8.0.0
 */
public class CmsConfigurationFileFinder {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationFileFinder.class);

    /** The name of the property which should contain the configuration file path. */
    private String m_propertyName;

    /**
     * Creates a new configuration file finder which expects the location of configuration files to be stored in the
     * property with the given name.<p>
     *
     * @param propertyName the name of the property which should contain the configuration file path
     */
    public CmsConfigurationFileFinder(String propertyName) {

        m_propertyName = propertyName;
    }

    /**
     * Returns the configuration file to use.<p>
     *
     * @param cms the current cms context
     * @param containerPageUri the container page uri
     *
     * @return the configuration file to use, or <code>null</code> if not found
     */
    public CmsResource getConfigurationFile(CmsObject cms, String containerPageUri) {

        String cfgPath = null;
        try {
            // get the resource type configuration file from the vfs tree
            cfgPath = cms.readPropertyObject(containerPageUri, m_propertyName, true).getValue();
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(cfgPath)) {
            // if not found try at the template
            try {
                // retrieve the template uri
                String templateUri = cms.readPropertyObject(
                    containerPageUri,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    true).getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(templateUri)) {
                    // get the resource type configuration file from the template itself
                    cfgPath = cms.readPropertyObject(templateUri, m_propertyName, true).getValue();
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(cfgPath)) {
            // configuration could not be found
            LOG.warn(Messages.get().getBundle().key(Messages.ERR_CONFIG_NOT_SET_2, containerPageUri, m_propertyName));
            return null;
        }

        try {
            // read configuration file
            return cms.readResource(cfgPath);
        } catch (Exception e1) {
            try {
                CmsResource baseResource = cms.readResource(containerPageUri);
                String baseRootPath = baseResource.getRootPath();
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(baseRootPath);
                String rootCfgPath = CmsStringUtil.joinPaths(siteRoot, cfgPath);
                return cms.readResource(rootCfgPath);
            } catch (Exception e2) {
                throw new CmsIllegalStateException(
                    Messages.get().container(
                        Messages.ERR_CONFIG_NOT_FOUND_3,
                        containerPageUri,
                        m_propertyName,
                        cfgPath));
            }
        }
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/CmsXmlRemoveImmutables.java,v $
 * Date   : $Date: 2007/11/06 14:48:39 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove the old legacy immutable resources no longer supported by version 7.0.x.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.0.3
 */
public class CmsXmlRemoveImmutables extends A_CmsSetupXmlUpdate {

    /** List of xpaths to update. */
    private List m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Remove old legacy immutable resources";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsImportExportConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToRemove()
     */
    protected List getXPathsToRemove() {

        if (m_xpaths == null) {
            // "/opencms/importexport/import/immutables/resource[@uri='...']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsImportExportConfiguration.N_IMPORTEXPORT);
            xp.append("/").append(CmsImportExportConfiguration.N_IMPORT);
            xp.append("/").append(CmsImportExportConfiguration.N_IMMUTABLES);
            xp.append("/").append(I_CmsXmlConfiguration.N_RESOURCE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_URI);
            xp.append("='");
            m_xpaths = new ArrayList();
            m_xpaths.add(xp.toString() + "/channels/']");
            m_xpaths.add(xp.toString() + "/system/bodies/']");
            m_xpaths.add(xp.toString() + "/system/workplace/action/']");
            m_xpaths.add(xp.toString() + "/system/workplace/administration/']");
        }
        return m_xpaths;
    }

}
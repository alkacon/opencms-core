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

package org.opencms.setup.xml.v7;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new image gallery parameter processing parameter.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddImgGalleryParam extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Image Gallery parameter";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    CmsResourceTypeFolderExtended.CONFIGURATION_FOLDER_CLASS_PARAMS);
                CmsSetupXmlHelper.setValue(document, xpath, "w:120,h:90,c:D4D0C8,t:1,r:2");
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/vfs/resources/resourcetypes/type[@class='org.opencms.file.types.CmsResourceTypeFolderExtended']
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsVfsConfiguration.N_VFS);
        xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
        xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
        xp.append("/").append(CmsVfsConfiguration.N_TYPE);
        xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
        xp.append("='").append(CmsResourceTypeFolderExtended.class.getName());
        return xp.append("']").toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // "/opencms/vfs/resources/resourcetypes/type[@class='org.opencms.file.types.CmsResourceTypeFolderExtended']/param[@name='folder.class.params']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
            xp.append("/").append(CmsVfsConfiguration.N_TYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='").append(CmsResourceTypeFolderExtended.class.getName());
            xp.append("']/").append(I_CmsXmlConfiguration.N_PARAM);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='").append(CmsResourceTypeFolderExtended.CONFIGURATION_FOLDER_CLASS_PARAMS);
            xp.append("']");
            m_xpaths = Collections.singletonList(xp.toString());
        }
        return m_xpaths;
    }

}
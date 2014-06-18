/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.types.A_CmsResourceType;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxImageGallery;
import org.opencms.workplace.galleries.CmsAjaxLinkGallery;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new parameters of resource type.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddResourceTypeParams extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new parameters of resource type";
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
                    "formatter_gallery_preview");
                CmsSetupXmlHelper.setValue(document, xpath, "/system/workplace/editors/ade/image-preview-formatter.jsp");
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_JAVASCRIPT_PATH);
                CmsSetupXmlHelper.setValue(document, xpath, "editors/ade/js/cms.imagepreviewhandler.js");
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(2))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxImageGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(3))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxLinkGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(7))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
                CmsSetupXmlHelper.setValue(document, xpath, CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(8))) {
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_NAME,
                    "formatter_gallery_preview");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath,
                    "/system/workplace/editors/ade/binary-preview-formatter.jsp");
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

        // /opencms/vfs/resources/resourcetypes
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsVfsConfiguration.N_VFS);
        xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
        xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // "/opencms/vfs/resources/resourcetypes/type[@class='org.opencms.file.types.CmsResourceTypeImage']/param[@name='{0}']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
            xp.append("/").append(CmsVfsConfiguration.N_TYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='").append(org.opencms.file.types.CmsResourceTypeImage.class.getName());
            xp.append("']/").append(I_CmsXmlConfiguration.N_PARAM);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='").append("{0}");
            xp.append("']");
            m_xpaths = new ArrayList<String>();
            m_xpaths.add(xp.toString().replace("{0}", "formatter_gallery_preview"));
            m_xpaths.add(xp.toString().replace("{0}", A_CmsResourceType.CONFIGURATION_GALLERY_JAVASCRIPT_PATH));
            // "/opencms/vfs/resources/resourcetypes/type[@class='...']/param[@name='gallery.type.names']";
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
            xp.append("/").append(CmsVfsConfiguration.N_TYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='").append("{0}");
            xp.append("']/").append(I_CmsXmlConfiguration.N_PARAM);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='").append(A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES);
            xp.append("']");
            m_xpaths.add(xp.toString().replace("{0}", org.opencms.file.types.CmsResourceTypeImage.class.getName()));
            m_xpaths.add(xp.toString().replace("{0}", org.opencms.file.types.CmsResourceTypePlain.class.getName()));
            m_xpaths.add(xp.toString().replace("{0}", org.opencms.file.types.CmsResourceTypeBinary.class.getName()));
            m_xpaths.add(xp.toString().replace("{0}", org.opencms.file.types.CmsResourceTypePointer.class.getName()));
            m_xpaths.add(xp.toString().replace("{0}", org.opencms.file.types.CmsResourceTypeXmlContent.class.getName()));
            // "/opencms/vfs/resources/resourcetypes/type[@class='org.opencms.file.types.CmsResourceTypeBinary']/param[@name='{0}']";
            xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
            xp.append("/").append(CmsVfsConfiguration.N_TYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='").append(org.opencms.file.types.CmsResourceTypeBinary.class.getName());
            xp.append("']/").append(I_CmsXmlConfiguration.N_PARAM);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='").append("{0}");
            xp.append("']");
            m_xpaths.add(xp.toString().replace("{0}", "formatter_gallery_preview"));
        }
        return m_xpaths;
    }

}
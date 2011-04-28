/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/xml/v8/Attic/CmsXmlAddResourceTypes.java,v $
 * Date   : $Date: 2011/04/28 15:02:27 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.workplace.galleries.CmsAjaxDownloadGallery;
import org.opencms.workplace.galleries.CmsAjaxHtmlGallery;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new resource type classes, from 7.5.2 to 8.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 8.0.0
 */
public class CmsXmlAddResourceTypes extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new resource type classes";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                createResourceType(
                    document,
                    xpath,
                    CmsResourceTypeXmlContainerPage.getStaticTypeName(),
                    CmsResourceTypeXmlContainerPage.class,
                    CmsResourceTypeXmlContainerPage.getStaticTypeId());
                // parameters
                createRtParameter(
                    document,
                    xpath,
                    "formatter_gallery_preview",
                    "/system/workplace/editors/ade/container-preview-formatter.jsp");
                createRtParameter(document, xpath, A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES, "cntpagegallery");
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                createResourceType(
                    document,
                    xpath,
                    CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_NAME,
                    CmsResourceTypeXmlContent.class,
                    CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_ID);
                // parameters
                createRtParameter(
                    document,
                    xpath,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES,
                    CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                createRtParameter(
                    document,
                    xpath,
                    CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA,
                    "/system/modules/org.opencms.ade.config/schemas/containerpage_config.xsd");
            } else if (xpath.equals(getXPathsToUpdate().get(2))) {
                createResourceType(document, xpath, "cntpagegallery", CmsResourceTypeFolderExtended.class, 16);
                // parameters
                createRtParameter(
                    document,
                    xpath,
                    CmsResourceTypeFolderExtended.CONFIGURATION_FOLDER_CLASS,
                    CmsAjaxHtmlGallery.class.getName());
            } else if (xpath.equals(getXPathsToUpdate().get(3))) {
                createResourceType(
                    document,
                    xpath,
                    CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
                    CmsResourceTypeXmlContent.class,
                    CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_ID);
                // parameters
                createRtParameter(
                    document,
                    xpath,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES,
                    CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                createRtParameter(
                    document,
                    xpath,
                    CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA,
                    "/system/modules/org.opencms.ade.containerpage/schemas/group_container.xsd");
            } else if (xpath.equals(getXPathsToUpdate().get(4))) {
                createResourceType(
                    document,
                    xpath,
                    CmsResourceTypeJsp.getContainerPageTemplateTypeName(),
                    CmsResourceTypeJsp.class,
                    CmsResourceTypeJsp.getContainerPageTemplateTypeId());
            } else if (xpath.equals(getXPathsToUpdate().get(5))) {
                createResourceType(document, xpath, "sitemap_config", CmsResourceTypeXmlContent.class, 22);
                // parameters
                createRtParameter(
                    document,
                    xpath,
                    A_CmsResourceType.CONFIGURATION_GALLERY_TYPE_NAMES,
                    CmsAjaxDownloadGallery.GALLERYTYPE_NAME);
                createRtParameter(
                    document,
                    xpath,
                    CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA,
                    "/system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd");
            } else if (xpath.equals(getXPathsToUpdate().get(6))) {
                createResourceType(document, xpath, "xmlredirect", CmsResourceTypeXmlContent.class, 15);
                //NOTE: this only works because it's a single property. If we wanted to write multiple properties, we
                //would need to do it differently.
                CmsSetupXmlHelper.setValue(document, xpath + "/properties/property/name", "template-elements");
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/properties/property/value[@type='shared']",
                    "/system/modules/org.opencms.ade.sitemap/pages/redirect-detail.jsp");
                createRtParameter(
                    document,
                    xpath,
                    CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA,
                    "/system/modules/org.opencms.ade.sitemap/schemas/redirect.xsd");

            } else if (xpath.equals(getXPathsToUpdate().get(7))) {
                createResourceType(document, xpath, "entrypoint", CmsResourceTypeFolderExtended.class, 23);
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // "/opencms/vfs/resources/resourcetypes"
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
            // "/opencms/vfs/resources/resourcetypes/type[@name='...']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCES);
            xp.append("/").append(CmsVfsConfiguration.N_RESOURCETYPES);
            xp.append("/").append(CmsVfsConfiguration.N_TYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='");
            m_xpaths = new ArrayList<String>();
            /*0*/m_xpaths.add(xp.toString() + CmsResourceTypeXmlContainerPage.getStaticTypeName() + "']");
            /*1*/m_xpaths.add(xp.toString() + CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_NAME + "']");
            /*2*/m_xpaths.add(xp.toString() + "cntpagegallery" + "']");
            /*3*/m_xpaths.add(xp.toString() + CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME + "']");
            /*4*/m_xpaths.add(xp.toString() + CmsResourceTypeJsp.getContainerPageTemplateTypeName() + "']");
            /*5*/m_xpaths.add(xp.toString() + "sitemap_config" + "']");
            /*6*/m_xpaths.add(xp.toString() + "xmlredirect" + "']");
            /*7*/m_xpaths.add(xp.toString() + "entrypoint" + "']");

        }
        return m_xpaths;
    }

}
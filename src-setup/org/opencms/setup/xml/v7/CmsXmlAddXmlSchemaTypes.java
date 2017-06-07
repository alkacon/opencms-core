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
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.widgets.CmsVfsImageWidget;
import org.opencms.xml.types.CmsXmlCategoryValue;
import org.opencms.xml.types.CmsXmlDynamicCategoryValue;
import org.opencms.xml.types.CmsXmlPlainTextStringValue;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsImageValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new xml schema types.<p>
 *
 * @since 7.0.3
 */
public class CmsXmlAddXmlSchemaTypes extends A_CmsXmlVfs {

    /** The schema data. */
    private Map<String, String> m_schemaData;

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Xml schema types";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (getXPathsToUpdate().contains(xpath)) {
                Iterator<Map.Entry<String, String>> it = getSchemaData().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    String className = entry.getKey();
                    String widgetName = entry.getValue();
                    if (xpath.indexOf(className) > 0) {
                        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_CLASS, className);
                        CmsSetupXmlHelper.setValue(
                            document,
                            xpath + "/@" + CmsVfsConfiguration.A_DEFAULTWIDGET,
                            widgetName);
                        break;
                    }
                }
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

        // /opencms/vfs/xmlcontent/schematypes
        StringBuffer xp = new StringBuffer(256);
        xp.append("/").append(CmsConfigurationManager.N_ROOT);
        xp.append("/").append(CmsVfsConfiguration.N_VFS);
        xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
        xp.append("/").append(CmsVfsConfiguration.N_SCHEMATYPES);
        return xp.toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // "/opencms/vfs/xmlcontent/schematypes/schematype[@class='...']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
            xp.append("/").append(CmsVfsConfiguration.N_SCHEMATYPES);
            xp.append("/").append(CmsVfsConfiguration.N_SCHEMATYPE);
            xp.append("[@").append(I_CmsXmlConfiguration.A_CLASS);
            xp.append("='");
            m_xpaths = new ArrayList<String>();
            Iterator<Map.Entry<String, String>> it = getSchemaData().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String className = entry.getKey();
                m_xpaths.add(xp.toString() + className + "']");
            }
        }
        return m_xpaths;
    }

    /**
     * Returns the schema type data.<p>
     *
     * @return the schema type data
     */
    private Map<String, String> getSchemaData() {

        if (m_schemaData == null) {
            m_schemaData = new HashMap<String, String>();
            m_schemaData.put(CmsXmlVarLinkValue.class.getName(), CmsVfsFileWidget.class.getName());
            m_schemaData.put(CmsXmlVfsImageValue.class.getName(), CmsVfsImageWidget.class.getName());
            m_schemaData.put(CmsXmlPlainTextStringValue.class.getName(), CmsInputWidget.class.getName());
            m_schemaData.put(CmsXmlCategoryValue.class.getName(), CmsCategoryWidget.class.getName());
            m_schemaData.put(CmsXmlDynamicCategoryValue.class.getName(), CmsCategoryWidget.class.getName());
        }
        return m_schemaData;
    }

}
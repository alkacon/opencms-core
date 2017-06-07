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
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsInputWidgetPlaintext;
import org.opencms.widgets.CmsLocalizationWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
import org.opencms.widgets.CmsOrgUnitWidget;
import org.opencms.widgets.CmsPrincipalWidget;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsTextareaWidgetPlaintext;
import org.opencms.widgets.CmsUserWidget;
import org.opencms.widgets.CmsVfsImageWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new xml content widgets.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddXmlContentWidgets extends A_CmsXmlVfs {

    /** the widget data. */
    private Map<String, String> m_widgetData;

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new Xml Content widgets";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        boolean modified = false;
        if (node == null) {
            if (getXPathsToUpdate().contains(xpath)) {
                Iterator<Map.Entry<String, String>> it = getWidgetData().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    String widgetName = entry.getKey();
                    String className = entry.getValue();
                    if (xpath.indexOf(widgetName) > 0) {
                        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_CLASS, className);
                        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ALIAS, widgetName);
                        modified = true;
                        break;
                    }
                }
            }
        }
        return modified;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // "/opencms/vfs/xmlcontent/widgets/widget[@alias='widget']";
            StringBuffer xp = new StringBuffer(256);
            xp.append("/").append(CmsConfigurationManager.N_ROOT);
            xp.append("/").append(CmsVfsConfiguration.N_VFS);
            xp.append("/").append(CmsVfsConfiguration.N_XMLCONTENT);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGETS);
            xp.append("/").append(CmsVfsConfiguration.N_WIDGET);
            xp.append("[@").append(I_CmsXmlConfiguration.A_ALIAS);
            xp.append("='");
            m_xpaths = new ArrayList<String>();
            Iterator<Map.Entry<String, String>> it = getWidgetData().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String widgetName = entry.getKey();
                m_xpaths.add(xp.toString() + widgetName + "']");
            }
        }
        return m_xpaths;
    }

    /**
     * Returns the widget data.<p>
     *
     * @return the widget data
     */
    private Map<String, String> getWidgetData() {

        if (m_widgetData == null) {
            m_widgetData = new HashMap<String, String>();
            m_widgetData.put("DisplayWidget", CmsDisplayWidget.class.getName());
            m_widgetData.put("MultiSelectWidget", CmsMultiSelectWidget.class.getName());
            m_widgetData.put("UserWidget", CmsUserWidget.class.getName());
            m_widgetData.put("GroupWidget", CmsGroupWidget.class.getName());
            m_widgetData.put("CategoryWidget", CmsCategoryWidget.class.getName());
            m_widgetData.put("LocalizationWidget", CmsLocalizationWidget.class.getName());
            m_widgetData.put("TextareaWidgetPlaintext", CmsTextareaWidgetPlaintext.class.getName());
            m_widgetData.put("StringWidgetPlaintext", CmsInputWidgetPlaintext.class.getName());
            m_widgetData.put("OrgUnitWidget", CmsOrgUnitWidget.class.getName());
            m_widgetData.put("PrincipalWidget", CmsPrincipalWidget.class.getName());
            m_widgetData.put("RadioSelectWidget", CmsRadioSelectWidget.class.getName());
            m_widgetData.put("VfsImageWidget", CmsVfsImageWidget.class.getName());
        }
        return m_widgetData;
    }
}
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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;

/**
 * Updates the widget configuration.<p>
 *
 *
 * @since 8.0.0
 */
public class CmsXmlAddWidgets extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * The new widget definition data.<p>
     */
    private String[][] m_widgets = {
        {"org.opencms.widgets.CmsAdeDownloadGalleryWidget", "DownloadGalleryWidget"},
        {"org.opencms.widgets.CmsAdeImageGalleryWidget", "ImageGalleryWidget"},
        {"org.opencms.widgets.CmsDownloadGalleryWidget", "LegacyDownloadGalleryWidget"},
        {"org.opencms.widgets.CmsImageGalleryWidget", "LegacyImageGalleryWidget"},
        {"org.opencms.widgets.CmsSelectGroupWidget", "GroupSelectorWidget"}};

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new widget definitions for ADE galleries";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCodeToChange(setupBean));
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        for (int i = 0; i < m_widgets.length; i++) {
            if (xpath.equals(getXPathsToUpdate().get(i))) {
                CmsSetupXmlHelper.setValue(document, xpathForWidgetClassAttribute(m_widgets[i][1]), m_widgets[i][0]);
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

        return xpathForWidgets();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            for (int i = 0; i < m_widgets.length; i++) {
                m_xpaths.add(xpathForWidgetByAlias(m_widgets[i][1]));
            }
        }
        return m_xpaths;
    }

    /**
     * Returns the xpath for a widget with a given alias.<p>
     *
     * @param alias the widget alias
     *
     * @return the xpath for the widget with the given alias
     */
    protected String xpathForWidgetByAlias(String alias) {

        return xpathForWidgets()
            + "/"
            + CmsVfsConfiguration.N_WIDGET
            + "[@"
            + I_CmsXmlConfiguration.A_ALIAS
            + "='"
            + alias
            + "']";

    }

    /**
     * Returns the xpath of the class attribute for a widget with a given alias.<p>
     *
     * @param alias the widget alias
     *
     * @return the xpath of the widget's class attribute
     */
    protected String xpathForWidgetClassAttribute(String alias) {

        return xpathForWidgetByAlias(alias) + "/@" + I_CmsXmlConfiguration.A_CLASS;
    }

    /**
     * Returns the xpath for the widgets node.<p>
     *
     * @return the xpath for the widgets node
     */
    protected String xpathForWidgets() {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsVfsConfiguration.N_VFS
            + "/"
            + CmsVfsConfiguration.N_XMLCONTENT
            + "/"
            + CmsVfsConfiguration.N_WIDGETS;
    }

}
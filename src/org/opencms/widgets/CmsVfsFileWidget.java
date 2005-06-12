/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsVfsFileWidget.java,v $
 * Date   : $Date: 2005/06/12 11:18:21 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.I_CmsWpConstants;

/**
 * Provides a OpenCms VFS file selection widget, for use on a widget dialog.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.2
 */
public class CmsVfsFileWidget extends A_CmsWidget {

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_HIDESITESELECTOR = "hidesiteselector";

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_SHOWSITESELECTOR = "showsiteselector";

    /** Configuration parameter to set start site of the popup resource tree. */
    public static final String CONFIGURATION_STARTSITE = "startsite";

    /** Flag to determine if the site selector should be shown in popup window. */
    private boolean m_showSiteSelector;

    /** The start site used in the popup window. */
    private String m_startSite;

    /**
     * Creates a new vfs file widget.<p>
     */
    public CmsVfsFileWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Createa a new vfs file widget with the parameters to configure the popup tree window behaviour.<p>
     * 
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite) {

        m_showSiteSelector = showSiteSelector;
        m_startSite = startSite;
    }

    /**
     * Creates a new vfs file widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsVfsFileWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "commons/tree.js"));
        result.append("\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/fileselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinitVfsFileSelector();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("function initVfsFileSelector() {\n");
        //initialize tree javascript, does parts of CmsTree.initTree(CmsObject, encoding, skinuri);
        result.append("\tinitResources(\"");
        result.append(OpenCms.getWorkplaceManager().getEncoding());
        result.append("\", \"");
        result.append(I_CmsWpConstants.C_VFS_PATH_WORKPLACE);
        result.append("\", \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("\", \"");
        result.append(OpenCms.getSystemInfo().getOpenCmsContext());
        result.append("\");\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input class=\"xmlInputMedium");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openTreeWin('EDITOR',  '");
        buttonJs.append(id);
        buttonJs.append("', document");
        if (m_showSiteSelector) {
            buttonJs.append(", true");
        } else {
            buttonJs.append(", false");
        }
        buttonJs.append(", '");
        if (m_startSite != null) {
            buttonJs.append(m_startSite);
        } else {
            buttonJs.append(cms.getRequestContext().getSiteRoot());
        }
        buttonJs.append("'");
        buttonJs.append(");");

        result.append(widgetDialog.button(
            buttonJs.toString(),
            null,
            "folder",
            org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the start site root shown by the widget when first displayed.<p>
     *
     * If <code>null</code> is returned, the dialog will display the current site of 
     * the current user.<p>
     *
     * @return the start site root shown by the widget when first displayed
     */
    public String getStartSite() {

        return m_startSite;
    }

    /**
     * Returns <code>true</code> if the site selector is shown.<p>
     *
     * The default is <code>true</code>.<p>
     *
     * @return <code>true</code> if the site selector is shown
     */
    public boolean isShowingSiteSelector() {

        return m_showSiteSelector;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsVfsFileWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        m_showSiteSelector = true;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            if (configuration.indexOf(CONFIGURATION_HIDESITESELECTOR) != -1) {
                // site selector should be hidden
                m_showSiteSelector = false;
            }
            int siteIndex = configuration.indexOf(CONFIGURATION_STARTSITE);
            if (siteIndex != -1) {
                // start site is given
                String site = configuration.substring(CONFIGURATION_STARTSITE.length() + 1);
                if (site.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    site = site.substring(0, site.indexOf('|'));
                }
                m_startSite = site;
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    protected String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append site selector flag to configuration
        if (m_showSiteSelector) {
            result.append(CONFIGURATION_SHOWSITESELECTOR);
        } else {
            result.append(CONFIGURATION_HIDESITESELECTOR);
        }

        // append start site to configuration
        if (m_startSite != null) {
            result.append("|");
            result.append(CONFIGURATION_STARTSITE);
            result.append("=");
            result.append(m_startSite);
        }

        return result.toString();
    }
}
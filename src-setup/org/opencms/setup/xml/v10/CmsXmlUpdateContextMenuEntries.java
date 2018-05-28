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

package org.opencms.setup.xml.v10;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * XML updater class for adding context menu rules specific to ADE.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlUpdateContextMenuEntries extends A_CmsXmlWorkplace {

    /** Base path. */
    public static final String PATH = "/opencms/workplace/explorertypes['updatemenu_10.5'='updatemenu_10.5']";

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Update some context menu entries";
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

        if (!forReal) {
            return true;
        } else {
            String[] types = new String[] {"xmlcontent"};
            String[] advancedEntries = new String[] {
                "key:GUI_EXPLORER_CONTEXT_ADE_PUBLISHSCHEDULED_0|name:org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction|params:dialogId=org.opencms.ui.actions.CmsPublishScheduledDialogAction|rule:ade-publishscheduled"};
            for (String type : types) {
                String advancedPath = xpathForType(type)
                    + "/editoptions/contextmenu/entry[@key='GUI_EXPLORER_CONTEXT_ADE_ADVANCED_0']";
                Element advancedElem = (Element)(document.selectSingleNode(advancedPath));
                if (advancedElem != null) {
                    for (String advanced : advancedEntries) {
                        Map<String, String> entryMap = CmsStringUtil.splitAsMap(advanced, "|", ":");
                        Element existingElem = (Element)(advancedElem.selectSingleNode(
                            "entry[@key='" + entryMap.get("key") + "']"));
                        if (existingElem == null) {
                            Element newElem = advancedElem.addElement("entry");
                            for (Map.Entry<String, String> entry : entryMap.entrySet()) {
                                newElem.addAttribute(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return PATH;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList(PATH);

    }

    /**
     * Helper method for generating an xpath fragment "[@attr='value']".<p>
     *
     * @param attr the attribute name
     * @param value the attribute value
     * @return the xpath fragment
     */
    protected String xpathAttr(String attr, String value) {

        return "[@" + attr + "='" + value + "']";
    }

    /**
     * Returns the xpath for a specific explorer type.<p>
     *
     * @param explorerType the explorer type
     *
     * @return the xpath for that explorer type
     */
    protected String xpathForType(String explorerType) {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsWorkplaceConfiguration.N_WORKPLACE
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPES
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPE
            + "[@name='"
            + explorerType
            + "']";
    }

}

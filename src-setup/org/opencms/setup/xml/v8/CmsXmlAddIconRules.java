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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * XML updater for adding icon rules to opencms-workplace.xml.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlAddIconRules extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Adds icon rules for various explorer types.";
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

        Node node = document.selectSingleNode(xpath);
        if (node != null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                if (document.selectSingleNode(xpath + "/" + CmsWorkplaceConfiguration.N_ICONRULES) != null) {
                    return false;
                }
                org.dom4j.Element explorerTypePlain = (org.dom4j.Element)document.selectSingleNode(xpath);
                org.dom4j.Element iconRules = explorerTypePlain.addElement(CmsWorkplaceConfiguration.N_ICONRULES);
                addIconRule(iconRules, "java", "java.png", "java_big.png");
                addIconRule(iconRules, "js", "js.png", "js_big.png");
                addIconRule(iconRules, "html", "html.png", "html_big.png");
                addIconRule(iconRules, "xhtml", "html.png", "html_big.png");
                addIconRule(iconRules, "htm", "html.png", "html_big.png");
                addIconRule(iconRules, "txt", "text.png", "text_big.png");
                addIconRule(iconRules, "xml", "xml.png", "xml_big.png");
                return true;
            } else if (xpath.equals(getXPathsToUpdate().get(1))) {
                if (document.selectSingleNode(xpath + "/iconrules") != null) {
                    return false;
                }
                // binary
                org.dom4j.Element explorerTypeBinary = (org.dom4j.Element)document.selectSingleNode(xpath);
                org.dom4j.Element iconRules = explorerTypeBinary.addElement(CmsWorkplaceConfiguration.N_ICONRULES);
                addIconRule(iconRules, "doc", "msword.png", "msword_big.png");
                addIconRule(iconRules, "docx", "msword.png", "msword_big.png");
                addIconRule(iconRules, "xls", "excel.png", "excel_big.png");
                addIconRule(iconRules, "ppt", "powerpoint.png", "powerpoint_big.png");
                addIconRule(iconRules, "zip", "archive.png", "archive_big.png");
                addIconRule(iconRules, "rar", "archive.png", "archive_big.png");
                addIconRule(iconRules, "pdf", "pdf.png", "pdf_big.png");
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

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsWorkplaceConfiguration.N_WORKPLACE
            + "/"
            + CmsWorkplaceConfiguration.N_EXPLORERTYPES;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            m_xpaths.add(xpathForType("plain"));
            m_xpaths.add(xpathForType("binary"));
        }
        return m_xpaths;
    }

    /**
     * Adds an icon rule to the XML dom.<p>
     *
     * @param element the parent element
     * @param extension the extension
     * @param icon the icon name
     * @param bigicon the big icon name
     */
    private void addIconRule(org.dom4j.Element element, String extension, String icon, String bigicon) {

        org.dom4j.Element ruleElem = element.addElement(CmsWorkplaceConfiguration.N_ICONRULE);
        ruleElem.addAttribute(CmsWorkplaceConfiguration.A_EXTENSION, extension);
        ruleElem.addAttribute(I_CmsXmlConfiguration.A_ICON, icon);
        ruleElem.addAttribute(CmsWorkplaceConfiguration.A_BIGICON, bigicon);
    }

    /**
     * Returns the xpath for a given explorer type.<p>
     *
     * @param explorerType the explorer type
     *
     * @return the xpath for that explorer type
     */
    private String xpathForType(String explorerType) {

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

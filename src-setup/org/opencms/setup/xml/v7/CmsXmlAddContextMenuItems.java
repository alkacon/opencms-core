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
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds new context menu item nodes.<p>
 *
 * @since 6.1.8
 */
public class CmsXmlAddContextMenuItems extends A_CmsXmlWorkplace {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    public boolean executeUpdate(Document document, String xpath, boolean forReal) {

        if (forReal && !checkExplorerType(document, xpath)) {
            return false;
        }

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.indexOf("property_multifile") > 0) {
                // insert after /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/separator[2]']
                if (forReal) {
                    String xp = xpath.substring(0, xpath.indexOf("entry[@"))
                        + CmsWorkplaceConfiguration.N_SEPARATOR
                        + "[2]";
                    CmsSetupXmlHelper.setValue(document, xp, null, xpath.substring(xpath.indexOf("entry[@")));
                }
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_KEY,
                    org.opencms.workplace.commons.Messages.GUI_EXPLORER_CONTEXT_MULTIFILE_PROPERTY_0);
                CmsSetupXmlHelper.setValue(document, xpath + "/@" + CmsWorkplaceConfiguration.A_RULE, "nondeleted");
                // insert separator
                CmsSetupXmlHelper.setValue(document, xpath, null, CmsWorkplaceConfiguration.N_SEPARATOR);
            } else if (xpath.indexOf("publishscheduledresource") > 0) {
                // insert after /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/entry[@uri='commons/publishresource.jsp']
                if (forReal) {
                    String xp = xpath.replace("publishscheduledresource", "publishresource");
                    CmsSetupXmlHelper.setValue(document, xp, null, xpath.substring(xpath.indexOf("entry[@")));
                }
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + I_CmsXmlConfiguration.A_KEY,
                    org.opencms.workplace.explorer.Messages.GUI_EXPLORER_CONTEXT_PUBLISH_SCHEDULED_0);
                CmsSetupXmlHelper.setValue(
                    document,
                    xpath + "/@" + CmsWorkplaceConfiguration.A_RULE,
                    "publishscheduled");
            } else if (xpath.indexOf("publishscheduled") > 0) {
                CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_NAME, "publishscheduled");
                String xp = xpath
                    + "[@"
                    + I_CmsXmlConfiguration.A_NAME
                    + "='publishscheduled']/"
                    + CmsWorkplaceConfiguration.N_MENUITEMRULE
                    + "[@"
                    + I_CmsXmlConfiguration.A_CLASS
                    + "='";
                String[] classes = new String[] {
                    org.opencms.workplace.explorer.menu.CmsMirPrOnlineInvisible.class.getName(),
                    org.opencms.workplace.explorer.menu.CmsMirPrOtherInvisible.class.getName(),
                    org.opencms.workplace.explorer.menu.CmsMirDirectPublish.class.getName()};
                for (int i = 0; i < classes.length; i++) {
                    String xpMenuRule = xp + classes[i] + "']" + "/@" + I_CmsXmlConfiguration.A_CLASS;
                    CmsSetupXmlHelper.setValue(document, xpMenuRule, classes[i]);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new context menu items";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    public List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/workplace/explorertypes/explorertype[@name='${etype}']/editoptions/contextmenu/entry[@uri='commons/${res}.jsp']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPE);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='${etype}']/");
            xp.append(CmsWorkplaceConfiguration.N_EDITOPTIONS);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_CONTEXTMENU);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_ENTRY);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.A_URI);
            xp.append("='commons/${res}.jsp']");
            m_xpaths = new ArrayList<String>();

            Map<String, String> subs = new HashMap<String, String>();
            subs.put("${res}", "property_multifile");
            subs.put("${etype}", CmsResourceTypeFolder.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "downloadgallery");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));

            subs.put("${res}", "publishscheduledresource");
            subs.put("${etype}", "xmlcontent");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypePlain.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeImage.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeJsp.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypeBinary.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", CmsResourceTypePointer.getStaticTypeName());
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));
            subs.put("${etype}", "unknown_file");
            m_xpaths.add(CmsStringUtil.substitute(xp.toString(), subs));

            // /opencms/workplace/explorertypes/menurules/menurule[@name='publishscheduled']
            xp.setLength(0);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_WORKPLACE);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_EXPLORERTYPES);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_MENURULES);
            xp.append("/");
            xp.append(CmsWorkplaceConfiguration.N_MENURULE);
            xp.append("[@");
            xp.append(I_CmsXmlConfiguration.A_NAME);
            xp.append("='publishscheduled']");
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }

    /**
     * Checks whether the explorer type referenced by an xpath exists in the document.<p>
     *
     * @param doc the document
     * @param xpath the xpath, potentially referencing an explorertype
     * @return true if the xpath references no explorertype, or the referenced explorertype is contained in the document
     */
    protected boolean checkExplorerType(Document doc, String xpath) {

        Pattern pattern = Pattern.compile("^.*explorertype\\[@name='(.*?)'\\].*$");
        Matcher matcher = pattern.matcher(xpath);
        if (matcher.matches()) {
            String etype = matcher.group(1);
            return existsExplorerType(doc, etype);
        }
        return true;
    }

    /**
     * Checks whether a given explorertype exists in the document.<p>
     *
     * @param doc the XML document
     * @param explorertype the explorertype to check
     *
     * @return true if the explorertype exists
     */
    protected boolean existsExplorerType(Document doc, String explorertype) {

        Node etype = doc.selectSingleNode(
            "/opencms/workplace/explorertypes/explorertype[@name='" + explorertype + "']");
        return etype != null;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/workplace/explorertypes
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsWorkplaceConfiguration.N_WORKPLACE).append("/").append(
                CmsWorkplaceConfiguration.N_EXPLORERTYPES).toString();
    }
}

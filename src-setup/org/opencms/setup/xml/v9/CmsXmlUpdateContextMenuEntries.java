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

package org.opencms.setup.xml.v9;

import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlWorkplace;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Adjusts the JSP paths for certain context menu entries.<p>
 */
public class CmsXmlUpdateContextMenuEntries extends A_CmsXmlWorkplace {

    /** The derprecated JSP URIs. */
    private static final String[] DEPRECATED_URIS = new String[] {
        "/system/modules/org.opencms.ade.publish/direct_publish.jsp",
        "/system/modules/org.opencms.ade.properties/pages/properties.jsp"};

    /** The new JSP URIs. */
    private static final String[] REPLACE_URIS = new String[] {
        "/system/workplace/commons/direct_publish.jsp",
        "/system/workplace/commons/properties.jsp"};

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCodeToChange(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public String getCodeToChange(CmsSetupBean setupBean) throws Exception {

        StringBuffer buffer = new StringBuffer();
        Document doc = setupBean.getXmlHelper().getDocument(getXmlFilename());
        for (int i = 0; i < DEPRECATED_URIS.length; i++) {
            List<?> nodes = doc.selectNodes(getXPath(DEPRECATED_URIS[i]));
            for (Object obj : nodes) {
                Element node = (Element)obj;
                String xml = node.asXML();

                buffer.append(xml.replace(DEPRECATED_URIS[i], REPLACE_URIS[i])).append("\n");
            }
        }
        return buffer.toString();
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Updates the menu rules properties and direct publish";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsXmlWorkplace#getXmlFilename()
     */
    @Override
    public String getXmlFilename() {

        return "opencms-workplace.xml";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        Document doc = setupBean.getXmlHelper().getDocument(getXmlFilename());
        for (int i = 0; i < DEPRECATED_URIS.length; i++) {
            List<?> nodes = doc.selectNodes(getXPath(DEPRECATED_URIS[i]));
            if (!nodes.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean result = false;
        for (int i = 0; i < DEPRECATED_URIS.length; i++) {
            if (xpath.contains(DEPRECATED_URIS[i])) {
                List<?> nodes = document.selectNodes(xpath);
                for (Object obj : nodes) {
                    Element node = (Element)obj;
                    node.addAttribute("uri", REPLACE_URIS[i]);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        List<String> paths = new ArrayList<String>();
        for (int i = 0; i < DEPRECATED_URIS.length; i++) {
            paths.add(getXPath(DEPRECATED_URIS[i]));
        }
        return paths;
    }

    /**
     * Returns the XPath for the menu entry node.<p>
     *
     * @param uri the URI to match
     *
     * @return the XPath
     */
    private String getXPath(String uri) {

        return "//*/entry[@uri=\"" + uri + "\"]";
    }
}

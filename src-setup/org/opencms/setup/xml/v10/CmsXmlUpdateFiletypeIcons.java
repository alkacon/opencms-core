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

package org.opencms.setup.xml.v10;

import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Replaces gif icon paths with png icon paths in explorer types in opencms-workplace.xml.
 */
public class CmsXmlUpdateFiletypeIcons extends A_CmsSetupXmlUpdate {

    /** String containing the icon replacements. */
    public static final String ICON_REPLACEMENTS = "folder.gif:folder.png|plain.gif:plain.png|image.gif:image.png|jsp.gif:jsp.png|binary.gif:binary.png|pointer.gif:pointer.png|link.gif:link.png|imagegallery.gif:imagegallery.png|downloadgallery.gif:downloadgallery.png|linkgallery.gif:linkgallery.png|xmlcontent.gif:xmlcontent.png";

    /** The icon replacement map. */
    private Map<String, String> m_iconReplacements = CmsStringUtil.splitAsMap(ICON_REPLACEMENTS, "|", ":");

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Replaces file type icon paths.";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsWorkplaceConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        List<?> nodes = document.selectNodes("//explorertype");
        for (Object obj : nodes) {
            if (obj instanceof Element) {
                Element elem = (Element)obj;
                updateIcon(elem, "icon");
                updateIcon(elem, "bigicon");
            }
        }
        return true;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return "/opencms/workplace";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Arrays.asList("/opencms/workplace['replaceFileIcons'='replaceFileIcons']"); // xpath should be unique for update beans
    }

    /**
     * Updates an icon attribute in an explorer type element.<p>
     *
     * @param elem the element
     * @param attribute the attribute name
     */
    private void updateIcon(Element elem, String attribute) {

        String icon = elem.attributeValue(attribute);
        String newIcon = null;
        if ((icon != null) && m_iconReplacements.containsKey(icon)) {
            newIcon = m_iconReplacements.get(icon);
        }
        if (newIcon != null) {
            elem.addAttribute(attribute, newIcon);
        }
    }

}
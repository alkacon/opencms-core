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

import org.opencms.setup.xml.A_CmsXmlWorkplace;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Adjusts the JSP paths for certain context menu entries.<p>
 */
public class CmsXmlUpdateAvailabilityMenuEntries extends A_CmsXmlWorkplace {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Updates ADE context menu entries for the availability dialog.";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsXmlWorkplace#getXmlFilename()
     */
    @Override
    public String getXmlFilename() {

        return "opencms-workplace.xml";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        List<?> nodes = document.selectNodes(
            "//contextmenu//entry[@name='org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog']");
        for (Object node : nodes) {
            if (node instanceof Element) {
                Element element = (Element)node;
                element.addAttribute("name", "org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction");
                element.addAttribute(
                    "params",
                    "dialogId=" + org.opencms.ui.actions.CmsAvailabilityDialogAction.class.getName());
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

        return Arrays.asList("/opencms/workplace['updateAvailabilityMenuEntries'='updateAvailabilityMenuEntries']"); // xpath should be unique for update beans
    }

}

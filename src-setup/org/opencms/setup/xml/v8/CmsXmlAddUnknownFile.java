/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.setup.xml.A_CmsXmlWorkplace;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Update class for adding the "unknown_file" explorer type.<p>
 */
public class CmsXmlAddUnknownFile extends A_CmsXmlWorkplace {

    /** XML For the unknown_file explorer type. */
    private static String XML_UNKNOWN_FILE = "      <explorertype name=\"unknown_file\" key=\"fileicon.unknown_file\" icon=\"unknown_file.png\">\r\n"
        + "        <newresource uri=\"\" order=\"0\" autosetnavigation=\"false\" autosettitle=\"false\"/>\r\n"
        + "        <accesscontrol>\r\n"
        + "          <accessentry principal=\"DEFAULT\" permissions=\"+r+v\"/>\r\n"
        + "          <accessentry principal=\"ROLE.VFS_MANAGER\" permissions=\"+r+v+w+c\"/>\r\n"
        + "          <accessentry principal=\"GROUP.Guests\" permissions=\"-r-v-w-c\"/>\r\n"
        + "        </accesscontrol>\r\n"
        + "        <editoptions>\r\n"
        + "          <defaultproperties enabled=\"false\" shownavigation=\"false\" />\r\n"
        + "          <contextmenu>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_LOCK_0\" uri=\"commons/lock.jsp\" rule=\"lock\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0\" uri=\"commons/lockchange.jsp\" rule=\"changelock\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNLOCK_0\" uri=\"commons/unlock.jsp\" rule=\"unlock\"/>\r\n"
        + "            <separator/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_PUBLISH_0\" uri=\"commons/publishresource.jsp\" rule=\"directpublish\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_PUBLISH_SCHEDULED_0\" uri=\"commons/publishscheduledresource.jsp\" rule=\"publishscheduled\"/>\r\n"
        + "            <separator/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_MOVE_0\" uri=\"commons/move.jsp\" rule=\"standard\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_DELETE_0\" uri=\"commons/delete.jsp\" rule=\"standard\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDOCHANGES_0\" uri=\"commons/undochanges.jsp\" rule=\"undochanges\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_UNDELETE_0\" uri=\"commons/undelete.jsp\" rule=\"undelete\"/>\r\n"
        + "            <separator/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_RELATIONS_0\" rule=\"substandard\">\r\n"
        + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONTO_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationtarget\" rule=\"standard\"/>\r\n"
        + "                <entry key=\"GUI_EXPLORER_CONTEXT_LINKRELATIONFROM_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Flinkrelationsource\" rule=\"standard\"/>\r\n"
        + "                <separator/>\r\n"
        + "                <entry key=\"GUI_EXPLORER_CONTEXT_SHOWSIBLINGS_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fsiblings\" rule=\"showsiblings\"/>\r\n"
        + "            </entry>\r\n"
        + "            <separator/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_ADVANCED_0\" rule=\"substandard\">\r\n"
        + "                <entry key=\"GUI_EXPLORER_CONTEXT_AVAILABILITY_0\" uri=\"commons/availability.jsp\" rule=\"standard\"/>\r\n"
        + "                <separator/>\r\n"
        + "                <entry key=\"GUI_EXPLORER_CONTEXT_TYPE_0\" uri=\"commons/chtype.jsp\" rule=\"standard\"/>\r\n"
        + "            </entry>\r\n"
        + "            <separator/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_HISTORY_0\" uri=\"views/admin/admin-main.jsp?root=explorer&amp;path=%2Fhistory\" rule=\"nondeleted\"/>\r\n"
        + "            <entry key=\"GUI_EXPLORER_CONTEXT_PROPERTY_0\" uri=\"commons/property.jsp\" rule=\"nondeleted\"/>\r\n"
        + "          </contextmenu>\r\n"
        + "        </editoptions>\r\n"
        + "      </explorertype>\r\n";

    /**
     * Creates a dom4j element from an XML string.<p>
     *
     * @param xml the xml string
     * @return the dom4j element
     *
     * @throws DocumentException if the XML parsing fails
     */
    public static org.dom4j.Element createElementFromXml(String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document newNodeDocument = reader.read(new StringReader(xml));
        return newNodeDocument.getRootElement();
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Adds the 'unknown_file' explorer type";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        if (!forReal) {
            return true;
        }
        Node node = document.selectSingleNode("/opencms/workplace/explorertypes/explorertype[@name='unknown_file']");
        if (node == null) {
            try {
                Element newElem = createElementFromXml(XML_UNKNOWN_FILE);
                Element parent = (Element)(document.selectSingleNode("/opencms/workplace/explorertypes"));
                parent.elements().add(0, newElem);
            } catch (DocumentException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return "/opencms/workplace/explorertypes";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList(getCommonPath());
    }
}

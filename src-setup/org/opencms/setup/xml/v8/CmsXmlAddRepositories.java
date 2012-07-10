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

import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.setup.xml.A_CmsSetupXmlUpdate;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 * XML update class for adding new repositories to the import/export configuration.<p>
 */
public class CmsXmlAddRepositories extends A_CmsSetupXmlUpdate {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new repositories to the repository configuration";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsImportExportConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * Executes the adding/updating changes on the given document.<p>
     * 
     * Only needs to be overriden if {@link #getXPathsToUpdate()} is not empty.<p>
     * 
     * @param document the document to apply the changes to
     * @param xpath the xpath to execute the changes for
     * @param forReal is <code>false</code>, it is only on a empty doc to display the changes to the user
     * 
     * @return if something was modified
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        if (!forReal) {
            return true;
        }

        if (null == document.selectSingleNode("//opencms/repositories/repository[@name='cmis-offline']")) {
            try {
                org.dom4j.Element element = createElementFromXml("<repository name=\"cmis-offline\" class=\"org.opencms.cmis.CmsCmisRepository\">\r\n"
                    + "         <params>\r\n"
                    + "            <param name=\"project\">Offline</param>\r\n"
                    + "            <param name=\"description\">Offline project CMIS repository</param>\r\n"
                    + "        </params></repository>");
                ((org.dom4j.Element)document.selectSingleNode("//opencms/repositories")).add(element);
            } catch (DocumentException e) {
                System.out.println("Couldn't add repository");
                return false;
            }
        }

        if (null == document.selectSingleNode("//opencms/repositories/repository[@name='cmis-online']")) {
            try {
                org.dom4j.Element element = createElementFromXml("<repository name=\"cmis-offline\" class=\"org.opencms.cmis.CmsCmisRepository\">\r\n"
                    + "         <params>\r\n"
                    + "            <param name=\"project\">Online</param>\r\n"
                    + "            <param name=\"description\">Online project CMIS repository</param>\r\n"
                    + "        </params></repository>");
                ((org.dom4j.Element)document.selectSingleNode("//opencms/repositories")).add(element);
            } catch (DocumentException e) {
                System.out.println("Couldn't add repository");
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a parent path that is common for all nodes to modify.<p> 
     * 
     * @return common parent path
     */
    @Override
    protected String getCommonPath() {

        return "/opencms/repositories";
    }

    /**
     * Returns a list of xpaths for the nodes to add/update.<p>
     * 
     * @return a list of strings
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList("/opencms/repositories");
    }

}

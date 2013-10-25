/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.setup.xml.A_CmsXmlSearch;

import java.util.Collections;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * Adds the solr search node.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddSolrSearch extends A_CmsXmlSearch {

    /**
     * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

        Element node = (Element)doc.selectSingleNode("/opencms/search");
        if (node.selectSingleNode("solr") == null) {
            String solrComment = " To enable Solr in OpenCms you must create a solr/ home\n"
                + "           directory in the WEB-INF folder of your OpenCms application.\n"
                + "           Copy the solr/ folder from the OpenCms standard distribution\n"
                + "           as a starting point for your configuration. ";
            try {
                Element solrElement = createElementFromXml("<solr enabled=\"false\"></solr>");
                solrElement.addComment(solrComment);
                node.elements().add(0, solrElement);
            } catch (DocumentException e) {
                System.out.println("Could not add solr node");
                return false;
            }
        } else {
            String solrComment = "\n"
                + "           During the update Solr will be disabled in the WEB-INF/config/opencms-search.xml.\n"
                + "           To update Solr you must update the 'schema.xml and' the 'solrconfig.xml' manually.\n"
                + "           The new default configuration files are located in the solr-update/ directory in\n"
                + "           the WEB-INF folder of your application. If you are using the default configuration\n"
                + "           from the distribution, it is sufficient to copy the new configuration files to the\n"
                + "           WEB-INF/solr folder. Else if you have customized the Solr configuration you might\n"
                + "           want to merge the 'schema.xml' and the 'solrconfig.xml' first. When you are done\n"
                + "           set the attribute enabled to 'true' again.\n";
            Element solrElement = (Element)node.selectSingleNode("solr");
            Attribute a = solrElement.attribute("enabled");
            if (a != null) {
                a.setValue("false");
            }
            solrElement.addComment(solrComment);
        }
        return true;
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add the Solr configuration";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/system/internationalization
        return "/opencms/search";

    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList("/opencms/search");
    }
}
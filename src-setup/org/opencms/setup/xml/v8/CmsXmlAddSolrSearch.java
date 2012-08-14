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

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.main.OpenCms;
import org.opencms.setup.xml.A_CmsXmlSearch;
import org.opencms.setup.xml.CmsXmlUpdateAction;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Adds the gallery search nodes.<p>
 * 
 * @since 8.0.0
 */
public class CmsXmlAddSolrSearch extends A_CmsXmlSearch {

    /**
     * Action to add the gallery modules index source.<p>
     */
    public static class CmsAddSolrIndexSourceAction extends CmsXmlUpdateAction {

        /**
         * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
         */
        @Override
        public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

            OpenCms.writeConfiguration(CmsSearchConfiguration.class);

            Element node = (Element)doc.selectSingleNode("/opencms/search");

            // TODO: Is not yet tested in "real life"
            String solrComment = "To enable Solr search engine you have to create a Solr home directory\n"
                + "        according to the OpenCms default distribution below your webapplications\n"
                + "        WEB-INF folder\n\n"
                + "    <solr enabled=\"true\"/>\n";
            node.addComment(solrComment);
            return true;
        }
    }

    public String getName() {

        return "Add the Solr configuration";
    }
}
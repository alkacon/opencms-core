/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.xml.A_CmsXmlSearch;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Updates the opencms-serach.xml.<p>
 */
public class CmsXmlCleanUpSearchConfiguration extends A_CmsXmlSearch {

    /** List of xpaths to remove. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Remove unnecessary Solr Gallery Index";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToRemove()
     */
    @Override
    protected List<String> getXPathsToRemove() {

        if (m_xpaths == null) {
            // /opencms/search/indexes/index[name='System folder']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_SEARCH);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATIONS);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_FIELDCONFIGURATION);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='solr_gallery_fields']");
            m_xpaths = Collections.singletonList(xp.toString());
        }
        return m_xpaths;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        StringBuffer xp = new StringBuffer(256);
        xp.append("/");
        xp.append(CmsConfigurationManager.N_ROOT);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_SEARCH);
        xp.append("/");
        xp.append(CmsSearchConfiguration.N_INDEXES);
        try {
            Iterator<Node> it = CmsCollectionsGenericWrapper.<Node> list(document.selectNodes(xp.toString())).iterator();
            while (it.hasNext()) {
                Node node = it.next();
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)node;
                    // iterate through child elements of root
                    Iterator<Node> it2 = CmsCollectionsGenericWrapper.<Node> list(e.nodeIterator()).iterator();
                    while (it2.hasNext()) {
                        Node iNode = it2.next();
                        if (iNode.getNodeType() == Node.COMMENT_NODE) {
                            Comment com = (Comment)iNode;
                            if (com.getText().contains("<name>Solr Gallery Index</name>")) {
                                document.remove(com);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // only tried to remove a comment
            // if an error occurs here nothing will get damaged
        }
        return super.executeUpdate(document, xpath, forReal);
    }
}

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

package org.opencms.setup.xml.v7;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsProject;
import org.opencms.setup.xml.A_CmsXmlSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the german help search index.<p>
 * 
 * @since 6.1.8 
 */
public class CmsXmlAddDEHelpSearchIndex extends A_CmsXmlSearch {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add German Help search index";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            if (xpath.equals(getXPathsToUpdate().get(0))) {
                createIndex(
                    document,
                    xpath,
                    null,
                    "German online help",
                    "auto",
                    CmsProject.ONLINE_PROJECT_NAME,
                    Locale.GERMAN.getLanguage(),
                    null,
                    new String[] {"source2"});
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

        // /opencms/search
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsSearchConfiguration.N_SEARCH).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            // /opencms/search/indexes/index[name='German online help']
            StringBuffer xp = new StringBuffer(256);
            xp.append("/");
            xp.append(CmsConfigurationManager.N_ROOT);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_SEARCH);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEXES);
            xp.append("/");
            xp.append(CmsSearchConfiguration.N_INDEX);
            xp.append("[");
            xp.append(I_CmsXmlConfiguration.N_NAME);
            xp.append("='German online help']");
            m_xpaths = new ArrayList<String>();
            m_xpaths.add(xp.toString());
        }
        return m_xpaths;
    }

}
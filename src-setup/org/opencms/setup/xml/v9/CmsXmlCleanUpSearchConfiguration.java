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
import org.opencms.setup.xml.A_CmsXmlSearch;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Updates the opencms-serach.xml.<p>
 */
public class CmsXmlCleanUpSearchConfiguration extends A_CmsXmlSearch {

    /** The XPaths to remove. */
    public static final String[] REMOVE_PATHS = new String[] {
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_FIELDCONFIGURATIONS
            + "/"
            + CmsSearchConfiguration.N_FIELDCONFIGURATION
            + "[@class='org.opencms.search.solr.CmsGallerySolrFieldConfiguration']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_FIELDCONFIGURATIONS
            + "/"
            + CmsSearchConfiguration.N_FIELDCONFIGURATION
            + "[@class='org.opencms.search.galleries.CmsGallerySearchFieldConfiguration']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_INDEXES
            + "/"
            + CmsSearchConfiguration.N_INDEX
            + "[@class='org.opencms.search.galleries.CmsGallerySearchIndex']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_DOCUMENTTYPES
            + "/"
            + CmsSearchConfiguration.N_DOCUMENTTYPE
            + "[name='xmlcontent-galleries']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_DOCUMENTTYPES
            + "/"
            + CmsSearchConfiguration.N_DOCUMENTTYPE
            + "[name='xmlpage-galleries']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_ANALYZERS
            + "/"
            + CmsSearchConfiguration.N_ANALYZER
            + "[class='org.opencms.search.galleries.CmsGallerySearchAnalyzer']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCES
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCE
            + "[name='gallery_source']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCES
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCE
            + "[name='gallery_modules_source']",
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCES
            + "/"
            + CmsSearchConfiguration.N_INDEXSOURCE
            + "[name='gallery_source_all']"};

    /** The XPaths to update. */
    public static final String[] UPDATE_PATHS = new String[] {
        "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsSearchConfiguration.N_SEARCH
            + "/"
            + CmsSearchConfiguration.N_ANALYZERS};

    /** List of xpaths to remove. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Remove unnecessary Gallery Index";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean changed = false;
        if (document.selectSingleNode(
            xpath
                + "/"
                + CmsSearchConfiguration.N_ANALYZER
                + "["
                + CmsSearchConfiguration.N_CLASS
                + "='org.apache.lucene.analysis.standard.StandardAnalyzer']["
                + CmsSearchConfiguration.N_LOCALE
                + "='all']") == null) {
            Element analyzers = (Element)document.selectSingleNode(xpath);
            Element analyzer = analyzers.addElement(CmsSearchConfiguration.N_ANALYZER);
            Element clazz = analyzer.addElement(CmsSearchConfiguration.N_CLASS);
            clazz.setText("org.apache.lucene.analysis.standard.StandardAnalyzer");
            Element locale = analyzer.addElement(CmsSearchConfiguration.N_LOCALE);
            locale.setText("all");
            changed = true;
        }
        return changed;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToRemove()
     */
    @Override
    protected List<String> getXPathsToRemove() {

        if (m_xpaths == null) {
            m_xpaths = Arrays.asList(REMOVE_PATHS);
        }
        return m_xpaths;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Arrays.asList(UPDATE_PATHS);
    }
}

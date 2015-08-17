/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_CONFIGURATION;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_KEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NAME;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NEWELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_URI;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_VISIBLE;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A parser class which reads data from inherited container configuration files.<p>
 */
public class CmsContainerConfigurationParser {

    /** The internal CMS context used. */
    private final CmsObject m_cms;

    /** The configuration group which is currently being read. */
    private Map<String, CmsContainerConfiguration> m_currentConfigurationGroup;

    /** The locale which is currently being read. */
    private Locale m_currentLocale;

    /** The parse results. */
    private final Map<Locale, Map<String, CmsContainerConfiguration>> m_results;

    /**
     * Creates a new configuration parser.<p>
     *
     * @param cms the current CMS context
     */
    public CmsContainerConfigurationParser(CmsObject cms) {

        m_cms = cms;
        m_results = new HashMap<Locale, Map<String, CmsContainerConfiguration>>();
    }

    /**
     * Gets the parsed results as a map.<p>
     *
     * @return the parse results
     */
    public Map<Locale, Map<String, CmsContainerConfiguration>> getParsedResults() {

        return m_results;
    }

    /**
     * Parses the contents of a file.<p>
     *
     * @param file the file to parse
     *
     * @throws CmsException if something goes wrong
     */
    public void parse(CmsFile file) throws CmsException {

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        for (Locale locale : content.getLocales()) {
            m_currentLocale = locale;
            CmsXmlContentRootLocation rootLocation = new CmsXmlContentRootLocation(content, locale);
            parseConfigurationGroup(rootLocation);
        }
    }

    /**
     * Parses the contents of a resource.<p>
     *
     * @param resource the resource which should be parsed
     *
     * @throws CmsException if something goes wrong
     */
    public void parse(CmsResource resource) throws CmsException {

        CmsFile file = m_cms.readFile(resource);
        parse(file);
    }

    /**
     * Parses a group of named configurations from a given XML content location.<p>
     *
     * @param location the location from which to read the configuration group
     */
    protected void parseConfigurationGroup(I_CmsXmlContentLocation location) {

        List<I_CmsXmlContentValueLocation> locations = location.getSubValues(N_CONFIGURATION);
        m_currentConfigurationGroup = new HashMap<String, CmsContainerConfiguration>();
        m_results.put(m_currentLocale, m_currentConfigurationGroup);
        for (I_CmsXmlContentValueLocation configLocation : locations) {
            parseSingleConfiguration(configLocation);
        }
    }

    /**
     * Parses a single inheritance configuration from an XML content node.<p>
     *
     * @param location the node from which to read the single configuration
     */
    protected void parseSingleConfiguration(I_CmsXmlContentValueLocation location) {

        I_CmsXmlContentValueLocation nameLoc = location.getSubValue(N_NAME);
        if (nameLoc == null) {
            return;
        }
        String name = nameLoc.asString(m_cms);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            return;
        }
        List<String> ordering = null;
        List<I_CmsXmlContentValueLocation> orderKeyLocs = location.getSubValues(N_ORDERKEY);
        if (orderKeyLocs != null) {
            ordering = new ArrayList<String>();
            for (I_CmsXmlContentValueLocation orderKeyLoc : orderKeyLocs) {
                String orderKey = orderKeyLoc.asString(m_cms);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(orderKey)) {
                    ordering.add(orderKey.trim());
                }
            }
        }
        Map<String, Boolean> visibilities = new HashMap<String, Boolean>();
        List<I_CmsXmlContentValueLocation> visibleLocs = location.getSubValues(N_VISIBLE);
        for (I_CmsXmlContentValueLocation visibleLoc : visibleLocs) {
            String visibleStr = visibleLoc.asString(m_cms);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(visibleStr)) {
                visibilities.put(visibleStr.trim(), Boolean.TRUE);
            }
        }

        List<I_CmsXmlContentValueLocation> hiddenLocs = location.getSubValues(N_HIDDEN);
        for (I_CmsXmlContentValueLocation hiddenLoc : hiddenLocs) {
            String hiddenStr = hiddenLoc.asString(m_cms);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(hiddenStr)) {
                visibilities.put(hiddenStr.trim(), Boolean.FALSE);
            }
        }

        List<I_CmsXmlContentValueLocation> newElementLocs = location.getSubValues(N_NEWELEMENT);
        Map<String, CmsContainerElementBean> newElementBeans = new HashMap<String, CmsContainerElementBean>();
        for (I_CmsXmlContentValueLocation elementLoc : newElementLocs) {
            I_CmsXmlContentValueLocation keyLoc = elementLoc.getSubValue(N_KEY);
            String key = keyLoc.asString(m_cms).trim();
            I_CmsXmlContentValueLocation actualElementLoc = elementLoc.getSubValue(N_ELEMENT);
            I_CmsXmlContentValueLocation uriLoc = actualElementLoc.getSubValue(N_URI);
            CmsUUID structureId = uriLoc.asId(m_cms);
            if (structureId != null) {
                Map<String, String> settings = CmsXmlContentPropertyHelper.readProperties(m_cms, actualElementLoc);
                CmsContainerElementBean newElementBean = new CmsContainerElementBean(
                    structureId,
                    CmsUUID.getNullUUID(),
                    settings,
                    false);
                newElementBeans.put(key, newElementBean);
            }
        }
        CmsContainerConfiguration config = new CmsContainerConfiguration(ordering, visibilities, newElementBeans);
        m_currentConfigurationGroup.put(name, config);
    }

}

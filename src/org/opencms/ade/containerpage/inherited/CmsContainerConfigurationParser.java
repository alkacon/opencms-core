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

import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_HIDDEN;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NAME;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_NEWELEMENT;
import static org.opencms.ade.containerpage.inherited.CmsContainerConfiguration.N_ORDERKEY;
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

public class CmsContainerConfigurationParser {

    private final CmsObject m_cms;
    private Map<String, CmsContainerConfiguration> m_currentConfigurationGroup;
    private Locale m_currentLocale;
    private CmsResource m_resource;
    private final Map<Locale, Map<String, CmsContainerConfiguration>> m_results;

    public CmsContainerConfigurationParser(CmsObject cms) {

        m_cms = cms;
        m_results = new HashMap<Locale, Map<String, CmsContainerConfiguration>>();
    }

    public Map<Locale, Map<String, CmsContainerConfiguration>> getParsedResults() {

        return m_results;
    }

    public void parse(CmsFile file) throws CmsException {

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        m_resource = file;
        for (Locale locale : content.getLocales()) {
            m_currentLocale = locale;
            CmsXmlContentRootLocation rootLocation = new CmsXmlContentRootLocation(content, locale);
            parseConfigurationGroup(rootLocation);
        }
    }

    public void parse(CmsResource resource) throws CmsException {

        m_resource = resource;
        CmsFile file = m_cms.readFile(resource);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        for (Locale locale : content.getLocales()) {
            m_currentLocale = locale;
            CmsXmlContentRootLocation rootLocation = new CmsXmlContentRootLocation(content, locale);
            parseConfigurationGroup(rootLocation);
        }
    }

    /**
     * Parses a group of named configurations from a given XML content location.
     * <p>
     * 
     * @param location
     *            the location from which to read the configuration group
     */
    protected void parseConfigurationGroup(I_CmsXmlContentLocation location) {

        List<I_CmsXmlContentValueLocation> locations = location.getSubValues("Configuration");
        m_currentConfigurationGroup = new HashMap<String, CmsContainerConfiguration>();
        m_results.put(m_currentLocale, m_currentConfigurationGroup);
        for (I_CmsXmlContentValueLocation configLocation : locations) {
            parseSingleConfiguration(configLocation);
        }
    }

    /**
     * Parses a single inheritance configuration from an XML content node.
     * <p>
     * 
     * @param location
     *            the node from which to read the single configuration
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
                ordering.add(orderKeyLoc.asString(m_cms));
            }
        }
        Map<String, Boolean> visibilities = new HashMap<String, Boolean>();
        List<I_CmsXmlContentValueLocation> visibleLocs = location.getSubValues(N_VISIBLE);
        for (I_CmsXmlContentValueLocation visibleLoc : visibleLocs) {
            String visibleStr = visibleLoc.asString(m_cms).trim();
            visibilities.put(visibleStr, Boolean.TRUE);
        }

        List<I_CmsXmlContentValueLocation> hiddenLocs = location.getSubValues(N_HIDDEN);
        for (I_CmsXmlContentValueLocation hiddenLoc : hiddenLocs) {
            String hiddenStr = hiddenLoc.asString(m_cms).trim();
            visibilities.put(hiddenStr, Boolean.FALSE);
        }

        List<I_CmsXmlContentValueLocation> newElementLocs = location.getSubValues(N_NEWELEMENT);
        Map<String, CmsContainerElementBean> newElementBeans = new HashMap<String, CmsContainerElementBean>();
        for (I_CmsXmlContentValueLocation elementLoc : newElementLocs) {
            I_CmsXmlContentValueLocation keyLoc = elementLoc.getSubValue("Key");
            String key = keyLoc.asString(m_cms).trim();
            I_CmsXmlContentValueLocation actualElementLoc = elementLoc.getSubValue("Element");
            I_CmsXmlContentValueLocation uriLoc = actualElementLoc.getSubValue("Uri");
            CmsUUID structureId = uriLoc.asId(m_cms);
            Map<String, String> settings = CmsXmlContentPropertyHelper.readProperties(m_cms, actualElementLoc);
            CmsContainerElementBean newElementBean = new CmsContainerElementBean(
                structureId,
                CmsUUID.getNullUUID(),
                settings,
                false);
            newElementBeans.put(key, newElementBean);
        }
        CmsContainerConfiguration config = new CmsContainerConfiguration(ordering, visibilities, newElementBeans);
        m_currentConfigurationGroup.put(name, config);
    }

}

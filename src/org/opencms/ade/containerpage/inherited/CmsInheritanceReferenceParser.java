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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parser class for parsing inheritance container references.<p>
 */
public class CmsInheritanceReferenceParser {

    /** The CMS context used for parsing the file. */
    private CmsObject m_cms;

    /** The map of parsed references. */
    private Map<Locale, CmsInheritanceReference> m_references = new HashMap<Locale, CmsInheritanceReference>();

    /** The resource being parsed. */
    private CmsResource m_resource;

    /**
     * Creates a new parser instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsInheritanceReferenceParser(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Gets the parsed reference for a locale.<p>
     *
     * Gets the reference object for the locale, and uses the reference for the English language as a fallback.<p>
     *
     * @param  locale the locale to get the reference for
     * @return the reference for the locale
     */
    public CmsInheritanceReference getReference(Locale locale) {

        CmsInheritanceReference ref = m_references.get(locale);
        if (ref == null) {
            ref = m_references.get(Locale.ENGLISH);
        }
        return ref;
    }

    /**
     * Parses a given resource.<p>
     *
     * @param resource the resource to parse
     *
     * @throws CmsException if something goes wrong
     */
    public void parse(CmsResource resource) throws CmsException {

        CmsFile file = m_cms.readFile(resource);
        m_resource = resource;
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, file);
        parse(content);
    }

    /**
     * Parses the given XML content.<p>
     *
     * @param content the XML content to parse
     */
    protected void parse(CmsXmlContent content) {

        List<Locale> availableLocales = content.getLocales();
        for (Locale locale : availableLocales) {
            CmsXmlContentRootLocation location = new CmsXmlContentRootLocation(content, locale);
            CmsInheritanceReference ref = parseReference(location, locale);
            if (ref != null) {
                m_references.put(locale, ref);
            }
        }
    }

    /**
     * Extracts a single inheritance reference from a location in the XML content.<p>
     *
     * This method may return null if the given location doesn't contain a valid inheritance container reference.<p>
     *
     * @param location the location from which to parse the inheritance reference
     * @param locale the locale from  which to parse the inheritance reference
     *
     * @return the parsed inheritance reference, or null
     */
    protected CmsInheritanceReference parseReference(I_CmsXmlContentLocation location, Locale locale) {

        I_CmsXmlContentValueLocation nameLocation = location.getSubValue("ConfigName");
        if (nameLocation == null) {
            return null;
        }
        String configName = nameLocation.asString(m_cms);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(configName)) {
            return null;
        }
        configName = configName.trim();
        I_CmsXmlContentValueLocation titleLocation = location.getSubValue("Title");
        String title = null;
        if (titleLocation != null) {
            title = titleLocation.asString(m_cms);
        }
        I_CmsXmlContentValueLocation descriptionLocation = location.getSubValue("Description");
        String description = null;
        if (descriptionLocation != null) {
            description = descriptionLocation.asString(m_cms);
        }
        return new CmsInheritanceReference(configName, title, description, m_resource, locale);
    }

}

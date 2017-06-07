/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.I_CmsFileNameGenerator;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A class which represents the context for resolving all content value mappings of an XML content.<p>
 *
 * Since the content handler instance is shared between all contents of the same XML content type, we can't use
 * it to store data which is only relevant for resolving the mappings of a single XML content, so this class was created.
 */
public class CmsMappingResolutionContext {

    /**
     * Internal bean used to keep track of URL name mappings.<p>
     */
    class InternalUrlNameMappingEntry {

        /** Locale of the mapping. */
        private Locale m_locale;

        /** URL name of the mapping. */
        private String m_name;

        /** Structure ID of the mapping. */
        private CmsUUID m_structureId;

        /**
         * Creates a new instance.<p>
         *
         * @param structureId the structure id
         * @param name the URL name
         * @param locale the locale
         */
        public InternalUrlNameMappingEntry(CmsUUID structureId, String name, Locale locale) {

            m_name = name;
            m_structureId = structureId;
            m_locale = locale;
        }

        /**
         * Returns the locale.<p>
         *
         * @return the locale
         */
        public Locale getLocale() {

            return m_locale;
        }

        /**
         * Returns the name.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the structureId.<p>
         *
         * @return the structureId
         */
        public CmsUUID getStructureId() {

            return m_structureId;
        }

    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMappingResolutionContext.class);

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The list of URL name mappings. */
    private List<InternalUrlNameMappingEntry> m_urlNameMappingEntries = Lists.newArrayList();

    /**
     * Creates a new instance.<p>
     */
    public CmsMappingResolutionContext() {

        // empty
    }

    /**
     * Writes all the stored URL name mappings to the database.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void commitUrlNameMappings() throws CmsException {

        Set<CmsUUID> structureIds = Sets.newHashSet();
        for (InternalUrlNameMappingEntry entry : m_urlNameMappingEntries) {
            structureIds.add(entry.getStructureId());
        }

        boolean urlnameReplace = false;
        for (CmsUUID structureId : structureIds) {
            try {
                CmsResource resource = m_cms.readResource(structureId, CmsResourceFilter.ALL);
                CmsProperty prop = m_cms.readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_URLNAME_REPLACE,
                    true);
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(prop.getValue())) {
                    urlnameReplace = Boolean.parseBoolean(prop.getValue());
                }
            } catch (CmsException e) {
                LOG.error("Error while trying to read urlname.replace: " + e.getLocalizedMessage(), e);
            }

        }

        I_CmsFileNameGenerator nameGen = OpenCms.getResourceManager().getNameGenerator();
        for (InternalUrlNameMappingEntry entry : m_urlNameMappingEntries) {
            Iterator<String> nameSeq = nameGen.getUrlNameSequence(entry.getName());
            m_cms.writeUrlNameMapping(nameSeq, entry.getStructureId(), entry.getLocale().toString(), urlnameReplace);
        }

    }

    /**
     * Finalizes the mappings.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void finalizeMappings() throws CmsException {

        commitUrlNameMappings();
    }

    /**
     * Sets the CMS context to use.<p>
     *
     * @param cms the CMS context
     */
    public void setCmsObject(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Adds an URL name mapping which should be written to the database later.<p>
     *
     * @param name the mapping name
     * @param locale the locale
     * @param structureId the structure ID
     */
    void addUrlNameMapping(String name, Locale locale, CmsUUID structureId) {

        m_urlNameMappingEntries.add(new InternalUrlNameMappingEntry(structureId, name, locale));
    }
}

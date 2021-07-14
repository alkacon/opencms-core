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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.I_CmsFileNameGenerator;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
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
     * The attribute type.
     */
    public enum AttributeType {
        expiration, release;
    }

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

    /** The content being processed. */
    private CmsXmlContent m_content;

    /** Stored expiration dates. */
    private Map<Locale, Long> m_dateExpired = new HashMap<>();

    /** Stored release dates. */
    private Map<Locale, Long> m_dateReleased = new HashMap<>();

    /** True if the schema for the content has attribute mappings. */
    private boolean m_hasAttributeMappings;

    /** The list of URL name mappings. */
    private List<InternalUrlNameMappingEntry> m_urlNameMappingEntries = Lists.newArrayList();

    /**
     * Creates a new instance.<p>
     * @param content the xml content
     * @param hasAttributeMappings true if the schema has attribute mappings
     */
    public CmsMappingResolutionContext(CmsXmlContent content, boolean hasAttributeMappings) {

        m_content = content;
        m_hasAttributeMappings = hasAttributeMappings;
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
        if (m_hasAttributeMappings) {
            // we do not want to change manually set attributes in case there are no attribute mappings
            writeAttributes();
        }
    }

    /**
     * Stores the mapped expiration date for the given locale.
     *
     * @param locale the locale
     * @param expiration the expiration date
     */
    public void putExpirationDate(Locale locale, long expiration) {

        m_dateExpired.put(locale, Long.valueOf(expiration));
    }

    /**
     * Stores the mapped release date for the given locale.
     *
     * @param locale the locale
     * @param release the release date
     */
    public void putReleaseDate(Locale locale, long release) {

        m_dateReleased.put(locale, Long.valueOf(release));
    }

    /**
     * Helper method for setting release/expiration date.
     *
     * <p>Needs to also set the attributes on the resource of m_content because it's written later by the content handler.
     *
     * @param res the resource to set
     * @param type the attribute type
     * @param value the value to set (null for default value)
     *
     * @throws CmsException if something goes wrong
     */
    public void setAttribute(CmsResource res, AttributeType type, Long value) throws CmsException {

        if (type == AttributeType.release) {
            long actualValue = value != null ? value.longValue() : 0;
            m_cms.setDateReleased(res, actualValue, false);
            if ((m_content.getFile() != null) && res.getStructureId().equals(m_content.getFile().getStructureId())) {
                m_content.getFile().setDateReleased(actualValue);
            }
        } else if (type == AttributeType.expiration) {
            long actualValue = value != null ? value.longValue() : Long.MAX_VALUE;
            m_cms.setDateExpired(res, actualValue, false);
            if ((m_content.getFile() != null) && res.getStructureId().equals(m_content.getFile().getStructureId())) {
                m_content.getFile().setDateExpired(actualValue);
            }
        }
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
     * Writes the mapped attributes.
     */
    protected void writeAttributes() {

        String p = "[" + RandomStringUtils.randomAlphanumeric(6) + "] ";
        CmsFile file = m_content.getFile();
        LOG.info(p + "Processing attributes for " + file.getRootPath());
        try {
            for (CmsResource sibling : m_cms.readSiblings(file, CmsResourceFilter.IGNORE_EXPIRATION)) {
                LOG.info(p + "Processing sibling " + sibling.getRootPath());
                try {
                    CmsProperty localeProp = m_cms.readPropertyObject(
                        sibling,
                        CmsPropertyDefinition.PROPERTY_LOCALE,
                        true);
                    List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales();
                    if (!localeProp.isNullProperty()) {
                        String localeStr = localeProp.getValue();
                        List<Locale> tempLocales = CmsLocaleManager.getLocales(localeStr);
                        if (!tempLocales.isEmpty()) {
                            locales = tempLocales;
                        }
                    }
                    LOG.info(p + "Using locale precedence " + locales);
                    boolean foundLocale = false;
                    for (Locale locale : locales) {
                        // Use first locale from the property which is actually in the content, whether it has the values or not
                        // (if it doesn't have them, i.e. null is passed to setAttribute, the default values wlll be set)
                        if (m_content.hasLocale(locale)) {
                            LOG.info(p + "Mapping attributes from locale " + locale);
                            setAttribute(sibling, AttributeType.release, m_dateReleased.get(locale));
                            setAttribute(sibling, AttributeType.expiration, m_dateExpired.get(locale));
                            foundLocale = true;
                            break;
                        }
                    }
                    if (!foundLocale) {
                        LOG.info(p + "No mapping locale found, resetting attributes");
                        setAttribute(sibling, AttributeType.release, null);
                        setAttribute(sibling, AttributeType.expiration, null);
                    }
                } catch (CmsException e) {
                    LOG.error(p + e.getLocalizedMessage(), e);
                }
            }

        } catch (Exception e) {
            LOG.error(p + e.getLocalizedMessage(), e);
        }
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

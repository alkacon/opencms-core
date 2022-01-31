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

package org.opencms.ade.galleries;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlVfsFileValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Replacement configuration for the 'add content' dialog.
 *
 * <p>A replacement configuration contains a list of entries, one for each type,
 * with a list of resources for each type and optionally a title string for each of the resources.
 * The list of resources replaces the search results that would be normally shown for the type
 * in the 'add content' dialog.
 */
public class CmsAddContentRestriction {

    /**
     * Contains the replacements (and titles of the replacements) for a single type.
     */
    public static class TypeEntry {

        /** The location from which this entry was read. */
        private String m_origin;

        /** The replacement titles for the search results, with the corresponding structure ids as keys. */
        private Map<CmsUUID, String> m_replacedTitles = new HashMap<>();

        /** The resources to replace the search result for the configured type with. */
        private List<CmsResource> m_resources = new ArrayList<>();

        /** The name of the resource type. */
        private String m_type;

        /**
         * Creates a new entry.
         *
         * @param type the name of the resource type
         * @param resources the resources to use as a replacement
         * @param titleReplacements a map from structure ids to replacement titles for those
         * @param origin the place from which this entry was read
         */
        public TypeEntry(
            String type,
            List<CmsResource> resources,
            Map<CmsUUID, String> titleReplacements,
            String origin) {

            m_type = type;
            m_resources = new ArrayList<>(resources);
            m_replacedTitles = new HashMap<>(titleReplacements);
            m_origin = origin;
        }

        /**
         * Gets the location from which this entry was read.
         *
         * @return the location from which the entry was read
         */
        public String getOrigin() {

            return m_origin;
        }

        /**
         * Gets the results to be displayed in the 'add content' dialog.
         *
         * @param cms the CMS context
         * @return the results to display
         */
        @SuppressWarnings("synthetic-access")
        public List<CmsGallerySearchResult> getResults(CmsObject cms) {

            List<CmsGallerySearchResult> result = new ArrayList<>();
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            CmsMacroResolver macroResolver = new CmsMacroResolver();
            macroResolver.setCmsObject(cms);
            macroResolver.setMessages(OpenCms.getWorkplaceManager().getMessages(locale));

            for (CmsResource res : m_resources) {
                try {
                    // Read resources again because the user may not be allowed to access them,
                    // or they may have been moved since the replacement configuration was read
                    CmsResource currentRes = cms.readResource(
                        res.getStructureId(),
                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);

                    CmsGallerySearchResult singleResult = CmsGallerySearch.searchById(
                        cms,
                        res.getStructureId(),
                        locale);
                    String replacementTitle = m_replacedTitles.get(currentRes.getStructureId());
                    if (replacementTitle != null) {
                        replacementTitle = macroResolver.resolveMacros(replacementTitle);
                        singleResult = singleResult.withTitle(replacementTitle);
                    }
                    result.add(singleResult);
                } catch (CmsVfsResourceNotFoundException | CmsSecurityException e) {
                    LOG.debug("filtered resource " + res.getRootPath() + " (" + res.getStructureId() + ")");
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return result;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {

            return m_type;
        }
    }

    /** Empty configuration. */
    public static final CmsAddContentRestriction EMPTY = new CmsAddContentRestriction();

    /** XML node name. */
    public static final String N_ENTRY = "Entry";

    /** XML node name. */
    public static final String N_RESOURCE = "Resource";

    /** XML node name. */
    public static final String N_TITLE = "Title";

    /** XML node name. */
    public static final String N_TYPE = "Type";

    /** The name of the resource type from which the configuration is read. */
    public static final String TYPE_NAME = "add_content_replacement";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAddContentRestriction.class);

    /** The configuration entries, with their types as keys. */
    private Map<String, TypeEntry> m_entries = new HashMap<>();

    /**
     * Creates a new configuration from a list of entries.
     *
     * @param entries the list of configuration entries
     */
    public CmsAddContentRestriction(Collection<TypeEntry> entries) {

        for (TypeEntry replacer : entries) {
            m_entries.put(replacer.getType(), replacer);
        }
    }

    /**
     * Creates an empty configuration where nothing is replaced.
     */
    private CmsAddContentRestriction() {

        // do nothing
    }

    /**
     * Reads a content restriction from an XML content.
     *
     * @param cms the CMS context
     * @param parent the parent location
     * @param nodeName the name of the XML elements containing the content restrictions
     *
     * @return the content restriction
     */
    public static CmsAddContentRestriction read(CmsObject cms, I_CmsXmlContentLocation parent, String nodeName) {

        List<TypeEntry> entries = new ArrayList<>();
        for (I_CmsXmlContentValueLocation entryLoc : parent.getSubValues(nodeName)) {
            TypeEntry entry = readEntry(cms, entryLoc);
            entries.add(entry);
        }
        return new CmsAddContentRestriction(entries);

    }

    /**
     * Reads the entry for a single type from the configuration.
     *
     * @param cms the CMS context
     * @param location the location from which to read the entry
     *
     * @return the  entry that was read
     */
    public static TypeEntry readEntry(CmsObject cms, I_CmsXmlContentValueLocation location) {

        String type = location.getSubValue(N_TYPE).getValue().getStringValue(cms).trim();
        Map<CmsUUID, String> titleMap = new HashMap<>();
        List<CmsResource> resourceList = new ArrayList<>();
        for (I_CmsXmlContentValueLocation entryLoc : location.getSubValues(N_ENTRY)) {
            CmsXmlVfsFileValue resoureValue = (CmsXmlVfsFileValue)(entryLoc.getSubValue(N_RESOURCE).getValue());
            CmsResource resource = resoureValue.getLink(cms).getResource();
            if (resource != null) {
                resourceList.add(resource);
                I_CmsXmlContentValueLocation titleLoc = entryLoc.getSubValue(N_TITLE);
                if (titleLoc != null) {
                    String title = titleLoc.getValue().getStringValue(cms);
                    titleMap.put(resource.getStructureId(), title);
                }
            }
        }
        return new TypeEntry(type, resourceList, titleMap, location.getValue().getDocument().getFile().getRootPath());
    }

    /**
     * Gets the replaced results for a specific resource type.
     *
     * @param cms the CMS context
     * @param type the type name
     *
     * @return the replacement results for the given type
     */
    public List<CmsGallerySearchResult> getResult(CmsObject cms, String type) {

        TypeEntry replacer = m_entries.get(type);
        if (replacer == null) {
            return null;
        }
        return replacer.getResults(cms);
    }

    /**
     * Gets the set of names of types for which the search results are replaced.
     *
     * @return the set of types for which there are replacements configured
     */
    public Set<String> getTypes() {

        return Collections.unmodifiableSet(m_entries.keySet());
    }

    /**
     * Merges this configuration and a child configuration into a new configuration object, where an entry for a type in the child
     * overrides an entry for the same type in the parent.
     *
     * @param child the child configuration
     * @return the merged configuration
     */
    public CmsAddContentRestriction merge(CmsAddContentRestriction child) {

        Map<String, TypeEntry> combinedMap = new HashMap<>();
        combinedMap.putAll(m_entries);
        combinedMap.putAll(child.m_entries);
        return new CmsAddContentRestriction(combinedMap.values());

    }

}

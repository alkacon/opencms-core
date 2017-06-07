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

package org.opencms.xml.containerpage;

import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents a formatter configuration.<p>
 *
 * A formatter configuration can be either defined in the XML schema XSD of a XML content,
 * or in a special sitemap configuration file.<p>
 *
 * @since 8.0.0
 */
public final class CmsFormatterConfiguration {

    /**
     * This class is used to sort lists of formatter beans in order of importance.<p>
     */
    public static class FormatterComparator implements Comparator<I_CmsFormatterBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsFormatterBean first, I_CmsFormatterBean second) {

            return ComparisonChain.start().compare(second.getRank(), first.getRank()).compare(
                second.isTypeFormatter() ? 1 : 0,
                first.isTypeFormatter() ? 1 : 0).compare(second.getMinWidth(), first.getMinWidth()).result();
        }
    }

    /**
     * Predicate which checks whether the given formatter is a detail formatter.<p>
     */
    public static class IsDetail implements Predicate<I_CmsFormatterBean> {

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        public boolean apply(I_CmsFormatterBean formatter) {

            return formatter.isDetailFormatter();
        }
    }

    /**
     * Predicate which checks whether the given formatter is a display formatter.<p>
     */
    public static class IsDisplay implements Predicate<I_CmsFormatterBean> {

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        public boolean apply(I_CmsFormatterBean formatter) {

            return formatter.isDisplayFormatter();
        }
    }

    /**
     * Predicate to check whether the formatter is from a schema.<p>
     */
    public static class IsSchemaFormatter implements Predicate<I_CmsFormatterBean> {

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        public boolean apply(I_CmsFormatterBean formatter) {

            return !formatter.isFromFormatterConfigFile();

        }
    }

    /**
     * Predicate which checks whether a formatter matches the given container type or width.<p>
     */
    private class MatchesTypeOrWidth implements Predicate<I_CmsFormatterBean> {

        /** If nested containers are allowed. */
        private boolean m_allowNested;

        /** The set of container types to match. */
        private Set<String> m_types = Sets.newHashSet();

        /** The container width. */
        private int m_width;

        /**
         * Creates a new matcher instance.<p>
         *
         * @param type the container type
         * @param width the container width
         * @param allowNested if nested containers are allowed
         */
        public MatchesTypeOrWidth(String type, int width, boolean allowNested) {

            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(type)) {
                // split with comma and optionally spaces to the left/right of the comma as separator
                m_types.addAll(Arrays.asList(type.trim().split(" *, *")));
            }
            m_width = width;
            m_allowNested = allowNested;
        }

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        public boolean apply(I_CmsFormatterBean formatter) {

            return matchFormatter(formatter, m_types, m_width, m_allowNested);
        }
    }

    /** The empty formatter configuration. */
    public static final CmsFormatterConfiguration EMPTY_CONFIGURATION = new CmsFormatterConfiguration(null, null);

    /** The log instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsFormatterConfiguration.class);

    /** The container width to match all width configured formatters. */
    public static final int MATCH_ALL_CONTAINER_WIDTH = -2;

    /** CmsObject used to read the JSP resources configured in the XSD schema. */
    private static CmsObject m_adminCms;

    /** All formatters that have been added to this configuration. */
    private List<I_CmsFormatterBean> m_allFormatters;

    /** The available display formatters. */
    private List<I_CmsFormatterBean> m_displayFormatters;

    /** Cache for the searchContent option. */
    private Map<CmsUUID, Boolean> m_searchContent = Maps.newHashMap();

    /**
     * Creates a new formatter configuration based on the given list of formatters.<p>
     *
     * @param cms the current users OpenCms context
     * @param formatters the list of configured formatters
     */
    private CmsFormatterConfiguration(CmsObject cms, List<I_CmsFormatterBean> formatters) {

        if (formatters == null) {
            // this is needed for the empty configuration
            m_allFormatters = Collections.emptyList();
        } else {
            m_allFormatters = new ArrayList<I_CmsFormatterBean>(formatters);
        }
        init(cms, m_adminCms);
    }

    /**
     * Returns the formatter configuration for the current project based on the given list of formatters.<p>
     *
     * @param cms the current users OpenCms context, required to know which project to read the JSP from
     * @param formatters the list of configured formatters
     *
     * @return the formatter configuration for the current project based on the given list of formatters
     */
    public static CmsFormatterConfiguration create(CmsObject cms, List<I_CmsFormatterBean> formatters) {

        if ((formatters != null) && (formatters.size() > 0) && (cms != null)) {
            return new CmsFormatterConfiguration(cms, formatters);
        } else {
            return EMPTY_CONFIGURATION;
        }
    }

    /**
     * Initialize the formatter configuration.<p>
     *
     * @param cms an initialized admin OpenCms user context
     *
     * @throws CmsException in case the initialization fails
     */
    public static void initialize(CmsObject cms) throws CmsException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ADMINISTRATOR);
        try {
            // store the Admin cms to index Cms resources
            m_adminCms = OpenCms.initCmsObject(cms);
            m_adminCms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            // this should never happen
        }
    }

    /**
     * Checks whether the given formatter bean matches the container types, width and nested flag.<p>
     *
     * @param formatter the formatter bean
     * @param types the container types
     * @param width the container width
     * @param allowNested whether nested containers are allowed
     *
     * @return <code>true</code> in case the formatter matches
     */
    public static boolean matchFormatter(
        I_CmsFormatterBean formatter,
        Set<String> types,
        int width,
        boolean allowNested) {

        if (!allowNested && formatter.hasNestedContainers()) {
            return false;
        }
        if (formatter.isMatchAll()) {
            return true;
        }
        if (formatter.isTypeFormatter()) {
            return !Sets.intersection(types, formatter.getContainerTypes()).isEmpty();
        } else {
            return (width == MATCH_ALL_CONTAINER_WIDTH)
                || ((formatter.getMinWidth() <= width) && (width <= formatter.getMaxWidth()));
        }
    }

    /**
     * Gets a list of all defined formatters.<p>
     *
     * @return the list of all formatters
     */
    public List<I_CmsFormatterBean> getAllFormatters() {

        return new ArrayList<I_CmsFormatterBean>(m_allFormatters);
    }

    /**
     * Gets the formatters which are available for the given container type and width.<p>
     *
     * @param containerTypes the container types (comma separated)
     * @param containerWidth the container width
     * @param allowNested if nested containers are allowed
     *
     * @return the list of available formatters
     */
    public List<I_CmsFormatterBean> getAllMatchingFormatters(
        String containerTypes,
        int containerWidth,
        boolean allowNested) {

        return new ArrayList<I_CmsFormatterBean>(
            Collections2.filter(m_allFormatters, new MatchesTypeOrWidth(containerTypes, containerWidth, allowNested)));

    }

    /**
     * Selects the best matching formatter for the provided type and width from this configuration.<p>
     *
     * This method first tries to find the formatter for the provided container type.
     * If this fails, it returns the width based formatter that matched the container width.<p>
     *
     * @param containerTypes the container types (comma separated)
     * @param containerWidth the container width
     * @param allowNested if nested containers are allowed
     *
     * @return the matching formatter, or <code>null</code> if none was found
     */
    public I_CmsFormatterBean getDefaultFormatter(
        final String containerTypes,
        final int containerWidth,
        final boolean allowNested) {

        Optional<I_CmsFormatterBean> result = Iterables.tryFind(
            m_allFormatters,
            new MatchesTypeOrWidth(containerTypes, containerWidth, allowNested));
        return result.orNull();
    }

    /**
     * Selects the best matching schema formatter for the provided type and width from this configuration.<p>
     *
     * @param containerTypes the container types (comma separated)
     * @param containerWidth the container width
     *
     * @return the matching formatter, or <code>null</code> if none was found
     */
    public I_CmsFormatterBean getDefaultSchemaFormatter(final String containerTypes, final int containerWidth) {

        Optional<I_CmsFormatterBean> result = Iterables.tryFind(
            m_allFormatters,
            Predicates.and(new IsSchemaFormatter(), new MatchesTypeOrWidth(containerTypes, containerWidth, false)));
        return result.orNull();
    }

    /**
     * Gets the detail formatter to use for the given type and container width.<p>
     *
     * @param types the container types (comma separated)
     * @param containerWidth the container width
     *
     * @return the detail formatter to use
     */
    public I_CmsFormatterBean getDetailFormatter(String types, int containerWidth) {

        // detail formatters must still match the type or width
        Predicate<I_CmsFormatterBean> checkValidDetailFormatter = Predicates.and(
            new MatchesTypeOrWidth(types, containerWidth, true),
            new IsDetail());
        Optional<I_CmsFormatterBean> result = Iterables.tryFind(m_allFormatters, checkValidDetailFormatter);
        return result.orNull();
    }

    /**
     * Gets all detail formatters.<p>
     *
     * @return the detail formatters
     */
    public Collection<I_CmsFormatterBean> getDetailFormatters() {

        return Collections.<I_CmsFormatterBean> unmodifiableCollection(
            Collections2.filter(m_allFormatters, new IsDetail()));
    }

    /**
     * Returns the display formatter for this type.<p>
     *
     * @return the display formatter
     */
    public I_CmsFormatterBean getDisplayFormatter() {

        if (!getDisplayFormatters().isEmpty()) {
            return getDisplayFormatters().get(0);
        }
        return null;
    }

    /**
     * Returns the available display formatters.<p>
     *
     * @return the display formatters
     */
    public List<I_CmsFormatterBean> getDisplayFormatters() {

        if (m_displayFormatters == null) {
            List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>(
                Collections2.filter(m_allFormatters, new IsDisplay()));
            if (formatters.size() > 1) {
                Collections.sort(formatters, new Comparator<I_CmsFormatterBean>() {

                    public int compare(I_CmsFormatterBean o1, I_CmsFormatterBean o2) {

                        return o1.getRank() == o2.getRank() ? 0 : (o1.getRank() < o2.getRank() ? -1 : 1);
                    }
                });
            }
            m_displayFormatters = Collections.unmodifiableList(formatters);
        }
        return m_displayFormatters;
    }

    /**
     * Returns the formatters available for selection for the given container type and width.<p>
     *
     * @param containerTypes the container types (comma separated)
     * @param containerWidth the container width
     * @param allowNested if nested containers are allowed
     *
     * @return the list of available formatters
     */
    public Map<String, I_CmsFormatterBean> getFormatterSelection(
        String containerTypes,
        int containerWidth,
        boolean allowNested) {

        Map<String, I_CmsFormatterBean> result = new LinkedHashMap<String, I_CmsFormatterBean>();
        for (I_CmsFormatterBean formatter : Collections2.filter(
            m_allFormatters,
            new MatchesTypeOrWidth(containerTypes, containerWidth, allowNested))) {
            if (formatter.isFromFormatterConfigFile()) {
                result.put(formatter.getId(), formatter);
            } else {
                result.put(
                    CmsFormatterConfig.SCHEMA_FORMATTER_ID + formatter.getJspStructureId().toString(),
                    formatter);
            }
        }
        return result;
    }

    /**
     * Returns the formatter from this configuration that is to be used for the preview in the ADE gallery GUI,
     * or <code>null</code> if there is no preview formatter configured.<p>
     *
     * @return the formatter from this configuration that is to be used for the preview in the ADE gallery GUI,
     * or <code>null</code> if there is no preview formatter configured
     */
    public I_CmsFormatterBean getPreviewFormatter() {

        Optional<I_CmsFormatterBean> result;
        result = Iterables.tryFind(m_allFormatters, new Predicate<I_CmsFormatterBean>() {

            public boolean apply(I_CmsFormatterBean formatter) {

                return formatter.isPreviewFormatter();
            }
        });
        if (!result.isPresent()) {
            result = Iterables.tryFind(m_allFormatters, new Predicate<I_CmsFormatterBean>() {

                public boolean apply(I_CmsFormatterBean formatter) {

                    if (formatter.isTypeFormatter()) {
                        return formatter.getContainerTypes().contains(CmsFormatterBean.PREVIEW_TYPE);
                    } else {
                        return (formatter.getMinWidth() <= CmsFormatterBean.PREVIEW_WIDTH)
                            && (CmsFormatterBean.PREVIEW_WIDTH <= formatter.getMaxWidth());
                    }
                }
            });
        }
        if (!result.isPresent()) {
            result = Iterables.tryFind(m_allFormatters, new Predicate<I_CmsFormatterBean>() {

                public boolean apply(I_CmsFormatterBean formatter) {

                    return !formatter.isTypeFormatter() && (formatter.getMaxWidth() >= CmsFormatterBean.PREVIEW_WIDTH);

                }
            });
        }
        if (!result.isPresent() && !m_allFormatters.isEmpty()) {
            result = Optional.fromNullable(m_allFormatters.iterator().next());
        }
        return result.orNull();
    }

    /**
     * Returns the provided <code>true</code> in case this configuration has a formatter
     * for the given type / width parameters.<p>
     *
     * @param containerTypes the container types (comma separated)
     * @param containerWidth the container width
     * @param allowNested if nested containers are allowed
     *
     * @return the provided <code>true</code> in case this configuration has a formatter
     *      for the given type / width parameters.
     */
    public boolean hasFormatter(String containerTypes, int containerWidth, boolean allowNested) {

        return getDefaultFormatter(containerTypes, containerWidth, allowNested) != null;
    }

    /**
     * Returns <code>true</code> in case there is at least one usable formatter configured in this configuration.<p>
     *
     * @return <code>true</code> in case there is at least one usable formatter configured in this configuration
     */
    public boolean hasFormatters() {

        return !m_allFormatters.isEmpty();
    }

    /**
     * Returns <code>true</code> in case this configuration contains a formatter with the
     * provided structure id that has been configured for including the formatted content in the online search.<p>
     *
     * @param formatterStructureId the formatter structure id
     *
     * @return <code>true</code> in case this configuration contains a formatter with the
     * provided structure id that has been configured for including the formatted content in the online search
     */
    public boolean isSearchContent(CmsUUID formatterStructureId) {

        if (EMPTY_CONFIGURATION == this) {
            // don't search if this is just the empty configuration
            return false;
        }
        // lookup the cache
        Boolean result = m_searchContent.get(formatterStructureId);
        if (result == null) {
            // result so far unknown
            for (I_CmsFormatterBean formatter : m_allFormatters) {
                if (formatter.getJspStructureId().equals(formatterStructureId)) {
                    // found the match
                    result = Boolean.valueOf(formatter.isSearchContent());
                    // first match rules
                    break;
                }
            }
            if (result == null) {
                // no match found, in this case dont search the content
                result = Boolean.FALSE;
            }
            // store result in the cache
            m_searchContent.put(formatterStructureId, result);
        }

        return result.booleanValue();
    }

    /**
     * Initializes all formatters of this configuration.<p>
     *
     * It is also checked if the configured JSP root path exists, if not the formatter is removed
     * as it is unusable.<p>
     *
     * @param userCms the current users OpenCms context, used for selecting the right project
     * @param adminCms the Admin user context to use for reading the JSP resources
     */
    private void init(CmsObject userCms, CmsObject adminCms) {

        List<I_CmsFormatterBean> filteredFormatters = new ArrayList<I_CmsFormatterBean>();
        for (I_CmsFormatterBean formatter : m_allFormatters) {

            if (formatter.getJspStructureId() == null) {
                // a formatter may have been re-used so the structure id is already available
                CmsResource res = null;
                // first we make sure that the JSP exists at all (and also we read the UUID that way)
                try {
                    // first get a cms copy so we can mess up the context without modifying the original
                    CmsObject cmsCopy = OpenCms.initCmsObject(adminCms);
                    cmsCopy.getRequestContext().setCurrentProject(userCms.getRequestContext().getCurrentProject());
                    // switch to the root site
                    cmsCopy.getRequestContext().setSiteRoot("");
                    // now read the JSP
                    res = cmsCopy.readResource(formatter.getJspRootPath());
                } catch (CmsException e) {
                    //if this happens the result is null and we write a LOG error
                }
                if ((res == null) || !CmsResourceTypeJsp.isJsp(res)) {
                    // the formatter must exist and it must be a JSP
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_FORMATTER_JSP_DONT_EXIST_1,
                            formatter.getJspRootPath()));
                } else {
                    formatter.setJspStructureId(res.getStructureId());
                    // res may still be null in case of failure
                }
            }

            if (formatter.getJspStructureId() != null) {
                filteredFormatters.add(formatter);
            } else {
                LOG.warn("Invalid formatter: " + formatter.getJspRootPath());
            }
        }
        Collections.sort(filteredFormatters, new FormatterComparator());
        m_allFormatters = Collections.unmodifiableList(filteredFormatters);
    }

}

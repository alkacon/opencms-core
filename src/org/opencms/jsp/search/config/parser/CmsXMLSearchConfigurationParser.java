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

package org.opencms.jsp.search.config.parser;

import org.opencms.jsp.search.config.CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.CmsSearchConfigurationSorting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/** Search configuration parser reading XML. */
public class CmsXMLSearchConfigurationParser implements I_CmsSearchConfigurationParser {

    /** Logger for the class. */
    protected static final Log LOG = CmsLog.getLog(CmsXMLSearchConfigurationParser.class);

    /** The element names of the xml content. */
    /** Elements for common options. */
    /** XML element name. */
    private static final String XML_ELEMENT_QUERYPARAM = "QueryParam";
    /** XML element name. */
    private static final String XML_ELEMENT_LAST_QUERYPARAM = "LastQueryParam";
    /** XML element name. */
    private static final String XML_ELEMENT_IGNORE_QUERY = "IgnoreQuery";
    /** XML element name. */
    private static final String XML_ELEMENT_PAGEPARAM = "PageParam";
    /** XML element name. */
    private static final String XML_ELEMENT_INDEX = "Index";
    /** XML element name. */
    private static final String XML_ELEMENT_CORE = "Core";
    /** XML element name. */
    private static final String XML_ELEMENT_EXTRASOLRPARAMS = "ExtraSolrParams";
    /** XML element name. */
    private static final String XML_ELEMENT_PAGESIZE = "PageSize";
    /** XML element name. */
    private static final String XML_ELEMENT_PAGENAVLENGTH = "PageNavLength";

    /** XML element names for facet configuration. */
    /** XML element name for the root element of a field facet configuration. */
    private static final String XML_ELEMENT_FIELD_FACETS = "FieldFacet";
    /** XML element names for facet options. */
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_LIMIT = "Limit";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_MINCOUNT = "MinCount";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_LABEL = "Label";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_NAME = "Name";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_FIELD = "Field";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_PREFIX = "Prefix";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_ORDER = "Order";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_FILTERQUERYMODIFIER = "FilterQueryModifier";
    /** XML element name. */
    private static final String XML_ELEMENT_FACET_ISANDFACET = "IsAndFacet";

    /** XML element names for sort options. */
    /** XML element name. */
    private static final String XML_ELEMENT_SORTPARAM = "SortParam";
    /** XML element name for the root element for sort options. */
    private static final String XML_ELEMENT_SORTOPTIONS = "SortOption";
    /** XML element names for a single search option. */
    private static final String XML_ELEMENT_SORTOPTION_LABEL = "Label";
    /** XML element name. */
    private static final String XML_ELEMENT_SORTOPTION_PARAMVALUE = "ParamValue";
    /** XML element name. */
    private static final String XML_ELEMENT_SORTOPTION_SOLRVALUE = "SolrValue";
    /** XML element name for the root element for the highlighting configuration. */
    private static final String XML_ELEMENT_HIGHLIGHTER = "Highlighting";
    /** XML elements for the highlighting configuration. */
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_FIELD = "Field";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_SNIPPETS = "Snippets";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_FRAGSIZE = "FragSize";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_ALTERNATE_FIELD = "AlternateField";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_MAX_LENGTH_ALTERNATE_FIELD = "MaxAlternateFieldLength";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_SIMPLE_PRE = "SimplePre";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_SIMPLE_POST = "SimplePost";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_FORMATTER = "Formatter";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_FRAGMENTER = "Fragmenter";
    /** XML element name. */
    private static final String XML_ELEMENT_HIGHLIGHTER_FASTVECTORHIGHLIGHTING = "UseFastVectorHighlighting";

    /** XML element names for "Did you mean ...?". */
    /** XML element name. */
    private static final String XML_ELEMENT_DIDYOUMEAN_ENABLED = "DidYouMeanEnabled";

    /** Default value. */
    private static final String DEFAULT_QUERY_PARAM = "q";
    /** Default value. */
    private static final String DEFAULT_LAST_QUERY_PARAM = "lq";
    /** Default value. */
    private static final String DEFAULT_SORT_PARAM = "sort";
    /** Default value. */
    private static final String DEFAULT_PAGE_PARAM = "page";
    /** Default value. */
    private static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(10);
    /** Default value. */
    private static final Integer DEFAULT_PAGENAVLENGTH = Integer.valueOf(5);

    /** The XML content that contains the configuration. */
    CmsXmlContent m_xml;
    /** The locale in which the configuration should be read. */
    Locale m_locale;

    /** Constructor taking the XML content that should be read and the locale in which it should be read.
     * @param xml The XML content that should be read for the configuration.
     * @param locale The locale in which the content should be read.
     */
    public CmsXMLSearchConfigurationParser(final CmsXmlContent xml, final Locale locale) {

        m_xml = xml;
        m_locale = locale;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseCommon()
     */
    @Override
    public I_CmsSearchConfigurationCommon parseCommon() {

        return new CmsSearchConfigurationCommon(
            getQueryParam(),
            getLastQueryParam(),
            getIgnoreQuery(),
            getIndex(),
            getCore(),
            getExtraSolrParams());
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
     */
    public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

        return new CmsSearchConfigurationDidYouMean(parseOptionalBooleanValue(XML_ELEMENT_DIDYOUMEAN_ENABLED));
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
     */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        final Map<String, I_CmsSearchConfigurationFacetField> facetConfigs = new LinkedHashMap<String, I_CmsSearchConfigurationFacetField>();
        final CmsXmlContentValueSequence fieldFacets = m_xml.getValueSequence(XML_ELEMENT_FIELD_FACETS, m_locale);
        if (fieldFacets == null) {
            return null;
        } else {
            for (int i = 0; i < fieldFacets.getElementCount(); i++) {

                final I_CmsSearchConfigurationFacetField config = parseFieldFacet(fieldFacets.getValue(i).getPath()
                    + "/");
                if (config != null) {
                    facetConfigs.put(config.getName(), config);
                }
            }
            return facetConfigs;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseHighlighter()
     */
    @Override
    public I_CmsSearchConfigurationHighlighting parseHighlighter() {

        final I_CmsXmlContentValue highlighter = m_xml.getValue(XML_ELEMENT_HIGHLIGHTER, m_locale);
        if (highlighter == null) {
            return null;
        } else {
            try {
                final String pathPrefix = highlighter.getPath() + "/";
                final String field = parseMandatoryStringValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_FIELD);
                final Integer snippets = parseOptionalIntValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_SNIPPETS);
                final Integer fragsize = parseOptionalIntValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_FRAGSIZE);
                final String alternateField = parseOptionalStringValue(pathPrefix
                    + XML_ELEMENT_HIGHLIGHTER_ALTERNATE_FIELD);
                final Integer maxAlternateFieldLength = parseOptionalIntValue(pathPrefix
                    + XML_ELEMENT_HIGHLIGHTER_MAX_LENGTH_ALTERNATE_FIELD);
                final String pre = parseOptionalStringValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_SIMPLE_PRE);
                final String post = parseOptionalStringValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_SIMPLE_POST);
                final String formatter = parseOptionalStringValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_FORMATTER);
                final String fragmenter = parseOptionalStringValue(pathPrefix + XML_ELEMENT_HIGHLIGHTER_FRAGMENTER);
                final Boolean useFastVectorHighlighting = parseOptionalBooleanValue(pathPrefix
                    + XML_ELEMENT_HIGHLIGHTER_FASTVECTORHIGHLIGHTING);
                return new CmsSearchConfigurationHighlighting(
                    field,
                    snippets,
                    fragsize,
                    alternateField,
                    maxAlternateFieldLength,
                    pre,
                    post,
                    formatter,
                    fragmenter,
                    useFastVectorHighlighting);
            } catch (final Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_MANDATORY_HIGHLIGHTING_FIELD_MISSING_0), e);
                return null;
            }
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parsePagination()
     */
    @Override
    public I_CmsSearchConfigurationPagination parsePagination() {

        return new CmsSearchConfigurationPagination(getPageParam(), getPageSize(), getPageNavLength());
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseSorting()
     */
    @Override
    public I_CmsSearchConfigurationSorting parseSorting() {

        return new CmsSearchConfigurationSorting(getSortParam(), getSortOptions());
    }

    /** Reads the configuration of a field facet.
     * @param pathPrefix The XML Path that leads to the field facet configuration, or <code>null</code> if the XML was not correctly structured.
     * @return The read configuration, or <code>null</code> if the XML was not correctly structured.
     */
    protected I_CmsSearchConfigurationFacetField parseFieldFacet(final String pathPrefix) {

        try {
            final String field = parseMandatoryStringValue(pathPrefix + XML_ELEMENT_FACET_FIELD);
            final String label = parseOptionalStringValue(pathPrefix + XML_ELEMENT_FACET_LABEL);
            final String name = parseOptionalStringValue(pathPrefix + XML_ELEMENT_FACET_NAME);
            final Integer minCount = parseOptionalIntValue(pathPrefix + XML_ELEMENT_FACET_MINCOUNT);
            final Integer limit = parseOptionalIntValue(pathPrefix + XML_ELEMENT_FACET_LIMIT);
            final String prefix = parseOptionalStringValue(pathPrefix + XML_ELEMENT_FACET_PREFIX);
            final String sorder = parseOptionalStringValue(pathPrefix + XML_ELEMENT_FACET_ORDER);
            I_CmsSearchConfigurationFacet.SortOrder order;
            try {
                order = I_CmsSearchConfigurationFacet.SortOrder.valueOf(sorder);
            } catch (final Exception e) {
                order = null;
            }
            final String filterQueryModifier = parseOptionalStringValue(pathPrefix
                + XML_ELEMENT_FACET_FILTERQUERYMODIFIER);
            final Boolean isAndFacet = parseOptionalBooleanValue(pathPrefix + XML_ELEMENT_FACET_ISANDFACET);
            return new CmsSearchConfigurationFacetField(
                field,
                minCount,
                limit,
                prefix,
                name,
                label,
                order,
                filterQueryModifier,
                isAndFacet);
        } catch (final Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_FIELD_FACET_MANDATORY_KEY_MISSING_1,
                    XML_ELEMENT_FACET_FIELD),
                e);
            return null;
        }
    }

    /** Helper to read an optional Boolean value.
     * @param path The XML path of the element to read.
     * @return The Boolean value stored in the XML, or <code>null</code> if the value could not be read.
     */
    protected Boolean parseOptionalBooleanValue(final String path) {

        final I_CmsXmlContentValue value = m_xml.getValue(path, m_locale);
        if (value == null) {
            return null;
        } else {
            final String stringValue = value.getStringValue(null);
            try {
                final Boolean boolValue = Boolean.valueOf(stringValue);
                return boolValue;
            } catch (final NumberFormatException e) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_BOOLEAN_MISSING_1, path), e);
                return null;
            }
        }
    }

    /** Helper to read an optional Integer value.
     * @param path The XML path of the element to read.
     * @return The Integer value stored in the XML, or <code>null</code> if the value could not be read.
     */
    protected Integer parseOptionalIntValue(final String path) {

        final I_CmsXmlContentValue value = m_xml.getValue(path, m_locale);
        if (value == null) {
            return null;
        } else {
            final String stringValue = value.getStringValue(null);
            try {
                final Integer intValue = Integer.valueOf(stringValue);
                return intValue;
            } catch (final NumberFormatException e) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_INTEGER_MISSING_1, path), e);
                return null;
            }
        }
    }

    /** Helper to read an optional String value.
     * @param path The XML path of the element to read.
     * @return The String value stored in the XML, or <code>null</code> if the value could not be read.
     */
    protected String parseOptionalStringValue(final String path) {

        final I_CmsXmlContentValue value = m_xml.getValue(path, m_locale);
        if (value == null) {
            return null;
        } else {
            return value.getStringValue(null);
        }
    }

    /** Returns the configured Solr core, or <code>null</code> if the core is not specified.
     * @return The configured Solr core, or <code>null</code> if the core is not specified.
     */
    private String getCore() {

        try {
            return parseMandatoryStringValue(XML_ELEMENT_CORE);
        } catch (final Exception e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_CORE_SPECIFIED_0), e);
            return null;
        }
    }

    /** Returns the extra Solr parameters specified in the configuration, or the empty string if no extra parameters are configured.
     * @return The extra Solr parameters specified in the configuration, or the empty string if no extra parameters are configured.
     */
    private String getExtraSolrParams() {

        return parseOptionalStringValue(XML_ELEMENT_EXTRASOLRPARAMS);
    }

    private Boolean getIgnoreQuery() {

        return parseOptionalBooleanValue(XML_ELEMENT_IGNORE_QUERY);
    }

    /** Returns the configured Solr index, or <code>null</code> if the core is not specified.
     * @return The configured Solr index, or <code>null</code> if the core is not specified.
     */
    private String getIndex() {

        try {
            return parseMandatoryStringValue(XML_ELEMENT_INDEX);
        } catch (final Exception e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_INDEX_SPECIFIED_0), e);
            return null;
        }
    }

    /** Returns the configured request parameter for the last query, or the default parameter if the core is not specified.
     * @return The configured request parameter for the last query, or the default parameter if the core is not specified.
     */
    private String getLastQueryParam() {

        final String param = parseOptionalStringValue(XML_ELEMENT_LAST_QUERYPARAM);
        if (param == null) {
            return DEFAULT_LAST_QUERY_PARAM;
        } else {
            return param;
        }
    }

    /** Returns the configured length of the "Google"-like page navigation, or the default length if it is not configured.
     * @return The configured length of the "Google"-like page navigation, or the default length if it is not configured.
     */
    private Integer getPageNavLength() {

        final Integer param = parseOptionalIntValue(XML_ELEMENT_PAGENAVLENGTH);
        if (param == null) {
            return DEFAULT_PAGENAVLENGTH;
        } else {
            return param;
        }
    }

    /** Returns the configured request parameter for the current page, or the default parameter if the core is not specified.
     * @return The configured request parameter for the current page, or the default parameter if the core is not specified.
     */
    private String getPageParam() {

        final String param = parseOptionalStringValue(XML_ELEMENT_PAGEPARAM);
        if (param == null) {
            return DEFAULT_PAGE_PARAM;
        } else {
            return param;
        }
    }

    /** Returns the configured page size, or the default page size if it is not configured.
     * @return The configured page size, or the default page size if it is not configured.
     */
    private Integer getPageSize() {

        final Integer pageSize = parseOptionalIntValue(XML_ELEMENT_PAGESIZE);
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        } else {
            return pageSize;
        }
    }

    /** Returns the configured request parameter for the current query string, or the default parameter if the core is not specified.
     * @return The configured request parameter for the current query string, or the default parameter if the core is not specified.
     */
    private String getQueryParam() {

        final String param = parseOptionalStringValue(XML_ELEMENT_QUERYPARAM);
        if (param == null) {
            return DEFAULT_QUERY_PARAM;
        } else {
            return param;
        }
    }

    /** Returns the configured sort options, or the empty list if no such options are configured.
     * @return The configured sort options, or the empty list if no such options are configured.
     */
    private List<I_CmsSearchConfigurationSortOption> getSortOptions() {

        final List<I_CmsSearchConfigurationSortOption> options = new ArrayList<I_CmsSearchConfigurationSortOption>();
        final CmsXmlContentValueSequence sortOptions = m_xml.getValueSequence(XML_ELEMENT_SORTOPTIONS, m_locale);
        if (sortOptions == null) {
            return null;
        } else {
            for (int i = 0; i < sortOptions.getElementCount(); i++) {
                final I_CmsSearchConfigurationSortOption option = parseSortOption(sortOptions.getValue(i).getPath()
                    + "/");
                if (option != null) {
                    options.add(option);
                }
            }
            return options;
        }
    }

    /** Returns the configured request parameter for the current sort option, or the default parameter if the core is not specified.
     * @return The configured request parameter for the current sort option, or the default parameter if the core is not specified.
     */
    private String getSortParam() {

        final String param = parseOptionalStringValue(XML_ELEMENT_SORTPARAM);
        if (param == null) {
            return DEFAULT_SORT_PARAM;
        } else {
            return param;
        }
    }

    /** Helper to read a mandatory String value.
     * @param path The XML path of the element to read.
     * @return The String value stored in the XML.
     * @throws Exception thrown if the value could not be read.
     */
    private String parseMandatoryStringValue(final String path) throws Exception {

        final String value = parseOptionalStringValue(path);
        if (value == null) {
            throw new Exception();
        }
        return value;
    }

    /** Returns the configuration of a single sort option, or <code>null</code> if the XML cannot be read.
     * @param pathPrefix The XML path to the root node of the sort option's configuration.
     * @return The configuration of a single sort option, or <code>null</code> if the XML cannot be read.
     */
    private I_CmsSearchConfigurationSortOption parseSortOption(final String pathPrefix) {

        try {
            final String solrValue = parseMandatoryStringValue(pathPrefix + XML_ELEMENT_SORTOPTION_SOLRVALUE);
            String paramValue = parseOptionalStringValue(pathPrefix + XML_ELEMENT_SORTOPTION_PARAMVALUE);
            paramValue = (paramValue == null) ? solrValue : paramValue;
            String label = parseOptionalStringValue(pathPrefix + XML_ELEMENT_SORTOPTION_LABEL);
            label = (label == null) ? paramValue : label;
            return new CmsSearchConfigurationSortOption(label, paramValue, solrValue);
        } catch (final Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_SORT_OPTION_NOT_PARSABLE_1,
                    XML_ELEMENT_SORTOPTION_SOLRVALUE),
                e);
            return null;
        }
    }
}

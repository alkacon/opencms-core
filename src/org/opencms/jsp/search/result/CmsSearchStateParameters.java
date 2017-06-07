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

package org.opencms.jsp.search.result;

import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetQuery;
import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetRange;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * State parameter wrapper that allows to manipulate the request parameters representing the state
 * of the current search. It can be used to generate links for an adjusted search.
 */
public class CmsSearchStateParameters implements I_CmsSearchStateParameters {

    /** Logger for the class. */
    protected static final Log LOG = CmsLog.getLog(CmsSearchStateParameters.class);

    /** Map of request parameters, representing the search's state. */
    Map<String, String[]> m_params;
    /** The result of the search. */
    I_CmsSearchResultWrapper m_result;

    /** Map with page numbers as keys and the according state parameters as values. */
    Map<String, I_CmsSearchStateParameters> m_paginationMap;
    /** Map from sort options to state parameters. */
    Map<String, I_CmsSearchStateParameters> m_sortingMap;
    /** Map from facet names to state parameters without the filter queries for the facet. */
    Map<String, I_CmsSearchStateParameters> m_resetFacetMap;
    /** Map from facet names to state parameters with parameters for ignoring the facet's limit added. */
    Map<String, I_CmsSearchStateParameters> m_ignoreLimitFacetMap;
    /** Map new queries to state parameters with the query replaced by the new query. */
    Map<String, I_CmsSearchStateParameters> m_newQueryMap;
    /** Map from facet names to state parameters with parameters for ignoring the facet's limit removed. */
    Map<String, I_CmsSearchStateParameters> m_respectLimitFacetMap;
    /** Map from facet names to a map from facet items to state parameters with the item unchecked. */
    Map<String, Map<String, I_CmsSearchStateParameters>> m_uncheckFacetMap;
    /** Map from facet names to a map from facet items to state parameters with the item checked. */
    Map<String, Map<String, I_CmsSearchStateParameters>> m_checkFacetMap;

    /** Constructor for a state parameters object.
     * @param result The search result, according to which the parameters are manipulated.
     * @param params The original parameter set.
     */
    CmsSearchStateParameters(final I_CmsSearchResultWrapper result, final Map<String, String[]> params) {

        m_params = params;
        m_result = result;
    }

    /** Converts a parameter map to the parameter string.
     * @param parameters the parameter map.
     * @return the parameter string.
     */
    public static String paramMapToString(final Map<String, String[]> parameters) {

        final StringBuffer result = new StringBuffer();
        for (final String key : parameters.keySet()) {
            String[] values = parameters.get(key);
            if (null == values) {
                result.append(key).append('&');
            } else {
                for (final String value : parameters.get(key)) {
                    result.append(key).append('=').append(value).append('&');
                }
            }
        }
        // remove last '&'
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getAddIgnoreFacetLimit()
     */
    public Map<String, I_CmsSearchStateParameters> getAddIgnoreFacetLimit() {

        if (m_ignoreLimitFacetMap == null) {
            m_ignoreLimitFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object facet) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    String facetParamKey = null;
                    try {
                        facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(
                            facet).getConfig().getIgnoreMaxParamKey();
                    } catch (Exception e) {
                        // Facet did not exist
                        LOG.warn(Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet), e);
                    }
                    if ((facetParamKey != null) && !parameters.containsKey(facetParamKey)) {
                        parameters.put(facetParamKey, null);
                    }
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_ignoreLimitFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getCheckFacetItem()
     */
    @Override
    public Map<String, Map<String, I_CmsSearchStateParameters>> getCheckFacetItem() {

        if (m_uncheckFacetMap == null) {
            m_uncheckFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object facet) {

                    Map<String, I_CmsSearchStateParameters> m_uncheckEntries = CmsCollectionsGenericWrapper.createLazyMap(
                        new Transformer() {

                            @Override
                            public Object transform(final Object facetItem) {

                                final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                                String facetParamKey = null;
                                try {
                                    facetParamKey = getFacetParamKey((String)facet);
                                } catch (Exception e) {
                                    // Facet did not exist
                                    LOG.warn(
                                        Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet),
                                        e);
                                }
                                if (facetParamKey != null) {
                                    if (parameters.containsKey(facetParamKey)) {
                                        String[] values = parameters.get(facetParamKey);
                                        Arrays.asList(values).contains(facetItem);
                                        if (Arrays.asList(values).contains(facetItem)) {
                                            String[] newValues = new String[Arrays.asList(values).size() + 1];
                                            int j = 0;
                                            for (int i = 0; i < (values.length); i++) {
                                                newValues[i] = values[i];
                                            }
                                            newValues[values.length] = (String)facetItem;
                                            parameters.put(facetParamKey, newValues);
                                        }
                                    } else {
                                        parameters.put(facetParamKey, new String[] {(String)facetItem});
                                    }

                                }
                                return new CmsSearchStateParameters(m_result, parameters);
                            }
                        });
                    return m_uncheckEntries;
                }
            });
        }
        return m_uncheckFacetMap;

    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getNewQuery()
     */
    @Override
    public Map<String, I_CmsSearchStateParameters> getNewQuery() {

        if (m_newQueryMap == null) {
            m_newQueryMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object queryString) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    String queryKey = m_result.getController().getCommon().getConfig().getQueryParam();
                    if (parameters.containsKey(queryKey)) {
                        parameters.remove(queryKey);
                    }
                    parameters.put(queryKey, new String[] {(String)queryString});
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_newQueryMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getRemoveIgnoreFacetLimit()
     */
    public Map<String, I_CmsSearchStateParameters> getRemoveIgnoreFacetLimit() {

        if (m_ignoreLimitFacetMap == null) {
            m_ignoreLimitFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object facet) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    String facetParamKey = null;
                    try {
                        facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(
                            facet).getConfig().getIgnoreMaxParamKey();
                    } catch (Exception e) {
                        // Facet did not exist
                        LOG.warn(Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet), e);
                    }
                    if ((facetParamKey != null) && parameters.containsKey(facetParamKey)) {
                        parameters.remove(facetParamKey);
                    }
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_ignoreLimitFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getResetAllFacetStates()
     */
    @Override
    public I_CmsSearchStateParameters getResetAllFacetStates() {

        final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
        // Remove selected entries from field facets
        Collection<I_CmsSearchControllerFacetField> fieldFacets = m_result.getController().getFieldFacets().getFieldFacetControllers();
        for (I_CmsSearchControllerFacetField facet : fieldFacets) {
            String facetParamKey = facet.getConfig().getParamKey();
            if (parameters.containsKey(facetParamKey)) {
                parameters.remove(facetParamKey);
            }
        }
        // Remove selected entries from range facets
        Collection<I_CmsSearchControllerFacetRange> rangeFacets = m_result.getController().getRangeFacets().getRangeFacetControllers();
        for (I_CmsSearchControllerFacetRange facet : rangeFacets) {
            String facetParamKey = facet.getConfig().getParamKey();
            if (parameters.containsKey(facetParamKey)) {
                parameters.remove(facetParamKey);
            }
        }
        // Remove selected entries from the query facet
        I_CmsSearchControllerFacetQuery facet = m_result.getController().getQueryFacet();
        if (null != facet) {
            String facetParamKey = facet.getConfig().getParamKey();
            if (parameters.containsKey(facetParamKey)) {
                parameters.remove(facetParamKey);
            }
        }
        return new CmsSearchStateParameters(m_result, parameters);
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getResetFacetState()
     */
    @Override
    public Map<String, I_CmsSearchStateParameters> getResetFacetState() {

        if (m_resetFacetMap == null) {
            m_resetFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object facet) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    String facetParamKey = getFacetParamKey((String)facet);
                    if ((facetParamKey != null) && parameters.containsKey(facetParamKey)) {
                        parameters.remove(facetParamKey);
                    }
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_resetFacetMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getSetPage()
     */
    @Override
    public Map<String, I_CmsSearchStateParameters> getSetPage() {

        if (m_paginationMap == null) {
            m_paginationMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object page) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    parameters.put(
                        m_result.getController().getPagination().getConfig().getPageParam(),
                        new String[] {(String)page});
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_paginationMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getSetSortOption()
     */
    @Override
    public Map<String, I_CmsSearchStateParameters> getSetSortOption() {

        if (m_sortingMap == null) {
            m_sortingMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object sortOption) {

                    final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                    m_result.getController().addParametersForCurrentState(parameters);
                    parameters.put(
                        m_result.getController().getSorting().getConfig().getSortParam(),
                        new String[] {(String)sortOption});
                    return new CmsSearchStateParameters(m_result, parameters);
                }
            });
        }
        return m_sortingMap;
    }

    /**
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getUncheckFacetItem()
     */
    @Override
    public Map<String, Map<String, I_CmsSearchStateParameters>> getUncheckFacetItem() {

        if (m_checkFacetMap == null) {
            m_checkFacetMap = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                @Override
                public Object transform(final Object facet) {

                    Map<String, I_CmsSearchStateParameters> m_checkEntries = CmsCollectionsGenericWrapper.createLazyMap(
                        new Transformer() {

                            @Override
                            public Object transform(final Object facetItem) {

                                final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                                String facetParamKey = getFacetParamKey((String)facet);
                                if ((facetParamKey != null) && parameters.containsKey(facetParamKey)) {
                                    String[] values = parameters.get(facetParamKey);
                                    List<String> valueList = Arrays.asList(values);
                                    String item = (String)facetItem;
                                    if (valueList.contains(facetItem)) {
                                        String[] newValues = new String[valueList.size() - 1];
                                        int i = 0;
                                        for (String value : valueList) {
                                            if (value != item) {
                                                newValues[i++] = value;
                                            }
                                        }
                                        parameters.put(facetParamKey, newValues);
                                    }
                                }
                                return new CmsSearchStateParameters(m_result, parameters);
                            }
                        });
                    return m_checkEntries;
                }
            });
        }
        return m_checkFacetMap;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return paramMapToString(m_params);
    }

    /**
     * Returns the parameter key of the facet with the given name.
     * @param facet the facet's name.
     * @return the parameter key for the facet.
     */
    String getFacetParamKey(String facet) {

        I_CmsSearchControllerFacetField fieldFacet = m_result.getController().getFieldFacets().getFieldFacetController().get(
            facet);
        if (fieldFacet != null) {
            return fieldFacet.getConfig().getParamKey();
        }
        I_CmsSearchControllerFacetRange rangeFacet = m_result.getController().getRangeFacets().getRangeFacetController().get(
            facet);
        if (rangeFacet != null) {
            return rangeFacet.getConfig().getParamKey();
        }
        I_CmsSearchControllerFacetQuery queryFacet = m_result.getController().getQueryFacet();
        if ((queryFacet != null) && queryFacet.getConfig().getName().equals(facet)) {
            return queryFacet.getConfig().getParamKey();
        }

        // Facet did not exist
        LOG.warn(Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet), new Throwable());

        return null;
    }

}

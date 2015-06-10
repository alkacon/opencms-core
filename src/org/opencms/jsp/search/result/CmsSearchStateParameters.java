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

package org.opencms.jsp.search.result;

import org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField;
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
    I_SearchResultWrapper m_result;

    /** Map with page numbers as keys and the according state parameters as values. */
    Map<String, I_CmsSearchStateParameters> m_paginationMap;
    /** Map from sort options to state parameters. */
    Map<String, I_CmsSearchStateParameters> m_sortingMap;
    /** Map from facet names to state parameters without the filter queries for the facet. */
    Map<String, I_CmsSearchStateParameters> m_resetFacetMap;
    /** Map from facet names to state parameters with parameters for ignoring the facet's limit added. */
    Map<String, I_CmsSearchStateParameters> m_ignoreLimitFacetMap;
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
    CmsSearchStateParameters(final I_SearchResultWrapper result, final Map<String, String[]> params) {

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
                        facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(facet).getConfig().getIgnoreMaxParamKey();
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

                    Map<String, I_CmsSearchStateParameters> m_uncheckEntries = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                        @Override
                        public Object transform(final Object facetItem) {

                            final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                            String facetParamKey = null;
                            try {
                                facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(
                                    facet).getConfig().getParamKey();
                            } catch (Exception e) {
                                // Facet did not exist
                                LOG.warn(Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet), e);
                            }
                            if (facetParamKey != null) {
                                if (parameters.containsKey(facetParamKey)) {
                                    String[] values = parameters.get(facet);
                                    Arrays.asList(values).contains(facetItem);
                                    if (Arrays.asList(values).contains(facetItem)) {
                                        String[] newValues = new String[Arrays.asList(values).size() - 1];
                                        int j = 0;
                                        for (int i = 0; i < (Arrays.asList(values).size() - 1); i++) {
                                            if (!values[i].equals(facetItem)) {
                                                newValues[j] = values[i];
                                                j++;
                                            }
                                        }
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
     * @see org.opencms.jsp.search.result.I_CmsSearchStateParameters#getQueryDidYouMean()
     */
    @Override
    public I_CmsSearchStateParameters getQueryDidYouMean() {

        final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
        parameters.put(
            m_result.getController().getDidYouMean().getConfig().getQueryParam(),
            new String[] {m_result.getDidYouMean()});
        return new CmsSearchStateParameters(m_result, parameters);
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
                        facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(facet).getConfig().getIgnoreMaxParamKey();
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
        Collection<I_CmsSearchControllerFacetField> fieldFacets = m_result.getController().getFieldFacets().getFieldFacetControllers();
        for (I_CmsSearchControllerFacetField facet : fieldFacets) {
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
                    String facetParamKey = null;
                    try {
                        facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(facet).getConfig().getParamKey();
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

                    Map<String, I_CmsSearchStateParameters> m_checkEntries = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                        @Override
                        public Object transform(final Object facetItem) {

                            final Map<String, String[]> parameters = new HashMap<String, String[]>(m_params);
                            String facetParamKey = null;
                            try {
                                facetParamKey = m_result.getController().getFieldFacets().getFieldFacetController().get(
                                    facet).getConfig().getParamKey();
                            } catch (Exception e) {
                                // Facet did not exist
                                LOG.warn(Messages.get().getBundle().key(Messages.LOG_FACET_NOT_CONFIGURED_1, facet), e);
                            }
                            if ((facetParamKey != null) && parameters.containsKey(facetParamKey)) {
                                String[] values = parameters.get(facet);
                                List<String> valueList = Arrays.asList(values);
                                if (!valueList.contains(facetItem)) {
                                    String[] newValues = new String[valueList.size() + 1];
                                    for (int i = 0; i < (valueList.size() - 1); i++) {
                                        newValues[i] = values[i];
                                    }
                                    newValues[valueList.size()] = (String)facetItem;
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
}

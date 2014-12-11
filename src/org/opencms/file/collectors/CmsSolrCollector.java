/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * A Solr collector.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrCollector extends A_CmsResourceCollector {

    /**
     * Acts as an wrapper so that the Solr Collector is able to return facets delivered by Solr whilst returning the list of CmsResources. 
     */
    public static class CmsSolrCollectorResult {

        /** Returns a list of {@link org.opencms.file.CmsResource} Objects that are gathered in the VFS using the collector. */
        private List<CmsResource> resources;

        /** Returns a map of Facets that are gathered by Solr using the collector. */
        private Map<String, List<String>> facets;

        /**
         * Default constructor.  
         * @param resources The list of resources that are returned by the collector. 
         * @param facets The facets that are returned by the Solr collector. 
         */
        public CmsSolrCollectorResult(List<CmsResource> resources, Map<String, List<String>> facets) {

            this.resources = resources;
            this.facets = facets;
        }

        /**
         * Returns the facets.<p>
         *
         * @return the facets
         */
        public Map<String, List<String>> getFacets() {

            return facets;
        }

        /**
         * Returns the resources.<p>
         *
         * @return the resources
         */
        public List<CmsResource> getResources() {

            return resources;
        }

    }

    /** Constant array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {"byQuery", "byContext"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The folder path to create the "create link" for. */
    private static final String PARAM_CREATE_PATH = "createPath";

    /** A constant for a key. */
    private static final String SOLR_PART = "solrPart";

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    public CmsSolrCollector getCopy() {

        CmsSolrCollector result = new CmsSolrCollector();
        result.m_order = m_order;
        return result;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) throws CmsException {

        collectorName = collectorName == null ? COLLECTORS[1] : collectorName;
        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0: // byQuery
            case 1: // byContext
                Map<String, String> paramsAsMap = getParamsAsMap(param);
                CmsSolrQuery q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(paramsAsMap.get(SOLR_PART)));
                String type = CmsSolrQuery.getResourceType(q.getFilterQueries());
                String path = paramsAsMap.get(PARAM_CREATE_PATH);
                if ((type != null) && (path != null)) {
                    return OpenCms.getResourceManager().getNameGenerator().getNewFileName(cms, path, 4);
                }
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {

        collectorName = collectorName == null ? COLLECTORS[1] : collectorName;
        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0: // byQuery
            case 1: // byContext
                // check if the param supports resource creation
                Map<String, String> paramsAsMap = getParamsAsMap(param);
                CmsSolrQuery q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(paramsAsMap.get(SOLR_PART)));
                String type = CmsSolrQuery.getResourceType(q.getFilterQueries());
                String path = paramsAsMap.get(PARAM_CREATE_PATH);
                if ((type != null) && (path != null)) {
                    return param;
                }
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.A_CmsResourceCollector#getCreateTypeId(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public int getCreateTypeId(CmsObject cms, String collectorName, String param) throws CmsException {

        int result = -1;
        if (param.indexOf('|') > 0) {
            String solrParams = param.substring(0, param.indexOf('|'));
            CmsSolrQuery q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(solrParams));
            String type = CmsSolrQuery.getResourceType(q.getFilterQueries());
            if (type != null) {
                result = OpenCms.getResourceManager().getResourceType(type).getTypeId();
            }
        }
        return result;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        return getResults(cms, collectorName, param, -1);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String name, String param, int numResults) throws CmsException {

        final CmsSolrCollectorResult results = getResultsWithFacets(cms, name, param, numResults);
        if (null != results) {
            return results.resources;
        }

        return null;
    }

    /**
     * Returns an object of type CmsSolrCollectorResult that contains a list of CmsResource objects and Solr facets. 
     * 
     * @param cms the current CmsObject 
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter
     * @return An instance of CmsSolrCollectorResult. 
     * @throws CmsException if something goes wrong
     */
    public CmsSolrCollectorResult getResultsWithFacets(CmsObject cms, String name, String param) throws CmsException {

        return getResultsWithFacets(cms, name, param, -1);
    }

    /**
     * Returns an object of type CmsSolrCollectorResult that contains a list of CmsResource objects and Solr facets.
     * 
     * @param cms the current CmsObject 
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter
     * @param numResults the desired number of results (overrides result number possibl
     * @return An instance of CmsSolrCollectorResult. 
     * @throws CmsException if something goes wrong
     */
    public CmsSolrCollectorResult getResultsWithFacets(CmsObject cms, String name, String param, int numResults)
    throws CmsException {

        name = name == null ? COLLECTORS[1] : name;
        Map<String, String> paramsAsMap = getParamsAsMap(param);
        Map<String, String[]> pm = CmsRequestUtil.createParameterMap(paramsAsMap.get(SOLR_PART));
        CmsSolrQuery q = COLLECTORS_LIST.indexOf(name) == 0 ? new CmsSolrQuery(null, pm) : new CmsSolrQuery(cms, pm);
        boolean excludeTimerange = Boolean.valueOf(paramsAsMap.get(CmsCollectorData.PARAM_EXCLUDETIMERANGE)).booleanValue();
        if (excludeTimerange) {
            q.removeExpiration();
        }
        if (numResults > 0) {
            q.setRows(Integer.valueOf(numResults));
        }
        CmsSolrIndex index = CmsSearchManager.getIndexSolr(cms, pm);
        CmsSolrResultList results = index.search(cms, q, true);

        // Convert the facets
        final Map<String, List<String>> facets;

        if (null == results.getFacetFields()) {
            // If no facets are returned return immutable empty map
            facets = Collections.<String, List<String>> emptyMap();
        } else {
            // Otherwise convert the results
            facets = new HashMap<String, List<String>>();
            for (FacetField f : results.getFacetFields()) {
                String fieldName = f.getName();
                List<String> values = new LinkedList<String>();
                for (Count c : f.getValues()) {
                    values.add(c.getName());
                }
                facets.put(fieldName, values);
            }
        }

        return new CmsSolrCollectorResult(new ArrayList<CmsResource>(results), facets);
    }

    /**
     * Splits the given parameter String into the query part and the cms specific arguments.<p>
     *  
     * @param param the parameter String to parse
     * 
     * @return a map containing the arguments
     */
    private Map<String, String> getParamsAsMap(String param) {

        Map<String, String> result = new HashMap<String, String>();
        if (param != null) {
            int in = (param.indexOf('|'));
            if (in != -1) {
                String solrPart = param.substring(0, in);
                String cmsPart = param.substring(in);
                result = CmsStringUtil.splitAsMap(cmsPart, "|", "=");
                result.put(SOLR_PART, solrPart);

            } else {
                result.put(SOLR_PART, param);
            }
        }
        return result;
    }
}
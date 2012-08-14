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

package org.opencms.search.solr;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.IndexSchema;

import org.xml.sax.InputSource;

/**
 * A test class.<p>
 */
public class TestSolr {

    /** The file name of the Solr configuration. */
    public static final String SOLR_CONFIG_FILE = "solr.xml";

    /** The home folder of Solr. */
    public static final String SOLR_HOME = "/home/kurz/dev/tomcat/webapps/opencms/WEB-INF/solr";

    /** Sample query. */
    public static final String eventPlaceFromUntil = "+resourceType:v8event +place:Ulm +startDate:[2009-06-06T00:00:00Z TO NOW]";

    /** Sample query. */
    public static final String articleText = "+lily +resourceType:v8article";

    /** Sample query. */
    public static final String eventArticleAuthor = "+resourceType:[v8article TO v8event] +author:Admin";

    /** Sample query. */
    public static final String event00006 = "+url:*/opencms/opencms/.content/event/e_00006";

    /**
     * The main method.<p>
     * 
     * @param args args
     */
    public static void main(String[] args) {

        try {
            new TestSolr().query();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs a query.<p>
     * @throws Exception 
     */
    public void query() throws Exception {

        EmbeddedSolrServer server = null;

        // System.setProperty("solr.solr.home", SOLR_HOME);
        File config = new File(SOLR_HOME + "/conf/solrconfig.xml");
        CoreContainer coreContainer = new CoreContainer(SOLR_HOME, new File(SOLR_HOME + "/solr.xml"));

        // core1
        InputSource solrConfig = new InputSource(new FileInputStream(config));
        SolrConfig defaultConfig = new SolrConfig(SOLR_HOME, null, solrConfig);
        InputSource solrSchema = new InputSource(new FileInputStream(new File(SOLR_HOME + "/conf/schema.xml")));
        IndexSchema defaultSchema = new IndexSchema(defaultConfig, null, solrSchema);
        CoreDescriptor defaultDescriptor = new CoreDescriptor(coreContainer, "descriptor", SOLR_HOME);
        defaultDescriptor.setDataDir(SOLR_HOME + "/data");
        SolrCore core1 = new SolrCore("default", null, defaultConfig, defaultSchema, defaultDescriptor);

        // core2
        InputSource testSolrConfig = new InputSource(new FileInputStream(config));
        SolrConfig testConfig = new SolrConfig(SOLR_HOME, null, testSolrConfig);
        InputSource testSolrSchema = new InputSource(new FileInputStream(new File(SOLR_HOME + "/conf/schema.xml")));
        IndexSchema testSchema = new IndexSchema(testConfig, null, testSolrSchema);
        CoreDescriptor testDescriptor = new CoreDescriptor(coreContainer, "descriptor", SOLR_HOME);
        testDescriptor.setDataDir(SOLR_HOME + "/data");
        SolrCore core2 = new SolrCore("test", null, testConfig, testSchema, testDescriptor);

        // register cores
        coreContainer.register(core1, false);
        coreContainer.register(core2, false);

        // start the server
        server = new EmbeddedSolrServer(coreContainer, "test");

        // create and execute the test query
        SolrQuery query = new SolrQuery("*:*");
        query.setQueryType("dismax");
        query.setFacet(true);
        query.addFacetField("category_exact");
        query.addFacetField("resourceType");
        query.setFacetMinCount(2);
        query.setIncludeScore(true);
        query.setRows(new Integer(100));
        QueryResponse qr = server.query(query);
        printResultCount(qr, coreContainer.getCoreNames());
        // printResultDetails(qr);

        // shutdown
        server.shutdown();
    }

    /**
     * Prints a Solr query response.<p>
     * 
     * @param qr the query response
     * @param names names
     * @throws InterruptedException 
     */
    private void printResultCount(QueryResponse qr, Collection<String> names) throws InterruptedException {

        System.out.println("——————————–");
        for (String name : names) {
            System.out.println(name + " core");
        }
        System.out.println("——————————–");
        SolrDocumentList sdl = qr.getResults();
        // System.out.println(sdl.toString());
        System.out.println("Found: " + sdl.getNumFound());
        System.out.println("Start: " + sdl.getStart());
        System.out.println("Max Score: " + sdl.getMaxScore());
        System.out.println("——————————–");

        Thread.sleep(3000);

    }

    /**
     * Prints a Solr query response.<p>
     * 
     * @param qr the query response
     */
    @SuppressWarnings("unused")
    private void printResultDetails(QueryResponse qr) {

        SolrDocumentList sdl = qr.getResults();
        System.out.println(sdl.toString());

        ArrayList<HashMap<String, Object>> hitsOnPage = new ArrayList<HashMap<String, Object>>();
        for (SolrDocument d : sdl) {
            HashMap<String, Object> values = new HashMap<String, Object>();
            Iterator<Map.Entry<String, Object>> i = d.iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> e2 = i.next();
                values.put(e2.getKey(), e2.getValue());
            }

            hitsOnPage.add(values);
            System.out.println(values.get("url") + " (" + values.get("title") + ")");
        }
        List<FacetField> facets = qr.getFacetFields();

        if (facets != null) {
            for (FacetField facet : facets) {
                List<FacetField.Count> facetEntries = facet.getValues();

                if (facetEntries != null) {
                    for (FacetField.Count fcount : facetEntries) {
                        System.out.println(fcount.getName() + ": " + fcount.getCount());
                    }
                }
            }
        }
    }
}

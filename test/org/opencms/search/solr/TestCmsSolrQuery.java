/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.solr;

import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestRunner;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests the query building of {@link CmsSolrQuery}, which requires no OpenCms context.<p>
 *
 * Both aspects covered here failed silently before: a wrong query is still a valid query, so
 * the search returns results for something the caller did not ask for, without an error.<p>
 */
public class TestCmsSolrQuery extends OpenCmsTestRunner {

    /**
     * Returns the value of the query's <code>parent-folders</code> filter query.<p>
     *
     * @param query the query to read the filter from
     *
     * @return the filter query, or <code>null</code> if the query has none
     */
    private static String parentFoldersFilter(CmsSolrQuery query) {

        String prefix = CmsSearchField.FIELD_PARENT_FOLDERS + ":";
        if (query.getFilterQueries() != null) {
            for (String filterQuery : query.getFilterQueries()) {
                if (filterQuery.startsWith(prefix)) {
                    return filterQuery;
                }
            }
        }
        return null;
    }

    /**
     * Test that search roots are normalized to folder paths.<p>
     *
     * A root without a trailing slash matches nothing, because the <code>parent-folders</code>
     * field stores every ancestor path with one (see
     * {@link org.opencms.search.fields.CmsSearchFieldConfiguration#getParentFolderTokens(String)}).<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testSearchRootsAreNormalizedToFolderPaths() throws Exception {

        CmsSolrQuery query = new CmsSolrQuery();
        query.setSearchRoots(Arrays.asList("/sites/default"));
        assertEquals(CmsSearchField.FIELD_PARENT_FOLDERS + ":\"/sites/default/\"", parentFoldersFilter(query));

        query = new CmsSolrQuery();
        query.setSearchRoots(Arrays.asList("/sites/default/"));
        assertEquals(CmsSearchField.FIELD_PARENT_FOLDERS + ":\"/sites/default/\"", parentFoldersFilter(query));

        query = new CmsSolrQuery();
        query.setSearchRoots(Arrays.asList("/sites/default", "/shared/"));
        assertEquals(
            CmsSearchField.FIELD_PARENT_FOLDERS + ":(\"/sites/default/\" OR \"/shared/\")",
            parentFoldersFilter(query));

        query = new CmsSolrQuery();
        query.setSearchRoots(Arrays.asList((String)null));
        assertNull(parentFoldersFilter(query), "A search root list holding only null must not add a filter");
    }

    /**
     * Test that a text query searches every configured text field.<p>
     *
     * The field list has to be separated and quoted: Solr local parameters are separated by
     * spaces, so an unquoted <code>qf</code> holding more than one field makes Solr read
     * everything after the first field as further local parameters.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testTextQueryUsesAllTextSearchFields() throws Exception {

        CmsSolrQuery query = new CmsSolrQuery();
        query.setTextSearchFields("text_en");
        query.setText("christmas");
        assertEquals("{!q.op=OR type=edismax qf=\"text_en\"}christmas", query.getQuery());

        query = new CmsSolrQuery();
        query.setTextSearchFields("text_de", "text_en");
        query.setText("christmas");
        assertEquals("{!q.op=OR type=edismax qf=\"text_de text_en\"}christmas", query.getQuery());

        // field boosts travel as part of the field token
        query = new CmsSolrQuery();
        query.setTextSearchFields("Title_prop^2", "Description_prop");
        query.setText("christmas");
        assertEquals("{!q.op=OR type=edismax qf=\"Title_prop^2 Description_prop\"}christmas", query.getQuery());
    }
}

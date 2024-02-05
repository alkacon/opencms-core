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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.main.CmsLog;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

/**
 * Collects category information during a search process.<p>
 *
 * <b>Please note:</b> The calculation of the category count slows down the search time by an order
 * of magnitude. Make sure that you only use this feature if it's really required!
 * Be especially careful if your search result list can become large (> 1000 documents), since in this case
 * overall system performance will certainly be impacted considerably when calculating the categories.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchCategoryCollector extends SimpleCollector {

    /**
     * Class with an increasing counter to avoid multiple look ups and
     * object creations when dealing with the category count.<p>
     */
    private static class CmsCategroyCount {

        /** The category count. */
        int m_count;

        /**
         * Creates a new instance with a initial count of 1.<p>
         */
        CmsCategroyCount() {

            m_count = 1;
        }

        /**
         * Increases the count by one.<p>
         */
        void inc() {

            m_count++;
        }

        /**
         * Creates an Integer for this count.<p>
         *
         * @return an Integer for this count
         */
        Integer toInteger() {

            return Integer.valueOf(m_count);
        }
    }

    /** Category used in case the document belongs to no category. */
    public static final String UNKNOWN_CATEGORY = "unknown";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchCategoryCollector.class);

    /** The internal map of the categories found. */
    private Map<String, CmsCategroyCount> m_categories;

    /** The index of the document reader. */
    private int m_docBase;

    /** The index searcher used. */
    private IndexSearcher m_searcher;

    /**
     * Creates a new category search collector instance.<p>
     *
     * @param searcher the index searcher used
     */
    public CmsSearchCategoryCollector(IndexSearcher searcher) {

        super();
        m_docBase = 0;
        m_searcher = searcher;
        m_categories = new HashMap<String, CmsCategroyCount>();
    }

    /**
     * Convenience method to format a map of categories in a nice 2 column list, for example
     * for display of debugging output.<p>
     *
     * @param categories the map to format
     * @return the formatted category map
     */
    public static final String formatCategoryMap(Map<String, Integer> categories) {

        StringBuffer result = new StringBuffer(256);
        result.append("Total categories: ");
        result.append(categories.size());
        result.append('\n');
        Iterator<Map.Entry<String, Integer>> i = categories.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Integer> entry = i.next();
            result.append(CmsStringUtil.padRight(entry.getKey(), 30));
            result.append(entry.getValue().intValue());
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * @see org.apache.lucene.search.SimpleCollector#collect(int)
     */
    @Override
    public void collect(int id) {

        String category = null;
        int rebasedId = m_docBase + id;
        try {
            Document doc = m_searcher.doc(rebasedId);
            category = doc.get(CmsSearchField.FIELD_CATEGORY);
        } catch (IOException e) {
            // category will be null
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_READ_CATEGORY_FAILED_1, Integer.valueOf(rebasedId)),
                    e);
            }

        }
        if (category == null) {
            category = UNKNOWN_CATEGORY;
        }
        CmsCategroyCount count = m_categories.get(category);
        if (count != null) {
            count.inc();
        } else {
            count = new CmsCategroyCount();
            m_categories.put(category, count);
        }
    }

    /**
     * Returns the category count result, the returned map
     * contains Strings (category names) mapped to an Integer (the count).<p>
     *
     * @return the category count result
     */
    public Map<String, Integer> getCategoryCountResult() {

        Map<String, Integer> result = new TreeMap<String, Integer>();
        Iterator<Map.Entry<String, CmsCategroyCount>> i = m_categories.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, CmsCategroyCount> entry = i.next();
            result.put(entry.getKey(), entry.getValue().toInteger());
        }
        return result;
    }

    /**
     * @see org.apache.lucene.search.Collector#scoreMode()
     */
    public ScoreMode scoreMode() {

        // we do not need scores
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return formatCategoryMap(getCategoryCountResult());
    }

    /**
     * @see org.apache.lucene.search.SimpleCollector#doSetNextReader(org.apache.lucene.index.LeafReaderContext)
     */
    @Override
    protected void doSetNextReader(LeafReaderContext ctx) {

        m_docBase = ctx.docBase;
    }
}

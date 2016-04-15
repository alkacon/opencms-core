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

package org.opencms.widgets.dataview.example;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.dataview.CmsDataViewColumn;
import org.opencms.widgets.dataview.CmsDataViewColumn.Type;
import org.opencms.widgets.dataview.CmsDataViewFilter;
import org.opencms.widgets.dataview.CmsDataViewQuery;
import org.opencms.widgets.dataview.CmsDataViewResult;
import org.opencms.widgets.dataview.I_CmsDataView;
import org.opencms.widgets.dataview.I_CmsDataViewItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;

/**
 * Example implementation of I_CmsDataView.
 *
 * This view implementation deterministically generates a list with some random data, and simply processes queries/filtering/sorting in-memory.
 *
 */
public class CmsExampleDataView implements I_CmsDataView {

    /** Example image URL. */
    public static final String IMG_RED = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFAAAABQCAIAAAABc2X6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AQPCCEkyfynPgAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABkSURBVHja7c8BAQAABAOw078zPdgarCa/dISFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYUPWd2/AZ/1e0H/AAAAAElFTkSuQmCC";

    /** Example image URL. */
    public static final String IMG_BLUE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFAAAABQCAIAAAABc2X6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AQPCCIBqdUgugAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABoSURBVHja7c8BAQAwCAOg+Vz2r6U9LjSgOpNLXo4RFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhb+2wKDUwHbUtKRCwAAAABJRU5ErkJggg==";

    /** Example image URL. */
    public static final String IMG_GREEN = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFAAAABQCAIAAAABc2X6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AQPCCEyPSgSbwAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABnSURBVHja7c8BAQAwCAOg+RT2L6o9LjSgenLKS4SFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYX/seyRAbcjcrhWAAAAAElFTkSuQmCC";

    /** List of all data items. */
    private List<I_CmsDataViewItem> m_allItems = new ArrayList<I_CmsDataViewItem>();

    /** Minimum value filter. */
    private CmsDataViewFilter m_minValueFilter;

    /** Super category filter. */
    private CmsDataViewFilter m_categoryFilter;

    /** Sub category filter. */
    private CmsDataViewFilter m_fooFilter;

    /** Sub category filter. */
    private CmsDataViewFilter m_barFilter;

    /**
     * Generates a random test string.<p>
     *
     * @param random random number generator
     * @param count word count
     *
     * @return the generated string
     */
    public String generateString(Random random, int count) {

        List<String> words = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            words.add(RandomStringUtils.random(7, 0, 0, true, false, null, random).toLowerCase());

        }
        return CmsStringUtil.listAsString(words, " ");
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#getColumns()
     */
    public List<CmsDataViewColumn> getColumns() {

        List<CmsDataViewColumn> cols = new ArrayList<CmsDataViewColumn>();
        cols.add(new CmsDataViewColumn("id", Type.textType, "ID", true, 80));
        cols.add(new CmsDataViewColumn("image", Type.imageType, "Image", false, 200));
        cols.add(new CmsDataViewColumn("title", Type.textType, "Title", true, 300));
        cols.add(new CmsDataViewColumn("description", Type.textType, "Description", true, 300));
        cols.add(new CmsDataViewColumn("size", Type.doubleType, "Size", true, 90));
        cols.add(new CmsDataViewColumn("category", Type.textType, "Category", true, 90));
        cols.add(new CmsDataViewColumn("good", Type.booleanType, "Good?", true, 70));
        return cols;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#getFilters()
     */
    public List<CmsDataViewFilter> getFilters() {

        return Arrays.asList(m_minValueFilter, m_categoryFilter);
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#getItemById(java.lang.String)
     */
    public I_CmsDataViewItem getItemById(String id) {

        for (I_CmsDataViewItem item : m_allItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#getPageSize()
     */
    public int getPageSize() {

        return 10;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#getResults(org.opencms.widgets.dataview.CmsDataViewQuery, int, int)
     */
    public CmsDataViewResult getResults(CmsDataViewQuery query, int offset, int count) {

        try {
            // simulate being slow
            Thread.sleep(300);
        } catch (Exception e) {
            // ignore
        }

        List<I_CmsDataViewItem> foundItems = new ArrayList<I_CmsDataViewItem>();
        Map<String, String> filters = query.getFilterValues();
        for (I_CmsDataViewItem item : m_allItems) {
            String fullText = item.getColumnData("title") + " " + item.getColumnData("description");
            String fullTextQuery = query.getFullTextQuery();
            if (fullTextQuery == null) {
                fullTextQuery = "";
            }
            if (!fullTextQuery.isEmpty() && !fullText.contains(fullTextQuery)) {
                continue;
            }
            if (filters.containsKey("minSize") && !filters.get("minSize").isEmpty()) {
                int minSize = Integer.parseInt(filters.get("minSize"));
                Double size = (Double)(item.getColumnData("size"));
                if (size.doubleValue() < minSize) {
                    continue;
                }
            }
            String categoryPrefix = null;
            if (filters.containsKey("superCategory") && !filters.get("superCategory").isEmpty()) {
                categoryPrefix = filters.get("superCategory");
            }
            if (filters.containsKey("subCategory") && !filters.get("subCategory").isEmpty()) {
                categoryPrefix = filters.get("subCategory");
            }
            if (categoryPrefix != null) {
                String category = (String)(item.getColumnData("category"));
                if (!(category.startsWith(categoryPrefix))) {
                    continue;
                }

            }

            foundItems.add(item);
        }

        final int compareDirection = query.isSortAscending() ? 1 : -1;
        final String field = query.getSortColumn();

        if (query.getSortColumn() != null) {
            Collections.sort(foundItems, new Comparator<I_CmsDataViewItem>() {

                public int compare(I_CmsDataViewItem first, I_CmsDataViewItem second) {

                    return ComparisonChain.start().compare(
                        (Comparable<?>)first.getColumnData(field),
                        (Comparable<?>)second.getColumnData(field)).result() * compareDirection;
                }
            });

        }

        int unpagedCount = foundItems.size();
        if (offset >= unpagedCount) {
            offset = unpagedCount;
        }
        if ((offset + count) > unpagedCount) {
            count = Math.max(0, unpagedCount - offset);

        }
        foundItems = foundItems.subList(offset, offset + count);

        return new CmsDataViewResult(foundItems, unpagedCount);

    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#initialize(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public void initialize(CmsObject cms, String configData, Locale locale) {

        Random random = new Random(42L);
        String[] colors = new String[] {IMG_RED, IMG_GREEN, IMG_BLUE};
        m_allItems = new ArrayList<I_CmsDataViewItem>();
        String longString = Strings.repeat("123456789 ", 100);
        String[] categories = {"foo1", "foo2", "foo3", "bar1", "bar2", "bar3"};
        for (int i = 0; i < 157; i++) {

            m_allItems.add(
                new CmsExampleDataItem(
                    "ID_" + (1000 + i),
                    generateString(random, 4),
                    (i % 37) == 0 ? longString : generateString(random, 6),
                    random.nextInt(30),
                    random.nextBoolean(),
                    colors[i % 3],
                    categories[i % 6]));
        }

        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("", "(no filter)");
        for (int i : new int[] {0, 5, 10, 15, 20, 25, 30}) {
            options.put("" + i, "" + i);
        }
        m_minValueFilter = new CmsDataViewFilter("minSize", "Minimum size", options, "");
        LinkedHashMap<String, String> superCategoryChoices = new LinkedHashMap<String, String>();
        superCategoryChoices.put("", "(no filter)");
        superCategoryChoices.put("foo", "Foo");
        superCategoryChoices.put("bar", "Bar");
        m_categoryFilter = new CmsDataViewFilter("superCategory", "Category", superCategoryChoices, "");

        LinkedHashMap<String, String> fooChoices = new LinkedHashMap<String, String>();
        fooChoices.put("", "(no filter)");
        fooChoices.put("foo1", "Foo.1");
        fooChoices.put("foo2", "Foo.2");
        fooChoices.put("foo3", "Foo.3");
        m_fooFilter = new CmsDataViewFilter("subCategory", "Subcategory", fooChoices, "");

        LinkedHashMap<String, String> barChoices = new LinkedHashMap<String, String>();
        barChoices.put("", "(no filter)");
        barChoices.put("bar1", "Bar.1");
        barChoices.put("bar2", "Bar.2");
        barChoices.put("bar3", "Bar.3");
        m_barFilter = new CmsDataViewFilter("subCategory", "Subcategory", barChoices, "");

    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataView#updateFilters(java.util.List)
     */
    public List<CmsDataViewFilter> updateFilters(List<CmsDataViewFilter> prevFilters) {

        LinkedHashMap<String, CmsDataViewFilter> filterMap = new LinkedHashMap<String, CmsDataViewFilter>();
        for (CmsDataViewFilter filter : prevFilters) {
            filterMap.put(filter.getId(), filter);
        }
        String superCategory = filterMap.get("superCategory").getValue();
        String subCategory = "";
        if (filterMap.containsKey("subCategory")) {
            subCategory = filterMap.get("subCategory").getValue();
        }

        filterMap.remove("subCategory");
        if ("foo".equals(superCategory)) {
            filterMap.put("subCategory", m_fooFilter);
        } else if ("bar".equals(superCategory)) {
            filterMap.put("subCategory", m_barFilter);
        }

        if (filterMap.containsKey("subCategory")
            && filterMap.get("subCategory").getOptions().containsKey(subCategory)) {
            filterMap.put("subCategory", filterMap.get("subCategory").copyWithValue(subCategory));
        }
        return new ArrayList<CmsDataViewFilter>(filterMap.values());

    }

}

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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 * 
 * @since 6.0.0 
 */
public class CmsLuceneIndex extends A_CmsSearchIndex {

    /**
     * Lucene filter index reader implementation that will ensure the OpenCms default search index fields
     * {@link CmsSearchField#FIELD_CONTENT} and {@link CmsSearchField#FIELD_CONTENT_BLOB}
     * are lazy loaded.<p>
     * 
     * This is to optimize performance - these 2 fields will be rather large especially for extracted
     * binary documents like PDF, MS Office etc. By using lazy fields the data is only read when it is 
     * actually used.<p>
     */
    protected class LazyContentReader extends FilterIndexReader {

        /** The initial index reader. */
        private IndexReader m_reader;

        /**
         * Create a new lazy content reader.<p>
         * 
         * @param indexReader the index reader to use this lazy content reader with
         */
        public LazyContentReader(IndexReader indexReader) {

            super(indexReader);
            m_reader = indexReader;
        }

        /**
         * @see org.apache.lucene.index.FilterIndexReader#document(int, org.apache.lucene.document.FieldSelector)
         */
        @Override
        public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {

            return super.document(n, getFieldSelector(fieldSelector));
        }

        /**
         * @see org.apache.lucene.index.IndexReader#reopen()
         * 
         * @deprecated since Lucene 3.5 but kept for backward compatibility
         */
        @Override
        @Deprecated
        public synchronized IndexReader reopen() throws CorruptIndexException, IOException {

            return m_reader.reopen();
        }

        /**
         * @see org.apache.lucene.index.IndexReader#doOpenIfChanged()
         */
        @Override
        protected IndexReader doOpenIfChanged() throws CorruptIndexException, IOException {

            IndexReader result = IndexReader.openIfChanged(m_reader);
            if (result != null) {
                result = new LazyContentReader(result);
            }
            return result;
        }

        /**
         * @see org.apache.lucene.index.IndexReader#doOpenIfChanged(boolean)
         */
        @Override
        @Deprecated
        protected IndexReader doOpenIfChanged(boolean openReadOnly) throws CorruptIndexException, IOException {

            IndexReader result = IndexReader.openIfChanged(m_reader, openReadOnly);
            if (result != null) {
                result = new LazyContentReader(result);
            }
            return result;
        }
    }

    /** Look table to quickly zero-pad days / months in date Strings. */
    public static final String[] DATES = new String[] {
        "00",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
        "30",
        "31"};

    /** Constant for a field list that contains the "meta" field as well as the "content" field. */
    public static final String[] DOC_META_FIELDS = new String[] {
        I_CmsSearchField.FIELD_META,
        I_CmsSearchField.FIELD_CONTENT};

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_AUTO_COMMIT = "lucene.AutoCommit";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_MAX_MERGE_DOCS = "lucene.MaxMergeDocs";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_MERGE_FACTOR = "lucene.MergeFactor";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_RAM_BUFFER_SIZE_MB = "lucene.RAMBufferSizeMB";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_USE_COMPOUND_FILE = "lucene.UseCompoundFile";

    /** The Lucene Version used to create Query parsers and such. */
    public static final Version LUCENE_VERSION = Version.LUCENE_36;

    /** Constant for years max range span in document search. */
    public static final int MAX_YEAR_RANGE = 12;

    /** Special value for the search.exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_GALLERY = "gallery";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLuceneIndex.class);

    /** The configured Lucene analyzer used for this index. */
    private Analyzer m_analyzer;

    /** The time range check mode for this index. */
    private boolean m_checkTimeRange;

    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Map of display query filters to use. */
    private Map<String, Filter> m_displayFilters;

    /** The Lucene index searcher to use. */
    private IndexSearcher m_indexSearcher;

    /** The Lucene index merge factor setting, see {@link LogMergePolicy#setMaxMergeDocs(int)}. */
    private Integer m_luceneMaxMergeDocs;

    /** The Lucene index merge factor setting, see {@link LogMergePolicy#setMergeFactor(int)}. */
    private Integer m_luceneMergeFactor;

    /** The Lucene index RAM buffer size, see {@link IndexWriterConfig#setRAMBufferSizeMB(double)}. */
    private Double m_luceneRAMBufferSizeMB;

    /** The Lucene index setting that controls, see {@link LogMergePolicy#setUseCompoundFile(boolean)}.  */
    private Boolean m_luceneUseCompoundFile;

    /** Indicates how many hits are loaded at maximum. */
    private int m_maxHits;

    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     * 
     * It is recommended to use the constructor <code>{@link #CmsLuceneIndex(String)}</code> 
     * as it enforces the mandatory name argument. <p>
     */
    public CmsLuceneIndex() {

        super();
        m_createExcerpt = true;
        m_maxHits = MAX_HITS_DEFAULT;
        m_checkTimeRange = false;
    }

    /**
     * Creates a new CmsSearchIndex with the given name.<p>
     * 
     * @param name the system-wide unique name for the search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     */
    public CmsLuceneIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        setName(name);
    }

    /**
     * Generates a list of date terms for the optimized date range search with "daily" granularity level.<p> 
     * 
     * How this works:<ul>
     * <li>For each document, terms are added for the year, the month and the day the document
     * was modified or created) in. So for example if a document is modified at February 02, 2009, 
     * then the following terms are stored for this document:
     * "20090202", "200902" and "2009".</li>
     * <li>In case a date range search is done, then all possible matches for the
     * provided rage are created as search terms and matched with the document terms.</li>
     * <li>Consider the following use case: You want to find out if a resource has been changed
     * in the time between November 29, 2007 and March 01, 2009.
     * One term to match is simply "2008" because if a document 
     * was modified in 2008, then it is clearly in the date range.
     * Other terms are "200712", "200901" and "200902", because all documents 
     * modified in these months are also a certain matches.
     * Finally we need to add terms for "20071129", "20071130" and "20090301" to match the days in the 
     * starting and final month.</li>
     * </ul>
     * 
     * @param startDate start date of the range to search in
     * @param endDate end date of the range to search in
     * 
     * @return a list of date terms for the optimized date range search
     */
    public static List<String> getDateRangeSpan(long startDate, long endDate) {

        if (startDate > endDate) {
            // switch so that the end is always before the start
            long temp = endDate;
            endDate = startDate;
            startDate = temp;
        }

        List<String> result = new ArrayList<String>(100);

        // initialize calendars from the time value
        Calendar calStart = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
        Calendar calEnd = Calendar.getInstance(calStart.getTimeZone());
        calStart.setTimeInMillis(startDate);
        calEnd.setTimeInMillis(endDate);

        // get the required info to build the date range from the calendars
        int startDay = calStart.get(Calendar.DAY_OF_MONTH);
        int endDay = calEnd.get(Calendar.DAY_OF_MONTH);
        int maxDayInStartMonth = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        int startMonth = calStart.get(Calendar.MONTH) + 1;
        int endMonth = calEnd.get(Calendar.MONTH) + 1;
        int startYear = calStart.get(Calendar.YEAR);
        int endYear = calEnd.get(Calendar.YEAR);

        // first add all full years in the date range
        result.addAll(getYearSpan(startYear + 1, endYear - 1));

        if (startYear != endYear) {
            // different year, different month
            result.addAll(getMonthSpan(startMonth + 1, 12, startYear));
            result.addAll(getMonthSpan(1, endMonth - 1, endYear));
            result.addAll(getDaySpan(startDay, maxDayInStartMonth, startMonth, startYear));
            result.addAll(getDaySpan(1, endDay, endMonth, endYear));
        } else {
            if (startMonth != endMonth) {
                // same year, different month
                result.addAll(getMonthSpan(startMonth + 1, endMonth - 1, startYear));
                result.addAll(getDaySpan(startDay, maxDayInStartMonth, startMonth, startYear));
                result.addAll(getDaySpan(1, endDay, endMonth, endYear));
            } else {
                // same year, same month
                result.addAll(getDaySpan(startDay, endDay, endMonth, endYear));
            }
        }

        // sort the result, makes the range better readable in the debugger
        Collections.sort(result);
        return result;
    }

    /**
     * Returns a field selector for Lucene that that will ensure the OpenCms default search index fields
     * {@link CmsSearchField#FIELD_CONTENT} and {@link CmsSearchField#FIELD_CONTENT_BLOB}
     * are lazy loaded.<p>
     * 
     * This is to optimize performance - these 2 fields will be rather large especially for extracted
     * binary documents like PDF, MS Office etc. By using lazy fields the data is only read when it is 
     * actually used.<p>
     * 
     * @param base the base field selector 
     * 
     * @return a field selector that that will ensure the OpenCms default search index fields are lazy loaded
     */
    protected static FieldSelector getFieldSelector(final FieldSelector base) {

        return new FieldSelector() {

            /** Required for safe serialization. */
            private static final long serialVersionUID = 622179189540785073L;

            /**
             * Makes the content fields lazy.<p>
             * 
             * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
             */
            public FieldSelectorResult accept(String fieldName) {

                if (I_CmsSearchField.FIELD_CONTENT.equals(fieldName)
                    || I_CmsSearchField.FIELD_CONTENT_BLOB.equals(fieldName)) {
                    return FieldSelectorResult.LAZY_LOAD;
                }
                if (base == null) {
                    return FieldSelectorResult.LOAD;
                }
                return base.accept(fieldName);
            }
        };
    }

    /**
     * Calculate a span of days in the given year and month for the optimized date range search.<p>
     *  
     * The result will contain dates formatted like "yyyyMMDD", for example "20080131".<p> 
     *  
     * @param startDay the start day
     * @param endDay the end day
     * @param month the month
     * @param year the year
     * 
     * @return a span of days in the given year and month for the optimized date range search
     */
    private static List<String> getDaySpan(int startDay, int endDay, int month, int year) {

        List<String> result = new ArrayList<String>();
        String yearMonthStr = String.valueOf(year) + DATES[month];
        for (int i = startDay; i <= endDay; i++) {
            String dateStr = yearMonthStr + DATES[i];
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Calculate a span of months in the given year for the optimized date range search.<p>
     *  
     * The result will contain dates formatted like "yyyyMM", for example "200801".<p> 
     *  
     * @param startMonth the start month
     * @param endMonth the end month
     * @param year the year
     * 
     * @return a span of months in the given year for the optimized date range search
     */
    private static List<String> getMonthSpan(int startMonth, int endMonth, int year) {

        List<String> result = new ArrayList<String>();
        String yearStr = String.valueOf(year);
        for (int i = startMonth; i <= endMonth; i++) {
            String dateStr = yearStr + DATES[i];
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Calculate a span of years for the optimized date range search.<p>
     *  
     * The result will contain dates formatted like "yyyy", for example "2008".<p> 
     *  
     * @param startYear the start year
     * @param endYear the end year
     * 
     * @return a span of years for the optimized date range search
     */
    private static List<String> getYearSpan(int startYear, int endYear) {

        List<String> result = new ArrayList<String>();
        for (int i = startYear; i <= endYear; i++) {
            String dateStr = String.valueOf(i);
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     * 
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String key, String value) {

        super.addConfigurationParameter(key, value);
        if (MAX_HITS.equals(key)) {
            try {
                m_maxHits = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
            if (m_maxHits < (MAX_HITS_DEFAULT / 100)) {
                m_maxHits = MAX_HITS_DEFAULT;
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (TIME_RANGE.equals(key)) {
            m_checkTimeRange = Boolean.valueOf(value).booleanValue();
        } else if (A_CmsSearchIndex.EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();
        } else if (LUCENE_MAX_MERGE_DOCS.equals(key)) {
            try {
                m_luceneMaxMergeDocs = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_MERGE_FACTOR.equals(key)) {
            try {
                m_luceneMergeFactor = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_RAM_BUFFER_SIZE_MB.equals(key)) {
            try {
                m_luceneRAMBufferSizeMB = Double.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_USE_COMPOUND_FILE.equals(key)) {
            m_luceneUseCompoundFile = Boolean.valueOf(value);
        }
    }

    /**
     * Returns the Lucene analyzer used for this index.<p>
     *
     * @return the Lucene analyzer used for this index
     */
    public Analyzer getAnalyzer() {

        return m_analyzer;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = super.getConfiguration();
        if (getMaxHits() != MAX_HITS_DEFAULT) {
            result.put(MAX_HITS, String.valueOf(getMaxHits()));
        }
        if (!isCreatingExcerpt()) {
            result.put(EXCERPT, String.valueOf(m_createExcerpt));
        }
        if (m_luceneMaxMergeDocs != null) {
            result.put(LUCENE_MAX_MERGE_DOCS, String.valueOf(m_luceneMaxMergeDocs));
        }
        if (m_luceneMergeFactor != null) {
            result.put(LUCENE_MERGE_FACTOR, String.valueOf(m_luceneMergeFactor));
        }
        if (m_luceneRAMBufferSizeMB != null) {
            result.put(LUCENE_RAM_BUFFER_SIZE_MB, String.valueOf(m_luceneRAMBufferSizeMB));
        }
        if (m_luceneUseCompoundFile != null) {
            result.put(LUCENE_USE_COMPOUND_FILE, String.valueOf(m_luceneUseCompoundFile));
        }
        // always write time range check parameter because of logic change in OpenCms 8.0
        result.put(TIME_RANGE, String.valueOf(m_checkTimeRange));
        return result;
    }

    /**
     * Returns the Lucene document with the given root path from the index.<p>
     * 
     * @param rootPath the root path of the document to get 
     * 
     * @return the Lucene document with the given root path from the index
     * 
     * @deprecated Use {@link #getDocument(String, String)} instead and provide {@link CmsSearchField#FIELD_PATH} as field to search in
     */
    @Deprecated
    public Document getDocument(String rootPath) {

        if (getDocument(I_CmsSearchField.FIELD_PATH, rootPath) != null) {
            return (Document)getDocument(I_CmsSearchField.FIELD_PATH, rootPath).getDocument();
        }
        return null;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#getDocument(java.lang.String, java.lang.String)
     */
    @Override
    public synchronized I_CmsSearchDocument getDocument(String field, String term) {

        Document result = null;
        IndexSearcher searcher = getSearcher();
        if (searcher != null) {
            // search for an exact match on the selected field
            Term resultTerm = new Term(field, term);
            try {
                TopDocs hits = searcher.search(new TermQuery(resultTerm), 1);
                if (hits.scoreDocs.length > 0) {
                    result = searcher.doc(hits.scoreDocs[0].doc);
                }
            } catch (IOException e) {
                // ignore, return null and assume document was not found
            }
        }
        if (result != null) {
            return new CmsLuceneDocument(result);
        }
        return null;
    }

    /**
     * Returns the concrete Lucene field configuration.<p>
     * 
     * @return the Lucene field configuration
     */
    public CmsSearchFieldConfiguration getLuceneFieldConfiguration() {

        return (getFieldConfiguration() instanceof CmsSearchFieldConfiguration)
        ? (CmsSearchFieldConfiguration)getFieldConfiguration()
        : null;
    }

    /**
     * Indicates the number of how many hits are loaded at maximum.<p> 
     * 
     * The number of maximum documents to load from the index
     * must be specified. The default of this setting is {@link A_CmsSearchIndex#MAX_HITS_DEFAULT} (5000).
     * This means that at maximum 5000 results are returned from the index.
     * Please note that this number may be reduced further because of OpenCms read permissions 
     * or per-user file visibility settings not controlled in the index.<p>
     * 
     * @return the number of how many hits are loaded at maximum
     * 
     * @since 7.5.1
     */
    public int getMaxHits() {

        return m_maxHits;
    }

    /**
     * Returns the Lucene index searcher used for this search index.<p>
     *
     * @return the Lucene index searcher used for this search index
     */
    public IndexSearcher getSearcher() {

        return m_indexSearcher;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#initialize()
     */
    @Override
    public void initialize() throws CmsSearchException {

        super.initialize();

        // get the configured analyzer and apply the the field configuration analyzer wrapper
        Analyzer baseAnalyzer = OpenCms.getSearchManager().getAnalyzer(getLocale());
        if (getFieldConfiguration() instanceof CmsSearchFieldConfiguration) {
            setAnalyzer(getLuceneFieldConfiguration().getAnalyzer(baseAnalyzer));
        }
        // initialize the index searcher instance
        indexSearcherOpen(getPath());
    }

    /**
     * Returns <code>true</code> if the document time range is checked with a granularity level of seconds
     * for search results by this index.<p>
     * 
     * Since OpenCms 8.0, time range checks are always done if {@link CmsSearchParameters#setMinDateLastModified(long)}
     * or any of the corresponding methods are used. 
     * This is done very efficiently using optimized Lucene filers.
     * However, the granularity of these checks are done only on a daily
     * basis, which means that you can only find "changes made yesterday" but not "changes made last hour". 
     * For normal limitation of search results, a daily granularity should be enough.<p> 
     * 
     * If time range checks with a granularity level of seconds are required, 
     * they can be turned on in the index search configuration parameters
     * in <code>opencms-search.xml</code>. 
     * Not checking the time range  with a granularity level of seconds will improve performance.<p>
     * 
     * By default the granularity level of seconds is turned off since OpenCms 8.0<p> 
     * 
     * @return <code>true</code> if the document time range is checked  with a granularity level of seconds for search results by this index
     */
    public boolean isCheckingTimeRange() {

        return m_checkTimeRange;
    }

    /**
     * Returns <code>true</code> if an excerpt is generated by this index.<p>
     *
     * If no except is required, generation can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not generating an excerpt will improve performance.<p> 
     *
     * @return <code>true</code> if an excerpt is generated by this index
     */
    public boolean isCreatingExcerpt() {

        return m_createExcerpt;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#search(org.opencms.file.CmsObject, org.opencms.search.CmsSearchParameters)
     */
    @Override
    public synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) throws CmsSearchException {

        long timeTotal = -System.currentTimeMillis();
        long timeLucene;
        long timeResultProcessing;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SEARCH_PARAMS_2, params, getName()));
        }

        // the hits found during the search
        TopDocs hits;

        // storage for the results found
        CmsSearchResultList searchResults = new CmsSearchResultList();

        int previousPriority = Thread.currentThread().getPriority();

        try {
            // copy the user OpenCms context
            CmsObject searchCms = OpenCms.initCmsObject(cms);

            if (getPriority() > 0) {
                // change thread priority in order to reduce search impact on overall system performance
                Thread.currentThread().setPriority(getPriority());
            }

            // change the project     
            searchCms.getRequestContext().setCurrentProject(searchCms.readProject(getProject()));

            timeLucene = -System.currentTimeMillis();

            // several search options are searched using filters
            BooleanFilter filter = new BooleanFilter();
            // append root path filter
            filter = appendPathFilter(searchCms, filter, params.getRoots());
            // append category filter
            filter = appendCategoryFilter(searchCms, filter, params.getCategories());
            // append resource type filter
            filter = appendResourceTypeFilter(searchCms, filter, params.getResourceTypes());

            // append date last modified filter
            filter = appendDateLastModifiedFilter(
                filter,
                params.getMinDateLastModified(),
                params.getMaxDateLastModified());
            // append date created filter
            filter = appendDateCreatedFilter(filter, params.getMinDateCreated(), params.getMaxDateCreated());

            // the search query to use, will be constructed in the next lines 
            Query query = null;
            // store separate fields query for excerpt highlighting  
            Query fieldsQuery = null;

            // get an index searcher that is certainly up to date
            indexSearcherUpdate();
            IndexSearcher searcher = getSearcher();

            if (!params.isIgnoreQuery()) {
                // since OpenCms 8 the query can be empty in which case only filters are used for the result
                if (params.getParsedQuery() != null) {
                    // the query was already build, re-use it 
                    QueryParser p = new QueryParser(LUCENE_VERSION, I_CmsSearchField.FIELD_CONTENT, getAnalyzer());
                    fieldsQuery = p.parse(params.getParsedQuery());
                } else if (params.getFieldQueries() != null) {
                    // each field has an individual query
                    BooleanQuery mustOccur = null;
                    BooleanQuery shouldOccur = null;
                    for (CmsSearchParameters.CmsSearchFieldQuery fq : params.getFieldQueries()) {
                        // add one sub-query for each defined field
                        QueryParser p = new QueryParser(LUCENE_VERSION, fq.getFieldName(), getAnalyzer());
                        // first generate the combined keyword query
                        Query keywordQuery = null;
                        if (fq.getSearchTerms().size() == 1) {
                            // this is just a single size keyword list
                            keywordQuery = p.parse(fq.getSearchTerms().get(0));
                        } else {
                            // multiple size keyword list
                            BooleanQuery keywordListQuery = new BooleanQuery();
                            for (String keyword : fq.getSearchTerms()) {
                                keywordListQuery.add(p.parse(keyword), fq.getTermOccur());
                            }
                            keywordQuery = keywordListQuery;
                        }
                        if (BooleanClause.Occur.SHOULD.equals(fq.getOccur())) {
                            if (shouldOccur == null) {
                                shouldOccur = new BooleanQuery();
                            }
                            shouldOccur.add(keywordQuery, fq.getOccur());
                        } else {
                            if (mustOccur == null) {
                                mustOccur = new BooleanQuery();
                            }
                            mustOccur.add(keywordQuery, fq.getOccur());
                        }
                    }
                    BooleanQuery booleanFieldsQuery = new BooleanQuery();
                    if (mustOccur != null) {
                        booleanFieldsQuery.add(mustOccur, BooleanClause.Occur.MUST);
                    }
                    if (shouldOccur != null) {
                        booleanFieldsQuery.add(shouldOccur, BooleanClause.Occur.MUST);
                    }
                    fieldsQuery = searcher.rewrite(booleanFieldsQuery);
                } else if ((params.getFields() != null) && (params.getFields().size() > 0)) {
                    // no individual field queries have been defined, so use one query for all fields 
                    BooleanQuery booleanFieldsQuery = new BooleanQuery();
                    // this is a "regular" query over one or more fields
                    // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                    for (int i = 0; i < params.getFields().size(); i++) {
                        QueryParser p = new QueryParser(LUCENE_VERSION, params.getFields().get(i), getAnalyzer());
                        booleanFieldsQuery.add(p.parse(params.getQuery()), BooleanClause.Occur.SHOULD);
                    }
                    fieldsQuery = searcher.rewrite(booleanFieldsQuery);
                } else {
                    // if no fields are provided, just use the "content" field by default
                    QueryParser p = new QueryParser(LUCENE_VERSION, I_CmsSearchField.FIELD_CONTENT, getAnalyzer());
                    fieldsQuery = searcher.rewrite(p.parse(params.getQuery()));
                }

                // finally set the main query to the fields query
                // please note that we still need both variables in case the query is a MatchAllDocsQuery - see below
                query = fieldsQuery;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_BASE_QUERY_1, query));
            }

            if (query == null) {
                // if no text query is set, then we match all documents 
                query = new MatchAllDocsQuery();
            } else {
                // store the parsed query for page browsing
                params.setParsedQuery(query.toString(I_CmsSearchField.FIELD_CONTENT));
            }

            // collect the categories
            CmsSearchCategoryCollector categoryCollector;
            if (params.isCalculateCategories()) {
                // USE THIS OPTION WITH CAUTION
                // this may slow down searched by an order of magnitude
                categoryCollector = new CmsSearchCategoryCollector(searcher);
                // perform a first search to collect the categories
                searcher.search(query, filter, categoryCollector);
                // store the result
                searchResults.setCategories(categoryCollector.getCategoryCountResult());
            }

            // perform the search operation          
            if ((params.getSort() == null) || (params.getSort() == CmsSearchParameters.SORT_DEFAULT)) {
                // apparently scoring is always enabled by Lucene if no sort order is provided
                hits = searcher.search(query, filter, getMaxHits());
            } else {
                // if  a sort order is provided, we must check if scoring must be calculated by the searcher
                prepareSortScoring(searcher, params.getSort());
                hits = searcher.search(query, filter, getMaxHits(), params.getSort());
            }

            timeLucene += System.currentTimeMillis();
            timeResultProcessing = -System.currentTimeMillis();

            Document doc;
            CmsSearchResult searchResult;

            if (hits != null) {
                int hitCount = hits.totalHits > hits.scoreDocs.length ? hits.scoreDocs.length : hits.totalHits;
                int page = params.getSearchPage();
                int start = -1, end = -1;
                if ((params.getMatchesPerPage() > 0) && (page > 0) && (hitCount > 0)) {
                    // calculate the final size of the search result
                    start = params.getMatchesPerPage() * (page - 1);
                    end = start + params.getMatchesPerPage();
                    // ensure that both i and n are inside the range of foundDocuments.size()
                    start = (start > hitCount) ? hitCount : start;
                    end = (end > hitCount) ? hitCount : end;
                } else {
                    // return all found documents in the search result
                    start = 0;
                    end = hitCount;
                }

                int visibleHitCount = hitCount;
                for (int i = 0, cnt = 0; (i < hitCount) && (cnt < end); i++) {
                    try {
                        doc = searcher.doc(hits.scoreDocs[i].doc);
                        I_CmsSearchDocument searchDoc = new CmsLuceneDocument(doc);
                        searchDoc.setScore(hits.scoreDocs[i].score);
                        if ((isInTimeRange(doc, params)) && (hasReadPermission(searchCms, searchDoc))) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document!
                                String excerpt = null;
                                if (isCreatingExcerpt() && (fieldsQuery != null)) {
                                    I_CmsTermHighlighter highlighter = OpenCms.getSearchManager().getHighlighter();
                                    excerpt = highlighter.getExcerpt(doc, this, params, fieldsQuery, getAnalyzer());
                                }
                                searchResult = new CmsSearchResult(
                                    Math.round((hits.scoreDocs[i].score / hits.getMaxScore()) * 100f),
                                    doc,
                                    excerpt);
                                searchResults.add(searchResult);
                            }
                            cnt++;
                        } else {
                            visibleHitCount--;
                        }
                    } catch (Exception e) {
                        // should not happen, but if it does we want to go on with the next result nevertheless                        
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
                        }
                    }
                }

                // save the total count of search results
                searchResults.setHitCount(visibleHitCount);
            } else {
                searchResults.setHitCount(0);
            }

            timeResultProcessing += System.currentTimeMillis();
        } catch (RuntimeException e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        } catch (Exception e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        } finally {

            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);
        }

        if (LOG.isDebugEnabled()) {
            timeTotal += System.currentTimeMillis();
            Object[] logParams = new Object[] {
                new Integer(hits == null ? 0 : hits.totalHits),
                new Long(timeTotal),
                new Long(timeLucene),
                new Long(timeResultProcessing)};
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_STAT_RESULTS_TIME_4, logParams));
        }

        return searchResults;
    }

    /**
     * Sets the Lucene analyzer used for this index.<p>
     *
     * @param analyzer the Lucene analyzer to set
     */
    public void setAnalyzer(Analyzer analyzer) {

        m_analyzer = analyzer;
    }

    /**
     * Sets the number of how many hits are loaded at maximum.<p> 
     * 
     * This must be set at least to 50, or this setting is ignored.<p>
     * 
     * @param maxHits the number of how many hits are loaded at maximum to set
     * 
     * @see #getMaxHits()
     * 
     * @since 7.5.1
     */
    public void setMaxHits(int maxHits) {

        if (m_maxHits >= (MAX_HITS_DEFAULT / 100)) {
            m_maxHits = maxHits;
        }
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#shutDown()
     */
    @Override
    public void shutDown() {

        super.shutDown();
        indexSearcherClose();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_INDEX_1, getName()));
        }

    }

    /**
     * Appends the a category filter to the given filter clause that matches all given categories.<p>
     * 
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param categories the categories that will compose the filter
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendCategoryFilter(CmsObject cms, BooleanFilter filter, List<String> categories) {

        if ((categories != null) && (categories.size() > 0)) {
            // add query categories (if required)

            // categories are indexed as lower-case strings 
            // @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendCategories
            List<String> lowerCaseCategories = new ArrayList<String>();
            for (String category : categories) {
                lowerCaseCategories.add(category.toLowerCase());
            }
            filter.add(new FilterClause(
                getMultiTermQueryFilter(I_CmsSearchField.FIELD_CATEGORY, lowerCaseCategories),
                BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Appends a date of creation filter to the given filter clause that matches the
     * given time range.<p>
     * 
     * If the start time is equal to {@link Long#MIN_VALUE} and the end time is equal to {@link Long#MAX_VALUE}  
     * than the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param filter the filter to extend
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendDateCreatedFilter(BooleanFilter filter, long startTime, long endTime) {

        // create special optimized sub-filter for the date last modified search
        Filter dateFilter = createDateRangeFilter(I_CmsSearchField.FIELD_DATE_CREATED_LOOKUP, startTime, endTime);
        if (dateFilter != null) {
            // extend main filter with the created date filter
            filter.add(new FilterClause(dateFilter, BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Appends a date of last modification filter to the given filter clause that matches the
     * given time range.<p>
     * 
     * If the start time is equal to {@link Long#MIN_VALUE} and the end time is equal to {@link Long#MAX_VALUE}  
     * than the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param filter the filter to extend
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendDateLastModifiedFilter(BooleanFilter filter, long startTime, long endTime) {

        // create special optimized sub-filter for the date last modified search
        Filter dateFilter = createDateRangeFilter(I_CmsSearchField.FIELD_DATE_LASTMODIFIED_LOOKUP, startTime, endTime);
        if (dateFilter != null) {
            // extend main filter with the created date filter
            filter.add(new FilterClause(dateFilter, BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Appends the a VFS path filter to the given filter clause that matches all given root paths.<p>
     * 
     * In case the provided List is null or empty, the current request context site root is appended.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param roots the VFS root paths that will compose the filter
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendPathFilter(CmsObject cms, BooleanFilter filter, List<String> roots) {

        // complete the search root
        TermsFilter pathFilter = new TermsFilter();
        if ((roots != null) && (roots.size() > 0)) {
            // add the all configured search roots with will request context
            for (int i = 0; i < roots.size(); i++) {
                String searchRoot = cms.getRequestContext().addSiteRoot(roots.get(i));
                extendPathFilter(pathFilter, searchRoot);
            }
        } else {
            // use the current site root as the search root
            extendPathFilter(pathFilter, cms.getRequestContext().getSiteRoot());
            // also add the shared folder (v 8.0)
            if (OpenCms.getSiteManager().getSharedFolder() != null) {
                extendPathFilter(pathFilter, OpenCms.getSiteManager().getSharedFolder());
            }
        }

        // add the calculated path filter for the root path
        filter.add(new FilterClause(pathFilter, BooleanClause.Occur.MUST));
        return filter;
    }

    /**
     * Appends the a resource type filter to the given filter clause that matches all given resource types.<p>
     * 
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     * 
     * The original filter parameter is extended and also provided as return value.<p> 
     * 
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param resourceTypes the resource types that will compose the filter
     * 
     * @return the extended filter clause
     */
    protected BooleanFilter appendResourceTypeFilter(CmsObject cms, BooleanFilter filter, List<String> resourceTypes) {

        if ((resourceTypes != null) && (resourceTypes.size() > 0)) {
            // add query resource types (if required)
            filter.add(new FilterClause(
                getMultiTermQueryFilter(I_CmsSearchField.FIELD_TYPE, resourceTypes),
                BooleanClause.Occur.MUST));
        }

        return filter;
    }

    /**
     * Creates an optimized date range filter for the date of last modification or creation.<p>
     * 
     * If the start date is equal to {@link Long#MIN_VALUE} and the end date is equal to {@link Long#MAX_VALUE}  
     * than <code>null</code> is returned.<p>
     * 
     * @param fieldName the name of the field to search
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     * 
     * @return an optimized date range filter for the date of last modification or creation
     */
    protected Filter createDateRangeFilter(String fieldName, long startTime, long endTime) {

        TermsFilter filter = null;
        if ((startTime != Long.MIN_VALUE) || (endTime != Long.MAX_VALUE)) {
            // a date range has been set for this LGT document search
            if (startTime == Long.MIN_VALUE) {
                // default start will always be "yyyy1231" in order to reduce term size                    
                Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
                cal.setTimeInMillis(endTime);
                cal.set(cal.get(Calendar.YEAR) - MAX_YEAR_RANGE, 11, 31, 0, 0, 0);
                startTime = cal.getTimeInMillis();
            } else if (endTime == Long.MAX_VALUE) {
                // default end will always be "yyyy0101" in order to reduce term size                    
                Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
                cal.setTimeInMillis(startTime);
                cal.set(cal.get(Calendar.YEAR) + MAX_YEAR_RANGE, 0, 1, 0, 0, 0);
                endTime = cal.getTimeInMillis();
            }
            // create the filter for the date
            filter = new TermsFilter();
            // get the list of all possible date range options
            List<String> dateRage = getDateRangeSpan(startTime, endTime);
            Iterator<String> i = dateRage.iterator();
            while (i.hasNext()) {
                // generate one term query per date range option
                filter.addTerm(new Term(fieldName, i.next()));
            }
        }
        return filter;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#createIndexWriter(boolean, org.opencms.report.I_CmsReport)
     */
    @Override
    protected I_CmsIndexWriter createIndexWriter(boolean create, I_CmsReport report) throws CmsIndexException {

        indexWriterUnlock(report);
        IndexWriter indexWriter;
        try {
            File f = new File(getPath());
            if (!f.exists()) {
                f = f.getParentFile();
                if ((f != null) && (!f.exists())) {
                    f.mkdirs();
                }

                create = true;
            }

            FSDirectory dir = FSDirectory.open(new File(getPath()));

            LogMergePolicy mergePolicy = new LogByteSizeMergePolicy();
            if (m_luceneMaxMergeDocs != null) {
                mergePolicy.setMaxMergeDocs(m_luceneMaxMergeDocs.intValue());
            }
            if (m_luceneMergeFactor != null) {
                mergePolicy.setMergeFactor(m_luceneMergeFactor.intValue());
            }
            if (m_luceneUseCompoundFile != null) {
                mergePolicy.setUseCompoundFile(m_luceneUseCompoundFile.booleanValue());
            }

            IndexWriterConfig indexConfig = new IndexWriterConfig(LUCENE_VERSION, getAnalyzer());

            if (m_luceneRAMBufferSizeMB != null) {
                indexConfig.setRAMBufferSizeMB(m_luceneRAMBufferSizeMB.doubleValue());
            }
            if (create) {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            indexWriter = new IndexWriter(dir, indexConfig);
        } catch (Exception e) {
            throw new CmsIndexException(Messages.get().container(
                Messages.ERR_IO_INDEX_WRITER_OPEN_2,
                getPath(),
                getName()), e);
        }

        return new CmsLuceneIndexWriter(indexWriter, this);

    }

    /**
     * Extends the given path query with another term for the given search root element.<p>
     * 
     * @param pathFilter the path filter to extend
     * @param searchRoot the search root to add to the path query
     */
    protected void extendPathFilter(TermsFilter pathFilter, String searchRoot) {

        if (!CmsResource.isFolder(searchRoot)) {
            searchRoot += "/";
        }
        pathFilter.addTerm(new Term(I_CmsSearchField.FIELD_PARENT_FOLDERS, searchRoot));
    }

    @Override
    protected String generateIndexDirectory() {

        return OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + getName());
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     * 
     * @param field the field to use
     * @param terms the term to use
     * 
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected Filter getMultiTermQueryFilter(String field, List<String> terms) {

        return getMultiTermQueryFilter(field, null, terms);
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     * 
     * @param field the field to use
     * @param terms the term to use
     * 
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected Filter getMultiTermQueryFilter(String field, String terms) {

        return getMultiTermQueryFilter(field, terms, null);
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     * 
     * @param field the field to use
     * @param termsStr the terms to use as a String separated by a space ' ' char
     * @param termsList the list of terms to use
     * 
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected Filter getMultiTermQueryFilter(String field, String termsStr, List<String> termsList) {

        if (termsStr == null) {
            StringBuffer buf = new StringBuffer(64);
            for (int i = 0; i < termsList.size(); i++) {
                if (i > 0) {
                    buf.append(' ');
                }
                buf.append(termsList.get(i));
            }
            termsStr = buf.toString();
        }
        Filter result = m_displayFilters.get((new StringBuffer(64)).append(field).append('|').append(termsStr).toString());
        if (result == null) {
            TermsFilter filter = new TermsFilter();
            if (termsList == null) {
                termsList = CmsStringUtil.splitAsList(termsStr, ' ');
            }
            for (int i = 0; i < termsList.size(); i++) {
                filter.addTerm(new Term(field, termsList.get(i)));
            }
            result = new CachingWrapperFilter(filter);
            m_displayFilters.put(field + termsStr, result);
        }
        return result;
    }

    /**
     * Returns a cached Lucene term query filter for the given field and term.<p>
     * 
     * @param field the field to use
     * @param term the term to use
     * 
     * @return a cached Lucene term query filter for the given field and term
     */
    protected Filter getTermQueryFilter(String field, String term) {

        return getMultiTermQueryFilter(field, term, Collections.singletonList(term));
    }

    /**
     * Closes the Lucene index searcher for this index.<p>
     * 
     * @see #indexSearcherOpen(String)
     */
    @Override
    protected synchronized void indexSearcherClose() {

        indexSearcherClose(m_indexSearcher);
    }

    /**
     * Closes the given Lucene index searcher.<p>
     * 
     * @param searcher the searcher to close
     */
    protected synchronized void indexSearcherClose(IndexSearcher searcher) {

        // in case there is an index searcher available close it
        if ((searcher != null) && (searcher.getIndexReader() != null)) {
            try {
                searcher.getIndexReader().close();
                searcher.close();
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_CLOSE_1, getName()), e);
            }
        }
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#indexSearcherOpen(java.lang.String)
     * 
     * Use {@link #getSearcher()} in order to obtain the searcher that has been opened.<p>
     */
    @Override
    protected synchronized void indexSearcherOpen(String path) {

        IndexSearcher oldSearcher = null;
        try {
            Directory indexDirectory = FSDirectory.open(new File(path));
            if (IndexReader.indexExists(indexDirectory)) {
                IndexReader reader = new LazyContentReader(IndexReader.open(indexDirectory));
                if (m_indexSearcher != null) {
                    // store old searcher instance to close it later
                    oldSearcher = m_indexSearcher;
                }
                m_indexSearcher = new IndexSearcher(reader);
                m_displayFilters = new HashMap<String, Filter>();
            }
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
        }
        if (oldSearcher != null) {
            // close the old searcher if required
            indexSearcherClose(oldSearcher);
        }
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#indexSearcherUpdate()
     */
    @Override
    protected synchronized void indexSearcherUpdate() {

        IndexSearcher oldSearcher = m_indexSearcher;
        if ((oldSearcher != null) && (oldSearcher.getIndexReader() != null)) {
            // in case there is an index searcher available close it
            try {
                IndexReader newReader = IndexReader.openIfChanged(oldSearcher.getIndexReader());
                if (newReader != null) {
                    m_indexSearcher = new IndexSearcher(newReader);
                    indexSearcherClose(oldSearcher);
                }
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_REOPEN_1, getName()), e);
            }
        } else {
            // make sure we end up with an open index searcher / reader           
            indexSearcherOpen(getPath());
        }
    }

    /**
     * Unlocks the Lucene index writer of this index if required.<p>
     * 
     * @param report the report to write error messages on
     * 
     * @throws CmsIndexException if unlocking of the index is impossible for any reason
     */
    protected void indexWriterUnlock(I_CmsReport report) throws CmsIndexException {

        File indexPath = new File(getPath());
        boolean indexLocked = true;
        // check if the target index path already exists
        if (indexPath.exists()) {
            Directory indexDirectory = null;
            // get the lock state of the given index            
            try {
                indexDirectory = FSDirectory.open(indexPath);
                indexLocked = IndexWriter.isLocked(indexDirectory);
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_IO_INDEX_READER_OPEN_2, getPath(), getName()), e);
            }

            // if index is locked try unlocking
            if (indexLocked) {
                try {
                    // try to force unlock on the index
                    IndexWriter.unlock(indexDirectory);
                } catch (Exception e) {
                    // unable to force unlock of Lucene index, we can't continue this way
                    CmsMessageContainer msg = Messages.get().container(Messages.ERR_INDEX_LOCK_FAILED_1, getName());
                    report.println(msg, I_CmsReport.FORMAT_ERROR);
                    throw new CmsIndexException(msg, e);
                }
            }
        }
    }

    /**
     * Checks if the document is in the time range specified in the search parameters.<p>
     * 
     * The creation date and/or the last modification date are checked.<p>
     * 
     * @param doc the document to check the dates against the given time range
     * @param params the search parameters where the time ranges are specified
     * 
     * @return true if document is in time range or not time range set otherwise false
     */
    protected boolean isInTimeRange(Document doc, CmsSearchParameters params) {

        if (!isCheckingTimeRange()) {
            // time range check disabled
            return true;
        }

        try {
            // check the creation date of the document against the given time range
            Date dateCreated = DateTools.stringToDate(doc.getFieldable(I_CmsSearchField.FIELD_DATE_CREATED).stringValue());
            if (dateCreated.getTime() < params.getMinDateCreated()) {
                return false;
            }
            if (dateCreated.getTime() > params.getMaxDateCreated()) {
                return false;
            }

            // check the last modification date of the document against the given time range
            Date dateLastModified = DateTools.stringToDate(doc.getFieldable(I_CmsSearchField.FIELD_DATE_LASTMODIFIED).stringValue());
            if (dateLastModified.getTime() < params.getMinDateLastModified()) {
                return false;
            }
            if (dateLastModified.getTime() > params.getMaxDateLastModified()) {
                return false;
            }

        } catch (ParseException ex) {
            // date could not be parsed -> doc is in time range
        }

        return true;
    }

    /**
     * Checks if the score for the results must be calculated based on the provided sort option.<p>  
     * 
     * Since Lucene 3 apparently the score is no longer calculated by default, but only if the 
     * searcher is explicitly told so. This methods checks if, based on the given sort, 
     * the score must be calculated.<p> 
     * 
     * @param searcher the index searcher to prepare 
     * @param sort the sort option to use
     */
    protected void prepareSortScoring(IndexSearcher searcher, Sort sort) {

        boolean doScoring = false;
        if (sort != null) {
            if ((sort == CmsSearchParameters.SORT_DEFAULT) || (sort == CmsSearchParameters.SORT_TITLE)) {
                // these default sorts do need score calculation
                doScoring = true;
            } else if ((sort == CmsSearchParameters.SORT_DATE_CREATED)
                || (sort == CmsSearchParameters.SORT_DATE_LASTMODIFIED)) {
                // these default sorts don't need score calculation
                doScoring = false;
            } else {
                // for all non-defaults: check if the score field is present, in that case we must calculate the score
                SortField[] fields = sort.getSort();
                for (SortField field : fields) {
                    if (field == SortField.FIELD_SCORE) {
                        doScoring = true;
                        break;
                    }
                }
            }
        }
        searcher.setDefaultFieldSortScoring(doScoring, doScoring);
    }
}
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

package org.opencms.search;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.solr.uninverting.UninvertingReader;
import org.apache.solr.uninverting.UninvertingReader.Type;

/**
 * Abstract search index implementation.<p>
 */
public class CmsSearchIndex extends A_CmsSearchIndex {

    /** A constant for the full qualified name of the CmsSearchIndex class. */
    public static final String A_PARAM_PREFIX = "org.opencms.search.CmsSearchIndex";

    /** Constant for additional parameter to enable optimized full index regeneration (default: false). */
    public static final String BACKUP_REINDEXING = A_PARAM_PREFIX + ".useBackupReindexing";

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
        CmsSearchField.FIELD_META,
        CmsSearchField.FIELD_CONTENT};

    /** Constant for additional parameter to enable excerpt creation (default: true). */
    public static final String EXCERPT = A_PARAM_PREFIX + ".createExcerpt";

    /** Constant for additional parameter for index content extraction. */
    public static final String EXTRACT_CONTENT = A_PARAM_PREFIX + ".extractContent";

    /** Constant for additional parameter to enable/disable language detection (default: false). */
    public static final String IGNORE_EXPIRATION = A_PARAM_PREFIX + ".ignoreExpiration";

    /** Constant for additional parameter to enable/disable language detection (default: false). */
    public static final String LANGUAGEDETECTION = "search.solr.useLanguageDetection";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_AUTO_COMMIT = "lucene.AutoCommit";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_RAM_BUFFER_SIZE_MB = "lucene.RAMBufferSizeMB";

    /** Constant for additional parameter for controlling how many hits are loaded at maximum (default: 1000). */
    public static final String MAX_HITS = A_PARAM_PREFIX + ".maxHits";

    /** Indicates how many hits are loaded at maximum by default. */
    public static final int MAX_HITS_DEFAULT = 5000;

    /** Constant for years max range span in document search. */
    public static final int MAX_YEAR_RANGE = 25;

    /** Constant for additional parameter to enable permission checks (default: true). */
    public static final String PERMISSIONS = A_PARAM_PREFIX + ".checkPermissions";

    /** Constant for additional parameter to set the thread priority during search. */
    public static final String PRIORITY = A_PARAM_PREFIX + ".priority";

    /** Constant for additional parameter to enable time range checks (default: true). */
    public static final String TIME_RANGE = A_PARAM_PREFIX + ".checkTimeRange";

    /**
     * A stored field visitor, that does not return the large fields: "content" and "contentblob".<p>
     */
    protected static final StoredFieldVisitor VISITOR = new StoredFieldVisitor() {

        /**
         * @see org.apache.lucene.index.StoredFieldVisitor#needsField(org.apache.lucene.index.FieldInfo)
         */
        @Override
        public Status needsField(FieldInfo fieldInfo) {

            return !CmsSearchFieldConfiguration.LAZY_FIELDS.contains(fieldInfo.name) ? Status.YES : Status.NO;
        }
    };

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndex.class);

    /** The serial version id. */
    private static final long serialVersionUID = 8461682478204452718L;

    /** The configured Lucene analyzer used for this index. */
    private transient Analyzer m_analyzer;

    /** Indicates if backup re-indexing is used by this index. */
    private boolean m_backupReindexing;

    /** The permission check mode for this index. */
    private boolean m_checkPermissions;

    /** The time range check mode for this index. */
    private boolean m_checkTimeRange;

    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Map of display query filters to use. */
    private transient Map<String, Query> m_displayFilters;

    /**
     * Signals whether expiration dates should be ignored when checking permissions or not.<p>
     * @see #IGNORE_EXPIRATION
     */
    private boolean m_ignoreExpiration;

    /** The Lucene index searcher to use. */
    private transient IndexSearcher m_indexSearcher;

    /** The Lucene index RAM buffer size, see {@link IndexWriterConfig#setRAMBufferSizeMB(double)}. */
    private Double m_luceneRAMBufferSizeMB;

    /** Indicates how many hits are loaded at maximum. */
    private int m_maxHits;

    /** The thread priority for a search. */
    private int m_priority;

    /** Controls if a resource requires view permission to be displayed in the result list. */
    private boolean m_requireViewPermission;

    /** The cms specific Similarity implementation. */
    private final transient Similarity m_sim = new CmsSearchSimilarity();

    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     *
     * It is recommended to use the constructor <code>{@link #CmsSearchIndex(String)}</code>
     * as it enforces the mandatory name argument. <p>
     */
    public CmsSearchIndex() {

        super();
        m_checkPermissions = true;
        m_priority = -1;
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
    public CmsSearchIndex(String name)
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
     */
    @Override
    public void addConfigurationParameter(String key, String value) {

        if (PERMISSIONS.equals(key)) {
            m_checkPermissions = Boolean.valueOf(value).booleanValue();
        } else if (EXTRACT_CONTENT.equals(key)) {
            setExtractContent(Boolean.valueOf(value).booleanValue());
        } else if (BACKUP_REINDEXING.equals(key)) {
            m_backupReindexing = Boolean.valueOf(value).booleanValue();
        } else if (LANGUAGEDETECTION.equals(key)) {
            setLanguageDetection(Boolean.valueOf(value).booleanValue());
        } else if (IGNORE_EXPIRATION.equals(key)) {
            m_ignoreExpiration = Boolean.valueOf(value).booleanValue();
        } else if (PRIORITY.equals(key)) {
            m_priority = Integer.parseInt(value);
            if (m_priority < Thread.MIN_PRIORITY) {
                m_priority = Thread.MIN_PRIORITY;
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_SEARCH_PRIORITY_TOO_LOW_2,
                        value,
                        Integer.valueOf(Thread.MIN_PRIORITY)));

            } else if (m_priority > Thread.MAX_PRIORITY) {
                m_priority = Thread.MAX_PRIORITY;
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_SEARCH_PRIORITY_TOO_HIGH_2,
                        value,
                        Integer.valueOf(Thread.MAX_PRIORITY)));
            }
        }

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
        } else if (CmsSearchIndex.EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();

        } else if (LUCENE_RAM_BUFFER_SIZE_MB.equals(key)) {
            try {
                m_luceneRAMBufferSizeMB = Double.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        }
    }

    /**
     * Creates an empty document that can be used by this search field configuration.<p>
     *
     * @param resource the resource to create the document for
     *
     * @return a new and empty document
     */
    public I_CmsSearchDocument createEmptyDocument(CmsResource resource) {

        return new CmsLuceneDocument(new Document());
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

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        if (getPriority() > 0) {
            result.put(PRIORITY, String.valueOf(m_priority));
        }
        if (!isExtractingContent()) {
            result.put(EXTRACT_CONTENT, String.valueOf(isExtractingContent()));
        }
        if (!isCheckingPermissions()) {
            result.put(PERMISSIONS, String.valueOf(m_checkPermissions));
        }
        if (isBackupReindexing()) {
            result.put(BACKUP_REINDEXING, String.valueOf(m_backupReindexing));
        }
        if (isLanguageDetection()) {
            result.put(LANGUAGEDETECTION, String.valueOf(isLanguageDetection()));
        }
        if (getMaxHits() != MAX_HITS_DEFAULT) {
            result.put(MAX_HITS, String.valueOf(getMaxHits()));
        }
        if (!isCreatingExcerpt()) {
            result.put(EXCERPT, String.valueOf(m_createExcerpt));
        }
        if (m_luceneRAMBufferSizeMB != null) {
            result.put(LUCENE_RAM_BUFFER_SIZE_MB, String.valueOf(m_luceneRAMBufferSizeMB));
        }
        // always write time range check parameter because of logic change in OpenCms 8.0
        result.put(TIME_RANGE, String.valueOf(m_checkTimeRange));
        return result;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getContentIfUnchanged(org.opencms.file.CmsResource)
     */
    @Override
    public I_CmsExtractionResult getContentIfUnchanged(CmsResource resource) {

        // compare "date of last modification of content" from Lucene index and OpenCms VFS
        // if this is identical, then the data from the Lucene index can be re-used
        I_CmsSearchDocument oldDoc = getDocument(CmsSearchField.FIELD_PATH, resource.getRootPath());
        // first check if the document is already in the index
        if ((oldDoc != null) && (oldDoc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_CONTENT) != null)) {
            long contentDateIndex = oldDoc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_CONTENT).getTime();
            // now compare the date with the date stored in the resource
            // we truncate to seconds, since the index stores no milliseconds
            // and it seems practically irrelevant that a content is updated twice in a second.
            if ((contentDateIndex / 1000L) == (resource.getDateContent() / 1000L)) {
                // extract stored content blob from index
                return CmsExtractionResult.fromBytes(oldDoc.getContentBlob());
            }
        }
        return null;
    }

    /**
     * Returns a document by document ID.<p>
     *
     * @param docId the id to get the document for
     *
     * @return the CMS specific document
     */
    public I_CmsSearchDocument getDocument(int docId) {

        try {
            IndexSearcher searcher = getSearcher();
            return new CmsLuceneDocument(searcher.doc(docId));
        } catch (IOException e) {
            // ignore, return null and assume document was not found
        }
        return null;
    }

    /**
     * Returns the Lucene document with the given root path from the index.<p>
     *
     * @param rootPath the root path of the document to get
     *
     * @return the Lucene document with the given root path from the index
     *
     * @deprecated Use {@link #getDocument(String, String)} instead and provide {@link org.opencms.search.fields.CmsLuceneField#FIELD_PATH} as field to search in
     */
    @Deprecated
    public Document getDocument(String rootPath) {

        if (getDocument(CmsSearchField.FIELD_PATH, rootPath) != null) {
            return (Document)getDocument(CmsSearchField.FIELD_PATH, rootPath).getDocument();
        }
        return null;
    }

    /**
     * Returns the first document where the given term matches the selected index field.<p>
     *
     * Use this method to search for documents which have unique field values, like a unique id.<p>
     *
     * @param field the field to search in
     * @param term the term to search for
     *
     * @return the first document where the given term matches the selected index field
     */
    public I_CmsSearchDocument getDocument(String field, String term) {

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
     * Returns the language locale for the given resource in this index.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the resource to check
     * @param availableLocales a list of locales supported by the resource
     *
     * @return the language locale for the given resource in this index
     */
    @Override
    public Locale getLocaleForResource(CmsObject cms, CmsResource resource, List<Locale> availableLocales) {

        Locale result;
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
        List<Locale> locales = availableLocales;
        if ((locales == null) || (locales.size() == 0)) {
            locales = defaultLocales;
        }
        result = OpenCms.getLocaleManager().getBestMatchingLocale(getLocale(), defaultLocales, locales);
        return result;
    }

    /**
    * Returns the language locale of the index as a String.<p>
    *
    * @return the language locale of the index as a String
    *
    * @see #getLocale()
    */
    public String getLocaleString() {

        return getLocale().toString();
    }

    /**
     * Indicates the number of how many hits are loaded at maximum.<p>
     *
     * The number of maximum documents to load from the index
     * must be specified. The default of this setting is {@link CmsSearchIndex#MAX_HITS_DEFAULT} (5000).
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
     * Returns the path where this index stores it's data in the "real" file system.<p>
     *
     * @return the path where this index stores it's data in the "real" file system
     */
    @Override
    public String getPath() {

        if (super.getPath() == null) {
            setPath(generateIndexDirectory());
        }
        return super.getPath();
    }

    /**
     * Returns the Thread priority for this search index.<p>
     *
     * @return the Thread priority for this search index
     */
    public int getPriority() {

        return m_priority;
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
        @SuppressWarnings("resource")
        Analyzer baseAnalyzer = OpenCms.getSearchManager().getAnalyzer(getLocale());

        if (getFieldConfiguration() instanceof CmsLuceneFieldConfiguration) {
            CmsLuceneFieldConfiguration fc = (CmsLuceneFieldConfiguration)getFieldConfiguration();
            setAnalyzer(fc.getAnalyzer(baseAnalyzer));
        }
    }

    /**
     * Returns <code>true</code> if backup re-indexing is done by this index.<p>
     *
     * This is an optimization method by which the old extracted content is
     * reused in order to save performance when re-indexing.<p>
     *
     * @return  <code>true</code> if backup re-indexing is done by this index
     *
     * @since 7.5.1
     */
    public boolean isBackupReindexing() {

        return m_backupReindexing;
    }

    /**
     * Returns <code>true</code> if permissions are checked for search results by this index.<p>
     *
     * If permission checks are not required, they can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not checking permissions will improve performance.<p>
     *
     * This is can be of use in scenarios when you know that all search results are always readable,
     * which is usually true for public websites that do not have personalized accounts.<p>
     *
     * Please note that even if a result is returned where the current user has no read permissions,
     * the user can not actually access this document. It will only appear in the search result list,
     * but if the user clicks the link to open the document he will get an error.<p>
     *
     *
     * @return <code>true</code> if permissions are checked for search results by this index
     */
    public boolean isCheckingPermissions() {

        return m_checkPermissions;
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
     * Returns the checkPermissions.<p>
     *
     * @return the checkPermissions
     */
    public boolean isCheckPermissions() {

        return m_checkPermissions;
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
     * Returns the ignoreExpiration.<p>
     *
     * @return the ignoreExpiration
     */
    public boolean isIgnoreExpiration() {

        return m_ignoreExpiration;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#isInitialized()
     */
    @Override
    public boolean isInitialized() {

        return super.isInitialized() && (null != getPath());
    }

    /**
     * Returns <code>true</code> if a resource requires read permission to be included in the result list.<p>
     *
     * @return <code>true</code> if a resource requires read permission to be included in the result list
     */
    public boolean isRequireViewPermission() {

        return m_requireViewPermission;
    }

    /**
     * @see org.opencms.search.A_CmsSearchIndex#onIndexChanged(boolean)
     */
    @Override
    public void onIndexChanged(boolean force) {

        if (force) {
            indexSearcherOpen(getPath());
        } else {
            indexSearcherUpdate();
        }
    }

    /**
     * Performs a search on the index within the given fields.<p>
     *
     * The result is returned as List with entries of type I_CmsSearchResult.<p>
     *
     * @param cms the current user's Cms object
     * @param params the parameters to use for the search
     *
     * @return the List of results found or an empty list
     *
     * @throws CmsSearchException if something goes wrong
     */
    public CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) throws CmsSearchException {

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
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            // append root path filter
            builder = appendPathFilter(searchCms, builder, params.getRoots());
            // append category filter
            builder = appendCategoryFilter(searchCms, builder, params.getCategories());
            // append resource type filter
            builder = appendResourceTypeFilter(searchCms, builder, params.getResourceTypes());

            // append date last modified filter
            builder = appendDateLastModifiedFilter(
                builder,
                params.getMinDateLastModified(),
                params.getMaxDateLastModified());
            // append date created filter
            builder = appendDateCreatedFilter(builder, params.getMinDateCreated(), params.getMaxDateCreated());

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
                    QueryParser p = new QueryParser(CmsSearchField.FIELD_CONTENT, getAnalyzer());
                    fieldsQuery = p.parse(params.getParsedQuery());
                } else if (params.getFieldQueries() != null) {
                    // each field has an individual query
                    BooleanQuery.Builder mustOccur = null;
                    BooleanQuery.Builder shouldOccur = null;
                    for (CmsSearchParameters.CmsSearchFieldQuery fq : params.getFieldQueries()) {
                        // add one sub-query for each defined field
                        QueryParser p = new QueryParser(fq.getFieldName(), getAnalyzer());
                        // first generate the combined keyword query
                        Query keywordQuery = null;
                        if (fq.getSearchTerms().size() == 1) {
                            // this is just a single size keyword list
                            keywordQuery = p.parse(fq.getSearchTerms().get(0));
                        } else {
                            // multiple size keyword list
                            BooleanQuery.Builder keywordListQuery = new BooleanQuery.Builder();
                            for (String keyword : fq.getSearchTerms()) {
                                keywordListQuery.add(p.parse(keyword), fq.getTermOccur());
                            }
                            keywordQuery = keywordListQuery.build();
                        }
                        if (BooleanClause.Occur.SHOULD.equals(fq.getOccur())) {
                            if (shouldOccur == null) {
                                shouldOccur = new BooleanQuery.Builder();
                            }
                            shouldOccur.add(keywordQuery, fq.getOccur());
                        } else {
                            if (mustOccur == null) {
                                mustOccur = new BooleanQuery.Builder();
                            }
                            mustOccur.add(keywordQuery, fq.getOccur());
                        }
                    }
                    BooleanQuery.Builder booleanFieldsQuery = new BooleanQuery.Builder();
                    if (mustOccur != null) {
                        booleanFieldsQuery.add(mustOccur.build(), BooleanClause.Occur.MUST);
                    }
                    if (shouldOccur != null) {
                        booleanFieldsQuery.add(shouldOccur.build(), BooleanClause.Occur.MUST);
                    }
                    fieldsQuery = searcher.rewrite(booleanFieldsQuery.build());
                } else if ((params.getFields() != null) && (params.getFields().size() > 0)) {
                    // no individual field queries have been defined, so use one query for all fields
                    BooleanQuery.Builder booleanFieldsQuery = new BooleanQuery.Builder();
                    // this is a "regular" query over one or more fields
                    // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                    for (int i = 0; i < params.getFields().size(); i++) {
                        QueryParser p = new QueryParser(params.getFields().get(i), getAnalyzer());
                        p.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_REWRITE);
                        booleanFieldsQuery.add(p.parse(params.getQuery()), BooleanClause.Occur.SHOULD);
                    }
                    fieldsQuery = searcher.rewrite(booleanFieldsQuery.build());
                } else {
                    // if no fields are provided, just use the "content" field by default
                    QueryParser p = new QueryParser(CmsSearchField.FIELD_CONTENT, getAnalyzer());
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
                params.setParsedQuery(query.toString(CmsSearchField.FIELD_CONTENT));
            }

            // build the final query
            final BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
            finalQueryBuilder.add(query, BooleanClause.Occur.MUST);
            finalQueryBuilder.add(builder.build(), BooleanClause.Occur.FILTER);
            final BooleanQuery finalQuery = finalQueryBuilder.build();

            // collect the categories
            CmsSearchCategoryCollector categoryCollector;
            if (params.isCalculateCategories()) {
                // USE THIS OPTION WITH CAUTION
                // this may slow down searched by an order of magnitude
                categoryCollector = new CmsSearchCategoryCollector(searcher);
                // perform a first search to collect the categories
                searcher.search(finalQuery, categoryCollector);
                // store the result
                searchResults.setCategories(categoryCollector.getCategoryCountResult());
            }

            // get maxScore first, since Lucene 8, it's not computed automatically anymore
            TopDocs scoreHits = searcher.search(query, 1);
            float maxScore = scoreHits.scoreDocs.length == 0 ? Float.NaN : scoreHits.scoreDocs[0].score;
            // perform the search operation
            if ((params.getSort() == null) || (params.getSort() == CmsSearchParameters.SORT_DEFAULT)) {
                // apparently scoring is always enabled by Lucene if no sort order is provided
                hits = searcher.search(finalQuery, getMaxHits());
            } else {
                // if  a sort order is provided, we must check if scoring must be calculated by the searcher
                boolean isSortScore = isSortScoring(searcher, params.getSort());
                hits = searcher.search(finalQuery, getMaxHits(), params.getSort(), isSortScore);
            }

            timeLucene += System.currentTimeMillis();
            timeResultProcessing = -System.currentTimeMillis();

            if (hits != null) {
                long hitCount = hits.totalHits.value > hits.scoreDocs.length
                ? hits.scoreDocs.length
                : hits.totalHits.value;
                int page = params.getSearchPage();
                long start = -1, end = -1;
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

                Set<String> returnFields = ((CmsLuceneFieldConfiguration)getFieldConfiguration()).getReturnFields();
                Set<String> excerptFields = ((CmsLuceneFieldConfiguration)getFieldConfiguration()).getExcerptFields();

                long visibleHitCount = hitCount;
                for (int i = 0, cnt = 0; (i < hitCount) && (cnt < end); i++) {
                    try {
                        Document doc = searcher.doc(hits.scoreDocs[i].doc, returnFields);
                        I_CmsSearchDocument searchDoc = new CmsLuceneDocument(doc);
                        searchDoc.setScore(hits.scoreDocs[i].score);
                        if ((isInTimeRange(doc, params)) && (hasReadPermission(searchCms, searchDoc))) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document!
                                String excerpt = null;
                                if (isCreatingExcerpt() && (fieldsQuery != null)) {
                                    Document exDoc = searcher.doc(hits.scoreDocs[i].doc, excerptFields);
                                    I_CmsTermHighlighter highlighter = OpenCms.getSearchManager().getHighlighter();
                                    excerpt = highlighter.getExcerpt(exDoc, this, params, fieldsQuery, getAnalyzer());
                                }
                                int score = Math.round(
                                    (maxScore != Float.NaN ? (hits.scoreDocs[i].score / maxScore) * 100f : 0));
                                searchResults.add(new CmsSearchResult(score, doc, excerpt));
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
                searchResults.setHitCount((int)visibleHitCount);
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
                Long.valueOf(hits == null ? 0 : hits.totalHits.value),
                Long.valueOf(timeTotal),
                Long.valueOf(timeLucene),
                Long.valueOf(timeResultProcessing)};
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
     * Sets the checkPermissions.<p>
     *
     * @param checkPermissions the checkPermissions to set
     */
    public void setCheckPermissions(boolean checkPermissions) {

        m_checkPermissions = checkPermissions;
    }

    /**
     * Sets the ignoreExpiration.<p>
     *
     * @param ignoreExpiration the ignoreExpiration to set
     */
    public void setIgnoreExpiration(boolean ignoreExpiration) {

        m_ignoreExpiration = ignoreExpiration;
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
     * Controls if a resource requires view permission to be displayed in the result list.<p>
     *
     * By default this is <code>false</code>.<p>
     *
     * @param requireViewPermission controls if a resource requires view permission to be displayed in the result list
     */
    public void setRequireViewPermission(boolean requireViewPermission) {

        m_requireViewPermission = requireViewPermission;
    }

    /**
     * Shuts down the search index.<p>
     *
     * This will close the local Lucene index searcher instance.<p>
     */
    @Override
    public void shutDown() {

        super.shutDown();
        indexSearcherClose();
        if (m_analyzer != null) {
            m_analyzer.close();
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_INDEX_1, getName()));
        }
    }

    /**
     * Returns the name (<code>{@link #getName()}</code>) of this search index.<p>
     *
     * @return the name (<code>{@link #getName()}</code>) of this search index
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getName();
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
    protected BooleanQuery.Builder appendCategoryFilter(
        CmsObject cms,
        BooleanQuery.Builder filter,
        List<String> categories) {

        if ((categories != null) && (categories.size() > 0)) {
            // add query categories (if required)

            // categories are indexed as lower-case strings
            // @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendCategories
            List<String> lowerCaseCategories = new ArrayList<String>();
            for (String category : categories) {
                lowerCaseCategories.add(category.toLowerCase());
            }
            filter.add(
                new BooleanClause(
                    getMultiTermQueryFilter(CmsSearchField.FIELD_CATEGORY, lowerCaseCategories),
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
    protected BooleanQuery.Builder appendDateCreatedFilter(BooleanQuery.Builder filter, long startTime, long endTime) {

        // create special optimized sub-filter for the date last modified search
        Query dateFilter = createDateRangeFilter(CmsSearchField.FIELD_DATE_CREATED_LOOKUP, startTime, endTime);
        if (dateFilter != null) {
            // extend main filter with the created date filter
            filter.add(new BooleanClause(dateFilter, BooleanClause.Occur.MUST));
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
    protected BooleanQuery.Builder appendDateLastModifiedFilter(
        BooleanQuery.Builder filter,
        long startTime,
        long endTime) {

        // create special optimized sub-filter for the date last modified search
        Query dateFilter = createDateRangeFilter(CmsSearchField.FIELD_DATE_LASTMODIFIED_LOOKUP, startTime, endTime);
        if (dateFilter != null) {
            // extend main filter with the created date filter
            filter.add(new BooleanClause(dateFilter, BooleanClause.Occur.MUST));
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
    protected BooleanQuery.Builder appendPathFilter(CmsObject cms, BooleanQuery.Builder filter, List<String> roots) {

        // complete the search root
        List<Term> terms = new ArrayList<Term>();
        if ((roots != null) && (roots.size() > 0)) {
            // add the all configured search roots with will request context
            for (int i = 0; i < roots.size(); i++) {
                String searchRoot = cms.getRequestContext().addSiteRoot(roots.get(i));
                extendPathFilter(terms, searchRoot);
            }
        } else {
            // use the current site root as the search root
            extendPathFilter(terms, cms.getRequestContext().getSiteRoot());
            // also add the shared folder (v 8.0)
            if (OpenCms.getSiteManager().getSharedFolder() != null) {
                extendPathFilter(terms, OpenCms.getSiteManager().getSharedFolder());
            }
        }

        // add the calculated path filter for the root path
        BooleanQuery.Builder build = new BooleanQuery.Builder();
        terms.forEach(term -> build.add(new TermQuery(term), Occur.SHOULD));
        filter.add(new BooleanClause(build.build(), BooleanClause.Occur.MUST));
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
    protected BooleanQuery.Builder appendResourceTypeFilter(
        CmsObject cms,
        BooleanQuery.Builder filter,
        List<String> resourceTypes) {

        if ((resourceTypes != null) && (resourceTypes.size() > 0)) {
            // add query resource types (if required)
            filter.add(
                new BooleanClause(
                    getMultiTermQueryFilter(CmsSearchField.FIELD_TYPE, resourceTypes),
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
    protected Query createDateRangeFilter(String fieldName, long startTime, long endTime) {

        Query filter = null;
        if ((startTime != Long.MIN_VALUE) || (endTime != Long.MAX_VALUE)) {
            // a date range has been set for this document search
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

            // get the list of all possible date range options
            List<String> dateRange = getDateRangeSpan(startTime, endTime);
            List<Term> terms = new ArrayList<Term>();
            for (String range : dateRange) {
                terms.add(new Term(fieldName, range));
            }
            // create the filter for the date
            BooleanQuery.Builder build = new BooleanQuery.Builder();
            terms.forEach(term -> build.add(new TermQuery(term), Occur.SHOULD));
            filter = build.build();
        }
        return filter;
    }

    /**
     * Creates a backup of this index for optimized re-indexing of the whole content.<p>
     *
     * @return the path to the backup folder, or <code>null</code> in case no backup was created
     */
    protected String createIndexBackup() {

        if (!isBackupReindexing()) {
            // if no backup is generated we don't need to do anything
            return null;
        }

        // check if the target directory already exists
        File file = new File(getPath());
        if (!file.exists()) {
            // index does not exist yet, so we can't backup it
            return null;
        }
        String backupPath = getPath() + "_backup";
        FSDirectory oldDir = null;
        FSDirectory newDir = null;
        try {
            // open file directory for Lucene
            oldDir = FSDirectory.open(file.toPath());
            newDir = FSDirectory.open(Paths.get(backupPath));
            for (String fileName : oldDir.listAll()) {
                newDir.copyFrom(oldDir, fileName, fileName, IOContext.DEFAULT);
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_IO_INDEX_BACKUP_CREATE_3, getName(), getPath(), backupPath),
                e);
            backupPath = null;
        } finally {
            if (oldDir != null) {
                try {
                    oldDir.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (newDir != null) {
                try {
                    newDir.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return backupPath;
    }

    /**
     * Creates a new index writer.<p>
     *
     * @param create if <code>true</code> a whole new index is created, if <code>false</code> an existing index is updated
     * @param report the report
     *
     * @return the created new index writer
     *
     * @throws CmsIndexException in case the writer could not be created
     *
     * @see #getIndexWriter(I_CmsReport, boolean)
     */
    @Override
    protected I_CmsIndexWriter createIndexWriter(boolean create, I_CmsReport report) throws CmsIndexException {

        IndexWriter indexWriter = null;
        FSDirectory dir = null;
        try {
            File f = new File(getPath());
            if (!f.exists()) {
                f = f.getParentFile();
                if ((f != null) && (!f.exists())) {
                    f.mkdirs();
                }

                create = true;
            }

            dir = FSDirectory.open(Paths.get(getPath()));
            IndexWriterConfig indexConfig = new IndexWriterConfig(getAnalyzer());
            //indexConfig.setMergePolicy(mergePolicy);

            if (m_luceneRAMBufferSizeMB != null) {
                indexConfig.setRAMBufferSizeMB(m_luceneRAMBufferSizeMB.doubleValue());
            }
            if (create) {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }
            // register the modified default similarity implementation
            indexConfig.setSimilarity(m_sim);

            indexWriter = new IndexWriter(dir, indexConfig);
        } catch (Exception e) {
            if (dir != null) {
                try {
                    dir.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (IOException closeExeception) {
                    throw new CmsIndexException(
                        Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, getPath(), getName()),
                        e);
                }
            }
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, getPath(), getName()),
                e);
        }

        return new CmsLuceneIndexWriter(indexWriter, this);
    }

    /**
     * Extends the given path query with another term for the given search root element.<p>
     *
     * @param terms the path filter to extend
     * @param searchRoot the search root to add to the path query
     */
    protected void extendPathFilter(List<Term> terms, String searchRoot) {

        if (!CmsResource.isFolder(searchRoot)) {
            searchRoot += "/";
        }
        terms.add(new Term(CmsSearchField.FIELD_PARENT_FOLDERS, searchRoot));
    }

    /**
     * Generates the directory on the RFS for this index.<p>
     *
     * @return the directory on the RFS for this index
     */
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
    protected Query getMultiTermQueryFilter(String field, List<String> terms) {

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
    protected Query getMultiTermQueryFilter(String field, String terms) {

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
    protected Query getMultiTermQueryFilter(String field, String termsStr, List<String> termsList) {

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
        Query result = m_displayFilters.get(
            (new StringBuffer(64)).append(field).append('|').append(termsStr).toString());
        if (result == null) {
            List<Term> terms = new ArrayList<Term>();
            if (termsList == null) {
                termsList = CmsStringUtil.splitAsList(termsStr, ' ');
            }
            for (int i = 0; i < termsList.size(); i++) {
                terms.add(new Term(field, termsList.get(i)));
            }

            BooleanQuery.Builder build = new BooleanQuery.Builder();
            terms.forEach(term -> build.add(new TermQuery(term), Occur.SHOULD));
            Query termsQuery = build.build(); //termsFilter

            try {
                result = termsQuery.createWeight(m_indexSearcher, ScoreMode.COMPLETE_NO_SCORES, 1).getQuery();
                m_displayFilters.put(field + termsStr, result);
            } catch (IOException e) {
                // TODO don't know what happend
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Checks if the OpenCms resource referenced by the result document can be read
     * by the user of the given OpenCms context.
     *
     * Returns the referenced <code>CmsResource</code> or <code>null</code> if
     * the user is not permitted to read the resource.<p>
     *
     * @param cms the OpenCms user context to use for permission testing
     * @param doc the search result document to check
     *
     * @return the referenced <code>CmsResource</code> or <code>null</code> if the user is not permitted
     */
    protected CmsResource getResource(CmsObject cms, I_CmsSearchDocument doc) {

        // check if the resource exits in the VFS,
        // this will implicitly check read permission and if the resource was deleted
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
        if (isRequireViewPermission()) {
            filter = CmsResourceFilter.DEFAULT_ONLY_VISIBLE;
        } else if (isIgnoreExpiration()) {
            filter = CmsResourceFilter.IGNORE_EXPIRATION;
        }

        return getResource(cms, doc, filter);
    }

    /**
     * Checks if the OpenCms resource referenced by the result document can be read
     * by the user of the given OpenCms context.
     *
     * Returns the referenced <code>CmsResource</code> or <code>null</code> if
     * the user is not permitted to read the resource.<p>
     *
     * @param cms the OpenCms user context to use for permission testing
     * @param doc the search result document to check
     * @param filter the resource filter to apply
     *
     * @return the referenced <code>CmsResource</code> or <code>null</code> if the user is not permitted
     */
    protected CmsResource getResource(CmsObject cms, I_CmsSearchDocument doc, CmsResourceFilter filter) {

        try {
            CmsObject clone = OpenCms.initCmsObject(cms);
            clone.getRequestContext().setSiteRoot("");
            return clone.readResource(doc.getPath(), filter);
        } catch (CmsException e) {
            // Do nothing
        }

        return null;
    }

    /**
     * Returns a cached Lucene term query filter for the given field and term.<p>
     *
     * @param field the field to use
     * @param term the term to use
     *
     * @return a cached Lucene term query filter for the given field and term
     */
    protected Query getTermQueryFilter(String field, String term) {

        return getMultiTermQueryFilter(field, term, Collections.singletonList(term));
    }

    /**
     * Checks if the OpenCms resource referenced by the result document can be read
     * be the user of the given OpenCms context.<p>
     *
     * @param cms the OpenCms user context to use for permission testing
     * @param doc the search result document to check
     * @return <code>true</code> if the user has read permissions to the resource
     */
    protected boolean hasReadPermission(CmsObject cms, I_CmsSearchDocument doc) {

        // If no permission check is needed: the document can be read
        // Else try to read the resource if this is not possible the user does not have enough permissions
        return !needsPermissionCheck(doc) ? true : (null != getResource(cms, doc));
    }

    /**
     * Closes the index searcher for this index.<p>
     *
     * @see #indexSearcherOpen(String)
     */
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
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_CLOSE_1, getName()), e);
            }
        }
    }

    /**
     * Initializes the index searcher for this index.<p>
     *
     * In case there is an index searcher still open, it is closed first.<p>
     *
     * For performance reasons, one instance of the index searcher should be kept
     * for all searches. However, if the index is updated or changed
     * this searcher instance needs to be re-initialized.<p>
     *
     * @param path the path to the index directory
     */
    protected synchronized void indexSearcherOpen(String path) {

        IndexSearcher oldSearcher = null;
        Directory indexDirectory = null;
        try {
            indexDirectory = FSDirectory.open(Paths.get(path));
            if (DirectoryReader.indexExists(indexDirectory)) {
                IndexReader reader = UninvertingReader.wrap(
                    DirectoryReader.open(indexDirectory),
                    createUninvertingMap());
                if (m_indexSearcher != null) {
                    // store old searcher instance to close it later
                    oldSearcher = m_indexSearcher;
                }
                m_indexSearcher = new IndexSearcher(reader);
                m_indexSearcher.setSimilarity(m_sim);
                m_displayFilters = new HashMap<>();
            }
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
            if (indexDirectory != null) {
                try {
                    indexDirectory.close();
                } catch (IOException closeException) {
                    // do nothing
                }
            }
        }
        if (oldSearcher != null) {
            // close the old searcher if required
            indexSearcherClose(oldSearcher);
        }
    }

    /**
     * Reopens the index search reader for this index, required after the index has been changed.<p>
     *
     * @see #indexSearcherOpen(String)
     */
    protected synchronized void indexSearcherUpdate() {

        IndexSearcher oldSearcher = m_indexSearcher;
        if ((oldSearcher != null) && (oldSearcher.getIndexReader() != null)) {
            // in case there is an index searcher available close it
            try {
                if (oldSearcher.getIndexReader() instanceof DirectoryReader) {
                    IndexReader newReader = DirectoryReader.openIfChanged(
                        (DirectoryReader)oldSearcher.getIndexReader());
                    if (newReader != null) {
                        m_indexSearcher = new IndexSearcher(newReader);
                        m_indexSearcher.setSimilarity(m_sim);
                        indexSearcherClose(oldSearcher);
                    }
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
            Date dateCreated = DateTools.stringToDate(doc.getField(CmsSearchField.FIELD_DATE_CREATED).stringValue());
            if (dateCreated.getTime() < params.getMinDateCreated()) {
                return false;
            }
            if (dateCreated.getTime() > params.getMaxDateCreated()) {
                return false;
            }

            // check the last modification date of the document against the given time range
            Date dateLastModified = DateTools.stringToDate(
                doc.getField(CmsSearchField.FIELD_DATE_LASTMODIFIED).stringValue());
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
     *
     * @return true if the sort option should be used
     */
    protected boolean isSortScoring(IndexSearcher searcher, Sort sort) {

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
        return doScoring;
    }

    /**
     * Checks if the OpenCms resource referenced by the result document needs to be checked.<p>
     *
     * @param doc the search result document to check
     *
     * @return <code>true</code> if the document needs to be checked <code>false</code> otherwise
     */
    protected boolean needsPermissionCheck(I_CmsSearchDocument doc) {

        if (!isCheckingPermissions()) {
            // no permission check is performed at all
            return false;
        }

        if ((doc.getType() == null) || (doc.getPath() == null)) {
            // permission check needs only to be performed for VFS documents that contain both fields
            return false;
        }

        if (!I_CmsSearchDocument.VFS_DOCUMENT_KEY_PREFIX.equals(doc.getType())
            && !OpenCms.getResourceManager().hasResourceType(doc.getType())) {
            // this is an unknown VFS resource type (also not the generic "VFS" type of OpenCms before 7.0)
            return false;
        }
        return true;
    }

    /**
     * Removes the given backup folder of this index.<p>
     *
     * @param path the backup folder to remove
     */
    protected void removeIndexBackup(String path) {

        if (!isBackupReindexing()) {
            // if no backup is generated we don't need to do anything
            return;
        }

        // check if the target directory already exists
        File file = new File(path);
        if (!file.exists()) {
            // index does not exist yet
            return;
        }
        try {
            FSDirectory dir = FSDirectory.open(file.toPath());
            dir.close();
            CmsFileUtil.purgeDirectory(file);
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_IO_INDEX_BACKUP_REMOVE_2, getName(), path), e);
        }
    }

    /**
     * Generates the uninverting map and adds it to the field configuration.
     * @return the generated uninverting map
     *
     * @see CmsSearchField#addUninvertingMappings(Map)
     */
    private Map<String, Type> createUninvertingMap() {

        Map<String, UninvertingReader.Type> uninvertingMap = new HashMap<String, UninvertingReader.Type>();
        CmsSearchField.addUninvertingMappings(uninvertingMap);
        getFieldConfiguration().addUninvertingMappings(uninvertingMap);
        return uninvertingMap;
    }

}

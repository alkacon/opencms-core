/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2009/09/17 15:13:46 $
 * Version: $Revision: 1.81 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 * 
 * @author Alexander Kandzior 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.81 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchIndex implements I_CmsConfigurationParameterHandler {

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

        /**
         * Create a new lazy content reader.<p>
         * 
         * @param indexReader the index reader to use this lazy content reader with
         */
        public LazyContentReader(IndexReader indexReader) {

            super(indexReader);
        }

        /**
         * @see org.apache.lucene.index.IndexReader#document(int)
         */
        @Override
        public Document document(int n) throws CorruptIndexException, IOException {

            return super.document(n, CONTENT_SELECTOR);
        }
    }

    /** Constant for additional parameter to enable optimized full index regeneration (default: false). */
    public static final String BACKUP_REINDEXING = CmsSearchIndex.class.getName() + ".useBackupReindexing";

    /** Constant for additional parameter to enable excerpt creation (default: true). */
    public static final String EXCERPT = CmsSearchIndex.class.getName() + ".createExcerpt";

    /** Constant for additional parameter for index content extraction. */
    public static final String EXTRACT_CONTENT = CmsSearchIndex.class.getName() + ".extractContent";

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

    /** Constant for additional parameter to enable permission checks (default: true). */
    public static final String PERMISSIONS = CmsSearchIndex.class.getName() + ".checkPermissions";

    /** Constant for additional parameter to set the thread priority during search. */
    public static final String PRIORITY = CmsSearchIndex.class.getName() + ".priority";

    /** Automatic ("auto") index rebuild mode. */
    public static final String REBUILD_MODE_AUTO = "auto";

    /** Manual ("manual") index rebuild mode. */
    public static final String REBUILD_MODE_MANUAL = "manual";

    /** Offline ("offline") index rebuild mode. */
    public static final String REBUILD_MODE_OFFLINE = "offline";

    /** Constant for additional parameter to enable time range checks (default: true). */
    public static final String TIME_RANGE = CmsSearchIndex.class.getName() + ".checkTimeRange";

    /**
     * Field selector for Lucene that that will ensure the OpenCms default search index fields
     * {@link CmsSearchField#FIELD_CONTENT} and {@link CmsSearchField#FIELD_CONTENT_BLOB}
     * are lazy loaded.<p>
     * 
     * This is to optimize performance - these 2 fields will be rather large especially for extracted
     * binary documents like PDF, MS Office etc. By using lazy fields the data is only read when it is 
     * actually used.<p>
     */
    protected static final FieldSelector CONTENT_SELECTOR = new FieldSelector() {

        /** Required for safe serialization. */
        private static final long serialVersionUID = 2785064181424297998L;

        /**
         * Makes the content fields lazy.<p>
         * 
         * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
         */
        public FieldSelectorResult accept(String fieldName) {

            if (CmsSearchField.FIELD_CONTENT.equals(fieldName) || CmsSearchField.FIELD_CONTENT_BLOB.equals(fieldName)) {
                return FieldSelectorResult.LAZY_LOAD;
            }
            return FieldSelectorResult.LOAD;
        }
    };

    /** Constant for a field list that contains the "meta" field as well as the "content" field. */
    static final String[] DOC_META_FIELDS = new String[] {CmsSearchField.FIELD_META, CmsSearchField.FIELD_CONTENT};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndex.class);

    /** The list of configured index sources. */
    protected List<CmsSearchIndexSource> m_sources;

    /** The configured Lucene analyzer used for this index. */
    private Analyzer m_analyzer;

    /** Indicates if backup re-indexing is used by this index. */
    private boolean m_backupReindexing;

    /** The permission check mode for this index. */
    private boolean m_checkPermissions;

    /** The time range check mode for this index. */
    private boolean m_checkTimeRange;

    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Map of display query filters to use. */
    private Map<String, Filter> m_displayFilters;

    /** Document types of folders/channels. */
    private Map<String, List<String>> m_documenttypes;

    /** An internal enabled flag, used to disable the index if for instance the configured project does not exist. */
    private boolean m_enabled;

    /** The content extraction mode for this index. */
    private boolean m_extractContent;

    /** The search field configuration of this index. */
    private CmsSearchFieldConfiguration m_fieldConfiguration;

    /** The name of the search field configuration used by this index. */
    private String m_fieldConfigurationName;

    /** The locale of this index. */
    private Locale m_locale;

    /** The Lucene index auto commit setting, see {@link IndexWriter} constructors. */
    private Boolean m_luceneAutoCommit;

    /** The Lucene index merge factor setting, see {@link IndexWriter#setMaxMergeDocs(int)}. */
    private Integer m_luceneMaxMergeDocs;

    /** The Lucene index merge factor setting, see {@link IndexWriter#setMergeFactor(int)}. */
    private Integer m_luceneMergeFactor;

    /** The Lucene index RAM buffer size, see {@link IndexWriter#setRAMBufferSizeMB(double)}. */
    private Double m_luceneRAMBufferSizeMB;

    /** The Lucene index setting that controls, see {@link IndexWriter#setUseCompoundFile(boolean)}.  */
    private Boolean m_luceneUseCompoundFile;

    /** The name of this index. */
    private String m_name;

    /** The path where this index stores it's data in the "real" file system. */
    private String m_path;

    /** The thread priority for a search. */
    private int m_priority;

    /** The project of this index. */
    private String m_project;

    /** The rebuild mode for this index. */
    private String m_rebuild;

    /** The Lucene index searcher to use. */
    private IndexSearcher m_searcher;

    /** The configured sources for this index. */
    private List<String> m_sourceNames;

    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     * 
     * It is recommended to use the constructor <code>{@link #CmsSearchIndex(String)}</code> 
     * as it enforces the mandatory name argument. <p>
     * 
     */
    public CmsSearchIndex() {

        m_sourceNames = new ArrayList<String>();
        m_documenttypes = new HashMap<String, List<String>>();
        m_createExcerpt = true;
        m_extractContent = true;
        m_checkTimeRange = true;
        m_checkPermissions = true;
        m_enabled = true;
        m_priority = -1;
    }

    /**
     * Creates a new CmsSearchIndex with the given name.<p>
     * 
     * @param name the system-wide unique name for the search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     * 
     */
    public CmsSearchIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        setName(name);
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     */
    public void addConfigurationParameter(String key, String value) {

        if (PERMISSIONS.equals(key)) {
            m_checkPermissions = Boolean.valueOf(value).booleanValue();
        } else if (TIME_RANGE.equals(key)) {
            m_checkTimeRange = Boolean.valueOf(value).booleanValue();
        } else if (EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();
        } else if (EXTRACT_CONTENT.equals(key)) {
            m_extractContent = Boolean.valueOf(value).booleanValue();
        } else if (BACKUP_REINDEXING.equals(key)) {
            m_backupReindexing = Boolean.valueOf(value).booleanValue();
        } else if (PRIORITY.equals(key)) {
            m_priority = Integer.parseInt(value);
            if (m_priority < Thread.MIN_PRIORITY) {
                m_priority = Thread.MIN_PRIORITY;
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_SEARCH_PRIORITY_TOO_LOW_2,
                    value,
                    new Integer(Thread.MIN_PRIORITY)));

            } else if (m_priority > Thread.MAX_PRIORITY) {
                m_priority = Thread.MAX_PRIORITY;
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_SEARCH_PRIORITY_TOO_HIGH_2,
                    value,
                    new Integer(Thread.MAX_PRIORITY)));

            }
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
        } else if (LUCENE_AUTO_COMMIT.equals(key)) {
            m_luceneAutoCommit = Boolean.valueOf(value);
        }
    }

    /**
     * Adds am index source to this search index.<p>
     * 
     * @param sourceName the index source name to add
     */
    public void addSourceName(String sourceName) {

        m_sourceNames.add(sourceName);
    }

    /**
     * Checks is this index has been configured correctly.<p>
     * 
     * In case the check fails, the <code>enabled</code> property
     * is set to <code>false</code>
     * 
     * @param cms a OpenCms user context to perform the checks with (should have "Administrator" permissions)
     *
     * @return <code>true</code> in case the index is correctly configured and enabled after the check
     * 
     * @see #isEnabled()
     */
    public boolean checkConfiguration(CmsObject cms) {

        if (isEnabled()) {
            // check if the project for the index exists        
            try {
                cms.readProject(getProject());
                setEnabled(true);
            } catch (CmsException e) {
                // the project does not exist, disable the index
                setEnabled(false);
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_SEARCHINDEX_CREATE_BAD_PROJECT_2,
                        getProject(),
                        getName()));
                }
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_SEARCHINDEX_DISABLED_1, getName()));
            }
        }

        return isEnabled();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchIndex) {
            return ((CmsSearchIndex)obj).m_name.equals(m_name);
        }
        return false;
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
    public Map<String, Object> getConfiguration() {

        Map<String, Object> result = new TreeMap<String, Object>();
        if (getPriority() > 0) {
            result.put(PRIORITY, new Integer(m_priority));
        }
        if (!isCreatingExcerpt()) {
            result.put(EXCERPT, Boolean.valueOf(m_createExcerpt));
        }
        if (!isExtractingContent()) {
            result.put(EXTRACT_CONTENT, Boolean.valueOf(m_extractContent));
        }
        if (!isCheckingPermissions()) {
            result.put(PERMISSIONS, Boolean.valueOf(m_checkPermissions));
        }
        if (!isCheckingTimeRange()) {
            result.put(TIME_RANGE, Boolean.valueOf(m_checkTimeRange));
        }
        if (isBackupReindexing()) {
            result.put(BACKUP_REINDEXING, Boolean.valueOf(m_backupReindexing));
        }
        // set the index writer parameter if required 
        if (m_luceneMaxMergeDocs != null) {
            result.put(LUCENE_MAX_MERGE_DOCS, m_luceneMaxMergeDocs);
        }
        if (m_luceneMergeFactor != null) {
            result.put(LUCENE_MERGE_FACTOR, m_luceneMergeFactor);
        }
        if (m_luceneRAMBufferSizeMB != null) {
            result.put(LUCENE_RAM_BUFFER_SIZE_MB, m_luceneRAMBufferSizeMB);
        }
        if (m_luceneUseCompoundFile != null) {
            result.put(LUCENE_USE_COMPOUND_FILE, m_luceneUseCompoundFile);
        }
        if (m_luceneAutoCommit != null) {
            result.put(LUCENE_AUTO_COMMIT, m_luceneAutoCommit);
        }
        return result;
    }

    /**
     * Returns the Lucene document with the given root path from the index.<p>
     * 
     * @param rootPath the root path of the document to get 
     * 
     * @return the Lucene document with the given root path from the index
     */
    public Document getDocument(String rootPath) {

        Document result = null;
        IndexSearcher searcher = getSearcher();
        if (searcher != null) {
            // search for an exact match on the document root path
            Term pathTerm = new Term(CmsSearchField.FIELD_PATH, rootPath);
            try {
                Hits hits = searcher.search(new TermQuery(pathTerm));
                if (hits.length() > 0) {
                    result = hits.doc(0);
                }
            } catch (IOException e) {
                // ignore, return null and assume document was not found
            }
        }
        return result;
    }

    /**
     * Returns the document type factory used for the given resource in this index, or <code>null</code>  
     * in case the resource is not indexed by this index.<p>
     * 
     * A resource is indexed if the following is all true: <ol>
     * <li>The index contains at last one index source matching the root path of the given resource.
     * <li>For this matching index source, the document type factory needed by the resource is also configured.
     * </ol>
     * 
     * @param res the resource to check
     * 
     * @return he document type factory used for the given resource in this index, or <code>null</code>  
     * in case the resource is not indexed by this index
     */
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if ((res != null) && (m_sources != null)) {
            // the result can only be null or the type configured for the resource
            I_CmsDocumentFactory result = OpenCms.getSearchManager().getDocumentFactory(res);
            if (result != null) {
                // check the path of the resource if it matches with one (or more) of the configured index sources
                Iterator<CmsSearchIndexSource> i = m_sources.iterator();
                while (i.hasNext()) {
                    CmsSearchIndexSource source = i.next();
                    if (source.isIndexing(res.getRootPath(), result.getName())) {
                        // we found an index source that indexes the resource
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the search field configuration of this index.<p>
     * 
     * @return the search field configuration of this index
     */
    public CmsSearchFieldConfiguration getFieldConfiguration() {

        return m_fieldConfiguration;
    }

    /**
     * Returns the name of the field configuration used for this index.<p>
     * 
     * @return the name of the field configuration used for this index
     */
    public String getFieldConfigurationName() {

        return m_fieldConfigurationName;
    }

    /**
     * Returns a new index writer for this index.<p>
     * 
     * @param create if <code>true</code> a whole new index is created, if <code>false</code> an existing index is updated
     * 
     * @return a new instance of IndexWriter
     * @throws CmsIndexException if the index can not be opened
     */
    public IndexWriter getIndexWriter(boolean create) throws CmsIndexException {

        IndexWriter indexWriter;

        try {

            // check if the target directory already exists
            File f = new File(m_path);
            if (!f.exists()) {
                // index does not exist yet
                f = f.getParentFile();
                if ((f != null) && !f.exists()) {
                    // create the parent folders if required
                    f.mkdirs();
                }
                // create must be true if the directory does not exist
                create = true;
            }

            // auto commit status, according to Lucene experts setting this to 
            // false increases performance
            boolean autoCommit = (m_luceneAutoCommit == null) ? true : m_luceneAutoCommit.booleanValue();
            // open file directory for Lucene
            FSDirectory dir = FSDirectory.getDirectory(m_path);
            // index already exists
            indexWriter = new IndexWriter(dir, autoCommit, getAnalyzer(), create);

            // set the index writer parameter if required 
            if (m_luceneMaxMergeDocs != null) {
                indexWriter.setMaxMergeDocs(m_luceneMaxMergeDocs.intValue());
            }
            if (m_luceneMergeFactor != null) {
                indexWriter.setMergeFactor(m_luceneMergeFactor.intValue());
            }
            if (m_luceneRAMBufferSizeMB != null) {
                indexWriter.setRAMBufferSizeMB(m_luceneRAMBufferSizeMB.doubleValue());
            }
            if (m_luceneUseCompoundFile != null) {
                indexWriter.setUseCompoundFile(m_luceneUseCompoundFile.booleanValue());
            }

        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, m_path, m_name),
                e);
        }

        return indexWriter;
    }

    /**
     * Returns the language locale of this index.<p>
     * 
     * @return the language locale of this index, for example "en"
     */
    public Locale getLocale() {

        return m_locale;
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
     * Gets the name of this index.<p>
     * 
     * @return the name of the index
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the path where this index stores it's data in the "real" file system.<p>
     * 
     * @return the path where this index stores it's data in the "real" file system
     */
    public String getPath() {

        return m_path;
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
     * Gets the project of this index.<p>
     * 
     * @return the project of the index, i.e. "online"
     */
    public String getProject() {

        return m_project;
    }

    /**
     * Get the rebuild mode of this index.<p>
     * 
     * @return the current rebuild mode
     */
    public String getRebuildMode() {

        return m_rebuild;
    }

    /**
     * Returns the Lucene index searcher used for this search index.<p>
     *
     * @return the Lucene index searcher used for this search index
     */
    public IndexSearcher getSearcher() {

        return m_searcher;
    }

    /**
     * Returns all configured sources names of this search index.<p>
     * 
     * @return a list with all configured sources names of this search index
     */
    public List<String> getSourceNames() {

        return m_sourceNames;
    }

    /**
     * Returns all configured index sources of this search index.<p>
     * 
     * @return all configured index sources of this search index
     */
    public List<CmsSearchIndexSource> getSources() {

        return m_sources;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name != null ? m_name.hashCode() : 0;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // noting to do here
    }

    /**
     * Initializes the search index.<p>
     * 
     * @throws CmsSearchException if the index source association failed
     */
    public void initialize() throws CmsSearchException {

        if (!isEnabled()) {
            // index is disabled, no initialization is required
            return;
        }

        String sourceName = null;
        CmsSearchIndexSource indexSource = null;
        List<String> searchIndexSourceDocumentTypes = null;
        List<String> resourceNames = null;
        String resourceName = null;
        m_sources = new ArrayList<CmsSearchIndexSource>();

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + m_name);

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {

            try {
                sourceName = m_sourceNames.get(i);
                indexSource = OpenCms.getSearchManager().getIndexSource(sourceName);
                m_sources.add(indexSource);

                resourceNames = indexSource.getResourcesNames();
                searchIndexSourceDocumentTypes = indexSource.getDocumentTypes();
                for (int j = 0, m = resourceNames.size(); j < m; j++) {

                    resourceName = resourceNames.get(j);
                    m_documenttypes.put(resourceName, searchIndexSourceDocumentTypes);
                }
            } catch (Exception e) {
                // mark this index as disabled
                setEnabled(false);
                throw new CmsSearchException(Messages.get().container(
                    Messages.ERR_INDEX_SOURCE_ASSOCIATION_1,
                    sourceName), e);
            }
        }

        // initialize the search field configuration
        if (m_fieldConfigurationName == null) {
            // if not set, use standard field configuration
            m_fieldConfigurationName = CmsSearchFieldConfiguration.STR_STANDARD;
        }
        m_fieldConfiguration = OpenCms.getSearchManager().getFieldConfiguration(m_fieldConfigurationName);
        if (m_fieldConfiguration == null) {
            // we must have a valid field configuration to continue
            throw new CmsSearchException(Messages.get().container(
                Messages.ERR_FIELD_CONFIGURATION_UNKNOWN_2,
                m_name,
                m_fieldConfigurationName));
        }

        // get the configured analyzer and apply the the field configuration analyzer wrapper
        Analyzer baseAnalyzer = OpenCms.getSearchManager().getAnalyzer(getLocale());
        setAnalyzer(m_fieldConfiguration.getAnalyzer(baseAnalyzer));

        // initialize the index searcher instance
        indexSearcherOpen(m_path);
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
     * Returns <code>true</code> if the document time range is checked for search results by this index.<p>
     * 
     * If time range checks are not required, they can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not checking the time range will improve performance.<p> 
     * 
     * @return <code>true</code> if the document time range is checked for search results by this index
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
     * Returns <code>true</code> if this index is currently disabled.<p>
     * 
     * @return <code>true</code> if this index is currently disabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns <code>true</code> if full text is extracted by this index.<p>
     *
     * Full text content extraction can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. 
     * Not extraction the full text information will highly improve performance.<p> 
     *
     * @return <code>true</code> if full text is extracted by this index
     */
    public boolean isExtractingContent() {

        return m_extractContent;
    }

    /**
     * Removes an index source from this search index.<p>
     * 
     * @param sourceName the index source name to remove
     */
    public void removeSourceName(String sourceName) {

        m_sourceNames.remove(sourceName);
    }

    /**
     * Performs a search on the index within the given fields.<p>
     * 
     * The result is returned as List with entries of type I_CmsSearchResult.<p>
     * @param cms the current user's Cms object
     * @param params the parameters to use for the search
     * @return the List of results found or an empty list
     * @throws CmsSearchException if something goes wrong
     */
    public synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) throws CmsSearchException {

        long timeTotal = -System.currentTimeMillis();
        long timeLucene;
        long timeResultProcessing;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SEARCH_PARAMS_2, params, getName()));
        }

        // the hits found during the search
        Hits hits;

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
            // complete the search root
            TermsFilter pathFilter = new TermsFilter();
            if ((params.getRoots() != null) && (params.getRoots().size() > 0)) {
                // add the all configured search roots with will request context
                for (int i = 0; i < params.getRoots().size(); i++) {
                    String searchRoot = searchCms.getRequestContext().addSiteRoot(params.getRoots().get(i));
                    extendPathFilter(pathFilter, searchRoot);
                }
            } else {
                // just use the current site root as the search root
                extendPathFilter(pathFilter, searchCms.getRequestContext().getSiteRoot());
            }
            // add the calculated phrase query for the root path
            filter.add(new FilterClause(pathFilter, BooleanClause.Occur.MUST));

            if ((params.getCategories() != null) && (params.getCategories().size() > 0)) {
                // add query categories (if required)
                filter.add(new FilterClause(getMultiTermQueryFilter(
                    CmsSearchField.FIELD_CATEGORY,
                    params.getCategories()), BooleanClause.Occur.MUST));
            }

            if ((params.getResourceTypes() != null) && (params.getResourceTypes().size() > 0)) {
                // add query resource types (if required)
                filter.add(new FilterClause(getMultiTermQueryFilter(
                    CmsSearchField.FIELD_TYPE,
                    params.getResourceTypes()), BooleanClause.Occur.MUST));
            }

            // the search query to use, will be constructed in the next lines 
            BooleanQuery query = new BooleanQuery();
            // store separate fields query for excerpt highlighting  
            Query fieldsQuery;
            if (params.getFieldQueries() != null) {
                // each field has an individual query
                BooleanQuery mustOccur = null;
                BooleanQuery shouldOccur = null;
                Iterator<CmsSearchParameters.CmsSearchFieldQuery> i = params.getFieldQueries().iterator();
                while (i.hasNext()) {
                    CmsSearchParameters.CmsSearchFieldQuery fq = i.next();
                    // add one sub-query for each defined field
                    QueryParser p = new QueryParser(fq.getFieldName(), getAnalyzer());
                    if (BooleanClause.Occur.SHOULD.equals(fq.getOccur())) {
                        if (shouldOccur == null) {
                            shouldOccur = new BooleanQuery();
                        }
                        shouldOccur.add(p.parse(fq.getSearchQuery()), fq.getOccur());
                    } else {
                        if (mustOccur == null) {
                            mustOccur = new BooleanQuery();
                        }
                        mustOccur.add(p.parse(fq.getSearchQuery()), fq.getOccur());
                    }
                }
                BooleanQuery booleanFieldsQuery = new BooleanQuery();
                if (mustOccur != null) {
                    booleanFieldsQuery.add(mustOccur, BooleanClause.Occur.MUST);
                }
                if (shouldOccur != null) {
                    booleanFieldsQuery.add(shouldOccur, BooleanClause.Occur.MUST);
                }
                fieldsQuery = getSearcher().rewrite(booleanFieldsQuery);
            } else if ((params.getFields() != null) && (params.getFields().size() > 0)) {
                // no individual field queries have been defined, so use one query for all fields 
                BooleanQuery booleanFieldsQuery = new BooleanQuery();
                // this is a "regular" query over one or more fields
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                for (int i = 0; i < params.getFields().size(); i++) {
                    QueryParser p = new QueryParser(params.getFields().get(i), getAnalyzer());
                    booleanFieldsQuery.add(p.parse(params.getQuery()), BooleanClause.Occur.SHOULD);
                }
                fieldsQuery = getSearcher().rewrite(booleanFieldsQuery);
            } else {
                // if no fields are provided, just use the "content" field by default
                QueryParser p = new QueryParser(CmsSearchField.FIELD_CONTENT, getAnalyzer());
                fieldsQuery = getSearcher().rewrite(p.parse(params.getQuery()));
            }

            // finally add the field queries to the main query
            query.add(fieldsQuery, BooleanClause.Occur.MUST);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_BASE_QUERY_1, query));
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FIELDS_QUERY_1, fieldsQuery));
            }

            // collect the categories
            CmsSearchCategoryCollector categoryCollector;
            if (params.isCalculateCategories()) {
                // USE THIS OPTION WITH CAUTION
                // this may slow down searched by an order of magnitude
                categoryCollector = new CmsSearchCategoryCollector(getSearcher());
                // perform a first search to collect the categories
                getSearcher().search(query, filter, categoryCollector);
                // store the result
                searchResults.setCategories(categoryCollector.getCategoryCountResult());
            }

            // perform the search operation          
            hits = getSearcher().search(query, filter, params.getSort());

            timeLucene += System.currentTimeMillis();
            timeResultProcessing = -System.currentTimeMillis();

            Document doc;
            CmsSearchResult searchResult;

            if (hits != null) {
                int hitCount = hits.length();
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
                        doc = hits.doc(i);
                        if ((isInTimeRange(doc, params)) && (hasReadPermission(searchCms, doc))) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document!
                                String excerpt = null;
                                if (isCreatingExcerpt()) {
                                    I_CmsTermHighlighter highlighter = OpenCms.getSearchManager().getHighlighter();
                                    excerpt = highlighter.getExcerpt(doc, this, params, fieldsQuery, getAnalyzer());
                                }
                                searchResult = new CmsSearchResult(Math.round(hits.score(i) * 100f), doc, excerpt);
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

                // save the total count of search results at the last index of the search result 
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
                new Integer(hits == null ? 0 : hits.length()),
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
     * Can be used to enable / disable this index.<p>
     * 
     * @param enabled the state of the index to set
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * Sets the field configuration used for this index.<p>
     * 
     * @param fieldConfiguration the field configuration to set
     */
    public void setFieldConfiguration(CmsSearchFieldConfiguration fieldConfiguration) {

        m_fieldConfiguration = fieldConfiguration;
    }

    /**
     * Sets the name of the field configuration used for this index.<p>
     * 
     * @param fieldConfigurationName the name of the field configuration to set
     */
    public void setFieldConfigurationName(String fieldConfigurationName) {

        m_fieldConfigurationName = fieldConfigurationName;
    }

    /**
     * Sets the locale to index resources.<p>
     * 
     * @param locale the locale to index resources
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the locale to index resources as a String.<p>
     * 
     * @param locale the locale to index resources
     * 
     * @see #setLocale(Locale)
     */
    public void setLocaleString(String locale) {

        setLocale(CmsLocaleManager.getLocale(locale));
    }

    /**
     * Sets the logical key/name of this search index.<p>
     * 
     * @param name the logical key/name of this search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     */
    public void setName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
        } else {
            // check if already used, but only if the name was modified: 
            // this is important as unmodifiable DisplayWidgets will also invoke this...
            if (!name.equals(m_name)) {
                // don't mess with XML configuration
                if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // not needed at startup and additionally getSearchManager may return null
                    Iterator<String> itIdxNames = OpenCms.getSearchManager().getIndexNames().iterator();
                    while (itIdxNames.hasNext()) {
                        if (itIdxNames.next().equals(name)) {
                            throw new CmsIllegalArgumentException(Messages.get().container(
                                Messages.ERR_SEARCHINDEX_CREATE_INVALID_NAME_1,
                                name));
                        }
                    }
                }
            }
        }
        m_name = name;
    }

    /**
     * Sets the name of the project used to index resources.<p>
     * 
     * A duplicate method of <code>{@link #setProjectName(String)}</code> that allows 
     * to use instances of this class as a widget object (bean convention, 
     * cp.: <code>{@link #getProject()}</code>.<p> 
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProject(String projectName) {

        setProjectName(projectName);
    }

    /**
     * Sets the name of the project used to index resources.<p>
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProjectName(String projectName) {

        m_project = projectName;
    }

    /**
     * Sets the rebuild mode of this search index.<p>
     * 
     * @param rebuildMode the rebuild mode of this search index {auto|manual}
     */
    public void setRebuildMode(String rebuildMode) {

        m_rebuild = rebuildMode;
    }

    /**
     * Shuts down the search index.<p>
     * 
     * This will close the local Lucene index searcher instance.<p>
     * 
     * @throws IOException in case the index could not be closed
     */
    public void shutDown() throws IOException {

        if (m_searcher != null) {
            m_searcher.close();
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
        File file = new File(m_path);
        if (!file.exists()) {
            // index does not exist yet, so we can't backup it
            return null;
        }
        String backupPath = m_path + "_backup";
        try {
            // open file directory for Lucene
            FSDirectory oldDir = FSDirectory.getDirectory(file);
            FSDirectory newDir = FSDirectory.getDirectory(backupPath);
            Directory.copy(oldDir, newDir, true);
        } catch (Exception e) {
            // TODO: logging etc. 
            backupPath = null;
        }
        return backupPath;
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
        pathFilter.addTerm(new Term(CmsSearchField.FIELD_PARENT_FOLDERS, searchRoot));
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
     * Checks if the OpenCms resource referenced by the result document can be read 
     * be the user of the given OpenCms context.<p>
     * 
     * @param cms the OpenCms user context to use for permission testing
     * @param doc the search result document to check
     * @return <code>true</code> if the user has read permissions to the resource
     */
    protected boolean hasReadPermission(CmsObject cms, Document doc) {

        if (!isCheckingPermissions()) {
            // no permission check is performed at all
            return true;
        }

        Fieldable typeField = doc.getFieldable(CmsSearchField.FIELD_TYPE);
        Fieldable pathField = doc.getFieldable(CmsSearchField.FIELD_PATH);
        if ((typeField == null) || (pathField == null)) {
            // permission check needs only to be performed for VFS documents that contain both fields
            return true;
        }

        String type = typeField.stringValue();
        if (!CmsSearchFieldConfiguration.VFS_DOCUMENT_KEY_PREFIX.equals(type)
            && !OpenCms.getResourceManager().hasResourceType(type)) {
            // this is not a known VFS resource type (also not the generic "VFS" type of OpenCms before 7.0)
            return true;
        }

        // check if the resource exits in the VFS, 
        // this will implicitly check read permission and if the resource was deleted
        String contextPath = cms.getRequestContext().removeSiteRoot(pathField.stringValue());
        return cms.existsResource(contextPath);
    }

    /**
     * Closes the Lucene index searcher for this index.<p>
     * 
     * @see #indexSearcherOpen(String)
     */
    protected synchronized void indexSearcherClose() {

        // in case there is an index searcher available close it
        if (m_searcher != null) {
            try {
                m_searcher.close();
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SHUTDOWN_1, getName()), e);
            }
        }
    }

    /**
     * Initializes the Lucene index searcher for this index.<p>
     * 
     * Use {@link #getSearcher()} in order to obtain the searcher that has been opened.<p>
     * 
     * In case there is an index searcher still open, it is closed first.<p>
     * 
     * For performance reasons, one instance of the Lucene index searcher should be kept 
     * for all searches. However, if the index is updated or changed 
     * this searcher instance needs to be re-initialized.<p>
     * 
     * @param path the path to the index directory
     */
    protected synchronized void indexSearcherOpen(String path) {

        // first close the current searcher instance
        indexSearcherClose();

        // create the index searcher
        try {
            if (IndexReader.indexExists(path)) {
                IndexReader reader = new LazyContentReader(IndexReader.open(path));
                m_searcher = new IndexSearcher(reader);
                m_displayFilters = new HashMap<String, Filter>();
            }
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
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
            Date dateCreated = DateTools.stringToDate(doc.getFieldable(CmsSearchField.FIELD_DATE_CREATED).stringValue());
            if ((params.getMinDateCreated() > Long.MIN_VALUE) && (dateCreated.getTime() < params.getMinDateCreated())) {
                return false;
            }
            if ((params.getMaxDateCreated() < Long.MAX_VALUE) && (dateCreated.getTime() > params.getMaxDateCreated())) {
                return false;
            }

            // check the last modification date of the document against the given time range
            Date dateLastModified = DateTools.stringToDate(doc.getFieldable(CmsSearchField.FIELD_DATE_LASTMODIFIED).stringValue());
            if ((params.getMinDateLastModified() > Long.MIN_VALUE)
                && (dateLastModified.getTime() < params.getMinDateLastModified())) {
                return false;
            }
            if ((params.getMaxDateLastModified() < Long.MAX_VALUE)
                && (dateLastModified.getTime() > params.getMaxDateLastModified())) {
                return false;
            }

        } catch (ParseException ex) {
            // date could not be parsed -> doc is in time range
        }

        return true;
    }

    /**
     * Removes the given backup folder of this index.<p>
     * 
     * @param path the backup folder to remove
     * 
     * @see #isBackupReindexing()
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
            FSDirectory dir = FSDirectory.getDirectory(file);
            dir.close();
            CmsFileUtil.purgeDirectory(file);
        } catch (Exception e) {
            // TODO: logging etc. 
        }
    }
}
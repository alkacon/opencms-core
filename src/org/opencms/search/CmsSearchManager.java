/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchManager.java,v $
 * Date   : $Date: 2009/09/23 12:43:10 $
 * Version: $Revision: 1.82 $
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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.documents.CmsExtractionResultCache;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.A_CmsModeStringEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * Implements the general management and configuration of the search and 
 * indexing facilities in OpenCms.<p>
 * 
 * @author Alexander Kandzior
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.82 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchManager implements I_CmsScheduledJob, I_CmsEventListener {

    /**
     *  Enumeration class for force unlock types.<p>
     */
    public static final class CmsSearchForceUnlockMode extends A_CmsModeStringEnumeration {

        /** Force unlock type "always". */
        public static final CmsSearchForceUnlockMode ALWAYS = new CmsSearchForceUnlockMode("always");

        /** Force unlock type "never". */
        public static final CmsSearchForceUnlockMode NEVER = new CmsSearchForceUnlockMode("never");

        /** Force unlock type "only full". */
        public static final CmsSearchForceUnlockMode ONLYFULL = new CmsSearchForceUnlockMode("onlyfull");

        /** Serializable version id. */
        private static final long serialVersionUID = 74746076708908673L;

        /**
         * Creates a new force unlock type with the given name.<p>
         * 
         * @param mode the mode id to use
         */
        protected CmsSearchForceUnlockMode(String mode) {

            super(mode);
        }

        /**
         * Returns the lock type for the given type value.<p>
         *  
         * @param type the type value to get the lock type for
         * 
         * @return the lock type for the given type value
         */
        public static CmsSearchForceUnlockMode valueOf(String type) {

            if (type.equals(ALWAYS.toString())) {
                return ALWAYS;
            } else if (type.equals(NEVER.toString())) {
                return NEVER;
            } else {
                return ONLYFULL;
            }
        }
    }

    /**
     * Handles offline index generation.<p>
     */
    protected class CmsSearchOfflineHandler implements I_CmsEventListener {

        /** Indicates if the event handlers for the offline search have been already registered. */
        private boolean m_isEventRegistered;

        /** The list of resources to index. */
        private List<CmsPublishedResource> m_resourcesToIndex;

        /**
         * Initializes the offline index handler.<p>
         */
        protected CmsSearchOfflineHandler() {

            m_resourcesToIndex = new ArrayList<CmsPublishedResource>();
        }

        /**
         * Implements the event listener of this class.<p>
         * 
         * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
         */
        public void cmsEvent(CmsEvent event) {

            switch (event.getType()) {
                case I_CmsEventListener.EVENT_PROPERTY_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_CREATED:
                case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                    // a resource has been modified - offline indexes require (re)indexing
                    List<CmsResource> resources = Collections.singletonList((CmsResource)event.getData().get(
                        I_CmsEventListener.KEY_RESOURCE));
                    reIndexResources(resources);
                    break;
                case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                case I_CmsEventListener.EVENT_RESOURCE_COPIED:
                case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                    // a list of resources has been modified - offline indexes require (re)indexing
                    reIndexResources((List<CmsResource>)event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    break;
                default:
                    // no operation
            }
        }

        /**
         * Adds a list of {@link CmsPublishedResource} objects to be indexed.<p>
         * 
         * @param resourcesToIndex the list of {@link CmsPublishedResource} objects to be indexed
         */
        protected synchronized void addResourcesToIndex(List<CmsPublishedResource> resourcesToIndex) {

            m_resourcesToIndex.addAll(resourcesToIndex);
        }

        /**
         * Returns the list of {@link CmsPublishedResource} objects to index.<p>
         *
         * @return the resources to index
         */
        protected synchronized List<CmsPublishedResource> getResourcesToIndex() {

            List<CmsPublishedResource> temp = m_resourcesToIndex;
            m_resourcesToIndex = new ArrayList<CmsPublishedResource>();
            return temp;
        }

        /**
         * Initializes this offline search handler, registering the event handlers if required.<p>
         */
        protected void initialize() {

            if (m_offlineIndexes.size() > 0) {
                // there is at least one offline index configured
                if ((m_offlineIndexThread == null) || !m_offlineIndexThread.isAlive()) {
                    // create the offline indexing thread
                    m_offlineIndexThread = new CmsSearchOfflineIndexThread(this);
                    // start the offline index thread
                    m_offlineIndexThread.start();
                }
            } else {
                if ((m_offlineIndexThread != null) && m_offlineIndexThread.isAlive()) {
                    // no offline indexes but thread still running, stop the thread
                    m_offlineIndexThread.shutDown();
                    m_offlineIndexThread = null;
                }
            }
            // do this only in case there are offline indexes configured
            if (!m_isEventRegistered && (m_offlineIndexes.size() > 0)) {
                m_isEventRegistered = true;
                // register this object as event listener
                OpenCms.addCmsEventListener(this, new int[] {
                    I_CmsEventListener.EVENT_PROPERTY_MODIFIED,
                    I_CmsEventListener.EVENT_RESOURCE_CREATED,
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                    I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
                    I_CmsEventListener.EVENT_RESOURCE_MOVED,
                    I_CmsEventListener.EVENT_RESOURCE_DELETED,
                    I_CmsEventListener.EVENT_RESOURCE_COPIED,
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED});
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("initialize --- end");
            }
        }

        /**
         * Updates all offline indexes for the given list of {@link CmsResource} objects.<p>
         * 
         * @param resources a list of {@link CmsResource} objects to update in the offline indexes
         */
        protected synchronized void reIndexResources(List<CmsResource> resources) {

            List<CmsPublishedResource> resourcesToIndex = new ArrayList<CmsPublishedResource>(resources.size());
            Iterator<CmsResource> r = resources.iterator();
            while (r.hasNext()) {
                CmsResource res = r.next();
                CmsPublishedResource pubRes = new CmsPublishedResource(res);
                resourcesToIndex.add(pubRes);
            }
            if (resourcesToIndex.size() > 0) {
                // add the resources found to the offline index thread
                addResourcesToIndex(resourcesToIndex);
            }
        }
    }

    /**
     * The offline indexer thread runs periodically and indexes all resources added by the event handler.<p>
     */
    protected class CmsSearchOfflineIndexThread extends Thread {

        /** Indicates if this thread is still alive. */
        boolean m_isAlive;

        /** The event handler that triggers this thread. */
        CmsSearchOfflineHandler m_handler;

        /**
         * Constructor.<p>
         * 
         * @param handler the offline index event handler
         */
        protected CmsSearchOfflineIndexThread(CmsSearchOfflineHandler handler) {

            super("OpenCms: Offline Search Indexer");
            m_handler = handler;
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            // create a log report for the output
            I_CmsReport report = new CmsLogReport(m_adminCms.getRequestContext().getLocale(), CmsSearchManager.class);
            try {
                while (m_isAlive) {
                    List<CmsPublishedResource> resourcesToIndex = getResourcesToIndex();
                    if (resourcesToIndex.size() > 0) {
                        // only start indexing if there is at least one resource
                        updateIndexOffline(report, resourcesToIndex);
                    }

                    try {
                        sleep(OpenCms.getSearchManager().getOfflineUpdateFrequency());
                    } catch (InterruptedException e) {
                        // continue the thread after interruption
                    }
                }
            } finally {
                // make sure that live status is reset in case of Exceptions
                m_isAlive = false;
            }

        }

        /**
         * @see java.lang.Thread#start()
         */
        @Override
        public synchronized void start() {

            m_isAlive = true;
            super.start();
        }

        /**
         * Obtains the list of resource to update in the offline index, 
         * then optimizes the list by removing duplicate entries.<p>
         * 
         * @return the list of resource to update in the offline index
         */
        protected List<CmsPublishedResource> getResourcesToIndex() {

            List<CmsPublishedResource> resourcesToIndex = m_handler.getResourcesToIndex();
            List<CmsPublishedResource> result = new ArrayList<CmsPublishedResource>(resourcesToIndex.size());

            Iterator<CmsPublishedResource> i = resourcesToIndex.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                CmsPublishedResource pubRes = (CmsPublishedResource)o;
                int pos = result.indexOf(pubRes);
                if (pos < 0) {
                    // resource not already contained in the update list
                    result.add(pubRes);
                } else {
                    CmsPublishedResource curRes = result.get(pos);
                    if ((pubRes.getState() != curRes.getState())
                        || (pubRes.getMovedState() != curRes.getMovedState())
                        || !pubRes.getRootPath().equals(curRes.getRootPath())) {
                        // resource already in the update list but new state is different, so also add this
                        result.add(pubRes);
                    }
                }
            }

            return result;
        }

        /**
         * Shuts down this offline index thread.<p>
         */
        protected void shutDown() {

            m_isAlive = false;
            interrupt();
        }
    }

    /** The default value used for generating search result excerpts (1024 chars). */
    public static final int DEFAULT_EXCERPT_LENGTH = 1024;

    /** The default value used for keeping the extraction results in the cache (672 hours = 4 weeks). */
    public static final float DEFAULT_EXTRACTION_CACHE_MAX_AGE = 672.0f;

    /** The default update frequency for offline indexes (15000 msec = 15 sec). */
    public static final int DEFAULT_OFFLINE_UPDATE_FREQNENCY = 15000;

    /** The default timeout value used for generating a document for the search index (60000 msec = 1 min). */
    public static final int DEFAULT_TIMEOUT = 60000;

    /** Scheduler parameter: Update only a specified list of indexes. */
    public static final String JOB_PARAM_INDEXLIST = "indexList";

    /** Scheduler parameter: Write the output of the update to the logfile. */
    public static final String JOB_PARAM_WRITELOG = "writeLog";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsSearchManager.class);

    /** The administrator OpenCms user context to access OpenCms VFS resources. */
    protected CmsObject m_adminCms;

    /** The list of indexes that are configured for offline index mode. */
    protected List<CmsSearchIndex> m_offlineIndexes;

    /** The thread used of offline indexing. */
    protected CmsSearchOfflineIndexThread m_offlineIndexThread;

    /** Configured analyzers for languages using &lt;analyzer&gt;. */
    private HashMap<Locale, CmsSearchAnalyzer> m_analyzers;

    /** A map of document factory configurations. */
    private List<CmsSearchDocumentType> m_documentTypeConfigs;

    /** A map of document factories keyed by their matching Cms resource types and/or mimetypes. */
    private Map<String, I_CmsDocumentFactory> m_documentTypes;

    /** The max age for extraction results to remain in the cache. */
    private float m_extractionCacheMaxAge;

    /** The cache for the extraction results. */
    private CmsExtractionResultCache m_extractionResultCache;

    /** Contains the available field configurations. */
    private Map<String, CmsSearchFieldConfiguration> m_fieldConfigurations;

    /** The force unlock type. */
    private CmsSearchForceUnlockMode m_forceUnlockMode;

    /** The class used to highlight the search terms in the excerpt of a search result. */
    private I_CmsTermHighlighter m_highlighter;

    /** A list of search indexes. */
    private List<CmsSearchIndex> m_indexes;

    /** Seconds to wait for an index lock. */
    private int m_indexLockMaxWaitSeconds = 10;

    /** Configured index sources. */
    private Map<String, CmsSearchIndexSource> m_indexSources;

    /** The max. char. length of the excerpt in the search result. */
    private int m_maxExcerptLength;

    /** The offline index search handler. */
    private CmsSearchOfflineHandler m_offlineHandler;

    /** The update frequency of the offline indexer in milliseconds. */
    private long m_offlineUpdateFrequency;

    /** Path to index files below WEB-INF/. */
    private String m_path;

    /** Timeout for abandoning indexing thread. */
    private long m_timeout;

    /**
     * Default constructor when called as cron job.<p>
     */
    public CmsSearchManager() {

        m_documentTypes = new HashMap<String, I_CmsDocumentFactory>();
        m_documentTypeConfigs = new ArrayList<CmsSearchDocumentType>();
        m_analyzers = new HashMap<Locale, CmsSearchAnalyzer>();
        m_indexes = new ArrayList<CmsSearchIndex>();
        m_indexSources = new TreeMap<String, CmsSearchIndexSource>();
        m_extractionCacheMaxAge = DEFAULT_EXTRACTION_CACHE_MAX_AGE;
        m_maxExcerptLength = DEFAULT_EXCERPT_LENGTH;
        m_offlineHandler = new CmsSearchOfflineHandler();
        m_offlineUpdateFrequency = DEFAULT_OFFLINE_UPDATE_FREQNENCY;

        m_fieldConfigurations = new HashMap<String, CmsSearchFieldConfiguration>();
        // make sure we have a "standard" field configuration
        addFieldConfiguration(CmsSearchFieldConfiguration.DEFAULT_STANDARD);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_START_SEARCH_CONFIG_0));
        }
    }

    /**
     * Adds an analyzer.<p>
     * 
     * @param analyzer an analyzer
     */
    public void addAnalyzer(CmsSearchAnalyzer analyzer) {

        m_analyzers.put(analyzer.getLocale(), analyzer);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADD_ANALYZER_2,
                analyzer.getLocale(),
                analyzer.getClassName()));
        }
    }

    /**
     * Adds a document type.<p>
     * 
     * @param documentType a document type
     */
    public void addDocumentTypeConfig(CmsSearchDocumentType documentType) {

        m_documentTypeConfigs.add(documentType);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_SEARCH_DOC_TYPES_2,
                documentType.getName(),
                documentType.getClassName()));
        }
    }

    /**
     * Adds a search field configuration to the search manager.<p>
     * 
     * @param fieldConfiguration the search field configuration to add
     */
    public void addFieldConfiguration(CmsSearchFieldConfiguration fieldConfiguration) {

        m_fieldConfigurations.put(fieldConfiguration.getName(), fieldConfiguration);
    }

    /**
     * Adds a search index to the configuration.<p>
     * 
     * @param searchIndex the search index to add
     */
    public void addSearchIndex(CmsSearchIndex searchIndex) {

        if ((searchIndex.getSources() == null) || (searchIndex.getPath() == null)) {
            if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
                try {
                    searchIndex.initialize();
                } catch (CmsSearchException e) {
                    // should never happen
                }
            }
        }

        // name: not null or emtpy and unique
        String name = searchIndex.getName();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
        }
        if (m_indexSources.keySet().contains(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_CREATE_INVALID_NAME_1,
                name));
        }

        m_indexes.add(searchIndex);
        if (m_adminCms != null) {
            initOfflineIndexes();
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_ADD_SEARCH_INDEX_2,
                searchIndex.getName(),
                searchIndex.getProject()));
        }
    }

    /**
     * Adds a search index source configuration.<p>
     * 
     * @param searchIndexSource a search index source configuration
     */
    public void addSearchIndexSource(CmsSearchIndexSource searchIndexSource) {

        m_indexSources.put(searchIndexSource.getName(), searchIndexSource);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_SEARCH_INDEX_SOURCE_2,
                searchIndexSource.getName(),
                searchIndexSource.getIndexerClassName()));
        }
    }

    /**
     * Implements the event listener of this class.<p>
     * 
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_REBUILD_SEARCHINDEXES:
                List<String> indexNames = null;
                if ((event.getData() != null)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)event.getData().get(
                        I_CmsEventListener.KEY_INDEX_NAMES))) {
                    indexNames = CmsStringUtil.splitAsList((String)event.getData().get(
                        I_CmsEventListener.KEY_INDEX_NAMES), ",", true);
                }
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_EVENT_REBUILD_SEARCHINDEX_1,
                                indexNames == null ? "" : CmsStringUtil.collectionAsString(indexNames, ",")),
                            new Exception());
                    }
                    if (indexNames == null) {
                        rebuildAllIndexes(getEventReport(event));
                    } else {
                        rebuildIndexes(indexNames, getEventReport(event));
                    }
                } catch (CmsException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.ERR_EVENT_REBUILD_SEARCHINDEX_1,
                            indexNames == null ? "" : CmsStringUtil.collectionAsString(indexNames, ",")), e);
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_CLEAR_CACHES_0), new Exception());
                }
                break;
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                // event data contains a list of the published resources
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_PUBLISH_PROJECT_1, publishHistoryId));
                }
                updateAllIndexes(m_adminCms, publishHistoryId, getEventReport(event));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_EVENT_PUBLISH_PROJECT_FINISHED_1,
                        publishHistoryId));
                }
                break;
            default:
                // no operation
        }
    }

    /**
     * Returns an analyzer for the given language.<p>
     * 
     * The analyzer is selected according to the analyzer configuration.<p>
     * 
     * @param locale the locale to get the analyzer for
     * @return the appropriate lucene analyzer
     * @throws CmsSearchException if something goes wrong
     */
    public Analyzer getAnalyzer(Locale locale) throws CmsSearchException {

        Analyzer analyzer = null;
        String className = null;

        CmsSearchAnalyzer analyzerConf = m_analyzers.get(locale);
        if (analyzerConf == null) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_ANALYZER_NOT_FOUND_1, locale));
        }

        try {
            className = analyzerConf.getClassName();
            Class<?> analyzerClass = Class.forName(className);

            // added parameter for snowball analyzer
            String stemmerAlgorithm = analyzerConf.getStemmerAlgorithm();
            if (stemmerAlgorithm != null) {
                analyzer = (Analyzer)analyzerClass.getDeclaredConstructor(new Class[] {String.class}).newInstance(
                    new Object[] {stemmerAlgorithm});
            } else {
                analyzer = (Analyzer)analyzerClass.newInstance();
            }

        } catch (Exception e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_LOAD_ANALYZER_1, className), e);
        }

        return analyzer;
    }

    /**
     * Returns an unmodifiable view of the map that contains the {@link CmsSearchAnalyzer} list.<p>
     * 
     * The keys in the map are {@link Locale} objects, and the values are {@link CmsSearchAnalyzer} objects.
     *
     * @return an unmodifiable view of the Analyzers Map
     */
    public Map<Locale, CmsSearchAnalyzer> getAnalyzers() {

        return Collections.unmodifiableMap(m_analyzers);
    }

    /**
     * Returns the search analyzer for the given locale.<p>
     * 
     * @param locale the locale to get the analyzer for
     * 
     * @return the search analyzer for the given locale
     */
    public CmsSearchAnalyzer getCmsSearchAnalyzer(Locale locale) {

        return m_analyzers.get(locale);
    }

    /**
     * Returns the name of the directory below WEB-INF/ where the search indexes are stored.<p>
     * 
     * @return the name of the directory below WEB-INF/ where the search indexes are stored
     */
    public String getDirectory() {

        return m_path;
    }

    /**
     * Returns a document type config.<p>
     * 
     * @param name the name of the document type config
     * @return the document type config.
     */
    public CmsSearchDocumentType getDocumentTypeConfig(String name) {

        // this is really used only for the search manager GUI, 
        // so performance is not an issue and no lookup map is generated
        for (int i = 0; i < m_documentTypeConfigs.size(); i++) {
            CmsSearchDocumentType type = m_documentTypeConfigs.get(i);
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable view (read-only) of the DocumentTypeConfigs Map.<p>
     *
     * @return an unmodifiable view (read-only) of the DocumentTypeConfigs Map
     */
    public List<CmsSearchDocumentType> getDocumentTypeConfigs() {

        return Collections.unmodifiableList(m_documentTypeConfigs);
    }

    /**
     * Returns the maximum age a text extraction result is kept in the cache (in hours).<p>
     *
     * @return the maximum age a text extraction result is kept in the cache (in hours)
     */
    public float getExtractionCacheMaxAge() {

        return m_extractionCacheMaxAge;
    }

    /**
     * Returns the search field configuration with the given name.<p>
     * 
     * In case no configuration is available with the given name, <code>null</code> is returned.<p>
     * 
     * @param name the name to get the search field configuration for
     * 
     * @return the search field configuration with the given name
     */
    public CmsSearchFieldConfiguration getFieldConfiguration(String name) {

        return m_fieldConfigurations.get(name);
    }

    /**
     * Returns the unmodifieable List of configured {@link CmsSearchFieldConfiguration} entries.<p>
     * 
     * @return the unmodifieable List of configured {@link CmsSearchFieldConfiguration} entries
     */
    public List<CmsSearchFieldConfiguration> getFieldConfigurations() {

        List<CmsSearchFieldConfiguration> result = new ArrayList<CmsSearchFieldConfiguration>(
            m_fieldConfigurations.values());
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the force unlock mode during indexing.<p>
     *
     * @return the force unlock mode during indexing
     */
    public CmsSearchForceUnlockMode getForceunlock() {

        return m_forceUnlockMode;
    }

    /**
     * Returns the highlighter.<p>
     * 
     * @return the highlighter
     */
    public I_CmsTermHighlighter getHighlighter() {

        return m_highlighter;
    }

    /**
     * Returns the index belonging to the passed name.<p>
     * The index must exist already.
     * 
     * @param indexName then name of the index
     * @return an object representing the desired index
     */
    public CmsSearchIndex getIndex(String indexName) {

        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            CmsSearchIndex searchIndex = m_indexes.get(i);

            if (indexName.equalsIgnoreCase(searchIndex.getName())) {
                return searchIndex;
            }
        }

        return null;
    }

    /**
     * Returns the seconds to wait for an index lock during an update operation.<p>
     * 
     * @return the seconds to wait for an index lock during an update operation
     */
    public int getIndexLockMaxWaitSeconds() {

        return m_indexLockMaxWaitSeconds;
    }

    /**
     * Returns the names of all configured indexes.<p>
     * 
     * @return list of names
     */
    public List<String> getIndexNames() {

        List<String> indexNames = new ArrayList<String>();
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            indexNames.add((m_indexes.get(i)).getName());
        }

        return indexNames;
    }

    /**
     * Returns a search index source for a specified source name.<p>
     * 
     * @param sourceName the name of the index source
     * @return a search index source
     */
    public CmsSearchIndexSource getIndexSource(String sourceName) {

        return m_indexSources.get(sourceName);
    }

    /**
     * Returns the max. excerpt length.<p>
     *
     * @return the max excerpt length
     */
    public int getMaxExcerptLength() {

        return m_maxExcerptLength;
    }

    /**
     * Returns the update frequency of the offline indexer in milliseconds.<p>
     *
     * @return the update frequency of the offline indexer in milliseconds
     */
    public long getOfflineUpdateFrequency() {

        return m_offlineUpdateFrequency;
    }

    /**
     * Returns an unmodifiable list of all configured <code>{@link CmsSearchIndex}</code> instances.<p>
     * 
     * @return an unmodifiable list of all configured <code>{@link CmsSearchIndex}</code> instances
     */
    public List<CmsSearchIndex> getSearchIndexes() {

        return Collections.unmodifiableList(m_indexes);
    }

    /**
     * Returns an unmodifiable view (read-only) of the SearchIndexSources Map.<p>
     * 
     * @return an unmodifiable view (read-only) of the SearchIndexSources Map
     */
    public Map<String, CmsSearchIndexSource> getSearchIndexSources() {

        return Collections.unmodifiableMap(m_indexSources);
    }

    /**
     * Returns the timeout to abandon threads indexing a resource.<p>
     *
     * @return the timeout to abandon threads indexing a resource
     */
    public long getTimeout() {

        return m_timeout;
    }

    /**
     * Initializes the search manager.<p>
     * 
     * @param cms the cms object
     * 
     * @throws CmsRoleViolationException in case the given opencms object does not have <code>{@link CmsRole#WORKPLACE_MANAGER}</code> permissions
     */
    public void initialize(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.WORKPLACE_MANAGER);
        try {
            // store the Admin cms to index Cms resources
            m_adminCms = OpenCms.initCmsObject(cms);
        } catch (CmsException e) {
            // this should never happen
        }
        // make sure the site root is the root site
        m_adminCms.getRequestContext().setSiteRoot("/");

        // create the extraction result cache
        m_extractionResultCache = new CmsExtractionResultCache(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(getDirectory()),
            "/extractCache");

        initializeIndexes();
        initOfflineIndexes();

        // register the modified default similarity implementation
        Similarity.setDefault(new CmsSearchSimilarity());

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_REBUILD_SEARCHINDEXES});
    }

    /**
     * Initializes all configured document types and search indexes.<p>
     * 
     * This methods needs to be called if after a change in the index configuration has been made.
     */
    public void initializeIndexes() {

        initAvailableDocumentTypes();
        initSearchIndexes();
    }

    /**
     * Initialize the offline index handler, require after an offline index has been added.<p>
     */
    public void initOfflineIndexes() {

        // check which indexes are configured as offline indexes
        List<CmsSearchIndex> offlineIndexes = new ArrayList<CmsSearchIndex>();
        Iterator<CmsSearchIndex> i = m_indexes.iterator();
        while (i.hasNext()) {
            CmsSearchIndex index = i.next();
            if (CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode())) {
                // this is an offline index
                offlineIndexes.add(index);
            }
        }
        m_offlineIndexes = offlineIndexes;
        m_offlineHandler.initialize();

    }

    /**
     * Updates the indexes from as a scheduled job.<p> 
     * 
     * @param cms the OpenCms user context to use when reading resources from the VFS
     * @param parameters the parameters for the scheduled job
     * 
     * @throws Exception if something goes wrong
     * 
     * @return the String to write in the scheduler log
     * 
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        CmsSearchManager manager = OpenCms.getSearchManager();

        I_CmsReport report = null;
        boolean writeLog = Boolean.valueOf((String)parameters.get(JOB_PARAM_WRITELOG)).booleanValue();

        if (writeLog) {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsSearchManager.class);
        }

        List<String> updateList = null;
        String indexList = (String)parameters.get(JOB_PARAM_INDEXLIST);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(indexList)) {
            // index list has been provided as job parameter
            updateList = new ArrayList<String>();
            String[] indexNames = CmsStringUtil.splitAsArray(indexList, '|');
            for (int i = 0; i < indexNames.length; i++) {
                // check if the index actually exists
                if (manager.getIndex(indexNames[i]) != null) {
                    updateList.add(indexNames[i]);
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_INDEX_WITH_NAME_1, indexNames[i]));
                    }
                }
            }
        }

        long startTime = System.currentTimeMillis();

        if (updateList == null) {
            // all indexes need to be updated
            manager.rebuildAllIndexes(report);
        } else {
            // rebuild only the selected indexes
            manager.rebuildIndexes(updateList, report);
        }

        long runTime = System.currentTimeMillis() - startTime;

        String finishMessage = Messages.get().getBundle().key(
            Messages.LOG_REBUILD_INDEXES_FINISHED_1,
            CmsStringUtil.formatRuntime(runTime));

        if (LOG.isInfoEnabled()) {
            LOG.info(finishMessage);
        }
        return finishMessage;
    }

    /**
     * Rebuilds (if required creates) all configured indexes.<p>
     * 
     * @param report the report object to write messages (or <code>null</code>)
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized void rebuildAllIndexes(I_CmsReport report) throws CmsException {

        CmsMessageContainer container = null;
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            // iterate all configured search indexes
            CmsSearchIndex searchIndex = m_indexes.get(i);
            try {
                // update the index 
                updateIndex(searchIndex, report, null);
            } catch (CmsException e) {
                container = new CmsMessageContainer(
                    Messages.get(),
                    Messages.ERR_INDEX_REBUILD_ALL_1,
                    new Object[] {searchIndex.getName()});
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_REBUILD_ALL_1, searchIndex.getName()), e);
            }
        }
        // clean up the extraction result cache
        cleanExtractionCache();
        if (container != null) {
            // throw stored exception
            throw new CmsSearchException(container);
        }
    }

    /**
     * Rebuilds (if required creates) the index with the given name.<p>
     * 
     * @param indexName the name of the index to rebuild
     * @param report the report object to write messages (or <code>null</code>)
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized void rebuildIndex(String indexName, I_CmsReport report) throws CmsException {

        // get the search index by name
        CmsSearchIndex index = getIndex(indexName);
        // update the index 
        updateIndex(index, report, null);
        // clean up the extraction result cache
        cleanExtractionCache();
    }

    /**
     * Rebuilds (if required creates) the List of indexes with the given name.<p>
     * 
     * @param indexNames the names (String) of the index to rebuild
     * @param report the report object to write messages (or <code>null</code>)
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized void rebuildIndexes(List<String> indexNames, I_CmsReport report) throws CmsException {

        Iterator<String> i = indexNames.iterator();
        while (i.hasNext()) {
            String indexName = i.next();
            // get the search index by name
            CmsSearchIndex index = getIndex(indexName);
            if (index != null) {
                // update the index 
                updateIndex(index, report, null);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_INDEX_WITH_NAME_1, indexName));
                }
            }
        }
        // clean up the extraction result cache
        cleanExtractionCache();
    }

    /**
     * Removes this fieldconfiguration from the OpenCms configuration (if it is not used any more).<p>
     * 
     * @param fieldConfiguration the fieldconfiguration to remove from the configuration 
     * 
     * @return true if remove was successful, false if preconditions for removal are ok but the given 
     *         field configuration was unknown to the manager.
     * 
     * @throws CmsIllegalStateException if the given field configuration is still used by at least one 
     *         <code>{@link CmsSearchIndex}</code>.
     *  
     */
    public boolean removeSearchFieldConfiguration(CmsSearchFieldConfiguration fieldConfiguration)
    throws CmsIllegalStateException {

        // never remove the standard field configuration
        if (fieldConfiguration.getName().equals(CmsSearchFieldConfiguration.STR_STANDARD)) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_INDEX_CONFIGURATION_DELETE_STANDARD_1,
                fieldConfiguration.getName()));
        }
        // validation if removal will be granted
        Iterator<CmsSearchIndex> itIndexes = m_indexes.iterator();
        CmsSearchIndex idx;
        // the list for collecting indexes that use the given field configuration
        List<CmsSearchIndex> referrers = new ArrayList<CmsSearchIndex>();
        CmsSearchFieldConfiguration refFieldConfig;
        while (itIndexes.hasNext()) {
            idx = itIndexes.next();
            refFieldConfig = idx.getFieldConfiguration();
            if (refFieldConfig.equals(fieldConfiguration)) {
                referrers.add(idx);
            }
        }
        if (referrers.size() > 0) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_INDEX_CONFIGURATION_DELETE_2,
                fieldConfiguration.getName(),
                referrers.toString()));
        }

        // remove operation (no exception)
        return m_fieldConfigurations.remove(fieldConfiguration.getName()) != null;

    }

    /**
     * Removes a search field from the field configuration.<p>
     * 
     * @param fieldConfiguration the field configuration
     * @param field field to remove from the field configuration
     * 
     * @return true if remove was successful, false if preconditions for removal are ok but the given 
     *         field was unknown.
     * 
     * @throws CmsIllegalStateException if the given field is the last field inside the given field configuration.
     */
    public boolean removeSearchFieldConfigurationField(
        CmsSearchFieldConfiguration fieldConfiguration,
        CmsSearchField field) throws CmsIllegalStateException {

        if (fieldConfiguration.getFields().size() < 2) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_CONFIGURATION_FIELD_DELETE_2,
                field.getName(),
                fieldConfiguration.getName()));
        } else {

            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_REMOVE_FIELDCONFIGURATION_FIELD_INDEX_2,
                    field.getName(),
                    fieldConfiguration.getName()));
            }

            return fieldConfiguration.getFields().remove(field);
        }
    }

    /**
     * Removes a search field mapping from the given field.<p>
     * 
     * @param field the field
     * @param mapping mapping to remove from the field
     * 
     * @return true if remove was successful, false if preconditions for removal are ok but the given 
     *         mapping was unknown.
     * 
     * @throws CmsIllegalStateException if the given mapping is the last mapping inside the given field.
     */
    public boolean removeSearchFieldMapping(CmsSearchField field, CmsSearchFieldMapping mapping)
    throws CmsIllegalStateException {

        if (field.getMappings().size() < 2) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_FIELD_MAPPING_DELETE_2,
                mapping.getType().toString(),
                field.getName()));
        } else {

            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_REMOVE_FIELD_MAPPING_INDEX_2,
                    mapping.toString(),
                    field.getName()));
            }
            return field.getMappings().remove(mapping);
        }
    }

    /**
     * Removes a search index from the configuration.<p>
     * 
     * @param searchIndex the search index to remove
     */
    public void removeSearchIndex(CmsSearchIndex searchIndex) {

        m_indexes.remove(searchIndex);
        initOfflineIndexes();

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(
                Messages.LOG_REMOVE_SEARCH_INDEX_2,
                searchIndex.getName(),
                searchIndex.getProject()));
        }
    }

    /**
     * Removes all indexes included in the given list (which must contain the name of an index to remove).<p>
     * 
     * @param indexNames the names of the index to remove
     */
    public void removeSearchIndexes(List<String> indexNames) {

        Iterator<String> i = indexNames.iterator();
        while (i.hasNext()) {
            String indexName = i.next();
            // get the search index by name
            CmsSearchIndex index = getIndex(indexName);
            if (index != null) {
                // remove the index 
                removeSearchIndex(index);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_NO_INDEX_WITH_NAME_1, indexName));
                }
            }
        }
    }

    /**
     * Removes this indexsource from the OpenCms configuration (if it is not used any more).<p>
     * 
     * @param indexsource the indexsource to remove from the configuration 
     * 
     * @return true if remove was successful, false if preconditions for removal are ok but the given 
     *         searchindex was unknown to the manager.
     * 
     * @throws CmsIllegalStateException if the given indexsource is still used by at least one 
     *         <code>{@link CmsSearchIndex}</code>.
     *  
     */
    public boolean removeSearchIndexSource(CmsSearchIndexSource indexsource) throws CmsIllegalStateException {

        // validation if removal will be granted
        Iterator<CmsSearchIndex> itIndexes = m_indexes.iterator();
        CmsSearchIndex idx;
        // the list for collecting indexes that use the given index source
        List<CmsSearchIndex> referrers = new ArrayList<CmsSearchIndex>();
        // the current list of referred index sources of the iterated index
        List<CmsSearchIndexSource> refsources;
        while (itIndexes.hasNext()) {
            idx = itIndexes.next();
            refsources = idx.getSources();
            if (refsources != null) {
                if (refsources.contains(indexsource)) {
                    referrers.add(idx);
                }
            }
        }
        if (referrers.size() > 0) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_INDEX_SOURCE_DELETE_2,
                indexsource.getName(),
                referrers.toString()));
        }

        // remove operation (no exception) 
        return m_indexSources.remove(indexsource.getName()) != null;

    }

    /**
     * Sets the name of the directory below WEB-INF/ where the search indexes are stored.<p>
     * 
     * @param value the name of the directory below WEB-INF/ where the search indexes are stored
     */
    public void setDirectory(String value) {

        m_path = value;
    }

    /**
     * Sets the maximum age a text extraction result is kept in the cache (in hours).<p>
     *
     * @param extractionCacheMaxAge the maximum age for a text extraction result to set
     */
    public void setExtractionCacheMaxAge(float extractionCacheMaxAge) {

        m_extractionCacheMaxAge = extractionCacheMaxAge;
    }

    /**
     * Sets the maximum age a text extraction result is kept in the cache (in hours) as a String.<p>
     *
     * @param extractionCacheMaxAge the maximum age for a text extraction result to set
     */
    public void setExtractionCacheMaxAge(String extractionCacheMaxAge) {

        try {
            setExtractionCacheMaxAge(Float.parseFloat(extractionCacheMaxAge));
        } catch (NumberFormatException e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_PARSE_EXTRACTION_CACHE_AGE_FAILED_2,
                extractionCacheMaxAge,
                new Float(DEFAULT_EXTRACTION_CACHE_MAX_AGE)), e);
            setExtractionCacheMaxAge(DEFAULT_EXTRACTION_CACHE_MAX_AGE);
        }
    }

    /**
     * Sets the unlock mode during indexing.<p>
     * 
     * @param value the value 
     */
    public void setForceunlock(String value) {

        m_forceUnlockMode = CmsSearchForceUnlockMode.valueOf(value);
    }

    /**
     * Sets the highlighter.<p>
     *
     * A highlighter is a class implementing org.opencms.search.documents.I_TermHighlighter.<p>
     *
     * @param highlighter the package/class name of the highlighter
     */
    public void setHighlighter(String highlighter) {

        try {
            m_highlighter = (I_CmsTermHighlighter)Class.forName(highlighter).newInstance();
        } catch (Exception exc) {
            m_highlighter = null;
        }
    }

    /**
     * Sets the seconds to wait for an index lock during an update operation.<p>
     * 
     * @param value the seconds to wait for an index lock during an update operation
     */
    public void setIndexLockMaxWaitSeconds(int value) {

        m_indexLockMaxWaitSeconds = value;
    }

    /**
     * Sets the max. excerpt length.<p>
     *
     * @param maxExcerptLength the max. excerpt length to set
     */
    public void setMaxExcerptLength(int maxExcerptLength) {

        m_maxExcerptLength = maxExcerptLength;
    }

    /**
     * Sets the max. excerpt length as a String.<p>
     *
     * @param maxExcerptLength the max. excerpt length to set
     */
    public void setMaxExcerptLength(String maxExcerptLength) {

        try {
            setMaxExcerptLength(Integer.parseInt(maxExcerptLength));
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_PARSE_EXCERPT_LENGTH_FAILED_2,
                maxExcerptLength,
                new Integer(DEFAULT_EXCERPT_LENGTH)), e);
            setMaxExcerptLength(DEFAULT_EXCERPT_LENGTH);
        }
    }

    /**
     * Sets the update frequency of the offline indexer in milliseconds.<p>
     *
     * @param offlineUpdateFrequency the update frequency in milliseconds to set
     */
    public void setOfflineUpdateFrequency(long offlineUpdateFrequency) {

        m_offlineUpdateFrequency = offlineUpdateFrequency;
    }

    /**
     * Sets the update frequency of the offline indexer in milliseconds.<p>
     *
     * @param offlineUpdateFrequency the update frequency in milliseconds to set
     */
    public void setOfflineUpdateFrequency(String offlineUpdateFrequency) {

        try {
            setOfflineUpdateFrequency(Long.parseLong(offlineUpdateFrequency));
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_PARSE_OFFLINE_UPDATE_FAILED_2,
                offlineUpdateFrequency,
                new Long(DEFAULT_OFFLINE_UPDATE_FREQNENCY)), e);
            setOfflineUpdateFrequency(DEFAULT_OFFLINE_UPDATE_FREQNENCY);
        }
    }

    /**
     * Sets the timeout to abandon threads indexing a resource.<p>
     * 
     * @param value the timeout in milliseconds
     */
    public void setTimeout(long value) {

        m_timeout = value;
    }

    /**
     * Sets the timeout to abandon threads indexing a resource as a String.<p>
     * 
     * @param value the timeout in milliseconds
     */
    public void setTimeout(String value) {

        try {
            setTimeout(Long.parseLong(value));
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_PARSE_TIMEOUT_FAILED_2,
                value,
                new Long(DEFAULT_TIMEOUT)), e);
            setTimeout(DEFAULT_TIMEOUT);
        }
    }

    /**
     * Shuts down the search manager.<p>
     * 
     * This will cause all search indices to be shut down.<p>
     */
    public void shutDown() {

        if (m_offlineIndexThread != null) {
            m_offlineIndexThread.shutDown();
        }
        Iterator<CmsSearchIndex> i = m_indexes.iterator();
        while (i.hasNext()) {
            CmsSearchIndex index = i.next();
            try {
                index.shutDown();
            } catch (IOException e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SHUTDOWN_1, index.getName()), e);
            }
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_MANAGER_0));
        }
    }

    /**
     * Cleans up the extraction result cache.<p>
     */
    protected void cleanExtractionCache() {

        // clean up the extraction result cache
        m_extractionResultCache.cleanCache(m_extractionCacheMaxAge);
    }

    /**
     * Proceed the unlocking of the given index depending on the setting of <code>m_forceUnlockMode</code> and the given mode.<p>
     * 
     * @param index the index to check the lock for
     * @param report the report to write error messages on
     * @param mode the mode of the index process if true the index is updated otherwise it is rebuild completely
     * 
     * @throws CmsIndexException if unlocking of the index is impossible for some reasons
     */
    protected void forceIndexUnlock(CmsSearchIndex index, I_CmsReport report, boolean mode) throws CmsIndexException {

        File indexPath = new File(index.getPath());
        boolean indexLocked = true;
        // check if the target index path already exists
        if (indexPath.exists()) {
            // get the lock state of the given index
            try {
                indexLocked = IndexWriter.isLocked(index.getPath());
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_IO_INDEX_READER_OPEN_2,
                    index.getPath(),
                    index.getName()), e);
            }

            // if index is unlocked do nothing
            if (indexLocked) {
                if ((m_forceUnlockMode != null) && m_forceUnlockMode.equals(CmsSearchForceUnlockMode.ALWAYS)) {
                    try {
                        // try to force unlock on the index                        
                        IndexWriter.unlock(FSDirectory.getDirectory(index.getPath()));
                    } catch (Exception e) {
                        // unable to force unlock of Lucene index, we can't continue this way
                        CmsMessageContainer msg = Messages.get().container(
                            Messages.ERR_INDEX_LOCK_FAILED_1,
                            index.getName());
                        report.println(msg, I_CmsReport.FORMAT_ERROR);
                        throw new CmsIndexException(msg, e);
                    }
                } else if ((m_forceUnlockMode != null) && m_forceUnlockMode.equals(CmsSearchForceUnlockMode.NEVER)) {
                    // wait if index will be unlocked during waiting
                    indexLocked = waitIndexLock(index, report, indexLocked);
                    // if index is still locked throw an exception
                    if (indexLocked) {
                        CmsMessageContainer msg = Messages.get().container(
                            Messages.ERR_INDEX_LOCK_FAILED_1,
                            index.getName());
                        report.println(msg, I_CmsReport.FORMAT_ERROR);
                        throw new CmsIndexException(msg);
                    }
                } else {
                    if (mode) {
                        // if index has to be updated wait if index will be unlocked during waiting
                        indexLocked = waitIndexLock(index, report, indexLocked);
                    }
                    // check if the index is locked
                    if (indexLocked) {
                        // mode equals update throw exception
                        if (mode) {
                            // unable to lock the index for updating
                            CmsMessageContainer msg = Messages.get().container(
                                Messages.ERR_INDEX_LOCK_FAILED_1,
                                index.getName());
                            report.println(msg, I_CmsReport.FORMAT_ERROR);
                            throw new CmsIndexException(msg);
                        } else {
                            try {
                                // try to force unlock on the index
                                IndexWriter.unlock(FSDirectory.getDirectory(index.getPath()));
                            } catch (Exception e) {
                                // unable to force unlock of Lucene index, we can't continue this way
                                CmsMessageContainer msg = Messages.get().container(
                                    Messages.ERR_INDEX_LOCK_FAILED_1,
                                    index.getName());
                                report.println(msg, I_CmsReport.FORMAT_ERROR);
                                throw new CmsIndexException(msg, e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a lucene document factory for given resource.<p>
     * 
     * The type of the document factory is selected by the type of the resource
     * and the MIME type of the resource content, according to the configuration in <code>opencms-search.xml</code>.<p>
     * 
     * @param resource a cms resource
     * @return a lucene document factory or null
     */
    protected I_CmsDocumentFactory getDocumentFactory(CmsResource resource) {

        // first get the MIME type of the resource
        String mimeType = OpenCms.getResourceManager().getMimeType(resource.getRootPath(), null, "unknown");
        I_CmsDocumentFactory result = null;
        String typeName = null;
        try {
            typeName = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();
        } catch (CmsLoaderException e) {
            // ignore, unknown resource type, resource can not be indexed
        }
        if (typeName != null) {
            // create the factory lookup key for the document
            String documentTypeKey = A_CmsVfsDocument.getDocumentKey(typeName, mimeType);
            // check if a setting is available for this specific MIME type
            result = m_documentTypes.get(documentTypeKey);
            if (result == null) {
                // no setting is available, try to use a generic setting without MIME type
                result = m_documentTypes.get(A_CmsVfsDocument.getDocumentKey(typeName, null));
                // please note: the result may still be null
            }
        }
        return result;
    }

    /**
     * Returns the set of names of all configured document types.<p>
     * 
     * @return the set of names of all configured document types
     */
    protected List<String> getDocumentTypes() {

        List<String> names = new ArrayList<String>();
        for (Iterator<I_CmsDocumentFactory> i = m_documentTypes.values().iterator(); i.hasNext();) {
            I_CmsDocumentFactory factory = i.next();
            names.add(factory.getName());
        }

        return names;
    }

    /**
     * Initializes the available Cms resource types to be indexed.<p>
     * 
     * A map stores document factories keyed by a string representing
     * a colon separated list of Cms resource types and/or mimetypes.<p>
     * 
     * The keys of this map are used to trigger a document factory to convert 
     * a Cms resource into a Lucene index document.<p>
     * 
     * A document factory is a class implementing the interface
     * {@link org.opencms.search.documents.I_CmsDocumentFactory}.<p>
     */
    protected void initAvailableDocumentTypes() {

        CmsSearchDocumentType documenttype = null;
        String className = null;
        String name = null;
        I_CmsDocumentFactory documentFactory = null;
        List<String> resourceTypes = null;
        List<String> mimeTypes = null;
        Class<?> c = null;

        m_documentTypes = new HashMap<String, I_CmsDocumentFactory>();

        for (int i = 0, n = m_documentTypeConfigs.size(); i < n; i++) {

            documenttype = m_documentTypeConfigs.get(i);
            name = documenttype.getName();

            try {
                className = documenttype.getClassName();
                resourceTypes = documenttype.getResourceTypes();
                mimeTypes = documenttype.getMimeTypes();

                if (name == null) {
                    throw new CmsIndexException(Messages.get().container(Messages.ERR_DOCTYPE_NO_NAME_0));
                }
                if (className == null) {
                    throw new CmsIndexException(Messages.get().container(Messages.ERR_DOCTYPE_NO_CLASS_DEF_0));
                }
                if (resourceTypes.size() == 0) {
                    throw new CmsIndexException(Messages.get().container(Messages.ERR_DOCTYPE_NO_RESOURCETYPE_DEF_0));
                }

                try {
                    c = Class.forName(className);
                    documentFactory = (I_CmsDocumentFactory)c.getConstructor(new Class[] {String.class}).newInstance(
                        new Object[] {name});
                } catch (ClassNotFoundException exc) {
                    throw new CmsIndexException(
                        Messages.get().container(Messages.ERR_DOCCLASS_NOT_FOUND_1, className),
                        exc);
                } catch (Exception exc) {
                    throw new CmsIndexException(Messages.get().container(Messages.ERR_DOCCLASS_INIT_1, className), exc);
                }

                if (documentFactory.isUsingCache()) {
                    // init cache if used by the factory
                    documentFactory.setCache(m_extractionResultCache);
                }

                for (Iterator<String> key = documentFactory.getDocumentKeys(resourceTypes, mimeTypes).iterator(); key.hasNext();) {
                    m_documentTypes.put(key.next(), documentFactory);
                }

            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_DOCTYPE_CONFIG_FAILED_1, name), e);
                }
            }
        }
    }

    /**
     * Initializes the configured search indexes.<p>
     * 
     * This initializes also the list of Cms resources types
     * to be indexed by an index source.<p>
     */
    protected void initSearchIndexes() {

        CmsSearchIndex index = null;
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            index = m_indexes.get(i);
            // reset disabled flag
            index.setEnabled(true);
            // check if the index has been configured correctly
            if (index.checkConfiguration(m_adminCms)) {
                // the index is configured correctly
                try {
                    index.initialize();
                } catch (CmsException e) {
                    // in this case the index will be disabled
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().getBundle().key(
                            Messages.INIT_SEARCH_INIT_FAILED_1,
                            index.getName()), e);
                    }
                }
            }
            if (CmsLog.INIT.isInfoEnabled()) {
                // output a log message if the index was successfully configured or not
                if (index.isEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_INDEX_CONFIGURED_2,
                        index.getName(),
                        index.getProject()));
                } else {
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_INDEX_NOT_CONFIGURED_2,
                        index.getName(),
                        index.getProject()));
                }
            }
        }
    }

    /**
     * Incrementally updates all indexes that have their rebuild mode set to <code>"auto"</code>
     * after resources have been published.<p> 
     * 
     * @param adminCms an OpenCms user context with Admin permissions
     * @param publishHistoryId the history ID of the published project 
     * @param report the report to write the output to
     */
    protected synchronized void updateAllIndexes(CmsObject adminCms, CmsUUID publishHistoryId, I_CmsReport report) {

        int oldPrio = Thread.currentThread().getPriority();
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            List<CmsPublishedResource> publishedResources;
            try {
                // read the list of all published resources
                publishedResources = adminCms.readPublishedResources(publishHistoryId);
            } catch (CmsException e) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_READING_CHANGED_RESOURCES_FAILED_1,
                    publishHistoryId), e);
                return;
            }

            List<CmsPublishedResource> updateResources = new ArrayList<CmsPublishedResource>();
            Iterator<CmsPublishedResource> itPubRes = publishedResources.iterator();
            while (itPubRes.hasNext()) {
                CmsPublishedResource res = itPubRes.next();
                if (res.isFolder() || res.getState().isUnchanged()) {
                    // folders and unchanged resources don't need to be indexed after publish
                    continue;
                }
                if (res.getState().isDeleted() || res.getState().isNew() || res.getState().isChanged()) {
                    if (updateResources.contains(res)) {
                        // resource may have been added as a sibling of another resource
                        // in this case we make sure to use the value from the publish list because of the "deleted" flag
                        boolean hasMoved = (res.getMovedState() == CmsPublishedResource.STATE_MOVED_DESTINATION)
                            || (res.getMovedState() == CmsPublishedResource.STATE_MOVED_SOURCE);
                        // check it this is a moved resource with source / target info, in this case we need both entries
                        if (!hasMoved) {
                            // if the resource was moved, we must contain both entries
                            updateResources.remove(res);
                        }
                        // "equals()" implementation of published resource checks for id, 
                        // so the removed value may have a different "deleted" or "modified" status value
                        updateResources.add(res);
                    } else {
                        // resource not yet contained in the list
                        updateResources.add(res);
                        // check for the siblings (not for deleted resources, these are already gone)
                        if (!res.getState().isDeleted() && (res.getSiblingCount() > 1)) {
                            // this resource has siblings                    
                            try {
                                // read siblings from the online project
                                List<CmsResource> siblings = adminCms.readSiblings(
                                    res.getRootPath(),
                                    CmsResourceFilter.ALL);
                                Iterator<CmsResource> itSib = siblings.iterator();
                                while (itSib.hasNext()) {
                                    // check all siblings
                                    CmsResource sibling = itSib.next();
                                    CmsPublishedResource sib = new CmsPublishedResource(sibling);
                                    if (!updateResources.contains(sib)) {
                                        // ensure sibling is added only once
                                        updateResources.add(sib);
                                    }
                                }
                            } catch (CmsException e) {
                                // ignore, just use the original resource
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn(Messages.get().getBundle().key(
                                        Messages.LOG_UNABLE_TO_READ_SIBLINGS_1,
                                        res.getRootPath()), e);
                                }
                            }
                        }
                    }
                }
            }

            if (!updateResources.isEmpty()) {
                // sort the resource to update
                Collections.sort(updateResources);
                // only update the indexes if the list of remaining published resources is not empty
                Iterator<CmsSearchIndex> i = m_indexes.iterator();
                while (i.hasNext()) {
                    CmsSearchIndex index = i.next();
                    if (CmsSearchIndex.REBUILD_MODE_AUTO.equals(index.getRebuildMode())) {
                        // only update indexes which have the rebuild mode set to "auto"
                        try {
                            updateIndex(index, report, updateResources);
                        } catch (CmsException e) {
                            LOG.error(Messages.get().getBundle().key(
                                Messages.LOG_UPDATE_INDEX_FAILED_1,
                                index.getName()), e);
                        }
                    }
                }
            }
            // clean up the extraction result cache
            cleanExtractionCache();
        } finally {
            Thread.currentThread().setPriority(oldPrio);
        }
    }

    /**
     * Updates (if required creates) the index with the given name.<p>
     * 
     * If the optional List of <code>{@link CmsPublishedResource}</code> instances is provided, the index will be 
     * incrementally updated for these resources only. If this List is <code>null</code> or empty, 
     * the index will be fully rebuild.<p>
     * 
     * @param index the index to update or rebuild
     * @param report the report to write output messages to 
     * @param resourcesToIndex an (optional) list of <code>{@link CmsPublishedResource}</code> objects to update in the index
     * 
     * @throws CmsException if something goes wrong
     */
    protected void updateIndex(CmsSearchIndex index, I_CmsReport report, List<CmsPublishedResource> resourcesToIndex)
    throws CmsException {

        // copy the stored admin context for the indexing
        CmsObject cms = OpenCms.initCmsObject(m_adminCms);
        // make sure a report is available
        if (report == null) {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsSearchManager.class);
        }

        // check if the index has been configured correctly
        if (!index.checkConfiguration(cms)) {
            // the index is disabled
            return;
        }

        // set site root and project for this index
        cms.getRequestContext().setSiteRoot("/");
        // switch to the index project
        cms.getRequestContext().setCurrentProject(cms.readProject(index.getProject()));

        if ((resourcesToIndex == null) || resourcesToIndex.isEmpty()) {
            // rebuild the complete index

            forceIndexUnlock(index, report, false);
            // create a new thread manager for the indexing threads
            CmsIndexingThreadManager threadManager = new CmsIndexingThreadManager(m_timeout);

            boolean isOfflineIndex = false;
            if (CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode())) {
                // disable offline indexing while the complete index is rebuild
                isOfflineIndex = true;
                index.setRebuildMode(CmsSearchIndex.REBUILD_MODE_MANUAL);
                // re-initialize the offline indexes, this will disable this offline index
                initOfflineIndexes();
            }

            IndexWriter writer = null;
            try {
                // create a backup of the existing index
                String backup = index.createIndexBackup();
                if (backup != null) {
                    index.indexSearcherOpen(backup);
                }
                // create a new index writer
                writer = index.getIndexWriter(true);

                // output start information on the report
                report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_REBUILD_BEGIN_1, index.getName()),
                    I_CmsReport.FORMAT_HEADLINE);

                // iterate all configured index sources of this index
                Iterator<CmsSearchIndexSource> sources = index.getSources().iterator();
                while (sources.hasNext()) {
                    // get the next index source
                    CmsSearchIndexSource source = sources.next();
                    // create the indexer
                    I_CmsIndexer indexer = source.getIndexer().newInstance(cms, report, index);
                    // new index creation, use all resources from the index source
                    indexer.rebuildIndex(writer, threadManager, source);

                    // wait for indexing threads to finish
                    while (threadManager.isRunning()) {
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            // just continue with the loop after interruption
                        }
                    }

                    // optimize and commit the index after each index source has been finished
                    try {
                        writer.optimize();
                        writer.commit();
                    } catch (IOException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(
                                Messages.LOG_IO_INDEX_WRITER_OPTIMIZE_1,
                                index.getPath(),
                                index.getName()), e);
                        }
                    }
                }

                if (backup != null) {
                    // remove the backup after the files have been re-indexed
                    index.indexSearcherClose();
                    index.removeIndexBackup(backup);
                }

                // output finish information on the report
                report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_REBUILD_END_1, index.getName()),
                    I_CmsReport.FORMAT_HEADLINE);

            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(
                                Messages.LOG_IO_INDEX_WRITER_CLOSE_2,
                                index.getPath(),
                                index.getName()), e);
                        }
                    }
                }
                if (isOfflineIndex) {
                    // reset the mode of the offline index
                    index.setRebuildMode(CmsSearchIndex.REBUILD_MODE_OFFLINE);
                    // re-initialize the offline indexes, this will re-enable this index
                    initOfflineIndexes();
                }
                // index has changed - initialize the index searcher instance
                index.indexSearcherOpen(index.getPath());
            }

            // show information about indexing runtime
            threadManager.reportStatistics(report);

        } else {
            updateIndexIncremental(cms, index, report, resourcesToIndex);
        }
    }

    /**
     * Incrementally updates the given index.<p>
     * 
     * @param cms the OpenCms user context to use for accessing the VFS
     * @param index the index to update
     * @param report the report to write output messages to 
     * @param resourcesToIndex a list of <code>{@link CmsPublishedResource}</code> objects to update in the index
     * 
     * @throws CmsException if something goes wrong
     */
    protected void updateIndexIncremental(
        CmsObject cms,
        CmsSearchIndex index,
        I_CmsReport report,
        List<CmsPublishedResource> resourcesToIndex) throws CmsException {

        // update the existing index
        List<CmsSearchIndexUpdateData> updateCollections = new ArrayList<CmsSearchIndexUpdateData>();

        boolean hasResourcesToDelete = false;
        boolean hasResourcesToUpdate = false;

        // iterate all configured index sources of this index
        Iterator<CmsSearchIndexSource> sources = index.getSources().iterator();
        while (sources.hasNext()) {
            // get the next index source
            CmsSearchIndexSource source = sources.next();
            // create the indexer
            I_CmsIndexer indexer = source.getIndexer().newInstance(cms, report, index);
            // collect the resources to update
            CmsSearchIndexUpdateData updateData = indexer.getUpdateData(source, resourcesToIndex);
            if (!updateData.isEmpty()) {
                // add the update collection to the internal pipeline
                updateCollections.add(updateData);
                hasResourcesToDelete = hasResourcesToDelete | updateData.hasResourcesToDelete();
                hasResourcesToUpdate = hasResourcesToUpdate | updateData.hasResourceToUpdate();
            }
        }

        // only start index modification if required
        if (hasResourcesToDelete || hasResourcesToUpdate) {
            // output start information on the report
            report.println(
                Messages.get().container(Messages.RPT_SEARCH_INDEXING_UPDATE_BEGIN_1, index.getName()),
                I_CmsReport.FORMAT_HEADLINE);

            // unlock the index
            forceIndexUnlock(index, report, true);

            IndexWriter writer = null;
            try {
                // obtain an index writer that updates the current index
                writer = index.getIndexWriter(false);

                if (hasResourcesToDelete) {
                    // delete the resource from the index
                    Iterator<CmsSearchIndexUpdateData> i = updateCollections.iterator();
                    while (i.hasNext()) {
                        CmsSearchIndexUpdateData updateCollection = i.next();
                        if (updateCollection.hasResourcesToDelete()) {
                            updateCollection.getIndexer().deleteResources(
                                writer,
                                updateCollection.getResourcesToDelete());
                        }
                    }
                }

                if (hasResourcesToUpdate) {
                    // create a new thread manager
                    CmsIndexingThreadManager threadManager = new CmsIndexingThreadManager(m_timeout);

                    Iterator<CmsSearchIndexUpdateData> i = updateCollections.iterator();
                    while (i.hasNext()) {
                        CmsSearchIndexUpdateData updateCollection = i.next();
                        if (updateCollection.hasResourceToUpdate()) {
                            updateCollection.getIndexer().updateResources(
                                writer,
                                threadManager,
                                updateCollection.getResourcesToUpdate());
                        }
                    }

                    // wait for indexing threads to finish
                    while (threadManager.isRunning()) {
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            // just continue with the loop after interruption
                        }
                    }
                }
            } finally {
                // close the index writer
                if (writer != null) {
                    try {
                        writer.commit();
                        writer.close();
                    } catch (IOException e) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_IO_INDEX_WRITER_CLOSE_2,
                            index.getPath(),
                            index.getName()), e);
                    }
                }
                // index has changed - initialize the index searcher instance
                index.indexSearcherOpen(index.getPath());
            }

            // output finish information on the report
            report.println(
                Messages.get().container(Messages.RPT_SEARCH_INDEXING_UPDATE_END_1, index.getName()),
                I_CmsReport.FORMAT_HEADLINE);
        }
    }

    /**
     * Updates the offline search indexes for the given list of resources.<p>
     * 
     * @param report the report to write the index information to
     * @param resourcesToIndex the list of {@link CmsPublishedResource} objects to index
     */
    protected void updateIndexOffline(I_CmsReport report, List<CmsPublishedResource> resourcesToIndex) {

        CmsObject cms = m_adminCms;
        try {
            // copy the administration context for the indexing
            cms = OpenCms.initCmsObject(m_adminCms);
            // set site root and project for this index
            cms.getRequestContext().setSiteRoot("/");
        } catch (CmsException e1) {
            // NOOP, should never happen
        }

        Iterator<CmsSearchIndex> j = m_offlineIndexes.iterator();
        while (j.hasNext()) {
            CmsSearchIndex index = j.next();
            if (index.getSources() != null) {
                try {
                    // switch to the index project
                    cms.getRequestContext().setCurrentProject(cms.readProject(index.getProject()));
                    updateIndexIncremental(cms, index, report, resourcesToIndex);
                } catch (CmsException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_UPDATE_INDEX_FAILED_1, index.getName()), e);
                }
            }
        }
    }

    /**
     * Returns the report in the given event data, if <code>null</code>
     * a new log report is used.<p>
     * 
     * @param event the event to get the report for
     * 
     * @return the report
     */
    private I_CmsReport getEventReport(CmsEvent event) {

        I_CmsReport report = null;
        if (event.getData() != null) {
            report = (I_CmsReport)event.getData().get(I_CmsEventListener.KEY_REPORT);
        }
        if (report == null) {
            report = new CmsLogReport(Locale.ENGLISH, getClass());
        }
        return report;
    }

    /**
     * Checks is a given index is locked, if so waits for a number of seconds and checks again,
     * until either the index is unlocked or a limit of seconds set by <code>{@link #setIndexLockMaxWaitSeconds(int)}</code>
     * is reached and returns the lock state of the index.<p>
     * 
     * @param index the index to check the lock for
     * @param report the report to write error messages on
     * @param indexLocked the boolean value if the index is locked
     * 
     * @return the lock state of the index
     */
    private boolean waitIndexLock(CmsSearchIndex index, I_CmsReport report, boolean indexLocked) {

        try {
            int lockSecs = 0;
            while (indexLocked && (lockSecs < m_indexLockMaxWaitSeconds)) {
                indexLocked = IndexWriter.isLocked(index.getPath());
                if (indexLocked) {
                    // index is still locked, wait one second
                    report.println(Messages.get().container(
                        Messages.RPT_SEARCH_INDEXING_LOCK_WAIT_2,
                        index.getName(),
                        new Integer(m_indexLockMaxWaitSeconds - lockSecs)), I_CmsReport.FORMAT_ERROR);
                    // sleep one second
                    Thread.sleep(1000);
                    lockSecs++;
                }
            }
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_IO_INDEX_READER_OPEN_2,
                index.getPath(),
                index.getName()), e);
        }
        return indexLocked;
    }
}
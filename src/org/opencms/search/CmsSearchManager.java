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

import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsSolrHandler;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsLogReport;
import org.opencms.report.CmsShellLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.documents.CmsExtractionResultCache;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.I_CmsSearchFieldConfiguration;
import org.opencms.search.solr.CmsSolrConfiguration;
import org.opencms.search.solr.CmsSolrFieldConfiguration;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.I_CmsSolrIndexWriter;
import org.opencms.search.solr.spellchecking.CmsSolrSpellchecker;
import org.opencms.search.solr.spellchecking.CmsSpellcheckDictionaryIndexer;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.A_CmsModeStringEnumeration;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsWaitHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient.Builder;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrCore;

/**
 * Implements the general management and configuration of the search and
 * indexing facilities in OpenCms.<p>
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
        @SuppressWarnings("unchecked")
        public void cmsEvent(CmsEvent event) {

            Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
            switch (event.getType()) {
                case I_CmsEventListener.EVENT_PROPERTY_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_CREATED:
                case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                    if ((change != null) && change.equals(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED))) {
                        // skip lock & unlock
                        return;
                    }
                    // skip indexing if flag is set in event
                    Object skip = event.getData().get(I_CmsEventListener.KEY_SKIPINDEX);
                    if (skip != null) {
                        return;
                    }

                    // a resource has been modified - offline indexes require (re)indexing
                    List<CmsResource> resources = Collections.singletonList(
                        (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE));
                    reIndexResources(resources);
                    break;
                case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                    List<CmsResource> eventResources = (List<CmsResource>)event.getData().get(
                        I_CmsEventListener.KEY_RESOURCES);
                    List<CmsResource> resourcesToDelete = new ArrayList<CmsResource>(eventResources);
                    for (CmsResource res : resourcesToDelete) {
                        if (res.getState().isNew()) {
                            // if the resource is new and a delete action was performed
                            // --> set the state of the resource to deleted
                            res.setState(CmsResourceState.STATE_DELETED);
                        }
                    }
                    reIndexResources(resourcesToDelete);
                    break;
                case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                    if (I_CmsEventListener.VALUE_CREATE_SIBLING.equals(change)) {
                        List<CmsResource> resList = (List<CmsResource>)event.getData().get(
                            I_CmsEventListener.KEY_RESOURCES);
                        if ((resList != null) && (resList.size() >= 3)) {
                            System.out.println("Sibling creation case, resource = " + resList.get(1).getRootPath());
                            reIndexResources(Collections.singletonList(resList.get(1)));

                        }
                    } else {
                        reIndexResources((List<CmsResource>)event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    }
                    break;
                case I_CmsEventListener.EVENT_RESOURCE_MOVED:
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
        protected List<CmsPublishedResource> getResourcesToIndex() {

            List<CmsPublishedResource> result;
            synchronized (this) {
                result = m_resourcesToIndex;
                m_resourcesToIndex = new ArrayList<CmsPublishedResource>();
            }
            try {
                CmsObject cms = m_adminCms;
                CmsProject offline = getOfflineIndexProject();
                if (offline != null) {
                    // switch to the offline project if available
                    cms = OpenCms.initCmsObject(m_adminCms);
                    cms.getRequestContext().setCurrentProject(offline);
                }
                addAdditionallyAffectedResources(cms, result);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return result;
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
                OpenCms.addCmsEventListener(
                    this,
                    new int[] {
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
        }

        /**
         * Updates all offline indexes for the given list of {@link CmsResource} objects.<p>
         *
         * @param resources a list of {@link CmsResource} objects to update in the offline indexes
         */
        protected synchronized void reIndexResources(List<CmsResource> resources) {

            List<CmsPublishedResource> resourcesToIndex = new ArrayList<CmsPublishedResource>(resources.size());
            for (CmsResource res : resources) {
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

        /** The event handler that triggers this thread. */
        CmsSearchOfflineHandler m_handler;

        /** Indicates if this thread is still alive. */
        boolean m_isAlive;

        /** Indicates that an index update thread is currently running. */
        private boolean m_isUpdating;

        /** If true a manual update (after file upload) was triggered. */
        private boolean m_updateTriggered;

        /** The wait handle used for signalling when the worker thread has finished. */
        private CmsWaitHandle m_waitHandle = new CmsWaitHandle();

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
         * Gets the wait handle used for signalling when the worker thread has finished.
         *
         * @return the wait handle
         **/
        public CmsWaitHandle getWaitHandle() {

            return m_waitHandle;
        }

        /**
         * @see java.lang.Thread#interrupt()
         */
        @Override
        public void interrupt() {

            super.interrupt();
            m_updateTriggered = true;
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            // create a log report for the output
            I_CmsReport report = new CmsLogReport(m_adminCms.getRequestContext().getLocale(), CmsSearchManager.class);
            long offlineUpdateFrequency = getOfflineUpdateFrequency();
            m_updateTriggered = false;
            try {
                while (m_isAlive) {
                    if (!m_updateTriggered) {
                        try {
                            sleep(offlineUpdateFrequency);
                        } catch (InterruptedException e) {
                            // continue the thread after interruption
                            if (!m_isAlive) {
                                // the thread has been shut down while sleeping
                                continue;
                            }
                            if (offlineUpdateFrequency != getOfflineUpdateFrequency()) {
                                // offline update frequency change - clear interrupt status
                                offlineUpdateFrequency = getOfflineUpdateFrequency();
                            }
                            LOG.info(e.getLocalizedMessage(), e);
                        }
                    }
                    if (m_isAlive) {
                        // set update trigger to false since we do the update now
                        m_updateTriggered = false;
                        // get list of resource to update
                        List<CmsPublishedResource> resourcesToIndex = getResourcesToIndex();
                        if (resourcesToIndex.size() > 0) {
                            // only start indexing if there is at least one resource
                            startOfflineUpdateThread(report, resourcesToIndex);
                        } else {
                            getWaitHandle().release();
                        }
                        // this is just called to clear the interrupt status of the thread
                        interrupted();
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

            // Reverse to always keep the last list entries
            Collections.reverse(resourcesToIndex);
            for (CmsPublishedResource pubRes : resourcesToIndex) {
                boolean addResource = true;
                for (CmsPublishedResource resRes : result) {
                    if (pubRes.equals(resRes)
                        && (pubRes.getState() == resRes.getState())
                        && (pubRes.getMovedState() == resRes.getMovedState())
                        && pubRes.getRootPath().equals(resRes.getRootPath())) {
                        // resource already in the update list
                        addResource = false;
                        break;
                    }
                }
                if (addResource) {
                    result.add(pubRes);
                }

            }
            Collections.reverse(result);
            return changeStateOfMoveOriginsToDeleted(result);
        }

        /**
         * Shuts down this offline index thread.<p>
         */
        protected void shutDown() {

            m_isAlive = false;
            interrupt();
            if (m_isUpdating) {
                long waitTime = getOfflineUpdateFrequency() / 2;
                int waitSteps = 0;
                do {
                    try {
                        // wait half the time of the offline index frequency for the thread to finish
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        // continue
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                    waitSteps++;
                    // wait 5 times then stop waiting
                } while ((waitSteps < 5) && m_isUpdating);
            }
        }

        /**
         * Updates the offline search indexes for the given list of resources.<p>
         *
         * @param report the report to write the index information to
         * @param resourcesToIndex the list of {@link CmsPublishedResource} objects to index
         */
        protected void startOfflineUpdateThread(I_CmsReport report, List<CmsPublishedResource> resourcesToIndex) {

            CmsSearchOfflineIndexWorkThread thread = new CmsSearchOfflineIndexWorkThread(report, resourcesToIndex);
            long startTime = System.currentTimeMillis();
            long waitTime = getOfflineUpdateFrequency() / 2;
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_OI_UPDATE_START_1,
                        Integer.valueOf(resourcesToIndex.size())));
            }

            m_isUpdating = true;
            thread.start();

            do {
                try {
                    // wait half the time of the offline index frequency for the thread to finish
                    thread.join(waitTime);
                } catch (InterruptedException e) {
                    // continue
                    LOG.info(e.getLocalizedMessage(), e);
                }
                if (thread.isAlive()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_OI_UPDATE_LONG_2,
                            Integer.valueOf(resourcesToIndex.size()),
                            Long.valueOf(System.currentTimeMillis() - startTime)));
                }
            } while (thread.isAlive());
            m_isUpdating = false;

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_OI_UPDATE_FINISH_2,
                        Integer.valueOf(resourcesToIndex.size()),
                        Long.valueOf(System.currentTimeMillis() - startTime)));
            }
        }

        /**
         * Helper method which changes the states of resources which are to be indexed but have the wrong path to 'deleted'.
         * This is needed to deal with moved resources, since the documents with the old paths must be removed from the index,
         *
         * @param resourcesToIndex the resources to index
         *
         * @return the resources to index, but resource states are set to 'deleted' for resources with outdated paths
         */
        private List<CmsPublishedResource> changeStateOfMoveOriginsToDeleted(
            List<CmsPublishedResource> resourcesToIndex) {

            Map<CmsUUID, String> lastValidPaths = new HashMap<CmsUUID, String>();
            for (CmsPublishedResource resource : resourcesToIndex) {
                if (resource.getState().isDeleted()) {
                    // we don't want the last path to be from a deleted resource
                    continue;
                }
                lastValidPaths.put(resource.getStructureId(), resource.getRootPath());
            }
            List<CmsPublishedResource> result = new ArrayList<CmsPublishedResource>();
            for (CmsPublishedResource resource : resourcesToIndex) {
                if (resource.getState().isDeleted()) {
                    result.add(resource);
                    continue;
                }
                String lastValidPath = lastValidPaths.get(resource.getStructureId());
                if (resource.getRootPath().equals(lastValidPath) || resource.getStructureId().isNullUUID()) {
                    result.add(resource);
                } else {
                    result.add(
                        new CmsPublishedResource(
                            resource.getStructureId(),
                            resource.getResourceId(),
                            resource.getPublishTag(),
                            resource.getRootPath(),
                            resource.getType(),
                            resource.isFolder(),
                            CmsResource.STATE_DELETED, // make sure index entry with outdated path is deleted
                            resource.getSiblingCount()));
                }
            }
            return result;
        }
    }

    /**
     * An offline index worker Thread runs each time for every offline index update action.<p>
     *
     * This was decoupled from the main {@link CmsSearchOfflineIndexThread} in order to avoid
     * problems if a single operation "hangs" the Tread.<p>
     */
    protected class CmsSearchOfflineIndexWorkThread extends Thread {

        /** The report to write the index information to. */
        I_CmsReport m_report;

        /** The list of {@link CmsPublishedResource} objects to index. */
        List<CmsPublishedResource> m_resourcesToIndex;

        /**
         * Updates the offline search indexes for the given list of resources.<p>
         *
         * @param report the report to write the index information to
         * @param resourcesToIndex the list of {@link CmsPublishedResource} objects to index
         */
        protected CmsSearchOfflineIndexWorkThread(I_CmsReport report, List<CmsPublishedResource> resourcesToIndex) {

            super("OpenCms: Offline Search Index Worker");
            m_report = report;
            m_resourcesToIndex = resourcesToIndex;
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            updateIndexOffline(m_report, m_resourcesToIndex);
            if (m_offlineIndexThread != null) {
                m_offlineIndexThread.getWaitHandle().release();
            }
        }
    }

    /** This needs to be a fair lock to preserve order of threads accessing the search manager. */
    private static final ReentrantLock SEARCH_MANAGER_LOCK = new ReentrantLock(true);

    /** The default value used for generating search result excerpts (1024 chars). */
    public static final int DEFAULT_EXCERPT_LENGTH = 1024;

    /** The default value used for keeping the extraction results in the cache (672 hours = 4 weeks). */
    public static final float DEFAULT_EXTRACTION_CACHE_MAX_AGE = 672.0f;

    /** Default for the maximum number of modifications before a commit in the search index is triggered (500). */
    public static final int DEFAULT_MAX_MODIFICATIONS_BEFORE_COMMIT = 500;

    /** The default update frequency for offline indexes (15000 msec = 15 sec). */
    public static final int DEFAULT_OFFLINE_UPDATE_FREQNENCY = 15000;

    /** The default maximal wait time for re-indexing after editing a content. */
    public static final int DEFAULT_MAX_INDEX_WAITTIME = 30000;

    /** The default timeout value used for generating a document for the search index (60000 msec = 1 min). */
    public static final int DEFAULT_TIMEOUT = 60000;

    /** Scheduler parameter: Update only a specified list of indexes. */
    public static final String JOB_PARAM_INDEXLIST = "indexList";

    /** Scheduler parameter: Write the output of the update to the logfile. */
    public static final String JOB_PARAM_WRITELOG = "writeLog";

    /** Prefix for Lucene default analyzers package (<code>org.apache.lucene.analysis.</code>). */
    public static final String LUCENE_ANALYZER = "org.apache.lucene.analysis.core.";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsSearchManager.class);

    /** List of resource types which represent groups of elements. */
    private static final String[] groupTypes = {
        CmsResourceTypeXmlContainerPage.MODEL_GROUP_TYPE_NAME,
        CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
        CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME};

    /** The administrator OpenCms user context to access OpenCms VFS resources. */
    protected CmsObject m_adminCms;

    /** The list of indexes that are configured for offline index mode. */
    protected List<I_CmsSearchIndex> m_offlineIndexes;

    /** The thread used of offline indexing. */
    protected CmsSearchOfflineIndexThread m_offlineIndexThread;

    /** Configured analyzers for languages using &lt;analyzer&gt;. */
    private HashMap<Locale, CmsSearchAnalyzer> m_analyzers;

    /** Stores the offline update frequency while indexing is paused. */
    private long m_configuredOfflineIndexingFrequency;

    /** The Solr core container. */
    private CoreContainer m_coreContainer;

    /** A map of document factory configurations. */
    private List<CmsSearchDocumentType> m_documentTypeConfigs;

    /** A map of document factories keyed first by their name and then by their extraction keys. */
    private Map<String, Map<String, I_CmsDocumentFactory>> m_documentTypes;

    /** The set of all globally available extraction keys for document factories. */
    private Set<String> m_extractionKeys;

    /** The max age for extraction results to remain in the cache. */
    private float m_extractionCacheMaxAge;

    /** The cache for the extraction results. */
    private CmsExtractionResultCache m_extractionResultCache;

    /** Contains the available field configurations. */
    private Map<String, I_CmsSearchFieldConfiguration> m_fieldConfigurations;

    /** The force unlock type. */
    private CmsSearchForceUnlockMode m_forceUnlockMode;

    /** The class used to highlight the search terms in the excerpt of a search result. */
    private I_CmsTermHighlighter m_highlighter;

    /** A list of search indexes. */
    private List<I_CmsSearchIndex> m_indexes;

    /** Seconds to wait for an index lock. */
    private int m_indexLockMaxWaitSeconds = 10;

    /** Configured index sources. */
    private Map<String, CmsSearchIndexSource> m_indexSources;

    /** The max. char. length of the excerpt in the search result. */
    private int m_maxExcerptLength;

    /** The maximum number of modifications before a commit in the search index is triggered. */
    private int m_maxModificationsBeforeCommit;

    /** The offline index search handler. */
    private CmsSearchOfflineHandler m_offlineHandler;

    /** The update frequency of the offline indexer in milliseconds. */
    private long m_offlineUpdateFrequency;

    /** The maximal time to wait for re-indexing after a content is edited (in milliseconds). */
    private long m_maxIndexWaitTime;

    /** Path to index files below WEB-INF/. */
    private String m_path;

    /** The Solr configuration. */
    private CmsSolrConfiguration m_solrConfig;

    /** Timeout for abandoning indexing thread. */
    private long m_timeout;

    /** Offline indexing pause requests */
    private final Set<CmsUUID> m_pauseRequests = new HashSet<>();

    /**
     * Default constructor when called as cron job.<p>
     */
    public CmsSearchManager() {

        m_documentTypes = new HashMap<String, Map<String, I_CmsDocumentFactory>>();
        m_extractionKeys = new HashSet<String>();
        m_documentTypeConfigs = new ArrayList<CmsSearchDocumentType>();
        m_analyzers = new HashMap<Locale, CmsSearchAnalyzer>();
        m_indexes = new ArrayList<I_CmsSearchIndex>();
        m_indexSources = new TreeMap<String, CmsSearchIndexSource>();
        m_offlineHandler = new CmsSearchOfflineHandler();
        m_extractionCacheMaxAge = DEFAULT_EXTRACTION_CACHE_MAX_AGE;
        m_maxExcerptLength = DEFAULT_EXCERPT_LENGTH;
        m_offlineUpdateFrequency = DEFAULT_OFFLINE_UPDATE_FREQNENCY;
        m_maxIndexWaitTime = DEFAULT_MAX_INDEX_WAITTIME;
        m_maxModificationsBeforeCommit = DEFAULT_MAX_MODIFICATIONS_BEFORE_COMMIT;

        m_fieldConfigurations = new HashMap<String, I_CmsSearchFieldConfiguration>();
        // make sure we have a "standard" field configuration
        addFieldConfiguration(CmsLuceneFieldConfiguration.DEFAULT_STANDARD);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_START_SEARCH_CONFIG_0));
        }
    }

    /**
     * Returns an analyzer for the given class name.<p>
     *
     * @param className the class name of the analyzer
     *
     * @return the appropriate lucene analyzer
     *
     * @throws Exception if something goes wrong
     */
    public static Analyzer getAnalyzer(String className) throws Exception {

        Analyzer analyzer = null;
        Class<?> analyzerClass;
        try {
            analyzerClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // allow Lucene standard classes to be written in a short form
            analyzerClass = Class.forName(LUCENE_ANALYZER + className);
        }

        // since Lucene 3.0 most analyzers need a "version" parameter and don't support an empty constructor
        if (StandardAnalyzer.class.equals(analyzerClass)) {
            // the Lucene standard analyzer is used - but without any stopwords.
            analyzer = new StandardAnalyzer(new CharArraySet(0, false));
        } else {
            analyzer = (Analyzer)analyzerClass.newInstance();
        }
        return analyzer;
    }

    /**
     * Returns the Solr index configured with the parameters name.
     * The parameters must contain a key/value pair with an existing
     * Solr index, otherwise <code>null</code> is returned.<p>
     *
     * @param cms the current context
     * @param params the parameter map
     *
     * @return the best matching Solr index
     */
    public static final CmsSolrIndex getIndexSolr(CmsObject cms, Map<String, String[]> params) {

        String indexName = null;
        CmsSolrIndex index = null;
        // try to get the index name from the parameters: 'core' or 'index'
        if (params != null) {
            indexName = params.get(OpenCmsSolrHandler.PARAM_CORE) != null
            ? params.get(OpenCmsSolrHandler.PARAM_CORE)[0]
            : (params.get(OpenCmsSolrHandler.PARAM_INDEX) != null
            ? params.get(OpenCmsSolrHandler.PARAM_INDEX)[0]
            : null);
        }
        if (indexName == null) {
            // if no parameter is specified try to use the default online/offline indexes by context
            indexName = cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE;
        }
        // try to get the index
        index = OpenCms.getSearchManager().getIndexSolr(indexName);
        if (index == null) {
            // if there is exactly one index, a missing core / index parameter doesn't matter, since there is no choice.
            List<CmsSolrIndex> solrs = OpenCms.getSearchManager().getAllSolrIndexes();
            if ((solrs != null) && !solrs.isEmpty() && (solrs.size() == 1)) {
                index = solrs.get(0);
            }
        }
        return index;
    }

    /**
     * Returns <code>true</code> if the index for the given name is a Lucene index, <code>false</code> otherwise.<p>
     *
     * @param indexName the name of the index to check
     *
     * @return <code>true</code> if the index for the given name is a Lucene index
     */
    public static boolean isLuceneIndex(String indexName) {

        I_CmsSearchIndex i = OpenCms.getSearchManager().getIndex(indexName);
        return (i instanceof CmsSearchIndex) && (!(i instanceof CmsSolrIndex));
    }

    /**
     * Adds an analyzer.<p>
     *
     * @param analyzer an analyzer
     */
    public void addAnalyzer(CmsSearchAnalyzer analyzer) {

        m_analyzers.put(analyzer.getLocale(), analyzer);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
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
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
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
    public void addFieldConfiguration(I_CmsSearchFieldConfiguration fieldConfiguration) {

        m_fieldConfigurations.put(fieldConfiguration.getName(), fieldConfiguration);
    }

    /**
     * Adds a search index to the configuration.<p>
     *
     * @param searchIndex the search index to add
     */
    public void addSearchIndex(I_CmsSearchIndex searchIndex) {

        if (!searchIndex.isInitialized()) {
            if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
                try {
                    searchIndex.initialize();
                } catch (CmsException e) {
                    // should never happen
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        // name: not null or emtpy and unique
        String name = searchIndex.getName();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
        }
        if (m_indexSources.keySet().contains(name)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_SEARCHINDEX_CREATE_INVALID_NAME_1, name));
        }

        m_indexes.add(searchIndex);
        if (m_adminCms != null) {
            initOfflineIndexes();
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
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
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
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
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                        (String)event.getData().get(I_CmsEventListener.KEY_INDEX_NAMES))) {
                    indexNames = CmsStringUtil.splitAsList(
                        (String)event.getData().get(I_CmsEventListener.KEY_INDEX_NAMES),
                        ",",
                        true);
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
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_EVENT_REBUILD_SEARCHINDEX_1,
                                indexNames == null ? "" : CmsStringUtil.collectionAsString(indexNames, ",")),
                            e);
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
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_EVENT_PUBLISH_PROJECT_FINISHED_1,
                            publishHistoryId));
                }
                break;
            case I_CmsEventListener.EVENT_REINDEX_OFFLINE:
            case I_CmsEventListener.EVENT_REINDEX_ONLINE:
                boolean isOnline = I_CmsEventListener.EVENT_REINDEX_ONLINE == event.getType();
                Map<String, Object> eventData = event.getData();
                CmsUUID userId = (CmsUUID)eventData.get(I_CmsEventListener.KEY_USER_ID);
                CmsUser user = null;
                if (userId != null) {
                    try {
                        user = m_adminCms.readUser(userId);
                    } catch (Throwable t) {
                        // should not normally happen
                        LOG.debug(t.getMessage(), t);
                    }
                }
                try {
                    SEARCH_MANAGER_LOCK.lock();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_REINDEX_STARTED_0));
                    }
                    CmsObject cms = m_adminCms;
                    if (!isOnline) {
                        OpenCms.initCmsObject(m_adminCms);
                        cms.getRequestContext().setCurrentProject(
                            cms.readProject((CmsUUID)eventData.get(I_CmsEventListener.KEY_PROJECTID)));
                    }
                    @SuppressWarnings("unchecked")
                    List<CmsResource> resources = (List<CmsResource>)eventData.get(I_CmsEventListener.KEY_RESOURCES);
                    I_CmsReport report = (I_CmsReport)eventData.get(I_CmsEventListener.KEY_REPORT);
                    List<CmsResource> resourcesToIndex = new ArrayList<>();
                    for (CmsResource res : resources) {
                        if (res.isFile()) {
                            resourcesToIndex.add(res);
                        } else {
                            try {
                                resourcesToIndex.addAll(
                                    cms.readResources(res, CmsResourceFilter.IGNORE_EXPIRATION, true));
                            } catch (CmsException e) {
                                LOG.error(e, e);
                            }
                        }
                    }
                    // we reindex and prevent using cached results
                    cleanExtractionCache();
                    List<CmsPublishedResource> publishedResourcesToIndex = resourcesToIndex.stream().map(
                        res -> new CmsPublishedResource(res)).collect(Collectors.toList());
                    if (Boolean.TRUE.equals(eventData.get(I_CmsEventListener.KEY_REINDEX_RELATED))) {
                        addAdditionallyAffectedResources(cms, publishedResourcesToIndex);
                    }
                    if (isOnline) {
                        updateAllIndexes(
                            m_adminCms,
                            publishedResourcesToIndex,
                            new CmsShellLogReport(CmsLocaleManager.MASTER_LOCALE));
                    } else {
                        updateIndexOffline(report, publishedResourcesToIndex);
                    }
                    cms = null;
                    SEARCH_MANAGER_LOCK.unlock();
                    if (null != user) {
                        Locale l = OpenCms.getWorkplaceManager().getWorkplaceLocale(user);
                        OpenCms.getSessionManager().sendBroadcast(
                            null,
                            Messages.get().getBundle(l).key(Messages.GUI_REINDEXING_SUCCESS_0),
                            user,
                            ContentMode.html);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_REINDEX_FINISHED_0));
                    }

                } catch (Throwable e) {
                    if (SEARCH_MANAGER_LOCK.isHeldByCurrentThread()) {
                        SEARCH_MANAGER_LOCK.unlock();
                    }
                    if (null != user) {
                        Locale l = OpenCms.getWorkplaceManager().getWorkplaceLocale(user);
                        OpenCms.getSessionManager().sendBroadcast(
                            null,
                            Messages.get().getBundle(l).key(Messages.GUI_REINDEXING_FAILED_0),
                            user,
                            ContentMode.html);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.ERR_EVENT_REINDEX_FAILED_1, event.getData()),
                            e);
                    } else if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_EVENT_REINDEX_FAILED_1, event.getData()));
                    }
                }
                break;
            default:
                // no operation
        }
    }

    /**
     * Returns all Solr index.<p>
     *
     * @return all Solr indexes
     */
    public List<CmsSolrIndex> getAllSolrIndexes() {

        List<CmsSolrIndex> result = new ArrayList<CmsSolrIndex>();
        for (String indexName : getIndexNames()) {
            CmsSolrIndex index = getIndexSolr(indexName);
            if (index != null) {
                result.add(index);
            }
        }
        return result;
    }

    /**
     * Returns an analyzer for the given language.<p>
     *
     * The analyzer is selected according to the analyzer configuration.<p>
     *
     * @param locale the locale to get the analyzer for
     * @return the appropriate lucene analyzer
     *
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
            analyzer = getAnalyzer(analyzerConf.getClassName());
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
     * Returns the configured Solr home directory <code>null</code> if not set.<p>
     *
     * @return the Solr home directory
     */
    public String getDirectorySolr() {

        return m_solrConfig != null ? m_solrConfig.getHome() : null;
    }

    /**
     * Returns the document factory configured under the provided name.
     * @param docTypeName the name of the document type.
     * @return the factory for the provided name.
     */
    public I_CmsDocumentFactory getDocumentFactoryForName(String docTypeName) {

        Map<String, I_CmsDocumentFactory> factoryMap = m_documentTypes.get(docTypeName);
        if (factoryMap != null) {
            Iterator<I_CmsDocumentFactory> factoryIt = factoryMap.values().iterator();
            if (factoryIt.hasNext()) {
                return factoryMap.values().iterator().next();
            }
        }
        return null;
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
     * Returns the document type keys used to specify the correct document factory.
     *
     * @see #getDocumentTypeKeys(String, String) for detailed information on the returned keys.
     *
     * @param resource the resource to generate the list of document type keys for.
     * @return the document type keys.
     */
    public List<String> getDocumentTypeKeys(CmsResource resource) {

        // first get the MIME type of the resource
        String mimeType = OpenCms.getResourceManager().getMimeType(resource.getRootPath(), null, "unknown");
        String resourceType = null;
        try {
            resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName();
        } catch (CmsLoaderException e) {
            // ignore, unknown resource type, resource can not be indexed
            LOG.info(e.getLocalizedMessage(), e);
        }
        return getDocumentTypeKeys(resourceType, mimeType);
    }

    /**
     * Returns the document type keys used to specify the correct document factory.
     * One resource typically has more than one key. The document factories are matched
     * in the provided order and the first matching factory is used.
     *
     * The keys for type name "typename" and mimetype "mimetype" would be a subset of:
     * <ul>
     *  <li><code>typename_mimetype</code></li>
     *  <li><code>typename</code></li>
     *  <li>if <code>typename</code> is a sub-type of <code>containerpage</code>
     *      <ul>
     *          <li><code>containerpage_mimetype</code></li>
     *          <li><code>containerpage</code></li>
     *      </ul>
     *  </li>
     *  <li>if <code>typename</code> is a sub-type of <code>xmlcontent</code>
     *      <ul>
     *          <li><code>xmlcontent_mimetype</code></li>
     *          <li><code>xmlcontent</code></li>
     *      </ul>
     *  </li>
     *  <li><code>__unconfigured___mimetype</code></li>
     *  <li><code>__unconfigured__</code></li>
     *  <li><code>__all___mimetype</code></li>
     *  <li><code>__all__</code></li>
     * <ul>
     * Note that all keys except the "__all__"-keys are only added as long as globally
     * there is no matching factory for the key.
     * This in particular means that a factory matching "typename" will never be used
     * if you have a factory for "typename__mimetype" - even if this is not configured
     * for the used index source. Eventually, the content will not be indexed in such cases.
     * @param resourceType the resource type to generate the list of document type keys for.
     * @param mimeType the mime type to generate the list of document type keys for.
     * @return the document type keys.
     */
    public List<String> getDocumentTypeKeys(String resourceType, String mimeType) {

        List<String> result = new ArrayList<>(8);
        if (null != resourceType) {
            String currentKey = A_CmsVfsDocument.getDocumentKey(resourceType, mimeType);
            result.add(currentKey);
            if (!m_extractionKeys.contains(currentKey)) {
                currentKey = A_CmsVfsDocument.getDocumentKey(resourceType, null);
                result.add(currentKey);
                if (!m_extractionKeys.contains(currentKey)) {
                    boolean hasGlobalMatch = false;
                    try {
                        String containerpageTypeName = CmsResourceTypeXmlContainerPage.getStaticTypeName();
                        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resourceType);
                        if (!resourceType.equals(containerpageTypeName)) {
                            if (type instanceof CmsResourceTypeXmlContainerPage) {
                                if (!resourceType.equals(CmsResourceTypeXmlContainerPage.getStaticTypeName())) {
                                    currentKey = A_CmsVfsDocument.getDocumentKey(containerpageTypeName, mimeType);
                                    result.add(currentKey);
                                    hasGlobalMatch = m_extractionKeys.contains(currentKey);
                                    if (!hasGlobalMatch) {
                                        currentKey = A_CmsVfsDocument.getDocumentKey(containerpageTypeName, null);
                                        result.add(currentKey);
                                        hasGlobalMatch = m_extractionKeys.contains(currentKey);
                                    }
                                }
                            }
                        }
                        String xmlcontentTypeName = CmsResourceTypeXmlContent.getStaticTypeName();
                        if (!resourceType.equals(containerpageTypeName)) {
                            if (!hasGlobalMatch && (type instanceof CmsResourceTypeXmlContent)) {
                                currentKey = A_CmsVfsDocument.getDocumentKey(xmlcontentTypeName, mimeType);
                                result.add(currentKey);
                                hasGlobalMatch = m_extractionKeys.contains(currentKey);
                                if (!hasGlobalMatch) {
                                    currentKey = A_CmsVfsDocument.getDocumentKey(xmlcontentTypeName, null);
                                    result.add(currentKey);
                                    hasGlobalMatch = m_extractionKeys.contains(currentKey);
                                }
                            }
                        }
                    } catch (Throwable t) {
                        LOG.warn("Could not read type for name \"" + resourceType + "\".", t);
                    }
                    if (!hasGlobalMatch) {
                        result.add(
                            A_CmsVfsDocument.getDocumentKey(A_CmsVfsDocument.DEFAULT_ALL_UNCONFIGURED_TYPES, mimeType));
                        result.add(
                            A_CmsVfsDocument.getDocumentKey(A_CmsVfsDocument.DEFAULT_ALL_UNCONFIGURED_TYPES, null));
                    }
                }
            }
            result.add(A_CmsVfsDocument.getDocumentKey(A_CmsVfsDocument.DEFAULT_ALL_TYPES, mimeType));
            result.add(A_CmsVfsDocument.getDocumentKey(A_CmsVfsDocument.DEFAULT_ALL_TYPES, null));
        }
        return result;

    }

    /**
     * Returns the map from document type keys to document factories with all entries for the provided document type names.
     * @param documentTypeNames list of document type names to generate the map for.
     * @return the map from document type keys to document factories.
     */
    public Map<String, I_CmsDocumentFactory> getDocumentTypeMapForTypeNames(List<String> documentTypeNames) {

        Map<String, I_CmsDocumentFactory> result = new LinkedHashMap<>();
        if (null != documentTypeNames) {
            // Iterate the list in reverse order to prefer factories that are added by document types listed earlier.
            ListIterator<String> typesIterator = documentTypeNames.listIterator(documentTypeNames.size());
            while (typesIterator.hasPrevious()) {
                Map<String, I_CmsDocumentFactory> factories = m_documentTypes.get(typesIterator.previous());
                if (null != factories) {
                    result.putAll(factories);
                }
            }
        }
        return result;
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
    public I_CmsSearchFieldConfiguration getFieldConfiguration(String name) {

        return m_fieldConfigurations.get(name);
    }

    /**
     * Returns the unmodifieable List of configured {@link I_CmsSearchFieldConfiguration} entries.<p>
     *
     * @return the unmodifieable List of configured {@link I_CmsSearchFieldConfiguration} entries
     */
    public List<I_CmsSearchFieldConfiguration> getFieldConfigurations() {

        List<I_CmsSearchFieldConfiguration> result = new ArrayList<I_CmsSearchFieldConfiguration>(
            m_fieldConfigurations.values());
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the Lucene search field configurations only.<p>
     *
     * @return the Lucene search field configurations
     */
    public List<CmsLuceneFieldConfiguration> getFieldConfigurationsLucene() {

        List<CmsLuceneFieldConfiguration> result = new ArrayList<CmsLuceneFieldConfiguration>();
        for (I_CmsSearchFieldConfiguration conf : m_fieldConfigurations.values()) {
            if (conf instanceof CmsLuceneFieldConfiguration) {
                result.add((CmsLuceneFieldConfiguration)conf);
            }
        }
        Collections.sort(result);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the Solr search field configurations only.<p>
     *
     * @return the Solr search field configurations
     */
    public List<CmsSolrFieldConfiguration> getFieldConfigurationsSolr() {

        List<CmsSolrFieldConfiguration> result = new ArrayList<CmsSolrFieldConfiguration>();
        for (I_CmsSearchFieldConfiguration conf : m_fieldConfigurations.values()) {
            if (conf instanceof CmsSolrFieldConfiguration) {
                result.add((CmsSolrFieldConfiguration)conf);
            }
        }
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
     * Returns the Lucene search index configured with the given name.<p>
     * The index must exist, otherwise <code>null</code> is returned.
     *
     * @param indexName then name of the requested search index
     *
     * @return the Lucene search index configured with the given name
     */
    public I_CmsSearchIndex getIndex(String indexName) {

        for (I_CmsSearchIndex index : m_indexes) {
            if (indexName.equalsIgnoreCase(index.getName())) {
                return index;
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
     * Returns the Solr index configured with the given name.<p>
     * The index must exist, otherwise <code>null</code> is returned.
     *
     * @param indexName then name of the requested Solr index
     * @return the Solr index configured with the given name
     */
    public CmsSolrIndex getIndexSolr(String indexName) {

        I_CmsSearchIndex index = getIndex(indexName);
        if (index instanceof CmsSolrIndex) {
            return (CmsSolrIndex)index;
        }
        return null;
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
     * Returns the maximal time to wait for re-indexing after a content is edited (in milliseconds).<p>
     *
     * @return the maximal time to wait for re-indexing after a content is edited (in milliseconds)
     */
    public long getMaxIndexWaitTime() {

        return m_maxIndexWaitTime;
    }

    /**
     * Returns the maximum number of modifications before a commit in the search index is triggered.<p>
     *
     * @return the maximum number of modifications before a commit in the search index is triggered
     */
    public int getMaxModificationsBeforeCommit() {

        return m_maxModificationsBeforeCommit;
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
     * Returns an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances.<p>
     *
     * @return an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances
     */
    public List<I_CmsSearchIndex> getSearchIndexes() {

        return Collections.unmodifiableList(m_indexes);
    }

    /**
     * Returns an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances.<p>
     *
     * @return an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances
     */
    public List<I_CmsSearchIndex> getSearchIndexesAll() {

        return Collections.unmodifiableList(m_indexes);
    }

    /**
     * Returns an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances.<p>
     *
     * @return an unmodifiable list of all configured <code>{@link I_CmsSearchIndex}</code> instances
     */
    public List<CmsSolrIndex> getSearchIndexesSolr() {

        List<CmsSolrIndex> indexes = new ArrayList<CmsSolrIndex>();
        for (I_CmsSearchIndex index : m_indexes) {
            if (index instanceof CmsSolrIndex) {
                indexes.add((CmsSolrIndex)index);
            }
        }
        return Collections.unmodifiableList(indexes);
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
     * Return singleton instance of the OpenCms spellchecker.<p>
     *
     * @return instance of CmsSolrSpellchecker.
     */
    public CmsSolrSpellchecker getSolrDictionary() {

        // get the core container that contains one core for each configured index
        if (m_coreContainer == null) {
            m_coreContainer = createCoreContainer();
        }
        return CmsSolrSpellchecker.getInstance(m_coreContainer);
    }

    /**
     * Returns the Solr configuration.<p>
     *
     * @return the Solr configuration
     */
    public CmsSolrConfiguration getSolrServerConfiguration() {

        return m_solrConfig;
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
            LOG.error(e.getLocalizedMessage(), e);
        }
        // make sure the site root is the root site
        m_adminCms.getRequestContext().setSiteRoot("/");

        // create the extraction result cache
        m_extractionResultCache = new CmsExtractionResultCache(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(getDirectory()),
            "/extractCache");
        initializeFieldConfigurations();
        initializeIndexes();
        initOfflineIndexes();

        // register this object as event listener
        OpenCms.addCmsEventListener(
            this,
            new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_REBUILD_SEARCHINDEXES,
                I_CmsEventListener.EVENT_REINDEX_OFFLINE,
                I_CmsEventListener.EVENT_REINDEX_ONLINE});
    }

    /**
     * Calls {@link I_CmsSearchFieldConfiguration#init()} for all registered field configurations.
     */
    public void initializeFieldConfigurations() {

        for (I_CmsSearchFieldConfiguration config : m_fieldConfigurations.values()) {
            config.init();
        }

    }

    /**
     * Initializes all configured document types, index sources and search indexes.<p>
     *
     * This method needs to be called if after a change in the index configuration has been made.
     */
    public void initializeIndexes() {

        initAvailableDocumentTypes();
        initIndexSources();
        initSearchIndexes();
    }

    /**
     * Initialize the offline index handler, require after an offline index has been added.<p>
     */
    public void initOfflineIndexes() {

        // check which indexes are configured as offline indexes
        List<I_CmsSearchIndex> offlineIndexes = new ArrayList<I_CmsSearchIndex>();
        Iterator<I_CmsSearchIndex> i = m_indexes.iterator();
        while (i.hasNext()) {
            I_CmsSearchIndex index = i.next();
            if (I_CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode())) {
                // this is an offline index
                offlineIndexes.add(index);
            }
        }
        m_offlineIndexes = offlineIndexes;
        m_offlineHandler.initialize();

    }

    /**
     * Initializes the spell check index.<p>
     *
     * @param adminCms the ROOT_ADMIN cms context
     */
    public void initSpellcheckIndex(CmsObject adminCms) {

        if (CmsSpellcheckDictionaryIndexer.updatingIndexNecessesary(adminCms)) {
            final CmsSolrSpellchecker spellchecker = OpenCms.getSearchManager().getSolrDictionary();
            if (spellchecker != null) {

                Runnable initRunner = new Runnable() {

                    public void run() {

                        try {
                            spellchecker.parseAndAddDictionaries(adminCms);
                        } catch (CmsRoleViolationException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                };
                new Thread(initRunner).start();
            }
        }
    }

    /**
     * Returns if the offline indexing is paused.<p>
     *
     * @return <code>true</code> if the offline indexing is paused
     */
    public boolean isOfflineIndexingPaused() {

        return m_offlineUpdateFrequency == Long.MAX_VALUE;
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
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        CmsSearchManager manager = OpenCms.getSearchManager();

        I_CmsReport report = null;
        boolean writeLog = Boolean.valueOf(parameters.get(JOB_PARAM_WRITELOG)).booleanValue();

        if (writeLog) {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsSearchManager.class);
        }

        List<String> updateList = null;
        String indexList = parameters.get(JOB_PARAM_INDEXLIST);
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
     * Pauses the offline indexing and returns a pause request id that has to be used for resuming offline indexing again.<p>
     * May take some time, because the indexes are updated first.<p>
     *
     *@return the pause request id. The id has to be given to the {@link #resumeOfflineIndexing(CmsUUID)} method to resume offline indexing.
     */
    public CmsUUID pauseOfflineIndexing() {

        CmsUUID pauseId = new CmsUUID();
        synchronized (m_pauseRequests) {
            if (m_pauseRequests.isEmpty()) {
                LOG.info("Pausing offline indexing.");
                m_configuredOfflineIndexingFrequency = m_offlineUpdateFrequency;
                m_offlineUpdateFrequency = Long.MAX_VALUE;
                updateOfflineIndexes(0);
            }
            m_pauseRequests.add(pauseId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added pause request with id " + pauseId);
            }
        }
        return pauseId;
    }

    /**
     * Rebuilds (if required creates) all configured indexes.<p>
     *
     * @param report the report object to write messages (or <code>null</code>)
     *
     * @throws CmsException if something goes wrong
     */
    public void rebuildAllIndexes(I_CmsReport report) throws CmsException {

        try {
            SEARCH_MANAGER_LOCK.lock();

            CmsMessageContainer container = null;
            for (int i = 0, n = m_indexes.size(); i < n; i++) {
                // iterate all configured search indexes
                I_CmsSearchIndex searchIndex = m_indexes.get(i);
                try {
                    // update the index
                    updateIndex(searchIndex, report, null);
                } catch (CmsException e) {
                    container = new CmsMessageContainer(
                        Messages.get(),
                        Messages.ERR_INDEX_REBUILD_ALL_1,
                        new Object[] {searchIndex.getName()});
                    LOG.error(
                        Messages.get().getBundle().key(Messages.ERR_INDEX_REBUILD_ALL_1, searchIndex.getName()),
                        e);
                }
            }
            // clean up the extraction result cache
            cleanExtractionCache();
            if (container != null) {
                // throw stored exception
                throw new CmsSearchException(container);
            }
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
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
    public void rebuildIndex(String indexName, I_CmsReport report) throws CmsException {

        try {
            SEARCH_MANAGER_LOCK.lock();
            // get the search index by name
            I_CmsSearchIndex index = getIndex(indexName);
            // update the index
            updateIndex(index, report, null);
            // clean up the extraction result cache
            cleanExtractionCache();
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
        }
    }

    /**
     * Rebuilds (if required creates) the List of indexes with the given name.<p>
     *
     * @param indexNames the names (String) of the index to rebuild
     * @param report the report object to write messages (or <code>null</code>)
     *
     * @throws CmsException if something goes wrong
     */
    public void rebuildIndexes(List<String> indexNames, I_CmsReport report) throws CmsException {

        try {
            SEARCH_MANAGER_LOCK.lock();
            Iterator<String> i = indexNames.iterator();
            while (i.hasNext()) {
                String indexName = i.next();
                // get the search index by name
                I_CmsSearchIndex index = getIndex(indexName);
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
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
        }
    }

    /**
     * Registers a new Solr core for the given index.<p>
     *
     * @param index the index to register a new Solr core for
     *
     * @throws CmsConfigurationException if no Solr server is configured
     */
    @SuppressWarnings("resource")
    public void registerSolrIndex(CmsSolrIndex index) throws CmsConfigurationException {

        if ((m_solrConfig == null) || !m_solrConfig.isEnabled()) {
            // No solr server configured
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_SOLR_NOT_ENABLED_0));
        }

        if (index.getServerUrl() != null) { // Use the index-specific Solr-Server if present.
            index.setSolrServer(new Builder().withBaseSolrUrl(index.getServerUrl()).build());
        } else if (m_solrConfig.getServerUrl() != null) { // Use the globally configured external Solr-Server if present.
            // HTTP Server configured
            // TODO Implement multi core support for HTTP server
            // @see http://lucidworks.lucidimagination.com/display/solr/Configuring+solr.xml
            index.setSolrServer(new Builder().withBaseSolrUrl(m_solrConfig.getServerUrl()).build());
        } else { // Default to the embedded Solr Server

            // get the core container that contains one core for each configured index
            if (m_coreContainer == null) {
                m_coreContainer = createCoreContainer();
            }

            // unload the existing core if it exists to avoid problems with forced unlock.
            if (m_coreContainer.getAllCoreNames().contains(index.getCoreName())) {
                m_coreContainer.unload(index.getCoreName(), false, false, true);
            }
            // ensure that all locks on the index are gone
            ensureIndexIsUnlocked(index.getPath());

            // load the core to the container
            File dataDir = new File(index.getPath());
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_SOLR_INDEX_DIR_CREATED_2,
                            index.getName(),
                            index.getPath()));
                }
            }
            File instanceDir = new File(
                m_solrConfig.getHome() + FileSystems.getDefault().getSeparator() + index.getName());
            if (!instanceDir.exists()) {
                instanceDir.mkdirs();
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_SOLR_INDEX_DIR_CREATED_2,
                            index.getName(),
                            index.getPath()));
                }
            }

            // create the core
            // TODO: suboptimal - forces always the same schema
            SolrCore core = null;
            try {
                // creation includes registration.
                // TODO: this was the old code: core = m_coreContainer.create(descriptor, false);
                Map<String, String> properties = new HashMap<String, String>(3);
                properties.put(CoreDescriptor.CORE_DATADIR, dataDir.getAbsolutePath());
                properties.put(CoreDescriptor.CORE_CONFIGSET, "default");
                core = m_coreContainer.create(index.getCoreName(), instanceDir.toPath(), properties, false);
            } catch (NullPointerException e) {
                if (core != null) {
                    core.close();
                }
                throw new CmsConfigurationException(
                    Messages.get().container(
                        Messages.ERR_SOLR_SERVER_NOT_CREATED_3,
                        index.getName() + " (" + index.getCoreName() + ")",
                        index.getPath(),
                        m_solrConfig.getSolrConfigFile().getAbsolutePath()),
                    e);
            }

            if (index.isNoSolrServerSet()) {
                index.setSolrServer(new EmbeddedSolrServer(m_coreContainer, index.getCoreName()));
            }
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_SOLR_SERVER_CREATED_1,
                        index.getName() + " (" + index.getCoreName() + ")"));
            }
        }
    }

    /**
     * Removes this field configuration from the OpenCms configuration (if it is not used any more).<p>
     *
     * @param fieldConfiguration the field configuration to remove from the configuration
     *
     * @return true if remove was successful, false if preconditions for removal are ok but the given
     *         field configuration was unknown to the manager.
     *
     * @throws CmsIllegalStateException if the given field configuration is still used by at least one
     *         <code>{@link I_CmsSearchIndex}</code>.
     *
     */
    public boolean removeSearchFieldConfiguration(I_CmsSearchFieldConfiguration fieldConfiguration)
    throws CmsIllegalStateException {

        // never remove the standard field configuration
        if (fieldConfiguration.getName().equals(CmsSearchFieldConfiguration.STR_STANDARD)) {
            throw new CmsIllegalStateException(
                Messages.get().container(
                    Messages.ERR_INDEX_CONFIGURATION_DELETE_STANDARD_1,
                    fieldConfiguration.getName()));
        }
        // validation if removal will be granted
        Iterator<I_CmsSearchIndex> itIndexes = m_indexes.iterator();
        I_CmsSearchIndex idx;
        // the list for collecting indexes that use the given field configuration
        List<I_CmsSearchIndex> referrers = new ArrayList<I_CmsSearchIndex>();
        I_CmsSearchFieldConfiguration refFieldConfig;
        while (itIndexes.hasNext()) {
            idx = itIndexes.next();
            refFieldConfig = idx.getFieldConfiguration();
            if (refFieldConfig.equals(fieldConfiguration)) {
                referrers.add(idx);
            }
        }
        if (referrers.size() > 0) {
            throw new CmsIllegalStateException(
                Messages.get().container(
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
     */
    public boolean removeSearchFieldConfigurationField(
        I_CmsSearchFieldConfiguration fieldConfiguration,
        CmsSearchField field) {

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
                    Messages.LOG_REMOVE_FIELDCONFIGURATION_FIELD_INDEX_2,
                    field.getName(),
                    fieldConfiguration.getName()));
        }

        return fieldConfiguration.getFields().remove(field);
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
    public boolean removeSearchFieldMapping(CmsLuceneField field, CmsSearchFieldMapping mapping)
    throws CmsIllegalStateException {

        if (field.getMappings().size() < 2) {
            throw new CmsIllegalStateException(
                Messages.get().container(
                    Messages.ERR_FIELD_MAPPING_DELETE_2,
                    mapping.getType().toString(),
                    field.getName()));
        } else {

            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
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
    public void removeSearchIndex(I_CmsSearchIndex searchIndex) {

        // shut down index to remove potential config files of Solr indexes
        searchIndex.shutDown();
        if (searchIndex instanceof CmsSolrIndex) {
            CmsSolrIndex solrIndex = (CmsSolrIndex)searchIndex;
            m_coreContainer.unload(solrIndex.getCoreName(), true, true, true);
        }
        m_indexes.remove(searchIndex);
        initOfflineIndexes();

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(
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
            I_CmsSearchIndex index = getIndex(indexName);
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
     *         <code>{@link I_CmsSearchIndex}</code>.
     *
     */
    public boolean removeSearchIndexSource(CmsSearchIndexSource indexsource) throws CmsIllegalStateException {

        // validation if removal will be granted
        Iterator<I_CmsSearchIndex> itIndexes = m_indexes.iterator();
        I_CmsSearchIndex idx;
        // the list for collecting indexes that use the given index source
        List<I_CmsSearchIndex> referrers = new ArrayList<I_CmsSearchIndex>();
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
            throw new CmsIllegalStateException(
                Messages.get().container(
                    Messages.ERR_INDEX_SOURCE_DELETE_2,
                    indexsource.getName(),
                    referrers.toString()));
        }

        // remove operation (no exception)
        return m_indexSources.remove(indexsource.getName()) != null;

    }

    /**
     * Resumes offline indexing if it was paused and no pause for another pauseId is still present.<p>
     * @param pauseId the id of the pause request, which now allows for resuming.
     */
    public void resumeOfflineIndexing(CmsUUID pauseId) {

        synchronized (m_pauseRequests) {
            if (!m_pauseRequests.contains(pauseId)) {
                try {
                    throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    LOG.warn("Cannot resume for pause request " + pauseId + ". The request id is unknown.", e);
                }
            } else {
                m_pauseRequests.remove(pauseId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        "Removed pause request "
                            + pauseId
                            + " from pause requests. Remaining pauses are: "
                            + m_pauseRequests);
                }
                if (m_pauseRequests.isEmpty()) {
                    LOG.info("Resuming offline indexing.");
                    setOfflineUpdateFrequency(
                        m_configuredOfflineIndexingFrequency > 0
                        ? m_configuredOfflineIndexingFrequency
                        : DEFAULT_OFFLINE_UPDATE_FREQNENCY);
                }
            }
        }
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
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_EXTRACTION_CACHE_AGE_FAILED_2,
                    extractionCacheMaxAge,
                    Float.valueOf(DEFAULT_EXTRACTION_CACHE_MAX_AGE)),
                e);
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
        } catch (Exception e) {
            m_highlighter = null;
            LOG.error(e.getLocalizedMessage(), e);
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
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_EXCERPT_LENGTH_FAILED_2,
                    maxExcerptLength,
                    Integer.valueOf(DEFAULT_EXCERPT_LENGTH)),
                e);
            setMaxExcerptLength(DEFAULT_EXCERPT_LENGTH);
        }
    }

    /**
     * Sets the maximal wait time for offline index updates after edit operations.<p>
     *
     * @param maxIndexWaitTime  the maximal wait time to set in milliseconds
     */
    public void setMaxIndexWaitTime(long maxIndexWaitTime) {

        m_maxIndexWaitTime = maxIndexWaitTime;
    }

    /**
     * Sets the maximal wait time for offline index updates after edit operations.<p>
     *
     * @param maxIndexWaitTime the maximal wait time to set in milliseconds
     */
    public void setMaxIndexWaitTime(String maxIndexWaitTime) {

        try {
            setMaxIndexWaitTime(Long.parseLong(maxIndexWaitTime));
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_MAX_INDEX_WAITTIME_FAILED_2,
                    maxIndexWaitTime,
                    Long.valueOf(DEFAULT_MAX_INDEX_WAITTIME)),
                e);
            setMaxIndexWaitTime(DEFAULT_MAX_INDEX_WAITTIME);
        }
    }

    /**
     * Sets the maximum number of modifications before a commit in the search index is triggered.<p>
     *
     * @param maxModificationsBeforeCommit the maximum number of modifications to set
     */
    public void setMaxModificationsBeforeCommit(int maxModificationsBeforeCommit) {

        m_maxModificationsBeforeCommit = maxModificationsBeforeCommit;
    }

    /**
     * Sets the maximum number of modifications before a commit in the search index is triggered as a string.<p>
     *
     * @param value the maximum number of modifications to set
     */
    public void setMaxModificationsBeforeCommit(String value) {

        try {
            setMaxModificationsBeforeCommit(Integer.parseInt(value));
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_MAXCOMMIT_FAILED_2,
                    value,
                    Integer.valueOf(DEFAULT_MAX_MODIFICATIONS_BEFORE_COMMIT)),
                e);
            setMaxModificationsBeforeCommit(DEFAULT_MAX_MODIFICATIONS_BEFORE_COMMIT);
        }
    }

    /**
     * Sets the update frequency of the offline indexer in milliseconds.<p>
     *
     * @param offlineUpdateFrequency the update frequency in milliseconds to set
     */
    public void setOfflineUpdateFrequency(long offlineUpdateFrequency) {

        m_offlineUpdateFrequency = offlineUpdateFrequency;
        updateOfflineIndexes(0);
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
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_OFFLINE_UPDATE_FAILED_2,
                    offlineUpdateFrequency,
                    Long.valueOf(DEFAULT_OFFLINE_UPDATE_FREQNENCY)),
                e);
            setOfflineUpdateFrequency(DEFAULT_OFFLINE_UPDATE_FREQNENCY);
        }
    }

    /**
     * Sets the Solr configuration.<p>
     *
     * @param config the Solr configuration
     */
    public void setSolrServerConfiguration(CmsSolrConfiguration config) {

        m_solrConfig = config;
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
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_PARSE_TIMEOUT_FAILED_2,
                    value,
                    Long.valueOf(DEFAULT_TIMEOUT)),
                e);
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

        if (m_offlineHandler != null) {
            OpenCms.removeCmsEventListener(m_offlineHandler);
        }

        Iterator<I_CmsSearchIndex> i = m_indexes.iterator();
        while (i.hasNext()) {
            I_CmsSearchIndex index = i.next();
            index.shutDown();
            index = null;
        }
        m_indexes.clear();

        shutDownSolrContainer();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_MANAGER_0));
        }
    }

    /**
     * Updates all offline indexes.<p>
     *
     * Can be used to force an index update when it's not convenient to wait until the
     * offline update interval has eclipsed.<p>
     *
     * Since the offline indexes still need some time to update the new resources,
     * the method waits for at most the configurable <code>maxIndexWaitTime</code>
     * to ensure that updating is finished.
     *
     * @see #updateOfflineIndexes(long)
     *
     */
    public void updateOfflineIndexes() {

        updateOfflineIndexes(getMaxIndexWaitTime());
    }

    /**
     * Updates all offline indexes.<p>
     *
     * Can be used to force an index update when it's not convenient to wait until the
     * offline update interval has eclipsed.<p>
     *
     * Since the offline index will still need some time to update the new resources even if it runs directly,
     * a wait time of 2500 or so should be given in order to make sure the index finished updating.
     *
     * @param waitTime milliseconds to wait after the offline update index was notified of the changes
     */
    public void updateOfflineIndexes(long waitTime) {

        if ((m_offlineIndexThread != null) && m_offlineIndexThread.isAlive()) {
            // notify existing thread of update frequency change
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_OI_UPDATE_INTERRUPT_0));
            }
            m_offlineIndexThread.interrupt();
            if (waitTime > 0) {
                m_offlineIndexThread.getWaitHandle().enter(waitTime);
            }
        }
    }

    /**
     * Collects the resources whose indexed document depends on one of the updated resources.<p>
     * We take transitive dependencies into account and handle cyclic dependencies correctly as well.
     *
     * @param adminCms an OpenCms user context with Admin permissions
     * @param updateResources the resources to be re-indexed
     *
     * @return the updated list of resource to re-index
     */
    protected List<CmsPublishedResource> addAdditionallyAffectedResources(
        CmsObject adminCms,
        List<CmsPublishedResource> updateResources) {

        if (updateResources.size() > 0) {
            Set<CmsPublishedResource> updateResourceSet = new HashSet<>(updateResources);
            Collection<CmsPublishedResource> resourcesToCheck = updateResourceSet;
            Collection<CmsPublishedResource> additionalResources = Collections.emptySet();
            do {
                additionalResources = findRelatedContainerPages(adminCms, updateResourceSet, resourcesToCheck);
                additionalResources.addAll(
                    addIndexContentRelatedResources(adminCms, updateResourceSet, resourcesToCheck));
                updateResources.addAll(additionalResources);
                updateResourceSet.addAll(additionalResources);
                resourcesToCheck = additionalResources;
            } while (resourcesToCheck.size() > 0);
        }
        return updateResources;
    }

    /**
     * Collects the resources whose indexed document depends on one of the updated resources.<p>
     *
     * @param adminCms an OpenCms user context with Admin permissions
     * @param updateResources the resources to be re-indexed
     * @param updateResourcesToCheck the resources to check additionally affected resources for, subset of updateResources
     *
     * @return the list of resources that need to be additionally re-index
     */
    protected Collection<CmsPublishedResource> addIndexContentRelatedResources(
        CmsObject adminCms,
        Collection<CmsPublishedResource> updateResources,
        Collection<CmsPublishedResource> updateResourcesToCheck) {

        Collection<CmsPublishedResource> additionalUpdateResources = new HashSet<>();
        for (CmsPublishedResource checkedRes : updateResourcesToCheck) {
            try {
                CmsRelationFilter filter = CmsRelationFilter.relationsToStructureId(checkedRes.getStructureId());
                filter = filter.filterType(CmsRelationType.INDEX_CONTENT);
                List<CmsRelation> relations = adminCms.readRelations(filter);
                for (CmsRelation relation : relations) {
                    CmsResource res = relation.getSource(adminCms, CmsResourceFilter.ALL);
                    CmsPublishedResource additionalPubRes = new CmsPublishedResource(res);
                    if (!updateResources.contains(additionalPubRes)) {
                        additionalUpdateResources.add(additionalPubRes);
                    }
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return additionalUpdateResources;
    }

    /**
     * Cleans up the extraction result cache.<p>
     */
    protected void cleanExtractionCache() {

        // clean up the extraction result cache
        m_extractionResultCache.cleanCache(m_extractionCacheMaxAge);
    }

    /**
     * Collects the related containerpages to the resources that have been published.<p>
     *
     * @param adminCms an OpenCms user context with Admin permissions
     * @param updateResources the resources to be re-indexed
     * @param updateResourcesToCheck the resources to check additionally affected resources for, subset of updateResources
     *
     * @return the list of resources that need to be additionally re-index
     */
    protected Collection<CmsPublishedResource> findRelatedContainerPages(
        CmsObject adminCms,
        Collection<CmsPublishedResource> updateResources,
        Collection<CmsPublishedResource> updateResourcesToCheck) {

        CmsResourceManager resMan = OpenCms.getResourceManager();
        Collection<CmsPublishedResource> additionalUpdateResources = new HashSet<>();

        Set<CmsResource> containerPages = new HashSet<CmsResource>();
        int containerPageTypeId = -1;
        try {
            containerPageTypeId = CmsResourceTypeXmlContainerPage.getContainerPageTypeId();
        } catch (CmsLoaderException e) {
            // will happen during setup, when container page type is not available yet
            LOG.info(e.getLocalizedMessage(), e);
        }
        if (containerPageTypeId != -1) {
            for (CmsPublishedResource pubRes : updateResourcesToCheck) {
                try {
                    if (resMan.getResourceType(pubRes.getType()) instanceof CmsResourceTypeXmlContent) {
                        if (!isGroup(pubRes.getType())) {
                            CmsRelationFilter filter = CmsRelationFilter.relationsToStructureId(
                                pubRes.getStructureId()).filterStrong();
                            List<CmsRelation> relations = adminCms.readRelations(filter);
                            for (CmsRelation relation : relations) {
                                CmsResource res = relation.getSource(adminCms, CmsResourceFilter.ALL);
                                if (CmsResourceTypeXmlContainerPage.isContainerPage(res)) {
                                    containerPages.add(res);
                                    if (CmsDetailOnlyContainerUtil.isDetailContainersPage(
                                        adminCms,
                                        adminCms.getSitePath(res))) {
                                        addDetailContent(adminCms, containerPages, adminCms.getSitePath(res));
                                    }
                                }
                            }
                        }
                    }
                    if (containerPageTypeId == pubRes.getType()) {
                        addDetailContent(
                            adminCms,
                            containerPages,
                            adminCms.getRequestContext().removeSiteRoot(pubRes.getRootPath()));
                    }
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            // add all found container pages as published resource objects to the list
            for (CmsResource page : containerPages) {
                CmsPublishedResource pubCont = new CmsPublishedResource(page);
                if (!updateResources.contains(pubCont)) {
                    // ensure container page is added only once
                    additionalUpdateResources.add(pubCont);
                }
            }
        }
        return additionalUpdateResources;
    }

    /**
     * Returns the set of names of all configured document types.<p>
     *
     * @return the set of names of all configured document types
     */
    protected List<String> getDocumentTypes() {

        return Collections.unmodifiableList(new ArrayList<String>(m_documentTypes.keySet()));
    }

    /**
     * Returns the a offline project used for offline indexing.<p>
     *
     * @return the offline project if available
     */
    protected CmsProject getOfflineIndexProject() {

        CmsProject result = null;
        for (I_CmsSearchIndex index : m_offlineIndexes) {
            try {
                result = m_adminCms.readProject(index.getProject());

                if (!result.isOnlineProject()) {
                    break;
                }
            } catch (Exception e) {
                // may be a missconfigured index, ignore
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Returns a new thread manager for the indexing threads.<p>
     *
     * @return a new thread manager for the indexing threads
     */
    protected CmsIndexingThreadManager getThreadManager() {

        return new CmsIndexingThreadManager(m_timeout, m_maxModificationsBeforeCommit);
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

        m_documentTypes = new LinkedHashMap<String, Map<String, I_CmsDocumentFactory>>();

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

                Map<String, I_CmsDocumentFactory> matchingTypes = new HashMap<>();
                for (Iterator<String> keyIt = documentFactory.getDocumentKeys(
                    resourceTypes,
                    mimeTypes).iterator(); keyIt.hasNext();) {
                    String key = keyIt.next();
                    matchingTypes.put(key, documentFactory);
                    m_extractionKeys.add(key);
                }
                m_documentTypes.put(name, matchingTypes);

            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_DOCTYPE_CONFIG_FAILED_1, name), e);
                }
            }
        }
    }

    /**
     * Initializes the index sources.
     */
    protected void initIndexSources() {

        for (CmsSearchIndexSource source : m_indexSources.values()) {
            source.init();
        }
    }

    /**
     * Initializes the configured search indexes.<p>
     *
     * This initializes also the list of Cms resources types
     * to be indexed by an index source.<p>
     */
    protected void initSearchIndexes() {

        I_CmsSearchIndex index = null;
        for (int i = 0, n = m_indexes.size(); i < n; i++) {
            index = m_indexes.get(i);
            // reset disabled flag
            index.setEnabled(true);
            // check if the index has been configured correctly
            if (index.checkConfiguration(m_adminCms)) {
                // the index is configured correctly
                try {
                    index.initialize();
                } catch (Exception e) {
                    if (CmsLog.INIT.isWarnEnabled()) {
                        // in this case the index will be disabled
                        CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_SEARCH_INIT_FAILED_1, index), e);
                    }
                }
            }
            // output a log message if the index was successfully configured or not
            if (CmsLog.INIT.isInfoEnabled()) {
                if (index.isEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.INIT_INDEX_CONFIGURED_2, index, index.getProject()));
                } else {
                    CmsLog.INIT.warn(
                        Messages.get().getBundle().key(
                            Messages.INIT_INDEX_NOT_CONFIGURED_2,
                            index,
                            index.getProject()));
                }
            }
        }
    }

    /**
     * Checks, if the index should be rebuilt/updated at all by the search manager.
     * @param index the index to check.
     * @return a flag, indicating if the index should be rebuilt/updated at all.
     */
    protected boolean shouldUpdateAtAll(I_CmsSearchIndex index) {

        if (I_CmsSearchIndex.REBUILD_MODE_NEVER.equals(index.getRebuildMode())) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SKIP_REBUILD_FOR_MODE_NEVER_1, index.getName()));
            return false;
        } else {
            return true;
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
    protected void updateAllIndexes(CmsObject adminCms, CmsUUID publishHistoryId, I_CmsReport report) {

        int oldPriority = Thread.currentThread().getPriority();
        try {
            SEARCH_MANAGER_LOCK.lock();
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            List<CmsPublishedResource> publishedResources;
            try {
                // read the list of all published resources
                publishedResources = adminCms.readPublishedResources(publishHistoryId);
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_READING_CHANGED_RESOURCES_FAILED_1, publishHistoryId),
                    e);
                return;
            }
            Set<CmsUUID> bothNewAndDeleted = getIdsOfPublishResourcesWhichAreBothNewAndDeleted(publishedResources);
            // When published resources with both states 'new' and 'deleted' exist in the same publish job history, the resource has been moved

            List<CmsPublishedResource> updateResources = new ArrayList<CmsPublishedResource>();
            for (CmsPublishedResource res : publishedResources) {
                if (res.getState().isUnchanged()) {
                    // unchanged resources don't need to be indexed after publish
                    continue;
                }
                if (res.getState().isDeleted() || res.getState().isNew() || res.getState().isChanged()) {
                    if (updateResources.contains(res)) {
                        // resource may have been added as a sibling of another resource
                        // in this case we make sure to use the value from the publish list because of the "deleted" flag
                        boolean hasMoved = bothNewAndDeleted.contains(res.getStructureId())
                            || (res.getMovedState() == CmsPublishedResource.STATE_MOVED_DESTINATION)
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
                                    LOG.warn(
                                        Messages.get().getBundle().key(
                                            Messages.LOG_UNABLE_TO_READ_SIBLINGS_1,
                                            res.getRootPath()),
                                        e);
                                }
                            }
                        }
                    }
                }
            }

            addAdditionallyAffectedResources(adminCms, updateResources);
            updateAllIndexes(adminCms, updateResources, report);
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
            Thread.currentThread().setPriority(oldPriority);
        }
    }

    /**
     * Incrementally updates all indexes that have their rebuild mode set to <code>"auto"</code>.<p>
     *
     * @param adminCms an OpenCms user context with Admin permissions
     * @param updateResources the resources to update
     * @param report the report to write the output to
     */
    protected void updateAllIndexes(
        CmsObject adminCms,
        List<CmsPublishedResource> updateResources,
        I_CmsReport report) {

        try {
            SEARCH_MANAGER_LOCK.lock();
            if (!updateResources.isEmpty()) {
                // sort the resource to update
                Collections.sort(updateResources);
                // only update the indexes if the list of remaining published resources is not empty
                Iterator<I_CmsSearchIndex> i = m_indexes.iterator();
                while (i.hasNext()) {
                    I_CmsSearchIndex index = i.next();
                    if (I_CmsSearchIndex.REBUILD_MODE_AUTO.equals(index.getRebuildMode())) {
                        // only update indexes which have the rebuild mode set to "auto"
                        try {
                            updateIndex(index, report, updateResources);
                        } catch (CmsException e) {
                            LOG.error(
                                Messages.get().getBundle().key(Messages.LOG_UPDATE_INDEX_FAILED_1, index.getName()),
                                e);
                        }
                    }
                }
            }
            // clean up the extraction result cache
            cleanExtractionCache();
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
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
    protected void updateIndex(I_CmsSearchIndex index, I_CmsReport report, List<CmsPublishedResource> resourcesToIndex)
    throws CmsException {

        if (shouldUpdateAtAll(index)) {
            try {
                SEARCH_MANAGER_LOCK.lock();

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

                    updateIndexCompletely(cms, index, report);
                } else {
                    updateIndexIncremental(cms, index, report, resourcesToIndex);
                }
            } finally {
                SEARCH_MANAGER_LOCK.unlock();
            }
        }
    }

    /**
     * The method updates all OpenCms documents that are indexed.
     * @param cms the OpenCms user context to use for accessing the VFS
     * @param index the index to update
     * @param report the report to write output messages to
     * @throws CmsIndexException thrown if indexing fails for some reason
     */
    @SuppressWarnings("null")
    protected void updateIndexCompletely(CmsObject cms, I_CmsSearchIndex index, I_CmsReport report)
    throws CmsIndexException {

        // create a new thread manager for the indexing threads
        CmsIndexingThreadManager threadManager = getThreadManager();

        boolean isOfflineIndex = false;
        if (I_CmsSearchIndex.REBUILD_MODE_OFFLINE.equals(index.getRebuildMode())) {
            // disable offline indexing while the complete index is rebuild
            isOfflineIndex = true;
            index.setRebuildMode(I_CmsSearchIndex.REBUILD_MODE_MANUAL);
            // re-initialize the offline indexes, this will disable this offline index
            initOfflineIndexes();
        }

        I_CmsIndexWriter writer = null;
        try {
            // create a backup of the existing index
            CmsSearchIndex indexInternal = null;
            String backup = null;
            if (index instanceof CmsSearchIndex) {
                indexInternal = (CmsSearchIndex)index;
                backup = indexInternal.createIndexBackup();
                if (backup != null) {
                    indexInternal.indexSearcherOpen(backup);
                }
            }

            // create a new index writer
            writer = index.getIndexWriter(report, true);
            if (writer instanceof I_CmsSolrIndexWriter) {
                try {
                    ((I_CmsSolrIndexWriter)writer).deleteAllDocuments();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

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
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // just continue with the loop after interruption
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                }

                // commit and optimize the index after each index source has been finished
                try {
                    writer.commit();
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_IO_INDEX_WRITER_COMMIT_2,
                                index.getName(),
                                index.getPath()),
                            e);
                    }
                }
                try {
                    writer.optimize();
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_IO_INDEX_WRITER_OPTIMIZE_2,
                                index.getName(),
                                index.getPath()),
                            e);
                    }
                }
            }

            // we are sure here that indexInternal is not null
            if (backup != null) {
                // remove the backup after the files have been re-indexed
                indexInternal.indexSearcherClose();
                indexInternal.removeIndexBackup(backup);
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
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_IO_INDEX_WRITER_CLOSE_2,
                                index.getPath(),
                                index.getName()),
                            e);
                    }
                }
            }
            if (isOfflineIndex) {
                // reset the mode of the offline index
                index.setRebuildMode(I_CmsSearchIndex.REBUILD_MODE_OFFLINE);
                // re-initialize the offline indexes, this will re-enable this index
                initOfflineIndexes();
            }
            // index has changed - initialize the index searcher instance
            index.onIndexChanged(true);
        }

        // show information about indexing runtime
        threadManager.reportStatistics(report);
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
        I_CmsSearchIndex index,
        I_CmsReport report,
        List<CmsPublishedResource> resourcesToIndex)
    throws CmsException {

        try {
            SEARCH_MANAGER_LOCK.lock();

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

                I_CmsIndexWriter writer = null;
                try {
                    // obtain an index writer that updates the current index
                    writer = index.getIndexWriter(report, false);

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
                        CmsIndexingThreadManager threadManager = getThreadManager();

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
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                // just continue with the loop after interruption
                                LOG.info(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                } finally {
                    // close the index writer
                    if (writer != null) {
                        try {
                            writer.commit();
                        } catch (IOException e) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_IO_INDEX_WRITER_COMMIT_2,
                                    index.getName(),
                                    index.getPath()),
                                e);
                        }
                    }
                    // index has changed - initialize the index searcher instance
                    index.onIndexChanged(false);
                }

                // output finish information on the report
                report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_UPDATE_END_1, index.getName()),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        } finally {
            SEARCH_MANAGER_LOCK.unlock();
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
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        Iterator<I_CmsSearchIndex> j = m_offlineIndexes.iterator();
        while (j.hasNext()) {
            I_CmsSearchIndex index = j.next();
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
     * Checks if the given containerpage is used as a detail containers and adds the related detail content to the resource set.<p>
     *
     * @param adminCms the cms context
     * @param containerPages the containerpages
     * @param containerPage the container page site path
     */
    private void addDetailContent(CmsObject adminCms, Set<CmsResource> containerPages, String containerPage) {

        if (CmsDetailOnlyContainerUtil.isDetailContainersPage(adminCms, containerPage)) {

            try {
                CmsResource detailRes = adminCms.readResource(
                    CmsDetailOnlyContainerUtil.getDetailContentPath(containerPage),
                    CmsResourceFilter.IGNORE_EXPIRATION);
                containerPages.add(detailRes);
            } catch (Throwable e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Creates the Solr core container.<p>
     *
     * @return the created core container
     */
    private CoreContainer createCoreContainer() {

        CoreContainer container = null;
        try {
            // get the core container
            // still no core container: create it
            container = CoreContainer.createAndLoad(
                Paths.get(m_solrConfig.getHome()),
                m_solrConfig.getSolrFile().toPath());
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_SOLR_CORE_CONTAINER_CREATED_2,
                        m_solrConfig.getHome(),
                        m_solrConfig.getSolrFile().getName()));
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_SOLR_CORE_CONTAINER_NOT_CREATED_1,
                    m_solrConfig.getSolrFile().getAbsolutePath()),
                e);
        }
        return container;

    }

    /**
     * Remove write.lock file in the data directory to ensure the index is unlocked.
     * @param dataDir the data directory of the Solr index that should be unlocked.
     */
    private void ensureIndexIsUnlocked(String dataDir) {

        Collection<File> lockFiles = new ArrayList<File>(2);
        lockFiles.add(
            new File(
                CmsFileUtil.addTrailingSeparator(CmsFileUtil.addTrailingSeparator(dataDir) + "index") + "write.lock"));
        lockFiles.add(
            new File(
                CmsFileUtil.addTrailingSeparator(CmsFileUtil.addTrailingSeparator(dataDir) + "spellcheck")
                    + "write.lock"));
        for (File lockFile : lockFiles) {
            if (lockFile.exists()) {
                lockFile.delete();
                LOG.warn(
                    "Forcely unlocking index with data dir \""
                        + dataDir
                        + "\" by removing file \""
                        + lockFile.getAbsolutePath()
                        + "\".");
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
     * Gets all structure ids for which published resources of both states 'new' and 'deleted' exist in the given list.<p>
     *
     * @param publishedResources a list of published resources
     *
     * @return the set of structure ids that satisfy the condition above
     */
    private Set<CmsUUID> getIdsOfPublishResourcesWhichAreBothNewAndDeleted(
        List<CmsPublishedResource> publishedResources) {

        Set<CmsUUID> result = new HashSet<CmsUUID>();
        Set<CmsUUID> deletedSet = new HashSet<CmsUUID>();
        for (CmsPublishedResource pubRes : publishedResources) {
            if (pubRes.getState().isNew()) {
                result.add(pubRes.getStructureId());
            }
            if (pubRes.getState().isDeleted()) {
                deletedSet.add(pubRes.getStructureId());
            }
        }
        result.retainAll(deletedSet);
        return result;
    }

    /**
     * Checks if the given type id belongs to a group type.
     *
     * @param type the type id to check
     * @return true if the type is a group type
     */
    private boolean isGroup(int type) {

        for (String groupType : groupTypes) {
            if (OpenCms.getResourceManager().matchResourceType(groupType, type)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Shuts down the Solr core container.<p>
     */
    private void shutDownSolrContainer() {

        if (m_coreContainer != null) {
            for (SolrCore core : m_coreContainer.getCores()) {
                // do not unload spellcheck core because otherwise the core.properties file is removed
                // even when calling m_coreContainer.unload(core.getName(), false, false, false);
                if (!core.getName().equals(CmsSolrSpellchecker.SPELLCHECKER_INDEX_CORE)) {
                    m_coreContainer.unload(core.getName(), false, false, true);
                }
            }
            m_coreContainer.shutdown();
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SOLR_SHUTDOWN_SUCCESS_0));
            }
            m_coreContainer = null;
        }
    }

}

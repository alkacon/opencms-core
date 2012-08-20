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
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchFieldConfiguration;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.store.FSDirectory;

/**
 * Abstract search index implementation.<p>
 */
public abstract class A_CmsSearchIndex implements I_CmsConfigurationParameterHandler {

    /** Constant for additional parameter to enable optimized full index regeneration (default: false). */
    public static final String BACKUP_REINDEXING = A_CmsSearchIndex.class.getName() + ".useBackupReindexing";

    /** Constant for additional parameter to enable excerpt creation (default: true). */
    public static final String EXCERPT = A_CmsSearchIndex.class.getName() + ".createExcerpt";

    /** Constant for additional parameter for index content extraction. */
    public static final String EXTRACT_CONTENT = A_CmsSearchIndex.class.getName() + ".extractContent";

    /** Constant for additional parameter for controlling how many hits are loaded at maximum (default: 1000). */
    public static final String MAX_HITS = A_CmsSearchIndex.class.getName() + ".maxHits";

    /** Indicates how many hits are loaded at maximum by default. */
    public static final int MAX_HITS_DEFAULT = 5000;

    /** Constant for additional parameter to enable permission checks (default: true). */
    public static final String PERMISSIONS = A_CmsSearchIndex.class.getName() + ".checkPermissions";

    /** Constant for additional parameter to set the thread priority during search. */
    public static final String PRIORITY = A_CmsSearchIndex.class.getName() + ".priority";

    /** Special value for the search.exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_ALL = "all";

    /** Automatic ("auto") index rebuild mode. */
    public static final String REBUILD_MODE_AUTO = "auto";

    /** Manual ("manual") index rebuild mode. */
    public static final String REBUILD_MODE_MANUAL = "manual";

    /** Offline ("offline") index rebuild mode. */
    public static final String REBUILD_MODE_OFFLINE = "offline";

    /** Constant for additional parameter to enable time range checks (default: true). */
    public static final String TIME_RANGE = A_CmsSearchIndex.class.getName() + ".checkTimeRange";

    /** The use all locale. */
    public static final String USE_ALL_LOCALE = "all";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsSearchIndex.class);

    /** Indicates if backup re-indexing is used by this index. */
    private boolean m_backupReindexing;

    /** The permission check mode for this index. */
    private boolean m_checkPermissions;

    /** Document types of folders/channels. */
    private Map<String, List<String>> m_documenttypes;

    /** An internal enabled flag, used to disable the index if for instance the configured project does not exist. */
    private boolean m_enabled;

    /** The content extraction mode for this index. */
    private boolean m_extractContent;

    /** The search field configuration of this index. */
    private I_CmsSearchFieldConfiguration m_fieldConfiguration;

    /** The name of the search field configuration used by this index. */
    private String m_fieldConfigurationName;

    /** The index writer to use. */
    private I_CmsIndexWriter m_indexWriter;

    /** The locale of this index. */
    private Locale m_locale;

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

    /** Controls if a resource requires view permission to be displayed in the result list. */
    private boolean m_requireViewPermission;

    /** The list of configured index source names. */
    private List<String> m_sourceNames;

    /** The list of configured index sources. */
    private List<CmsSearchIndexSource> m_sources;

    /**
     * Public default constructor.<p>
     */
    public A_CmsSearchIndex() {

        m_sourceNames = new ArrayList<String>();
        m_documenttypes = new HashMap<String, List<String>>();
        m_enabled = true;
        m_checkPermissions = true;
        m_extractContent = true;
        m_priority = -1;
    }

    /**
     * Public constructor.<p>
     * 
     * @param name the name for this index
     * 
     * @throws CmsIllegalArgumentException if something goes wrong
     */
    public A_CmsSearchIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        setName(name);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String key, String value) {

        if (PERMISSIONS.equals(key)) {
            m_checkPermissions = Boolean.valueOf(value).booleanValue();
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
        if ((obj instanceof A_CmsSearchIndex)) {
            return ((A_CmsSearchIndex)obj).getName().equals(m_name);
        }
        return false;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        if (getPriority() > 0) {
            result.put(PRIORITY, String.valueOf(m_priority));
        }
        if (!isExtractingContent()) {
            result.put(EXTRACT_CONTENT, String.valueOf(m_extractContent));
        }
        if (!isCheckingPermissions()) {
            result.put(PERMISSIONS, String.valueOf(m_checkPermissions));
        }
        if (isBackupReindexing()) {
            result.put(BACKUP_REINDEXING, String.valueOf(m_backupReindexing));
        }
        return result;
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
    public abstract I_CmsSearchDocument getDocument(String field, String term);

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

        if ((res != null) && (getSources() != null)) {
            // the result can only be null or the type configured for the resource
            I_CmsDocumentFactory result = OpenCms.getSearchManager().getDocumentFactory(res);
            if (result != null) {
                // check the path of the resource if it matches with one (or more) of the configured index sources
                Iterator<CmsSearchIndexSource> i = getSources().iterator();
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
    public I_CmsSearchFieldConfiguration getFieldConfiguration() {

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
     * @param report the report to write error messages on
     * @param create if <code>true</code> a whole new index is created, if <code>false</code> an existing index is updated
     * 
     * @return a new instance of IndexWriter
     * 
     * @throws CmsIndexException if the index can not be opened
     */
    public I_CmsIndexWriter getIndexWriter(I_CmsReport report, boolean create) throws CmsIndexException {

        // note - create will be: 
        //   true if the index is to be fully rebuild, 
        //   false if the index is to be incrementally updated
        if (m_indexWriter != null) {
            if (!create) {
                // re-use existing index writer
                return m_indexWriter;
            }
            // need to close the index writer if create is "true"
            try {
                m_indexWriter.close();
                m_indexWriter = null;
            } catch (IOException e) {
                // if we can't close the index we are busted!
                throw new CmsIndexException(Messages.get().container(
                    Messages.LOG_IO_INDEX_WRITER_CLOSE_2,
                    getPath(),
                    getName()), e);
            }

        }

        // now create is true of false, but the index writer is definitely null / closed
        I_CmsIndexWriter indexWriter = createIndexWriter(create, report);

        if (!create) {
            m_indexWriter = indexWriter;
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
     * Returns the language locale for the given resource in this index.<p>
     * 
     * @param cms the current OpenCms user context
     * @param resource the resource to check
     * @param availableLocales a list of locales supported by the resource
     * 
     * @return the language locale for the given resource in this index
     */
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

        if (m_path == null) {
            m_path = generateIndexDirectory();
        }
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
     * @throws CmsSearchException if the index source association failed or a configuration error occurred
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

        m_path = getPath();

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
            m_fieldConfigurationName = A_CmsSearchFieldConfiguration.STR_STANDARD;
        }
        m_fieldConfiguration = OpenCms.getSearchManager().getFieldConfiguration(m_fieldConfigurationName);
        if (m_fieldConfiguration == null) {
            // we must have a valid field configuration to continue
            throw new CmsSearchException(Messages.get().container(
                Messages.ERR_FIELD_CONFIGURATION_UNKNOWN_2,
                m_name,
                m_fieldConfigurationName));
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
     * Returns <code>true</code> if a resource requires read permission to be incuded in the result list.<p>
     * 
     * @return <code>true</code> if a resource requires read permission to be incuded in the result list
     */
    public boolean isRequireViewPermission() {

        return m_requireViewPermission;
    }

    /**
     * Returns <code>true</code> in case this index is updated incremental.<p>
     * 
     * An index is updated incremental if the index rebuild mode as defined by 
     * {@link #getRebuildMode()} is either set to {@value #REBUILD_MODE_AUTO} or 
     * {@value #REBUILD_MODE_OFFLINE}. Moreover, at least one update must have 
     * been written to the index already.
     * 
     * @return <code>true</code> in case this index is updated incremental
     */
    public boolean isUpdatedIncremental() {

        return m_indexWriter != null;
    }

    /**
     * Removes an index source from this search index.<p>
     * 
     * @param sourceName the index source name to remove
     */
    public void removeSourceName(String sourceName) {

        m_sources.remove(sourceName);
        m_sourceNames.remove(sourceName);
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
    public abstract CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) throws CmsSearchException;

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
    public void setFieldConfiguration(I_CmsSearchFieldConfiguration fieldConfiguration) {

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
     * @param project the name of the project used to index resources
     */
    public void setProject(String project) {

        m_project = project;
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
    public void shutDown() {

        // close the index writer
        if (m_indexWriter != null) {
            try {
                m_indexWriter.commit();
                m_indexWriter.close();
            } catch (IOException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()), e);
            }
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
        File file = new File(getPath());
        if (!file.exists()) {
            // index does not exist yet, so we can't backup it
            return null;
        }
        String backupPath = getPath() + "_backup";
        try {
            // open file directory for Lucene
            FSDirectory oldDir = FSDirectory.open(file);
            FSDirectory newDir = FSDirectory.open(new File(backupPath));
            for (String fileName : oldDir.listAll()) {
                oldDir.copy(newDir, fileName, fileName);
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_IO_INDEX_BACKUP_CREATE_3, getName(), getPath(), backupPath),
                e);
            backupPath = null;
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
    protected abstract I_CmsIndexWriter createIndexWriter(boolean create, I_CmsReport report) throws CmsIndexException;

    /**
     * Checks if the provided resource should be excluded from this search index.<p> 
     * 
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource to index
     * 
     * @return true if the resource should be excluded, false if it should be included in this index
     */
    protected boolean excludeFromIndex(CmsObject cms, CmsResource resource) {

        // check if this resource should be excluded from the index, if so skip it
        boolean excludeFromIndex = false;

        if (resource.isInternal()
            || resource.isFolder()
            || resource.isTemporaryFile()
            || (resource.getDateExpired() <= System.currentTimeMillis())) {
            // don't index internal resources, folders or temporary files or resources with expire date in the past
            return true;
        }

        try {
            // do property lookup with folder search
            String propValue = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE, true).getValue();
            excludeFromIndex = Boolean.valueOf(propValue).booleanValue();
            if (!excludeFromIndex && (propValue != null)) {
                // property value was neither "true" nor null, must check for "all"
                excludeFromIndex = PROPERTY_SEARCH_EXCLUDE_VALUE_ALL.equalsIgnoreCase(propValue.trim());
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNABLE_TO_READ_PROPERTY_1, resource.getRootPath()));
            }
        }
        if (!excludeFromIndex && !USE_ALL_LOCALE.equalsIgnoreCase(getLocale().getLanguage())) {
            // check if any resource default locale has a match with the index locale, if not skip resource
            List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
            Locale match = OpenCms.getLocaleManager().getFirstMatchingLocale(
                Collections.singletonList(getLocale()),
                locales);
            excludeFromIndex = (match == null);
        }

        return excludeFromIndex;
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
        String contextPath = cms.getRequestContext().removeSiteRoot(doc.getPath());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
        if (isRequireViewPermission()) {
            filter = CmsResourceFilter.DEFAULT_ONLY_VISIBLE;
        }
        try {
            return cms.readResource(contextPath, filter);
        } catch (CmsException e) {
            // Do nothing 
        }

        return null;
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
    protected abstract void indexSearcherClose();

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
    protected abstract void indexSearcherOpen(String path);

    /**
     * Reopens the index search reader for this index, required after the index has been changed.<p>
     * 
     * @see #indexSearcherOpen(String)
     */
    protected abstract void indexSearcherUpdate();

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
            FSDirectory dir = FSDirectory.open(file);
            dir.close();
            CmsFileUtil.purgeDirectory(file);
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_IO_INDEX_BACKUP_REMOVE_2, getName(), path), e);
        }
    }

    /**
     * Sets the index writer.<p>
     * 
     * @param writer the index writer to set
     */
    protected void setIndexWriter(I_CmsIndexWriter writer) {

        m_indexWriter = writer;
    }

}

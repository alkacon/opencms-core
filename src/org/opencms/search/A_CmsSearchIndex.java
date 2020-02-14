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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;

/**
 * Abstract base class for search indexes. It provides default implementations that should fit most use
 * cases when adding own index implementations.
 */
public abstract class A_CmsSearchIndex implements I_CmsSearchIndex {

    /** Special value for the search.exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_ALL = "all";

    /** Special value for the search.exclude property. */
    public static final String PROPERTY_SEARCH_EXCLUDE_VALUE_GALLERY = "gallery";

    /** The use all locale. */
    public static final String USE_ALL_LOCALE = "all";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsSearchIndex.class);

    /** The serial version id. */
    private static final long serialVersionUID = 5831386499514765251L;

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
    private transient I_CmsIndexWriter m_indexWriter;

    /** Signals whether the language detection. */
    private boolean m_languageDetection;

    /** The locale of this index. */
    private Locale m_locale;

    /** The name of this index. */
    private String m_name;

    /** The path where this index stores it's data in the "real" file system. */
    private String m_path;

    /** The project of this index. */
    private String m_project;

    /** The rebuild mode for this index. */
    private String m_rebuild;

    /** The list of configured index source names. */
    private List<String> m_sourceNames;

    /** The list of configured index sources. */
    private List<CmsSearchIndexSource> m_sources;

    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     *
     * It is recommended to use the constructor <code>{@link #A_CmsSearchIndex(String)}</code>
     * as it enforces the mandatory name argument. <p>
     */
    public A_CmsSearchIndex() {

        m_sourceNames = new ArrayList<String>();
        m_documenttypes = new HashMap<String, List<String>>();
        m_enabled = true;
        m_extractContent = true;
    }

    /**
     * Creates a new CmsSearchIndex with the given name.<p>
     *
     * @param name the system-wide unique name for the search index
     *
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index
     */
    public A_CmsSearchIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        setName(name);
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String key, String value) {

        // by default no parameters are excepted

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
     * @see org.opencms.search.I_CmsSearchIndex#checkConfiguration(org.opencms.file.CmsObject)
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
                    LOG.error(
                        Messages.get().getBundle().key(
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
        if ((null != obj) && this.getClass().getName().equals(obj.getClass().getName())) {
            return ((I_CmsSearchIndex)obj).getName().equals(m_name);
        }
        return false;
    }

    /**
     * Checks if the provided resource should be excluded from this search index.<p>
     *
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource to index
     *
     * @return true if the resource should be excluded, false if it should be included in this index
     */
    public boolean excludeFromIndex(CmsObject cms, CmsResource resource) {

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
            String propValue = cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE,
                true).getValue();
            excludeFromIndex = Boolean.valueOf(propValue).booleanValue();
            if (!excludeFromIndex && (propValue != null)) {
                // property value was neither "true" nor null, must check for "all"
                excludeFromIndex = PROPERTY_SEARCH_EXCLUDE_VALUE_ALL.equalsIgnoreCase(propValue.trim());
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_UNABLE_TO_READ_PROPERTY_1, resource.getRootPath()));
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
     * Returns the empty configuration.
     * Override the method if your index is configurable.
     *
     * @see org.opencms.search.I_CmsSearchIndex#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return new CmsParameterConfiguration();
    }

    /**
     * We always assume we have no unchanged copy of the content, since it depends on the concrete index.
     * Override the method to enhance indexing performance if you know where to grap the content from your index.
     * See the implementation {@link org.opencms.search.CmsSearchIndex#getContentIfUnchanged(CmsResource)} for an example.
     * @see org.opencms.search.I_CmsSearchIndex#getContentIfUnchanged(org.opencms.file.CmsResource)
     */
    public I_CmsExtractionResult getContentIfUnchanged(CmsResource resource) {

        return null;
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
     * @return the document type factory used for the given resource in this index, or <code>null</code>
     * in case the resource is not indexed by this index
     */
    @Override
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if ((res != null) && (getSources() != null)) {
            // the result can only be null or the type configured for the resource
            List<String> documentTypeKeys = OpenCms.getSearchManager().getDocumentTypeKeys(res);
            for (String documentTypeKey : documentTypeKeys) {
                for (CmsSearchIndexSource source : getSources()) {
                    if (source.isIndexing(res.getRootPath(), documentTypeKey)) {
                        // we found an index source that indexes the resource
                        return source.getDocumentFactory(documentTypeKey);
                    }
                }
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getFieldConfiguration()
     */
    public I_CmsSearchFieldConfiguration getFieldConfiguration() {

        return m_fieldConfiguration;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getFieldConfigurationName()
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
                throw new CmsIndexException(
                    Messages.get().container(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()),
                    e);
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

        Locale result = null;
        List<Locale> defaultLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
        if ((availableLocales != null) && (availableLocales.size() > 0)) {
            result = OpenCms.getLocaleManager().getBestMatchingLocale(
                defaultLocales.get(0),
                defaultLocales,
                availableLocales);
        }
        if (result == null) {
            result = ((availableLocales != null) && availableLocales.isEmpty())
            ? availableLocales.get(0)
            : defaultLocales.get(0);
        }
        return result;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getPath()
     */
    public String getPath() {

        return m_path;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getProject()
     */
    public String getProject() {

        return m_project;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getRebuildMode()
     */
    public String getRebuildMode() {

        return m_rebuild;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#getSourceNames()
     */
    public List<String> getSourceNames() {

        return m_sourceNames;
    }

    /**
    * @see org.opencms.search.I_CmsSearchIndex#getSources()
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

        // Do nothing by default

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
                throw new CmsSearchException(
                    Messages.get().container(Messages.ERR_INDEX_SOURCE_ASSOCIATION_1, sourceName),
                    e);
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
            throw new CmsSearchException(
                Messages.get().container(Messages.ERR_FIELD_CONFIGURATION_UNKNOWN_2, m_name, m_fieldConfigurationName));
        }

        // initialize the index searcher instance
        onIndexChanged(true);
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
    * @see org.opencms.search.I_CmsSearchIndex#isExtractingContent()
    */
    public boolean isExtractingContent() {

        return m_extractContent;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#isInitialized()
     */
    public boolean isInitialized() {

        return m_sources != null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#isLanguageDetection()
     */
    public boolean isLanguageDetection() {

        return m_languageDetection;
    }

    /**
     * Returns <code>true</code> in case this index is updated incremental.<p>
     *
     * An index is updated incremental if the index rebuild mode as defined by
     * {@link #getRebuildMode()} is either set to {@value I_CmsSearchIndex#REBUILD_MODE_AUTO} or
     * {@value I_CmsSearchIndex#REBUILD_MODE_OFFLINE}. Moreover, at least one update must have
     * been written to the index already.
     *
     * @return <code>true</code> in case this index is updated incremental
     */
    public boolean isUpdatedIncremental() {

        return m_indexWriter != null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#onIndexChanged(boolean)
     */
    public void onIndexChanged(boolean force) {

        // Do nothing by default.

    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#removeSourceName(String)
     */
    public void removeSourceName(String sourceName) {

        Iterator<CmsSearchIndexSource> it = m_sources.iterator();
        while (it.hasNext()) {
            if (Objects.equals(it.next().getName(), sourceName)) {
                it.remove();
            }
        }
        m_sourceNames.remove(sourceName);
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#setEnabled(boolean)
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
     * Sets the languageDetection.<p>
     *
     * @param languageDetection the languageDetection to set
     */
    public void setLanguageDetection(boolean languageDetection) {

        m_languageDetection = languageDetection;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#setLocaleString(java.lang.String)
     */
    public void setLocaleString(String locale) {

        setLocale(CmsLocaleManager.getLocale(locale));
    }

    /**
    * @see org.opencms.search.I_CmsSearchIndex#setName(java.lang.String)
    */
    public void setName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
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
                            throw new CmsIllegalArgumentException(
                                Messages.get().container(Messages.ERR_SEARCHINDEX_CREATE_INVALID_NAME_1, name));
                        }
                    }
                }
            }
        }
        m_name = name;
    }

    /**
     * Set the path to the index/core. This can either be the path to the directory where the
     * index is stored or the URL where the index/core is reached.
     * @param path to the index/core.
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#setProject(java.lang.String)
     */
    public void setProject(String project) {

        m_project = project;
    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#setRebuildMode(java.lang.String)
     */
    public void setRebuildMode(String rebuildMode) {

        m_rebuild = rebuildMode;

    }

    /**
     * @see org.opencms.search.I_CmsSearchIndex#shutDown()
     */
    public void shutDown() {

        // close the index writer
        if (m_indexWriter != null) {
            try {
                m_indexWriter.commit();
                m_indexWriter.close();
            } catch (IOException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()),
                    e);
            }
        }
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
     * Checks if the given resource should be indexed by this index or not.<p>
     *
     * @param res the resource candidate
     *
     * @return <code>true</code> if the given resource should be indexed or <code>false</code> if not
     */
    protected boolean isIndexing(CmsResource res) {

        // NOTE: This method checks also if the resource is on a path that should be indexed.
        return getDocumentFactory(res) != null;
    }

    /**
     * Sets a flag, indicating if the index should extract content.
     * @param extract a flag, indicating if the index should extract content.
     */
    protected void setExtractContent(boolean extract) {

        m_extractContent = extract;
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

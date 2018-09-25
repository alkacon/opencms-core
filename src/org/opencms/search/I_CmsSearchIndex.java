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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.I_CmsSearchFieldConfiguration;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Interface for search indizes that should be handled by the {@link org.opencms.search.CmsSearchManager}.
 */
public interface I_CmsSearchIndex extends I_CmsConfigurationParameterHandler, Serializable {

    /** Automatic ("auto") index rebuild mode. */
    static final String REBUILD_MODE_AUTO = "auto";

    /** Manual ("manual") index rebuild mode. */
    static final String REBUILD_MODE_MANUAL = "manual";

    /** Offline ("offline") index rebuild mode. */
    static final String REBUILD_MODE_OFFLINE = "offline";

    /** Never ("never") index rebuild mode for indexes that should never be updated via OpenCms. */
    static final String REBUILD_MODE_NEVER = "never";

    /**
     * The method should return the extraction result of a content from the index, if sure the
     * content has not changed since last indexing. This will prevent re-extraction of the content.
     * Preventing re-extraction saves time while indexing, e.g., if only meta-data of a content has changed.
     * If no up-to-date content can be provided from the index, return <code>null</code>.
     *
     * @param resource the resource the content should be provided for.
     * @return the up-to-date extraction result as gained from the index - if possible, or <code>null</code>,
     *  if no up-to-date extraction result can be obtained from the index.
     */
    public I_CmsExtractionResult getContentIfUnchanged(CmsResource resource);

    /**
     * Returns the name of the field configuration used for this index.<p>
     *
     * @return the name of the field configuration used for this index
     */
    public String getFieldConfigurationName();

    /**
     * Returns the language locale for the given resource in this index.<p>
     *
     * @param cms the current OpenCms user context
     * @param resource the resource to check
     * @param availableLocales a list of locales supported by the resource
     *
     * @return the language locale for the given resource in this index
     */
    public Locale getLocaleForResource(CmsObject cms, CmsResource resource, List<Locale> availableLocales);

    /**
     * Returns all configured sources names of this search index.<p>
     *
     * @return a list with all configured sources names of this search index
     */
    public List<String> getSourceNames();

    /**
     * Returns <code>true</code> if full text is extracted by this index.<p>
     *
     * Full text content extraction can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>.
     * Not extraction the full text information will highly improve performance.<p>
     *
     * @return <code>true</code> if full text is extracted by this index
     */
    public boolean isExtractingContent();

    /**
     * Returns the languageDetection.<p>
     *
     * @return the languageDetection
     */
    public boolean isLanguageDetection();

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
    public boolean isUpdatedIncremental();

    /**
     * Removes an index source from this search index.<p>
     *
     * @param sourceName the index source name to remove
     */
    public void removeSourceName(String sourceName);

    /**
     * Sets the locale to index resources.<p>
     *
     * @param locale the locale to index resources
     */
    public void setLocale(Locale locale);

    /**
     * Adds am index source to this search index.<p>
     *
     * @param sourceName the index source name to add
     */
    void addSourceName(String sourceName);

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
    boolean checkConfiguration(CmsObject cms);

    /**
     * Creates an empty document that can be used by this search field configuration.<p>
     *
     * @param resource the resource to create the document for
     *
     * @return a new and empty document
     */
    I_CmsSearchDocument createEmptyDocument(CmsResource resource);

    /**
     * Checks if the provided resource should be excluded from this search index.<p>
     *
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource to index
     *
     * @return true if the resource should be excluded, false if it should be included in this index
     */
    boolean excludeFromIndex(CmsObject cms, CmsResource resource);

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
    I_CmsDocumentFactory getDocumentFactory(CmsResource res);

    /**
     * Returns the search field configuration of this index.<p>
     *
     * @return the search field configuration of this index
     */
    I_CmsSearchFieldConfiguration getFieldConfiguration();

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
    I_CmsIndexWriter getIndexWriter(I_CmsReport report, boolean create) throws CmsIndexException;

    /**
     * Returns the language locale of this index.<p>
     *
     * @return the language locale of this index, for example "en"
     */
    Locale getLocale();

    /**
     * Gets the name of this index.<p>
     *
     * @return the name of the index
     */
    String getName();

    /**
     * Returns the path where this index stores it's data in the "real" file system.<p>
     *
     * @return the path where this index stores it's data in the "real" file system
     */
    String getPath();

    /**
     * Gets the project of this index.<p>
     *
     * @return the project of the index, i.e. "online"
     */
    String getProject();

    /**
     * Get the rebuild mode of this index.<p>
     *
     * @return the current rebuild mode
     */
    String getRebuildMode();

    /**
     * Returns all configured index sources of this search index.<p>
     *
     * @return all configured index sources of this search index
     */
    List<CmsSearchIndexSource> getSources();

    /**
     * Initializes the search index.<p>
     *
     * @throws CmsSearchException if the index source association failed or a configuration error occurred
     */

    void initialize() throws CmsSearchException;

    /**
     * Returns <code>true</code> if this index is currently disabled.<p>
     *
     * @return <code>true</code> if this index is currently disabled
     */
    boolean isEnabled();

    /**
     * Returns a flag, indicating if the search index is successfully initialized.
     * @return a flag, indicating if the search index is successfully initialized.
     */
    boolean isInitialized();

    /**
     * Method called by the search manager if the index has changed.
     * Typically the index searcher is reset when the method is called.
     *
     * @param force if <code>false</code> the index might decide itself it it has to act on the change,
     *  if <code>true</code> it should act, even if itself cannot detect an index change.
     */
    void onIndexChanged(boolean force);

    /**
     * Can be used to enable / disable this index.<p>
     *
     * @param enabled the state of the index to set
     */
    void setEnabled(boolean enabled);

    /**
     * Sets the name of the field configuration used for this index.<p>
     *
     * @param fieldConfigurationName the name of the field configuration to set
     */
    void setFieldConfigurationName(String fieldConfigurationName);

    /**
     * Sets the locale to index resources as a String.<p>
     *
     * @param locale the locale to index resources
     *
     * @see #setLocale(Locale)
     */
    void setLocaleString(String locale);

    /**
     * Sets the logical key/name of this search index.<p>
     *
     * @param name the logical key/name of this search index
     *
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index
     */
    void setName(String name) throws CmsIllegalArgumentException;

    /**
     * Sets the name of the project used to index resources.<p>
     *
     * @param project the name of the project used to index resources
     */
    void setProject(String project);

    /**
     * Sets the rebuild mode of this search index.<p>
     *
     * @param rebuildMode the rebuild mode of this search index {auto|manual}
     */
    void setRebuildMode(String rebuildMode);

    /**
     * Shuts down the search index.<p>
     *
     * This will close the local Lucene index searcher instance.<p>
     */
    void shutDown();

}

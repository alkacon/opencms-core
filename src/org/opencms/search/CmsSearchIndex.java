/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2005/09/26 16:19:04 $
 * Version: $Revision: 1.56.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.CmsHighlightFinder;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.56.2.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchIndex implements I_CmsConfigurationParameterHandler {

    /** Constant for a field list that contains the "meta" field as well as the "content" field. */
    public static final String[] DOC_META_FIELDS = new String[] {
        I_CmsDocumentFactory.DOC_META,
        I_CmsDocumentFactory.DOC_CONTENT};

    /** Constant for additional param to enable excerpt creation (default: true). */
    public static final String EXCERPT = CmsSearchIndex.class.getName() + ".createExcerpt";

    /** Constant for additional param to enable permission checks (default: true). */
    public static final String PERMISSIONS = CmsSearchIndex.class.getName() + ".checkPermissions";

    /** Constant for additional param to set the thread priority during search. */
    public static final String PRIORITY = CmsSearchIndex.class.getName() + ".priority";

    /** Automatic ("auto") index rebuild mode. */
    public static final String REBUILD_MODE_AUTO = "auto";

    /** Manual ("manual") index rebuild mode. */
    public static final String REBUILD_MODE_MANUAL = "manual";

    /** Special root path append token for optimized path queries. */
    public static final String ROOT_PATH_SUFFIX = "@o.c";

    /** Special root path start token for optimized path queries. */
    public static final String ROOT_PATH_TOKEN = "root" + ROOT_PATH_SUFFIX;

    /** Separator for the search excerpt fragments. */
    private static final String EXCERPT_FRAGMENT_SEPARATOR = " ... ";

    /** Size of the excerpt fragments in byte. */
    private static final int EXCERPT_FRAGMENT_SIZE = 60;

    /** Fragments required in excerpt. */
    private static final int EXCERPT_REQUIRED_FRAGMENTS = 5;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndex.class);

    /** The list of configured index sources. */
    List m_sources;

    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Documenttypes of folders/channels. */
    private Map m_documenttypes;

    /** The permission check mode for this index. */
    private boolean m_dontCheckPermissions;

    /** The language filter of this index. */
    private String m_locale;

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

    /** The configured sources for this index. */
    private List m_sourceNames;

    /**
     * Default constructor only intended to be used by the xml configuration. <p>
     * 
     * It is recommended to use the constructor <code>{@link #CmsSearchIndex(String)}</code> 
     * as it enforces the mandatory name argument. <p>
     * 
     */
    public CmsSearchIndex() {

        m_sourceNames = new ArrayList();
        m_documenttypes = new HashMap();
        m_createExcerpt = true;
        m_priority = -1;
    }

    /**
     * Creates a new CmsSearchIndex with the given name.<p>
     * 
     * @param name the system-wide unique name for the search index 
     * 
     * @throws org.opencms.main.CmsIllegalArgumentException 
     *   if the given name is null, empty or already taken 
     *   by another search index. 
     * 
     */
    public CmsSearchIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        this.setName(name);
    }

    /**
     * Rewrites the a resource path for use in the {@link I_CmsDocumentFactory#DOC_ROOT} field.<p>
     * 
     * All "/" chars in the path are replaced with the {@link #ROOT_PATH_SUFFIX} token.
     * This is required in order to use a Lucene "phrase query" on the resource path.
     * Using a phrase query is much, much better for the search performance then using a straightforward 
     * "prefix query". With a "prefix query", Lucene would interally generate a huge list of boolean sub-queries,
     * exactly one for every document in the VFS subtree of the query. So if you query on "/sites/default/*" on 
     * a large OpenCms installation, this means thousands of sub-queries.
     * Using the "phrase query", only one (or very few) queries are internally generated, and the result 
     * is just the same.<p>  
     * 
     * This implementation basically replaces the "/" of a path with "@o.c ". 
     * This is a trick so that the Lucene analyzer leaves the
     * directory names untouched, since it treats them like literal email addresses. 
     * Otherwise the language analyzer might modify the directory names, leading to potential
     * duplicates (e.g. <code>members/</code> and <code>member/</code> may both be trimmed to <code>member</code>),
     * so that the prefix search returns more results then expected.<p>
     * @param path the path to rewrite
     * 
     * @return the re-written path
     */
    public static String rootPathRewrite(String path) {

        StringBuffer result = new StringBuffer(256);
        String[] elements = rootPathSplit(path);
        for (int i = 0; i < elements.length; i++) {
            result.append(elements[i]);
            if ((i + 1) < elements.length) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    /**
     * Spits the a resource path into tokens for use in the <code>{@link I_CmsDocumentFactory#DOC_ROOT}</code> field
     * and with the <code>{@link #rootPathRewrite(String)}</code> method.<p>
     * 
     * @param path the path to split
     * 
     * @return the splitted path
     * 
     * @see #rootPathRewrite(String)
     */
    public static String[] rootPathSplit(String path) {

        if (CmsStringUtil.isEmpty(path)) {
            return new String[] {ROOT_PATH_TOKEN};
        }

        // split the path
        String[] elements = CmsStringUtil.splitAsArray(path, '/');
        int length = elements.length + 1;
        String[] result = new String[length];
        result[0] = ROOT_PATH_TOKEN;
        for (int i = 1; i < length; i++) {
            // append suffix to all path elements
            result[i] = elements[i - 1] + ROOT_PATH_SUFFIX;
            // underscore '_' is a word separator for the Lucene analyzer, must replace this
            result[i] = result[i].replace('_', '0');
        }
        return result;
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     */
    public void addConfigurationParameter(String key, String value) {

        if (PERMISSIONS.equals(key)) {
            m_dontCheckPermissions = !Boolean.valueOf(value).booleanValue();
        } else if (EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();
        } else if (PRIORITY.equals(key)) {
            m_priority = Integer.parseInt(value);
            if (m_priority < Thread.MIN_PRIORITY) {
                m_priority = Thread.MIN_PRIORITY;
                LOG.error(Messages.get().key(
                    Messages.LOG_SEARCH_PRIORITY_TOO_LOW_2,
                    value,
                    new Integer(Thread.MIN_PRIORITY)));

            } else if (m_priority > Thread.MAX_PRIORITY) {
                m_priority = Thread.MAX_PRIORITY;
                LOG.debug(Messages.get().key(
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
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        Map result = new TreeMap();
        if (m_priority > 0) {
            result.put(PRIORITY, new Integer(m_priority));
        }
        if (!m_createExcerpt) {
            result.put(EXCERPT, new Boolean(m_createExcerpt));
        }
        if (m_dontCheckPermissions) {
            result.put(PERMISSIONS, new Boolean(!m_dontCheckPermissions));
        }
        return result;
    }

    /**
     * Returns the configured document types of this index for the given resource path.<p>
     * 
     * The result List contains Strings with the names of the document types.<p>
     * 
     * @param path path of the folder 
     * 
     * @return the configured document types of this index for the given resource path
     */
    public List getDocumenttypes(String path) {

        List documenttypes = null;
        if (m_documenttypes != null) {
            for (Iterator i = m_documenttypes.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                // NOTE: assumed that configured resource paths do not overlap, otherwise result is undefined
                if (path.startsWith(key)) {
                    documenttypes = (List)m_documenttypes.get(key);
                    break;
                }
            }
        }
        if (documenttypes == null) {
            documenttypes = OpenCms.getSearchManager().getDocumentTypes();
        }
        return documenttypes;
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
        Analyzer analyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);

        try {
            File f = new File(m_path);
            if (f.exists()) {
                // index already exists
                indexWriter = new IndexWriter(m_path, analyzer, create);
            } else {
                // index does not exist yet
                f = f.getParentFile();
                if (f != null && !f.exists()) {
                    // create the parent folders if required
                    f.mkdirs();
                }
                indexWriter = new IndexWriter(m_path, analyzer, true);
            }

        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, m_path, m_name),
                e);
        }

        return indexWriter;
    }

    /**
     * Gets the langauge of this index.<p>
     * 
     * @return the language of the index, i.e. de
     */
    public String getLocale() {

        return m_locale;
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
    public List getSourceNames() {

        return m_sourceNames;
    }

    /**
     * Returns all configured index sources of this search index.<p>
     * 
     * @return all configured index sources of this search index
     */
    public List getSources() {

        return m_sources;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
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

        String sourceName = null;
        CmsSearchIndexSource indexSource = null;
        List searchIndexSourceDocumentTypes = null;
        List resourceNames = null;
        String resourceName = null;
        m_sources = new ArrayList();

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + m_name);

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {

            try {
                sourceName = (String)m_sourceNames.get(i);
                indexSource = OpenCms.getSearchManager().getIndexSource(sourceName);
                m_sources.add(indexSource);

                resourceNames = indexSource.getResourcesNames();
                searchIndexSourceDocumentTypes = indexSource.getDocumentTypes();
                for (int j = 0, m = resourceNames.size(); j < m; j++) {

                    resourceName = (String)resourceNames.get(j);
                    m_documenttypes.put(resourceName, searchIndexSourceDocumentTypes);
                }
            } catch (Exception exc) {
                throw new CmsSearchException(Messages.get().container(
                    Messages.ERR_INDEX_SOURCE_ASSOCIATION_1,
                    sourceName), exc);
            }
        }
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
     * @param matchesPerPage the number of search results per page, or -1 to return all found documents in the search result
     * @return the List of results found or an empty list
     * @throws CmsSearchException if something goes wrong
     */
    public synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params, int matchesPerPage)
    throws CmsSearchException {

        long timeTotal = -System.currentTimeMillis();
        long timeLucene;
        long timeResultProcessing;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SEARCH_PARAMS_2, params, m_name));
        }

        CmsRequestContext context = cms.getRequestContext();
        CmsProject currentProject = context.currentProject();

        // the searcher to perform the operation in
        IndexSearcher searcher = null;

        // the hits found during the search
        Hits hits;

        // storage for the results found
        CmsSearchResultList searchResults = new CmsSearchResultList();

        int previousPriority = Thread.currentThread().getPriority();

        try {

            if (m_priority > 0) {
                // change thread priority in order to reduce search impact on overall system performance
                Thread.currentThread().setPriority(m_priority);
            }

            // change the project     
            context.setCurrentProject(cms.readProject(m_project));

            // complete the search root
            String[] roots;
            if ((params.getRoots() != null) && (params.getRoots().size() > 0)) {
                // add the site root to all the search root
                roots = new String[params.getRoots().size()];
                for (int i = 0; i < params.getRoots().size(); i++) {
                    roots[i] = cms.getRequestContext().addSiteRoot((String)params.getRoots().get(i));
                }
            } else {
                // just use the site root as the search root
                // this permits searching in indexes that contain content of other sites than the current selected one?!?!
                roots = new String[] {cms.getRequestContext().getSiteRoot()};
            }

            timeLucene = -System.currentTimeMillis();

            // the language analyzer to use for creating the queries
            Analyzer languageAnalyzer = OpenCms.getSearchManager().getAnalyzer(m_locale);

            // the main query to use, will be constructed in the next lines 
            BooleanQuery query = new BooleanQuery();

            // implementation note: 
            // initially this was a simple PrefixQuery based on the DOC_PATH
            // however, internally Lucene rewrote that to literally hundreds of BooleanQuery parts
            // the following implementation will lead to just one Lucene PhraseQuery per directory and is thus much better    
            BooleanQuery pathQuery = new BooleanQuery();
            for (int i = 0; i < roots.length; i++) {
                String[] paths = rootPathSplit(roots[i]);
                PhraseQuery phrase = new PhraseQuery();
                for (int j = 0; j < paths.length; j++) {
                    Term term = new Term(I_CmsDocumentFactory.DOC_ROOT, paths[j]);
                    phrase.add(term);
                }
                pathQuery.add(phrase, false, false);
            }
            // add the calculated phrase query for the root path
            query.add(pathQuery, true, false);

            if ((params.getCategories() != null) && (params.getCategories().size() > 0)) {
                // add query categories (if required)
                BooleanQuery categoryQuery = new BooleanQuery();
                for (int i = 0; i < params.getCategories().size(); i++) {
                    Term term = new Term(I_CmsDocumentFactory.DOC_CATEGORY, (String)params.getCategories().get(i));
                    TermQuery termQuery = new TermQuery(term);
                    categoryQuery.add(termQuery, false, false);
                }
                query.add(categoryQuery, true, false);
            }

            if ((params.getFields() != null) && (params.getFields().size() > 0)) {
                // this is a "regular" query over one or more fields
                BooleanQuery fieldsQuery = new BooleanQuery();
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                for (int i = 0; i < params.getFields().size(); i++) {
                    fieldsQuery.add(QueryParser.parse(
                        params.getQuery(),
                        (String)params.getFields().get(i),
                        languageAnalyzer), false, false);
                }
                // finally add the field queries to the main query
                query.add(fieldsQuery, true, false);
            } else {
                // if no fields are provided, just use the "content" field by default
                query.add(
                    QueryParser.parse(params.getQuery(), I_CmsDocumentFactory.DOC_CONTENT, languageAnalyzer),
                    true,
                    false);
            }

            // create the index searcher
            searcher = new IndexSearcher(m_path);
            Query finalQuery;

            if (m_createExcerpt || LOG.isDebugEnabled()) {
                // we re-write the query because this enables highlighting of wildcard terms in excerpts 
                finalQuery = searcher.rewrite(query);
            } else {
                finalQuery = query;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_BASE_QUERY_1, query));
                LOG.debug(Messages.get().key(Messages.LOG_REWRITTEN_QUERY_1, finalQuery));

            }

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

            // perform the search operation          
            hits = searcher.search(finalQuery, params.getSort());

            int hitCount = hits.length();

            timeLucene += System.currentTimeMillis();
            timeResultProcessing = -System.currentTimeMillis();

            Document doc;
            CmsSearchResult searchResult;
            String excerpt = null;

            if (hits != null) {
                int page = params.getSearchPage();
                int start = -1, end = -1;
                if (matchesPerPage > 0 && page > 0 && hitCount > 0) {
                    // calculate the final size of the search result
                    start = matchesPerPage * (page - 1);
                    end = start + matchesPerPage;
                    // ensure that both i and n are inside the range of foundDocuments.size()
                    start = (start > hitCount) ? hitCount : start;
                    end = (end > hitCount) ? hitCount : end;
                } else {
                    // return all found documents in the search result
                    start = 0;
                    end = hitCount;
                }

                int visibleHitCount = hitCount;
                for (int i = 0, cnt = 0; i < hitCount && cnt < end; i++) {
                    try {
                        doc = hits.doc(i);
                        if (hasReadPermission(cms, doc)) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document !
                                if (m_createExcerpt) {
                                    excerpt = getExcerpt(
                                        doc.getField(I_CmsDocumentFactory.DOC_CONTENT).stringValue(),
                                        finalQuery,
                                        languageAnalyzer);
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
                            LOG.warn(Messages.get().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
                        }
                    }
                }

                // save the total count of search results at the last index of the search result 
                searchResults.setHitCount(visibleHitCount);
            } else {
                searchResults.setHitCount(0);
            }

            timeResultProcessing += System.currentTimeMillis();

        } catch (Exception exc) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), exc);
        } finally {

            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);

            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException exc) {
                    // noop
                }
            }

            // switch back to the original project
            context.setCurrentProject(currentProject);
        }

        timeTotal += System.currentTimeMillis();

        Object[] logParams = new Object[] {
            new Integer(hits.length()),
            new Long(timeTotal),
            new Long(timeLucene),
            new Long(timeResultProcessing)};
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_STAT_RESULTS_TIME_4, logParams));
        }

        return searchResults;
    }

    /**
     * Sets the locale to index resources.<p>
     * 
     * @param locale the locale to index resources
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the logical key/name of this search index.<p>
     * 
     * @param name the logical key/name of this search index 
     * 
     * @throws org.opencms.main.CmsIllegalArgumentException 
     *   if the given name is null, empty or already taken 
     *   by another search index. 
     */
    public void setName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
        } else {

            // check if already used, but only if the name was modified: 
            // this is important as unmodifiable DisplayWidgets will also invoke this...
            if (!name.equals(m_name)) {
                // don't mess with xml-configuration
                if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // Not needed at startup and additionally getSearchManager may return null
                    Iterator itIdxNames = OpenCms.getSearchManager().getIndexNames().iterator();
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

        m_project = projectName;
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
     * Returns the name (<code>{@link #getName()}</code>) of this search index.<p>
     *  
     * @return the name (<code>{@link #getName()}</code>) of this search index
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return getName();
    }

    /**
     * Returns an excerpt of the given content related to the given search query.<p>
     * 
     * @param content the content
     * @param searchQuery the search query
     * @param analyzer the analyzer used 
     * 
     * @return an excerpt of the content
     * 
     * @throws IOException if something goes wrong
     */
    protected String getExcerpt(String content, Query searchQuery, Analyzer analyzer) throws IOException {

        if (content == null) {
            return null;
        }

        CmsHighlightFinder highlighter = new CmsHighlightFinder(
            OpenCms.getSearchManager().getHighlighter(),
            searchQuery,
            analyzer);

        String excerpt = highlighter.getBestFragments(
            content,
            EXCERPT_FRAGMENT_SIZE,
            EXCERPT_REQUIRED_FRAGMENTS,
            EXCERPT_FRAGMENT_SEPARATOR);

        // kill all unwanted chars in the excerpt
        excerpt = excerpt.replace('\t', ' ');
        excerpt = excerpt.replace('\n', ' ');
        excerpt = excerpt.replace('\r', ' ');
        excerpt = excerpt.replace('\f', ' ');

        int maxLength = OpenCms.getSearchManager().getMaxExcerptLength();
        if (excerpt != null && excerpt.length() > maxLength) {
            excerpt = excerpt.substring(0, maxLength);
        }

        return excerpt;
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

        if (m_dontCheckPermissions) {
            // no permission check is performed at all
            return true;
        }

        Field typeField = doc.getField(I_CmsDocumentFactory.DOC_TYPE);
        Field pathField = doc.getField(I_CmsDocumentFactory.DOC_PATH);
        if ((typeField == null) || (pathField == null)) {
            // permission check needs only to be performed for VFS documents that contain both fields
            return true;
        }

        String rootPath = cms.getRequestContext().removeSiteRoot(pathField.stringValue());

        // check if the resource "exits", this will implicitly check read permission and if the resource was deleted
        return cms.existsResource(rootPath);
    }
}
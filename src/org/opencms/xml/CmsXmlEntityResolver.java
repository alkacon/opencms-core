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

package org.opencms.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.page.CmsXmlPage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities (e.g. external DTDs) in the OpenCms VFS.<p>
 *
 * Also provides a cache for XML content schema definitions.<p>
 *
 * @since 6.0.0
 */
public class CmsXmlEntityResolver implements EntityResolver, I_CmsEventListener {

    /** Maximum size of the content definition cache. */
    public static final int CONTENT_DEFINITION_CACHE_SIZE = 2048;

    /** Scheme for files which should be retrieved from the classpath. */
    public static final String INTERNAL_SCHEME = "internal://";

    /** The scheme to identify a file in the OpenCms VFS. */
    public static final String OPENCMS_SCHEME = "opencms://";

    /**
     * A list of string pairs used to translate legacy system ids to a new form. The first component of each pair
     * is the prefix which should be replaced by the second component of that pair.
     */
    private static final String[][] LEGACY_TRANSLATIONS = {
        {"opencms://system/modules/org.opencms.ade.config/schemas/", "internal://org/opencms/xml/adeconfig/"},
        {
            "opencms://system/modules/org.opencms.ade.containerpage/schemas/",
            "internal://org/opencms/xml/containerpage/"},
        {"opencms://system/modules/org.opencms.ade.sitemap/schemas/", "internal://org/opencms/xml/adeconfig/sitemap/"},
        {"opencms://system/modules/org.opencms.ugc/schemas/", "internal://org/opencms/ugc/"},
        {"opencms://system/modules/org.opencms.jsp.search/schemas/", "internal://org/opencms/jsp/search/"}

    };

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlEntityResolver.class);

    /** A temporary cache for XML content definitions. */
    private static Map<String, CmsXmlContentDefinition> m_cacheContentDefinitions;

    /** A permanent cache to avoid multiple readings of often used files from the VFS. */
    private static Map<String, byte[]> m_cachePermanent;

    /** A temporary cache to avoid multiple readings of often used files from the VFS. */
    private static Map<String, byte[]> m_cacheTemporary;

    /** The location of the XML page XML schema. */
    private static final String XMLPAGE_OLD_DTD_LOCATION = "org/opencms/xml/page/xmlpage.dtd";

    /** The (old) DTD address of the OpenCms xmlpage (used in 5.3.5). */
    private static final String XMLPAGE_OLD_DTD_SYSTEM_ID_1 = "http://www.opencms.org/dtd/6.0/xmlpage.dtd";

    /** The (old) DTD address of the OpenCms xmlpage (used until 5.3.5). */
    private static final String XMLPAGE_OLD_DTD_SYSTEM_ID_2 = "/system/shared/page.dtd";

    /** The location of the xmlpage XSD. */
    private static final String XMLPAGE_XSD_LOCATION = "org/opencms/xml/page/xmlpage.xsd";

    /** The cms object to use for VFS access (will be initialized with "Guest" permissions). */
    private CmsObject m_cms;

    /**
     * Creates a new XML entity resolver based on the provided CmsObject.<p>
     *
     * If the provided CmsObject is null, then the OpenCms VFS is not
     * searched for XML entities, however the internal cache and
     * other OpenCms internal entities not in the VFS are still resolved.<p>
     *
     * @param cms the cms context to use for resolving XML files from the OpenCms VFS
     */
    public CmsXmlEntityResolver(CmsObject cms) {

        initCaches();
        m_cms = cms;
    }

    /**
     * Adds a system ID URL to to internal permanent cache.<p>
     *
     * This cache will NOT be cleared automatically.<p>
     *
     * @param systemId the system ID to add
     * @param content the content of the system id
     */
    public static void cacheSystemId(String systemId, byte[] content) {

        initCaches();
        m_cachePermanent.put(systemId, content);
    }

    /**
     * Checks if a given system ID URL is in the internal permanent cache.<p>
     *
     * This check is required to see if a XML content is based on a file that actually exists in the OpenCms VFS,
     * or if the schema has been just cached without a VFS file.<p>
     *
     * @param systemId the system id ID check
     *
     * @return <code>true</code> if the system ID is in the internal permanent cache, <code>false</code> otherwise
     */
    public static boolean isCachedSystemId(String systemId) {

        if (m_cachePermanent != null) {
            return m_cachePermanent.containsKey(systemId);
        }
        return false;
    }

    /**
     * Checks whether the given schema id is an internal schema id or is translated to an internal schema id.<p>
     * @param schema the schema id
     * @return true if the given schema id is an internal schema id or translated to an internal schema id
     */
    public static boolean isInternalId(String schema) {

        String translatedId = translateLegacySystemId(schema);
        if (translatedId.startsWith(INTERNAL_SCHEME)) {
            return true;
        }
        return false;
    }

    /**
     * Initialize the OpenCms XML entity resolver.<p>
     *
     * @param adminCms an initialized OpenCms user context with "Administrator" role permissions
     * @param typeSchemaBytes the base widget type XML schema definitions
     *
     * @see CmsXmlContentTypeManager#initialize(CmsObject)
     */
    protected static void initialize(CmsObject adminCms, byte[] typeSchemaBytes) {

        // create the resolver to register as event listener
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(adminCms);

        // register this object as event listener
        OpenCms.addCmsEventListener(
            resolver,
            new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_MOVED,
                I_CmsEventListener.EVENT_RESOURCE_DELETED});

        // cache the base widget type XML schema definitions
        cacheSystemId(CmsXmlContentDefinition.XSD_INCLUDE_OPENCMS, typeSchemaBytes);
    }

    /**
     * Initializes the internal caches for permanent and temporary system IDs.<p>
     */
    private static void initCaches() {

        if (m_cacheTemporary == null) {
            m_cacheTemporary = CmsMemoryMonitor.createLRUCacheMap(1024);

            m_cachePermanent = new ConcurrentHashMap<String, byte[]>(32);

            m_cacheContentDefinitions = CmsMemoryMonitor.createLRUCacheMap(CONTENT_DEFINITION_CACHE_SIZE);
        }
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            if ((OpenCms.getMemoryMonitor() != null)
                && !OpenCms.getMemoryMonitor().isMonitoring(CmsXmlEntityResolver.class.getName() + ".cacheTemporary")) {
                // reinitialize the caches after the memory monitor is set up
                Map<String, byte[]> cacheTemporary = CmsMemoryMonitor.createLRUCacheMap(128);
                cacheTemporary.putAll(m_cacheTemporary);
                m_cacheTemporary = cacheTemporary;
                OpenCms.getMemoryMonitor().register(
                    CmsXmlEntityResolver.class.getName() + ".cacheTemporary",
                    cacheTemporary);

                Map<String, byte[]> cachePermanent = new ConcurrentHashMap<String, byte[]>(32);
                cachePermanent.putAll(m_cachePermanent);
                m_cachePermanent = cachePermanent;
                OpenCms.getMemoryMonitor().register(
                    CmsXmlEntityResolver.class.getName() + ".cachePermanent",
                    cachePermanent);

                Map<String, CmsXmlContentDefinition> cacheContentDefinitions = CmsMemoryMonitor.createLRUCacheMap(
                    CONTENT_DEFINITION_CACHE_SIZE);
                cacheContentDefinitions.putAll(m_cacheContentDefinitions);
                m_cacheContentDefinitions = cacheContentDefinitions;
                OpenCms.getMemoryMonitor().register(
                    CmsXmlEntityResolver.class.getName() + ".cacheContentDefinitions",
                    cacheContentDefinitions);
            }
        }
    }

    /**
     * Translates a legacy system id to a new form.<p>
     *
     * @param systemId the original system id
     * @return the new system id
     */
    private static String translateLegacySystemId(String systemId) {

        String result = systemId;
        for (String[] translation : LEGACY_TRANSLATIONS) {
            if (systemId.startsWith(translation[0])) {
                // replace prefix with second component if it matches the first component
                result = translation[1] + systemId.substring(translation[0].length());
                break;
            }
        }
        if (OpenCms.getRepositoryManager() != null) {
            result = OpenCms.getResourceManager().getXsdTranslator().translateResource(result);
        }
        return result;
    }

    /**
     * Caches an XML content definition based on the given system id and the online / offline status
     * of this entity resolver instance.<p>
     *
     * @param systemId the system id to use as cache key
     * @param contentDefinition the content definition to cache
     */
    public void cacheContentDefinition(String systemId, CmsXmlContentDefinition contentDefinition) {

        String cacheKey = getCacheKeyForCurrentProject(systemId);
        m_cacheContentDefinitions.put(cacheKey, contentDefinition);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_CACHED_SYSTEM_ID_1, cacheKey));
        }
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource;
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                // only flush cache if a schema definition where published
                CmsUUID publishHistoryId = new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID));
                if (isSchemaDefinitionInPublishList(publishHistoryId)) {
                    m_cacheTemporary.clear();
                    m_cacheContentDefinitions.clear();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_FLUSHED_CACHES_0));
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                // flush cache
                m_cacheTemporary.clear();
                m_cacheContentDefinitions.clear();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_FLUSHED_CACHES_0));
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                if ((change != null) && change.equals(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED))) {
                    // skip lock & unlock
                    return;
                }
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                uncacheSystemId(resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                List<CmsResource> resources = CmsCollectionsGenericWrapper.list(
                    event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (int i = 0; i < resources.size(); i++) {
                    resource = resources.get(i);
                    uncacheSystemId(resource.getRootPath());
                }
                break;
            default:
                // no operation
        }
    }

    /**
     * Looks up the given XML content definition system id in the internal content definition cache.<p>
     *
     * @param systemId the system id of the XML content definition to look up
     *
     * @return the XML content definition found, or null if no definition is cached for the given system id
     */
    public CmsXmlContentDefinition getCachedContentDefinition(String systemId) {

        String cacheKey = getCacheKeyForCurrentProject(systemId);
        CmsXmlContentDefinition result = m_cacheContentDefinitions.get(cacheKey);
        if ((result != null) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CACHE_LOOKUP_SUCCEEDED_1, cacheKey));
        }
        return result;
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) throws IOException {

        // lookup the system id caches first
        byte[] content;
        systemId = translateLegacySystemId(systemId);
        content = m_cachePermanent.get(systemId);
        if (content != null) {
            // permanent cache contains system id
            return createInputSource(content, systemId);
        } else if (systemId.equals(CmsXmlPage.XMLPAGE_XSD_SYSTEM_ID)) {

            // XML page XSD reference
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(XMLPAGE_XSD_LOCATION)) {
                content = CmsFileUtil.readFully(stream);
                // cache the XML page DTD
                m_cachePermanent.put(systemId, content);
                return createInputSource(content, systemId);
            } catch (Throwable t) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_XMLPAGE_XSD_NOT_FOUND_1, XMLPAGE_XSD_LOCATION),
                    t);
            }

        } else if (systemId.equals(XMLPAGE_OLD_DTD_SYSTEM_ID_1) || systemId.endsWith(XMLPAGE_OLD_DTD_SYSTEM_ID_2)) {

            // XML page DTD reference
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(XMLPAGE_OLD_DTD_LOCATION)) {
                // cache the XML page DTD
                content = CmsFileUtil.readFully(stream);
                m_cachePermanent.put(systemId, content);
                return createInputSource(content, systemId);
            } catch (Throwable t) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_XMLPAGE_DTD_NOT_FOUND_1, XMLPAGE_OLD_DTD_LOCATION),
                    t);
            }
        } else if ((m_cms != null) && systemId.startsWith(OPENCMS_SCHEME)) {

            // opencms:// VFS reference
            String cacheSystemId = systemId.substring(OPENCMS_SCHEME.length() - 1);
            String cacheKey = getCacheKey(
                cacheSystemId,
                m_cms.getRequestContext().getCurrentProject().isOnlineProject());
            // look up temporary cache
            content = m_cacheTemporary.get(cacheKey);
            if (content != null) {
                return createInputSource(content, systemId);
            }
            String storedSiteRoot = m_cms.getRequestContext().getSiteRoot();
            try {
                // content not cached, read from VFS
                m_cms.getRequestContext().setSiteRoot("/");
                CmsFile file = m_cms.readFile(cacheSystemId, CmsResourceFilter.IGNORE_EXPIRATION);
                content = file.getContents();
                // store content in cache
                m_cacheTemporary.put(cacheKey, content);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_CACHED_SYS_ID_1, cacheKey));
                }
                return createInputSource(content, systemId);
            } catch (CmsException e) {
                throw new IOException(
                    Messages.get().getBundle().key(Messages.LOG_ENTITY_RESOLVE_FAILED_1, systemId),
                    e);
            } finally {
                m_cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }

        } else if (systemId.startsWith(INTERNAL_SCHEME)) {
            String location = systemId.substring(INTERNAL_SCHEME.length());
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(location)) {
                content = CmsFileUtil.readFully(stream);
                m_cachePermanent.put(systemId, content);
                return createInputSource(content, systemId);
            } catch (Throwable t) {
                LOG.error(t.getLocalizedMessage(), t);
            }

        } else if (systemId.substring(0, systemId.lastIndexOf("/") + 1).equalsIgnoreCase(
            CmsConfigurationManager.DEFAULT_DTD_PREFIX)//
        ) {
            // default DTD location in the org.opencms.configuration package
            String location = null;
            try {
                String dtdFilename = systemId.substring(systemId.lastIndexOf("/") + 1);
                location = CmsConfigurationManager.DEFAULT_DTD_LOCATION + dtdFilename;
                InputStream stream = getClass().getClassLoader().getResourceAsStream(location);
                content = CmsFileUtil.readFully(stream);
                // cache the DTD
                m_cachePermanent.put(systemId, content);
                return createInputSource(content, systemId);
            } catch (Throwable t) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_DTD_NOT_FOUND_1, location), t);
            }
        }
        LOG.error("Entity reference not allowed: " + systemId, new IOException());
        throw new IOException("Entity reference not allowed (see log for details)");
    }

    /**
     * Removes a cached entry for a system id (filename) from the internal offline temporary and content definition caches.<p>
     *
     * The online resources cached for the online project are only flushed when a project is published.<p>
     *
     * @param systemId the system id (filename) to remove from the cache
     */
    public void uncacheSystemId(String systemId) {

        Object o;
        o = m_cacheTemporary.remove(getCacheKey(systemId, false));
        if (null != o) {
            // if an object was removed from the temporary cache, all XML content definitions must be cleared
            // because this may be a nested subschema
            m_cacheContentDefinitions.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_ERR_UNCACHED_SYS_ID_1, getCacheKey(systemId, false)));
            }
        } else {
            // check if a cached content definition has to be removed based on the system id
            o = m_cacheContentDefinitions.remove(getCacheKey(systemId, false));
            if ((null != o) && LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_ERR_UNCACHED_CONTENT_DEF_1,
                        getCacheKey(systemId, false)));
            }
        }
    }

    /**
     * Creates an input source for the given byte data and system id.<p>
     *
     * @param data the data which the input source should return
     * @param systemId the system id for the input source
     *
     * @return the input source
     */
    InputSource createInputSource(byte[] data, String systemId) {

        InputSource result = new InputSource(new ByteArrayInputStream(data));
        result.setSystemId(systemId);
        return result;
    }

    /**
     * Returns a cache key for the given system id (filename) based on the status
     * of the given project flag.<p>
     *
     * @param systemId the system id (filename) to get the cache key for
     * @param online indicates if this key is generated for the online project
     *
     * @return the cache key for the system id
     */
    private String getCacheKey(String systemId, boolean online) {

        if (online) {
            return "online_".concat(systemId);
        }
        return "offline_".concat(systemId);
    }

    /**
     * Returns a cache key for the given system id (filename) based on the status
     * of the internal CmsObject.<p>
     *
     * @param systemId the system id (filename) to get the cache key for
     *
     * @return the cache key for the system id
     */
    private String getCacheKeyForCurrentProject(String systemId) {

        // check the project
        boolean project = (m_cms != null) ? m_cms.getRequestContext().getCurrentProject().isOnlineProject() : false;

        // remove opencms:// prefix
        if (systemId.startsWith(OPENCMS_SCHEME)) {
            systemId = systemId.substring(OPENCMS_SCHEME.length() - 1);
        }

        return getCacheKey(systemId, project);
    }

    /**
     * Proves if there is at least one xsd or dtd file in the list of resources to publish.<p>
     *
     * @param publishHistoryId the publish history id
     *
     * @return true, if there is at least one xsd or dtd file in the list of resources to publish, otherwise false
     */
    private boolean isSchemaDefinitionInPublishList(CmsUUID publishHistoryId) {

        if (m_cms == null) {
            // CmsObject not available, assume there may be a schema definition in the publish history
            return true;
        }
        try {
            List<CmsPublishedResource> publishedResources = m_cms.readPublishedResources(publishHistoryId);
            for (CmsPublishedResource cmsPublishedResource : publishedResources) {
                String resourceRootPath = cmsPublishedResource.getRootPath();
                String resourceRootPathLowerCase = resourceRootPath.toLowerCase();
                if (resourceRootPathLowerCase.endsWith(".xsd")
                    || resourceRootPathLowerCase.endsWith(".dtd")
                    || m_cacheTemporary.containsKey(getCacheKey(resourceRootPath, true))) {
                    return true;
                }
            }
        } catch (CmsException e) {
            // error reading published Resources.
            LOG.warn(e.getMessage(), e);
        }
        return false;
    }
}
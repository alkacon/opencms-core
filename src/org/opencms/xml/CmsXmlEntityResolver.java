/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlEntityResolver.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.xml.page.CmsXmlPage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities (e.g. external DTDs) in the OpenCms VFS.<p>
 * 
 * Also provides a cache for XML content schema definitions.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.17 $ 
 */
public class CmsXmlEntityResolver implements EntityResolver, I_CmsEventListener {

    /** The scheme to identify a file in the OpenCms VFS. */
    public static final String OPENCMS_SCHEME = "opencms://";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlEntityResolver.class);

    /** A temporary cache for XML content definitions. */
    private static Map m_cacheContentDefinitions;

    /** A permanent cache to avoid multiple readings of often used files from the VFS. */
    private static Map m_cachePermanent;

    /** A temporary cache to avoid multiple readings of often used files from the VFS. */
    private static Map m_cacheTemporary;

    /** The static default entity resolver for reading / writing xml content. */
    private static CmsXmlEntityResolver m_resolver;

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

        if ((cms != null) && (m_resolver == null)) {
            // initialize singleton only if cms Object creation is surley possible
            m_resolver = new CmsXmlEntityResolver();
        }

        m_cms = cms;
    }

    /**
     * Initializes the caches and registers the event handler.<p>
     */
    private CmsXmlEntityResolver() {

        // required for unit tests where no OpenCms is available
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {

            try {
                m_cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            } catch (CmsException e) {
                // this should never happen
                LOG.error(Messages.get().key(Messages.LOG_INITIALIZE_DEFAULT_GUEST_USER_FAILED_0));
            }

            // register this object as event listener
            OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_DELETED});
        }
    }

    /**
     * Adds a sytem id URL to to internal permanent cache.<p>
     * 
     * This cache will NOT be cleared automatically.<p>
     * 
     * @param systemId the system id to add
     * @param content the content of the system id
     */
    public static void cacheSystemId(String systemId, byte[] content) {

        initCaches();
        m_cachePermanent.put(systemId, content);
    }

    /**
     * Initializes the internal caches for permanent and temporary system IDs.<p>
     */
    private static void initCaches() {

        if (m_cacheTemporary == null) {

            LRUMap cacheTemporary = new LRUMap(128);
            m_cacheTemporary = Collections.synchronizedMap(cacheTemporary);

            HashMap cachePermanent = new HashMap(32);
            m_cachePermanent = Collections.synchronizedMap(cachePermanent);

            LRUMap cacheContentDefinitions = new LRUMap(64);
            m_cacheContentDefinitions = Collections.synchronizedMap(cacheContentDefinitions);

            if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
                if ((OpenCms.getMemoryMonitor() != null) && OpenCms.getMemoryMonitor().enabled()) {
                    // map must be of type "LRUMap" so that memory monitor can acecss all information
                    OpenCms.getMemoryMonitor().register(
                        CmsXmlEntityResolver.class.getName() + ".m_cacheTemporary",
                        cacheTemporary);
                    // map must be of type "HashMap" so that memory monitor can acecss all information
                    OpenCms.getMemoryMonitor().register(
                        CmsXmlEntityResolver.class.getName() + ".m_cachePermanent",
                        cachePermanent);
                    // map must be of type "LRUMap" so that memory monitor can acecss all information
                    OpenCms.getMemoryMonitor().register(
                        CmsXmlEntityResolver.class.getName() + ".m_cacheContentDefinitions",
                        cacheContentDefinitions);
                }
            }
        }
    }

    /**
     * Caches an XML content defintion based on the given system id and the online / offline status
     * of this entity resolver instance.<p>
     * 
     * @param systemId the system id to use as cache key
     * @param contentDefinition the content definition to cache
     */
    public void cacheContentDefinition(String systemId, CmsXmlContentDefinition contentDefinition) {

        String cacheKey = getCacheKeyForCurrentProject(systemId);
        m_cacheContentDefinitions.put(cacheKey, contentDefinition);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ER_CACHED_SYSTEM_ID_1, cacheKey));
        }
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource;
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                // flush cache   
                m_cacheTemporary.clear();
                m_cacheContentDefinitions.clear();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_ER_FLUSHED_CACHES_0));
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                resource = (CmsResource)event.getData().get("resource");
                uncacheSystemId(resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                List resources = (List)event.getData().get("resources");
                for (int i = 0; i < resources.size(); i++) {
                    resource = (CmsResource)resources.get(i);
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
        CmsXmlContentDefinition result = (CmsXmlContentDefinition)m_cacheContentDefinitions.get(cacheKey);
        if ((result != null) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_CACHE_LOOKUP_SUCCEEDED_1, cacheKey));
        }
        return result;
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) {

        // lookup the system id caches first
        byte[] content;
        content = (byte[])m_cachePermanent.get(systemId);
        if (content != null) {

            // permanent cache contains system id
            return new InputSource(new ByteArrayInputStream(content));
        } else if (systemId.equals(CmsXmlPage.C_XMLPAGE_XSD_SYSTEM_ID)) {

            // XML page XSD reference
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(XMLPAGE_XSD_LOCATION);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
                for (int b = stream.read(); b > -1; b = stream.read()) {
                    bytes.write(b);
                }
                content = bytes.toByteArray();
                // cache the XML page DTD
                m_cachePermanent.put(systemId, content);
                return new InputSource(new ByteArrayInputStream(content));
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_XMLPAGE_XSD_NOT_FOUND_1, XMLPAGE_XSD_LOCATION), t);
            }

        } else if (systemId.equals(XMLPAGE_OLD_DTD_SYSTEM_ID_1) || systemId.endsWith(XMLPAGE_OLD_DTD_SYSTEM_ID_2)) {

            // XML page DTD reference
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(XMLPAGE_OLD_DTD_LOCATION);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
                for (int b = stream.read(); b > -1; b = stream.read()) {
                    bytes.write(b);
                }
                content = bytes.toByteArray();
                // cache the XML page DTD
                m_cachePermanent.put(systemId, content);
                return new InputSource(new ByteArrayInputStream(content));
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_XMLPAGE_DTD_NOT_FOUND_1, XMLPAGE_OLD_DTD_LOCATION), t);
            }
        } else if ((m_cms != null) && systemId.startsWith(OPENCMS_SCHEME)) {

            // opencms:// VFS reference
            String cacheSystemId = systemId.substring(OPENCMS_SCHEME.length() - 1);
            String cacheKey = getCacheKey(cacheSystemId, m_cms.getRequestContext().currentProject().isOnlineProject());
            // look up temporary cache
            content = (byte[])m_cacheTemporary.get(cacheKey);
            if (content != null) {
                return new InputSource(new ByteArrayInputStream(content));
            }
            try {
                // content not cached, read from VFS
                m_cms.getRequestContext().saveSiteRoot();
                m_cms.getRequestContext().setSiteRoot("/");
                CmsFile file = m_cms.readFile(cacheSystemId);
                content = file.getContents();
                // store content in cache
                m_cacheTemporary.put(cacheKey, content);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_ER_CACHED_SYS_ID_1, cacheKey));
                }
                return new InputSource(new ByteArrayInputStream(content));
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_ENTITY_RESOLVE_FAILED_1, systemId), t);
            } finally {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        } else if (systemId.substring(0, systemId.lastIndexOf("/") + 1).equalsIgnoreCase(CmsConfigurationManager.C_DEFAULT_DTD_PREFIX)) {
            
            // default DTD location in the org.opencms.configuration package
            String location = null;
            try {
                String dtdFilename = systemId.substring(systemId.lastIndexOf("/") + 1);
                location = CmsConfigurationManager.C_DEFAULT_DTD_LOCATION + dtdFilename;
                InputStream stream = getClass().getClassLoader().getResourceAsStream(location);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
                for (int b = stream.read(); b > -1; b = stream.read()) {
                    bytes.write(b);
                }
                content = bytes.toByteArray();
                // cache the DTD
                m_cachePermanent.put(systemId, content);
                return new InputSource(new ByteArrayInputStream(content));
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_DTD_NOT_FOUND_1, location), t);
            }            
            
        }

        // use the default behaviour (i.e. resolve through external URL)
        return null;
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
        boolean project = (m_cms != null) ? m_cms.getRequestContext().currentProject().isOnlineProject() : false;

        // remove opencms:// prefix
        if (systemId.startsWith(OPENCMS_SCHEME)) {
            systemId = systemId.substring(OPENCMS_SCHEME.length() - 1);
        }

        return getCacheKey(systemId, project);
    }

    /**
     * Uncaches a system id (filename) from the internal offline temporary and content defintions caches.<p>
     * 
     * The online resources cached for the online project are only flushed when a project is published.<p>
     * 
     * @param systemId the system id (filename) to uncache
     */
    private void uncacheSystemId(String systemId) {

        Object o;
        o = m_cacheTemporary.remove(getCacheKey(systemId, false));
        if ((null != o) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ER_UNCACHED_SYS_ID_1, getCacheKey(systemId, false)));
        }
        o = m_cacheContentDefinitions.remove(getCacheKey(systemId, false));
        if ((null != o) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ER_UNCACHED_CONTENT_DEF_1, getCacheKey(systemId, false)));
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/CmsXmlEntityResolver.java,v $
 * Date   : $Date: 2004/06/13 23:43:31 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities (e.g. external DTDs) in the OpenCms VFS.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $ 
 */
public class CmsXmlEntityResolver implements EntityResolver, I_CmsEventListener {

    /** The scheme to identify a file in the OpenCms VFS. */
    public static final String C_OPENCMS_SCHEME = "opencms://";

    /** The location of the xmlpage DTD. */
    private static final String C_XMLPAGE_DTD_LOCATION = "org/opencms/xml/page/xmlpage.dtd";
    
    /** The (old) DTD address of the OpenCms xmlpage (used until 5.3.5). */
    public static final String C_XMLPAGE_DTD_OLD_SYSTEM_ID = "/system/shared/page.dtd";    

    /** A permanent cache to avoid multiple readings of often used files from the VFS. */
    private static Map m_cachePermanent;
    
    /** A temporary cache to avoid multiple readings of often used files from the VFS. */
    private static Map m_cacheTemporary;
    
    /** The static default entity resolver for reading / writing xml content. */
    private static CmsXmlEntityResolver m_resolver;        

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
        
        if (m_resolver == null) {
            m_resolver = new CmsXmlEntityResolver();
        }
        
        m_cms = cms;
    }
    
    /**
     * Initializes the caches and registers the event handler.<p>
     */
    private CmsXmlEntityResolver() {

        LRUMap lruMap = new LRUMap(128);
        m_cacheTemporary = Collections.synchronizedMap(lruMap);
        
        HashMap hashMap = new HashMap(32);
        m_cachePermanent = Collections.synchronizedMap(hashMap);
        
        // required for unit tests where no OpenCms is available
        if (OpenCms.getRunLevel() > 1) {
            
            m_cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());  
            
            if ((OpenCms.getMemoryMonitor() != null) && OpenCms.getMemoryMonitor().enabled()) {
                // map must be of type "LRUMap" so that memory monitor can acecss all information
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_cacheTemporary", lruMap);
                // map must be of type "HashMap" so that memory monitor can acecss all information
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_cachePermanent", hashMap);
            }
            // register this object as event listener
            OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_DELETED                
            });            
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
        
        if (m_resolver == null) {
            m_resolver = new CmsXmlEntityResolver();
        }
        
        m_cachePermanent.put(systemId, content);
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
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Xml entity resolver flushed caches after recieving clearcache event");
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                resource = (CmsResource)event.getData().get("resource");
                uncacheSystemId(resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                List resources = (List)event.getData().get("resources");
                for (int i=0; i<resources.size(); i++) {
                    resource = (CmsResource)resources.get(i);
                    uncacheSystemId(resource.getRootPath());
                }            
                break;
            default:
                // no operation
        }
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
        } else if (systemId.equals(CmsXmlPage.C_XMLPAGE_DTD_SYSTEM_ID) || systemId.endsWith(C_XMLPAGE_DTD_OLD_SYSTEM_ID)) {

            // XML page DTD reference
            try {                
                InputStream stream = getClass().getClassLoader().getResourceAsStream(C_XMLPAGE_DTD_LOCATION);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);                
                for (int b=stream.read(); b>-1; b=stream.read()) {
                    bytes.write(b);
                }
                content = bytes.toByteArray();
                // cache the XML page DTD
                m_cachePermanent.put(systemId, content);                
                return new InputSource(new ByteArrayInputStream(content));
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Did not find CmsXmlPage DTD at " + C_XMLPAGE_DTD_LOCATION, t);
            }
        } else if ((m_cms != null) && systemId.startsWith(C_OPENCMS_SCHEME)) {

            // opencms:// VFS reference
            String filename = systemId.substring(C_OPENCMS_SCHEME.length()-1);
            String cacheKey = getCacheKey(filename, m_cms.getRequestContext().currentProject().isOnlineProject());
            // look up temporary cache
            content = (byte[])m_cacheTemporary.get(cacheKey);
            if (content != null) {
                return new InputSource(new ByteArrayInputStream(content));
            } 
            try {      
                // content not cached, read from VFS
                m_cms.getRequestContext().saveSiteRoot();
                m_cms.getRequestContext().setSiteRoot("/");
                CmsFile file = m_cms.readFile(filename);                    
                content = file.getContents();
                // store content in cache
                m_cacheTemporary.put(cacheKey, content);
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Xml entity resolver cached " + cacheKey);
                }           
                return new InputSource(new ByteArrayInputStream(content));                
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Could not resolve OpenCms xml entity reference '" + systemId + "'", t);
            } finally {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        }
        
        // use the default behaviour (i.e. resolve through external URL)
        return null;
    }
    
    /**
     * Returns a cache key for the given filename based on the status 
     * of the internal CmsObject.<p>
     * 
     * @param filename the filename to get the cache key for
     * @param online indicates if this key is generated for the online project
     * @return the cache key for the filename
     */
    private String getCacheKey(String filename, boolean online) {
        if (online) {
            return "online:".concat(filename);
        } 
        return "offline:".concat(filename);
    }

    /**
     * Uncaches a sytem id URL from the internal cache.<p>
     * 
     * @param systemId the system id to uncache
     */
    private void uncacheSystemId(String systemId) {
        
        Object o = m_cacheTemporary.remove(getCacheKey(systemId, false));
        if ((null != o) && OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Xml entity resolver uncached " + getCacheKey(systemId, false));
        }        
    }
}
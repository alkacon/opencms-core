/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsLinkTable.java,v $
 * Date   : $Date: 2003/12/10 17:37:15 $
 * Version: $Revision: 1.3 $
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
package org.opencms.util;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteMatcher;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @version $Revision: 1.3 $ $Date: 2003/12/10 17:37:15 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsLinkTable {

    private static final String C_LINK_PREFIX = "link";
    
    private HashMap m_linkTable;
    
    
    /**
     * Class to keep a single link entry.<p>
     * 
     * @version $Revision: 1.3 $ $Date: 2003/12/10 17:37:15 $
     * @author Carsten Weinholz (c.weinholz@alkacon.com)
     */
    public class CmsLink {
        
        /** The internal name of the link */
        protected String m_name;
       
        /** The type of the link */
        protected String m_type;
        
        /** The link target (destination) */
        protected URI m_target;
        
        /** Indicates if the link is a local link within opencms */
        protected boolean m_internal;
        
        /**
         * Returns the internal name of the link.<p>
         * 
         * @return the internal name
         */
        public String getName() {
            
            return m_name;
        }
        
        /**
         * Returns the type of the link.<p>
         * 
         * @return the type of the link
         */
        public String getType() {
            
            return m_type;
        }
        
        /**
         * Returns the target (destination) of the link.<p>
         * 
         * @return the target
         */
        public String getTarget() {
            
            return m_target.toString();
        }

        /**
         * Convenience method to get a vfs link from the target.<p>
         * If the link is internal and starts with the context (i.e. /opencms/opencms),
         * the context is removed
         * 
         * @return the full link destination
         */
        public String getVfsTarget() {
        
            String context = OpenCms.getOpenCmsContext();
            if (m_internal && m_target.getPath().startsWith(context)) {
                return m_target.getPath().substring(context.length());
            } else {
                return m_target.toString();
            }
        } 
        
        /**
         * Returns if the link is internal.<p>
         * 
         * @return true if the link is a local link
         */
        public boolean isInternal() {
            
            return m_internal;
        }   
    }
    
    /**
     * Creates a new CmsLinkTable.<p>
     */
    public CmsLinkTable () {
        
        m_linkTable = new HashMap();
    }
    
    /**
     * Adds a new link to the link table.<p>
     * 
     * @param type type of the link
     * @param target link destination
     * @return the new link entry
     */
    public CmsLink addLink (String type, String target) {
        
        CmsLink link = new CmsLink();
        link.m_name = C_LINK_PREFIX + m_linkTable.size();
        link.m_type = type;
        link.m_target = URI.create(target);
        link.m_internal = isInternal(link.m_target);

        m_linkTable.put(link.m_name, link);
        return link;
    }

    /**
     * Adds a new link with a given internal name and internal flag to the link table.<p>
     * 
     * @param name the internal name of the link
     * @param type the type of the link
     * @param target the destination of the link
     * @param internal flag to indicate if the link is a local link
     * @return the new link entry
     */
    public CmsLink addLink (String name, String type, String target, boolean internal) {

        CmsLink link = new CmsLink();
        link.m_name = name;
        link.m_type = type;
        link.m_target = URI.create(target);
        link.m_internal = internal;
        
        m_linkTable.put(link.m_name, link);
        return link;
    }
    
    /**
     * Returns the CmsLink Entry for a given name.<p>
     * 
     * @param name the internal name of the link
     * @return the CmsLink entry
     */
    public CmsLink getLink (String name) {
        
        return (CmsLink)m_linkTable.get(name);
    }

    /**
     * Returns if the link table is empty.<p>
     * 
     * @return true if the link table is empty, false otherwise
     */
    public boolean isEmpty() {
        
        return m_linkTable.isEmpty();
    }
    
    /**
     * Returns an iterator over the internal names for links in the table.<p>
     * 
     * @return a string iterator for internal link names
     */
    public Iterator iterator() {
    
        return m_linkTable.keySet().iterator();
    }
    
    /**
     * Checks if a given link target is a local link within the opencms system.
     * 
     * @param targetURI the link target
     * @return true if the target is identidfied as local
     */
    private boolean isInternal(URI targetURI) {
        
        String context = OpenCms.getOpenCmsContext();
        
        int pos = context.indexOf("/", 1);
        String webapp  = (pos > 0) ? context.substring(0, pos) : context;
        
        if (targetURI.isOpaque()) {
            // an opaque uri (i.e. mailto:name@host) is not internal
            return false;
        }

        if (targetURI.isAbsolute()) {
            // an absolute uri is internal if it starts with a site root or if its part starts with the opencms context
            return OpenCms.getSiteManager().isMatching(new CmsSiteMatcher(targetURI.toString())) || targetURI.getPath().startsWith(context);
        }
        
        if (targetURI.getPath().startsWith(context)) {
            // a relative uri that starts with the opencms context (/opencms/opencms as default) is internal
            return true;
        }
        
        if (targetURI.getPath().startsWith(webapp)) {
            // a relative uri that starts with the webapp context (but not with the opencms context) is not internal
            return false;
        }    
        
        // every other relative uri is internal
        return true;
    }
}

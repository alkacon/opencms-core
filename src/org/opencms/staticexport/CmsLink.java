/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsLink.java,v $
 * Date   : $Date: 2004/06/14 15:50:09 $
 * Version: $Revision: 1.9 $
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
package org.opencms.staticexport;

import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringSubstitution;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A single link entry in the link table.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @version $Revision: 1.9 $ 
 */
public class CmsLink {
    
    /** The anchor of the uri, if any. */
    private String m_anchor;
    
    /** Indicates if the link is an internal link within the OpenCms VFS. */
    private boolean m_internal;

    /** The internal name of the link. */
    private String m_name;
    
    /** The parameters of the query , if any. */
    private Map m_parameters;
    
    /** The query, if any. */
    private String m_query;
    
    /** The link target (destination). */
    private String m_target;

    /** The type of the link. */
    private String m_type;
    
    /** The raw uri. */
    private String m_uri;
    
    /**
     * Creates a new link object.<p>
     * 
     * @param name the internal name of this link
     * @param type the type of this link
     * @param uri the link uri
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(String name, String type, String uri, boolean internal) {
        
        m_name = name;
        m_type = type;
        m_uri = uri;
        m_internal = internal;
        
        String components[] = split(uri);
        if (components != null) {
            m_target = components[0];
            m_anchor = components[1];
            m_query  = components[2];
        } else {
            m_target = uri;
            m_anchor = null;
            m_query  = null;
        }

        if (m_query != null) {
            m_parameters = getParameters(m_query);
        } else {
            m_parameters = new HashMap();
        }
    }

    /**
     * Creates a new link object.<p>
     * 
     * @param name the internal name of this link
     * @param type the type of this link
     * @param target the link target (without anchor/query)
     * @param anchor the anchor or null 
     * @param query the query or null
     * @param internal indicates if the link is internal within OpenCms 
     */
    public CmsLink(String name, String type, String target, String anchor, String query, boolean internal) {
        
        m_name = name;
        m_type = type;
        m_target = target;
        m_anchor = anchor;
        m_query = query;
        m_internal = internal;
        
        if (m_query != null) {
            m_parameters = getParameters(m_query);
        } else {
            m_parameters = new HashMap(); 
        }
        
        m_uri = m_target 
            + ((m_query!=null)  ? "?" + m_query  : "")
            + ((m_anchor!=null) ? "#" + m_anchor : "");
    }

    /**
     * Reads the parameters of the given query into the parameter map.<p>
     * 
     * @param query the query of this link
     * @return the parameter map
     */
    private static Map getParameters(String query) {
        
        HashMap parameters = new HashMap();
        String params[] = CmsStringSubstitution.split(query, "&");
        for (int i = 0; i < params.length; i++) {
            String pair[] = CmsStringSubstitution.split(params[i], "=");
            String[] p = (String[])parameters.get(pair[0]);
            if (p == null) {
                if (pair.length > 1) {
                    p = new String[]{pair[1]};
                } else {
                    // TODO: Check what that standard API does here
                    p = new String[0];
                }
            } else {
                String[] p2 = new String[p.length+1];
                System.arraycopy(p, 0, p2, 0, p.length);
                p2[p2.length-1] = pair[1];
                p = p2;
            }
            parameters.put(pair[0], p);
        }
        
        return parameters;
    }
    
    /**
     * Splits the given uri string into its components <code>scheme://authority/path#fragment?query</code>.<p>
     * 
     * @param targetUri the uri string to split
     * @return array of component strings
     */
    private static String[] split(String targetUri) {
        
        URI uri;
        String components[] = new String[3];
        
        // malformed uri
        try {
            uri = new URI(targetUri);
            components[0] = ((uri.getScheme() != null) ? uri.getScheme() + ":" : "") + uri.getRawSchemeSpecificPart();
            components[1] = uri.getRawFragment();
            components[2] = uri.getRawQuery();
            
            if (components[0] != null) {
                int i = components[0].indexOf("?");
                if (i >= 0) {
                    components[2] = components[0].substring(i+1);
                    components[0] = components[0].substring(0, i);
                }
            }
            
            if (components[1] != null) {
                int i = components[1].indexOf("?");
                if (i >= 0) {
                    components[2] = components[1].substring(i+1);
                    components[1] = components[1].substring(0, i);
                }
            }
        } catch (Exception exc) {
            return null;
        }
        
        return components; 
    }

    /**
     * Returns the anchor of this link.<p>
     * 
     * @return the anchor or null if undefined
     */
    public String getAnchor() {
        return m_anchor;
    }
    
    /**
     * Returns the macro name of this link.<p>
     * 
     * @return the macro name name of this link
     */
    public String getName() {            
        return m_name;
    }
    
    /**
     * Returns the first parameter value for the given parameter name.<p>
     * 
     * @param name the name of the parameter
     * @return the first value for this name or <code>null</code>
     */
    public String getParameter(String name) {

        String[] p = (String[])m_parameters.get(name);
        if (p != null) {
            return p[0];
        }
        
        return null;
    }

    /**
     * Returns the map of parameters of this link.<p>
     * 
     * @return the map of parameters (<code>Map(String[])</code>)
     */
    public Map getParameterMap() {
        
        return m_parameters;
    }
    
    /**
     * Returns the set of available parameter names for this link.<p>
     * 
     * @return a <code>Set</code> of parameter names
     */
    public Set getParameterNames() {
        
        return m_parameters.keySet();
    }
    
    /**
     * Returns all parameter values for the given name.<p>
     * 
     * @param name the name of the parameter
     * @return a <code>String[]</code> of all parameter values or <code>null</code>
     */
    public String[] getParameterValues(String name) {
        
        return (String[])m_parameters.get(name);
    }
    
    /**
     * Returns the query of this link.<p>
     * 
     * @return the query or null if undefined
     */
    public String getQuery() {
        return m_query;
    }
    
    /**
     * Return the site root of the target if it is internal.<p>
     * 
     * @return the site root or null
     */
    public String getSiteRoot() {
        
        if (m_internal) {
            return CmsSiteManager.getSiteRoot(m_target);
        }
        
        return null;
    }
    
    /**
     * Returns the target (destination) of this link.<p>
     * 
     * @return the target the target (destination) of this link
     */
    public String getTarget() {            
        return m_target;
    }
    
    /**
     * Returns the type of this link.<p>
     * 
     * @return the type of this link
     */
    public String getType() {            
        return m_type;
    }

    /**
     * Returns the raw uri of this link.<p>
     * 
     * @return the uri
     */
    public String getUri() {
        return m_uri;
    }
    
    /**
     * Returns the vfs link of the target if it is internal.<p>
     * 
     * @return the full link destination or null if the link is not internal.
     */
    public String getVfsUri() {        
        
        if (m_internal) {
            String siteRoot = CmsSiteManager.getSiteRoot(m_uri);
            return m_uri.substring(siteRoot.length());
        }
        
        return null;
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
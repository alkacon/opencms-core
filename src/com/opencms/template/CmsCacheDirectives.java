
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsCacheDirectives.java,v $
* Date   : $Date: 2001/05/07 14:46:58 $
* Version: $Revision: 1.2 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.template;

import com.opencms.core.*;
import com.opencms.template.cache.CmsTimeout;
import com.opencms.file.*;
import java.util.*;

/**
 * Collection of all information about cacheability and
 * used keys.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2001/05/07 14:46:58 $
 */
public class CmsCacheDirectives implements I_CmsLogChannels {

    /** Bitfield for storing external cache properties */
    public int m_cd;

    // everthing to get the cache key
    //
    private boolean user = false;
    //
    private boolean group = false;
    //
    private Vector cacheGroups;
    //
    private boolean uri = false;
    //
    private Vector cacheParameter = null;

    //
    private Vector dynamicParameter = null;

    // the timeout object
    private CmsTimeout timeout;

    /** Flag for internal cacheable */
    public static final int C_CACHE_INTERNAL = 1;

    /** Flag for cacheable in private proxies */
    public static final int C_CACHE_PROXY_PRIVATE  = 2;

    /** Flag for cacheable in public proxies */
    public static final int C_CACHE_PROXY_PUBLIC = 4;

    /** Flag for exportable */
    public static final int C_CACHE_EXPORT = 8;

    /** Flag for streamable */
    public static final int C_CACHE_STREAM = 16;

    /**
     * Constructor for initializing all caching properties with the same boolean
     * value.
     * @param b Boolean value that should be set for all caching properties
     */
    public CmsCacheDirectives(boolean b) {
        if(b) {
            m_cd = C_CACHE_INTERNAL | C_CACHE_PROXY_PRIVATE | C_CACHE_PROXY_PUBLIC | C_CACHE_EXPORT | C_CACHE_STREAM;
        } else {
            m_cd = 0;
        }
    }

    /**
     * Constructor for initializing all caching properties given boolean
     * values.
     * @param internal Initial value for "internal cacheable" property.
     * @param proxyPriv Initial value for "proxy private cacheable" property.
     * @param proxyPub Initial value for "internal cacheable" property.
     * @param export Initial value for "exportable" property.
     * @param stream Initial value for "streamable" property.
     */
    public CmsCacheDirectives(boolean internal, boolean proxyPriv, boolean proxyPub, boolean export, boolean stream) {
        m_cd = 0;
        m_cd |= internal?C_CACHE_INTERNAL:0;
        m_cd |= proxyPriv?C_CACHE_PROXY_PRIVATE:0;
        m_cd |= proxyPub?C_CACHE_PROXY_PUBLIC:0;
        m_cd |= export?C_CACHE_EXPORT:0;
        m_cd |= stream?C_CACHE_STREAM:0;
    }

    /**
     * Merge the current CmsCacheDirective object with another cache directive.
     * Resulting properties will be build by a conjunction (logical AND) of
     * the two source properties.
     * @param cd CmsCacheDirectives to be merged.
     */
    public void merge(CmsCacheDirectives cd) {
        m_cd &= cd.m_cd;
    }

    /**
     * Get the state of the "internal cacheable" property.
     * @return <code>true</code> if internal caching is possible, <code>false</code> otherwise.
     */
    public boolean isInternalCacheable() {
        return (m_cd & C_CACHE_INTERNAL) == C_CACHE_INTERNAL;
    }

    /**
     * Get the state of the "proxy private cacheable" property.
     * @return <code>true</code> if proxy private caching is possible, <code>false</code> otherwise.
     */
    public boolean isProxyPrivateCacheable() {
        return (m_cd & C_CACHE_PROXY_PRIVATE) == C_CACHE_PROXY_PRIVATE;
    }

    /**
     * Get the state of the "proxy public cacheable" property.
     * @return <code>true</code> if proxy public caching is possible, <code>false</code> otherwise.
     */
    public boolean isProxyPublicCacheable() {
        return (m_cd & C_CACHE_PROXY_PUBLIC) == C_CACHE_PROXY_PUBLIC;
    }

    /**
     * Get the state of the "exporting ability" property.
     * @return <code>true</code> if exporting is possible, <code>false</code> otherwise.
     */
    public boolean isExportable() {
        return (m_cd & C_CACHE_EXPORT) == C_CACHE_EXPORT;
    }

    /**
     * Get the state of the "streaming ability" property.
     * @return <code>true</code> if streaming is possible, <code>false</code> otherwise.
     */
    public boolean isStreamable() {
        return (m_cd & C_CACHE_STREAM) == C_CACHE_STREAM;
    }

    /**
     * Get the timeout object(used if the element should be reloaded every x minutes.
     * @return timeout object.
     */
    public CmsTimeout getTimeout() {
        return timeout;
    }

    /**
     * calculates the cacheKey for the element.
     * @return The cache key or null if it is not cacheable
     */
    public String getCacheKey(CmsObject cms, Hashtable parameters) {

        if ( ! this.isInternalCacheable()){
            return null;
        }
        if (parameters == null){
            parameters = new Hashtable();
        }
        // first test the parameters which say it is dynamic
        if((dynamicParameter != null) && (!dynamicParameter.isEmpty())){
            for (int i=0; i<dynamicParameter.size(); i++){
                String testparameter = (String)dynamicParameter.elementAt(i);
                if(parameters.containsKey(testparameter)){
                    return null;
                }
            }
        }
        CmsRequestContext reqContext = cms.getRequestContext();
        String groupKey = "";
        if(group){
            groupKey = reqContext.currentGroup().getName();
            if((cacheGroups != null) && (!cacheGroups.isEmpty())){
                if(!cacheGroups.contains(groupKey)){
                    return null;
                }
            }
        }

        // ok, a cachekey exists. lets put it together
        String key = "";
        if(uri){
            key += reqContext.getUri();
        }
        if(user){
            key += reqContext.currentUser().getName();
        }
        key += groupKey;
        if((cacheParameter != null) && ( !cacheParameter.isEmpty())){
            for (int i=0; i<cacheParameter.size(); i++){
                String para = (String)cacheParameter.elementAt(i);
                if (parameters.containsKey(para)){
                    key += (String)parameters.get(para);
                }
            }
        }
        if(key.equals("")){
            return null;
        }
        return key;
    }

    /**
     *
     */
    public void setCacheGroups(boolean groupCache){
        group = groupCache;
    }

    /**
     *
     */
    public void setCacheGroups(Vector groupNames){
        group = true;
        cacheGroups = groupNames;
    }

    /**
     *
     */
    public void setCacheUser(boolean userCache){
        user = userCache;
    }

    /**
     *
     */
    public void setCacheUri(boolean uriCache){
        uri = uriCache;
    }

    /**
     *
     */
    public void setCacheParameters(Vector parameterNames){
        cacheParameter = parameterNames;
    }

}

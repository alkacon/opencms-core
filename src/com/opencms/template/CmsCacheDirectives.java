
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsCacheDirectives.java,v $
* Date   : $Date: 2001/05/18 10:11:07 $
* Version: $Revision: 1.7 $
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
 * @version $Revision: 1.7 $ $Date: 2001/05/18 10:11:07 $
 */
public class CmsCacheDirectives implements I_CmsLogChannels {

    /** Bitfield for storing external cache properties */
    public int m_cd;

    // indicates if the external cache properties should be automaticlay changed
    private boolean m_changeCd = true;

    // everthing to get the cache key

    // indicates if the username is part of the cache key
    private boolean m_user = false;
    // indicates if the groupname is part of the cache key
    private boolean m_group = false;
    //the groupnames for which the element is cacheable
    private Vector m_cacheGroups;
    //indicates if the uri is part of the cache key
    private boolean m_uri = false;
    //the parameters for which the element is cacheable
    private Vector m_cacheParameter = null;

    // if one of these parameters occures the element is dynamic
    private Vector m_dynamicParameter = null;

    // the timeout object
    private CmsTimeout m_timeout;
    boolean m_timecheck = false;

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

        m_changeCd = false;
    }

    /**
     * Constructor
     * @param internal Initial value for "internal cacheable" property.
     * @param stream Initial value for "streamable" property.
     */
    public CmsCacheDirectives(boolean internal, boolean stream) {
        setExternalCaching(internal, true, true, true, stream);
        autoSetExternalCache();
    }

    /**
     * Method for setting all caching properties given boolean
     * values.
     * @param internal Initial value for "internal cacheable" property.
     * @param proxyPriv Initial value for "proxy private cacheable" property.
     * @param proxyPub Initial value for "internal cacheable" property.
     * @param export Initial value for "exportable" property.
     * @param stream Initial value for "streamable" property.
     */
    public void setExternalCaching(boolean internal, boolean proxyPriv, boolean proxyPub, boolean export, boolean stream) {
        m_cd = 0;
        m_cd |= internal?C_CACHE_INTERNAL:0;
        m_cd |= proxyPriv?C_CACHE_PROXY_PRIVATE:0;
        m_cd |= proxyPub?C_CACHE_PROXY_PUBLIC:0;
        m_cd |= export?C_CACHE_EXPORT:0;
        m_cd |= stream?C_CACHE_STREAM:0;

        m_changeCd = false;
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
        return m_timeout;
    }

    public boolean isTimeCritical(){
        return m_timecheck;
    }
    /**
     * set the timeout object(used if the element should be reloaded every x minutes.
     */
    public void setTimeout(CmsTimeout timeout) {
        m_timecheck = true;
        m_timeout = timeout;
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
        if((m_dynamicParameter != null) && (!m_dynamicParameter.isEmpty())){
            for (int i=0; i < m_dynamicParameter.size(); i++){
                String testparameter = (String)m_dynamicParameter.elementAt(i);
                if(parameters.containsKey(testparameter)){
                    return null;
                }
            }
        }
        CmsRequestContext reqContext = cms.getRequestContext();
        String groupKey = "";
        if(m_group){
            groupKey = reqContext.currentGroup().getName();
            if((m_cacheGroups != null) && (!m_cacheGroups.isEmpty())){
                if(!m_cacheGroups.contains(groupKey)){
                    return null;
                }
            }
        }

        // ok, a cachekey exists. lets put it together
        String key = "key_";
        if(m_uri){
            key += reqContext.getUri();
        }
        if(m_user){
            key += reqContext.currentUser().getName();
        }
        key += groupKey;
        if((m_cacheParameter != null) && ( !m_cacheParameter.isEmpty())){
            for (int i=0; i < m_cacheParameter.size(); i++){
                String para = (String)m_cacheParameter.elementAt(i);
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
        m_group = groupCache;
    }

    /**
     *
     */
    public void setCacheGroups(Vector groupNames){
        m_group = true;
        m_cacheGroups = groupNames;
    }

    /**
     *
     */
    public void setCacheUser(boolean userCache){
        m_user = userCache;
        autoSetExternalCache();
    }

    /**
     *
     */
    public void setCacheUri(boolean uriCache){
        m_uri = uriCache;
        autoSetExternalCache();
    }

    /**
     *
     */
    public void setCacheParameters(Vector parameterNames){
        m_cacheParameter = parameterNames;
        autoSetExternalCache();
    }

    /**
     *
     */
    public void setNoCacheParameters(Vector parameterNames){
        m_dynamicParameter = parameterNames;
    }

    /**
     *
     */
    private void autoSetExternalCache(){
        if (m_changeCd){
            boolean proxPriv = m_uri && (m_cacheParameter == null || m_cacheParameter.isEmpty())
                                && isInternalCacheable();
            boolean proxPubl = proxPriv && m_user;
            // ToDo: check the internal flag for export
            boolean export = proxPubl; // && !flag(extenal);
            setExternalCaching(isInternalCacheable(), proxPriv, proxPubl, export, isStreamable());
        }
    }
}

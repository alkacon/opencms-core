/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/cache/Attic/CmsFlexController.java,v $
 * Date   : $Date: 2003/05/13 12:44:54 $
 * Version: $Revision: 1.1 $
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
package com.opencms.flex.cache;

import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for getting access to the CmsObject, should be used as a 
 * request attribute.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class CmsFlexController {
    
    /** Constant for the controller request attribute name */
    public static final String ATTRIBUTE_NAME = "__com.opencms.flex.cache.CmsFlexController";

    /** The wrapped CmsObject provides JSP with access to the core system */
    private CmsObject m_cmsObject;
    
    /** The CmsFlexCache where the result will be cached in, required for the dispatcher */    
    private CmsFlexCache m_cache;   

    /** The CmsFile that was initialized by the original request, required for URI actions */    
    private CmsFile m_file;   
        
    /** Wrapped top request */
    private HttpServletRequest m_req;
    
    /** Wrapped to response */     
    private HttpServletResponse m_res;    
        
    /** List of wrapped CmsFlexRequests */
    private List m_flexRequestList;
    
    /** List of wrapped CmsFlexResponses */
    private List m_flexResponseList;
        
    /**
     * Default constructor.<p>
     * 
     * @param cms the initial CmsObject to wrap in the controller
     */
    public CmsFlexController(
        CmsObject cms, 
        CmsFile file, 
        CmsFlexCache cache, 
        HttpServletRequest req, 
        HttpServletResponse res
    ) {
        m_cmsObject = cms;
        m_file = file;
        m_cache = cache;
        m_req = req;
        m_res = res;
        m_flexRequestList = Collections.synchronizedList(new ArrayList());
        m_flexResponseList = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Returns the wrapped CmsObject.<p>
     * 
     * @return the wrapped CmsObject
     */
    public CmsObject getCmsObject() {
        return m_cmsObject;
    }
    
    /**
     * Returns the wrapped CmsObject form the provided request, or null if the 
     * request is not running inside OpenCms.<p>
     * 
     * @return the wrapped CmsObject
     */    
    public static CmsObject getCmsObject(ServletRequest req) {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getCmsObject();
        } else {
            return null;
        }
    }
    
    /**
     * Checks if the provided request is running in OpenCms.<p>
     *
     * @return true if the request is running in OpenCms, false otherwise
     */
    public static boolean isCmsRequest(ServletRequest req) {
        return ((req != null) && (req.getAttribute(ATTRIBUTE_NAME) != null));
    }
    
    /**
     * Returns the CmsFlexCache instance where all results from this request will be cached in.<p>
     * 
     * This is public so that pages like the Flex Cache Administration page
     * have a way to access the cache object.<p>
     *
     * @return the CmsFlexCache instance where all results from this request will be cached in
     */    
    public CmsFlexCache getCmsCache() {
        return m_cache;
    }
    
    /** 
     * This method provides access to the top-level CmsFile of the request
     * which is of a type that supports the FlexCache,
     * i.e. usually the CmsFile that is identical to the file uri requested by the user,
     * not he current included element.<p>
     *
     * In case a JSP is used as a sub-element in a XMLTemplate,
     * this method will not return the top-level uri but
     * the "topmost" file of a type that is supported by the FlexCache.
     * In case you need the top uri, use
     * getCmsObject().getRequestContext().getUri().
     *
     * @return the requested top-level CmsFile
     */    
    public CmsFile getCmsFile() {
        return m_file;
    }     
    
    public CmsFlexRequest getCurrentRequest() {
        return (CmsFlexRequest)m_flexRequestList.get(m_flexRequestList.size()-1);
    }  
    
    public void pushRequest(CmsFlexRequest req) {
        m_flexRequestList.add(req);
    }
    
    public CmsFlexRequest popRequest() {
        CmsFlexRequest result = null;
        if (m_flexRequestList.size() > 0) {
            result = getCurrentRequest();
            m_flexRequestList.remove(m_flexRequestList.size()-1);            
        }
        return result;
    }
    
    public CmsFlexResponse getCurrentResponse() {
        return (CmsFlexResponse)m_flexResponseList.get(m_flexResponseList.size()-1);
    }
    
    public void pushResponse(CmsFlexResponse res) {
        m_flexResponseList.add(res);
    }
    
    public CmsFlexResponse popResponse() {
        CmsFlexResponse result = null;
        if (m_flexResponseList.size() > 0) {
            result = getCurrentResponse();
            m_flexResponseList.remove(m_flexResponseList.size()-1);            
        }
        return result;
    }    
    
    public void suspendFlexResponse() {
        Iterator i = m_flexResponseList.iterator();
        while (i.hasNext()) {
            CmsFlexResponse res = (CmsFlexResponse)i.next();
            res.setSuspended(true);
        }
    }
    
    public int getResponseQueueSize() {
        return m_flexResponseList.size();
    }
    
    public HttpServletRequest getTopRequest() {
        return m_req;
    }
    
    public HttpServletResponse getTopResponse() {
        return m_res;
    }
}

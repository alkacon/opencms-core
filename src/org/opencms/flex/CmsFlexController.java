/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexController.java,v $
 * Date   : $Date: 2004/02/20 12:45:54 $
 * Version: $Revision: 1.5 $
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
package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;

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
 * @version $Revision: 1.5 $
 */
public class CmsFlexController {
    
    /** Constant for the controller request attribute name */
    public static final String ATTRIBUTE_NAME = "__com.opencms.flex.cache.CmsFlexController";
    
    /** The CmsFlexCache where the result will be cached in, required for the dispatcher */    
    private CmsFlexCache m_cache;   

    /** The wrapped CmsObject provides JSP with access to the core system */
    private CmsObject m_cmsObject;
        
    /** List of wrapped CmsFlexRequests */
    private List m_flexRequestList;
    
    /** List of wrapped CmsFlexResponses */
    private List m_flexResponseList;
        
    /** Wrapped top request */
    private HttpServletRequest m_req;
    
    /** Wrapped top response */     
    private HttpServletResponse m_res;    

    /** The CmsResource that was initialized by the original request, required for URI actions */    
    private CmsResource m_resource;   
    
    /** Exception that was caught during inclusion of sub elements */
    private Throwable m_throwable;
    
    /** URI of a VFS resource that caused the exception */
    private String m_throwableResourceUri;
    
    /**
     * Default constructor.<p>
     * 
     * @param cms the initial CmsObject to wrap in the controller
     * @param file the file requested 
     * @param cache the instance of the flex cache
     * @param req the current request
     * @param res the current response
     */
    public CmsFlexController(
        CmsObject cms, 
        CmsResource file, 
        CmsFlexCache cache, 
        HttpServletRequest req, 
        HttpServletResponse res
    ) {
        m_cmsObject = cms;
        m_resource = file;
        m_cache = cache;
        m_req = req;
        m_res = res;
        m_flexRequestList = Collections.synchronizedList(new ArrayList());
        m_flexResponseList = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Returns the wrapped CmsObject form the provided request, or null if the 
     * request is not running inside OpenCms.<p>
     * 
     * @param req the current request
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
     * Provides access to a root cause Exception that might have occured in a complex inlucde scenario.<p>
     * 
     * @param req the current request
     * @return the root cause exception or null if no root cause exception is available
     * @see #getThrowable()
     */
    public static Throwable getThrowable(ServletRequest req) {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getThrowable();
        } else {
            return null;
        }
    }    
    
    /**
     * Provides access to URI of a VFS resource that caused an exception that might have occured in a complex inlucde scenario.<p>
     * 
     * @param req the current request
     * @return to URI of a VFS resource that caused an exception, or null
     * @see #getThrowableResourceUri()
     */
    public static String getThrowableResourceUri(ServletRequest req) {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            return controller.getThrowableResourceUri();
        } else {
            return null;
        }    
    }
    
    /**
     * Checks if the provided request is running in OpenCms.<p>
     *
     * @param req the current request
     * @return true if the request is running in OpenCms, false otherwise
     */
    public static boolean isCmsRequest(ServletRequest req) {
        return ((req != null) && (req.getAttribute(ATTRIBUTE_NAME) != null));
    }
    
    /**
     * Removes the controller attribute from a request.<p>
     * 
     * @param req the request to remove the controller from
     */
    public static void removeController(ServletRequest req) {
        CmsFlexController controller = (CmsFlexController)req.getAttribute(ATTRIBUTE_NAME);
        if (controller != null) {
            controller.clear();
        }
    }
    
    /**
     * Clears all data of this controller.<p>
     */
    public void clear() {
        if (m_flexRequestList != null) {
            m_flexRequestList.clear();
        }
        m_flexRequestList = null;
        if (m_flexResponseList != null) {
            m_flexResponseList.clear();
        }
        m_flexResponseList = null;
        if (m_req != null) {
            m_req.removeAttribute(ATTRIBUTE_NAME);
        }
        m_req = null;
        m_res = null;
        m_cmsObject = null;
        m_resource = null;
        m_cache = null;
        m_throwable = null;
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
     * Returns the wrapped CmsObject.<p>
     * 
     * @return the wrapped CmsObject
     */
    public CmsObject getCmsObject() {
        return m_cmsObject;
    }
    
    /** 
     * This method provides access to the top-level CmsResource of the request
     * which is of a type that supports the FlexCache,
     * i.e. usually the CmsFile that is identical to the file uri requested by the user,
     * not he current included element.<p>
     * 
     * @return the requested top-level CmsFile
     */    
    public CmsResource getCmsResource() {
        return m_resource;
    }     
    
    /**
     * Returns the current flex request.<p>
     * 
     * @return the current flex request
     */
    public CmsFlexRequest getCurrentRequest() {
        return (CmsFlexRequest)m_flexRequestList.get(m_flexRequestList.size()-1);
    }  
    
    /**
     * Returns the current flex response.<p>
     * 
     * @return the current flex response
     */
    public CmsFlexResponse getCurrentResponse() {
        return (CmsFlexResponse)m_flexResponseList.get(m_flexResponseList.size()-1);
    }
    
    /**
     * Returns the size of the response stack.<p>
     * 
     * @return the size of the response stack
     */
    public int getResponseStackSize() {
        return m_flexResponseList.size();
    }
    
    /**
     * Returns an exception (Throwable) that was caught during inclusion of sub elements.<p>
     * 
     * @return an exception (Throwable) that was caught during inclusion of sub elements
     */
    public Throwable getThrowable() {
        return m_throwable;
    }
        
    /**
     * Returns the URI of a VFS resource that caused the exception that was caught during inclusion of sub elements,
     * might return null if no URI information was available for the exception.<p>
     * 
     * @return the URI of a VFS resource that caused the exception that was caught during inclusion of sub elements
     */
    public String getThrowableResourceUri() {
        return m_throwableResourceUri;
    }
    
    /**
     * Returns the current http request.<p>
     * 
     * @return the current http request
     */
    public HttpServletRequest getTopRequest() {
        return m_req;
    }
    
    /**
     * Returns the current http response.<p>
     * 
     * @return the current http response
     */
    public HttpServletResponse getTopResponse() {
        return m_res;
    }
    
    /**
     * Returns the topmost request from the stack.<p>
     * 
     * @return the topmost request from the stack
     */
    public CmsFlexRequest popRequest() {
        CmsFlexRequest result = null;
        if (m_flexRequestList.size() > 0) {
            result = getCurrentRequest();
            m_flexRequestList.remove(m_flexRequestList.size()-1);            
        }
        return result;
    }
    
    /**
     * Returns the topmost response from the stack.<p>
     * 
     * @return the topmost response from the stack
     */
    public CmsFlexResponse popResponse() {
        CmsFlexResponse result = null;
        if (m_flexResponseList.size() > 0) {
            result = getCurrentResponse();
            m_flexResponseList.remove(m_flexResponseList.size()-1);            
        }
        return result;
    }    
    
    /**
     * Adds another flex request to the stack.<p>
     * 
     * @param req the request to add
     */
    public void pushRequest(CmsFlexRequest req) {
        m_flexRequestList.add(req);
    }
    
    /**
     * Adds another flex response to the stack.<p>
     * 
     * @param res the response to add
     */    
    public void pushResponse(CmsFlexResponse res) {
        m_flexResponseList.add(res);
    }
    
    /**
     * Sets an exception (Throwable) that was caught during inclusion of sub elements.<p>
     * 
     * If another exception is already set in this controller, then the additional exception
     * is ignored.
     * 
     * @param throwable the exception (Throwable) to set
     * @param resource the URI of the VFS resource the error occured on (might be null if unknown)
     * @return the exception stored in the contoller
     */
    public Throwable setThrowable(Throwable throwable, String resource) {
        if (m_throwable == null) {
            m_throwable = throwable;
            m_throwableResourceUri = resource;
        } else {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Ignored additional exception" + ((resource!=null)?" on resource " + resource:""), throwable);
            }
        }
        return m_throwable;
    }
    
    /**
     * Puts the response in a suspended state.<p>  
     */
    public void suspendFlexResponse() {
        Iterator i = m_flexResponseList.iterator();
        while (i.hasNext()) {
            CmsFlexResponse res = (CmsFlexResponse)i.next();
            res.setSuspended(true);
        }
    }
}

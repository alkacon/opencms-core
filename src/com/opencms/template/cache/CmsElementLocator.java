/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementLocator.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.15 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * The ElementLocator is used to receive CmsElement-Objects. It is the Cache for
 * these CmsElement-Objects. The CmsElement-Objects are stored in memory or -
 * if they are notc used a long time - written to an external database. The
 * locator manages all the reading, writing and management of the CmsElement's.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public class CmsElementLocator implements com.opencms.boot.I_CmsLogChannels {

    /**
     * A hashtable to store the elements.
     */
    private CmsLruCache m_elements;

    /**
     * The default constructor for this locator.
     */
    CmsElementLocator(int cacheSize) {
        if(cacheSize < 2){
            cacheSize = 50000;
        }
        m_elements = new CmsLruCache(cacheSize);
    }

    /**
     * Adds a new Element to this locator.
     * This method is kept private and must not be used from outside.
     * New elements automatically are generated and stored by the Locator,
     * so no one really needs to use this method.
     * @param descriptor - the descriptor for this element.
     * @param element - the Element to put in this locator.
     */
    private void put(CmsElementDescriptor desc, A_CmsElement element) {
        m_elements.put(desc, element);
    }

    /**
     * Gets a Elements from this locator.
     * @param desc - the descriptor to locate the element.
     * @returns the element that was found.
     */
    public A_CmsElement get(CmsObject cms, CmsElementDescriptor desc, Hashtable parameters) {
        A_CmsElement result;
        result = (A_CmsElement)m_elements.get(desc);
        if(result == null) {
            // the element was not found in the element cache
            // we have to generate it
            I_CmsTemplate cmsTemplate = null;
            // look if it is an methode element
            if("METHOD".equals(desc.getTemplateName())){
                String orgClassName = desc.getClassName();
                String className = orgClassName.substring(0,orgClassName.lastIndexOf("."));
                String methodName = orgClassName.substring(orgClassName.lastIndexOf(".")+1);
                try {
                    cmsTemplate = (I_CmsTemplate)com.opencms.template.CmsTemplateClassManager.getClassInstance(cms, className);
                    CmsMethodCacheDirectives mcd = (CmsMethodCacheDirectives)cmsTemplate.getClass().getMethod(
                                                    "getMethodCacheDirectives", new Class[] {
                                                    CmsObject.class, String.class}).invoke(cmsTemplate,
                                                    new Object[] {cms, methodName});;
                    result = new CmsMethodElement(className, methodName, mcd,
                             cms.getRequestContext().getElementCache().getVariantCachesize());
                    put(desc, result);
                } catch(Throwable e) {
                    if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Could not initialize method element for class \"" + className  + "\". ");
                        A_OpenCms.log(C_OPENCMS_CRITICAL, e.toString());
                        return null;
                    }
                }
            }else{
                try {
                    cmsTemplate = (I_CmsTemplate)com.opencms.template.CmsTemplateClassManager.getClassInstance(cms, desc.getClassName());
                    result = cmsTemplate.createElement(cms, desc.getTemplateName(), parameters);
                    put(desc, result);
                } catch(Throwable e) {
                    if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, toString() + " Could not initialize (sub-)element for class \"" + desc.getClassName() + "\". ");
                        A_OpenCms.log(C_OPENCMS_CRITICAL, e.toString());
                        return null;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the Information of max size and size for the cache.
     *
     * @return a Vector whith informations about the size of the cache.
     */
    public Vector getCacheInfo(){
        return m_elements.getCacheInfo();
    }

    /**
     * deletes all elements in the cache that depend on one of the invalid Templates.
     * @param invalidTemplates A vector with the ablolute path of the templates (String)
     */
    public void cleanupElementCache(Vector invalidTemplates){
        m_elements.deleteElementsAfterPublish();
        for(int i=0; i < invalidTemplates.size(); i++){
            m_elements.deleteElementsByTemplate((String)invalidTemplates.elementAt(i)) ;
        }
    }

    /**
     * Clears the cache compleatly.
     */
    public void clearCache(){
        m_elements.clearCache();
    }
}
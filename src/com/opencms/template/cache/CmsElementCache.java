/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementCache.java,v $
* Date   : $Date: 2003/07/11 21:35:49 $
* Version: $Revision: 1.17 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the starting class for OpenCms element cache. Element cache was implemented for
 * performance issues. The idea is to create a flat hirarchie of elements that
 * can be accessed fast and efficient for the frontend users in the online
 * project.
 *
 * On publishing-time the data in the element cache area will be created or updated.
 * All inefficiant XML-files are changed to the efficient element cache data
 * structure. For createing the content no XML-parsing and DOM-accessing is
 * neccessairy.
 * @author Andreas Schouten
 */
public class CmsElementCache {

    private CmsUriLocator m_uriLocator;

    private CmsElementLocator m_elementLocator;

    private int m_variantCachesize;

    public CmsElementCache(){
        m_uriLocator = new CmsUriLocator(10000);
        m_elementLocator = new CmsElementLocator(50000);
        m_variantCachesize = 100;
    }

    public CmsElementCache(int uriCachesize, int elementCachesize, int variantCachesize) {
        m_uriLocator = new CmsUriLocator(uriCachesize);
        m_elementLocator = new CmsElementLocator(elementCachesize);
        if (variantCachesize < 2){
            variantCachesize = 100;
        }
        m_variantCachesize = variantCachesize;
    }

    public CmsUriLocator getUriLocator() {
        return m_uriLocator;
    }

    public CmsElementLocator getElementLocator() {
        return m_elementLocator;
    }

    /**
     * returns the size of the variant cache for each element.
     */
    public int getVariantCachesize(){
        return m_variantCachesize;
    }

    /**
     * Deletes all the content of the caches that depend on the changed resources
     * after publishProject.
     * @param changedResources A vector (of Strings) with the resources that have
     *                          changed during publishing.
     */
    public void cleanupCache(Vector changedResources, Vector changedModuleRes){

        // chanchedResources have chanched, first we have to edit them
        Vector resForUpdate = new Vector();
        if(changedResources != null){
            for(int i=0; i<changedResources.size(); i++){
                String current = (String)changedResources.elementAt(i);
                resForUpdate.add(current);
                resForUpdate.add(current + I_CmsConstants.C_XML_CONTROL_FILE_SUFFIX);
            }
        }
        m_uriLocator.deleteUris(resForUpdate);
        m_elementLocator.cleanupElementCache(resForUpdate);

        // for the dependencies cache we use the original vectors
        m_elementLocator.cleanupDependencies(changedResources);
        m_elementLocator.cleanupDependencies(changedModuleRes);
    }

    /**
     * Clears the uri and the element cache compleatly.
     * and the extern dependencies.
     */
    public void clearCache(){
        m_elementLocator.clearCache();
        m_uriLocator.clearCache();
    }

    /**
     * prints the cache info in the errorlog.
     * @param int   1: print the info for the dependencies cache.
     *              2:
     */
    public void printCacheInfo(int which){
        if(which ==1){
            // the dependencies cache
            m_elementLocator.printCacheInfo(which);
        }
    }

    /**
     * Gets the Information of max size and size for the uriCache and the
     * element cache.
     * @return a Vector whith informations about the size of the caches.
     */
    public Vector getCacheInfo(){
        Vector uriInfo = m_uriLocator.getCacheInfo();
        Vector elementInfo = m_elementLocator.getCacheInfo();
        for (int i=0; i < elementInfo.size(); i++){
            uriInfo.addElement(elementInfo.elementAt(i));
        }
        return uriInfo;
    }

    public byte[] callCanonicalRoot(CmsObject cms, Hashtable parameters) throws CmsException {
        CmsUri uri = m_uriLocator.get(new CmsUriDescriptor(cms.getRequestContext().getUri()));
        return uri.callCanonicalRoot(this, cms, parameters);
    }

    public byte[] callCanonicalRoot(CmsObject cms, Hashtable parameters, String uriParam) throws CmsException {
        CmsUri uri = m_uriLocator.get(new CmsUriDescriptor(uriParam));
        return uri.callCanonicalRoot(this, cms, parameters);
    }
}
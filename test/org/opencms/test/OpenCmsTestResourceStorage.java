/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceStorage.java,v $
 * Date   : $Date: 2004/05/28 10:52:46 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.test;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage object for storing all attributes of vfs resources.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class OpenCmsTestResourceStorage {

    /** the name of the default storage */
    public static String DEFAULT_STORAGE = "default";

    /** A CmsObject to use to access resources */
    private CmsObject m_cms;
    
    /** the name of the storage */
    private String m_name;
    
    /** storeage for precalculation of states **/
    private Map m_precalcState;

    /** Strings for mapping the filename */
    private String m_sourceNameMapping;

    /** internal storage */
    private Map m_storage;

    /** Prefix mapping for target name */
    private String m_targetNameMapping;
    
    /**
     * Creates a new OpenCmsTestResourceStorage.<p>
     * 
     * @param cms the current CmsObject
     * @param name the name of the storage
     */
    public OpenCmsTestResourceStorage(CmsObject cms, String name) {

        m_storage = new HashMap();
        m_precalcState = new HashMap();
        m_sourceNameMapping = null;
        m_targetNameMapping = null;
        m_cms = cms;
        m_name = name;
    }

    /** 
     * Adds a CmsResource to the resource storage.<p>
     * 
     * @param resourceName the resource name to add
     * @param resource the resource to add
     * @throws CmsException if something goes wrong
     */
    public void add(String resourceName, CmsResource resource) throws CmsException {

        m_storage.put(resourceName, new OpenCmsTestResourceStorageEntry(m_cms, resourceName, resource));
        m_precalcState.put(resourceName, preCalculateState(resource));
    }
    
    
    /**
     * Gets an entry from the storage.<p>
     * 
     * @param resourceName the name of the resource to get 
     * @return OpenCmsTestResourceStorageEntry with all the attributes of a CmsResource
     * @throws CmsException in case something goes wrong
     */
    public OpenCmsTestResourceStorageEntry get(String resourceName) throws CmsException {

        String mappedResourceName = mapResourcename(resourceName);

        OpenCmsTestResourceStorageEntry entry = null;
        entry = (OpenCmsTestResourceStorageEntry)m_storage.get(mappedResourceName);

        if (entry == null) {
            throw new CmsException(
                "Not found in storage " + resourceName + " -> " + mappedResourceName,
                CmsException.C_NOT_FOUND);
        }

        return entry;
    }
    
    /**
     * Gets the name of the storage.<p>
     * 
     * @return the name of the storage
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Gets an precalculate resource state from the storage.<p>
     * 
     * @param resourceName the name of the resource to get  the state
     * @return precalculated resource state
     * @throws CmsException in case something goes wrong
     */
    public int getPreCalculatedState(String resourceName) throws CmsException {
         String mappedResourceName = mapResourcename(resourceName);
         
         Integer state = null;
         state = (Integer)m_precalcState.get(mappedResourceName);
         if (state == null) {
            throw new CmsException(
                "Not found in storage " + resourceName + " -> " + mappedResourceName,
                CmsException.C_NOT_FOUND);
        }
        return state.intValue();
    }
    
    

    /**
     * Returns the source name mapping.<p>
     * 
     * @return the source name mapping
     */
    public String getSourceNameMapping() {

        return m_sourceNameMapping;
    }

    /**
     * Returns the target name mapping.<p>
     * 
     * @return the the target name mapping
     */
    public String getTargetNameMapping() {

        return m_targetNameMapping;
    }

    /**
     * Sets the mapping for resourcenames.<p>
     *
     * @param source the source resource name
     * @param target the target resource name
     */
    public void setMapping(String source, String target) {

        m_sourceNameMapping = source;
        m_targetNameMapping = target;
    }
    
    /**
     * Does the name mapping of a resourceName.<p>
     * 
     * This is required to find resources in the resource storage afer their path in the vfs
     * has changed (e.g. after a copy operation).<p>
     *
     * @param resourceName the resource name to map
     * @return mapped resource name
     */
    private String mapResourcename(String resourceName) {
        // only modify the name if we have set some kind of mapping
        if (m_sourceNameMapping != null && m_targetNameMapping != null) {
            // check if the resourcename starts with the source map name
            if (resourceName.startsWith(m_sourceNameMapping)) {
                // exchange the prefix with the target map name
                resourceName = m_targetNameMapping + resourceName.substring(m_sourceNameMapping.length());
            }
        }
        return resourceName;
    }

    
    /**
     * Precalculates the state of a resource after an operation based on its state before 
     * the operation is excecuted.<p>
     * 
     * The following states are precalculated:
     * <ul>
     * <li>Unchanged -> Changed</li>
     * <li>Changed -> Changed</li>
     * <li>New -> New</li>
     * <li>Deleted -> Deleted</li>
     * </ul>
     * @param res the resource 
     * @return new precalculated state
     */
    private Integer preCalculateState(CmsResource res) {
        int newState = I_CmsConstants.C_STATE_UNCHANGED;
        int state = res.getState();
        switch (state) {
            case I_CmsConstants.C_STATE_UNCHANGED:
                newState = I_CmsConstants.C_STATE_CHANGED;
                break;
            case I_CmsConstants.C_STATE_CHANGED:
                newState = I_CmsConstants.C_STATE_CHANGED;
                break;
            case I_CmsConstants.C_STATE_NEW:
                newState = I_CmsConstants.C_STATE_NEW;
                break;  
            case I_CmsConstants.C_STATE_DELETED:
                newState = I_CmsConstants.C_STATE_DELETED;
                break;   
            default:
                newState = I_CmsConstants.C_STATE_UNCHANGED;
                break;
        }        
        return new Integer(newState);
    }
}
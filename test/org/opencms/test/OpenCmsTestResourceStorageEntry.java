/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceStorageEntry.java,v $
 * Date   : $Date: 2004/08/25 07:47:21 $
 * Version: $Revision: 1.11 $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.security.CmsAccessControlList;
import org.opencms.util.CmsUUID;

/**
 * A single entry of the OpenCmsTestResourceStorage.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.11 $
 */
public class OpenCmsTestResourceStorageEntry {

    /** The size of the content. */
    protected int m_length;
    
    /** The ccess control list entries. */
    private Vector m_accessControlEntries;
    
    /** The access control list. */
    private CmsAccessControlList m_accessControlList;
    
       /** The ID of the content database record. */
    // private CmsUUID m_contentId;
    
    /** The content of the resource. If the resource is a folder, the content is null. */
    private byte[] m_contents;

    /** The creation date of this resource. */
    private long m_dateCreated;

    /** The expiration date of this resource. */
    private long m_dateExpired;    

    /** The date of the last modification of this resource. */
    private long m_dateLastModified;

    /** The release date of this resource. */
    private long m_dateReleased;
    
    /** The flags of this resource ( not used yet; the Accessflags are stored in m_accessFlags). */
    private int m_flags;

    /** Boolean flag whether the timestamp of this resource was modified by a touch command. */
    private boolean m_isTouched;

    /** The id of the loader which is used to process this resource. */
    private int m_loaderId;
    
    /** The lockstate of the resource. */
    private CmsLock m_lockstate;
    
    /** The name of this resource. */
    private String m_name;

    /** The project id where this resource has been last modified in. */
    private int m_projectLastModified;
    
    /** The properties of the resource. */
    private List m_properties;

    /** The ID of the resource database record. */
    private CmsUUID m_resourceId;

    /** The number of links that point to this resource. */
    private int m_siblingCount;

    /** The state of this resource. */
    private int m_state;

    /** The ID of the structure database record. */
    private CmsUUID m_structureId;

    /** The type of this resource. */
    private int m_type;
    
    /** The id of the user who created this resource. */
    private CmsUUID m_userCreated;
    
    /** The id of the user who modified this resource last. */
    private CmsUUID m_userLastModified;
    
    /**
     * Creates a new empty OpenCmsTestResourceStorageEntry.<p>
     */
    public OpenCmsTestResourceStorageEntry() {
        // noop
    }
    
    /**
     * Creates a new OpenCmsTestResourceStorageEntry.<p>
     * 
     * @param cms current CmsObject
     * @param resourceName the complete name of the resource
     * @param res the CmsResource to store.
     * @throws CmsException if something goes wrong 
     */
    public OpenCmsTestResourceStorageEntry(CmsObject cms, String resourceName, CmsResource res) throws CmsException {
        // m_contentId = res.getContentId();
        m_dateCreated = res.getDateCreated();
        m_dateLastModified = res.getDateLastModified();
        m_dateReleased = res.getDateReleased();
        m_dateExpired = res.getDateExpired();
        m_flags = res.getFlags();
        m_isTouched = res.isTouched();
        m_length = res.getLength();
        m_siblingCount = res.getSiblingCount();
        m_name = res.getName();
        m_projectLastModified = res.getProjectLastModified();
        m_resourceId = res.getResourceId();
        m_state = res.getState();
        m_structureId = res.getStructureId();
        m_type = res.getTypeId();
        m_userCreated = res.getUserCreated();
        m_userLastModified = res.getUserLastModified();     
        m_lockstate = cms.getLock(res);
        if (res.isFile()) {
            m_contents = cms.readFile(resourceName, CmsResourceFilter.ALL).getContents();
        } else {
            m_contents = null;
        }
        
        m_properties = new ArrayList();         
        List properties =  cms.readPropertyObjects(resourceName, false);    
        Iterator i = properties.iterator();   
        while (i.hasNext()) {
            CmsProperty prop = (CmsProperty)i.next();
            m_properties.add(prop.clone());
        }
        
        m_accessControlList = cms.getAccessControlList(resourceName);
        m_accessControlEntries = cms.getAccessControlEntries(resourceName);
    }
    

    /**
     * Returns the access control entries of the resource.<p>
     *
     * @return  the access control entries of the resource
     */
    public Vector getAccessControlEntries() {
        return m_accessControlEntries;
    }
    
    

    /**
     * Returns the access control list of the resource.<p>
     *
     * @return  the access control list of the resource
     */
    public CmsAccessControlList getAccessControlList() {
        return m_accessControlList;
    }
    
    /**
     * Returns the date of the creation of this resource.<p>
     *
     * @return the date of the creation of this resource
     */
    public byte[] getContents() {
        return m_contents;
    }
    
    
    /**
     * Returns the date of the creation of this resource.<p>
     *
     * @return the date of the creation of this resource
     */
    public long getDateCreated() {
        return m_dateCreated;
    }
    
    /**
     * Returns the expiration date this resource.<p>
     *
     * @return the expiration date of this resource
     */
    public long getDateExpired() {
        return m_dateExpired;
    }
    
    /**
     * Returns the date of the last modification of this resource.<p>
     *
     * @return the date of the last modification of this resource
     */
    public long getDateLastModified() {
        return m_dateLastModified;
    }
    
    /**
     * Returns the release date this resource.<p>
     *
     * @return the release date of this resource
     */
    public long getDateReleased() {
        return m_dateReleased;
    }

    /**
     * Gets the id of the file content database entry.<p>
     *
     * @return the ID of the file content database entry
     */
    /* public CmsUUID getFileId() {
        return m_contentId;
    } */

    /**
     * Returns the flags of this resource.<p>
     *
     * @return the flags of this resource
     */
    public int getFlags() {
        return m_flags;
    }
    
    /**
     * Gets the length of the content (i.e. the file size).<p>
     *
     * @return the length of the content
     */
    public int getLength() {
        return m_length;
    }
    
    /**
     * Gets the loader id of this resource.<p>
     *
     * @return the loader type id of this resource
     */
    public int getLoaderId() {
        return m_loaderId;
    }
    
    /**
     * Gets the lockstate of this resource.<p>
     *
     * @return the lockstate  of this resource
     */
    public CmsLock getLock() {
        return m_lockstate;
    }


    /**
     * Returns the name of this resource, e.g. <code>index.html</code>.<p>
     *
     * @return the name of this resource
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Returns the name of this resource including the full path in the current site,
     * but without the current site root.<p>
     *
     * @return the name of this resource including the full path in the current site
     */
    public String getPath() {
        // TODO: Must be implemented
        return null;
    }    

    /**
     * Returns the id of the project where the resource has been last modified.<p>
     *
     * @return the id of the project where the resource has been last modified
     */
    public int getProjectLastModified() {
        return m_projectLastModified;
    }
    

    /**
     * Returns the properties of the resource.<p>
     *
     * @return  the properties of the resource
     */
    public List getProperties() {
        return m_properties;
    }
    

    /**
     * Returns the id of the resource database entry of this resource.<p>
     *
     * @return the id of the resource database entry
     */
    public CmsUUID getResourceId() {
        return m_resourceId;
    }

    /**
     * Gets the number of references to the resource.<p>
     * 
     * @return the number of links
     */
    public int getSiblingCount() {
        return m_siblingCount;
    }

    
    /**
     * Returns the state of this resource.<p>
     *
     * This may be C_STATE_UNCHANGED, C_STATE_CHANGED, C_STATE_NEW or C_STATE_DELETED.<p>
     *
     * @return the state of this resource
     */
    public int getState() {
        return m_state;
    }

    /**
     * Returns the id of the structure record of this resource.<p>
     * 
     * @return the id of the structure record of this resource
     */
    public CmsUUID getStructureId() {
        return m_structureId;
    }
    
    /**
     * Returns the type id for this resource.<p>
     *
     * @return the type id of this resource.
     */
    public int getType() {
        return m_type;
    }

    /**
     * Returns the user id of the user who created this resource.<p>
     * 
     * @return the user id
     */
    public CmsUUID getUserCreated() {
        return m_userCreated;
    }
    
    /**
     * Returns the user id of the user who made the last change on this resource.<p>
     *
     * @return the user id of the user who made the last change<p>
     */
    public CmsUUID getUserLastModified() {
        return m_userLastModified;
    }
    
    /**
     * Returns true if this resource was touched.<p>
     * 
     * @return boolean true if this resource was touched
     */
    public boolean isTouched() {
        return m_isTouched;
    }

    
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishList.java,v $
 * Date   : $Date: 2007/03/27 14:16:25 $
 * Version: $Revision: 1.25.4.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * A container for all new/changed/deteled Cms resources that are published together.<p>
 * 
 * Only classes inside the org.opencms.db package can add or remove elements to or from this list. 
 * This allows the OpenCms API to pass the list around between classes, but with restricted access to 
 * create this list.<p>
 * 
 * To create a publish list, one of the public constructors must be used in order to set the basic operation mode
 * (project publish or direct publish).
 * After this, use <code>{@link org.opencms.db.CmsDriverManager#fillPublishList(CmsDbContext, CmsPublishList)}</code>
 * to fill the actual values of the publish list.<p>
 * 
 * @author Alexander Kandzior
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.25.4.8 $
 * 
 * @since 6.0.0
 * 
 * @see org.opencms.db.CmsDriverManager#fillPublishList(CmsDbContext, CmsPublishList)
 */
public class CmsPublishList implements Externalizable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishList.class);
    
    /** Indicates a nonexistant object in the serialized data. */
    private static final int NIL = -1;
    
    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2578909250462750927L;

    /** Lenght of a serialized uuid. */
    private static final int UUID_LENGTH = CmsUUID.getNullUUID().toByteArray().length;
    
    /** The list of deleted Cms folder resources to be published.<p> */
    private List m_deletedFolderList;

    /** The list of direct publish resources. */
    private List m_directPublishResources;

    /** The list of new/changed/deleted Cms file resources to be published.<p> */
    private List m_fileList;

    /** The list of new/changed Cms folder resources to be published.<p> */
    private List m_folderList;

    /** Flag to indicate if the list needs to be revived. */
    private boolean m_needsRevive = false;

    /** The id of the project that is to be published. */
    private CmsUUID m_projectId;

    /** The publish history ID.<p> */
    private CmsUUID m_publishHistoryId;

    /** Indicates if unpublished related resources should be published. */
    private boolean m_publishRelatedResources;

    /** Indicates if siblings of the resources in the list should also be published. */
    private boolean m_publishSiblings;

    /** Indicates if sub-resources in folders should be published (for direct publish only). */
    private boolean m_publishSubResources;

    /**
     * Empty constructor.<p> 
     */
    public CmsPublishList() {
        
        // noop
    }
    
    /**
     * Constructs a publish list for a given project.<p>
     * 
     * @param project the project to publish, this should always be the id of the current project
     */
    public CmsPublishList(CmsProject project) {

        this(project, null, false, true, false);
    }

    /**
     * Constructs a publish list for a single direct publish resource.<p>
     * 
     * @param directPublishResource a VFS resource to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     */
    public CmsPublishList(CmsResource directPublishResource, boolean publishSiblings) {

        this(null, Collections.singletonList(directPublishResource), publishSiblings, true, false);
    }

    /**
     * Constructs a publish list for a list of direct publish resources.<p>
     * 
     * @param directPublishResources a list of <code>{@link CmsResource}</code> instances to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     */
    public CmsPublishList(List directPublishResources, boolean publishSiblings) {

        this(null, directPublishResources, publishSiblings, true, false);
    }

    /**
     * Constructs a publish list for a list of direct publish resources.<p>
     * 
     * @param directPublishResources a list of <code>{@link CmsResource}</code> instances to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     * @param publishRelatedResources indicates if unpublished related resources should be published
     */
    public CmsPublishList(
        List directPublishResources,
        boolean publishSiblings,
        boolean publishSubResources,
        boolean publishRelatedResources) {

        this(null, directPublishResources, publishSiblings, publishSubResources, publishRelatedResources);
    }

    /**
     * Internal constructor for a publish list.<p>
     * 
     * @param project the project to publish
     * @param directPublishResources the list of direct publish resources
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     * @param publishRelatedResources indicates if unpublished related resources should be published
     */
    private CmsPublishList(
        CmsProject project,
        List directPublishResources,
        boolean publishSiblings,
        boolean publishSubResources,
        boolean publishRelatedResources) {

        m_fileList = new ArrayList();
        m_folderList = new ArrayList();
        m_deletedFolderList = new ArrayList();
        m_publishHistoryId = new CmsUUID();
        m_publishSiblings = publishSiblings;
        m_publishSubResources = publishSubResources;
        m_projectId = (project != null) ? project.getUuid() : null;
        m_publishRelatedResources = publishRelatedResources;
        if (directPublishResources != null) {
            // reduce list of folders to minimum
            m_directPublishResources = Collections.unmodifiableList(CmsFileUtil.removeRedundantResources(directPublishResources));
        }
    }

    /**
     * Returns a list of folder resources with the given state.<p>
     * 
     * @return a list of folder resources with the desired state
     */
    public List getDeletedFolderList() {

        if (m_needsRevive) {
            return null;
        } else {
            return m_deletedFolderList;
        }
    }

    /**
     * Returns the list of resources that should be published for a "direct" publish operation.<p>
     * 
     * Will return <code>null</code> if this publish list was not initilaized for a "direct publish" but
     * for a project publish.<p>
     * 
     * @return the list of resources that should be published for a "direct" publish operation, or <code>null</code>
     */
    public List getDirectPublishResources() {

        if (m_needsRevive) {
            return null;
        } else {
            return m_directPublishResources;
        }
    }

    /**
     * Returns an unmodifiable list of the Cms file resources in this publish list.<p>
     * 
     * @return the list with the Cms file resources in this publish list
     */
    public List getFileList() {

        if (m_needsRevive) {
            return null;
        } else {
            return Collections.unmodifiableList(m_fileList);
        }
    }

    /**
     * Returns an unmodifiable list of the new/changed Cms folder resources in this publish list.<p>
     * 
     * @return the list with the new/changed Cms file resources in this publish list
     */
    public List getFolderList() {

        if (m_needsRevive) {
            return null;
        } else {
            return Collections.unmodifiableList(m_folderList);
        }
    }

    /**
     * Returns the id of the project that should be published, or <code>-1</code> if this publish list
     * is initialized for a "direct publish" operation.<p>
     * 
     * @return the id of the project that should be published, or <code>-1</code>
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the publish history Id for this publish list.<p>
     * 
     * @return the publish history Id
     */
    public CmsUUID getPublishHistoryId() {

        return m_publishHistoryId;
    }

    /**
     * Checks if this is a publish list is used for a "direct publish" operation.<p>
     * 
     * @return true if this is a publish list is used for a "direct publish" operation
     */
    public boolean isDirectPublish() {

        return (m_projectId == null);
    }

    /**
     * Checks if unpublished related resources should be published.<p>
     * 
     * @return <code>true</code> if unpublished related resources should be published
     */
    public boolean isPublishRelatedResources() {

        return m_publishRelatedResources;
    }

    /**
     * Returns <code>true</code> if all siblings of the project resources are to be published.<p>
     *  
     * @return <code>true</code> if all siblings of the project resources are to be publisheds
     */
    public boolean isPublishSiblings() {

        return m_publishSiblings;
    }

    /**
     * Returns <code>true</code> if sub-resources in folders should be published (for direct publish only).<p>
     * 
     * @return <code>true</code> if sub-resources in folders should be published (for direct publish only)
     */
    public boolean isPublishSubResources() {

        return m_publishSubResources;
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException {

        // read the history id
        m_publishHistoryId = internalReadUUID(in);        
        // read the project id
        m_projectId = internalReadUUID(in);
        if (m_projectId.isNullUUID()) {
            m_projectId = null;
        }
        // read the flags
        m_publishSiblings = (in.readInt() != 0);
        m_publishSubResources = (in.readInt() != 0);
        // read the list of direct published resources
        m_directPublishResources = internalReadUUIDList(in);
        // read the list of published files
        m_fileList = internalReadUUIDList(in);
        // read the list of published folders
        m_folderList = internalReadUUIDList(in);
        // read the list of deleted folders
        m_deletedFolderList = internalReadUUIDList(in);
        // set revive flag to indicate that resource lists must be revived
        m_needsRevive = true;
    }

    /**
     * Revives the publish list by populating the internal resource lists with <code>CmsResource</code> instances.<p>
     * 
     * @param cms a cms object used to read the resource instances
     */
    public void revive(CmsObject cms) {
    
        if (m_needsRevive) {
            if (m_directPublishResources != null) {
                m_directPublishResources = internalReadResourceList(cms, m_directPublishResources);
            }
            if (m_fileList != null) {
                m_fileList = internalReadResourceList(cms, m_fileList);
            }   
            if (m_folderList != null) {
                m_folderList = internalReadResourceList(cms, m_folderList);
            }
            if (m_deletedFolderList != null) {
                m_deletedFolderList = internalReadResourceList(cms, m_deletedFolderList);
            }
            m_needsRevive = false;
        }
    }
      
    /**
     * Returns the number of all resources to be published.<p>
     * 
     * @return the number of all resources to be published
     */
    public int size() {

        if (m_needsRevive) {
            return 0;
        } else {
            return m_folderList.size() + m_fileList.size() + m_deletedFolderList.size();
        }
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("\n[\n");
        if (isDirectPublish()) {
            result.append("direct publish of resources: ").append(m_directPublishResources.toString()).append("\n");
        } else {
            result.append("publish of project: ").append(m_projectId).append("\n");
        }
        result.append("publish history ID: ").append(m_publishHistoryId.toString()).append("\n");
        result.append("resources: ").append(m_fileList.toString()).append("\n");
        result.append("folders: ").append(m_folderList.toString()).append("\n");
        result.append("deletedFolders: ").append(m_deletedFolderList.toString()).append("\n");
        result.append("]\n");
        return result.toString();
    }
    
    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal (ObjectOutput out) throws IOException {
        
        // write the history id
        out.write(m_publishHistoryId.toByteArray());
        // write the project id
        out.write((m_projectId != null) ? m_projectId.toByteArray() : CmsUUID.getNullUUID().toByteArray());
        // write the flags
        out.writeInt((m_publishSiblings) ? 1 : 0);
        out.writeInt((m_publishSubResources) ? 1 : 0);
        // write the list of direct publish resources by writing the uuid of each resource
        if (m_directPublishResources != null) {
            out.writeInt(m_directPublishResources.size());
            for (Iterator i = m_directPublishResources.iterator(); i.hasNext();) {
                out.write(((CmsResource)i.next()).getStructureId().toByteArray());
            }
        } else {
            out.writeInt(NIL);
        }
        // write the list of published files by writing the uuid of each resource
        if (m_fileList != null) {
            out.writeInt(m_fileList.size());
            for (Iterator i = m_fileList.iterator(); i.hasNext();) {
                out.write(((CmsResource)i.next()).getStructureId().toByteArray());
            }            
        } else {
            out.writeInt(NIL);
        }     
        // write the list of published folders by writing the uuid of each resource
        if (m_folderList != null) {
            out.writeInt(m_folderList.size());
            for (Iterator i = m_folderList.iterator(); i.hasNext();) {
                out.write(((CmsResource)i.next()).getStructureId().toByteArray());
            }    
        } else {
            out.writeInt(NIL);
        }
        // write the list of deleted folders by writing the uuid of each resource
        if (m_deletedFolderList != null) {
            out.writeInt(m_deletedFolderList.size());
            for (Iterator i = m_deletedFolderList.iterator(); i.hasNext();) {
                out.write(((CmsResource)i.next()).getStructureId().toByteArray());
            } 
        } else {
            out.writeInt(NIL);
        }
    }
    
    /**
     * Adds a new/changed Cms folder resource to the publish list.<p>
     * 
     * @param resource a new/changed Cms folder resource
     * @throws IllegalArgumentException if the specified resource is not a folder or unchanged
     */
    protected void add(CmsResource resource) throws IllegalArgumentException {

        // it is essential that this method is only visible within the db package!
        if (resource.getState().isUnchanged()) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_PUBLISH_UNCHANGED_RESOURCE_1,
                resource.getRootPath()));
        }

        if (resource.isFolder()) {
        if (resource.getState().isDeleted()) {
                if (!m_deletedFolderList.contains(resource)) {
                    // only add files not already contained in the list
            m_deletedFolderList.add(resource);
                }
        } else {
                if (!m_folderList.contains(resource)) {
                    // only add files not already contained in the list
            m_folderList.add(resource);
        }
    }
        } else {
            if (!m_fileList.contains(resource)) {
                // only add files not already contained in the list
                // this is required to make sure no siblings are duplicated
                m_fileList.add(resource);
            }
        }
    }
    
    /**
     * Appends all of the new/changed Cms folder resources in the specified list to the end 
     * of this publish list.<p>
     * 
     * @param list a list with new/changed Cms folder resources to be added to this publish list
     * @throws IllegalArgumentException if one of the resources is unchanged
     */
    protected void addAll(List list) throws IllegalArgumentException {

        // it is essential that this method is only visible within the db package!

        Iterator i = list.iterator();
        while (i.hasNext()) {
            add((CmsResource)i.next());
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_fileList != null) {
                m_fileList.clear();
            }
            m_fileList = null;

            if (m_folderList != null) {
                m_folderList.clear();
            }
            m_folderList = null;

            if (m_deletedFolderList != null) {
                m_deletedFolderList.clear();
            }
            m_deletedFolderList = null;
        } catch (Throwable t) {
            // ignore
        }

        super.finalize();
    }

    /**
     * Initializes the publish list, ensuring all internal lists are in the right order.<p>
     */
    protected void initialize() {

        if (m_folderList != null) {
            // ensure folders are sorted starting with parent folders
            Collections.sort(m_folderList, CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_fileList != null) {
            // ensure files are sorted starting with files in parent folders
            Collections.sort(m_fileList, CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_deletedFolderList != null) {
            // ensure deleted folders are sorted starting with child folders
            Collections.sort(m_deletedFolderList, CmsResource.COMPARE_ROOT_PATH);
            Collections.reverse(m_deletedFolderList);
        }
    }

    /**
     * Removes a Cms resource from the publish list.<p>
     * 
     * @param resource a Cms resource
     * @return true if this publish list contains the specified resource
     * @see List#remove(java.lang.Object)
     */
    protected boolean remove(CmsResource resource) {

        // it is essential that this method is only visible within the db package!
        return m_fileList.remove(resource);
    }

    /**
     * Builds a list of <code>CmsResource</code> instances from a list of resource structure ids.<p>
     * 
     * @param cms a cms object
     * @param uuidList the list of structure ids
     * @return a list of <code>CmsResource</code> instances
     */
    private List internalReadResourceList(CmsObject cms, List uuidList) {

        List resList = new ArrayList(uuidList.size());
        for (Iterator i = uuidList.iterator(); i.hasNext();) {
            try  {
                CmsResource res = cms.readResource((CmsUUID)i.next());
                resList.add(res);
            } catch (CmsException exc) {
                LOG.error(exc);
            }
        }
        
        return resList;
    }

    /**
     * Reads a UUID from an object input.<p>
     * 
     * @param in the object input
     * @return a UUID
     * @throws IOException
     */
    private CmsUUID internalReadUUID(ObjectInput in) throws IOException {
        
        byte[] bytes = new byte[UUID_LENGTH];
        in.read(bytes, 0, UUID_LENGTH);
        return new CmsUUID(bytes);
    }

    /**
     * Reads a sequence of UUIDs from an objetc input and builds a list of <code>CmsResource</code> instances from it.<p>
     * 
     * @param in the object input
     * @return a list of <code>CmsResource</code> instances
     * 
     * @throws IOException if something goes wrong 
     */
    private List internalReadUUIDList(ObjectInput in) throws IOException {
  
        List result = null;
        byte[] bytes = new byte[UUID_LENGTH];
        
        int i = in.readInt();
        if (i >= 0) {
            result = new ArrayList();
            while (i-- > 0) {
                in.read(bytes, 0, UUID_LENGTH);
                result.add(new CmsUUID(bytes));
            }    
        } 
        
        return result;
    }
}
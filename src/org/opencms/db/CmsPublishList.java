/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.I_CmsResource;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @since 6.0.0
 *
 * @see org.opencms.db.CmsDriverManager#fillPublishList(CmsDbContext, CmsPublishList)
 */
public class CmsPublishList implements Externalizable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishList.class);

    /** Indicates a non existent object in the serialized data. */
    private static final int NIL = -1;

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -2578909250462750927L;

    /** Length of a serialized uuid. */
    private static final int UUID_LENGTH = CmsUUID.getNullUUID().toByteArray().length;

    /** The list of deleted folder resources to be published.<p> */
    private List<CmsResource> m_deletedFolderList;

    /** The list of deleted folder UUIDs to be published for later retrieval.<p> */
    private List<CmsUUID> m_deletedFolderUUIDs;

    /** The list of direct publish resources. */
    private List<CmsResource> m_directPublishResources;

    /** The list of direct publish resource UUIDs to be published for later retrieval.<p> */
    private List<CmsUUID> m_directPublishResourceUUIDs;

    /** The list of new/changed/deleted file resources to be published.<p> */
    private List<CmsResource> m_fileList;

    /** The list of new/changed/deleted file resource UUIDs to be published for later retrieval.<p> */
    private List<CmsUUID> m_fileUUIDs;

    /** The list of new/changed folder resources to be published.<p> */
    private List<CmsResource> m_folderList;

    /** The list of new/changed folder resource UUIDs to be published for later retrieval.<p> */
    private List<CmsUUID> m_folderUUIDs;

    /** Indicates whether this is a user publish list. */
    private boolean m_isUserPublishList;

    /** Flag to indicate if the list needs to be revived. */
    private boolean m_needsRevive;

    /** The id of the project that is to be published. */
    private CmsUUID m_projectId;

    /** The publish history ID.<p> */
    private CmsUUID m_publishHistoryId;

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
     * Constructs a publish list for a list of direct publish resources.<p>
     *
     * @param all no redundant resource are filtered out
     * @param directPublishResources a list of <code>{@link CmsResource}</code> instances to be published directly
     * @param directPublishSiblings indicates if all siblings of the selected resources should be published
     */
    public CmsPublishList(boolean all, List<CmsResource> directPublishResources, boolean directPublishSiblings) {

        this(null, directPublishResources, directPublishSiblings, false, true);
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
    public CmsPublishList(List<CmsResource> directPublishResources, boolean publishSiblings) {

        this(null, directPublishResources, publishSiblings, true, false);
    }

    /**
     * Constructs a publish list for a list of direct publish resources.<p>
     *
     * @param directPublishResources a list of <code>{@link CmsResource}</code> instances to be published directly
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     */
    public CmsPublishList(
        List<CmsResource> directPublishResources,
        boolean publishSiblings,
        boolean publishSubResources) {

        this(null, directPublishResources, publishSiblings, publishSubResources, false);
    }

    /**
     * Internal constructor for a publish list.<p>
     *
     * @param project the project to publish
     * @param directPublishResources the list of direct publish resources
     * @param publishSiblings indicates if all siblings of the selected resources should be published
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     * @param all if <code>true</code> the publish list will not be filtered for redundant resources
     */
    private CmsPublishList(
        CmsProject project,
        List<CmsResource> directPublishResources,
        boolean publishSiblings,
        boolean publishSubResources,
        boolean all) {

        m_fileList = new ArrayList<CmsResource>();
        m_folderList = new ArrayList<CmsResource>();
        m_deletedFolderList = new ArrayList<CmsResource>();
        m_publishHistoryId = new CmsUUID();
        m_publishSiblings = publishSiblings;
        m_publishSubResources = publishSubResources;
        m_projectId = (project != null) ? project.getUuid() : null;
        if (directPublishResources != null) {
            if (!all) {
                // reduce list of folders to minimum
                m_directPublishResources = Collections.unmodifiableList(
                    CmsFileUtil.removeRedundantResources(directPublishResources));
            } else {
                m_directPublishResources = Collections.unmodifiableList(directPublishResources);
            }
        }
    }

    /**
     * Returns a list of all resources in the publish list,
     * including folders and files.<p>
     *
     * @return a list of {@link CmsResource} objects
     */
    public List<CmsResource> getAllResources() {

        List<CmsResource> all = new ArrayList<CmsResource>();
        all.addAll(m_folderList);
        all.addAll(m_fileList);
        all.addAll(m_deletedFolderList);

        Collections.sort(all, I_CmsResource.COMPARE_ROOT_PATH);
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns a list of folder resources with the deleted state.<p>
     *
     * @return a list of folder resources with the deleted state
     */
    public List<CmsResource> getDeletedFolderList() {

        if (m_needsRevive) {
            return null;
        } else {
            return m_deletedFolderList;
        }
    }

    /**
     * Returns the list of resources that should be published for a "direct" publish operation.<p>
     *
     * Will return <code>null</code> if this publish list was not initialized for a "direct publish" but
     * for a project publish.<p>
     *
     * @return the list of resources that should be published for a "direct" publish operation, or <code>null</code>
     */
    public List<CmsResource> getDirectPublishResources() {

        if (m_needsRevive) {
            return null;
        } else {
            return m_directPublishResources;
        }
    }

    /**
     * Returns an unmodifiable list of the files in this publish list.<p>
     *
     * @return the list with the files in this publish list
     */
    public List<CmsResource> getFileList() {

        if (m_needsRevive) {
            return null;
        } else {
            return Collections.unmodifiableList(m_fileList);
        }
    }

    /**
     * Returns an unmodifiable list of the new/changed folders in this publish list.<p>
     *
     * @return the list with the new/changed folders in this publish list
     */
    public List<CmsResource> getFolderList() {

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
     * Gets the list of moved folders which are not subfolders of other moved folders in the publish list.<p>
     * @param cms the current cms context
     * @return the moved folders which are not subfolders of other moved folders in the publish list
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getTopMovedFolders(CmsObject cms) throws CmsException {

        List<CmsResource> movedFolders = getMovedFolders(cms);
        List<CmsResource> result = getTopFolders(movedFolders);
        return result;
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
     * Returns true if this is a user publish list.<p>
     *
     * @return true if this is a user publish list
     */
    public boolean isUserPublishList() {

        return m_isUserPublishList;
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
        m_directPublishResourceUUIDs = internalReadUUIDList(in);
        // read the list of published files
        m_fileUUIDs = internalReadUUIDList(in);
        // read the list of published folders
        m_folderUUIDs = internalReadUUIDList(in);
        // read the list of deleted folders
        m_deletedFolderUUIDs = internalReadUUIDList(in);
        // set revive flag to indicate that resource lists must be revived
        m_needsRevive = true;
    }

    /**
     * Revives the publish list by populating the internal resource lists with <code>{@link CmsResource}</code> instances.<p>
     *
     * @param cms a cms object used to read the resource instances
     */
    public void revive(CmsObject cms) {

        if (m_needsRevive) {
            if (m_directPublishResourceUUIDs != null) {
                m_directPublishResources = internalReadResourceList(cms, m_directPublishResourceUUIDs);
            }
            if (m_fileUUIDs != null) {
                m_fileList = internalReadResourceList(cms, m_fileUUIDs);
            }
            if (m_folderUUIDs != null) {
                m_folderList = internalReadResourceList(cms, m_folderUUIDs);
            }
            if (m_deletedFolderUUIDs != null) {
                m_deletedFolderList = internalReadResourceList(cms, m_deletedFolderUUIDs);
            }
            m_needsRevive = false;
        }
    }

    /**
     * Sets the 'user publish list' flag on this publish list.<p>
     *
     * @param isUserPublishList if true, the list is marked as a user publish list
     */
    public void setUserPublishList(boolean isUserPublishList) {

        m_isUserPublishList = isUserPublishList;
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
    @Override
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
    public void writeExternal(ObjectOutput out) throws IOException {

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
            for (Iterator<CmsResource> i = m_directPublishResources.iterator(); i.hasNext();) {
                out.write((i.next()).getStructureId().toByteArray());
            }
        } else {
            out.writeInt(NIL);
        }
        // write the list of published files by writing the uuid of each resource
        if (m_fileList != null) {
            out.writeInt(m_fileList.size());
            for (Iterator<CmsResource> i = m_fileList.iterator(); i.hasNext();) {
                out.write((i.next()).getStructureId().toByteArray());
            }
        } else {
            out.writeInt(NIL);
        }
        // write the list of published folders by writing the uuid of each resource
        if (m_folderList != null) {
            out.writeInt(m_folderList.size());
            for (Iterator<CmsResource> i = m_folderList.iterator(); i.hasNext();) {
                out.write((i.next()).getStructureId().toByteArray());
            }
        } else {
            out.writeInt(NIL);
        }
        // write the list of deleted folders by writing the uuid of each resource
        if (m_deletedFolderList != null) {
            out.writeInt(m_deletedFolderList.size());
            for (Iterator<CmsResource> i = m_deletedFolderList.iterator(); i.hasNext();) {
                out.write((i.next()).getStructureId().toByteArray());
            }
        } else {
            out.writeInt(NIL);
        }
    }

    /**
     * Adds a new/changed Cms folder resource to the publish list.<p>
     *
     * @param resource a new/changed Cms folder resource
     * @param check if set an exception is thrown if the specified resource is unchanged,
     *              if not set the resource is ignored
     *
     * @throws IllegalArgumentException if the specified resource is unchanged
     */
    protected void add(CmsResource resource, boolean check) throws IllegalArgumentException {

        if (check) {
            // it is essential that this method is only visible within the db package!
            if (resource.getState().isUnchanged()) {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_PUBLISH_UNCHANGED_RESOURCE_1, resource.getRootPath()));
            }
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
     * Appends all the given resources to this publish list.<p>
     *
     * @param resources resources to be added to this publish list
     * @param check if set an exception is thrown if the a resource is unchanged,
     *              if not set the resource is ignored
     *
     * @throws IllegalArgumentException if one of the resources is unchanged
     */
    protected void addAll(Collection<CmsResource> resources, boolean check) throws IllegalArgumentException {

        // it is essential that this method is only visible within the db package!
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            add(i.next(), check);
        }
    }

    /**
     * Checks whether the publish list contains all sub-resources of a list of folders.<p>
     *
     * @param cms the current CMS context
     * @param folders the folders which should be checked
     * @return a folder from the list if one of its sub-resources is not contained in the publish list, otherwise null
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsResource checkContainsSubResources(CmsObject cms, List<CmsResource> folders) throws CmsException {

        for (CmsResource folder : folders) {

            if (!containsSubResources(cms, folder)) {
                return folder;
            }
        }
        return null;
    }

    /**
     * Checks if the publish list contains a resource.<p>
     *
     * @param res the resource
     * @return true if the publish list contains a resource
     */
    protected boolean containsResource(CmsResource res) {

        return m_deletedFolderList.contains(res) || m_folderList.contains(res) || m_fileList.contains(res);
    }

    /**
     * Checks if the publish list contains all sub-resources of a given folder.<p>
     *
     * @param cms the current CMS context
     * @param folder the folder for which the check should be performed
     * @return true if the publish list contains all sub-resources of a given folder
     * @throws CmsException if something goes wrong
     */
    protected boolean containsSubResources(CmsObject cms, CmsResource folder) throws CmsException {

        String folderPath = cms.getSitePath(folder);
        List<CmsResource> subResources = cms.readResources(folderPath, CmsResourceFilter.ALL, true);
        for (CmsResource resource : subResources) {
            if (!containsResource(resource)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the sub-resources of a list of folders which are missing from the publish list.<p>
     *
     * @param cms the current CMS context
     * @param folders the folders which should be checked
     * @return a list of missing sub resources
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getMissingSubResources(CmsObject cms, List<CmsResource> folders) throws CmsException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsResource folder : folders) {
            String folderPath = cms.getSitePath(folder);
            List<CmsResource> subResources = cms.readResources(folderPath, CmsResourceFilter.ALL, true);
            for (CmsResource resource : subResources) {
                if (!containsResource(resource)) {
                    result.add(resource);
                }
            }
        }
        return result;
    }

    /**
     * Internal method to get the moved folders from the publish list.<p>
     *
     * @param cms the current CMS context
     * @return the list of moved folders from the publish list
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getMovedFolders(CmsObject cms) throws CmsException {

        CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
        List<CmsResource> movedFolders = new ArrayList<CmsResource>();
        for (CmsResource folder : m_folderList) {
            if (folder.getState().isChanged()) {
                CmsProject oldProject = cms.getRequestContext().getCurrentProject();
                boolean isMoved = false;
                try {
                    cms.getRequestContext().setCurrentProject(onlineProject);
                    CmsResource onlineResource = cms.readResource(folder.getStructureId());
                    isMoved = !onlineResource.getRootPath().equals(folder.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // resource not found online, this means it doesn't matter whether it has been moved
                } finally {
                    cms.getRequestContext().setCurrentProject(oldProject);
                }
                if (isMoved) {
                    movedFolders.add(folder);
                }
            }
        }
        return movedFolders;
    }

    /**
     * Gives the "roots" of a list of folders, i.e. the list of folders which are not descendants of any other folders in the original list
     * @param folders the original list of folders
     * @return the root folders of the list
     */
    protected List<CmsResource> getTopFolders(List<CmsResource> folders) {

        List<String> folderPaths = new ArrayList<String>();
        List<CmsResource> topFolders = new ArrayList<CmsResource>();
        Map<String, CmsResource> foldersByPath = new HashMap<String, CmsResource>();
        for (CmsResource folder : folders) {
            folderPaths.add(folder.getRootPath());
            foldersByPath.put(folder.getRootPath(), folder);
        }
        Collections.sort(folderPaths);
        Set<String> topFolderPaths = new HashSet<String>(folderPaths);
        for (int i = 0; i < folderPaths.size(); i++) {
            for (int j = i + 1; j < folderPaths.size(); j++) {
                if (folderPaths.get(j).startsWith((folderPaths.get(i)))) {
                    topFolderPaths.remove(folderPaths.get(j));
                } else {
                    break;
                }
            }
        }
        for (String path : topFolderPaths) {
            topFolders.add(foldersByPath.get(path));
        }
        return topFolders;
    }

    /**
     * Initializes the publish list, ensuring all internal lists are in the right order.<p>
     */
    protected void initialize() {

        if (m_folderList != null) {
            // ensure folders are sorted starting with parent folders
            Collections.sort(m_folderList, I_CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_fileList != null) {
            // ensure files are sorted starting with files in parent folders
            Collections.sort(m_fileList, I_CmsResource.COMPARE_ROOT_PATH);
        }

        if (m_deletedFolderList != null) {
            // ensure deleted folders are sorted starting with child folders
            Collections.sort(m_deletedFolderList, I_CmsResource.COMPARE_ROOT_PATH);
            Collections.reverse(m_deletedFolderList);
        }
    }

    /**
     * Removes a Cms resource from the publish list.<p>
     *
     * @param resource a Cms resource
     *
     * @return true if this publish list contains the specified resource
     *
     * @see List#remove(java.lang.Object)
     */
    protected boolean remove(CmsResource resource) {

        // it is essential that this method is only visible within the db package!
        boolean ret = m_fileList.remove(resource);
        ret |= m_folderList.remove(resource);
        ret |= m_deletedFolderList.remove(resource);
        return ret;
    }

    /**
     * Builds a list of <code>CmsResource</code> instances from a list of resource structure IDs.<p>
     *
     * @param cms a cms object
     * @param uuidList the list of structure IDs
     * @return a list of <code>CmsResource</code> instances
     */
    private List<CmsResource> internalReadResourceList(CmsObject cms, List<CmsUUID> uuidList) {

        List<CmsResource> resList = new ArrayList<CmsResource>(uuidList.size());
        for (Iterator<CmsUUID> i = uuidList.iterator(); i.hasNext();) {
            try {
                CmsResource res = cms.readResource(i.next(), CmsResourceFilter.ALL);
                resList.add(res);
            } catch (CmsException exc) {
                LOG.error(exc.getLocalizedMessage(), exc);
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
        in.readFully(bytes, 0, UUID_LENGTH);
        return new CmsUUID(bytes);
    }

    /**
     * Reads a sequence of UUIDs from an object input and builds a list of <code>CmsResource</code> instances from it.<p>
     *
     * @param in the object input
     * @return a list of <code>{@link CmsResource}</code> instances
     *
     * @throws IOException if something goes wrong
     */
    private List<CmsUUID> internalReadUUIDList(ObjectInput in) throws IOException {

        List<CmsUUID> result = null;

        int i = in.readInt();
        if (i >= 0) {
            result = new ArrayList<CmsUUID>();
            while (i > 0) {
                result.add(internalReadUUID(in));
                i--;
            }
        }

        return result;
    }
}
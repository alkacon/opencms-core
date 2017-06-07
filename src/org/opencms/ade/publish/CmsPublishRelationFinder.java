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

package org.opencms.ade.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Helper class for finding all related resources for a set of resources to be published, for use with the new ADE publish dialog.<p>
 */
public class CmsPublishRelationFinder {

    /**
     * A map from resources to sets of resources, which automtically instantiates an empty set when accessing a key that
     * doesn't exist via get().<p>
     */
    public static class ResourceMap extends HashMap<CmsResource, Set<CmsResource>> {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.<p>
         */
        public ResourceMap() {

            super();
        }

        /**
         * Creates a new resource map based on this instance while filtering some elements out.<p>
         *
         * The given predicate is used to check whether any single resource should be kept. If it returns
         * false for a top-level resource (map key), the parent will be removed and all its children added as
         * keys. If it returns false for a map value, the value will be removed for its key.
         *
         * @param pred predicate to check whether resources should be kept
         * @return the new filtered resource map
         */
        public ResourceMap filter(Predicate<CmsResource> pred) {

            ResourceMap result = new ResourceMap();
            for (CmsResource key : keySet()) {
                if (pred.apply(key)) {
                    result.get(key);
                    for (CmsResource value : get(key)) {
                        if (pred.apply(value)) {
                            result.get(key).add(value);
                        }
                    }
                } else {
                    for (CmsResource value : get(key)) {
                        if (pred.apply(value)) {
                            result.get(value);
                        }
                    }

                }
            }
            return result;
        }

        /**
         * @see java.util.HashMap#get(java.lang.Object)
         */
        @Override
        public Set<CmsResource> get(Object res) {

            Set<CmsResource> result = super.get(res);
            if (result == null) {
                result = Sets.newHashSet();
                put((CmsResource)res, result);
            }
            return result;
        }

        /**
         * Returns the sum of all sizes of set values.<p>
         *
         * @return the total size
         */
        public int totalSize() {

            int result = 0;
            for (Map.Entry<CmsResource, Set<CmsResource>> entry : entrySet()) {
                result += entry.getValue().size();
            }
            return result;
        }

    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishRelationFinder.class);

    /** The resource types whose resources will be also added as related resources, even if the relation pointing to them is a weak relation.<p> */
    private static final String[] VALID_WEAK_RELATION_TARGET_TYPES = {
        CmsResourceTypePlain.getStaticTypeName(),
        CmsResourceTypeImage.getStaticTypeName(),
        CmsResourceTypePointer.getStaticTypeName(),
        CmsResourceTypeBinary.getStaticTypeName()};

    /** The CMS context used by this object. */
    private CmsObject m_cms;

    /** Flag which controls whether unchanged resources in the original resource list should be kept or removed. */
    private boolean m_keepOriginalUnchangedResources;

    /** The original set of resources passed in the constructor. */
    private Set<CmsResource> m_originalResources;

    /** The provider for additional related resources. */
    private I_CmsPublishRelatedResourceProvider m_relatedResourceProvider;

    /** Cache for resources. */
    private Map<CmsUUID, CmsResource> m_resources = Maps.newHashMap();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     * @param resources the resources for which the related resources should be found
     * @param keepOriginalUnchangedResources true if unchanged resources from the original resource list should be kept
     * @param relProvider provider for additional related resources
     */
    public CmsPublishRelationFinder(
        CmsObject cms,
        Collection<CmsResource> resources,
        boolean keepOriginalUnchangedResources,
        I_CmsPublishRelatedResourceProvider relProvider) {

        m_cms = cms;
        // put resources in a map with the structure id as a key
        m_originalResources = Sets.newHashSet(resources);
        for (CmsResource res : resources) {
            m_resources.put(res.getStructureId(), res);
        }
        m_keepOriginalUnchangedResources = keepOriginalUnchangedResources;
        m_relatedResourceProvider = relProvider;
    }

    /**
     * Gets the related resources in the form of a ResourceMap.<p>
     *
     * @return a ResourceMap which has resources from the original set of resources as keys, and sets of related resources as values
     *
     */
    public ResourceMap getPublishRelatedResources() {

        ResourceMap related = computeRelatedResources();
        ResourceMap reachable = computeReachability(related);
        ResourceMap publishRelatedResources = getChangedResourcesReachableFromOriginalResources(reachable);
        removeNestedItemsFromTopLevel(publishRelatedResources);
        //addParentFolders(publishRelatedResources);
        removeUnchangedTopLevelResources(publishRelatedResources, reachable);
        return publishRelatedResources;
    }

    /**
     * Removes unchanged resources from the top level, and if they have children which do not occur anywhere else,
     * moves these children to the top level.<p>
     *
     * @param publishRelatedResources the resource map to modify
     * @param reachability the reachability map
     */
    public void removeUnchangedTopLevelResources(ResourceMap publishRelatedResources, ResourceMap reachability) {

        Set<CmsResource> unchangedParents = Sets.newHashSet();
        Set<CmsResource> childrenOfUnchangedParents = Sets.newHashSet();
        Set<CmsResource> other = Sets.newHashSet();
        for (CmsResource parent : publishRelatedResources.keySet()) {
            if (isUnchangedAndShouldBeRemoved(parent)) {
                unchangedParents.add(parent);
                childrenOfUnchangedParents.addAll(publishRelatedResources.get(parent));
            } else {
                other.add(parent);
                other.addAll(publishRelatedResources.get(parent));
            }
        }

        // we want the resources which *only* occur as children of unchanged parents
        childrenOfUnchangedParents.removeAll(other);

        for (CmsResource parent : unchangedParents) {
            publishRelatedResources.remove(parent);
        }

        // Try to find hierarchical relationships in childrenOfUnchangedParents
        while (findAndMoveParentWithChildren(childrenOfUnchangedParents, reachability, publishRelatedResources)) {
            // do nothing
        }
        // only the resources with no 'children' are left, transfer them to the target map
        for (CmsResource remainingResource : childrenOfUnchangedParents) {
            publishRelatedResources.get(remainingResource);
        }
    }

    /**
     * Computes the "reachability map", given the map of direct relations between resources.<p>
     *
     * @param relatedResources a map containing the direct relations between resources
     * @return a map from resources to the sets of resources which are reachable via relations
     */
    private ResourceMap computeReachability(ResourceMap relatedResources) {

        ResourceMap result = new ResourceMap();
        for (CmsResource resource : relatedResources.keySet()) {
            result.get(resource).add(resource);
            result.get(resource).addAll(relatedResources.get(resource));
        }
        int oldSize, newSize;
        do {
            ResourceMap newReachableResources = new ResourceMap();
            oldSize = result.totalSize();
            for (CmsResource source : result.keySet()) {
                for (CmsResource target : result.get(source)) {
                    // need to check if the key is present, otherwise we may get a ConcurrentModificationException
                    if (result.containsKey(target)) {
                        newReachableResources.get(source).addAll(result.get(target));
                    }
                }
            }
            newSize = newReachableResources.totalSize();
            result = newReachableResources;
        } while (oldSize < newSize);
        return result;
    }

    /**
     * Gets a ResourceMap which contains, for each resource reachable from the original set of resources, the directly related resources.<p>
     *
     * @return a map from resources to their directly related resources
     */
    private ResourceMap computeRelatedResources() {

        ResourceMap relatedResources = new ResourceMap();
        Set<CmsResource> resourcesToProcess = Sets.newHashSet(m_originalResources);
        Set<CmsResource> processedResources = Sets.newHashSet();
        while (!resourcesToProcess.isEmpty()) {
            CmsResource currentResource = resourcesToProcess.iterator().next();
            resourcesToProcess.remove(currentResource);
            processedResources.add(currentResource);
            if (!currentResource.getState().isDeleted()) {
                Set<CmsResource> directlyRelatedResources = getDirectlyRelatedResources(currentResource);
                for (CmsResource target : directlyRelatedResources) {
                    if (!processedResources.contains(target)) {
                        resourcesToProcess.add(target);
                    }
                    relatedResources.get(currentResource).add(target);
                }
            }
        }
        return relatedResources;
    }

    /**
     * Tries to find a parent with related children in a set, and moves them to a result ResourceMap.<p>
     *
     * @param originalSet the original set
     * @param reachability the reachability ResourceMap
     * @param result the target ResourceMap to move the parent/children to
     *
     * @return true if a parent with children could be found (and moved)
     */
    private boolean findAndMoveParentWithChildren(
        Set<CmsResource> originalSet,
        ResourceMap reachability,
        ResourceMap result) {

        for (CmsResource parent : originalSet) {
            Set<CmsResource> reachableResources = reachability.get(parent);
            Set<CmsResource> children = Sets.newHashSet();
            if (reachableResources.size() > 1) {
                for (CmsResource potentialChild : reachableResources) {
                    if ((potentialChild != parent) && originalSet.contains(potentialChild)) {
                        children.add(potentialChild);
                    }
                }
                if (children.size() > 0) {
                    result.get(parent).addAll(children);
                    originalSet.removeAll(children);
                    originalSet.remove(parent);
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Gets the resources which are reachable from the original set of resources and are not unchanged.<p>
     *
     * @param reachable the resource map of reachable resources
     * @return the resources which are unchanged and reachable from the original set of resources
     */
    private ResourceMap getChangedResourcesReachableFromOriginalResources(ResourceMap reachable) {

        ResourceMap publishRelatedResources = new ResourceMap();
        for (CmsResource res : m_originalResources) {
            Collection<CmsResource> reachableItems = reachable.get(res);
            List<CmsResource> changedItems = Lists.newArrayList();
            for (CmsResource item : reachableItems) {
                if (!isUnchangedAndShouldBeRemoved(item) && !item.getStructureId().equals(res.getStructureId())) {
                    changedItems.add(item);
                }
            }
            publishRelatedResources.get(res).addAll(changedItems);
        }
        return publishRelatedResources;
    }

    /**
     * Fetches the directly related resources for a given resource.<p>
     *
     * @param currentResource the resource for which to get the related resources
     * @return the directly related resources
     */
    private Set<CmsResource> getDirectlyRelatedResources(CmsResource currentResource) {

        Set<CmsResource> directlyRelatedResources = Sets.newHashSet();
        List<CmsRelation> relations = getRelationsFromResource(currentResource);
        for (CmsRelation relation : relations) {
            LOG.info("Trying to read resource for relation " + relation.getTargetPath());
            CmsResource target = getResource(relation.getTargetId());
            if (target != null) {
                if (relation.getType().isStrong() || shouldAddWeakRelationTarget(target)) {
                    directlyRelatedResources.add(target);
                }
            }
        }
        try {
            CmsResource parentFolder = m_cms.readParentFolder(currentResource.getStructureId());
            if (parentFolder != null) { // parent folder of root folder is null
                if (parentFolder.getState().isNew() || currentResource.isFile()) {
                    directlyRelatedResources.add(parentFolder);
                }
            }
        } catch (CmsException e) {
            LOG.error(
                "Error processing parent folder for " + currentResource.getRootPath() + ": " + e.getLocalizedMessage(),
                e);
        }

        try {
            directlyRelatedResources.addAll(
                m_relatedResourceProvider.getAdditionalRelatedResources(m_cms, currentResource));
        } catch (Exception e) {
            LOG.error(
                "Error processing additional related resource for "
                    + currentResource.getRootPath()
                    + ": "
                    + e.getLocalizedMessage(),
                e);
        }
        return directlyRelatedResources;
    }

    /**
     * Reads the relations from a given resource, and returns an empty list if an error occurs while reading them.<p>
     *
     * @param currentResource the resource for which to get the relation
     * @return the outgoing relations
     */
    private List<CmsRelation> getRelationsFromResource(CmsResource currentResource) {

        try {
            return m_cms.readRelations(CmsRelationFilter.relationsFromStructureId(currentResource.getStructureId()));
        } catch (CmsException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Reads a resource with a given id, but will get a resource from a cache if it has already been read before.<p>
     * If an error occurs, null will be returned.
     *
     * @param structureId the structure id
     * @return the resource with the given structure id
     */
    private CmsResource getResource(CmsUUID structureId) {

        CmsResource resource = m_resources.get(structureId);
        if (resource == null) {
            try {
                resource = m_cms.readResource(structureId, CmsResourceFilter.ALL);
                m_resources.put(structureId, resource);
            } catch (CmsException e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        return resource;
    }

    /**
     * Checks if the resource is unchanged *and* should be removed.<p>
     *
     * @param item the resource to check
     * @return true if the resource is unchanged and should be removed
     */
    private boolean isUnchangedAndShouldBeRemoved(CmsResource item) {

        if (item.getState().isUnchanged()) {
            return !m_keepOriginalUnchangedResources || !m_originalResources.contains(item);
        }
        return false;
    }

    /**
     * Removes those resources as keys from the resource map which also occur as related resources under a different key.<p>
     *
     * @param publishRelatedResources the resource map from which to remove the duplicate items
     */
    private void removeNestedItemsFromTopLevel(ResourceMap publishRelatedResources) {

        Set<CmsResource> toDelete = Sets.newHashSet();
        for (CmsResource parent : publishRelatedResources.keySet()) {
            if (toDelete.contains(parent)) {
                continue;
            }
            for (CmsResource child : publishRelatedResources.get(parent)) {
                if (publishRelatedResources.containsKey(child)) {
                    toDelete.add(child);
                }
            }
        }
        for (CmsResource delResource : toDelete) {
            publishRelatedResources.remove(delResource);
        }
    }

    /**
     * Checks if the resource should be added to the related resources even if the relation pointing to it is a weak relation.<p>
     *
     * @param weakRelationTarget the relation target resource
     * @return true if the resource should be added as a related resource
     */
    private boolean shouldAddWeakRelationTarget(CmsResource weakRelationTarget) {

        for (String typeName : VALID_WEAK_RELATION_TARGET_TYPES) {
            if (OpenCms.getResourceManager().matchResourceType(typeName, weakRelationTarget.getTypeId())) {
                return true;
            }
        }
        return false;
    }
}

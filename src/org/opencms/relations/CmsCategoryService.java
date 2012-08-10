/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides several simplified methods for manipulating category relations.<p>
 * 
 * @since 6.9.2
 * 
 * @see CmsCategory
 */
public class CmsCategoryService {

    /** The centralized path for categories. */
    public static final String CENTRALIZED_REPOSITORY = "/system/categories/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategoryService.class);

    /** The singleton instance. */
    private static CmsCategoryService m_instance;

    /** The folder for the local category repositories. */
    private static final String REPOSITORY_BASE_FOLDER = "/_categories/";

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the singleton instance
     */
    public static CmsCategoryService getInstance() {

        if (m_instance == null) {
            m_instance = new CmsCategoryService();
        }
        return m_instance;
    }

    /**
     * Adds a resource identified by the given resource name to the given category.<p>
     * 
     * The resource has to be locked.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to add
     * @param category the category to add the resource to
     * 
     * @throws CmsException if something goes wrong
     */
    public void addResourceToCategory(CmsObject cms, String resourceName, CmsCategory category) throws CmsException {

        if (readResourceCategories(cms, cms.readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION)).contains(
            category)) {
            return;
        }
        String sitePath = cms.getRequestContext().removeSiteRoot(category.getRootPath());
        cms.addRelationToResource(resourceName, sitePath, CmsRelationType.CATEGORY.getName());

        String parentCatPath = category.getPath();
        // recursively add to higher level categories
        if (parentCatPath.endsWith("/")) {
            parentCatPath = parentCatPath.substring(0, parentCatPath.length() - 1);
        }
        if (parentCatPath.lastIndexOf('/') > 0) {
            addResourceToCategory(cms, resourceName, parentCatPath.substring(0, parentCatPath.lastIndexOf('/') + 1));
        }
    }

    /**
     * Adds a resource identified by the given resource name to the category
     * identified by the given category path.<p>
     * 
     * Only the most global category matching the given category path for the 
     * given resource will be affected.<p>
     * 
     * The resource has to be locked.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to add
     * @param categoryPath the path of the category to add the resource to
     * 
     * @throws CmsException if something goes wrong
     */
    public void addResourceToCategory(CmsObject cms, String resourceName, String categoryPath) throws CmsException {

        CmsCategory category = readCategory(cms, categoryPath, resourceName);
        addResourceToCategory(cms, resourceName, category);
    }

    /**
     * Removes the given resource from all categories.<p>
     * 
     * @param cms the cms context
     * @param resourcePath the resource to reset the categories for
     * 
     * @throws CmsException if something goes wrong
     */
    public void clearCategoriesForResource(CmsObject cms, String resourcePath) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        filter = filter.filterType(CmsRelationType.CATEGORY);
        cms.deleteRelationsFromResource(resourcePath, filter);
    }

    /**
     * Creates a new category.<p>
     * 
     * @param cms the current cms context
     * @param parent the parent category or <code>null</code> for a new top level category
     * @param name the name of the new category 
     * @param title the title
     * @param description the description
     * 
     * @return the new created category 
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #createCategory(CmsObject, CmsCategory, String, String, String, String)} instead
     */
    public CmsCategory createCategory(CmsObject cms, CmsCategory parent, String name, String title, String description)
    throws CmsException {

        return createCategory(cms, parent, name, title, description, null);
    }

    /**
     * Creates a new category.<p>
     * 
     * Will use the same category repository as the parent if specified,
     * or the closest category repository to the reference path if specified,
     * or the centralized category repository in all other cases.<p>
     * 
     * @param cms the current cms context
     * @param parent the parent category or <code>null</code> for a new top level category
     * @param name the name of the new category 
     * @param title the title
     * @param description the description
     * @param referencePath the reference path for the category repository
     * 
     * @return the new created category 
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsCategory createCategory(
        CmsObject cms,
        CmsCategory parent,
        String name,
        String title,
        String description,
        String referencePath) throws CmsException {

        List properties = new ArrayList();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(description)) {
            properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, description, null));
        }
        String folderPath = "";
        if (parent != null) {
            folderPath += parent.getRootPath();
        } else {
            if (referencePath == null) {
                folderPath += CmsCategoryService.CENTRALIZED_REPOSITORY;
            } else {
                List repositories = getCategoryRepositories(cms, referencePath);
                // take the last one
                folderPath = (String)repositories.get(repositories.size() - 1);
            }
        }
        folderPath = cms.getRequestContext().removeSiteRoot(internalCategoryRootPath(folderPath, name));
        CmsResource resource;
        try {
            resource = cms.createResource(folderPath, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, properties);
        } catch (CmsVfsResourceNotFoundException e) {
            // may be is the centralized repository missing, try to create it
            cms.createResource(CmsCategoryService.CENTRALIZED_REPOSITORY, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
            // now try again
            resource = cms.createResource(folderPath, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, properties);
        }
        return getCategory(cms, resource);
    }

    /**
     * Deletes the category identified by the given path.<p> 
     * 
     * The given category path may be a relative path to the centralized categories repository,
     * or an absolute path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to delete
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #deleteCategory(CmsObject, String, String)} instead
     */
    public void deleteCategory(CmsObject cms, String categoryPath) throws CmsException {

        deleteCategory(cms, categoryPath, null);
    }

    /**
     * Deletes the category identified by the given path.<p> 
     * 
     * Only the most global category matching the given category path for the 
     * given resource will be affected.<p>
     * 
     * This method will try to lock the involved resource.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to delete
     * @param referencePath the reference path to find the category repositories
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteCategory(CmsObject cms, String categoryPath, String referencePath) throws CmsException {

        CmsCategory category = readCategory(cms, categoryPath, referencePath);
        String folderPath = cms.getRequestContext().removeSiteRoot(category.getRootPath());
        CmsLock lock = cms.getLock(folderPath);
        if (lock.isNullLock()) {
            cms.lockResource(folderPath);
        } else if (lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
            cms.changeLock(folderPath);
        }
        cms.deleteResource(folderPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Creates a category from the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource
     * 
     * @return a category object
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsCategory getCategory(CmsObject cms, CmsResource resource) throws CmsException {

        CmsProperty title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
        CmsProperty description = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        return new CmsCategory(
            resource.getStructureId(),
            resource.getRootPath(),
            title.getValue(resource.getName()),
            description.getValue(""),
            getRepositoryBaseFolderName(cms));
    }

    /**
     * Creates a category from the given category root path.<p>
     * 
     * @param cms the cms context
     * @param categoryRootPath the category root path
     * 
     * @return a category object
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsCategory getCategory(CmsObject cms, String categoryRootPath) throws CmsException {

        CmsResource resource = cms.readResource(cms.getRequestContext().removeSiteRoot(categoryRootPath));
        return getCategory(cms, resource);
    }

    /**
     * Returns all category repositories for the given reference path.<p>
     * 
     * @param cms the cms context
     * @param referencePath the reference path
     * 
     * @return a list of root paths
     */
    public List<String> getCategoryRepositories(CmsObject cms, String referencePath) {

        List<String> ret = new ArrayList<String>();
        if (referencePath == null) {
            ret.add(CmsCategoryService.CENTRALIZED_REPOSITORY);
            return ret;
        }
        String path = referencePath;
        if (!CmsResource.isFolder(path)) {
            path = CmsResource.getParentFolder(path);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            path = "/";
        }
        String categoryBase = getRepositoryBaseFolderName(cms);
        do {
            String repositoryPath = internalCategoryRootPath(path, categoryBase);
            if (cms.existsResource(repositoryPath)) {
                ret.add(repositoryPath);
            }
            path = CmsResource.getParentFolder(path);
        } while (path != null);
        ret.add(CmsCategoryService.CENTRALIZED_REPOSITORY);
        // the order is important in case of conflicts
        Collections.reverse(ret);
        return ret;
    }

    /**
     * Returns the category repositories base folder name.<p>
     * 
     * @param cms the cms context
     * 
     * @return the category repositories base folder name
     */
    public String getRepositoryBaseFolderName(CmsObject cms) {

        try {
            String value = cms.readPropertyObject(
                CmsCategoryService.CENTRALIZED_REPOSITORY,
                CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                false).getValue(CmsCategoryService.REPOSITORY_BASE_FOLDER);
            if (!value.endsWith("/")) {
                value += "/";
            }
            if (!value.startsWith("/")) {
                value = "/" + value;
            }
            return value;
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return CmsCategoryService.REPOSITORY_BASE_FOLDER;
        }
    }

    /**
     * Renames/Moves a category from the old path to the new one.<p>
     * 
     * @param cms the current cms context
     * @param oldCatPath the path of the category to move
     * @param newCatPath the new category path
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #moveCategory(CmsObject, String, String, String)} instead
     */
    public void moveCategory(CmsObject cms, String oldCatPath, String newCatPath) throws CmsException {

        moveCategory(cms, oldCatPath, newCatPath, null);
    }

    /**
     * Renames/Moves a category from the old path to the new one.<p>
     * 
     * This method will keep all categories in their original repository.<p>
     * 
     * @param cms the current cms context
     * @param oldCatPath the path of the category to move
     * @param newCatPath the new category path
     * @param referencePath the reference path to find the category
     * 
     * @throws CmsException if something goes wrong
     */
    public void moveCategory(CmsObject cms, String oldCatPath, String newCatPath, String referencePath)
    throws CmsException {

        CmsCategory category = readCategory(cms, oldCatPath, referencePath);
        String catPath = cms.getRequestContext().removeSiteRoot(category.getRootPath());
        CmsLock lock = cms.getLock(catPath);
        if (lock.isNullLock()) {
            cms.lockResource(catPath);
        } else if (lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
            cms.changeLock(catPath);
        }
        cms.moveResource(
            catPath,
            cms.getRequestContext().removeSiteRoot(internalCategoryRootPath(category.getBasePath(), newCatPath)));
    }

    /**
     * Reads all first level categories, including sub categories if needed.<p> 
     * 
     * @param cms the current cms context
     * @param includeSubCats flag to indicate if sub categories should also be read
     * 
     * @return a list of {@link CmsCategory} objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readCategories(CmsObject, String, boolean, String)} instead
     */
    public List<CmsCategory> readAllCategories(CmsObject cms, boolean includeSubCats) throws CmsException {

        return readCategories(cms, null, includeSubCats, null);
    }

    /**
     * Returns all categories given some search parameters.<p>
     * 
     * @param cms the current cms context
     * @param parentCategoryPath the path of the parent category to get the categories for
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param referencePath the reference path to find all the category repositories
     * 
     * @return a list of {@link CmsCategory} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsCategory> readCategories(
        CmsObject cms,
        String parentCategoryPath,
        boolean includeSubCats,
        String referencePath) throws CmsException {

        List repositories = getCategoryRepositories(cms, referencePath);
        return readCategoriesForRepositories(cms, parentCategoryPath, includeSubCats, repositories);
    }

    /**
     * Returns all categories given some search parameters.<p>
     * 
     * @param cms the current cms context
     * @param parentCategoryPath the path of the parent category to get the categories for
     * @param includeSubCats if to include all categories, or first level child categories only
     * @param repositories a list of root paths
     * @return a list of {@link CmsCategory} objects
     * @throws CmsException  if something goes wrong
     */
    public List<CmsCategory> readCategoriesForRepositories(
        CmsObject cms,
        String parentCategoryPath,
        boolean includeSubCats,
        List repositories) throws CmsException {

        String catPath = parentCategoryPath;
        if (catPath == null) {
            catPath = "";
        }
        Set<CmsCategory> cats = new HashSet<CmsCategory>();
        // traverse in reverse order, to ensure the set will contain most global categories
        Iterator it = repositories.iterator();
        while (it.hasNext()) {
            String repository = (String)it.next();
            try {
                cats.addAll(internalReadSubCategories(
                    cms,
                    internalCategoryRootPath(repository, catPath),
                    includeSubCats));
            } catch (CmsVfsResourceNotFoundException e) {
                // it may be that the given category is not defined in this repository
                // just ignore
            }
        }
        List<CmsCategory> ret = new ArrayList<CmsCategory>(cats);
        Collections.sort(ret);
        return ret;
    }

    /**
     * Reads the category identified by the given category path.<p>
     * 
     * This method will only lookup in the centralized repository.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read
     * 
     * @return the category
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readCategory(CmsObject, String, String)} instead
     */
    public CmsCategory readCategory(CmsObject cms, String categoryPath) throws CmsException {

        return getCategory(cms, internalCategoryRootPath(CmsCategoryService.CENTRALIZED_REPOSITORY, categoryPath));
    }

    /**
     * Reads all categories identified by the given category path for the given reference path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read
     * @param referencePath the reference path to find all the category repositories
     * 
     * @return a list of matching categories, could also be empty, if no category exists with the given path
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsCategory readCategory(CmsObject cms, String categoryPath, String referencePath) throws CmsException {

        // iterate all possible category repositories, starting with the most global one
        Iterator it = getCategoryRepositories(cms, referencePath).iterator();
        while (it.hasNext()) {
            String repository = (String)it.next();
            try {
                return getCategory(cms, internalCategoryRootPath(repository, categoryPath));
            } catch (CmsVfsResourceNotFoundException e) {
                // throw the exception if no repository left
                if (!it.hasNext()) {
                    throw e;
                }
            }
        }
        // this will never be executed
        return null;
    }

    /**
     * Reads the resources for a category identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read the resources for
     * @param recursive <code>true</code> if including sub-categories
     * 
     * @return a list of {@link CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readCategoryResources(CmsObject, String, boolean, String)} instead
     */
    public List readCategoryResources(CmsObject cms, String categoryPath, boolean recursive) throws CmsException {

        return readCategoryResources(cms, categoryPath, recursive, null);
    }

    /**
     * Reads the resources for a category identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read the resources for
     * @param recursive <code>true</code> if including sub-categories
     * @param referencePath the reference path to find all the category repositories
     * 
     * @return a list of {@link CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readCategoryResources(
        CmsObject cms,
        String categoryPath,
        boolean recursive,
        String referencePath) throws CmsException {

        return readCategoryResources(cms, categoryPath, recursive, referencePath, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads the resources for a category identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read the resources for
     * @param recursive <code>true</code> if including sub-categories
     * @param referencePath the reference path to find all the category repositories
     * @param resFilter the resource filter to use
     * 
     * @return a list of {@link CmsResource} objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readCategoryResources(
        CmsObject cms,
        String categoryPath,
        boolean recursive,
        String referencePath,
        CmsResourceFilter resFilter) throws CmsException {

        Set<CmsResource> resources = new HashSet<CmsResource>();
        CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.CATEGORY);
        if (recursive) {
            filter = filter.filterIncludeChildren();
        }
        CmsCategory category = readCategory(cms, categoryPath, referencePath);
        Iterator<CmsRelation> itRelations = cms.getRelationsForResource(
            cms.getRequestContext().removeSiteRoot(category.getRootPath()),
            filter).iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = itRelations.next();
            try {
                resources.add(relation.getSource(cms, resFilter));
            } catch (CmsException e) {
                // source does not match the filter
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
        }
        List<CmsResource> result = new ArrayList<CmsResource>(resources);
        Collections.sort(result);
        return result;
    }

    /**
     * Reads the categories for a resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to get the categories for
     * 
     * @return the categories list
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsCategory> readResourceCategories(CmsObject cms, CmsResource resource) throws CmsException {

        return internalReadResourceCategories(cms, resource, false);
    }

    /**
     * Reads the categories for a resource identified by the given resource name.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the path of the resource to get the categories for
     * 
     * @return the categories list
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsCategory> readResourceCategories(CmsObject cms, String resourceName) throws CmsException {

        return internalReadResourceCategories(cms, cms.readResource(resourceName), false);
    }

    /**
     * Returns all sub categories of the given category.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to get the sub categories for
     * @param includeSubCats if to include sub-subcategories
     * 
     * @return a list of {@link CmsCategory} objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readCategories(CmsObject, String, boolean, String)} instead
     */
    public List readSubCategories(CmsObject cms, String categoryPath, boolean includeSubCats) throws CmsException {

        return readCategories(cms, categoryPath, includeSubCats, null);
    }

    /**
     * Removes a resource identified by the given resource name from the given category.<p>
     * 
     * The resource has to be previously locked.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to remove
     * @param category the category to remove the resource from
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromCategory(CmsObject cms, String resourceName, CmsCategory category)
    throws CmsException {

        // remove the resource just from this category
        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        filter = filter.filterType(CmsRelationType.CATEGORY);
        filter = filter.filterResource(cms.readResource(cms.getRequestContext().removeSiteRoot(category.getRootPath())));
        filter = filter.filterIncludeChildren();
        cms.deleteRelationsFromResource(resourceName, filter);
    }

    /**
     * Removes a resource identified by the given resource name from the category
     * identified by the given category path.<p>
     * 
     * The resource has to be previously locked.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to remove
     * @param categoryPath the path of the category to remove the resource from
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromCategory(CmsObject cms, String resourceName, String categoryPath) throws CmsException {

        CmsCategory category = readCategory(cms, categoryPath, resourceName);
        removeResourceFromCategory(cms, resourceName, category);
    }

    /**
     * Repairs broken category relations.<p>
     * 
     * This could be caused by renaming/moving a category folder,
     * or changing the category repositories base folder name.<p>
     * 
     * Also repairs problems when creating/deleting conflicting
     * category folders across several repositories.<p>
     * 
     * The resource has to be previously locked.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to repair
     * 
     * @throws CmsException if something goes wrong 
     */
    public void repairRelations(CmsObject cms, CmsResource resource) throws CmsException {

        internalReadResourceCategories(cms, resource, true);
    }

    /**
     * Repairs broken category relations.<p>
     * 
     * This could be caused by renaming/moving a category folder,
     * or changing the category repositories base folder name.<p>
     * 
     * Also repairs problems when creating/deleting conflicting
     * category folders across several repositories.<p>
     * 
     * The resource has to be previously locked.<p>
     * 
     * @param cms the cms context
     * @param resourceName the site relative path to the resource to repair
     * 
     * @throws CmsException if something goes wrong 
     */
    public void repairRelations(CmsObject cms, String resourceName) throws CmsException {

        repairRelations(cms, cms.readResource(resourceName));
    }

    /**
     * Composes the category root path by appending the category path to the given category repository path.<p> 
     * 
     * @param basePath the category repository path
     * @param categoryPath the category path
     * 
     * @return the category root path
     */
    private String internalCategoryRootPath(String basePath, String categoryPath) {

        if (categoryPath.startsWith("/") && basePath.endsWith("/")) {
            // one slash too much
            return basePath + categoryPath.substring(1);
        } else if (!categoryPath.startsWith("/") && !basePath.endsWith("/")) {
            // one slash too less
            return basePath + "/" + categoryPath;
        } else {
            return basePath + categoryPath;
        }
    }

    /**
     * Reads/Repairs the categories for a resource identified by the given resource name.<p>
     * 
     * For reparation, the resource has to be previously locked.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to get the categories for
     * @param repair if to repair broken relations
     * 
     * @return the categories list
     * 
     * @throws CmsException if something goes wrong
     */
    private List internalReadResourceCategories(CmsObject cms, CmsResource resource, boolean repair)
    throws CmsException {

        List result = new ArrayList();
        String baseFolder = null;
        Iterator itRelations = cms.getRelationsForResource(
            resource,
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY)).iterator();
        if (repair && itRelations.hasNext()) {
            baseFolder = getRepositoryBaseFolderName(cms);
        }
        String resourceName = cms.getSitePath(resource);
        boolean repaired = false;
        while (itRelations.hasNext()) {
            CmsRelation relation = (CmsRelation)itRelations.next();
            try {
                CmsResource res = relation.getTarget(cms, CmsResourceFilter.DEFAULT_FOLDERS);
                CmsCategory category = getCategory(cms, res);
                if (!repair) {
                    result.add(category);
                } else {
                    CmsCategory actualCat = readCategory(cms, category.getPath(), resourceName);
                    if (!category.getId().equals(actualCat.getId())) {
                        // repair broken categories caused by creation/deletion of 
                        // category folders across several repositories
                        CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY).filterResource(
                            res);
                        cms.deleteRelationsFromResource(resourceName, filter);
                        repaired = true;
                        // set the right category
                        String catPath = cms.getRequestContext().removeSiteRoot(actualCat.getRootPath());
                        cms.addRelationToResource(resourceName, catPath, CmsRelationType.CATEGORY.getName());
                    }
                    result.add(actualCat);
                }
            } catch (CmsException e) {
                if (!repair) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                } else {
                    // repair broken categories caused by moving category folders
                    // could also happen when deleting an assigned category folder
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                    CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY).filterPath(
                        relation.getTargetPath());
                    if (!relation.getTargetId().isNullUUID()) {
                        filter = filter.filterStructureId(relation.getTargetId());
                    }
                    cms.deleteRelationsFromResource(resourceName, filter);
                    repaired = true;
                    // try to set the right category again
                    try {
                        CmsCategory actualCat = readCategory(
                            cms,
                            CmsCategory.getCategoryPath(relation.getTargetPath(), baseFolder),
                            resourceName);
                        addResourceToCategory(cms, resourceName, actualCat);
                        result.add(actualCat);
                    } catch (CmsException ex) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(e.getLocalizedMessage(), ex);
                        }
                    }
                }
            }
        }
        if (!repair) {
            Collections.sort(result);
        } else if (repaired) {
            // be sure that no higher level category is missing
            Iterator it = result.iterator();
            while (it.hasNext()) {
                CmsCategory category = (CmsCategory)it.next();
                addResourceToCategory(cms, resourceName, category.getPath());
            }
        }
        return result;
    }

    /**
     * Returns all sub categories of the given one, including sub sub categories if needed.<p>
     * 
     * @param cms the current cms context
     * @param rootPath the base category's root path (this category is not part of the result)
     * @param includeSubCats flag to indicate if sub categories should also be read
     * 
     * @return a list of {@link CmsCategory} objects
     * 
     * @throws CmsException if something goes wrong
     */
    private List<CmsCategory> internalReadSubCategories(CmsObject cms, String rootPath, boolean includeSubCats)
    throws CmsException {

        List<CmsCategory> categories = new ArrayList<CmsCategory>();
        List<CmsResource> resources = cms.readResources(
            cms.getRequestContext().removeSiteRoot(rootPath),
            CmsResourceFilter.DEFAULT.addRequireType(CmsResourceTypeFolder.RESOURCE_TYPE_ID),
            includeSubCats);
        Iterator<CmsResource> it = resources.iterator();
        while (it.hasNext()) {
            CmsResource resource = it.next();
            categories.add(getCategory(cms, resource));
        }
        return categories;
    }
}
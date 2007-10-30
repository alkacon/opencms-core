/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsCategoryService.java,v $
 * Date   : $Date: 2007/10/30 09:22:17 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides several simplified methods for manipulating category relations.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.9.2
 */
public class CmsCategoryService {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCategoryService.class);

    /** The default base path for categories. */
    private static final String BASE_PATH = "/system/categories/";

    /** The singleton instance. */
    private static CmsCategoryService m_instance;

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
     * Adds a resource identified by the given resource name to the category
     * identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to add
     * @param categoryPath the path of the category to add the resource to
     * 
     * @throws CmsException if something goes wrong
     */
    public void addResourceToCategory(CmsObject cms, String resourceName, String categoryPath) throws CmsException {

        CmsCategory category = readCategory(cms, categoryPath);
        if (!readResourceCategories(cms, resourceName).contains(category)) {
            cms.addRelationToResource(
                resourceName,
                getCategoryFolderPath(categoryPath),
                CmsRelationType.CATEGORY.getName());

            // recursively add to higher level categories
            if (categoryPath.endsWith("/")) {
                categoryPath = categoryPath.substring(0, categoryPath.lastIndexOf("/"));
            }
            if (categoryPath.lastIndexOf('/') > 0) {
                addResourceToCategory(cms, resourceName, categoryPath.substring(0, categoryPath.lastIndexOf('/') + 1));
            }
        }
    }

    /**
     * Creates a new category, which is just a simplification of a folder under /system/categories/.<p>
     * 
     * @param cms the current cms context
     * @param parent the parent category or <code>null</code> for a new top level category
     * @param name the (file-) name for the new category 
     * @param title the title
     * @param description the description
     * 
     * @return the new created category 
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsCategory createCategory(CmsObject cms, CmsCategory parent, String name, String title, String description)
    throws CmsException {

        List properties = new ArrayList();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(description)) {
            properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, description, null));
        }
        String folderPath = "";
        if (parent != null) {
            folderPath += parent.getPath() + "/";
        }
        if (name.startsWith("/")) {
            folderPath += name.substring(1);
        } else {
            folderPath += name;
        }
        folderPath = getCategoryFolderPath(folderPath);
        CmsResource resource;
        try {
            resource = cms.createResource(folderPath, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, properties);
        } catch (CmsVfsResourceNotFoundException e) {
            // if parent not found, create it
            cms.createResource(BASE_PATH, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
            // now try again
            resource = cms.createResource(folderPath, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, properties);
        }
        return new CmsCategory(resource.getStructureId(), getCategoryPath(folderPath), title, description);
    }

    /**
     * Deletes the category identified by the given path.<p> 
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to delete
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteCategory(CmsObject cms, String categoryPath) throws CmsException {

        cms.deleteResource(getCategoryFolderPath(categoryPath), CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Returns an OpenCms VFS root path for the given category path.<p>
     * 
     * @param categoryPath the category path to compute the root path for
     * 
     * @return an OpenCms VFS root path for the given category path
     */
    private String getCategoryFolderPath(String categoryPath) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(categoryPath)) {
            return null;
        }
        String path = categoryPath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return BASE_PATH + path;
    }

    /**
     * Returns a category path from the given OpenCms VFS root path.<p>
     * 
     * @param rootPath the OpenCms VFS root path to compute the category path for
     * 
     * @return a category path
     */
    private String getCategoryPath(String rootPath) {

        if (rootPath.startsWith(BASE_PATH)) {
            return rootPath.substring(BASE_PATH.length());
        }
        return null;
    }

    /**
     * Returns all sub categories of the given one, including sub categories if needed.<p>
     * 
     * @param cms the current cms context
     * @param baseCategory the path of the base category (this category is not part of the result)
     * @param includeSubCats flag to indicate if sub categories should also be read
     * 
     * @return a list of {@link CmsCategory} objects
     * 
     * @throws CmsException if something goes wrong
     */
    private List internalReadCategories(CmsObject cms, String baseCategory, boolean includeSubCats) throws CmsException {

        List resources = Collections.EMPTY_LIST;
        try {
            resources = cms.readResources(
                BASE_PATH + baseCategory,
                CmsResourceFilter.DEFAULT.addRequireType(CmsResourceTypeFolder.RESOURCE_TYPE_ID),
                includeSubCats);
        } catch (CmsVfsResourceNotFoundException cvrnfe) {
            // create the category folder, only if the base category folder is missing: 
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(baseCategory)) {
                Locale wpLocale = OpenCms.getWorkplaceManager().getDefaultLocale();
                try {
                    cms.createResource(BASE_PATH, CmsResourceTypeFolder.getStaticTypeId());
                } catch (Exception ex) {
                    LOG.error(Messages.get().getBundle(wpLocale).key(
                        "LOG_ERR_CREATE_CATEGORY_FOLDER_1",
                        new String[] {BASE_PATH}));
                }
            }
        }
        List categories = new ArrayList();
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            CmsResource resource = (CmsResource)it.next();
            CmsProperty title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
            CmsProperty description = cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false);
            categories.add(new CmsCategory(
                resource.getStructureId(),
                getCategoryPath(resource.getRootPath()),
                title.getValue(),
                description.getValue()));
        }
        return categories;
    }

    /**
     * Renames/Moves a category from the olda path to the new one.<p>
     * 
     * @param cms the current cms context
     * @param oldCatPath the path of the category to move
     * @param newCatPath the new category path
     * 
     * @throws CmsException if something goes wrong
     */
    public void moveCategory(CmsObject cms, String oldCatPath, String newCatPath) throws CmsException {

        cms.moveResource(getCategoryFolderPath(oldCatPath), getCategoryFolderPath(newCatPath));
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
     */
    public List readAllCategories(CmsObject cms, boolean includeSubCats) throws CmsException {

        return internalReadCategories(cms, "", includeSubCats);
    }

    /**
     * Reads the category identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param categoryPath the path of the category to read
     * 
     * @return the category
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsCategory readCategory(CmsObject cms, String categoryPath) throws CmsException {

        CmsResource resource = cms.readResource(getCategoryFolderPath(categoryPath));
        CmsProperty title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false);
        CmsProperty description = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        return new CmsCategory(
            resource.getStructureId(),
            getCategoryPath(resource.getRootPath()),
            title.getValue(),
            description.getValue());
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
    public List readResourceCategories(CmsObject cms, String resourceName) throws CmsException {

        List result = new ArrayList();
        Iterator itRelations = cms.getRelationsForResource(
            resourceName,
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY)).iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = (CmsRelation)itRelations.next();
            try {
                CmsResource resource = relation.getTarget(cms, CmsResourceFilter.DEFAULT_FOLDERS);
                CmsCategory category = new CmsCategory(
                    resource.getStructureId(),
                    resource.getRootPath().substring(BASE_PATH.length()),
                    cms.readPropertyObject(resource, "Title", false).getValue(),
                    cms.readPropertyObject(resource, "Description", false).getValue());
                result.add(category);
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
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
     */
    public List readSubCategories(CmsObject cms, String categoryPath, boolean includeSubCats) throws CmsException {

        readCategory(cms, categoryPath); // check the category exists
        return internalReadCategories(cms, categoryPath, includeSubCats);
    }

    /**
     * Removes a resource identified by the given resource name from the category
     * identified by the given category path.<p>
     * 
     * @param cms the current cms context
     * @param resourceName the site relative path to the resource to remove
     * @param categoryPath the path of the category to remove the resource from
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromCategory(CmsObject cms, String resourceName, String categoryPath) throws CmsException {

        // check the category exists
        readCategory(cms, categoryPath);

        // remove the resource just from this category
        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        filter = filter.filterType(CmsRelationType.CATEGORY);
        filter = filter.filterResource(cms.readResource(getCategoryFolderPath(categoryPath)));
        filter = filter.filterIncludeChildren();
        cms.deleteRelationsFromResource(resourceName, filter);
    }
}
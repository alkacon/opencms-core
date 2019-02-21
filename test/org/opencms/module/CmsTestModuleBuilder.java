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

package org.opencms.module;

import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for creating modules for test purposes.<p>
 */
public class CmsTestModuleBuilder {

    /**
     * Represents a type to be added.<p>
     */
    class TypeEntry {

        /** Type id. */
        private int m_id;

        /** Type name. */
        private String m_name;

        /**
         * Creates a new instance.<p>
         *
         * @param name the type name
         * @param id the type id
         */
        public TypeEntry(String name, int id) {

            m_name = name;
            m_id = id;
        }

        /**
         * Gets the id.<p>
         *
         * @return the id
         */
        public int getId() {

            return m_id;
        }

        /**
         * Gets the name.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The CMS object. */
    private CmsObject m_cms;

    /** The export points. */
    private List<CmsExportPoint> m_exportPoints = new ArrayList<>();

    /** The import script. */
    private String m_importScript;

    /** The module. */
    private CmsModule m_module;

    /** The module name. */
    private String m_moduleName;

    /** The structure id for the next resource. */
    private CmsUUID m_nextStructureId;

    /** The type entries. */
    private List<TypeEntry> m_typeEntries = new ArrayList<>();

    /** The next resource id. */
    private CmsUUID m_nextResourceId;

    /** Module resources to set. */
    private List<String> m_moduleResources;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param moduleName the module name
     */
    public CmsTestModuleBuilder(CmsObject cms, String moduleName) {

        m_cms = cms;
        m_moduleName = moduleName;
    }

    /**
     * Adds file using absolute path.
     *
     * @param type file type
     * @param text file content
     * @param path absolute file path
     *
     * @return the created resource
     *
     * @throws CmsLoaderException
     * @throws CmsException
     */
    public CmsResource addAbsoluteFile(String type, String text, String path) throws CmsLoaderException, CmsException {

        String name = CmsFileUtil.removeTrailingSeparator(CmsResource.getName(path));
        CmsUUID structureId = createStructureId(name);
        CmsUUID resourceId = createResourceId(name);

        long dummyTime = 1000000;
        CmsUUID userId = m_cms.getRequestContext().getCurrentUser().getId();
        byte[] data = getBytes(text);
        int length = data == null ? 0 : data.length;
        // create a new CmsResource
        CmsResource resource = new CmsResource(
            structureId,
            resourceId,
            path,
            OpenCms.getResourceManager().getResourceType(type),
            0,
            m_cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            dummyTime,
            userId,
            dummyTime,
            userId,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            length, // size
            System.currentTimeMillis(),
            0);
        return importResource(path, resource, data, new ArrayList<>());
    }

    /**
     * Adds folder using absolute file path.
     *
     * @param path the path
     *
     * @throws CmsLoaderException
     * @throws CmsException
     */
    public void addAbsoluteFolder(String path) throws CmsLoaderException, CmsException {

        String name = CmsFileUtil.removeTrailingSeparator(CmsResource.getName(path));
        CmsUUID structureId = createStructureId(name);
        CmsUUID resourceId = CmsUUID.getConstantUUID("r-" + name);
        long dummyTime = 1000000;
        CmsUUID userId = m_cms.getRequestContext().getCurrentUser().getId();
        // create a new CmsResource
        CmsResource resource = new CmsResource(
            structureId,
            resourceId,
            path,
            OpenCms.getResourceManager().getResourceType("folder"),
            0,
            m_cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            dummyTime,
            userId,
            dummyTime,
            userId,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            -1, // sizem_module
            System.currentTimeMillis(),
            0);
        importResource(path, resource, null, new ArrayList<>());
    }

    /**
     * Instead of adding the module with the name passed in the constructor, add it with  a different name and explicitly set
     * list of module resources. This is used to simulate modules like the Apollo modules which have their resources under system/modules/org.opencms.apollo,
     * which is not a module resource itself.
     *
     * @param moduleName the real module name
     * @param moduleResources the module resource list
     *
     * @throws CmsException if something goes wrong
     */
    public void addExplodedModule(String moduleName, List<String> moduleResources) throws CmsException {

        m_module = new CmsModule();
        m_module.setReducedExportMode(true);
        m_module.setName(moduleName);
        if (m_importScript != null) {
            m_module.setImportScript(m_importScript);
        }
        m_module.setResources(moduleResources);
        List<I_CmsResourceType> types = new ArrayList<>();
        for (TypeEntry entry : m_typeEntries) {
            I_CmsResourceType type = createResourceType(entry.getName(), entry.getId());
            types.add(type);
        }
        m_module.setResourceTypes(types);
        OpenCms.getModuleManager().addModule(m_cms, m_module);
        OpenCms.getResourceManager().initialize(m_cms);

    }

    /**
     * Adds an export point.<p>
     *
     * @param exportPoint the export point
     */
    public void addExportPoint(CmsExportPoint exportPoint) {

        m_exportPoints.add(exportPoint);
    }

    /**
     * Adds a file.<p>
     *
     * @param type the file type
     * @param relPath the path relative to the base directory
     * @param text the file content
     * @return the resource which has been created
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResource addFile(String type, String relPath, String text) throws CmsException {

        String path = moduleToAbsolutePath(relPath);
        return addAbsoluteFile(type, text, path);
    }

    /**
     * Adds a folder.<p>
     *
     * @param relPath the path relative to the base path
     * @throws CmsException if something goes wrong
     */
    public void addFolder(String relPath) throws CmsException {

        String path = moduleToAbsolutePath(relPath);
        addAbsoluteFolder(path);
    }

    /**
     * Adds the module object to the module manager.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void addModule() throws CmsException {

        m_module = new CmsModule();
        m_module.setReducedExportMode(true);
        m_module.setName(m_moduleName);
        if (m_importScript != null) {
            m_module.setImportScript(m_importScript);
        }
        if (m_moduleResources == null) {
            m_moduleResources = Arrays.asList(moduleToAbsolutePath(""));
        }
        m_module.setResources(m_moduleResources);
        m_module.setExportPoints(m_exportPoints);
        List<I_CmsResourceType> types = new ArrayList<>();
        List<CmsExplorerTypeSettings> expTypes = new ArrayList<>();
        for (TypeEntry entry : m_typeEntries) {
            I_CmsResourceType type = createResourceType(entry.getName(), entry.getId());
            types.add(type);
            CmsExplorerTypeSettings expType = createExplorerType(entry.getName());
            expTypes.add(expType);
        }
        m_module.setResourceTypes(types);
        m_module.setExplorerTypes(expTypes);
        OpenCms.getModuleManager().addModule(m_cms, m_module);
        OpenCms.getResourceManager().initialize(m_cms);

    }

    /**
     * Adds te text file.<p>
     *
     * @param relPath the relative path
     * @param text the content
     *
     * @return the created resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource addTextFile(String relPath, String text) throws CmsException {

        String type = "plain";
        return addFile(type, relPath, text);
    }

    /**
     * Adds a resource type.<p>
     *
     * @param name the type name
     * @param id the type id
     */
    public void addType(String name, int id) {

        if (m_module != null) {
            throw new IllegalStateException("Must add types before call to addModule");
        }
        TypeEntry entry = new TypeEntry(name, id);
        m_typeEntries.add(entry);
    }

    /**
     * Creates an explorer type for the module.<p>
     *
     * @param name the type name
     * @return the explorer type
     */
    public CmsExplorerTypeSettings createExplorerType(String name) {

        CmsExplorerTypeSettings settings = new CmsExplorerTypeSettings();
        settings.setName(name);
        settings.setAddititionalModuleExplorerType(true);
        return settings;
    }

    /**
     * Creates a resource type for the module.<p>
     *
     * @param name the name
     * @param id the type id
     * @return the resource type
     *
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType createResourceType(String name, int id) throws CmsException {

        I_CmsResourceType type = new CmsResourceTypePlain();
        type.initConfiguration(name, "" + id, CmsResourceTypePlain.class.getName());
        type.setModuleName(m_moduleName);
        type.setAdditionalModuleResourceType(true);
        return type;

    }

    /**
     * Deletes the module.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void delete() throws CmsException {

        OpenCms.getModuleManager().deleteModule(m_cms, m_module.getName(), false, new CmsShellReport(Locale.ENGLISH));

    }

    /**
     * Exports the module to the given file.<p>
     *
     * @param filename the file path
     * @throws Exception if something goes wrong
     */
    public void export(String filename) throws Exception {

        CmsModuleImportExportHandler handler = CmsModuleImportExportHandler.getExportHandler(
            m_cms,
            OpenCms.getModuleManager().getModule(m_module.getName()),
            "test module export for " + m_moduleName);
        handler.setFileName(filename);
        OpenCms.getImportExportManager().exportData(m_cms, handler, new CmsShellReport(Locale.ENGLISH));

    }

    /**
     * Gets the module object.<p>
     *
     * @return the module object
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Prepends the base path to the given path.<p>
     * @param relPath a relative path
     * @return the absolute path
     */
    public String moduleToAbsolutePath(String relPath) {

        return CmsStringUtil.joinPaths("/system/modules", m_moduleName, relPath);
    }

    /**
     * Publishes the current project.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void publish() throws CmsException {

        try {
            OpenCms.getPublishManager().publishProject(m_cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } catch (CmsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the import script.<p>
     *
     * @param importScript the import script
     */
    public void setImportScript(String importScript) {

        m_importScript = importScript;
    }

    /**
     * Sets the initial module resources.
     *
     * @param moduleResources the module resources
     */
    public void setModuleResources(List<String> moduleResources) {

        m_moduleResources = moduleResources;
    }

    /**
     * Sets the next resource id.<p>
     *
     * @param nextResourceId the next resource id
     */
    public void setNextResourceId(CmsUUID nextResourceId) {

        m_nextResourceId = nextResourceId;
    }

    /**
     * Sets the next structure id (if not set, it will be automatically generated).<p>
     *
     * @param cmsUUID the next structure id
     */
    public void setNextStructureId(CmsUUID cmsUUID) {

        m_nextStructureId = cmsUUID;
    }

    /**
     * Updates the module resources.
     *
     * <p>Works only if the module has been added to the module manager already.
     *
     * @param moduleResources the module resources
     * @throws CmsException if something goes wrong
     */
    public void updateModuleResources(String... moduleResources) throws CmsException {

        CmsModule module = OpenCms.getModuleManager().getModule(m_module.getName());
        CmsModule copy = module.clone();
        copy.setResources(Arrays.asList(moduleResources));
        OpenCms.getModuleManager().updateModule(m_cms, copy);

    }

    /**
     * Gets the UTF-8 encoding of a string.<p>
     *
     * @param str a string
     * @return the UTF-8 encoding for the string
     */
    byte[] getBytes(String str) {

        if (str == null) {
            return null;
        }
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen
            return null;
        }
    }

    /**
     * Gets the next resource id.<p>
     *
     * If a resource id  has been set, it will be returned and cleared, otherwise the resource id will be generated from the parameter passed in.
     *
     * @param name the resource name
     * @return the new resource id
     */
    private CmsUUID createResourceId(String name) {

        CmsUUID result = m_nextResourceId;
        if (result == null) {
            result = CmsUUID.getConstantUUID("r-" + name);
        }
        m_nextResourceId = null;
        return result;

    }

    /**
     * Gets the next structure id.<p>
     *
     * If a structure  has been set, it will be returned and cleared, otherwise the structure id will be generated from the parameter passed in.
     *
     * @param name the resource name
     * @return the new structure id
     */
    private CmsUUID createStructureId(String name) {

        CmsUUID result = m_nextStructureId;
        if (result == null) {
            result = CmsUUID.getConstantUUID("s-" + name);
        }
        m_nextStructureId = null;
        return result;

    }

    /**
     * Imports a resource.<p>
     *
     * @param path the path
     * @param resource the resource to import
     * @param data the resource contetn
     * @param props the properties
     * @return the imported rsource
     *
     * @throws CmsException if something goes wrong
     */
    private CmsResource importResource(String path, CmsResource resource, byte[] data, List<CmsProperty> props)
    throws CmsException {

        return m_cms.importResource(path, resource, data, props);
    }

}

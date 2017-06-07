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

package org.opencms.module;

import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Describes an OpenCms module.<p>
 *
 * OpenCms modules provide a standard mechanism to extend the OpenCms functionality.
 * Modules can contain VFS data, Java classes and a number of configuration options.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.module.I_CmsModuleAction
 * @see org.opencms.module.A_CmsModuleAction
 */
public class CmsModule implements Comparable<CmsModule> {

    /** The available module export modes. */
    public enum ExportMode {
        /** Default export mode. */
        DEFAULT, /** Reduced export, that omits last modification information (dates and users). */
        REDUCED;

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return super.toString().toLowerCase();
        }
    }

    /** The default date for module created / installed if not provided. */
    public static final long DEFAULT_DATE = 0L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModule.class);

    /**
     * The module property key name to specifiy additional resources which are
     * part of a module outside of {system/modules}.
     */
    private static final String MODULE_PROPERTY_ADDITIONAL_RESOURCES = "additionalresources";

    /** Character to separate additional resources specified in the module properties.  */
    private static final String MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR = ";";

    /** The module action class name. */
    private String m_actionClass;

    /** Initialized module action instance. */
    private I_CmsModuleAction m_actionInstance;

    /** The email of the author of this module. */
    private String m_authorEmail;

    /** The name of the author of this module. */
    private String m_authorName;

    /** Flag to create the classes folders when creating the module. */
    private boolean m_createClassesFolder;

    /** Flag to create the elements folder when creating the module. */
    private boolean m_createElementsFolder;

    /** Flag to create the formatters folder when creating the module. */
    private boolean m_createFormattersFolder;

    /** Flag to create the lib folder when creating the module. */
    private boolean m_createLibFolder;

    /** Flag to create the module folder when creating the module. */
    private boolean m_createModuleFolder;

    /** Flag to create the resources folder when creating the module. */
    private boolean m_createResourcesFolder;

    /** Flag to create the schemas folder when creating the module. */
    private boolean m_createSchemasFolder;

    /** Flag to create the template folder when creating the module. */
    private boolean m_createTemplateFolder;

    /** The date this module was created by the author. */
    private long m_dateCreated;

    /** The date this module was installed. */
    private long m_dateInstalled;

    /** List of dependencies of this module. */
    private List<CmsModuleDependency> m_dependencies;

    /** The description of this module. */
    private String m_description;

    /** The explorer type settings. */
    private List<CmsExplorerTypeSettings> m_explorerTypeSettings;

    /** List of export points added by this module. */
    private List<CmsExportPoint> m_exportPoints;

    /** Indicates if this modules configuration has already been frozen. */
    private boolean m_frozen;

    /** The group of the module. */
    private String m_group;

    /** The script to execute when the module is imported. */
    private String m_importScript;

    /** The import site. */
    private String m_importSite;

    /** The name of this module, must be a valid Java package name. */
    private String m_name;

    /** The "nice" display name of this module. */
    private String m_niceName;

    /** A timestamp from the time this object was created. */
    private long m_objectCreateTime = System.currentTimeMillis();

    /** The additional configuration parameters of this module. */
    private SortedMap<String, String> m_parameters;

    /** The export mode to use for the module. */
    private ExportMode m_exportMode;

    /** List of VFS resources that belong to this module. */
    private List<String> m_resources;

    /** List of VFS resources that do not belong to this module.
     *  In particular used for files / folders in folders that belong to the module.
     */
    private List<String> m_excluderesources;

    /** The list of additional resource types. */
    private List<I_CmsResourceType> m_resourceTypes;

    /** The name of the user who installed this module. */
    private String m_userInstalled;

    /** The version of this module. */
    private CmsModuleVersion m_version;

    /**
     * Creates a new, empty CmsModule object.<p>
     */
    public CmsModule() {

        m_version = new CmsModuleVersion(CmsModuleVersion.DEFAULT_VERSION);
        m_resources = Collections.emptyList();
        m_excluderesources = Collections.emptyList();
        m_exportPoints = Collections.emptyList();
        m_dependencies = Collections.emptyList();
        m_exportMode = ExportMode.DEFAULT;
    }

    /**
     * Creates a new module description with the specified values.<p>
     *
     * @param name the name of this module, must be a valid Java package name
     * @param niceName the "nice" display name of this module
     * @param group the group of this module
     * @param actionClass the (optional) module class name
     * @param importScript the script to execute when the module is imported
     * @param importSite the site root into which this module should be imported
     * @param exportMode the export mode that should be used for the module
     * @param description the description of this module
     * @param version the version of this module
     * @param authorName the name of the author of this module
     * @param authorEmail the email of the author of this module
     * @param dateCreated the date this module was created by the author
     * @param userInstalled the name of the user who uploaded this module
     * @param dateInstalled the date this module was uploaded
     * @param dependencies a list of dependencies of this module
     * @param exportPoints a list of export point added by this module
     * @param resources a list of VFS resources that belong to this module
     * @param excluderesources a list of VFS resources that are exclude from the module's resources
     * @param parameters the parameters for this module
     */
    public CmsModule(
        String name,
        String niceName,
        String group,
        String actionClass,
        String importScript,
        String importSite,
        ExportMode exportMode,
        String description,
        CmsModuleVersion version,
        String authorName,
        String authorEmail,
        long dateCreated,
        String userInstalled,
        long dateInstalled,
        List<CmsModuleDependency> dependencies,
        List<CmsExportPoint> exportPoints,
        List<String> resources,
        List<String> excluderesources,
        Map<String, String> parameters) {

        super();
        m_name = name;
        setNiceName(niceName);
        setActionClass(actionClass);
        setGroup(group);

        m_exportMode = null == exportMode ? ExportMode.DEFAULT : exportMode;

        if (CmsStringUtil.isEmpty(description)) {
            m_description = "";
        } else {
            m_description = description;
        }
        m_version = version;
        if (CmsStringUtil.isEmpty(authorName)) {
            m_authorName = "";
        } else {
            m_authorName = authorName;
        }
        if (CmsStringUtil.isEmpty(authorEmail)) {
            m_authorEmail = "";
        } else {
            m_authorEmail = authorEmail;
        }
        // remove milisecounds
        m_dateCreated = (dateCreated / 1000L) * 1000L;
        if (CmsStringUtil.isEmpty(userInstalled)) {
            m_userInstalled = "";
        } else {
            m_userInstalled = userInstalled;
        }
        m_dateInstalled = (dateInstalled / 1000L) * 1000L;
        if (dependencies == null) {
            m_dependencies = Collections.emptyList();
        } else {
            m_dependencies = Collections.unmodifiableList(dependencies);
        }
        if (exportPoints == null) {
            m_exportPoints = Collections.emptyList();
        } else {
            m_exportPoints = Collections.unmodifiableList(exportPoints);
        }
        if (resources == null) {
            m_resources = Collections.emptyList();
        } else {
            m_resources = Collections.unmodifiableList(resources);
        }
        if (excluderesources == null) {
            m_excluderesources = Collections.emptyList();
        } else {
            m_excluderesources = Collections.unmodifiableList(excluderesources);
        }
        if (parameters == null) {
            m_parameters = new TreeMap<String, String>();
        } else {
            m_parameters = new TreeMap<String, String>(parameters);
        }
        m_importSite = importSite;

        m_importScript = importScript;

        initOldAdditionalResources();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MODULE_INSTANCE_CREATED_1, m_name));
        }
        m_resourceTypes = Collections.emptyList();
        m_explorerTypeSettings = Collections.emptyList();
    }

    /** Determines the resources that are:
     * <ul>
     *  <li>accessible with the provided {@link CmsObject},</li>
     *  <li>part of the <code>moduleResources</code> (or in a folder of these resources) and</li>
     *  <li><em>not</em> contained in <code>excludedResources</code> (or a folder of these resources).</li>
     * </ul>
     * and adds the to <code>result</code>
     *
     * @param result the resource list, that gets extended by the calculated resources.
     * @param cms the {@link CmsObject} used to read resources.
     * @param moduleResources the resources to include.
     * @param excludeResources the site paths of the resources to exclude.
     * @throws CmsException thrown if reading resources fails.
     */
    public static void addCalculatedModuleResources(
        List<CmsResource> result,
        final CmsObject cms,
        final List<CmsResource> moduleResources,
        final List<String> excludeResources) throws CmsException {

        for (CmsResource resource : moduleResources) {

            String sitePath = cms.getSitePath(resource);

            List<String> excludedSubResources = getExcludedForResource(sitePath, excludeResources);

            // check if resources have to be excluded
            if (excludedSubResources.isEmpty()) {
                // no resource has to be excluded - add the whole resource
                // (that is, also all resources in the folder, if the resource is a folder)
                result.add(resource);
            } else {
                // cannot add the complete resource (i.e., including the whole sub-tree)
                if (sitePath.equals(excludedSubResources.get(0))) {
                    // the resource itself is excluded -> do not add it and check the next resource
                    continue;
                }
                // try to include sub-resources.
                List<CmsResource> subResources = cms.readResources(sitePath, CmsResourceFilter.ALL, false);
                addCalculatedModuleResources(result, cms, subResources, excludedSubResources);
            }
        }

    }

    /** Calculates the resources belonging to the module, taking excluded resources and readability of resources into account,
     *  and returns site paths of the module resources.<p>
     *  For more details of the returned resource, see {@link #calculateModuleResources(CmsObject, CmsModule)}.
     *
     * @param cms the {@link CmsObject} used to read the resources.
     * @param module the module, for which the resources should be calculated
     * @return the calculated module resources
     * @throws CmsException thrown if reading resources fails.
     */
    public static List<String> calculateModuleResourceNames(final CmsObject cms, final CmsModule module)
    throws CmsException {

        // adjust the site root, if necessary
        CmsObject cmsClone = adjustSiteRootIfNecessary(cms, module);

        // calculate the module resources
        List<CmsResource> moduleResources = calculateModuleResources(cmsClone, module);

        // get the site paths
        List<String> moduleResouceNames = new ArrayList<String>(moduleResources.size());
        for (CmsResource resource : moduleResources) {
            moduleResouceNames.add(cmsClone.getSitePath(resource));
        }
        return moduleResouceNames;
    }

    /** Calculates and returns the resources belonging to the module, taking excluded resources and readability of resources into account.
     * The list of returned resources contains:
     * <ul>
     *  <li>Only resources that are readable (present at the system and accessible with the provided {@link CmsObject}</li>
     *  <li>Only the resource for a folder, if <em>all</em> resources in the folder belong to the module.</li>
     *  <li>Only resources that are specified as module resources and <em>not</em> excluded by the module's exclude resources.</li>
     * </ul>
     *
     * @param cms the {@link CmsObject} used to read the resources.
     * @param module the module, for which the resources should be calculated
     * @return the calculated module resources
     * @throws CmsException thrown if reading resources fails.
     */
    public static List<CmsResource> calculateModuleResources(final CmsObject cms, final CmsModule module)
    throws CmsException {

        CmsObject cmsClone = adjustSiteRootIfNecessary(cms, module);
        List<CmsResource> result = null;
        List<String> excluded = CmsFileUtil.removeRedundancies(module.getExcludeResources());
        excluded = removeNonAccessible(cmsClone, excluded);
        List<String> resourceSitePaths = CmsFileUtil.removeRedundancies(module.getResources());
        resourceSitePaths = removeNonAccessible(cmsClone, resourceSitePaths);

        List<CmsResource> moduleResources = new ArrayList<CmsResource>(resourceSitePaths.size());
        for (String resourceSitePath : resourceSitePaths) {
            // assumes resources are accessible - already checked aboveremoveNonAccessible
            CmsResource resource = cmsClone.readResource(resourceSitePath);
            moduleResources.add(resource);
        }

        if (excluded.isEmpty()) {
            result = moduleResources;
        } else {
            result = new ArrayList<CmsResource>();

            addCalculatedModuleResources(result, cmsClone, moduleResources, excluded);

        }
        return result;

    }

    /** Adjusts the site root and returns a cloned CmsObject, iff the module has set an import site that differs
     * from the site root of the CmsObject provided as argument. Otherwise returns the provided CmsObject unchanged.
     * @param cms The original CmsObject.
     * @param module The module where the import site is read from.
     * @return The original CmsObject, or, if necessary, a clone with adjusted site root
     * @throws CmsException see {@link OpenCms#initCmsObject(CmsObject)}
     */
    private static CmsObject adjustSiteRootIfNecessary(final CmsObject cms, final CmsModule module)
    throws CmsException {

        CmsObject cmsClone;
        if ((null == module.getImportSite()) || cms.getRequestContext().getSiteRoot().equals(module.getImportSite())) {
            cmsClone = cms;
        } else {
            cmsClone = OpenCms.initCmsObject(cms);
            cmsClone.getRequestContext().setSiteRoot(module.getImportSite());
        }

        return cmsClone;
    }

    /** Returns only the resource names starting with the provided <code>sitePath</code>.
     *
     * @param sitePath the site relative path, all paths should start with.
     * @param excluded the paths to filter.
     * @return the paths from <code>excluded</code>, that start with <code>sitePath</code>.
     */
    private static List<String> getExcludedForResource(final String sitePath, final List<String> excluded) {

        List<String> result = new ArrayList<String>();
        for (String exclude : excluded) {
            if (exclude.startsWith(sitePath)) {
                result.add(exclude);
            }
        }
        return result;
    }

    /** Removes the resources not accessible with the provided {@link CmsObject}.
     *
     * @param cms the {@link CmsObject} used to read the resources.
     * @param sitePaths site relative paths of the resources that should be checked for accessibility.
     * @return site paths of the accessible resources.
     */
    private static List<String> removeNonAccessible(CmsObject cms, List<String> sitePaths) {

        List<String> result = new ArrayList<String>(sitePaths.size());
        for (String sitePath : sitePaths) {
            if (cms.existsResource(sitePath, CmsResourceFilter.ALL)) {
                result.add(sitePath);
            }
        }
        return result;
    }

    /**
     * Checks if this module depends on another given module,
     * will return the dependency, or <code>null</code> if no dependency was found.<p>
     *
     * @param module the other module to check against
     * @return the dependency, or null if no dependency was found
     */
    public CmsModuleDependency checkDependency(CmsModule module) {

        CmsModuleDependency otherDepdendency = new CmsModuleDependency(module.getName(), module.getVersion());

        // loop through all the dependencies
        for (int i = 0; i < m_dependencies.size(); i++) {
            CmsModuleDependency dependency = m_dependencies.get(i);
            if (dependency.dependesOn(otherDepdendency)) {
                // short circuit here
                return dependency;
            }
        }

        // no dependency was found
        return null;
    }

    /**
     * Checks if all resources of the module are present.<p>
     *
     * @param cms an initialized OpenCms user context which must have read access to all module resources
     *
     * @throws CmsIllegalArgumentException in case not all module resources exist or can be read with the given OpenCms user context
     */
    public void checkResources(CmsObject cms) throws CmsIllegalArgumentException {

        CmsFileUtil.checkResources(cms, getResources());
    }

    /**
     * Clones a CmsModule which is not set to frozen.<p>
     * This clones module can be used to be update the module information.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        // create a copy of the module
        CmsModule result = new CmsModule(
            m_name,
            m_niceName,
            m_group,
            m_actionClass,
            m_importScript,
            m_importSite,
            m_exportMode,
            m_description,
            m_version,
            m_authorName,
            m_authorEmail,
            m_dateCreated,
            m_userInstalled,
            m_dateInstalled,
            m_dependencies,
            m_exportPoints,
            m_resources,
            m_excluderesources,
            m_parameters);
        // and set its frozen state to false
        result.m_frozen = false;

        if (getExplorerTypes() != null) {
            List<CmsExplorerTypeSettings> settings = new ArrayList<CmsExplorerTypeSettings>();
            for (CmsExplorerTypeSettings setting : getExplorerTypes()) {
                settings.add((CmsExplorerTypeSettings)setting.clone());
            }
            result.setExplorerTypes(settings);
        }
        if (getResourceTypes() != null) {
            // TODO: The resource types must be cloned also, otherwise modification will effect the origin also
            result.setResourceTypes(new ArrayList<I_CmsResourceType>(getResourceTypes()));
        }
        if (getDependencies() != null) {
            List<CmsModuleDependency> deps = new ArrayList<CmsModuleDependency>();
            for (CmsModuleDependency dep : getDependencies()) {
                deps.add((CmsModuleDependency)dep.clone());
            }
            result.setDependencies(new ArrayList<CmsModuleDependency>(getDependencies()));
        }
        if (getExportPoints() != null) {
            List<CmsExportPoint> exps = new ArrayList<CmsExportPoint>();
            for (CmsExportPoint exp : getExportPoints()) {
                exps.add((CmsExportPoint)exp.clone());
            }
            result.setExportPoints(exps);
        }

        result.setCreateClassesFolder(m_createClassesFolder);
        result.setCreateElementsFolder(m_createElementsFolder);
        result.setCreateLibFolder(m_createLibFolder);
        result.setCreateModuleFolder(m_createModuleFolder);
        result.setCreateResourcesFolder(m_createResourcesFolder);
        result.setCreateSchemasFolder(m_createSchemasFolder);
        result.setCreateTemplateFolder(m_createTemplateFolder);
        result.setCreateFormattersFolder(m_createFormattersFolder);

        result.setResources(new ArrayList<String>(m_resources));
        result.setExcludeResources(new ArrayList<String>(m_excluderesources));

        return result;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsModule obj) {

        if (obj == this) {
            return 0;
        }
        return m_name.compareTo(obj.m_name);
    }

    /**
     * Two instances of a module are considered equal if their name is equal.<p>
     *
     * @param obj the object to compare
     *
     * @return true if the objects are equal
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * @see #isIdentical(CmsModule)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsModule) {
            return ((CmsModule)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns the class name of this modules (optional) action class.<p>
     *
     * If this module does not use an action class,
     * <code>null</code> is returned.<p>
     *
     * @return the class name of this modules (optional) action class
     */
    public String getActionClass() {

        return m_actionClass;
    }

    /**
     * Returns the module action instance of this module, or <code>null</code>
     * if no module action instance is configured.<p>
     *
     * @return the module action instance of this module
     */
    public I_CmsModuleAction getActionInstance() {

        return m_actionInstance;
    }

    /**
     * Returns the email of the module author.<p>
     *
     * @return the email of the module author
     */
    public String getAuthorEmail() {

        return m_authorEmail;
    }

    /**
     * Returns the name of the author of this module.<p>
     *
     * @return the name of the author of this module
     */
    public String getAuthorName() {

        return m_authorName;
    }

    /**
     * Gets the module configuration path.<p>
     *
     * @return the module configuration path
     */
    public String getConfigurationPath() {

        String parameter = getParameter("config.sitemap");
        if (parameter != null) {
            return parameter;
        } else {
            return "/system/modules/" + getName() + "/.config";
        }
    }

    /**
     * Returns the date this module was created by the author.<p>
     *
     * @return the date this module was created by the author
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the date this module was uploaded.<p>
     *
     * @return the date this module was uploaded
     */
    public long getDateInstalled() {

        return m_dateInstalled;
    }

    /**
     * Returns the list of dependencies of this module.<p>
     *
     * @return the list of dependencies of this module
     */
    public List<CmsModuleDependency> getDependencies() {

        return m_dependencies;
    }

    /**
     * Returns the description of this module.<p>
     *
     * @return the description of this module
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the list of VFS resources that do not belong to this module.<p>
     * In particular, files / folders that would be included otherwise,
     * considering the module resources.<p>
     *
     * @return the list of VFS resources that do not belong to this module
     */
    public List<String> getExcludeResources() {

        return m_excluderesources;
    }

    /**
     * Returns the list of explorer resource types that belong to this module.<p>
     *
     * @return the list of explorer resource types that belong to this module
     */
    public List<CmsExplorerTypeSettings> getExplorerTypes() {

        return m_explorerTypeSettings;
    }

    /** Returns the export mode specified for the module.
     * @return the module's export mode.
     */
    public ExportMode getExportMode() {

        return m_exportMode;
    }

    /**
     * Returns the list of export point added by this module.<p>
     *
     * @return the list of export point added by this module
     */
    public List<CmsExportPoint> getExportPoints() {

        return m_exportPoints;
    }

    /**
     * Returns the group name of this module.<p>
     *
     * @return the group name of this module
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the importScript.<p>
     *
     * @return the importScript
     */
    public String getImportScript() {

        return m_importScript;
    }

    /**
     * Gets the import site.<p>
     *
     * If this is not empty, then it will be used as the site root for importing and exporting this module.<p>
     *
     * @return the import site
     */
    public String getImportSite() {

        return m_importSite;
    }

    /**
     * Returns the name of this module.<p>
     *
     * The module name must be a valid java package name.<p>
     *
     * @return the name of this module
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the "nice" display name of this module.<p>
     *
     * @return the "nice" display name of this module
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Gets the timestamp of this object's creation time.<p>
     *
     * @return the object creation timestamp
     */
    public long getObjectCreateTime() {

        return m_objectCreateTime;
    }

    /**
     * Returns a parameter value from the module parameters.<p>
     *
     * @param key the parameter to return the value for
     * @return the parameter value from the module parameters
     */
    public String getParameter(String key) {

        return m_parameters.get(key);
    }

    /**
     * Returns a parameter value from the module parameters,
     * or a given default value in case the parameter is not set.<p>
     *
     * @param key the parameter to return the value for
     * @param defaultValue the default value in case there is no value stored for this key
     * @return the parameter value from the module parameters
     */
    public String getParameter(String key, String defaultValue) {

        String value = m_parameters.get(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns the configured (immutable) module parameters.<p>
     *
     * @return the configured (immutable) module parameters
     */
    public SortedMap<String, String> getParameters() {

        return m_parameters;
    }

    /**
     * Returns the list of VFS resources that belong to this module.<p>
     *
     * @return the list of VFS resources that belong to this module
     */
    public List<String> getResources() {

        return m_resources;
    }

    /**
     * Returns the list of additional resource types that belong to this module.<p>
     *
     * @return the list of additional resource types that belong to this module
     */
    public List<I_CmsResourceType> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the name of the user who uploaded this module.<p>
     *
     * @return the name of the user who uploaded this module
     */
    public String getUserInstalled() {

        return m_userInstalled;
    }

    /**
     * Returns the version of this module.<p>
     *
     * @return the version of this module
     */
    public CmsModuleVersion getVersion() {

        return m_version;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Returns the createClassesFolder flag.<p>
     *
     * @return the createClassesFolder flag
     */
    public boolean isCreateClassesFolder() {

        return m_createClassesFolder;
    }

    /**
     * Returns the createElementsFolder flag.<p>
     *
     * @return the createElementsFolder flag
     */
    public boolean isCreateElementsFolder() {

        return m_createElementsFolder;
    }

    /**
     * Returns the createFormattersFolder flag.<p>
     *
     * @return the createFormattersFolder flag
     */
    public boolean isCreateFormattersFolder() {

        return m_createFormattersFolder;
    }

    /**
     * Returns the createLibFolder flag.<p>
     *
     * @return the createLibFolder flag
     */
    public boolean isCreateLibFolder() {

        return m_createLibFolder;
    }

    /**
     * Returns the createModuleFolder flag.<p>
     *
     * @return the createModuleFolder flag
     */
    public boolean isCreateModuleFolder() {

        return m_createModuleFolder;
    }

    /**
     * Returns the createResourcesFolder flag.<p>
     *
     * @return the createResourcesFolder flag
     */
    public boolean isCreateResourcesFolder() {

        return m_createResourcesFolder;
    }

    /**
     * Returns the createSchemasFolder flag.<p>
     *
     * @return the createSchemasFolder flag
     */
    public boolean isCreateSchemasFolder() {

        return m_createSchemasFolder;
    }

    /**
     * Returns the createTemplateFolder flag.<p>
     *
     * @return the createTemplateFolder flag
     */
    public boolean isCreateTemplateFolder() {

        return m_createTemplateFolder;
    }

    /**
     * Checks if this module is identical with another module.<p>
     *
     * Modules A, B are <b>identical</b> if <i>all</i> values of A are equal to B.
     * The values from {@link #getUserInstalled()} and {@link #getDateInstalled()}
     * are ignored for this test.<p>
     *
     * Modules A, B are <b>equal</b> if just the name of A is equal to the name of B.<p>
     *
     * @param other the module to compare with
     *
     * @return if the modules are identical
     *
     * @see #equals(Object)
     */
    public boolean isIdentical(CmsModule other) {

        // some code redundancy here but this is easier to debug
        if (!isEqual(m_name, other.m_name)) {
            return false;
        }
        if (!isEqual(m_niceName, other.m_niceName)) {
            return false;
        }
        if (!isEqual(m_version, other.m_version)) {
            return false;
        }
        if (!isEqual(m_actionClass, other.m_actionClass)) {
            return false;
        }
        if (!isEqual(m_description, other.m_description)) {
            return false;
        }
        if (!isEqual(m_authorName, other.m_authorName)) {
            return false;
        }
        if (!isEqual(m_authorEmail, other.m_authorEmail)) {
            return false;
        }
        if (m_dateCreated != other.m_dateCreated) {
            return false;
        }
        return true;
    }

    /** Checks, if the module should use the reduced export mode.
     * @return if reduce export mode should be used <code>true</code>, otherwise <code>false</code>.
     */
    public boolean isReducedExportMode() {

        return ExportMode.REDUCED.equals(m_exportMode);
    }

    /**
     * Sets the class name of this modules (optional) action class.<p>
     *
     * Providing <code>null</code> as a value indicates that this module does not use an action class.<p>
     *
     * <i>Please note:</i>It's not possible to set the action class name once the module
     * configuration has been frozen.<p>
     *
     * @param value the class name of this modules (optional) action class to set
     */
    public void setActionClass(String value) {

        checkFrozen();
        if (CmsStringUtil.isEmpty(value)) {
            m_actionClass = null;
        } else {
            if (!CmsStringUtil.isValidJavaClassName(value)) {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_MODULE_ACTION_CLASS_2, value, getName()));
            }
            m_actionClass = value;
        }
    }

    /**
     * Sets the author email of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the modules author email once the module
     * configuration has been frozen.<p>
     *
     * @param value the module description to set
     */
    public void setAuthorEmail(String value) {

        checkFrozen();
        m_authorEmail = value.trim();
    }

    /**
     * Sets the author name of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the modules author name once the module
     * configuration has been frozen.<p>
     *
     * @param value the module description to set
     */
    public void setAuthorName(String value) {

        checkFrozen();
        m_authorName = value.trim();
    }

    /**
     * Sets the createClassesFolder flag.<p>
     *
     * @param createClassesFolder the createClassesFolder flag to set
     */
    public void setCreateClassesFolder(boolean createClassesFolder) {

        m_createClassesFolder = createClassesFolder;
    }

    /**
     * Sets the createElementsFolder flag.<p>
     *
     * @param createElementsFolder the createElementsFolder flag to set
     */
    public void setCreateElementsFolder(boolean createElementsFolder) {

        m_createElementsFolder = createElementsFolder;
    }

    /**
     * Sets the createFormattersFolder flag.<p>
     *
     * @param createFormattersFolder the createFormattersFolder flag to set
     */
    public void setCreateFormattersFolder(boolean createFormattersFolder) {

        m_createFormattersFolder = createFormattersFolder;
    }

    /**
     * Sets the createLibFolder flag.<p>
     *
     * @param createLibFolder the createLibFolder flag to set
     */
    public void setCreateLibFolder(boolean createLibFolder) {

        m_createLibFolder = createLibFolder;
    }

    /**
     * Sets the createModuleFolder flag.<p>
     *
     * @param createModuleFolder the createModuleFolder flag to set
     */
    public void setCreateModuleFolder(boolean createModuleFolder) {

        m_createModuleFolder = createModuleFolder;
    }

    /**
     * Sets the createResourcesFolder flag.<p>
     *
     * @param createResourcesFolder the createResourcesFolder flag to set
     */
    public void setCreateResourcesFolder(boolean createResourcesFolder) {

        m_createResourcesFolder = createResourcesFolder;
    }

    /**
     * Sets the createSchemasFolder flag .<p>
     *
     * @param createSchemasFolder the createSchemasFolder flag to set
     */
    public void setCreateSchemasFolder(boolean createSchemasFolder) {

        m_createSchemasFolder = createSchemasFolder;
    }

    /**
     * Sets the createTemplateFolder flag .<p>
     *
     * @param createTemplateFolder the createTemplateFolder flag to set
     */
    public void setCreateTemplateFolder(boolean createTemplateFolder) {

        m_createTemplateFolder = createTemplateFolder;
    }

    /**
     * Sets the date created of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the module date created once the module
     * configuration has been frozen.<p>
     *
     * @param value the date created to set
     */
    public void setDateCreated(long value) {

        checkFrozen();
        m_dateCreated = value;
    }

    /**
     * Sets the installation date of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the installation date once the module
     * configuration has been frozen.<p>
     *
     * @param value the installation date this module
     */
    public void setDateInstalled(long value) {

        checkFrozen();
        m_dateInstalled = value;
    }

    /**
     * Sets the list of module dependencies.<p>
     *
     * @param dependencies list of module dependencies
     */
    public void setDependencies(List<CmsModuleDependency> dependencies) {

        checkFrozen();
        m_dependencies = dependencies;
    }

    /**
     * Sets the description of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the modules description once the module
     * configuration has been frozen.<p>
     *
     * @param value the module description to set
     */
    public void setDescription(String value) {

        checkFrozen();
        m_description = value.trim();
    }

    /**
     * Sets the resources excluded from this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the module resources once the module
     * configuration has been frozen.<p>
     *
     * @param value the resources to exclude from the module
     */
    public void setExcludeResources(List<String> value) {

        checkFrozen();
        m_excluderesources = value;
    }

    /**
     * Sets the additional explorer types that belong to this module.<p>
     *
     * @param explorerTypeSettings the explorer type settings.
     */
    public void setExplorerTypes(List<CmsExplorerTypeSettings> explorerTypeSettings) {

        m_explorerTypeSettings = explorerTypeSettings;
    }

    /**
     * Sets the export points of this module.<p>
     *
     * @param exportPoints the export points of this module.
     */
    public void setExportPoints(List<CmsExportPoint> exportPoints) {

        m_exportPoints = exportPoints;
    }

    /**
     * Sets the group name of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the modules group name once the module
     * configuration has been frozen.<p>
     *
     * @param value the module group name to set
     */
    public void setGroup(String value) {

        checkFrozen();
        m_group = value;
    }

    /**
     * Sets the importScript.<p>
     *
     * @param importScript the importScript to set
     */
    public void setImportScript(String importScript) {

        checkFrozen();
        m_importScript = importScript;
    }

    /**
     * Sets the import site.<p>
     *
     * @param importSite the import site
     */
    public void setImportSite(String importSite) {

        checkFrozen();
        if (importSite != null) {
            importSite = importSite.trim();
        }
        m_importSite = importSite;
    }

    /**
     * Sets the name of this module.<p>
     *
     * The module name must be a valid java package name.<p>
     *
     * <i>Please note:</i>It's not possible to set the modules name once the module
     * configuration has been frozen.<p>
     *
     * @param value the module name to set
     */
    public void setName(String value) {

        checkFrozen();
        if (!CmsStringUtil.isValidJavaClassName(value)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_MODULE_NAME_1, value));
        }
        m_name = value;
    }

    /**
     * Sets the "nice" display name of this module.<p>
     *
     * <i>Please note:</i>It's not possible to set the modules "nice" name once the module
     * configuration has been frozen.<p>
     *
     * @param value the "nice" display name of this module to set
     */
    public void setNiceName(String value) {

        checkFrozen();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            m_niceName = getName();
        } else {
            m_niceName = value.trim();
        }
    }

    /**
     * Sets the parameters of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the module parameters once the module
     * configuration has been frozen.<p>
     *
     * @param parameters the module parameters to set
     */
    public void setParameters(SortedMap<String, String> parameters) {

        checkFrozen();
        m_parameters = parameters;
    }

    /** Set/unset the reduced export mode.
     * @param reducedExportMode if <code>true</code>, the export mode is set to {@link ExportMode#REDUCED}, otherwise to {@link ExportMode#DEFAULT}.
     */
    public void setReducedExportMode(boolean reducedExportMode) {

        m_exportMode = reducedExportMode ? ExportMode.REDUCED : ExportMode.DEFAULT;
    }

    /**
     * Sets the resources of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the module resources once the module
     * configuration has been frozen.<p>
     *
     * @param value the module resources to set
     */
    public void setResources(List<String> value) {

        checkFrozen();
        m_resources = value;
    }

    /**
     * Sets the list of additional resource types that belong to this module.<p>
     *
     * @param resourceTypes list of additional resource types that belong to this module
     */
    public void setResourceTypes(List<I_CmsResourceType> resourceTypes) {

        m_resourceTypes = Collections.unmodifiableList(resourceTypes);
    }

    /**
     * Sets the user who installed of this module.<p>
     *
     *
     * <i>Please note:</i>It's not possible to set the user installed once the module
     * configuration has been frozen.<p>
     *
     * @param value the user who installed this module
     */
    public void setUserInstalled(String value) {

        checkFrozen();
        m_userInstalled = value.trim();
    }

    /**
     * Checks if this modules configuration is frozen.<p>
     *
     * @throws CmsIllegalArgumentException in case the configuration is already frozen
     */
    protected void checkFrozen() throws CmsIllegalArgumentException {

        if (m_frozen) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_MODULE_FROZEN_1, getName()));
        }
    }

    /**
     * Initializes this module, also freezing the module configuration.<p>
     *
     * @param cms an initialized OpenCms user context
     *
     * @throws CmsRoleViolationException if the given users does not have the <code>{@link CmsRole#DATABASE_MANAGER}</code> role
     */
    protected void initialize(CmsObject cms) throws CmsRoleViolationException {

        checkFrozen();
        // check if the user has the required permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        m_frozen = true;
        m_resources = Collections.unmodifiableList(m_resources);
        m_excluderesources = Collections.unmodifiableList(m_excluderesources);
    }

    /**
     * Sets the module action instance for this module.<p>
     *
     * @param actionInstance the module action instance for this module
     */
    /*package*/void setActionInstance(I_CmsModuleAction actionInstance) {

        m_actionInstance = actionInstance;

    }

    /**
     * Resolves the module property "additionalresources" to the resource list and
     * vice versa.<p>
     *
     * This "special" module property is required as long as we do not have a new
     * GUI for editing of module resource entries. Once we have the new GUI, the
     * handling of "additionalresources" will be moved to the import of the module
     * and done only if the imported module is a 5.0 module.<p>
     */
    private void initOldAdditionalResources() {

        SortedMap<String, String> parameters = new TreeMap<String, String>(m_parameters);
        List<String> resources = new ArrayList<String>(m_resources);

        String additionalResources;
        additionalResources = parameters.get(MODULE_PROPERTY_ADDITIONAL_RESOURCES);
        if (additionalResources != null) {
            StringTokenizer tok = new StringTokenizer(
                additionalResources,
                MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String resource = tok.nextToken().trim();
                if ((!"-".equals(resource)) && (!resources.contains(resource))) {
                    resources.add(resource);
                }
            }
        }

        m_resources = resources;
    }

    /**
     * Checks if two objects are either both null, or equal.<p>
     *
     * @param a the first object to check
     * @param b the second object to check
     * @return true if the two object are either both null, or equal
     */
    private boolean isEqual(Object a, Object b) {

        if (a == null) {
            return (b == null);
        }
        if (b == null) {
            return false;
        }
        return a.equals(b);
    }
}

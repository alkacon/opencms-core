/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModule.java,v $
 * Date   : $Date: 2005/06/12 11:18:21 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsConstants;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;

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
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3.6
 * 
 * @see org.opencms.module.I_CmsModuleAction
 * @see org.opencms.module.A_CmsModuleAction
 */
public class CmsModule implements Comparable {

    /** The default date for module created / installed if not provided. */
    public static final long C_DEFAULT_DATE = 0L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModule.class);

    /** The module action class name. */
    private String m_actionClass;

    /** The email of the author of this module. */
    private String m_authorEmail;

    /** The name of the author of this module. */
    private String m_authorName;

    /** The date this module was created by the author. */
    private long m_dateCreated;

    /** The date this module was installed. */
    private long m_dateInstalled;

    /** List of dependencies of this module. */
    private List m_dependencies;

    /** The description of this module. */
    private String m_description;

    /** The explorer type settings. */
    private List m_explorerTypeSettings;

    /** List of export points added by this module. */
    private List m_exportPoints;

    /** Indicates if this modules configuration has already been frozen. */
    private boolean m_frozen;

    /** The group of the module. */
    private String m_group;

    /** The name of this module, must be a valid Java package name. */
    private String m_name;

    /** The "nice" display name of this module. */
    private String m_niceName;

    /** The additional configuration parameters of this module. */
    private SortedMap m_parameters;

    /** List of VFS resources that belong to this module. */
    private List m_resources;

    /** The list of additional resource types. */
    private List m_resourceTypes;

    /** The name of the user who installed this module. */
    private String m_userInstalled;

    /** The version of this module. */
    private CmsModuleVersion m_version;

    /**
     * Creates a new, empty CmsModule object.<p>
     */
    public CmsModule() {

        m_version = new CmsModuleVersion(CmsModuleVersion.DEFAULT_VERSION);
        m_resources = Collections.EMPTY_LIST;
        // noop
    }

    /**
     * Creates a new module description with the specified values.<p>
     * 
     * @param name the name of this module, must be a valid Java package name
     * @param niceName the "nice" display name of this module
     * @param group the group of this module
     * @param actionClass the (optional) module class name
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
     * @param parameters the parameters for this module
     */
    public CmsModule(
        String name,
        String niceName,
        String group,
        String actionClass,
        String description,
        CmsModuleVersion version,
        String authorName,
        String authorEmail,
        long dateCreated,
        String userInstalled,
        long dateInstalled,
        List dependencies,
        List exportPoints,
        List resources,
        Map parameters) {

        super();
        m_name = name;
        setNiceName(niceName);
        setActionClass(actionClass);
        setGroup(group);

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
            m_dependencies = Collections.EMPTY_LIST;
        } else {
            m_dependencies = Collections.unmodifiableList(dependencies);
        }
        if (exportPoints == null) {
            m_exportPoints = Collections.EMPTY_LIST;
        } else {
            m_exportPoints = Collections.unmodifiableList(exportPoints);
        }
        if (resources == null) {
            m_resources = Collections.EMPTY_LIST;
        } else {
            m_resources = Collections.unmodifiableList(resources);
        }
        if (parameters == null) {
            //m_parameters = Collections.EMPTY_MAP;
            m_parameters = new TreeMap();
        } else {
            m_parameters = new TreeMap(parameters);
        }

        initOldAdditionalResources();

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_MODULE_INSTANCE_CREATED_1, m_name));
        }
        m_resourceTypes = Collections.EMPTY_LIST;
        m_explorerTypeSettings = Collections.EMPTY_LIST;
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
            CmsModuleDependency dependency = (CmsModuleDependency)m_dependencies.get(i);
            if (dependency.dependesOn(otherDepdendency)) {
                // short circuit here
                return dependency;
            }
        }

        // no dependency was found
        return null;
    }

    /**
     * Clones a CmsModule which is not set to frozen.<p>
     * This clones module can be used to be update the module information.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        // create a copy of the module
        CmsModule result = new CmsModule(
            m_name,
            m_niceName,
            m_group,
            m_actionClass,
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
            m_parameters);
        // and set its frozen state to false
        result.m_frozen = false;

        if (getExplorerTypes() != null) {
            result.setExplorerTypes(new ArrayList(getExplorerTypes()));
        }
        if (getResourceTypes() != null) {
            result.setResourceTypes(new ArrayList(getResourceTypes()));
        }

        if (getDependencies() != null) {
            result.setDependencies(new ArrayList(getDependencies()));
        }

        result.setResources(new ArrayList(m_resources));
        return result;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsModule) {
            return m_name.compareTo(((CmsModule)obj).m_name);
        }
        return 0;
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
    public List getDependencies() {

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
     * Returns the list of explorer resource types that belong to this module.<p>
     *
     * @return the list of explorer resource types that belong to this module
     */
    public List getExplorerTypes() {

        return m_explorerTypeSettings;
    }

    /**
     * Returns the list of export point added by this module.<p>
     *
     * @return the list of export point added by this module
     */
    public List getExportPoints() {

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
     * Returns a parameter value from the module parameters.<p>
     * 
     * @param key the parameter to return the value for
     * @return the parameter value from the module parameters
     */
    public String getParameter(String key) {

        return (String)m_parameters.get(key);
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

        String value = (String)m_parameters.get(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns the configured (immutable) module parameters.<p>
     * 
     * @return the configured (immutable) module parameters
     */
    public SortedMap getParameters() {

        return m_parameters;
    }

    /**
     * Returns the list of VFS resources that belong to this module.<p>
     *
     * @return the list of VFS resources that belong to this module
     */
    public List getResources() {

        return m_resources;
    }

    /**
     * Returns the list of additional resource types that belong to this module.<p>
     *
     * @return the list of additional resource types that belong to this module
     */
    public List getResourceTypes() {

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
    public int hashCode() {

        return m_name.hashCode();
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
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_MODULE_ACTION_CLASS_2,
                    value,
                    getName()));
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
    public void setDependencies(List dependencies) {

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
     * Sets the additional explorer types that belong to this module.<p>
     *
     * @param explorerTypeSettings the explorer type settings.
     */
    public void setExplorerTypes(List explorerTypeSettings) {

        m_explorerTypeSettings = explorerTypeSettings;
    }

    /**
     * Sets the exportpoints of  this module.<p>
     *
     * @param exportPoints the exportpoints of this module.
     */
    public void setExportPoints(List exportPoints) {

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
     * @param value the module parameters to set
     */
    public void setParameters(SortedMap value) {

        checkFrozen();
        m_parameters = value;
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
    public void setResources(List value) {

        checkFrozen();
        m_resources = value;
    }

    /**
     * Sets the list of additional resource types that belong to this module.<p>
     *
     * @param resourceTypes list of additional resource types that belong to this module
     */
    public void setResourceTypes(List resourceTypes) {

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
     * @throws CmsRuntimeException in case the configuration is already frozen
     */
    protected void checkFrozen() throws CmsRuntimeException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_MODULE_FROZEN_1, getName()));
        }
    }

    /**
     * Initializes this module, also freezing the module configuration.<p>
     * 
     * @param cms an initialized OpenCms user context
     * 
     * @throws CmsRoleViolationException if the given users does not have the <code>{@link CmsRole#MODULE_MANAGER}</code> role 
     */
    protected void initialize(CmsObject cms) throws CmsRoleViolationException {

        checkFrozen();
        // check if the user has the required permissions
        cms.checkRole(CmsRole.MODULE_MANAGER);

        m_frozen = true;
        m_resources = Collections.unmodifiableList(m_resources);
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

        SortedMap parameters = new TreeMap(m_parameters);
        List resources = new ArrayList(m_resources);

        String additionalResources;
        additionalResources = (String)parameters.get(I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES);
        if (additionalResources != null) {
            StringTokenizer tok = new StringTokenizer(
                additionalResources,
                I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR);
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
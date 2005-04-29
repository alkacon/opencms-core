/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModule.java,v $
 * Date   : $Date: 2005/04/29 15:00:35 $
 * Version: $Revision: 1.5 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModule.class);  

    /** The default date for module created / installed if not provided. */
    public static final long C_DEFAULT_DATE = 0L;

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

    /** The name of this module, must be a valid Java package name. */
    private String m_name;

    /** The "nice" display name of this module. */
    private String m_niceName;

    /** The additional configuration parameters of this module. */
    private Map m_parameters;

    /** List of VFS resources that belong to this module. */
    private List m_resources;
    
    /** The list of additional resource types. */
    private List m_resourceTypes;

    /** The name of the user who installed this module. */
    private String m_userInstalled;

    /** The version of this module. */
    private CmsModuleVersion m_version;


    /**
     * Creates a new module description with the specified values.<p>
     * 
     * @param name the name of this module, must be a valid Java package name
     * @param niceName the "nice" display name of this module
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
     * @param moduleResources a list of VFS resources that belong to this module
     * @param parameters the parameters for this module
     */
    public CmsModule(
        String name,
        String niceName,
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
        List moduleResources,
        Map parameters) {

        super();
        if (!CmsStringUtil.isValidJavaClassName(name)) {
            throw new IllegalArgumentException("Module name must be a valid Java class name");
        }
        m_name = name;
        if (CmsStringUtil.isEmpty(niceName)) {
            m_niceName = m_name;
        } else {
            m_niceName = niceName;
        }
        if (CmsStringUtil.isEmpty(actionClass)) {
            m_actionClass = null;
        } else {
            if (!CmsStringUtil.isValidJavaClassName(actionClass)) {
                throw new IllegalArgumentException("Module action class name must be a valid Java class name");
            }
            m_actionClass = actionClass;
        }
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
        if (moduleResources == null) {
            m_resources = Collections.EMPTY_LIST;
        } else {
            m_resources = Collections.unmodifiableList(moduleResources);
        }
        if (parameters == null) {
            m_parameters = Collections.EMPTY_MAP;
        } else {
            m_parameters = Collections.unmodifiableMap(parameters);
        }
        // handle old style "additional resources" for backward compatiblity
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if ((obj == null) || (!(obj instanceof CmsModule))) {
            return 0;
        }
        return m_name.compareTo(((CmsModule)obj).m_name);
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

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CmsModule)) {
            return false;
        }

        CmsModule other = (CmsModule)obj;

        return m_name.equals(other.m_name);
    }

    /**
     * Returns the (optional) class name of the modules action class.<p>
     *
     * @return the (optional) class name of the modules action class
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
     * Returns the name of this module.<p>
     * 
     * The module name usually looks like a java package name.<p>
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
    public Map getParameters() {

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
     * Sets the additional explorer types that belong to this module.<p>
     *
     * @param explorerTypeSettings the explorer type settings.
     */
    public void setExplorerTypes(List explorerTypeSettings) {
        m_explorerTypeSettings = explorerTypeSettings;
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
     * Resolves the module property "additionalresources" to the resource list and
     * vice versa.<p>
     * 
     * This "special" module property is required as long as we do not have a new 
     * GUI for editing of module resource entries. Once we have the new GUI, the 
     * handling of "additionalresources" will be moved to the import of the module 
     * and done only if the imported module is a 5.0 module.<p>
     */
    private void initOldAdditionalResources() {

        Map parameters = new HashMap(m_parameters);
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
        Collections.sort(resources);
        StringBuffer buf = new StringBuffer(512);
        for (int i = 0; i < resources.size(); i++) {
            String resource = (String)resources.get(i);
            buf.append(resource);
            if ((i + 1) < resources.size()) {
                buf.append(I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR);
            }
        }
        parameters.put(I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES, buf.toString());

        m_parameters = Collections.unmodifiableMap(parameters);
        m_resources = Collections.unmodifiableList(resources);
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
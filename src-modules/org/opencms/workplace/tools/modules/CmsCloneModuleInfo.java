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

package org.opencms.workplace.tools.modules;

/**
 * Clone module information bean.<p>
 */
public class CmsCloneModuleInfo {

    /** The action class used for the clone. */
    private String m_actionClass;

    /** If 'true' resource types in all sites will be adjusted. */
    private String m_applyChangesEverywhere;

    /** The author's email used for the clone. */
    private String m_authorEmail = "sales@alkacon.com";

    /** The author's name used for the clone. */
    private String m_authorName = "Alkacon Software GmbH & Co. KG";

    /** Option to change the resource types (optional flag). */
    private String m_changeResourceTypes;

    /** The description used for the clone. */
    private String m_description = "This module provides the template layout.";

    /** A module name where the formatters are located that are referenced within the XSDs of the module to clone. */
    private String m_formatterSourceModule = "com.alkacon.bootstrap.formatters";

    /** A module name where the formatters are located that should be referenced by the XSDs of the clone. */
    private String m_formatterTargetModule;

    /** The module group used for the clone. */
    private String m_group;

    /** The new module name used for the clone. */
    private String m_name = "my.company.template";

    /** The nice name used for the clone. */
    private String m_niceName = "My new template module.";

    /** Flag that controls whether container pages should be rewritten. */
    private boolean m_rewriteContainerPages;

    /** The name of the source module to be cloned. */
    private String m_sourceModuleName = "com.alkacon.bootstrap.formatters";

    /** The prefix that is used by the source module. */
    private String m_sourceNamePrefix = "bs-";

    /** The prefix that is used by the target module. */
    private String m_targetNamePrefix = "my-";

    /**
     * Returns the action class.<p>
     *
     * @return the action class
     */
    public String getActionClass() {

        return m_actionClass;
    }

    /**
     * Returns the changeResourceTypesEverywhere.<p>
     *
     * @return the changeResourceTypesEverywhere
     */
    public String getApplyChangesEverywhere() {

        return m_applyChangesEverywhere;
    }

    /**
     * Returns the author email.<p>
     *
     * @return the author email
     */
    public String getAuthorEmail() {

        return m_authorEmail;
    }

    /**
     * Returns the author name.<p>
     *
     * @return the author name
     */
    public String getAuthorName() {

        return m_authorName;
    }

    /**
     * Returns the change resource types flag as String.<p>
     *
     * @return the change resource types flag as String
     */
    public String getChangeResourceTypes() {

        return m_changeResourceTypes;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the formatter source module package/name.<p>
     *
     * @return the formatter source module package/name
     */
    public String getFormatterSourceModule() {

        return m_formatterSourceModule;
    }

    /**
     * Returns the formatter target module package/name.<p>
     *
     * @return the formatter target module package/name
     */
    public String getFormatterTargetModule() {

        return m_formatterTargetModule;
    }

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the package/module name for the clone/target.<p>
     *
     * @return the package/module name for the clone/target
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the nice name.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Returns the source module package/name (the module to clone).<p>
     *
     * @return the source module package/name (the module to clone)
     */
    public String getSourceModuleName() {

        return m_sourceModuleName;
    }

    /**
     * Returns the source name prefix.<p>
     *
     * @return the source name prefix
     */
    public String getSourceNamePrefix() {

        return m_sourceNamePrefix;
    }

    /**
     * Returns the target name prefix.<p>
     *
     * @return the target name prefix
     */
    public String getTargetNamePrefix() {

        return m_targetNamePrefix;
    }

    /**
     * Returns the rewriteContainerPages.<p>
     *
     * @return the rewriteContainerPages
     */
    public boolean isRewriteContainerPages() {

        return m_rewriteContainerPages;
    }

    /**
     * Sets the action class.<p>
     *
     * @param actionClass the action class
     */
    public void setActionClass(String actionClass) {

        m_actionClass = actionClass;
    }

    /**
     * Sets the changeResourceTypesEverywhere.<p>
     *
     * @param applyChangesEverywhere the changeResourceTypesEverywhere to set
     */
    public void setApplyChangesEverywhere(String applyChangesEverywhere) {

        m_applyChangesEverywhere = applyChangesEverywhere;
    }

    /**
     * Sets the author email.<p>
     *
     * @param authorEmail the author email to set
     */
    public void setAuthorEmail(String authorEmail) {

        m_authorEmail = authorEmail;
    }

    /**
     * Sets the author name.<p>
     *
     * @param authorName the author name to set
     */
    public void setAuthorName(String authorName) {

        m_authorName = authorName;
    }

    /**
     * Sets the change resource types flag.<p>
     *
     * @param changeResourceTypes the change resource types falg to set
     */
    public void setChangeResourceTypes(String changeResourceTypes) {

        m_changeResourceTypes = changeResourceTypes;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the formatter source module name.<p>
     *
     * @param formatterSourceModule the formatter source module name to set
     */
    public void setFormatterSourceModule(String formatterSourceModule) {

        m_formatterSourceModule = formatterSourceModule;
    }

    /**
     * Sets the formatter target module name.<p>
     *
     * @param formatterTargetModule the formatter target module name to set
     */
    public void setFormatterTargetModule(String formatterTargetModule) {

        m_formatterTargetModule = formatterTargetModule;
    }

    /**
     * Sets the group.<p>
     *
     * @param group the group to set
     */
    public void setGroup(String group) {

        m_group = group;
    }

    /**
     * Sets the module package name.<p>
     *
     * @param name the module package name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the nice name.<p>
     *
     * @param niceName the nice name to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
    }

    /**
     * Sets the rewriteContainerPages.<p>
     *
     * @param rewriteContainerPages the rewriteContainerPages to set
     */
    public void setRewriteContainerPages(boolean rewriteContainerPages) {

        m_rewriteContainerPages = rewriteContainerPages;
    }

    /**
     * Sets the source module name.<p>
     *
     * @param sourceModuleName the source module name to set
     */
    public void setSourceModuleName(String sourceModuleName) {

        m_sourceModuleName = sourceModuleName;
    }

    /**
     * Sets the source name prefix.<p>
     *
     * @param sourceNamePrefix the source name prefix to set
     */
    public void setSourceNamePrefix(String sourceNamePrefix) {

        m_sourceNamePrefix = sourceNamePrefix;
    }

    /**
     * Sets the target name prefix.<p>
     *
     * @param targetNamePrefix the target name prefix to set
     */
    public void setTargetNamePrefix(String targetNamePrefix) {

        m_targetNamePrefix = targetNamePrefix;
    }
}

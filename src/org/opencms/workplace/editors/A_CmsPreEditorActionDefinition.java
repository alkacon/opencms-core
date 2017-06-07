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

package org.opencms.workplace.editors;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;

/**
 * Defines an action to be performed before the workplace editor is opened for the first time.<p>
 *
 * Implements the basic methods to handle the resource type.<p>
 *
 * @since 6.5.4
 */
public abstract class A_CmsPreEditorActionDefinition implements I_CmsPreEditorActionDefinition {

    /** Configuration parameters. */
    protected CmsParameterConfiguration m_configuration;

    /** The resource type for which the action should be performed. */
    private I_CmsResourceType m_resourceType;

    /** The resource type name for which the action should be performed. */
    private String m_resourceTypeName;

    /**
     * Constructor, without parameters.<p>
     */
    public A_CmsPreEditorActionDefinition() {

        // empty constructor, needed for initialization
        m_configuration = new CmsParameterConfiguration();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#doPreAction(org.opencms.file.CmsResource, org.opencms.workplace.CmsDialog, java.lang.String)
     */
    public abstract boolean doPreAction(CmsResource resource, CmsDialog dialog, String originalParams) throws Exception;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#getResourceType()
     */
    public I_CmsResourceType getResourceType() {

        if (m_resourceType == null) {
            try {
                m_resourceType = OpenCms.getResourceManager().getResourceType(m_resourceTypeName);
            } catch (CmsLoaderException e) {
                // should not happen, ignore
            }
        }
        return m_resourceType;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#getResourceTypeName()
     */
    public String getResourceTypeName() {

        return m_resourceTypeName;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public final void initConfiguration() {

        // final since subclasses should NOT implement this
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsPreEditorActionDefinition#setResourceTypeName(java.lang.String)
     */
    public void setResourceTypeName(String resourceTypeName) {

        m_resourceTypeName = resourceTypeName;
    }

}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C)  Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.xml.content.CmsXmlContentProperty;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * This class represents the property configuration for a sitemap region.<p>
 *
 * (This is mostly a wrapper around CmsXmlContentProperty. We don't use that class directly because
 *  we may want to put server-specific logic in here, and CmsXmlContentProperty is used as a bean for
 *  RPC data transfer to the client.)
 *
 * @author Georg Westenberger
 *
 * @version $Revision: 1.0 $
 *
 * @since 8.0.1
 */
public class CmsPropertyConfig implements I_CmsConfigurationObject<CmsPropertyConfig>, Cloneable {

    /** True if this property is disabled. */
    private boolean m_disabled;

    /** The order. **/
    private int m_order;

    /** The internal property data. */
    private CmsXmlContentProperty m_propData;

    /** Is top property (should be displayed near the top of a property list). */
    private boolean m_top;

    /**
     * Creates a new propery configuration bean.<p>
     *
     * @param propData the property data
     * @param disabled true if this property is disabled
     */
    public CmsPropertyConfig(CmsXmlContentProperty propData, boolean disabled) {

        this(propData, disabled, I_CmsConfigurationObject.DEFAULT_ORDER);
    }

    /**
     * Creates a new property configuration bean.<p>
     *
     * @param propData the property data
     * @param disabled true if this property is disabled
     * @param order the number used for sorting the property configurations
     */
    public CmsPropertyConfig(CmsXmlContentProperty propData, boolean disabled, int order) {

        m_propData = propData;
        m_disabled = disabled;
        m_order = order;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsPropertyConfig clone() {

        try {
            return (CmsPropertyConfig)(super.clone());
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Creates a clone and sets the 'top' property in the clone.
     *
     * @param top the value for the top property in the clone
     *
     * @return the clone
     */
    public CmsPropertyConfig cloneWithTop(boolean top) {

        CmsPropertyConfig result = (this.clone());
        result.m_top = top;
        return result;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#getKey()
     */
    public String getKey() {

        return getName();
    }

    /**
     * Gets the name of the property.<p>
     *
     * @return the name of the property
     */
    public String getName() {

        return m_propData.getName();
    }

    /**
     * Gets the order.<p>
     *
     * @return the order
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * Returns the property configuration data.<p>
     *
     * @return the property configuration data
     */
    public CmsXmlContentProperty getPropertyData() {

        return m_propData;
    }

    /**
     * Returns true if the entry disables a property, rather than adding it.<p>
     *
     * @return true if the property should be disabled
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * Returns true if this is a 'top property' which should be displayed near the top of a property list.
     *
     * @return true if this is a top property
     */
    public boolean isTop() {

        return m_top;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsConfigurationObject#merge(org.opencms.ade.configuration.I_CmsConfigurationObject)
     */
    public CmsPropertyConfig merge(CmsPropertyConfig child) {

        return child.cloneWithTop(m_top);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }
}

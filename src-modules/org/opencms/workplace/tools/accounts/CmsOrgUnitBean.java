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

package org.opencms.workplace.tools.accounts;

import org.opencms.security.CmsOrganizationalUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizational unit bean for use in new organizational unit dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitBean {

    /** The description of this object. */
    private String m_description;

    /** The fqn of this object. */
    private String m_fqn;

    /** The name of this object. */
    private String m_name;

    /** The hidden login form flag. */
    private boolean m_nologin;

    /** The parent ou of this object. */
    private String m_parentOu;

    /** The description of the parent ou. */
    private String m_parentOuDesc;

    /** The resource list of this object. */
    private List<String> m_resources;

    /** The webusers flag. */
    private boolean m_webusers;

    /**
     * Public constructor.<p>
     */
    public CmsOrgUnitBean() {

        m_resources = new ArrayList<String>();
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
     * Returns the flag value depending on the boolean flag set.<p>
     *
     * @return  the flag value
     */
    public int getFlags() {

        int flags = 0;
        if (isNologin()) {
            flags += CmsOrganizationalUnit.FLAG_HIDE_LOGIN;
        }
        if (isWebusers()) {
            flags += CmsOrganizationalUnit.FLAG_WEBUSERS;
        }
        return flags;
    }

    /**
     * Returns the fqn.<p>
     *
     * @return the fqn
     */
    public String getFqn() {

        if (m_fqn != null) {
            return m_fqn;
        } else {
            return m_parentOu + m_name;
        }
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the parentOu.<p>
     *
     * @return the parentOu
     */
    public String getParentOu() {

        if (m_parentOu == null) {
            return "";
        }
        return CmsOrganizationalUnit.SEPARATOR + m_parentOu;
    }

    /**
     * Returns the description of the parent ou.<p>
     *
     * @return the description of the parent ou
     */
    public String getParentOuDesc() {

        return m_parentOuDesc;
    }

    /**
     * Returns the resources.<p>
     *
     * @return the resources
     */
    public List<String> getResources() {

        return m_resources;
    }

    /**
     * Returns the hidden login form flag.<p>
     *
     * @return the hidden login form flag
     */
    public boolean isNologin() {

        if (isWebusers()) {
            return true;
        }
        return m_nologin;
    }

    /**
     * Returns the webusers flag.<p>
     *
     * @return the webusers flag
     */
    public boolean isWebusers() {

        return m_webusers;
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
     * Sets the fqn.<p>
     *
     * @param fqn the fqn to set
     */
    public void setFqn(String fqn) {

        m_fqn = fqn;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the hidden login form flag.<p>
     *
     * @param nologin the hidden login form flag to set
     */
    public void setNologin(boolean nologin) {

        m_nologin = nologin;
    }

    /**
     * Sets the parentOu.<p>
     *
     * @param parentOu the parentOu to set
     */
    public void setParentOu(String parentOu) {

        if (parentOu.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
            parentOu = parentOu.substring(1);
        }
        m_parentOu = parentOu;
    }

    /**
     * Sets the description of the parent ou.<p>
     *
     * @param parentOuDesc the description of the parent ou to set
     */
    public void setParentOuDesc(String parentOuDesc) {

        m_parentOuDesc = parentOuDesc;
    }

    /**
     * Sets the resources.<p>
     *
     * @param resources the resources to set
     */
    public void setResources(List<String> resources) {

        m_resources = resources;
    }

    /**
     * Sets the webusers flag.<p>
     *
     * @param webusers the webusers flag to set
     */
    public void setWebusers(boolean webusers) {

        m_webusers = webusers;
    }
}

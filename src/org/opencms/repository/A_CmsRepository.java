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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.main.CmsException;

/**
 * Abstract implementation of the repository interface {@link I_CmsRepository}.<p>
 *
 * Get a {@link I_CmsRepositorySession} through login in with the
 * username and password ({@link #login(String, String)}).<p>
 *
 * Handles the functionality of basic configuration. This is actually the configuration
 * of param/values and the filters ({@link CmsRepositoryFilter}) to use of the repository.<p>
 *
 * @since 6.2.4
 */
public abstract class A_CmsRepository implements I_CmsRepository {

    /** The repository configuration. */
    private CmsParameterConfiguration m_configuration;

    /** The filter to use for the repository. */
    private CmsRepositoryFilter m_filter;

    /** The name of the repository. */
    private String m_name;

    /**
     * Default constructor initializing member variables.<p>
     */
    public A_CmsRepository() {

        m_configuration = new CmsParameterConfiguration();
        m_filter = null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Returns the filter.<p>
     *
     * @return the filter
     */
    public CmsRepositoryFilter getFilter() {

        return m_filter;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        if (m_filter != null) {
            m_filter.initConfiguration();
        }

        // suppress the compiler warning, this is never true
        if (m_configuration == null) {
            throw new CmsConfigurationException(null);
        }

    }

    /**
     * Login a user given the username and the password.<p>
     *
     * @param userName the user name
     * @param password the user's password
     *
     * @return the authenticated session
     *
     * @throws CmsException if the login was not succesful
     */
    public abstract I_CmsRepositorySession login(String userName, String password) throws CmsException;

    /**
     * Sets the filter.<p>
     *
     * @param filter the filter to set
     */
    public void setFilter(CmsRepositoryFilter filter) {

        m_filter = filter;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#setName(String)
     */
    public void setName(String name) {

        m_name = name;
    }

}

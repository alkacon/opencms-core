/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/A_CmsRepository.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Abstract implementation of the repository interface {@link I_CmsRepository}.<p>
 * 
 * Handles the functionality of basic configuration.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $ 
 * 
 * @since 6.5.6
 */
public abstract class A_CmsRepository implements I_CmsRepository, I_CmsConfigurationParameterHandler {

    /** The repository configuration. */
    private Map m_configuration;

    /** The name of the repository. */
    private String m_name;

    /**
     * Default constructor initializing member variables.<p>
     */
    public A_CmsRepository() {

        m_configuration = new TreeMap();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (m_configuration.containsKey(paramName)) {
            String[] values = (String[])m_configuration.get(paramName);
            
            String[] added = new String[values.length + 1];
            System.arraycopy(values, 0, added, 0, values.length);
            added[added.length-1] = paramValue;
            
            m_configuration.put(paramName, added);
        } else {
            m_configuration.put(paramName, new String[] {paramValue});
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#init(javax.servlet.ServletConfig)
     */
    public abstract void init(ServletConfig servletConfig) throws ServletException;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        // suppress the compiler warning, this is never true
        if (m_configuration == null) {
            throw new CmsConfigurationException(null);
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepository#login(java.lang.String, java.lang.String)
     */
    public abstract I_CmsRepositorySession login(String userName, String password)
    throws CmsRepositoryAuthorizationException;

    /**
     * @see org.opencms.repository.I_CmsRepository#setName(String)
     */
    public void setName(String name) {

        m_name = name;
    }

}

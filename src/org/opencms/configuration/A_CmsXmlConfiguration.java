/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/A_CmsXmlConfiguration.java,v $
 * Date   : $Date: 2004/03/07 19:22:02 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.main.OpenCms;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.digester.Digester;

import org.xml.sax.Attributes;

/**
 * Abstract base implementation for xml configurations.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public abstract class A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
       
    /** The digester instance that was set suring object creation */
    private Digester m_digester;
    
    /**
     * @see org.apache.commons.digester.ObjectCreationFactory#createObject(org.xml.sax.Attributes)
     */
    public Object createObject(Attributes attributes) throws Exception {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("createObject(attributes) called on " + this);
            for (int i = 0; i < attributes.getLength(); i++) {
                OpenCms.getLog(this).debug("createObject() attribute: " + attributes.getLocalName(i) + "=" + attributes.getValue(i));
            }
        }
        initialize();
        return this;
    }

    /**
     * @see org.apache.commons.digester.ObjectCreationFactory#getDigester()
     */
    public Digester getDigester() {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getDigester() called on " + this);
        }           
        return m_digester;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#initialize()
     */
    public void initialize() {
        // noop, overload this in your implementation if required
    }

    /**
     * @see org.apache.commons.digester.ObjectCreationFactory#setDigester(org.apache.commons.digester.Digester)
     */
    public void setDigester(Digester digester) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("setDigester(digester) called on " + this);
        }         
        m_digester = digester;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {
        // this configuration does not support parameters
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getConfiguration() called on " + this);
        }          
        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {
        // this configuration does not support parameters 
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("addConfigurationParameter(" + paramName + ", " + paramValue + ") called on " + this);
        }            
    }    
}

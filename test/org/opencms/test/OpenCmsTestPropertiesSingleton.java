/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/Attic/OpenCmsTestPropertiesSingleton.java,v $
 * Date   : $Date: 2004/11/24 15:57:25 $
 * Version: $Revision: 1.1 $
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
package org.opencms.test;

import java.io.IOException;

import org.apache.commons.collections.ExtendedProperties;
import org.opencms.util.CmsPropertyUtils;

/**
 * Reads and manages the test.properties file
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 6.0.0
 */
public class OpenCmsTestPropertiesSingleton {
	
	/**
	 * the singleton instance
	 */
	private static OpenCmsTestPropertiesSingleton m_testSingleton;
	
	/**
	 * the path to the test.properties file
	 */
	private String m_basePath;

	/**
	 * the path to the data test folder
	 */
	private String m_testDataPath;
	
	/**
	 * the path to the webapp test folder
	 */
	private String m_testWebappPath;
	
	/**
	 * private default constructor
	 */
	private OpenCmsTestPropertiesSingleton() {
		
	}
	
	/**
	 * @return the singleton instance
	 */
	public static OpenCmsTestPropertiesSingleton getInstance() {
		if (m_testSingleton==null) {
			throw new RuntimeException("You have to initialize the test properties.");
		}
		return m_testSingleton;
	}
	
	/**
	 * @return the path to the data test directory
	 */
	public String getTestDataPath() {
		return m_testDataPath;
	}
	
	/**
	 * @return the path to the webapp test directory
	 */
	public String getTestWebappPath() {
		return m_testWebappPath;
	}
	
	/**
	 * reads property file test.properties and fill singleton members
	 * 
	 * @param basePath the path where to find the test.properties file
	 */
	public static void initialize(String basePath) {
		ExtendedProperties props=null;
		
		try {
			props = CmsPropertyUtils.loadProperties(basePath + "test.properties");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		m_testSingleton = new OpenCmsTestPropertiesSingleton();
		
		m_testSingleton.m_basePath = basePath;
		m_testSingleton.m_testDataPath = props.getString("test.data.path");
		m_testSingleton.m_testWebappPath = props.getString("test.webapp.path");
	}
	/**
	 * @return Returns the path to the test.properties file
	 */
	public String getBasePath() {
		return m_basePath;
	}
}

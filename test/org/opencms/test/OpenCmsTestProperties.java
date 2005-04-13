/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestProperties.java,v $
 * Date   : $Date: 2005/04/13 13:25:43 $
 * Version: $Revision: 1.6 $
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

package org.opencms.test;

import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Reads and manages the test.properties file.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.6 $
 * 
 * @since 6.0.0
 */
public final class OpenCmsTestProperties {

    /**
     * the singleton instance.
     */
    private static OpenCmsTestProperties m_testSingleton;

    /**
     * the path to the test.properties file.
     */
    private String m_basePath;

    /**
     * the path to the data test folder.
     */
    private String m_testDataPath;

    /**
     * the path to the webapp test folder.
     */
    private String m_testWebappPath;
    
    /**
     * the database to use. 
     */
    private String m_dbProduct;

    
    private static ExtendedProperties m_configuration;
    /**
     * private default constructor.
     */
    private OpenCmsTestProperties() {

        // noop
    }

    
    /**
     * @return the singleton instance
     */
    public static OpenCmsTestProperties getInstance() {

        if (m_testSingleton == null) {
            throw new RuntimeException("You have to initialize the test properties.");
        }
        return m_testSingleton;
    }

    /**
     * Reads property file test.properties and fills singleton members.<p>
     * 
     * @param basePath the path where to find the test.properties file
     */
    public static void initialize(String basePath) {

        if (m_testSingleton != null) {
            return;
        }

        String testPropPath;
        m_testSingleton = new OpenCmsTestProperties();

        m_testSingleton.m_basePath = basePath;
        if (!m_testSingleton.m_basePath.endsWith("/")) {
            m_testSingleton.m_basePath += "/";
        }

        try {
            testPropPath = CmsFileUtil.getResourcePathFromClassloader("test.properties");
            if(testPropPath == null){
              throw new RuntimeException("Test property file ('test.properties') could not be found by context Classloader."); 
            }
            File f = new File(testPropPath);
            if(!f.exists()){
                throw new RuntimeException("Test property file ('test.properties') could not be found. Context Classloader suggested location: "+testPropPath); 
            }
            m_configuration = CmsPropertyUtils.loadProperties(testPropPath);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }

        m_testSingleton.m_testDataPath = m_configuration.getString("test.data.path");
        m_testSingleton.m_testWebappPath = m_configuration.getString("test.webapp.path");
        m_testSingleton.m_dbProduct = m_configuration.getString("db.product");
        
    }

    /**
     * @return Returns the path to the test.properties file
     */
    public String getBasePath() {

        return m_basePath;
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
     * 
     * @return a String identifying the db.product property value of the 'test.properties' value.
     */
    public String getDbProduct() {
    
        return m_dbProduct;
    }


    /**
     * @return the parsed configuration file ('test.properties')
     */
    
    public ExtendedProperties getConfiguration() {
    
        return m_configuration;
    }
    

    
    
}
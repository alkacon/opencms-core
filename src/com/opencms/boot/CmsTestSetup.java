/*
* File: $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsTestSetup.java,v $
* Date: $Date: 2002/06/12 14:55:09 $
* Version: $Revision: 1.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.boot;

import com.opencms.core.*;
import com.opencms.file.*;

import java.util.*;
import java.io.*;
import source.org.apache.java.util.*;
import junit.framework.*;

/**
 * Class that is being used for the JUnit test. Start JUnit tests via the
 * Ant target "test-all"
 *
 * @author Matthias Schmidt
 * @author Klaus Marquart
 * @see com.opencms.core.CmsTestHelper
 */
public class CmsTestSetup extends TestCase implements I_CmsConstants{

    /**
     * The resource broker to get access to the cms.
     */
    protected CmsObject m_cms;

    /**
     * The open-cms.
     */
    private A_OpenCms m_openCms;

    /**
     * If this member is set to true the memory-logging is enabled.
     */
    boolean m_logMemory = false;


    /**
     * Constructor
     *
     * @param name Name of test case.
     */
    public CmsTestSetup(String name) {
      super(name);
    }

    /**
     * Method that is called before every single JUnit test.
     */
    protected void setUp() {
      // String containing the OpenCms base path.
      String base = null;

      //setting the home base
      base = CmsMain.searchBaseFolder(System.getProperty("user.dir") + System.getProperty("build.webinf").substring(1));

      base = CmsBase.setBasePath(base);

      // Starting the OpenCms
      try {
        String propsPath = CmsBase.getPropertiesPath(true);
        Configurations conf = new Configurations(new ExtendedProperties(propsPath));
        m_openCms = com.opencms.core.CmsTestHelper.getOpenCmsObject(conf);
        m_cms = new CmsObject();

        m_logMemory = conf.getBoolean("log.memory", false);
        //log in default user.
        m_openCms.initUser(m_cms, null, null, C_USER_GUEST, C_GROUP_GUEST, C_PROJECT_ONLINE_ID, null);
      } catch(Exception exc) {
        exc.printStackTrace();
      }
    }

     /**
     * Shuts down the OpenCms.
     * @see com.opencms.core.CmsTestHelper#destroyOpenCmsObject
     */
    protected void tearDown() {
      try {
        com.opencms.core.CmsTestHelper.destroyOpenCmsObject((OpenCms)m_openCms);
      } catch (CmsException exc) {
        exc.printStackTrace();
      }
    }
}
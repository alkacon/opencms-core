/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/setup/TestCmsSetupDb.java,v $
 * Date   : $Date: 2004/11/25 09:29:58 $
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
 
package org.opencms.setup;

import org.opencms.test.OpenCmsTestCase;

/** 
 * Tests the database creation / removal used during setup.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.3.5
 */
public class TestCmsSetupDb extends OpenCmsTestCase {
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsSetupDb(String arg0) {
        super(arg0);
    }
        
    /**
     * Tests database creation.<p>
     */
    public void testCreateDatabase() {
        
        if (C_DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testCreateDatabase not applicable for oracle.");
            return;
        }
        
        // use create method form superclass
        CmsSetupDb setupDb = getSetupDb(m_setupConnection);
        setupDb.createDatabase(m_dbProduct, getReplacer(m_defaultConnection), true);    
        
        // check for errors 
        checkErrors(setupDb);
        
        // close connections
        setupDb.closeConnection();
    }
    
    /**
     * Tests table creation.<p>
     */
    public void testCreateTables() {
        
        if (C_DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }        
        
        // use create method form superclass
        CmsSetupDb setupDb = getSetupDb(m_defaultConnection);
        setupDb.createTables(m_dbProduct, getReplacer(m_defaultConnection), true);     
        
        // check for errors 
        checkErrors(setupDb);
        
        // close connections
        setupDb.closeConnection();
    }
    
    /**
     * Tests table removal.<p>
     */
    public void testDropTables() {
        
        if (C_DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }        
        
        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDb(m_defaultConnection);
        setupDb.dropTables(m_dbProduct, getReplacer(m_defaultConnection), true);         
        
        // check for errors 
        checkErrors(setupDb);     
        
        // close connections
        setupDb.closeConnection();
    }
 
    /**
     * Tests database removal.<p>
     */
    public void testDropDatabase() {

        if (C_DB_ORACLE.equals(getDatabaseProduct())) {
            System.out.println("testDropDatabase not applicable for oracle.");
            return;
        }
        
        // use drop method form superclass
        CmsSetupDb setupDb = getSetupDb(m_setupConnection);
        setupDb.dropDatabase(m_dbProduct, getReplacer(m_defaultConnection), true);     
        
        // check for errors 
        checkErrors(setupDb);     
        
        // close connections
        setupDb.closeConnection();
    }
}

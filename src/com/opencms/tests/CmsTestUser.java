/*
* File: $Source: /alkacon/cvs/opencms/src/com/opencms/tests/Attic/CmsTestUser.java,v $
* Date: $Date: 2002/06/12 14:53:30 $
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

package com.opencms.tests;

import com.opencms.boot.*;
import java.util.*;
import java.io.*;

public class CmsTestUser extends CmsTestSetup {

  public CmsTestUser(String name) {
    super(name);
  }

  /**
   * Method testing a login
   */
  public void testUserLogin() {

    try {
      String test = m_cms.loginUser("Admin","admin");
     }
     catch (Exception ex) {
       ex.toString();
     }

     try {
       Vector c = m_cms.getUsers();
       Object ob[] = c.toArray();
       int i = ob.length;
       for (int j = 0; j <= i; j++) {
         System.out.println(ob[j].toString());
       }

     }
     catch (Exception ex) {
       ex.toString();
     }
     try {
       Vector c = m_cms.getUsers();
       Object ob[] = c.toArray();
       int i = ob.length;
       for (int j = 0; j <= i; j++) {
         System.out.println(ob[j].toString());
       }
     }
     catch (Exception ex) {
       ex.toString();
     }

   }
}
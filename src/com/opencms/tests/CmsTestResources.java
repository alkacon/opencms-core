/*
* File: $Source: /alkacon/cvs/opencms/src/com/opencms/tests/Attic/CmsTestResources.java,v $
* Date: $Date: 2002/06/19 08:56:59 $
* Version: $Revision: 1.2 $
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

import com.opencms.core.*;
import com.opencms.boot.*;

import java.util.*;
import java.io.*;


/**
 * Class that is being used for the JUnit test. Start JUnit tests via the
 * Ant target "test-resources"
 *
 * @author Matthias Schmidt
 * @see com.opencms.core.CmsTestHelper
 */
public class CmsTestResources extends CmsTestSetup {

  public CmsTestResources(String name) {
    super(name);
  }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsDbImport.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
 * Version: $Revision: 1.2 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;

/**
 * This interface describes the CMS database import.<BR/>
 * Imports into database form XML
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michaela Schleich
 * @version $Revision: 1.2 $ $Date: 2000/02/15 17:44:00 $
 */
interface I_CmsDbImport {

 /**
 * xmlImport
 * initialize the database import
 * 
 * @return a vector with error messages
 * 
 */
	public Vector xmlImport()
			throws CmsException, Exception;
		
}
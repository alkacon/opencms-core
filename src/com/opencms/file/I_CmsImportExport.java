/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsImportExport.java,v $
 * Date   : $Date: 2000/05/18 12:37:41 $
 * Version: $Revision: 1.1 $
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

/**
 * Thie interface defines some constants for import and export.
 */
interface I_CmsImportExport {
	
	/**
	 * The filename of the xml manifest.
	 */
	public static String C_XMLFILENAME = "manifest.xml";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_FILE = "file";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_SOURCE = "source";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_DESTINATION= "destination";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_TYPE = "type";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_USER = "user";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_GROUP = "group";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_ACCESS = "access";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_PROPERTY = "property";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_NAME = "name";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_VALUE = "value";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_EXPORT = "export";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_FILES = "files";

	/**
	 * A tag in the manifest-file.
	 */
	public static String C_TAG_PROPERTIES = "properties";
}

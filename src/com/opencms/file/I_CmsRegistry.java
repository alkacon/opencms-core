package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsRegistry.java,v $
 * Date   : $Date: 2000/08/30 12:54:22 $
 * Version: $Revision: 1.6 $
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

import java.lang.*;
import java.util.*;
import com.opencms.core.*;

/**
 * This interface describes the registry for OpenCms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.6 $ $Date: 2000/08/30 12:54:22 $
 * 
 */
public interface I_CmsRegistry extends Cloneable {
	public static final int C_ANY_VERSION = -1;

/**
 * This method clones the registry.
 *
 * @param CmsObject the current cms-object for the user.
 * @return the cloned registry.
 */
public I_CmsRegistry clone(CmsObject cms);
/**
 *  Deletes a module. This method is synchronized, so only one module can be deleted at one time.
 *
 *  @param module-name the name of the module that should be deleted.
 *  @param exclusion a Vector with resource-names that should be excluded from this deletion.
 */
public void deleteModule(String module, Vector exclusion) throws CmsException;
	/**
	 *  Checks for files that already exist in the system but should be replaced by the module.
	 *
	 *  @param moduleZip The name of the zip-file to import.
	 *  @returns The complete paths to the resources that have conflicts.
	 */
	public Vector getConflictingFileNames(String moduleZip) throws CmsException;
/**
 * This method returns the author of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the author of the module.
 */
public String getModuleAuthor(String modulename);
	/**
	 * This method returns the email of author of the module.
	 *
	 * @parameter String the name of the module.
	 * @return java.lang.String the email of author of the module.
	 */
	public String getModuleAuthorEmail(String modulename);
/**
 * Gets the create date of the module.
 *
 * @parameter String the name of the module.
 * @return long the create date of the module.
 */
public long getModuleCreateDate(String modulname);
/**
 * Returns the module dependencies for the module.
 *
 * @param module String the name of the module to check.
 * @param modules Vector in this parameter the names of the dependend modules will be returned.
 * @param minVersions Vector in this parameter the minimum versions of the dependend modules will be returned.
 * @param maxVersions Vector in this parameter the maximum versions of the dependend modules will be returned.
 * @return int the amount of dependencies for the module will be returned.
 */
public int getModuleDependencies(String modulename, Vector modules, Vector minVersions, Vector maxVersions);
/**
 * Returns the description of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the description of the module.
 */
public String getModuleDescription(String module);
/**
 * Gets the url to the documentation of the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the url to the documentation of the module.
 */
public String getModuleDocumentPath(String modulename);
/**
 * Returns all filenames and hashcodes belonging to the module.
 *
 * @param String modulname the name of the module.
 * @param retNames the names of the resources belonging to the module.
 * @param retCodes the hashcodes of the resources belonging to the module.
 * @return the amount of entrys.
 */
public int getModuleFiles(String modulename, Vector retNames, Vector retCodes);
/**
 * Returns the class, that receives all maintenance-events for the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.Class that receives all maintenance-events for the module.
 */
public Class getModuleMaintenanceEventClass(String modulname);
/**
 * Returns the names of all available modules.
 *
 * @return String[] the names of all available modules.
 */
public Enumeration getModuleNames();
	/**
	 * Gets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @return value java.lang.String the value to set for the parameter.
	 */
	public String getModuleParameter(String modulename, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public boolean getModuleParameterBoolean(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Boolean getModuleParameterBoolean(String modulname, String parameter, Boolean defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public boolean getModuleParameterBoolean(String modulname, String parameter, boolean defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
byte getModuleParameterByte(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public byte getModuleParameterByte(String modulname, String parameter, byte defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Byte getModuleParameterByte(String modulname, String parameter, Byte defaultValue);
/**
 * Returns a description for parameter in a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return String the description for the parameter in the module.
 */
public String getModuleParameterDescription(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public double getModuleParameterDouble(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public double getModuleParameterDouble(String modulname, String parameter, double defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Double getModuleParameterDouble(String modulname, String parameter, Double defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public float getModuleParameterFloat(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public float getModuleParameterFloat(String modulname, String parameter, float defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Float getModuleParameterFloat(String modulname, String parameter, Float defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public int getModuleParameterInteger(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public int getModuleParameterInteger(String modulname, String parameter, int defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Integer getModuleParameterInteger(String modulname, String parameter, Integer defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public long getModuleParameterLong(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public long getModuleParameterLong(String modulname, String parameter, long defaultValue);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Long getModuleParameterLong(String modulname, String parameter, Long defaultValue);
/**
 * Gets all parameter-names for a module.
 * 
 * @param modulename String the name of the module.
 * @return value String[] the names of the parameters for a module.
 */
public String[] getModuleParameterNames(String modulename);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public String getModuleParameterString(String modulname, String parameter);
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public String getModuleParameterString(String modulname, String parameter, String defaultValue);
	/**
	 * This method returns the type of a parameter in a module.
	 *
	 * @param modulename the name of the module.
	 * @param parameter the name of the parameter.
	 * @return the type of the parameter.
	 */
	public String getModuleParameterType(String modulename, String parameter);
/**
 * Returns all repositories for a module.
 *
 * @parameter String modulname the name of the module.
 * @return java.lang.String[] the reprositories of a module.
 */
public String[] getModuleRepositories(String modulename);
/**
 * Returns the upload-date for the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the upload-date for the module.
 */
public long getModuleUploadDate(String modulename);
/**
 * Returns the user-name of the user who had uploaded the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the user-name of the user who had uploaded the module.
 */
public String getModuleUploadedBy(String module);
/**
 * This method returns the version of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the version of the module.
 */
public int getModuleVersion(String modulename);
/**
 * Returns the name of the view, that is implemented by the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the name of the view, that is implemented by the module.
 */
public String getModuleViewName(String modulename);
/**
 * Returns the url to the view-url for the module within the system. 
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the view-url to the module.
 */
public String getModuleViewUrl(String modulename);
/**
 * Returns all repositories for all modules.
 * 
 * @return java.lang.String[] the reprositories of all modules.
 */
public String[] getRepositories();
/**
 * Returns all views and korresponding urls for all modules.
 *
 * @parameter String[] views in this parameter the views will be returned.
 * @parameter String[] urls in this parameters the urls vor the views will be returned.
 * @return int the amount of views.
 */
public int getViews(Vector views, Vector urls);
/**
 *  Checks for files that already exist in the system but should be replaced by the module.
 *
 *  @param moduleZip The name of the zip-file to import.
 *  @returns The complete paths to the resources that have conflicts.
 */
public Vector importGetConflictingFileNames(String moduleZip) throws CmsException;
/**
 *  Imports a module. This method is synchronized, so only one module can be imported at on time.
 *
 *  @param moduleZip the name of the zip-file to import from.
 *  @param exclusion a Vector with resource-names that should be excluded from this import.
 */
public void importModule(String moduleZip, Vector exclusion) throws CmsException;
/**
 * Checks if the module exists already in the repository.
 * 
 * @parameter String the name of the module.
 * @return true if the module exists, else false.
 */
public boolean moduleExists(String modulename);
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, byte value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, double value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, float value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, int value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, long value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Boolean value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Byte value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Double value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Float value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Integer value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, Long value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param value java.lang.String the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, String value) throws CmsException;
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, boolean value) throws CmsException;
}

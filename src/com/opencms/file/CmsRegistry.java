package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRegistry.java,v $
 * Date   : $Date: 2000/09/05 17:13:05 $
 * Version: $Revision: 1.7 $
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import org.w3c.dom.*;
import com.opencms.template.*;
import com.opencms.core.*;

/**
 * This class implements the registry for OpenCms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.7 $ $Date: 2000/09/05 17:13:05 $
 * 
 */
public class CmsRegistry extends A_CmsXmlContent implements I_CmsRegistry {

	/**
	 *  The xml-document representing the registry.
	 */
	private Document m_xmlReg;

	/**
	 *  The filename for the registry.
	 */
	private String m_regFileName;

	/**
	 *  A hashtable with shortcuts into the dom-structure for each module.
	 */
	private Hashtable m_modules = new Hashtable();

	/**
	 *  The cms-object to get access to the system with the context of the current user.
	 */
	private CmsObject m_cms = null;

	/**
	 *  The date-format to use.
	 */
	private SimpleDateFormat m_dateFormat = new java.text.SimpleDateFormat("MM.dd.yyyy");

/**
 * Creates a new CmsRegistry for a user. The cms-object represents the current state of the current user.
 *
 * @param CmsObject the cms-object to get access to the system
 */
public CmsRegistry(CmsRegistry reg, CmsObject cms) {
	super();
	// there is no need of a real copy for this parameters
	m_modules = reg.m_modules;
	m_regFileName = reg.m_regFileName;
	m_xmlReg = reg.m_xmlReg;
	// store the cms-object for this instance.
	m_cms = cms;
}
/**
 * Creates a new CmsRegistry. The regFileName is the path to the registry-file in
 * the server filesystem.
 *
 *  @param String regFileName the path to the registry-file in the server fs.
 */
public CmsRegistry(String regFileName) throws CmsException {
	super();
	try {
		// store the filename
		m_regFileName = regFileName;

		// get the file
		File xmlFile = new File(m_regFileName);

		// get a buffered reader
		BufferedReader reader = new BufferedReader(new FileReader(xmlFile));

		String content = "";
		String buffer = "";
		do {
			content += buffer;
			buffer = reader.readLine();
		} while (buffer != null);

		reader.close();

		// parse the registry-xmlfile and store it.
		m_xmlReg = parse(content);
		init();
	} catch (Exception exc) {
		throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
	}
}
/**
 * Checks, if the dependencies are fullfilled.
 * @param module the dom-element describing the new module.
 * @returns, if the dependencies are fullfilled, or not.
 */
private Vector checkDependencies(Element module) throws CmsException {
	Vector retValue = new Vector();
	try {
		Element dependencies = (Element) (module.getElementsByTagName("dependencies").item(0));
		NodeList deps = dependencies.getElementsByTagName("dependency");
		for (int i = 0; i < deps.getLength(); i++) {
			String name = ((Element) deps.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			int minVersion = Integer.parseInt(((Element) deps.item(i)).getElementsByTagName("minversion").item(0).getFirstChild().getNodeValue());
			int maxVersion = Integer.parseInt(((Element) deps.item(i)).getElementsByTagName("maxversion").item(0).getFirstChild().getNodeValue());

			// get the version of the needed repository
			int currentVersion = getModuleVersion(name);

			if( currentVersion == -1 ) {
				retValue.addElement(name + " dosen't exists");
			} else if( currentVersion < minVersion ) {
				retValue.addElement(name + " version is not high enougth" );
			} else if( ( maxVersion != C_ANY_VERSION ) && (currentVersion > maxVersion) ) {
				retValue.addElement(name + " version is to high");
			}
		}
	} catch (Exception exc) {
		throw new CmsException("Could not check the dependencies", CmsException.C_REGISTRY_ERROR, exc);
	}
	return retValue;
}
	/**
	 *  Checks if the type of the value is correct.
	 *  @param type the type that the value should have..
	 *  @param value the value to check.
	 */
	private boolean checkType(String type, String value) {
		type = type.toLowerCase();
		try {
			if("string".equals(type) ) {
				if( value != null) {
					return true;
				} else {
					return false;
				}				
			} else if("int".equals(type) || "integer".equals(type)) {
				Integer.parseInt(value);
				return true;
			} else if("float".equals(type)) {
				Float.valueOf(value);
				return true;
			} else if("boolean".equals(type)) {
				Boolean.valueOf(value);
				return true;
			} else if("long".equals(type)) {
				Long.valueOf(value);
				return true;
			} else if("double".equals(type)) {
				Double.valueOf(value);
				return true;
			} else if("byte".equals(type)) {
				Byte.valueOf(value);
				return true;
			} else {
				// the type dosen't exist
				return false;
			}
		} catch(Exception exc) {
			// the type of the value was wrong
			return false;
		}
	}
/**
 * This method clones the registry.
 *
 * @param CmsObject the current cms-object to get access to the system.
 * @return the cloned registry.
 */
public I_CmsRegistry clone(CmsObject cms) {
	return new CmsRegistry(this, cms);
}
/**
 *  Deletes a module. This method is synchronized, so only one module can be deleted at one time.
 *
 *  @param module-name the name of the module that should be deleted.
 *  @param exclusion a Vector with resource-names that should be excluded from this deletion.
 */
public synchronized void deleteModule(String module, Vector exclusion) throws CmsException {
	// check if the user is allowed to perform this action
	if (!hasAccess()) {
		throw new CmsException("No access to perform the action 'importModule'", CmsException.C_REGISTRY_ERROR);
	}

	// TODO: invoke the event-method

	// TODO: check, if deletion is allowed

	// get the files, that are belonging to the module.
	Vector resourceNames = new Vector();
	Vector resourceCodes = new Vector();
	getModuleFiles(module, resourceNames, resourceCodes);

	// move through all resource-names and try to delete them
	for (int i = resourceNames.size() - 1; i >= 0; i--) {
		try {
			String currentResource = (String) resourceNames.elementAt(i);
			if (!exclusion.contains(currentResource)) {
				m_cms.lockResource(currentResource, true);
				if(currentResource.endsWith("/") ) {
					// this is a folder
					m_cms.deleteFolder(currentResource);
				} else {
					// this is a file
					m_cms.deleteFile(currentResource);
				}
			}
		} catch (CmsException exc) {
			// ignore the exception and delete the next resource.
			exc.printStackTrace();
		}
	}

	// delete all entries for the module in the registry
	Element moduleElement = getModuleElement(module);
	moduleElement.getParentNode().removeChild(moduleElement);
	saveRegistry();
	try {
		init();
	} catch (Exception exc) {
		throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
	}
}
/**
 *  Checks for files that already exist in the system but should be replaced by the module.
 *
 *  @param moduleZip The name of the zip-file to import.
 *  @returns The complete paths to the resources that have conflicts.
 */
public Vector getConflictingFileNames(String moduleZip) throws CmsException {
	if (!hasAccess()) {
		throw new CmsException("No access to perform the action 'getConflictingFileNames'", CmsException.C_REGISTRY_ERROR);
	}
	// TODO: getConflictingFileNames
	return new Vector();
}
	/**
	 * Gets a description of this content type.
	 * For OpenCms internal use only.
	 * @return Content type description.
	 */
	public String getContentDescription() {
		return "Registry";
	}
/**
 * This method returns the author of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the author of the module.
 */
public String getModuleAuthor(String modulename) {
	return getModuleData(modulename, "author");
}
/**
 * This method returns the email of author of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the email of author of the module.
 */
public String getModuleAuthorEmail(String modulename) {
	return getModuleData(modulename, "email");
}
/**
 * Gets the create date of the module.
 *
 * @parameter String the name of the module.
 * @return long the create date of the module.
 */
public long getModuleCreateDate(String modulname) {
	long retValue = -1;
	try {
		String value = getModuleData(modulname, "creationdate");
		retValue = m_dateFormat.parse(value).getTime();
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 *  Private method to return module data like author.
 *
 * @param String modulename the name of the module.
 * @param String dataName the name of the tag to get the data from.
 * @returns String the value for the requested data.
 */
private String getModuleData(String module, String dataName) {
	String retValue = null;
	try {
		Element moduleElement = getModuleElement(module);
		retValue = moduleElement.getElementsByTagName(dataName).item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - registry is not wellformed
	}
	return retValue;
}
/**
 * Returns the module dependencies for the module.
 *
 * @param module String the name of the module to check.
 * @param modules Vector in this parameter the names of the dependend modules will be returned.
 * @param minVersions Vector in this parameter the minimum versions of the dependend modules will be returned.
 * @param maxVersions Vector in this parameter the maximum versions of the dependend modules will be returned.
 * @return int the amount of dependencies for the module will be returned.
 */
public int getModuleDependencies(String modulename, Vector modules, Vector minVersions, Vector maxVersions) {
	try {
		Element module = getModuleElement(modulename);
		Element dependencies = (Element) (module.getElementsByTagName("dependencies").item(0));
		NodeList deps = dependencies.getElementsByTagName("dependency");
		for (int i = 0; i < deps.getLength(); i++) {
			modules.addElement(((Element) deps.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
			minVersions.addElement(((Element) deps.item(i)).getElementsByTagName("minversion").item(0).getFirstChild().getNodeValue());
			maxVersions.addElement(((Element) deps.item(i)).getElementsByTagName("maxversion").item(0).getFirstChild().getNodeValue());
		}
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return modules.size();
}
/**
 * Returns the description of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the description of the module.
 */
public String getModuleDescription(String module) {
	return getModuleData(module, "description");
}
/**
 * Gets the url to the documentation of the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the url to the documentation of the module.
 */
public String getModuleDocumentPath(String modulename) {
	return getModuleData(modulename, "documentation");
}
/**
 *  Private method to get the Element representing a module.
 *
 * @param String the name of the module.
 *
 */
private Element getModuleElement(String name) {
	return (Element) m_modules.get(name);
}
/**
 * Reads the module-element from the manifest in the zip-file.
 * @param string the name of the zip-file to read from.
 * @returns the module-element or null if it dosen't exist.
 */
private Element getModuleElementFromImport(String filename) {
	try {
		// get the zip-file
		ZipFile importZip = new ZipFile(filename);
		// read the minifest
		ZipEntry entry = importZip.getEntry("manifest.xml");
		InputStream stream = importZip.getInputStream(entry);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String content = "";
		String buffer = "";
		do {
			content += buffer;
			buffer = reader.readLine();
		} while (buffer != null);
		// parse the manifest
		Document manifest = parse(content);
		reader.close();
		importZip.close();
		// get the module-element
		return (Element)(manifest.getElementsByTagName("module").item(0));
	} catch (Exception exc) {
		return null;
	}
}
/**
 * Returns all filenames and hashcodes belonging to the module.
 *
 * @param String modulname the name of the module.
 * @param retNames the names of the resources belonging to the module.
 * @param retCodes the hashcodes of the resources belonging to the module.
 * @return the amount of entrys.
 */
public int getModuleFiles(String modulename, Vector retNames, Vector retCodes) {
	try {
		Element module = getModuleElement(modulename);
		Element files = (Element) (module.getElementsByTagName("files").item(0));
		NodeList file = files.getElementsByTagName("file");
		for (int i = 0; i < file.getLength(); i++) {
			retNames.addElement(((Element) file.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue());
			retCodes.addElement(((Element) file.item(i)).getElementsByTagName("checksum").item(0).getFirstChild().getNodeValue());
		}
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retNames.size();
}
/**
 * Returns the class, that receives all maintenance-events for the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.Class that receives all maintenance-events for the module.
 */
public Class getModuleMaintenanceEventClass(String modulname) {
	return null;
}
/**
 * Returns the names of all available modules.
 *
 * @return Enumeration the names of all available modules.
 */
public Enumeration getModuleNames() {
	return m_modules.keys();
}
/**
 * Gets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @return value java.lang.String the value to set for the parameter.
 */
public String getModuleParameter(String modulename, String parameter) {
	String retValue = null;
	try {
		Element param = getModuleParameterElement(modulename, parameter);
		retValue = param.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - parameter is not existent
	}
	return retValue;
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public boolean getModuleParameterBoolean(String modulname, String parameter) {
	if ("true".equals(getModuleParameter(modulname, parameter).toLowerCase())) {
		return true;
	} else {
		return false;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Boolean getModuleParameterBoolean(String modulname, String parameter, Boolean defaultValue) {
	return new Boolean(getModuleParameterBoolean(modulname, parameter, defaultValue.booleanValue()));
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public boolean getModuleParameterBoolean(String modulname, String parameter, boolean defaultValue) {
	if (getModuleParameterBoolean(modulname, parameter)) {
		return true;
	} else {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public byte getModuleParameterByte(String modulname, String parameter) {
	return Byte.parseByte(getModuleParameter(modulname, parameter));
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public byte getModuleParameterByte(String modulname, String parameter, byte defaultValue) {
	try {
		return getModuleParameterByte(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Byte getModuleParameterByte(String modulname, String parameter, Byte defaultValue) {
	return new Byte(getModuleParameterByte(modulname, parameter, defaultValue.byteValue()));
}
/**
 * Returns a description for parameter in a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return String the description for the parameter in the module.
 */
public String getModuleParameterDescription(String modulname, String parameter) {
	String retValue = null;
	try {
		Element param = getModuleParameterElement(modulname, parameter);
		retValue = param.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - parameter is not existent
	}
	return retValue;
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public double getModuleParameterDouble(String modulname, String parameter) {
	return Double.valueOf(getModuleParameter(modulname, parameter)).doubleValue();
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public double getModuleParameterDouble(String modulname, String parameter, double defaultValue) {
	try {
		return getModuleParameterDouble(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Double getModuleParameterDouble(String modulname, String parameter, Double defaultValue) {
	return new Double(getModuleParameterDouble(modulname, parameter, defaultValue.doubleValue()));
}
/**
 * Private method to get them XML-Element for a parameter in a module.
 * 
 * @param modulename String the name of the module.
 * @param parameter String the name of the parameter.
 * @return Element the XML-Element corresponding to the parameter.
 */
private Element getModuleParameterElement(String modulename, String parameter) {
	Element retValue = null;
	try {
		Element module = getModuleElement(modulename);
		Element parameters = (Element) (module.getElementsByTagName("parameters").item(0));
		NodeList para = parameters.getElementsByTagName("para");
		for (int i = 0; i < para.getLength(); i++) {
			if (((Element) para.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue().equals(parameter)) {
				// this is the element for the parameter.
				retValue = (Element) para.item(i);
				// stop searching - parameter was found
				break;
			}
		}
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public float getModuleParameterFloat(String modulname, String parameter) {
	return Float.valueOf(getModuleParameter(modulname, parameter)).floatValue();
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public float getModuleParameterFloat(String modulname, String parameter, float defaultValue) {
	try {
		return getModuleParameterFloat(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Float getModuleParameterFloat(String modulname, String parameter, Float defaultValue) {
	return new Float(getModuleParameterFloat(modulname, parameter, defaultValue.floatValue()));
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public int getModuleParameterInteger(String modulname, String parameter) {
	return Integer.parseInt(getModuleParameter(modulname, parameter));
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public int getModuleParameterInteger(String modulname, String parameter, int defaultValue) {
	try {
		return getModuleParameterInteger(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Integer getModuleParameterInteger(String modulname, String parameter, Integer defaultValue) {
	return new Integer(getModuleParameterInteger(modulname, parameter, defaultValue.intValue()));
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public long getModuleParameterLong(String modulname, String parameter) {
	return Long.valueOf(getModuleParameter(modulname, parameter)).longValue();
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public long getModuleParameterLong(String modulname, String parameter, long defaultValue) {
	try {
		return getModuleParameterLong(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public Long getModuleParameterLong(String modulname, String parameter, Long defaultValue) {
	return new Long(getModuleParameterLong(modulname, parameter, defaultValue.longValue()));
}
/**
 * Gets all parameter-names for a module.
 * 
 * @param modulename String the name of the module.
 * @return value String[] the names of the parameters for a module.
 */
public String[] getModuleParameterNames(String modulename) {
	String[] retValue = null;
	try {
		Element module = getModuleElement(modulename);
		Element parameters = (Element) (module.getElementsByTagName("parameters").item(0));
		NodeList para = parameters.getElementsByTagName("para");
		retValue = new String[para.getLength()];
		for (int i = 0; i < para.getLength(); i++) {
			retValue[i] = ((Element) para.item(i)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		}
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @return boolean the value for the parameter in the module.
 */
public String getModuleParameterString(String modulname, String parameter) {
	return getModuleParameter(modulname, parameter);
}
/**
 * Returns a parameter for a module.
 * 
 * @param modulname String the name of the module.
 * @param parameter String the name of the parameter.
 * @param default the default value.
 * @return boolean the value for the parameter in the module.
 */
public String getModuleParameterString(String modulname, String parameter, String defaultValue) {
	try {
		return getModuleParameterString(modulname, parameter);
	} catch (Exception exc) {
		return defaultValue;
	}
}
/**
 * This method returns the type of a parameter in a module.
 *
 * @param modulename the name of the module.
 * @param parameter the name of the parameter.
 * @return the type of the parameter.
 */
public String getModuleParameterType(String modulename, String parameter) {
	String retValue = null;
	try {
		Element param = getModuleParameterElement(modulename, parameter);
		retValue = param.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - parameter is not existent
	}
	return retValue;
}
/**
 * Returns all repositories for a module.
 *
 * @parameter String modulname the name of the module.
 * @return java.lang.String[] the reprositories of a module.
 */
public java.lang.String[] getModuleRepositories(String modulename) {
	String[] retValue = null;
	try {
		Element module = getModuleElement(modulename);
		Element repository = (Element) (module.getElementsByTagName("repository").item(0));
		NodeList paths = repository.getElementsByTagName("path");
		retValue = new String[paths.getLength()];
		for (int i = 0; i < paths.getLength(); i++) {
			retValue[i] = paths.item(i).getFirstChild().getNodeValue();
		}
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns the upload-date for the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the upload-date for the module.
 */
public long getModuleUploadDate(String modulname) {
	long retValue = -1;
	try {
		String value = getModuleData(modulname, "uploaddate");
		retValue = m_dateFormat.parse(value).getTime();
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns the user-name of the user who had uploaded the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the user-name of the user who had uploaded the module.
 */
public String getModuleUploadedBy(String modulename) {
	return getModuleData(modulename, "uploadedby");
}
/**
 * This method returns the version of the module.
 *
 * @parameter String the name of the module.
 * @return java.lang.String the version of the module.
 */
public int getModuleVersion(String modulename) {
	int retValue = -1;
	try {
		retValue = Integer.parseInt(getModuleData(modulename, "version"));
	} catch (Exception exc) { 
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns the name of the view, that is implemented by the module.
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the name of the view, that is implemented by the module.
 */
public String getModuleViewName(String modulename) {
	String retValue = null;
	try {
		Element module = getModuleElement(modulename);
		Element view = (Element) (module.getElementsByTagName("view").item(0));
		retValue = view.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns the url to the view-url for the module within the system. 
 * 
 * @parameter String the name of the module.
 * @return java.lang.String the view-url to the module.
 */
public String getModuleViewUrl(String modulname) {
	String retValue = null;
	try {
		Element module = getModuleElement(modulname);
		Element view = (Element) (module.getElementsByTagName("view").item(0));
		retValue = view.getElementsByTagName("url").item(0).getFirstChild().getNodeValue();
	} catch (Exception exc) {
		// ignore the exception - reg is not welformed
	}
	return retValue;
}
/**
 * Returns all repositories for all modules.
 * 
 * @return java.lang.String[] the reprositories of all modules.
 */
public java.lang.String[] getRepositories() {
	NodeList repositories = m_xmlReg.getElementsByTagName("repository");
	Vector retValue = new Vector();
	String[] retValueArray = new String[0];
	for (int x = 0; x < repositories.getLength(); x++) {
		NodeList paths = ((Element) repositories.item(x)).getElementsByTagName("path");
		for (int y = 0; y < paths.getLength(); y++) {
			retValue.addElement(paths.item(y).getFirstChild().getNodeValue());
		}
	}
	retValueArray = new String[retValue.size()];
	retValue.copyInto(retValueArray);
	return retValueArray;
}
/**
 * Returns all views and korresponding urls for all modules.
 *
 * @parameter String[] views in this parameter the views will be returned.
 * @parameter String[] urls in this parameters the urls vor the views will be returned.
 * @return int the amount of views.
 */
public int getViews(Vector views, Vector urls) {
	try {
		NodeList viewList = m_xmlReg.getElementsByTagName("view");
		for (int x = 0; x < viewList.getLength(); x++) {
			try {
				String name = ((Element) viewList.item(x)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
				String url = ((Element) viewList.item(x)).getElementsByTagName("url").item(0).getFirstChild().getNodeValue();
				views.addElement(name);
				urls.addElement(url);
			} catch(Exception exc) {
				// ignore the exception and try the next view-pair.
			}
		}
		return views.size();
	} catch (Exception exc) {
		// no return-vaules
		return 0;
	}
}
	/**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public String getXmlDocumentTagName() {
		return "registry";
	}
/**
 * Returns true if the user has write-access to the registry. Otherwise false.
 * @returns true if access is granted, else false.
 */
private boolean hasAccess() {
	// check the access - only the admin has write access.
	boolean retValue = false;
	try{
		retValue = m_cms.isAdmin();
	} catch(CmsException exc) {
		// ignore the exception - no access granted
	}
	return retValue;
}
/**
 *  Checks for files that already exist in the system but should be replaced by the module.
 *
 *  @param moduleZip The name of the zip-file to import.
 *  @returns The complete paths to the resources that have conflicts.
 */
public Vector importGetConflictingFileNames(String moduleZip) throws CmsException {
	if (!hasAccess()) {
		throw new CmsException("No access to perform the action 'getConflictingFileNames'", CmsException.C_REGISTRY_ERROR);
	}

	CmsImport cmsImport = new CmsImport(moduleZip, "/", m_cms);
	return cmsImport.getConflictingFilenames();
}
/**
 *  Imports a module. This method is synchronized, so only one module can be imported at on time.
 *
 *  @param moduleZip the name of the zip-file to import from.
 *  @param exclusion a Vector with resource-names that should be excluded from this import.
 */
public synchronized void importModule(String moduleZip, Vector exclusion) throws CmsException {
	// check if the user is allowed to set parameters

	if (!hasAccess()) {
		throw new CmsException("No access to perform the action 'importModule'", CmsException.C_REGISTRY_ERROR);
	}
	Element newModule = getModuleElementFromImport(moduleZip);
	String newModuleName = newModule.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
	String newModuleVersion = newModule.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();

	// exists the module already?
	if (moduleExists(newModuleName)) {
		throw new CmsException("The module " + newModuleName + " exists already", CmsException.C_REGISTRY_ERROR);
	}
	Vector dependencies = checkDependencies(newModule);

	// are there any dependencies not fulfilled?
	if (dependencies.size() != 0) {
		throw new CmsException("the dependencies for the module are not fulfilled.", CmsException.C_REGISTRY_ERROR);
	}
	Vector resourceNames = new Vector();
	Vector resourceCodes = new Vector();
	CmsImport cmsImport = new CmsImport(moduleZip, "/", m_cms);
	cmsImport.importResources(exclusion, resourceNames, resourceCodes, "module", newModuleName + "_" + newModuleVersion);
	// import the module data into the registry
	Element regModules = (Element) (m_xmlReg.getElementsByTagName("modules").item(0));
	// set the import-date
	Node uploadDate = newModule.getOwnerDocument().createElement("uploaddate");

	uploadDate.appendChild(newModule.getOwnerDocument().createTextNode(m_dateFormat.format(new java.util.Date())));
	newModule.appendChild(uploadDate);

	// set the import-user
	Node uploadBy = newModule.getOwnerDocument().createElement("uploadedby");
	uploadBy.appendChild(newModule.getOwnerDocument().createTextNode(m_cms.getRequestContext().currentUser().getName()));
	newModule.appendChild(uploadBy);

	// set the files
	Node files = newModule.getOwnerDocument().createElement("files");

	// store the resources-names that are depending to the module
	for (int i = 0; i < resourceNames.size(); i++) {
		Node file = newModule.getOwnerDocument().createElement("file");
		files.appendChild(file);
		Node name = newModule.getOwnerDocument().createElement("name");
		file.appendChild(name);
		Node checksum = newModule.getOwnerDocument().createElement("checksum");
		file.appendChild(checksum);
		name.appendChild(newModule.getOwnerDocument().createTextNode((String) resourceNames.elementAt(i)));
		checksum.appendChild(newModule.getOwnerDocument().createTextNode(com.opencms.util.Encoder.escape( (String) resourceCodes.elementAt(i))));
	}

	// append the files to the module-entry
	newModule.appendChild(files);

	// append the module data to the registry
	Node newNode = getXmlParser().importNode(m_xmlReg, newModule);
	regModules.appendChild(newNode);
	saveRegistry();

	// TODO: invoke the event-method

	try {
		init();
	} catch (Exception exc) {
		throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
	}
}
/**
 *  Inits all member-variables for the instance.
 */
private void init() throws Exception {
	// get the entry-points for the modules
	NodeList modules = m_xmlReg.getElementsByTagName("module");
	// create the hashtable for the shortcuts
	m_modules.clear();

	// walk throug all modules
	for (int i = 0; i < modules.getLength(); i++) {
		Element module = (Element) modules.item(i);
		String moduleName = module.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();

		// store the shortcuts to the modules
		m_modules.put(moduleName, module);
	}
}
/**
 * Checks if the module exists already in the repository.
 * 
 * @parameter String the name of the module.
 * @return true if the module exists, else false.
 */
public boolean moduleExists(String modulename) {
	return m_modules.containsKey(modulename);
}
/**
 *  Saves the registry and stores it to the registry-file.
 */
private void saveRegistry() throws CmsException {
	try {
		// get the file
		File xmlFile = new File(m_regFileName);

		// get a buffered writer
		BufferedWriter xmlWriter = new BufferedWriter(new FileWriter(xmlFile));

		// parse the registry-xmlfile and store it.
		A_CmsXmlContent.getXmlParser().getXmlText(m_xmlReg, xmlWriter);
	} catch (Exception exc) {
		throw new CmsException("couldn't save registry", CmsException.C_REGISTRY_ERROR, exc);
	}
}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, byte value) throws CmsException {
		setModuleParameter(modulename, parameter, value + "");
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, double value) throws CmsException {
		setModuleParameter(modulename, parameter, value + "");
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, float value) throws CmsException {
		setModuleParameter(modulename, parameter, value + "");
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, int value) throws CmsException {
		setModuleParameter(modulename, parameter, value + "");
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, long value) throws CmsException {
		setModuleParameter(modulename, parameter, value + "");
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Boolean value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Byte value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Double value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Float value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Integer value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, Long value) throws CmsException {
		setModuleParameter(modulename, parameter, value.toString());
	}
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param value java.lang.String the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, String value) throws CmsException {
	// check if the user is allowed to set parameters

	if (!hasAccess()) {
		throw new CmsException("No access to perform the action 'setModuleParameter'", CmsException.C_REGISTRY_ERROR);
	}
	try {
		Element param = getModuleParameterElement(modulename, parameter);
		if (!checkType(getModuleParameterType(modulename, parameter), value)) {
			throw new CmsException("wrong number format for " + parameter + " -> " + value, CmsException.C_REGISTRY_ERROR);
		}
		param.getElementsByTagName("value").item(0).getFirstChild().setNodeValue(value);
		saveRegistry();
	} catch (CmsException exc) {
		throw exc;
	} catch (Exception exc) {
		throw new CmsException("couldn't set parameter " + parameter + " for module " + modulename + " to vale " + value, CmsException.C_REGISTRY_ERROR, exc);
	}
}
/**
 * Sets a parameter for a module.
 * 
 * @param modulename java.lang.String the name of the module.
 * @param parameter java.lang.String the name of the parameter to set.
 * @param the value to set for the parameter.
 */
public void setModuleParameter(String modulename, String parameter, boolean value) throws CmsException {
	setModuleParameter(modulename, parameter, value + "");
}
}

package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRegistry.java,v $
 * Date   : $Date: 2000/08/21 08:11:27 $
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

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.opencms.template.*;
import com.opencms.core.*;

/**
 * This class implements the registry for OpenCms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/08/21 08:11:27 $
 * 
 */
public class CmsRegistry implements I_CmsRegistry {

	/**
	 *  The xml-document representing the registry.
	 */
	private Document m_xmlReg;

	/**
	 *  A hashtable with shortcuts into the dom-structure for each module.
	 */
	private Hashtable m_modules;

/**
 * Creates a new CmsRegistry. The regFileName is the path to the registry-file in
 * the server filesystem.
 *
 *  @param String regFileName the path to the registry-file in the server fs.
 */
public CmsRegistry(String regFileName) throws CmsException {
	super();
	try {

		// get the file
		File xmlFile = new File(regFileName);

		// get a buffered reader	
		BufferedReader xmlReader = new BufferedReader(new FileReader(xmlFile));

		// parse the registry-xmlfile and store it.
		m_xmlReg = A_CmsXmlContent.getXmlParser().parse(xmlReader);
		init();
	} catch (Exception exc) {
		throw new CmsException("couldn't init registry", CmsException.C_REGISTRY_ERROR, exc);
	}
}
	/**
	 * This method clones the registry.
	 *
	 * @return the cloned registry.
	 */
	public Object clone() {
		return null;
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
		retValue = Long.parseLong(value);
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
	 * @param modules[] String in this parameter the names of the dependend modules will be returned.
	 * @param minVersions int[] in this parameter the minimum versions of the dependend modules will be returned.
	 * @param maxVersions int[] in this parameter the maximum versions of the dependend modules will be returned.
	 * @return int the amount of dependencies for the module will be returned.
	 */
	public int getModuleDependencies(String module, java.lang.String[] modules, int[] minVersions, int[] maxVersions) {
		String mod[] = {"TestModule A", "TestModule b"};
		int min[] = {2, 5};
		int max[] = {5, C_ANY_VERSION};
		modules = mod;
		minVersions = min;
		maxVersions = max;
		return mod.length;
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
		return "www.welivit.de";
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @return boolean the value for the parameter in the module.
	 */
	public boolean getModuleParameterBoolean(String modulname, String parameter) {
		return false;
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @param default the default value.
	 * @return boolean the value for the parameter in the module.
	 */
	public boolean getModuleParameterBoolean(String modulname, String parameter, Boolean defaultValue) {
		return false;
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
		return false;
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
		return (byte) 0;
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
		return (byte) 0;
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
		return new Byte((byte) 0);
	}
	/**
	 * Returns a description for parameter in a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @return String the description for the parameter in the module.
	 */
	public String getModuleParameterDescription(String modulname, String parameter) {
		return "A very interesting parameter. You will never forget it.";
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @return boolean the value for the parameter in the module.
	 */
	public double getModuleParameterDouble(String modulname, String parameter) {
		return 0;
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
		return 0;
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
		return new Double(0);
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @param default the default value.
	 * @return boolean the value for the parameter in the module.
	 */
	public long getModuleParameterFloat(String modulname, String parameter) {
		return 0;
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
		return 0;
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
		return new Float(0);
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @return boolean the value for the parameter in the module.
	 */
	public int getModuleParameterInteger(String modulname, String parameter) {
		return 0;
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
		return 0;
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
		return new Integer(0);
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
		return 0;
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
		return 0;
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
		return new Long(0);
	}
	/**
	 * Returns a parameter for a module.
	 * 
	 * @param modulname String the name of the module.
	 * @param parameter String the name of the parameter.
	 * @return boolean the value for the parameter in the module.
	 */
	public String getModuleParameterString(String modulname, String parameter) {
		return "This is a parameter value";
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
		return defaultValue;
	}
	/**
	 * This method returns the type of a parameter in a module.
	 *
	 * @param modulename the name of the module.
	 * @param parameter the name of the parameter.
	 * @return the type of the parameter.
	 */
	public String getModuleParameterType(String modulename, String parameter) {
		return "String";
	}
/**
 * Returns all repositories for a module.
 *
 * @parameter String modulname the name of the module.
 * @return java.lang.String[] the reprositories of a module.
 */
public java.lang.String[] getModuleRepositories(String modulename) {
	String[] retValue = new String[0];
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
		retValue = Long.parseLong(value);
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
			String name = ((Element) viewList.item(x)).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
			String url = ((Element) viewList.item(x)).getElementsByTagName("url").item(0).getFirstChild().getNodeValue();
			views.addElement(name);
			urls.addElement(url);
		}
		return views.size();
	} catch (Exception exc) {
		// no return-vaules
		return 0;
	}
}
	/**
	 *  Inits all member-variables for the instance.
	 */
	private void init() throws Exception {
		// get the entry-points for the modules
		NodeList modules = m_xmlReg.getElementsByTagName("module");

		// create the hashtable for the shortcuts
		m_modules = new Hashtable(modules.getLength());

		// walk throug all modules
		for (int i = 0; i < modules.getLength(); i++) {
			Element module = (Element) modules.item(i);
			String moduleName = module.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();

			// store the shortcuts to the modules
			m_modules.put(moduleName, module);
		}
	}
	/**
	 * Sets a parameter for a module.
	 * 
	 * @param modulename java.lang.String the name of the module.
	 * @param parameter java.lang.String the name of the parameter to set.
	 * @param value java.lang.String the value to set for the parameter.
	 */
	public void setModuleParameter(String modulename, String parameter, String value) {
	}
}

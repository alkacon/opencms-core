package com.opencms.workplace;

/**
 * The class is used to handle the template for the new site functionallity
 * Creation date: (25-09-2000 12:29:08)
 * @author: Martin Langelund
 */
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

import javax.servlet.http.*;

public class CmsAdminSiteNew extends CmsWorkplaceDefault implements com.opencms.core.I_CmsConstants {
/**
 * Gets all available categories
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the current value in the vectors.
 * @exception CmsException
 */
public Integer getCategories(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	Vector categories = cms.getAllCategories();

	for (int z = 0; z < categories.size(); z++)
	{
		names.addElement(((CmsCategory) categories.elementAt(z)).getName());
		values.addElement(new String(""+((CmsCategory) categories.elementAt(z)).getId()));
	}
	return new Integer(0);
}
/**
 * Gets the content of a defined section in a given template file and its subtemplates
 * with the given parameters. 
 * 
 * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
 * @param cms CmsObject Object for accessing system resources.
 * @param templateFile Filename of the template file.
 * @param elementName Element name of this template in our parent template.
 * @param parameters Hashtable with all template class parameters.
 * @param templateSelector template section that should be processed.
 */
public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws com.opencms.core.CmsException
{
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	if (parameters.get("submitform") != null)
	{
		//System.err.println("***********************************************");
		if (((String) parameters.get("NAME")).equals("") || ((String) parameters.get("DOMAINNAME")).equals("") || ((String) parameters.get("DESCRIPTION")).equals(""))
		{
			templateSelector = "datamissing";
		}
		else
		{
			cms.newSite((String) parameters.get("NAME"), (String) parameters.get("DESCRIPTION"), Integer.parseInt((String) parameters.get("CATEGORY")), Integer.parseInt((String) parameters.get("LANGUAGE")), Integer.parseInt((String) parameters.get("DOMAIN")), (String) parameters.get("DOMAINNAME"), (String) parameters.get("MANAGERGROUP"), (String) parameters.get("GROUP"));
			templateSelector = "wait";
		}
	}
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
/**
 * Gets all available domains
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the current value in the vectors.
 * @exception CmsException
 */
public Integer getDomains(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	Vector countries = cms.getAllCountries();

	for (int z = 0; z < countries.size(); z++)
	{
		names.addElement(((CmsCountry) countries.elementAt(z)).getName());
		values.addElement(new String(""+((CmsCountry) countries.elementAt(z)).getCountryId()));
	}
	return new Integer(0);
}
/**
 * Gets all available groups
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the current value in the vectors.
 * @exception CmsException
 */
public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException
{
	// get all groups
	Vector groups = cms.getGroups();
	int retValue = -1;
	String defaultGroup = C_GROUP_USERS;
	I_CmsSession session = cms.getRequestContext().getSession(true);
	String enteredGroup = (String) session.getValue("new_project_group");
	if (enteredGroup != null && !enteredGroup.equals(""))
	{
		// if an error has occurred before, take the previous entry of the user
		defaultGroup = enteredGroup;
	}
	// fill the names and values
	int n = 0;
	for (int z = 0; z < groups.size(); z++)
	{
		if (((CmsGroup) groups.elementAt(z)).getProjectCoWorker())
		{
			String name = ((CmsGroup) groups.elementAt(z)).getName();
			if (defaultGroup.equals(name))
			{
				retValue = n;
			}
			names.addElement(name);
			values.addElement(name);
			n++; // count the number of ProjectCoWorkers
		}
	}
	return new Integer(retValue);
}
/**
 * Gets all available languages
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the current value in the vectors.
 * @exception CmsException
 */
public Integer getLanguages(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	Vector languages = cms.getAllLanguages();
	for (int z = 0; z < languages.size(); z++)
	{
		names.addElement(((CmsLanguage) languages.elementAt(z)).getName());
		values.addElement(new String("" + ((CmsLanguage) languages.elementAt(z)).getLanguageId()));
	}
	return new Integer(0);
}
/**
 * Gets all available groups.
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the current value in the vectors.
 * @exception CmsException
 */
public Integer getManagerGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException
{
	// get all groups
	Vector groups = cms.getGroups();
	int retValue = -1;
	String defaultGroup = C_GROUP_PROJECTLEADER;
	I_CmsSession session = cms.getRequestContext().getSession(true);
	String enteredGroup = (String) session.getValue("new_project_managergroup");
	if (enteredGroup != null && !enteredGroup.equals(""))
	{
		// if an error has occurred before, take the previous entry of the user
		defaultGroup = enteredGroup;
	}

	// fill the names and values
	int n = 0;
	for (int z = 0; z < groups.size(); z++)
	{
		if (((CmsGroup) groups.elementAt(z)).getProjectmanager())
		{
			String name = ((CmsGroup) groups.elementAt(z)).getName();
			if (defaultGroup.equals(name))
			{
				retValue = n;
			}
			names.addElement(name);
			values.addElement(name);
			n++; // count the number of project managers
		}
	}
	return new Integer(retValue);
}
/**
 * Indicates if the results of this class are cacheable.
 * 
 * @param cms CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName Element name of this template in our parent template.
 * @param parameters Hashtable with all template class parameters.
 * @param templateSelector template section that should be processed.
 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
 */
public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
{
	return false;
}
}

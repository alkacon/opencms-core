package com.opencms.workplace;

/**
 * The class is used to handle the template for the edit site functionallity
 * Creation date: (28-09-2000 13:34:29)
 * @author: Martin Langelund
 */

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
public class CmsAdminSiteEdit extends CmsWorkplaceDefault implements com.opencms.core.I_CmsConstants
{
/**
 * Insert the method's description here.
 * Creation date: (28-09-2000 13:39:50)
 * @return java.lang.Integer
 * @param cms com.opencms.file.CmsObject
 * @param lang com.opencms.workplace.CmsXmlLanguageFile
 * @param names java.util.Vector
 * @param values java.util.Vector
 * @param parameters java.util.Hashtable
 * @exception com.opencms.core.CmsException The exception description.
 */
public Integer getCategories(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	I_CmsSession session = cms.getRequestContext().getSession(true);
	int category = Integer.parseInt((String) session.getValue("SITE_CATEGORY"));
	int index = 0;
	
	Vector categories = cms.getAllCategories();

	for (int z = 0; z < categories.size(); z++)
	{
		names.addElement(((CmsCategory) categories.elementAt(z)).getName());
		values.addElement(new String(""+((CmsCategory) categories.elementAt(z)).getId()));
		if (((CmsCategory) categories.elementAt(z)).getId()==category) index = z;
	}
	return new Integer(index);
}
/**
 * Insert the method's description here.
 * Creation date: (28-09-2000 13:39:23)
 * @return byte[]
 * @param cms com.opencms.file.CmsObject
 * @param templateFile java.lang.String
 * @param elementName java.lang.String
 * @param parameters java.util.Hashtable
 * @param templateSelector java.lang.String
 * @exception com.opencms.core.CmsException The exception description.
 */
public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws com.opencms.core.CmsException
{
	if (C_DEBUG && A_OpenCms.isLogging())
	{
		A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
		A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
		A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
	}
	I_CmsSession session = cms.getRequestContext().getSession(true);


	String name = (String) parameters.get("NAME");
	if (name==null) name = (String) session.getValue("SITE_NAME");
	String domainname = (String) parameters.get("DOMAINNAME");
	if (domainname==null) domainname = (String) session.getValue("SITE_DOMAINNAME");
	String description = (String) parameters.get("DESCRIPTION");
	if (description==null) description = (String) session.getValue("SITE_DESCRIPTION");
	String categoryId = (String) parameters.get("CATEGORY");
	if (categoryId==null) categoryId = (String) session.getValue("SITE_CATEGORY");
	String languageId = (String) parameters.get("LANGUAGE");
	if (languageId==null) languageId = (String) session.getValue("SITE_LANGUAGE");
	String domainId = (String) parameters.get("DOMAIN");
	if (domainId==null) domainId = (String) session.getValue("SITE_DOMAIN");
	String siteId = (String) session.getValue("SITE_ID");

	String initial = (String) parameters.get(C_PARA_INITIAL);
	if (initial != null)
	{
		session.removeValue("SITE_NAME");
		session.removeValue("SITE_DOMAINNAME");
		session.removeValue("SITE_DESCRIPTION");
		session.removeValue("SITE_CATEGORY");
		session.removeValue("SITE_LANGUAGE");
		session.removeValue("SITE_DOMAIN");
		session.removeValue("SITE_ID");
		CmsSite site = cms.getSiteBySiteId(Integer.parseInt((String) parameters.get("siteid")));
		name = site.getName();
		description = site.getDescription();
		Vector siteUrls = cms.getSiteUrls(site.getId());

		for (int i = 0; i < siteUrls.size(); i++)
			if (((CmsSiteUrls) siteUrls.elementAt(i)).getUrlId() == ((CmsSiteUrls) siteUrls.elementAt(i)).getPrimaryUrl())
				domainname = ((CmsSiteUrls) siteUrls.elementAt(0)).getUrl();
				
		siteId = "" + site.getId();
		categoryId = "" + site.getCategoryId();
		languageId = "" + site.getLanguageId();
		domainId = "" + site.getCountryId();
		
		session.putValue("SITE_ID", siteId);
	}
	
	session.putValue("SITE_NAME", name);
	session.putValue("SITE_DOMAINNAME", domainname);
	session.putValue("SITE_DESCRIPTION", description);
	session.putValue("SITE_CATEGORY", categoryId);
	session.putValue("SITE_LANGUAGE", languageId);
	session.putValue("SITE_DOMAIN", domainId);
	
	String action = (String) parameters.get("action");
	
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	
	if (parameters.get("submitform") != null)
	{		
		if (name.equals("") || domainname.equals("") || description.equals(""))
		{
			templateSelector = "datamissing";
		}
		else
		{
			session.removeValue("SITE_NAME");
			session.removeValue("SITE_DOMAINNAME");
			session.removeValue("SITE_DESCRIPTION");
			session.removeValue("SITE_CATEGORY");
			session.removeValue("SITE_LANGUAGE");
			session.removeValue("SITE_DOMAIN");
		
			session.putValue("SITE_NAME", parameters.get("NAME"));
			session.putValue("SITE_DOMAINNAME", parameters.get("DOMAINNAME"));
			session.putValue("SITE_DESCRIPTION", parameters.get("DESCRIPTION"));
			session.putValue("SITE_CATEGORY", parameters.get("CATEGORY"));
			session.putValue("SITE_LANGUAGE", parameters.get("LANGUAGE"));
			session.putValue("SITE_DOMAIN", parameters.get("DOMAIN"));
			templateSelector = "wait";
		}
	}
	if ("start".equals(action))
	{
		try
		{
			if (cms.isSiteLegal(Integer.parseInt((String) session.getValue("SITE_ID")), (String) session.getValue("SITE_NAME"), (String) session.getValue("SITE_DOMAINNAME"), Integer.parseInt((String) session.getValue("SITE_CATEGORY")), Integer.parseInt((String) session.getValue("SITE_LANGUAGE")), Integer.parseInt((String) session.getValue("SITE_DOMAIN"))))
			{
				cms.updateSite(Integer.parseInt((String) session.getValue("SITE_ID")), (String) session.getValue("SITE_NAME"), (String) session.getValue("SITE_DESCRIPTION"), Integer.parseInt((String) session.getValue("SITE_CATEGORY")), Integer.parseInt((String) session.getValue("SITE_LANGUAGE")), Integer.parseInt((String) session.getValue("SITE_DOMAIN")), (String) session.getValue("SITE_DOMAINNAME"));
			
				templateSelector = "done";
			
				session.removeValue("SITE_NAME");
				session.removeValue("SITE_DOMAINNAME");
				session.removeValue("SITE_DESCRIPTION");
				session.removeValue("SITE_CATEGORY");
				session.removeValue("SITE_LANGUAGE");
				session.removeValue("SITE_DOMAIN");
				session.removeValue("SITE_ID");
			}
			else templateSelector = "erroreditsite";
		}
		catch (CmsException exc)
		{
			xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
			templateSelector = "erroreditsite";
		}
	}
	xmlTemplateDocument.setData("edit_site_name", name);
	xmlTemplateDocument.setData("edit_site_domainname", domainname);
	xmlTemplateDocument.setData("edit_site_description", description);
	
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
/**
 * Insert the method's description here.
 * Creation date: (28-09-2000 13:39:00)
 * @return java.lang.Integer
 * @param cms com.opencms.file.CmsObject
 * @param lang com.opencms.workplace.CmsXmlLanguageFile
 * @param names java.util.Vector
 * @param values java.util.Vector
 * @param parameters java.util.Hashtable
 * @exception com.opencms.core.CmsException The exception description.
 */
public Integer getDomains(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	Vector countries = cms.getAllCountries();
	I_CmsSession session = cms.getRequestContext().getSession(true);
	int country = Integer.parseInt((String) session.getValue("SITE_DOMAIN"));
	int index = 0;

	for (int z = 0; z < countries.size(); z++)
	{
		names.addElement(((CmsCountry) countries.elementAt(z)).getName());
		values.addElement(new String(""+((CmsCountry) countries.elementAt(z)).getCountryId()));
		if (((CmsCountry) countries.elementAt(z)).getCountryId() == country) index=z;
	}
	return new Integer(index);
}
/**
 * Insert the method's description here.
 * Creation date: (28-09-2000 13:37:49)
 * @return java.lang.Integer
 * @param cms com.opencms.file.CmsObject
 * @param lang com.opencms.workplace.CmsXmlLanguageFile
 * @param names java.util.Vector
 * @param values java.util.Vector
 * @param parameters java.util.Hashtable
 * @exception com.opencms.core.CmsException The exception description.
 */
public Integer getLanguages(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws com.opencms.core.CmsException
{
	Vector languages = cms.getAllLanguages();
	I_CmsSession session = cms.getRequestContext().getSession(true);
	int language = Integer.parseInt((String) session.getValue("SITE_LANGUAGE"));
	int index = 0;
	
	for (int z = 0; z < languages.size(); z++)
	{
		names.addElement(((CmsLanguage) languages.elementAt(z)).getName());
		values.addElement(new String("" + ((CmsLanguage) languages.elementAt(z)).getLanguageId()));
		if (((CmsLanguage) languages.elementAt(z)).getLanguageId() == language) index = z;
	}
	return new Integer(index);
}
/**
 * Insert the method's description here.
 * Creation date: (28-09-2000 13:36:14)
 * @return boolean
 * @param cms com.opencms.file.CmsObject
 * @param templateFile java.lang.String
 * @param elementName java.lang.String
 * @param parameters java.util.Hashtable
 * @param templateSelector java.lang.String
 */
public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
{
	return false;
}
}

package com.opencms.workplace;

/**
 * Insert the type's description here.
 * Creation date: (03-10-2000 14:18:34)
 * @author: Administrator
 */
 
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;

public class CmsAdminSiteProperty extends CmsWorkplaceDefault implements com.opencms.core.I_CmsConstants {
/**
 * Insert the method's description here.
 * Creation date: (03-10-2000 14:19:45)
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
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

		CmsSite site = cms.getSiteBySiteId(Integer.parseInt((String) parameters.get("siteid")));
		String name = site.getName();
		String description = site.getDescription();
		String domainname = "";
		
		Vector siteUrls = cms.getSiteUrls(site.getId());
		for (int i = 0; i < siteUrls.size(); i++)
			if (((CmsSiteUrls) siteUrls.elementAt(i)).getUrlId() == ((CmsSiteUrls) siteUrls.elementAt(i)).getPrimaryUrl())
				domainname = ((CmsSiteUrls) siteUrls.elementAt(0)).getUrl();
				
		String category = cms.getCategory(site.getCategoryId()).getName();
		String language = cms.getLanguage(site.getLanguageId()).getName();
		String domain = cms.getCountry(site.getCountryId()).getName();
			
		CmsProject project = cms.readProject(site.getOnlineProjectId());
		String projectmanager = cms.readManagerGroup(project).getName();
		String projectworker = cms.readGroup(project).getName();

		xmlTemplateDocument.setData("name", name);
		xmlTemplateDocument.setData("domainname", domainname);
		xmlTemplateDocument.setData("description", description);
		xmlTemplateDocument.setData("language", language);
		xmlTemplateDocument.setData("category", category);
		xmlTemplateDocument.setData("domain", domain);
		xmlTemplateDocument.setData("projectmanager", projectmanager);
		xmlTemplateDocument.setData("projectworker", projectworker);
		
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
/**
 * Insert the method's description here.
 * Creation date: (03-10-2000 14:19:30)
 * @return boolean
 * @param cms com.opencms.file.CmsObject
 * @param templateFile java.lang.String
 * @param elementName java.lang.String
 * @param parameters java.util.Hashtable
 * @param templateSelector java.lang.String
 */
public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
	return false;
}
}

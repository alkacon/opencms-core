package com.opencms.modules.systeminformation;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import javax.servlet.http.*;
import java.util.*;
/**
 * Insert the type's description here.
 * Creation date: (25.08.00 09:47:48)
 * @author: Michael Emmerich
 */
public class CmsSystemInfo  extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsWpConstants{

	/**
	* Formats an ooutput in the system info box
	* @param in The original input string
	* @returns A formatted output string
	*/
	private String formatOutput(String in) {
		String out="";
		while (in.length()>40) {
			out=out+in.substring(0,40);
			out=out+"<br>";
			in=in.substring(40);
		}
		out=out+in;
		return out;
	}
	/**
	 * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
	 * Gets the content of the rename template and processed the data input.
	 * @param cms The CmsObject.
	 * @param templateFile The lock template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearre containgine the processed data of the template.
	 * @exception Throws CmsException if something goes wrong.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
							 Hashtable parameters, String templateSelector)
		throws CmsException {
			
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);

	    HttpServletRequest req=(HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
	    I_CmsSession session=cms.getRequestContext().getSession(false);
		
		// version
		xmlTemplateDocument.setData("version",C_VERSION);
		// memory
		xmlTemplateDocument.setData("avilmem",getMem(1));
		xmlTemplateDocument.setData("freemem",getMem(2));
		xmlTemplateDocument.setData("usedmem",getMem(3));

		// session values
		if (session != null) {
			String[] valueNames=session.getValueNames();
			String sessionvalues="";
			for (int i=0;i<valueNames.length;i++) {
				
				xmlTemplateDocument.setData("key",valueNames[i]);
				xmlTemplateDocument.setData("value",formatOutput(session.getValue(valueNames[i]).toString()));
				sessionvalues=sessionvalues+xmlTemplateDocument.getProcessedDataValue("row");
			}
			xmlTemplateDocument.setData("session",sessionvalues);		
		} else {
			xmlTemplateDocument.setData("session","");				
		}

	
		// caching infos
		Hashtable cacheinfo=cms.getCacheInfo();
		Enumeration keys=cacheinfo.keys();

		
		String cachevalues="";
		while (keys.hasMoreElements()) {
	 		String key= (String)keys.nextElement();
	 
	 		xmlTemplateDocument.setData("key",key);
			xmlTemplateDocument.setData("value",cacheinfo.get(key).toString());
			cachevalues=cachevalues+xmlTemplateDocument.getProcessedDataValue("row");
		}
		xmlTemplateDocument.setData("cache",cachevalues);		
	

		// server infos
		xmlTemplateDocument.setData("server",req.getServerName()+":"+req.getServerPort());
		xmlTemplateDocument.setData("servlet",req.getServletPath());
	
		// process the selected template 
		return startProcessing(cms,xmlTemplateDocument,"",parameters,templateSelector);
	
	}
public String getMem(int mode)	{
	String value="";
	long total=Runtime.getRuntime().totalMemory()/1024;
	long free=Runtime.getRuntime().freeMemory()/1024;
	long used=total-free;

	if(mode ==1) {
		value=""+total;
	} else if (mode==2) {
		value=""+free;
	} else {
		value=""+used;
	}
	
	return value; 
}
}

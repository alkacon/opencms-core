package com.opencms.modules.homepage.news;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.defaults.*;

import java.util.*;
import javax.servlet.http.*;

/**
 * This class controls the template that inserts the selectbox for the channels.
 * This is done within one separate class because this class has to extend the class
 * CmsXmlFormTemplate!
 */
public class Selectbox extends CmsXmlFormTemplate {

	public Integer my_SelectorMethod(CmsObject cms, Vector values, Vector names, Hashtable parameters)
	throws CmsException {
		int returnValue = 0;
		String temp = null;
		CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
		// get the slected value from the session
		String checkboxValue = (String) session.getValue("checkSelectChannel");
		// is this the rigth place to remove the value?
		session.removeValue("checkSelectChannel");
		if (checkboxValue == null){
				checkboxValue = "";
		}
		Vector channels = NewsChannelContentDefinition.getChannelList();
		for(Enumeration el=channels.elements(); el.hasMoreElements();) {
			temp = ((NewsChannelContentDefinition)el.nextElement()).getName();
			// add values for the checkbox
			values.addElement(temp);
			// add corresponding names for the checkboxvalues
			names.addElement(temp);
		}
		for(int i=0; i<channels.size(); i++) {
			// compare the channel Name in the session with the the names in the vector
			if( checkboxValue.equals(((NewsChannelContentDefinition)channels.elementAt(i)).getName()) )
				returnValue = i;
		}
		return new Integer(returnValue);
	}

	/**
	 * Indicates if the results of this class are cacheable.
	 * <P>
	 * Checks if the templateCache is set and if all subtemplates
	 * are cacheable.
	 *
	 * @param cms CmsObject Object for accessing system resources
	 * @param templateFile Filename of the template file
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.

	public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		return false;
	}*/

}

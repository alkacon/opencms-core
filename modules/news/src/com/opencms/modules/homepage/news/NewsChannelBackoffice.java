package com.opencms.modules.homepage.news;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import com.opencms.defaults.*;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * class that controls the backoffice templates for the channels
 */
public class NewsChannelBackoffice extends A_CmsBackoffice {

	public Class getContentDefinitionClass() {
		return NewsChannelContentDefinition.class;
	}
  /**
   * when clicking directly on one entry in the list ...
	 */
  /*
  public String getUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception{
		return "system/modules/com.opencms.modules.homepage.news/administration/news/index.html";
	}*/

	public String getCreateUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception{
		return "system/modules/com.opencms.modules.homepage.news/administration/channel/EditBackoffice.html";
	}

	public String getEditUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception {
		return "system/modules/com.opencms.modules.homepage.news/administration/channel/EditBackoffice.html";
	}

	public String getBackofficeUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception {
		return "system/modules/com.opencms.modules.homepage.news/administration/channel/Backoffice.html";
	}

	public byte[] getContentEdit(CmsObject cms,
								 CmsXmlWpTemplateFile template,
								 String elementName,
								 Hashtable parameters,
								 String templateSelector)
								 throws CmsException {
		// session will be created or fetched
		I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
		// get value of hidden input field action
		String action = (String) parameters.get("action");
    if (action == null) action = "";
		//get value of id
		String id = (String) parameters.get("id");
		if (id == null) id = "";
		// set existing data in the content definition object
		//& go to the done section of the template
		if (action.equals("save")) {
			//move to the done section in the template
			templateSelector = "done";

			//get data from the template
			int idIntValue2 = Integer.valueOf(id).intValue();
      //read value of the inputfields
			String name = (String) parameters.get("name");
      if(name == null) name = "";
			String descript = (String) parameters.get("descript");
      if(descript == null) descript = "";
			//System.err.println("NewsEditBackoffice: id: " + id + " name: " +name +" descr: "+descript);
			// ensure something was filled in!
			if( name.equals("") || descript.equals("") ) {
					templateSelector = "default";
          template.setData("error", template.getData("missing"));
					template.setData("setaction", "save");   // still not first time shown
          template.setData("name", name);             // in case right data was given somewhere
          template.setData("descript", descript);
          if( name.equals("") )  {
            template.setData("nameHighlightStart", template.getData("redStart"));
            template.setData("nameHighlightEnd", template.getData("redEnd"));
	        }
          if( descript.equals("") ) {
            template.setData("descriptHighlightStart", template.getData("redStart"));
            template.setData("descriptHighlightEnd", template.getData("redEnd"));
          }
					return startProcessing(cms, template, elementName, parameters, templateSelector);
			}
			//change the cd object content
			NewsChannelContentDefinition editCD = new NewsChannelContentDefinition(cms, new  Integer(idIntValue2));
			editCD.setName(name);
			editCD.setDescription(descript);
			try {
				//System.err.println("NewsEditBackoffice: try to update!");
				editCD.write(cms);
			} catch (Exception e) {
				System.err.println("NewsEditBackoffice: couldn´t update!");
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				template.setData("error", e.toString());
				template.setData("name", "");
				template.setData("descript", descript);
				template.setData("setaction", "save");
				templateSelector = "default";
				return startProcessing(cms, template, elementName, parameters, templateSelector);
			}
		}
		//START: first time here and got valid id parameter:
		//		 Fill the template with data!
   if(id != null && !id.equals("")) {
				//get data from cd object
				int idIntValue = Integer.valueOf(id).intValue();
				NewsChannelContentDefinition newsCD = new NewsChannelContentDefinition(cms, new  Integer(idIntValue));
				String name = newsCD.getName();
				String descript = newsCD.getDescription();

				//set the data in the template
				template.setData("name", name);
				template.setData("descript", descript);
        template.setData("setaction", "save");

				//marker for second access of this method
			return startProcessing(cms, template, elementName, parameters, templateSelector);
		}
	//finally start the processing
	return startProcessing(cms, template, elementName, parameters, templateSelector);
}

        /**
         *
         */
	public byte[] getContentNew(CmsObject cms,
								CmsXmlWpTemplateFile template,
								String elementName,
								Hashtable parameters,
								String templateSelector)
								throws CmsException {
		// session will be created or fetched
		I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
		// get value of hidden input field action
		String action = (String) parameters.get("action");

		//get value of id and the marker parameters.get("id");
		String id = (String) parameters.get("id");

		if (id == null) id = "";

		//no button pressed: go to the default section!
		if (action == null || action.equals("")) {
			templateSelector = "default";
			template.setData("setaction", "default");
			//confirmation button pressed, process data!
		} else {
			//create new data, therefore create new content definition instance
			//& go to the done section of the template
			templateSelector = "done";

			//read value of the inputfields
			String name = (String) parameters.get("name");
      if(name == null) name = "";
			String descript = (String) parameters.get("descript");
      if(descript == null) descript = "";
			// System.err.println("NewsEditBackoffice: name: " +name +" descr: "+descript);
			// ensure something was filled in!
			if( name.equals("") || descript.equals("") ) {
					templateSelector = "default";
          template.setData("error", template.getData("missing"));
					template.setData("setaction", "default");   // still not first time shown
          template.setData("name", name);             // in case right data was given somewhere
          template.setData("descript", descript);
          if( name.equals("") )  {
            template.setData("nameHighlightStart", template.getData("redStart"));
            template.setData("nameHighlightEnd", template.getData("redEnd"));
	        }
          if( descript.equals("") ) {
            template.setData("descriptHighlightStart", template.getData("redStart"));
            template.setData("descriptHighlightEnd", template.getData("redEnd"));
          }
					return startProcessing(cms, template, elementName, parameters, templateSelector);
			}

			//create new cd object with the fetched values of the inputfields
			NewsChannelContentDefinition newCD = new NewsChannelContentDefinition(name, descript);
			// newCD.setLockstate("lock");
			try {
				newCD.write(cms);
			} catch (Exception e) {
				System.err.println("NewsEditBackoffice: couldn´t write!");
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				template.setData("error", e.toString());
				template.setData("name", "");
				template.setData("descript", descript);
				template.setData("setaction", "default");
				templateSelector = "default";
			}
		}

		//finally start the processing
		return startProcessing(cms, template, elementName, parameters, templateSelector);
	}
}

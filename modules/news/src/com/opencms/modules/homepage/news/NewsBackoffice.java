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
import java.text.*;

/**
 * The class that controls the backoffice templates.
 */
public class NewsBackoffice extends A_CmsBackoffice {
	/**
	 */
	public Class getContentDefinitionClass() {
		return NewsContentDefinition.class;
	}

  /**
   * when clicking directly on one entry in the list ...
	 */
   /*
	public String getUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception{
		return "system/modules/com.opencms.modules.homepage.news/administration/news/EditBackoffice.html";
	}*/

	/**
	 */
	public String getCreateUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception{
		return "system/modules/com.opencms.modules.homepage.news/administration/news/EditBackoffice.html";
	}

	public String getBackofficeUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception {
		return "system/modules/com.opencms.modules.homepage.news/administration/news/Backoffice.html";
	}

	public String getEditUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
	throws Exception {
		return "system/modules/com.opencms.modules.homepage.news/administration/news/EditBackoffice.html";
	}

	public byte[] getContentEdit(CmsObject cms,
								 CmsXmlWpTemplateFile template,
								 String elementName,
								 Hashtable parameters,
								 String templateSelector)
								 throws CmsException {
		boolean dateError = false;

    // System.err.println("--- Edit -"+template.toString());

    String error = "";
		GregorianCalendar actDate = new GregorianCalendar();
		// session will be created or fetched
		I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
		// get value of hidden input field action
		String action = (String) parameters.get("action");

		//get value of id
		String id = (String) parameters.get("id");
		if (id == null) id = "";
		String idsave = "";
		if ((id == null) || (id == "")) {
			idsave = (String) session.getValue("idsave");
			if (idsave == null) idsave = "";
		}
		//System.err.println("getContentEdit:     id= "+id);
		//System.err.println("getContentEdit: idsave= "+idsave);
		// set existing data in the content definition object
		//& go to the done section of the template
		if ((idsave != "") && (id == "") && (idsave.compareTo("new") != 0) && (id.compareTo("new") != 0)) {
			//move to the done section in the template
			templateSelector = "done";

			//get data from the template
			int idIntValue2 = Integer.valueOf(idsave).intValue();
			String headline = (String) parameters.get("headline");
			String description = (String) parameters.get("description");
			String text = (String) parameters.get("text");
			String author = (String) parameters.get("author");
			String link = (String) parameters.get("link");
			String linkText = (String) parameters.get("linkText");
			String sDate = (String) parameters.get("date");
			String channelName = (String)parameters.get("channel");
			String a_info1 = (String)parameters.get("a_info1");
			String a_info2 = (String)parameters.get("a_info2");
			String a_info3 = (String)parameters.get("a_info3");
			// create an Object to access the Id
			NewsChannelContentDefinition temp = new NewsChannelContentDefinition(channelName);
			int channelId = temp.getIntId();
			GregorianCalendar date = null;
			headline.trim();
			try{
				date = NewsContentDefinition.string2date(sDate);
			}catch(ParseException e) {
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				dateError = true;
			}
			NewsContentDefinition editCD = new NewsContentDefinition(cms, new  Integer(idIntValue2));
			// ensure something was filled in!
			if((headline.equals("") || headline == null || dateError == true)) {
					templateSelector = "default";
					template.setData("headline", headline);
					template.setData("descript", description);
					template.setData("text", text);
					template.setData("link", link);
					template.setData("linkText", linkText);
					template.setData("author", author);
					template.setData("date", NewsContentDefinition.date2string(actDate));
					template.setData("a_info1", a_info1);
					template.setData("a_info2", a_info2);
					template.setData("a_info3", a_info3);
					// re-display data in selectbox
				    session.putValue("checkSelectChannel", editCD.getChannel());
					if( headline.equals("") || headline == null) {
            error += template.getProcessedDataValue("missing", this, parameters) + "<br>";
						template.setData("error", error);
            template.setData("headlineHighlightStart", template.getData("redStart"));
            template.setData("headlineHighlightEnd", template.getData("redEnd"));
					}
					if(dateError == true) {
            error += template.getProcessedDataValue("dateError", this, parameters) +"<br>";
            template.setData("dateHighlightStart", template.getData("redStart"));
            template.setData("dateHighlightEnd", template.getData("redEnd"));
						template.setData("error", error);
					}
					session.putValue("idsave", ""+idIntValue2);  // This is somehow important!
					template.setData("setaction", "default");   // still not first time shown
					return startProcessing(cms, template, elementName, parameters, templateSelector);
			}
			//change the cd object content
			editCD.setHeadline(headline);
			editCD.setDescription(description);
			editCD.setText(text);
			editCD.setAuthor(author);
			editCD.setLink(link);
			editCD.setLink(linkText);
			editCD.setDate(date);
			editCD.setChannel(channelId);
			editCD.setA_info1(a_info1);
			editCD.setA_info2(a_info2);
			editCD.setA_info3(a_info3);
			try {
				editCD.write(cms);
			} catch (Exception e) {
				System.err.println("NewsEditBackoffice: couldn´t update!");
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				template.setData("error", e.toString());
				template.setData("headline", headline);
				template.setData("descript", description);
				template.setData("text", text);
				template.setData("link", link);
				template.setData("linkText", linkText);
				template.setData("author", author);
				template.setData("date", NewsContentDefinition.date2string(actDate));
				template.setData("a_info1", a_info1);
				template.setData("a_info2", a_info2);
				template.setData("a_info3", a_info3);
				session.putValue("idsave", ""+idsave);
				template.setData("setaction", "default");
				templateSelector = "default";
				return startProcessing(cms, template, elementName, parameters, templateSelector);
			}
			// remove the marker
			session.removeValue("idsave");
		}
		//START: first time here and got valid id parameter:
		//		 Fill the template with data!
		if (idsave == "") {
			if (id != "") {
				//get data from cd object
				int idIntValue = Integer.valueOf(id).intValue();
				NewsContentDefinition newsCD = new NewsContentDefinition(cms, new  Integer(idIntValue));

				//set the data from the CD in the template
				template.setData("headline", newsCD.getHeadline());
				template.setData("descript", newsCD.getDescription());
				template.setData("text", newsCD.getText());
				template.setData("author", newsCD.getAuthor());
				template.setData("link", newsCD.getLink());
				template.setData("linkText", newsCD.getLinkText());
				template.setData("date", newsCD.getDate());
				template.setData("a_info1", newsCD.getA_info1());
				template.setData("a_info2", newsCD.getA_info2());
				template.setData("a_info3", newsCD.getA_info3());
				// re-display data in selectbox
				session.putValue("checkSelectChannel", newsCD.getChannel());
				//marker for second access of this method
				session.putValue("idsave", id);
			return startProcessing(cms, template, elementName, parameters, templateSelector);
		}
	}
	//finally start the processing
	return startProcessing(cms, template, elementName, parameters, templateSelector);
	}

	public byte[] getContentNew(CmsObject cms,
								CmsXmlWpTemplateFile template,
								String elementName,
								Hashtable parameters,
								String templateSelector)
								throws CmsException {
		// session will be created or fetched

     System.err.println("---NEW -"+template.toString());

		I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
		// get value of hidden input field action
		String action = (String) parameters.get("action");
		GregorianCalendar actDate = new GregorianCalendar();  // the actual Date
		boolean dateError = false;   // could the date-string be parsed?
		boolean channelError = false;
		int channelId = 0;
    String error = "";

		//get value of id and the marker idsaveng) parameters.get("id");
		String id = (String) parameters.get("id");

		if (id == null) id = "";
		String idsave = "";
		if ((id == null) || (id == "")) {
			idsave = (String) session.getValue("idsave");
			if (idsave == null) idsave = "";
		}

		//no button pressed: go to the default section!
		if (action == null || action.equals("")) {
			templateSelector = "default";
			template.setData("setaction", "default");
			template.setData("date", NewsContentDefinition.date2string(actDate)); // set actual Date
			template.setData("link", "http://");
			//store marker in the session
			if (idsave == "")
				session.putValue("idsave", "new");
			//confirmation button pressed, process data!
		} else {
			//create new data, therefore create new content definition instance
			//& go to the done section of the template
			templateSelector = "done";
			session.removeValue("idsave");

			//read value of the inputfields
			String headline = (String) parameters.get("headline");
			String description = (String) parameters.get("description");
			String text = (String) parameters.get("text");
			String author = (String) parameters.get("author");
			String link = (String) parameters.get("link");
			String linkText = (String) parameters.get("linkText");
			String sDate = (String) parameters.get("date");
			String channelName = (String)parameters.get("channel");
			String a_info1 = (String)parameters.get("a_info1");
			String a_info2 = (String)parameters.get("a_info2");
			String a_info3 = (String)parameters.get("a_info3");
			if(channelName == null || channelName.equals(" ") || channelName.equals("") ) {
				channelError = true;
			}else {
				// create an ChannelObject to access the Id
				NewsChannelContentDefinition temp = new NewsChannelContentDefinition(channelName);
				channelId = temp.getIntId();
			}
			GregorianCalendar date = null;
			headline.trim();
			try{
				date = NewsContentDefinition.string2date(sDate);
			}catch(ParseException e) {
				System.err.println("getContentNew: ParseException");
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				dateError = true;
			}
			// ensure something was filled in!
			if(headline.equals("") || headline == null || dateError == true || channelError == true) {
					templateSelector = "default";
					template.setData("headline", headline);
					template.setData("descript", description);
					template.setData("text", text);
					template.setData("link", link);
					template.setData("linkText", linkText);
					template.setData("author", author);
					template.setData("date", NewsContentDefinition.date2string(actDate));
					template.setData("a_info1", a_info1);
					template.setData("a_info2", a_info2);
					template.setData("a_info3", a_info3);
					// re-display data in selectbox
          session.putValue("checkSelectChannel", ""+channelId);
					if( headline.equals("") || headline == null) {
            error += template.getProcessedDataValue("missing", this, parameters) + "<br>";
						template.setData("error", error);
            template.setData("headlineHighlightStart", template.getData("redStart"));
            template.setData("headlineHighlightEnd", template.getData("redEnd"));
					}
					if(dateError == true) {
            error += template.getProcessedDataValue("dateError", this, parameters) +"<br>";
            template.setData("dateHighlightStart", template.getData("redStart"));
            template.setData("dateHighlightEnd", template.getData("redEnd"));
						template.setData("error", error);
					}
					if(channelError == true) {
            error += template.getProcessedDataValue("channelError", this, parameters) +"<br>";
            template.setData("channelHighlightStart", template.getData("redStart"));
            template.setData("channelHighlightEnd", template.getData("redEnd"));
						template.setData("error", error);
					}
					session.putValue("idsave", "new");  // This is somehow important!
					template.setData("setaction", "default");   // still not first time shown
					return startProcessing(cms, template, elementName, parameters, templateSelector);
			}

			//create new cd object with the fetched values of the inputfields
			NewsContentDefinition newCD = new NewsContentDefinition(headline,description, text, author, link, linkText, date, channelId, a_info1, a_info2, a_info3);
			//newCD.setLockstate("lock");
			try {
				newCD.write(cms);
			} catch (Exception e) {
				System.err.println("NewsEditBackoffice: couldn´t write!");
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
				template.setData("error", e.toString());
				template.setData("headline", "headline");
				template.setData("descript", description);
				template.setData("a_info1", a_info1);
				template.setData("a_info2", a_info2);
				template.setData("a_info3", a_info3);
				session.putValue("idsave", "new");
				template.setData("setaction", "default");
				templateSelector = "default";
			}
		}

		//finally start the processing
		return startProcessing(cms, template, elementName, parameters, templateSelector);
	}
}

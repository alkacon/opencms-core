package com.opencms.modules.homepage.news;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;

/**
 * class to generate the output for the templates that display the news.
 * This class privides the methods, that can be called from the templates.
 */
public class NewsTemplate extends CmsXmlTemplate {

  private static int NAME = 1;
  private static int ID = 2;

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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG ) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        if(templateSelector == null || "".equals(templateSelector)) {
            templateSelector = (String)parameters.get(C_FRAME_SELECTOR);
        }
        parameters.put("_ELEMENT_", elementName);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

	/**
	 * method that should be called from a template File to get a list of news that
	 * belong to one channel.
	 * The number of entries that should be displayed have to be
	 * passed as a parameter.
	 * The method returns all entries if no valid parameter is passed.
	 * @return the news list as a string
	 */
	public Object getNewsList(CmsObject cms,
						  String tagcontent, // the parameter from the method
						  A_CmsXmlContent doc,
						  Object userObject)
	throws CmsException {
		Hashtable parameters = (Hashtable)userObject;
		Enumeration keys = parameters.keys();
                int select = -1;
                int n = -1;
                int id = -1;
                StringBuffer list = new StringBuffer();
		String ret = "";                // the return value
		Vector newsList = null;         // stores the list of CD'S
		NewsChannelContentDefinition myChannel = null;
    CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;
		/*while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			System.err.println(key + " -> " + parameters.get(key));
		}*/
		// get the parameter with is assigned to the element
		String elementName = (String)parameters.get("_ELEMENT_");
		String channelName = (String)parameters.get(elementName+".channelname");
    String channelId = (String)parameters.get(elementName+".channelid");
    // check what has been specified....
     if(channelId != null && channelName == null) {
        select = ID;
     }else if(channelId == null && channelName != null) {
        select = NAME;
     }else if(channelId != null && channelName != null) {
        select = ID;
     }else{ // no parameter selected!
        System.err.println("[NewsTemplate.getNewsList] No channel selcted!");
      return "";   // step out here, need valid channel!
     }
     if(select == ID) {
     // parse the id
        try{
          id = Integer.parseInt(channelId);
        }
        catch(NumberFormatException e) {
          System.err.println("[NewsTemplate.getNewsList] NumberFormatException! "+ e.getMessage());
          System.err.println("[NewsTemplate.getNewsList] channelId: " + channelId);
			    return "";
        }
      }
      // now try to read the cd ...
		  try{
          // check if the channel exists and get the id...
			if(select==NAME) myChannel = new NewsChannelContentDefinition(channelName);
      else {
        myChannel = new NewsChannelContentDefinition(null, new Integer(id));
      }
		}catch(CmsException e) {
			System.err.println("[NewsTemplate.getNewsList] CmsException! "+ e.getMessage());
			return "";   // step out here, need valid channel!
		}
		if(tagcontent == null || tagcontent.equals("")) {
			// get all entries
			newsList = NewsContentDefinition.getNewsList(new Integer(myChannel.getIntId()), ""+-1);
		}else {
			try{
				n = Integer.parseInt(tagcontent);
				newsList = NewsContentDefinition.getNewsList(new Integer(myChannel.getIntId()), ""+n);	// get a Number of entries
			}catch(NumberFormatException e) {
				newsList = NewsContentDefinition.getNewsList(new Integer(myChannel.getIntId()), ""+-1);
			}
		}
		for(int i = 0; i < newsList.size(); i++) {
			//run through vector of CD´s and and create the String for the template
			template.setData("id", ((NewsContentDefinition)newsList.elementAt(i)).getUniqueId(null));
			template.setData("headline", ((NewsContentDefinition)newsList.elementAt(i)).getHeadline());
			template.setData("description", ((NewsContentDefinition)newsList.elementAt(i)).getDescription());
			template.setData("author", ((NewsContentDefinition)newsList.elementAt(i)).getAuthor());
			template.setData("date", ((NewsContentDefinition)newsList.elementAt(i)).getDate());
			template.setData("link", ((NewsContentDefinition)newsList.elementAt(i)).getLink());
			template.setData("linkText", ((NewsContentDefinition)newsList.elementAt(i)).getLinkText());
			template.setData("text", ((NewsContentDefinition)newsList.elementAt(i)).getText());
			template.setData("channel", ((NewsContentDefinition)newsList.elementAt(i)).getChannel());
			template.setData("a_info1", ((NewsContentDefinition)newsList.elementAt(i)).getA_info1());
			template.setData("a_info2", ((NewsContentDefinition)newsList.elementAt(i)).getA_info2());
			template.setData("a_info3", ((NewsContentDefinition)newsList.elementAt(i)).getA_info3());
			// "this" and "userObject" have to be passed here altough this is not said in the documentation!!!
			String temp = template.getProcessedDataValue("newsentry", this, userObject);
			list.append(temp);
		}
		ret = list.toString();
		return ret;
	}

	/**
	 * method that should be called from a template File to get a one news entry.
	 * The id of the element has to passed with the URL (?id=).
	 * The method returns an ErrorText if a invalid id is passed.
	 * @return the news entry as a string
	 */
	public Object getSingleNews(CmsObject cms,
								String tagcontent, // the parameter from the method
								A_CmsXmlContent doc,
						        Object userObject)
	throws CmsException {
		Hashtable parameters = (Hashtable)userObject;
		CmsXmlTemplateFile template = (CmsXmlTemplateFile)doc;
		String uId = (String)parameters.get("id");  // get the id of the news entry
		String ret = "";
		int id = -1;
		NewsContentDefinition myNewsCD = null;
		if( !(uId==null) && !uId.equals("") ) {
			try{
				id = Integer.parseInt(uId);
				//System.err.println("id: "+id);
				myNewsCD = new NewsContentDefinition(null, new Integer(id));
			}catch(NumberFormatException e) {
				System.err.println("[NewsTemplate.getSingleNews()] NumberFormatException " + e.getMessage());
			}catch(CmsException e) {
				System.err.println("[NewsTemplate.getSingleNews()] CmsException " + e.getMessage());
			}
		}
		if(myNewsCD != null && (myNewsCD.getIntId() != -1)) {
			// id == -1 means no valid id was requested!
			// fill the template with data
			template.setData("id", myNewsCD.getUniqueId(null));
			template.setData("headline", myNewsCD.getHeadline());
			template.setData("description", myNewsCD.getDescription());
			template.setData("author",myNewsCD.getAuthor());
			template.setData("date", myNewsCD.getDate());
			template.setData("link", myNewsCD.getLink());
			template.setData("linkText", myNewsCD.getLinkText());
			template.setData("text", myNewsCD.getText());
			template.setData("channel", myNewsCD.getChannel());
			template.setData("a_info1", myNewsCD.getA_info1());
			template.setData("a_info2", myNewsCD.getA_info2());
			template.setData("a_info3", myNewsCD.getA_info3());
			ret = template.getProcessedDataValue("newsentry", this, userObject);
		} else {
			// The requested element did not exist!
			if(template.hasData("errorText")) {
				// datablock with errortext is defined in the template
				ret= template.getDataValue("errorText");
			}else {
				ret= "Element does not exist!";
			}
		}
		return ret;
	}
	/**
	 * Gets a valid URI especially for use in XSL-templates to produce PDF files.
	 * <P>
	 * Called by the template file using
	 * <code>&lt;METHOD name="getValidURI"/&gt;</code>.
	 *
	 * @param cms A_CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object the initiating XML document.
	 * @param userObj Hashtable with parameters.
	 * @return Valid URI.
	 */
	public String getValidURI(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
			throws CmsException {

		Hashtable parameters = (Hashtable)userObject;
		String uId = (String)parameters.get("id");  // get the id of the news entry
		String ret = "";
		int id = -1;
		NewsContentDefinition myNewsCD = null;
		// try to read the element...
		if( !(uId==null) && !uId.equals("") ) {
			try{
				id = Integer.parseInt(uId);
				//System.err.println("id: "+id);
				myNewsCD = new NewsContentDefinition(null, new Integer(id));
			}catch(NumberFormatException e) {
				System.err.println("[NewsTemplate.getValidURI()] NumberFormatException " + e.getMessage());
			}catch(CmsException e) {
				System.err.println("[NewsTemplate.getValidURI()] CmsException " + e.getMessage());
			}
		}
		if(myNewsCD != null && (myNewsCD.getIntId() != -1)) {
			String tmp =  myNewsCD.getLink();
			if(tmp == null || tmp.equals("")) {
				// link is empty, so fill something in...
				ret = "#";
			}else{
				// link was set
				ret = myNewsCD.getLink();
			}
		} else {
			ret = "";
		}
		return ret;
	}

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {

        // First build our own cache directives.
        CmsCacheDirectives result = new CmsCacheDirectives(true);
        Vector para = new Vector();
        para.add("id");
        result.setCacheParameters(para);
        return result;
    }

}

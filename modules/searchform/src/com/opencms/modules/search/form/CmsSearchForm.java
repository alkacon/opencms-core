package com.opencms.modules.search.form;

/**
 * Creation date: (25.10.00 17:45:54)
 * @author: Markus Fabritius
 **/

import java.io.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.net.*;
import java.util.*;
import java.lang.reflect.*;


public class CmsSearchForm extends com.opencms.defaults.CmsXmlFormTemplate {

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
 * Creation date: (17.11.00 10:23:25)
 */

public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	// Variable declaration and initialistion
	int currentSite = 1, matchPerPage = 10;
	int counter = 1, site = 0, pageset = 0;
	int compare;
	boolean noSingleRestrict = false, errorContentDef = false;
	String contentDefName = "", areaRestrict = "", singleRestrict = "", localRestrict = "";
	String configuration, noContentDef, noServer, noMatch, noWord, syntaxError, buildQuery;
	String list = "", navigate = "", buildquery = "";
	String server = cms.getRequestContext().getRequest().getServletUrl();
	String uri = cms.getRequestContext().getUri();
	Vector getResult = new Vector();
	Object cd = null;

	// get exist session or create a new session if no session exist
	I_CmsSession session = cms.getRequestContext().getSession(true);
	// get template
	CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	// get session parameter
	String sessionCheck = (String) parameters.get("newsearch");
	// check if the site is use the first time and delete the session variable
	if (sessionCheck == null) {
		session.removeValue("searchengineSort");
		session.removeValue("searchengineMethod");
		session.removeValue("searchengineFormat");
		session.removeValue("searchengineName");
		session.removeValue("searchengineRestrict");
		sessionCheck = "";
	}
	// Try to get data value from the template
	try {
		contentDefName = (String) templateDocument.getDataValue("contentDefinition");
		parameters.put("searchengineContentDefinition", contentDefName);
	} catch (CmsException e) {
		contentDefName = "";
	}
	try {
		configuration = (String) templateDocument.getDataValue("configurationFile");
	} catch (CmsException e) {
		configuration = "contentDefinitionSearchengineConfiguration";
	}
	try {
		matchPerPage = Integer.parseInt(templateDocument.getDataValue("matchesperpage"));
		if (matchPerPage<1)
			matchPerPage=10;
	} catch (CmsException e) {
		matchPerPage = 10;
	} catch (NumberFormatException e) {
		matchPerPage = 10;
	}
	try {
		singleRestrict = (String) templateDocument.getDataValue("singleRestriction");
	} catch (CmsException e) {
		noSingleRestrict = true;
	}
	try {
		areaRestrict = (String) templateDocument.getDataValue("areaRestrict");
		parameters.put("searchengineAreaRestrict", areaRestrict);
	} catch (CmsException e) {
		areaRestrict = "";
	}
	try {
		noContentDef = (String) templateDocument.getDataValue("cdNotFound");
	} catch (CmsException e) {
		noContentDef = "Content Definition not Found!";
	}
	try {
		noServer = (String) templateDocument.getDataValue("serverNotFound");
	} catch (CmsException e) {
		noServer = "Serverpath not found!";
	}
	try {
		noMatch = (String) templateDocument.getDataValue("noMatch");
	} catch (CmsException e) {
		noMatch = "No match!";
	}
	try {
		noWord = (String) templateDocument.getDataValue("noWord");
	} catch (CmsException e) {
		noWord = "Please insert a search word!";
	}
	try {
		syntaxError = (String) templateDocument.getDataValue("syntax");
	} catch (CmsException e) {
		syntaxError = "Syntax error";
	}
	// Get button parameter for changing the templates
	String page = (String) parameters.get("page");
	String action = (String) parameters.get("action");
	try {
		// check if content definition and the serverpath in the registry exist
		Class c = Class.forName(contentDefName);
		String serverpath = cms.getRegistry().getModuleParameter(contentDefName.substring(0, contentDefName.lastIndexOf(".")), "Serverpath");
		if (serverpath == null) {
			templateDocument.setData("message", noServer);
		} else {
			// Content definition and serverpath exist go on
			// Check if button of the formular is press or nota
			if ((action == null || action.equals("")) && (page == null || page.equals(""))) {
				templateSelector = "default";
				templateDocument.setData("setaction", "default");
				if (sessionCheck.equals("start")) {
					templateDocument.setData("lastname", (String) session.getValue("searchengineName"));
				}
			} else {
				// a button was pressed	go on
				// get the parameter from the request
				String word = (String) parameters.get("words");
				String method = (String) parameters.get("method");
				String format = (String) parameters.get("format");
				String sort = (String) parameters.get("sort");
				// if the parameter does not exit set default value
				if (method == null)
					method = "and";
				if (format == null)
					format = "long";
				if (sort == null)
					sort = "score";
				// check which kind of restriction is set, the priority of singleRestrict is higher than areaRestrict
				if (noSingleRestrict == true || singleRestrict == null || singleRestrict.equals("")) {
					if (areaRestrict.equals("") || areaRestrict == null)
						localRestrict = "restricttoall";
					else {
						localRestrict = (String) parameters.get("restrict");
					}
					if (localRestrict == null)
						localRestrict = "restricttoall";
				} else {
					localRestrict = singleRestrict;
				}
				try {
					// error request for search word
					if (word == null || word.equals("")) {
						templateDocument.setData("message", noWord);
						templateDocument.setData("setaction", "default");
						templateSelector = "default";
					} else {
						// write session
						session.putValue("searchengineSort", sort);
						session.putValue("searchengineMethod", method);
						session.putValue("searchengineFormat", format);
						session.putValue("searchengineName", word);
						session.putValue("searchengineRestrict", localRestrict);
						// required for refelction
						Integer setMatch = new Integer(matchPerPage);
						// start reflection with the classname from the template
						Method m = c.getMethod("read", new Class[] {String.class, String.class, String.class, String.class, String.class, String.class, String.class, Integer.class, CmsObject.class});
						getResult = (Vector) m.invoke(null, new Object[] {word, method, sort, page, configuration, localRestrict, serverpath, setMatch, cms});
						// error request for a hit, nomatch or syntax error
						if (getResult.size() <= 1) {
							if (getResult.elementAt(0).equals("syntax")) {
								templateDocument.setData("message", syntaxError);
							} else {
								templateDocument.setData("message", noMatch);
							}
							templateDocument.setData("lastname", (String) session.getValue("searchengineName"));
							templateDocument.setData("setaction", "default");
							templateSelector = "default";
						} else {
							// output of the result
							templateSelector = "Result";
							// get sequential numbering
							cd = c.newInstance();
							cd = (I_CmsSearchEngine) getResult.elementAt(0);
							pageset = ((I_CmsSearchEngine) cd).getPages();
							if (page != null) {
								compare = Integer.parseInt(page);
								for (int i = 1; i <= pageset; i++) {
									if (compare == i)
										counter = ((i * matchPerPage) - (matchPerPage - 1));
								}
							}
							// fill template head
							for (int i = 0; i < getResult.size(); i++) {
								cd = c.newInstance();
								cd = (I_CmsSearchEngine) getResult.elementAt(i);
								if (i == 0) {
									templateDocument.setData("searchword", ((I_CmsSearchEngine) cd).getSearchWord());
									templateDocument.setData("first", String.valueOf(((I_CmsSearchEngine) cd).getFirstDisplay()));
									templateDocument.setData("last", String.valueOf(((I_CmsSearchEngine) cd).getLastDisplay()));
									templateDocument.setData("match", String.valueOf(((I_CmsSearchEngine) cd).getMatch()));
									templateDocument.setData("pages", String.valueOf(((I_CmsSearchEngine) cd).getPages()));
									site = ((I_CmsSearchEngine) cd).getPages();
								} else {
									// check for format ( long ) fill template body
									if (format.equals("long") || (format == null)) {
										templateDocument.setData("number", counter + "");
										templateDocument.setData("url", ((I_CmsSearchEngine) cd).getUrl());
										templateDocument.setData("title", ((I_CmsSearchEngine) cd).getTitle());
										templateDocument.setData("percent", String.valueOf(((I_CmsSearchEngine) cd).getPercentMatch()));
										templateDocument.setData("excerpt", ((I_CmsSearchEngine) cd).getExcerpt());
										templateDocument.setData("size", String.valueOf(((I_CmsSearchEngine) cd).getSize()));
										String longrow = templateDocument.getProcessedDataValue("longrow");
										list += longrow;
										// check for format ( short ) fill template body
									} else {
										templateDocument.setData("number", counter + "");
										templateDocument.setData("url", ((I_CmsSearchEngine) cd).getUrl());
										templateDocument.setData("title", ((I_CmsSearchEngine) cd).getTitle());
										templateDocument.setData("percent", String.valueOf(((I_CmsSearchEngine) cd).getPercentMatch()));
										String shortrow = templateDocument.getProcessedDataValue("shortrow");
										list += shortrow;
									}
									counter++;
								}
							}
						}
						templateDocument.setData("resultlist", list);
						// if the first site of result is reqiured for the navigation
						if (page == null) {
							page = "1";
						}
						for (int i = 1; i <= site; i++) {
							if (Integer.parseInt(page) == i) {
								templateDocument.setData("currentpage", String.valueOf(i));
								String currentnav = templateDocument.getProcessedDataValue("currentnav");
								navigate += currentnav;
							} else {
								buildquery = setQuery(localRestrict, word, method, format, sort, String.valueOf(i));
								templateDocument.setData("server", server + uri);
								templateDocument.setData("query", buildquery);
								templateDocument.setData("numberurl", String.valueOf(i));
								String nav = templateDocument.getProcessedDataValue("nav");
								navigate += nav;
							}
						}
					}
					templateDocument.setData("navigation", navigate);
				} catch (ClassCastException e) {
					System.err.println(e.toString());
				} catch (InstantiationException e) {
					System.err.println(e.toString());
				} catch (IllegalAccessException e) {
					System.err.println(e.toString());
				} catch (InvocationTargetException e) {
					e.printStackTrace(System.err);
					templateDocument.setData("message", noServer);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					System.err.println(e.toString());
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			}
		}
	} catch (ClassNotFoundException e) {
		System.err.println("Fehler liegt hier!");
		templateDocument.setData("message", noContentDef);
	}
	return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
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
     return new CmsCacheDirectives(false);
}

/**
 * Used for dynamically generating the selectbox for selecting
 * the format method of the search engine (for example long, short).
 * This method use reflection to get dynamically the value for the selectbox
 * from the Content Definition which is used.
 *
 * Called while generating the template content from CmsXmlFormTemplateFile.
 * @param cms A_CmsObject for accessing system resources.
 * @param names Vector that will be filled with radio button descriptions.
 * @param values Vector that will be filled with radio buttom values.
 * @param parameters Hashtable with all user parameters.
 * @return Index of the currently checked radio button
 * @see CmsXmlFormTemplateFile
 * Creation date: (27.11.00 11:06:11)
 */

public Integer selectFormat(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
	// session for presetting
	I_CmsSession session = cms.getRequestContext().getSession(true);
	Vector result = new Vector();
	String formatlist = (String) session.getValue("searchengineFormat");
	if (formatlist == null) {
		formatlist = "";
	}
	try {
		Class c = Class.forName((String) parameters.get("searchengineContentDefinition"));
		Method m = c.getMethod("setParameter", new Class[] {String.class, CmsObject.class});
		result = (Vector) m.invoke(null, new Object[] {"format", cms});
	} catch (ClassNotFoundException e) {
		System.err.println(e.toString());
	} catch (ClassCastException e) {
		System.err.println(e.toString());
	} catch (IllegalAccessException e) {
		System.err.println(e.toString());
	} catch (InvocationTargetException e) {
		System.err.println(e.toString());
	} catch (NoSuchMethodException e) {
		System.err.println(e.toString());
	} catch (Exception e) {
		System.err.println(e.toString());
	}
	int index = 0;
	for (int i = 0; i < result.size(); i += 2) {
		values.addElement(String.valueOf(result.elementAt(i)));
		names.addElement(String.valueOf(result.elementAt(i + 1)));
	}
	for (int i = 1; i < result.size(); i += 2) {
		if (formatlist.equals(String.valueOf(result.elementAt(i - 1)))) {
			index = i / 2;
		}
	}
	return new Integer(index);
}
/**
 * Used for dynamically generating the selectbox for selecting
 * the method of the search engine (for example and, or, boolean).
 * This method use reflection to get dynamically the value for the selectbox
 * from the Content Definition which is used.
 *
 * Called while generating the template content from CmsXmlFormTemplateFile.
 * @param cms A_CmsObject for accessing system resources.
 * @param names Vector that will be filled with radio button descriptions.
 * @param values Vector that will be filled with radio buttom values.
 * @param parameters Hashtable with all user parameters.
 * @return Index of the currently checked radio button
 * @see CmsXmlFormTemplateFile
 * Creation date: (27.11.00 11:06:11)
 */

public Integer selectMethod(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
	// start session
	I_CmsSession session = cms.getRequestContext().getSession(true);
	Vector result = new Vector();
	String methodlist = (String) session.getValue("searchengineMethod");
	if (methodlist == null) {
		methodlist = "";
	}
	try {
		Class c = Class.forName((String) parameters.get("searchengineContentDefinition"));
		Method m = c.getMethod("setParameter", new Class[] {String.class, CmsObject.class});
		result = (Vector) m.invoke(null, new Object[] {"method", cms});
	} catch (ClassNotFoundException e) {
		System.err.println(e.toString());
	} catch (ClassCastException e) {
		System.err.println(e.toString());
	} catch (IllegalAccessException e) {
		System.err.println(e.toString());
	} catch (InvocationTargetException e) {
		System.err.println(e.toString());
	} catch (NoSuchMethodException e) {
		System.err.println(e.toString());
	} catch (Exception e) {
		System.err.println(e.toString());
	}
	int index = 0;
	for (int i = 0; i < result.size(); i += 2) {
		values.addElement(String.valueOf(result.elementAt(i)));
		names.addElement(String.valueOf(result.elementAt(i + 1)));
	}
	for (int i = 1; i < result.size(); i += 2) {
		if (methodlist.equals(String.valueOf(result.elementAt(i - 1)))) {
			index = i / 2;
		}
	}
	return new Integer(index);
}
/**
 * Used selectbox for restrict the search if requiered. The paramters are set in the
 * process Tag <area restrict> in the Template.
 *
 * Called while generating the template content from CmsXmlFormTemplateFile.
 * @param cms A_CmsObject for accessing system resources.
 * @param names Vector that will be filled with radio button descriptions.
 * @param values Vector that will be filled with radio buttom values.
 * @param parameters Hashtable with all user parameters.
 * @return Index of the currently checked radio button
 * @see CmsXmlFormTemplateFile
 * Creation date: (27.11.00 11:06:11)
 */

public Integer selectRestrict(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
	int counter = 1, index = 0;
	// session for presetting
	I_CmsSession session = cms.getRequestContext().getSession(true);
	// get the parameter for the selectbox
	String restrictlist = (String) session.getValue("searchengineRestrict");
	// get the parameter for the selectbox and parse it
	if ((String) parameters.get("searchengineAreaRestrict")!=null){
	StringTokenizer zone = new StringTokenizer((String) parameters.get("searchengineAreaRestrict"), ",");
	if (restrictlist == null) {
		restrictlist = "";
	}
	while (zone.hasMoreTokens()) {
		if ((counter % 2) != 0)
			values.addElement(zone.nextToken());
		else
			names.addElement(zone.nextToken());
		counter++;
	}
	for (int i = 0, u = 0; i < (counter - 1); i += 2, u++) {
		if (restrictlist.equals(values.elementAt(u))) {
			index = u;
		}
	}
	}
	return new Integer(index);
}
/**
* Used for dynamically generating the selectbox for selecting
* the sort method of the search engine (for example title, time, score).
* This method use reflection to get dynamically the value for the selectbox
* from the Content Definition which is used.
*
* Called while generating the template content from CmsXmlFormTemplateFile.
* @param cms A_CmsObject for accessing system resources.
* @param names Vector that will be filled with radio button descriptions.
* @param values Vector that will be filled with radio buttom values.
* @param parameters Hashtable with all user parameters.
* @return Index of the currently checked radio button
* @see CmsXmlFormTemplateFile
* Creation date: (27.11.00 11:06:11)
*/

public Integer selectSort(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
	// start session
	I_CmsSession session = cms.getRequestContext().getSession(true);
	Vector result = new Vector();
	String sortlist = (String) session.getValue("searchengineSort");
	if (sortlist == null) {
		sortlist = "";
	}
	try {
		Class c = Class.forName((String) parameters.get("searchengineContentDefinition"));
		Method m = c.getMethod("setParameter", new Class[] {String.class, CmsObject.class});
		result = (Vector) m.invoke(null, new Object[] {"sort", cms});
	} catch (ClassNotFoundException e) {
		System.err.println(e.toString());
	} catch (ClassCastException e) {
		System.err.println(e.toString());
	} catch (IllegalAccessException e) {
		System.err.println(e.toString());
	} catch (InvocationTargetException e) {
		System.err.println(e.toString());
	} catch (NoSuchMethodException e) {
		System.err.println(e.toString());
	} catch (Exception e) {
		System.err.println(e.toString());
	}
	int index = 0;
	for (int i = 0; i < result.size(); i += 2) {
		values.addElement(String.valueOf(result.elementAt(i)));
		names.addElement(String.valueOf(result.elementAt(i + 1)));
	}
	for (int i = 1; i < result.size(); i += 2) {
		if (sortlist.equals(String.valueOf(result.elementAt(i - 1)))) {
			index = i / 2;
		}
	}
	return new Integer(index);
}
/**
 * This method build the query string
 * Creation date: (15.11.00 14:05:15)
 */
private String setQuery(String getRestrict, String getWord, String getMethod, String getFormat, String getSort, String getPage) {
	StringBuffer query = new StringBuffer();
	query.append(("restrict=" + getRestrict));
	query.append(("&method=" + getMethod));
	query.append(("&format=" + getFormat));
	query.append(("&sort=" + getSort));
	query.append(("&words=" + getWord));
	query.append(("&page=" + getPage));
	return String.valueOf(query);
}
}

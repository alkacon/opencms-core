/*
* File   : $Source: /alkacon/cvs/opencms/modules/searchform/src/com/opencms/modules/search/form/Attic/CmsSearchForm.java,v $
* Date   : $Date: 2002/05/10 09:02:20 $
* Version: $Revision: 1.5 $
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

package com.opencms.modules.search.form;

import java.io.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;

import java.net.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * This class is the generic frontend of the OpenCms search. It is possible to integrate
 * different searchengine's during runtime. To call a searchengine class it used reflection.
 * The Properties for the search are set in the Module Administration. Look at the Dokumentation
 * of the Module.
 *
 * @author    Markus Fabritius
 * @version $Revision: 1.5 $ $Date: 2002/05/10 09:02:20 $
 */
public class CmsSearchForm extends com.opencms.defaults.CmsXmlFormTemplate implements I_CmsSearchConstant {

	public final static byte LOGGING = 0;
	public static boolean c_areaField = false;
	public static int c_navigationRange = 10;
	public static String c_contentDefinition = null;
	public static String c_configFile = null;
	public static String c_areaSection = null;
	public static String c_match = null;
	public static String c_parsingUrl = null;

	static {
		try {
			// read the moduleparameters from registry
			moduleParameterWasUpdated(null);
		} catch (CmsException e) {
			System.out.println("CmsSearchFormNew::initializer: Error to call moduleParameterWasUpdated, print Stack Trace now ...");
			e.printStackTrace();
		}
	}


	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters.
	 *
	 * @param cms               CmsObject Object for accessing system resources
	 * @param templateFile      Filename of the template file
	 * @param elementName       Element name of this template in our parent template
	 * @param parameters        Hashtable with all template class parameters
	 * @param templateSelector  template section that should be processed
         *
	 * @return                  The content value
	 * @exception CmsException  Throws Cms Exception
	 * @see                     getContent(CmsObject cms, String templateFile, String
	 *      elementName, Hashtable parameters)
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		// get template
		CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		// check if first request
		String page = (String) parameters.get(C_PARAM_SEARCHFORM_PAGE);
		String action = (String) parameters.get("action");
		// set selectboxes
		setSeparateSelectbox(cms, templateDocument);

		// first time to show the page
		if ((action == null || action.equals("")) && (page == null || page.equals(""))) {
			templateSelector = "default";
		} else {
			// send the request of search to the search module and get the result
			Vector result = getResultFromSearchengine(cms, templateSelector, page, templateDocument, parameters);
			if (result == null) {
				templateSelector = "default";
				templateDocument.setData("lastword", (String) parameters.get(C_PARAM_SEARCHFORM_WORD));
			} else if (validateResultFromSearchengine(cms, result, templateSelector, templateDocument, (String) parameters.get(C_PARAM_SEARCHFORM_WORD))) {
				templateSelector = "result";
				try {
					Class search = Class.forName(c_contentDefinition);
					Object contentDefinition = new Object();
					setSearchResultHead(cms, contentDefinition, search, result, templateDocument);
					setSearchResultBody(cms, contentDefinition, search, result, templateDocument, parameters, page);
					setSearchResultNavigation(cms, page, contentDefinition, search, result, templateDocument, parameters);
				} catch (Exception e) {
					System.out.println("getContent:: Fatal Error to call Method for result, sorry print stack trace now...");
					e.printStackTrace();
				}
			}
		}
		return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
	}


	/**
	 * This event-method is invoked if a user changes a parameter for a module in the
	 * module-administration. It reads the current parameters.
	 *
	 * @param cms               CmsObject Object for accessing system resources
	 * @exception CmsException  Throws Cms Exception
	 */
	public static void moduleParameterWasUpdated(CmsObject cms) throws CmsException {
		// read the parameters from the module properties
		c_contentDefinition = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_MODULE_NAME, C_PARAM_SEARCH_MODUL);
		c_configFile = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_MODULE_NAME, C_PARAM_CONFIGURATION_FILE);
		c_areaField = OpenCms.getRegistry().getModuleParameterBoolean(
			C_PARAM_MODULE_NAME, C_PARAM_AREA_FIELD);
		c_match = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_MODULE_NAME, C_PARAM_MATCH_PER_PAGE);
		c_parsingUrl = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_MODULE_NAME, C_PARAM_PARSING_EXCERPT_URL);
		c_navigationRange = OpenCms.getRegistry().getModuleParameterInteger(
			C_PARAM_MODULE_NAME, C_PARAM_NAVIGATION_RANGE);
		if (c_configFile.equals("")) {
			c_configFile = "default";
		}
		if (c_match.equals("")) {
			c_match = "10";
		}
		if (c_navigationRange == 0) {
			c_navigationRange = 10;
		}
		if (c_areaField) {
			c_areaSection = OpenCms.getRegistry().getModuleParameterString(
				C_PARAM_MODULE_NAME, C_PARAM_AREA_SECTION);
		}
	}


    /**
     * Set selectboxes for match per page and restrict if the variable set in the
     * administration of the module. For processing the correspond datablock set in the
     * template.
     *
     * @param cms       CmsObject Object for accessing system resources
     * @param template  the current used template
     */
     private void setSeparateSelectbox(CmsObject cms, CmsXmlTemplateFile template) {
        try {
            if (c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN) != -1) {
	      template.setData("setMatchPerPage", template.getData("selectMatch"));
            }
	    if (c_areaField) {
	      template.setData("setSelectForRestrict", template.getData("selectRestrict"));
            }
         } catch (Exception e) {
	      System.out.println("setSeparateSelectbox::set tempalte data failed, print Stack Trace ...");
              e.printStackTrace();
         }
     }

    /**
     * Set the head of the search result into the template.
     *
     * @param cms          CmsObject Object for accessing system resources
     * @param cd           Content definition object
     * @param searchModul  class name of the searchengine class
     * @param getResult    vector of the result set
     * @param template     the current used template
     */
     public void setSearchResultHead(CmsObject cms, Object cd, Class searchModul, Vector getResult, CmsXmlTemplateFile template) {
        try {
	    cd = searchModul.newInstance();
            cd = (I_CmsSearchEngine) getResult.elementAt(0);
        } catch (Exception e) {
            System.out.println("setSearchResultBody:: Fatal Error to create new Instance, print stack trace now...");
	    e.printStackTrace();
	}
        template.setData("searchword", ((I_CmsSearchEngine) cd).getSearchWord());
        template.setData("lastword", ((I_CmsSearchEngine) cd).getSearchWord());
	template.setData("first", String.valueOf(((I_CmsSearchEngine) cd).getFirstDisplay()));
	template.setData("last", String.valueOf(((I_CmsSearchEngine) cd).getLastDisplay()));
	template.setData("match", String.valueOf(((I_CmsSearchEngine) cd).getMatch()));
	template.setData("pages", String.valueOf(((I_CmsSearchEngine) cd).getPages()));
    }

    /**
     * Set the body of the search result into the template.
     *
     * @param cms          CmsObject Object for accessing system resources
     * @param cd           Content definition object
     * @param searchModul  class name of the searchengine class
     * @param getResult    vector of the result set
     * @param template     the current used template
     * @param parameters   Hashtable with all template class parameters
     * @param page         the current page of the search result
     */
     public void setSearchResultBody(CmsObject cms, Object cd, Class searchModul, Vector getResult, CmsXmlTemplateFile template, Hashtable parameters, String page) {
     	String list = "";
      	String parsingUrl = "";
      	String tempParsing = "";
      	String format = (String) parameters.get(C_PARAM_SEARCHFORM_FORMAT);
      	String match = (String) parameters.get(C_PARAM_SEARCHFORM_MATCH_PER_PAGE);
      	int matchPerPage = 0;
      	int counter = 1;
      	int compare = 0;
      	int pageset = 0;
        int startIndex = 0;

        if (match == null) {
	  matchPerPage = (c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN) == -1) ? Integer.parseInt(c_match) : Integer.parseInt(c_match.substring(0, c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN)));
	} else {
	  matchPerPage = Integer.parseInt(match);
	}

        try {
	  cd = searchModul.newInstance();
	  cd = (I_CmsSearchEngine) getResult.elementAt(0);
	  pageset = ((I_CmsSearchEngine) cd).getPages();
	} catch (Exception e) {
	  System.out.println("setSearchResultBody:: Fatal Error to create new Instance , print stack trace now...");
	  e.printStackTrace();
	}

        if (page != null) {
	  compare = Integer.parseInt(page);
	  for (int i = 1; i <= pageset; i++) {
	    if (compare == i) {
	      counter = ((i * matchPerPage) - (matchPerPage - 1));
            }
          }
	}
	try {
	  for (int i = 1; i < getResult.size(); i++) {
	    cd = searchModul.newInstance();
	    cd = (I_CmsSearchEngine) getResult.elementAt(i);
	    parsingUrl = ((I_CmsSearchEngine) cd).getUrl();
            try {
	      if ((c_parsingUrl != null) && (!c_parsingUrl.equals("")) && (!c_parsingUrl.equals("none"))) {
		startIndex = parsingUrl.indexOf(c_parsingUrl);
                if(startIndex!=-1){
                  StringBuffer tempWord = new StringBuffer(parsingUrl);
                  tempWord.delete(startIndex,startIndex+(c_parsingUrl.length()));
                  parsingUrl = tempWord.toString();
                }
              }
            } catch (Exception e) {
              System.out.println("Error in setSearchResultBody, parsing Excerpt Url print Stack Trace ...");
              e.printStackTrace();
	    }
	    // check for format ( long ) fill template body
	    if ((format == null) || format.equals("long")) {
	      template.setData("number", counter + "");
	      template.setData("url", ((I_CmsSearchEngine) cd).getUrl());
	      template.setData("url_description", parsingUrl);
	      template.setData("title", ((I_CmsSearchEngine) cd).getTitle());
              template.setData("percent", String.valueOf(((I_CmsSearchEngine) cd).getPercentMatch()));
              template.setData("excerpt", ((I_CmsSearchEngine) cd).getExcerpt());
              template.setData("size", String.valueOf(((I_CmsSearchEngine) cd).getSize()));
              template.setData("modified", ((I_CmsSearchEngine) cd).getModified());
              String longrow = template.getProcessedDataValue("longrow");
              list += longrow;
            // check for format ( short ) fill template body
            } else {
	      template.setData("number", counter + "");
              template.setData("url", ((I_CmsSearchEngine) cd).getUrl());
              template.setData("url_description", parsingUrl);
              template.setData("title", ((I_CmsSearchEngine) cd).getTitle());
              template.setData("percent", String.valueOf(((I_CmsSearchEngine) cd).getPercentMatch()));
              String shortrow = template.getProcessedDataValue("shortrow");
              list += shortrow;
            }
            counter++;
          }
        } catch (Exception e) {
	  System.out.println("setSearchResultBody:: Fatal Error , for seting result. Print stack trace now...");
	  e.printStackTrace();
	}
	  template.setData("resultlist", list);
      }

	/**
	 * Called while generating the template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms          A_CmsObject for accessing system resources.
	 * @param parameters   Hashtable with all user parameters.
	 * @param page         the current page of the search result
	 * @param cd           Content definition object
	 * @param searchModul  class name of the searchengine class
	 * @param getResult    vector of the result set
	 * @param template     the current used template
	 * @see                CmsXmlFormTemplateFile
	 */
	public void setSearchResultNavigation(CmsObject cms, String page, Object cd, Class searchModul, Vector getResult, CmsXmlTemplateFile template, Hashtable parameters) {
		String navigation = "";
		String buildquery = "";
		String tempNaviagtion = "";
		int result = 0;
		int site = 1;
		int constant = 0;
		int factor = 0;
		int loop = 0;
		int startloop = 0;
		int range = c_navigationRange;
		String matchPerPage = (String) parameters.get(C_PARAM_SEARCHFORM_MATCH_PER_PAGE);
		String serverpath = cms.getRequestContext().getRequest().getServletUrl() + "" +
			cms.getRequestContext().getUri();
		if (matchPerPage == null) {
			matchPerPage = (c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN) == -1) ? c_match : c_match.substring(0, c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN));
		}

		try {
			cd = searchModul.newInstance();
			cd = (I_CmsSearchEngine) getResult.elementAt(0);
			site = ((I_CmsSearchEngine) cd).getPages();
		} catch (Exception e) {
			System.out.println("setSearchResultNavigation:: Fatal Error to create new Instance, print stack trace now...");
			e.printStackTrace();
		}
		page = (page == null) ? "1" : page;
		result = (Integer.parseInt(page) - 1) / (range);
		constant = site / range;
		factor = (constant > 0) ? result : 0;
		startloop = ((factor * range) + 1);
		loop = (((factor * range) + range) > site) ? site : ((factor * range) + range);

		if (result >= 1) {
			buildquery = setQuery(cms, parameters, matchPerPage, String.valueOf(startloop - 1));
			template.setData("query_back", buildquery);
			try {
				tempNaviagtion = template.getProcessedDataValue("back");
			} catch (Exception e) {
				System.out.println("setSearchResultNavigation:: Fatal Error to Process navigation forward, print stack trace now...");
				e.printStackTrace();
			}
			navigation += tempNaviagtion;
		}

		for (int i = startloop; i <= loop; i++) {
			if (Integer.parseInt(page) == i) {
				template.setData("currentpage", String.valueOf(i));
				try {
					tempNaviagtion = template.getProcessedDataValue("currentnav");
				} catch (Exception e) {
					System.out.println("setSearchResultNavigation:: Fatal Error to Process current site, print stack trace now...");
					e.printStackTrace();
				}
				navigation += tempNaviagtion;
			} else {
				buildquery = setQuery(cms, parameters, matchPerPage, String.valueOf(i));
				template.setData("server", serverpath);
				template.setData("query", buildquery);
				template.setData("numberurl", String.valueOf(i));
				try {
					tempNaviagtion = template.getProcessedDataValue("nav");
				} catch (Exception e) {
					System.out.println("setSearchResultNavigation:: Fatal Error to Process navigation, print stack trace now...");
					e.printStackTrace();
				}
				navigation += tempNaviagtion;
			}
		}

		if ((constant >= 1) && (site != loop)) {
			buildquery = setQuery(cms, parameters, matchPerPage, String.valueOf(loop + 1));
			template.setData("query_next", buildquery);
			try {
				tempNaviagtion = template.getProcessedDataValue("next");
			} catch (Exception e) {
				System.out.println("setSearchResultNavigation:: Fatal Error to Process navigation back, print stack trace now...");
				e.printStackTrace();
			}

			navigation += tempNaviagtion;
		}
		template.setData("navigation", navigation);
	}


	/**
	 * This method build the query string to send this to the searchengine (class).
	 *
	 * @param getPage          current page of the result
	 * @param cms              CmsObject Object for accessing system resources
	 * @param parameters       Hashtable with all template class parameters
	 * @param match            total hit of search result
	 *
         * @return                 return the current query string for the searchengine
	 */
	private String setQuery(CmsObject cms, Hashtable parameters, String match, String getPage) {
		String word = Encoder.escapeWBlanks((String) parameters.get(C_PARAM_SEARCHFORM_WORD));
		String method = (String) parameters.get(C_PARAM_SEARCHFORM_METHOD);
		String format = (String) parameters.get(C_PARAM_SEARCHFORM_FORMAT);
		String sort = (String) parameters.get(C_PARAM_SEARCHFORM_SORT);
		String restrict = (String) parameters.get(C_PARAM_SEARCHFORM_RESTRICT);

		StringBuffer query = new StringBuffer();
		if (restrict != null) {
			query.append((C_PARAM_SEARCHFORM_RESTRICT + "=" + Encoder.escapeWBlanks(restrict) + "&"));
		}
		if (method != null) {
			query.append((C_PARAM_SEARCHFORM_METHOD + "=" + Encoder.escapeWBlanks(method) + "&"));
		}
		if (format != null) {
			query.append((C_PARAM_SEARCHFORM_FORMAT + "=" + Encoder.escapeWBlanks(format) + "&"));
		}
		if (sort != null) {
			query.append((C_PARAM_SEARCHFORM_SORT + "=" + Encoder.escapeWBlanks(sort) + "&"));
		}
		query.append((C_PARAM_SEARCHFORM_MATCH_PER_PAGE + "=" + match + "&"));
		query.append((C_PARAM_SEARCHFORM_WORD + "=" + word + "&"));
		query.append((C_PARAM_SEARCHFORM_PAGE + "=" + getPage));
		return String.valueOf(query);
	}

	/**
         * Calling the respective searchengine class which is define in the administration
         * property of the module. It used the reflection method for calling.
         * The result is a vector with content definition elements.
         *
	 * @param cms               CmsObject Object for accessing system resources
	 * @param templateSelector  template section that should be processed
	 * @param page              the current page of the search result
	 * @param template          the current used template
	 * @param parameters        Hashtable with all template class parameters
         *
	 * @return                  the result from the calling Searchengine class
	 */
	private Vector getResultFromSearchengine(CmsObject cms, String templateSelector, String page, CmsXmlTemplateFile template, Hashtable parameters) {
		String noWord = "";
		Vector getResult = null;
		String word = (String) parameters.get(C_PARAM_SEARCHFORM_WORD);
		String method = (String) parameters.get(C_PARAM_SEARCHFORM_METHOD);
		String sort = (String) parameters.get(C_PARAM_SEARCHFORM_SORT);
		String restrict = (String) parameters.get(C_PARAM_SEARCHFORM_RESTRICT);
		String matchPerPage = (String) parameters.get(C_PARAM_SEARCHFORM_MATCH_PER_PAGE);
		if (matchPerPage == null) {
			matchPerPage = (c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN) == -1) ? c_match : c_match.substring(0, c_match.indexOf(C_PARAM_SEARCHENGINE_TOKEN));
		}
		// if the parameter does not exit set default value this is e.g.
		// quicksearch there is only a textfield for the searchword
		//logView("\n\nParameter:\n------------\nwords: " + word + "\nmethod: " + method + "\nsort: " + sort + "\npage: " + page +
		//	"\nConfig: " + c_configFile + "\nrestrict: " + restrict + "\nmatchperpage: " + matchPerPage + "\n\n");
		// error request for search word
		if (word == null || word.equals("")) {
			try {
				noWord = template.getDataValue(C_PARAM_ERROR_NOWORD);
			} catch (Exception e) {
			}
			template.setData("message", noWord);
			return null;
		}
		// start reflection with the classname from the template
		try {
			Class c = Class.forName(c_contentDefinition);
			Method m = c.getMethod("read", new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, CmsObject.class});
			getResult = (Vector) m.invoke(null, new Object[]{word, method, sort, page, c_configFile, restrict, matchPerPage, cms});
		} catch (InvocationTargetException e) {
			String error = String.valueOf(e.getTargetException());
			System.out.println("getResultFromSearchengine:: reflection:" + e.getTargetException());
			e.printStackTrace();
			getErrorMessage(cms, error, template);
			return null;
		} catch (Exception e) {
			System.out.println("getResultFromSearchengine:: Fatal Error, printing stack now ...");
			e.printStackTrace();
		}
		return getResult;
	}

	/**
	 * Gets the errorMessage attribute of the CmsSearchFormNew object
	 *
	 * @param cms       CmsObject Object for accessing system resources
	 * @param error     String of error type
	 * @param template  the current used template
	 */
	public void getErrorMessage(CmsObject cms, String error, CmsXmlTemplateFile template) {
		String noServer = "";
		String fileTopicError = "";
		String reflectionError = "";

		int end = error.indexOf(":");
		if (end != -1) {
			error = error.substring(0, end);
		}
		if (error.equalsIgnoreCase("java.io.FileNotFoundException")) {
			try {
				noServer = template.getDataValue(C_PARAM_ERROR_SERVERPATH);
			} catch (Exception e) {
				System.out.println("getErrorMessage::No Datablocks found ...");
			}
			template.setData("message", noServer);
		} else if ((error.equalsIgnoreCase("java.lang.NumberFormatException")) || (error.equalsIgnoreCase("java.lang.ArrayIndexOutOfBoundsException"))) {
			try {
				fileTopicError = template.getDataValue(C_PARAM_ERROR_FILE_TOPIC);
			} catch (Exception e) {
				System.out.println("getErrorMessage::No Datablocks found ...");
			}
			template.setData("message", fileTopicError);
		} else {
			try {
				reflectionError = template.getDataValue(C_PARAM_ERROR_REFLECTION);
			} catch (Exception e) {
				System.out.println("getErrorMessage::No Datablocks found ...");
			}
			template.setData("message", reflectionError);
		}
	}


	/**
	 * @param cms               CmsObject Object for accessing system resources
	 * @param result            vector with the result of the searchengine class
	 * @param templateSelector  template section that should be processed
	 * @param template          the current used template
	 * @param words             value of the searchword
         *
	 * @return                  <EM>true</EM> if any result is given, <EM>false</EM> otherwise.
	 */
	public boolean validateResultFromSearchengine(CmsObject cms, Vector result, String templateSelector, CmsXmlTemplateFile template, String words) {
		if (result.size() <= 1) {
			try {
				if (result.elementAt(0).equals("syntax")) {
					template.setData("message", template.getDataValue("SyntaxError"));
				} else {
					template.setData("message", template.getDataValue("NoMatch"));
				}
			} catch (Exception e) {
			}
			template.setData("lastword", words);
			templateSelector = "default";
			return false;
		}
		return true;
	}


	/**
	 * Used for dynamically generating the selectbox for selecting the format method
	 * of the search engine (for example long, short). This method use reflection to
	 * get dynamically the value for the selectbox from the Content Definition which
	 * is used. Called while generating the template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms            A_CmsObject for accessing system resources.
	 * @param names          Vector that will be filled with radio button descriptions.
	 * @param values         Vector that will be filled with radio buttom values.
	 * @param parameters     Hashtable with all user parameters.
	 * @return               Index of the currently checked radio button
	 * @exception Exception  Description of the Exception
	 * @see                  CmsXmlFormTemplateFile
	 */
	public Integer selectFormat(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
		Vector result = new Vector();
		String formatlist = (String) parameters.get(C_PARAM_SEARCHFORM_FORMAT);
		if (formatlist == null) {
			formatlist = "";
		}
		try {
			Class c = Class.forName(c_contentDefinition);
			Method m = c.getMethod("setParameter", new Class[]{String.class, CmsObject.class});
			result = (Vector) m.invoke(null, new Object[]{C_PARAM_SEARCHFORM_FORMAT, cms});
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
	 * Used for dynamically generating the selectbox for selecting the sort method of
	 * the search engine (for example title, time, score). This method use reflection
	 * to get dynamically the value for the selectbox from the Content Definition which
	 * is used. Called while generating the template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms            A_CmsObject for accessing system resources.
	 * @param names          Vector that will be filled with radio button descriptions.
	 * @param values         Vector that will be filled with radio buttom values.
	 * @param parameters     Hashtable with all user parameters.
	 * @return               Index of the currently checked radio button
	 * @exception Exception  Description of the Exception
	 * @see                  CmsXmlFormTemplateFile
	 */
	public Integer selectSort(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
		Vector result = new Vector();
		String sortlist = (String) parameters.get(C_PARAM_SEARCHFORM_SORT);
		if (sortlist == null) {
			sortlist = "";
		}
		try {
			Class c = Class.forName(c_contentDefinition);
			Method m = c.getMethod("setParameter", new Class[]{String.class, CmsObject.class});
			result = (Vector) m.invoke(null, new Object[]{C_PARAM_SEARCHFORM_SORT, cms});
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
	 * Used for dynamically generating the selectbox for selecting the method of the
	 * search engine (for example and, or, boolean). This method use reflection to get
	 * dynamically the value for the selectbox from the Content Definition which is
	 * used. Called while generating the template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms            A_CmsObject for accessing system resources.
	 * @param names          Vector that will be filled with radio button descriptions.
	 * @param values         Vector that will be filled with radio buttom values.
	 * @param parameters     Hashtable with all user parameters.
	 * @return               Index of the currently checked radio button
	 * @exception Exception  Description of the Exception
	 * @see                  CmsXmlFormTemplateFile
	 */
	public Integer selectMethod(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
		Vector result = new Vector();
		String methodlist = (String) parameters.get(C_PARAM_SEARCHFORM_METHOD);
		if (methodlist == null) {
			methodlist = "";
		}
		try {
			Class c = Class.forName(c_contentDefinition);
			Method m = c.getMethod("setParameter", new Class[]{String.class, CmsObject.class});
			result = (Vector) m.invoke(null, new Object[]{C_PARAM_SEARCHFORM_METHOD, cms});
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
			e.printStackTrace();
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
	 * Used selectbox for restrict the search if requiered. The paramters are set in
	 * the process Tag <area restrict> in the Template. Called while generating the
	 * template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms            A_CmsObject for accessing system resources.
	 * @param names          Vector that will be filled with radio button descriptions.
	 * @param values         Vector that will be filled with radio buttom values.
	 * @param parameters     Hashtable with all user parameters.
	 * @return               Index of the currently checked radio button
	 * @exception Exception  Description of the Exception
	 * @see                  CmsXmlFormTemplateFile
	 */
	public Integer selectRestrict(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
		int counter = 1;
		int index = -1;
		// get the parameter for the selectbox
		String restrictlist = (String) parameters.get(C_PARAM_SEARCHFORM_RESTRICT);
		// get the parameter for the selectbox and parse it
		StringTokenizer zone = new StringTokenizer(c_areaSection, C_PARAM_SEARCHENGINE_TOKEN);
		if (restrictlist == null) {
			restrictlist = "";
		}
		while (zone.hasMoreTokens()) {
			if ((counter % 2) != 0) {
				values.addElement(zone.nextToken().trim());
			} else {
				names.addElement(zone.nextToken().trim());
			}
			counter++;
		}
		for (int i = 0, u = 0; i < (counter - 1); i += 2, u++) {
			if (restrictlist.equals(values.elementAt(u))) {
				index = u;
			}
		}
		return new Integer(index);
	}


	/**
	 * Used selectbox for restrict the search if requiered. The paramters are set in
	 * the process Tag <area restrict> in the Template. Called while generating the
	 * template content from CmsXmlFormTemplateFile.
	 *
	 * @param cms            A_CmsObject for accessing system resources.
	 * @param names          Vector that will be filled with radio button descriptions.
	 * @param values         Vector that will be filled with radio buttom values.
	 * @param parameters     Hashtable with all user parameters.
	 * @return               Index of the currently checked radio button
	 * @exception Exception  Description of the Exception
	 * @see                  CmsXmlFormTemplateFile Creation date: (27.11.00 11:06:11)
	 */
	public Integer selectMatchperPage(CmsObject cms, Vector values, Vector names, Hashtable parameters) throws Exception {
		String tempMatch = "";
		int counter = 1;
		int index = 0;
		// get the parameter for the selectbox
		String match = (String) parameters.get(C_PARAM_SEARCHFORM_MATCH_PER_PAGE);
		// get the parameter for the selectbox and parse it
		StringTokenizer matchesValue = new StringTokenizer(c_match, C_PARAM_SEARCHENGINE_TOKEN);
		if (match == null) {
			match = "";
		}
		while (matchesValue.hasMoreTokens()) {
			tempMatch = matchesValue.nextToken().trim();
			values.addElement(tempMatch);
			names.addElement(tempMatch);
			counter++;
		}
		for (int i = 0, u = 0; i < (counter - 1); i += 2, u++) {
			String test = (String) values.elementAt(u);
			if (test.equals(match)) {
				index = u;
			}
		}
		return new Integer(index);
	}


	/**
	 * gets the caching information from the current template class.
	 *
	 * @param cms               CmsObject Object for accessing system resources
	 * @param templateFile      Filename of the template file
	 * @param elementName       Element name of this template in our parent template.
	 * @param parameters        Hashtable with all template class parameters.
	 * @param templateSelector  template section that should be processed.
	 * @return                  <EM>true</EM> if this class may stream it's results,
	 *      <EM>false</EM> otherwise.
	 */
	public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		// First build our own cache directives.
		return new CmsCacheDirectives(false);
	}

	/**
	 * display some debug hints dependent from configuration
	 *
	 * @param display  String which should shown
	 */
	public void logView(String display) {
		if (LOGGING > 0) {
			if (display != null) {
				System.out.println(display);
			}
		}
	}

}

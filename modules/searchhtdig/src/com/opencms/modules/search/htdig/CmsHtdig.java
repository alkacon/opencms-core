/*
* File   : $Source: /alkacon/cvs/opencms/modules/searchhtdig/src/com/opencms/modules/search/htdig/Attic/CmsHtdig.java,v $
* Date   : $Date: 2002/02/19 09:27:50 $
* Version: $Revision: 1.3 $
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

package com.opencms.modules.search.htdig;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is the class for the searchengine Ht//dig. To work correct it is nessary to modify the result Templates
 * of Htdig. Look at the Dokumentation of the Module. The class passes the retrieval query to Ht://Dig
 * and analyses the result and sent it back to the called program. This is the Content Definition.
 *
 * @author    Markus Fabritius
 * @version $Revision: 1.3 $ $Date: 2002/02/19 09:27:50 $
 */
public class CmsHtdig extends com.opencms.defaults.CmsXmlFormTemplate implements com.opencms.core.I_CmsConstants, com.opencms.modules.search.form.I_CmsSearchEngine {

	private String m_query;
	private String m_searchword;
	private String m_url;
	private String m_title;
	private String m_excerpt;
	private String m_modified;

	private int m_percent;
	private int m_size;
	private int m_pages;
	private int m_matches;
	private int m_firstdisplay;
	private int m_lastdisplay;
	private int m_urlpage;

	private static String c_serverpath;
	private static String c_select_method;
	private static String c_select_format;
	private static String c_select_sort;

	public final static String C_PARAM_SEARCH_MODULE_HTDIG = "com.opencms.modules.search.htdig";
	public final static String C_PARAM_HTDIG_FORMAT = "format";
	public final static String C_PARAM_HTDIG_FORMAT_SHORT = "short";
	public final static String C_PARAM_HTDIG_FORMAT_LONG = "long";
	public final static String C_PARAM_HTDIG_METHOD = "method";
	public final static String C_PARAM_HTDIG_METHOD_AND = "and";
	public final static String C_PARAM_HTDIG_METHOD_OR = "or";
	public final static String C_PARAM_HTDIG_METHOD_BOOLEAN = "boolean";
	public final static String C_PARAM_HTDIG_SORT = "sort";
	public final static String C_PARAM_HTDIG_SORT_SCORE = "score";
	public final static String C_PARAM_HTDIG_SORT_DATE = "date";
	public final static String C_PARAM_HTDIG_SORT_TITLE = "title";
	public final static String C_PARAM_HTDIG_SORT_REV_SCORE = "revscore";
	public final static String C_PARAM_HTDIG_SORT_REV_DATE = "revdate";
	public final static String C_PARAM_HTDIG_SORT_REV_TITLE = "revtitle";
	public final static int C_PARAM_HTDIG_ADD = 5;
	public final static int C_PARAM_HTDIG_BODY_SIZE = 6;
	public final static String C_PARAM_SEARCHENGINE_TOKEN = ",";
	public final static String C_PARAM_SERVERPATH = "Serverpath";

	static {
		try {
			// read the moduleparameters from registry
			moduleParameterWasUpdated(null);
		} catch (CmsException e) {
			e.printStackTrace();
		}
	}

	/**
	 * These empty Constructorfor needs the method setParameter.
	 */
	public CmsHtdig() { }

	/**
	 * The Constructor used for the content definition (head ,one times).
	 *
	 * @param word   String Object set the variable m_searchword with the searchword.
	 * @param pages  set the variable m_pages with number of all pages for the match.
	 * @param first  set the variable m_firstdisplay with with the first number of the current page.
	 * @param last   set the variable m_lastdisplay with with the last number of the current page.
	 * @param match  set the variable m_matches specification of all obtained hits.
	 */
	public CmsHtdig(String word, int pages, int first, int last, int match) {
		m_searchword = word;
		m_pages = pages;
		m_firstdisplay = first;
		m_lastdisplay = last;
		m_matches = match;
	}

	/**
	 * The Constructor used for the content definition (body).
	 *
	 * @param url       set the variable m_url with the url for the hit.
	 * @param title     set the variable m_title with the titel the hit.
	 * @param excerpt   set the variable m_excerpt with a small excerpt of the hit.
	 * @param size      set the variable m_size with the size of the file.
	 * @param percent   set the variable m_percent gives the wight of the hit.
	 * @param modified  set the variable m_url with the modify date.
	 */
	public CmsHtdig(String url, String title, String excerpt, int size, int percent, String modified) {
		m_url = url;
		m_title = title;
		m_excerpt = excerpt;
		m_size = size;
		m_percent = percent;
		m_modified = modified;
	}

	/**
	 * Get method for the excerpt.
	 *
	 * @return   The excerpt value
	 */
	public String getExcerpt() {
		return m_excerpt;
	}

	/**
	 * Get method for first number of the current page.
	 *
	 * @return   The firstDisplay value
	 */
	public int getFirstDisplay() {
		return m_firstdisplay;
	}

	/**
	 * Get method for last number of the current page.
	 *
	 * @return   The lastDisplay value
	 */
	public int getLastDisplay() {
		return m_lastdisplay;
	}

	/**
	 * Get method for the total hit of search.
	 *
	 * @return   The match value
	 */
	public int getMatch() {
		return m_matches;
	}

	/**
	 * Get method for last modified Date of the file.
	 *
	 * @return   The modified value
	 */
	public String getModified() {
		return m_modified;
	}

	/**
	 * Get method for the total number of page.
	 *
	 * @return   The pages value
	 */
	public int getPages() {
		return m_pages;
	}

	/**
	 * Get method for the weight of hit in percent.
	 *
	 * @return   The percentMatch value
	 */
	public int getPercentMatch() {
		return m_percent;
	}

	/**
	 * Get method for the current search word.
	 *
	 * @return   The searchWord value
	 */
	public String getSearchWord() {
		return m_searchword;
	}

	/**
	 * Get method for the size of the file.
	 *
	 * @return   The size value
	 */
	public int getSize() {
		return m_size;
	}

	/**
	 * Get method for the title.
	 *
	 * @return   The title value
	 */
	public String getTitle() {
		return m_title;
	}

	/**
	 * Get method for the Url.
	 *
	 * @return   The url value
	 */
	public String getUrl() {
		return m_url;
	}

	/**
	 * This method build the interface between the searchengine Ht://Dig and the search
	 * form from opencms. The method get the parameters form the search form and build
	 * a query String a send it to the search program htsearch from Ht://Dig. The result
	 * from htsearch are evaluate and send back to the search form class of opencms.
	 *
	 * @param word           for the searchword.
	 * @param method         indicates which logical method (AND, OR, BOOLEAN) used.
	 * @param sort           indicates which sort method used.
	 * @param page           indicates of the current page.
	 * @param conf           indicates of the configuration file which should used.
	 * @param restrict       indicates of the restrict search.
	 * @param cms            CmsObject Object for accessing system resources.
	 * @param matchpp        indicates match per page.
	 *
     * @return               return a vector with content definition objects.
	 * @exception Exception  Throws Exception.
	 */
	public static Vector read(String word, String method, String sort, String page, String conf, String restrict, String matchpp, CmsObject cms) throws Exception {
		int x = 0;
		int i = 0;
		int itemp1;
		int itemp2;
		int itemp3;
		int itemp4;
		int itemp5;
		int setMatch = 10;
		String query;
		String temp1;
		String temp2;
		String temp3;
		String temp4;
		Vector result = new Vector();
		Reader reader = null;
		CmsHtdig cdh;
		// check the rectrict area
		if (method == null) {
			method = "and";
		}
		if (sort == null) {
			sort = "score";
		}
		if (conf.equals("default")) {
			conf = "htdig";
		}
		if (restrict == null || restrict.equals("ALL")) {
			restrict = "";
		}
		if (matchpp != null) {
			setMatch = Integer.parseInt(matchpp);
		}
		// check for first time to start the page
		// set the size of output array
		String[] output = new String[((setMatch * C_PARAM_HTDIG_BODY_SIZE) + C_PARAM_HTDIG_ADD)];

		if (page == null) {
			query = "restrict=" + restrict + "&config=" + conf + "&method=" + method + "&format=long&sort=" + sort + "&matchesperpage=" + setMatch + "&words=" + word;
		} else {
			query = "restrict=" + restrict + "&config=" + conf + "&method=" + method + "&format=long&sort=" + sort + "&matchesperpage=" + setMatch + "&words=" + word + "&page=" + page;
		}

		// Start of Url Connection
		URL url = new URL(c_serverpath);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStream connectionOutput = connection.getOutputStream();
		Writer writer = new OutputStreamWriter(connectionOutput, "latin1");
		writer.write(query);
		writer.close();
		InputStream in = connection.getInputStream();
		reader = new InputStreamReader(in, "latin1");
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = new String();

		while ((line = bufferedReader.readLine()) != null) {
			output[i] = line;
			i++;
		}
		bufferedReader.close();

		// if the return is nomatch or syntax Error
		if (i <= 1) {
			if (output[0].equals("syntax")) {
				result.addElement(output[0]);
			} else {
				String nomatch = "nomatch";
				result.addElement(nomatch);
			}
		} else {
			// fill head of content definition
			temp1 = output[x];
			itemp1 = Integer.parseInt(output[x + 1]);
			itemp2 = Integer.parseInt(output[x + 2]);
			itemp3 = Integer.parseInt(output[x + 3]);
			itemp4 = Integer.parseInt(output[x + 4]);
			cdh = new CmsHtdig(temp1, itemp1, itemp2, itemp3, itemp4);
			result.addElement(cdh);
			// fill body of content definition
			for (x = C_PARAM_HTDIG_ADD; x < i; x += C_PARAM_HTDIG_BODY_SIZE) {
				temp1 = output[x];
				temp2 = output[x + 1];
				temp3 = output[x + 2];
				itemp1 = Integer.parseInt(output[x + 3]);
				itemp2 = Integer.parseInt(output[x + 4]);
				temp4 = output[x + 5];
				cdh = new CmsHtdig(temp1, temp2, temp3, itemp1, itemp2, temp4);
				result.addElement(cdh);
			}
		}
		return result;
	}

	/**
	 * The method fills the selectbox of the search form. The first value is the value
	 * of the selectbox, the second is the name.
	 *
	 * @param selection      indicates which selectbox should be filled.
	 * @param cms            CmsObject Object for accessing system resources.
     *
	 * @return               Vector Object indicates which type of selection should used in Frontend.
	 * @exception Exception  Throws Exception.
	 */
	public static Vector setParameter(String selection, CmsObject cms) throws Exception {
		Vector parameter = new Vector();
		String[] temp = new String[6];
		int i = 0;
		if (selection.equals("method")) {
			if ((c_select_method != "") && (c_select_method != null)) {
				StringTokenizer st = new StringTokenizer(c_select_method, C_PARAM_SEARCHENGINE_TOKEN);
				try {
					while (st.hasMoreTokens() && i < 3) {
						temp[i] = st.nextToken();
						i++;
					}
					parameter.addElement(C_PARAM_HTDIG_METHOD_AND);
					parameter.addElement(temp[0]);
					parameter.addElement(C_PARAM_HTDIG_METHOD_OR);
					parameter.addElement(temp[1]);
					if (temp[2] != null) {
						parameter.addElement(C_PARAM_HTDIG_METHOD_BOOLEAN);
						parameter.addElement(temp[2]);
					}
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} else {
				parameter.addElement(C_PARAM_HTDIG_METHOD_AND);
				parameter.addElement("All");
				parameter.addElement(C_PARAM_HTDIG_METHOD_OR);
				parameter.addElement("Any");
				parameter.addElement(C_PARAM_HTDIG_METHOD_BOOLEAN);
				parameter.addElement("Boolean");
			}
		} else
			if (selection.equals("format")) {
			if ((c_select_format != "") && (c_select_format != null)) {
				StringTokenizer st = new StringTokenizer(c_select_format, C_PARAM_SEARCHENGINE_TOKEN);
				try {
					while (st.hasMoreTokens() && i < 2) {
						temp[i] = st.nextToken();
						i++;
					}
					parameter.addElement(C_PARAM_HTDIG_FORMAT_LONG);
					parameter.addElement(temp[0]);
					parameter.addElement(C_PARAM_HTDIG_FORMAT_SHORT);
					parameter.addElement(temp[1]);
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} else {
				parameter.addElement(C_PARAM_HTDIG_FORMAT_LONG);
				parameter.addElement("Long");
				parameter.addElement(C_PARAM_HTDIG_FORMAT_SHORT);
				parameter.addElement("Short");
			}
		} else
			if (selection.equals("sort")) {
			if ((c_select_sort != "") && (c_select_sort != null)) {
				StringTokenizer st = new StringTokenizer(c_select_sort, C_PARAM_SEARCHENGINE_TOKEN);
				try {
					while (st.hasMoreTokens() && i < 6) {
						temp[i] = st.nextToken();
						i++;
					}
					parameter.addElement(C_PARAM_HTDIG_SORT_SCORE);
					parameter.addElement(temp[0]);
					parameter.addElement(C_PARAM_HTDIG_SORT_DATE);
					parameter.addElement(temp[1]);
					if (temp[2] != null) {
						parameter.addElement(C_PARAM_HTDIG_SORT_TITLE);
						parameter.addElement(temp[2]);
					}
					if (temp[3] != null) {
						parameter.addElement(C_PARAM_HTDIG_SORT_REV_SCORE);
						parameter.addElement(temp[3]);
					}
					if (temp[4] != null) {
						parameter.addElement(C_PARAM_HTDIG_SORT_REV_DATE);
						parameter.addElement(temp[4]);
					}
					if (temp[5] != null) {
						parameter.addElement(C_PARAM_HTDIG_SORT_REV_TITLE);
						parameter.addElement(temp[5]);
					}
				} catch (Exception e) {
					System.err.println(e.toString());
				}
			} else {
				parameter.addElement(C_PARAM_HTDIG_SORT_SCORE);
				parameter.addElement("Score");
				parameter.addElement(C_PARAM_HTDIG_SORT_DATE);
				parameter.addElement("Time");
				parameter.addElement(C_PARAM_HTDIG_SORT_TITLE);
				parameter.addElement("Title");
				parameter.addElement(C_PARAM_HTDIG_SORT_REV_SCORE);
				parameter.addElement("Reverse Score");
				parameter.addElement(C_PARAM_HTDIG_SORT_REV_DATE);
				parameter.addElement("Reverse Time");
				parameter.addElement(C_PARAM_HTDIG_SORT_REV_TITLE);
				parameter.addElement("Reverse Title");
			}
		}
		return parameter;
	}

	/**
	 * gets the caching information from the current template class.
	 *
	 * @param cms               CmsObject Object for accessing system resources
	 * @param templateFile      Filename of the template file
	 * @param elementName       Element name of this template in our parent template.
	 * @param parameters        Hashtable with all template class parameters.
	 * @param templateSelector  indicates which template section should be processed.
	 *
     * @return                  <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
	 */
	public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		// First build our own cache directives.
		return new CmsCacheDirectives(false);
	}

	/**
	 * This event-method is invoked if a user changes a parameter for a module in the
	 * module-administration. It reads the current parameters.
	 *
	 * @param cms               CmsObject Object for accessing system resources
	 * @exception CmsException  Throws Cms Exception.
	 */
	public static void moduleParameterWasUpdated(CmsObject cms) throws CmsException {
		// read the parameters from the module properties
		// System.out.println("HTDIG moduleParameterWasUpdated:: get Parameter");
		c_serverpath = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_SEARCH_MODULE_HTDIG, C_PARAM_SERVERPATH);
		// System.out.println("HTDIG moduleParameterWasUpdated:: c_serverpath: "+c_serverpath);
		c_select_format = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_SEARCH_MODULE_HTDIG, C_PARAM_HTDIG_FORMAT);
		// System.out.println("HTDIG moduleParameterWasUpdated:: c_select_format: "+c_select_format);
		c_select_method = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_SEARCH_MODULE_HTDIG, C_PARAM_HTDIG_METHOD);
		// System.out.println("HTDIG moduleParameterWasUpdated:: c_select_method: "+c_select_method);
		c_select_sort = OpenCms.getRegistry().getModuleParameterString(
			C_PARAM_SEARCH_MODULE_HTDIG, C_PARAM_HTDIG_SORT);
		//System.out.println("HTDIG moduleParameterWasUpdated:: c_select_sort: "+c_select_sort);
	}

}

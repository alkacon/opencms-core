package com.opencms.modules.search.htdig;

/**
 * This is the class for the searchengine Ht//dig. To work correct it is nessary to modify the result Templates
 * of Htdig. Look at the Dokumentation of the Module. The class passes the retrieval query to Ht://Dig
 * and analyses the result and sent it back to the called program. This is the Content Definition.
 *
 * Creation date: (17.11.00 10:22:45)
 * @author: Markus Fabritius
 */
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import java.io.*;
import java.net.*;
import java.util.*;

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

private static final String C_PARAM_MODULE_NAME = "com.opencms.modules.search.htdig.CmsHtdig";

private static final String C_PARAM_F_SHORT = "short";
private static final String C_PARAM_F_LONG = "long";

private static final String C_PARAM_M_AND = "and";
private static final String C_PARAM_M_OR = "or";
private static final String C_PARAM_M_BOOLEAN = "boolean";

private static final String C_PARAM_S_SCORE = "score";
private static final String C_PARAM_S_DATE = "date";
private static final String C_PARAM_S_TITLE = "title";
private static final String C_PARAM_S_REV_SCORE = "revscore";
private static final String C_PARAM_S_REV_DATE = "revdate";
private static final String C_PARAM_S_REV_TITLE = "revtitle";

private static final int C_PARAM_ADD = 5;
private static final int C_PARAM_BODY_SIZE = 5;
/**
 * These empty Constructorfor needs the method setParameter.
 *
 * Creation date: (05.12.00 15:36:31)
 */
public CmsHtdig() {}
/**
 * The Constructor used for the content definition (head ,one times).
 *
 * @param r_word String Object set the variable m_searchword with the searchword.
 * @param r_pages int set the variable m_pages with number of all pages for the match.
 * @param r_first int set the variable m_firstdisplay with with the first number of the current page.
 * @param r_last int set the variable m_lastdisplay with with the last number of the current page.
 * @param r_match int set the variable m_matches specification of all obtained hits.
 * Creation date: (17.11.00 10:25:31)
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
 * @param url String Object set the variable m_url with the url for the hit.
 * @param title String Object set the variable m_title with the titel the hit.
 * @param excerpt String Object set the variable m_excerpt with a small excerpt of the hit.
 * @param size int set the variable m_size with the size of the file.
 * @param percent int set the variable m_percent gives the wight of the hit.
 * Creation date: (17.11.00 10:25:31)
 */
public CmsHtdig(String url, String title, String excerpt, int size, int percent) {
	m_url = url;
	m_title = title;
	m_excerpt = excerpt;
	m_size = size;
	m_percent = percent;
}
/**
 * Get method for the excerpt.
 * Creation date: (17.11.00 10:25:31)
 */
public String getExcerpt() {
	return m_excerpt;
	}
/**
 * Get method for first number of the current page.
 * Creation date: (17.11.00 10:25:31)
 */
public int getFirstDisplay() {
	return m_firstdisplay;
	}
/**
 * Get method for last number of the current page.
 * Creation date: (17.11.00 10:25:31)
 */
public int getLastDisplay() {
	return m_lastdisplay;
	}
/**
 * Get method for the total hit of search.
 * Creation date: (17.11.00 10:25:31)
 */
public int getMatch() {
	return m_matches;
	}
/**
 * Get method for last modified Date of the file.
 * Creation date: (17.11.00 10:25:31)
 */
public String getModified() {
	return m_modified;
	}
/**
 * Get method for the total number of page.
 * Creation date: (17.11.00 10:25:31)
 */
public int getPages() {
	return m_pages;
	}
/**
 * Get method for the weight of hit in percent.
 * Creation date: (17.11.00 10:25:31)
 */
public int getPercentMatch() {
	return m_percent;
	}
/**
 * Get method for the current search word.
 * Creation date: (17.11.00 10:25:31)
 */
public String getSearchWord() {
	return m_searchword;
	}
/**
 * Get method for the size of the file.
 * Creation date: (17.11.00 10:25:31)
 */
public int getSize() {
	return m_size;
	}
/**
 * Get method for the title.
 * Creation date: (17.11.00 10:25:31)
 */
public String getTitle() {
	return m_title;
	}
/**
 * Get method for the Url.
 * Creation date: (17.11.00 10:25:31)
 */
public String getUrl() {
	return m_url;
	}
/**
 * This method build the interface between the searchengine Ht://Dig and the search form from opencms.
 * The method get the parameters form the search form and build a query String a send it to the
 * search program htsearch from Ht://Dig. The result from htsearch are evaluate and send back to the search form class
 * of opencms.

 * @param word  String Object for the searchword.
 * @param method  String Object indicates which logical method (AND, OR, BOOLEAN) used.
 * @param sort  String Object indicates which sort method used.
 * @param page  String Object indicates of the current page.
 * @param conf  String Object indicates of the configuration file which should used.
 * @param restrict  String Object indicates of the restrict search.
 * @param setServerpath Object indicates ther serverpath of the registry
 * @param matchPage  int indicates of matches per page should shown.
 * @param cms CmsObject Object for accessing system resources.
 * Creation date: (17.11.00 10:25:31)
 */
public static Vector read(String word, String method, String sort, String page, String conf, String restrict, String setServerpath, Integer matchPage, CmsObject cms) throws Exception {
	// Variable declaration
	int x = 0, i = 0;
	int itemp1, itemp2, itemp3, itemp4, itemp5, setMatch;
	String query, temp1, temp2, temp3, temp4;
	Vector result = new Vector();
	CmsHtdig cdh;
	// get value of match per page of the search form
	setMatch = (matchPage).intValue();
	// set the size of output array
	String[] output = new String[ ((setMatch * C_PARAM_BODY_SIZE) + C_PARAM_ADD)];
	// check the rectrict area
	if (restrict.equals("restricttoall"))
		restrict = "";

	// check the config file
	if (conf.equals("contentDefinitionSearchengineConfiguration"))
		conf = "htdig";

	// check for first time to start the page
	if (page == null)
		query = "restrict=" + restrict + "&config=" + conf + "&method=" + method + "&format=long&sort=" + sort + "&matchesperpage=" + setMatch + "&words=" + word;
	else
		query = "restrict=" + restrict + "&config=" + conf + "&method=" + method + "&format=long&sort=" + sort + "&matchesperpage=" + setMatch + "&words=" + word + "&page=" + page;
	try {
		// Url connection to the search program htsearch
		URL url = new URL(setServerpath);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		OutputStream connectionOutput = connection.getOutputStream();
		Writer writer = new OutputStreamWriter(connectionOutput, "latin1");
		writer.write(query);
		writer.close();
		InputStream in = connection.getInputStream();
		Reader reader = new InputStreamReader(in, "latin1");
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = new String();
		while ((line = bufferedReader.readLine()) != null) {
			output[i] = line;
			i++;
		}
		bufferedReader.close();
	} catch (IOException e) {
		System.err.println(e.toString());
	}
	// nomatch or syntax Error
	if (i <= 1) {
		if (output[0].equals("syntax"))
			result.addElement(output[0]);
		else {
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
		for (x = C_PARAM_ADD; x < i; x += C_PARAM_BODY_SIZE) {
			temp1 = output[x];
			temp2 = output[x + 1];
			temp3 = output[x + 2];
			itemp1 = Integer.parseInt(output[x + 3]);
			itemp2 = Integer.parseInt(output[x + 4]);
			cdh = new CmsHtdig(temp1, temp2, temp3, itemp1, itemp2);
			result.addElement(cdh);
		}
	}
	return result;
}
/**
 * The method fills the selectbox of the search form. The first value is the value of the selectbox,
 * the second is the name.
 *
 * @param selection String Object indicates which selectbox should be filled.
 * Creation date: (27.11.00 10:55:32)
 */
public static Vector setParameter(String selection, CmsObject cms) throws Exception {
	Vector parameter = new Vector();
	String [] temp = new String [6];
        int i = 0;
        String moduleName = C_PARAM_MODULE_NAME;
        String select_method = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "method");
        String select_format = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "format");
        String select_sort = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "sort");
        if (selection.equals("method")) {
                if((select_method != "") && (select_method != null)){
                  StringTokenizer st = new StringTokenizer(select_method, ";");
                  try{
                    while(st.hasMoreTokens() && i<3){
                      temp[i] = st.nextToken();
                      i++;
                    }
                    parameter.addElement(C_PARAM_M_AND);
		    parameter.addElement(temp[0]);
		    parameter.addElement(C_PARAM_M_OR);
		    parameter.addElement(temp[1]);
		    parameter.addElement(C_PARAM_M_BOOLEAN);
		    parameter.addElement(temp[2]);
                  }catch (Exception e){
                    System.err.println(e.toString());
                  }
                }else {
                  parameter.addElement(C_PARAM_M_AND);
		  parameter.addElement("All");
		  parameter.addElement(C_PARAM_M_OR);
		  parameter.addElement("Any");
		  parameter.addElement(C_PARAM_M_BOOLEAN);
		  parameter.addElement("Boolean");
	        }
        } else
		if (selection.equals("format")) {
		  if((select_format != "") && (select_format != null)){
                  StringTokenizer st = new StringTokenizer(select_format, ";");
                  try{
                    while(st.hasMoreTokens() && i<2){
                      temp[i] = st.nextToken();
                      i++;
                    }
                    parameter.addElement(C_PARAM_F_LONG);
		    parameter.addElement(temp[0]);
		    parameter.addElement(C_PARAM_F_SHORT);
		    parameter.addElement(temp[1]);
		  }catch (Exception e){
                    System.err.println(e.toString());
                  }
                  }else {
                  parameter.addElement(C_PARAM_F_LONG);
		  parameter.addElement("Long");
		  parameter.addElement(C_PARAM_F_SHORT);
		  parameter.addElement("Short");
		}
                } else
			if (selection.equals("sort")) {
			  if((select_sort != "") && (select_sort != null)){
                            StringTokenizer st = new StringTokenizer(select_sort,";");
                            try{
                              while(st.hasMoreTokens() && i<6){
                                  temp[i] = st.nextToken();
                                  i++;
                              }
                              parameter.addElement(C_PARAM_S_SCORE);
		              parameter.addElement(temp[0]);
		              parameter.addElement(C_PARAM_S_DATE);
		              parameter.addElement(temp[1]);
		              parameter.addElement(C_PARAM_S_TITLE);
		              parameter.addElement(temp[2]);
                              parameter.addElement(C_PARAM_S_REV_SCORE);
		              parameter.addElement(temp[3]);
                              parameter.addElement(C_PARAM_S_REV_DATE);
		              parameter.addElement(temp[4]);
                              parameter.addElement(C_PARAM_S_REV_TITLE);
		              parameter.addElement(temp[5]);
                            }catch (Exception e){
                                System.err.println(e.toString());
                            }
                          }else {
                                parameter.addElement(C_PARAM_S_SCORE);
				parameter.addElement("Score");
				parameter.addElement(C_PARAM_S_DATE);
				parameter.addElement("Time");
				parameter.addElement(C_PARAM_S_TITLE);
				parameter.addElement("Title");
				parameter.addElement(C_PARAM_S_REV_SCORE);
				parameter.addElement("Reverse Score");
				parameter.addElement(C_PARAM_S_REV_DATE);
				parameter.addElement("Reverse Time");
				parameter.addElement(C_PARAM_S_REV_TITLE);
				parameter.addElement("Reverse Title");
			}
                        }
	return parameter;
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

}

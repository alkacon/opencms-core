package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: ControlLucene.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/20 11:06:09 $
 *  $Revision: 1.2 $
 *
 *  Copyright (c) 2002 FRAMFAB Deutschland AG. All Rights Reserved.
 *
 *  THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 *  To use this software you must purchease a licencse from Framfab.
 *  In order to use this source code, you need written permission from
 *  Framfab. Redistribution of this source code, in modified or
 *  unmodified form, is not allowed.
 *
 *  FRAMFAB MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 *  OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *  TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 *  PURPOSE, OR NON-INFRINGEMENT. FRAMFAB SHALL NOT BE LIABLE FOR ANY
 *  DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 *  DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
import java.util.*;
import java.io.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.boot.*;
import javax.servlet.http.*;

/**
 *  Description of the Class
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class ControlLucene implements com.opencms.core.I_CmsConstants, com.opencms.modules.search.form.I_CmsSearchEngine {
    // the debug flag
    private final static boolean debug = false;
    private static boolean mainStart = false;
    private String m_query;
    private static String m_searchword = "Jar besitzt verschiedene Optionen um Archive zu erzeugen, sie auszupacken nd anzuschauen.";
    private static String m_url = CmsBase.getAbsoluteWebPath("lucene/index");
    /**
     *  Description of the Field
     */
    private static String m_configPath = CmsBase.getAbsolutePath("config");
    private String m_link = "";
    private String m_title;
    private String m_excerpt;
    private String m_modified;
    private int m_percent;
    private int m_size;
    private static int m_pages;
    private int m_matches;
    private int m_firstdisplay;
    private int m_lastdisplay;
    private int m_urlpage;

    private static String c_select_format;

    private final static String C_PARAM_MODULE_NAME = "com.opencms.modules.search.lucene.ControlLucene";
    public static final String C_PARAM_FORMAT_SHORT = "short";
    public static final String C_PARAM_FORMAT_LONG = "long";

    private final static String C_PARAM_METHOD_AND = "AND";
    private final static String C_PARAM_METHOD_OR = "OR";
    private final static String C_PARAM_METHOD_BOOLEAN = "boolean";

    private final static String C_PARAM_SORT_SCORE = "score";
    private final static String C_PARAM_SORT_DATE = "date";
    private final static String C_PARAM_SORT_TITLE = "title";
    private final static String C_PARAM_SORT_REV_SCORE = "revscore";
    private final static String C_PARAM_SORT_REV_DATE = "revdate";
    private final static String C_PARAM_SORT_REV_TITLE = "revtitle";
    public static final String C_PARAM_SEARCHENGINE_TOKEN = ",";

    private static Vector C_INDEX_FILES = null;


    /**
     *  Constructor for the ControlLucene object
     */
    public ControlLucene() { }


    /**
     *  Constructor for the ControlLucene object
     *
     *@param  word   Description of the Parameter
     *@param  pages  Description of the Parameter
     *@param  first  Description of the Parameter
     *@param  last   Description of the Parameter
     *@param  match  Description of the Parameter
     */
    public ControlLucene(String word, int pages, int first, int last, int match) {
        m_searchword = word;
        m_pages = pages;
        m_firstdisplay = first;
        m_lastdisplay = last;
        m_matches = match;
    }


    /**
     *  Constructor for the ControlLucene object
     *
     *@param  url      Description of the Parameter
     *@param  title    Description of the Parameter
     *@param  excerpt  Description of the Parameter
     *@param  size     Description of the Parameter
     *@param  percent  Description of the Parameter
     */
    public ControlLucene(String url, String title, String excerpt, int size, int percent) {
        m_link = url;
        m_title = title;
        m_excerpt = excerpt;
        m_size = size;
        m_percent = percent;
    }


    /**
     *  Get method for the excerpt. Creation date: (06.02.02 10:25:31)
     *
     *@return    The excerpt value
     */
    public String getExcerpt() {
        return m_excerpt;
    }


    /**
     *  Get method for first number of the current page. Creation date:
     *  (06.02.02 10:25:31)
     *
     *@return    The firstDisplay value
     */
    public int getFirstDisplay() {
        return m_firstdisplay;
    }


    /**
     *  Get method for last number of the current page. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The lastDisplay value
     */
    public int getLastDisplay() {
        return m_lastdisplay;
    }


    /**
     *  Get method for the total hit of search. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The match value
     */
    public int getMatch() {
        return m_matches;
    }


    /**
     *  Get method for last modified Date of the file. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The modified value
     */
    public String getModified() {
        return m_modified;
    }


    /**
     *  Get method for the total number of page. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The pages value
     */
    public int getPages() {
        return m_pages;
    }


    /**
     *  Get method for the weight of hit in percent. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The percentMatch value
     */
    public int getPercentMatch() {
        return m_percent;
    }


    /**
     *  Get method for the current search word. Creation date: (06.02.02
     *  10:25:31)
     *
     *@return    The searchWord value
     */
    public String getSearchWord() {
        return m_searchword;
    }


    /**
     *  Get method for the size of the file. Creation date: (06.02.02 10:25:31)
     *
     *@return    The size value
     */
    public int getSize() {
        return m_size;
    }


    /**
     *  Get method for the title. Creation date: (06.02.02 10:25:31)
     *
     *@return    The title value
     */
    public String getTitle() {
        return m_title;
    }


    /**
     *  Get method for the Url. Creation date: (06.02.02 10:25:31)
     *
     *@return    The url value
     */
    public String getUrl() {
        return m_link;
    }


    /**
     *  Starts the creation of an index for evere file in the vector vfiles
     *  which contains ".htm" in its link. If the index-directory does not exist
     *  it is created.
     *
     *@param  cms     Description of the Parameter
     *@param  path    Description of the Parameter
     *@param  vfiles  Description of the Parameter
     */
    public static void createIndex(CmsObject cms, String path, Vector vfiles) {
        Vector files = new Vector();
        String link = "";
        StringBuffer bl = null;

        if (mainStart){
            String linkPrefix = "";
            for(int i = 0; i < vfiles.size(); i++) {
                link = (String) vfiles.elementAt(i);
                bl = new StringBuffer(link);
                if(debug) System.out.println("link=" + link);
                if(link.toLowerCase().indexOf(".htm") != -1 || link.toLowerCase().indexOf(".pdf") != -1) {
                    files.addElement(link);
                    if(debug) System.out.println("wird indiziert");
                }
            }
            m_configPath="D:/Programme/Apache Group/jakarta-tomcat-4.0/webapps/opencms2/WEB-INF/config";
            path ="D:/Programme/Apache Group/jakarta-tomcat-4.0/webapps/opencms2/lucene/index";
        } else {
            CmsRequestContext req = cms.getRequestContext();
            String linkPrefix = req.getRequest().getScheme() + "://"
                     + req.getRequest().getServerName() + ":" + req.getRequest().getServerPort();

            for(int i = 0; i < vfiles.size(); i++) {
                link = (String) vfiles.elementAt(i);
                bl = new StringBuffer(link);
                if(debug) {
                    System.out.println("link=" + link);
                }

                //every html-page should contain a ".htm"
                if(!link.startsWith(cms.getRequestContext().getRequest().getServletUrl())
                         && (link.indexOf(".htm") != -1 || link.indexOf(".pdf") != -1)) {
                    if(link.startsWith("/")) {
                        files.addElement(linkPrefix + link);
                    }
                    if(link.startsWith("http")) {
                        files.addElement(link);
                    }
                    if(debug) {
                        System.out.println("wird indiziert");
                    }
                }
            }
        }
        try {
            if(files.size() > 0) {
                if(debug) {
                    System.out.println("thread gestartet");
                }
                IndexFiles indexfiles = new IndexFiles(path, files, m_configPath);
                indexfiles.start();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    public void createIndexDirectory() throws Exception {
        if (mainStart) m_url ="D:/Programme/Apache Group/jakarta-tomcat-4.0/webapps/opencms2/lucene/index";
        IndexDirectory.createIndexDirectory(m_url);
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    public void searchIndex() throws Exception {
        SearchLucene s = new SearchLucene();
        if (mainStart) m_url ="D:/Programme/Apache Group/jakarta-tomcat-4.0/webapps/opencms2/lucene/index";
        showSearchResults(s.performSearch(m_url, m_searchword));
    }


    /**
     *  only for the debug-mode
     *
     *@param  results  Description of the Parameter
     */
    public static void showSearchResults(Vector results) {
        Hashtable oneHit = null;
        int matches = results.size();
        if(matches != 0) {
            System.out.println(matches + " Treffer  für: " + m_searchword);
        } else {
            System.out.println("Keine Treffer  für: " + m_searchword);
        }
        for(int i = 0; i < matches; i++) {
            oneHit = (Hashtable) results.elementAt(i);
            System.out.println("description=" + oneHit.get("description"));
            System.out.println("title=" + oneHit.get("title"));
            System.out.println("keywords=" + oneHit.get("keywords"));
            System.out.println("URL=" + oneHit.get("url"));
            System.out.println("score=" + oneHit.get("score"));
        }
    }


    /**
     *  This method build the interface between the searchengine Lucene and the
     *  search form from opencms. The method get the parameters form the search
     *  form and build a query String a send it to the search program htsearch
     *  from Lucene. The result from htsearch are evaluate and send back to the
     *  search form class of opencms.
     *
     *@param  word           String Object for the searchword.
     *@param  method         String Object indicates which logical method (AND,
     *      OR, BOOLEAN) used.
     *@param  sort           String Object indicates which sort method used.
     *@param  page           String Object indicates of the current page.
     *@param  conf           String Object indicates of the configuration file
     *      which should used.
     *@param  restrict       String Object indicates of the restrict search.
     *@param  matchPage      int indicates of matches per page should shown.
     *@param  cms            CmsObject Object for accessing system resources.
     *      Creation date: (06.02.02 10:25:31)
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public static Vector read(String word, String method, String sort,
            String page, String conf, String restrict,
            String matchPage, CmsObject cms) throws Exception {
        if(debug) {
            System.out.println("word=" + word + " method=" + method + " sort=" + sort +
                    " page=" + page + " conf=" + conf + " restrict=" + restrict + " matchPage=" + matchPage + " cms=" + cms);
        }

        if (conf.equals("default"))		conf = "Lucene";
        // Variable declaration
        int x = 0;
        // Variable declaration
        int i = 0;
        int size;
        int score;
        int last;
        int matches;
        int first;
        int setMatch;
        int pageNr;
        String url;
        String title;
        String description;
        m_searchword = word;

        //start the search for m_searchword
        SearchLucene s = new SearchLucene();
        Vector results = s.performSearch(m_url, m_searchword);

        matches = results.size();

        Vector result = new Vector();
        ControlLucene cdh;
        // get value of match per page of the search form
        setMatch = Integer.valueOf(matchPage).intValue();
        if(debug) {
            System.out.println("setMatch=" + setMatch);
        }

        // nomatch or syntax Error
        if(matches < 1) {
            String nomatch = "nomatch";
            result.addElement(nomatch);
        } else {
            // fill head of content definition
            if(page == null) {
                pageNr = 1;
            } else {
                pageNr = Integer.valueOf(page).intValue();
            }
            first = (pageNr - 1) * setMatch + 1;
            last = setMatch * pageNr;
            if(matches < last) {
                last = matches;
            }
            cdh = new ControlLucene(m_searchword, matches / setMatch + 1, first, last, matches);
            result.addElement(cdh);
            Hashtable oneHit = null;
            if(debug) {
                showSearchResults(results);
            }
            for(int j = first - 1; j <= last - 1; j++) {
                oneHit = (Hashtable) results.elementAt(j);
                url = (String) oneHit.get("url");
                title = (String) oneHit.get("title");
                description = (String) oneHit.get("description");
                size = 77;
                score = Integer.valueOf((((String) oneHit.get("score")).substring(0, ((String) oneHit.get("score")).length() - 2)).trim()).intValue();
                cdh = new ControlLucene(url, title, description, size, score);
                result.addElement(cdh);
            }
        }
        return result;
    }


    /**
     *  The main program for the ControlLucene class
     *
     *@param  args           The command line arguments
     *@exception  Exception  Description of the Exception
     */
    public static void main(String[] args) throws Exception {
        mainStart=true;
        ControlLucene cl = new ControlLucene();
        C_INDEX_FILES = new Vector();
        //C_INDEX_FILES.add("http://localhost/lucineTest/Thinking_in_Java.pdf");
        //C_INDEX_FILES.add("http://localhost/lucineTest/Urlaubsantrag3_02.pdf");
        //C_INDEX_FILES.add("http://localhost/lucineTest/OpenCMS_de.pdf");
        //C_INDEX_FILES.add("http://localhost/lucineTest/test2.pdf");
        //C_INDEX_FILES.add("http://localhost/lucineTest/Java_ist_auch_eine_Insel_20010426.PDF");
        //C_INDEX_FILES.add("http://localhost/lucineTest/XXVI.pdf");
        //C_INDEX_FILES.add("http://localhost/lucineTest/OpenCmsDoc300102.pdf");

        //C_INDEX_FILES.add("http://www.dkv.com/ernaehrung_naehrstoffe.phtml?typ=content");
        //C_INDEX_FILES.add("http://www.dkv.com/medizin_krankheitsbild.phtml?typ=content");
        C_INDEX_FILES.add("http://localhost:8080/opencms2/export/testseiten/warmwasser.html");
        //C_INDEX_FILES.add("http://intranet.ff.de/framfab/opencms/framfab/kantine/index.html");
        /*
         *  C_INDEX_FILES.add("http://www.dkv.com/gesundheit_gesundheitsserie.phtml?typ=content");
         *  C_INDEX_FILES.add("http://www.dkv.com/versicherungsschutz.phtml?typ=content");
         *  C_INDEX_FILES.add("http://www.dkv.com/gesundheitsserie_alkohol.phtml?typ=content");
         */
        /*
         *  "http://intranet.ff.de/framfab/opencms/bla.html"
         */
        cl.publishLinks(new CmsObject(), C_INDEX_FILES);
        //cl.searchIndex();
        //cl.createIndexDirectory();
    }


    /**
     *  The method fills the selectbox of the search form. The first value is
     *  the value of the selectbox, the second is the name.
     *
     *@param  selection      String Object indicates which selectbox should be
     *      filled. Creation date: (27.11.00 10:55:32)
     *@param  cms            The new parameter value
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public static Vector setParameter(String selection, CmsObject cms) throws Exception {
        Vector parameter = new Vector();
        String[] temp = new String[6];
        int i = 0;
        String moduleName = C_PARAM_MODULE_NAME;
        String select_method = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "method");
        String select_format = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "format");
        String select_sort = cms.getRegistry().getModuleParameter(moduleName.substring(0, moduleName.lastIndexOf(".")), "sort");
        if(debug) {
            System.out.println("select_method =" + select_method + " select_format =" + select_format + " select_sort =" + select_sort);
        }
        if(selection.equals("method")) {
            if((select_method != "") && (select_method != null)) {
                StringTokenizer st = new StringTokenizer(select_method, ";");
                try {
                    while(st.hasMoreTokens() && i < 3) {
                        temp[i] = st.nextToken();
                        i++;
                    }
                    parameter.addElement(C_PARAM_METHOD_AND);
                    parameter.addElement(temp[0]);
                    parameter.addElement(C_PARAM_METHOD_OR);
                    parameter.addElement(temp[1]);
                    parameter.addElement(C_PARAM_METHOD_BOOLEAN);
                    parameter.addElement(temp[2]);
                } catch(Exception e) {
                    System.err.println(e.toString());
                }
            } else {
                parameter.addElement(C_PARAM_METHOD_AND);
                parameter.addElement("All");
                parameter.addElement(C_PARAM_METHOD_OR);
                parameter.addElement("Any");
                parameter.addElement(C_PARAM_METHOD_BOOLEAN);
                parameter.addElement("Boolean");
            }
        }
        if(selection.equals("format")) {
            if((c_select_format != "") && (c_select_format != null)) {
                StringTokenizer st = new StringTokenizer(c_select_format, C_PARAM_SEARCHENGINE_TOKEN);
                try {
                    while(st.hasMoreTokens() && i < 2) {
                        temp[i] = st.nextToken();
                        i++;
                    }
                    parameter.addElement(C_PARAM_FORMAT_LONG);
                    parameter.addElement(temp[0]);
                    parameter.addElement(C_PARAM_FORMAT_SHORT);
                    parameter.addElement(temp[1]);
                } catch(Exception e) {
                    System.err.println(e.toString());
                }
            } else {
                parameter.addElement(C_PARAM_FORMAT_LONG);
                parameter.addElement("Long");
                parameter.addElement(C_PARAM_FORMAT_SHORT);
                parameter.addElement("Short");
            }
        } else
                if(selection.equals("sort")) {
            if((select_sort != "") && (select_sort != null)) {
                StringTokenizer st = new StringTokenizer(select_sort, ";");
                try {
                    while(st.hasMoreTokens() && i < 6) {
                        temp[i] = st.nextToken();
                        i++;
                    }
                    parameter.addElement(C_PARAM_SORT_SCORE);
                    parameter.addElement(temp[0]);
                    parameter.addElement(C_PARAM_SORT_DATE);
                    parameter.addElement(temp[1]);
                    parameter.addElement(C_PARAM_SORT_TITLE);
                    parameter.addElement(temp[2]);
                    parameter.addElement(C_PARAM_SORT_REV_SCORE);
                    parameter.addElement(temp[3]);
                    parameter.addElement(C_PARAM_SORT_REV_DATE);
                    parameter.addElement(temp[4]);
                    parameter.addElement(C_PARAM_SORT_REV_TITLE);
                    parameter.addElement(temp[5]);
                } catch(Exception e) {
                    System.err.println(e.toString());
                }
            } else {
                parameter.addElement(C_PARAM_SORT_SCORE);
                parameter.addElement("Score");
                parameter.addElement(C_PARAM_SORT_DATE);
                parameter.addElement("Time");
                parameter.addElement(C_PARAM_SORT_TITLE);
                parameter.addElement("Title");
                parameter.addElement(C_PARAM_SORT_REV_SCORE);
                parameter.addElement("Reverse Score");
                parameter.addElement(C_PARAM_SORT_REV_DATE);
                parameter.addElement("Reverse Time");
                parameter.addElement(C_PARAM_SORT_REV_TITLE);
                parameter.addElement("Reverse Title");
            }
        }
        return parameter;
    }


    /**
     *  gets the caching information from the current template class.
     *
     *@param  cms               CmsObject Object for accessing system resources
     *@param  templateFile      Filename of the template file
     *@param  elementName       Element name of this template in our parent
     *      template.
     *@param  parameters        Hashtable with all template class parameters.
     *@param  templateSelector  template section that should be processed.
     *@return                   <EM>true</EM> if this class may stream it's
     *      results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile,
            String elementName, Hashtable parameters, String templateSelector) {
        // First build our own cache directives.
        return new CmsCacheDirectives(false);
    }


    /**
     *  Description of the Method
     *
     *@param  cms               Description of the Parameter
     *@param  changedResources  Description of the Parameter
     */
    public static void publishLinks(CmsObject cms, Vector changedResources) {
        createIndex(cms, m_url, changedResources);
    }

}

package com.opencms.modules.search.lucene;

/*
    $RCSfile: ControlLucene.java,v $
    $Date: 2003/03/25 14:48:29 $
    $Revision: 1.10 $
    Copyright (C) 2000  The OpenCms Group
    This File is part of OpenCms -
    the Open Source Content Mananagement System
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    For further information about OpenCms, please see the
    OpenCms Website: http://www.opencms.com
    You should have received a copy of the GNU General Public License
    long with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
import java.util.*;
import java.io.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.boot.*;
import javax.servlet.http.*;
import java.text.*;

/**
 *  The Control class to cennect OpenCms to the fulltext searchengine Lucene.
 *  All links to the by the static export from opencms exported files are passed
 *  as vector to the method publishLinks(). For all html- and pdf-files which
 *  are inclosed to this vector the IndexFiles-method createIndexFiles() is
 *  started. The parameter newIndex in the modul-configuration of openCms
 *  determines if a new index is created (true) or if an existing index is
 *  expanded (default). The indexing can be deactivated by setting the parameter
 *  active on false.
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class ControlLucene implements com.opencms.core.I_CmsConstants,
        com.opencms.modules.search.form.I_CmsSearchEngine {
    //the name of the thread
    protected final static String C_INDEXING = "indexing Lucene";

    //the names of the modul-properties
    protected final static String C_ACTIVE = "active";
    protected final static String C_INDEXPDFS = "indexPDFs";

    private final static String C_NOINDEXSTART = "noIndexStartstring";
    private final static String C_NOINDEXEND = "noIndexEndstring";


    // the debug flag
    private final static boolean debug = false;

    // for start as standalone application
    private static boolean mainStart = false;

    // the switch to stop or start indexing
    private static boolean m_active = false;

    private static String m_noIndexStart = "<!--htdig_noindex-->";
    private static String m_noIndexEnd = "<!--/htdig_noindex-->";

    // the switch to stop or start indexing all exported files and not only
    // the changed files
    private static boolean m_indexAll = false;

    // the switch to stop or start indexing
    private static String m_analyzer = "stopAnalyzer";

    private static DateFormat m_format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    // the switch to stop or start indexing all exported files and not only
    // the changed files
    private static boolean m_indexPDFs = false;

    // the switch to create a completely ne indexdirectory
    private static boolean m_newIndex = false;

    private static String m_searchword = "K*";
    private static String m_url = CmsBase.getAbsoluteWebPath("search/content");
    /**
     *  Description of the Field
     */
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

    private final static String C_PARAM_MODULE_NAME = "com.opencms.modules.search.lucene";
    /**
     *  Description of the Field
     */
    private final static String C_PARAM_FORMAT_SHORT = "short";
    /**
     *  Description of the Field
     */
    private final static String C_PARAM_FORMAT_LONG = "long";

    protected final static String C_PARAM_METHOD_AND = "AND";
    //private final static String C_PARAM_METHOD_OR = "OR";
    protected final static String C_PARAM_METHOD_BOOLEAN = "boolean";

    private final static String C_PARAM_SORT_SCORE = "score";
    private final static String C_PARAM_SORT_DATE = "date";
    private final static String C_PARAM_SORT_TITLE = "title";
    private final static String C_PARAM_SORT_REV_SCORE = "revscore";
    private final static String C_PARAM_SORT_REV_DATE = "revdate";
    private final static String C_PARAM_SORT_REV_TITLE = "revtitle";
    /**
     *  Description of the Field
     */

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
     *@param  url       Description of the Parameter
     *@param  title     Description of the Parameter
     *@param  excerpt   Description of the Parameter
     *@param  size      Description of the Parameter
     *@param  percent   Description of the Parameter
     *@param  modified  Description of the Parameter
     */
    public ControlLucene(String url, String title, String excerpt, int size, int percent, String modified) {
        m_link = url;
        m_title = title;
        m_excerpt = excerpt;
        m_size = size;
        m_percent = percent;
        m_modified = modified;
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
        if(m_title.length() == 0) {
            return m_link.substring(m_link.lastIndexOf("/") + 1);
        }
        return m_title;
    }


    /**
     *  Gets the noIndexStart attribute of the ControlLucene class
     *
     *@return    The noIndexStart value
     */
    protected static String getNoIndexStart() {
        return m_noIndexStart;
    }


    /**
     *  Gets the noIndexEnd attribute of the ControlLucene class
     *
     *@return    The noIndexEnd value
     */
    protected static String getNoIndexEnd() {
        return m_noIndexEnd;
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
    protected static void createIndex(CmsObject cms, String path, Vector vfiles) {
        Vector files = new Vector();
        String link = "";
        StringBuffer bl = null;
        //for a start by the main-method
        if(mainStart) {
            String linkPrefix = "";
            for(int i = 0; i < vfiles.size(); i++) {
                link = (String) vfiles.elementAt(i);
                bl = new StringBuffer(link);
                if(debug) {
                    System.err.println("link=" + link);
                }
                if(link.toLowerCase().indexOf(".htm") != -1 ||
                        link.toLowerCase().indexOf(".pdf") != -1) {
                    files.addElement(link);
                    if(debug) {
                        System.err.println("wird indiziert");
                    }
                }
            }
            path = "C:/Programme/Apache Tomcat 4.0/webapps/opencms474/" + m_url;
        }
        //for a start of indexing by opencms
        else {
            CmsRequestContext req = cms.getRequestContext();
            String linkPrefix = req.getRequest().getScheme() + "://"
                     + req.getRequest().getServerName() + ":"
                     + req.getRequest().getServerPort();
            for(int i = 0; i < vfiles.size(); i++) {
                link = (String) vfiles.elementAt(i);
                bl = new StringBuffer(link);

                if(debug) {
                    System.err.println("link=" + link);
                }
                //the application name must be added to the exported links
                if(m_indexAll) {
                    link = cms.getLinkSubstitution(link);
                }

                if(debug) {
                    System.err.println("link=" + link);
                }

                //every link should contain a ".htm" or ".pdf" (if property m_indexPDFs is true)
                if((!link.startsWith(cms.getRequestContext().getRequest().getServletUrl()) || m_indexAll)
                         && (link.indexOf(".htm") != -1
                         || (m_indexPDFs && link.indexOf(".pdf") != -1))) {
                    if(link.startsWith("/")) {
                        files.addElement(linkPrefix + link);
                    }
                    if(link.startsWith("http")) {
                        files.addElement(link);
                    }
                    if(debug) {
                        System.err.println("wird indiziert");
                    }
                }
            }
        }
        try {
            if(files.size() > 0) {
                IndexFiles indexfiles = new IndexFiles(path, files, m_analyzer);
                indexfiles.setNewIndex(m_newIndex);
                indexfiles.setPriority(Thread.MIN_PRIORITY);
                indexfiles.start();
                indexfiles.setName(C_INDEXING);
                if(debug) {
                    System.err.println("thread gestartet");
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  To create a new IndexDirectory.
     *
     */
    protected static void createIndexDirectory() {
        if(mainStart) {
            m_url = "C:/Programme/Apache Tomcat 4.0/webapps/opencms4714/" + m_url;
        }
        IndexDirectory.createIndexDirectory(m_url);
    }


    /**
     *  To start a search in the existing index.
     *
     *@exception  Exception  Description of the Exception
     */
    protected void searchIndex() throws Exception {
        SearchLucene s = new SearchLucene();
        if(mainStart) {
            m_url = "C:/Programme/Apache Tomcat 4.0/webapps/opencms4714/" + m_url;
        }
        if(m_active) {
            showSearchResults(s.performSearch(m_url, m_searchword, m_analyzer, "any"));
        }
    }


    /**
     *  show results only for the debug-mode
     *
     *@param  results  Description of the Parameter
     */
    private static void showSearchResults(Vector results) {
        Hashtable oneHit = null;
        int matches = results.size();
        if(matches != 0) {
            System.err.println(matches + " Treffer  für: " + m_searchword);
        } else {
            System.err.println("Keine Treffer  für: " + m_searchword);
        }
        for(int i = 0; i < matches; i++) {
            oneHit = (Hashtable) results.elementAt(i);
            System.err.println("excerpt=" + oneHit.get("excerpt"));
            System.err.println("description=" + oneHit.get("description"));
            System.err.println("title=" + oneHit.get("title"));
            System.err.println("keywords=" + oneHit.get("keywords"));
            System.err.println("URL=" + oneHit.get("url"));
            System.err.println("score=" + oneHit.get("score"));
            System.err.println("size=" + (Integer.valueOf((String) oneHit.get("length")).intValue()) / 1024 + 1);
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
            System.err.println("word=" + word + " method=" + method + " sort=" + sort +
                    " page=" + page + " conf=" + conf + " restrict=" + restrict + " matchPage=" + matchPage + " cms=" + cms);
        }

        if(conf.equals("default")) {
            conf = "Lucene";
        }
        if(sort == null) {
            sort = "nolimit";
        }

        if(method == null) {
            method = C_PARAM_METHOD_BOOLEAN;
        }
        // Variable declaration
        int x = 0;
        // Variable declaration
        int i = 0;
        int size;
        int score;
        int last;
        int matches = 0;
        int first;
        int setMatch;
        int pageNr;
        String url;
        String title;
        String description;
        String modified;
        long lmodified;
        String excerpt = "";
        m_searchword = word;
        int from = 0;

        //start the search for m_searchword
        SearchLucene search = new SearchLucene();
        if(!sort.equalsIgnoreCase("nolimit")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("ECT"));
            if(sort.equalsIgnoreCase("hour")) {
                cal.roll(Calendar.HOUR, -1);
            } else if(sort.equalsIgnoreCase("day")) {
                cal.roll(Calendar.DAY_OF_YEAR, -1);
            } else if(sort.equalsIgnoreCase("week")) {
                cal.roll(Calendar.DAY_OF_YEAR, -7);
            } else if(sort.equalsIgnoreCase("month")) {
                cal.roll(Calendar.MONTH, -1);
            } else if(sort.equalsIgnoreCase("year")) {
                cal.roll(Calendar.YEAR, -1);
            }
            search.setFrom(cal.getTime().getTime());
            if(debug) {
                System.err.println("cal=" + m_format.format(new Date(cal.getTime().getTime())));
            }
        }

        Vector results = search.performSearch(m_url, m_searchword, m_analyzer, method);

        if(results != null) {
            matches = results.size();
        }

        Vector result = new Vector();
        ControlLucene cdh;
        // get value of match per page of the search form
        setMatch = Integer.valueOf(matchPage).intValue();
        if(debug) {
            System.err.println("setMatch=" + setMatch);
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
                excerpt = (String) oneHit.get("excerpt");
                lmodified = ((Long) oneHit.get("modified")).longValue();
                modified = m_format.format(new Date(lmodified));
                size = Integer.valueOf((String) oneHit.get("length")).intValue();
                if(size != 0) {
                    size = size / 1024;
                }
                //set size to at least 1 KByte
                if(size == 0) {
                    size = 1;
                }
                score = ((Integer) oneHit.get("score")).intValue();
                cdh = new ControlLucene(url, title, excerpt, size, score, modified);
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
        mainStart = true;
        m_active = true;
        m_newIndex = true;
        m_url = "search/content";
        ControlLucene cl = new ControlLucene();
        C_INDEX_FILES = new Vector();

        C_INDEX_FILES.add("http://www.erzbistum-koeln.de/opencms/opencms/erzbistum/index.html");
        C_INDEX_FILES.add("http://www.erzbistum-koeln.de/opencms/opencms/index.html");

        if(args.length != 0) {
            if(args[0].equalsIgnoreCase("search")) {
                cl.searchIndex();
            } else if(args[0].equalsIgnoreCase("index")) {
                cl.createIndex(new CmsObject(), m_url, C_INDEX_FILES);
            } else if(args[0].equalsIgnoreCase("dir")) {
                cl.createIndexDirectory();
            }
        }
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

        String select_method = cms.getRegistry().getModuleParameter(C_PARAM_MODULE_NAME, "method");
        String select_format = cms.getRegistry().getModuleParameter(C_PARAM_MODULE_NAME, "format");
        String select_sort = cms.getRegistry().getModuleParameter(C_PARAM_MODULE_NAME, "sort");
        if(debug) {
            System.err.println("selection=" + selection);
            System.err.println("select_method =" + select_method + " select_format =" + select_format + " select_sort =" + select_sort);
        }
        if(selection.equals("method")) {
            if((select_method != "") && (select_method != null)) {
                StringTokenizer st = new StringTokenizer(select_method, ";");
                try {
                    while(st.hasMoreTokens() && i < 3) {
                        temp[i] = st.nextToken();
                        i++;
                    }
                    parameter.addElement(C_PARAM_METHOD_BOOLEAN);
                    parameter.addElement(temp[0]);
                    /*
                        parameter.addElement(C_PARAM_METHOD_OR);
                        parameter.addElement(temp[1]);
                     */
                    parameter.addElement(C_PARAM_METHOD_AND);
                    parameter.addElement(temp[1]);
                } catch(Exception e) {
                    e.printStackTrace();
                    System.err.println(e.toString());
                }
            } else {
                parameter.addElement(C_PARAM_METHOD_AND);
                parameter.addElement("All");
                /*
                    parameter.addElement(C_PARAM_METHOD_OR);
                    parameter.addElement("Any");
                 */
                parameter.addElement(C_PARAM_METHOD_BOOLEAN);
                parameter.addElement("Boolean");
            }
        }
        if(selection.equals("format")) {
            if((select_format != "") && (select_format != null)) {
                StringTokenizer st = new StringTokenizer(select_format, ";");
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
                    e.printStackTrace();
                    System.err.println(e.toString());
                }
            } else {
                parameter.addElement(C_PARAM_FORMAT_LONG);
                parameter.addElement("Long");
                parameter.addElement(C_PARAM_FORMAT_SHORT);
                parameter.addElement("Short");
            }
        }
        //else
        if(selection.equals("sort")) {
            if((select_sort != "") && (select_sort != null)) {
                StringTokenizer st = new StringTokenizer(select_sort, ";");
                try {
                    /*
                        while(st.hasMoreTokens() && i < 6) {
                        temp[i] = st.nextToken();
                        i++;
                        }
                     */
                    if(st.hasMoreTokens()) {
                        parameter.addElement("nolimit");
                        parameter.addElement(st.nextToken());
                    }
                    if(st.hasMoreTokens()) {
                        parameter.addElement("hour");
                        parameter.addElement(st.nextToken());
                    }
                    if(st.hasMoreTokens()) {
                        parameter.addElement("day");
                        parameter.addElement(st.nextToken());
                    }
                    if(st.hasMoreTokens()) {
                        parameter.addElement("week");
                        parameter.addElement(st.nextToken());
                    }
                    if(st.hasMoreTokens()) {
                        parameter.addElement("month");
                        parameter.addElement(st.nextToken());
                    }
                    if(st.hasMoreTokens()) {
                        parameter.addElement("year");
                        parameter.addElement(st.nextToken());
                    }
                    /*
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
                     */
                } catch(Exception e) {
                    e.printStackTrace();
                    System.err.println(e.toString());
                }
                //} else {
                /*
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
                 */
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
     *  The method invoked by the publishing-mechanism of OpenCms
     *
     *@param  cms               the Cms Object
     *@param  changedResources  Vector with the links to all exported Resources
     */
    public static void publishLinks(CmsObject cms, Vector changedResources) {
        Vector files;
        String noIndexStart = "";
        String noIndexEnd = "";
        try {
            m_indexAll = false;
            m_newIndex = false;
            //get the modul-properties
            m_active = OpenCms.getRegistry().getModuleParameterBoolean(
                    C_PARAM_MODULE_NAME, C_ACTIVE);

            m_indexPDFs = OpenCms.getRegistry().getModuleParameterBoolean(
                    C_PARAM_MODULE_NAME, C_INDEXPDFS);
            noIndexStart = OpenCms.getRegistry().getModuleParameter(
                    C_PARAM_MODULE_NAME, C_NOINDEXSTART);
            noIndexEnd = OpenCms.getRegistry().getModuleParameter(
                    C_PARAM_MODULE_NAME, C_NOINDEXEND);
            //
            if(noIndexStart != null && !noIndexStart.equals("")
                     && noIndexEnd != null && !noIndexEnd.equals("")) {
                m_noIndexStart = noIndexStart;
                m_noIndexEnd = noIndexEnd;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            m_active = true;
            m_newIndex = false;
            m_indexAll = false;
            m_indexPDFs = false;
        }
        if(debug) {
            System.err.println("ControlLucene.publishLinks.m_active=" + m_active);
            System.err.println("ControlLucene.publishLinks.m_newIndex=" + m_newIndex);
            System.err.println("ControlLucene.publishLinks.m_indexAll=" + m_indexAll);
            System.err.println("ControlLucene.publishLinks.m_analyzer=" + m_analyzer);
            System.err.println("ControlLucene.indexProject.m_noIndexStart=" + m_noIndexStart);
            System.err.println("ControlLucene.indexProject.m_noIndexEnd=" + m_noIndexEnd);
        }
        if(m_active) {
            files = changedResources;
            if(files != null && files.size() > 0) {
                createIndex(cms, m_url, files);
            }
        }
    }


    /**
     *  The method invoked by the publishing-mechanism of OpenCms
     *
     *@param  cms               the Cms Object
     */
    protected static void indexProject(CmsObject cms) {
        Vector files = new Vector();
        String noIndexStart = "";
        String noIndexEnd = "";
        try {
            m_indexAll = true;
            //if all export-files are indexed a completely new index is created
            m_newIndex = true;
            //get the modul-properties
            m_active = OpenCms.getRegistry().getModuleParameterBoolean(
                    C_PARAM_MODULE_NAME, C_ACTIVE);
            m_indexPDFs = OpenCms.getRegistry().getModuleParameterBoolean(
                    C_PARAM_MODULE_NAME, C_INDEXPDFS);

            noIndexStart = OpenCms.getRegistry().getModuleParameter(
                    C_PARAM_MODULE_NAME, C_NOINDEXSTART);
            noIndexEnd = OpenCms.getRegistry().getModuleParameter(
                    C_PARAM_MODULE_NAME, C_NOINDEXEND);
            //
            if(noIndexStart != null && !noIndexStart.equals("")
                     && noIndexEnd != null && !noIndexEnd.equals("")) {
                m_noIndexStart = noIndexStart;
                m_noIndexEnd = noIndexEnd;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            m_active = true;
            m_newIndex = true;
            m_indexAll = true;
            m_indexPDFs = false;
        }
        if(debug) {
            System.err.println("ControlLucene.indexProject.m_active=" + m_active);
            System.err.println("ControlLucene.indexProject.m_newIndex=" + m_newIndex);
            System.err.println("ControlLucene.indexProject.m_indexAll=" + m_indexAll);
            System.err.println("ControlLucene.indexProject.m_analyzer=" + m_analyzer);
            System.err.println("ControlLucene.indexProject.m_noIndexStart=" + m_noIndexStart);
            System.err.println("ControlLucene.indexProject.m_noIndexEnd=" + m_noIndexEnd);
        }
        if(m_active) {
            try {
                files = cms.getAllExportLinks();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            if(files != null) {
                createIndex(cms, m_url, files);
            }
        }
    }

}

package com.opencms.modules.search.lucene;

/*
    $RCSfile: HtmlParser.java,v $
    $Date: 2003/03/25 14:48:28 $
    $Revision: 1.9 $
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
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.opencms.htmlconverter.CmsHtmlConverter;

/**
 *  Parses the html-code by using tidy in connection with the HtmlConverter which
 *  is alseo part of the OpenCms-project.
 *
 *@author     grehuh
 *@created    28. Februar 2002
 */
public class HtmlParser implements I_ContentParser {

    // the debug flag
    private final boolean debug = false;

    private String m_title = "";
    private String m_keywords = "";
    private String m_description = "";
    private String m_content = "";
    private StringBuffer m_completeContent = null;
    private StringBuffer m_ablage = null;
    private String m_published = "";
    private String m_contentConf = "";
    private String m_keywordConf = "";
    private String m_descConf = "";
    private String m_titleConf = "";
    private String m_noindexStart = "";
    private String m_noindexEnd = "";
    private int m_length = 0;
    private CmsHtmlConverter conv = null;


    /**
     *  Constructor for the HtmlParser object
     */
    public HtmlParser() {
        new StringBuffer();
        m_noindexStart = ControlLucene.getNoIndexStart();
        m_noindexEnd = ControlLucene.getNoIndexEnd();
        m_length = m_noindexEnd.length();
        conv = new CmsHtmlConverter();
        actualDate();
        m_ablage = new StringBuffer();
        m_completeContent = new StringBuffer();
        readConfigFiles();
    }


    /**
     *  Gets the contents attribute of the HtmlParser object
     *
     *@return    The contents value
     */
    public String getContents() {
        return m_content;
    }


    /**
     *  Parsed Content.
     *
     *@return    The description value
     */
    public String getDescription() {
        return m_description;
    }


    /**
     *  Return m_title
     *
     *@return    The title value
     */
    public String getTitle() {
        return m_title;
    }


    /**
     *  Parsed keywords.
     *
     *@return    The keywords value
     */
    public String getKeywords() {
        return m_keywords;
    }


    /**
     *  Filter the complete content by this configuration string for the html-Converter
     *
     *@param  content        Description of the Parameter
     *@param  configuration  Description of the Parameter
     *@return                Description of the Return Value
     */
    private String filterContent(String content, String configuration) {
        //conv.setConverterConfFile(confFile);
        conv.setConverterConfString(configuration);
        content = conv.convertHTML(content);
        if(debug) {
            System.err.println("filterContent.html.content=" + content);
        }
        return content;
    }


    /**
     *  Description of the Method
     */
    private void readConfigFiles() {
        StringBuffer configuration = new StringBuffer("");
        InputStreamReader reader;
        try {
            ClassLoader classloader = Class.forName("com.opencms.modules.search.lucene.HtmlParser").getClassLoader();
            //content-config
            reader = new InputStreamReader(classloader.getResourceAsStream("com/opencms/modules/search/lucene/htmlcontent.properties"));
            int c;
            while((c = reader.read()) != -1) {
                configuration.append((char) c);
            }
            m_contentConf = configuration.toString();

            //description-config
            configuration.setLength(0);
            reader = new InputStreamReader(classloader.getResourceAsStream("com/opencms/modules/search/lucene/htmldescription.properties"));
            while((c = reader.read()) != -1) {
                configuration.append((char) c);
            }
            m_descConf = configuration.toString();

            //keyword-config
            configuration.setLength(0);
            reader = new InputStreamReader(classloader.getResourceAsStream("com/opencms/modules/search/lucene/htmlkeywords.properties"));
            while((c = reader.read()) != -1) {
                configuration.append((char) c);
            }
            m_keywordConf = configuration.toString();

            //title-config
            configuration.setLength(0);
            reader = new InputStreamReader(classloader.getResourceAsStream("com/opencms/modules/search/lucene/htmltitle.properties"));
            while((c = reader.read()) != -1) {
                configuration.append((char) c);
            }
            m_titleConf = configuration.toString();
            if(debug) {
                System.err.println("m_contentConf=" + m_contentConf);
                System.err.println("m_descConf=" + m_descConf);
                System.err.println("m_keywordConf=" + m_keywordConf);
                System.err.println("m_titleConf=" + m_titleConf);
            }
        } catch(Exception ex) {
            System.err.println("Error at reading Config-file");
            ex.printStackTrace();
        }
    }


    /**
     *  Central method of the class to start of the parsing-procedure.
     *
     *@param  con  The complete content of the html-page
     */
    public void parse(InputStream con) {
        m_completeContent.setLength(0);
        m_completeContent.append(fetchHtmlContent(con));
        removeNoindex();
        String completeContent = m_completeContent.toString();
        m_keywords = filterContent(completeContent, m_keywordConf);
        m_description = filterContent(completeContent, m_descConf);
        m_title = filterContent(completeContent, m_titleConf);
        m_content = filterContent(completeContent, m_contentConf);
    }


    /**
     *  Fetch the complete content of the page.
     *
     *@param  con  Description of the Parameter
     *@return      Description of the Return Value
     */
    private String fetchHtmlContent(InputStream con) {
        int index = -1;
        StringBuffer content = null;
        DataInputStream input = null;
        String line = null;

        try {
            input = new DataInputStream(con);

            content = new StringBuffer();
            while((line = input.readLine()) != null) {
                content = content.append(line);
            }
            input.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        if(debug) {
            System.err.println("' done!");
        }

        return content.toString();
    }


    /**
     *  Get the actual date.
     */
    private void actualDate() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("ECT"));
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        m_published = format.format(cal.getTime());
    }


    /**
     *  Gets the published attribute of the HtmlParser object
     *
     *@return    The published value
     */
    public String getPublished() {
        return m_published;
    }


    /**
     *  Remove all STrings between the start- and stop-String
     */
    private void removeNoindex() {
        int start = 0;
        int end = 0;
        int i = 0;
        while(start != -1 && end != -1) {
            m_ablage.setLength(0);
            start = m_completeContent.toString().indexOf(m_noindexStart);
            end = m_completeContent.toString().indexOf(m_noindexEnd);
            if(start != -1 && end != -1) {
                m_ablage.append(m_completeContent.substring(0, start));
                m_ablage.append(m_completeContent.substring(end + m_length));
                m_completeContent.setLength(0);
                m_completeContent.append(m_ablage);
            }
        }
        if(debug) {
            System.err.println("HtmlParser.removeNoindex.m_completeContent=" + m_completeContent);
        }
    }
}

package com.opencms.modules.search.lucene;

/*
    $RCSfile: HtmlParser.java,v $
    $Date: 2002/03/01 13:30:35 $
    $Revision: 1.6 $
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
import com.opencms.htmlconverter.*;
import java.io.*;
import java.util.*;
import java.text.*;

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
    private String m_completeContent = "";
    private String m_published = "";

    private CmsHtmlConverter conv = null;

    // the Config Strings for the html-converter

    // the Config Strings to replace the german Sonderzeichen.
    private final String REPLACE_STRINGS =
            "<replacestrings usedefaults=\"true\">" +
            "   <string content=\"&amp;auml;\" replace=\"ae\"/>        <string content=\"&amp;uuml;\" replace=\"ue\"/>" +
            "   <string content=\"&amp;ouml;\" replace=\"oe\"/>        <string content=\"&amp;Uuml;\" replace=\"Ue\"/>" +
            "   <string content=\"&amp;Auml;\" replace=\"Ae\"/>        <string content=\"&amp;Ouml;\" replace=\"Oe\"/>" +
            "   <string content=\"&amp;lt;\" replace=\" \"/>		<string content=\"&amp;gt;\" replace=\" \"/>" +
            "   <string content=\"&amp;szlig;\" replace=\"ss\"/><string content=\"&amp;qout;\" replace=\" \"/>        <string content=\"&amp;nbsp;\" replace=\" \"/>" +
            "</replacestrings>";

    // the Config Strings to get the keywords from the meta-tag
    private final String KEYWORD_CONF = "<?xml version=\"1.0\"?>" +
            "<ConverterConfig>" +
            "	<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>" +
            "   <removetags><tag name=\"html\"/><tag name=\"head\"/></removetags>" +
            "	<replacetags usedefaults=\"false\">" +
            "		<tag name=\"meta\" attrib=\"name\" value=\"keywords\" replacestarttag=\" $parameter$ \" replaceendtag=\"\"" +
            "			getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "		<tag name=\"meta\" attrib=\"name\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\"" +
            "			getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "		<tag name=\"meta\" attrib=\"http-equiv\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\"" +
            "			getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "	</replacetags>" +
            "    <removeblocks>" +
            "		<tag name=\"link\"/><tag name=\"script\"/><tag name=\"style\"/>" +
            "		<tag name=\"body\"/><tag name=\"title\"/><tag name=\"base\"/>" +
            "    </removeblocks>" +
            "</ConverterConfig>";

    // the Config Strings to get the description from the meta-tag
    private final String DESCRIPTION_CONF = "<?xml version=\"1.0\"?>" +
            "<ConverterConfig>" +
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>" +
            "<removetags><tag name=\"html\"/><tag name=\"head\"/></removetags>" +
            "<replacetags usedefaults=\"false\">" +
            "    <tag name=\"meta\" attrib=\"name\" value=\"description\" replacestarttag=\" $parameter$ \" replaceendtag=\"\"" +
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "    <tag name=\"meta\" attrib=\"name\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\"" +
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "    <tag name=\"meta\" attrib=\"http-equiv\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\"" +
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>" +
            "</replacetags>" +
            "<removeblocks>" +
            "    <tag name=\"link\"/><tag name=\"script\"/><tag name=\"style\"/>" +
            "    <tag name=\"body\"/><tag name=\"title\"/><tag name=\"base\"/>" +
            "</removeblocks>" +
            REPLACE_STRINGS +
            "</ConverterConfig>";

    // the Config Strings to get the title from the meta-tag
    private final String TITLE_CONF = "<?xml version=\"1.0\"?>" +
            "<ConverterConfig>" +
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>" +
            "<removetags>" +
            "    <tag name=\"html\"/><tag name=\"head\"/><tag name=\"title\"/>" +
            "</removetags><replacetags usedefaults=\"false\"></replacetags>" +
            "<removeblocks>" +
            "    <tag name=\"link\"/><tag name=\"meta\"/><tag name=\"script\"/>" +
            "    <tag name=\"style\"/><tag name=\"body\"/><tag name=\"base\"/>" +
            "</removeblocks>" +
            REPLACE_STRINGS +
            "</ConverterConfig>";

    // the Config Strings to get the complete content
    private final String CONTENT_CONF =
            "<?xml version=\"1.0\"?>" +
            "<ConverterConfig>" +
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput>    </defaults>" +
            "<replacetags usedefaults=\"true\">		<tag name=\"p\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>		<tag name=\"td\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"tr\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"th\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"tf\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h1\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h2\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h3\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h4\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"hr\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>" +
            "        <tag name=\"title\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"br\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"a\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"li\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"dl\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"ul\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"input\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"center\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"link\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>	</replacetags>" +
            "<removetags>		<tag name=\"font\"/>        <tag name=\"frame\"/><tag name=\"frameset\"/>		<tag name=\"form\"/>		<tag name=\"table\"/>		<tag name=\"div\"/>		<tag name=\"html\"/>		<tag name=\"body\"/>" +
            "		<tag name=\"head\"/>        <tag name=\"dd\"/>		<tag name=\"nobr\"/>		<tag name=\"meta\"/>		<tag name=\"img\"/>	<tag name=\"b\"/>        <tag name=\"i\"/>        <tag name=\"bgsound\"/>        <tag name=\"u\"/>        <tag name=\"blockquote\"/>        <tag name=\"span\"/>        <tag name=\"strong\"/>    </removetags>" +
            "<removeblocks>        <tag name=\"script\"/>        <tag name=\"style\"/>        <tag name=\"base\"/>    </removeblocks>	" +
            REPLACE_STRINGS +
            "</ConverterConfig>";


    /**
     *  Constructor for the HtmlParser object
     */
    public HtmlParser() {
        conv = new CmsHtmlConverter();
        actualDate();
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
     *  Parse Content.
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
     *  Parse Content.
     *
     *@return    The keywords value
     */
    public String getKeywords() {
        return m_keywords;
    }


    /**
     *  Description of the Method
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
            System.out.println("filterContent.html.content=" + content);
        }
        return content;
    }


    /**
     *  Description of the Method
     *
     *@param  con  Description of the Parameter
     */
    public void parse(InputStream con) {
        m_completeContent = fetchHtmlContent(con);
        m_keywords = filterContent(m_completeContent, KEYWORD_CONF);
        m_description = filterContent(m_completeContent, DESCRIPTION_CONF);
        m_title = filterContent(m_completeContent, TITLE_CONF);
        m_content = filterContent(m_completeContent, CONTENT_CONF);
    }


    /**
     *  Description of the Method
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
            System.out.println("' done!");
        }

        return content.toString();
    }


    /**
     *  Description of the Method
     */
    private void actualDate() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("ECT"));
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
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

}

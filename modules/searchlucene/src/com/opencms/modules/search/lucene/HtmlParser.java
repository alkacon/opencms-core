package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: HtmlParser.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/26 16:16:48 $
 *  $Revision: 1.3 $
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

 /**
 * <p><code>HtmlParser</code>
 *  Content Handler for Html documents.
 * </p>
 *
 * @author
 * @version 1.0
 */
import com.opencms.htmlconverter.*;
import java.io.*;

public class HtmlParser implements I_ContentParser{

    // the debug flag
    private final boolean debug = false;

    private String m_title="";
    private String m_keywords="";
    private String m_description="";
    private String m_content="";
    private String m_completeContent="";

    private CmsHtmlConverter conv = null;

    // the Config Strings for the html-converter
    private final String REPLACE_STRINGS=
        "<replacestrings usedefaults=\"true\">"+
		"   <string content=\"&amp;auml;\" replace=\"ae\"/>        <string content=\"&amp;uuml;\" replace=\"ue\"/>"+
        "   <string content=\"&amp;ouml;\" replace=\"oe\"/>        <string content=\"&amp;Uuml;\" replace=\"Ue\"/>"+
        "   <string content=\"&amp;Auml;\" replace=\"Ae\"/>        <string content=\"&amp;Ouml;\" replace=\"Oe\"/>"+
        "   <string content=\"&amp;lt;\" replace=\" \"/>		<string content=\"&amp;gt;\" replace=\" \"/>"+
        "   <string content=\"&amp;szlig;\" replace=\"ss\"/>        <string content=\"&amp;nbsp;\" replace=\" \"/>"+
	    "</replacestrings>";
    private final String KEYWORD_CONF="<?xml version=\"1.0\"?>"+
        "<ConverterConfig>"+
        "	<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>"+
        "   <removetags><tag name=\"html\"/><tag name=\"head\"/></removetags>"+
        "	<replacetags usedefaults=\"false\">"+
        "		<tag name=\"meta\" attrib=\"name\" value=\"keywords\" replacestarttag=\" $parameter$ \" replaceendtag=\"\""+
        "			getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
        "		<tag name=\"meta\" attrib=\"name\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\""+
        "			getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
        "		<tag name=\"meta\" attrib=\"http-equiv\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\""+
        "			getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
        "	</replacetags>"+
        "    <removeblocks>"+
        "		<tag name=\"link\"/><tag name=\"script\"/><tag name=\"style\"/>"+
        "		<tag name=\"body\"/><tag name=\"title\"/><tag name=\"base\"/>"+
        "    </removeblocks>"+
        "</ConverterConfig>";
    private final String DESCRIPTION_CONF="<?xml version=\"1.0\"?>"+
        "<ConverterConfig>"+
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>"+
            "<removetags><tag name=\"html\"/><tag name=\"head\"/></removetags>"+
            "<replacetags usedefaults=\"false\">"+
            "    <tag name=\"meta\" attrib=\"name\" value=\"description\" replacestarttag=\" $parameter$ \" replaceendtag=\"\""+
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
            "    <tag name=\"meta\" attrib=\"name\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\""+
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
            "    <tag name=\"meta\" attrib=\"http-equiv\" value=\"\" replacestarttag=\"tratra\" replaceendtag=\"\""+
            "        getreplacefromattrs=\"false\"  parameter=\"content\"/>"+
            "</replacetags>"+
            "<removeblocks>"+
            "    <tag name=\"link\"/><tag name=\"script\"/><tag name=\"style\"/>"+
            "    <tag name=\"body\"/><tag name=\"title\"/><tag name=\"base\"/>"+
            "</removeblocks>"+
            REPLACE_STRINGS+
        "</ConverterConfig>";
    private final String TITLE_CONF="<?xml version=\"1.0\"?>"+
        "<ConverterConfig>"+
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput></defaults>"+
            "<removetags>"+
            "    <tag name=\"html\"/><tag name=\"head\"/><tag name=\"title\"/>"+
            "</removetags><replacetags usedefaults=\"false\"></replacetags>"+
            "<removeblocks>"+
            "    <tag name=\"link\"/><tag name=\"meta\"/><tag name=\"script\"/>"+
            "    <tag name=\"style\"/><tag name=\"body\"/><tag name=\"base\"/>"+
            "</removeblocks>"+
            REPLACE_STRINGS+
        "</ConverterConfig>";

    private final String CONTENT_CONF=
        "<?xml version=\"1.0\"?>"+
        "<ConverterConfig>"+
            "<defaults><xhtmloutput value=\"false\"></xhtmloutput>    </defaults>"+
            "<replacetags usedefaults=\"true\">		<tag name=\"p\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>		<tag name=\"td\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"tr\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"th\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"tf\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h1\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h2\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h3\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"h4\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"hr\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>"+
            "        <tag name=\"title\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"br\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"a\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"li\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"dl\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"ul\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"input\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"center\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>        <tag name=\"link\" attrib=\"\" value=\"\" replacestarttag=\" \" replaceendtag=\" \"/>	</replacetags>"+
            "<removetags>		<tag name=\"font\"/>        <tag name=\"frame\"/><tag name=\"frameset\"/>		<tag name=\"form\"/>		<tag name=\"table\"/>		<tag name=\"div\"/>		<tag name=\"html\"/>		<tag name=\"body\"/>"+
            "		<tag name=\"head\"/>        <tag name=\"dd\"/>		<tag name=\"nobr\"/>		<tag name=\"meta\"/>		<tag name=\"img\"/>	<tag name=\"b\"/>        <tag name=\"i\"/>        <tag name=\"bgsound\"/>        <tag name=\"u\"/>        <tag name=\"blockquote\"/>        <tag name=\"span\"/>        <tag name=\"strong\"/>    </removetags>"+
            "<removeblocks>        <tag name=\"script\"/>        <tag name=\"style\"/>        <tag name=\"base\"/>    </removeblocks>	"+
            REPLACE_STRINGS+
        "</ConverterConfig>";

    public HtmlParser() {
        conv = new CmsHtmlConverter();
    }

    public String getContents() {
        return m_content;
    }

    /**
     * Parse Content.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     *	Return m_title
     */
    public String getTitle() {
        return m_title;
    }
    /**
     * Parse Content.
     */
    public String getKeywords() {
        return m_keywords;
    }
    /**
     *  Description of the Method
     *
     *@param  content   Description of the Parameter
     *@param  confFile  Description of the Parameter
     *@return           Description of the Return Value
     */
    private String filterContent(String content, String configuration) {
        if (debug) System.out.println("filterContent.html.content=" + content);
        //conv.setConverterConfFile(confFile);
        conv.setConverterConfString(configuration);
        content = conv.convertHTML(content);
        return content;
    }

    public void parse(InputStream con){
        m_completeContent=fetchHtmlContent(con);
        m_keywords=filterContent(m_completeContent, KEYWORD_CONF);
        m_description=filterContent(m_completeContent, DESCRIPTION_CONF);
        m_title=filterContent(m_completeContent, TITLE_CONF);
        m_content=filterContent(m_completeContent, CONTENT_CONF);
    }
    /**
     *  Description of the Method
     *
     *@param  theUrl  Description of the Parameter
     *@return         Description of the Return Value
     */
    private String fetchHtmlContent(InputStream con) {
        int index = -1;
        StringBuffer content = null;
        DataInputStream input = null;
        String line = null;

        try {
            input = new DataInputStream(con);
            content = new StringBuffer();
            while ((line = input.readLine()) != null) {
                content = content.append(line);
            }
            input.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (debug) {
            System.out.println("' done!");
        }

        return content.toString();
    }


}
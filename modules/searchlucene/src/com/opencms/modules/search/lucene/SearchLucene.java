package com.opencms.modules.search.lucene;

/*
    $RCSfile: SearchLucene.java,v $
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
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.analysis.de.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.util.*;

/**
 *  Class to perform a search in an existing Lucene-index.
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class SearchLucene {
    // the debug flag
    private final boolean debug = false;

    private String m_queryString = "";


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Token next() {
        return new Token("", 0, 0);
    }


    // usage: Search <index-path> <query-string>

    /**
     *  Constructor for the SearchLucene object
     */
    public SearchLucene() { }


    /**
     *  Description of the Method
     *
     *@param  indexPath      Description of the Parameter
     *@param  queryString    Description of the Parameter
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public Vector performSearch(String indexPath, String queryString) throws Exception {
        if(debug) {
            System.out.println("indexPath=" + indexPath + " queryString=" + queryString);
        }
        m_queryString = queryString;
        Vector res = new Vector();
        Hashtable oneHit = null;
        buildQueryString();
        Query query = null;
        Searcher searcher = new IndexSearcher(indexPath);

        //BooleanQuery query = (BooleanQuery) QueryParser.parse(m_queryString, "body", new SimpleAnalyzer());
        //query = (BooleanQuery) QueryParser.parse(m_queryString, "body", new GermanAnalyzer());
        query = (BooleanQuery) QueryParser.parse(m_queryString, "body", new StandardAnalyzer());
        Hits hits = searcher.search(query);
        int countHits = hits.length();
        String excerpt = "";
        for(int i = 0; i < countHits; i++) {
            excerpt = LuceneTools.highlightTerms(hits.doc(i).get("content"), new TermHighlighter(), query, new StandardAnalyzer());
            excerpt = cutExcerpt(excerpt);
            oneHit = new Hashtable();
            oneHit.put("excerpt", excerpt);
            oneHit.put("description", hits.doc(i).get("description"));
            oneHit.put("title", hits.doc(i).get("title"));
            oneHit.put("keywords", hits.doc(i).get("keywords"));
            oneHit.put("url", hits.doc(i).get("path"));
            oneHit.put("score", Math.round(hits.score(i) * 100) + " %");
            oneHit.put("modified", hits.doc(i).get("modified"));
            oneHit.put("length", hits.doc(i).get("length"));
            res.add(oneHit);
        }

        return res;
    }


    /**
     *  To cut the excerpt to the right length
     *
     *@param  excerpt  the complete content with included highlights
     *@return          the substring with the first highlights
     */
    private String cutExcerpt(String excerpt) {
        int firstHighlight = excerpt.indexOf("<b>");
        int excerptLength = excerpt.length();
        int before = 50;
        int after = 150;
        if(debug) {
            System.out.println("firstHighlight=" + firstHighlight);
            System.out.println("excerptLength=" + excerptLength);
        }
        if(firstHighlight != -1) {
            if(firstHighlight >= before) {
                if(excerptLength >= firstHighlight + after) {
                    excerpt = excerpt.substring(firstHighlight - before, firstHighlight + after);
                } else {
                    excerpt = excerpt.substring(firstHighlight - before, excerptLength);
                }
            } else {
                if(excerptLength >= firstHighlight + after) {
                    excerpt = excerpt.substring(firstHighlight, firstHighlight + after);
                } else {
                    excerpt = excerpt.substring(firstHighlight, excerptLength);
                }
            }
        }
        excerpt=replaceAll(excerpt,"&nbsp;"," ");

        //remove all \xxx
        while (excerpt.indexOf("\\")!=-1){
            excerpt=excerpt.substring(0,excerpt.indexOf("\\"))+
                    excerpt.substring(excerpt.indexOf("\\")+4,excerpt.length());
        }

        if(debug) {
            System.out.println("excerpt=" + excerpt);
        }
        return excerpt;
    }


    /**
     *  Description of the Method
     */
    public void buildQueryString() {
        m_queryString = m_queryString.trim();
        if(m_queryString.startsWith("/") ||
                m_queryString.startsWith("`") ||
                m_queryString.startsWith("´") ||
                m_queryString.startsWith("*") ||
                m_queryString.startsWith("~") ||
                m_queryString.startsWith("'") ||
                m_queryString.startsWith("#") ||
                m_queryString.startsWith("$") ||
                m_queryString.startsWith("%")
                ) {
            m_queryString = m_queryString.substring(1);
        }

        m_queryString=replaceAll(m_queryString,"Ü", "Ue");
        m_queryString=replaceAll(m_queryString,"Ä", "Ae");
        m_queryString=replaceAll(m_queryString,"Ö", "Oe");
        m_queryString=replaceAll(m_queryString,"ü", "ue");
        m_queryString=replaceAll(m_queryString,"ä", "ae");
        m_queryString=replaceAll(m_queryString,"ö", "oe");
        m_queryString=replaceAll(m_queryString,"ß", "ss");
        m_queryString=replaceAll(m_queryString,"&nbsp;", " ");

        if(debug) {
            System.out.println("m_queryString=" + m_queryString);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  replace      Description of the Parameter
     *@param  replaceWith  Description of the Parameter
     */
    private String replaceAll(String text,String replace, String replaceWith) {
        StringBuffer sb = new StringBuffer(text);
        int stelle = 0;
        while(text.indexOf(replace) != -1) {
            stelle = text.indexOf(replace);
            sb.replace(stelle, stelle + replace.length(), replaceWith);
            text = sb.toString();
        }
        return text;
    }
}

package com.opencms.modules.search.lucene;

/*
    $RCSfile: SearchLucene.java,v $
    $Date: 2002/07/15 14:03:57 $
    $Revision: 1.7 $
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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.document.*;
import java.util.*;
import org.apache.oro.text.perl.*;
//import Entities;

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
    private long m_from=0;
    private long m_to=0;
    private DateFilter dateFilter;


    // the Perl5Util from the ORO-Package for perl5 like regular expressions
	Perl5Util m_util = new Perl5Util();


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
    public Vector performSearch(String indexPath, String queryString, String analyzer, String method) throws Exception {
        if(debug) {
            System.err.println("indexPath=" + indexPath + " queryString=" + queryString+ " method=" + method);
        }
        Hits hits;
        Query query;
        m_queryString = queryString;
        Vector res = new Vector();
        Hashtable oneHit;

        //m_queryString=Entities.encode(m_queryString);
        buildQueryString(method);
        Searcher searcher = new IndexSearcher(indexPath);
        //don't perform a search if the query-string is ""!
        if (m_queryString.length()==0) return null;
        query = QueryParser.parse(m_queryString, "body", new StopAnalyzer());

        /*else if (analyzer.equalsIgnoreCase("german")){
            query =  QueryParser.parse(m_queryString, "body", new GermanAnalyzer());
        }
        if (debug) System.err.println("SearchLucene.performSearch.query="+query.toString("body"));*/

        //filter the search by date if a limiting date is given
        if (m_from != 0) {
            if (m_to != 0) {
                dateFilter = new DateFilter("modified", m_from, m_to);
            } else {
                dateFilter = DateFilter.After("modified", m_from);
            }
        } else if (m_to != 0) {
            dateFilter = DateFilter.Before("modified", m_to);
        }

        //search now with or without date filter
        if (dateFilter == null) {
            hits = searcher.search(query);
        } else {
            hits = searcher.search(query, dateFilter);
        }

        //fill and return the vector with hits
        int countHits = hits.length();
        String excerpt = "";
        String title = "";
        for(int i = 0; i < countHits; i++) {
            excerpt = LuceneTools.highlightTerms(hits.doc(i).get("content"), new TermHighlighter(), query, new StopAnalyzer());
            excerpt = cutExcerpt(excerpt);
            title=hits.doc(i).get("title");
            title=replaceAll(title,"\\n","&nbsp;");
            oneHit = new Hashtable();
            oneHit.put("excerpt", excerpt);
            oneHit.put("description", hits.doc(i).get("description"));
            oneHit.put("title",title);
            oneHit.put("keywords", hits.doc(i).get("keywords"));
            oneHit.put("url", hits.doc(i).get("path"));
            oneHit.put("score", new Integer(Math.round(hits.score(i) * 100)));
            oneHit.put("modified", new Long(DateField.stringToTime((String)hits.doc(i).get("modified"))));
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
            System.err.println("firstHighlight=" + firstHighlight);
            System.err.println("excerptLength=" + excerptLength);
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
        while (excerpt.indexOf("\\")!=-1 && excerpt.length()>excerpt.indexOf("\\")+4){
            excerpt=excerpt.substring(0,excerpt.indexOf("\\"))+
                    excerpt.substring(excerpt.indexOf("\\")+4,excerpt.length());
        }

        if(debug) {
            System.err.println("excerpt=" + excerpt);
        }
        return excerpt;
    }


    /**
     *  Remove all not allowed charcters and insert if the chosen method is "AND"
     */
    public void buildQueryString(String method) {
        StringTokenizer tokenizer=null;
        StringBuffer newQueryStringb=new StringBuffer();
        //the allowed characters. The pattern-string starts with the characters the querystring is not allowed to start with.
        String pattern = "+-*?1234567890äöüabcdefghijklmnopqrstuvwxyzÄÜÖABCDEFGHIJKLMNOPQRSTUVWXYZ\" ";
        //String pattern = "/^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_-])+(\\.[a-zA-Z0-9_-])+/";
		//m_util.match(pattern, m_queryString);
        m_queryString = m_queryString.trim();

        //remove all characters that are not in the allowed pattern
        for (int i=0;i<m_queryString.length();i++) {
            if (pattern.indexOf(m_queryString.substring(i,i+1))==-1){
                    m_queryString = m_queryString.substring(0,i)+m_queryString.substring(i+1);
                    i--;
            }
        }

        //Cut the characters the querystring is not allowed to start with.
        //while (m_queryString.length()>=1 && pattern.substring(4).indexOf(m_queryString)==-1) m_queryString ="";
        for (int i=0;i<m_queryString.length();i++) {
            if (pattern.substring(4).indexOf(m_queryString.substring(0,1))==-1){
                    m_queryString = m_queryString.substring(1);
                    i--;
            }
        }

        m_queryString=m_queryString.toLowerCase();

        //replace the german sonderzeichen
        m_queryString=replaceAll(m_queryString,"Ü", "Ue");
        m_queryString=replaceAll(m_queryString,"Ä", "Ae");
        m_queryString=replaceAll(m_queryString,"Ö", "Oe");
        m_queryString=replaceAll(m_queryString,"ü", "ue");
        m_queryString=replaceAll(m_queryString,"ä", "ae");
        m_queryString=replaceAll(m_queryString,"ö", "oe");
        m_queryString=replaceAll(m_queryString,"ß", "ss");
        m_queryString=replaceAll(m_queryString,"&nbsp;", " ");

        if (m_queryString.length()>1 && method!=null && method.equalsIgnoreCase("AND")){
            String nextWord="";
            tokenizer=new StringTokenizer(m_queryString," ");

            newQueryStringb.append(tokenizer.nextToken());
            while(tokenizer.hasMoreElements()){
                newQueryStringb.append(" AND ");
                nextWord=tokenizer.nextToken();
                if (!nextWord.equals("OR")) newQueryStringb.append(nextWord);
            }
            m_queryString=newQueryStringb.toString();
        }
        if(debug) {
            System.err.println("m_queryString=" + m_queryString);
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

    public void setFrom(long time) {
        m_from = time;
    }
    public long getFrom() {
        return m_from;
    }
    public void setFromDays(int days) {

        //System.out.println(new Date(System.currentTimeMillis() - (long)1000*60*60*24*days));
        m_from = System.currentTimeMillis() - (long)1000*60*60*24*days;

    }
}

package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: SearchLucene.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/28 09:31:59 $
 *  $Revision: 1.4 $
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
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.util.*;


/**
 *  Description of the Class
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class SearchLucene {
    // the debug flag
    private final boolean debug = false;

    private String m_queryString = "";


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
        if (debug) {
            System.out.println("indexPath=" + indexPath + " queryString=" + queryString);
        }
        m_queryString = queryString;
        Vector res = new Vector();
        Hashtable oneHit = null;
        buildQueryString();
        Searcher searcher = new IndexSearcher(indexPath);
        //BooleanQuery query = (BooleanQuery) QueryParser.parse(m_queryString, "body", new SimpleAnalyzer());
        BooleanQuery query = (BooleanQuery) QueryParser.parse(m_queryString, "body", new GermanAnalyzer());
        Hits hits = searcher.search(query);
        int countHits = hits.length();
        for (int i = 0; i < countHits; i++) {
            oneHit = new Hashtable();
            oneHit.put("description", hits.doc(i).get("description"));
            oneHit.put("title", hits.doc(i).get("title"));
            oneHit.put("keywords", hits.doc(i).get("keywords"));
            oneHit.put("url", hits.doc(i).get("path"));
            oneHit.put("score", Math.round(hits.score(i) * 100) + " %");
            oneHit.put("modified", hits.doc(i).get("modified"));
            res.add(oneHit);
        }
        return res;
    }


    /**
     *  Description of the Method
     */
    public void buildQueryString() {
        m_queryString = m_queryString.trim();
        if (m_queryString.startsWith("/") ||
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

        replaceAll("Ü", "Ue");
        replaceAll("Ä", "Ae");
        replaceAll("Ö", "Oe");
        replaceAll("ü", "ue");
        replaceAll("ä", "ae");
        replaceAll("ö", "oe");
        replaceAll("&nbsp;", "");

        if (debug) {
            System.out.println("m_queryString=" + m_queryString);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  replace      Description of the Parameter
     *@param  replaceWith  Description of the Parameter
     */
    private void replaceAll(String replace, String replaceWith) {
        StringBuffer sb = new StringBuffer(m_queryString);
        int stelle = 0;
        while (m_queryString.indexOf(replace) != -1) {
            stelle = m_queryString.indexOf(replace);
            sb.replace(stelle, stelle + replace.length(), replaceWith);
            m_queryString = sb.toString();
        }
    }
}

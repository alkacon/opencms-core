package com.opencms.modules.search.lucene;

/*
    $RCSfile: IndexFiles.java,v $
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
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 *  Class to create a new or to expand an existing index for Lucene by adding
 *  the content of html- or pdf-files.
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class IndexFiles extends Thread {
    private File m_file = null;

    // the debug flag
    private final boolean debug = false;

    // flag that controls if new documents are added to an existing directory or if a new index directory is created
    private boolean m_newIndex = false;

    private String m_indexPath = "";

    private boolean m_optimize = true;

    private Vector m_files = null;

    private int m_contentLength = 0;

    private PdfParser m_convPdf = null;

    private String m_contentType = "";

    private long m_publishDate = 0;

    private HtmlParser m_convHtml = null;

    private String m_analyzer = "stopAnalyzer";

    private DateFormat m_dateformat;



    // usage: IndexFiles <index-path> <file> ...
    /**
     *  Constructor for the IndexFiles object
     *
     *@param  indexPath   Description of the Parameter
     *@param  files       Description of the Parameter
     *@param  analyzer    Description of the Parameter
     */
    public IndexFiles(String indexPath, Vector files, String analyzer) {
        m_convPdf = new PdfParser();
        m_convHtml = new HtmlParser();
        m_indexPath = indexPath;
        m_files = files;
        m_file = new File(m_indexPath);
        m_dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if(analyzer.equalsIgnoreCase("german") || analyzer.equalsIgnoreCase("stopanalyzer")) {
            m_analyzer = analyzer;
        }
        if(debug) {
            System.err.println("IndexFiles.IndexFiles(...).files.size()=" + files.size());
            System.err.println("IndexFiles.IndexFiles(...).m_analyzer=" + m_analyzer);
        }
    }


    /**
     *  Main processing method for the IndexFiles object
     */
    public void run() {
        try {
            System.out.println("start indexing");
            createIndexFiles();
            System.out.println("indexing completed successfully");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    protected void createIndexFiles() throws Exception {
        IndexWriter writer = null;
        deleteIndexFiles();
        Document doc = null;
        //
        InputStream con = null;
        //

        try {
            if(m_analyzer.equalsIgnoreCase("stopanalyzer")) {
                writer = new IndexWriter(m_indexPath, new StandardAnalyzer(), m_newIndex);
            } else if(m_analyzer.equalsIgnoreCase("german")) {
                writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            }
            String completeContent = "";
            String keywords = "";
            String description = "";
            String title = "";
            String parsedContent = "";
            String published = "";
            InputStream is = null;
            I_ContentParser parser = null;

            for(int i = 0; i < m_files.size(); i++) {
                if(isInterrupted()) {
                    break;
                }
                if(debug) {
                    System.err.println("Indexing file " + m_files.elementAt(i));
                }
                doc = new Document();
                con = connectUrl((String) m_files.elementAt(i));
                if(con == null) {
                    continue;
                }

                //select the parser
                if(m_contentType != null && m_contentType.equals("text/html")) {
                    parser = m_convHtml;
                } else if(m_contentType != null && m_contentType.equals("application/pdf")) {
                    parser = m_convPdf;
                } else {
                    continue;
                }

                //the html-Parsing and Indexing
                parser.parse(con);
                completeContent = parser.getContents();
                if(parser.getKeywords() != null) {
                    keywords = parser.getKeywords();
                }
                if(parser.getDescription() != null) {
                    description = parser.getDescription();
                }
                if(parser.getTitle() != null) {
                    title = parser.getTitle();
                }
                if(parser.getContents() != null) {
                    parsedContent = parser.getContents();
                }
                /*
                    if(parser.getPublished() != null) {
                    published = parser.getPublished();
                    }else {
                 */
                published = DateField.timeToString(m_publishDate);
                //m_dateformat.format(m_publishDate);
                //}
                doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                doc.add(Field.Keyword("length", m_contentLength + ""));
                doc.add(Field.Keyword("keywords", keywords));
                doc.add(Field.Keyword("description", description));
                doc.add(Field.Keyword("modified", published));
                doc.add(Field.Keyword("title", title));
                doc.add(Field.Keyword("content", parsedContent));
                is = new ByteArrayInputStream(parsedContent.getBytes());

                doc.add(Field.Text("body", (Reader) new InputStreamReader(is)));
                writer.addDocument(doc);
                is.close();
            }
            writer.optimize();
            if(debug) {
                System.err.println("Docs in Index " + writer.docCount());
            }
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    private void deleteIndexFiles() throws Exception {
        IndexReader reader = null;
        createPath();
        try {
            reader = IndexReader.open(m_indexPath);
            for(int i = 0; i < m_files.size(); i++) {
                Term urlTerm = new Term("path", (String) m_files.elementAt(i));
                if(reader.docFreq(urlTerm) > 0) {
                    if(debug) {
                        System.err.println("Number of docs " + reader.docFreq(new Term("path", (String) m_files.elementAt(i))));
                    }
                    if(reader.delete(urlTerm) == 1) {
                        if(debug) {
                            System.err.println("deleted from index " + (String) m_files.elementAt(i));
                        }
                    } else {
                        if(debug) {
                            System.err.println("not deleted from index " + (String) m_files.elementAt(i));
                        }
                    }
                }
            }
            reader.close();
        } catch(Exception ex) {
            if(debug) {
                ex.printStackTrace();
            }
            if(!m_newIndex) {
                m_newIndex = true;
            }
            IndexDirectory.createIndexDirectory(m_indexPath);
            createIndexFiles();
            System.out.println("created new search-index for Lucene");
        }
        if(m_optimize) {
            IndexWriter writer = null;
            if(m_analyzer.equalsIgnoreCase("stopanalyzer")) {
                writer = new IndexWriter(m_indexPath, new StandardAnalyzer(), m_newIndex);
            } else if(m_analyzer.equalsIgnoreCase("german")) {
                writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            }
            writer.optimize();
            if(debug) {
                System.err.println("Docs in Index " + writer.docCount());
            }
            writer.close();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  theUrl  Description of the Parameter
     *@return         Description of the Return Value
     */
    private InputStream connectUrl(String theUrl) {
        int index = -1;
        StringBuffer content = null;
        URLConnection urlCon = null;
        DataInputStream input = null;
        String line = null;

        try {
            urlCon = new URL(theUrl).openConnection();
            urlCon.connect();

            //return, if this file does not exist
            if((urlCon.getHeaderField(0)).indexOf("404") != -1) {
                if(debug) {
                    System.err.println(urlCon.getHeaderField(0));
                }
                return null;
            } else if((urlCon.getHeaderField(0)).indexOf("500") != -1) {
                if(debug) {
                    System.err.println(urlCon.getHeaderField(0));
                }
                return null;
            }

            m_contentLength = urlCon.getContentLength();
            m_contentType = urlCon.getContentType();
            m_publishDate = urlCon.getDate();

            if(debug) {
                System.err.println("m_contentLength=" + m_contentLength);
                System.err.println("connectUrl.theUrl=" + theUrl);
                System.err.println("connectUrl.getContentType()=" + m_contentType);
            }
            input = new DataInputStream(urlCon.getInputStream());
            if(debug) {
                System.err.println("' done!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return input;
    }



    /**
     *  Sets the newIndex attribute of the IndexFiles object
     *
     *@param  newIndex  The new newIndex value
     */
    protected void setNewIndex(boolean newIndex) {
        m_newIndex = newIndex;
    }


    /**
     *  Gets the newIndex attribute of the IndexFiles object
     *
     *@return    The newIndex value
     */
    private boolean isNewIndex() {
        return m_newIndex;
    }


    /**
     *  Description of the Method
     */
    private void createPath() {
        try {
            if(!m_file.exists()) {
                m_file.mkdirs();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

package com.opencms.modules.search.lucene;

/*
    $RCSfile: IndexFiles.java,v $
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
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *  Class to create a new or to expand an existing index for Lucene by adding
 *  the content of html- or pdf-files.
 *
 *@author     grehuh
 *@created    13. Februar 2002
 */
public class IndexFiles extends Thread {
    // the debug flag
    private final boolean debug = false;

    // flag that controls if new documents are added to an existing directory or if a new index directory is created
    private boolean m_newIndex = false;

    private String m_indexPath = "";

    private boolean m_optimize = true;

    private Vector m_files = null;

    private String m_configPath = "";

    private int m_contentLenth=0;

    private PdfParser convPdf = null;

    private String m_contentType = "";

    private HtmlParser convHtml = null;


    // usage: IndexFiles <index-path> <file> ...
    /**
     *  Constructor for the IndexFiles object
     *
     *@param  indexPath   Description of the Parameter
     *@param  files       Description of the Parameter
     *@param  configPath  Description of the Parameter
     */
    public IndexFiles(String indexPath, Vector files, String configPath) {
        convPdf = new PdfParser();
        convHtml = new HtmlParser();
        m_indexPath = indexPath;
        m_files = files;
        m_configPath = configPath;
    }


    /**
     *  Main processing method for the IndexFiles object
     */
    public void run() {
        try {
            createIndexFiles();
        } catch(Exception ex) {

        }
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    public void createIndexFiles() throws Exception {
        IndexWriter writer = null;
        deleteIndexFiles();
        Document doc = null;
        //
        InputStream con = null;
        //

        try {
            //GermanAnalyzer ga=new GermanAnalyzer();
            StandardAnalyzer ga=new StandardAnalyzer();
            writer = new IndexWriter(m_indexPath, ga, m_newIndex);
            String completeContent = "";
            String keywords = "";
            String description = "";
            String title = "";
            String
                    parsedContent = "";
            String published = "";
            InputStream is = null;
            I_ContentParser parser = null;

            for(int i = 0; i < m_files.size(); i++) {
                if(debug) {
                    System.out.println("Indexing file " + m_files.elementAt(i));
                }
                doc = new Document();
                con = connectUrl((String) m_files.elementAt(i));
                if(m_contentType.equals("text/html")) {
                    parser = convHtml;
                } else if(m_contentType.equals("application/pdf")) {
                    parser = convPdf;
                } else {
                    continue;
                }

                //the html-Parsing and Indexing
                parser.parse(con);
                completeContent = parser.getContents();
                if(completeContent.indexOf("Not Found (404)") != -1 ||
                        completeContent.indexOf("HTTP Status 404") != -1 ||
                        completeContent.indexOf("Error 404") != -1) {
                    if(debug) {
                        System.out.println("Servermessage: Not Found (404)");
                    }
                    continue;
                }
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
                if(parser.getPublished() != null) {
                    published = parser.getPublished();
                }
                doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                doc.add(Field.Keyword("length", m_contentLenth+""));
                doc.add(Field.Keyword("keywords", keywords));
                doc.add(Field.Keyword("description", description));
                doc.add(Field.Keyword("modified", published));
                doc.add(Field.Keyword("title", title));
                doc.add(Field.Keyword("content", parsedContent));
                is = new ByteArrayInputStream(parsedContent.getBytes());

                if(debug) {
                    File output = new File("D:/Programme/Apache Group/Apache/htdocs/lucineTest/" + ((String) m_files.elementAt(i)).substring(((String) m_files.elementAt(i)).lastIndexOf("/"), ((String) m_files.elementAt(i)).lastIndexOf(".")) + ".txt");
                    FileOutputStream os = new FileOutputStream(output);
                    os.write(completeContent.getBytes());
                    os.close();
                }

                doc.add(Field.Text("body", (Reader) new InputStreamReader(is)));
                writer.addDocument(doc);
                is.close();
            }
            writer.optimize();
            if(debug) {
                System.out.println("Docs in Index " + writer.docCount());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    private void deleteIndexFiles() throws Exception {
        IndexReader reader = null;
        try {
            reader = IndexReader.open(m_indexPath);
            for(int i = 0; i < m_files.size(); i++) {
                Term urlTerm = new Term("path", (String) m_files.elementAt(i));
                if(reader.docFreq(urlTerm) > 0) {
                    if(debug) {
                        System.out.println("Number of docs " + reader.docFreq(new Term("path", (String) m_files.elementAt(i))));
                    }
                    if(reader.delete(urlTerm) == 1) {
                        if(debug) {
                            System.out.println("deleted from index " + (String) m_files.elementAt(i));
                        }
                    } else {
                        if(debug) {
                            System.out.println("not deleted from index " + (String) m_files.elementAt(i));
                        }
                    }
                }
            }
        } catch(Exception ex) {
            if(debug) {
                ex.printStackTrace();
            }
            createPath();
            if(!m_newIndex) {
                m_newIndex = true;
            }
            IndexDirectory.createIndexDirectory(m_indexPath);
            createIndexFiles();
        } finally {
            reader.close();
        }
        if(m_optimize) {
            IndexWriter writer = null;
            writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            writer.optimize();
            if(debug) {
                System.out.println("Docs in Index " + writer.docCount());
            }
            writer.close();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  con     Description of the Parameter
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
            m_contentLenth=urlCon.getContentLength();
            System.out.println("m_contentLenth="+m_contentLenth);
            m_contentType = urlCon.getContentType();
            if(debug) {
                System.out.println("connectUrl.theUrl=" + theUrl);
                System.out.println("connectUrl.getContentType()=" + m_contentType);
            }
            input = new DataInputStream(urlCon.getInputStream());
            if(debug) {
                System.out.println("' done!");
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
    public void setNewIndex(boolean newIndex) {
        m_newIndex = newIndex;
    }


    /**
     *  Gets the newIndex attribute of the IndexFiles object
     *
     *@return    The newIndex value
     */
    public boolean isNewIndex() {
        return m_newIndex;
    }


    /**
     *  Description of the Method
     */
    public void createPath() {
        File f = new File(m_indexPath);
        try {
            if(!f.exists()) {
                f.mkdirs();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

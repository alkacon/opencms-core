package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: IndexFiles.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/26 14:02:46 $
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
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 *  Description of the Class
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

    private PdfParser convPdf = null;

    private String m_contentType="";

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
        convHtml= new HtmlParser();
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
        } catch (Exception ex) {

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
        Document doc=null;
        //
        InputStream con=null;
        //

        try {
            writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            String completeContent = "", keywords="",description="",title="",parsedContent="";
            InputStream is = null;
            I_ContentParser parser= null;

            for (int i = 0; i < m_files.size(); i++) {
                if (debug) {
                    System.out.println("Indexing file " + m_files.elementAt(i));
                }
                doc = new Document();
                con=connectUrl((String) m_files.elementAt(i));
                if(m_contentType.equals("text/html")){
                    parser=convHtml;
                }else if(m_contentType.equals("application/pdf")){
                    parser=convPdf;
                }else continue;

                //the html-Parsing and Indexing
                //if (((String)m_files.elementAt(i)).toLowerCase().indexOf(".htm")!=-1){
                parser.parse(con);
                completeContent = parser.getContents();
                if (completeContent.indexOf("Not Found (404)") != -1 ||
                        completeContent.indexOf("HTTP Status 404") != -1 ||
                        completeContent.indexOf("Error 404") != -1) {
                    if (debug) System.out.println("Servermessage: Not Found (404)");
                    continue;
                }
                if (parser.getKeywords()!=null) keywords=parser.getKeywords();
                if (parser.getDescription()!=null)description=parser.getDescription();
                if (parser.getTitle()!=null)title=parser.getTitle();
                if (parser.getContents()!=null) parsedContent=parser.getContents();
                doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                doc.add(Field.Keyword("keywords", keywords));
                doc.add(Field.Keyword("description", description));
                doc.add(Field.Keyword("title", title));
                is = new ByteArrayInputStream(parsedContent.getBytes());
                //}
                //the PDF-Parsing and Indexing
                /*else {
                    doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                    convPdf.parse(con);
                    completeContent=convPdf.getContents();
                    if (completeContent.indexOf("Not Found (404)") != -1 ||
                            completeContent.indexOf("HTTP Status 404") != -1 ||
                            completeContent.indexOf("Error 404") != -1) {
                        if (debug) System.out.println("Servermessage: Not Found (404)");
                        continue;
                    }
                    if (convPdf.getKeywords()!=null) keywords=convPdf.getKeywords();
                    if (convPdf.getDescription()!=null)description=convPdf.getDescription();
                    if (convPdf.getTitle()!=null)title=convPdf.getTitle();
                    */
                    if (debug){
                        File output=new File("D:/Programme/Apache Group/Apache/htdocs/lucineTest/"+((String)m_files.elementAt(i)).substring(((String)m_files.elementAt(i)).lastIndexOf("/"),((String)m_files.elementAt(i)).lastIndexOf("."))+".txt");
                        FileOutputStream os=new FileOutputStream(output);
                        os.write(completeContent.getBytes());
                        os.close();
                    }
                    /*
                    doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                    doc.add(Field.Keyword("keywords", keywords));
                    doc.add(Field.Keyword("description", description));
                    doc.add(Field.Keyword("title", title));
                    is = new ByteArrayInputStream(completeContent.getBytes());
                }*/

                doc.add(Field.Text("body", (Reader) new InputStreamReader(is)));
                writer.addDocument(doc);
                is.close();
            }
            writer.optimize();
            if (debug) {
                System.out.println("Docs in Index " + writer.docCount());
            }
        } catch (Exception e) {
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
            for (int i = 0; i < m_files.size(); i++) {
                Term urlTerm = new Term("path", (String) m_files.elementAt(i));
                if (reader.docFreq(urlTerm) > 0) {
                    if (debug) {
                        System.out.println("Number of docs " + reader.docFreq(new Term("path", (String) m_files.elementAt(i))));
                    }
                    if (reader.delete(urlTerm) == 1) {
                        if (debug) {
                            System.out.println("deleted from index " + (String) m_files.elementAt(i));
                        }
                    } else {
                        if (debug) {
                            System.out.println("not deleted from index " + (String) m_files.elementAt(i));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
            createPath();
            if (!m_newIndex) {
                m_newIndex = true;
            }
            IndexDirectory.createIndexDirectory(m_indexPath);
            createIndexFiles();
        } finally {
            reader.close();
        }
        if (m_optimize) {
            IndexWriter writer = null;
            writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            writer.optimize();
            if (debug) {
                System.out.println("Docs in Index " + writer.docCount());
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

    private InputStream connectUrl(String theUrl) {
        int index = -1;
        StringBuffer content = null;
        URLConnection urlCon = null;
        DataInputStream input = null;
        String line = null;

        try {
            urlCon = new URL(theUrl).openConnection();
            urlCon.connect();
            m_contentType=urlCon.getContentType();
            if (debug) {
                System.out.println("connectUrl.theUrl=" + theUrl);
                System.out.println("connectUrl.getContentType()="+m_contentType);
            }
            input = new DataInputStream(urlCon.getInputStream());
            if (debug) System.out.println("' done!");
        } catch (Exception e) {
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
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

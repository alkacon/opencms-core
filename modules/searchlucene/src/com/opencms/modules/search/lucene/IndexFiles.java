package com.opencms.modules.search.lucene;

/*
 *  $RCSfile: IndexFiles.java,v $
 *  $Author: g.huhn $
 *  $Date: 2002/02/13 14:42:10 $
 *  $Revision: 1.1 $
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
import com.opencms.htmlconverter.*;

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

    private CmsHtmlConverter conv = null;


    // usage: IndexFiles <index-path> <file> ...
    /**
     *  Constructor for the IndexFiles object
     *
     *@param  indexPath   Description of the Parameter
     *@param  files       Description of the Parameter
     *@param  configPath  Description of the Parameter
     */
    public IndexFiles(String indexPath, Vector files, String configPath) {
        conv = new CmsHtmlConverter();
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
        try {
            writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            //writer = new IndexWriter(m_indexPath, new GermanAnalyzer(), m_newIndex);
            String completeContent = null;

            for (int i = 0; i < m_files.size(); i++) {
                if (debug) {
                    System.out.println("Indexing file " + m_files.elementAt(i));
                }
                completeContent = fetchContentForUrl((String) m_files.elementAt(i));
                if (completeContent.indexOf("Not Found (404)") != -1 ||
                        completeContent.indexOf("HTTP Status 404") != -1 ||
                        completeContent.indexOf("Error 404") != -1) {
                    if (debug) {
                        System.out.println("Servermessage: Not Found (404)");
                    }
                    continue;
                }

                InputStream is = new ByteArrayInputStream(filterContent(completeContent, m_configPath + "/content.xml").getBytes());
                Document doc = new Document();
                doc.add(Field.Keyword("path", (String) m_files.elementAt(i)));
                doc.add(Field.Keyword("keywords", filterContent(completeContent, m_configPath + "/keywords.xml")));
                doc.add(Field.Keyword("description", filterContent(completeContent, m_configPath + "/description.xml")));
                doc.add(Field.Keyword("title", filterContent(completeContent, m_configPath + "/title.xml")));
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
    public void deleteIndexFiles() throws Exception {
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
    private String fetchContentForUrl(String theUrl) {
        int index = -1;
        StringBuffer content = null;
        URLConnection urlCon = null;
        DataInputStream input = null;
        String line = null;

        try {
            if (debug) {
                System.out.print("Fetching URL '" + theUrl);
            }
            urlCon = new URL(theUrl).openConnection();
            urlCon.connect();
            input = new DataInputStream(urlCon.getInputStream());
            content = new StringBuffer();

            while ((line = input.readLine()) != null) {
                content = content.append(line);
            }
            input.close();
            if (debug) {
                System.out.println("' done!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }


    /**
     *  Description of the Method
     *
     *@param  content   Description of the Parameter
     *@param  confFile  Description of the Parameter
     *@return           Description of the Return Value
     */
    private String filterContent(String content, String confFile) {
        conv.setConverterConfFile(confFile);
        content = conv.convertHTML(content);
        if (debug) {
            System.out.println("filteredContent=" + content);
        }
        return content;
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

/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/FileUploadApplet.java,v $
 * Date   : $Date: 2003/10/28 13:28:41 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.applet.upload;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

/**
 * File Upload Applet, displays a file selector box to upload multiple resources into OpenCms.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public class FileUploadApplet extends JApplet implements Runnable {

    // Applet threat
    Thread m_runner = null;
    
    /** The URL of the opencms */
    private String m_opencms="";
    
    /** The URL to send the uploaded files to */
    private String m_targetUrl="";

    /** The URL to retun to after upload */    
    private String m_redirectUrl="";  
    
    /** The URL to retun to after an error */
    private String m_errorUrl="";  
    
    /** The name of the temporary zip file */
    private String m_zipfile="_tmpupload.zip";
    
    /** The name of the folder to upload to */
    private String m_uploadFolder="";
    
    /** Maximum file upload size */
    private long m_maxsize=-1;
    
    /** Number of resources to upload */
    private int m_resources=0;
    
    /** File extensions, used to find the correct icons for the selectbox */
    private String m_fileExtensions="";
    
    /** Color defintions */
    private HashMap m_colors = new HashMap();
    
    /** Output string for action messages */
    private String m_action="";
    
    /** Output string for loggin messages */
    private String m_message="";
    
    /** Output mode selector */
    private int m_outputMode=0;
    
    /** Counter for creating the progress bar */
    private int m_step=0;
    
    /** Definition of the images during upload */
    private Image m_source;
    private Image m_target;
    private Image m_floater;
    
    /** image position for the floater during upload */
    private int m_floaterPos=80;
    
    
    /** Defintion of output strings*/
    private String m_actionOutputSelect = "Seleting files for upload....";
    private String m_actionOutputCount = "Counting resources ....";
    private String m_actionOutputCreate = "Creating Zip-File...";
    private String m_actionOutputUpload = "Upload Zip-File";
    private String m_actionOutputError = "Error";
    private String m_messageNoPreview = "no preview available";
    private String m_errorLine1 = "An error has been occurred on the server:";
   
    private String m_messageOutputUpload = "Please wait, uploading data...";
    private String m_messageOutputAdding = "Adding ";
    private String m_messageOutputErrorSize = "Zip file too big:";
    private String m_messageOutputErrorZip = "Error creating Zip-File, see Java Console.";
    
    /** Definition variables for graphics output */
    private Font m_font;
    private FontMetrics m_metrics;
    private Image m_offscreen;
    private Graphics m_offgraphics;
    
    /** The file selector */    
    private JFileChooser m_fileSelector;
    
    

    /**
     * @see java.applet.Applet#init()
     */
    public void init() { 
        m_opencms = getParameter("opencms");
        m_targetUrl = getParameter("target");
        m_redirectUrl = getParameter("redirect");
        m_errorUrl = getParameter("error");
        m_uploadFolder = getParameter("filelist");
        String tmpSize = getParameter("maxsize");
        if (tmpSize!= null && tmpSize.length()>0) {
            m_maxsize=Long.parseLong(tmpSize);
        }      
        m_fileExtensions=getParameter("fileExtensions");
        m_colors=extractColors(getParameter("colors"));
    
        
        
        // setup the applet output
        m_font = new java.awt.Font("Verdana", Font.BOLD, 12);
        m_metrics = getFontMetrics(m_font);
        m_source=getImage(getCodeBase(), "images/folder_open.gif");
        m_target=getImage(getCodeBase(), "images/ocms.gif");
        m_floater=getImage(getCodeBase(), "images/floater.gif");
        
        // get the output massages in the correct language
        if (getParameter("actionOutputSelect")!=null) m_actionOutputSelect = getParameter("actionOutputSelect");
        if (getParameter("actionOutputCount")!=null) m_actionOutputCount = getParameter("actionOutputCount");
        if (getParameter("actionOutputCreate")!=null) m_actionOutputCreate = getParameter("actionOutputCreate");
        if (getParameter("actionOutputUpload")!=null) m_actionOutputUpload = getParameter("actionOutputUpload");
        if (getParameter("actionOutputError")!=null) m_actionOutputError = getParameter("actionOutputError");
        if (getParameter("messageOutputUpload")!=null) m_messageOutputUpload = getParameter("messageOutputUpload");
        if (getParameter("messageOutputAdding")!=null) m_messageOutputAdding = getParameter("messageOutputAdding");
        if (getParameter("messageOutputErrorZip")!=null) m_messageOutputErrorZip = getParameter("messageOutputErrorZip");
        if (getParameter("messageOutputErrorSize")!=null) m_messageOutputErrorSize = getParameter("messageOutputErrorSize");
        if (getParameter("messageNoPreview")!=null) m_messageNoPreview = getParameter("messageNoPreview");
        if (getParameter("errorLine1")!=null) m_errorLine1 = getParameter("errorLine1");
        
        
    }



    /**
     * @see java.applet.Applet#destroy()
     */
    public void destroy() { }

    /**
     * @see java.applet.Applet#start()
     */
    public void start() {
        m_runner = new Thread(this);
        m_runner.start();
    }

    /**
     * @see java.applet.Applet#stop()
     */
    public void stop() {
        m_runner = null;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            boolean ok=true;
            while (ok) {
                ok=true;
                //System.out.println("Version 1.37");
                m_message="";
                m_resources=0;
                m_step=0;
                // create a new file chooser
             
                if (m_fileSelector==null) {
                    m_fileSelector = new JFileChooser();
                }        
               
                // file selector can read files and folders
                m_fileSelector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                
                m_fileSelector.setDialogTitle(m_actionOutputSelect);
        
                // add two custom file filters (office and images) and the default filters
                m_fileSelector.addChoosableFileFilter(new ImageFilter());
                m_fileSelector.addChoosableFileFilter(new OfficeFilter());
                m_fileSelector.setAcceptAllFileFilterUsed(true);
                // enable multi-selection of files
                m_fileSelector.setMultiSelectionEnabled(true);
                // add custom icons for file types.
                m_fileSelector.setFileView(new ImageFileView(m_opencms, m_fileExtensions));
                // add the image preview pane.
                m_fileSelector.setAccessory(new ImagePreview(m_fileSelector, m_messageNoPreview));
                           
                m_action=m_actionOutputSelect;
                repaint();

                // show the file selector dialog
                int returnVal = m_fileSelector.showDialog(this, "OK");
 
    
                // process the results.
                if (returnVal == JFileChooser.APPROVE_OPTION) {       
                    // count all resources
                    m_outputMode=1;
                    m_action=m_actionOutputCount;
                    repaint();
                    m_resources=countResources(m_fileSelector.getSelectedFiles());   
                    // create the zipfile  
                    m_outputMode=2;
                    File targetFile= createZipFile(m_fileSelector.getSelectedFiles());
                    // check the size of the zip files
                    if (targetFile == null || m_maxsize>0 && targetFile.length() > m_maxsize) {
                        // show some details in the applet itself
                        m_outputMode=4;   
                        if (targetFile == null) {
                            m_message=m_messageOutputErrorZip;
                        } else {
                            m_message=m_messageOutputErrorSize+" "+targetFile.length() +" > "+ m_maxsize;
                        }
                        m_action=m_actionOutputError;
                        repaint();
                        // show an error-alertbog
                        JOptionPane.showMessageDialog(this, m_message, m_action, JOptionPane.ERROR_MESSAGE);                                  
                    } else {
                        m_outputMode=3;
                        m_message=m_messageOutputUpload+" ("+targetFile.length()/1024+" kb)";
                        repaint();             
                        // upload the zipfile
                        FileUploadThread uploadThreat=new FileUploadThread();
                    
                        uploadThreat.init(this);
                        uploadThreat.start();              
                    
                        uploadZipFile(targetFile);
                        ok=false;
                     }                
        
                } else {
                    //the cancel button was used, so go back to the workplace
                    ok=false;
                    getAppletContext().showDocument(new URL(m_redirectUrl), "explorer_files");
                }
            }
        }  catch (Exception e) {
            System.err.println(e);
        }
    }


    /**
     * Counts all resources to add to the zip file.<p>
     * 
     * @param files the files to be packed into the zipfile
     * @return number of resources
     */
    private int countResources(File[] files) {
        int count=0;
        // look through all selected resources
        for (int i=0; i<files.length; i++) {
            if (files[i].isFile()) {
                // its a file, count it
                count++;
            } else {
                // its a folder, count all resources in it and add the number
                count += countSubresources(files[i]);
            }
        }
        return count;
    } 


    /**
      * Counts all resources in a folder.<p>
      * 
      * @param folder the folder to count
      * @return number of resources
      */
    private int countSubresources(File folder) {
        int count=0;
        if (folder.isFile()) {
            // check if is really a folder
            count = 1;
        } else {
            // recurest to count
            count=countResources(folder.listFiles());
        }
        return count;            
    }


    /**
     * @see java.awt.Component#update(java.awt.Graphics)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * @see java.awt.Component#paint(Graphics)
     */
    public void paint(Graphics g) {
        
        // create the box
        m_offscreen = createImage(getSize().width, getSize().height);          
        m_offgraphics = m_offscreen.getGraphics();       
        m_offgraphics.setColor(getColor("bgColor"));  
        m_offgraphics.fillRect(0, 0, getSize().width, getSize().height);        
        m_offgraphics.setColor(getColor("outerBorderRightBottom"));
        m_offgraphics.drawLine(0, getSize().height - 1, getSize().width - 1, getSize().height - 1);  
        m_offgraphics.drawLine(getSize().width - 1, 0, getSize().width - 1, getSize().height - 1);  
        m_offgraphics.setColor(getColor("outerBorderLeftTop"));
        m_offgraphics.drawLine(0, 0, getSize().width - 1, 0);  
        m_offgraphics.drawLine(0, 0, 0, getSize().height - 1);   
        m_offgraphics.setColor(getColor("innerBorderRightBottom"));
        m_offgraphics.drawLine(1, getSize().height - 2, getSize().width - 2, getSize().height - 2);  
        m_offgraphics.drawLine(getSize().width - 2, 1, getSize().width - 2, getSize().height - 2);  
        m_offgraphics.setColor(getColor("innerBorderLeftTop"));
        m_offgraphics.drawLine(1, 1, getSize().width - 2, 1);  
        m_offgraphics.drawLine(1, 1, 1, getSize().height - 2);           
        m_offgraphics.setColor(getColor("bgHeadline"));
        m_offgraphics.fillRect(4, 4, getSize().width-5, 18);  

        m_offgraphics.setColor(getColor("innerBorderRightBottom"));
        m_offgraphics.drawLine(10, getSize().height - 11, getSize().width - 11, getSize().height - 11);  
        m_offgraphics.drawLine(getSize().width - 11, 25, getSize().width - 11, getSize().height - 11);  
        m_offgraphics.setColor(getColor("innerBorderLeftTop"));
        m_offgraphics.drawLine(10, 25, getSize().width - 11, 25);  
        m_offgraphics.drawLine(10, 25, 10, getSize().height - 11); 
  
  
        
        // draw title
        int cx = 10;
        int cy = 17;
        m_offgraphics.setFont(m_font);
        m_offgraphics.setColor(getColor("colorHeadline"));
        m_offgraphics.drawString(m_action, cx, cy);
        
        m_offgraphics.setColor(getColor("colorText"));
        //draw process message
        if (m_outputMode==3 || m_outputMode==4) {
           cx = Math.max((getSize().width - m_metrics.stringWidth(m_message)) / 2, 0);
        } else {
            cx = 25;
        }
        cy = 41;
        m_offgraphics.drawString(m_message, cx, cy);
        
        // draw process bar during zip creation
       if (m_outputMode==2) {
            float bar=new Float(m_step).floatValue()/new Float(m_resources).floatValue();
            String barText="("+m_step+" / "+m_resources+")";
            m_offgraphics.drawRect(25, 50, 450, 20);
            m_offgraphics.setColor(Color.white);           
            m_offgraphics.fillRect(26, 51, 449, 19);
            m_offgraphics.setColor(getColor("progessBar")); 
            m_offgraphics.fillRect(26, 51, new Float(bar*449).intValue(), 19);
            int progressWith = m_metrics.stringWidth(barText);
            cx = Math.max((getSize().width - progressWith) / 2, 0);
            cy = 64;
            m_offgraphics.setColor(Color.black); 
            m_offgraphics.drawString(barText, cx, cy);
        }

        // show nonsense during upload
        if (m_outputMode==3) {
            m_offgraphics.drawImage(m_source, 50, 59, this);
            m_offgraphics.drawImage(m_target, 440, 52, this);
            m_offgraphics.drawImage(m_floater, m_floaterPos, 59, this);         
        }
        
        // copy the offcreen graphics to the applet
        g.drawImage(m_offscreen, 0, 0, null);
    }


    /**
     * Move the floating upload image to right, wrap aroud on right side.<p>
     */
    public void moveFloater() {
        m_floaterPos += 10;
        if ((m_floaterPos) >410) {
            m_floaterPos=80;
        }   
        repaint();
    }

    
    /**
     * Creates a ZipFile from all files to upload.<p>
     * 
     * @param files the files to be packed into the zipfile
     * @return reference to the zipfile
     */
    private File createZipFile(File[] files) {
        File targetFile = null;
        m_action=m_actionOutputCreate;
        try {
            // create a new zipStream
            ZipOutputStream zipStream=new ZipOutputStream(new FileOutputStream(m_zipfile));
            // loop thorugh all files
            for (int i = 0; i < files.length; i++) {                          

                // if its a file, add it to the zipfile
                if (files[i].isFile()) {
                    addFileToZip(zipStream, files[i], files[i].getName());
                } else {
                    addFolderToZip(zipStream, files[i], "");                    
                }
                repaint();
                // free mem
                files[i]=null;          
            }            
            zipStream.close();
            // get the zipfile
            targetFile=new File(m_zipfile);
        } catch (Exception e) {
            System.err.println("Error creating zipfile "+e);            
        }
        return targetFile;
        
    }
    
    
    /**
     * Adds a single file to the zip output.<p>
     * @param zipStream the zip output stream
     * @param file the file to add to the stream
     * @param filename the name of the file to add
     * @throws Exception if something goes wrong
     */
    private void addFileToZip(ZipOutputStream zipStream, File file, String filename) throws Exception {
        // add to zipfile
        String name=filename;
        if (name.length() >40) {
            name="..."+name.substring(name.length()-40, name.length());
        }
        m_message=m_messageOutputAdding+" "+name+"..";
        m_step++;
        repaint();
        ZipEntry entry = new ZipEntry(filename);
        zipStream.putNextEntry(entry);
        zipStream.write(getFileBytes(file));
        zipStream.closeEntry();        
    }
    
    
    /**
     * Adds a folder and all subresources to the zip output.<p>
     * @param zipStream the zip output stream
     * @param file the file to add to the stream
     * @param prefix the foldername prefix
     * @throws Exception if something goes wrong
     */
    private void addFolderToZip(ZipOutputStream zipStream, File file, String prefix) throws Exception {
        String foldername=file.getName();
       
        prefix += "/"+foldername;
        // get all subresources
        File[] subresources=file.listFiles();
        // loop through the results                        
        for (int i=0; i<subresources.length; i++) {
            // add it its a file
            if (subresources[i].isFile()) {

                // use the prefix for the filename, since it inside the folder     
                addFileToZip(zipStream, subresources[i], prefix+"/"+subresources[i].getName());
            } else {
                // recurse into the subfolder
                addFolderToZip(zipStream, subresources[i], prefix);
            }
         }
    }
    
    
    
    /**
     * Uploads the zipfile to the OpenCms.<p>
     * @param uploadFile the zipfile to upload
     */
    private void uploadZipFile(File uploadFile) {     
         m_action=m_actionOutputUpload;
         repaint();
        
         MultipartPostMethod filePost = new MultipartPostMethod(m_targetUrl);       
  
         try {           
            filePost.addParameter(uploadFile.getName(), uploadFile);
             
            filePost.setQueryString("?"+getParameter("browserCookie")+"&STEP=1&unzip=true&filelist="+m_uploadFolder);
           
            filePost.addRequestHeader("Cookie", getParameter("browserCookie"));
            
            HttpClient client = new HttpClient();
             
            client.setConnectionTimeout(5000);
           
            int status = client.executeMethod(filePost);
           
            if (status == HttpStatus.SC_OK) {                                             
                //return to the workplace
                getAppletContext().showDocument(new URL(m_redirectUrl), "explorer_files");
            } else {
                // System.err.println("ERROR " +status);      
                // create the error text
                String error=m_errorLine1+"\n"+filePost.getStatusLine();
                //JOptionPane.showMessageDialog(this, error, m_errorTitle, JOptionPane.ERROR_MESSAGE);
                getAppletContext().showDocument(new URL(m_errorUrl+"?error="+error), "explorer_files");                        
            }
        } catch (Exception ex) {            
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
           // finally delete the zipFile on the harddisc
           uploadFile.delete();
        }
     }
     
   
     
    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * @return bytes[] the content of the file
     * @throws Exception if something goes wrong
     */
    private byte[] getFileBytes(File file) throws Exception {
        byte[] buffer = null;
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) { }
        }
    }

    /**
     * Extracts the colors from the parameter String.<p>
     * 
     * @param colors list of color names and values
     * @return HashMap with color names and values
     */
    private HashMap extractColors(String colors) {
        HashMap colorStorage=new HashMap();

        if (colors!=null) {
            StringTokenizer tok=new StringTokenizer(colors, ",");
            // loop through the tokens
            // all tokens have the format "extension=type"    
            while (tok.hasMoreElements()) {
                String token=tok.nextToken();
                // now extract the file extension and the type
                String colorName=token.substring(0, token.indexOf("="));
                String colorValue=token.substring(token.indexOf("=")+1);
                colorStorage.put(colorName, colorValue);                           
            }
        }
      return colorStorage;
    }    
    
    
    /**
     * Gets a color for drawing the ourput.<p>
     * @param colorName the name of the color
     * @return color
     */
    private Color getColor(String colorName) {
        Color col=Color.black;
        try {
            col = Color.decode((String)m_colors.get(colorName));            
        } catch (Exception e) {
            System.err.println("Error reading "+colorName+":"+e);            
        }
        return col;
    }


}


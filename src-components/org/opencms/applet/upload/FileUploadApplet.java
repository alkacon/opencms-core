/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/FileUploadApplet.java,v $
 * Date   : $Date: 2011/03/23 14:56:55 $
 * Version: $Revision: 1.34 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * File Upload Applet, displays a file selector box to upload multiple resources into OpenCms.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.34 $ 
 * 
 * @since 6.0.0 
 */
public class FileUploadApplet extends JApplet implements Runnable {

    /** The JSESSIONID cookie header name. */
    public static final String C_JSESSIONID = "JSESSIONID";

    /** The value for the resource upload applet action. */
    // Warning: This constant has to be kept in sync with the same named constant in 
    // org.opencms.explorer.CmsNewResourceUpload
    public static final String DIALOG_CHECK_OVERWRITE = "checkoverwrite";

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3710093915699772778L;

    /** Output string for action messages. */
    private String m_action = "";

    private String m_actionOutputCount = "Counting resources ....";

    private String m_actionOutputCreate = "Creating Zip-File...";

    private String m_actionOutputError = "Error";

    /** Definition of output strings.*/
    private String m_actionOutputSelect = "Seleting files for upload....";

    private String m_actionOutputUpload = "Upload Zip-File";

    private String m_actionOverwriteCheck = "Checking file existance on server...";

    /** Indicates if the applet certificate has been accepted. */
    private boolean m_certificateAccepted;

    private String m_certificateErrorMessage = "The required Applet certificate has not been accepted!";

    private String m_certificateErrorTitle = "Error initializing the OpenCms Upload Applet";

    /** The initial folder path for the file chooser. */
    private String m_clientFolder;

    /** Color definitions. */
    private HashMap m_colors = new HashMap();

    private String m_errorLine1 = "An error has occurred on the server:";

    /** The URL to return to after an error. */
    private String m_errorUrl = "";

    /** File extensions, used to find the correct icons for the select box. */
    private String m_fileExtensions = "";

    /** The type of gallery to upload to. */
    private String m_fileFilterSelection = "";

    /** The file selector. */
    private UploadAppletFileChooser m_fileSelector;

    private Image m_floater;
    /** Image position for the floater during upload. */
    private int m_floaterPos = 50;
    /** Definition variables for graphics output. */
    private Font m_font;

    /** Maximum file upload size. */
    private long m_maxsize = -1;

    /** Output string for logging messages. */
    private String m_message = "";
    private String m_messageNoPreview = "no preview available";
    private String m_messageOutputAdding = "Adding ";
    private String m_messageOutputErrorSize = "Zip file too big:";
    private String m_messageOutputErrorZip = "Error creating Zip-File, see Java Console.";
    private String m_messageOutputUpload = "Please wait, uploading data...";
    private FontMetrics m_metrics;
    private Graphics m_offgraphics;
    private Image m_offscreen;

    /** The URL of the OpenCms instance. */
    private String m_opencms = "";

    /** Output mode selector. */
    private int m_outputMode;

    private ModalDialog m_overwriteDialog;
    private String m_overwriteDialogCancel = "Cancel";
    private String m_overwriteDialogIntro = "The files listed below already exist on the server. \nAll checked files will be overwritten.";
    private String m_overwriteDialogLocale = "en";
    private String m_overwriteDialogOk = "Ok";
    private String m_overwriteDialogTitle = "Select the files to overwrite on the server";

    /** List of potential overwrites. */
    private List m_overwrites;

    /** The Target Frame to return to after uploading the files. */
    private String m_redirectTargetFrame = "";

    /** The URL to return to after uploading the files. */
    private String m_redirectUrl = "";

    /** Number of resources to upload. */
    private int m_resources;

    /** Applet thread. */
    private Thread m_runner;

    /** Definition of the images during upload. */
    private Image m_source;

    /** Counter for creating the progress bar. */
    private int m_step;

    private Image m_target;

    /** The URL to send the uploaded files to. */
    private String m_targetUrl = "";

    /** The name of the folder to upload to. */
    private String m_uploadFolder = "";

    /**
     * Adds a single file to the zip output.<p>
     * 
     * @param zipStream the zip output stream
     * @param file the file to add to the stream
     * @throws Exception if something goes wrong
     */
    private void addFileToZip(ZipOutputStream zipStream, File file) throws Exception {

        // add to zipfile
        String name = file.getAbsolutePath().replace('\\', '/');
        name = name.substring(m_fileSelector.getCurrentDirectory().getAbsolutePath().length());

        m_message = m_messageOutputAdding + " " + name + "..";
        m_step++;
        repaint();
        ZipEntry entry = new ZipEntry(name);
        zipStream.putNextEntry(entry);
        writeFileBytes(file, zipStream);
        zipStream.closeEntry();
    }

    private void addFolderToZip(ZipOutputStream zipStream, File file) throws Exception {

        File[] children = file.listFiles();
        File child;
        for (int i = 0; i < children.length; i++) {
            child = children[i];
            if (child.isDirectory()) {
                addFolderToZip(zipStream, child);
            } else {
                addFileToZip(zipStream, child);
            }
        }
    }

    /**
     * Returns the merge of both file arrays with no check for duplications. <p>
     * 
     * @param files first array of files 
     * 
     * @param overwriteFiles 2nd array of files 
     * 
     * @return the union of both file arrays
     */
    private File[] addOverwrites(File[] files, File[] overwriteFiles) {

        List result = new ArrayList(files.length + overwriteFiles.length);
        // faster for loop;)
        for (int i = files.length - 1; i >= 0; i--) {
            result.add(files[i]);
        }
        for (int i = overwriteFiles.length - 1; i >= 0; i--) {
            result.add(overwriteFiles[i]);
        }

        return (File[])result.toArray(new File[result.size()]);
    }

    /**
     * Checks if the given client files exist on the server and internally stores duplications.<p>
     * 
     * Comparison is made by cutting the current directory of the file chooser from the path of the given files. 
     * The server files (VFS files) to compare to are found by the current session of the user which finds the correct site and 
     * the knowledge about the current directory. File translation rules are taken into account on the server. <p>
     * 
     * @param files the local files to check if they exist in the VFS 
     * 
     * @return one of {@link ModalDialog#ERROR_OPTION} , {@link ModalDialog#CANCEL_OPTION}, {@link ModalDialog#APPROVE_OPTION}. 
     */
    int checkServerOverwrites(File[] files) {

        m_action = m_actionOverwriteCheck;
        repaint();
        int rtv = ModalDialog.ERROR_OPTION;
        // collect files
        List fileNames = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            getRelativeFilePaths(files[i], fileNames);
        }

        StringBuffer uploadFiles = new StringBuffer();
        Iterator it = fileNames.iterator();
        // Http post header is limited, therefore only a ceratain amount of files may be checked 
        // for server overwrites. Solution is: multiple requests. 
        int count = 0;
        List duplications;
        // request to server
        HttpClient client = new HttpClient();
        this.m_overwrites = new ArrayList();
        try {
            while (it.hasNext()) {
                count++;
                uploadFiles.append(((String)it.next())).append('\n');

                if (((count % 40) == 0) || (!it.hasNext())) {
                    // files to upload:
                    PostMethod post = new PostMethod(m_targetUrl);
                    Header postHeader = new Header("uploadFiles", URLEncoder.encode(uploadFiles.toString(), "utf-8"));
                    post.addRequestHeader(postHeader);
                    // upload folder in vfs: 
                    Header header2 = new Header("uploadFolder", URLEncoder.encode(getParameter("filelist"), "utf-8"));
                    post.addRequestHeader(header2);

                    // the action constant
                    post.setParameter("action", DIALOG_CHECK_OVERWRITE);

                    // add jsessionid query string
                    String sessionId = getParameter("sessionId");
                    String query = ";" + C_JSESSIONID.toLowerCase() + "=" + sessionId;
                    post.setQueryString(query);
                    post.addRequestHeader(C_JSESSIONID, sessionId);

                    HttpConnectionParams connectionParams = client.getHttpConnectionManager().getParams();
                    connectionParams.setConnectionTimeout(5000);

                    // add the session cookie
                    client.getState();
                    client.getHostConfiguration().getHost();

                    HttpState initialState = new HttpState();
                    URI uri = new URI(m_targetUrl, false);
                    Cookie sessionCookie = new Cookie(uri.getHost(), C_JSESSIONID, sessionId, "/", null, false);
                    initialState.addCookie(sessionCookie);
                    client.setState(initialState);
                    int status = client.executeMethod(post);

                    if (status == HttpStatus.SC_OK) {
                        String response = post.getResponseBodyAsString();
                        duplications = parseDuplicateFiles(URLDecoder.decode(response, "utf-8"));
                        this.m_overwrites.addAll(duplications);

                    } else {
                        // continue without overwrite check 
                        String error = m_errorLine1 + "\n" + post.getStatusLine();
                        System.err.println(error);
                    }

                    count = 0;
                    uploadFiles = new StringBuffer();
                }

            }
            if (m_overwrites.size() > 0) {
                rtv = showDuplicationsDialog(m_overwrites);
            } else {
                rtv = ModalDialog.APPROVE_OPTION;
            }

        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(System.err);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(System.err);
        }

        return rtv;
    }

    /**
     * Counts all resources to add to the zip file.<p>
     * 
     * @param files the files to be packed into the zipfile
     * @return number of resources
     */
    private int countResources(File[] files) {

        int count = 0;
        // look through all selected resources
        for (int i = 0; i < files.length; i++) {
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

        int count = 0;
        if (folder.isFile()) {
            // check if is really a folder
            count = 1;
        } else {
            // recurse to count
            count = countResources(folder.listFiles());
        }
        return count;
    }

    /**
     * Creates a ZipFile from all files to upload.<p>
     * 
     * @param files the files to be packed into the zipfile
     * @return reference to the zipfile
     */
    private File createZipFile(File[] files) {

        File targetFile = null;
        if (files.length > 0) {
            m_action = m_actionOutputCreate;
            try {
                // create a new zipStream
                String zipFileName = ".opencms_upload.zip";
                String userHome = System.getProperty("user.home");
                // create file in user home directory where write permissions should exist
                if (userHome != null) {
                    if (!userHome.endsWith(File.separator)) {
                        userHome = userHome + File.separator;
                    }
                    zipFileName = userHome + zipFileName;
                }
                ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFileName));
                // loop through all files
                for (int i = 0; i < files.length; i++) {

                    // if its a file, add it to the zipfile
                    if (files[i].isFile()) {
                        addFileToZip(zipStream, files[i]);
                    } else {

                        addFolderToZip(zipStream, files[i]);
                    }
                    repaint();
                    // free mem
                    files[i] = null;
                }
                zipStream.close();
                // get the zipfile
                targetFile = new File(zipFileName);
            } catch (Exception e) {
                System.err.println("Error creating zipfile " + getStackTraceAsString(e));
            }

        }
        return targetFile;
    }

    /**
     * Returns the stack trace (including the message) of an exception as a String.<p>
     * 
     * If the exception is a CmsException, 
     * also writes the root cause to the String.<p>
     * 
     * @param e the exception to get the stack trace from
     * @return the stack trace of an exception as a String
     */
    private static String getStackTraceAsString(Throwable e) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * @see java.applet.Applet#destroy()
     */
    @Override
    public void destroy() {

        // NOOP
    }

    /**
     * Displays an error message in case the applet could not be initialized.<p>
     */
    public void displayError() {

        m_outputMode = 5;
        m_action = m_certificateErrorTitle;
        m_message = m_certificateErrorMessage;

        JOptionPane.showMessageDialog(this, m_message, m_action, JOptionPane.ERROR_MESSAGE);

        try {
            // redirect back to the server
            getAppletContext().showDocument(new URL(m_redirectUrl), m_redirectTargetFrame);
        } catch (MalformedURLException e) {
            // this should never happen
            e.printStackTrace();
        }

        stop();
    }

    /**
     * Extracts the colors from the parameter String.<p>
     * 
     * @param colors list of color names and values
     * @return HashMap with color names and values
     */
    private HashMap extractColors(String colors) {

        HashMap colorStorage = new HashMap();

        if (colors != null) {
            StringTokenizer tok = new StringTokenizer(colors, ",");
            // loop through the tokens
            // all tokens have the format "extension=type"    
            while (tok.hasMoreElements()) {
                String token = tok.nextToken();
                // now extract the file extension and the type
                String colorName = token.substring(0, token.indexOf("="));
                String colorValue = token.substring(token.indexOf("=") + 1);
                colorStorage.put(colorName, colorValue);
            }
        }
        return colorStorage;
    }

    /**
     * Gets a color for drawing the output.<p>
     * 
     * @param colorName the name of the color
     * @return color
     */
    private Color getColor(String colorName) {

        Color col = Color.black;
        try {
            col = Color.decode((String)m_colors.get(colorName));
        } catch (Exception e) {
            System.err.println("Error reading " + colorName + ":" + e);
        }
        return col;
    }

    /**
     * Puts all given files (and all files in subtree of given potential "folder" file) into the given 
     * list (as {@link File} instances) with respect to the current file filter in the file chooser. 
     * <p>
     * 
     * 
     * @param file the file or folder to collect all subfiles of with respect to the current file chooser. 
     * 
     * @param fileNames all given files (and all files in subtree of given potential "folder" file) into the given 
     *      list (as {@link File} instances) with respect to the current file filter in the file chooser 
     */
    private void getFilesInTree(final File file, final List fileNames) {

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                getFilesInTree(children[i], fileNames);

            }
        } else {
            FileFilter filter = m_fileSelector.getFileFilter();
            if (filter.accept(file)) {
                fileNames.add(file);
            }
        }
    }

    /**
     * Puts the path of the given file in relation to the current root directory of the intenal 
     * file chooser with support for folder-recursion. <p>
     * 
     * 
     * @param file the file to put into relation to the root directory of the internal file chooser or a folder 
     *      of files to do this. 
     * 
     * @param fileNames will contain the result. 
     */
    private void getRelativeFilePaths(final File file, final List fileNames) {

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; i++) {
                getRelativeFilePaths(children[i], fileNames);

            }
        } else {
            FileFilter filter = m_fileSelector.getFileFilter();
            if (filter.accept(file)) {
                String rootDir = m_fileSelector.getCurrentDirectory().getAbsolutePath();
                int rootDirLength = rootDir.length();
                String filePath = file.getAbsolutePath();
                filePath = filePath.substring(rootDirLength);
                filePath = filePath.replace('\\', '/');
                fileNames.add(filePath);
            }
        }
    }

    /**
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {

        // has to be first before any gui components are created
        if (getParameter("overwriteDialogLocale") != null) {
            m_overwriteDialogLocale = getParameter("overwriteDialogLocale");
            Locale wpLocale = new Locale(m_overwriteDialogLocale);
            setLocale(wpLocale);
        }

        m_opencms = getParameter("opencms");
        m_targetUrl = getParameter("target");
        m_redirectUrl = getParameter("redirect");
        m_redirectTargetFrame = getParameter("targetframe");
        if ((m_redirectTargetFrame == null) || m_redirectTargetFrame.equals("")) {
            m_redirectTargetFrame = "explorer_files";
        }
        m_errorUrl = getParameter("error");
        m_uploadFolder = getParameter("filelist");
        m_fileFilterSelection = getParameter("filefilterselection");
        String tmpSize = getParameter("maxsize");
        if ((tmpSize != null) && (tmpSize.length() > 0)) {
            m_maxsize = Long.parseLong(tmpSize);
        }
        m_fileExtensions = getParameter("fileExtensions");
        m_colors = extractColors(getParameter("colors"));

        // setup the applet output
        m_font = new java.awt.Font(null, Font.BOLD, 12);
        m_metrics = getFontMetrics(m_font);
        m_source = getImage(getCodeBase(), "org/opencms/applet/upload/applet_source.png");
        m_target = getImage(getCodeBase(), "org/opencms/applet/upload/applet_target.png");
        m_floater = getImage(getCodeBase(), "org/opencms/applet/upload/floater.gif");

        // get the output massages in the correct language
        if (getParameter("actionOutputSelect") != null) {
            m_actionOutputSelect = getParameter("actionOutputSelect");
        }
        if (getParameter("actionOutputCount") != null) {
            m_actionOutputCount = getParameter("actionOutputCount");
        }
        if (getParameter("actionOutputCreate") != null) {
            m_actionOutputCreate = getParameter("actionOutputCreate");
        }
        if (getParameter("actionOverwriteCheck") != null) {
            m_actionOverwriteCheck = getParameter("actionOverwriteCheck");
        }
        if (getParameter("actionOutputUpload") != null) {
            m_actionOutputUpload = getParameter("actionOutputUpload");
        }
        if (getParameter("actionOutputError") != null) {
            m_actionOutputError = getParameter("actionOutputError");
        }
        if (getParameter("messageOutputUpload") != null) {
            m_messageOutputUpload = getParameter("messageOutputUpload");
        }
        if (getParameter("messageOutputAdding") != null) {
            m_messageOutputAdding = getParameter("messageOutputAdding");
        }
        if (getParameter("messageOutputErrorZip") != null) {
            m_messageOutputErrorZip = getParameter("messageOutputErrorZip");
        }
        if (getParameter("messageOutputErrorSize") != null) {
            m_messageOutputErrorSize = getParameter("messageOutputErrorSize");
        }
        if (getParameter("messageNoPreview") != null) {
            m_messageNoPreview = getParameter("messageNoPreview");
        }
        if (getParameter("errorLine1") != null) {
            m_errorLine1 = getParameter("errorLine1");
        }
        if (getParameter("certificateErrorTitle") != null) {
            m_certificateErrorTitle = getParameter("certificateErrorTitle");
        }
        if (getParameter("certificateErrorMessage") != null) {
            m_certificateErrorMessage = getParameter("certificateErrorMessage");
        }
        if (getParameter("overwriteDialogTitle") != null) {
            m_overwriteDialogTitle = getParameter("overwriteDialogTitle");
        }
        if (getParameter("overwriteDialogIntro") != null) {
            m_overwriteDialogIntro = getParameter("overwriteDialogIntro");
        }
        if (getParameter("overwriteDialogCancel") != null) {
            m_overwriteDialogCancel = getParameter("overwriteDialogCancel");
        }
        if (getParameter("overwriteDialogOk") != null) {
            m_overwriteDialogOk = getParameter("overwriteDialogOk");
        }
        if (getParameter("clientFolder") != null) {
            m_clientFolder = getParameter("clientFolder");
        }

        m_certificateAccepted = true;
        try {
            // set log factory to default log factory, otherwise commons logging detection will fail with an exception 
            System.setProperty(
                org.apache.commons.logging.LogFactory.FACTORY_PROPERTY,
                org.apache.commons.logging.LogFactory.FACTORY_DEFAULT);
        } catch (SecurityException e) {
            // this indicates the applet certificate has not been accepted
            m_certificateAccepted = false;
            e.printStackTrace();
        }
    }

    /**
     * Move the floating upload image to right, wrap around on right side.<p>
     */
    public void moveFloater() {

        m_floaterPos += 10;
        if ((m_floaterPos) > 430) {
            m_floaterPos = 50;
        }
        repaint();
    }

    /**
     * @see java.awt.Component#paint(Graphics)
     */
    @Override
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
        m_offgraphics.fillRect(4, 4, getSize().width - 5, 18);

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
        // draw process message
        if (m_outputMode >= 3) {
            cx = Math.max((getSize().width - m_metrics.stringWidth(m_message)) / 2, 0);
        } else {
            cx = 25;
        }
        cy = 41;
        m_offgraphics.drawString(m_message, cx, cy);

        // draw process bar during zip creation
        if (m_outputMode == 2) {
            float bar = new Float(m_step).floatValue() / new Float(m_resources).floatValue();
            String barText = "(" + m_step + " / " + m_resources + ")";
            m_offgraphics.drawRect(25, 50, 450, 20);
            m_offgraphics.setColor(Color.white);
            m_offgraphics.fillRect(26, 51, 449, 19);
            m_offgraphics.setColor(getColor("progessBar"));
            m_offgraphics.fillRect(26, 51, new Float(bar * 449).intValue(), 19);
            int progressWith = m_metrics.stringWidth(barText);
            cx = Math.max((getSize().width - progressWith) / 2, 0);
            cy = 64;
            m_offgraphics.setColor(Color.black);
            m_offgraphics.drawString(barText, cx, cy);
        }

        // show floater during upload
        if (m_outputMode == 3) {
            m_offgraphics.drawImage(m_floater, m_floaterPos, 57, this);
            m_offgraphics.drawImage(m_source, 30, 47, this);
            m_offgraphics.drawImage(m_target, 440, 47, this);
        }

        // copy the offcreen graphics to the applet
        g.drawImage(m_offscreen, 0, 0, null);
    }

    private List parseDuplicateFiles(String responseBodyAsString) {

        List result = new ArrayList();
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(
            responseBodyAsString.getBytes())));
        try {
            String trim;
            for (String read = reader.readLine(); read != null; read = reader.readLine()) {
                trim = read.trim();
                if (!(trim.equals("") || trim.equals("\n"))) {
                    // empty strings could happen if the serverside jsp is edited and has new unwanted linebreaks
                    result.add(read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        try {
            boolean ok = true;
            while (ok) {
                ok = true;

                m_message = "";
                m_resources = 0;
                m_step = 0;
                // create a new file chooser

                if (m_fileSelector == null) {
                    m_fileSelector = new UploadAppletFileChooser(this);
                }

                // file selector can read files and folders
                m_fileSelector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                m_fileSelector.setDialogTitle(m_actionOutputSelect);

                FileFilter imageFilter = new ImageFilter();
                FileFilter officeFilter = new OfficeFilter();
                FileFilter webFilter = new WebFilter();

                // add two custom file filters (office and images) and the default filters
                m_fileSelector.addChoosableFileFilter(imageFilter);
                m_fileSelector.addChoosableFileFilter(officeFilter);
                m_fileSelector.addChoosableFileFilter(webFilter);

                m_fileSelector.setAcceptAllFileFilterUsed(true);
                // enable multi-selection of files
                m_fileSelector.setMultiSelectionEnabled(true);
                // add custom icons for file types.
                m_fileSelector.setFileView(new ImageFileView(m_opencms, m_fileExtensions));
                // add the image preview pane.
                m_fileSelector.setAccessory(new ImagePreview(m_fileSelector, m_messageNoPreview));
                if ((m_clientFolder != null) && !m_clientFolder.trim().equals("")) {
                    File clientFolder = new File(m_clientFolder);
                    if (clientFolder.exists() && clientFolder.isDirectory()) {
                        m_fileSelector.setCurrentDirectory(clientFolder);
                    }
                }

                m_action = m_actionOutputSelect;

                // pre - selection of the filter: 
                if ((m_fileFilterSelection != null) && !m_fileFilterSelection.trim().equals("")) {
                    if (WebFilter.FILTER_ID.equals(m_fileFilterSelection)) {
                        m_fileSelector.setFileFilter(webFilter);
                    } else if (OfficeFilter.FILTER_ID.equals(m_fileFilterSelection)) {
                        m_fileSelector.setFileFilter(officeFilter);
                    } else if (ImageFilter.FILTER_ID.equals(m_fileFilterSelection)) {
                        m_fileSelector.setFileFilter(imageFilter);
                    }
                }
                repaint();
                m_overwrites = new ArrayList();
                int returnVal = m_fileSelector.showDialog(this, "OK");

                // process the results.
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // count all resources
                    m_outputMode = 1;
                    m_action = m_actionOutputCount;
                    repaint();

                    File[] files = m_fileSelector.getSelectedFiles();
                    List fileNames = new ArrayList();
                    for (int i = 0; i < files.length; i++) {
                        getRelativeFilePaths(files[i], fileNames);
                    }

                    if (m_overwrites.size() > 0) {
                        // subtract all duplicate files first
                        files = subtractDuplicates(fileNames, m_overwrites);
                        files = addOverwrites(
                            files,
                            ((FileSelectionPanel)m_overwriteDialog.getControlPanel().getComponent(1)).getSelectedFiles());
                    } else {

                        fileNames = new ArrayList();
                        for (int i = 0; i < files.length; i++) {
                            getFilesInTree(files[i], fileNames);
                        }
                        files = (File[])fileNames.toArray(new File[fileNames.size()]);
                    }

                    m_resources = countResources(files);

                    // create the zipfile  
                    m_outputMode = 2;
                    if (files.length > 0) {
                        File targetFile = createZipFile(files);
                        // check the size of the zip files
                        if ((targetFile == null) || ((m_maxsize > 0) && (targetFile.length() > m_maxsize))) {
                            // show some details in the applet itself
                            m_outputMode = 4;
                            if (targetFile == null) {
                                m_message = m_messageOutputErrorZip;
                            } else {
                                m_message = m_messageOutputErrorSize + " " + targetFile.length() + " > " + m_maxsize;
                            }
                            m_action = m_actionOutputError;
                            repaint();
                            // show an error-alertbog
                            JOptionPane.showMessageDialog(this, m_message, m_action, JOptionPane.ERROR_MESSAGE);
                        } else {
                            m_outputMode = 3;
                            m_message = m_messageOutputUpload + " (" + targetFile.length() / 1024 + " kb)";
                            repaint();
                            // upload the zipfile
                            FileUploadThread uploadThreat = new FileUploadThread();

                            uploadThreat.init(this);
                            uploadThreat.start();

                            uploadZipFile(targetFile);
                            ok = false;
                        }
                    } else {
                        // zero files were selected for upload (might be that all potential overwrites were deselected)
                        ok = false;
                        getAppletContext().showDocument(new URL(m_redirectUrl), m_redirectTargetFrame);
                    }

                } else {
                    //the cancel button was used, so go back to the workplace
                    ok = false;
                    getAppletContext().showDocument(new URL(m_redirectUrl), m_redirectTargetFrame);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Displays the dialog that shows the list of files that will be overwritten on the server.
     * <p>
     * The user may uncheck the checkboxes in front of the relative paths to avoid overwriting. 
     * <p>
     * 
     * @param duplications 
     *      a list of Strings that are relative paths to the files that will be overwritten on the server
     *      
     * @return one of 
     */
    private int showDuplicationsDialog(List duplications) {

        int rtv = ModalDialog.ERROR_OPTION;
        try {

            JTextArea dialogIntroPanel = new JTextArea();
            dialogIntroPanel.setLineWrap(true);
            dialogIntroPanel.setWrapStyleWord(true);
            dialogIntroPanel.setText(m_overwriteDialogIntro);
            dialogIntroPanel.setEditable(false);
            dialogIntroPanel.setBackground(m_fileSelector.getBackground());
            dialogIntroPanel.setFont(m_font);

            FileSelectionPanel selectionPanel = new FileSelectionPanel(
                duplications,
                m_fileSelector.getCurrentDirectory().getAbsolutePath());

            JPanel stacker = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridheight = 1;
            gbc.gridwidth = 1;
            gbc.weightx = 1f;
            gbc.weighty = 0f;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2, 2, 2, 2);

            stacker.add(dialogIntroPanel, gbc);

            gbc.weighty = 1f;
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 2, 0, 2);
            stacker.add(selectionPanel, gbc);

            m_overwriteDialog = new ModalDialog(
                m_fileSelector,
                m_overwriteDialogTitle,
                m_overwriteDialogOk,
                m_overwriteDialogCancel,
                stacker);
            m_overwriteDialog.setSize(new Dimension(560, 280));

            //dialog.setResizable(false);
            m_overwriteDialog.showDialog();
            rtv = m_overwriteDialog.getReturnValue();

        } catch (Throwable f) {
            f.printStackTrace(System.err);
        }
        return rtv;
    }

    /**
     * @see java.applet.Applet#start()
     */
    @Override
    public void start() {

        if (m_certificateAccepted) {
            // certificate was accepted, start upload thread
            m_runner = new Thread(this);
            m_runner.start();
        } else {
            // certificate was not accepted, show error message
            displayError();
        }
    }

    /**
     * @see java.applet.Applet#stop()
     */
    @Override
    public void stop() {

        m_runner = null;
    }

    /**
     * Return all files that are found in the first argument except a matching path suffix is found in the list 
     * of Strings given by the 2nd argument. <p>
     * 
     * @param fileNames the list of paths to diminish by matching path suffixes of the 2nd argument. 
     * 
     * @param duplications a list of Strings that contains "relative" paths (without absolute location)
     * 
     * @return all files that are found in the first argument except a matching path suffix is found in the list 
     *      of Strings given by the 2nd argument
     */
    private File[] subtractDuplicates(List fileNames, List duplications) {

        // subtract: 
        String path;
        Iterator itDuplications = duplications.iterator();
        while (itDuplications.hasNext()) {
            path = (String)itDuplications.next();
            fileNames.remove(path);
        }

        // no recreate the subtractor list to files: 
        List result = new ArrayList();
        File rootPath = m_fileSelector.getCurrentDirectory();
        Iterator it = fileNames.iterator();
        while (it.hasNext()) {
            path = (String)it.next();
            result.add(new File(rootPath, path));
        }
        return (File[])result.toArray(new File[result.size()]);
    }

    /**
     * @see java.awt.Component#update(java.awt.Graphics)
     */
    @Override
    public void update(Graphics g) {

        paint(g);
    }

    /**
     * Uploads the zipfile to the OpenCms.<p>
     * 
     * @param uploadFile the zipfile to upload
     */
    private void uploadZipFile(File uploadFile) {

        m_action = m_actionOutputUpload;
        repaint();

        PostMethod post = new PostMethod(m_targetUrl);

        try {
            Part[] parts = new Part[5];
            parts[0] = new FilePart(uploadFile.getName(), uploadFile);
            parts[1] = new StringPart("action", "submitform");
            parts[2] = new StringPart("unzipfile", "true");
            parts[3] = new StringPart("uploadfolder", m_uploadFolder);
            parts[4] = new StringPart("clientfolder", m_fileSelector.getCurrentDirectory().getAbsolutePath());

            HttpMethodParams methodParams = post.getParams();
            methodParams.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            MultipartRequestEntity request = new MultipartRequestEntity(parts, methodParams);
            post.setRequestEntity(request);

            // add jsessionid query string
            String sessionId = getParameter("sessionId");
            String query = ";" + C_JSESSIONID.toLowerCase() + "=" + sessionId;
            post.setQueryString(query);
            post.addRequestHeader(C_JSESSIONID, sessionId);

            HttpClient client = new HttpClient();
            HttpConnectionParams connectionParams = client.getHttpConnectionManager().getParams();
            connectionParams.setConnectionTimeout(5000);

            // add the session cookie
            client.getState();
            client.getHostConfiguration().getHost();

            HttpState initialState = new HttpState();
            URI uri = new URI(m_targetUrl, false);
            Cookie sessionCookie = new Cookie(uri.getHost(), C_JSESSIONID, sessionId, "/", null, false);
            initialState.addCookie(sessionCookie);
            client.setState(initialState);

            // no execute the file upload
            int status = client.executeMethod(post);

            if (status == HttpStatus.SC_OK) {
                //return to the specified url and frame target
                getAppletContext().showDocument(new URL(m_redirectUrl), m_redirectTargetFrame);
            } else {
                // create the error text
                String error = m_errorLine1 + "\n" + post.getStatusLine();
                //JOptionPane.showMessageDialog(this, error, "Error!", JOptionPane.ERROR_MESSAGE);
                getAppletContext().showDocument(
                    new URL(m_errorUrl + "?action=showerror&uploaderror=" + error),
                    "explorer_files");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
            // finally delete the zipFile on the harddisc
            uploadFile.delete();
        }
    }

    /**
     * Writes the bytes of the file to the zip output stream.<p>
     *
     * @param file the name of the file to read
     * 
     * @param out the zip outputstream
     * 
     * @throws Exception if something goes wrong
     */
    private void writeFileBytes(File file, OutputStream out) throws Exception {

        byte[] buffer = new byte[2048];
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            int readCount = 0;
            while ((charsRead < size) && (readCount != -1)) {
                readCount = fileStream.read(buffer);
                charsRead += readCount;
                out.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileStream != null) {
                    fileStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
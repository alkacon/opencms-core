/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/applet/upload/ImageFileView.java,v $
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
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;


/**
 * Image File View class, plugs image preview into the file selector box.<p>
 * 
 * Based on the Java 1.4 example.
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 *
 */
public class ImageFileView extends FileView {

    /** Extension storage */
    private HashMap m_extensions;

    /** The path to opencms, required to read the images */
    private String m_opencms;

    /**
     * Creates a new ImageFile Vew object.<p>
     */
    public ImageFileView() {
        super();      
    }

    /**
     * Creates a new ImageFile Vew object.<p>
     * 
     * @param opencms the complete path to opencms
     * @param fileExtensions list of file extensions to select the correct icons
     */
    public ImageFileView(String opencms, String fileExtensions) {
        super();
        m_opencms=opencms;
        m_extensions=extractExtensions(fileExtensions);   
                   
    }

    /**
     * Extracts the file extensions from the parameter String.<p>
     * 
     * @param fileExtensions list of file extensions and their file type
     * @return HashMap with file extension 
     */
    private HashMap extractExtensions(String fileExtensions) {
        HashMap extensions=new HashMap();
        // add a dummy extention for the folder
        fileExtensions += ",FOLDER=folder,";
        StringTokenizer tok=new StringTokenizer(fileExtensions, ",");
        // loop through the tokens
        // all tokens have the format "extension=type"    
        while (tok.hasMoreElements()) {
            String token=tok.nextToken();
            // now extract the file extension and the type
            String extension=token.substring(0, token.indexOf("="));
            String type=token.substring(token.indexOf("=")+1);
            // try to load the image     
           ImageIcon icon=createImageIcon(m_opencms+type+".gif");         
           extensions.put(extension, icon);                           
        }
      return extensions;
    }


    /**
     * @see javax.swing.filechooser.FileView#getName(java.io.File)
     */
    public String getName(File f) {
        return null; 
    }

    /**
     * @see javax.swing.filechooser.FileView#getDescription(java.io.File)
     */
    public String getDescription(File f) {
        return null; 
    }

    /**
     * @see javax.swing.filechooser.FileView#isTraversable(java.io.File)
     */
    public Boolean isTraversable(File f) {
        return null; 
    }


    /**
     * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
     */
    public Icon getIcon(File f) {
        String extension = FileUploadUtils.getExtension(f);
        // set the default icon
        Icon icon=null;        
        if (f.isDirectory()) {
            icon = (Icon)m_extensions.get("FOLDER");
        } else {
            icon=(Icon)m_extensions.get(extension);
        }
        // if no icon was found, set it to the default
        if (icon==null) {
            icon=(Icon)m_extensions.get("txt");
        }        
        return icon;
        }
        
        
    /**
     * Returns a resource image icon for a given path. <p>
     * 
     * The resource icon is chossen by the resource path, i.g. the resource type.
     * 
     * @param path the path of the resource
     * @return ImageIcon, or null if the path was invalid
     */
    private ImageIcon createImageIcon(String path) {
      try {         
       URL imgURL = new URL(path);    
       if (imgURL != null) {
           ImageIcon img=new ImageIcon(imgURL);        
           return img;
       } else {
           System.err.println("Couldn't find file: " + path);
           return null;
       }
       } catch (Exception e) {
           System.err.println(e);
           return null;
       }
    }
        
    }

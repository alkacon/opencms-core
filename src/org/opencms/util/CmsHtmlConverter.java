/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlConverter.java,v $
 * Date   : $Date: 2004/10/07 10:45:17 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.w3c.tidy.Tidy;

/**
 * Comment for <code>CmsHtmlParserBean</code>.<p>
 */
public class CmsHtmlConverter {
    
    /** constant for XHTML paring mode. */
    static final int C_MODE_XHTML = 0;
    
    /** constant for HTML parsing mode. */
    static final int C_MODE_HMTL = 1;
    
    /** constant for WORD-removal parsing mode. */
    static final int C_MODE_WORD = 2;
    
    /** the tidy to use. */
    Tidy m_tidy = null;
    
    /** the input encoding. */
    String m_encoding;
    
    /** the operation mode */
    int m_mode;

      
    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     * @param encoding the input encoding
     * @param mode the conversion mode, possible values are C_MODE_XHTML, C_MODE_HTML, C_MODE_WORD
     */
    public CmsHtmlConverter(String encoding, int mode) {
 
        m_tidy = new Tidy();
        m_encoding = encoding;    
        m_mode = mode;
        init();
    } 
    
    
    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     */
    public CmsHtmlConverter() {
        m_tidy = new Tidy();
        m_encoding = CmsEncoder.C_UTF8_ENCODING;
        m_mode = C_MODE_WORD;
        init();
    } 
    
    
    /**
     * Initializes the CmsHtmlConverter.<p>
     */
    private void init() {
        // add additional tags
        Properties additionalTags = new Properties();
        additionalTags.put("new-empty-tags", "o:SmartTagType, o:smarttagtype");
        additionalTags.put("new-inline-tags", "o:SmartTagType, o:smarttagtype");
        m_tidy.getConfiguration().addProps(additionalTags);
        
        // set the default tidy configuration
        m_tidy.setTidyMark(false);
        m_tidy.setMakeClean(true);
        m_tidy.setNumEntities(true);
        m_tidy.setPrintBodyOnly(true);
        
        switch (m_mode) {
            case C_MODE_HMTL:
                m_tidy.setXHTML(false);
                m_tidy.setWord2000(false);                
                break;
            case C_MODE_XHTML:
                m_tidy.setXHTML(true);
                m_tidy.setWord2000(true);                
            break;
            case C_MODE_WORD:
                m_tidy.setXHTML(false);
                m_tidy.setWord2000(true);                
            break;
            default:
                break;
        }
    }
    
    
    
    /**
     * Gets the  ncoding.<p>
     * 
     * @return encoding as string representation
     */
    public String getEncoding() {
        return m_encoding;
    }
        
    /**
     * Parses a string containing html code with different paring modes.<p>
     * 
     * @param htmlInput a string containing raw html code
     * @param encoding the  encoding
     * @return parsed and cleared html code
     */
    private byte[] parse(byte[] htmlInput, String encoding) {
        byte[] parsedHtml;
        
        // set the encoding
        m_tidy.setInputEncoding(encoding);
        m_tidy.setOutputEncoding(encoding);
        
        // prepare the streams
        ByteArrayInputStream in = new ByteArrayInputStream(htmlInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // do the parsing
        m_tidy.parse(in, out);
        parsedHtml = out.toByteArray();
                
        return parsedHtml;        
    }

    
    private String regExp(String htmlInput) {
        String parsedHtml = new String();
        parsedHtml = htmlInput;
        
        Pattern clearStyle = Pattern.compile("<o:smarttagtype");
        parsedHtml = clearStyle.matcher(parsedHtml).replaceAll("");

        
        return parsedHtml;
    }
    
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByte(byte[] htmlInput) {
        // parsing run 1, remove word tags
        byte[] parsedRun1 = parse(htmlInput, m_encoding);
        // parsing run 2, remove empty tags
        byte[] parsedRun2 = parse(parsedRun1, m_encoding);
        // parsing run 3, remove additional word tags wirh regexp
        return parsedRun2;
    }
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in astring
     * @return string contining the converted html
     * @throws Exception if something goes wrong
     */
    public String convertToString(String htmlInput) throws Exception {
        byte[] result = convertToByte(adjustHtml(htmlInput).getBytes(m_encoding)); 
        return new String(result, m_encoding);
    } 
     
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     */
    public String convertToStringSilent(String htmlInput) {
        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(
                    "[" + this.getClass().getName() + "] " + "Error converting HTML, ",
                    e);
            }
            return htmlInput;
        }
    } 
    

    private String adjustHtml(String htmlInput) {
        // we only have to do an adjustment id we are in WORD mode
        if (m_mode == C_MODE_WORD) {
            // check if we have some opening and closing html tags
            if ((htmlInput.toLowerCase().indexOf("<html>") == -1) &&
                (htmlInput.toLowerCase().indexOf("</html>") == -1)){
                // add a correct <html> tag with
                htmlInput = "<html xmlns:o=\"urn:schemas-microsoft-com:office:office\"><body>" 
                            + htmlInput
                            + "</body></html>";
            }
        }
        return htmlInput;
    }
    
    /**
     * Main method for the CmsHtmlParserBean, used for testing.<p>
     * 
     * @param args first parameter is the filename
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            File inputfile = new File(args[0]);            
            File outputfile = new File(args[1]);
            int mode = Integer.parseInt(args[2]);
            CmsHtmlConverter parser = new CmsHtmlConverter("UTF-8", mode);
            
            try {
                byte[] htmlInput = parser.getFileBytes(inputfile);
                // parsing run 1, remove word tags
                byte[] htmlOutput = parser.convertToByte(htmlInput);

                System.out.println(new String(htmlOutput, parser.m_encoding));
                
                parser.writeFileByte(htmlOutput, outputfile);
   
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Usage: CmsHtmlParser inputfile outputfile mode");
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
                if (fileStream != null) {
                    fileStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    /**
     * This writes the byte content of a resource to the file on the server
     * filesystem.<p>
     *
     * @param content the content of the file in the VFS
     * @param file the file in SFS that has to be updated with content
     * @throws Exception if something goes wrong
     */
    private void writeFileByte(byte[] content, File file) throws Exception {
        FileOutputStream fOut = null;
        DataOutputStream dOut = null;
        try {
            // write the content to the file in server filesystem
            fOut = new FileOutputStream(file);
            dOut = new DataOutputStream(fOut);
            dOut.write(content);
            dOut.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    } 
    
    
}

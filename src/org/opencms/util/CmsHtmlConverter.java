/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlConverter.java,v $
 * Date   : $Date: 2004/10/11 09:48:34 $
 * Version: $Revision: 1.4 $
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
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.w3c.tidy.Tidy;

/**
 * Html Parser, used to clean up html code (e.g. remove word tags) and created xhtml output.<p>
 *  *  
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsHtmlConverter {
    
    /** param value for disabled mode. **/
    public static final String C_PARAM_DISABLED = "false";    
    
    /** param value for enabled mode. **/
    public static final String C_PARAM_ENABLED = "true";

    /** param value for PRETTYPRINT mode. **/
    public static final String C_PARAM_PRETTYPRINT = "prettyprint";
    
    /** param value for WORD mode. **/
    public static final String C_PARAM_WORD = "cleanup-office";
    
    /** param value for XHTML mode. **/
    public static final String C_PARAM_XHTML = "XHTML";

    /** constant for disabled mode. */
    static final int C_MODE_DISABLED = 0;

    /** constant for enabled mode. */
    static final int C_MODE_ENABLED = 1;
    /** constant for WORD-removal parsing mode. */
    static final int C_MODE_WORD = 3;
    
    /** constant for XHTML parsing mode. */
    static final int C_MODE_XHTML = 2;
    
    
    /** the input encoding. */
    String m_encoding;
    
    /** the operation mode. */
    int m_mode;
    
    /** the tidy to use. */
    Tidy m_tidy;
    
    
    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     */
    public CmsHtmlConverter() {
        m_tidy = new Tidy();
        m_encoding = CmsEncoder.C_UTF8_ENCODING;
        init(C_PARAM_ENABLED);
    } 

      
    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     * @param encoding the input encoding
     * @param mode the conversion mode, possible values are C_MODE_XHTML, C_MODE_HTML, C_MODE_WORD
     */
    public CmsHtmlConverter(String encoding, String mode) {
 
        m_tidy = new Tidy();
        m_encoding = encoding;           
        init(mode);
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
            String mode = args[2];
            CmsHtmlConverter parser = new CmsHtmlConverter("UTF-8", mode);
            
            try {
                byte[] htmlInput = parser.getFileBytes(inputfile);

                String inputContent = new String(htmlInput, parser.m_encoding);
                inputContent = parser.adjustHtml(inputContent);
                
                byte[] htmlOutput = parser.convertToByte(inputContent);

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
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     * @throws Exception if something goes wrong
     */
    public byte[] convertToByte(byte[] htmlInput) throws Exception {
        // only do some parsing if the parsing mode is not set to disabled
        if (m_mode != C_MODE_DISABLED) {
            // parsing run 1, remove word tags
            byte[] parsedRun1 = parse(htmlInput, m_encoding);
            // parsing run 2, remove empty tags
            byte[] parsedRun2 = parse(parsedRun1, m_encoding);
            // parsing run 3, remove additional word tags wirh regexp
            return parsedRun2;
        } else {
            // the parsing mode was disabled, so return the oringinal value
            return htmlInput;
        }
    }
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     * @throws Exception if something goes wrong
     */
    public byte[] convertToByte(String htmlInput) throws Exception {
       return convertToByte(adjustHtml(htmlInput).getBytes(m_encoding));
    }
    
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(byte[] htmlInput) {
        try {
            return convertToByte(htmlInput);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(
                    "[" + this.getClass().getName() + "] " + " convertToByteSilent, error converting HTML, ",
                    e);
            }
            return htmlInput;
        }
    }    
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(String htmlInput) {
        try {
            return convertToByte(adjustHtml(htmlInput).getBytes(m_encoding));
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(
                    "[" + this.getClass().getName() + "] " + " convertToByteSilent, error converting HTML, ",
                    e);
            }
            try {
                return htmlInput.getBytes(m_encoding);
            } catch (UnsupportedEncodingException e1) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn(
                        "[" + this.getClass().getName() + "] " + " convertToByteSilent error converting HTML, ",
                        e1);
                }
                return htmlInput.getBytes();
            }
        }
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return string contining the converted html
     * @throws Exception if something goes wrong
     */
    public String convertToString(byte[] htmlInput) throws Exception {
        byte[] result = convertToByte(htmlInput); 
        return new String(result, m_encoding);
    } 
    
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
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
     * @return string contining the converted html
     */
    public String convertToStringSilent(byte[] htmlInput) {
        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(
                    "[" + this.getClass().getName() + "] " + " convertToStringSilent error converting HTML, ",
                    e);
            }
          try {
                return new String(htmlInput, m_encoding);
          } catch (UnsupportedEncodingException e1) {
              if (OpenCms.getLog(this).isWarnEnabled()) {
                  OpenCms.getLog(this).warn(
                      "[" + this.getClass().getName() + "] " + " convertToStringSilent error converting HTML, ",
                      e1);
              }
               return new String(htmlInput);
          }
        }
    } 
     
    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in string 
     * @return string contining the converted html
     */
    public String convertToStringSilent(String htmlInput) {
        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn(
                    "[" + this.getClass().getName() + "] " + " convertToStringSilent error converting HTML, ",
                    e);
            }
            return htmlInput;
        }
    } 
    
 
    /**
     * Gets the encoding.<p>
     * 
     * @return encoding as string representation
     */
    public String getEncoding() {
        return m_encoding;
    }
    

    /**
     * Adjusts the html input code in WORD mode if nescessary.<p>
     * 
     * When in WORD mode, the html tag must contain the xmlns:o="urn:schemas-microsoft-com:office:office"
     * attribute, otherwiese tide will not remove the WORD tags from the document.
     * 
     * @param htmlInput the html input
     * @return adjusted html input
     */
    private String adjustHtml(String htmlInput) {
        // we only have to do an adjustment id we are in WORD mode
        if (m_mode == C_MODE_WORD) {
            // check if we have some opening and closing html tags
            if ((htmlInput.toLowerCase().indexOf("<html>") == -1) 
            && (htmlInput.toLowerCase().indexOf("</html>") == -1)) {
                // add a correct <html> tag for word generated html
                htmlInput = "<html xmlns:o=\"urn:schemas-microsoft-com:office:office\">" 
                    + "<body>" 
                    + htmlInput
                    + "</body></html>";
            }
        }
        return htmlInput;
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
     * Initializes the CmsHtmlConverter.<p>
     * 
     * @param mode the mode parameter to select the operation mode of the converter. 
     */
    private void init(String mode) {
        // add additional tags
        // those are newscessary to handle word 2002+ documents
        Properties additionalTags = new Properties();
        additionalTags.put("new-empty-tags", "o:smarttagtype");
        additionalTags.put("new-inline-tags", "o:smarttagtype");
        m_tidy.getConfiguration().addProps(additionalTags);
        
        // set the default tidy configuration
        
        // disable the tidy meta element in output
        m_tidy.setTidyMark(false);
        // enable clean mode
        m_tidy.setMakeClean(true);
        // enable num entities
        m_tidy.setNumEntities(true);
        // create output of the body only
        m_tidy.setPrintBodyOnly(true);
        // force output creation even if there are tidy errors
        m_tidy.setForceOutput(true);
        // set tidy to quiet mode to prevent output        
        m_tidy.setQuiet(true);
        // disable warning output
        m_tidy.setShowWarnings(false);
        
        // set the operating mode     
        if (mode == null) {
            m_mode = C_MODE_DISABLED;
        } else if (mode.equals(C_PARAM_DISABLED)) {
            m_mode = C_MODE_DISABLED;
        } else if (mode.equals(C_PARAM_ENABLED)) {
            m_mode = C_MODE_ENABLED;
        } else if (mode.equals(C_PARAM_XHTML)) {
            m_mode = C_MODE_XHTML;
        } else if (mode.equals(C_PARAM_WORD)) {
           m_mode = C_MODE_WORD;   
        } else {
            m_mode = C_MODE_DISABLED;
        }

        // confiugurate the tidy depending on the operation mode
        switch (m_mode) {
            case C_MODE_ENABLED:
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

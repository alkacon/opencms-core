/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/CmsHtmlReport.java,v $
 * Date   : $Date: 2004/01/22 11:50:01 $
 * Version: $Revision: 1.10 $
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
 
package org.opencms.report;

import org.opencms.util.CmsStringSubstitution;

import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * HTML report output to be used for import / export / publish operations 
 * in the entire OpenCms system.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.10 $
 */
public class CmsHtmlReport extends A_CmsReport {
    
    /** Constant for a HTML linebreak with added "real" line break */
    private static final String C_LINEBREAK = "<br>";
    
    /** 
     * Constant for a HTML linebreak with added "real" line break- 
     * traditional style for report threads that still use XML templates for their output
     */
    private static final String C_LINEBREAK_TRADITIONAL = "<br>\\n";    
    
    /** The list of report objects e.g. String, CmsPageLink, Exception ... */
    private List m_content;

    /**
     * Counter to remember what is already shown,
     * indicates the next index of the m_content list that has to be reported
     */
    private int m_indexNext;        
    
    /** Flag to indicate if exception should be displayed long or short */
    private boolean m_showExceptionStackTracke;
    
    /** Boolean flag indicating whether this report should generate HTML or JavaScript output */
    private boolean m_writeHtml;
        
    /**
     * Constructs a new report using the provided locale and the default OpenCms 
     * workplace resource bundle for the output language.<p>
     * 
     * @param locale a 2-letter language code according to ISO 639
     * @see I_CmsReport#C_BUNDLE_NAME 
     */    
    public CmsHtmlReport(String locale) {
        this(C_BUNDLE_NAME, locale, false);
    }
    
    /**
     * Constructs a new report using the provided locale and the default OpenCms 
     * workplace resource bundle for the output language.<p>
     * 
     * This constructor is reserved for older report threads that still use
     * XML templates to generate their output. It allows to specifiy to write 
     * HTML instead of JavaScript output.<p>
     * 
     * @param locale a 2-letter language code according to ISO 639
     * @param writeHtml true, if this report should generate HTML instead of JavaScript output
     * @see I_CmsReport#C_BUNDLE_NAME 
     */    
    public CmsHtmlReport(String locale, boolean writeHtml) {
        this(C_BUNDLE_NAME, locale, writeHtml);
    }    
    
    /**
     * Constructs a new report using the provided locale and resource bundle
     * for the output language.<p>
     * 
     * @param locale a 2-letter language code according to ISO 639 
     * @param bundleName the name of the resource bundle with localized strings
     */    
    public CmsHtmlReport(String bundleName, String locale) {
        this(bundleName, locale, false);
    }
    
    /**
     * Constructs a new report using the provided locale and resource bundle
     * for the output language.<p>
     * 
     * This constructor is reserved for older report threads that still use
     * XML templates to generate their output. It allows to specifiy to write 
     * HTML instead of JavaScript output.<p>
     *  
     * @param bundleName the name of the resource bundle with localized strings
     * @param locale a 2-letter language code according to ISO 639 
     * @param writeHtml true, if this report should generate HTML instead of JavaScript output
     */
    protected CmsHtmlReport(String bundleName, String locale, boolean writeHtml) {
        init();
        addBundle(bundleName, locale);
        
        m_content = new ArrayList(256);
        m_showExceptionStackTracke = true;   
        m_writeHtml = writeHtml;
    }
    
    /**
     * Converts chars and removes linebreaks from a String.<p>
     * 
     * @param value the String to convert
     * @return the char converted String without linebreaks
     */
    private String convertChars(String value) {
        value = CmsStringSubstitution.substitute(value, "\"", "\\\"");

        StringBuffer buf = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(value, "\r\n");
        while (tok.hasMoreTokens()) {
            buf.append(tok.nextToken());
            buf.append(" ");
        }
        
        return buf.toString();
    }

    /**
     * Output helper method to format a reported <code>Throwable</code> element.<p>
     * 
     * This method ensures that exception stack traces are properly escaped
     * when they are added to the report.<p>
     * 
     * There is a member variable {@link #m_showExceptionStackTracke} in this
     * class that controls if the stack track is shown or not.
     * In a later version this might be configurable on a per-user basis.<p>
     *      
     * @param throwable the exception to format
     * @return the formatted StringBuffer
     */
    private StringBuffer getExceptionElement(Throwable throwable) {        
        StringBuffer buf = new StringBuffer();
        
        if (!m_writeHtml) {
            if (m_showExceptionStackTracke) {
                buf.append("aT('");
                buf.append(key("report.exception"));
                String exception = Encoder.escapeXml(Utils.getStackTrace(throwable));
                exception = CmsStringSubstitution.substitute(exception, "\\", "\\\\");
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    buf.append(tok.nextToken());
                    buf.append(getLineBreak());
                }
                buf.append("'); ");
                buf.append(getLineBreak());
                m_content.add(buf);                
            } else {
                buf.append("aT('");
                buf.append(key("report.exception"));
                buf.append(throwable.toString());
                buf.append("'); ");
                buf.append(getLineBreak());
                m_content.add(buf);                  
            }
        } else {
            if (m_showExceptionStackTracke) {
                buf.append("<span class='throw'>");
                buf.append(key("report.exception"));
                String exception = Encoder.escapeXml(Utils.getStackTrace(throwable));
                exception = CmsStringSubstitution.substitute(exception, "\\", "\\\\");
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    buf.append(tok.nextToken());
                    buf.append(getLineBreak());
                }
                buf.append("</span>");
            } else {
                buf.append("<span class='throw'>");
                buf.append(key("report.exception"));
                buf.append(throwable.toString());
                buf.append("</span>");
                buf.append(getLineBreak());
            }
        }

        return buf;
    } 
    
    /**
     * Returns the corrent linebreak notation depending on the output style of thsi report.
     * 
     * @return the corrent linebreak notation
     */
    protected String getLineBreak() {
        return m_writeHtml ? C_LINEBREAK_TRADITIONAL : C_LINEBREAK;
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {
        StringBuffer result = new StringBuffer();
        int indexEnd = m_content.size();
        for (int i=m_indexNext; i<indexEnd; i++) {
            Object obj = m_content.get(i); 
            if (obj instanceof String || obj instanceof StringBuffer) {
                result.append(obj);
            } else if (obj instanceof Throwable) {
                result.append(getExceptionElement((Throwable)obj));
            }
        }
        m_indexNext = indexEnd;

        return result.toString();
    }  

    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String)
     */
    public synchronized void print(String value) {
        this.print(value, C_FORMAT_DEFAULT);
    }
    
    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String, int)
     */
    public synchronized void print(String value, int format) {
        value = convertChars(value);
        StringBuffer buf;

        if (!m_writeHtml) {
            switch (format) {
                case C_FORMAT_HEADLINE :
                    buf = new StringBuffer();
                    buf.append("aH('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case C_FORMAT_WARNING :
                    buf = new StringBuffer();
                    buf.append("aW('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case C_FORMAT_NOTE :
                    buf = new StringBuffer();
                    buf.append("aN('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case C_FORMAT_OK :
                    buf = new StringBuffer();
                    buf.append("aO('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case C_FORMAT_DEFAULT :
                default :
                    buf = new StringBuffer();
                    buf.append("a('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
            }
            
            // the output lines get split back into single lines on the client-side.
            // thus, a separate JavaScript call has to be added here to tell the
            // client that we want a linebreak here...
            if (value.trim().endsWith(getLineBreak())) {
                buf.append("aB(); ");
            }            
        } else {
            // TODO remove this code when all reports are switched from XML templates to JSP pages
            switch (format) {
                case C_FORMAT_HEADLINE :
                    buf = new StringBuffer();
                    buf.append("<span class='head'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case C_FORMAT_WARNING :
                    buf = new StringBuffer();
                    buf.append("<span class='warn'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case C_FORMAT_NOTE :
                    buf = new StringBuffer();
                    buf.append("<span class='note'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case C_FORMAT_OK :
                    buf = new StringBuffer();
                    buf.append("<span class='ok'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case C_FORMAT_DEFAULT :
                default :
                    m_content.add(value);
            }
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public synchronized void println() {
        this.print(getLineBreak());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String)
     */
    public synchronized void println(String value) {
        this.print(value + getLineBreak(), C_FORMAT_DEFAULT);
    }
    
    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String, int)
     */
    public synchronized void println(String value, int format) {
        this.print(value + getLineBreak(), format);        
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {
        m_content.add(t);
    }
    
}

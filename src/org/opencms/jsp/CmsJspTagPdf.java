/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.pdftools.CmsPdfLink;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * JSP tag to generate a link to a PDF produced from a given XML content.<p>
 */
public class CmsJspTagPdf extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagPdf.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The path of the content resource for which the PDF link should be generated. */
    private String m_content;

    /** File name (optional). */
    private String m_filename;

    /** The path of the JSP used to generate the XHTML for the content (which is used to generate the PDF). */
    private String m_format;

    /** The locale attribute. */
    private String m_locale;

    /** Parameter encoding. */
    private String m_paramEncoding;

    /** The map of parameters. */
    private SortedMap<String, String> m_parameters;

    /**
     * The implementation of the tag.<p>
     *
     * @param request the current request
     * @param format the format path
     * @param content the content path
     * @param localeStr the name of the locale to include in the PDF link
     * @param params map of parameters
     * @param paramEncoding the character encoding to use for URL parameters
     *
     * @return the link to the PDF
     *
     * @throws CmsException if something goes wrong
     */
    public static String pdfTagAction(
        ServletRequest request,
        String format,
        String content,
        String localeStr,
        String filename,
        SortedMap<String, String> params,
        String paramEncoding)
    throws CmsException {

        filename = filename != null ? filename.replaceFirst("\\.pdf$", "") : null;
        CmsFlexController controller = CmsFlexController.getController(request);
        CmsObject cms = OpenCms.initCmsObject(controller.getCmsObject());
        if (localeStr != null) {
            Locale localeObj = CmsLocaleManager.getLocale(localeStr);
            cms.getRequestContext().setLocale(localeObj);
        }
        CmsResource formatterRes = cms.readResource(format);
        CmsResource contentRes = cms.readResource(content, CmsResourceFilter.ignoreExpirationOffline(cms));
        CmsPdfLink pdfLink = new CmsPdfLink(cms, formatterRes, contentRes, filename);
        StringBuilder paramBuf = new StringBuilder();
        if ((params != null) && !params.isEmpty()) {
            paramBuf.append("?");
            List<String> paramList = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    paramList.add(
                        URLEncoder.encode(entry.getKey(), paramEncoding)
                            + "="
                            + URLEncoder.encode(entry.getValue(), paramEncoding));
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            paramBuf.append(CmsStringUtil.listAsString(paramList, "&amp;"));
        }
        return pdfLink.getLink() + paramBuf.toString();
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        m_parameters.put(name, value);

    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    @Override
    public int doEndTag() {

        try {
            pageContext.getOut().print(
               pdfTagAction(
                    pageContext.getRequest(),
                    m_format,
                    m_content,
                    m_locale,
                    m_filename,
                    m_parameters,
                    getParamEncoding()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        m_parameters = new TreeMap<>();
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Setter for the content path.<p>
     *
     * @param content the content path
     */
    public void setContent(String content) {

        m_content = content;
    }

    /**
     * Sets the file name for the PDF download link.
     *
     * @param filename the file name
     */
    public void setFilename(String filename) {

        m_filename = filename;
    }

    /**
     * Setter for the format path.<p>
     *
     * @param format the format path
     */
    public void setFormat(String format) {

        m_format = format;
    }

    /**
     * Sets the locale to use for the PDF link.<p>
     *
     * @param locale the locale to use
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the parameter encoding.
     *
     * @param encoding the parameter encoding
     */
    public void setParamEncoding(String encoding) {

        m_paramEncoding = encoding;
    }

    /**
     * Gets the parameter encoding.
     *
     * @return the parameter encoding
     */
    private String getParamEncoding() {

        if (m_paramEncoding != null) {
            return m_paramEncoding;
        }
        return "UTF-8";
    }

}

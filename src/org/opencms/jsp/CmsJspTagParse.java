/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagParse.java,v $
 * Date   : $Date: 2005/12/14 10:20:41 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsHtmlNodeVisitor;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

import org.htmlparser.util.ParserException;

/**
 * Implements the <code>&lt;cms:parse&gt;&lt;/cms:parse&gt;</code> tag to allow parsing of nested
 * HTML with the visitor/parser implementation specified by the "visitorClass" attribute.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.1.3
 */
public class CmsJspTagParse extends BodyTagSupport {

    /** The name of the mandatory Tag attribute for the visitor class an instance of will be guided throught the body content. */
    public static final String ATT_VISITOR_CLASS = "visitorClass";

    /** Tag name constant for log output. */
    public static final String TAG_NAME = "parse";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagParse.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6541745426202242240L;

    /** The attribute value of the param attribute - currently unused. */
    private String m_param;

    /** The visitor / parser classname to use. */
    private String m_visitorClassname;

    /**
     * Internal action method.
     * <p>
     * 
     * Parses (and potentially transforms) a HTMl content block.
     * <p>
     * 
     * @param content the content to be parsed / transformed.
     * 
     * @param context needed for getting the encoding / the locale.
     * 
     * @param visitor the visitor / parser to use.
     * 
     * @return the transformed content.
     * 
     */
    public static String parseTagAction(String content, PageContext context, I_CmsHtmlNodeVisitor visitor) {

        String result = null;
        CmsRequestContext cmsContext = CmsFlexController.getCmsObject(context.getRequest()).getRequestContext();

        if (visitor == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(
                    cmsContext.getLocale(),
                    Messages.GUI_ERR_TAG_ATTRIBUTE_MISSING_2,
                    new Object[] {TAG_NAME, ATT_VISITOR_CLASS}));
            }
            result = content;
        } else {

            String encoding = cmsContext.getEncoding();
            try {
                visitor.process(content, encoding);
                result = visitor.getResult();

            } catch (ParserException e) {

                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(
                        cmsContext.getLocale(),
                        Messages.ERR_PROCESS_TAG_1,
                        new Object[] {TAG_NAME}), e);
                }
            }

        }
        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @return EVAL_PAGE
     * @throws JspException in case soemthing goes wrong
     */
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        I_CmsHtmlNodeVisitor visitor;

        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {
            String content = "";
            try {
                if (CmsStringUtil.isEmpty(m_visitorClassname)) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(
                            Messages.GUI_ERR_TAG_ATTRIBUTE_MISSING_2,
                            new Object[] {TAG_NAME, ATT_VISITOR_CLASS}));
                    }

                }
                // wrong attribute visitorClass -> content will remain empty, but no exception is
                // thrown
                try {
                    // load
                    Class cl = Class.forName(m_visitorClassname);
                    // instanciate
                    Object instance = cl.newInstance();
                    // cast
                    visitor = (I_CmsHtmlNodeVisitor)instance;
                    content = parseTagAction(getBodyContent().getString(), pageContext, visitor);

                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(
                            Messages.GUI_ERR_TAG_ATTRIBUTE_INVALID_3,
                            new Object[] {TAG_NAME, ATT_VISITOR_CLASS, I_CmsHtmlNodeVisitor.class.getName()}));

                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                }

            } finally {
                try {
                    getBodyContent().clear();
                    getBodyContent().print(content);
                    getBodyContent().writeOut(pageContext.getOut());
                    // need to release manually, JSP container may not call release as required (happens with Tomcat)
                    release();

                } catch (Exception ex) {
                    release();
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.ERR_PROCESS_TAG_1, TAG_NAME), ex);
                    }
                    // this is severe
                    throw new JspException(ex);
                }

            }

        }
        return EVAL_PAGE;
    }

    /**
     * Returns the param.
     * <p>
     * 
     * @return the param
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns the visitorClass.
     * <p>
     * 
     * @return the visitorClass
     */
    public String getVisitorClass() {

        return m_visitorClassname;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {

        m_visitorClassname = null;
        m_param = null;
        super.release();
    }

    /**
     * Sets the param.
     * <p>
     * 
     * @param param the param to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Sets the visitorClass.
     * <p>
     * 
     * @param visitorClass the visitorClass to set
     */
    public void setVisitorClass(String visitorClass) {

        m_visitorClassname = visitorClass;
    }

}

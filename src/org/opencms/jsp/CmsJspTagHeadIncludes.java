/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagHeadIncludes.java,v $
 * Date   : $Date: 2011/04/21 10:31:39 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * This tag includes required css or javascript resources, place in html/head.
 * Required resources can be configured in the resource type schema.
 * Set attribute type to 'css' to include css resources or to 'javascript' to include java-script resources.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0
 */
public class CmsJspTagHeadIncludes extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** The include type CSS. */
    public static final String TYPE_CSS = "css";

    /** The include type java-script. */
    public static final String TYPE_JAVASCRIPT = "javascript";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagHeadIncludes.class);

    /** Serial version UID required for safe serialisation. */
    private static final long serialVersionUID = 5496349529835666345L;

    /** Map to save parameters to the include in. */
    private Map<String, String[]> m_parameterMap;

    /** The include type. */
    private String m_type;

    /**
     * Adds parameters to a parameter Map that can be used for a http request.<p>
     * 
     * @param parameters the Map to add the parameters to
     * @param name the name to add
     * @param value the value to add
     * @param overwrite if <code>true</code>, a parameter in the map will be overwritten by
     *      a parameter with the same name, otherwise the request will have multiple parameters 
     *      with the same name (which is possible in http requests)
     */
    public static void addParameter(Map<String, String[]> parameters, String name, String value, boolean overwrite) {

        // No null values allowed in parameters
        if ((parameters == null) || (name == null) || (value == null)) {
            return;
        }

        // Check if the parameter name (key) exists
        if (parameters.containsKey(name) && (!overwrite)) {
            // Yes: Check name values if value exists, if so do nothing, else add new value
            String[] values = parameters.get(name);
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
            parameters.put(name, newValues);
        } else {
            // No: Add new parameter name / value pair
            String[] values = new String[] {value};
            parameters.put(name, values);
        }
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        // No null values allowed in parameters
        if ((name == null) || (value == null)) {
            return;
        }

        // Check if internal map exists, create new one if not
        if (m_parameterMap == null) {
            m_parameterMap = new HashMap<String, String[]>();
        }

        addParameter(m_parameterMap, name, value, false);

    }

    /**
     * @return <code>EVAL_PAGE</code>
     * 
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * 
     * @throws JspException by interface default
     */
    @Override
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();
        try {
            if (TYPE_CSS.equals(m_type)) {
                tagCssAction(cms, req);
            }
            if (TYPE_JAVASCRIPT.equals(m_type)) {
                tagJSAction(cms, req);
            }
        } catch (Exception e) {
            throw new JspException(e);
        }

        return EVAL_PAGE;

    }

    /**
     * Returns <code>{@link #EVAL_BODY_BUFFERED}</code>.<p>
     * 
     * @return <code>{@link #EVAL_BODY_BUFFERED}</code>
     * 
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Action to include the CSS resources.<p>
     * 
     * @param cms the current cms context
     * @param req the current request
     * 
     * @throws CmsException if something goes wrong reading the resources
     * @throws IOException if something goes wrong writing to the response out
     */
    public void tagCssAction(CmsObject cms, ServletRequest req) throws CmsException, IOException {

        CmsJspStandardContextBean standardContext = getStandardContext(cms, req);
        CmsContainerPageBean containerPage = standardContext.getPage();

        Set<String> cssIncludes = new LinkedHashSet<String>();
        for (CmsContainerElementBean element : containerPage.getElements()) {
            try {
                element.initResource(cms);
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(
                    cms,
                    element.getResource());
                cssIncludes.addAll(handler.getCSSHeadIncludes());
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_READING_REQUIRED_RESOURCE_1, element.getSitePath()),
                    e);
            }
        }
        if (standardContext.getDetailContentId() != null) {
            try {
                CmsResource detailContent = cms.readResource(standardContext.getDetailContentId());
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(
                    cms,
                    detailContent);
                cssIncludes.addAll(handler.getCSSHeadIncludes());

            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_READING_REQUIRED_RESOURCE_1,
                        standardContext.getDetailContentId()),
                    e);
            }
        }
        for (String cssUri : cssIncludes) {
            pageContext.getOut().print(
                "<link href=\""
                    + CmsJspTagLink.linkTagAction(cssUri, req)
                    + generateReqParams()
                    + "\" rel=\"stylesheet\" type=\"text/css\">");
        }
    }

    /**
     * Action to include the java-script resources.<p>
     * 
     * @param cms the current cms context
     * @param req the current request
     * 
     * @throws CmsException if something goes wrong reading the resources
     * @throws IOException if something goes wrong writing to the response out
     */
    public void tagJSAction(CmsObject cms, ServletRequest req) throws CmsException, IOException {

        CmsJspStandardContextBean standardContext = getStandardContext(cms, req);
        CmsContainerPageBean containerPage = standardContext.getPage();
        Set<String> jsIncludes = new LinkedHashSet<String>();
        for (CmsContainerElementBean element : containerPage.getElements()) {
            try {
                element.initResource(cms);
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(
                    cms,
                    element.getResource());
                jsIncludes.addAll(handler.getJSHeadIncludes());
            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_READING_REQUIRED_RESOURCE_1, element.getSitePath()),
                    e);
            }
        }
        if (standardContext.getDetailContentId() != null) {
            try {
                CmsResource detailContent = cms.readResource(standardContext.getDetailContentId());
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(
                    cms,
                    detailContent);
                jsIncludes.addAll(handler.getJSHeadIncludes());

            } catch (CmsException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_READING_REQUIRED_RESOURCE_1,
                        standardContext.getDetailContentId()),
                    e);
            }
        }
        for (String cssUri : jsIncludes) {
            pageContext.getOut().print(
                "<script type=\"text/javascript\" src=\""
                    + CmsJspTagLink.linkTagAction(cssUri, req)
                    + generateReqParams()
                    + "\"></script>");
        }
    }

    private String generateReqParams() throws UnsupportedEncodingException {

        String params = "";
        if ((m_parameterMap != null) && !m_parameterMap.isEmpty()) {
            for (Entry<String, String[]> paramEntry : m_parameterMap.entrySet()) {
                if (paramEntry.getValue() != null) {
                    for (int i = 0; i < paramEntry.getValue().length; i++) {
                        params += "&"
                            + paramEntry.getKey()
                            + "="
                            + URLEncoder.encode(paramEntry.getValue()[i], "UTF-8");
                    }
                }
            }
            params = "?" + params.substring(1);
        }
        return params;
    }

    private CmsJspStandardContextBean getStandardContext(CmsObject cms, ServletRequest req) throws CmsException {

        String requestUri = cms.getRequestContext().getUri();
        CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
        CmsContainerPageBean containerPage = standardContext.getPage();
        if (containerPage == null) {
            // get the container page itself, checking the history first
            CmsResource pageResource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(req);
            if (pageResource == null) {
                pageResource = cms.readResource(requestUri);
            }
            CmsXmlContainerPage xmlContainerPage = CmsXmlContainerPageFactory.unmarshal(cms, pageResource, req);
            containerPage = xmlContainerPage.getCntPage(cms, cms.getRequestContext().getLocale());
            standardContext.setPage(containerPage);
        }
        return standardContext;
    }

}

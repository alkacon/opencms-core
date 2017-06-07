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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Provides access to OpenCms and System related information.<p>
 *
 * This tag supports the following special "property" values:
 * <ul>
 * <li><code>opencms.version</code> returns the current OpenCms version, e.g. <i>8.0.0</i>.
 * <li><code>opencms.url</code> returns the current request URL, e.g.
 * <i>http://localhost:8080/opencms/opencms/index.jsp</i>.
 * <li><code>opencms.uri</code> returns the current request URI, e.g.
 * <i>/opencms/opencms/index.jsp</i>.
 * <li><code>opencms.webapp</code> returns the name of the OpenCms web application, e.g.
 * <i>opencms</i>.
 * <li><code>opencms.webbasepath</code> returns the name of system path to the OpenCms web
 * application, e.g. <i>C:\Java\Tomcat\webapps\opencms\</i>.
 * <li><code>opencms.request.uri</code> returns the name of the currently requested URI in
 * the OpenCms VFS, e.g. <i>/index.jsp</i>.
 * <li><code>opencms.request.element.uri</code> returns the name of the currently processed element,
 * which might be a sub-element like a template part,
 * in the OpenCms VFS, e.g. <i>/system/modules/org.opencms.welcome/jsptemplates/welcome.jsp</i>.
 * <li><code>opencms.request.folder</code> returns the name of the parent folder of the currently
 * requested URI in the OpenCms VFS, e.g. <i>/</i>.
 * <li><code>opencms.request.encoding</code> returns the content encoding that has been set
 * for the currently requested resource, e.g. <i>ISO-8859-1</i>.
 * <li><code>opencms.title</code> (since 8.0.0) returns the title of the document that should be used for the
 * HTML title tag. This is useful for container detail pages, in which case it will return the Title
 * of the detail, not the container page. Otherwise it just returns the value of the Title property.
 * <li><code>opencms.description</code> (since 9.0.1)
 * <li><code>opencms.keywords</code> (since 9.0.1)
 * </ul>
 *
 * All other property values that are passes to the tag as routed to a standard
 * <code>System.getProperty(value)</code> call,
 * so you can also get information about the Java VM environment,
 * using values like <code>java.vm.version</code> or <code>os.name</code>.<p>
 *
 * If the given property value does not match a key from the special OpenCms values
 * and also not the system values, a (String) message is returned with a formatted
 * error message.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagInfo extends TagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagInfo.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -3881095296148023924L;

    /** Static array with allowed info property values. */
    private static final String[] SYSTEM_PROPERTIES = {
        "opencms.version", // 0
        "opencms.url", // 1
        "opencms.uri", // 2
        "opencms.webapp", // 3
        "opencms.webbasepath", // 4
        "opencms.request.uri", // 5
        "opencms.request.element.uri", // 6
        "opencms.request.folder", // 7
        "opencms.request.encoding", // 8
        "opencms.request.locale", // 9
        "opencms.title", // 10
        "opencms.description", // 11
        "opencms.keywords" // 12
    };

    /** Array list of allowed property values for more convenient lookup. */
    private static final List<String> SYSTEM_PROPERTIES_LIST = Arrays.asList(SYSTEM_PROPERTIES);

    /** The value of the <code>property</code> attribute. */
    private String m_property;

    /**
     * Returns the description of a page delivered from OpenCms, usually used for the <code>description</code> metatag of
     * a HTML page.<p>
     *
     * If no description information has been found, the empty String "" is returned.<p>
     *
     * @param controller the current OpenCms request controller
     * @param req the current request
     *
     * @return the description of a page delivered from OpenCms
     */
    public static String getDescriptionInfo(CmsFlexController controller, HttpServletRequest req) {

        String result = null;
        CmsObject cms = controller.getCmsObject();

        try {

            CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(req);
            if (contextBean.isDetailRequest()) {
                // this is a request to a detail page
                CmsResource res = contextBean.getDetailContent();
                // read the description of the detail resource as fall back (may contain mapping from another locale)
                result = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue();
            }
            if (result == null) {
                // read the title of the requested resource as fall back
                result = cms.readPropertyObject(
                    cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    true).getValue();
            }
        } catch (CmsException e) {
            // NOOP, result will be null
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "";
        }

        return result;
    }

    /**
     * Returns the keywords of a page delivered from OpenCms, usually used for the <code>keywords</code> metatag of
     * a HTML page.<p>
     *
     * If no description information has been found, the empty String "" is returned.<p>
     *
     * @param controller the current OpenCms request controller
     * @param req the current request
     *
     * @return the description of a page delivered from OpenCms
     */
    public static String getKeywordsInfo(CmsFlexController controller, HttpServletRequest req) {

        String result = null;
        CmsObject cms = controller.getCmsObject();

        try {

            CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(req);
            if (contextBean.isDetailRequest()) {
                // this is a request to a detail page
                CmsResource res = contextBean.getDetailContent();
                // read the keywords of the detail resource as fall back (may contain mapping from another locale)
                result = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_KEYWORDS, false).getValue();
            }
            if (result == null) {
                // read the title of the requested resource as fall back
                result = cms.readPropertyObject(
                    cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_KEYWORDS,
                    true).getValue();
            }
        } catch (CmsException e) {
            // NOOP, result will be null
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "";
        }

        return result;
    }

    /**
     * Returns the title of a page delivered from OpenCms, usually used for the <code>&lt;title&gt;</code> tag of
     * a HTML page.<p>
     *
     * If no title information has been found, the empty String "" is returned.<p>
     *
     * @param controller the current OpenCms request controller
     * @param req the current request
     *
     * @return the title of a page delivered from OpenCms
     */
    public static String getTitleInfo(CmsFlexController controller, HttpServletRequest req) {

        String result = null;
        CmsObject cms = controller.getCmsObject();

        try {

            CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(req);
            if (contextBean.isDetailRequest()) {
                // this is a request to a detail page
                CmsResource res = contextBean.getDetailContent();
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, res, req);
                result = content.getHandler().getTitleMapping(cms, content, cms.getRequestContext().getLocale());
                if (result == null) {
                    // title not found, maybe no mapping OR not available in the current locale
                    // read the title of the detail resource as fall back (may contain mapping from another locale)
                    result = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                }
            }
            if (result == null) {
                // read the title of the requested resource as fall back
                result = cms.readPropertyObject(
                    cms.getRequestContext().getUri(),
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    true).getValue();
            }
        } catch (CmsException e) {
            // NOOP, result will be null
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "";
        }

        return result;
    }

    /**
     * Returns the selected info property value based on the provided
     * parameters.<p>
     *
     * @param property the info property to look up
     * @param req the currents request
     * @return the looked up property value
     */
    public static String infoTagAction(String property, HttpServletRequest req) {

        if (property == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(Messages.GUI_ERR_INVALID_INFO_PROP_0);
            return Messages.getLocalizedMessage(errMsgContainer, req);
        }
        CmsFlexController controller = CmsFlexController.getController(req);

        String result = null;
        switch (SYSTEM_PROPERTIES_LIST.indexOf(property)) {
            case 0: // opencms.version
                result = OpenCms.getSystemInfo().getVersionNumber();
                break;
            case 1: // opencms.url
                result = req.getRequestURL().toString();
                break;
            case 2: // opencms.uri
                result = req.getRequestURI();
                break;
            case 3: // opencms.webapp
                result = OpenCms.getSystemInfo().getWebApplicationName();
                break;
            case 4: // opencms.webbasepath
                result = OpenCms.getSystemInfo().getWebApplicationRfsPath();
                break;
            case 5: // opencms.request.uri
                result = controller.getCmsObject().getRequestContext().getUri();
                break;
            case 6: // opencms.request.element.uri
                result = controller.getCurrentRequest().getElementUri();
                break;
            case 7: // opencms.request.folder
                result = CmsResource.getParentFolder(controller.getCmsObject().getRequestContext().getUri());
                break;
            case 8: // opencms.request.encoding
                result = controller.getCmsObject().getRequestContext().getEncoding();
                break;
            case 9: // opencms.request.locale
                result = controller.getCmsObject().getRequestContext().getLocale().toString();
                break;
            case 10: // opencms.title
                result = getTitleInfo(controller, req);
                break;
            case 11: // opencms.description
                result = getDescriptionInfo(controller, req);
                break;
            case 12: // opencms.keywords
                result = getKeywordsInfo(controller, req);
                break;
            default:
                result = System.getProperty(property);
                if (result == null) {
                    CmsMessageContainer errMsgContainer = Messages.get().container(
                        Messages.GUI_ERR_INVALID_INFO_PROP_1,
                        property);
                    return Messages.getLocalizedMessage(errMsgContainer, req);
                }
        }

        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String result = infoTagAction(m_property, (HttpServletRequest)req);
                // Return value of selected property
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "info"), ex);
                }
                throw new JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Returns the selected info property.<p>
     *
     * @return the selected info property
     */
    public String getProperty() {

        return m_property != null ? m_property : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        super.release();
        m_property = null;
    }

    /**
     * Sets the info property name.<p>
     *
     * @param name the info property name to set
     */
    public void setProperty(String name) {

        if (name != null) {
            m_property = name.toLowerCase();
        }
    }

}

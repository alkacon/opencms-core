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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagInclude;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;
import org.opencms.xml.containerpage.CmsDynamicFunctionParser;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Class used for rendering dynamic functions (v2).<p>
 */
public class CmsFunctionRenderer {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFunctionRenderer.class);

    /** The current cms context. */
    private CmsObject m_cms;

    /** The page context. */
    private PageContext m_context;

    /** The JSP context bean. */
    private CmsJspStandardContextBean m_contextBean;

    /** The element to render. */
    private CmsContainerElementBean m_element;

    /** The request. */
    private HttpServletRequest m_request;

    /** The response. */
    private HttpServletResponse m_response;

    /**
     * Constructor.<p>
     *
     * @param context the page context
     * @param req the request
     * @param res the response
     */
    public CmsFunctionRenderer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        m_context = context;
        m_request = req;
        m_response = res;
        CmsFlexController controller = CmsFlexController.getController(req);
        if (controller == null) {
            handleMissingFlexController();
            return;
        }
        m_cms = controller.getCmsObject();
        m_contextBean = CmsJspStandardContextBean.getInstance(m_request);
        m_element = m_contextBean.getElement();
    }

    /**
     * Returns the default output for functions without configured JSPs.
     *
     * @param request the current request
     * @return the default HTML output
     */
    public static String defaultHtml(HttpServletRequest request) {

        CmsObject cms = CmsFlexController.getController(request).getCmsObject();

        // We only want the big red warning in Offline mode
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return "<div><!--Dynamic function not configured--></div>";
        } else {
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            String message = Messages.get().getBundle(locale).key(Messages.GUI_FUNCTION_DEFAULT_HTML_0);
            return "<div style=\"border: 2px solid red; padding: 10px;\">" + message + "</div>";
        }
    }

    /**
     * Cached method for accessing the default function formatter.<p>
     *
     * @param cms the current CMS context
     * @return the default function formatter resource
     */
    public static CmsResource getDefaultFunctionInstance(CmsObject cms) {

        String path = "/system/modules/org.opencms.base/formatters/function-default.xml";
        return getDefaultResource(cms, path);
    }

    /**
     * Cached method for accessing the default function formatter JSP.<p>
     *
     * @param cms the current CMS context
     * @return the default function formatter JSP
     */
    public static CmsResource getDefaultFunctionJsp(CmsObject cms) {

        return getDefaultResource(cms, "/system/modules/org.opencms.base/formatters/function-default.jsp");
    }

    /**
     * Helper method for cached reading of resources under specific, fixed paths.<p>
     *
     * @param cms the current CMS context
     * @param path the path to read
     *
     * @return the resource which has been read
     */
    private static CmsResource getDefaultResource(CmsObject cms, String path) {

        CmsResource resource = (CmsResource)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(
            cms,
            path);
        if (resource == null) {
            try {
                resource = cms.readResource(path);
                CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(cms, path, resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return resource;
    }

    /**
     * Renders the requested element content with the flex formatter string template.<p>
     *
     * @throws IOException in case writing to to page context out fails
     * @throws JspException in case something goes wrong during the JSP include
     */
    public void render() throws IOException, JspException {

        boolean isNewFunctionType = OpenCms.getResourceManager().matchResourceType(
            CmsResourceTypeFunctionConfig.TYPE_NAME,
            m_element.getResource().getTypeId());
        if (isNewFunctionType) {
            CmsFunctionFormatterBean function = getFormatterBean(m_cms);
            if (function != null) {
                CmsUUID jspId = function.getRealJspId();
                if (jspId != null) {
                    CmsJspTagInclude.includeTagAction(
                        m_context,
                        m_cms.getRequestContext().removeSiteRoot(function.getRealJspRootPath()),
                        null,
                        m_cms.getRequestContext().getLocale(),
                        false,
                        m_cms.getRequestContext().getCurrentProject().isOnlineProject(),
                        function.getParameters(),
                        CmsRequestUtil.getAttributeMap(m_request),
                        m_request,
                        m_response);
                } else {
                    m_context.getOut().print(defaultHtml(m_request));
                }
            } else {
                m_context.getOut().print(defaultHtml(m_request));
            }
        } else {
            CmsDynamicFunctionBean.Format format = getFunctionFormat();
            if ((format != null) && m_cms.existsResource(format.getJspStructureId())) {
                try {
                    CmsResource jspResource = m_cms.readResource(format.getJspStructureId());
                    Map<String, String[]> params = new HashMap<>();
                    for (Entry<String, String> paramEntry : format.getParameters().entrySet()) {
                        params.put(paramEntry.getKey(), new String[] {paramEntry.getValue()});
                    }
                    CmsJspTagInclude.includeTagAction(
                        m_context,

                        m_cms.getSitePath(jspResource),
                        null,
                        m_cms.getRequestContext().getLocale(),
                        false,
                        m_cms.getRequestContext().getCurrentProject().isOnlineProject(),
                        params,
                        CmsRequestUtil.getAttributeMap(m_request),
                        m_request,
                        m_response);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } else {
                m_context.getOut().print(defaultHtml(m_request));
            }
        }
    }

    /**
     * Gets the formatter bean for the current element.<p>
     *
     * @param cms the current CMS contxt
     * @return the formatter bean for the current element
     */
    private CmsFunctionFormatterBean getFormatterBean(CmsObject cms) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.getRequestContext().getRootUri());
        // Using getId(), *not* getFormatterId() here , since the former is the id of the function formatter configuration,
        // and the latter is just the id of the internal JSP used to render it
        I_CmsFormatterBean formatterConfig = config.findFormatter(m_element.getId());
        CmsFunctionFormatterBean function = (CmsFunctionFormatterBean)formatterConfig;
        return function;
    }

    /**
     * Returns the function format for the current element.<p>
     *
     * @return the function format
     */
    private CmsDynamicFunctionBean.Format getFunctionFormat() {

        CmsDynamicFunctionBean functionBean = null;
        try {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(m_element.getResource()));
            CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
            functionBean = parser.parseFunctionBean(m_cms, content);
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
        CmsJspStandardContextBean contextBean = CmsJspStandardContextBean.getInstance(m_request);
        String type = contextBean.getContainer().getType();
        String width = contextBean.getContainer().getWidth();
        int widthNum = -1;
        try {
            widthNum = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        return functionBean.getFormatForContainer(m_cms, type, widthNum);
    }

    /**
     * This method is called when the flex controller can not be found during initialization.<p>
     *
     * Override this if you are reusing old workplace classes in a context where no flex controller is available.
     */
    private void handleMissingFlexController() {

        // controller not found - this request was not initialized properly
        throw new CmsRuntimeException(
            org.opencms.jsp.Messages.get().container(
                org.opencms.jsp.Messages.ERR_MISSING_CMS_CONTROLLER_1,
                CmsMacroFormatterResolver.class.getName()));
    }

}

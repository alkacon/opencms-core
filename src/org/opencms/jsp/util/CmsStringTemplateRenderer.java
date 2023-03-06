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
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspTagEditable;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsHtmlValidator;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsFlexFormatterBean;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.stringtemplate.v4.DateRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.compiler.FormalArgument;

/**
 * Renderer for string templates.<p>
 */
public class CmsStringTemplateRenderer {

    /** The error display HTML. */
    public static final String ERROR_DISPLAY = "<div class='oc-fomatter-error'>\n"
        + "<div class='oc-formatter-error-head'>%1$s</div>\n"
        + "<div class='oc-formatter-error-body'>\n"
        + "<div class='oc-formatter-error-source'>%2$s</div>\n"
        + "<div class='oc-formatter-error-message'>%3$s</div>\n"
        + "</div>\n</div>";

    /** The error display HTML, including error details. */
    public static final String ERROR_DISPLAY_WITH_DETAILS = "<div class='oc-fomatter-error'>\n"
        + "<div class='oc-formatter-error-head'>%1$s</div>\n"
        + "<div class='oc-formatter-error-body'>\n"
        + "<div class='oc-formatter-error-source'>%2$s</div>\n"
        + "<div class='oc-formatter-error-message'>%3$s</div>\n"
        + "<div class='oc-formatter-error-details'><pre>%4$s</pre></div>\n"
        + "</div>\n</div>";

    /** Key to access object function wrapper. */
    public static final String KEY_FUNCTIONS = "fn";

    /** Key to access element settings. */
    public static final String KEY_SETTINGS = "settings";

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

    /**
     * Constructor.<p>
     *
     * @param context the page context
     * @param req the request
     */
    public CmsStringTemplateRenderer(PageContext context, HttpServletRequest req) {

        m_context = context;
        m_request = req;
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
     * Renders the given string template.<p>
     *
     * @param cms the cms context
     * @param template the template
     * @param content the content
     * @param contextObjects additional context objects made available to the template
     *
     * @return the rendering result
     */
    public static String renderTemplate(
        CmsObject cms,
        String template,
        CmsJspContentAccessBean content,
        Map<String, Object> contextObjects) {

        return renderTemplateInternal(cms, template, content, contextObjects);
    }

    /**
     * Renders the given string template.<p>
     *
     * @param cms the cms context
     * @param template the template
     * @param contentValue the content value (part of a content)
     * @param contextObjects additional context objects made available to the template
     *
     * @return the rendering result
     */
    public static String renderTemplate(
        CmsObject cms,
        String template,
        CmsJspContentAccessValueWrapper contentValue,
        Map<String, Object> contextObjects) {

        return renderTemplateInternal(cms, template, contentValue, contextObjects);
    }

    /**
     * Renders the given string template.<p>
     *
     * @param cms the cms context
     * @param template the template
     * @param content the content
     * @param contextObjects additional context objects made available to the template
     *
     * @return the rendering result
     */
    public static String renderTemplate(
        CmsObject cms,
        String template,
        CmsResource content,
        Map<String, Object> contextObjects) {

        return renderTemplate(cms, template, new CmsJspContentAccessBean(cms, content), contextObjects);
    }

    /**
     * Renders the given string template.<p>
     *
     * @param cms the cms context
     * @param template the template
     * @param content the content
     * @param contextObjects additional context objects made available to the template
     * @param pathPrefix the path to the content part that should be accessible as content to the string template.
     *
     * @return the rendering result
     */
    public static String renderTemplate(
        CmsObject cms,
        String template,
        CmsResource content,
        Map<String, Object> contextObjects,
        String pathPrefix) {

        CmsJspContentAccessBean contentBean = new CmsJspContentAccessBean(cms, content);
        return (null != pathPrefix) && !pathPrefix.isEmpty()
        ? renderTemplate(cms, template, contentBean.getValue().get(pathPrefix), contextObjects)
        : renderTemplate(cms, template, contentBean, contextObjects);
    }

    /**
     * Wraps the element settings with access wrappers.<p>
     *
     * @param cms the current OpenCms user context
     * @param settings the settings to wrap
     *
     * @return the element settings wrapped in access wrappers
     */
    public static Map<String, CmsJspObjectValueWrapper> wrapSettings(CmsObject cms, Map<String, String> settings) {

        Map<String, CmsJspObjectValueWrapper> wrappedSettings = null;
        if (settings != null) {
            wrappedSettings = new HashMap<String, CmsJspObjectValueWrapper>(settings.size());
            for (Entry<String, String> setting : settings.entrySet()) {
                wrappedSettings.put(setting.getKey(), CmsJspObjectValueWrapper.createWrapper(cms, setting.getValue()));
            }
        }
        return wrappedSettings;
    }

    /**
     * Renders the given string template.<p>
     *
     * @param cms the cms context
     * @param template the template
     * @param content the content
     * @param contextObjects additional context objects made available to the template
     *
     * @return the rendering result
     */
    private static String renderTemplateInternal(
        CmsObject cms,
        String template,
        Object content,
        Map<String, Object> contextObjects) {

        STGroup group = new STGroup('%', '%');
        group.registerRenderer(Date.class, new DateRenderer());
        CompiledST cST = group.defineTemplate("main", template);
        cST.addArg(new FormalArgument("content"));
        if (contextObjects != null) {
            for (Entry<String, Object> entry : contextObjects.entrySet()) {
                cST.addArg(new FormalArgument(entry.getKey()));
            }
        }
        ST st = group.getInstanceOf("main");
        st.add("content", content);
        if (contextObjects != null) {
            for (Entry<String, Object> entry : contextObjects.entrySet()) {
                st.add(entry.getKey(), entry.getValue());
            }
        }
        return st.render(cms.getRequestContext().getLocale());
    }

    /**
     * Renders the requested element content with the flex formatter string template.<p>
     *
     * @throws IOException in case writing to to page context out fails
     */
    @SuppressWarnings("resource")
    public void render() throws IOException {

        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfigurationWithCache(
            m_cms,
            m_cms.getRequestContext().getRootUri());
        I_CmsFormatterBean formatterConfig = adeConfig.findFormatter(m_element.getFormatterId());
        if (formatterConfig instanceof CmsFlexFormatterBean) {
            CmsFlexFormatterBean config = (CmsFlexFormatterBean)formatterConfig;
            String template = config.getStringTemplate();
            if (m_element.isInMemoryOnly()) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config.getPlaceholderStringTemplate())) {
                    template = config.getPlaceholderStringTemplate();
                }
                if (config.getDefaultContentStructureId() != null) {
                    try {
                        CmsResource defaultContent = m_cms.readResource(
                            ((CmsMacroFormatterBean)formatterConfig).getDefaultContentStructureId());
                        CmsFile defaultFile = m_cms.readFile(defaultContent);
                        m_element = new CmsContainerElementBean(
                            defaultFile,
                            m_element.getFormatterId(),
                            m_element.getIndividualSettings(),
                            true,
                            m_element.editorHash(),
                            m_element.isCreateNew());
                    } catch (CmsException e) {
                        //      LOG.error("Error reading default content for new resource", e);
                    }
                }
            }
            try {
                Map<String, Object> context = new HashMap<String, Object>();
                context.put(KEY_SETTINGS, wrapSettings(m_cms, m_element.getSettings()));
                context.put(
                    KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(m_cms)));
                String output = renderTemplate(m_cms, template, m_element.getResource(), context);
                if (CmsJspTagEditable.isEditableRequest(m_request)) {
                    CmsHtmlValidator validator = new CmsHtmlValidator();
                    validator.validate(output);
                    if (!validator.isBalanced()) {
                        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                        String messages = "";
                        for (CmsMessageContainer message : validator.getMessages()) {
                            messages += message.key(locale) + "\n";
                        }
                        output = String.format(
                            ERROR_DISPLAY_WITH_DETAILS,
                            Messages.get().getBundle(locale).key(Messages.GUI_FORMATTER_RENDERING_ERROR_0),
                            formatterConfig.getJspRootPath(),
                            Messages.get().getBundle(locale).key(Messages.GUI_FORMATTER_RENDERING_NOT_WELL_FORMED_0),
                            messages);
                    } else if (validator.getRootElementCount() > 1) {
                        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                        output = String.format(
                            ERROR_DISPLAY,
                            Messages.get().getBundle(locale).key(Messages.GUI_FORMATTER_RENDERING_ERROR_0),
                            formatterConfig.getJspRootPath(),
                            Messages.get().getBundle(locale).key(
                                Messages.GUI_FORMATTER_RENDERING_MULTIPLE_ROOT_ELEMENTS_0));
                    }

                }
                m_context.getOut().print(output);
            } catch (Throwable t) {
                if (CmsJspTagEditable.isEditableRequest(m_request)) {
                    String stackTrace = "";
                    for (StackTraceElement element : t.getStackTrace()) {
                        stackTrace += element.toString() + "\n";
                    }
                    Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
                    m_context.getOut().println(
                        String.format(
                            ERROR_DISPLAY_WITH_DETAILS,
                            Messages.get().getBundle(locale).key(Messages.GUI_FORMATTER_RENDERING_ERROR_0),
                            formatterConfig.getJspRootPath(),
                            t.getMessage(),
                            stackTrace));
                }
            }
        }
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

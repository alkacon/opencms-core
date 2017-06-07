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

import org.opencms.ade.configuration.formatters.CmsFormatterBeanParser;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspTagDisplay;
import org.opencms.jsp.Messages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;

/**
 * Resolver for macro formatters.<p>
 */
public class CmsMacroFormatterResolver {

    /** The parent macro key. */
    public static final String KEY_CMS = "cms.";

    /** The element macro key. */
    public static final String KEY_ELEMENT = "element.";

    /** The parent macro key. */
    public static final String KEY_PARENT = "parent.";

    /** The settings macro key. */
    public static final String KEY_SETTINGS = "settings.";

    /** Node name. */
    public static final String N_FORMATTER = "Formatter";

    /** Node name. */
    public static final String N_FORMATTERS = "Formatters";

    /** Node name. */
    public static final String N_MACRO = "Macro";

    /** Node name. */
    public static final String N_MACRO_NAME = "MacroName";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMacroFormatterResolver.class);

    /** The current cms context. */
    private CmsObject m_cms;

    /** The page context. */
    private PageContext m_context;

    /** The JSP context bean. */
    private CmsJspStandardContextBean m_contextBean;

    /** The element to render. */
    private CmsContainerElementBean m_element;

    /** The formatter references. */
    private Map<String, CmsUUID> m_formatterReferences;

    /** The macro input string. */
    private String m_input;

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
    public CmsMacroFormatterResolver(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
     * Resolves the macro.<p>
     *
     * @throws IOException in case writing to the page context output stream fails
     * @throws CmsException in case reading the macro settings fails
     */
    public void resolve() throws IOException, CmsException {

        initMacroContent();
        String input = getMacroInput();
        if (input == null) {
            return;
        }
        if (input.length() < 3) {
            // macro must have at last 3 chars "${}" or "%()"
            m_context.getOut().print(input);
            return;
        }

        int newDelimPos = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER);
        int oldDelomPos = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER_OLD);

        if ((oldDelomPos == -1) && (newDelimPos == -1)) {
            // no macro delimiter found in input
            m_context.getOut().print(input);
            return;
        }

        int len = input.length();
        int nextDelimPos, delimPos1, delimPos2, endPos;
        String macro;
        char startChar, endChar;
        int delimPos;

        if ((oldDelomPos == -1) || ((newDelimPos > -1) && (newDelimPos < oldDelomPos))) {
            delimPos = newDelimPos;
            startChar = I_CmsMacroResolver.MACRO_START;
            endChar = I_CmsMacroResolver.MACRO_END;
        } else {
            delimPos = oldDelomPos;
            startChar = I_CmsMacroResolver.MACRO_START_OLD;
            endChar = I_CmsMacroResolver.MACRO_END_OLD;
        }

        // append chars before the first delimiter found
        m_context.getOut().print(input.substring(0, delimPos));
        do {
            delimPos1 = delimPos + 1;
            delimPos2 = delimPos1 + 1;
            if (delimPos2 >= len) {
                // remaining chars can't be a macro (minimum size is 3)
                m_context.getOut().print(input.substring(delimPos, len));
                break;
            }
            // get the next macro delimiter
            if ((newDelimPos > -1) && (newDelimPos < delimPos1)) {
                newDelimPos = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER, delimPos1);
            }
            if ((oldDelomPos > -1) && (oldDelomPos < delimPos1)) {
                oldDelomPos = input.indexOf(I_CmsMacroResolver.MACRO_DELIMITER_OLD, delimPos1);
            }
            if ((oldDelomPos == -1) && (newDelimPos == -1)) {
                // none found, make sure remaining chars in this segment are appended
                nextDelimPos = len;
            } else {
                // check if the next delimiter is old or new style
                if ((oldDelomPos == -1) || ((newDelimPos > -1) && (newDelimPos < oldDelomPos))) {
                    nextDelimPos = newDelimPos;
                } else {
                    nextDelimPos = oldDelomPos;
                }
            }
            // check if the next char is a "macro start"
            char start = input.charAt(delimPos1);
            if (start == startChar) {
                // we have a starting macro sequence "${" or "%(", now check if this segment contains a "}" or ")"
                endPos = input.indexOf(endChar, delimPos);
                if ((endPos > 0) && (endPos < nextDelimPos)) {
                    // this segment contains a closing macro delimiter "}" or "]", so we may have found a macro
                    macro = input.substring(delimPos2, endPos);
                    // resolve macro
                    try {
                        printMacroValue(macro);
                    } catch (Exception ex) {
                        LOG.error("Writing value for macro '" + macro + "' failed.", ex);
                    }
                    endPos++;
                } else {
                    // no complete macro "${...}" or "%(...)" in this segment
                    endPos = delimPos;
                }
            } else {
                // no macro start char after the "$" or "%"
                endPos = delimPos;
            }
            // set macro style for next delimiter found
            if (nextDelimPos == newDelimPos) {
                startChar = I_CmsMacroResolver.MACRO_START;
                endChar = I_CmsMacroResolver.MACRO_END;
            } else {
                startChar = I_CmsMacroResolver.MACRO_START_OLD;
                endChar = I_CmsMacroResolver.MACRO_END_OLD;
            }
            // append the remaining chars after the macro to the start of the next macro
            m_context.getOut().print(input.substring(endPos, nextDelimPos));
            delimPos = nextDelimPos;
        } while (delimPos < len);
    }

    /**
     * Returns the formatter bean for the given macro string, or <code>null</code> if none available.<p>
     *
     * @param macro the macro
     *
     * @return the formatter bean
     */
    protected I_CmsFormatterBean getFormatterForMacro(String macro) {

        CmsUUID formatterId = null;
        if (m_formatterReferences.containsKey(macro)) {
            formatterId = m_formatterReferences.get(macro);
        } else {
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG);
                CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(type);
                if (m_cms.existsResource(macro, filter)) {
                    CmsResource res = m_cms.readResource(macro);
                    formatterId = res.getStructureId();
                }
            } catch (CmsException e) {
                LOG.error("Failed to read formatter configuration.", e);
            }
        }
        if (formatterId != null) {
            return OpenCms.getADEManager().getCachedFormatters(
                m_cms.getRequestContext().getCurrentProject().isOnlineProject()).getFormatters().get(formatterId);
        }
        return null;
    }

    /**
     * Returns the property value read from the given JavaBean.
     *
     * @param bean the JavaBean to read the property from
     * @param property the property to read
     *
     * @return the property value read from the given JavaBean
     */
    protected Object getMacroBeanValue(Object bean, String property) {

        Object result = null;
        if ((bean != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(property)) {
            try {
                PropertyUtilsBean propBean = BeanUtilsBean.getInstance().getPropertyUtils();
                result = propBean.getProperty(bean, property);
            } catch (Exception e) {
                LOG.error("Unable to access property '" + property + "' of '" + bean + "'.", e);
            }
        } else {
            LOG.info("Invalid parameters: property='" + property + "' bean='" + bean + "'.");
        }
        return result;
    }

    /**
     * Returns the macro input string.<p>
     *
     * @return the macro input string
     */
    protected String getMacroInput() {

        return m_input;
    }

    /**
     * Prints the macro value to the output stream.<p>
     *
     * @param macro the macro string
     *
     * @throws IOException in case writing to the page context output stream fails
     */
    protected void printMacroValue(String macro) throws IOException {

        if (macro.startsWith(KEY_CMS)) {
            Object result = getMacroBeanValue(m_contextBean, macro.substring(KEY_CMS.length()));
            if (result != null) {
                m_context.getOut().print(result);
            }
        } else if (macro.startsWith(KEY_ELEMENT)) {
            Object result = getMacroBeanValue(m_contextBean.getElement(), macro.substring(KEY_ELEMENT.length()));
            if (result != null) {
                m_context.getOut().print(result);
            }
        } else if (macro.startsWith(KEY_PARENT)) {
            Object result = getMacroBeanValue(
                m_contextBean.getParentElement(m_element),
                macro.substring(KEY_PARENT.length()));
            if (result != null) {
                m_context.getOut().print(result);
            }
        } else if (macro.startsWith(KEY_SETTINGS)) {
            String settingValue = m_element.getSettings().get(macro.substring(KEY_SETTINGS.length()));
            if (settingValue != null) {
                m_context.getOut().print(settingValue);
            }
        } else {

            I_CmsFormatterBean formatter = getFormatterForMacro(macro);
            if (formatter != null) {
                try {
                    CmsJspTagDisplay.displayAction(
                        CmsContainerElementBean.cloneWithFormatter(m_element, formatter.getJspStructureId()),
                        formatter,
                        m_context,
                        m_request,
                        m_response);
                } catch (Exception e) {
                    LOG.error("Failed to display formatted content.", e);
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
            Messages.get().container(Messages.ERR_MISSING_CMS_CONTROLLER_1, CmsMacroFormatterResolver.class.getName()));
    }

    /**
     * Initializes settings from the macro content.<p>
     *
     * @throws CmsException in case reading the settings fails
     */
    private void initMacroContent() throws CmsException {

        I_CmsFormatterBean formatterConfig = OpenCms.getADEManager().getCachedFormatters(
            m_cms.getRequestContext().getCurrentProject().isOnlineProject()).getFormatters().get(
                m_element.getFormatterId());
        if (formatterConfig instanceof CmsMacroFormatterBean) {
            CmsMacroFormatterBean config = (CmsMacroFormatterBean)formatterConfig;
            m_input = config.getMacroInput();
            m_formatterReferences = config.getReferencedFormatters();
            if (m_element.isInMemoryOnly()) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config.getPlaceholderMacroInput())) {
                    m_input = config.getPlaceholderMacroInput();
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
                        LOG.error("Error reading default content for new resource", e);
                    }
                }
            }
        } else {
            // only as a fall back, should not be used
            m_formatterReferences = new HashMap<String, CmsUUID>();
            CmsResource macroContent = m_cms.readResource(m_element.getFormatterId());
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_cms, macroContent, m_request);
            m_input = xmlContent.getStringValue(m_cms, CmsFormatterBeanParser.N_MACRO, CmsLocaleManager.MASTER_LOCALE);
            List<I_CmsXmlContentValue> formatters = xmlContent.getValues(
                CmsFormatterBeanParser.N_FORMATTERS,
                CmsLocaleManager.MASTER_LOCALE);
            for (I_CmsXmlContentValue formatterValue : formatters) {
                CmsXmlVfsFileValue file = (CmsXmlVfsFileValue)xmlContent.getValue(
                    formatterValue.getPath() + "/" + CmsFormatterBeanParser.N_FORMATTER,
                    CmsLocaleManager.MASTER_LOCALE);
                String macroName = xmlContent.getStringValue(
                    m_cms,
                    formatterValue.getPath() + "/" + CmsFormatterBeanParser.N_MACRO_NAME,
                    CmsLocaleManager.MASTER_LOCALE);
                m_formatterReferences.put(macroName, file.getLink(m_cms).getStructureId());
            }
        }
    }
}

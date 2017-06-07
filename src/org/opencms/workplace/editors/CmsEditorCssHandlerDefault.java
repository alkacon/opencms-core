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

package org.opencms.workplace.editors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsTemplateContextProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;

/**
 * A default editor CSS handler to obtain the CSS style sheet path from the template property value of the template itself.<p>
 *
 * @since 6.9.2
 */
public class CmsEditorCssHandlerDefault implements I_CmsEditorCssHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorCssHandlerDefault.class);

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorCssHandler#getUriStyleSheet(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getUriStyleSheet(CmsObject cms, String editedResourcePath) {

        String editContext = (String)(cms.getRequestContext().getAttribute(CmsXmlContentEditor.ATTRIBUTE_EDITCONTEXT));
        String result = "";
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(editContext)) {
            // prefer the style sheet of the edit context (usually this will be a container page)
            result = internalGetUriStyleSheet(cms, editContext);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = internalGetUriStyleSheet(cms, editedResourcePath);
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorCssHandler#matches(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean matches(CmsObject cms, String editedResourcePath) {

        // this returns always true, as it is the default CSS handler
        return true;
    }

    /**
     * Finds the style sheet by reading the template property of the template for a given path.<p>
     *
     * @param cms the current CMS context
     * @param editedResourcePath the resource path
     *
     * @return the CSS uri from the template for the given path
     */
    private String internalGetUriStyleSheet(CmsObject cms, String editedResourcePath) {

        if (editedResourcePath == null) {
            return "";
        }
        String result = "";
        try {
            // determine the path of the template
            String templatePath = "";
            try {
                templatePath = cms.readPropertyObject(
                    editedResourcePath,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    true).getValue("");
                if (CmsTemplateContextManager.isProvider(templatePath)) {
                    I_CmsTemplateContextProvider provider = OpenCms.getTemplateContextManager().getTemplateContextProvider(
                        templatePath);
                    if (provider != null) {
                        String providerResult = provider.getEditorStyleSheet(cms, editedResourcePath);
                        if (providerResult != null) {
                            return providerResult;
                        }
                    }
                }
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_READ_TEMPLATE_PROP_FAILED_0), e);
                }
            }
            if (CmsStringUtil.isNotEmpty(templatePath)) {
                // read the template property value from the template file where the absolute CSS path is (or should be) stored
                result = cms.readPropertyObject(templatePath, CmsPropertyDefinition.PROPERTY_TEMPLATE, false).getValue(
                    "");
            }
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_READ_TEMPLATE_PROP_STYLESHEET_FAILED_0), e);
            }
        }
        return result;
    }

}

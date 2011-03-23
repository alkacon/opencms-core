/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateCssHandler.java,v $
 * Date   : $Date: 2011/03/23 14:52:01 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.editors.Messages;

import org.apache.commons.logging.Log;

/**
 * An editor CSS handler to obtain the CSS style sheet path for pages using template one as template.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.9.2 
 */
public class CmsTemplateCssHandler implements I_CmsEditorCssHandler {

    /** The absolute VFS path to the template one template. */
    public static final String VFS_PATH_TEMPLATE = CmsWorkplace.VFS_PATH_MODULES
        + CmsTemplateBean.MODULE_NAME
        + "/templates/main";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateCssHandler.class);

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorCssHandler#getUriStyleSheet(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getUriStyleSheet(CmsObject cms, String editedResourcePath) {

        StringBuffer result = new StringBuffer(512);
        String cssConfigPath = getStyleSheetConfigUri(cms, editedResourcePath);
        StringBuffer params = new StringBuffer(256);
        if (CmsStringUtil.isNotEmpty(cssConfigPath) && cms.existsResource(cssConfigPath)) {
            // found special configuration file, append Templateone parameters to CSS call
            params.append("?respath=");
            params.append(editedResourcePath);
            params.append("&config=");
            params.append(cssConfigPath);
            params.append("&site=");
            params.append(cms.getRequestContext().getSiteRoot());
            params.append("&__locale=");
            String locale = cms.getRequestContext().getLocale().toString();
            try {
                locale = cms.readPropertyObject(editedResourcePath, CmsPropertyDefinition.PROPERTY_LOCALE, true).getValue(
                    locale);
            } catch (CmsException e) {
                // ignore, failed to read property value
            }
            params.append(locale);
        }
        result.append(CmsWorkplace.VFS_PATH_MODULES);
        result.append(CmsTemplateBean.MODULE_NAME);
        result.append("/resources/");
        result.append(CmsTemplateBean.FILE_CSS);
        result.append(params);
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorCssHandler#matches(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean matches(CmsObject cms, String editedResourcePath) {

        // determine the path of the template
        String templatePath = "";
        try {
            templatePath = cms.readPropertyObject(editedResourcePath, CmsPropertyDefinition.PROPERTY_TEMPLATE, true).getValue(
                "");
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_READ_TEMPLATE_PROP_FAILED_0), e);
            }
        }
        if (CmsStringUtil.isNotEmpty(templatePath)) {
            // found the template path, check if it is the template one template
            return VFS_PATH_TEMPLATE.equals(templatePath);
        }
        return false;
    }

    /**
     * Returns the URI of the CSS style sheet configuration file.<p>
     * 
     * @param cms the current OpenCms user context
     * @param editedResourcePath the absolute VFS path of the currently edited resource
     * @return the URI of the CSS style sheet configuration file
     */
    protected String getStyleSheetConfigUri(CmsObject cms, String editedResourcePath) {

        String originalUri = cms.getRequestContext().getUri();
        String confUri = null;
        try {
            cms.getRequestContext().setUri(editedResourcePath);
            confUri = cms.readPropertyObject(editedResourcePath, CmsTemplateStyleSheet.PROPERTY_CONFIGFILE, true).getValue(
                "");
            if (CmsStringUtil.isEmpty(confUri)) {
                // property not set, try to get default configuration file
                confUri = cms.readPropertyObject(editedResourcePath, CmsTemplateBean.PROPERTY_CONFIGPATH, true).getValue(
                    "/")
                    + CmsTemplateStyleSheet.FILENAME_CONFIGFILE;
            }
        } catch (CmsException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(org.opencms.frontend.templateone.Messages.get().getBundle().key(
                    org.opencms.frontend.templateone.Messages.LOG_XMLCONTEN_CONFIG_NOT_FOUND_2,
                    CmsTemplateStyleSheet.FILENAME_CONFIGFILE,
                    editedResourcePath));
            }
        } finally {
            cms.getRequestContext().setUri(originalUri);
        }
        return confUri;
    }

}

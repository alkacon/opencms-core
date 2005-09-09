/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaField.java,v $
 * Date   : $Date: 2005/09/09 10:31:59 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.form;

import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.octo.captcha.service.image.ImageCaptchaService;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Creates captcha images and validates the pharses submitted by a request parameter.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class CmsCaptchaField extends A_CmsField {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCaptchaField.class);
    
    /** Request parameter name of the captcha phrase. */
    public static final String C_PARAM_CAPTCHA_PHRASE = "captchaphrase";
    
    /** The settings to render captcha images. */
    private CmsCaptchaSettings m_captchaSettings;

    /** HTML field type: captcha image. */
    private static final String TYPE = "captcha";
    
    /**
     * Creates a new captcha field.<p>
     * @param captchaSettings the settings to render captcha images
     * @param fieldLabel the localized label of this field
     * @param fieldValue the submitted value of this field
     */
    public CmsCaptchaField(CmsCaptchaSettings captchaSettings, String fieldLabel, String fieldValue) {
        
        super();
        
        m_captchaSettings = captchaSettings;
        
        setName(C_PARAM_CAPTCHA_PHRASE);
        setValue(fieldValue);
        setLabel(fieldLabel);
        setMandatory(true);
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getType()
     */
    public String getType() {

        return TYPE;
    }
    
    /**
     * Returns the type of the input field, e.g. "text" or "select".<p>
     * 
     * @return the type of the input field
     */
    public static String getStaticType() {
        
        return TYPE;
    }
    
    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#buildHtml(CmsFormHandler, org.opencms.i18n.CmsMessages, String)
     */
    public String buildHtml(CmsFormHandler formHandler, CmsMessages messages, String errorKey) {
        
        StringBuffer buf = new StringBuffer();
        String fieldLabel = getLabel();
        String errorMessage = "";
        String mandatory = "";
        
        CmsCaptchaSettings captchaSettings = getCaptchaSettings();
        
        if (CmsStringUtil.isNotEmpty(errorKey)) {
            
            if (CmsFormHandler.ERROR_MANDATORY.equals(errorKey)) {
                errorMessage = messages.key("form.error.mandatory");
            } else if (CmsStringUtil.isNotEmpty(getErrorMessage())) {
                errorMessage = getErrorMessage();
            } else {
                errorMessage = messages.key("form.error.validation");
            }
            
            errorMessage = messages.key("form.html.error.start") + errorMessage + messages.key("form.html.error.end");
            fieldLabel = messages.key("form.html.label.error.start") + fieldLabel + messages.key("form.html.label.error.end");
        }
        
        if (isMandatory()) {
            mandatory = messages.key("form.html.mandatory");
        }
        
        // line #1
        buf.append(messages.key("form.html.row.start")).append("\n");
        
        // line #2
        buf.append(messages.key("form.html.label.start"))
            .append(fieldLabel)
            .append(mandatory)
            .append(messages.key("form.html.label.end")).append("\n");
        
        // line #3
        buf.append(messages.key("form.html.field.start")).append("\n");
        
        // line #4
        buf.append("<img src=\"")
            .append(formHandler.link("/system/modules/org.opencms.frontend.templateone.form/pages/captcha?" + captchaSettings.toRequestParams(formHandler)))
            .append("\" width=\"").append(captchaSettings.getImageWidth())
            .append("\" height=\"").append(captchaSettings.getImageHeight())
            .append("\" alt=\"\">").append("\n"); 
        
        // line #5
        buf.append("<br>\n");
        
        // line #6
        buf.append("<input type=\"text\" name=\"").append(getName()).append("\" value=\"").append(getValue()).append("\"")
            .append(formHandler.getFormConfiguration().getFormFieldAttributes())
            .append(">")
            .append(errorMessage)
            .append(messages.key("form.html.field.end")).append("\n");
        
        // line #7
        buf.append(messages.key("form.html.row.end")).append("\n");
        
        return buf.toString();
    }

    /**
     * Writes a Captcha JPEG image to the servlet response output stream.<p>
     * 
     * @param cms an initialized Cms JSP action element
     * @throws IOException if something goes wrong
     */
    public void writeCaptchaImage(CmsJspActionElement cms) throws IOException {

        ByteArrayOutputStream captchaImageOutput = new ByteArrayOutputStream();
        ServletOutputStream out = null;     
        
        try {
            
            String sessionId = cms.getRequest().getSession().getId();
            Locale locale = cms.getRequestContext().getLocale();
            
            BufferedImage captchaImage = CmsCaptchaServiceCache.getSharedInstance().getCaptchaService(m_captchaSettings).getImageChallengeForID(sessionId, locale);
            JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(captchaImageOutput);
            jpegEncoder.encode(captchaImage);
            
            CmsFlexController controller = CmsFlexController.getController(cms.getRequest());
            HttpServletResponse response = controller.getTopResponse();
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            
            out = cms.getResponse().getOutputStream();
            out.write(captchaImageOutput.toByteArray());
            out.flush();
        } catch (Exception e) {
            
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage());
            }
            
            cms.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable t) {
                // intentionally left blank
            }
        }
    }
    
    /**
     * Validates the captcha phrase entered by the user.<p>
     * 
     * @param jsp the Cms JSP
     * @param captchaPhrase the captcha phrase to be validate
     * @return true, if the captcha phrase entered by the user is correct, false otherwise
     */
    public boolean validateCaptchaPhrase(CmsJspActionElement jsp, String captchaPhrase) {
        
        boolean result = false;
        String sessionId = jsp.getRequest().getSession().getId();  
        
        if (CmsStringUtil.isNotEmpty(captchaPhrase)) {
            
            ImageCaptchaService captchaService = CmsCaptchaServiceCache.getSharedInstance().getCaptchaService(m_captchaSettings);
            if (captchaService != null) {
                result = captchaService.validateResponseForID(sessionId, captchaPhrase).booleanValue();
            }
        }
        
        return result;
    }
    
    /**
     * Returns the captcha settings of this field.<p>
     * 
     * @return the captcha settings of this field
     */
    public CmsCaptchaSettings getCaptchaSettings() {
        
        return m_captchaSettings;
    }

}

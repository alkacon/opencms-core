/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaField.java,v $
 * Date   : $Date: 2011/03/23 14:50:50 $
 * Version: $Revision: 1.18 $
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

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * Creates captcha images and validates the pharses submitted by a request parameter.
 * <p>
 * 
 * @author Thomas Weckert
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.18 $
 */
public class CmsCaptchaField extends A_CmsField {

    /** Request parameter name of the captcha phrase. */
    public static final String C_PARAM_CAPTCHA_PHRASE = "captchaphrase";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCaptchaField.class);

    /** HTML field type: captcha image. */
    private static final String TYPE = "captcha";

    /** The settings to render captcha images. */
    private CmsCaptchaSettings m_captchaSettings;

    /**
     * Creates a new captcha field.
     * <p>
     * 
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
     * Returns the type of the input field, e.g. "text" or "select".
     * <p>
     * 
     * @return the type of the input field
     */
    public static String getStaticType() {

        return TYPE;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#buildHtml(CmsFormHandler,
     *      org.opencms.i18n.CmsMessages, String)
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
            fieldLabel = messages.key("form.html.label.error.start")
                + fieldLabel
                + messages.key("form.html.label.error.end");
        }

        if (isMandatory()) {
            mandatory = messages.key("form.html.mandatory");
        }

        // line #1
        if (showRowStart(messages.key("form.html.col.two"))) {
            buf.append(messages.key("form.html.row.start")).append("\n");
        }

        // line #2
        buf.append(messages.key("form.html.label.start")).append(fieldLabel).append(mandatory).append(
            messages.key("form.html.label.end")).append("\n");

        // line #3
        buf.append(messages.key("form.html.field.start")).append("\n");

        // line #4
        buf.append("<img src=\"").append(
            formHandler.link("/system/modules/org.opencms.frontend.templateone.form/pages/captcha?"
                + captchaSettings.toRequestParams(formHandler.getCmsObject()))).append("\" width=\"").append(
            captchaSettings.getImageWidth()).append("\" height=\"").append(captchaSettings.getImageHeight()).append(
            "\" alt=\"\"/>").append("\n");

        // line #5
        buf.append("<br/>\n");

        // line #6
        buf.append("<input type=\"text\" name=\"").append(getName()).append("\" value=\"").append(getValue()).append(
            "\"").append(formHandler.getFormConfiguration().getFormFieldAttributes()).append("/>").append(errorMessage).append(
            messages.key("form.html.field.end")).append("\n");

        // line #7
        if (showRowEnd(messages.key("form.html.col.two"))) {
            buf.append(messages.key("form.html.row.end")).append("\n");
        }

        return buf.toString();
    }

    /**
     * Returns the captcha settings of this field.
     * <p>
     * 
     * @return the captcha settings of this field
     */
    public CmsCaptchaSettings getCaptchaSettings() {

        return m_captchaSettings;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getType()
     */
    public String getType() {

        return TYPE;
    }

    /**
     * Validates the captcha phrase entered by the user.
     * <p>
     * 
     * @param jsp the Cms JSP
     * @param captchaPhrase the captcha phrase to be validate
     * @return true, if the captcha phrase entered by the user is correct, false otherwise
     */
    public boolean validateCaptchaPhrase(CmsJspActionElement jsp, String captchaPhrase) {

        boolean result = false;
        String sessionId = jsp.getRequest().getSession().getId();

        if (CmsStringUtil.isNotEmpty(captchaPhrase)) {

            try {
                ImageCaptchaService captchaService = CmsCaptchaServiceCache.getSharedInstance().getCaptchaService(
                    m_captchaSettings,
                    jsp.getCmsObject());
                if (captchaService != null) {
                    result = captchaService.validateResponseForID(sessionId, captchaPhrase).booleanValue();
                }
            } catch (CaptchaServiceException cse) {
                // most often this will be 
                // "com.octo.captcha.service.CaptchaServiceException: Invalid ID, could not validate unexisting or already validated captcha"
                // in case someone hits the back button and submits again 
            }
        }

        return result;
    }

    /**
     * Writes a Captcha JPEG image to the servlet response output stream.
     * <p>
     * 
     * @param cms an initialized Cms JSP action element
     * @throws IOException if something goes wrong
     */
    public void writeCaptchaImage(CmsJspActionElement cms) throws IOException {

        ByteArrayOutputStream captchaImageOutput = new ByteArrayOutputStream();
        ServletOutputStream out = null;
        BufferedImage captchaImage = null;
        int maxTries = 10;
        do {
            try {

                maxTries--;
                String sessionId = cms.getRequest().getSession().getId();
                Locale locale = cms.getRequestContext().getLocale();

                captchaImage = CmsCaptchaServiceCache.getSharedInstance().getCaptchaService(
                    m_captchaSettings,
                    cms.getCmsObject()).getImageChallengeForID(sessionId, locale);
            } catch (CaptchaException cex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(cex);
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_ERR_CAPTCHA_CONFIG_IMAGE_SIZE_2,
                        new Object[] {m_captchaSettings.getPresetPath(), new Integer(maxTries)}));
                }
                m_captchaSettings.setImageHeight(m_captchaSettings.getImageHeight() + 40);
                m_captchaSettings.setImageWidth(m_captchaSettings.getImageWidth() + 80);
            }
        } while ((captchaImage == null) && (maxTries > 0));
        try {

            ImageIO.write(captchaImage, "jpg", captchaImageOutput);
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
                LOG.error(e.getLocalizedMessage(), e);
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

}

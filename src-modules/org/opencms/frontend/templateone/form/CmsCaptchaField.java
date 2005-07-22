/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaField.java,v $
 * Date   : $Date: 2005/07/22 15:22:39 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $
 */
public class CmsCaptchaField extends CmsField {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCaptchaField.class);
    
    /** Request parameter name of the captcha phrase. */
    public static final String C_PARAM_CAPTCHA_PHRASE = "captchaphrase";
    
    /** The settings to render captcha images. */
    private CmsCaptchaSettings m_captchaSettings;
    
    /**
     * Creates a new captcha field.<p>
     * @param captchaSettings the settings to render captcha images
     * @param fieldLabel the localized label of this field
     * @param fieldValue the submitted value of this field
     */
    public CmsCaptchaField(CmsCaptchaSettings captchaSettings, String fieldLabel, String fieldValue) {
        
        super();
        
        m_captchaSettings = captchaSettings;
        
        setType(CmsField.TYPE_CAPTCHA);
        setName(C_PARAM_CAPTCHA_PHRASE);
        setValue(fieldValue);
        setLabel(fieldLabel);
        setMandatory(true);
    }


    /**
     * Writes a Captcha JPEG image to the servlet response output stream.<p>
     * 
     * @param cms an initialized Cms JSP action element
     * @throws IOException if something goes wrong
     */
    public void writeCaptchaImage(CmsJspActionElement cms) throws IOException {

        ByteArrayOutputStream captchaImageOutput = new ByteArrayOutputStream();
        
        try {
            
            String sessionId = cms.getRequest().getSession().getId();
            Locale locale = cms.getRequestContext().getLocale();
            
            BufferedImage captchaImage = CmsCaptchaServiceCache.getSharedInstance().getCaptchaService(m_captchaSettings).getImageChallengeForID(sessionId, locale);
            JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(captchaImageOutput);
            jpegEncoder.encode(captchaImage);
        } catch (Exception e) {
            
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage());
            }
            
            cms.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        cms.getResponse().setHeader("Cache-Control", "no-store");
        cms.getResponse().setHeader("Pragma", "no-cache");
        cms.getResponse().setDateHeader("Expires", 0);
        cms.getResponse().setContentType("image/jpeg");
        
        ServletOutputStream out = cms.getResponse().getOutputStream();
        out.write(captchaImageOutput.toByteArray());
        out.flush();
        out.close();
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

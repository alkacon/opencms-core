/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaService.java,v $
 * Date   : $Date: 2011/03/23 14:50:48 $
 * Version: $Revision: 1.10 $
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

import com.octo.captcha.service.captchastore.MapCaptchaStore;
import com.octo.captcha.service.image.AbstractManageableImageCaptchaService;

/**
 * Provides the facility to create and cache the captcha images.
 * <p>
 * 
 * @author Thomas Weckert 
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.10 $
 */
public class CmsCaptchaService extends AbstractManageableImageCaptchaService {

    /**
     * Creates a new captcha service.
     * <p>
     * 
     * minGuarantedStorageDelayInSeconds = 180s maxCaptchaStoreSize = 100000
     * captchaStoreLoadBeforeGarbageCollection=75000
     * 
     * @param captchaSettings the settings to render captcha images
     */
    public CmsCaptchaService(CmsCaptchaSettings captchaSettings) {

        super(new MapCaptchaStore(), new CmsCaptchaEngine(captchaSettings), 180, 100000, 75000);
    }

    /**
     * Implant new captcha settings to this service.
     * <p>
     * This is an expensive method as new Image filters and many processing objects are allocated anew. 
     * Prefer using the {@link CmsCaptchaServiceCache#getCaptchaService(CmsCaptchaSettings, org.opencms.file.CmsObject)} method instead. 
     * It will return cached instances for equal settings.
     * <p>
     *
     * @param settings the captcha settings to implant.
     */
    protected void setSettings(CmsCaptchaSettings settings) {

        CmsCaptchaEngine captchaEngine = (CmsCaptchaEngine)engine;
        captchaEngine.setSettings(settings);
    }

}

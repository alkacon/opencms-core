/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaServiceCache.java,v $
 * Date   : $Date: 2011/03/23 14:50:50 $
 * Version: $Revision: 1.12 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.util.HashMap;
import java.util.Map;

import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * Caches captcha services.
 * <p>
 * 
 * @author Thomas Weckert 
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.12 $
 */
public final class CmsCaptchaServiceCache implements I_CmsEventListener {

    /** The shared instance of the captcha service cache. */
    private static CmsCaptchaServiceCache sharedInstance;

    /** Stores the captcha services. */
    private Map m_captchaServices;

    /**
     * Default constructor.
     * <p>
     */
    private CmsCaptchaServiceCache() {

        super();

        // add this class as an event handler to the Cms event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES,
            I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT});

        m_captchaServices = new HashMap();
    }

    /**
     * Returns the shared instance of the captcha service cache.
     * <p>
     * 
     * @return the shared instance of the captcha service cache
     */
    public static synchronized CmsCaptchaServiceCache getSharedInstance() {

        if (sharedInstance == null) {
            sharedInstance = new CmsCaptchaServiceCache();
        }

        return sharedInstance;
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                clearCaptchaServices();
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Returns the captcha service specified by the settings.
     * <p>
     * 
     * @param captchaSettings the settings to render captcha images.
     * 
     * @param cms needed for context information when getting the key for caching.
     * 
     * @return the captcha service.
     */
    public synchronized ImageCaptchaService getCaptchaService(CmsCaptchaSettings captchaSettings, CmsObject cms) {

        String key = null;

        if (m_captchaServices == null) {
            m_captchaServices = new HashMap();
        }

        key = captchaSettings.toRequestParams(cms);
        CmsCaptchaService captchaService = (CmsCaptchaService)m_captchaServices.get(key);
        if (captchaService == null) {
            captchaService = new CmsCaptchaService(captchaSettings);
            m_captchaServices.put(key, captchaService);
        } else {
            // install the parameters to the internal engine
            // captchaService.setSettings(captchaSettings);
        }

        return captchaService;
    }

    /**
     * Clears the map storing the captcha services.
     * <p>
     */
    private synchronized void clearCaptchaServices() {

        if (m_captchaServices != null) {
            m_captchaServices.clear();
        }
    }
}
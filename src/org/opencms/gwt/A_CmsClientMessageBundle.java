/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * Intended only for test cases.<p>
 * 
 * @since 8.0.0
 */
public abstract class A_CmsClientMessageBundle implements I_CmsClientMessageBundle {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(A_CmsClientMessageBundle.class);

    /**
     * Hides the public constructor for this utility class.<p>
     */
    protected A_CmsClientMessageBundle() {

        // empty
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        CmsObject cms = CmsFlexController.getCmsObject(request);
        return export(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#export(java.lang.String)
     */
    public String export(String localeName) {

        return export(CmsLocaleManager.getLocale(localeName));
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#export(java.util.Locale)
     */
    public String export(Locale locale) {

        JSONObject keys = new JSONObject();
        try {
            ResourceBundle resourceBundle = CmsResourceBundleLoader.getBundle(getBundleName(), locale);
            Enumeration<String> bundleKeys = resourceBundle.getKeys();
            while (bundleKeys.hasMoreElements()) {
                String bundleKey = bundleKeys.nextElement();
                keys.put(bundleKey, resourceBundle.getString(bundleKey));
            }
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                keys.put("error", e.getLocalizedMessage());
            } catch (JSONException e1) {
                // ignore, should never happen
                LOG.error(e1.getLocalizedMessage(), e1);
            }
        }
        return getBundleName().replace('.', '_') + "=" + keys.toString() + ";";
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#getBundleName()
     */
    public String getBundleName() {

        return getClass().getPackage().getName() + ".clientmessages";
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#getClientImpl()
     */
    public Class<?> getClientImpl() throws Exception {

        return Class.forName(getClass().getPackage().getName() + ".client.Messages");
    }
}

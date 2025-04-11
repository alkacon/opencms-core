/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.cacheadmin;

import org.opencms.loader.CmsImageLoader;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.cacheadmin.CmsFlushCache.I_CloseableDialog;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsDateField;

import java.util.Calendar;
import java.util.Collections;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

/**
 * Dialog to clean Image Cache.<p>
 */
public class CmsImageCacheCleanDialog extends CmsBasicDialog implements I_CloseableDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6902585433676013120L;

    /**Runnable for close action.*/
    Runnable m_closeRunnable;

    /**Runnable for ok action. */
    Runnable m_okRunnable;

    /**Vaadin component.*/
    private Button m_cancelButton;

    /**Date field.*/
    private CmsDateField m_dateField;

    /**Vaadin component.*/
    private Label m_icon;

    /**Vaadin component.*/
    private Button m_okButton;

    /**
     * Public constructor.<p>
     */
    public CmsImageCacheCleanDialog() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        //Setup icon
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -30);
        m_dateField.setDate(cal.getTime());
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8281661241498918564L;

            public void buttonClick(ClickEvent event) {

                flushCache();
                m_closeRunnable.run();
                if (m_okRunnable != null) {
                    m_okRunnable.run();
                }
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -936541994114016527L;

            public void buttonClick(ClickEvent event) {

                m_closeRunnable.run();
            }
        });
    }

    /**
     * @see org.opencms.ui.apps.cacheadmin.CmsFlushCache.I_CloseableDialog#setCloseRunnable(java.lang.Runnable)
     */
    public void setCloseRunnable(Runnable closeRunnable) {

        m_closeRunnable = closeRunnable;

    }

    /**
     * @see org.opencms.ui.apps.cacheadmin.CmsFlushCache.I_CloseableDialog#setOkRunnable(java.lang.Runnable)
     */
    public void setOkRunnable(Runnable okRunnable) {

        m_okRunnable = okRunnable;

    }

    /**
     * Clears the Image Cache according to value of date field.<p>
     */
    void flushCache() {

        float age = (System.currentTimeMillis() - m_dateField.getDate().getTime()) / (60f * 60f * 1000f);
        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                Collections.<String, Object> singletonMap(CmsImageLoader.PARAM_CLEAR_IMAGES_CACHE, "" + age)));
    }
}

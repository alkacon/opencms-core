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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.flex.CmsFlexCache;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.cacheadmin.CmsFlushCache.I_CloseableDialog;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.Collections;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;

/**
 * Dialog for clean flex cache.<p>
 */
public class CmsFlexCacheCleanDialog extends CmsBasicDialog implements I_CloseableDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 142178694100824093L;

    /**Runnable for close action.*/
    Runnable m_closeRunnable;

    /**Runnable for ok button.*/
    Runnable m_okRunnable;

    /**Vaadin component.*/
    private Button m_cancelButton;

    /**Vaadin label for icon.*/
    private Label m_icon;

    /**Vaadin component.*/
    private OptionGroup m_mode;

    /**Vaadin component.*/
    private Button m_okButton;

    /**
     * Public constructor.<p>
     *
     */
    public CmsFlexCacheCleanDialog() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        setDefaultValues();

        //Set Clicklistener
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -5769891739879269176L;

            public void buttonClick(ClickEvent event) {

                m_closeRunnable.run();
            }
        });
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6932464669055039855L;

            public void buttonClick(ClickEvent event) {

                submit();
                m_closeRunnable.run();
                if (m_okRunnable != null) {
                    m_okRunnable.run();
                }
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
     * Reads out Checkboxes and chosen mode.<p>
     */
    void submit() {

        int action = -1;

        if (isModeAll()) {
            action = CmsFlexCache.CLEAR_ONLINE_ALL;
        } else {
            action = CmsFlexCache.CLEAR_ONLINE_ENTRIES;
        }

        OpenCms.fireCmsEvent(
            new CmsEvent(
                I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR,
                Collections.<String, Object> singletonMap(CmsFlexCache.CACHE_ACTION, Integer.valueOf(action))));
    }

    /**
     * Reads out option group for mode.<p>
     *
     * @return true if clear all
     */
    private boolean isModeAll() {

        return m_mode.getValue().equals("keysAndVariations");
    }

    /**
     * Set defautl values to vaadin components.<p>
     */
    private void setDefaultValues() {

        //Setup icon
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        //Set Mode option.
        m_mode.setValue("keysAndVariations");
    }

}

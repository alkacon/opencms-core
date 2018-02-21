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

package org.opencms.ui.apps.logfile;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;

import javax.servlet.http.HttpSession;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Class for the log file view settings dialog.<p>
 */
public class CmsLogFileViewSettings extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 3564444938636162445L;

    /**Vaadin component. */
    ComboBox m_charset;

    /**Vaadin component. */
    TextField m_size;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaadin component.*/
    private Button m_ok;

    /**
     * public constructor.<p>
     *
     * @param window where the dialog is shown in
     */
    public CmsLogFileViewSettings(final Window window) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_size.setValue(
            (String)CmsVaadinUtils.getRequest().getSession().getAttribute(CmsLogFileView.ATTR_FILE_VIEW_SIZE));

        SortedMap<String, Charset> csMap = Charset.availableCharsets();
        Charset cs;
        Iterator<Charset> it = csMap.values().iterator();
        while (it.hasNext()) {
            cs = it.next();
            m_charset.addItem(cs);
        }

        m_charset.select(CmsVaadinUtils.getRequest().getSession().getAttribute(CmsLogFileView.ATTR_FILE_VIEW_CHARSET));

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -3277236106838992809L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 3179892867228610166L;

            public void buttonClick(ClickEvent event) {

                HttpSession session = CmsVaadinUtils.getRequest().getSession();
                session.setAttribute(CmsLogFileView.ATTR_FILE_VIEW_CHARSET, m_charset.getValue());
                session.setAttribute(CmsLogFileView.ATTR_FILE_VIEW_SIZE, m_size.getValue());
                window.close();

            }
        });
    }
}
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

package org.opencms.ui.dialogs.embedded;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.dataview.CmsDataViewPanel;
import org.opencms.widgets.dataview.I_CmsDataView;
import org.opencms.widgets.dataview.I_CmsDataViewItem;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog used to select data items from an external data source.<p>
 */
public class CmsDataViewDialog extends CmsBasicDialog {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The container for the other widgets. */
    private VerticalLayout m_container;

    /** The OK button. */
    private Button m_okButton;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsDataViewDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        final CmsDataViewParams params = new CmsDataViewParams(A_CmsUI.get().getPage().getLocation());

        I_CmsDataView example = params.createViewInstance(context.getCms(), A_CmsUI.get().getLocale());
        final CmsDataViewPanel panel = new CmsDataViewPanel(example, params.isMultiSelect());

        panel.setSizeFull();
        m_container.addComponent(panel);
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                List<I_CmsDataViewItem> result = panel.getSelection();
                String script = params.prepareCallbackScript(result);
                JavaScript.eval(script);
                m_context.finish(null);

            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_context.finish(null);
            }
        });
    }
}

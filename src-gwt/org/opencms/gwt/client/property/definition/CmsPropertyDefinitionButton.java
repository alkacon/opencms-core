/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.property.definition;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Button for defining new properties from the property dialog.<p>
 */
public class CmsPropertyDefinitionButton extends CmsPushButton {

    /** The dialog instance. */
    private CmsFormDialog m_dialog;

    /**
     * Gets the dialog which this button is used for.<p>
     *
     * @return the dialog for this button
     */
    public CmsFormDialog getDialog() {

        return m_dialog;
    }

    /**
     * Creates a new instance of the button.<p>
     */
    public CmsPropertyDefinitionButton() {

        super(I_CmsLayoutBundle.INSTANCE.propertiesCss().propertyDefinitionButton());
        setTitle(CmsPropertyDefinitionMessages.messageDialogCaption());
        setButtonStyle(ButtonStyle.TRANSPARENT, null);
        getElement().getStyle().setFloat(Style.Float.LEFT);
        addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                onBeforeEditPropertyDefinition();
                editPropertyDefinition();
            }
        });
    }

    /**
     * Installs the button on a dialog if the user has sufficient permissions.<p>
     *
     * @param dialog the dialog to which the button should be added
     */
    public void installOnDialog(CmsFormDialog dialog) {

        if (CmsCoreProvider.get().getUserInfo().isDeveloper()) {
            setDialog(dialog);
            dialog.addButton(this);
        }
    }

    /**
     * Method which is called directly before the property definition dialog is opened.<p>
     */
    public void onBeforeEditPropertyDefinition() {

        // do nothing in the default implementation
    }

    /**
     * Method which is called when the property definition dialog is closed.<p>
     */
    public void onClosePropertyDefinitionDialog() {

        // do nothing in the default implementation
    }

    /**
     * Sets the dialog instance.<p>
     *
     * @param dialog the dialog instance
     */
    public void setDialog(CmsFormDialog dialog) {

        m_dialog = dialog;
    }

    /**
     * Opens the dialog for creating new property definitions.<p>
     */
    protected void editPropertyDefinition() {

        CmsRpcAction<ArrayList<String>> action = new CmsRpcAction<ArrayList<String>>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getDefinedProperties(this);
            }

            @Override
            protected void onResponse(ArrayList<String> result) {

                stop(false);
                CmsPropertyDefinitionDialog dialog = new CmsPropertyDefinitionDialog(result);
                dialog.center();
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        onClosePropertyDefinitionDialog();
                    }

                });
            }

        };
        action.execute();
    }

}

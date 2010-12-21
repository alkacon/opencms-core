/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsVfsSelector.java,v $
 * Date   : $Date: 2010/12/21 10:23:32 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.exttree.CmsVfsTreeFactory;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.shared.CmsVfsEntryBean;

import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.WidgetListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;

/**
 * A widget which allows the user to select a file from the VFS tree.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsVfsSelector extends Composite implements I_CmsFormWidget, I_CmsHasInit {

    /** UiBinder interface for this widget. */
    protected interface I_CmsVfsSelectorUiBinder extends UiBinder<Panel, CmsVfsSelector> {
        // binder interface
    }

    /** The widget type. */
    public static final String WIDGET_TYPE = "vfs";

    /** UiBinder instance for this widget. */
    protected static I_CmsVfsSelectorUiBinder uiBinder = GWT.create(I_CmsVfsSelectorUiBinder.class);

    /** The auto hide parent. */
    protected I_CmsAutoHider m_autoHideParent;

    /** The button for opening the file selection tree. */
    //@UiField
    protected CmsPushButton m_button;
    /** The GUI field displaying the current value. */
    @UiField
    protected CmsTextBox m_field;

    /**
     * Creates a new VFS selector.<p>
     */
    public CmsVfsSelector() {

        initWidget(uiBinder.createAndBindUi(this));
        //m_button.setUseMinWidth(true);
        //m_button.setText("Select***");
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsVfsSelector();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        String result = m_field.getText();
        if (result.equals("")) {
            result = null;
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        // TODO: Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        m_autoHideParent = autoHideParent;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        //m_button.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        // not implemented
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        if (value == null) {
            value = "";
        }
        m_field.setText(value);
    }

    /**
     * Internal selection handler for resource selection.<p>
     * 
     * @param entry the entry which has been selected, or null if an entry has been deselected 
     */
    protected void onSelect(CmsVfsEntryBean entry) {

        if (entry == null) {
            m_field.setText("");
        } else {
            m_field.setText(entry.getPath());
        }

    }

    /**
     * Event handler for clicks on the "select resource" button.<p>
     * 
     * @param e the click event 
     */
    @UiHandler("m_field")
    void onClickSelect(ClickEvent e) {

        final TreePanel<BeanModel> tree = CmsVfsTreeFactory.createVfsTree(createSelectionChangedListener());

        final Dialog dialog = new Dialog();
        tree.getStore().getLoader().addLoadListener(new LoadListener() {

            @Override
            public void loaderLoadException(LoadEvent le) {

                super.loaderLoadException(le);
                dialog.hide();
                String alertTitle = Messages.get().key(Messages.GUI_ERROR_0);
                String exceptionText = le.exception.toString();
                String alertContent = Messages.get().key(Messages.GUI_CANT_LOAD_TREE_1, exceptionText);
                CmsAlertDialog alert = new CmsAlertDialog(alertTitle, alertContent);
                alert.center();
            }
        });

        // When the dialog is closed, we remove it from the parent dialog's list of auto-hide partners 
        dialog.addWidgetListener(new WidgetListener() {

            /**
             * @see com.extjs.gxt.ui.client.event.WidgetListener#widgetDetached(com.extjs.gxt.ui.client.event.ComponentEvent)
             */
            @Override
            public void widgetDetached(ComponentEvent event) {

                if (m_autoHideParent != null) {
                    m_autoHideParent.removeAutoHidePartner(dialog.getElement());
                }
            }
        });
        dialog.setHeading("Select file");
        dialog.setButtons("");
        dialog.add(tree);
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setScrollMode(Scroll.AUTO);
        ScheduledCommand cmd = new Scheduler.ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                dialog.show();
                if (m_autoHideParent != null) {
                    m_autoHideParent.addAutoHidePartner(dialog.getElement());
                }
                tree.getStore().getLoader().load(null);
            }
        };
        Scheduler.get().scheduleDeferred(cmd);
    }

    /**
     * Helper method for creating the selection handler for the tree.<p>
     * 
     * @return the selection handler for the tree
     */
    private SelectionChangedListener<BeanModel> createSelectionChangedListener() {

        return new SelectionChangedListener<BeanModel>() {

            /**
             * @see com.extjs.gxt.ui.client.event.SelectionChangedListener#selectionChanged(com.extjs.gxt.ui.client.event.SelectionChangedEvent)
             */
            @Override
            public void selectionChanged(SelectionChangedEvent<BeanModel> se) {

                BeanModel model = se.getSelectedItem();
                if (model == null) {
                    onSelect(null);
                } else {
                    onSelect((CmsVfsEntryBean)model.getBean());
                }
            }
        };
    }
}

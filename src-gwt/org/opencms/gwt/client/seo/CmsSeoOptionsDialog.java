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

package org.opencms.gwt.client.seo;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.property.A_CmsPropertyEditor;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsSimplePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The SEO options dialog, which makes it possible to both edit the SEO relevant properties of
 * a resource as well as alias paths for the resource.<p>
 */
public class CmsSeoOptionsDialog extends CmsPopup implements I_CmsFormHandler {

    /** The alias messages. */
    protected static CmsAliasMessages aliasMessages = new CmsAliasMessages();

    /** The properties which should be displayed. */
    protected static String[] seoProperties = new String[] {"Title", "Description", "Keywords"};

    /** The validation has detected an error. */
    protected static final int VALIDATION_FAILED = 2;

    /** The validation has finished successfully. */
    protected static final int VALIDATION_OK = 0;

    /** The validation isn't finished yet. */
    protected static final int VALIDATION_RUNNING = 1;

    /** The inner alias list. */
    protected CmsAliasList m_aliasList;

    /** The validation status for the aliases. */
    protected int m_aliasValidationStatus;

    /** The root panel for this dialog. */
    protected FlowPanel m_panel = new FlowPanel();

    /** The validation status for the properties. */
    protected int m_propertyValidationStatus;

    /** The structure id of the resource whose aliases are being edited. */
    protected CmsUUID m_structureId;

    /** The property editor instance. */
    A_CmsPropertyEditor m_propertyEditor;

    /** The form submit handler. */
    private I_CmsFormSubmitHandler m_formSubmitHandler;

    /**
     * The field set containing the properties relevant for SEO.
     */
    private CmsFieldSet m_propertyFieldset = new CmsFieldSet();

    /**
     * Creates a new dialog instance.<p>
     * 
     * @param structureId the structure id of the resource whose aliases are being edited
     * @param infoBean a bean containing the information to display in the resource info box 
     * @param aliases the existing aliases of the resource 
     * @param propertyConfig the property configuration 
     * @param propertyEditorHandler the property editor handler 
     */
    public CmsSeoOptionsDialog(
        CmsUUID structureId,
        CmsListInfoBean infoBean,
        List<CmsAliasBean> aliases,
        Map<String, CmsXmlContentProperty> propertyConfig,
        I_CmsPropertyEditorHandler propertyEditorHandler) {

        super(aliasMessages.seoOptions());
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(true);
        setWidth(590);

        //-----------------------INFO BOX -------------------------------------------

        CmsListItemWidget liWidget = new CmsListItemWidget(infoBean);
        liWidget.setStateIcon(StateIcon.standard);
        m_panel.add(liWidget);

        //------------------------ PROPERTIES ------------------------------------------        
        LinkedHashMap<String, CmsXmlContentProperty> props = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (String seoProperty : seoProperties) {
            if (propertyConfig.containsKey(seoProperty)) {
                props.put(seoProperty, propertyConfig.get(seoProperty));
            }
        }
        m_propertyEditor = new CmsSimplePropertyEditor(props, propertyEditorHandler);
        m_propertyEditor.getForm().setFormHandler(this);
        m_formSubmitHandler = new CmsPropertySubmitHandler(propertyEditorHandler);
        m_structureId = structureId;
        m_propertyFieldset.getElement().getStyle().setMarginTop(10, Unit.PX);
        m_propertyFieldset.getContentPanel().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        m_propertyFieldset.setLegend(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_PROPERTIES_0));
        m_propertyEditor.initializeWidgets(this);
        m_panel.add(m_propertyFieldset);

        //------------------------ ALIASES ------------------------------------------

        CmsFieldSet aliasFieldset = new CmsFieldSet();
        aliasFieldset.setLegend(aliasMessages.aliases());
        m_aliasList = new CmsAliasList(structureId, aliases);
        aliasFieldset.getElement().getStyle().setMarginTop(10, Unit.PX);
        CmsScrollPanel scrollPanel = GWT.create(CmsScrollPanel.class);
        scrollPanel.setWidget(m_aliasList);
        aliasFieldset.addContent(scrollPanel);
        m_panel.add(scrollPanel);
        Style style = scrollPanel.getElement().getStyle();
        style.setProperty("minHeight", "300px"); //$NON-NLS-1$ //$NON-NLS-2$
        style.setProperty("maxHeight", "450px"); //$NON-NLS-1$ //$NON-NLS-2$
        style.setOverflowY(Overflow.AUTO);

        setMainContent(m_panel);
        addButton(createCancelButton());
        addButton(saveButton());

    }

    /**
     * Loads the aliases for a given page.<p>
     * 
     * @param structureId the structure id of the page 
     * @param callback the callback for the loaded aliases 
     */
    public static void loadAliases(final CmsUUID structureId, final AsyncCallback<List<CmsAliasBean>> callback) {

        final CmsRpcAction<List<CmsAliasBean>> action = new CmsRpcAction<List<CmsAliasBean>>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getAliasesForPage(structureId, this);
            }

            @Override
            protected void onResponse(List<CmsAliasBean> result) {

                stop(false);
                callback.onSuccess(result);
            }
        };
        action.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onSubmitValidationResult(CmsForm form, boolean ok) {

        m_propertyValidationStatus = ok ? VALIDATION_OK : VALIDATION_FAILED;
        update(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
     */
    public void onValidationResult(CmsForm form, boolean ok) {

        m_propertyValidationStatus = ok ? VALIDATION_OK : VALIDATION_FAILED;
        update(false);
    }

    /**
     * Saves the aliases for a given page.<p>
     *  
     * @param uuid the page structure id 
     * @param aliases the aliases to save
     */
    public void saveAliases(final CmsUUID uuid, final List<CmsAliasBean> aliases) {

        final CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().saveAliases(uuid, aliases, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void onResponse(Void result) {

                stop(false);
            }

        };
        action.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        m_propertyFieldset.addContent(m_propertyEditor.getForm().getWidget());
        m_propertyFieldset.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        super.show();
        notifyWidgetsOfOpen();
    }

    /**
     * Updates the validation status and optionally submits the data.<p>
     * 
     * @param submit the submit flag 
     */
    public void update(boolean submit) {

        boolean ok = (m_propertyValidationStatus == VALIDATION_OK) && (m_aliasValidationStatus == VALIDATION_OK);
        if (submit && ok) {
            saveProperties();
            saveAliases();
            hide();
        }
    }

    /**
     * The method which is called when the user clicks the save button of the dialog.<p>
     */
    protected void onClickSave() {

        Timer timer = new Timer() {

            @Override
            public void run() {

                m_aliasList.clearValidationErrors();
                m_aliasValidationStatus = VALIDATION_RUNNING;
                m_propertyValidationStatus = VALIDATION_RUNNING;
                m_aliasList.validate(new Runnable() {

                    public void run() {

                        m_aliasValidationStatus = !m_aliasList.hasValidationErrors()
                        ? VALIDATION_OK
                        : VALIDATION_FAILED;
                        update(true);

                    }
                });
                m_propertyEditor.getForm().validateAndSubmit();
            }
        };
        // slight delay so that the validation doesn't interfere with validations triggered by the change event 
        timer.schedule(20);
    }

    /**
     * Saves the aliases.<p>
     */
    protected void saveAliases() {

        List<CmsAliasBean> aliases = m_aliasList.getAliases();
        saveAliases(m_structureId, aliases);
    }

    /**
     * Saves the properties.<p>
     */
    protected void saveProperties() {

        m_propertyEditor.getForm().handleSubmit(m_formSubmitHandler);
    }

    /**
     * Creates the cancel button.<p>
     * 
     * @return the cancel button
     */
    private CmsPushButton createCancelButton() {

        addDialogClose(null);
        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsSeoOptionsDialog.this.hide();
            }
        });

        return button;
    }

    /**
     * Tells all widgets that the dialog has been opened.<p>
     */
    private void notifyWidgetsOfOpen() {

        for (Map.Entry<String, I_CmsFormField> fieldEntry : m_propertyEditor.getForm().getFields().entrySet()) {
            fieldEntry.getValue().getWidget().setAutoHideParent(this);
        }
    }

    /**
     * Creates the OK button.<p>
     * 
     * @return the OK button
     */
    private CmsPushButton saveButton() {

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_SAVE_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsSeoOptionsDialog.this.onClickSave();
            }
        });
        return button;
    }

}

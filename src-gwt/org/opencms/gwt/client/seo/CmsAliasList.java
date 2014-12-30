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
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle.I_CmsImageStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.alias.CmsAliasBean;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

/**
 * A widget used for editing the alias list of a page.<p>
 */
public class CmsAliasList extends Composite {

    /**
     * A helper class which encapsulates the input widgets for a single alias.<p>
     */
    protected class AliasControls {

        /** The alias to which these controls belong. */
        protected CmsAliasBean m_alias;

        /** A string which uniquely identiy this set of controls. */
        protected String m_id = "" + (idCounter++); //$NON-NLS-1$

        /** The select box for selecting the alias mode. */
        protected CmsSelectBox m_selectBox;

        /** The text box for the alias path. */
        protected CmsTextBox m_textBox;

        /**
         * Creates a new alias controls instance.<p>
         *
         * @param alias the alias to which the controls belong
         * @param textBox the text box for entering the alias site path
         * @param selectBox the select box for selecting alias modes
         */
        public AliasControls(CmsAliasBean alias, CmsTextBox textBox, CmsSelectBox selectBox) {

            m_alias = alias;
            m_textBox = textBox;
            m_selectBox = selectBox;
        }

        /**
         * Gets the alias to which these controls belong.<p>
         *
         * @return the alias to which these controls belong
         */
        public CmsAliasBean getAlias() {

            return m_alias;
        }

        /**
         * Gets the id of this set of controls.<p>
         *
         * @return the id
         */
        public String getId() {

            return m_id;
        }

        /**
         * Gets the alias mode select box.<p>
         *
         * @return the alias mode select box
         */
        public CmsSelectBox getSelectBox() {

            return m_selectBox;
        }

        /**
         * Gets the text box for the alias site path.<p>
         *
         * @return the text box for the alias site path
         */
        public CmsTextBox getTextBox() {

            return m_textBox;
        }

    }

    /** The CSS bundle for input widgets. */
    public static final I_CmsInputCss INPUT_CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The alias messages. */
    protected static CmsAliasMessages aliasMessages = new CmsAliasMessages();

    /** Static variable used to generate new ids. */
    protected static int idCounter;

    /** The callback which is normally used for validation of the site paths. */
    protected AsyncCallback<Map<String, String>> m_defaultValidationHandler = new AsyncCallback<Map<String, String>>() {

        public void onFailure(Throwable caught) {

            // do nothing
        }

        public void onSuccess(java.util.Map<String, String> result) {

            for (Map.Entry<String, String> entry : result.entrySet()) {
                String id = entry.getKey();
                String errorMessage = entry.getValue();
                AliasControls controls = m_aliasControls.get(id);
                controls.getTextBox().setErrorMessage(errorMessage);
                if (errorMessage != null) {
                    m_hasValidationErrors = true;
                }
            }
        }
    };

    /** A flag used to keep track of whether the last validation had any errors. */
    protected boolean m_hasValidationErrors;

    /** The structure id of the page for which the aliases are being edited. */
    protected CmsUUID m_structureId;

    /** A map containing the alias controls which are currently visible. */
    LinkedHashMap<String, AliasControls> m_aliasControls = new LinkedHashMap<String, AliasControls>();

    /** This panel contains the existing aliases. */
    private FlowPanel m_changeBox = new FlowPanel();

    /** This panel contains input widgets for adding new aliases. */
    private FlowPanel m_newBox = new FlowPanel();

    /** The root panel for this widget. */
    private FlowPanel m_panel = new FlowPanel();

    /**
     * Creates a new widget instance.<p>
     *
     * @param structureId the structure id of the page for which the aliases should be edited
     * @param aliases the aliases being edited
     */
    public CmsAliasList(CmsUUID structureId, List<CmsAliasBean> aliases) {

        initWidget(m_panel);

        m_panel.addStyleName(INPUT_CSS.highTextBoxes());
        m_structureId = structureId;
        Label newLabel = createLabel(aliasMessages.newAlias());
        m_panel.add(newLabel);
        m_panel.add(m_newBox);
        Label changeLabel = createLabel(aliasMessages.existingAliases());
        m_panel.add(changeLabel);
        m_panel.add(m_changeBox);
        init(aliases);
    }

    /**
     * Adds the controls for a single alias to the widget.<p>
     *
     * @param alias the alias for which the controls should be added
     */
    public void addAlias(final CmsAliasBean alias) {

        final HorizontalPanel hp = new HorizontalPanel();
        hp.getElement().getStyle().setMargin(2, Unit.PX);
        final CmsTextBox textbox = createTextBox();
        textbox.setFormValueAsString(alias.getSitePath());
        hp.add(textbox);

        CmsSelectBox selectbox = createSelectBox();
        selectbox.setFormValueAsString(alias.getMode().toString());
        hp.add(selectbox);
        PushButton deleteButton = createDeleteButton();
        hp.add(deleteButton);

        final AliasControls controls = new AliasControls(alias, textbox, selectbox);
        m_aliasControls.put(controls.getId(), controls);

        selectbox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                onChangePath(controls);
            }
        });

        deleteButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                m_aliasControls.remove(controls.getId());
                hp.removeFromParent();
                validateFull(m_structureId, getAliasPaths(), m_defaultValidationHandler);
            }
        });
        textbox.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> e) {

                onChangePath(controls);
                validateFull(m_structureId, getAliasPaths(), m_defaultValidationHandler);
            }
        });

        textbox.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                onChangePath(controls);
            }
        });
        m_changeBox.add(hp);
        CmsDomUtil.resizeAncestor(this);
    }

    /**
     * Clears the validation error flag.<p>
     */
    public void clearValidationErrors() {

        m_hasValidationErrors = false;
    }

    /**
     * Gets a list of the changed aliases.<p>
     *
     * @return a list of the aliases
     */
    public List<CmsAliasBean> getAliases() {

        List<CmsAliasBean> beans = new ArrayList<CmsAliasBean>();
        for (AliasControls controls : m_aliasControls.values()) {
            beans.add(controls.getAlias());
        }
        return beans;
    }

    /**
     * Gets a map of the current alias site paths, with the alias controls ids as the keys.<p>
     *
     * @return a map from control ids to alias site paths
     */
    public Map<String, String> getAliasPaths() {

        Map<String, String> paths = new HashMap<String, String>();
        for (AliasControls controls : m_aliasControls.values()) {
            paths.put(controls.getId(), controls.getAlias().getSitePath());
        }
        return paths;
    }

    /**
     * Checks whether there have been validation errors since the validation errors were cleared the last time.<p>
     *
     * @return true if there were validation errors
     */
    public boolean hasValidationErrors() {

        return m_hasValidationErrors;
    }

    /**
     * Initializes the alias controls.<p>
     *
     * @param aliases the existing aliases
     */
    public void init(List<CmsAliasBean> aliases) {

        for (CmsAliasBean alias : aliases) {
            addAlias(alias);
        }

        final HorizontalPanel hp = new HorizontalPanel();
        final CmsTextBox textbox = createTextBox();
        textbox.setGhostMode(true);
        textbox.setGhostValue(aliasMessages.enterAlias(), true);
        textbox.setGhostModeClear(true);
        hp.add(textbox);
        final CmsSelectBox selectbox = createSelectBox();
        hp.add(selectbox);
        PushButton addButton = createAddButton();
        hp.add(addButton);
        final Runnable addAction = new Runnable() {

            public void run() {

                textbox.setErrorMessage(null);
                validateSingle(m_structureId, getAliasPaths(), textbox.getText(), new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {

                        // shouldn't be called
                    }

                    public void onSuccess(String result) {

                        if (result == null) {
                            CmsAliasMode mode = CmsAliasMode.valueOf(selectbox.getFormValueAsString());
                            addAlias(new CmsAliasBean(textbox.getText(), mode));
                            textbox.setFormValueAsString("");
                        } else {
                            textbox.setErrorMessage(result);
                        }
                    }
                });
            }
        };

        ClickHandler clickHandler = new ClickHandler() {

            public void onClick(ClickEvent e) {

                addAction.run();
            }
        };
        addButton.addClickHandler(clickHandler);
        textbox.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                int keycode = event.getNativeEvent().getKeyCode();
                if ((keycode == 10) || (keycode == 13)) {
                    addAction.run();
                }
            }
        });

        m_newBox.add(hp);
    }

    /**
     * Simplified method to perform a full validation of the aliases in the list and execute an action afterwards.<p>
     *
     * @param nextAction the action to execute after the validation finished
     */
    public void validate(final Runnable nextAction) {

        validateFull(m_structureId, getAliasPaths(), new AsyncCallback<Map<String, String>>() {

            public void onFailure(Throwable caught) {

                // do nothing

            }

            public void onSuccess(Map<String, String> result) {

                for (Map.Entry<String, String> entry : result.entrySet()) {
                    if (entry.getValue() != null) {
                        m_hasValidationErrors = true;
                    }
                }
                m_defaultValidationHandler.onSuccess(result);
                nextAction.run();
            }
        });
    }

    /**
     * Validates aliases.
     * 
     * @param uuid The structure id for which the aliases should be valid
     * @param aliasPaths a map from id strings to alias paths 
     * @param callback the callback which should be called with the validation results 
     */
    public void validateAliases(
        final CmsUUID uuid,
        final Map<String, String> aliasPaths,
        final AsyncCallback<Map<String, String>> callback) {

        CmsRpcAction<Map<String, String>> action = new CmsRpcAction<Map<String, String>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().validateAliases(uuid, aliasPaths, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Map<String, String> result) {

                stop(false);
                callback.onSuccess(result);
            }

        };
        action.execute();
    }

    /**
     * Creates the button used for adding new aliases.<p>
     *
     * @return the new button
     */
    protected PushButton createAddButton() {

        I_CmsImageStyle imagestyle = I_CmsImageBundle.INSTANCE.style();
        PushButton button = createIconButton(imagestyle.addIcon());
        button.setTitle(aliasMessages.addAlias());
        return button;
    }

    /**
     * Creates the button used for deleting aliases.<p>
     *
     * @return the new button
     */
    protected PushButton createDeleteButton() {

        I_CmsImageStyle imagestyle = I_CmsImageBundle.INSTANCE.style();
        PushButton button = createIconButton(imagestyle.deleteIcon());
        button.setTitle(aliasMessages.removeAlias());
        return button;
    }

    /**
     * Creates an icon button for editing aliases.<p>
     *
     * @param icon the icon css class to use
     *
     * @return the new icon button
     */
    protected PushButton createIconButton(String icon) {

        CmsPushButton button = new CmsPushButton();
        button.setImageClass(icon);
        button.setSize(Size.small);
        button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        button.getElement().getStyle().setMarginTop(1, Unit.PX);
        return button;
    }

    /**
     * Creates a label for this widget.<p>
     *
     * @param text the text to display in the label
     *
     * @return the created label
     */
    protected Label createLabel(String text) {

        Label label = new Label(text);
        Style style = label.getElement().getStyle();
        style.setMarginTop(10, Unit.PX);
        style.setMarginBottom(4, Unit.PX);
        style.setFontWeight(FontWeight.BOLD);
        return label;
    }

    /**
     * Creates the select box for selecting alias modes.<p>
     *
     * @return the select box for selecting alias modes
     */
    protected CmsSelectBox createSelectBox() {

        CmsSelectBox selectbox = new CmsSelectBox();
        selectbox.setTitle(CmsAliasMode.page.toString(), aliasMessages.pageDescription());
        selectbox.setTitle(CmsAliasMode.redirect.toString(), aliasMessages.redirectDescription());
        selectbox.setTitle(CmsAliasMode.permanentRedirect.toString(), aliasMessages.movedDescription());
        selectbox.addOption(CmsAliasMode.page.toString(), aliasMessages.optionPage());
        selectbox.addOption(CmsAliasMode.redirect.toString(), aliasMessages.optionRedirect());
        selectbox.addOption(CmsAliasMode.permanentRedirect.toString(), aliasMessages.optionMoved());

        selectbox.getElement().getStyle().setWidth(190, Unit.PX);
        selectbox.getElement().getStyle().setMarginRight(5, Unit.PX);
        return selectbox;
    }

    /**
     * Creates a text box for entering an alias path.<p>
     *
     * @return the new text box
     */
    protected CmsTextBox createTextBox() {

        CmsTextBox textbox = new CmsTextBox();
        textbox.getElement().getStyle().setWidth(325, Unit.PX);
        textbox.getElement().getStyle().setMarginRight(5, Unit.PX);
        return textbox;
    }

    /**
     * This method is called when an alias path changes.<p>
     *
     * @param controls the alias controls
     */
    protected void onChangePath(AliasControls controls) {

        CmsTextBox textbox = controls.getTextBox();
        CmsAliasBean alias = controls.getAlias();
        CmsSelectBox selectbox = controls.getSelectBox();
        String text = textbox.getText();
        alias.setSitePath(text);
        alias.setMode(CmsAliasMode.valueOf(selectbox.getFormValueAsString()));
    }

    /**
     * Performs a validation of the current list of aliases in the widget.<p>
     *
     * @param structureId the resource's structure id
     * @param sitePaths the map from ids to alias site paths
     *
     * @param errorCallback the callback to invoke when the validation finishes
     */
    protected void validateFull(
        CmsUUID structureId,
        Map<String, String> sitePaths,
        final AsyncCallback<Map<String, String>> errorCallback) {

        validateAliases(structureId, sitePaths, errorCallback);
    }

    /**
     * Validation method used when adding a new alias.<p>
     *
     * @param structureId the structure id
     * @param sitePaths the site paths
     * @param newSitePath the new site path
     * @param errorCallback on error callback
     */
    protected void validateSingle(
        CmsUUID structureId,
        Map<String, String> sitePaths,
        String newSitePath,
        final AsyncCallback<String> errorCallback) {

        Map<String, String> newMap = new HashMap<String, String>(sitePaths);
        newMap.put("NEW", newSitePath); //$NON-NLS-1$
        AsyncCallback<Map<String, String>> callback = new AsyncCallback<Map<String, String>>() {

            public void onFailure(Throwable caught) {

                assert false; // should never happen
            }

            public void onSuccess(Map<String, String> result) {

                String newRes = result.get("NEW"); //$NON-NLS-1$
                errorCallback.onSuccess(newRes);
            }
        };
        validateAliases(structureId, newMap, callback);
    }
}

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

package org.opencms.acacia.client;

import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.acacia.client.entity.CmsEntityBackend;
import org.opencms.acacia.client.entity.I_CmsEntityBackend;
import org.opencms.acacia.client.ui.CmsInlineEditOverlay;
import org.opencms.acacia.client.widgets.CmsFormWidgetWrapper;
import org.opencms.acacia.client.widgets.CmsStringWidget;
import org.opencms.acacia.client.widgets.CmsTinyMCEWidget;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;
import org.opencms.acacia.client.widgets.complex.CmsDataViewWidgetRenderer;
import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityHtml;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.acacia.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * The content editor base.<p>
 */
public class CmsEditorBase implements I_CmsInlineHtmlUpdateHandler {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHOICE_ADD_CHOICE_1 = "GUI_CHOICE_ADD_CHOICE_1"; //Add choice {0}

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_ADD_1 = "GUI_VIEW_ADD_1"; //Add {0}

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_CLOSE_0 = "GUI_VIEW_CLOSE_0"; //Close

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_DELETE_1 = "GUI_VIEW_DELETE_1"; //Delete {0}

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_EDIT_1 = "GUI_VIEW_EDIT_1"; // Edit {0}

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_MOVE_1 = "GUI_VIEW_MOVE_1"; //Move {0}

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_MOVE_DOWN_0 = "GUI_VIEW_MOVE_DOWN_0"; //Move down

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VIEW_MOVE_UP_0 = "GUI_VIEW_MOVE_UP_0"; //Move up

    /** The inline edit focus marker. */
    private static final String INLINE_EDIT_FOCUS_MARKER = "shouldFocusOnInlineEdit";

    /** The localized dictionary. */
    private static Dictionary m_dictionary;

    /** The id of the edited entity. */
    protected String m_entityId;

    /** The entity back-end instance. */
    protected I_CmsEntityBackend m_entityBackend;

    /** The in-line edit overlay hiding other content. */
    private CmsInlineEditOverlay m_editOverlay;

    /** The edited entity. */
    private CmsEntity m_entity;

    /** The form panel. */
    private FlowPanel m_formPanel;

    /** The tab panel if tabs are used. */
    private CmsTabbedPanel<?> m_formTabs;

    /** The window resize handler registration. */
    private HandlerRegistration m_resizeHandlerRegistration;

    /** The root attribute handler. */
    private CmsRootHandler m_rootHandler;

    /** The content service instance. */
    private I_CmsContentServiceAsync m_service;

    /** The tab infos. */
    private List<CmsTabInfo> m_tabInfos;

    /** The validation handler. */
    private CmsValidationHandler m_validationHandler;

    /** The widget service. */
    private I_CmsWidgetService m_widgetService;

    /**
     * Constructor.<p>
     *
     * @param service the content service
     * @param widgetService the widget service to use
     */
    public CmsEditorBase(I_CmsContentServiceAsync service, I_CmsWidgetService widgetService) {

        I_CmsLayoutBundle.INSTANCE.generalCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.buttonCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.highlightCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dialogCss().ensureInjected();
        org.opencms.acacia.client.css.I_CmsLayoutBundle.INSTANCE.form().ensureInjected();
        org.opencms.acacia.client.css.I_CmsLayoutBundle.INSTANCE.attributeChoice().ensureInjected();
        I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().ensureInjected();
        I_CmsWidgetsLayoutBundle.INSTANCE.galleryWidgetsCss().ensureInjected();
        m_service = service;
        m_entityBackend = CmsEntityBackend.getInstance();
        m_widgetService = widgetService;
        I_CmsEntityRenderer renderer = new CmsRenderer(m_entityBackend, m_widgetService);
        m_widgetService.setDefaultRenderer(renderer);
        m_widgetService.addWidgetFactory("string", new I_CmsWidgetFactory() {

            public I_CmsFormEditWidget createFormWidget(String configuration) {

                return new CmsFormWidgetWrapper(new CmsStringWidget());
            }

            public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

                return new CmsStringWidget(element);
            }
        });
        m_widgetService.addWidgetFactory("html", new I_CmsWidgetFactory() {

            public I_CmsFormEditWidget createFormWidget(String configuration) {

                return new CmsFormWidgetWrapper(new CmsTinyMCEWidget(null));
            }

            public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

                return new CmsTinyMCEWidget(element, null);
            }
        });

        // we may want to explicitly use the default renderer for specific attributes.
        m_widgetService.addRenderer(new CmsRenderer(CmsEntityBackend.getInstance(), getWidgetService()));
        m_widgetService.addRenderer(new CmsNativeComplexWidgetRenderer());
        m_widgetService.addRenderer(new CmsDataViewWidgetRenderer());
        m_validationHandler = new CmsValidationHandler();
        m_validationHandler.setContentService(m_service);
    }

    /**
     * Returns the formated message.<p>
     *
     * @param key the message key
     * @param args  the parameters to insert into the placeholders
     *
     * @return the formated message
     */
    public static String getMessageForKey(String key, Object... args) {

        String result = null;
        if (hasDictionary()) {
            result = m_dictionary.get(key);
            if ((result != null) && (args != null) && (args.length > 0)) {
                for (int i = 0; i < args.length; i++) {
                    result = result.replace("{" + i + "}", String.valueOf(args[i]));
                }
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Returns if the messages dictionary is set.<p>
     *
     * @return <code>true</code> if the messages dictionary is set
     */
    public static boolean hasDictionary() {

        return m_dictionary != null;
    }

    /**
     * Marks the given element to receive focus once the inline editing is initialized.<p>
     *
     * @param element the element to mark
     */
    public static void markForInlineFocus(Element element) {

        element.setAttribute("rel", INLINE_EDIT_FOCUS_MARKER);
    }

    /**
     * Sets the m_dictionary.<p>
     *
     * @param dictionary the m_dictionary to set
     */
    public static void setDictionary(Dictionary dictionary) {

        m_dictionary = dictionary;
    }

    /**
     * Checks whether the given element is marked to receive focus once the inline editing is initialized.<p>
     *
     * @param element the element to check
     *
     * @return <code>true</code> if the given element is marked to receive focus once the inline editing is initialized
     */
    public static boolean shouldFocusOnInlineEdit(Element element) {

        return INLINE_EDIT_FOCUS_MARKER.equals(element.getAttribute("rel"));
    }

    /**
     * Adds the value change handler to the entity with the given id.<p>
     *
     * @param entityId the entity id
     * @param handler the change handler
     */
    public void addEntityChangeHandler(String entityId, ValueChangeHandler<CmsEntity> handler) {

        CmsEntity entity = m_entityBackend.getEntity(entityId);
        if (entity != null) {
            entity.addValueChangeHandler(handler);
        }
    }

    /**
     * Adds a validation change handler.<p>
     *
     * @param handler the validation change handler
     *
     * @return the handler registration
     */
    public HandlerRegistration addValidationChangeHandler(ValueChangeHandler<CmsValidationContext> handler) {

        return m_validationHandler.addValueChangeHandler(handler);
    }

    /**
     * Destroys the form and related resources. Also clears all entities from the entity back-end<p>
     *
     * @param clearEntities <code>true</code> to also clear all entities
     */
    public void destroyForm(boolean clearEntities) {

        CmsValueFocusHandler.getInstance().destroy();
        if (clearEntities) {
            m_entityBackend.clearEntities();
        }
    }

    /**
     * Returns the currently edited entity.<p>
     *
     * @return the currently edited entity
     */
    public CmsEntity getCurrentEntity() {

        return m_entityBackend.getEntity(m_entityId);
    }

    /**
     * Returns the content service instance.<p>
     *
     * @return the content service
     */
    public I_CmsContentServiceAsync getService() {

        return m_service;
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     *
     * @param entityId the entity id
     * @param callback the callback
     */
    public void loadContentDefinition(final String entityId, final Command callback) {

        AsyncCallback<CmsContentDefinition> asyncCallback = new AsyncCallback<CmsContentDefinition>() {

            public void onFailure(Throwable caught) {

                onRpcError(caught);
            }

            public void onSuccess(CmsContentDefinition result) {

                registerContentDefinition(result);
                callback.execute();
            }
        };
        getService().loadContentDefinition(entityId, asyncCallback);
    }

    /**
     * Registers the types and entities of the given content definition.<p>
     *
     * @param definition the content definition
     */
    public void registerContentDefinition(CmsContentDefinition definition) {

        m_widgetService.addConfigurations(definition.getConfigurations());
        CmsType baseType = definition.getTypes().get(definition.getEntityTypeName());
        m_entityBackend.registerTypes(baseType, definition.getTypes());
        m_entityBackend.registerEntity(definition.getEntity());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler#reinitWidgets(org.opencms.acacia.client.I_CmsInlineFormParent)
     */
    public void reinitWidgets(I_CmsInlineFormParent formParent) {

        renderInlineEntity(m_entityId, formParent);
    }

    /**
     * Renders the entity form within the given context.<p>
     *
     * @param entityId the entity id
     * @param tabInfos the tab informations
     * @param context the context element
     * @param scrollParent the scroll element to be used for automatic scrolling during drag and drop
    
     */
    public void renderEntityForm(String entityId, List<CmsTabInfo> tabInfos, Panel context, Element scrollParent) {

        CmsEntity entity = m_entityBackend.getEntity(entityId);
        if (entity != null) {
            boolean initUndo = (m_entity == null) || !entity.getId().equals(m_entity.getId());
            m_entity = entity;
            CmsType type = m_entityBackend.getType(m_entity.getTypeName());
            m_formPanel = new FlowPanel();
            context.add(m_formPanel);
            CmsAttributeHandler.setScrollElement(scrollParent);
            CmsButtonBarHandler.INSTANCE.setWidgetService(m_widgetService);
            if (m_rootHandler == null) {
                m_rootHandler = new CmsRootHandler();
            } else {
                m_rootHandler.clearHandlers();
            }
            m_tabInfos = tabInfos;
            m_formTabs = m_widgetService.getRendererForType(type).renderForm(
                m_entity,
                m_tabInfos,
                m_formPanel,
                m_rootHandler,
                0);
            m_validationHandler.registerEntity(m_entity);
            m_validationHandler.setRootHandler(m_rootHandler);
            m_validationHandler.setFormTabPanel(m_formTabs);
            if (initUndo) {
                CmsUndoRedoHandler.getInstance().initialize(m_entity, this, m_rootHandler);
            }
            // trigger validation right away
            m_validationHandler.validate(m_entity);
        }
    }

    /**
     * Renders the entity form within the given context.<p>
     *
     * @param entityId the entity id
     * @param context the context element
     * @param scrollParent the scroll element to be used for automatic scrolling during drag and drop
     */
    public void renderEntityForm(String entityId, Panel context, Element scrollParent) {

        CmsEntity entity = m_entityBackend.getEntity(entityId);
        if (entity != null) {
            boolean initUndo = (m_entity == null) || !entity.getId().equals(m_entity.getId());
            m_entity = entity;
            CmsType type = m_entityBackend.getType(m_entity.getTypeName());
            m_formPanel = new FlowPanel();
            context.add(m_formPanel);
            CmsAttributeHandler.setScrollElement(scrollParent);
            CmsButtonBarHandler.INSTANCE.setWidgetService(m_widgetService);
            if (m_rootHandler == null) {
                m_rootHandler = new CmsRootHandler();
            } else {
                m_rootHandler.clearHandlers();
            }
            m_widgetService.getRendererForType(type).renderForm(m_entity, m_formPanel, m_rootHandler, 0);
            m_formTabs = null;
            m_tabInfos = null;
            m_validationHandler.setContentService(m_service);
            m_validationHandler.registerEntity(m_entity);
            m_validationHandler.setRootHandler(m_rootHandler);
            m_validationHandler.setFormTabPanel(null);
            if (initUndo) {
                CmsUndoRedoHandler.getInstance().initialize(m_entity, this, m_rootHandler);
            }
        }
    }

    /**
     * Renders the entity form within the given context.<p>
     *
     * @param entityId the entity id
     * @param formParent the form parent widget
     */
    public void renderInlineEntity(String entityId, I_CmsInlineFormParent formParent) {

        m_entity = m_entityBackend.getEntity(entityId);
        if (m_entity != null) {
            if (m_rootHandler == null) {
                m_rootHandler = new CmsRootHandler();
            } else {
                m_rootHandler.clearHandlers();
            }
            m_validationHandler.setContentService(m_service);
            m_validationHandler.registerEntity(m_entity);
            m_validationHandler.setRootHandler(m_rootHandler);
            CmsType type = m_entityBackend.getType(m_entity.getTypeName());
            CmsButtonBarHandler.INSTANCE.setWidgetService(m_widgetService);
            m_widgetService.getRendererForType(type).renderInline(m_entity, formParent, this, m_rootHandler, 0);
            CmsUndoRedoHandler.getInstance().initialize(m_entity, this, m_rootHandler);
        }
    }

    /**
     * Re-renders the form with the given entity data.<p>
     *
     * @param newContent the entity data
     */
    public void rerenderForm(CmsEntity newContent) {

        m_validationHandler.setPaused(true, m_entity);
        m_entityBackend.changeEntityContentValues(m_entity, newContent);
        CmsType type = m_entityBackend.getType(m_entity.getTypeName());
        if ((m_tabInfos != null) && !m_tabInfos.isEmpty()) {
            int currentTab = m_formTabs.getSelectedIndex();
            m_formPanel.clear();
            m_rootHandler.clearHandlers();
            m_formTabs = m_widgetService.getRendererForType(type).renderForm(
                m_entity,
                m_tabInfos,
                m_formPanel,
                m_rootHandler,
                0);
            m_formTabs.selectTab(currentTab);
        } else {
            m_formPanel.clear();
            m_rootHandler.clearHandlers();
            m_widgetService.getRendererForType(type).renderForm(m_entity, m_tabInfos, m_formPanel, m_rootHandler, 0);
        }
        m_validationHandler.setPaused(false, m_entity);
    }

    /**
     * Saves the given entities.<p>
     *
     * @param entities the entities to save
     * @param clearOnSuccess <code>true</code> to clear the entity back-end instance on success
     * @param callback the call back command
     */
    public void saveEntities(List<CmsEntity> entities, final boolean clearOnSuccess, final Command callback) {

        AsyncCallback<CmsValidationResult> asyncCallback = new AsyncCallback<CmsValidationResult>() {

            public void onFailure(Throwable caught) {

                onRpcError(caught);
            }

            public void onSuccess(CmsValidationResult result) {

                callback.execute();
                if ((result != null) && result.hasErrors()) {
                    //   CmsValidationHandler.getInstance().displayErrors(null, result)
                }
                if (clearOnSuccess) {
                    destroyForm(true);
                }
            }
        };
        getService().saveEntities(entities, asyncCallback);
    }

    /**
     * Saves the given entity.<p>
     *
     * @param entityIds the entity ids
     * @param clearOnSuccess <code>true</code> to clear all entities from entity back-end on success
     * @param callback the callback executed on success
     */
    public void saveEntities(Set<String> entityIds, boolean clearOnSuccess, Command callback) {

        List<CmsEntity> entities = new ArrayList<CmsEntity>();
        for (String entityId : entityIds) {
            CmsEntity entity = m_entityBackend.getEntity(entityId);
            if (entity != null) {
                entities.add(entity);
            }
        }
        saveEntities(entities, clearOnSuccess, callback);
    }

    /**
     * Saves the given entity.<p>
     *
     * @param entity the entity
     * @param clearOnSuccess <code>true</code> to clear all entities from entity back-end on success
     * @param callback the callback executed on success
     */
    public void saveEntity(CmsEntity entity, final boolean clearOnSuccess, final Command callback) {

        AsyncCallback<CmsValidationResult> asyncCallback = new AsyncCallback<CmsValidationResult>() {

            public void onFailure(Throwable caught) {

                onRpcError(caught);
            }

            public void onSuccess(CmsValidationResult result) {

                callback.execute();
                if (clearOnSuccess) {
                    destroyForm(true);
                }
            }
        };
        getService().saveEntity(entity, asyncCallback);
    }

    /**
     * Saves the given entity.<p>
     *
     * @param entityId the entity id
     * @param clearOnSuccess <code>true</code> to clear all entities from entity back-end on success
     * @param callback the callback executed on success
     */
    public void saveEntity(String entityId, boolean clearOnSuccess, Command callback) {

        CmsEntity entity = m_entityBackend.getEntity(entityId);
        saveEntity(entity, clearOnSuccess, callback);
    }

    /**
     * Saves the given entity.<p>
     *
     * @param entityId the entity id
     * @param callback the callback executed on success
     */
    public void saveEntity(String entityId, Command callback) {

        CmsEntity entity = m_entityBackend.getEntity(entityId);
        saveEntity(entity, false, callback);
    }

    /**
    * @see org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler#updateHtml(org.opencms.acacia.client.I_CmsInlineFormParent, com.google.gwt.user.client.Command)
    */
    public void updateHtml(final I_CmsInlineFormParent formParent, final Command onSuccess) {

        AsyncCallback<CmsEntityHtml> callback = new AsyncCallback<CmsEntityHtml>() {

            public void onFailure(Throwable caught) {

                onRpcError(caught);
            }

            public void onSuccess(CmsEntityHtml result) {

                if (result.getHtmlContent() != null) {
                    formParent.replaceHtml(result.getHtmlContent());
                    onSuccess.execute();
                }
            }
        };
        getService().updateEntityHtml(getCurrentEntity(), getContextUri(), getHtmlContextInfo(), callback);
    }

    /**
     * Adds a click handler to the edit overlay.<p>
     *
     * @param handler the click handler
     *
     * @return the click handler registration
     */
    protected HandlerRegistration addOverlayClickHandler(ClickHandler handler) {

        return m_editOverlay.addClickHandler(handler);
    }

    /**
     * Clears the editor.<p>
     */
    protected void clearEditor() {

        removeEditOverlays();
        CmsUndoRedoHandler.getInstance().clear();
        m_validationHandler.clear();
        m_entity = null;
        m_entityId = null;
        m_tabInfos = null;
        m_rootHandler = null;
        m_formPanel = null;
        m_formTabs = null;
    }

    /**
     * Returns the context URI.<p>
     * Needed when updating the HTML due to content data changes.<p>
     *
     * Override to supply the required info.<p>
     *
     * @return the context URI
     */
    protected String getContextUri() {

        return "";
    }

    /**
     * Returns the in-line HTML context info.<p>
     * Needed when updating the HTML due to content data changes.<p>
     *
     * Override to supply the required info.<p>
     *
     * @return the HTML context info
     */
    protected String getHtmlContextInfo() {

        return "";
    }

    /**
     * Returns the root attribute handler.<p>
     *
     * @return the root attribute handler
     */
    protected CmsRootHandler getRootAttributeHandler() {

        return m_rootHandler;
    }

    /**
     * Returns the validation handler.<p>
     *
     * @return the validation handler
     */
    protected CmsValidationHandler getValidationHandler() {

        return m_validationHandler;
    }

    /**
     * Returns the widget service.<p>
     *
     * @return the widget service
     */
    protected I_CmsWidgetService getWidgetService() {

        return m_widgetService;
    }

    /**
     * Initializes the edit overlay to be positioned around the given element.<p>
     *
     * @param element the element
     */
    protected void initEditOverlay(Element element) {

        CmsInlineEditOverlay.removeAll();
        m_editOverlay = CmsInlineEditOverlay.addOverlayForElement(element);
        if (m_resizeHandlerRegistration != null) {
            m_resizeHandlerRegistration.removeHandler();
        }
        // add a handler to ensure the edit overlays get adjusted to changed window size
        m_resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {

            private Timer m_resizeTimer;

            public void onResize(ResizeEvent event) {

                if (m_resizeTimer == null) {
                    m_resizeTimer = new Timer() {

                        @Override
                        public void run() {

                            handleResize();
                        }
                    };
                    m_resizeTimer.schedule(300);
                }
            }

            /**
             * Handles the window resize.<p>
             */
            void handleResize() {

                m_resizeTimer = null;
                CmsInlineEditOverlay.updateCurrentOverlayPosition();
            }
        });
    }

    /**
     * Handles RPC errors.<p>
     *
     * Override this for better error handling
     *
     * @param caught the error caught from the RPC
     */
    protected void onRpcError(Throwable caught) {

        // doing nothing
    }

    /**
     * Removes the edit overlay from the DOM.<p>
     */
    protected void removeEditOverlays() {

        CmsInlineEditOverlay.removeAll();
        m_editOverlay = null;
        if (m_resizeHandlerRegistration != null) {
            m_resizeHandlerRegistration.removeHandler();
            m_resizeHandlerRegistration = null;
        }
    }

    /**
     * Updates the edit overlay position.<p>
     */
    protected void updateOverlayPosition() {

        if (m_editOverlay != null) {
            m_editOverlay.updatePosition();
        }
    }
}

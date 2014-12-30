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

package org.opencms.ade.contenteditor.client;

import org.opencms.acacia.client.CmsAttributeHandler;
import org.opencms.acacia.client.CmsEditorBase;
import org.opencms.acacia.client.CmsUndoRedoHandler;
import org.opencms.acacia.client.CmsUndoRedoHandler.UndoRedoState;
import org.opencms.acacia.client.CmsValidationContext;
import org.opencms.acacia.client.CmsValueFocusHandler;
import org.opencms.acacia.client.I_CmsEntityRenderer;
import org.opencms.acacia.client.I_CmsInlineFormParent;
import org.opencms.acacia.client.entity.CmsEntityBackend;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.ade.contenteditor.shared.CmsComplexWidgetData;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsInfoHeader;
import org.opencms.gwt.client.ui.CmsModelSelectDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotificationMessage;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.CmsToolbar;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.I_CmsModelSelectHandler;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The content editor.<p>
 */
public final class CmsContentEditor extends CmsEditorBase {

    /**
     * CmsEntity change handler to watch for changes within given scopes and call the editor change handlers accordingly.<p>
     */
    class EditorChangeHandler implements ValueChangeHandler<CmsEntity> {

        /** The scope values. */
        Map<String, String> m_scopeValues;

        /** The change handler registration. */
        private HandlerRegistration m_handlerRegistration;

        /** The observed entity. */
        private CmsEntity m_observerdEntity;

        /**
         * Constructor.<p>
         * 
         * @param entity the entity to observe
         * @param changeScopes the value scopes to watch for changes
         */
        public EditorChangeHandler(CmsEntity entity, Collection<String> changeScopes) {

            m_observerdEntity = entity;
            m_handlerRegistration = entity.addValueChangeHandler(this);
            m_scopeValues = new HashMap<String, String>();
            for (String scope : changeScopes) {
                m_scopeValues.put(scope, CmsContentDefinition.getValueForPath(m_observerdEntity, scope));
            }
        }

        /**
         * Removes this observer from the entities change handler registration and clears registered listeners.<p>
         */
        public void clear() {

            if (m_handlerRegistration != null) {
                m_handlerRegistration.removeHandler();
                m_handlerRegistration = null;
            }
            m_scopeValues.clear();
            m_observerdEntity = null;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<CmsEntity> event) {

            CmsEntity entity = event.getValue();
            Set<String> changedScopes = new HashSet<String>();
            for (String scope : m_scopeValues.keySet()) {
                String scopeValue = CmsContentDefinition.getValueForPath(entity, scope);
                String previousValue = m_scopeValues.get(scope);
                if (((scopeValue != null) && !scopeValue.equals(previousValue))
                    || ((scopeValue == null) && (previousValue != null))) {
                    // the value within this scope has changed, notify all listeners
                    changedScopes.add(scope);
                    m_scopeValues.put(scope, scopeValue);
                }
            }
            if (!changedScopes.isEmpty()) {
                callEditorChangeHanlders(changedScopes);
            }
        }
    }

    /** The add change listener method name. */
    private static final String ADD_CHANGE_LISTENER_METHOD = "cmsAddEntityChangeListener";

    /** The get current entity method name. */
    private static final String GET_CURRENT_ENTITY_METHOD = "cmsGetCurrentEntity";

    /** The in-line editor instance. */
    private static CmsContentEditor INSTANCE;

    /** The current content locale. */
    protected String m_locale;

    /** The on close call back. */
    protected Command m_onClose;

    /** The edit tool-bar. */
    protected CmsToolbar m_toolbar;

    /** Value of the auto-unlock option from the configuration. */
    private boolean m_autoUnlock;

    /** The available locales. */
    private Map<String, String> m_availableLocales;

    /** The form editing base panel. */
    private FlowPanel m_basePanel;

    /** The cancel button. */
    private CmsPushButton m_cancelButton;

    /** The id's of the changed entities. */
    private Set<String> m_changedEntityIds;

    /** The window closing handler registration. */
    private HandlerRegistration m_closingHandlerRegistration;

    /** The locales present within the edited content. */
    private Set<String> m_contentLocales;

    /** The editor context. */
    private CmsEditorContext m_context;

    /** The copy locale button. */
    private CmsPushButton m_copyLocaleButton;

    /** The loaded content definitions by locale. */
    private Map<String, CmsContentDefinition> m_definitions;

    /** The entities to delete. */
    private Set<String> m_deletedEntities;

    /** The delete locale button. */
    private CmsPushButton m_deleteLocaleButton;

    /** Flag indicating the resource needs to removed on cancel. */
    private boolean m_deleteOnCancel;

    /** The entity value change handler calling configured editor change handlers. */
    private EditorChangeHandler m_editorChangeHandler;

    /** The entity observer instance. */
    private CmsEntityObserver m_entityObserver;

    /** The hide help bubbles button. */
    private CmsToggleButton m_hideHelpBubblesButton;

    /** Flag which indicate whether the directedit parameter was set to true when loading the editor. */
    private boolean m_isDirectEdit;

    /** Flag indicating the editor was opened as the stand alone version, not from within any other module. */
    private boolean m_isStandAlone;

    /** The locale select label. */
    private CmsLabel m_localeLabel;

    /** The locale select box. */
    private CmsSelectBox m_localeSelect;

    /** The open form button. */
    private CmsPushButton m_openFormButton;

    /** The publish button. */
    private CmsPushButton m_publishButton;

    /** The redo button. */
    private CmsPushButton m_redoButton;

    /** The registered entity id's. */
    private Set<String> m_registeredEntities;

    /** The resource type name. */
    private String m_resourceTypeName;

    /** The save button. */
    private CmsPushButton m_saveButton;

    /** The save and exit button. */
    private CmsPushButton m_saveExitButton;

    /** The content service. */
    private I_CmsContentServiceAsync m_service;

    /** The resource site path. */
    private String m_sitePath;

    /** The tab informations for this form. */
    private List<CmsTabInfo> m_tabInfos;

    /** The resource title. */
    private String m_title;

    /** The undo button. */
    private CmsPushButton m_undoButton;

    /** The undo redo event handler registration. */
    private HandlerRegistration m_undoRedoHandlerRegistration;

    /**
     * Constructor.<p>
     */
    private CmsContentEditor() {

        super((I_CmsContentServiceAsync)GWT.create(I_CmsContentService.class), new CmsDefaultWidgetService());
        m_service = (I_CmsContentServiceAsync)super.getService();
        String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.contenteditor.CmsContentService.gwt");
        ((ServiceDefTarget)m_service).setServiceEntryPoint(serviceUrl);
        getWidgetService().setWidgetFactories(WidgetRegistry.getInstance().getWidgetFactories());
        for (I_CmsEntityRenderer renderer : WidgetRegistry.getInstance().getRenderers()) {
            getWidgetService().addRenderer(renderer);
        }
        // set the acacia editor message bundle
        setDictionary(Messages.get().getDictionary());
        I_CmsLayoutBundle.INSTANCE.editorCss().ensureInjected();
        m_changedEntityIds = new HashSet<String>();
        m_registeredEntities = new HashSet<String>();
        m_availableLocales = new HashMap<String, String>();
        m_contentLocales = new HashSet<String>();
        m_deletedEntities = new HashSet<String>();
        m_definitions = new HashMap<String, CmsContentDefinition>();
        addValidationChangeHandler(new ValueChangeHandler<CmsValidationContext>() {

            public void onValueChange(ValueChangeEvent<CmsValidationContext> event) {

                handleValidationChange(event.getValue());
            }
        });
    }

    /**
     * Adds an entity change listener.<p>
     *
     * @param changeListener the change listener
     * @param changeScope the change scope
     */
    public static void addEntityChangeListener(I_CmsEntityChangeListener changeListener, String changeScope) {

        CmsDebugLog.getInstance().printLine("trying to ad change listener for scope: " + changeScope);
        if ((INSTANCE == null) || (INSTANCE.m_entityObserver == null)) {
            CmsDebugLog.getInstance().printLine("handling external registration");
            if (isObserverExported()) {
                CmsDebugLog.getInstance().printLine("registration is available");
                try {
                    addNativeListsner(changeListener, changeScope);
                } catch (Exception e) {

                    CmsDebugLog.getInstance().printLine(
                        "Exception occured during listener registration" + e.getMessage());
                }
            } else {
                throw new RuntimeException("Editor is not initialized yet.");
            }
        } else {
            INSTANCE.m_entityObserver.addEntityChangeListener(changeListener, changeScope);
        }
    }

    /**
     * Returns the currently edited entity.<p>
     *
     * @return the currently edited entity
     */
    public static CmsEntity getEntity() {

        if ((INSTANCE == null) || (INSTANCE.m_entityObserver == null)) {
            CmsDebugLog.getInstance().printLine("handling external registration");
            if (isObserverExported()) {
                return CmsEntityBackend.createFromNativeWrapper(nativeGetEntity());
            } else {
                throw new RuntimeException("Editor is not initialized yet.");
            }
        } else {
            return INSTANCE.getCurrentEntity();
        }
    }

    /**
     * Returns the in-line editor instance.<p>
     *
     * @return the in-line editor instance
     */
    public static CmsContentEditor getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContentEditor();
        }
        return INSTANCE;
    }

    /**
     * Returns if the given element or it's descendants are inline editable.<p>
     *
     * @param element the element
     *
     * @return <code>true</code> if the element has editable descendants
     */
    public static boolean hasEditable(Element element) {

        NodeList<Element> children = CmsDomUtil.querySelectorAll("[property^=\"opencms://\"]", element);
        return (children != null) && (children.getLength() > 0);
    }

    /**
     * Checks whether the given element is annotated for inline editing.<p>
     *
     * @param element the element to check
     *
     * @return <code>true</code> if the given element is annotated for inline editing
     */
    public static boolean isEditable(Element element) {

        String property = element.getAttribute("property");
        return (property != null) && property.startsWith("opencms://");
    }

    /**
     * Replaces the id's within about attributes of the given element and all it's children.<p>
     *
     * @param element the element
     * @param oldId the old id
     * @param newId the new id
     */
    public static void replaceResourceIds(Element element, String oldId, String newId) {

        String about = element.getAttribute("about");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(about)) {
            about = about.replace(oldId, newId);
            element.setAttribute("about", about);
        }
        Element child = element.getFirstChildElement();
        while (child != null) {
            replaceResourceIds(child, oldId, newId);
            child = child.getNextSiblingElement();
        }
    }

    /**
     * Sets all annotated child elements editable.<p>
     *
     * @param element the element
     * @param editable <code>true</code> to enable editing
     *
     * @return <code>true</code> if the element had editable elements
     */
    public static boolean setEditable(Element element, boolean editable) {

        I_CmsLayoutBundle.INSTANCE.editorCss().ensureInjected();
        NodeList<Element> children = CmsDomUtil.querySelectorAll("[property^=\"opencms://\"]", element);
        if (children.getLength() > 0) {
            for (int i = 0; i < children.getLength(); i++) {
                Element child = children.getItem(i);
                if (editable) {
                    child.addClassName(I_CmsLayoutBundle.INSTANCE.editorCss().inlineEditable());
                } else {
                    child.removeClassName(I_CmsLayoutBundle.INSTANCE.editorCss().inlineEditable());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds the change listener.<p>
     *
     * @param changeListener the change listener
     * @param changeScope the change scope
     */
    static native void addNativeListsner(I_CmsEntityChangeListener changeListener, String changeScope)/*-{
                                                                                                              var instance = changeListener;
                                                                                                              var nat = {
                                                                                                              onChange : function(entity) {
                                                                                                              var cmsEntity=@org.opencms.acacia.client.entity.CmsEntityBackend::createFromNativeWrapper(Lcom/google/gwt/core/client/JavaScriptObject;)(entity);
                                                                                                              instance.@org.opencms.ade.contenteditor.client.I_CmsEntityChangeListener::onEntityChange(Lorg/opencms/acacia/shared/CmsEntity;)(cmsEntity);
                                                                                                              }
                                                                                                              }
                                                                                                              var method = $wnd[@org.opencms.ade.contenteditor.client.CmsContentEditor::ADD_CHANGE_LISTENER_METHOD];
                                                                                                              if (typeof method == 'function') {
                                                                                                              method(nat, changeScope);
                                                                                                              }
                                                                                                              }-*/;

    /**
     * Checks whether the add entity change listener method has been exported.<p>
     *
     * @return <code>true</code> if the add entity change listener method has been exported
     */
    private static native boolean isObserverExported()/*-{
                                                      var method = $wnd[@org.opencms.ade.contenteditor.client.CmsContentEditor::ADD_CHANGE_LISTENER_METHOD];
                                                      if (typeof method == 'function') {
                                                      return true;
                                                      } else {
                                                      return false;
                                                      }
                                                      }-*/;

    /**
     * Returns the current entity.<p>
     *
     * @return the current entity
     */
    private static native JavaScriptObject nativeGetEntity()/*-{
                                                            return $wnd[@org.opencms.ade.contenteditor.client.CmsContentEditor::GET_CURRENT_ENTITY_METHOD]
                                                            ();
                                                            }-*/;

    /**
     * Closes the editor.<p>
     * May be used from outside the editor module.<p>
     */
    public void closeEditor() {

        if (m_saveButton != null) {
            if (m_saveButton.isEnabled()) {
                CmsConfirmDialog dialog = new CmsConfirmDialog(org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TITLE_0), Messages.get().key(
                    Messages.GUI_CONFIRM_LEAVING_EDITOR_0));
                dialog.setHandler(new I_CmsConfirmDialogHandler() {

                    public void onClose() {

                        cancelEdit();
                    }

                    public void onOk() {

                        saveAndExit();
                    }
                });
                dialog.center();
            } else {
                cancelEdit();
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.CmsEditorBase#getService()
     */
    @Override
    public I_CmsContentServiceAsync getService() {

        return m_service;
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     *
     * @param entityId the entity id
     * @param editedEntity the currently edited entity
     * @param callback the callback
     */
    public void loadDefinition(
        final String entityId,
        final CmsEntity editedEntity,
        final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(0, true);
                getService().loadDefinition(entityId, editedEntity, getSkipPaths(), this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                registerContentDefinition(result);
                WidgetRegistry.getInstance().registerExternalWidgets(
                    result.getExternalWidgetConfigurations(),
                    new Command() {

                        public void execute() {

                            stop(false);
                            callback.execute(result);
                        }
                    });
            }
        };
        action.execute();
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     * 
     * @param entityId the entity id
     * @param newLink the new link
     * @param modelFileId  the model file id
     * @param mode the content creation mode
     * @param postCreateHandler the post-create handler class name (optional)
     * @param callback the callback
     */
    public void loadInitialDefinition(
        final String entityId,
        final String newLink,
        final CmsUUID modelFileId,
        final String postCreateHandler,
        final String mode,
        final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(0, true);
                getService().loadInitialDefinition(
                    entityId,
                    newLink,
                    modelFileId,
                    CmsCoreProvider.get().getUri(),
                    postCreateHandler,
                    mode,
                    this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                if (result.isModelInfo()) {
                    callback.execute(result);
                } else {
                    registerContentDefinition(result);
                    WidgetRegistry.getInstance().registerExternalWidgets(
                        result.getExternalWidgetConfigurations(),
                        new Command() {

                            public void execute() {

                                stop(false);
                                callback.execute(result);
                            }
                        });
                }
            }
        };
        action.execute();
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     *
     * @param entityId the entity id
     * @param editedEntity the currently edited entity
     * @param callback the callback
     */
    public void loadNewDefinition(
        final String entityId,
        final CmsEntity editedEntity,
        final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(0, true);
                getService().loadNewDefinition(entityId, editedEntity, getSkipPaths(), this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                registerContentDefinition(result);
                WidgetRegistry.getInstance().registerExternalWidgets(
                    result.getExternalWidgetConfigurations(),
                    new Command() {

                        public void execute() {

                            stop(false);
                            callback.execute(result);
                        }
                    });
            }
        };
        action.execute();
    }

    /**
     * Opens the content editor dialog.<p>
     *
     * @param context the editor context
     * @param locale the content locale
     * @param elementId the element id
     * @param newLink the new link
     * @param modelFileId the model file id
     * @param postCreateHandler the post-create handler class (optional)
     * @param mode the content creation mode
     * @param onClose the command executed on dialog close
     */
    public void openFormEditor(
        final CmsEditorContext context,
        String locale,
        String elementId,
        String newLink,
        CmsUUID modelFileId,
        String postCreateHandler,
        String mode,
        Command onClose) {

        m_onClose = onClose;
        CmsUUID structureId = new CmsUUID(elementId);
        // make sure the resource is locked, if we are not creating a new one
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(newLink) || CmsCoreProvider.get().lock(structureId)) {
            loadInitialDefinition(
                CmsContentDefinition.uuidToEntityId(structureId, locale),
                newLink,
                modelFileId,
                mode,
                postCreateHandler,
                new I_CmsSimpleCallback<CmsContentDefinition>() {

                    public void execute(CmsContentDefinition contentDefinition) {

                        if (contentDefinition.isModelInfo()) {
                            openModelSelectDialog(context, contentDefinition);
                        } else {
                            initEditor(context, contentDefinition, null, false);
                        }
                    }
                });
        } else {
            showLockedResourceMessage();
        }

    }

    /**
     * Renders the in-line editor for the given element.<p>
     *
     * @param context the editor context
     * @param elementId the element id
     * @param locale the content locale
     * @param panel the element panel
     * @param onClose the command to execute on close
     */
    public void openInlineEditor(

    final CmsEditorContext context, CmsUUID elementId, String locale, final I_CmsInlineFormParent panel, Command onClose) {

        String entityId = CmsContentDefinition.uuidToEntityId(elementId, locale);
        m_locale = locale;
        m_onClose = onClose;
        if (CmsCoreProvider.get().lock(elementId)) {
            loadInitialDefinition(entityId, null, null, null, null, new I_CmsSimpleCallback<CmsContentDefinition>() {

                public void execute(CmsContentDefinition contentDefinition) {

                    initEditor(context, contentDefinition, panel, true);
                }
            });
        } else {
            showLockedResourceMessage();
        }
    }

    /**
     * Opens the form based editor. Used within the stand alone contenteditor.jsp.<p>
     *
     * @param context the editor context
     */
    public void openStandAloneFormEditor(final CmsEditorContext context) {

        final CmsContentDefinition definition;
        try {
            definition = (CmsContentDefinition)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getService(),
                I_CmsContentService.DICT_CONTENT_DEFINITION);
        } catch (SerializationException e) {
            RootPanel.get().add(new Label(e.getMessage()));
            return;
        }
        m_isStandAlone = true;
        if (definition.isModelInfo()) {
            openModelSelectDialog(context, definition);
        } else {
            if (CmsCoreProvider.get().lock(CmsContentDefinition.entityIdToUuid(definition.getEntityId()))) {

                registerContentDefinition(definition);
                // register all external widgets
                WidgetRegistry.getInstance().registerExternalWidgets(
                    definition.getExternalWidgetConfigurations(),
                    new Command() {

                        public void execute() {

                            initEditor(context, definition, null, false);
                        }
                    });

            } else {
                showLockedResourceMessage();
            }
        }
    }

    /**
     * Registers a deep copy of the source entity with the given target entity id.<p>
     *
     * @param sourceEntityId the source entity id
     * @param targetEntityId the target entity id
     */
    public void registerClonedEntity(String sourceEntityId, String targetEntityId) {

        CmsEntityBackend.getInstance().getEntity(sourceEntityId).createDeepCopy(targetEntityId);
    }

    /**
     * Registers the given content definition.<p>
     *
     * @param definition the content definition
     */
    public void registerContentDefinition(CmsContentDefinition definition) {

        getWidgetService().addConfigurations(definition.getConfigurations());
        CmsType baseType = definition.getTypes().get(definition.getEntityTypeName());
        m_entityBackend.registerTypes(baseType, definition.getTypes());
        for (CmsEntity entity : definition.getEntities().values()) {
            CmsEntity previousValue = m_entityBackend.getEntity(entity.getId());
            if (previousValue != null) {
                m_entityBackend.changeEntityContentValues(previousValue, entity);
            } else {
                m_entityBackend.registerEntity(entity);
                m_registeredEntities.add(entity.getId());
            }
        }
        for (Map.Entry<String, CmsComplexWidgetData> entry : definition.getComplexWidgetData().entrySet()) {
            String attrName = entry.getKey();
            CmsComplexWidgetData widgetData = entry.getValue();
            getWidgetService().registerComplexWidgetAttribute(
                attrName,
                widgetData.getRendererName(),
                widgetData.getConfiguration());
        }
    }

    /**
     * Saves the given entities.<p>
     *
     * @param clearOnSuccess <code>true</code> to clear the VIE instance on success
     * @param callback the call back command
     */
    public void saveAndDeleteEntities(final boolean clearOnSuccess, final Command callback) {

        final CmsEntity entity = m_entityBackend.getEntity(m_entityId);
        saveAndDeleteEntities(entity, new ArrayList<String>(m_deletedEntities), clearOnSuccess, callback);
    }

    /**
     * Saves the given entities.<p>
     *
     * @param lastEditedEntity the last edited entity
     * @param deletedEntites the deleted entity id's
     * @param clearOnSuccess <code>true</code> to clear the VIE instance on success
     * @param callback the call back command
     */
    public void saveAndDeleteEntities(
        final CmsEntity lastEditedEntity,
        final List<String> deletedEntites,
        final boolean clearOnSuccess,
        final Command callback) {

        CmsRpcAction<CmsValidationResult> asyncCallback = new CmsRpcAction<CmsValidationResult>() {

            @Override
            public void execute() {

                start(200, true);
                getService().saveAndDeleteEntities(
                    lastEditedEntity,
                    deletedEntites,
                    getSkipPaths(),
                    m_locale,
                    clearOnSuccess,
                    this);
            }

            @Override
            protected void onResponse(CmsValidationResult result) {

                stop(false);
                if ((result != null) && result.hasErrors()) {
                    showValidationErrorDialog(result);
                } else {
                    callback.execute();
                    if (clearOnSuccess) {
                        destroyForm(true);
                    }
                }
            }
        };
        asyncCallback.execute();
    }

    /**
     * Sets the show editor help flag to the user session.<p>
     *
     * @param show the show editor help flag
     */
    public void setShowEditorHelp(final boolean show) {

        CmsCoreProvider.get().setShowEditorHelp(show);
    }

    /**
     * Removes the given entity from the entity VIE store.<p>
     *
     * @param entityId the entity id
     */
    public void unregistereEntity(String entityId) {

        CmsEntityBackend.getInstance().removeEntity(entityId);
    }

    /**
     * @see org.opencms.acacia.client.CmsEditorBase#clearEditor()
     */
    @Override
    protected void clearEditor() {

        super.clearEditor();
        m_context = null;
        if (m_undoRedoHandlerRegistration != null) {
            m_undoRedoHandlerRegistration.removeHandler();
        }
        if (m_toolbar != null) {
            m_toolbar.removeFromParent();
            m_toolbar = null;
        }
        m_cancelButton = null;
        m_localeSelect = null;
        m_localeLabel = null;
        m_deleteLocaleButton = null;
        m_copyLocaleButton = null;
        m_openFormButton = null;
        m_saveButton = null;
        m_onClose = null;
        m_locale = null;
        if (m_basePanel != null) {
            m_basePanel.removeFromParent();
            m_basePanel = null;
        }
        if (m_entityObserver != null) {
            m_entityObserver.clear();
            m_entityObserver = null;
        }
        m_changedEntityIds.clear();
        m_registeredEntities.clear();
        m_availableLocales.clear();
        m_contentLocales.clear();
        m_deletedEntities.clear();
        m_definitions.clear();
        m_title = null;
        m_sitePath = null;
        m_resourceTypeName = null;
        if (m_closingHandlerRegistration != null) {
            m_closingHandlerRegistration.removeHandler();
            m_closingHandlerRegistration = null;
        }
        if (m_isStandAlone) {
            closeEditorWidow();
        } else {
            RootPanel.getBodyElement().getParentElement().getStyle().clearOverflow();
        }
    }

    /**
     * Gets the editor context.<p>
     *
     * @return the editor context
     */
    protected CmsEditorContext getContext() {

        return m_context;
    }

    /**
     * @see org.opencms.acacia.client.CmsEditorBase#getContextUri()
     */
    @Override
    protected String getContextUri() {

        return CmsCoreProvider.get().getUri();
    }

    /**
     * Gets the entity id.<p>
     *
     * @return the entity id
     */
    protected String getEntityId() {

        return m_entityId;
    }

    /**
     * @see org.opencms.acacia.client.CmsEditorBase#getHtmlContextInfo()
     */
    @Override
    protected String getHtmlContextInfo() {

        return m_context.getHtmlContextInfo();
    }

    /**
     * Returns the paths to be skipped when synchronizing locale independent fields.<p>
     *
     * @return the paths to be skipped when synchronizing locale independent fields
     */
    protected Collection<String> getSkipPaths() {

        return ((CmsDefaultWidgetService)getWidgetService()).getSkipPaths();
    }

    /**
     * Adds a content definition to the internal store.<p>
     *
     * @param definition the definition to add
     */
    void addContentDefinition(CmsContentDefinition definition) {

        m_definitions.put(definition.getLocale(), definition);
        m_contentLocales.add(definition.getLocale());
    }

    /**
     * Calls the editor change handlers.<p>
     * 
     * @param changedScopes the changed content value scopes
     */
    void callEditorChangeHanlders(final Set<String> changedScopes) {

        final CmsEntity entity = m_entityBackend.getEntity(m_entityId);
        final org.opencms.acacia.shared.CmsEntity currentState = entity.createDeepCopy(m_entityId);
        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(200, true);
                getService().callEditorChangeHandlers(getEntityId(), currentState, getSkipPaths(), changedScopes, this);
            }

            @Override
            protected void onResponse(CmsContentDefinition result) {

                stop(false);
                updateEditorValues(currentState, result.getEntity());
            }
        };
        action.execute();
    }

    /**
     * Cancels the editing process.<p>
     */
    void cancelEdit() {

        // store the scroll position
        int scrollTop = RootPanel.getBodyElement().getOwnerDocument().getScrollTop();
        unlockResource();
        if (m_onClose != null) {
            m_onClose.execute();
        }
        destroyForm(true);
        clearEditor();
        // restore the scroll position
        RootPanel.getBodyElement().getOwnerDocument().setScrollTop(scrollTop);
    }

    /**
     * Asks the user to confirm resetting all changes.<p>
     */
    void confirmCancel() {

        if (m_saveButton.isEnabled()) {
            CmsConfirmDialog dialog = new CmsConfirmDialog(org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TITLE_0), org.opencms.gwt.client.Messages.get().key(
                org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TEXT_0));
            dialog.setHandler(new I_CmsConfirmDialogHandler() {

                public void onClose() {

                    // nothing to do
                }

                public void onOk() {

                    cancelEdit();
                }
            });
            dialog.center();
        } else {
            cancelEdit();
        }
    }

    /**
     * Opens the confirm delete locale dialog.<p>
     */
    void confirmDeleteLocale() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(
            Messages.get().key(Messages.GUI_CONFIRM_DELETE_LOCALE_TITLE_0),
            Messages.get().key(Messages.GUI_CONFIRM_DELETE_LOCALE_TEXT_0));
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

            public void onClose() {

                // nothing to do
            }

            public void onOk() {

                deleteCurrentLocale();
            }
        });
        dialog.center();
    }

    /**
     * Copies the current entity values to the given locales.<p>
     *
     * @param targetLocales the target locales
     */
    void copyLocales(final Set<String> targetLocales) {

        final CmsEntity entity = m_entityBackend.getEntity(m_entityId);
        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getService().copyLocale(targetLocales, entity, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
            }
        };
        action.execute();
        for (String targetLocale : targetLocales) {
            String targetId = getIdForLocale(targetLocale);
            if (!m_entityId.equals(targetId)) {
                if (m_registeredEntities.contains(targetId)) {
                    unregistereEntity(targetId);
                }
                registerClonedEntity(m_entityId, targetId);
                m_registeredEntities.add(targetId);
                m_changedEntityIds.add(targetId);
                m_contentLocales.add(targetLocale);
                m_deletedEntities.remove(targetId);
                enableSave();
            }
        }
        initLocaleSelect();
    }

    /**
     * Deferrers the save action to the end of the browser event queue.<p>
     */
    void deferSave() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                save();
            }
        });
    }

    /**
     * Deferrers the save and exit action to the end of the browser event queue.<p>
     */
    void deferSaveAndExit() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                saveAndExit();
            }
        });
    }

    /**
     * Deletes the current locale.<p>
     */
    void deleteCurrentLocale() {

        // there has to remain at least one content locale
        if (m_contentLocales.size() > 1) {
            String deletedLocale = m_locale;
            m_contentLocales.remove(deletedLocale);
            m_registeredEntities.remove(m_entityId);
            m_changedEntityIds.remove(m_entityId);
            m_deletedEntities.add(m_entityId);
            getValidationHandler().getValidationContext().removeEntityId(m_entityId);
            unregistereEntity(m_entityId);
            enableSave();
            String nextLocale = null;
            if (m_registeredEntities.isEmpty()) {
                nextLocale = m_contentLocales.iterator().next();
            } else {
                nextLocale = CmsContentDefinition.getLocaleFromId(m_registeredEntities.iterator().next());
            }
            switchLocale(nextLocale);
        }
    }

    /**
     * Disables the save buttons with the given message.<p>
     *
     * @param message the disabled message
     */
    void disableSave(String message) {

        m_saveButton.disable(message);
        m_saveExitButton.disable(message);
    }

    /**
     * Leaves the editor saving the content if necessary.<p>
     */
    void exitWithSaving() {

        if (m_saveExitButton.isEnabled()) {
            saveAndExit();
        } else {
            cancelEdit();
        }
    }

    /**
     * Handles validation changes.<p>
     *
     * @param validationContext the changed validation context
     */
    void handleValidationChange(CmsValidationContext validationContext) {

        if (validationContext.hasValidationErrors()) {
            String locales = "";
            for (String id : validationContext.getInvalidEntityIds()) {
                if (locales.length() > 0) {
                    locales += ", ";
                }
                String locale = CmsContentDefinition.getLocaleFromId(id);
                if (m_availableLocales.containsKey(locale)) {
                    locales += m_availableLocales.get(locale);
                }
            }
            disableSave(Messages.get().key(Messages.GUI_TOOLBAR_VALIDATION_ERRORS_1, locales));
        } else if (!m_changedEntityIds.isEmpty()) {
            enableSave();
        }
    }

    /**
     * Hides the editor help bubbles.<p>
     *
     * @param hide <code>true</code> to hide the help bubbles
     */
    void hideHelpBubbles(boolean hide) {

        setShowEditorHelp(!hide);
        CmsValueFocusHandler.getInstance().hideHelpBubbles(RootPanel.get(), hide);
        if (!hide) {
            m_hideHelpBubblesButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_HELP_BUBBLES_SHOWN_0));
        } else {
            m_hideHelpBubblesButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_HELP_BUBBLES_HIDDEN_0));
        }
    }

    /**
     * Initializes the editor.<p>
     *
     * @param context the editor context
     * @param contentDefinition the content definition
     * @param formParent the inline form parent panel, used for inline editing only
     * @param inline <code>true</code> to render the editor for inline editing
     */
    void initEditor(
        CmsEditorContext context,
        CmsContentDefinition contentDefinition,
        I_CmsInlineFormParent formParent,
        boolean inline) {

        m_context = context;
        m_locale = contentDefinition.getLocale();
        m_entityId = contentDefinition.getEntityId();
        m_deleteOnCancel = contentDefinition.isDeleteOnCancel();
        m_autoUnlock = contentDefinition.isAutoUnlock();
        m_isDirectEdit = contentDefinition.isDirectEdit();

        initClosingHandler();
        setContentDefinition(contentDefinition);
        initToolbar();
        if (inline && (formParent != null)) {
            initEditOverlay(formParent.getElement());
            addOverlayClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    exitWithSaving();
                }
            });
            m_hideHelpBubblesButton.setVisible(false);
            setNativeResourceInfo(m_sitePath, m_locale);
            initEntityObserver();
            if (m_definitions.get(m_locale).hasEditorChangeHandlers()) {
                initEditorChangeHandlers(m_definitions.get(m_locale).getEditorChangeScopes());
            }
            renderInlineEntity(m_entityId, formParent);
        } else {
            initFormPanel();
            renderFormContent();
        }
        if (contentDefinition.isPerformedAutocorrection()) {
            CmsNotification.get().send(
                CmsNotification.Type.NORMAL,
                Messages.get().key(Messages.GUI_WARN_INVALID_XML_STRUCTURE_0));
            setChanged();
        }
    }

    /**
     * Initializes the editor change handler.<p>
     * 
     * @param changeScopes the scopes to watch for changes
     */
    void initEditorChangeHandlers(Collection<String> changeScopes) {

        if (m_editorChangeHandler != null) {
            m_editorChangeHandler.clear();
        }
        m_editorChangeHandler = new EditorChangeHandler(getEntity(), changeScopes);
    }

    /**
     * Initializes the entity observer.<p>
     */
    void initEntityObserver() {

        if (m_entityObserver != null) {
            m_entityObserver.clear();
        }
        m_entityObserver = new CmsEntityObserver(getCurrentEntity());
        exportObserver();
    }

    /**
     * Opens the form based editor.<p>
     */
    void initFormPanel() {

        removeEditOverlays();
        m_openFormButton.setVisible(false);
        m_saveButton.setVisible(true);
        m_hideHelpBubblesButton.setVisible(true);
        m_undoButton.setVisible(true);
        m_redoButton.setVisible(true);
        m_basePanel = new FlowPanel();
        m_basePanel.addStyleName(I_CmsLayoutBundle.INSTANCE.editorCss().basePanel());
        // insert base panel before the tool bar to keep the tool bar visible
        RootPanel.get().add(m_basePanel);
        if (m_isStandAlone) {
            RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.editorCss().standAloneEditor());
        } else {
            RootPanel.getBodyElement().getParentElement().getStyle().setOverflow(Overflow.HIDDEN);
        }
    }

    /**
     * Opens the copy locale dialog.<p>
     */
    void openCopyLocaleDialog() {

        CmsCopyLocaleDialog dialog = new CmsCopyLocaleDialog(
            m_availableLocales,
            m_contentLocales,
            m_locale,
            m_definitions.get(m_locale).hasSynchronizedElements(),
            this);
        dialog.center();
    }

    /**
     * Opens the model file select dialog.<p>
     *
     * @param context the editor context
     * @param definition the content definition
     */
    void openModelSelectDialog(final CmsEditorContext context, final CmsContentDefinition definition) {

        I_CmsModelSelectHandler handler = new I_CmsModelSelectHandler() {

            public void onModelSelect(CmsUUID modelStructureId) {

                if (modelStructureId == null) {
                    modelStructureId = CmsUUID.getNullUUID();
                }
                openFormEditor(
                    context,
                    definition.getLocale(),
                    definition.getReferenceResourceId().toString(),
                    definition.getNewLink(),
                    modelStructureId,
                    null,
                    null,
                    m_onClose);
            }
        };
        String title = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_MODEL_SELECT_TITLE_0);
        String message = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_MODEL_SELECT_MESSAGE_0);
        CmsModelSelectDialog dialog = new CmsModelSelectDialog(handler, definition.getModelInfos(), title, message);
        dialog.center();
    }

    /**
     * Renders the form content.<p>
     */
    void renderFormContent() {

        initLocaleSelect();
        setNativeResourceInfo(m_sitePath, m_locale);
        CmsInfoHeader header = new CmsInfoHeader(
            m_title,
            null,
            m_sitePath,
            m_locale,
            CmsIconUtil.getResourceIconClasses(m_resourceTypeName, m_sitePath, false));
        m_basePanel.add(header);
        SimplePanel content = new SimplePanel();
        content.setStyleName(org.opencms.acacia.client.css.I_CmsLayoutBundle.INSTANCE.form().formParent());
        m_basePanel.add(content);
        initEntityObserver();
        if (m_definitions.get(m_locale).hasEditorChangeHandlers()) {
            initEditorChangeHandlers(m_definitions.get(m_locale).getEditorChangeScopes());
        }
        renderEntityForm(m_entityId, m_tabInfos, content, m_basePanel.getElement());
    }

    /**
     * Saves the content and closes the editor.<p>
     */
    void save() {

        saveAndDeleteEntities(false, new Command() {

            public void execute() {

                setSaved();
                setUnchanged();
            }
        });
    }

    /**
     * Saves the content and closes the editor.<p>
     */
    void saveAndExit() {

        boolean unlock = shouldUnlockAutomatically();
        // store the scroll position
        final int scrollTop = RootPanel.getBodyElement().getOwnerDocument().getScrollTop();
        saveAndDeleteEntities(unlock, new Command() {

            public void execute() {

                setSaved();
                if (m_onClose != null) {
                    m_onClose.execute();
                }
                clearEditor();
                // restore the scroll position
                RootPanel.getBodyElement().getOwnerDocument().setScrollTop(scrollTop);
            }
        });
    }

    /**
     * Sets the has changed flag and enables the save button.<p>
     */
    void setChanged() {

        enableSave();
        m_changedEntityIds.add(m_entityId);
        m_deletedEntities.remove(m_entityId);
        updateOverlayPosition();
    }

    /**
     * Sets the content definition.<p>
     *
     * @param definition the content definition
     */
    void setContentDefinition(CmsContentDefinition definition) {

        if (m_availableLocales.isEmpty()) {
            // only set the locales when initially setting the content definition
            m_availableLocales.putAll(definition.getAvailableLocales());
            m_contentLocales.addAll(definition.getContentLocales());
        } else {
            m_contentLocales.add(definition.getLocale());
        }
        m_title = definition.getTitle();
        m_sitePath = definition.getSitePath();
        m_resourceTypeName = definition.getResourceType();
        m_registeredEntities.add(definition.getEntityId());
        m_tabInfos = definition.getTabInfos();
        addContentDefinition(definition);
        CmsDefaultWidgetService service = (CmsDefaultWidgetService)getWidgetService();
        service.addConfigurations(definition.getConfigurations());
        service.setSyncValues(definition.getSyncValues());
        service.setSkipPaths(definition.getSkipPaths());
        addEntityChangeHandler(definition.getEntityId(), new ValueChangeHandler<CmsEntity>() {

            public void onValueChange(ValueChangeEvent<CmsEntity> event) {

                setChanged();
            }
        });
    }

    /**
     * Removes the delete on cancel flag for new resources.<p>
     */
    void setSaved() {

        m_deleteOnCancel = false;
    }

    /**
     * Call after save.<p>
     */
    void setUnchanged() {

        m_changedEntityIds.clear();
        m_deletedEntities.clear();
        disableSave(Messages.get().key(Messages.GUI_TOOLBAR_NOTHING_CHANGED_0));
    }

    /**
     * Enables and disabler the undo redo buttons according to the state.<p>
     *
     * @param state the undo redo state
     */
    void setUndoRedoState(UndoRedoState state) {

        if (state.hasUndo()) {
            m_undoButton.enable();
        } else {
            m_undoButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_UNDO_DISABLED_0));
        }
        if (state.hasRedo()) {
            m_redoButton.enable();
        } else {
            m_redoButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_REDO_DISABLED_0));
        }
    }

    /**
     * Returns true if the edited resource should be unlocked automatically after pressing Save/Exit.<p>
     *
     * @return true if the edited resource should be unlocked automatically
     */
    boolean shouldUnlockAutomatically() {

        if (m_isStandAlone) {
            if (m_isDirectEdit) {
                // Classic direct edit case - always unlock
                return true;
            } else {
                // Workplace case - determined by configuration
                return m_autoUnlock;
            }
        }
        // Container page case - always unlock
        return true;
    }

    /**
     * Shows the validation error dialog.<p>
     *
     * @param validationResult the validation result
     */
    void showValidationErrorDialog(CmsValidationResult validationResult) {

        if (validationResult.getErrors().keySet().contains(m_entityId)) {
            getValidationHandler().displayValidation(m_entityId, validationResult);
        }
        String errorLocales = "";
        for (String entityId : validationResult.getErrors().keySet()) {
            String locale = CmsContentDefinition.getLocaleFromId(entityId);
            errorLocales += m_availableLocales.get(locale) + ", ";
        }
        // remove trailing ','
        errorLocales = errorLocales.substring(0, errorLocales.length() - 2);
        CmsErrorDialog dialog = new CmsErrorDialog(
            Messages.get().key(Messages.GUI_VALIDATION_ERROR_1, errorLocales),
            null);
        dialog.center();
    }

    /**
     * Switches to the selected locale. Will save changes first.<p>
     *
     * @param locale the locale to switch to
     */
    void switchLocale(final String locale) {

        if (locale.equals(m_locale)) {
            return;
        }
        m_locale = locale;
        m_basePanel.clear();
        destroyForm(false);
        final CmsEntity entity = m_entityBackend.getEntity(m_entityId);
        m_entityId = getIdForLocale(locale);
        // if the content does not contain the requested locale yet, a new node will be created
        final boolean addedNewLocale = !m_contentLocales.contains(locale);
        if (m_registeredEntities.contains(m_entityId)) {
            unregistereEntity(m_entityId);
        }
        if (addedNewLocale) {
            loadNewDefinition(m_entityId, entity, new I_CmsSimpleCallback<CmsContentDefinition>() {

                public void execute(final CmsContentDefinition contentDefinition) {

                    registerContentDefinition(contentDefinition);
                    final CmsNotificationMessage message = CmsNotification.get().sendBusy(
                        CmsNotification.Type.NORMAL,
                        org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_LOADING_0));
                    WidgetRegistry.getInstance().registerExternalWidgets(
                        contentDefinition.getExternalWidgetConfigurations(),
                        new Command() {

                            public void execute() {

                                setContentDefinition(contentDefinition);
                                renderFormContent();
                                setChanged();
                                CmsNotification.get().removeMessage(message);
                            }
                        });
                }
            });
        } else {
            loadDefinition(m_entityId, entity, new I_CmsSimpleCallback<CmsContentDefinition>() {

                public void execute(CmsContentDefinition contentDefinition) {

                    setContentDefinition(contentDefinition);
                    renderFormContent();
                }
            });
        }
    }

    /**
     * Synchronizes the locale independent fields to the other locales.<p>
     */
    void synchronizeCurrentLocale() {

        m_basePanel.clear();
        destroyForm(false);
        CmsEntity entity = m_entityBackend.getEntity(m_entityId);
        m_entityId = getIdForLocale(m_locale);
        ((CmsDefaultWidgetService)getWidgetService()).setSkipPaths(Collections.<String> emptyList());
        loadDefinition(m_entityId, entity, new I_CmsSimpleCallback<CmsContentDefinition>() {

            public void execute(CmsContentDefinition contentDefinition) {

                setContentDefinition(contentDefinition);
                renderFormContent();
                setChanged();
            }
        });
    }

    /**
     * Unlocks the edited resource.<p>
     */
    void unlockResource() {

        if (!shouldUnlockAutomatically()) {
            return;
        }
        if (m_entityId != null) {
            final CmsUUID structureId = CmsContentDefinition.entityIdToUuid(m_entityId);
            if (m_deleteOnCancel) {
                CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                    @Override
                    public void execute() {

                        CmsCoreProvider.getVfsService().syncDeleteResource(structureId, this);
                    }

                    @Override
                    protected void onResponse(Void result) {

                        // nothing to do
                    }
                };
                action.executeSync();
            } else {
                CmsCoreProvider.get().unlock(structureId);
            }
        }
    }

    /**
     * Updates the editor values.<p>
     * 
     * @param previous the previous entity state
     * @param updated the updated entity state
     */
    void updateEditorValues(CmsEntity previous, CmsEntity updated) {

        if (updated.getId().equals(m_entityId)) {
            // only apply the changes to the same locale entity
            updateEditorValues(previous, updated, getEntity(), Collections.<String> emptyList());
        }
    }

    /**
     * Adds the change listener to the observer.<p>
     *
     * @param changeListener the change listener
     * @param changeScope the change scope
     */
    private void addChangeListener(JavaScriptObject changeListener, String changeScope) {

        try {
            System.out.println("Adding native listener for scope " + changeScope);
            m_entityObserver.addEntityChangeListener(new CmsEntityChangeListenerWrapper(changeListener), changeScope);
        } catch (Exception e) {

            CmsDebugLog.getInstance().printLine("Exception occured during listener registration" + e.getMessage());
        }
    }

    /**
     * Changes a simple type entity value.<p>
     * 
     * @param attributeName the attribute name
     * @param index the value index
     * @param value the value
     * @param parentPathElements the parent path elements
     */
    private void changeSimpleValue(String attributeName, int index, String value, List<String> parentPathElements) {

        CmsAttributeHandler handler = getAttributeHandler(attributeName, parentPathElements);
        handler.changeValue(value, index);
    }

    /**
     * Closes the editor.<p>
     */
    private native void closeEditorWidow() /*-{
                                           if ($wnd.top.cms_ade_closeEditorDialog) {
                                           $wnd.top.cms_ade_closeEditorDialog();
                                           } else {
                                           var backlink = $wnd[@org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService::PARAM_BACKLINK];
                                           if (backlink) {
                                           $wnd.top.location.href = backlink;
                                           }
                                           }
                                           }-*/;

    /**
     * Creates a push button for the edit tool-bar.<p>
     *
     * @param title the button title
     * @param imageClass the image class
     *
     * @return the button
     */
    private CmsPushButton createButton(String title, String imageClass) {

        CmsPushButton result = new CmsPushButton();
        result.setTitle(title);
        result.setImageClass(imageClass);
        result.setButtonStyle(ButtonStyle.IMAGE, null);
        result.setSize(Size.big);
        return result;
    }

    /**
     * Enables the save buttons.<p>
     */
    private void enableSave() {

        m_saveButton.enable();
        m_saveExitButton.enable();
    }

    /**
     * Exports the add entity change listener method.<p>
     */
    private native void exportObserver()/*-{
                                        var self = this;
                                        $wnd[@org.opencms.ade.contenteditor.client.CmsContentEditor::ADD_CHANGE_LISTENER_METHOD] = function(
                                        listener, scope) {
                                        var wrapper = {
                                        onChange : listener.onChange
                                        }
                                        self.@org.opencms.ade.contenteditor.client.CmsContentEditor::addChangeListener(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(wrapper, scope);
                                        }
                                        $wnd[@org.opencms.ade.contenteditor.client.CmsContentEditor::GET_CURRENT_ENTITY_METHOD] = function() {
                                        return new $wnd.acacia.CmsEntityWrapper(self.@org.opencms.ade.contenteditor.client.CmsContentEditor::getCurrentEntity()());
                                        }
                                        }-*/;

    /**
     * Returns the attribute handler for the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * @param parentPathElements the parent path elements
     * 
     * @return the attribute handler
     */
    private CmsAttributeHandler getAttributeHandler(String attributeName, List<String> parentPathElements) {

        List<String> childPathElements = new ArrayList<String>(parentPathElements);
        childPathElements.add(attributeName);
        CmsAttributeHandler handler = getRootAttributeHandler().getHandlerByPath(
            childPathElements.toArray(new String[childPathElements.size()]));
        return handler;
    }

    /**
     * Returns the entity id for the given locale.<p>
     *
     * @param locale the locale
     *
     * @return the entity id
     */
    private String getIdForLocale(String locale) {

        return CmsContentDefinition.uuidToEntityId(CmsContentDefinition.entityIdToUuid(m_entityId), locale);
    }

    /**
     * Initializes the window closing handler to ensure the resource will be unlocked when leaving the editor.<p>
     */
    private void initClosingHandler() {

        m_closingHandlerRegistration = Window.addWindowClosingHandler(new ClosingHandler() {

            /**
             * @see com.google.gwt.user.client.Window.ClosingHandler#onWindowClosing(com.google.gwt.user.client.Window.ClosingEvent)
             */
            public void onWindowClosing(ClosingEvent event) {

                unlockResource();
            }
        });
    }

    /**
     * Initializes the locale selector.<p>
     */
    private void initLocaleSelect() {

        if (m_availableLocales.size() < 2) {
            return;
        }
        if (m_localeLabel == null) {
            m_localeLabel = new CmsLabel();
            m_localeLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock());
            m_localeLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textBig());
            m_localeLabel.setText(Messages.get().key(Messages.GUI_TOOLBAR_LANGUAGE_0));
            m_toolbar.addLeft(m_localeLabel);
        }
        Map<String, String> selectOptions = new HashMap<String, String>();
        for (Entry<String, String> localeEntry : m_availableLocales.entrySet()) {
            if (m_contentLocales.contains(localeEntry.getKey())) {
                selectOptions.put(localeEntry.getKey(), localeEntry.getValue());
            } else {
                selectOptions.put(localeEntry.getKey(), localeEntry.getValue() + " [-]");
            }
        }
        if (m_localeSelect == null) {
            m_localeSelect = new CmsSelectBox(selectOptions);
            m_toolbar.addLeft(m_localeSelect);
            m_localeSelect.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().inlineBlock());
            m_localeSelect.getElement().getStyle().setWidth(100, Unit.PX);
            m_localeSelect.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
            m_localeSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

                public void onValueChange(ValueChangeEvent<String> event) {

                    switchLocale(event.getValue());
                }
            });
        } else {
            m_localeSelect.setItems(selectOptions);
        }
        m_localeSelect.setFormValueAsString(m_locale);
        if (m_deleteLocaleButton == null) {
            m_deleteLocaleButton = createButton(
                Messages.get().key(Messages.GUI_TOOLBAR_DELETE_LOCALE_0),
                I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarDeleteLocale());
            m_deleteLocaleButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    confirmDeleteLocale();
                }
            });
            m_toolbar.addLeft(m_deleteLocaleButton);
        }
        if (m_contentLocales.size() > 1) {
            m_deleteLocaleButton.enable();
        } else {
            m_deleteLocaleButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_CANT_DELETE_LAST_LOCALE_0));
        }
        if (m_copyLocaleButton == null) {
            m_copyLocaleButton = createButton(
                I_CmsButton.ButtonData.COPY_LOCALE.getTitle(),
                I_CmsButton.ButtonData.COPY_LOCALE.getIconClass());
            m_copyLocaleButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    openCopyLocaleDialog();
                }
            });
            m_toolbar.addLeft(m_copyLocaleButton);
        }
    }

    /**
     * Generates the button bar displayed beneath the editable fields.<p>
     */
    private void initToolbar() {

        m_toolbar = new CmsToolbar();
        m_publishButton = createButton(
            org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_TOOLBAR_PUBLISH_0),
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarPublish());
        m_toolbar.addLeft(m_publishButton);
        m_publishButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                boolean unlock = shouldUnlockAutomatically();

                saveAndDeleteEntities(unlock, new Command() {

                    public void execute() {

                        setSaved();
                        HashMap<String, String> params = new HashMap<String, String>(
                            getContext().getPublishParameters());
                        CmsUUID structureId = CmsContentDefinition.entityIdToUuid(getEntityId());
                        params.put(CmsPublishOptions.PARAM_CONTENT, "" + structureId);
                        params.put(CmsPublishOptions.PARAM_START_WITH_CURRENT_PAGE, "");
                        CmsPublishDialog.showPublishDialog(params, new CloseHandler<PopupPanel>() {

                            public void onClose(CloseEvent<PopupPanel> closeEvent) {

                                if (m_onClose != null) {
                                    m_onClose.execute();
                                }
                                clearEditor();
                            }
                        }, new Runnable() {

                            public void run() {

                                // ignore 
                            }

                        }, null);

                    }
                });

            }
        });
        m_saveExitButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_SAVE_AND_EXIT_0),
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarSaveExit());
        m_saveExitButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                deferSaveAndExit();
            }
        });
        m_toolbar.addLeft(m_saveExitButton);
        m_saveButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_SAVE_0),
            I_CmsButton.ButtonData.SAVE.getIconClass());
        m_saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                deferSave();
            }
        });
        m_saveButton.setVisible(false);
        m_toolbar.addLeft(m_saveButton);
        disableSave(Messages.get().key(Messages.GUI_TOOLBAR_NOTHING_CHANGED_0));
        m_undoButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_UNDO_0),
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarUndo());
        m_undoButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (CmsUndoRedoHandler.getInstance().isIntitalized()) {
                    CmsUndoRedoHandler.getInstance().undo();
                }
            }
        });
        m_undoButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_UNDO_DISABLED_0));
        m_undoButton.getElement().getStyle().setMarginLeft(10, Unit.PX);
        m_undoButton.setVisible(false);
        m_toolbar.addLeft(m_undoButton);
        m_redoButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_REDO_0),
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().toolbarRedo());
        m_redoButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (CmsUndoRedoHandler.getInstance().isIntitalized()) {
                    CmsUndoRedoHandler.getInstance().redo();
                }
            }
        });
        m_redoButton.disable(Messages.get().key(Messages.GUI_TOOLBAR_REDO_DISABLED_0));
        m_redoButton.getElement().getStyle().setMarginRight(20, Unit.PX);
        m_redoButton.setVisible(false);
        m_toolbar.addLeft(m_redoButton);

        m_undoRedoHandlerRegistration = CmsUndoRedoHandler.getInstance().addValueChangeHandler(
            new ValueChangeHandler<CmsUndoRedoHandler.UndoRedoState>() {

                public void onValueChange(ValueChangeEvent<UndoRedoState> event) {

                    setUndoRedoState(event.getValue());
                }
            });
        m_openFormButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_OPEN_FORM_0),
            I_CmsButton.ButtonData.EDIT.getIconClass());
        m_openFormButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                initFormPanel();
                renderFormContent();
            }
        });
        m_toolbar.addLeft(m_openFormButton);

        m_hideHelpBubblesButton = new CmsToggleButton();

        m_hideHelpBubblesButton.setImageClass(I_CmsButton.ButtonData.TOGGLE_HELP.getIconClass());
        m_hideHelpBubblesButton.setButtonStyle(ButtonStyle.IMAGE, null);
        m_hideHelpBubblesButton.setSize(Size.big);
        m_hideHelpBubblesButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                CmsToggleButton button = (CmsToggleButton)event.getSource();
                hideHelpBubbles(!button.isDown());
            }
        });
        m_hideHelpBubblesButton.setDown(CmsCoreProvider.get().isShowEditorHelp());
        CmsValueFocusHandler.getInstance().hideHelpBubbles(RootPanel.get(), !CmsCoreProvider.get().isShowEditorHelp());
        if (!CmsCoreProvider.get().isShowEditorHelp()) {
            m_hideHelpBubblesButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_HELP_BUBBLES_HIDDEN_0));
        } else {
            m_hideHelpBubblesButton.setTitle(Messages.get().key(Messages.GUI_TOOLBAR_HELP_BUBBLES_SHOWN_0));
        }
        m_toolbar.addRight(m_hideHelpBubblesButton);

        m_cancelButton = createButton(
            Messages.get().key(Messages.GUI_TOOLBAR_RESET_0),
            I_CmsButton.ButtonData.RESET.getIconClass());
        m_cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                confirmCancel();
            }
        });
        m_toolbar.addRight(m_cancelButton);
        RootPanel.get().add(m_toolbar);
    }

    /**
     * Sets the resource info to native window context variables.<p>
     *
     * @param sitePath the site path
     * @param locale the content locale
     */
    private native void setNativeResourceInfo(String sitePath, String locale)/*-{
                                                                             $wnd._editResource = sitePath;
                                                                             $wnd._editLanguage = locale;
                                                                             }-*/;

    /**
     * Shows the locked resource error message.<p>
     */
    private void showLockedResourceMessage() {

        CmsErrorDialog dialog = new CmsErrorDialog(Messages.get().key(
            Messages.ERR_RESOURCE_ALREADY_LOCKED_BY_OTHER_USER_0), null);
        dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                cancelEdit();
            }
        });
        dialog.center();
    }

    /**
     * Updates the editor values according to the given entity.<p>
     * 
     * @param previous the previous entity state
     * @param updated the updated entity state
     * @param target the target entity
     * @param parentPathElements the parent path elements
     */
    private void updateEditorValues(
        CmsEntity previous,
        CmsEntity updated,
        CmsEntity target,
        List<String> parentPathElements) {

        for (String attributeName : m_entityBackend.getType(target.getTypeName()).getAttributeNames()) {
            CmsAttributeHandler handler = getAttributeHandler(attributeName, parentPathElements);
            if (previous.hasAttribute(attributeName)
                && updated.hasAttribute(attributeName)
                && target.hasAttribute(attributeName)) {
                CmsEntityAttribute updatedAttribute = updated.getAttribute(attributeName);
                CmsEntityAttribute previousAttribute = previous.getAttribute(attributeName);
                CmsEntityAttribute targetAttribute = target.getAttribute(attributeName);
                if (updatedAttribute.isSimpleValue()) {
                    if ((updatedAttribute.getValueCount() == previousAttribute.getValueCount())
                        && (updatedAttribute.getValueCount() == targetAttribute.getValueCount())) {
                        for (int i = 0; i < updatedAttribute.getValueCount(); i++) {
                            if (!updatedAttribute.getSimpleValues().get(i).equals(
                                previousAttribute.getSimpleValues().get(i))
                                && previousAttribute.getSimpleValues().get(i).equals(
                                    targetAttribute.getSimpleValues().get(i))) {

                                changeSimpleValue(
                                    attributeName,
                                    i,
                                    updatedAttribute.getSimpleValues().get(i),
                                    parentPathElements);
                            }
                        }
                    } else {
                        if (targetAttribute.getValueCount() == previousAttribute.getValueCount()) {
                            // only act, if the value count has not been altered while executing the server request 
                            if (updatedAttribute.getValueCount() > previousAttribute.getValueCount()) {
                                // new values have been added
                                for (int i = 0; i < updatedAttribute.getValueCount(); i++) {
                                    if (i >= previousAttribute.getSimpleValues().size()) {
                                        handler.addNewAttributeValue(updatedAttribute.getSimpleValues().get(i));
                                    } else if (!updatedAttribute.getSimpleValues().get(i).equals(
                                        previousAttribute.getSimpleValues().get(i))
                                        && previousAttribute.getSimpleValues().get(i).equals(
                                            targetAttribute.getSimpleValues().get(i))) {
                                        changeSimpleValue(
                                            attributeName,
                                            i,
                                            updatedAttribute.getSimpleValues().get(i),
                                            parentPathElements);
                                    }
                                }
                            } else {
                                // values have been removed
                                for (int i = updatedAttribute.getValueCount() - 1; i >= 0; i--) {
                                    if (i >= updatedAttribute.getSimpleValues().size()) {
                                        handler.removeAttributeValue(i);
                                    } else if (!updatedAttribute.getSimpleValues().get(i).equals(
                                        previousAttribute.getSimpleValues().get(i))
                                        && previousAttribute.getSimpleValues().get(i).equals(
                                            targetAttribute.getSimpleValues().get(i))) {
                                        changeSimpleValue(
                                            attributeName,
                                            i,
                                            updatedAttribute.getSimpleValues().get(i),
                                            parentPathElements);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (targetAttribute.getValueCount() == previousAttribute.getValueCount()) {
                        // only act, if the value count has not been altered while executing the server request 
                        if (updatedAttribute.getValueCount() > previousAttribute.getValueCount()) {
                            // new values have been added
                            for (int i = 0; i < updatedAttribute.getValueCount(); i++) {
                                if (i >= previousAttribute.getSimpleValues().size()) {
                                    handler.addNewAttributeValue(m_entityBackend.registerEntity(
                                        updatedAttribute.getComplexValues().get(i),
                                        true));
                                } else {
                                    List<String> childPathElements = new ArrayList<String>(parentPathElements);
                                    childPathElements.add(attributeName + "[" + i + "]");
                                    updateEditorValues(
                                        previousAttribute.getComplexValues().get(i),
                                        updatedAttribute.getComplexValues().get(i),
                                        targetAttribute.getComplexValues().get(i),
                                        childPathElements);
                                }
                            }
                        } else {
                            // values have been removed
                            for (int i = updatedAttribute.getValueCount() - 1; i >= 0; i--) {
                                if (i >= updatedAttribute.getValueCount()) {

                                    handler.removeAttributeValue(i);
                                } else {
                                    List<String> childPathElements = new ArrayList<String>(parentPathElements);
                                    childPathElements.add(attributeName + "[" + i + "]");
                                    updateEditorValues(
                                        previousAttribute.getComplexValues().get(i),
                                        updatedAttribute.getComplexValues().get(i),
                                        targetAttribute.getComplexValues().get(i),
                                        childPathElements);
                                }
                            }
                        }
                    }
                }
            } else if (previous.hasAttribute(attributeName) && target.hasAttribute(attributeName)) {
                for (int i = target.getAttribute(attributeName).getValueCount() - 1; i >= 0; i--) {
                    handler.removeAttributeValue(i);
                }

            } else if (!previous.hasAttribute(attributeName)
                && !target.hasAttribute(attributeName)
                && updated.hasAttribute(attributeName)) {
                CmsEntityAttribute updatedAttribute = updated.getAttribute(attributeName);
                for (int i = 0; i < updatedAttribute.getValueCount(); i++) {
                    if (updatedAttribute.isSimpleValue()) {
                        handler.addNewAttributeValue(updatedAttribute.getSimpleValues().get(i));
                    } else {
                        handler.addNewAttributeValue(m_entityBackend.registerEntity(
                            updatedAttribute.getComplexValues().get(i),
                            true));
                    }
                }
            }
        }
    }
}

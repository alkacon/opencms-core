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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.acacia.client.CmsEditorBase;
import org.opencms.acacia.client.I_CmsInlineFormParent;
import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsElementLockInfo;
import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.gwt.client.I_CmsElementToolbarContext;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Tag;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.MutationObserver;
import elemental2.dom.MutationObserver.MutationObserverCallbackFn;
import elemental2.dom.MutationObserverInit;
import elemental2.dom.MutationRecord;
import jsinterop.base.Js;

/**
 * Content element within a container-page.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerPageElementPanel extends AbsolutePanel
implements I_CmsDraggable, HasClickHandlers, I_CmsInlineFormParent {

    /**
     * Parses CSS classess of the form 'oc-point-TY_LX', where X and Y are strings
     * of decimal digits possibly preceded by a minus sign.<p>
     *
     * The numeric values of Y and X will be available after a successful parse using the
     * methods getOffsetTop() and getOffsetLeft().
     *
     * This is used to offer the formatter developer some control over the edit point
     * positioning.
     */
    public static class PointPositioningParser {

        /** Regular expression used to match the special CSS classes. */
        private static final RegExp REGEX = RegExp.compile("^oc-point-T(-?[0-9]+)_L(-?[0-9]+)$");

        /** The left offset. */
        private int m_left;

        /** The top offset. */
        private int m_top;

        /**
         * Gets the left offset after a CSS class has successfully been parsed.<p>
         *
         * @return the left offset
         */
        int getOffsetLeft() {

            return m_left;
        }

        /**
         * Gets the top offset after a CSS class has successfully been parsed.<p>
         *
         * @return the top offset
         */
        int getOffsetTop() {

            return m_top;
        }

        /**
         * Tries to parse a point positioning instruction from an element's class attribute
         * and returns true when successful.<p>
         *
         * @param cssClass the value of a class attribute
         *
         * @return true if a positioning instruction was found
         */
        boolean tryParse(String cssClass) {

            m_left = 0;
            m_top = 0;
            if (cssClass == null) {
                cssClass = "";
            }
            for (String token : cssClass.trim().split(" +")) {
                if (tryParseSingleClass(token)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Parses a single token from a class attribute.<p>
         *
         * @param token the token
         * @return true if the token was a point positioning instruction
         */
        private boolean tryParseSingleClass(String token) {

            MatchResult result = REGEX.exec(token);
            if (result != null) {
                m_top = Integer.parseInt(result.getGroup(1));
                m_left = Integer.parseInt(result.getGroup(2));
                return true;
            } else {
                if (token.startsWith("oc-point")) {
                    CmsDebugLog.consoleLog("Malformed oc-point class: " + token);
                }
                return false;
            }
        }

    }

    /** Property used to mark an element as belonging to this widget. */
    public static final String PROP_ELEMENT_OBJECT_ID = "element_object_id";

    /** The is model group property key. */
    public static final String PROP_IS_MODEL_GROUP = "is_model_group";

    /** The former copy model property. */
    public static final String PROP_WAS_MODEL_GROUP = "was_model_group";

    /** Highlighting border for this element. */
    protected CmsHighlightingBorder m_highlighting;

    /** A flag which indicates whether the height has already been checked. */
    private boolean m_checkedHeight;

    /** Flag indicating the the editables are currently being checked. */
    private boolean m_checkingEditables;

    /** The elements client id. */
    private String m_clientId;

    /** The 'create new' flag. */
    private boolean m_createNew;

    /**
     * Flag which indicates whether the new editor is disabled for this element.<p>
     */
    private boolean m_disableNewEditor;

    /** The direct edit bar instances. */
    private Map<Element, CmsListCollectorEditor> m_editables;

    /** The editor click handler registration. */
    private HandlerRegistration m_editorClickHandlerRegistration;

    /** The option bar, holding optional function buttons. */
    private CmsElementOptionBar m_elementOptionBar;

    /** The element element view. */
    private CmsUUID m_elementView;

    /** The overlay for expired elements. */
    private Element m_expiredOverlay;

    /** Indicates an edit handler is configured for the element resource type. */
    private boolean m_hasEditHandler;

    /** Indicates whether this element has settings to edit. */
    private boolean m_hasSettings;

    /** The resource type icon CSS classes. */
    private String m_iconClasses;

    /** The inheritance info for this element. */
    private CmsInheritanceInfo m_inheritanceInfo;

    /** The lock information. */
    private CmsElementLockInfo m_lockInfo;

    /** The model group id. */
    private CmsUUID m_modelGroupId;

    /** The is new element type. */
    private String m_newType;

    /** The registered mutation observer. */
    private MutationObserver m_mutationObserver;

    /** The no edit reason, if empty editing is allowed. */
    private String m_noEditReason;

    /** A random id, which is also stored as a property on the HTML element for this widget. */
    private String m_objectId;

    /** The parent drop target. */
    private I_CmsDropContainer m_parent;

    /** Parser for point positioning isntructions. */
    private PointPositioningParser m_positioningInstructionParser = new PointPositioningParser();

    /** The drag and drop parent div. */
    private Element m_provisionalParent;

    /** Flag indicating if the element resource is currently released and not expired. */
    private boolean m_releasedAndNotExpired;

    /** The resource type. */
    private String m_resourceType;

    /** True if this element is marked as 'reused'. */
    private boolean m_reused;

    /** The element resource site-path. */
    private String m_sitePath;
    /** The sub title. */
    private String m_subTitle;

    /** The resource title. */
    private String m_title;

    /**
     * Indicates if the current user has view permissions on the element resource.
     * Without view permissions, the element can neither be edited, nor moved.
     **/
    private boolean m_viewPermission;

    /** The former copy model status. */
    private boolean m_wasModelGroup;

    /**
     * Indicates if the current user has write permissions on the element resource.
     * Without write permissions, the element can not be edited.
     **/
    private boolean m_writePermission;

    /**
     * Constructor.<p>
     *
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     * @param lockInfo the lock information
     * @param title the resource title
     * @param subTitle the sub title
     * @param resourceType the resource type
     * @param hasSettings should be true if the element has settings which can be edited
     * @param hasViewPermission indicates if the current user has view permissions on the element resource
     * @param hasWritePermission indicates if the current user has write permissions on the element resource
     * @param releasedAndNotExpired <code>true</code> if the element resource is currently released and not expired
     * @param disableNewEditor flag to disable the new editor for this element
     * @param hasEditHandler indicates an edit handler is configured for the element resource type
     * @param modelGroupId the model group id
     * @param wasModelGroup in case of a former copy model group
     * @param elementView the element view of the element
     * @param iconClasses the resource type icon CSS classes
     * @param isReused true if this element is marked as reused
     */
    public CmsContainerPageElementPanel(
        Element element,
        I_CmsDropContainer parent,
        String clientId,
        String sitePath,
        String noEditReason,
        CmsElementLockInfo lockInfo,
        String title,
        String subTitle,
        String resourceType,
        boolean hasSettings,
        boolean hasViewPermission,
        boolean hasWritePermission,
        boolean releasedAndNotExpired,
        boolean disableNewEditor,
        boolean hasEditHandler,
        CmsUUID modelGroupId,
        boolean wasModelGroup,
        CmsUUID elementView,
        String iconClasses,
        boolean isReused) {

        super(element);
        m_clientId = clientId;
        m_objectId = "" + Math.random();
        m_sitePath = sitePath;
        m_title = title;
        m_subTitle = subTitle;
        m_resourceType = resourceType;
        m_noEditReason = noEditReason;
        m_lockInfo = lockInfo;
        m_hasSettings = hasSettings;
        m_parent = parent;
        m_disableNewEditor = disableNewEditor;
        m_modelGroupId = modelGroupId;
        m_wasModelGroup = wasModelGroup;
        m_hasEditHandler = hasEditHandler;
        setViewPermission(hasViewPermission);
        setWritePermission(hasWritePermission);
        setReleasedAndNotExpired(releasedAndNotExpired);
        m_elementView = elementView;
        getElement().setPropertyBoolean(PROP_IS_MODEL_GROUP, modelGroupId != null);
        getElement().setPropertyBoolean(PROP_WAS_MODEL_GROUP, wasModelGroup);
        getElement().setPropertyString(PROP_ELEMENT_OBJECT_ID, m_objectId);
        m_iconClasses = iconClasses;
        m_reused = isReused;
    }

    /**
     * Checks if the element is an overlay for a container page element.<p>
     *
     * @param element the element to check
     * @return true if the element is an overlay
     */
    public static boolean isOverlay(Element element) {

        for (String overlayClass : Arrays.asList(
            I_CmsLayoutBundle.INSTANCE.containerpageCss().expiredOverlay(),
            I_CmsElementToolbarContext.ELEMENT_OPTION_BAR_CSS_CLASS)) {
            if (element.hasClassName(overlayClass)) {
                return true;
            }
        }
        return false;

    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsInlineFormParent#adoptWidget(com.google.gwt.user.client.ui.IsWidget)
     */
    public void adoptWidget(IsWidget widget) {

        getChildren().add(widget.asWidget());
        adopt(widget.asWidget());
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getCursorOffsetDelta()
     */
    public Optional<int[]> getCursorOffsetDelta() {

        return Optional.absent();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getDragHelper(I_CmsDropTarget target) {

        CmsListInfoBean info = new CmsListInfoBean(m_title, m_subTitle, null);
        info.setResourceType(m_resourceType);
        info.setBigIconClasses(m_iconClasses);
        CmsListItemWidget helperWidget = new CmsListItemWidget(info);
        helperWidget.setWidth("600px");
        helperWidget.truncate("ggg", 550);
        Element helper = helperWidget.getElement();
        Element button = DOM.createDiv();
        button.addClassName("opencms-icon");
        button.addClassName(I_CmsButton.MOVE_SMALL);
        button.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragHandle());
        helper.appendChild(button);
        helper.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        Element parentElement = getElement().getParentElement();
        int elementTop = getElement().getAbsoluteTop();
        int parentTop = parentElement.getAbsoluteTop();
        m_provisionalParent = DOM.createElement(parentElement.getTagName());
        RootPanel.getBodyElement().appendChild(m_provisionalParent);
        m_provisionalParent.addClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().clearStyles());
        m_provisionalParent.addClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().opencms());
        m_provisionalParent.getStyle().setWidth(parentElement.getOffsetWidth(), Unit.PX);
        m_provisionalParent.appendChild(helper);
        Style style = helper.getStyle();
        style.setWidth(helper.getOffsetWidth(), Unit.PX);
        // the dragging class will set position absolute
        helper.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().dragging());
        style.setTop(elementTop - parentTop, Unit.PX);
        m_provisionalParent.getStyle().setPosition(Position.ABSOLUTE);
        m_provisionalParent.getStyle().setTop(parentTop, Unit.PX);
        m_provisionalParent.getStyle().setLeft(parentElement.getAbsoluteLeft(), Unit.PX);
        m_provisionalParent.getStyle().setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexDND());

        return helper;
    }

    /**
     * Returns the option bar of this element.<p>
     *
     * @return the option bar widget
     */
    public CmsElementOptionBar getElementOptionBar() {

        return m_elementOptionBar;
    }

    /**
     * Returns the elements element view.<p>
     *
     * @return the element view
     */
    public CmsUUID getElementView() {

        return m_elementView;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getId()
     */
    public String getId() {

        return m_clientId;
    }

    /**
     * Returns the inheritance info for this element.<p>
     *
     * @return the inheritance info for this element
     */
    public CmsInheritanceInfo getInheritanceInfo() {

        return m_inheritanceInfo;
    }

    /**
     * Gets the lock information.
     *
     * @return the lock information
     */
    public CmsElementLockInfo getLockInfo() {

        return m_lockInfo != null ? m_lockInfo : new CmsElementLockInfo(null, false);
    }

    /**
     * Returns the model group id.<p>
     *
     * @return the model group id
     */
    public CmsUUID getModelGroupId() {

        return m_modelGroupId;
    }

    /**
     * Returns the new element type.
     *
     * @return the new element type
     */
    public String getNewType() {

        return m_newType;
    }

    /**
     * Returns the no edit reason.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Gets the random id identifying this widget.
     *
     * <p>The id is also stored in the element_object_id property of the DOM element for this widget.
     *
     * @return the random id identifying this widget
     */
    public String getObjectId() {

        return m_objectId;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getParentTarget()
     */
    public I_CmsDropContainer getParentTarget() {

        return m_parent;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getPlaceholder(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getPlaceholder(I_CmsDropTarget target) {

        Element placeholder = CmsDomUtil.clone(getElement());
        placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        return placeholder;
    }

    /**
     * Returns if the element resource is currently released and not expired.<p>
     *
     * @return <code>true</code> if the element resource is currently released and not expired
     */
    public boolean getReleasedAndNotExpired() {

        return m_releasedAndNotExpired;
    }

    /**
     * Returns the element resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the structure id of the element.<p>
     *
     * @return the structure id of the element
     */
    public CmsUUID getStructureId() {

        if (m_clientId == null) {
            return null;
        }
        return new CmsUUID(CmsContainerpageController.getServerId(m_clientId));
    }

    /**
     * Indicates an edit handler is configured for the element resource type.<p>
     *
     * @return indicates an edit handler is configured for the element resource type
     */
    public boolean hasEditHandler() {

        return m_hasEditHandler;
    }

    /**
     * Returns whether this element has a model group parent.<p>
     *
     * @return <code>true</code> if this element has a model group parent
     */
    public boolean hasModelGroupParent() {

        boolean result = false;
        Element parent = getElement().getParentElement();
        while (parent != null) {
            if (parent.getPropertyBoolean(PROP_IS_MODEL_GROUP)) {
                result = true;
                break;
            }
            parent = parent.getParentElement();
        }
        return result;
    }

    /**
     * In case the inner HTML contains the reload marker.<p>
     *
     * @return <code>true</code> in case the inner HTML contains the reload marker
     */
    public boolean hasReloadMarker() {

        return getElement().getInnerHTML().contains(CmsGwtConstants.FORMATTER_RELOAD_MARKER);
    }

    /**
     * Returns true if the element has settings to edit.<p>
     *
     * @return true if the element has settings to edit
     */
    public boolean hasSettings() {

        return m_hasSettings;
    }

    /**
     * Returns if the current user has view permissions for the element resource.<p>
     *
     * @return <code>true</code> if the current user has view permissions for the element resource
     */
    public boolean hasViewPermission() {

        return m_viewPermission;
    }

    /**
     * Returns if the user has write permission.<p>
     *
     * @return <code>true</code> if the user has write permission
     */
    public boolean hasWritePermission() {

        return m_writePermission;
    }

    /**
     * Hides list collector direct edit buttons, if present.<p>
     */
    public void hideEditableListButtons() {

        if (m_editables != null) {
            for (CmsListCollectorEditor editor : m_editables.values()) {
                editor.getElement().getStyle().setDisplay(Display.NONE);
            }
        }
    }

    /**
     * Puts a highlighting border around the element.<p>
     */
    public void highlightElement() {

        CmsPositionBean position = CmsPositionBean.getBoundingClientRect(getElement());
        if (m_highlighting == null) {
            m_highlighting = new CmsHighlightingBorder(
                position,
                isNew() || (CmsContainerpageController.get().getData().isModelPage() && isCreateNew())
                ? CmsHighlightingBorder.BorderColor.blue
                : CmsHighlightingBorder.BorderColor.red);
            RootPanel.get().add(m_highlighting);
        } else {
            m_highlighting.setPosition(position);
        }
    }

    /**
     * Initializes the editor click handler.<p>
     *
     * @param controller the container page controller instance
     */
    public void initInlineEditor(final CmsContainerpageController controller) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_noEditReason)
            && !m_disableNewEditor
            && CmsContentEditor.setEditable(getElement(), CmsContainerpageController.getServerId(m_clientId), true)) {
            if (m_editorClickHandlerRegistration != null) {
                m_editorClickHandlerRegistration.removeHandler();
            }
            m_editorClickHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {

                public void onPreviewNativeEvent(NativePreviewEvent event) {

                    if (event.getTypeInt() == Event.ONCLICK) {
                        // if another content is already being edited, don't start another editor
                        if (controller.isContentEditing()) {
                            return;
                        }
                        Element eventTarget = event.getNativeEvent().getEventTarget().cast();
                        // check if the event target is a child
                        if (getElement().isOrHasChild(eventTarget)) {
                            Element target = event.getNativeEvent().getEventTarget().cast();
                            // check if the target closest ancestor drag element is this element
                            Element parentContainerElement = CmsDomUtil.getAncestor(
                                target,
                                I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
                            if (parentContainerElement == getElement()) {
                                while ((target != null)
                                    && !target.getTagName().equalsIgnoreCase("a")
                                    && (target != getElement())) {
                                    if (CmsContentEditor.isEditable(target)) {
                                        final Element finalTarget = target;
                                        event.cancel();
                                        CmsContainerpageController.get().checkReuse(
                                            CmsContainerPageElementPanel.this,
                                            () -> {
                                                CmsEditorBase.markForInlineFocus(finalTarget);
                                                controller.getHandler().openEditorForElement(
                                                    CmsContainerPageElementPanel.this,
                                                    true,
                                                    isNew());
                                                removeEditorHandler();
                                            });
                                        break;
                                    } else {
                                        target = target.getParentElement();
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Checks if this element has 'createNew' status, i.e. will be copied when using this page as a model for a new container page.<p>
     *
     * @return true if this element has createNew status
     */
    public boolean isCreateNew() {

        return m_createNew;
    }

    /**
     * Returns whether the element is used as a model group.<p>
     *
     * @return <code>true</code> if the element is used as a model group
     */
    public boolean isModelGroup() {

        return m_modelGroupId != null;
    }

    /**
     * Returns if this is e newly created element.<p>
     *
     * @return <code>true</code> if the element is new
     */
    public boolean isNew() {

        return m_newType != null;
    }

    /**
     * Returns true if the new content editor is disabled for this element.<p>
     *
     * @return true if the new editor is disabled for this element
     */
    public boolean isNewEditorDisabled() {

        return m_disableNewEditor;
    }

    /**
     * Checks if this element is marked as reused.
     *
     * @return true if the element is marked as reused
     */
    public boolean isReused() {

        return m_reused;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    public void onDragCancel() {

        clearDrag();
        resetOptionbar();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onDrop(I_CmsDropTarget target) {

        clearDrag();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onStartDrag(I_CmsDropTarget target) {

        CmsDomUtil.addDisablingOverlay(getElement());
        getElement().getStyle().setOpacity(0.7);
        removeHighlighting();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#removeFromParent()
     */
    @Override
    public void removeFromParent() {

        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }
        super.removeFromParent();
    }

    /**
     * Removes the highlighting border.<p>
     */
    public void removeHighlighting() {

        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }
    }

    /**
     * Removes the inline editor.<p>
     */
    public void removeInlineEditor() {

        CmsContentEditor.setEditable(getElement(), CmsContainerpageController.getServerId(m_clientId), false);
        removeEditorHandler();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsInlineFormParent#replaceHtml(java.lang.String)
     */
    public void replaceHtml(String html) {

        // detach all children first
        while (getChildren().size() > 0) {
            getChildren().get(getChildren().size() - 1).removeFromParent();
        }
        Element tempDiv = DOM.createDiv();
        tempDiv.setInnerHTML(html);
        getElement().setInnerHTML(tempDiv.getFirstChildElement().getInnerHTML());
    }

    /**
     * Sets the 'create new' status of the element.<p>
     *
     * @param createNew the new value for the 'create new' status
     */
    public void setCreateNew(boolean createNew) {

        m_createNew = createNew;
    }

    /**
     * Sets the element option bar.<p>
     *
     * @param elementOptionBar the element option bar to set
     */
    public void setElementOptionBar(CmsElementOptionBar elementOptionBar) {

        if ((m_elementOptionBar != null) && (getWidgetIndex(m_elementOptionBar) >= 0)) {
            m_elementOptionBar.removeFromParent();
        }
        m_elementOptionBar = elementOptionBar;
        if (m_elementOptionBar != null) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
            insert(m_elementOptionBar, 0);
            updateOptionBarPosition();
        } else {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
        }
    }

    /**
     * Sets the element id.<p>
     *
     * @param id the id
     */
    public void setId(String id) {

        m_clientId = id;
    }

    /**
     * Sets the inheritance info for this element.<p>
     *
     * @param inheritanceInfo the inheritance info for this element to set
     */
    public void setInheritanceInfo(CmsInheritanceInfo inheritanceInfo) {

        m_inheritanceInfo = inheritanceInfo;
    }

    /**
     * Sets the new-type of the element.<p>
     *
     * @param newType the new-type
     */
    public void setNewType(String newType) {

        m_newType = newType;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

    /**
     * Sets if the element resource is currently released and not expired.<p>
     *
     * @param releasedAndNotExpired <code>true</code> if the element resource is currently released and not expired
     */
    public void setReleasedAndNotExpired(boolean releasedAndNotExpired) {

        m_releasedAndNotExpired = releasedAndNotExpired;
        if (m_releasedAndNotExpired) {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().expired());
            if (m_expiredOverlay != null) {
                m_expiredOverlay.removeFromParent();
                m_expiredOverlay = null;
            }

        } else {
            addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().expired());
            m_expiredOverlay = DOM.createDiv();
            m_expiredOverlay.setTitle("Expired resource");
            m_expiredOverlay.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().expiredOverlay());
            getElement().appendChild(m_expiredOverlay);
        }
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets if the current user has view permissions for the element resource.<p>
     *
     * @param viewPermission the view permission to set
     */
    public void setViewPermission(boolean viewPermission) {

        m_viewPermission = viewPermission;
    }

    /**
     * Sets the user write permission.<p>
     *
     * @param writePermission the user write permission to set
     */
    public void setWritePermission(boolean writePermission) {

        m_writePermission = writePermission;
    }

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    public void showEditableListButtons() {

        m_checkingEditables = true;
        if (m_editables == null) {
            m_editables = new HashMap<Element, CmsListCollectorEditor>();
            List<Element> editables = getEditableElements();
            if ((editables != null) && (editables.size() > 0)) {
                for (Element editable : editables) {
                    addListCollectorEditorButtons(editable);
                }
            }
        } else {

            Iterator<Entry<Element, CmsListCollectorEditor>> it = m_editables.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Element, CmsListCollectorEditor> entry = it.next();
                CmsListCollectorEditor editor = entry.getValue();
                if (!editor.isValid()) {
                    editor.removeFromParent();
                    it.remove();
                } else {
                    if (CmsDomUtil.hasDimension(editor.getElement().getParentElement())) {
                        editor.setParentHasDimensions(true);
                        editor.setPosition(
                            CmsDomUtil.getEditablePosition(entry.getValue().getMarkerTag()),
                            getElement());
                    } else {
                        editor.setParentHasDimensions(false);
                    }
                }
            }
            List<Element> editables = getEditableElements();
            if (editables.size() > m_editables.size()) {
                for (Element editable : editables) {
                    if (!m_editables.containsKey(editable)) {
                        addListCollectorEditorButtons(editable);
                    }
                }
            }
        }

        boolean editableContainer = true;
        if (m_parent instanceof CmsContainerPageContainer) {
            editableContainer = CmsContainerpageController.get().isContainerEditable(m_parent);
        }
        for (CmsListCollectorEditor editor : m_editables.values()) {
            editor.updateVisibility(editableContainer);
        }

        m_checkingEditables = false;
        initMutationObserver();
    }

    /**
     * Updates the option bar position.<p>
     */
    public void updateOptionBarPosition() {

        // only if attached to the DOM
        if ((m_elementOptionBar != null) && RootPanel.getBodyElement().isOrHasChild(getElement())) {
            int absoluteTop = getElement().getAbsoluteTop();
            int absoluteRight = getElement().getAbsoluteRight();
            CmsPositionBean dimensions = CmsPositionBean.getBoundingClientRect(getElement());

            int top = 0;
            int right = 0;
            int offsetLeft = 0;
            int offsetTop = 0;

            final Style style = m_elementOptionBar.getElement().getStyle();

            if (m_positioningInstructionParser.tryParse(getElement().getClassName())) {
                offsetLeft = m_positioningInstructionParser.getOffsetLeft();
                offsetTop = m_positioningInstructionParser.getOffsetTop();
            }

            if (Math.abs(absoluteTop - dimensions.getTop()) > 20) {
                absoluteTop = (dimensions.getTop() - absoluteTop) + 2;
                top = absoluteTop;
            }
            if (Math.abs(absoluteRight - dimensions.getLeft() - dimensions.getWidth()) > 20) {
                absoluteRight = (absoluteRight - dimensions.getLeft() - dimensions.getWidth()) + 2;
                right = absoluteRight;
            }

            top += offsetTop;
            right -= offsetLeft;

            if (top != 0) {
                style.setTop(top, Unit.PX);
            } else {
                style.clearTop();
            }

            if (right != 0) {
                style.setRight(right, Unit.PX);
            } else {
                style.clearRight();
            }

            if (isOptionbarIFrameCollision(absoluteTop, m_elementOptionBar.getCalculatedWidth())) {
                style.setPosition(Position.RELATIVE);
                int marginLeft = getElement().getClientWidth() - m_elementOptionBar.getCalculatedWidth();
                if (marginLeft > 0) {
                    style.setMarginLeft(marginLeft, Unit.PX);
                }
            } else {
                style.clearPosition();
                style.clearMarginLeft();
            }
        }
    }

    /**
     * Checks for changes in the list collector direct edit content.<p>
     */
    protected void checkForEditableChanges() {

        if (!m_checkingEditables) {
            m_checkingEditables = true;
            Timer timer = new Timer() {

                @Override
                public void run() {

                    showEditableListButtons();
                }
            };
            timer.schedule(500);
        }
    }

    /**
     * Gets the editable list elements.<p>
     *
     * @return the editable list elements
     */
    protected List<Element> getEditableElements() {

        List<Element> elems = CmsDomUtil.getElementsByClass(CmsGwtConstants.CLASS_EDITABLE, Tag.ALL, getElement());
        List<Element> result = Lists.newArrayList();
        for (Element currentElem : elems) {
            // don't return elements which are contained in nested containers
            if (m_parent.getContainerId().equals(getParentContainerId(currentElem))) {
                result.add(currentElem);
            }
        }
        return result;
    }

    /**
     * Returns if the list collector direct edit content has changed.<p>
     *
     * @return <code>true</code> if the list collector direct edit content has changed
     */
    protected boolean hasChangedEditables() {

        if (m_editables == null) {
            return true;
        }
        for (CmsListCollectorEditor editor : m_editables.values()) {
            if (!editor.isValid()) {
                return true;
            }
        }
        return getEditableElements().size() > m_editables.size();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    protected void onDetach() {

        super.onDetach();
        removeEditorHandler();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        if ((getParentTarget() instanceof CmsContainerPageContainer)
            && ((CmsContainerPageContainer)getParentTarget()).isEditable()
            && !hasCheckedHeight()) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    CmsContainerPageElementPanel thisElement = CmsContainerPageElementPanel.this;
                    if (!hasCheckedHeight() && CmsSmallElementsHandler.isSmall(thisElement)) {
                        CmsContainerpageController.get().getSmallElementsHandler().prepareSmallElement(thisElement);
                    }
                    setCheckedHeight(true);
                }
            });
        }
        resetOptionbar();
    }

    /**
     * Removes the inline editor handler.<p>
     */
    protected void removeEditorHandler() {

        if (m_editorClickHandlerRegistration != null) {
            m_editorClickHandlerRegistration.removeHandler();
            m_editorClickHandlerRegistration = null;
        }
    }

    /**
     * Returns if the minimum element height has been checked.<p>
     *
     * @return <code>true</code> if the minimum element height has been checked
     */
    boolean hasCheckedHeight() {

        return m_checkedHeight;
    }

    /**
     * Sets the checked height flag.<p>
     *
     * @param checked the checked height flag
     */
    void setCheckedHeight(boolean checked) {

        m_checkedHeight = checked;
    }

    /**
     * Adds the collector edit buttons.<p>
     *
     * @param editable the marker element for an editable list element
     */
    private void addListCollectorEditorButtons(Element editable) {

        CmsListCollectorEditor editor = new CmsListCollectorEditor(editable, m_clientId);
        add(editor, editable.getParentElement());
        if (CmsDomUtil.hasDimension(editable.getParentElement())) {
            editor.setParentHasDimensions(true);
            editor.setPosition(CmsDomUtil.getEditablePosition(editable), getElement());
        } else {
            editor.setParentHasDimensions(false);
        }
        m_editables.put(editable, editor);
    }

    /**
     * Removes all styling done during drag and drop.<p>
     */
    private void clearDrag() {

        CmsDomUtil.removeDisablingOverlay(getElement());
        m_elementOptionBar.getElement().removeClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.stateCss().cmsHovering());
        getElement().getStyle().clearOpacity();
        getElement().getStyle().clearDisplay();
        updateOptionBarPosition();
        if (m_provisionalParent != null) {
            m_provisionalParent.removeFromParent();
            m_provisionalParent = null;
        }
    }

    /**
     * Gets the container id of the most deeply nested container containing the given element, or null if no such container can be found.<p>
     *
     * @param elem the element
     * @return the container id of the deepest container containing the element
     */
    private String getParentContainerId(Element elem) {

        String attr = CmsContainerPageContainer.PROP_CONTAINER_MARKER;
        Element lastElem;
        do {
            String propValue = elem.getPropertyString(attr);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
                return propValue;
            }
            lastElem = elem;
            elem = elem.getParentElement();
        } while ((elem != null) && (elem != lastElem));
        return null;
    }

    /**
     * Initializes the mutation observer used for updating the edit buttons after DOM changes (e.g. pagination).
     */
    private void initMutationObserver() {

        if (m_mutationObserver == null) {
            MutationObserverCallbackFn callback = (JsArray<MutationRecord> records, MutationObserver obs) -> {
                checkForEditableChanges();
                return null;
            };
            m_mutationObserver = new MutationObserver(callback);
            MutationObserverInit options = Js.cast(new JsObject());
            options.setSubtree(true);
            options.setChildList(true);
            m_mutationObserver.observe(Js.cast(getElement()), options);
        }
    }

    /**
     * Returns if the option bar position collides with any iframe child elements.<p>
     *
     * @param optionTop the option bar absolute top
     * @param optionWidth the option bar width
     *
     * @return <code>true</code> if there are iframe child elements located no less than 25px below the upper edge of the element
     */
    private boolean isOptionbarIFrameCollision(int optionTop, int optionWidth) {

        if (RootPanel.getBodyElement().isOrHasChild(getElement())) {
            NodeList<Element> frames = getElement().getElementsByTagName(CmsDomUtil.Tag.iframe.name());
            for (int i = 0; i < frames.getLength(); i++) {
                int frameTop = frames.getItem(i).getAbsoluteTop();
                int frameHeight = frames.getItem(i).getOffsetHeight();
                int frameRight = frames.getItem(i).getAbsoluteRight();
                if (((frameTop - optionTop) < 25)
                    && (((frameTop + frameHeight) - optionTop) > 0)
                    && ((frameRight - getElement().getAbsoluteRight()) < optionWidth)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * This method removes the option-bar widget from DOM and re-attaches it at it's original position.<p>
     * Use to avoid mouse-over and mouse-down malfunction.<p>
     */
    private void resetOptionbar() {

        if (m_elementOptionBar != null) {
            if (getWidgetIndex(m_elementOptionBar) >= 0) {
                m_elementOptionBar.removeFromParent();
            }
            updateOptionBarPosition();
            insert(m_elementOptionBar, 0);
        }
    }
}

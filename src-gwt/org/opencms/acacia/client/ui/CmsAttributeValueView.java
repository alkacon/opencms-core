/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.acacia.client.ui;

import org.opencms.acacia.client.CmsAttributeHandler;
import org.opencms.acacia.client.CmsButtonBarHandler;
import org.opencms.acacia.client.CmsChoiceMenuEntryBean;
import org.opencms.acacia.client.CmsEditorBase;
import org.opencms.acacia.client.CmsValueFocusHandler;
import org.opencms.acacia.client.I_CmsEntityRenderer;
import org.opencms.acacia.client.I_CmsWidgetService;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.widgets.CmsFormWidgetWrapper;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;
import org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection;
import org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection.Direction;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.I_CmsHasResizeOnShow;
import org.opencms.gwt.client.dnd.I_CmsDragHandle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.common.base.Optional;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI object holding an attribute value.<p>
 */
public class CmsAttributeValueView extends Composite
implements I_CmsDraggable, I_CmsHasResizeOnShow, HasMouseOverHandlers, HasMouseOutHandlers, HasMouseDownHandlers {

    /**
     * The widget value change handler.<p>
     */
    protected class ChangeHandler implements ValueChangeHandler<String> {

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            getHandler().handleValueChange(CmsAttributeValueView.this, event.getValue());
            removeValidationMessage();
        }
    }

    /**
     * Handles showing and hiding the help bubble on mouse over the title label.<p>
     */
    protected class LabelHoverHandler implements MouseOverHandler, MouseOutHandler {

        /** Mouse in timer. */
        Timer m_inTimer;

        /** Mouse out timer. */
        Timer m_outTimer;

        /**
         * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        public void onMouseOut(MouseOutEvent event) {

            if (m_inTimer != null) {
                m_inTimer.cancel();
                m_inTimer = null;
            }
            if (m_outTimer == null) {
                m_outTimer = new Timer() {

                    @Override
                    public void run() {

                        toggleLabelHover(false);
                        m_outTimer = null;
                    }
                };
                m_outTimer.schedule(200);
            }
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        public void onMouseOver(MouseOverEvent event) {

            if (m_outTimer != null) {
                m_outTimer.cancel();
                m_outTimer = null;
            }
            if (m_inTimer == null) {
                m_inTimer = new Timer() {

                    @Override
                    public void run() {

                        toggleLabelHover(true);
                        m_inTimer = null;
                    }
                };
                m_inTimer.schedule(400);
            }
        }
    }

    /** The move handle. */
    protected class MoveHandle extends CmsPushButton implements I_CmsDragHandle {

        /** The draggable. */
        private CmsAttributeValueView m_draggable;

        /**
         * Constructor.<p>
         *
         * @param draggable the draggable
         */
        MoveHandle(CmsAttributeValueView draggable) {

            setImageClass(I_CmsButton.MOVE_SMALL);
            setButtonStyle(ButtonStyle.FONT_ICON, null);
            if (CmsEditorBase.hasDictionary()) {
                setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_MOVE_1, getLabel()));
            }
            m_draggable = draggable;
        }

        /**
         * @see org.opencms.gwt.client.dnd.I_CmsDragHandle#getDraggable()
         */
        public I_CmsDraggable getDraggable() {

            return m_draggable;
        }

    }

    /**
     * The UI binder interface.<p>
     */
    interface I_AttributeValueUiBinder extends UiBinder<HTMLPanel, CmsAttributeValueView> {
        // nothing to do
    }

    public static final String ATTR_KEY = "data-key";

    /** The first column compact view mode. */
    public static final int COMPACT_MODE_FIRST_COLUMN = 1;

    /** The nested compact view mode. */
    public static final int COMPACT_MODE_NESTED = 3;

    /** The second column compact view mode. */
    public static final int COMPACT_MODE_SECOND_COLUMN = 2;

    /** The single line compact view mode. */
    public static final int COMPACT_MODE_SINGLE_LINE = 4;

    /** The wide compact view mode. */
    public static final int COMPACT_MODE_WIDE = 0;

    /** The toolbar height. */
    private static final int TOOLBAR_HEIGHT = 52;

    /** Attribute for marking menu bars that are actually menu bars which should open on hovering (rather than just a single button). */
    public static final String ATTR_HAS_HOVER = "data-has-hover-menu";

    /** The UI binder instance. */
    private static I_AttributeValueUiBinder uiBinder = GWT.create(I_AttributeValueUiBinder.class);

    /** The add button. */
    @UiField
    protected CmsAttributeChoiceWidget m_addButton;

    /** The attribute choice button. */
    @UiField
    protected CmsAttributeChoiceWidget m_attributeChoice;

    /** The button bar. */
    @UiField
    protected FlowPanel m_buttonBar;

    /** The down button. */
    @UiField
    protected CmsPushButton m_downButton;

    /** The help bubble element. */
    @UiField
    protected DivElement m_helpBubble;

    /** The help bubble close button. */
    @UiField
    protected CmsPushButton m_helpBubbleClose;

    /** The help bubble text element. */
    @UiField
    protected DivElement m_helpBubbleText;

    /** The message text element. */
    @UiField
    protected SpanElement m_messageText;

    /** The move button. */
    @UiField(provided = true)
    protected MoveHandle m_moveButton;

    /** The remove button. */
    @UiField
    protected CmsPushButton m_removeButton;

    /** The up button. */
    @UiField
    protected CmsPushButton m_upButton;

    /** The widget holder elemenet. */
    @UiField
    protected FlowPanel m_widgetHolder;

    /** The currently running animation. */
    Animation m_currentAnimation;

    /** The activation mouse down handler registration. */
    private HandlerRegistration m_activationHandlerRegistration;

    /** Style variable to enable/disable 'collapsed' style. */
    private CmsStyleVariable m_collapsedStyle = new CmsStyleVariable(this);

    /** The compact view style variable. */
    private CmsStyleVariable m_compacteModeStyle;

    /** The default widget value. */
    private String m_defaultValue;

    /** Flag indicating if drag and drop is enabled for this attribute. */
    private boolean m_dragEnabled;

    /** Drag and drop helper element. */
    private Element m_dragHelper;

    /** The attribute handler. */
    private CmsAttributeHandler m_handler;

    /** Flag indicating a validation error. */
    private boolean m_hasError;

    /** Flag indicating if there is a value set for this UI object. */
    private boolean m_hasValue;

    /** The help text. */
    private String m_help;

    /** Flag indicating this is a representing an attribute choice value. */
    private boolean m_isChoice;

    /** Flag indicating that this view represents a simple value. */
    private boolean m_isSimpleValue;

    /** The label text. */
    private String m_label;

    /** The label mouse in handler registration. */
    private HandlerRegistration m_labelOutRegistration;

    /** The label mouse over handler registration. */
    private HandlerRegistration m_labelOverRegistration;

    /** The drag and drop place holder element. */
    private Element m_placeHolder;

    /** The editing widget. */
    private I_CmsFormEditWidget m_widget;

    /**
     * Constructor.<p>
     *
     * @param handler the attribute handler
     * @param label the attribute label
     * @param help the attribute help information
     */
    public CmsAttributeValueView(CmsAttributeHandler handler, String label, String help) {

        // important: provide the move button before binding the widget UI
        m_moveButton = new MoveHandle(this);
        initWidget(uiBinder.createAndBindUi(this));
        m_handler = handler;
        m_handler.registerAttributeValue(this);
        if (!CmsCoreProvider.isTouchOnly()) {
            m_moveButton.addMouseDownHandler(m_handler.getDNDHandler());
        }
        m_label = label;
        m_help = help;
        if (m_help == null) {
            m_helpBubble.getStyle().setDisplay(Display.NONE);
            m_help = "";
        }
        generateLabel();
        m_helpBubbleText.setInnerHTML(m_help);
        addStyleName(formCss().emptyValue());
        m_compacteModeStyle = new CmsStyleVariable(this);
        m_compacteModeStyle.setValue(formCss().defaultView());
        initHighlightingHandler();
        initButtons();
        m_buttonBar.addStyleName(CmsButtonBarHandler.HOVERABLE_MARKER);

        m_buttonBar.addDomHandler(CmsButtonBarHandler.INSTANCE, MouseOverEvent.getType());
        m_buttonBar.addDomHandler(CmsButtonBarHandler.INSTANCE, MouseOutEvent.getType());
        m_collapsedStyle.setValue(formCss().uncollapsed());

    }

    /**
     * Adds a new choice  choice selection menu.<p>
     *
     * @param widgetService the widget service to use for labels
     * @param menuEntry the menu entry bean for the choice
     */
    public void addChoice(I_CmsWidgetService widgetService, final CmsChoiceMenuEntryBean menuEntry) {

        AsyncCallback<CmsChoiceMenuEntryBean> selectHandler = new AsyncCallback<CmsChoiceMenuEntryBean>() {

            public void onFailure(Throwable caught) {

                // will not be called

            }

            public void onSuccess(CmsChoiceMenuEntryBean selectedEntry) {

                m_attributeChoice.hide();
                selectChoice(selectedEntry.getPath());
            }
        };

        m_attributeChoice.addChoice(widgetService, menuEntry, selectHandler);
        m_isChoice = true;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    public FlowPanel getButtonBar() {

        return m_buttonBar;
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

        closeHelpBubble(null);
        // using the widget element as the drag helper also to avoid cloning issues on input fields
        m_dragHelper = getElement();
        Element parentElement = getElement().getParentElement();
        if (parentElement == null) {
            parentElement = target.getElement();
        }
        int elementTop = getElement().getAbsoluteTop();
        int parentTop = parentElement.getAbsoluteTop();
        Style style = m_dragHelper.getStyle();
        style.setWidth(m_dragHelper.getOffsetWidth(), Unit.PX);
        // the dragging class will set position absolute
        style.setTop(elementTop - parentTop, Unit.PX);
        m_dragHelper.addClassName(formCss().dragHelper());
        return m_dragHelper;
    }

    /**
     * Returns the attribute handler.<p>
     *
     * @return the attribute handler
     */
    public CmsAttributeHandler getHandler() {

        return m_handler;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getId()
     */
    public String getId() {

        String id = getElement().getId();
        if ((id == null) || "".equals(id)) {
            id = Document.get().createUniqueId();
            getElement().setId(id);
        }
        return id;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getParentTarget()
     */
    public I_CmsDropTarget getParentTarget() {

        return (I_CmsDropTarget)getParent();
    }

    /**
     * Gets the parent attribute value view, or null if none exists.<p>
     *
     * @return the parent attribute value view
     */
    public CmsAttributeValueView getParentView() {

        Widget ancestor = getParent();
        while ((ancestor != null) && !(ancestor instanceof CmsAttributeValueView)) {
            ancestor = ancestor.getParent();
        }
        return (CmsAttributeValueView)ancestor;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getPlaceholder(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getPlaceholder(I_CmsDropTarget target) {

        m_placeHolder = CmsDomUtil.clone(getElement());
        removeDragHelperStyles(m_placeHolder);
        m_placeHolder.addClassName(formCss().dragPlaceholder());
        return m_placeHolder;
    }

    /**
     * Returns the attribute value index.<p>
     *
     * @return the attribute value index
     */
    public int getValueIndex() {

        int result = 0;
        Node previousSibling = getElement().getPreviousSibling();
        while (previousSibling != null) {
            result++;
            previousSibling = previousSibling.getPreviousSibling();
        }
        return result;
    }

    /**
     * Returns the editing widget.<p>
     *
     * @return the editing widget or <code>null</code> if not available
     */
    public I_CmsEditWidget getValueWidget() {

        return m_widget;
    }

    /**
     * Returns if there is a value set for this attribute.<p>
     *
     * @return <code>true</code> if there is a value set for this attribute
     */
    public boolean hasValue() {

        return m_hasValue && ((m_widget == null) || m_widget.isActive());
    }

    /**
     * Hides the button bar.<p>
     */
    public void hideAllButtons() {

        m_buttonBar.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Returns if drag and drop is enabled for this attribute.<p>
     *
     * @return <code>true</code> if drag and drop is enabled for this attribute
     */
    public boolean isDragEnabled() {

        return m_dragEnabled;
    }

    /**
     * Returns if this view represents a simple value.<p>
     *
     * @return <code>true</code> if this view represents a simple value
     */
    public boolean isSimpleValue() {

        return m_isSimpleValue;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    public void onDragCancel() {

        clearDrag();
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

        // nothing to do
    }

    /**
     * Checks if the attribute value view's widget "owns" the given element.<p>
     *
     * @param element the element to check
     * @return true if the widget owns the element
     */
    public boolean owns(Element element) {

        return (m_widget != null) && m_widget.owns(element);
    }

    /**
     * Removes any present error message.<p>
     */
    public void removeValidationMessage() {

        if (m_hasError) {
            m_messageText.setInnerText("");
            removeStyleName(formCss().hasError());
            removeStyleName(formCss().hasWarning());
            m_hasError = false;
        }

    }

    /**
     * Removes the value.<p>
     */
    public void removeValue() {

        if (!isSimpleValue()) {
            m_hasValue = false;
            m_widgetHolder.clear();
            generateLabel();
        } else {
            // only deactivate the widget and restore the default value
            m_widget.setActive(false);
            if (m_widget.shouldSetDefaultWhenDisabled() && (m_defaultValue != null)) {
                m_widget.setValue(m_defaultValue);
            } else {
                m_widget.setValue("");
            }
            addActivationHandler();
        }
        addStyleName(formCss().emptyValue());
        removeValidationMessage();
    }

    /**
     * @see org.opencms.gwt.client.I_CmsHasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        // call resize on all children implementing org.opencms.acacia.client.ui.I_HasResizeOnShow
        if (hasValue()) {
            if (isSimpleValue()) {
                if (m_widget instanceof I_CmsHasResizeOnShow) {
                    ((I_CmsHasResizeOnShow)m_widget).resizeOnShow();
                }
            } else {
                for (Widget panel : m_widgetHolder) {
                    if (panel instanceof HasWidgets.ForIsWidget) {
                        for (Widget w : (HasWidgets.ForIsWidget)panel) {
                            if (w instanceof I_CmsHasResizeOnShow) {
                                ((I_CmsHasResizeOnShow)w).resizeOnShow();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the value widget active and removes the inactive view styling.<p>
     */
    public void setActive() {

        if (m_widget != null) {
            m_widget.setActive(true);
            removeStyleName(formCss().emptyValue());
        }
    }

    /**
     * Enables or disables the "collapsed" style, which is used for choice elements to reduce the nesting level visually.<p>
     *
     * @param collapsed true if the view should be set to 'collapsed'
     */
    public void setCollapsed(boolean collapsed) {

        m_collapsedStyle.setValue(collapsed ? formCss().collapsed() : formCss().uncollapsed());
    }

    /**
     * Sets the compact view mode.<p>
     *
     * @param mode the mode to set
     */
    public void setCompactMode(int mode) {

        switch (mode) {
            case COMPACT_MODE_FIRST_COLUMN:
                m_compacteModeStyle.setValue(formCss().firstColumn());
                break;
            case COMPACT_MODE_SECOND_COLUMN:
                m_compacteModeStyle.setValue(formCss().secondColumn());
                break;
            case COMPACT_MODE_NESTED:
                m_compacteModeStyle.setValue(formCss().compactView());
                break;
            case COMPACT_MODE_SINGLE_LINE:
                m_compacteModeStyle.setValue(formCss().singleLine());
                break;
            default:

        }
        updateWidth();
    }

    /**
     * Shows a validation error message.<p>
     *
     * @param message the error message
     */
    public void setErrorMessage(String message) {

        m_messageText.setInnerHTML(message);
        addStyleName(formCss().hasError());
        m_hasError = true;
    }

    /**
     * Sets the value entity.<p>
     *
     * @param renderer the entity renderer
     * @param value the value entity
     */
    public void setValueEntity(I_CmsEntityRenderer renderer, CmsEntity value) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_hasValue = true;
        m_isSimpleValue = false;
        FlowPanel entityPanel = new FlowPanel();
        m_widgetHolder.add(entityPanel);
        int index = getValueIndex();
        m_handler.ensureHandlers(index);
        renderer.renderForm(value, entityPanel, m_handler, index);
        removeStyleName(formCss().emptyValue());
    }

    /**
     * Sets the value widget.<p>
     *
     * @param widget the widget
     * @param value the value
     * @param defaultValue the default attribute value
     * @param active <code>true</code> if the widget should be activated
     */
    public void setValueWidget(I_CmsFormEditWidget widget, String value, String defaultValue, boolean active) {

        if (m_hasValue) {
            throw new RuntimeException("Value has already been set");
        }
        m_defaultValue = defaultValue;
        m_hasValue = true;
        m_isSimpleValue = true;
        m_widget = widget;
        if (CmsAttributeHandler.hasResizeHandler() && (m_widget instanceof HasResizeHandlers)) {
            ((HasResizeHandlers)m_widget).addResizeHandler(CmsAttributeHandler.getResizeHandler());
        }
        m_widgetHolder.clear();
        m_widget.setWidgetInfo(m_label, m_help);
        if (active) {
            m_widget.setValue(value, false);
        } else if (m_widget.shouldSetDefaultWhenDisabled()) {
            m_widget.setValue(defaultValue, false);
        } else {
            m_widget.setValue("");
        }
        m_widgetHolder.add(m_widget);
        m_widget.setName(getHandler().getAttributeName());
        m_widget.addValueChangeHandler(new ChangeHandler());
        m_widget.addFocusHandler(new FocusHandler() {

            public void onFocus(FocusEvent event) {

                CmsValueFocusHandler.getInstance().setFocus(CmsAttributeValueView.this);
                activateWidget();
            }
        });
        m_widget.setActive(active);
        if (!active) {
            addActivationHandler();
        } else {
            removeStyleName(formCss().emptyValue());
        }
        addStyleName(formCss().simpleValue());

        if (m_widget instanceof CmsFormWidgetWrapper) {
            addLabelHoverHandler(((CmsFormWidgetWrapper)m_widget).getLabel());
        } else {
            removeLabelHoverHandler();
        }
    }

    /**
     * Shows a validation warning message.<p>
     *
     * @param message the warning message
     */
    public void setWarningMessage(String message) {

        m_messageText.setInnerText(message);
        addStyleName(formCss().hasWarning());
        m_hasError = true;
    }

    /**
     * Shows the button bar.<p>
     */
    public void showButtons() {

        m_buttonBar.getElement().getStyle().clearDisplay();
    }

    /**
     * Tells the attribute value view to change its display state between focused/unfocused (this doesn't actually change the focus).<p>
     *
     * @param focusOn <code>true</code> to change the display state to 'focused'
     */
    public void toggleFocus(boolean focusOn) {

        if (!isAttached()) {
            // can happen if values get actively deleted
            return;
        }
        if (focusOn) {
            addStyleName(formCss().focused());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_help) || m_hasError) {
                m_helpBubble.getStyle().clearDisplay();
                if (shouldDisplayTooltipAbove()) {
                    addStyleName(formCss().displayAbove());
                } else {
                    removeStyleName(formCss().displayAbove());
                }
            } else {
                m_helpBubble.getStyle().setDisplay(Display.NONE);
            }
        } else {
            removeStyleName(formCss().focused());
            if (m_widget != null) {
                if (m_handler.hasSingleOptionalValue()) {
                    if (m_handler.getWidgetService().shouldRemoveLastValueAfterUnfocus(m_widget)) {
                        m_handler.removeAttributeValue(this);
                    }
                }
            }
        }
    }

    /**
     * Updates the visibility of the add, remove, up and down buttons.<p>
     *
     * @param hasAddButton <code>true</code> if the add button should be visible
     * @param hasRemoveButton <code>true</code> if the remove button should be visible
     * @param hasSortButtons <code>true</code> if the sort buttons should be visible
     */
    public void updateButtonVisibility(boolean hasAddButton, boolean hasRemoveButton, boolean hasSortButtons) {

        boolean hasOnlyDelete = !hasAddButton && hasRemoveButton && !hasSortButtons;
        boolean hasHoverMenu = !hasOnlyDelete;

        m_buttonBar.getElement().setAttribute(ATTR_HAS_HOVER, "" + hasHoverMenu);

        if (hasAddButton && m_isChoice) {
            m_attributeChoice.getElement().getStyle().clearDisplay();
        } else {
            m_attributeChoice.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasAddButton && !m_isChoice) {
            m_addButton.getElement().getStyle().clearDisplay();
        } else {
            m_addButton.getElement().getStyle().setDisplay(Display.NONE);
        }

        if (hasRemoveButton) {
            m_removeButton.getElement().getStyle().clearDisplay();
        } else {
            m_removeButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons && (getValueIndex() != 0)) {
            m_upButton.getElement().getStyle().clearDisplay();
        } else {
            m_upButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons && (getElement().getNextSibling() != null)) {
            m_downButton.getElement().getStyle().clearDisplay();
        } else {
            m_downButton.getElement().getStyle().setDisplay(Display.NONE);
        }
        if (hasSortButtons) {
            m_moveButton.addStyleName(I_CmsLayoutBundle.INSTANCE.form().moveHandle());
            if (CmsEditorBase.hasDictionary()) {
                m_moveButton.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_MOVE_1, m_label));
            }
        } else {
            m_moveButton.setTitle("");
            m_moveButton.removeStyleName(I_CmsLayoutBundle.INSTANCE.form().moveHandle());
        }
        m_dragEnabled = hasSortButtons;
        if (!hasAddButton && !hasRemoveButton && !hasSortButtons) {
            // hide the button bar if no button is visible
            m_buttonBar.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            // show the button bar
            m_buttonBar.getElement().getStyle().clearDisplay();
            if (hasSortButtons || (hasAddButton && hasRemoveButton)) {
                // set multi button mode
                m_buttonBar.addStyleName(formCss().multiButtonBar());
                m_moveButton.getElement().getStyle().clearDisplay();
            } else {
                m_moveButton.getElement().getStyle().setDisplay(Display.NONE);
                m_buttonBar.removeStyleName(formCss().multiButtonBar());
            }
        }
    }

    /**
     * Adds a new attribute value.<p>
     */
    protected void addNewAttributeValue() {

        if ((m_widget != null) && !m_widget.isActive()) {
            activateWidget();
        } else {
            m_handler.addNewAttributeValue(this);
        }
        onResize();
    }

    /**
     * Handles the click event to close the help bubble.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_helpBubbleClose")
    protected void closeHelpBubble(ClickEvent event) {

        addStyleName(formCss().closedBubble());
    }

    /**
     * Returns the attribute label.<p>
     *
     * @return the attribute label
     */
    protected String getLabel() {

        return m_label;
    }

    /**
     * Handles the click event to move the attribute value down.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_downButton")
    protected void moveAttributeValueDown(ClickEvent event) {

        m_handler.moveAttributeValueDown(this);
    }

    /**
     * Handles the click event to move the attribute value up.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_upButton")
    protected void moveAttributeValueUp(ClickEvent event) {

        m_handler.moveAttributeValueUp(this);
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                updateWidth();
            }
        });
    }

    /**
     * Call when content changes.<p>
     */
    protected void onResize() {

        Widget parent = getParent();
        while (parent != null) {
            if (parent instanceof I_CmsDescendantResizeHandler) {
                ((I_CmsDescendantResizeHandler)parent).onResizeDescendant();
                break;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Handles the click event to remove the attribute value.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_removeButton")

    protected void removeAttributeValue(ClickEvent event) {

        m_handler.removeAttributeValue(this);
        onResize();
    }

    /**
     * Selects the attribute choice.<p>
     *
     * @param choicePath the choice attribute path
     */
    protected void selectChoice(List<String> choicePath) {

        m_handler.addNewChoiceAttributeValue(this, choicePath);
    }

    /**
     * Activates the value widget if prNamet.<p>
     */
    void activateWidget() {

        if (m_activationHandlerRegistration != null) {
            m_activationHandlerRegistration.removeHandler();
            m_activationHandlerRegistration = null;
        }
        if ((m_widget != null) && !m_widget.isActive()) {
            m_widget.setActive(true);
            if ((m_defaultValue != null) && (m_defaultValue.trim().length() > 0)) {
                m_widget.setValue(m_defaultValue, true);
            }
            m_handler.updateButtonVisisbility();
            removeStyleName(formCss().emptyValue());
        }
    }

    /**
     * Toggles the label hover CSS class.<p>
     *
     * @param hovered <code>true</code> in case the
     */
    void toggleLabelHover(boolean hovered) {

        boolean hover = hovered && (m_hasError || CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_help));
        if (hover) {
            addStyleName(formCss().labelHover());
            if (shouldDisplayTooltipAbove()) {
                addStyleName(formCss().displayAbove());
            } else {
                removeStyleName(formCss().displayAbove());
            }
        } else {
            removeStyleName(formCss().labelHover());
        }
        CmsValueFocusHandler.getInstance().hideHelpBubbles(
            RootPanel.get(),
            hover || !CmsCoreProvider.get().isShowEditorHelp());
    }

    /**
     * Updates the widget width according to the compact mode setting.<p>
     */
    void updateWidth() {

        if (formCss().firstColumn().equals(m_compacteModeStyle.getValue())) {
            int width = getElement().getParentElement().getOffsetWidth() - formCss().SECOND_COLUMN_WIDTH();
            // if width could not be evaluated, fall back to a 'save' value
            if (width < 0) {
                width = 400;
            }
            getElement().getStyle().setWidth(width, Unit.PX);
        } else {
            getElement().getStyle().clearWidth();
        }
    }

    /**
     * Adds a mouse down handler to activate the editing widget.<p>
     */
    private void addActivationHandler() {

        if (m_activationHandlerRegistration == null) {
            m_activationHandlerRegistration = addMouseDownHandler(new MouseDownHandler() {

                public void onMouseDown(MouseDownEvent event) {

                    // only act on click if not inside the button bar
                    if (!m_buttonBar.getElement().isOrHasChild((Node)event.getNativeEvent().getEventTarget().cast())) {
                        activateWidget();
                    }
                }
            });
        }
    }

    /**
     * Adds the label hover mouse handler.<p>
     *
     * @param label the label widget
     */
    private void addLabelHoverHandler(Label label) {

        removeLabelHoverHandler();
        LabelHoverHandler hoverHandler = new LabelHoverHandler();
        m_labelOutRegistration = label.addMouseOutHandler(hoverHandler);
        m_labelOverRegistration = label.addMouseOverHandler(hoverHandler);
    }

    /**
     * Called when a drag operation for this widget is stopped.<p>
     */
    private void clearDrag() {

        if (m_dragHelper != null) {
            removeDragHelperStyles(m_dragHelper);
            m_dragHelper = null;
        }
        // preventing issue where mouse out was never triggered after drag and drop
        m_moveButton.clearHoverState();
        CmsButtonBarHandler.INSTANCE.closeAll();
    }

    /**
     * Returns the CSS bundle for the form editor.<p>
     *
     * @return the form CSS bundle
     */
    private I_CmsLayoutBundle.I_Style formCss() {

        return I_CmsLayoutBundle.INSTANCE.form();
    }

    /**
     * Generates the attribute label.<p>
     */
    private void generateLabel() {

        HTML labelWidget = new HTML("<div class=\"" + formCss().label() + "\">" + m_label + "</div>");
        addLabelHoverHandler(labelWidget);
        m_widgetHolder.add(labelWidget);
    }

    /**
     * Initializes the button styling.<p>
     */
    private void initButtons() {

        m_addButton.addChoice(
            m_handler.getWidgetService(),
            new CmsChoiceMenuEntryBean(m_handler.getAttributeName()),
            new AsyncCallback<CmsChoiceMenuEntryBean>() {

                public void onFailure(Throwable caught) {

                    // will not be called

                }

                public void onSuccess(CmsChoiceMenuEntryBean selectedEntry) {

                    // nothing to do
                }
            });
        m_addButton.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                m_addButton.hide();
                addNewAttributeValue();
                event.preventDefault();
                event.stopPropagation();

            }
        }, ClickEvent.getType());

        m_removeButton.setImageClass(I_CmsButton.CUT_SMALL);
        m_removeButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_removeButton.setHideFromTabNav(true);

        m_upButton.setImageClass(I_CmsButton.EDIT_UP_SMALL);
        m_upButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_upButton.setHideFromTabNav(true);

        m_downButton.setImageClass(I_CmsButton.EDIT_DOWN_SMALL);
        m_downButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_downButton.setHideFromTabNav(true);

        m_helpBubbleClose.setImageClass(I_CmsButton.DELETE_SMALL);
        m_helpBubbleClose.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_helpBubbleClose.setHideFromTabNav(true);
        if (CmsEditorBase.hasDictionary()) {
            m_addButton.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_ADD_1, m_label));
            m_attributeChoice.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_CHOICE_ADD_CHOICE_1, m_label));
            m_removeButton.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_DELETE_1, m_label));
            m_helpBubbleClose.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_CLOSE_0));
            m_upButton.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_MOVE_UP_0, m_label));
            m_downButton.setTitle(CmsEditorBase.getMessageForKey(CmsEditorBase.GUI_VIEW_MOVE_DOWN_0, m_label));
        }
    }

    /**
     * Initializes the highlighting handler.<p>
     */
    private void initHighlightingHandler() {

        addMouseOverHandler(CmsValueFocusHandler.getInstance());
        addMouseOutHandler(CmsValueFocusHandler.getInstance());
        addMouseDownHandler(CmsValueFocusHandler.getInstance());
    }

    /**
     * Removes the drag helper styles from the given element.<p>
     *
     * @param helper the helper element
     */
    private void removeDragHelperStyles(Element helper) {

        Style style = helper.getStyle();
        style.clearTop();
        style.clearLeft();
        style.clearPosition();
        style.clearWidth();
        helper.removeClassName(formCss().dragHelper());
    }

    /**
     * Removes the label hover mouse handler.<p>
     */
    private void removeLabelHoverHandler() {

        if (m_labelOverRegistration != null) {
            m_labelOverRegistration.removeHandler();
            m_labelOverRegistration = null;
        }
        if (m_labelOutRegistration != null) {
            m_labelOutRegistration.removeHandler();
            m_labelOutRegistration = null;
        }
    }

    /**
     * Returns if the help bubble should be displayed above the value field.<p>
     *
     * @return <code>true</code> if the help bubble should be displayed above
     */
    private boolean shouldDisplayTooltipAbove() {

        boolean displayAbove;
        if (isSimpleValue()) {
            Direction direction = Direction.none;
            if (m_widget instanceof I_CmsHasDisplayDirection) {
                direction = ((I_CmsHasDisplayDirection)m_widget).getDisplayingDirection();
            }
            switch (direction) {
                case above:
                    displayAbove = false;
                    break;
                case below:
                case none:
                default:
                    displayAbove = true;
                    break;
            }
        } else {
            displayAbove = true;
        }

        m_helpBubble.getStyle().setDisplay(Display.BLOCK);
        int bubbleHeight = m_helpBubble.getOffsetHeight();
        m_helpBubble.getStyle().clearDisplay();

        Element widgetElement;
        if (m_widget != null) {
            widgetElement = m_widget.asWidget().getElement();
        } else {
            return true;
        }

        // Calculate top position for the popup
        int top = widgetElement.getAbsoluteTop();

        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();
        int distanceFromWindowTop = top - windowTop - TOOLBAR_HEIGHT;

        int distanceToWindowBottom = windowBottom - (top + widgetElement.getOffsetHeight());
        if (displayAbove
            && ((distanceFromWindowTop < bubbleHeight) && (distanceToWindowBottom > distanceFromWindowTop))) {
            // in case there is too little space above, and there is more below, change direction
            displayAbove = false;
        } else if (!displayAbove
            && ((distanceToWindowBottom < bubbleHeight) && (distanceFromWindowTop > distanceToWindowBottom))
            && !m_hasError) {
            // in case there is too little space below, and there is more above, change direction
            // (exception for m_hasError: when displaying a validation error for a widget that displays a popup above it, disregard the
            // available space under it, because if we display the message below, the page expands so the user can scroll down to read it, which is less
            // bad than the message covering a popup required to use the widget).
            displayAbove = true;
        }
        return displayAbove;
    }
}

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

import org.opencms.acacia.client.CmsUndoRedoHandler.ChangeType;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.entity.I_CmsEntityBackend;
import org.opencms.acacia.client.ui.CmsAttributeValueView;
import org.opencms.acacia.client.ui.CmsInlineEntityWidget;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsType;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.util.CmsMoveAnimation;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The attribute handler. Handles value changes, addition of new values, remove and move operations on values.<p> 
 */
public class CmsAttributeHandler extends CmsRootHandler {

    /** The global widget resize handler. */
    private static ResizeHandler m_resizeHandler;

    /** The scroll element. */
    private static Element m_scrollElement;

    /** The attribute name. */
    private String m_attributeName;

    /** The attribute type. */
    private CmsType m_attributeType;

    /** Registered attribute values. */
    private List<CmsAttributeValueView> m_attributeValueViews;

    /** The attribute drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /** The entity. */
    private CmsEntity m_entity;

    /** The entity type. */
    private CmsType m_entityType;

    /** The parent attribute handler. */
    private I_CmsAttributeHandler m_parentHandler;

    /** The single value index. */
    private int m_singleValueIndex;

    /** The VIE instance. */
    private I_CmsEntityBackend m_vie;

    /** The widget service. */
    private I_CmsWidgetService m_widgetService;

    /**
     * Constructor.<p>
     * 
     * @param vie the VIE instance
     * @param entity the entity
     * @param attributeName the attribute name
     * @param widgetService the widget service
     */
    public CmsAttributeHandler(I_CmsEntityBackend vie, CmsEntity entity, String attributeName, I_CmsWidgetService widgetService) {

        // single value handling is disable by default
        m_singleValueIndex = -1;
        m_vie = vie;
        m_entity = entity;
        m_attributeName = attributeName;
        m_widgetService = widgetService;
        m_attributeValueViews = new ArrayList<CmsAttributeValueView>();
        if (!getAttributeType().isSimpleType()) {
            int count = 0;
            CmsEntityAttribute attribute = entity.getAttribute(attributeName);
            if (attribute != null) {
                count = attribute.getValueCount();
            }
            initHandlers(count);
        }
    }

    /**
     * Clears the error styles on the given tabbed panel.<p>
     * 
     * @param tabbedPanel the tabbed panel
     */
    public static void clearErrorStyles(CmsTabbedPanel<?> tabbedPanel) {

        for (int i = 0; i < tabbedPanel.getTabCount(); i++) {
            Widget tab = tabbedPanel.getTabWidget(i);
            tab.setTitle(null);
            tab.getParent().removeStyleName(I_CmsLayoutBundle.INSTANCE.form().hasError());
            tab.getParent().removeStyleName(I_CmsLayoutBundle.INSTANCE.form().hasWarning());
        }
    }

    /**
     * Returns the global widget resize handler.<p>
     * 
     * @return the global widget resize handler
     */
    public static ResizeHandler getResizeHandler() {

        return m_resizeHandler;
    }

    /**
     * Returns <code>true</code> if a global widget resize handler is present.<p>
     * 
     * @return <code>true</code> if a global widget resize handler is present
     */
    public static boolean hasResizeHandler() {

        return m_resizeHandler != null;
    }

    /**
     * Sets the global widget resize handler.<p>
     * 
     * @param handler the resize handler
     */
    public static void setResizeHandler(ResizeHandler handler) {

        m_resizeHandler = handler;
    }

    /**
     * Sets the scroll element. To be used for automatic scrolling during drag and drop.<p>
     * 
     * @param scrollElement the scroll element
     */
    public static void setScrollElement(Element scrollElement) {

        m_scrollElement = scrollElement;
    }

    /**
     * Adds a new attribute value below the reference view.<p>
     * 
     * @param reference the reference value view
     */
    public void addNewAttributeValue(CmsAttributeValueView reference) {

        // make sure not to add more values than allowed
        int maxOccurrence = getEntityType().getAttributeMaxOccurrence(m_attributeName);
        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        boolean mayHaveMore = ((attribute == null) || (attribute.getValueCount() < maxOccurrence));
        if (mayHaveMore) {
            if (getAttributeType().isSimpleType()) {
                int valueIndex = reference.getValueIndex() + 1;
                String defaultValue = m_widgetService.getDefaultAttributeValue(
                    m_attributeName,
                    getSimplePath(valueIndex));
                I_CmsFormEditWidget widget = m_widgetService.getAttributeFormWidget(m_attributeName);

                boolean insertLast = false;
                if (reference.getElement().getNextSiblingElement() == null) {
                    m_entity.addAttributeValue(m_attributeName, defaultValue);
                    insertLast = true;
                } else {
                    valueIndex = reference.getValueIndex() + 1;
                    m_entity.insertAttributeValue(m_attributeName, defaultValue, valueIndex);
                    m_widgetService.addChangedOrderPath(getSimplePath(-1));
                }
                CmsAttributeValueView valueWidget = reference;
                if (reference.hasValue()) {
                    valueWidget = new CmsAttributeValueView(
                        this,
                        m_widgetService.getAttributeLabel(m_attributeName),
                        m_widgetService.getAttributeHelp(m_attributeName));
                    if (m_widgetService.isDisplaySingleLine(m_attributeName)) {
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                    if (insertLast) {
                        ((FlowPanel)reference.getParent()).add(valueWidget);
                    } else {
                        ((FlowPanel)reference.getParent()).insert(valueWidget, valueIndex);
                    }

                }
                valueWidget.setValueWidget(widget, defaultValue, defaultValue, true);
            } else {
                CmsEntity value = m_vie.createEntity(null, getAttributeType().getId());
                insertValueAfterReference(value, reference);
            }
            CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
            if (handler.isIntitalized()) {
                handler.addChange(m_entity.getId(), m_attributeName, reference.getValueIndex() + 1, ChangeType.add);
            }
        }
        updateButtonVisisbility();
    }

    /**
     * Adds a new attribute value and adds the required widgets to the editor DOM.<p>
     * 
     * @param value the value entity
     */
    public void addNewAttributeValue(CmsEntity value) {

        // make sure not to add more values than allowed
        int maxOccurrence = getEntityType().getAttributeMaxOccurrence(m_attributeName);
        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        boolean mayHaveMore = ((attribute == null) || (attribute.getValueCount() < maxOccurrence));
        if (mayHaveMore && value.getTypeName().equals(m_attributeType)) {
            m_entity.addAttributeValue(m_attributeName, value);
            int valueIndex = m_entity.getAttribute(m_attributeName).getValueCount() - 1;
            CmsAttributeValueView valueView = null;
            if ((m_attributeValueViews.size() == 1) && !m_attributeValueViews.get(0).hasValue()) {
                valueView = m_attributeValueViews.get(0);
            } else {
                valueView = new CmsAttributeValueView(
                    this,
                    m_widgetService.getAttributeLabel(m_attributeName),
                    m_widgetService.getAttributeHelp(m_attributeName));
            }

            CmsRenderer.setAttributeChoice(m_widgetService, valueView, getAttributeType());
            ((FlowPanel)m_attributeValueViews.get(0).getParent()).add(valueView);

            insertHandlers(valueIndex);
            I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(m_attributeName, getAttributeType());
            valueView.setValueEntity(renderer, value);

            CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
            if (handler.isIntitalized()) {
                handler.addChange(m_entity.getId(), m_attributeName, valueIndex, ChangeType.add);
            }
        }
    }

    /**
     * Adds a new attribute value and adds the required widgets to the editor DOM.<p>
     * 
     * @param value the simple value
     */
    public void addNewAttributeValue(String value) {

        // make sure not to add more values than allowed
        int maxOccurrence = getEntityType().getAttributeMaxOccurrence(m_attributeName);
        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        boolean mayHaveMore = ((attribute == null) || (attribute.getValueCount() < maxOccurrence));
        if (mayHaveMore && getAttributeType().isSimpleType()) {
            I_CmsFormEditWidget widget = m_widgetService.getAttributeFormWidget(m_attributeName);
            m_entity.addAttributeValue(m_attributeName, value);
            String defaultValue = m_widgetService.getDefaultAttributeValue(
                m_attributeName,
                getSimplePath(m_entity.getAttribute(m_attributeName).getValueCount() - 1));
            CmsAttributeValueView valueView = null;
            if ((m_attributeValueViews.size() == 1) && !m_attributeValueViews.get(0).hasValue()) {
                valueView = m_attributeValueViews.get(0);
                valueView.setActive();
                valueView.getValueWidget().setValue(value);
            } else {
                valueView = new CmsAttributeValueView(
                    this,
                    m_widgetService.getAttributeLabel(m_attributeName),
                    m_widgetService.getAttributeHelp(m_attributeName));
                if (m_widgetService.isDisplaySingleLine(m_attributeName)) {
                    valueView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                }
                ((FlowPanel)m_attributeValueViews.get(0).getParent()).add(valueView);
                valueView.setValueWidget(widget, value, defaultValue, true);
            }
            CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
            if (handler.isIntitalized()) {
                handler.addChange(
                    m_entity.getId(),
                    m_attributeName,
                    m_entity.getAttribute(m_attributeName).getValueCount() - 1,
                    ChangeType.add);
            }
            updateButtonVisisbility();
        }
    }

    /**
     * Adds a new attribute value below the reference index.<p>
     * This will not execute any DOM manipulations.<p>
     * 
     * @param referenceIndex the reference value index
     */
    public void addNewAttributeValueToEntity(int referenceIndex) {

        // make sure not to add more values than allowed
        int maxOccurrence = getEntityType().getAttributeMaxOccurrence(m_attributeName);
        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        boolean mayHaveMore = ((attribute == null) || (attribute.getValueCount() < maxOccurrence));
        if (mayHaveMore) {
            if (getAttributeType().isSimpleType()) {
                String defaultValue = m_widgetService.getDefaultAttributeValue(
                    m_attributeName,
                    getSimplePath(referenceIndex + 1));
                if ((attribute == null) || (attribute.getValueCount() == (referenceIndex + 1))) {
                    m_entity.addAttributeValue(m_attributeName, defaultValue);
                } else {
                    m_entity.insertAttributeValue(m_attributeName, defaultValue, referenceIndex + 1);
                    m_widgetService.addChangedOrderPath(getSimplePath(-1));
                }
            } else {
                CmsEntity value = m_vie.createEntity(null, m_attributeType.getId());
                if ((attribute == null) || (attribute.getValueCount() == (referenceIndex + 1))) {
                    m_entity.addAttributeValue(m_attributeName, value);
                } else {
                    m_entity.insertAttributeValue(m_attributeName, value, referenceIndex + 1);
                    m_widgetService.addChangedOrderPath(getSimplePath(-1));
                }
                insertHandlers(referenceIndex + 1);
            }
        }
    }

    /**
     * Adds a new choice attribute value.<p>
     * 
     * @param reference the reference value view
     * @param choicePath the path of the selected (possibly nested) choice attribute, consisting of attribute names 
     */
    public void addNewChoiceAttributeValue(CmsAttributeValueView reference, List<String> choicePath) {

        CmsValueFocusHandler.getInstance().clearFocus();
        m_widgetService.addChangedOrderPath(getSimplePath(-1));
        if (isChoiceHandler()) {
            addChoiceOption(reference, choicePath);
        } else {
            addComplexChoiceValue(reference, choicePath);
        }
        updateButtonVisisbility();
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, reference.getValueIndex() + 1, ChangeType.choice);
        }
    }

    /**
     * Applies a value change to the entity data as well as to the value view widget.<p>
     * 
     * @param value the value
     * @param valueIndex the value index
     */
    public void changeValue(String value, int valueIndex) {

        m_attributeValueViews.get(valueIndex).getValueWidget().setValue(value, false);
        changeEntityValue(value, valueIndex);
    }

    /**
     * @see org.opencms.acacia.client.CmsRootHandler#collectSimplePath(org.opencms.acacia.client.I_CmsAttributeHandler)
     */
    @Override
    public String collectSimplePath(I_CmsAttributeHandler childHandler) {

        int index = -1;
        for (int i = 0; i < m_handlers.size(); i++) {
            if (m_handlers.get(i).get(childHandler.getAttributeName()) == childHandler) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new RuntimeException("Child handler is not properly registered.");
        }
        return getSimplePath(index) + "/";
    }

    /**
     * Creates a sequence of nested entities according to a given path of choice attribute names.<p>
     * 
     * @param value the entity into which the new entities for the given path should be inserted 
     * @param choicePath the path of choice attributes 
     */
    public void createNestedEntitiesForChoicePath(CmsEntity value, List<String> choicePath) {

        CmsEntity parentValue = value;
        for (String attributeChoice : choicePath) {
            CmsType choiceType = m_vie.getType(parentValue.getTypeName()).getAttributeType(CmsType.CHOICE_ATTRIBUTE_NAME);
            CmsEntity choice = m_vie.createEntity(null, choiceType.getId());
            parentValue.addAttributeValue(CmsType.CHOICE_ATTRIBUTE_NAME, choice);
            CmsType choiceOptionType = choiceType.getAttributeType(attributeChoice);
            if (choiceOptionType.isSimpleType()) {
                String choiceValue = m_widgetService.getDefaultAttributeValue(attributeChoice, getSimplePath(0));
                choice.addAttributeValue(attributeChoice, choiceValue);
                break;
            } else {
                CmsEntity choiceValue = m_vie.createEntity(null, choiceOptionType.getId());
                choice.addAttributeValue(attributeChoice, choiceValue);
                parentValue = choiceValue;
            }
        }
    }

    /**
     * Destroys the attribute handler instance.<p>
     */
    public void destroy() {

        m_attributeName = null;
        m_attributeType = null;
        m_attributeValueViews.clear();
        m_attributeValueViews = null;
        m_dndHandler = null;
        m_entity = null;
        m_entityType = null;
        m_vie = null;
        m_widgetService = null;
    }

    /**
     * Returns the attribute name.<p>
     * 
     * @return the attribute name
     */
    @Override
    public String getAttributeName() {

        return m_attributeName;
    }

    /**
     * Returns the attribute type.<p>
     * 
     * @return the attribute type
     */
    public CmsType getAttributeType() {

        if (m_attributeType == null) {
            m_attributeType = getEntityType().getAttributeType(m_attributeName);
        }
        return m_attributeType;
    }

    /**
     * Returns the drag and drop handler.<p>
     * 
     * @return the drag and drop handler
     */
    public CmsDNDHandler getDNDHandler() {

        if (m_dndHandler == null) {
            m_dndHandler = new CmsDNDHandler(new CmsAttributeDNDController());
            m_dndHandler.setOrientation(Orientation.VERTICAL);
            m_dndHandler.setScrollEnabled(true);
            m_dndHandler.setScrollElement(m_scrollElement);
        }
        return m_dndHandler;
    }

    /**
     * Returns the entity id.<p>
     * 
     * @return the entity id
     */
    public String getEntityId() {

        return m_entity.getId();
    }

    /**
     * Gets the maximum occurrence of the attribute.<p>
     * 
     * @return the maximum occurrence 
     */
    public int getMaxOccurence() {

        return getEntityType().getAttributeMaxOccurrence(m_attributeName);
    }

    /**
     * Returns the simple value path for the given index.<p>
     * This will use the last fragment of the attribute name and concatenate it with the parent path.<p>
     * If the given index equals -1 no value index will be appended
     * 
     * @param index the value index
     * 
     * @return the simple path
     */
    public String getSimplePath(int index) {

        String simpleName = m_attributeName.substring(m_attributeName.lastIndexOf("/") + 1);
        String result = m_parentHandler.collectSimplePath(this) + simpleName;
        if (index != -1) {
            result += "[" + (index + 1) + "]";
        }
        return result;
    }

    /**
     * Gets the widget service.<p>
     * 
     * @return the widget service 
     */
    public I_CmsWidgetService getWidgetService() {

        return m_widgetService;
    }

    /**
     * Handles value changes from the view.<p>
     * 
     * @param reference the attribute value reference
     * @param value the value
     */
    public void handleValueChange(CmsAttributeValueView reference, String value) {

        handleValueChange(reference.getValueIndex(), value);
    }

    /**
     * Handles value changes from the view.<p>
     * 
     * @param valueIndex the value index
     * @param value the value
     */
    public void handleValueChange(int valueIndex, String value) {

        changeEntityValue(value, valueIndex);
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, valueIndex, ChangeType.value);
        }
    }

    /**
     * Return true if there is a single remaining value, which is optional.<p>
     * 
     * @return true if this has only one optional value
     */
    public boolean hasSingleOptionalValue() {

        return ((getEntityType().getAttributeMinOccurrence(m_attributeName) == 0)
            && (m_entity.getAttribute(m_attributeName) != null) && (m_entity.getAttribute(m_attributeName).getValueCount() == 1));
    }

    /**
     * Returns if there is a value view widget registered for the given index.<p>
     * 
     * @param valueIndex the value index
     * 
     * @return <code>true</code> if there is a value view widget registered for the given index
     */
    public boolean hasValueView(int valueIndex) {

        return m_attributeValueViews.size() > valueIndex;
    }

    /**
     * Returns if this is a choice handler.<p>
     * 
     * @return <code>true</code> if this is a choice handler
     */
    public boolean isChoiceHandler() {

        return CmsType.CHOICE_ATTRIBUTE_NAME.equals(m_attributeName);
    }

    /**
     * Moves the give attribute value from one position to another.<p>
     * 
     * @param valueView the value to move
     * @param currentPosition the current position
     * @param targetPosition the target position
     */
    public void moveAttributeValue(CmsAttributeValueView valueView, int currentPosition, int targetPosition) {

        if (currentPosition == targetPosition) {
            return;
        }
        FlowPanel parent = (FlowPanel)valueView.getParent();
        m_widgetService.addChangedOrderPath(getSimplePath(-1));
        valueView.removeFromParent();
        m_attributeValueViews.remove(valueView);
        CmsAttributeValueView valueWidget = null;
        if (isChoiceHandler()) {
            removeHandlers(currentPosition);
            CmsEntity value = m_entity.getAttribute(m_attributeName).getComplexValues().get(currentPosition);
            m_entity.removeAttributeValue(m_attributeName, currentPosition);
            m_entity.insertAttributeValue(m_attributeName, value, targetPosition);
            String attributeChoice = getChoiceName(targetPosition);
            CmsType optionType = getAttributeType().getAttributeType(attributeChoice);
            valueWidget = new CmsAttributeValueView(
                this,
                m_widgetService.getAttributeLabel(attributeChoice),
                m_widgetService.getAttributeHelp(attributeChoice));
            if (optionType.isSimpleType() && m_widgetService.isDisplaySingleLine(attributeChoice)) {
                valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
            }
            parent.insert(valueWidget, targetPosition);
            insertHandlers(targetPosition);
            if (optionType.isSimpleType()) {
                valueWidget.setValueWidget(
                    m_widgetService.getAttributeFormWidget(attributeChoice),
                    value.getAttribute(attributeChoice).getSimpleValue(),
                    m_widgetService.getDefaultAttributeValue(attributeChoice, getSimplePath(targetPosition)),
                    true);
            } else {
                valueWidget.setValueEntity(
                    m_widgetService.getRendererForAttribute(attributeChoice, getAttributeType()),
                    value.getAttribute(attributeChoice).getComplexValue());
            }

            List<CmsChoiceMenuEntryBean> menuEntries = CmsRenderer.getChoiceEntries(getAttributeType(), true);
            for (CmsChoiceMenuEntryBean menuEntry : menuEntries) {
                valueWidget.addChoice(m_widgetService, menuEntry);
            }
        } else if (getAttributeType().isSimpleType()) {
            String value = m_entity.getAttribute(m_attributeName).getSimpleValues().get(currentPosition);
            m_entity.removeAttributeValue(m_attributeName, currentPosition);
            m_entity.insertAttributeValue(m_attributeName, value, targetPosition);
            valueWidget = new CmsAttributeValueView(
                this,
                m_widgetService.getAttributeLabel(m_attributeName),
                m_widgetService.getAttributeHelp(m_attributeName));
            if (m_widgetService.isDisplaySingleLine(m_attributeName)) {
                valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
            }
            parent.insert(valueWidget, targetPosition);
            valueWidget.setValueWidget(
                m_widgetService.getAttributeFormWidget(m_attributeName),
                value,
                m_widgetService.getDefaultAttributeValue(m_attributeName, getSimplePath(targetPosition)),
                true);
        } else {
            removeHandlers(currentPosition);
            CmsEntity value = m_entity.getAttribute(m_attributeName).getComplexValues().get(currentPosition);
            m_entity.removeAttributeValue(m_attributeName, currentPosition);
            m_entity.insertAttributeValue(m_attributeName, value, targetPosition);
            valueWidget = new CmsAttributeValueView(
                this,
                m_widgetService.getAttributeLabel(m_attributeName),
                m_widgetService.getAttributeHelp(m_attributeName));
            parent.insert(valueWidget, targetPosition);
            insertHandlers(targetPosition);
            valueWidget.setValueEntity(
                m_widgetService.getRendererForAttribute(m_attributeName, getAttributeType()),
                value);

        }
        m_attributeValueViews.remove(valueWidget);
        m_attributeValueViews.add(targetPosition, valueWidget);
        updateButtonVisisbility();
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, 0, ChangeType.sort);
        }
    }

    /**
     * Moves the reference value down in the value list.<p>
     * 
     * @param reference the reference value
     */
    public void moveAttributeValueDown(final CmsAttributeValueView reference) {

        final int index = reference.getValueIndex();
        if (index >= (m_entity.getAttribute(m_attributeName).getValueCount() - 1)) {
            return;
        }
        m_widgetService.addChangedOrderPath(getSimplePath(-1));
        reference.hideAllButtons();
        Element parent = reference.getElement().getParentElement();
        parent.getStyle().setPosition(Position.RELATIVE);
        final Element placeHolder = reference.getPlaceholder(null);
        int top = reference.getElement().getOffsetTop();
        int left = reference.getElement().getOffsetLeft();
        int width = reference.getOffsetWidth();
        reference.getElement().getStyle().setPosition(Position.ABSOLUTE);
        reference.getElement().getStyle().setZIndex(5);
        parent.insertAfter(placeHolder, reference.getElement().getNextSibling());
        reference.getElement().getStyle().setTop(top, Unit.PX);
        reference.getElement().getStyle().setLeft(left, Unit.PX);
        reference.getElement().getStyle().setWidth(width, Unit.PX);
        new CmsMoveAnimation(reference.getElement(), top, left, placeHolder.getOffsetTop(), left, new Command() {

            public void execute() {

                clearMoveAnimationStyles(placeHolder, reference);
                moveAttributeValue(reference, index, index + 1);

            }
        }).run(200);
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, 0, ChangeType.sort);
        }
    }

    /**
     * Moves the reference value up in the value list.<p>
     * 
     * @param reference the reference value
     */
    public void moveAttributeValueUp(final CmsAttributeValueView reference) {

        final int index = reference.getValueIndex();
        if (index == 0) {
            return;
        }
        m_widgetService.addChangedOrderPath(getSimplePath(-1));
        reference.hideAllButtons();
        Element parent = reference.getElement().getParentElement();
        parent.getStyle().setPosition(Position.RELATIVE);
        final Element placeHolder = reference.getPlaceholder(null);
        int top = reference.getElement().getOffsetTop();
        int left = reference.getElement().getOffsetLeft();
        int width = reference.getOffsetWidth();
        reference.getElement().getStyle().setPosition(Position.ABSOLUTE);
        reference.getElement().getStyle().setZIndex(5);
        parent.insertBefore(placeHolder, reference.getElement().getPreviousSibling());
        reference.getElement().getStyle().setTop(top, Unit.PX);
        reference.getElement().getStyle().setLeft(left, Unit.PX);
        reference.getElement().getStyle().setWidth(width, Unit.PX);
        new CmsMoveAnimation(reference.getElement(), top, left, placeHolder.getOffsetTop(), left, new Command() {

            public void execute() {

                clearMoveAnimationStyles(placeHolder, reference);
                moveAttributeValue(reference, index, index - 1);

            }
        }).run(200);
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, 0, ChangeType.sort);
        }
    }

    /**
     * Registers an attribute value view.<p>
     * 
     * @param attributeValue the attribute value view
     */
    public void registerAttributeValue(CmsAttributeValueView attributeValue) {

        m_attributeValueViews.add(attributeValue);
    }

    /**
     * Removes the reference attribute value view.<p>
     * 
     * @param reference the reference view
     */
    public void removeAttributeValue(CmsAttributeValueView reference) {

        CmsAttributeHandler parentHandler = null;
        CmsAttributeValueView parentView = null;
        boolean removeParent = false;

        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        if (isChoiceHandler() && attribute.isSingleValue()) {
            // removing last choice value, so remove choice itself 
            parentHandler = (CmsAttributeHandler)m_parentHandler;
            parentView = reference.getParentView();
            removeParent = true;
        }

        if (attribute.isSingleValue()) {

            reference.removeValue();
            if (!attribute.isSimpleValue()) {
                removeHandlers(0);
            }
            m_entity.removeAttribute(m_attributeName);
        } else {
            int index = reference.getValueIndex();
            if (attribute.isComplexValue()) {
                removeHandlers(index);
            }
            CmsEntity value = attribute.getComplexValues().get(index);
            m_entity.removeAttributeValue(m_attributeName, index);
            m_vie.removeEntity(value.getId());
            reference.removeFromParent();
            m_attributeValueViews.remove(reference);

        }
        updateButtonVisisbility();
        if (removeParent && (parentHandler != null) && (parentView != null)) {
            parentHandler.removeAttributeValue(parentView);
            parentView.setCollapsed(false);
        }
        CmsUndoRedoHandler handler = CmsUndoRedoHandler.getInstance();
        if (handler.isIntitalized()) {
            handler.addChange(m_entity.getId(), m_attributeName, 0, ChangeType.remove);
        }
    }

    /**
     * Removes the attribute value from the given index, also manipulating the editor DOM to display the change.<p>
     * 
     * @param valueIndex the value index
     */
    public void removeAttributeValue(int valueIndex) {

        if (m_attributeValueViews.size() > valueIndex) {
            removeAttributeValue(m_attributeValueViews.get(valueIndex));
        }
    }

    /**
     * Removes the attribute value with the given index.<p>
     * This will not execute any DOM manipulations.<p>
     * 
     * @param valueIndex the index of the attribute value to remove
     */
    public void removeAttributeValueFromEntity(int valueIndex) {

        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        if (attribute.isSingleValue()) {
            if (attribute.isComplexValue()) {
                removeHandlers(0);
            }
            m_entity.removeAttribute(m_attributeName);
        } else {
            if (attribute.isComplexValue()) {
                removeHandlers(valueIndex);
                CmsEntity value = attribute.getComplexValues().get(valueIndex);
                m_entity.removeAttributeValue(m_attributeName, valueIndex);
                m_vie.removeEntity(value.getId());
            } else {
                m_entity.removeAttributeValue(m_attributeName, valueIndex);
            }
        }
    }

    /**
     * Sets the error message for the given value index.<p>
     * 
     * @param valueIndex the value index
     * @param message the error message
     * @param tabbedPanel the forms tabbed panel if available
     */
    public void setErrorMessage(int valueIndex, String message, CmsTabbedPanel<?> tabbedPanel) {

        if (!m_attributeValueViews.isEmpty()) {
            FlowPanel parent = (FlowPanel)m_attributeValueViews.get(0).getParent();
            CmsAttributeValueView valueView = (CmsAttributeValueView)parent.getWidget(valueIndex);
            valueView.setErrorMessage(message);
            if (tabbedPanel != null) {
                int tabIndex = tabbedPanel.getTabIndex(valueView.getElement());
                if (tabIndex > -1) {
                    Widget tab = tabbedPanel.getTabWidget(tabIndex);
                    tab.setTitle("This tab has errors.");
                    tab.getParent().removeStyleName(I_CmsLayoutBundle.INSTANCE.form().hasWarning());
                    tab.getParent().addStyleName(I_CmsLayoutBundle.INSTANCE.form().hasError());
                }

            }
        }
    }

    /**
     * @see org.opencms.acacia.client.CmsRootHandler#setHandlerById(java.lang.String, org.opencms.acacia.client.CmsAttributeHandler)
     */
    @Override
    public void setHandlerById(String attributeName, CmsAttributeHandler handler) {

        if (m_parentHandler != null) {
            m_parentHandler.setHandlerById(attributeName, handler);
        }
    }

    /**
     * Sets the parent attribute handler.<p>
     * 
     * @param handler the parent attribute handler 
     */
    public void setParentHandler(I_CmsAttributeHandler handler) {

        m_parentHandler = handler;
    }

    /**
     * Sets the warning message for the given value index.<p>
     * 
     * @param valueIndex the value index
     * @param message the warning message
     * @param tabbedPanel the forms tabbed panel if available
     */
    public void setWarningMessage(int valueIndex, String message, CmsTabbedPanel<?> tabbedPanel) {

        if (!m_attributeValueViews.isEmpty()) {
            FlowPanel parent = (FlowPanel)m_attributeValueViews.get(0).getParent();
            CmsAttributeValueView valueView = (CmsAttributeValueView)parent.getWidget(valueIndex);
            valueView.setWarningMessage(message);
            if (tabbedPanel != null) {
                int tabIndex = tabbedPanel.getTabIndex(valueView.getElement());
                if (tabIndex > -1) {
                    Widget tab = tabbedPanel.getTabWidget(tabIndex);
                    tab.setTitle("This tab has warnings.");
                    tab.getParent().addStyleName(I_CmsLayoutBundle.INSTANCE.form().hasWarning());
                }

            }
        }
    }

    /**
     * Updates the add, remove and sort button visibility on the given inline widget or all registered attribute value views.<p>
     * 
     * @param inlineWidget the inline widget
     */
    public void updateButtonVisibilty(CmsInlineEntityWidget inlineWidget) {

        int minOccurrence = 0;
        int maxOccurrence = 0;
        if (isChoiceHandler()) {
            minOccurrence = 0;
            maxOccurrence = getEntityType().getChoiceMaxOccurrence();
        } else {
            minOccurrence = getEntityType().getAttributeMinOccurrence(m_attributeName);
            maxOccurrence = getEntityType().getAttributeMaxOccurrence(m_attributeName);
        }
        CmsEntityAttribute attribute = m_entity.getAttribute(m_attributeName);
        boolean mayHaveMore = (maxOccurrence > minOccurrence)
            && ((((attribute == null) && (!getAttributeType().isSimpleType() || (inlineWidget != null))) || ((attribute != null) && (attribute.getValueCount() < maxOccurrence))));
        boolean needsRemove = false;
        boolean needsSort = false;
        if ((isChoiceHandler() || !getEntityType().isChoice()) && m_entity.hasAttribute(m_attributeName)) {
            int valueCount = m_entity.getAttribute(m_attributeName).getValueCount();
            needsRemove = (maxOccurrence > minOccurrence) && (valueCount > minOccurrence);
            needsSort = !isSingleValueHandler() && (valueCount > 1);
        }
        if (inlineWidget != null) {
            boolean mayEdit = (attribute != null) && (attribute.getValueCount() > inlineWidget.getAttributeIndex());
            inlineWidget.updateButtonVisibility(mayEdit, mayHaveMore, needsRemove, needsSort);
        } else {
            for (CmsAttributeValueView value : m_attributeValueViews) {
                value.updateButtonVisibility(mayHaveMore, needsRemove, needsSort);
            }
        }
    }

    /**
     * Updates the add, remove and sort button visibility on all registered attribute value views.<p>
     */
    public void updateButtonVisisbility() {

        updateButtonVisibilty(null);
    }

    /**
     * Returns if the attribute handler is handling a single value only.<p>
     * 
     * @return <code>true</code> if the attribute handler is handling a single value only
     */
    protected boolean isSingleValueHandler() {

        return m_singleValueIndex > -1;
    }

    /**
     * Sets the single value index.<p>
     * 
     * @param valueIndex the value index
     */
    protected void setSingleValueIndex(int valueIndex) {

        m_singleValueIndex = valueIndex;
    }

    /**
     * Clears the inline styles used during move animation.<p>
     * 
     * @param placeHolder the animation place holder
     * @param reference the moved attribute widget
     */
    void clearMoveAnimationStyles(Element placeHolder, CmsAttributeValueView reference) {

        placeHolder.removeFromParent();
        reference.getElement().getParentElement().getStyle().clearPosition();
        reference.getElement().getStyle().clearPosition();
        reference.getElement().getStyle().clearWidth();
        reference.getElement().getStyle().clearZIndex();
        reference.showButtons();
    }

    /**
     * Adds a new choice option.<p>
     * 
     * @param reference the reference view
     * @param choicePath the choice attribute path
     */
    private void addChoiceOption(CmsAttributeValueView reference, List<String> choicePath) {

        String attributeChoice = choicePath.get(0);
        CmsType optionType = getAttributeType().getAttributeType(attributeChoice);
        int valueIndex = reference.getValueIndex() + 1;
        CmsEntity choiceEntity = m_vie.createEntity(null, getAttributeType().getId());
        CmsAttributeValueView valueWidget = reference;
        if (reference.hasValue()) {
            valueWidget = new CmsAttributeValueView(
                this,
                m_widgetService.getAttributeLabel(attributeChoice),
                m_widgetService.getAttributeHelp(attributeChoice));
            if (optionType.isSimpleType() && m_widgetService.isDisplaySingleLine(attributeChoice)) {
                valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
            }
        }

        List<CmsChoiceMenuEntryBean> menuEntries = CmsRenderer.getChoiceEntries(getAttributeType(), true);
        for (CmsChoiceMenuEntryBean menuEntry : menuEntries) {
            valueWidget.addChoice(m_widgetService, menuEntry);
        }

        m_entity.insertAttributeValue(m_attributeName, choiceEntity, valueIndex);
        ((FlowPanel)reference.getParent()).insert(valueWidget, valueIndex);
        insertHandlers(valueWidget.getValueIndex());

        if (optionType.isSimpleType()) {
            String defaultValue = m_widgetService.getDefaultAttributeValue(attributeChoice, getSimplePath(valueIndex));
            I_CmsFormEditWidget widget = m_widgetService.getAttributeFormWidget(attributeChoice);
            choiceEntity.addAttributeValue(attributeChoice, defaultValue);
            valueWidget.setValueWidget(widget, defaultValue, defaultValue, true);
        } else {
            CmsEntity value = m_vie.createEntity(null, optionType.getId());
            choiceEntity.addAttributeValue(attributeChoice, value);
            List<String> remainingAttributeNames = tail(choicePath);
            createNestedEntitiesForChoicePath(value, remainingAttributeNames);
            I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeChoice, optionType);
            valueWidget.setValueEntity(renderer, value);
        }
        updateButtonVisisbility();

    }

    /**
     * Adds a new complex value which corresponds to a choice element.<p>
     * 
     * @param reference the reference view  
     * @param choicePath the path of choice attribute names 
     */
    private void addComplexChoiceValue(CmsAttributeValueView reference, List<String> choicePath) {

        CmsEntity value = m_vie.createEntity(null, getAttributeType().getId());
        CmsEntity parentValue = value;
        for (String attributeChoice : choicePath) {
            CmsType choiceType = m_vie.getType(parentValue.getTypeName()).getAttributeType(CmsType.CHOICE_ATTRIBUTE_NAME);
            CmsEntity choice = m_vie.createEntity(null, choiceType.getId());
            parentValue.addAttributeValue(CmsType.CHOICE_ATTRIBUTE_NAME, choice);
            CmsType choiceOptionType = choiceType.getAttributeType(attributeChoice);
            if (choiceOptionType.isSimpleType()) {
                String choiceValue = m_widgetService.getDefaultAttributeValue(attributeChoice, getSimplePath(0));
                choice.addAttributeValue(attributeChoice, choiceValue);
                break;
            } else {
                CmsEntity choiceValue = m_vie.createEntity(null, choiceOptionType.getId());
                choice.addAttributeValue(attributeChoice, choiceValue);
                parentValue = choiceValue;
            }
        }
        insertValueAfterReference(value, reference);
        if (getMaxOccurence() == 1) {
            reference.setCollapsed(true);
        }
    }

    /**
     * Changes the attribute value.<p>
     * 
     * @param valueIndex the attribute value index
     * @param value the value
     */
    private void changeEntityValue(String value, int valueIndex) {

        if (getEntityType().isChoice()) {
            CmsEntity choice = m_entity.getAttribute(CmsType.CHOICE_ATTRIBUTE_NAME).getComplexValues().get(valueIndex);
            String attributeName = getChoiceName(valueIndex);
            if (attributeName != null) {
                choice.setAttributeValue(attributeName, value, 0);
            }
        } else {
            m_entity.setAttributeValue(m_attributeName, value, valueIndex);
        }
    }

    /**
     * Returns the attribute choice name for the given index.<p>
     * 
     * @param valueIndex the value index
     * 
     * @return the attribute choice name
     */
    private String getChoiceName(int valueIndex) {

        if (isChoiceHandler()) {
            CmsEntity choice = m_entity.getAttribute(CmsType.CHOICE_ATTRIBUTE_NAME).getComplexValues().get(valueIndex);
            if (choice != null) {
                for (String option : getAttributeType().getAttributeNames()) {
                    if (choice.hasAttribute(option)) {
                        return option;

                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the entity type.<p>
     * 
     * @return the entity type
     */
    private CmsType getEntityType() {

        if (m_entityType == null) {
            m_entityType = m_vie.getType(m_entity.getTypeName());
        }
        return m_entityType;
    }

    /**
     * Inserts an entity value after the given reference.<p>
     * 
     * @param value the entity value
     * @param reference the reference
     */
    private void insertValueAfterReference(CmsEntity value, CmsAttributeValueView reference) {

        int valueIndex = -1;
        if (reference.getElement().getNextSiblingElement() == null) {
            m_entity.addAttributeValue(m_attributeName, value);
        } else {
            valueIndex = reference.getValueIndex() + 1;
            m_entity.insertAttributeValue(m_attributeName, value, valueIndex);
        }
        CmsAttributeValueView valueWidget = reference;
        if (reference.hasValue()) {
            valueWidget = new CmsAttributeValueView(
                this,
                m_widgetService.getAttributeLabel(m_attributeName),
                m_widgetService.getAttributeHelp(m_attributeName));
            CmsRenderer.setAttributeChoice(m_widgetService, valueWidget, getAttributeType());
            if (valueIndex == -1) {
                ((FlowPanel)reference.getParent()).add(valueWidget);
            } else {
                ((FlowPanel)reference.getParent()).insert(valueWidget, valueIndex);
                m_widgetService.addChangedOrderPath(getSimplePath(-1));
            }
        }
        valueIndex = valueWidget.getValueIndex();
        insertHandlers(valueIndex);
        I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(m_attributeName, getAttributeType());
        valueWidget.setValueEntity(renderer, value);
    }

    /**
     * Creates a list consisting of all but the first element of another list.<p>
     * 
     * @param values the list 
     * 
     * @return the tail of the list 
     */
    private List<String> tail(List<String> values) {

        List<String> result = new ArrayList<String>();
        boolean first = true;
        for (String value : values) {
            if (!first) {
                result.add(value);
            }
            first = false;
        }
        return result;

    }
}

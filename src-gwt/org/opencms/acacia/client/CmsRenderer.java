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

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.entity.I_CmsEntityBackend;
import org.opencms.acacia.client.ui.CmsAttributeValueView;
import org.opencms.acacia.client.ui.CmsInlineEntityWidget;
import org.opencms.acacia.client.ui.CmsValuePanel;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsTabInfo;
import org.opencms.acacia.shared.CmsType;
import org.opencms.gwt.client.I_CmsHasResizeOnShow;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Renders the widgets for an in-line form.<p>
 */
public class CmsRenderer implements I_CmsEntityRenderer {

    /**
     * Calls resize on tab selection on the tabs child hierarchy.<p>
     */
    protected class TabSelectionHandler implements SelectionHandler<Integer> {

        /** The tabbed panel. */
        CmsTabbedPanel<FlowPanel> m_tabsPanel;

        /**
         * Constructor.<p>
         *
         * @param tabsPanel the tabbed panel
         */
        TabSelectionHandler(CmsTabbedPanel<FlowPanel> tabsPanel) {

            m_tabsPanel = tabsPanel;
        }

        /**
         * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
         */
        public void onSelection(final SelectionEvent<Integer> event) {

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    FlowPanel tab = m_tabsPanel.getWidget(event.getSelectedItem().intValue());
                    for (Widget w : tab) {
                        if (w instanceof I_CmsHasResizeOnShow) {
                            ((I_CmsHasResizeOnShow)w).resizeOnShow();
                        }
                    }
                }
            });

        }
    }

    /**
     * Handles the size of a tabbed panel.<p>
     */
    protected class TabSizeHandler implements SelectionHandler<Integer>, ValueChangeHandler<CmsEntity>, ResizeHandler {

        /** The context panel. */
        private Panel m_context;

        /** The tabbed panel. */
        private CmsTabbedPanel<FlowPanel> m_tabbedPanel;

        /**
         * Constructor.<p>
         *
         * @param tabbedPanel the tabbed panel
         * @param context the context panel
         */
        public TabSizeHandler(CmsTabbedPanel<FlowPanel> tabbedPanel, Panel context) {

            m_tabbedPanel = tabbedPanel;
            m_context = context;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
         */
        public void onResize(ResizeEvent event) {

            triggerHeightAdjustment();
        }

        /**
         * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
         */
        public void onSelection(SelectionEvent<Integer> event) {

            triggerHeightAdjustment();
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<CmsEntity> event) {

            triggerHeightAdjustment();
        }

        /**
         * Adjusts the tabbed panel height to the height of the current tab content.<p>
         */
        protected void adjustContextHeight() {

            int tabIndex = m_tabbedPanel.getSelectedIndex();
            FlowPanel tab = m_tabbedPanel.getWidget(tabIndex);
            int height = CmsPositionBean.getInnerDimensions(tab.getElement()).getHeight()
                + m_tabbedPanel.getTabBarHeight();

            m_context.getElement().getStyle().setHeight(22 + height, Unit.PX);
        }

        /**
         * Triggers the tab panel height adjustment scheduled after the browsers event loop.<p>
         */
        private void triggerHeightAdjustment() {

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    adjustContextHeight();
                }
            });
        }
    }

    /**
     * The widget value change handler.<p>
     */
    protected class WidgetChangeHandler implements ValueChangeHandler<String> {

        /** The attribute handler. */
        private CmsAttributeHandler m_attributeHandler;

        /** The value index. */
        private int m_valueIndex;

        /**
         * Constructor.<p>
         *
         * @param attributeHandler the attribute handler
         * @param valueIndex the value index, only relevant for in-line rendering
         */
        protected WidgetChangeHandler(CmsAttributeHandler attributeHandler, int valueIndex) {

            m_attributeHandler = attributeHandler;
            m_valueIndex = valueIndex;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            m_attributeHandler.handleValueChange(m_valueIndex, event.getValue());
        }
    }

    /** The entity CSS class. */
    public static final String ENTITY_CLASS = I_CmsLayoutBundle.INSTANCE.form().entity();

    /** The attribute label CSS class. */
    public static final String LABEL_CLASS = I_CmsLayoutBundle.INSTANCE.form().label();

    /** The renderer name. */
    public static final String RENDERER_NAME = "default";

    /** The widget holder CSS class. */
    public static final String WIDGET_HOLDER_CLASS = I_CmsLayoutBundle.INSTANCE.form().widgetHolder();

    /** The entity back end instance. */
    I_CmsEntityBackend m_entityBackEnd;

    /** The widget service. */
    I_CmsWidgetService m_widgetService;

    /**
     * Constructor.<p>
     *
     * @param entityBackEnd the entity back end instance
     * @param widgetService the widget service
     */
    public CmsRenderer(I_CmsEntityBackend entityBackEnd, I_CmsWidgetService widgetService) {

        m_entityBackEnd = entityBackEnd;
        m_widgetService = widgetService;
    }

    /**
     * Gets the paths of nested choice attributes starting from a given type.<p>
     *
     * @param attributeType the type from which to start
     * @param startingAtChoiceAttribute true if the attribute is a synthetic CHOICE_ATTRIBUTE
     *
     * @return the list of nested choice attribute name paths
     */
    public static List<CmsChoiceMenuEntryBean> getChoiceEntries(
        CmsType attributeType,
        boolean startingAtChoiceAttribute) {

        CmsChoiceMenuEntryBean rootEntry = new CmsChoiceMenuEntryBean(null);
        collectChoiceEntries(attributeType, startingAtChoiceAttribute, rootEntry);
        return rootEntry.getChildren();
    }

    /**
     * Sets the attribute choices if present.<p>
     *
     * @param widgetService the widget service to use
     * @param valueWidget the value widget
     * @param attributeType the attribute type
     */
    public static void setAttributeChoice(
        I_CmsWidgetService widgetService,
        CmsAttributeValueView valueWidget,
        CmsType attributeType) {

        if (attributeType.isChoice()) {
            List<CmsChoiceMenuEntryBean> menuEntries = getChoiceEntries(attributeType, false);
            for (CmsChoiceMenuEntryBean menuEntry : menuEntries) {
                valueWidget.addChoice(widgetService, menuEntry);
            }
        }
    }

    /**
     * Recursive helper method to create a tree structure of choice menu entries for a choice type.<p>
     *
     * @param startType the type from which to start
     * @param startingAtChoiceAttribute true if the recursion starts at a synthetic choice attribute
     * @param currentEntry the current menu entry bean
     */
    private static void collectChoiceEntries(
        CmsType startType,
        boolean startingAtChoiceAttribute,
        CmsChoiceMenuEntryBean currentEntry) {

        if (startingAtChoiceAttribute || startType.isChoice()) {
            CmsType choiceType = startingAtChoiceAttribute
            ? startType
            : startType.getAttributeType(CmsType.CHOICE_ATTRIBUTE_NAME);
            for (String choiceName : choiceType.getAttributeNames()) {
                CmsChoiceMenuEntryBean subEntry = currentEntry.addChild(choiceName);
                CmsType includedType = choiceType.getAttributeType(choiceName);
                collectChoiceEntries(includedType, false, subEntry);
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#configure(java.lang.String)
     */
    public I_CmsEntityRenderer configure(String configuration) {

        return this;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#getName()
     */
    public String getName() {

        return RENDERER_NAME;
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderAttributeValue(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.CmsAttributeHandler, int, com.google.gwt.user.client.ui.Panel)
     */
    public void renderAttributeValue(
        CmsEntity parentEntity,
        CmsAttributeHandler attributeHandler,
        int attributeIndex,
        Panel context) {

        CmsType entityType = m_entityBackEnd.getType(parentEntity.getTypeName());
        CmsType attributeType = attributeHandler.getAttributeType();
        String attributeName = attributeHandler.getAttributeName();
        int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
        CmsEntityAttribute attribute = parentEntity.getAttribute(attributeName);
        if ((attribute == null) && (minOccurrence > 0)) {
            attribute = createEmptyAttribute(parentEntity, attributeName, attributeHandler, minOccurrence);
        }

        CmsValuePanel attributeElement = new CmsValuePanel();
        context.add(attributeElement);
        context.addStyleName(ENTITY_CLASS);
        CmsRootHandler parentHandler = new CmsRootHandler();
        parentHandler.ensureHandlers(attributeIndex);
        parentHandler.setHandler(attributeIndex, attributeName, attributeHandler);
        attributeHandler.setSingleValueIndex(attributeIndex);
        String label = m_widgetService.getAttributeLabel(attributeName);
        String help = m_widgetService.getAttributeHelp(attributeName);
        if (attribute != null) {
            I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            CmsAttributeValueView valueWidget = new CmsAttributeValueView(attributeHandler, label, help);
            if (attributeType.isChoice() && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                valueWidget.setCollapsed(true);
            }
            attributeElement.add(valueWidget);
            if (attribute.isSimpleValue()) {
                valueWidget.setValueWidget(
                    m_widgetService.getAttributeFormWidget(attributeName),
                    attribute.getSimpleValues().get(attributeIndex),
                    m_widgetService.getDefaultAttributeValue(
                        attributeName,
                        attributeHandler.getSimplePath(attributeIndex)),
                    true);
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    // widget should be displayed in compact view, using only 50% of the available width
                    valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                } else {
                    if (m_widgetService.isDisplaySingleLine(attributeName)) {
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                }
            } else {
                valueWidget.setValueEntity(renderer, attribute.getComplexValues().get(attributeIndex));
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_NESTED);
                }
            }
            setAttributeChoice(valueWidget, attributeType);
        }
        attributeHandler.updateButtonVisisbility();
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderForm(org.opencms.acacia.shared.CmsEntity, java.util.List, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public CmsTabbedPanel<FlowPanel> renderForm(
        CmsEntity entity,
        List<CmsTabInfo> tabInfos,
        Panel context,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex) {

        if ((tabInfos == null) || (tabInfos.size() < 2)) {
            if ((tabInfos != null) && (tabInfos.size() == 1)) {
                renderDescription(tabInfos.get(0), context);
            }
            renderForm(entity, context, parentHandler, attributeIndex);
            return null;
        } else {

            context.getElement().getStyle().setHeight(600, Unit.PX);
            context.getElement().setAttribute("typeof", entity.getTypeName());
            context.getElement().setAttribute("about", entity.getId());
            context.getElement().getStyle().setPadding(0, Unit.PX);
            CmsTabbedPanel<FlowPanel> tabbedPanel = new CmsTabbedPanel<FlowPanel>(CmsTabbedPanelStyle.classicTabs);
            tabbedPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().wrapTabs());
            final TabSizeHandler tabSizeHandler = new TabSizeHandler(tabbedPanel, context);
            tabbedPanel.addSelectionHandler(tabSizeHandler);
            entity.addValueChangeHandler(tabSizeHandler);
            // adjust the tab panel height after a delay as some widgets may need time to initialize
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                private int m_counter;

                /**
                 * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
                 */
                public boolean execute() {

                    tabSizeHandler.adjustContextHeight();
                    m_counter++;
                    return m_counter < 6;
                }
            }, 200);
            CmsAttributeHandler.setResizeHandler(tabSizeHandler);
            tabbedPanel.addSelectionHandler(new TabSelectionHandler(tabbedPanel));
            tabbedPanel.getElement().getStyle().setBorderWidth(0, Unit.PX);
            Iterator<CmsTabInfo> tabIt = tabInfos.iterator();
            CmsTabInfo currentTab = tabIt.next();
            CmsTabInfo nextTab = tabIt.next();
            FlowPanel tabPanel = createTab();
            renderDescription(currentTab, tabPanel);
            tabbedPanel.addNamed(tabPanel, currentTab.getTabName(), currentTab.getTabId());
            CmsType entityType = m_entityBackEnd.getType(entity.getTypeName());
            List<String> attributeNames = entityType.getAttributeNames();
            CmsAttributeValueView lastCompactView = null;
            boolean collapsed = currentTab.isCollapsed()
                && ((nextTab != null) && attributeNames.get(1).endsWith("/" + nextTab.getStartName()));
            for (final String attributeName : attributeNames) {
                if (!m_widgetService.isVisible(attributeName)) {
                    // attributes configured as invisible, will be skipped
                    continue;
                }
                if ((nextTab != null) && attributeName.endsWith("/" + nextTab.getStartName())) {
                    currentTab = nextTab;
                    nextTab = tabIt.hasNext() ? tabIt.next() : null;
                    tabPanel = createTab();
                    renderDescription(currentTab, tabPanel);
                    tabbedPanel.addNamed(tabPanel, currentTab.getTabName(), currentTab.getTabId());
                    // check if the tab content may be collapsed
                    if (currentTab.isCollapsed()) {
                        int currentIndex = attributeNames.indexOf(attributeName);
                        collapsed = ((currentIndex + 1) == attributeNames.size())
                            || ((nextTab != null)
                                && attributeNames.get(currentIndex + 1).endsWith("/" + nextTab.getStartName()));
                    }
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as no following widget will occupy the second column
                        lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
                    }
                }
                CmsAttributeHandler handler = new CmsAttributeHandler(
                    m_entityBackEnd,
                    entity,
                    attributeName,
                    m_widgetService);
                parentHandler.setHandler(attributeIndex, attributeName, handler);
                CmsType attributeType = entityType.getAttributeType(attributeName);
                int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
                CmsEntityAttribute attribute = entity.getAttribute(attributeName);
                // only single complex values may be collapsed
                if (collapsed
                    && (attribute != null)
                    && !attributeType.isSimpleType()
                    && (minOccurrence == 1)
                    && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                    I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(
                        attributeName,
                        attributeType);
                    renderer.renderForm(attribute.getComplexValue(), tabPanel, handler, 0);
                } else {
                    CmsValuePanel attributeElement = new CmsValuePanel();
                    tabPanel.add(attributeElement);
                    if ((attribute == null) && (minOccurrence > 0)) {
                        attribute = createEmptyAttribute(entity, attributeName, handler, minOccurrence);
                    }
                    lastCompactView = renderAttribute(
                        entityType,
                        attributeType,
                        attribute,
                        handler,
                        attributeElement,
                        attributeName,
                        lastCompactView);
                }
                handler.updateButtonVisisbility();
            }
            if (lastCompactView != null) {
                // previous widget was set to first column mode,
                // revert that as no following widget will occupy the second column
                lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
            }
            context.add(tabbedPanel);
            return tabbedPanel;
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderForm(org.opencms.acacia.shared.CmsEntity, com.google.gwt.user.client.ui.Panel, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public void renderForm(CmsEntity entity, Panel context, I_CmsAttributeHandler parentHandler, int attributeIndex) {

        context.addStyleName(ENTITY_CLASS);
        context.getElement().setAttribute("typeof", entity.getTypeName());
        context.getElement().setAttribute("about", entity.getId());
        CmsType entityType = m_entityBackEnd.getType(entity.getTypeName());
        CmsAttributeValueView lastCompactView = null;
        if (entityType.isChoice()) {
            CmsEntityAttribute attribute = entity.getAttribute(CmsType.CHOICE_ATTRIBUTE_NAME);
            CmsAttributeHandler handler = new CmsAttributeHandler(
                m_entityBackEnd,
                entity,
                CmsType.CHOICE_ATTRIBUTE_NAME,
                m_widgetService);
            parentHandler.setHandler(attributeIndex, CmsType.CHOICE_ATTRIBUTE_NAME, handler);
            CmsValuePanel attributeElement = new CmsValuePanel();
            if ((attribute != null) && attribute.isComplexValue()) {
                for (CmsEntity choiceEntity : attribute.getComplexValues()) {
                    CmsType choiceType = m_entityBackEnd.getType(choiceEntity.getTypeName());
                    List<CmsEntityAttribute> choiceAttributes = choiceEntity.getAttributes();
                    CmsEntityAttribute choiceAttribute = choiceAttributes.get(0);
                    CmsType attributeType = choiceType.getAttributeType(choiceAttribute.getAttributeName());
                    I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(
                        choiceAttribute.getAttributeName(),
                        attributeType);
                    String label = m_widgetService.getAttributeLabel(choiceAttribute.getAttributeName());
                    String help = m_widgetService.getAttributeHelp(choiceAttribute.getAttributeName());
                    context.add(attributeElement);
                    CmsAttributeValueView valueWidget = new CmsAttributeValueView(handler, label, help);
                    attributeElement.add(valueWidget);
                    if (choiceAttribute.isSimpleValue()) {
                        valueWidget.setValueWidget(
                            m_widgetService.getAttributeFormWidget(choiceAttribute.getAttributeName()),
                            choiceAttribute.getSimpleValue(),
                            m_widgetService.getDefaultAttributeValue(
                                choiceAttribute.getAttributeName(),
                                handler.getSimplePath(attributeIndex)),
                            true);
                        if (m_widgetService.isDisplaySingleLine(choiceAttribute.getAttributeName())) {
                            valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                        }
                    } else {
                        valueWidget.setValueEntity(renderer, choiceAttribute.getComplexValue());
                        if (m_widgetService.isDisplayCompact(choiceAttribute.getAttributeName())) {
                            valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_NESTED);
                        }
                    }
                    setAttributeChoice(valueWidget, entityType);
                }
            }
            handler.updateButtonVisisbility();
        } else {
            List<String> attributeNames = entityType.getAttributeNames();
            for (String attributeName : attributeNames) {
                if (!m_widgetService.isVisible(attributeName)) {
                    // attributes configured as invisible, will be skipped
                    continue;
                }
                int minOccurrence = entityType.getAttributeMinOccurrence(attributeName);
                CmsEntityAttribute attribute = entity.getAttribute(attributeName);
                CmsAttributeHandler handler = new CmsAttributeHandler(
                    m_entityBackEnd,
                    entity,
                    attributeName,
                    m_widgetService);
                parentHandler.setHandler(attributeIndex, attributeName, handler);
                if ((attribute == null) && (minOccurrence > 0)) {
                    attribute = createEmptyAttribute(entity, attributeName, handler, minOccurrence);
                }
                CmsType attributeType = entityType.getAttributeType(attributeName);
                CmsValuePanel attributeElement = new CmsValuePanel();
                context.add(attributeElement);
                lastCompactView = renderAttribute(
                    entityType,
                    attributeType,
                    attribute,
                    handler,
                    attributeElement,
                    attributeName,
                    lastCompactView);
            }
        }
        if (lastCompactView != null) {
            // previous widget was set to first column mode,
            // revert that as no following widget will occupy the second column
            lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler, org.opencms.acacia.client.I_CmsAttributeHandler, int)
     */
    public void renderInline(
        CmsEntity entity,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex) {

        CmsType entityType = m_entityBackEnd.getType(entity.getTypeName());
        List<String> attributeNames = entityType.getAttributeNames();
        for (String attributeName : attributeNames) {
            CmsType attributeType = entityType.getAttributeType(attributeName);
            I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            renderer.renderInline(
                entity,
                attributeName,
                formParent,
                updateHandler,
                parentHandler,
                attributeIndex,
                entityType.getAttributeMinOccurrence(attributeName),
                entityType.getAttributeMaxOccurrence(attributeName));
        }
    }

    /**
     * @see org.opencms.acacia.client.I_CmsEntityRenderer#renderInline(org.opencms.acacia.shared.CmsEntity, java.lang.String, org.opencms.acacia.client.I_CmsInlineFormParent, org.opencms.acacia.client.I_CmsInlineHtmlUpdateHandler, org.opencms.acacia.client.I_CmsAttributeHandler, int, int, int)
     */
    public void renderInline(
        CmsEntity parentEntity,
        String attributeName,
        I_CmsInlineFormParent formParent,
        I_CmsInlineHtmlUpdateHandler updateHandler,
        I_CmsAttributeHandler parentHandler,
        int attributeIndex,
        int minOccurrence,
        int maxOccurrence) {

        CmsEntityAttribute attribute = parentEntity.getAttribute(attributeName);
        CmsAttributeHandler handler = new CmsAttributeHandler(
            m_entityBackEnd,
            parentEntity,
            attributeName,
            m_widgetService);
        parentHandler.setHandler(attributeIndex, attributeName, handler);
        if (attribute != null) {
            List<Element> elements = m_entityBackEnd.getAttributeElements(
                parentEntity,
                attributeName,
                formParent.getElement());
            if (!elements.isEmpty()) {
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    I_CmsEditWidget widget = m_widgetService.getAttributeInlineWidget(attributeName, element);
                    if (attribute.isSimpleValue() && (widget != null)) {
                        Element tempSpan = DOM.createSpan();
                        tempSpan.setInnerHTML(attribute.getSimpleValues().get(i));
                        String value = tempSpan.getInnerHTML().trim();
                        // verify the current value equals the element content
                        String innerHtml = element.getInnerHTML().trim();
                        if (innerHtml.equals(value)) {
                            widget.addValueChangeHandler(new WidgetChangeHandler(handler, i));
                            formParent.adoptWidget(widget);
                        } else {
                            CmsInlineEntityWidget.createWidgetForEntity(
                                element,
                                formParent,
                                parentEntity,
                                handler,
                                i,
                                updateHandler,
                                m_widgetService);
                        }
                    } else {
                        CmsInlineEntityWidget.createWidgetForEntity(
                            element,
                            formParent,
                            parentEntity,
                            handler,
                            i,
                            updateHandler,
                            m_widgetService);
                    }
                }
            }
            if (attribute.isComplexValue()) {
                int index = 0;
                for (CmsEntity entity : attribute.getComplexValues()) {
                    renderInline(entity, formParent, updateHandler, handler, index);
                    index++;
                }
            }
        } else {
            List<Element> elements = m_entityBackEnd.getAttributeElements(
                parentEntity,
                attributeName,
                formParent.getElement());
            if (!elements.isEmpty() && (elements.size() == 1)) {
                CmsInlineEntityWidget.createWidgetForEntity(
                    elements.get(0),
                    formParent,
                    parentEntity,
                    handler,
                    -1,
                    updateHandler,
                    m_widgetService);
            }
        }

    }

    /**
     * Creates an empty attribute.<p>
     *
     * @param parentEntity the parent entity
     * @param attributeName the attribute name
     * @param handler the attribute handler
     * @param minOccurrence the minimum occurrence of the attribute
     *
     * @return the entity attribute
     */
    protected CmsEntityAttribute createEmptyAttribute(
        CmsEntity parentEntity,
        String attributeName,
        CmsAttributeHandler handler,
        int minOccurrence) {

        CmsEntityAttribute result = null;
        CmsType attributeType = m_entityBackEnd.getType(parentEntity.getTypeName()).getAttributeType(attributeName);
        if (attributeType.isSimpleType()) {
            for (int i = 0; i < minOccurrence; i++) {
                parentEntity.addAttributeValue(
                    attributeName,
                    m_widgetService.getDefaultAttributeValue(attributeName, handler.getSimplePath(i)));
            }
            result = parentEntity.getAttribute(attributeName);
        } else {
            for (int i = 0; i < minOccurrence; i++) {
                parentEntity.addAttributeValue(
                    attributeName,
                    m_entityBackEnd.createEntity(null, attributeType.getId()));
            }
            result = parentEntity.getAttribute(attributeName);
        }
        return result;
    }

    /**
     * Creates a tab.<p>
     *
     * @return the created tab
     */
    private FlowPanel createTab() {

        FlowPanel tabPanel;
        tabPanel = new FlowPanel();
        tabPanel.addStyleName(ENTITY_CLASS);
        tabPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.form().formParent());
        tabPanel.getElement().getStyle().setMargin(0, Unit.PX);
        return tabPanel;
    }

    /**
     * Renders a single attribute.<p>
     *
     * @param entityType the type of the entity containing the attribute
     * @param attributeType the attribute type
     * @param attribute the attribute, or null if not set
     * @param handler the attribute handler
     * @param attributeElement the attribute parent element
     * @param attributeName the attribute name
     * @param lastCompactView the previous attribute view that was rendered in compact mode if present
     *
     * @return the last attribute view that was rendered in compact mode if present
     */
    private CmsAttributeValueView renderAttribute(
        CmsType entityType,
        CmsType attributeType,
        CmsEntityAttribute attribute,
        CmsAttributeHandler handler,
        CmsValuePanel attributeElement,
        String attributeName,
        CmsAttributeValueView lastCompactView) {

        String label = m_widgetService.getAttributeLabel(attributeName);
        String help = m_widgetService.getAttributeHelp(attributeName);
        if (attribute != null) {
            I_CmsEntityRenderer renderer = m_widgetService.getRendererForAttribute(attributeName, attributeType);
            for (int i = 0; i < attribute.getValueCount(); i++) {
                CmsAttributeValueView valueWidget = new CmsAttributeValueView(handler, label, help);
                if (attributeType.isChoice() && (entityType.getAttributeMaxOccurrence(attributeName) == 1)) {
                    valueWidget.setCollapsed(true);
                }
                attributeElement.add(valueWidget);
                if (attribute.isSimpleValue()) {
                    valueWidget.setValueWidget(
                        m_widgetService.getAttributeFormWidget(attributeName),
                        attribute.getSimpleValues().get(i),
                        m_widgetService.getDefaultAttributeValue(attributeName, handler.getSimplePath(i)),
                        true);
                    // check for compact view setting
                    if (m_widgetService.isDisplayCompact(attributeName)) {
                        // widget should be displayed in compact view, using only 50% of the available width
                        if (lastCompactView == null) {
                            // set mode to first column
                            valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                            lastCompactView = valueWidget;
                        } else {
                            // previous widget is displayed as first column, set second column mode
                            valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SECOND_COLUMN);
                            lastCompactView = null;
                        }
                    } else {
                        if (lastCompactView != null) {
                            // previous widget was set to first column mode,
                            // revert that as the current widget will be displayed in a new line
                            lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
                            lastCompactView = null;
                        }
                        if (m_widgetService.isDisplaySingleLine(attributeName)) {
                            valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                        }
                    }
                } else {
                    valueWidget.setValueEntity(renderer, attribute.getComplexValues().get(i));
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as the current widget will be displayed in a new line
                        lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
                        lastCompactView = null;
                    }
                    if (m_widgetService.isDisplayCompact(attributeName)) {
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_NESTED);
                    }
                }
                setAttributeChoice(valueWidget, attributeType);
            }
        } else {
            CmsAttributeValueView valueWidget = new CmsAttributeValueView(handler, label, help); 
            attributeElement.add(valueWidget);
            if (attributeType.isSimpleType()) {
                // create a deactivated widget, to add the attribute on click
                valueWidget.setValueWidget(
                    m_widgetService.getAttributeFormWidget(attributeName),
                    "",
                    m_widgetService.getDefaultAttributeValue(attributeName, handler.getSimplePath(0)),
                    false);
                // check for compact view setting
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    // widget should be displayed in compact view, using only 50% of the available width
                    if (lastCompactView == null) {
                        // set mode to first column
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_FIRST_COLUMN);
                        lastCompactView = valueWidget;
                    } else {
                        // previous widget is displayed as first column, set second column mode
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SECOND_COLUMN);
                        lastCompactView = null;
                    }
                } else {
                    if (lastCompactView != null) {
                        // previous widget was set to first column mode,
                        // revert that as the current widget will be displayed in a new line
                        lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
                        lastCompactView = null;
                    }
                    if (m_widgetService.isDisplaySingleLine(attributeName)) {
                        valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_SINGLE_LINE);
                    }
                }
            } else {
                if (lastCompactView != null) {
                    // previous widget was set to first column mode,
                    // revert that as the current widget will be displayed in a new line
                    lastCompactView.setCompactMode(CmsAttributeValueView.COMPACT_MODE_WIDE);
                    lastCompactView = null;
                }
                if (m_widgetService.isDisplayCompact(attributeName)) {
                    valueWidget.setCompactMode(CmsAttributeValueView.COMPACT_MODE_NESTED);
                }
            }
            setAttributeChoice(valueWidget, attributeType);
        }
        handler.updateButtonVisisbility();
        return lastCompactView;
    }

    /**
     * Renders the tab description in a given panel.<p>
     * @param tabInfo the tab info object
     *
     * @param descriptionParent the panel in which to render the tab description
     */
    private void renderDescription(CmsTabInfo tabInfo, Panel descriptionParent) {

        if (tabInfo.getDescription() != null) {
            HTML descriptionLabel = new HTML();
            descriptionLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.form().tabDescription());
            descriptionLabel.setHTML(tabInfo.getDescription());
            descriptionParent.add(descriptionLabel);
        }
    }

    /**
     * Sets the attribute choices if present.<p>
     *
     * @param valueWidget the value widget
     * @param attributeType the attribute type
     */
    private void setAttributeChoice(CmsAttributeValueView valueWidget, CmsType attributeType) {

        setAttributeChoice(m_widgetService, valueWidget, attributeType);
    }
}

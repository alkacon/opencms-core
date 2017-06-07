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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsTextArea;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A tabbed form field container widget.<p>
 *
 * @since 8.0.0
 */
public class CmsPropertyPanel extends A_CmsFormFieldPanel {

    /** Layout data key. */
    public static final String LD_DISPLAY_VALUE = "displayValue";

    /** Layout data key. */
    public static final String LD_GROUP = "group";

    /** Layout data key. */
    public static final String LD_PROPERTY = "property";

    /** Tab id for the "individual" tab. */
    public static final String TAB_INDIVIDUAL = "individual";

    /** Tab id for the "shared" tab. */
    public static final String TAB_SHARED = "shared";

    /** Tab id for the "simple" tab. */
    public static final String TAB_SIMPLE = "simple";

    /** The tab panel. */
    protected CmsTabbedPanel<CmsScrollPanel> m_tabPanel = new CmsTabbedPanel<CmsScrollPanel>();

    /** Multimap of fields by field group. */
    private Multimap<String, I_CmsFormField> m_fieldsByGroup = ArrayListMultimap.create();

    /** Map from group/tab names to corresponding panels. */
    private Map<String, Panel> m_groups = new LinkedHashMap<String, Panel>();

    /** Set of fields which should be displayed at the top of the "individual" tab. */
    private Set<String> m_individualDisplay = Sets.newHashSet();

    /** The "individual" tab. */
    private FlowPanel m_individualTab = new FlowPanel();

    /** The tab wrapper for the individual tab. */
    private FlowPanel m_individualTabWrapper = new FlowPanel();

    /** Name of property which should be focused, used while rendering the extended tabs. */
    private String m_markedProperty;

    /** Set of fields which should be displayed at the top of the "shared" tab. */
    private Set<String> m_sharedDisplay = Sets.newHashSet();

    /** The "shared" tab. */
    private FlowPanel m_sharedTab = new FlowPanel();

    /** The tab wrapper for the shared tab. */
    private FlowPanel m_sharedTabWrapper = new FlowPanel();

    /** True if the "shared" tab should be shown. */
    private boolean m_showShared;

    /** The "simple" tab. */
    private FlowPanel m_simpleTab = new FlowPanel();

    /** The tab wrapper for the simple tab. */
    private FlowPanel m_simpleTabWrapper = new FlowPanel();

    /**
     * Creates a new instance.<p>
     *
     * @param showShared true if the "shared" tab should be shown
     * @param info the bean to use for displaying the info item
     */
    public CmsPropertyPanel(boolean showShared, CmsListInfoBean info) {

        m_infoWidget = createListItemWidget(info);
        m_simpleTabWrapper.add(m_infoWidget);
        m_simpleTabWrapper.add(m_simpleTab);
        m_simpleTab.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_simpleTab.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.propertiesCss().vfsModeSimplePropertiesBox());
        m_simpleTab.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        m_sharedTabWrapper.add(createListItemWidget(info));
        m_sharedTabWrapper.add(m_sharedTab);

        m_individualTabWrapper.add(createListItemWidget(info));
        m_individualTabWrapper.add(m_individualTab);
        m_groups.put(TAB_SIMPLE, m_simpleTab);
        m_groups.put(TAB_SHARED, m_sharedTab);
        m_groups.put(TAB_INDIVIDUAL, m_individualTab);
        CmsScrollPanel scrollPanel = GWT.create(CmsScrollPanel.class);
        scrollPanel.setWidget(m_simpleTabWrapper);
        m_tabPanel.add(scrollPanel, Messages.get().key(Messages.GUI_PROPERTY_TAB_SIMPLE_0));
        m_showShared = showShared;
        if (m_showShared) {
            scrollPanel = GWT.create(CmsScrollPanel.class);
            scrollPanel.setWidget(m_individualTabWrapper);
            m_tabPanel.add(scrollPanel, Messages.get().key(Messages.GUI_PROPERTY_TAB_STRUCTURE_0));
            scrollPanel = GWT.create(CmsScrollPanel.class);
            scrollPanel.setWidget(m_sharedTabWrapper);
            m_tabPanel.add(scrollPanel, Messages.get().key(Messages.GUI_PROPERTY_TAB_RESOURCE_0));
        } else {
            scrollPanel = GWT.create(CmsScrollPanel.class);
            scrollPanel.setWidget(m_individualTabWrapper);
            m_tabPanel.add(scrollPanel, Messages.get().key(Messages.GUI_PROPERTY_TAB_COMPLETE_0));
        }
        initWidget(m_tabPanel);
        addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.propertiesCss().propertyPanel());
        m_tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                Widget selectedTab = m_tabPanel.getWidget(event.getSelectedItem().intValue());
                if (selectedTab instanceof I_CmsDescendantResizeHandler) {
                    ((I_CmsDescendantResizeHandler)selectedTab).onResizeDescendant();
                }
            }
        });
    }

    /**
     * Adds the {@link BeforeSelectionHandler} for the tab panel.<p>
     *
     * @param handler the pre-selection handler
     */
    public void addBeforeSelectionHandler(BeforeSelectionHandler<Integer> handler) {

        m_tabPanel.addBeforeSelectionHandler(handler);
    }

    /**
     * Clears the tab with the given id.<p>
     *
     * @param tabId the id of the tab to clear
     */
    public void clearTab(String tabId) {

        m_groups.get(tabId).clear();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#getDefaultGroup()
     */
    @Override
    public String getDefaultGroup() {

        return TAB_SIMPLE;
    }

    /**
     * Renders a extended tab.<p>
     *
     * @param fields the fields to add
     * @param tab the tab
     */
    public void renderExtendedTab(Collection<I_CmsFormField> fields, FlowPanel tab) {

        List<CmsFieldSet> result = new ArrayList<CmsFieldSet>();

        tab.clear();

        String used = Messages.get().key(Messages.GUI_PROPERTY_BLOCK_USED_0);
        CmsFieldSet usedFieldSet = new CmsFieldSet();
        usedFieldSet.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        usedFieldSet.setLegend(used);
        usedFieldSet.setAnimationDuration(50);

        String unused = Messages.get().key(Messages.GUI_PROPERTY_BLOCK_UNUSED_0);
        CmsFieldSet unusedFieldSet = new CmsFieldSet();
        unusedFieldSet.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        unusedFieldSet.setOpen(false);
        unusedFieldSet.setLegend(unused);
        unusedFieldSet.setAnimationDuration(50);
        boolean reopen = false;
        final String currentMarkedProperty = m_markedProperty;
        m_markedProperty = null;
        for (I_CmsFormField field : fields) {
            if (isTop(field)) {
                usedFieldSet.addContent(createRow(field));
            } else {
                if ((currentMarkedProperty != null)
                    && field.getLayoutData().get(LD_PROPERTY).equals(currentMarkedProperty)) {
                    reopen = true;
                }
                unusedFieldSet.addContent(createRow(field));
            }
        }
        if (reopen) {
            unusedFieldSet.setOpen(true);
        }

        if (usedFieldSet.getWidgetCount() > 0) {
            result.add(usedFieldSet);
        }
        if (unusedFieldSet.getWidgetCount() > 0) {
            result.add(unusedFieldSet);
        }

        Iterator<CmsFieldSet> iter = result.iterator();
        while (iter.hasNext()) {
            CmsFieldSet fieldSet = iter.next();
            if (iter.hasNext()) {
                fieldSet.getElement().getStyle().setMarginTop(9, Unit.PX);
            } else {
                fieldSet.getElement().getStyle().setMarginTop(15, Unit.PX);
            }
            tab.add(fieldSet);
        }
        CmsDomUtil.resizeAncestor(tab.getParent());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#renderFields(java.util.Collection)
     */
    @Override
    public void renderFields(Collection<I_CmsFormField> fields) {

        m_fieldsByGroup = getFieldsByGroup(fields);
        Collection<I_CmsFormField> simpleTabFields = m_fieldsByGroup.get(TAB_SIMPLE);
        Collection<I_CmsFormField> individualTabFields = m_fieldsByGroup.get(TAB_INDIVIDUAL);
        Collection<I_CmsFormField> sharedTabfields = m_fieldsByGroup.get(TAB_SHARED);

        // process simple tab
        renderSimpleTab(simpleTabFields);

        // process individual tab
        m_individualDisplay = preprocessFields(individualTabFields);
        // process shared tab
        if (m_showShared) {
            m_sharedDisplay = preprocessFields(sharedTabfields);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#rerenderFields(java.lang.String, java.util.Collection)
     */
    @Override
    public void rerenderFields(String tab, Collection<I_CmsFormField> fields) {

        m_fieldsByGroup.removeAll(tab);
        m_fieldsByGroup.putAll(tab, fields);

        m_individualDisplay = preprocessFields(m_fieldsByGroup.get(TAB_INDIVIDUAL));
        m_sharedDisplay = preprocessFields(m_fieldsByGroup.get(TAB_SHARED));

        if (tab.equals(TAB_SIMPLE)) {
            m_simpleTab.clear();
            renderSimpleTab(fields);
        } else {
            if (tab.equals(TAB_INDIVIDUAL)) {
                renderExtendedTab(fields, m_individualTab);
            } else if (tab.equals(TAB_SHARED)) {
                renderExtendedTab(fields, m_sharedTab);
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        clientWidth -= 12;
        storeTruncation(textMetricsKey, clientWidth);
        truncatePanel(m_individualTab, textMetricsKey, clientWidth);
        truncatePanel(m_individualTabWrapper, textMetricsKey, clientWidth);
        truncatePanel(m_simpleTab, textMetricsKey, clientWidth);
        truncatePanel(m_simpleTabWrapper, textMetricsKey, clientWidth);
        truncatePanel(m_sharedTab, textMetricsKey, clientWidth);
        truncatePanel(m_sharedTabWrapper, textMetricsKey, clientWidth);
    }

    /**
     * Tries to restore the active field data.<p>
     *
     * @param fieldDataToBeRestored the field data which should be restored (may be null, in which case this method does nothing)
     */
    public void tryToRestoreFieldData(CmsActiveFieldData fieldDataToBeRestored) {

        if (fieldDataToBeRestored == null) {
            return;
        }
        m_markedProperty = null;
        final String propName = fieldDataToBeRestored.getProperty();
        final String tabName = fieldDataToBeRestored.getTab();
        m_markedProperty = propName;
        final int tabIndex;
        if (TAB_INDIVIDUAL.equals(tabName)) {
            tabIndex = 1;
        } else if (TAB_SHARED.equals(tabName)) {
            tabIndex = 2;
        } else {
            tabIndex = 0;
        }

        Timer timer = new Timer() {

            @Override
            public void run() {

                if ((tabIndex >= 0) && (tabIndex < m_tabPanel.getTabCount())) {
                    I_CmsFormField markedField = null;
                    m_tabPanel.selectTab(tabIndex);
                    @SuppressWarnings("synthetic-access")
                    Collection<I_CmsFormField> fieldsForTab = m_fieldsByGroup.get(tabName);
                    if ((fieldsForTab != null) && !fieldsForTab.isEmpty()) {
                        for (I_CmsFormField currentField : fieldsForTab) {
                            if (currentField.getLayoutData().get(LD_PROPERTY).equals(propName)) {
                                markedField = currentField;
                                break;
                            }
                        }
                    }
                    if (markedField != null) {
                        final I_CmsFormField constMarkedField = markedField;
                        Timer timer2 = new Timer() {

                            @Override
                            @SuppressWarnings("synthetic-access")
                            public void run() {

                                focusField(constMarkedField);
                            }

                        };
                        timer2.schedule(1);
                    }
                }

            }

            private void focusField(final I_CmsFormField constMarkedField) {

                I_CmsFormWidget widget = constMarkedField.getWidget();
                if (widget instanceof CmsTextBox) {
                    CmsTextBox box = (CmsTextBox)widget;
                    box.selectAll();
                    box.setFocus(true);
                } else if (widget instanceof CmsTextArea) {
                    CmsTextArea textarea = ((CmsTextArea)widget);
                    textarea.selectAll();
                    textarea.setFocus(true);
                }
            }
        };
        timer.schedule(1);

    }

    /**
     * Creates a list item widget from a list info bean.<p>
     *
     * @param info the list info bean
     *
     * @return the list item widget
     */
    protected CmsListItemWidget createListItemWidget(CmsListInfoBean info) {

        CmsListItemWidget result = new CmsListItemWidget(info);
        result.truncate(CmsInfoBoxFormFieldPanel.TM_INFOBOX, CmsFormDialog.STANDARD_DIALOG_WIDTH - 50);
        return result;
    }

    /**
     * Returns the tabbed panel.<p>
     *
     * @return the tabbed panel
     */
    protected CmsTabbedPanel<CmsScrollPanel> getTabPanel() {

        return m_tabPanel;
    }

    /**
     * Partitions a collection of fields by group.<p>
     *
     * @param fields the collection of fields
     *
     * @return a multimap from groups to form fields
     */
    private Multimap<String, I_CmsFormField> getFieldsByGroup(Collection<I_CmsFormField> fields) {

        Multimap<String, I_CmsFormField> result = ArrayListMultimap.create();
        for (I_CmsFormField field : fields) {
            String group = field.getLayoutData().get(LD_GROUP);
            result.put(group, field);
        }
        return result;
    }

    /**
     * Returns true if the field should be displayed at the top of both the "individual" and "shared" tabs.<p>
     *
     * @param field the field to test
     *
     * @return true if the field should be displayed at the top
     */
    private boolean isTop(I_CmsFormField field) {

        String propName = field.getLayoutData().get(LD_PROPERTY);
        return m_individualDisplay.contains(propName) || m_sharedDisplay.contains(propName);
    }

    /**
     * Preprocesses the fields to find out which fields need to displayed at the top/bottom later.<p>
     *
     * @param fields the fields
     *
     * @return the set of property names of the preprocessed fields
     */
    private Set<String> preprocessFields(Collection<I_CmsFormField> fields) {

        Set<String> displaySet = Sets.newHashSet();
        for (I_CmsFormField field : fields) {
            boolean hasValue = !CmsStringUtil.isEmpty(field.getWidget().getApparentValue());
            if (hasValue || Boolean.TRUE.toString().equals(field.getLayoutData().get(LD_DISPLAY_VALUE))) {
                String propName = field.getLayoutData().get(LD_PROPERTY);
                displaySet.add(propName);
            }
        }
        return displaySet;
    }

    /**
     * Renders the simple tab.<p>
     *
     * @param fields the fields to render
     */
    private void renderSimpleTab(Collection<I_CmsFormField> fields) {

        for (I_CmsFormField field : fields) {
            m_simpleTab.add(createRow(field));
        }
        CmsDomUtil.resizeAncestor(m_simpleTab.getParent());
    }
}

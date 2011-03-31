/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsExtendedPropertyPanel.java,v $
 * Date   : $Date: 2011/03/31 17:48:00 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.sitemap.client.ui;

import static org.opencms.ade.sitemap.client.Messages.GUI_PROPERTY_TAB_RESOURCE_0;
import static org.opencms.ade.sitemap.client.Messages.GUI_PROPERTY_TAB_SIMPLE_0;
import static org.opencms.ade.sitemap.client.Messages.GUI_PROPERTY_TAB_STRUCTURE_0;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * A tabbed form field container widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsExtendedPropertyPanel extends A_CmsFormFieldPanel {

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

    /** The list of form fields. */
    protected List<I_CmsFormField> m_fields = Lists.newArrayList();

    /** Multimap of fields by field group. */
    private Multimap<String, I_CmsFormField> m_fieldsByGroup = ArrayListMultimap.create();

    /** Map from group/tab names to corresponding panels. */
    private Map<String, Panel> m_groups = new LinkedHashMap<String, Panel>();

    /** Set of fields which should be displayed at the top of the "individual" tab. */
    private Set<String> m_individualDisplay = Sets.newHashSet();

    /** The "individual" tab. */
    private FlowPanel m_individualTab = new FlowPanel();

    /** The tab panel . */
    private CmsTabbedPanel<FlowPanel> m_panel;

    /** Set of fields which should be displayed at the top of the "shared" tab. */
    private Set<String> m_sharedDisplay = Sets.newHashSet();

    /** The "shared" tab. */
    private FlowPanel m_sharedTab = new FlowPanel();

    /** True if the "shared" tab should be shown. */
    private boolean m_showShared;

    /** The "simple" tab. */
    private FlowPanel m_simpleTab = new FlowPanel();

    /**
     * Creates a new instance.<p>
     * 
     * @param showShared true if the "shared" tab should be shown 
     */
    public CmsExtendedPropertyPanel(boolean showShared) {

        m_panel = new CmsTabbedPanel<FlowPanel>();

        m_panel.add(m_simpleTab, Messages.get().key(GUI_PROPERTY_TAB_SIMPLE_0));
        CmsDomUtil.makeScrollable(m_simpleTab);
        CmsDomUtil.makeScrollable(m_sharedTab);
        CmsDomUtil.makeScrollable(m_individualTab);
        setBorder(m_simpleTab);
        setBorder(m_sharedTab);
        setBorder(m_individualTab);
        m_groups.put(TAB_SIMPLE, m_simpleTab);
        m_groups.put(TAB_SHARED, m_sharedTab);
        m_groups.put(TAB_INDIVIDUAL, m_individualTab);

        m_panel.add(m_individualTab, Messages.get().key(GUI_PROPERTY_TAB_STRUCTURE_0));
        m_showShared = showShared;
        if (showShared) {
            m_panel.add(m_sharedTab, Messages.get().key(GUI_PROPERTY_TAB_RESOURCE_0));
        }
        initWidget(m_panel);

        m_panel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                for (I_CmsFormField field : m_fields) {
                    I_CmsFormWidget w = field.getWidget();
                    if (w instanceof CmsTextBox) {
                        ((CmsTextBox)w).updateLayout();
                    }
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

        m_panel.addBeforeSelectionHandler(handler);
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
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#renderFields(java.util.Collection)
     */
    @Override
    public void renderFields(Collection<I_CmsFormField> fields) {

        processFields(fields);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#rerenderFields(java.lang.String, java.util.Collection)
     */
    @Override
    public void rerenderFields(String tab, Collection<I_CmsFormField> fields) {

        m_fieldsByGroup.removeAll(tab);
        m_fieldsByGroup.putAll(tab, fields);

        preprocessFields(m_fieldsByGroup.get(TAB_INDIVIDUAL), m_individualDisplay);
        preprocessFields(m_fieldsByGroup.get(TAB_SHARED), m_sharedDisplay);
        if (tab.equals(TAB_SIMPLE)) {
            m_simpleTab.clear();
            renderSimpleTab(fields);
        } else {
            if (tab.equals(TAB_INDIVIDUAL)) {
                CmsFieldSetBox box = prepareExtendedTab(m_individualTab);
                renderExtendedTab(fields, box);
            } else if (tab.equals(TAB_SHARED)) {
                CmsFieldSetBox box = prepareExtendedTab(m_sharedTab);
                renderExtendedTab(fields, box);
            }
        }
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
     * Prepares the "individual" or "shared" tab.<p>
     *
     * @param tab the tab 
     * @return the widget which will contain the fields 
     */
    private CmsFieldSetBox prepareExtendedTab(FlowPanel tab) {

        String used = Messages.get().key(Messages.GUI_PROPERTY_BLOCK_USED_0);
        String unused = Messages.get().key(Messages.GUI_PROPERTY_BLOCK_UNUSED_0);
        CmsFieldSetBox box = new CmsFieldSetBox(used, unused);
        tab.clear();
        tab.add(box);
        return box;
    }

    /**
     * Preprocesses the fields to find out which fields need to displayed at the top/bottom later.<p>
     * 
     * @param fields the fields 
     * 
     * @param displaySet the set to which the field property names should be added if the corresponding property should be display at the top.
     *  
     */
    private void preprocessFields(Collection<I_CmsFormField> fields, Set<String> displaySet) {

        displaySet.clear();
        for (I_CmsFormField field : fields) {
            boolean hasValue = !CmsStringUtil.isEmpty(field.getWidget().getApparentValue());
            if (hasValue || "true".equals(field.getLayoutData().get(LD_DISPLAY_VALUE))) {
                String propName = field.getLayoutData().get(LD_PROPERTY);
                displaySet.add(propName);
            }
        }
    }

    /**
     * Processes the fields and renders them.<p>
     * 
     * @param fields a collection of all fields to render 
     */
    private void processFields(Collection<I_CmsFormField> fields) {

        m_fieldsByGroup = getFieldsByGroup(fields);
        Collection<I_CmsFormField> fields1 = m_fieldsByGroup.get(TAB_SIMPLE);
        Collection<I_CmsFormField> fields2 = m_fieldsByGroup.get(TAB_INDIVIDUAL);
        Collection<I_CmsFormField> fields3 = m_fieldsByGroup.get(TAB_SHARED);

        m_simpleTab.clear();
        preprocessFields(fields2, m_individualDisplay);
        preprocessFields(fields3, m_sharedDisplay);

        CmsFieldSetBox individualBox = prepareExtendedTab(m_individualTab);
        CmsFieldSetBox sharedBox = prepareExtendedTab(m_sharedTab);

        renderSimpleTab(fields1);
        renderExtendedTab(fields2, individualBox);
        if (m_showShared) {
            renderExtendedTab(fields3, sharedBox);
        }
    }

    /**
     * Renders the fields for the "individual" or "shared" tab.<p>
     * 
     * @param fields the fields to render 
     * @param box the widget in which the fields should be rendered 
     */
    private void renderExtendedTab(Collection<I_CmsFormField> fields, CmsFieldSetBox box) {

        if (m_labelText != null) {
            box.setLabel(m_labelText);
        }
        for (I_CmsFormField field : fields) {
            if (isTop(field)) {
                box.addToFieldSet(0, createRow(field));
            } else {
                box.addToFieldSet(1, createRow(field));
            }
        }
    }

    /**
     * Renders the fields for the "simple" tab.<p>
     * 
     * @param fields the fields to render 
     */
    private void renderSimpleTab(Collection<I_CmsFormField> fields) {

        if (m_labelText != null) {
            Label label = new Label(m_labelText);
            label.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formInfo());
            m_simpleTab.add(label);
        }
        for (I_CmsFormField field : fields) {
            m_simpleTab.add(createRow(field));
        }

    }
}

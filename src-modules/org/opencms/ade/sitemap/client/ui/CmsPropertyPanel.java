/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsPropertyPanel.java,v $
 * Date   : $Date: 2011/05/06 08:33:51 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidgetUtil;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A tabbed form field container widget.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.7 $
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

    /** Multimap of fields by field group. */
    private Multimap<String, I_CmsFormField> m_fieldsByGroup = ArrayListMultimap.create();

    /** Map from group/tab names to corresponding panels. */
    private Map<String, Panel> m_groups = new LinkedHashMap<String, Panel>();

    /** Set of fields which should be displayed at the top of the "individual" tab. */
    private Set<String> m_individualDisplay = Sets.newHashSet();

    /** The "individual" tab. */
    private FlowPanel m_individualTab = new FlowPanel();

    /** Set of fields which should be displayed at the top of the "shared" tab. */
    private Set<String> m_sharedDisplay = Sets.newHashSet();

    /** The "shared" tab. */
    private FlowPanel m_sharedTab = new FlowPanel();

    /** True if the "shared" tab should be shown. */
    private boolean m_showShared;

    /** The "simple" tab. */
    private FlowPanel m_simpleTab = new FlowPanel();

    /** The tab wrapper for the simple tab. */
    private FlowPanel m_simpleTabWrapper = new FlowPanel();

    /** The tab wrapper for the shared tab. */
    private FlowPanel m_sharedTabWrapper = new FlowPanel();

    /** The tab wrapper for the individual tab. */
    private FlowPanel m_individualTabWrapper = new FlowPanel();

    /** The tab panel. */
    private CmsTabbedPanel<Widget> m_tabPanel = new CmsTabbedPanel<Widget>();

    /**
     * Creates a new instance.<p>
     * 
     * @param showShared true if the "shared" tab should be shown 
     */
    public CmsPropertyPanel(boolean showShared) {

        // TODO: replace with dynamic calculation
        m_tabPanel.getElement().getStyle().setHeight(600, Unit.PX);
        CmsDomUtil.makeScrollable(m_simpleTab);
        CmsDomUtil.makeScrollable(m_sharedTab);
        CmsDomUtil.makeScrollable(m_individualTab);

        m_groups.put(TAB_SIMPLE, m_simpleTab);
        m_groups.put(TAB_SHARED, m_sharedTab);
        m_groups.put(TAB_INDIVIDUAL, m_individualTab);

        m_tabPanel.add(CmsPopup.wrapWithBorderPadding(m_simpleTab), Messages.get().key(
            Messages.GUI_PROPERTY_TAB_SIMPLE_0));
        m_showShared = showShared;
        if (m_showShared) {
            m_tabPanel.add(m_individualTab, Messages.get().key(Messages.GUI_PROPERTY_TAB_STRUCTURE_0));
            m_tabPanel.add(m_sharedTab, Messages.get().key(Messages.GUI_PROPERTY_TAB_RESOURCE_0));
        } else {
            m_tabPanel.add(m_individualTab, Messages.get().key(Messages.GUI_PROPERTY_TAB_COMPLETE_0));
        }
        initWidget(m_tabPanel);
    }

    /**
     * Creates a new instance.<p>
     * 
     * @param showShared true if the "shared" tab should be shown 
     * @param info the bean to use for displaying the info item
     */
    public CmsPropertyPanel(boolean showShared, CmsListInfoBean info) {

        // TODO: replace with dynamic calculation
        m_tabPanel.getElement().getStyle().setHeight(600, Unit.PX);

        CmsListItemWidget liWidget = new CmsListItemWidget(info);
        CmsListItemWidgetUtil.setPageIcon(liWidget, info.getPageIcon());
        if (CmsSitemapView.getInstance().isNavigationMode()) {
            liWidget.addStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        } else {
            liWidget.addStyleName(I_CmsLayoutBundle.INSTANCE.sitemapItemCss().vfsMode());
        }
        m_simpleTabWrapper.add(liWidget);
        m_simpleTabWrapper.add(m_simpleTab);
        m_simpleTab.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_simpleTab.addStyleName(I_CmsLayoutBundle.INSTANCE.propertiesCss().vfsModeSimplePropertiesBox());

        m_sharedTabWrapper.add(new CmsListItemWidget(info));
        m_sharedTabWrapper.add(m_sharedTab);

        m_individualTabWrapper.add(new CmsListItemWidget(info));
        m_individualTabWrapper.add(m_individualTab);

        CmsDomUtil.makeScrollable(m_simpleTabWrapper);
        CmsDomUtil.makeScrollable(m_sharedTabWrapper);
        CmsDomUtil.makeScrollable(m_individualTabWrapper);

        m_groups.put(TAB_SIMPLE, m_simpleTab);
        m_groups.put(TAB_SHARED, m_sharedTab);
        m_groups.put(TAB_INDIVIDUAL, m_individualTab);

        m_tabPanel.add(m_simpleTabWrapper, Messages.get().key(Messages.GUI_PROPERTY_TAB_SIMPLE_0));
        m_showShared = showShared;
        if (m_showShared) {
            m_tabPanel.add(m_individualTabWrapper, Messages.get().key(Messages.GUI_PROPERTY_TAB_STRUCTURE_0));
            m_tabPanel.add(m_sharedTabWrapper, Messages.get().key(Messages.GUI_PROPERTY_TAB_RESOURCE_0));
        } else {
            m_tabPanel.add(m_individualTabWrapper, Messages.get().key(Messages.GUI_PROPERTY_TAB_COMPLETE_0));
        }
        initWidget(m_tabPanel);

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
        usedFieldSet.setLegend(used);
        usedFieldSet.setAnimationDuration(50);

        String unused = Messages.get().key(Messages.GUI_PROPERTY_BLOCK_UNUSED_0);
        CmsFieldSet unusedFieldSet = new CmsFieldSet();
        unusedFieldSet.setOpen(false);
        unusedFieldSet.setLegend(unused);
        unusedFieldSet.setAnimationDuration(50);

        for (I_CmsFormField field : fields) {
            if (isTop(field)) {
                usedFieldSet.addContent(createRow(field));
            } else {
                unusedFieldSet.addContent(createRow(field));
            }
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
        renderExtendedTab(individualTabFields, m_individualTab);

        // process shared tab
        if (m_showShared) {
            m_sharedDisplay = preprocessFields(sharedTabfields);
            renderExtendedTab(sharedTabfields, m_sharedTab);
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
     * @param displaySet the set to which the field property names should be added if the corresponding property should be display at the top.
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
    }
}

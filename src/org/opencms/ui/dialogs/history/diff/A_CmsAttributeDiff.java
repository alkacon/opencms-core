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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.gwt.shared.CmsHistoryVersion;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.util.table.CmsBeanTableBuilder;
import org.opencms.workplace.comparison.CmsAttributeComparison;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract super class for displaying differences between a set of properties / attributes.<p>
 */
public abstract class A_CmsAttributeDiff implements I_CmsDiffProvider {

    /**
     * Predicate used to check if an attribute comparison bean actually represents a difference.<p>
     */
    public static class IsAttributeDifference implements Predicate<CmsAttributeComparison> {

        /**
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        public boolean apply(CmsAttributeComparison input) {

            return !Objects.equal(input.getVersion1(), input.getVersion2());
        }
    }

    /** CSS class to mark the compare table. */
    private static final String COMPARE_TABLE_MARKER = "cms-compare-table-marker";

    /**
     * Only selects the attributes comparisons that actually represent differences.<p>
     *
     * @param comps the attribute comparisons
     *
     * @return the list of attribute comparisons which correspond to actual differences
     */
    public static List<CmsAttributeComparison> filterDifferent(List<CmsAttributeComparison> comps) {

        return Lists.newArrayList(Collections2.filter(comps, new IsAttributeDifference()));
    }

    /**
     * Builds a version string which can be used by CmsResourceComparison from a history resource bean.<p>
     *
     * This is not really a user-readable string, it is meant for internal usage.
     *
     * @param bean the history resource bean
     * @return the version string
     */
    public static String getVersionString(CmsHistoryResourceBean bean) {

        if (bean.getVersion().getVersionNumber() != null) {
            return bean.getVersion().getVersionNumber().toString();
        } else if (bean.getVersion().isOffline()) {
            return "0";
        } else {
            return "-1";
        }
    }

    /**
     * Reads a historical resource for a history resource bean.<p>
     *
     * @param cms the CMS context
     * @param bean the history resource bean
     * @return the historical resource
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsResource readResource(CmsObject cms, CmsHistoryResourceBean bean) throws CmsException {

        CmsHistoryVersion versionBean = bean.getVersion();
        if (versionBean.getVersionNumber() != null) {
            return (CmsResource)(cms.readResource(bean.getStructureId(), versionBean.getVersionNumber().intValue()));
        } else {
            if (versionBean.isOnline()) {
                CmsObject onlineCms = OpenCms.initCmsObject(cms);
                onlineCms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                return onlineCms.readResource(bean.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
            } else {
                return cms.readResource(bean.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION);
            }
        }
    }

    /**
     * @see org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider#diff(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    public Optional<Component> diff(CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2)
    throws CmsException {

        List<CmsAttributeComparison> attrCompare = getDifferences(cms, v1, v2);
        if (attrCompare.isEmpty()) {
            return Optional.absent();
        }
        List<CmsPropertyCompareBean> compareBeans = Lists.newArrayList();

        for (CmsAttributeComparison comp : attrCompare) {
            compareBeans.add(new CmsPropertyCompareBean(comp));
        }
        CmsBeanTableBuilder<CmsPropertyCompareBean> builder = CmsBeanTableBuilder.newInstance(
            CmsPropertyCompareBean.class,
            A_CmsUI.get().getDisplayType().toString());
        builder.setMacroResolver(new CmsVersionMacroResolver(v1, v2));
        Table table = builder.buildTable(compareBeans);
        table.setSortEnabled(false);
        table.setWidth("100%");
        table.setPageLength(Math.min(12, compareBeans.size()));
        table.setStyleName(COMPARE_TABLE_MARKER);
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.addComponent(table);
        Panel panel = new Panel(getCaption());
        panel.setContent(vl);
        return Optional.fromNullable((Component)panel);
    }

    /**
     * Gets the caption.<p>
     *
     * @return the caption
     */
    public abstract String getCaption();

    /**
     * Gets the attribute comparison beans representing the differences.<p>
     *
     * @param cms the CMS context
     * @param v1 history bean representing the first version
     * @param v2  history bean representing the second version
     * @return the list of attribute comparisons for the given versions
     *
     * @throws CmsException if something goes wrong
     */
    public abstract List<CmsAttributeComparison> getDifferences(
        CmsObject cms,
        CmsHistoryResourceBean v1,
        CmsHistoryResourceBean v2) throws CmsException;

}

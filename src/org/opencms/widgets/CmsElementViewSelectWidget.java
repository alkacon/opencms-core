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

package org.opencms.widgets;

import org.opencms.ade.configuration.CmsElementView;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A widget to select an element view.<p>
 *
 * If the widget configuration contains the string 'selectparent', the widget will be used for selecting parent views of a view.
 * This makes sense *only* when the widget is used in the element view content itself.
 */
public class CmsElementViewSelectWidget extends CmsSelectWidget {

    /**
     * Constructor.<p>
     */
    public CmsElementViewSelectWidget() {

        super();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsElementViewSelectWidget();
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
        String myPath = getResourcePath(cms, widgetDialog);
        Map<CmsUUID, CmsElementView> views = OpenCms.getADEManager().getElementViews(cms);

        for (CmsElementView view : views.values()) {

            if (shouldIgnore(view, views, myPath)) {
                continue;
            }
            String value = "";
            if (view.getResource() != null) {
                value = cms.getSitePath(view.getResource());
            } else if ((view.getId() != null) && !view.getId().isNullUUID()) {
                // synthetic view
                value = "view://" + view.getId();
            }
            options.add(new CmsSelectWidgetOption(value, false, view.getTitle(cms, widgetDialog.getLocale())));
        }
        if (isSelectParent()) {
            //
            options.add(new CmsSelectWidgetOption(CmsElementView.PARENT_NONE, true, "--"));
        }
        return options;
    }

    /**
     * Returns true if the 'selectparent' option is enabled.<p>
     *
     * @return true if the 'selectparent' option is enabled
     */
    private boolean isSelectParent() {

        return (getConfiguration() != null) && getConfiguration().contains("selectparent");
    }

    /**
     * Check if the view should be ignored when constructing select options.<p>
     *
     * @param view the view to check
     * @param views the map of all views
     * @param myPath the path of the currently edited resource
     *
     * @return true if the view should be ignored
     */
    private boolean shouldIgnore(CmsElementView view, Map<CmsUUID, CmsElementView> views, String myPath) {

        if (isSelectParent()) {
            for (CmsElementView otherView : views.values()) {
                CmsUUID parentViewId = otherView.getParentViewId();
                if (parentViewId != null) {
                    CmsElementView parentOfOther = views.get(parentViewId);
                    if ((parentOfOther != null)
                        && (parentOfOther.getResource() != null)
                        && parentOfOther.getResource().getRootPath().equals(myPath)) {
                        // can't have "grandparents"
                        return true;
                    }
                }
            }
            boolean isStandardView = (view.getResource() != null) || view.getId().isNullUUID();
            if ((view.getParentViewId() != null) || !isStandardView) {
                // synthetic view, or already a sub-view of something else
                return true;
            }
            if ((view.getResource() != null) && view.getResource().getRootPath().equals(myPath)) {
                // can't make a view its own parent
                return true;
            }
        }
        return false;
    }
}

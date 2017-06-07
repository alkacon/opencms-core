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

package org.opencms.gwt.client.ui.history;

import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.shared.CmsHistoryResourceCollection;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget which is used as the container for the table which displays historical versions.<p>
 */
public class CmsResourceHistoryView extends Composite {

    /**
     * The uiBinder interface for this class.<p>
     */
    interface CmsResourceHistoryViewUiBinder extends UiBinder<Widget, CmsResourceHistoryView> {
        // empty
    }

    /** The uiBinder instance for this widget. */
    private static CmsResourceHistoryViewUiBinder uiBinder = GWT.create(CmsResourceHistoryViewUiBinder.class);

    /** The box containing the historical version table. */
    @UiField
    protected FlowPanel m_box;

    /** A box containing the file information widget. */
    @UiField
    protected FlowPanel m_infoBox;

    /** Widget used to show that there are no historical versions. */
    @UiField
    protected Widget m_noVersions;

    /**
     * Creates a new instance.<p>
     *
     * @param historyResources the resource history bean
     *
     * @param handler the handler class used to perform the list actions
     */
    public CmsResourceHistoryView(CmsHistoryResourceCollection historyResources, I_CmsHistoryActionHandler handler) {

        initWidget(uiBinder.createAndBindUi(this));
        CmsListInfoBean contentInfo = historyResources.getContentInfo();
        CmsListItemWidget infoWidget = new CmsListItemWidget(contentInfo);
        m_infoBox.add(infoWidget);
        if (historyResources.isEmpty()) {
            m_noVersions.setVisible(true);
        } else {
            m_box.add(new CmsResourceHistoryTable(historyResources, handler));
        }
    }
}

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

package org.opencms.ui.client;

import org.opencms.ui.report.CmsReportWidget;
import org.opencms.ui.shared.rpc.I_CmsReportClientRpc;
import org.opencms.ui.shared.rpc.I_CmsReportServerRpc;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for the report widget.<p>
 */
@Connect(CmsReportWidget.class)
public class CmsReportWidgetConnector extends AbstractComponentConnector implements I_CmsReportClientRpc {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** True if we know the report is finished (i.e. the last report update (null) has been received. */
    private boolean m_reportFinished;

    /** True if the connector has been unregistered. */
    private boolean m_unregistered;

    /**
     * Creates a new instance.<p>
     */
    public CmsReportWidgetConnector() {
        registerRpc(I_CmsReportClientRpc.class, this);
        RepeatingCommand command = new RepeatingCommand() {

            @SuppressWarnings("synthetic-access")
            public boolean execute() {

                if (m_unregistered || m_reportFinished) {
                    return false;
                }

                getRpcProxy(I_CmsReportServerRpc.class).requestReportUpdate();
                return true;
            }

        };
        Scheduler.get().scheduleFixedDelay(command, 2000);

    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsReportClientRpc#handleReportUpdate(java.lang.String)
     */
    public void handleReportUpdate(String reportUpdate) {

        if (reportUpdate == null) {
            m_reportFinished = true;
        } else {
            CmsClientReportWidget widget = (CmsClientReportWidget)getWidget();
            widget.append(reportUpdate);
        }
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onUnregister()
     */
    @Override
    public void onUnregister() {

        m_unregistered = true;
        super.onUnregister();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected Widget createWidget() {

        return new CmsClientReportWidget();
    }

}

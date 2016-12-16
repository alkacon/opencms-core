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

package org.opencms.ui.report;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.shared.components.CmsReportWidgetState;
import org.opencms.ui.shared.rpc.I_CmsReportClientRpc;
import org.opencms.ui.shared.rpc.I_CmsReportServerRpc;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.vaadin.ui.AbstractComponent;

/**
 * A widget used to display an OpenCms report.<p>
 */
public class CmsReportWidget extends AbstractComponent implements I_CmsReportServerRpc {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Handlers to execute when the report finishes while displaying the report widget. */
    private List<Runnable> m_reportFinishedHandlers = Lists.newArrayList();

    /** The report thread. */
    private A_CmsReportThread m_thread;

    /** True if the report thread is finished. */
    private boolean m_threadFinished;

    /**
     * Creates a new instance.<p>
     * Use in declarative layouts, remember to call .<p>
     * This does not start the report thread.<p>
     */
    public CmsReportWidget() {
        registerRpc(this, I_CmsReportServerRpc.class);
    }

    /**
     * Creates a new instance.<p>
     *
     * This does not start the report thread.
     *
     * @param thread the report thread
     */
    public CmsReportWidget(A_CmsReportThread thread) {
        this();
        m_thread = thread;
    }

    /**
     * Adds an action that should be executed if the report is finished.<p>
     *
     * Note that this action will only be called if the report is finished while the report widget is actually
     * displayed. For example, if the user closes the browser window before the report is finished, this will not be executed.
     *
     * @param handler the handler
     * @return the handler registration
     */
    public HandlerRegistration addReportFinishedHandler(final Runnable handler) {

        m_reportFinishedHandlers.add(handler);
        return new HandlerRegistration() {

            @SuppressWarnings("synthetic-access")
            public void removeHandler() {

                m_reportFinishedHandlers.remove(handler);
            }
        };
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsReportServerRpc#requestReportUpdate()
     */
    public void requestReportUpdate() {

        String reportUpdate = null;
        if (!m_threadFinished && (m_thread != null)) {
            // if thread is not alive at this point, there may still be report updates
            reportUpdate = m_thread.getReportUpdate();

            if (!m_thread.isAlive()) {
                m_threadFinished = true;
                for (Runnable handler : m_reportFinishedHandlers) {
                    handler.run();
                }
            }
        }
        getRpcProxy(I_CmsReportClientRpc.class).handleReportUpdate(reportUpdate);
    }

    /**
     * Sets the report thread.<p>
     *
     * @param thread the report thread
     */
    public void setReportThread(A_CmsReportThread thread) {

        m_thread = thread;
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    protected CmsReportWidgetState getState() {

        return (CmsReportWidgetState)(super.getState());
    }
}

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

import org.opencms.ui.editors.CmsEditorStateExtension;
import org.opencms.ui.shared.rpc.I_CmsEditorStateRPC;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client connector to the editor state extension.<p>
 */
@Connect(CmsEditorStateExtension.class)
public class CmsEditorStateConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = 7273974997472100908L;

    /** The number of export method attemps. */
    int m_exportRuns;

    /** The RPC proxy. */
    I_CmsEditorStateRPC m_rpc;

    /**
     * Constructor.<p>
     */
    public CmsEditorStateConnector() {
        m_rpc = getRpcProxy(I_CmsEditorStateRPC.class);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(final ServerConnector target) {

        m_exportRuns = 0;
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                m_exportRuns++;
                String frameName = ((ComponentConnector)target).getWidget().getElement().getAttribute("name");
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(frameName)) {
                    frameName = "edit";
                }
                return !exportMethod(frameName) && (m_exportRuns < 5);
            }
        }, 50);
    }

    /**
     * Exports the setEditorChangedState method to the edit frame.<p>
     *
     * @param frameName the edit frame name
     *
     * @return <code>true</code> indicates the frame was found and the method exported
     */
    native boolean exportMethod(String frameName)/*-{
		var self = this;
		var frame = $wnd.frames[frameName];
		if (frame == null && $wnd.frames.length > 0) {
			for (i = 0; i < $wnd.frames.length; i++) {
				if ($wnd.frames[i].frameElement.name == frameName) {
					frame = $wnd.frames[i];
					break;
				}
			}
		}

		if (frame != null) {
			frame.cmsSetEditorChangedState = function(changed) {
				self.@org.opencms.ui.client.CmsEditorStateConnector::setEditorChangedState(Z)(changed);
			}
			return true;
		} else
			return false;
    }-*/;

    /**
     * Sets the editor changed state.<p>
     *
     * @param changed indicates if the editor content was changed
     */
    private void setEditorChangedState(boolean changed) {

        m_rpc.setHasChanges(changed);
    }

}

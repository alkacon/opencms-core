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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageEvent;
import org.opencms.ade.containerpage.client.CmsContainerpageEvent.EventType;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.I_CmsContainerpageEventHandler;
import org.opencms.ade.publish.client.CmsPublishEvent;
import org.opencms.ade.publish.client.I_CmsPublishEventHandler;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.A_CmsToolbarButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.user.client.Timer;

/**
 * Class for the toolbar button to display elements information.<p>
 */
public class CmsToolbarElementInfoButton extends A_CmsToolbarButton<CmsContainerpageHandler>
implements I_CmsContainerpageEventHandler, I_CmsPublishEventHandler {

    /** The container page controller. */
    CmsContainerpageController m_controller;

    /** Style variable to change the button appearance depending on whether the page or elements have been changed. */
    private CmsStyleVariable m_changedStyleVar;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     * @param controller the container page controller
     */
    public CmsToolbarElementInfoButton(CmsContainerpageHandler handler, CmsContainerpageController controller) {

        super(I_CmsButton.ButtonData.ELEMENT_INFO, handler);
        m_changedStyleVar = new CmsStyleVariable(this);
        controller.addContainerpageEventHandler(this);
        CmsCoreProvider.get().getEventBus().addHandler(CmsPublishEvent.TYPE, this);
        m_controller = controller;
        setChanged(false);
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerpageEventHandler#onContainerpageEvent(org.opencms.ade.containerpage.client.CmsContainerpageEvent)
     */
    public void onContainerpageEvent(CmsContainerpageEvent event) {

        if ((event.getEventType() == EventType.elementEdited) || (event.getEventType() == EventType.pageSaved)) {
            asyncUpdate();
        }
    }

    /**
     * @see org.opencms.ade.publish.client.I_CmsPublishEventHandler#onPublish(org.opencms.ade.publish.client.CmsPublishEvent)
     */
    public void onPublish(CmsPublishEvent e) {

        Timer timer = new Timer() {

            @Override
            public void run() {

                asyncUpdate();
            }
        };
        // wait 5 seconds, which should be enough for small publish jobs
        // (if it's not enough, the user can still reload the page)
        timer.schedule(5000);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        CmsContainerpageHandler handler = getHandler();
        handler.openElementsInfo();
        setEnabled(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        setEnabled(true);
    }

    /**
     * Changes the "changed" state of the button.<p>
     *
     * @param changed the new value for the "changed" state
     */
    public void setChanged(boolean changed) {

        m_changedStyleVar.setValue(
            changed
            ? I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().elementInfoChanged()
            : I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().elementInfoUnchanged());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        Timer timer = new Timer() {

            @Override
            public void run() {

                asyncUpdate();
            }

        };
        timer.schedule(500);
    }

    /**
     * Fires off an RPC request to check for the elements state and then updates the button accordingly.<p>
     */
    void asyncUpdate() {

        CmsRpcAction<Boolean> action = new CmsRpcAction<Boolean>() {

            @Override
            public void execute() {

                start(200, false);
                m_controller.getContainerpageService().checkContainerpageOrElementsChanged(
                    CmsCoreProvider.get().getStructureId(),
                    CmsContainerpageController.get().getData().getDetailId(),
                    CmsContainerpageController.get().getData().getLocale(),
                    this);
            }

            @Override
            protected void onResponse(Boolean result) {

                stop(false);
                boolean changed = result.booleanValue();
                setChanged(changed);
            }

        };
        action.execute();
    }

}

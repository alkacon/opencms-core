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

package org.opencms.ui.components.extensions;

import org.opencms.ui.shared.rpc.I_CmsWindowClientRpc;
import org.opencms.ui.shared.rpc.I_CmsWindowServerRpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Extension used to open new browser windows.<p>
 * While Page.open() can also be used to open new windows, it doesn't give you any feedback on whether
 * opening the window was successful. Using this extension, it is possible to pass a callback which gets called
 * with a parameter indicating whether opening the window succeeded or not.
 */
public class CmsWindowExtension extends AbstractExtension {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Map of callbacks which have not yet been called. */
    private Map<String, FutureCallback<Boolean>> m_callbackMap = new ConcurrentHashMap<String, FutureCallback<Boolean>>();

    /**
     * Creates a new instance and binds it to the given UI.<p>
     *
     * @param ui the UI
     */
    public CmsWindowExtension(UI ui) {
        super(ui);
        registerRpc(new I_CmsWindowServerRpc() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void handleOpenResult(String id, boolean ok) {

                FutureCallback<Boolean> callback = m_callbackMap.get(id);
                if (callback != null) {
                    callback.onSuccess(Boolean.valueOf(ok));
                }
                m_callbackMap.remove(id);
            }

        }, I_CmsWindowServerRpc.class);
    }

    /**
     * Tries to open a new browser window.<p>
     *
     * If openning the window fails, the given callback is called.<p>
     *
     * @param location the URL to open in the new window
     * @param target the target window name
     *
     * @param onFailure the callback to call if opening the window fails
     */
    public void open(String location, String target, final Runnable onFailure) {

        String id = RandomStringUtils.randomAlphanumeric(16);
        m_callbackMap.put(id, new FutureCallback<Boolean>() {

            public void onFailure(Throwable t) {
                //
            }

            public void onSuccess(Boolean result) {

                if (!result.booleanValue()) {
                    onFailure.run();
                }
            }
        });
        getRpcProxy(I_CmsWindowClientRpc.class).open(location, target, id);
    }

}

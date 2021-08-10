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

package org.opencms.ui.login;

import org.opencms.crypto.CmsEncryptionException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.shared.login.I_CmsLoginTargetRpc;

import org.apache.commons.logging.Log;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Server side component used to open the login target for a logged in user.<p>
 */
public class CmsLoginTargetOpener extends AbstractExtension {

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginTargetOpener.class);

    /**
     * Creates a new instance.<p>
     *
     * @param ui the UI to extend
     */
    public CmsLoginTargetOpener(UI ui) {

        extend(ui);
    }

    /**
     * Opens the login target.<p>
     *
     * @param target the login target URL
     * @param isPublicPC the public PC flag
     */
    public void openTarget(String target, boolean isPublicPC) {

        if (isPublicPC) {
            getRpcProxy(I_CmsLoginTargetRpc.class).openTargetForPublicPc(target);
        } else {
            String encryptedTarget;
            // for private PCs, another form submission step is used on the client to trigger the browser password manager.
            // the form used to a contain a field with the plain redirect URI, but we now encrypt it beforehand.
            try {
                encryptedTarget = OpenCms.getDefaultTextEncryption().encrypt(target);
                getRpcProxy(I_CmsLoginTargetRpc.class).openTargetForPrivatePc(encryptedTarget);
            } catch (CmsEncryptionException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                getRpcProxy(I_CmsLoginTargetRpc.class).openTargetForPublicPc(target);
            }
        }
    }
}

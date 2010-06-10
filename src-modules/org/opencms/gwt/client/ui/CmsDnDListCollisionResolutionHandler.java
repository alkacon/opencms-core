/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListCollisionResolutionHandler.java,v $
 * Date   : $Date: 2010/06/10 12:56:38 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Default drag and drop list collision resolution handler.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsDnDListCollisionResolutionHandler implements I_CmsDnDListCollisionResolutionHandler {

    /**
     * @see org.opencms.gwt.client.ui.I_CmsDnDListCollisionResolutionHandler#checkCollision(org.opencms.gwt.client.ui.CmsDnDListDropEvent, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void checkCollision(final CmsDnDListDropEvent dropEvent, final AsyncCallback<String> asyncCallback) {

        final CmsDnDListItem item = dropEvent.getDestList().getItem(CmsDnDListItem.DRAGGED_PLACEHOLDER_ID);
        final String droppedItemId = item.getOriginalId();
        DeferredCommand.addCommand(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                String newId = droppedItemId;
                // TODO: use a nicer dialog, with ajax blacklist for already used ids 
                while ((newId != null) && (dropEvent.getDestList().getItem(newId) != null)) {
                    // TODO: i18n
                    newId = Window.prompt("duplicated id, please enter a new one", newId);
                }
                if (newId == null) {
                    asyncCallback.onFailure(null);
                } else {
                    item.setId(newId);
                    asyncCallback.onSuccess(newId);
                }
            }
        });
    }
}

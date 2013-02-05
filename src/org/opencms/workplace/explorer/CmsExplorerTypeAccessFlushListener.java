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

package org.opencms.workplace.explorer;

import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class which listens for cache flush events to uncache cached access control settings.<p>
 */
public class CmsExplorerTypeAccessFlushListener implements I_CmsEventListener {

    /** List of weak references to the access settings. */
    private List<WeakReference<CmsExplorerTypeAccess>> m_contents = new ArrayList<WeakReference<CmsExplorerTypeAccess>>();

    /**
     * Adds a new access settings object to this listener.<p>
     * 
     * @param access the access settings 
     */
    public synchronized void add(CmsExplorerTypeAccess access) {

        m_contents.add(new WeakReference<CmsExplorerTypeAccess>(access));
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
                doFlush();
                break;
            default: // ignore;
        }
    }

    /**
     * Installs this instance as an event listener.<p>
     */
    public void install() {

        OpenCms.getEventManager().addCmsEventListener(
            this,
            new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES,
                I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES});
    }

    /**
     * Flushes the cache for all registered access setting objects.<p>
     */
    protected synchronized void doFlush() {

        Iterator<WeakReference<CmsExplorerTypeAccess>> iter = m_contents.iterator();
        while (iter.hasNext()) {
            WeakReference<CmsExplorerTypeAccess> ref = iter.next();
            CmsExplorerTypeAccess access = ref.get();
            if (access == null) {
                iter.remove();
            } else {
                access.flushCache();
            }
        }
    }

}

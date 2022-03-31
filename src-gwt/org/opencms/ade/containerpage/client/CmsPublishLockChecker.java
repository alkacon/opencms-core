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

package org.opencms.ade.containerpage.client;

import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Timer;

/**
 * Helper class for periodically checking a set of elements for publish locks and then
 * reloading the corresponding elements when they are no longer locked.
 */
public class CmsPublishLockChecker {

    /** The delay between to publish lock checks. */
    public static final int DELAY = 500;

    /** True if we are currently checking for locks. */
    protected boolean m_active = false;

    /** The ids to check .*/
    protected Set<CmsUUID> m_toCheck = new HashSet<>();

    /** The container page controller. */
    protected CmsContainerpageController m_controller;

    /**
     * Creates a new instance.
     *
     * @param controller the container page controller
     */
    public CmsPublishLockChecker(CmsContainerpageController controller) {

        m_controller = controller;
    }

    /**
     * Adds a set of element ids which should be checked for publish locks.
     *
     * @param ids the ids which should be checked
     */
    public void addIdsToCheck(Set<CmsUUID> ids) {

        m_toCheck.addAll(ids);
        if (!m_active) {
            m_active = true;
            scheduleNextCheck();
        }
    }

    /**
     * Processes the results of the publish lock check RPC call.
     *
     * @param locked the remaining ids of elements with a publish lock
     */
    protected void processCheckResult(Set<CmsUUID> locked) {

        Iterator<CmsUUID> iterator = m_toCheck.iterator();
        Set<CmsUUID> toUpdate = new HashSet<>();
        while (iterator.hasNext()) {
            CmsUUID id = iterator.next();
            if (!locked.contains(id)) {
                toUpdate.add(id);
                iterator.remove();
            }
        }
        if (toUpdate.size() > 0) {
            m_controller.reloadElements(
                toUpdate.stream().map(id -> "" + id).collect(Collectors.toList()),
                () -> {/*do nothing*/});
        }
        if (m_toCheck.size() > 0) {
            scheduleNextCheck();
        } else {
            m_active = false;
        }
    }

    /**
     * Schedules the next check.
     */
    private void scheduleNextCheck() {

        Timer timer = new Timer() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                startCheck();
            }
        };
        timer.schedule(DELAY);
    }

    /**
     * Starts the RPC call to check for publish locks.
     */
    private void startCheck() {

        CmsRpcAction<Set<CmsUUID>> action = new CmsRpcAction<Set<CmsUUID>>() {

            @Override
            public void execute() {

                start(0, false);
                m_controller.getContainerpageService().getElementsLockedForPublishing(m_toCheck, this);
            }

            @Override
            protected void onResponse(Set<CmsUUID> locked) {

                stop(false);

                processCheckResult(locked);
            }

        };
        action.execute();
    }
}

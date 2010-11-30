/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapCompositeChange.java,v $
 * Date   : $Date: 2010/11/30 08:56:13 $
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sitemap change which executes multiple other sitemap changes.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapCompositeChange implements I_CmsClientSitemapChange {

    /** The list of internal changes. */
    private List<I_CmsClientSitemapChange> m_changes = new ArrayList<I_CmsClientSitemapChange>();

    /**
     * Constructs a new, empty composite change.<p>
     */
    public CmsClientSitemapCompositeChange() {

        // do nothing 
    }

    /**
     * Constructs a composite change from an existing list of changes.<p>
     * 
     * @param changes a list of changes
     */
    public CmsClientSitemapCompositeChange(List<I_CmsClientSitemapChange> changes) {

        m_changes = changes;
    }

    /**
     * Adds a new change to the composite change's internal list of changes.<p>
     * 
     * @param change the change object to add 
     */
    public void addChange(I_CmsClientSitemapChange change) {

        if (change != null) {
            m_changes.add(change);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        for (I_CmsClientSitemapChange change : m_changes) {
            change.applyToClipboardView(view);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        for (I_CmsClientSitemapChange change : m_changes) {
            change.applyToModel(controller);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        for (I_CmsClientSitemapChange change : m_changes) {
            change.applyToView(view);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForUndo()
     */
    public I_CmsClientSitemapChange getChangeForUndo() {

        return this;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangesForCommit()
     */
    public List<I_CmsSitemapChange> getChangesForCommit() {

        List<I_CmsSitemapChange> result = new ArrayList<I_CmsSitemapChange>();
        for (I_CmsClientSitemapChange change : m_changes) {
            result.addAll(change.getChangesForCommit());
        }
        return result;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getType()
     */
    public Type getType() {

        return null; // not needed
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#revert()
     */
    public I_CmsClientSitemapChange revert() {

        List<I_CmsClientSitemapChange> reverseChanges = new ArrayList<I_CmsClientSitemapChange>(m_changes);
        Collections.reverse(reverseChanges);
        List<I_CmsClientSitemapChange> undoChanges = new ArrayList<I_CmsClientSitemapChange>();
        for (I_CmsClientSitemapChange change : reverseChanges) {
            undoChanges.add(change.revert());
        }
        return new CmsClientSitemapCompositeChange(undoChanges);
    }

}

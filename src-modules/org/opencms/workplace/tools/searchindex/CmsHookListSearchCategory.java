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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex;

import org.opencms.search.CmsSearchParameters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A hook on the list of categories of the <code>CmsSearchParameters</code>
 * (see <code>{@link org.opencms.search.CmsSearchParameters#setCategories(List)}</code>)
 * that will set the page to display of the search parameters to start (1) whenever
 * a change in the amount of categories takes place. <p>
 *
 * This hook monitors the actions of the <code>{@link org.opencms.workplace.CmsWidgetDialog}</code>:
 * During a request - response cycle it clears the list (prepareCommit)
 * of categories and adds all request parameters (actionCommitValue) to the list. <p>
 *
 * The strategy here is to save all categories in a backup at "onClear" - time
 * and then wait until all add operations are finished. This is when the iterator()
 * method is triggered by the running search (<code>{@link org.opencms.search.CmsSearch#getSearchResult()}</code>).
 * At that time it is detected wether we have "lost" categories. If this is the
 * case, the search page will be reset to start. <p>
 *
 * <h3>Warning</h3>
 * This procedure is highly unstable as coupled to the behaviour of CmsWidgetDialog. It
 * will only be successful if the request parameter "action" has a value in
 * <ol>
 *  <li>
 *   <code>{@link org.opencms.workplace.CmsWidgetDialog#DIALOG_SAVE}</code>
 *  </li>
 *  <li>
 *   <code>{@link org.opencms.workplace.CmsWidgetDialog#DIALOG_BACK}</code>
 *  </li>
 *  <li>
 *   <code>{@link org.opencms.workplace.CmsWidgetDialog#DIALOG_CONTINUE}</code>
 *  </li>
 * </ol>
 *
 * Search page links (<code>{@link org.opencms.search.CmsSearch#getPageLinks()}</code>)
 * contain a parameter "action" with value "search" which has to be rewritten to one
 * of those values or the order of operations on widget - managed collections does not
 * work together with the detection strategy for category changes used here. <p>
 *
 * @since 6.0.0
 */
public class CmsHookListSearchCategory extends CmsHookList {

    /** The serial version UID. */
    private static final long serialVersionUID = 145986432425221213L;

    /** The snapshot (clone) of this list at "clear()" time. **/
    private List<Object> m_backupCategories;

    /**
     * @param peer the search parameters to modify upon category modifications.
     */
    public CmsHookListSearchCategory(CmsSearchParameters peer) {

        super(peer);
    }

    /**
     * @param peer the search parameters to modify upon category modifications
     *
     * @param c a collection with all values for this list
     */
    public CmsHookListSearchCategory(CmsSearchParameters peer, Collection<String> c) {

        super(peer, c);

    }

    /**
     * A category has been added: do nothing. <p>
     *
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onAdded(java.lang.Object, int, java.lang.Object)
     */
    @Override
    protected void onAdded(Object peer, int index, Object element) {

        // nop
    }

    /**
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onAdded(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void onAdded(Object peer, Object added) {

        // nop
    }

    /**
     * Takes a copy of the current categories contained to a backup list as this
     * operation is triggered by
     * <code>{@link org.opencms.workplace.CmsWidgetDialog#ACTION_SAVE}</code>.<p>
     *
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onClear(java.lang.Object)
     */
    @Override
    protected void onClear(Object peer) {

        m_backupCategories = new LinkedList<Object>(this);
    }

    /**
     *
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onCleared(java.lang.Object)
     */
    @Override
    protected void onCleared(Object peer) {

        // nop
    }

    /**
     * Set the search page of the peer Object
     * (<code>{@link org.opencms.search.CmsSearch#setSearchPage(int)}</code>)
     * to zero if the internal backup list of categories (taken at clear time which
     * is triggered by <code>{@link org.opencms.workplace.CmsWidgetDialog#ACTION_SAVE}</code>)
     * was empty (no restriction) and now categories are contained  or if the new
     * backup list of categories is no subset of the current categories any
     * more (more restrictive search than before). <p>
     *
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onGetCall(java.lang.Object, int)
     */
    @Override
    protected void onGetCall(Object peer, int index) {

        // zero categories are all (first condition)
        if (((m_backupCategories.size() == 0) && (size() != 0)) || !(containsAll(m_backupCategories))) {
            ((CmsSearchParameters)peer).setSearchPage(1);
        }
    }

    /**
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onIteratorCall(java.lang.Object)
     */
    @Override
    protected void onIteratorCall(Object peer) {

        // nop
    }

    /**
     * A category has been removed: set search page to start page as
     * new results may / will be smaller. <p>
     *
     *
     * @see org.opencms.workplace.tools.searchindex.CmsHookList#onRemoved(java.lang.Object, int)
     */
    @Override
    protected void onRemoved(Object peer, int index) {

        ((CmsSearchParameters)peer).setSearchPage(1);
    }
}

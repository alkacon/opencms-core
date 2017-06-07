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

/*
 * Opens a previously closed group in the menu.
 */
function openGroup(group) {

   var element = document.getElementById(group);
   if (element && element.className) {
     element.className = (element.className == 'navOpened') ? 'navClosed' : 'navOpened';
   }
}

/*
 * Sets a new active item in the menu, by changing its style class.
 */
function setActiveItem(id) {
    if (activeItem == id) {
        // if already active
        return false;
    }

    // try to desactivate the previous active one
    var cur = document.getElementById(activeItem);
    if (cur) {
      cur.className = 'node';
    }
    // sets the new active item
    activeItem = id;
    if (document.getElementById(id)) {
      document.getElementById(id).className='nodeActive';    
    }

    return true;
}

/*
 * Opens a new page in the given frame and activating the given item.
 */
function openView(id, url, frame) {
    setActiveItem(id);
    if (parent && parent.frames && parent.frames[frame]) {
      parent.frames[frame].location.href = url;
    }
    return false;
}

/*
 * Mouse event handler for item groups.
 */
function mouseGroupEvent(group, open) {
    if (open) {
       group.className = 'navTitleOver';
    } else {
       group.className = 'navTitle';
    }
}

/*
 * Sets the current displayed context help.
 */
function setInternalContextHelp(contextHelp) {
    if (!contextHelp) {
        // show default
        contextHelp = getContext();
    }

    // get the container
    var container = document.getElementById('contexthelp_text');
    // clear it
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
    // set the new text
    container.insertBefore(document.createTextNode(contextHelp), null);
}

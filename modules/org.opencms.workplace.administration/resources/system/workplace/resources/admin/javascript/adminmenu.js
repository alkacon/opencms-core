/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace.administration/resources/system/workplace/resources/admin/javascript/adminmenu.js,v $
 * Date   : $Date: 2005/06/03 16:29:19 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2003 Alkacon Software (http://www.alkacon.com)
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

/*
 * Explicitely sets the scroll bar if needed.
 */
function setScrollInIE() {
    try {
        var navL = document.getElementById ('navArea');
        document.body.scroll = (navL.clientHeight > document.documentElement.clientHeight || navL.clientWidth > document.documentElement.clientWidth) ? 'yes' : 'no';
    } catch (e) {
        return false;
    }
}

/*
 * Opens a previously closed group in the menu.
 */
function openGroup(group) {
    if (document.getElementById) {
        var element = document.getElementById (group);
        if (element && element.className) {
            element.className = (element.className == 'navOpened') ? 'navClosed' : 'navOpened';
        }
    }
    if (navigator.appName == 'Microsoft Internet Explorer' && document.documentElement && navigator.userAgent.indexOf ('Opera') == -1) {
        setScrollInIE();
    }
    return false;
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
    try {
        var cur = document.getElementById(activeItem);
        cur.className = 'node';
    }
    catch (e) { }

    // sets the new active item
    activeItem = id;
    document.getElementById(id).className='nodeActive';    
}

/*
 * Removes a parameter an its value from a given url.
 */
function removeParam(url, param) {
    var iniPos = url.indexOf("&" + param + "=");
    if (iniPos > -1) {
       var endPos = url.indexOf("&", iniPos + 1);
       ret = url.substring(0, iniPos);
       if (endPos > -1) {
          ret = ret + url.substring(endPos);
       }
       return ret;
    }
    iniPos = url.indexOf("?" + param + "=");
    if (iniPos > -1) {
       var endPos = url.indexOf("&", iniPos + 1);
       ret = url.substring(0, iniPos);
       if (endPos > -1) {
          ret = ret + "?" + url.substring(endPos + 1);
       }
       return ret;
    }
    return url;
}

/*
 * Opens a new page in the given frame and activating the given item.
 */
function openView(id, url, frame) {
    setActiveItem(id);
    if (url.indexOf("system/workplace/views/admin/admin-main.html")<0) {
       finalUrl = url;
    } else {
        finalUrl = parent.frames[frame].location.href;
        finalUrl = removeParam(finalUrl, "action");
        finalUrl = removeParam(finalUrl, "closelink");
        if (finalUrl.indexOf("?")>-1) {
            finalUrl = finalUrl + "&";
        } else {
            finalUrl = finalUrl + "?";
        }
        finalUrl = finalUrl + "action=cancel&closelink=" + encodeURIComponent(url);
    }
    parent.frames[frame].location.href = finalUrl;
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

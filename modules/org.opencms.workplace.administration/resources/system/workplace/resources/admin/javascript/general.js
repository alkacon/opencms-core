/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace.administration/resources/system/workplace/resources/admin/javascript/general.js,v $
 * Date   : $Date: 2005/06/07 16:25:40 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2004-2005 Alkacon Software (http://www.alkacon.com)
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
 * Returns the form that contains the control with the given id.
 */
function getFormForId(id) {
        return document.getElementById(id).form;
}

/*
 * Opens a new page in the right frame.
 * It also shows the loading screen during loading
 */
function openPage(url) {
    target = isFramed() ? parent.admin_content : this;
    if (!isFramed()) {
       if (parent.parent.admin_content) {
           target = parent.parent.admin_content;
       }
    }
    openPageIn(url, target);
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
 * Opens a new page in the given target frame.
 * It also shows the loading screen during loading
 */
function openPageIn(url, target) {
    loadingOn();
    if (url.indexOf("system/workplace/views/admin/admin-main.html")<0) {
       finalUrl = url;
    } else {
        finalUrl = target.location.href;
        finalUrl = removeParam(finalUrl, "action");
        finalUrl = removeParam(finalUrl, "closelink");
        if (finalUrl.indexOf("?")>-1) {
            finalUrl = finalUrl + "&";
        } else {
            finalUrl = finalUrl + "?";
        }
        finalUrl = finalUrl + "action=cancel&closelink=" + encodeURIComponent(url);
    }
    target.location.href = finalUrl;
}

/*
 * Sets the current displayed context help.
 * It only works if framed.
 */
function setContextHelp(contextHelp) {
    try {
        parent.admin_menu.setInternalContextHelp(contextHelp);
    } catch(e) {
        try {
             parent.parent.admin_menu.setInternalContextHelp(contextHelp);
        } catch (e1) { }
    }
}

/*
 * Sets the default context help.
 * It only works if framed.
 */
function setContext(defaultContext) {
    if (defaultContext) {
        parent.defContextHelp = defaultContext;
    }
    setContextHelp();
}

/*
 * Returns the default context help.
 * It only works if framed.
 */
function getContext() {
    return parent.defContextHelp;
}

/*
 * Sets the active item in the menu frame.
 * It only works if framed.
 */
function setActiveItemByName(name) {
    try {
        return parent.admin_menu.setActiveItem(name);
    } catch (e) {
        try {
             parent.parent.admin_menu.setActiveItem(name);
        } catch (e1) { }
    }
}

/*
 * Shows the context help.
 * If framed, it will be shown in the menu frame;
 * if not, as a help ballon.
 * The obj_id argument should be the id of a div tag,
 * which contains the help text.
 */
function sMH(obj_id) {
   try {
      if (!isFramed() && !parent.parent.admin_content) {
         showHelp(obj_id);
      } else {
         try {
            var writezone = document.getElementById('help' + obj_id);
            context = writezone.firstChild.nodeValue;
         } catch(e) {
            context = obj_id;
         }
         setContextHelp(context);
      }
   } catch(e) {
      // ignore
   }
}

/*
 * Hides the context help.
 * If framed, it will be shown in the menu frame;
 * if not, as a help ballon.
 * The obj_id argument should be the id of a div tag,
 * which contains the help text.
 */
function hMH(obj_id) {
   try {
      if (!isFramed() && !parent.parent.admin_content) {
         hideHelp(obj_id);
      } else {
         setContextHelp('');
      }
   } catch(e) {
      // ignore
   }
}

/*
 * This method is called everytime a new page is being loaded.
 * That is at unload, submit and links actions.
 * First it overlap a image so you will feel the page 
 * is disabled, and then it disables every combobox 
 * on the page.
 */
function loadingOn(target) {
    if (undefined == target || target == '') {
        // set default target
        target = this;
    }
    if (undefined == target.disabledControls) {
        // create a new array of disabled controls
        target.disabledControls = new Array();
    } else if (target.disabledControls.length > 0) {
        // if already disabled returns, should never happen
        return true;
    }

    // display an overlapped "disabled" image
    target.document.getElementById("loaderContainer").style.display = "";

    // disable the combo boxes
    var comboArray = target.document.getElementsByTagName("select");
    for (var i = 0; i < comboArray.length; i++) {
        comboArray[i].disabled = true;
        disabledControls.pop(comboArray[i]);
        var combo = target.document.createElement("input");
        combo.type = "hidden";
        combo.name = comboArray[i].name;
        var values = new Array();
        for (var n = 0; n < comboArray[i].length; n++) {
            if (comboArray[i][n].selected) {
                values[values.length] = comboArray[i][n].value;
            }
        }
        combo.value = values.join(",");
        comboArray[i].parentNode.insertBefore(combo, comboArray[i]);
    }

    return true;
}

/*
 * This method is called everytime a new page is ready to be used.
 * That is at body.onload event.
 * First it removes the overlapped "disabled" image, and then it 
 * enables every combobox on the page.
 */
function loadingOff(target) {
    if (undefined == target || target == '') {
        // set default target
        target = this;
    }

    // hide the overlapped "disabled" image
    target.document.getElementById("loaderContainer").style.display = "none";

    if (undefined == target.disabledControls) {
        // if nothing to enable
        return true;
    }

    // enable the combo boxes
    while (disabledControls.legth > 0) {
        var combo = disabledControls.push();
        combo.disabled = false;

        var comboArray = target.document.getElementsByName(combo.name);
        for (var n = 0; n < comboArray.length; n++) {
            if ("hidden" == comboArray[n].type)
                comboArray[n].parent.removeChild(comboArray[n]);
        }
    }
    return true;
}

/*
 * Submits a form, showing the loading screen during loading.
 */
function submitForm(theForm) {
    loadingOn();
    theForm.submit();
    return true;
}

/*
 * Checks is we are working in the admin-view framed environment.
 */
function isFramed() {
    return parent.admin_content && parent.admin_menu;
}


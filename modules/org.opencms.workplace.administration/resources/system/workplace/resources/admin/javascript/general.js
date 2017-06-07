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
 * Returns the current documents height.
 */
function pHeight () {
  var h;
  var test1 = document.body.scrollHeight;
  var test2 = document.body.offsetHeight;
  if (test1 > test2) {// all but Explorer Mac
    h = test1;
  } else {// Explorer Mac;
     //would also work in Explorer 6 Strict, Mozilla and Safari
    h = test2;
  }
  return h;
}

/*
 * Returns the current documents width.
 */
function pWidth() {
  var w;
  var test1 = document.body.scrollWidth;
  var test2 = document.body.offsetWidth;
  if (test1 > test2) {// all but Explorer Mac
    w = test1;
  } else {// Explorer Mac;
     //would also work in Explorer 6 Strict, Mozilla and Safari
    w = test2;
  }
  return w;
}

/*
 * Returns the current windows height.
 */
function wHeight() {
  var h;
  if (self.innerHeight) {// all except Explorer
    h = self.innerHeight;
  } else if (document.documentElement && document.documentElement.clientHeight) {
    // Explorer 6 Strict Mode
    h = document.documentElement.clientHeight;
  } else if (document.body) { // other Explorers
    h = document.body.clientHeight;
  } else {
    h = 0;
  }
  return h;
}

/*
 * Returns the current windows width.
 */
function wWidth() {
  var w;
  if (self.innerWidth) {// all except Explorer
    w = self.innerWidth;
  } else if (document.documentElement && document.documentElement.clientWidth) {
    // Explorer 6 Strict Mode
    w = document.documentElement.clientWidth;
  } else if (document.body) { // other Explorers
    w = document.body.clientWidth;
  } else {
    w = 0;
  }
  return w;
}

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
    var target = isFramed() ? parent.admin_content : this;
    if (!isFramed()) {
       if (parent.parent && parent.parent.admin_content) {
           target = parent.parent.admin_content;
       }
    }
    openPageIn(url, target);
}

/*
 * Opens a new page in the given target frame.
 * It also shows the loading screen during loading
 */
function openPageIn(url, target) {
    loadingOn();
    target.location.href = url;
}

/*
 * Sets the current displayed context help.
 * It only works if framed.
 */
function setContextHelp(contextHelp) {
    if (parent && parent.admin_menu && parent.admin_menu.setInternalContextHelp) {
        parent.admin_menu.setInternalContextHelp(contextHelp);
    } else if (parent && parent.parent && parent.parent.admin_menu && parent.parent.admin_menu.setInternalContextHelp) {
        parent.parent.admin_menu.setInternalContextHelp(contextHelp);
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
    if (parent && parent.defContextHelp) {
      return parent.defContextHelp;
    } else if (this.defContextHelp) {
      return this.defContextHelp;
    }
    return "";
}

/*
 * Sets the active item in the menu frame.
 * It only works if framed.
 */
function setActiveItemByName(name) {
    if (parent && parent.admin_menu && parent.admin_menu.setActiveItem) {
        parent.admin_menu.setActiveItem(name);
    } else if (parent && parent.parent && parent.parent.admin_menu && parent.parent.admin_menu.setActiveItem) {
        parent.parent.admin_menu.setActiveItem(name);
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
   var t = false;
   if (parent && parent.parent && parent.parent.admin_content) {
     t = true;
   }
   if (!isFramed() && !t) {
     showHelp(obj_id);
   } else {
     var context = obj_id;
     var writezone = document.getElementById('help' + obj_id);
     if (writezone) {
       context = writezone.firstChild.nodeValue;
     }
     setContextHelp(context);
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
   var t = false;
   if (parent && parent.parent && parent.parent.admin_content) {
     t = true;
   }
   if (!isFramed() && !t) {
     hideHelp(obj_id);
   } else {
     setContextHelp('');
   }
}

/*
 * Shows the context help when having one helptext for several buttons.
 * If framed, it will be shown in the menu frame;
 * if not, as a help ballon.
 * The obj_id argument should be the id of a div tag, where the ballon while be displayed.
 * The help_id argument should be the id of a div tag, which contains the help text.
 */
function sMHS(obj_id, help_id) {
   var t = false;
   if (parent && parent.parent && parent.parent.admin_content) {
     t = true;
   }
   if (!isFramed() && !t) {
     showHelpX(obj_id, help_id);
   } else {
     var context = obj_id;
     var writezone = document.getElementById('help' + help_id);
     if (writezone) {
       context = writezone.firstChild.nodeValue;
     }
     setContextHelp(context);
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
    try {
    	target.document.getElementById("loaderContainer").style.display = "";
	} catch (e) {}
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
    return parent && parent.admin_content && parent.admin_menu;
}


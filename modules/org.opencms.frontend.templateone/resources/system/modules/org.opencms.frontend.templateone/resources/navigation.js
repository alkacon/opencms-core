
var menu = new Array();
var activeMenu = -1;
var menuTimer = null;
var initialized = false;

function initMenu(id) {
    eval("menu[" + id + "] = new xbPositionableElement('xbmenu" + id + "',  'abs', 'abs', 0, 18, 'xbparent" + id + "', false)");
}

function init() {
    var menuCount = 0;
    do {
        var foundMenu = null;
        if (document.getElementById) {
            foundMenu = document.getElementById("xbmenu" + menuCount);
        } else if (document.all) {
            foundMenu = document.all["xbmenu" + menuCount];
        } else if (document.layers) {
            foundMenu = document.layers["xbmenu" + menuCount];
        }
        if (foundMenu != null) {
            initMenu(menuCount);
        }
        menuCount++;
    } while(foundMenu != null);

    for (i = 0; i < menu.length; i++) {
        menu[i].init();
    }
    
    initialized = true;
}

function resize() {
    hideMenu();
}

function stopMenuTimer() {
    if (menuTimer != null) {
        clearTimeout(menuTimer);
        menuTimer = null;
    }
}

function startMenuTimer() {
    stopMenuTimer();
    menuTimer = setTimeout("updateMenu()", 2000);
}

function showMenu(id) {
	if (! initialized) return;
    if (activeMenu >= 0) menu[activeMenu].hide();
    activeMenu = id;
    menu[activeMenu].show();
    startMenuTimer();
}

function hideMenu() {
	if (! initialized) return;
    if (activeMenu >= 0) menu[activeMenu].hide();
    activeMenu = -1;
}

function updateMenu() {
	if (! initialized) return;
    var isActive = false;
    if (activeMenu >= 0) {
        isActive = menu[activeMenu].checkPointer();
    }
    if (! isActive) {
        hideMenu();
    } else {
        startMenuTimer();
    }
}

// Event MouseClick
document.onclick=hideMenu;
// Event MouseMove
if ( document.layers ) { document.captureEvents(Event.MOUSEMOVE) }
document.onmousemove=moveMouse;


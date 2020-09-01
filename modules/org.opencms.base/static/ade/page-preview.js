function openEditor() {
    var target = window.location.href;
    if (target.indexOf("__disableDirectEdit") > 0) {
        target = target.replace("__disableDirectEdit=true", "__disableDirectEdit=false");
    } else {
        var anchor = "";
        if (target.indexOf("#") > 0) {
            anchor = target.substring(target.indexOf("#"));
            target = target.substring(0, target.indexOf("#"));
        }
        if (target.indexOf("?") > 0) {
            target += "&";
        } else {
            target += "?";
        }
        target += "__disableDirectEdit=false";
        target += anchor;
    }
    window.location.href = target;
}

function sendHeartbeat() { 
	// previewSettings is injected into the page 
	navigator.sendBeacon(previewSettings.heartbeatUrl, "");
}

function injectButton() {
    if (self === top) {
        var injectElement = document.createElement("div");
		// previewSettings is injected into the page 
        injectElement.innerHTML = "<button id='opencms-leave-preview' class='opencms-icon opencms-icon-edit-point cmsState-up' onClick='openEditor()' style='left:" + previewSettings.buttonLeft + ";' title='"+previewSettings.titleMessage+"'></button>";
        document.body.appendChild(injectElement);
        setInterval(sendHeartbeat, 15000);
    }
}
document.addEventListener("DOMContentLoaded", injectButton);
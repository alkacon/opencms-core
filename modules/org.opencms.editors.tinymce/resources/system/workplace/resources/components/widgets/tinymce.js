/*
 * These scripts are required for the TinyMCE widgets in the xml content editor
 */

// FCKeditor global objects
var editorInstances = new Array();
var contentFields = new Array();
var expandedToolbars = new Array();
var editorsLoaded = false;

// generates the TinyMCE instances
function generateEditors() {
    for (var i=0; i<editorInstances.length; i++) {
        var editInst = editorInstances[i];
        editInst.ReplaceTextarea();
    }
}

// writes the HTML from the editor instances back to the textareas
function submitHtml(form) {
    for (var i=0; i<contentFields.length; i++) {
        var cf = contentFields[i];
        var editInst = tinyMCE.get('ta_' + cf.getAttribute('id', 0));
        var editedContent = editInst.getContent();
        if (editedContent != null && editedContent != "null") {
            cf.value = encodeURIComponent(editedContent);
        }
    }
}

// checks if at least one of the editors was loaded successfully
function editorsLoaded() {
    return editorsLoaded;
}




function setupTinyMCE(editor) {
   editor.onInit.add(function(editor) {
       addCustomShortcuts(editor);
   });
   if (tinyMCE.isWebKit) {
      // fix weird layout problem in Chrome 
      // If we don't do this, the button bar won't wrap if the window is too small 
      editor.onInit.add(function() {
         var id = editor.id + "_tbl";
         var baseElem = document.getElementById(id); 
         var modElem = $(baseElem).parents(".cmsTinyMCE").get(0);
         $(modElem).removeClass("cmsTinyMCE");
         window.setTimeout(function() { $(modElem).addClass("cmsTinyMCE"); } , 1);
      });
   }
   if (tinyMCE.isIE7 && !window.cmsIE7CssFixForTinyMCE) {
      // fixing the issue where the fullscreen mode editor is positioned below the XML content editor instead of overlaying it 
      $("head").append("<style type='text/css'>#mce_fullscreen_container { position: absolute !important; }</style>");
      window.cmsIE7CssFixForTinyMCE = true; 
   }
}

// initializes the tinyMCE editor instance with the given options
function initTinyMCE(options){
    // default options:
    var defaults={
            relative_urls: false,
            remove_script_host: false,
            skin_variant: 'ocms',
            mode: "exact",
            theme: "advanced",
            file_browser_callback: 'cmsTinyMceFileBrowser',
            setup: function(editor) { setupTinyMCE(editor); },
            plugins: "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,-opencms",
            theme_advanced_toolbar_location: "top",
            theme_advanced_toolbar_align: "left",
            theme_advanced_statusbar_location: "bottom",
            width: '100%',
            theme_advanced_resizing: false,
            theme_advanced_resizing_use_cookie: false
          };
    // check for fullpage option
    if (options["fullpage"]){
        defaults["plugins"]+=",fullpage";
    }
    if (options["style_formats"]){
        try{
        options["style_formats"]=eval('('+options["style_formats"]+')');
        }catch(error){
            delete options["style_formats"];
            alert("Error while parsing style formats option for tinyMCE: "+error);
        }
    }
    // the fullpage attribute needs to be removed otherwise tinyMCE won't start
    delete options["fullpage"];
    $.extend(defaults, options);
    tinyMCE.init(defaults);
}

function addCustomShortcuts(editor){
    editor.addShortcut('ctrl+shift+z','','Redo');
    editor.addShortcut('ctrl+l','','mceAdvLink');
}
    // This method is alwas called on form submit
    function doUpdate() {
        var len = document.all.length;
        for (i=0; i<len; i++) {
            obj = document.all(i);
            if ((obj != null) && (obj.name != null)) {
                var pos = obj.name.indexOf("-unescaped");
                if (pos >= 0) {
                    var source = obj.name.substring(0, pos);
                    if (obj.value != null) {
                        document.all(source).value = obj.value; 
                    } else {
                        document.all(source).value = obj.innerHTML;
                    }
                }
            }
        }
        filterSpanTags();
        updateForm();
    }
    
    function filterSpanTags() {
        var len = document.all.length;
        for (i=0; i<len; i++) {
            obj = document.all(i);
            if ((obj != null) && (obj.name != null)) {
                var pos = obj.name.indexOf("-unescaped");
                if (pos >= 0) {
                    var source = obj.name.substring(0, pos);
                    var htmlSrc = "";
                    
                    if (obj.value != null) {
                    	htmlSrc += obj.value;                      
                    } else {
                    	htmlSrc += obj.innerHTML;
                    }

                   	var noOpeningSpanTags = "" + htmlSrc.replace( /<(span)[^>]*>/gi, "" );
                   	var noClosingSpanTags = "" + noOpeningSpanTags.replace( /<(\/span)>/gi, "" );
                    document.all(source).value = "" + noClosingSpanTags;                     
                }
            }
        }
    }    

    // This mehtod is called on form load
    function doInit() {    	
        // Check what base URL the server operates on (needed below to maintain relative links)
        var relPrefix = null;
        var linktester = document.all('edit_linktester');
        if (linktester) {
        	linktester.innerHTML = "<a href='/'></a>";
        	var relPrefix = linktester.firstChild.href;
        }
        // Now init all form elements        
        var len = document.all.length;
        for (i=0; i<len; i++) {
            obj = document.all(i);
            if ((obj != null) && (obj.name != null)) {
                var pos = obj.name.indexOf("-unescaped");
                if (pos >= 0) {
                    var source = obj.name.substring(0, pos);
                    if (obj.value != null) {
                        obj.value = unescape(document.all(source).value);
                    } else {
                        // Warning: This will replace all relative links and make them absolute
                        obj.innerHTML = unescape(document.all(source).value);
                    }
                }
            }
        }   
        // Check all "A" tags in the page that have been made absolute and make them relative again
        if (linktester) {
            allTags = document.all.tags("A");
            for (i=0; i<allTags.length; i++) {
                curTag = allTags[i];                        	
                var pos = curTag.href.indexOf(relPrefix);
                if (pos >= 0) {
                    var cutHref = curTag.href.substring(pos + relPrefix.length - 1);
                    curTag.setAttribute("HREF", cutHref, 0);
                }
            }      
        }
        // Finally call user initForm() method
        initForm();
    }
    
    // The following methods create a near - wysiwyg edit area
    var activeEdit = "null";
    var cancelToggle = true;
    function activate(editor) {
        if (activeEdit != editor) {
            activeEdit = editor;
            var elEdit = document.all(activeEdit);
            var elBbar = document.all("buttonbar");
            elEdit.focus();
            elBbar.style.display="none";
            elEdit.parentNode.insertBefore(elBbar, elEdit);            
            elBbar.style.pixelWidth = elEdit.style.pixelWidth;
            elBbar.style.display="inline";            
            document.recalc(true);
            elBbar.blur();
        }
        window.event.cancelBubble = true;
    }   

    function deactivate(editor) {
        activeEdit = "null";
        document.all("buttonbar").style.display="none";
    }

    var lastButton = null;
    // Makes button look sunken when button is clicked
    function BtnDown()
    {
        var src = window.event.srcElement;
        if ( (src != null) && (src.name != null) && (src.name.indexOf("edit_") >= 0) ) {        
            src.style.borderStyle = "inset";
            lastButton = src;
     		cancelToggle = false;
        } 
        window.event.cancelBubble = true;
    }

    // Makes button look raised when button is released
    function BtnUp()
    {
        if (lastButton != null) {
            lastButton.style.borderStyle = "outset";
        }
        cancelToggle = true;
    }

    var linkEditor = null;             
    var linkEditorAll = null;
    var linkEditorRange = null;
    var linkEditorSelection = null;
    var linkEditorStyleInputs = false;

    // Executes commands depending on which button has been pushed
    function Toggle()
    {
    	if (cancelToggle) return;
    	
        // get button label
        var text = null;
        var src = window.event.srcElement.name;
        if ( (src != null) && (src.indexOf("edit_") >= 0) ) {
            text = src.substring(src.indexOf("edit_")+5, src.length);
        } else {
            text = window.event.srcElement.innerText;        
        }

        if (text == "N") {
        	filterSpanTags();
            document.execCommand("RemoveFormat");
            document.execCommand("formatBlock", "", "<P>");
        }

        else if (text == "B")
            document.execCommand("Bold");

        else if (text == "I")
            document.execCommand("Italic");

        else if (text == "U")
            document.execCommand("Underline");

        else if (text == "S")
            document.execCommand("StrikeThrough");

        else if (text == "LINK") {            
            linkEditor = document.all(activeEdit);        
            linkEditorAll = document.all;  
            linkEditorSelection = document.selection;
            linkEditorRange = document.body.createTextRange();
            linkwin = window.open('../../../../workplace/action/edit_html_linkall.html','SetLink', "width=450, height=175, resizable=no, top=300, left=250");        
        }
        	
        else if (text == "UL")
        	document.execCommand("InsertOrderedList");
       
        else if (text == "OL")
            document.execCommand("InsertUnorderedList");

        else if (text == "I^")
            document.execCommand("SuperScript");

        else if (text == "I_")
            document.execCommand("SubScript");

        else if (text == "|--")
            document.execCommand("JustifyLeft");

        else if (text == "-|-")
            document.execCommand("JustifyCenter");

        else if (text == "--|")
            document.execCommand("JustifyRight");

        else if (text == ">>")
            document.execCommand("Indent");

        else if (text == "<<")
            document.execCommand("Outdent");
            
        else if (text == "HEADING") {       
            document.execCommand("formatBlock", "", "<H2>");            
        }

        else if (text == "+")
            if (document.all(activeEdit).style.overflow=="auto") {
                document.all(activeEdit).style.overflow="visible"
            } else {
                document.all(activeEdit).style.overflow="auto"
            };
    } 
    
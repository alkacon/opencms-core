function findObj(n, d) 
{ 
alert("findObj");
	var p,i,x;  
	if(!d) d=document; 
	if((p=n.indexOf("?"))>0&&parent.frames.length) 
	{
	    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);
	}
	if(!(x=d[n])&&d.all) x=d.all[n]; 
	for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  	for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=findObj(n,d.layers[i].document); return x;
}

function showHideLayers() 
{ 
	var i,p,v,obj,args=showHideLayers.arguments;
  	for (i=0; i<(args.length-2); i+=3) if ((obj=findObj(args[i]))!=null) 
	{ 
		v=args[i+2];
    	if (obj.style) 
		{ 
			obj=obj.style; v=(v=='show')?'visible':(v='hide')?'hidden':v; 
		}
    	obj.visibility=v; 
	}
}

function doCopyValue() 
{	
    document.Editor.HTMLPreviewTextArea.value = "" + myEditor.document.body.innerHTML;
    doTransform();
}

function trimIt(trimStr)
{    
	var i = trimStr.length;
	while (trimStr.charAt(0) == " ")
            {          
		trimStr = trimStr.substring(1,i) 
		i = i-1;
	}
	while (trimStr.charAt(trimStr.length-1) == " ")
	{
		trimStr = trimStr.substring( 0,trimStr.length-1) 
	}
	return trimStr;

}

function doMakeBold() 
{
    // Get a text range for the selection
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Bold")
    tr.select()
    doCopyValue();
}

function doCopy() 
{
    // Get a text range for the selection
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Copy", false)
   doCopyValue();
}

function doPaste() 
{
    // Get a text range for the selection
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Paste", false)
    doCopyValue();
}

function doInsertUL() 
{
    // Get a text range for the selection
    frames.myEditor.focus();
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("InsertUnorderedList", false)
    doCopyValue();
}

function doIndent() 
{
    // Get a text range for the selection
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Indent", false)
    doCopyValue();
}

function doUnIndent() 
{
    // Get a text range for the selection
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Outdent", false)
    doCopyValue();
}

function doMakeItalic() 
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Italic")
    tr.select()
    doCopyValue();    
}

function doMakeUnderline() 
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("Underline")
    tr.select()
    doCopyValue();    
}

function doInsertLink()
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("CreateLink")
    tr.select()
    doCopyValue();    
}

function doUnLink()
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("UnLink")
    tr.select()
    doCopyValue();    
}

function getActiveText(e) 
{ 
	text = (frames.myEditor.document.all) ? frames.myEditor.document.selection.createRange().text : frames.myEditor.document.getSelection();
    return text;
}

function getPos(el,which) 
{
    var iPos=0
    while (el.offsetParent!=null) 
    {
      iPos+=el["offset"+which]
      el = el.offsetParent
      el.onfocus = new Function("displayToolbar(null,false)")
    }
    return iPos
}

function displayToolbar(ed, how) 
{
    var eb = document.all.editbar
    if (how)
      eb.style.display = "block"
    else
      eb.style.display = "none" 
    if (ed!=null) {
      eb.style.pixelTop = getPos(ed,"Top") + ed.offsetHeight + 1
      eb.style.pixelLeft = getPos(ed,"Left")
      eb._editor = window.frames[ed.id]
      eb._editor.setFocus()
    }
}

  
function doInsertComment(comment, highlightText, fullText) 
{ 
    tempOne = eval("'" + highlightText + "'");
  	originalStr = new RegExp(tempOne);
    replaceStr  = "<a href=# title='" + comment + "'>" + highlightText + "</a>";
    fullText = fullText.replace(originalStr, replaceStr);
    alert(fullText);
    return(fullText);
}


function doFontName(what)
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("FontName", false, what)
    tr.select()
    doCopyValue();    
}

function doFontSize(what)
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("FontSize", false, what)
    tr.select()
    doCopyValue();    
}

function doCreateAnchor()
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("CreateBookmark")
    tr.select()
    doCopyValue();    
}

function doFontColor(what)
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("ForeColor", false, what)
    tr.select()
    doCopyValue();  
}

function doSelectAll()
{
    var tr = frames.myEditor.document
    tr.execCommand("SelectAll", false, null)
}

function doPrint()
{
    frames.myEditor.print();
}

function doJustify(where)
{
    var tr = frames.myEditor.document.selection.createRange()
    if (where == 'Left') tr.execCommand("JustifyLeft")
    else if (where == 'Right') tr.execCommand("JustifyRight")
    else if (where == 'Center') tr.execCommand("JustifyCenter")
    tr.select()
    doCopyValue();  
}

function doSwapMode(b) 
{
    var eb = document.all.myEditor
    eb.swapModes()
    b.value = eb.format + " Mode"
}

function doSave()
{
    var tr = frames.myEditor.document
    tr.execCommand("SaveAs", true)
}

function doRenderHTML() 
{
	myEditor.document.body.innerHTML=document.Editor.HTMLPreviewTextArea.value;
}

function doSelectColor(val) 
{
    var tr = frames.myEditor.document.selection.createRange()
    tr.execCommand("ForeColor", false, val)
    tr.select()
    doCopyValue();  
}

function setMediaAction(para) {
		window.top.body.admin_content.Editor.backofficepage.value=window.top.body.admin_head.backofficepage.value;
		switch(para) {
		case 1:	{
				document.Editor.media_action.value = "delPicture";
				document.Editor.submit();
				break;
				}
		case 2:	{	
				document.Editor.media_action.value = "editPicture";
				document.Editor.submit();
				break;
				}
		case 3: {
				document.Editor.media_action.value = "prevPicture";
				document.Editor.submit();
				break;
				}
		case 4: {
				document.Editor.media_action.value = "addPicture";
				document.Editor.submit();
				break;
				}
		case 5: {
				document.Editor.media_action.value = "clear";
				document.Editor.submit();
				break;
				}
		  }
}

	 
	 

// ===============================================
// JAVASCRIPT-FUNCTIONS OPENCMS
//
// Invokes servlet calls in Picture Browser:
// based on script from old cms version
//
// author:			m.schreiber
// company:			mindfact interaktive medien ag
// date:			08.02.2000
// update:		
// update author:	
// ===============================================


function updateSelection()
{
	if(document.form1.galerie.options[document.form1.galerie.selectedIndex].value)
	{
		top.selectedGallery = document.form1.galerie.selectedIndex;
		parent.frames[1].location.href=document.form1.galerie.options[document.form1.galerie.selectedIndex].value;
	}
}

function getSelection()
{
	document.form1.galerie.selectedIndex = top.selectedGallery;
}


var page=parseInt(1);  // the actual page
var filter="*";      // the actual filter
var DO_NEXT=1;
var DO_PREV=2;
var DO_SEARCH=3;
var DO_UPLOAD=4;


<!-- The update flag is set by the upload dialog. When it is set to one, the picture -->
<!-- list is reloaded (after a new image was added) -->

if (0 == 1)
{
 alert ("Update Flag gesetzt: Seite neu laden!");
 //parent.frames[1].location.href="#"+filter;
}


<!-- Main processing function, evaluates which button is pressed and activates the -->
<!-- according action -->

function doEdit(para)
{
 switch (para)
 {
  case 1: // next page
  {
    if (parent.frames[1].document.EOL !=true)  // no more pages available? Get this information from the picture list frame.
     {
      page++; // increase page number
      filter=document.form1.FilterInput.value;
      alert("Servlet: Nächste Seite der Galerie laden");
      //parent.frames[1].location.href = "#"+page+"&FILTER="+filter; //reload complete picture browser
     }
    break;
  }
  case 2: // next page
  {
    if (parent.frames[1].document.BOL !=true)
     {
      filter=document.form1.FilterInput.value;
      alert("Servlet: Vorhergehende Seite der Galerie laden");
      //parent.frames[1].location.href = "#"+page+"&FILTER="+filter; //reload complete picture browser
     }
    break;
   }
   case 3: // calls a servlet which does a full-text search on image-title and filename.
   {
     filter=document.form1.FilterInput.value;
     alert("Servlet: Über Titel oder Dateiname suchen");
     //parent.frames[1].location.href = "#"+filter;
     break;
   }
   case 4: // upload image, open the upload dialog.
   {
     filter=document.form1.FilterInput.value;
     parent.frames[1].location.href = "edit_html_piclist_upload.html";
     //openwin("/servlets/mht/system/def/action/upload?FOLDER=/pics/&BROWSER=1&FILTER="+filter);
     break;
   }
   default:
   {
		alert("Unknown Exception");
   }
 }
}


// ===============================================
// JAVASCRIPT-FUNCTIONEN OPENCMS
//
// dynamic write dropdown
// author:	unkonow
// company:	mindfact interaktive medien ag
// date:	?
// update:	03.02.2000
// update author: m.schreiber
// ===============================================

// Object for an Arry Entry
function RolleEntry(sText, sValue)
   {
      this.sText = sText;
      this.sValue = sValue;
   }

function writeUser()
   {
      if(navigator.appName.indexOf('Netscape') < 0 &&  parseInt(navigator.appVersion.substring(0,1)) < 4 )return;
      var SelHtml = '';
      SelHtml = '<select name="USER" width="150" STYLE="WIDTH: 150px" <OPTION VALUE="">...</OPTION>';
      for(var j = 1; j < iMaxOpts; j++)
      {
	 SelHtml += '<OPTION VALUE=""></OPTION>';
      }
      SelHtml += '</SELECT>';
	  document.open();
      document.write(SelHtml);
	  document.close();
   }

function setUser(objSel, bereich)
{
 	 	for(i = 0; i < objSel.length; i++)
		 {
	    	if(i < aRolle[bereich].length)
			{
				objSel.options[i].text = aRolle[bereich][i].sText;
	       		objSel.options[i].value = aRolle[bereich][i].sValue;
			}
			else
			{
				objSel.options[i].text = '';
				objSel.options[i].value = '';
	    	}
         }
		 objSel.selectedIndex = 0;		//reset selected User to default entry
}
  

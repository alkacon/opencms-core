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
  
   // arrays for Rolle
   
   var aRolle = new Array();
   aRolle['it'] = new Array();
   aRolle['html'] = new Array();
   aRolle['graf'] = new Array();
   aRolle['undef'] = new Array();
   

	aRolle['it'][0] = new RolleEntry('User bestimmen ...','');
	aRolle['it'][1] = new RolleEntry('a.kadzior','');
	aRolle['it'][2] = new RolleEntry('a.lucas','');
	aRolle['it'][3] = new RolleEntry('a.schouten','');
	aRolle['it'][4] = new RolleEntry('m.schleich','');
 
	aRolle['html'][0] = new RolleEntry('User bestimmen ...','');
	aRolle['html'][1] = new RolleEntry('s.fleskes','');
	aRolle['html'][2] = new RolleEntry('j.stoppenbach','');
	aRolle['html'][3] = new RolleEntry('m.schleich','');

	aRolle['graf'][0] = new RolleEntry('User bestimmen ...','');
	aRolle['graf'][1] = new RolleEntry('m.hahn','');
	aRolle['graf'][2] = new RolleEntry('m.seilz','');
	
	aRolle['undef'][0] = new RolleEntry('User bestimmen ...','');
	aRolle['undef'][1] = new RolleEntry('a.kadzior','');
	aRolle['undef'][2] = new RolleEntry('a.lucas','');
	aRolle['undef'][3] = new RolleEntry('a.schouten','');
	aRolle['undef'][4] = new RolleEntry('m.schleich','');
	aRolle['undef'][5] = new RolleEntry('s.fleskes','');
	aRolle['undef'][6] = new RolleEntry('j.stoppenbach','');
	aRolle['undef'][7] = new RolleEntry('m.hahn','');
	aRolle['undef'][8] = new RolleEntry('m.seilz','');

 

    var iMaxOpts = 0;
    for(var bereiche in aRolle)
    {
		if( iMaxOpts< aRolle[bereiche].length)
 		{
 	   		iMaxOpts = aRolle[bereiche].length;
 		}
    }
	

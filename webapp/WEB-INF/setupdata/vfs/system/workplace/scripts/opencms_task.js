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
	if(aRolle[bereich]) {
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
	}
	objSel.selectedIndex = 0;		//reset selected User to default entry
}
  
//------------------------------------------------------------------------------------
// check date
// 
// returns 0 if date is valid
// returns 1 if date is in the past
// returns 2 if the format is not correct
// returns 3 if the month is not correct
// returns 4 if the day is not correct
//------------------------------------------------------------------------------------
function checkTaskDate(eDat)
{
// get date
    schaltjahr=false;
    aktDat = new Date;
    aktTag= aktDat.getDate();
    aktMonat= aktDat.getMonth()+1;
    aktJahr= aktDat.getFullYear();
    
// split the userdate
    eTag = eDat.substring(0,2);
    eMonat = eDat.substring(3,5);
    eJahr = eDat.substring(6,10);
    

    if(eDat.length != 10 || isNaN(eTag) || isNaN(eMonat) || isNaN(eJahr))
		{
        return 2;
        }
    else
        {
        if(eJahr<aktJahr)
    		{
        	return 1;
    		}
    	else
    		{
        	if(eMonat<aktMonat && eJahr==aktJahr)
        		{
            	return 1;
        		}
        	else
        		{
            	if(eTag<aktTag && eMonat==aktMonat)
            		{
                	return 1;
            		}
        		}
    		}
        if(eMonat < 01 || eMonat > 12)
        {
        return 3;
        }
        else
            {
            if(eTag > 31 || eTag<=00)
            {
            return 4;
            }
            else
            {

// if must be split, to long for browser interpreter
                if(((eTag == 31) && (eMonat == 02)) || ((eTag == 31) && (eMonat == 04)) || ((eTag == 31) && (eMonat == 06)))
                {
                return 4;
                }
                if((eTag == 30 && eMonat == 02) || (eTag == 31 && eMonat==09) || (eTag==31 && eMonat==11))
                {
                return 4;
                }
                else
                {
                if(eTag==29 && eMonat == 02)
                    {
                    sj = eJahr%4;
                    if(sj==0)
                    schaltjahr=true;

                    sj = eJahr%100;
                    if(sj==0)
                    schaltjahr=false;
        
                    sj = eJahr%400;
                    if(sj==0)
                    schaltjahr=true;

                    if(!schaltjahr)
                    {
                    return 4;
                    }
                    }
                }
            }
        }       
    }
    return 0;
}
// ===============================================
// JAVASCRIPT-FUNCTIONEN OPENCMS
//
// dynamic timer
//
// author:			m.schleich
// company:			mindfact interaktive medien ag
// date:			24.01.2000
// update:		
// update author:	
// ===============================================

// global varibles and objects
var schaltjahr=false;

// get date
	aktDat = new Date;
	aktTag= aktDat.getDate();
	aktMonat= aktDat.getMonth()+1;
	aktJahr= aktDat.getFullYear();
// Object for month entry
function MonthEntry(sText, sValue)
   {
	  this.sText = sText;
      this.sValue = sValue;
   }
// Object for year entry
function YearEntry(sText, sValue)
   {
      this.sText = sText;
      this.sValue = sValue;
   }
// arrays
var aYear = new Array();
var aMonth = new Array();

for(var j=0; j<5; j++)
{
	help=aktJahr+j;
	aYear[j] = new YearEntry(help,help);
}

aMonth[0] = new MonthEntry('Januar',1);
aMonth[1] = new MonthEntry('Februar',2);
aMonth[2] = new MonthEntry('März',3);
aMonth[3] = new MonthEntry('April',4);
aMonth[4] = new MonthEntry('Mai',5);
aMonth[5] = new MonthEntry('Juni',6);
aMonth[6] = new MonthEntry('Juli',7);
aMonth[7] = new MonthEntry('August',8);
aMonth[8] = new MonthEntry('September',9);
aMonth[9] = new MonthEntry('Oktober',10);
aMonth[10] = new MonthEntry('November',11);
aMonth[11] = new MonthEntry('Dezember',12);

function writeYear() // fill the year and month selectbox
   {
      var SelHtml = '';
      SelHtml = '<select name="YEAR" width="100" STYLE="WIDTH:50px">';
      for(var j=0; j<5; j++)
      {
	 	SelHtml += '<OPTION VALUE=""></OPTION>';
      }
      SelHtml += '</SELECT>';
	  
	  var MoHtml = '';
	        MoHtml = '<select name="MONTH" width="150" STYLE="WIDTH:100px">';
	        for(var j=0; j<12; j++)
	        {
	  	 	if((j+1)==aktMonat)
	  		{
	  			MoHtml += '<OPTION VALUE="" selected></OPTION>';
	  		}
	  		else
	  		{
	  			MoHtml += '<OPTION VALUE=""></OPTION>';
	  		}
	        }
	        SelHtml += '</SELECT>';
	  
	  document.open;
      document.write(SelHtml);
	  document.write(MoHtml);
	  document.close;
   }

function writeMonth() // fill the Month selectbox
   {
	alert('in writeMonth');   
	  var MoHtml = '';
      MoHtml = '<select name="MONTH" width="150" STYLE="WIDTH:100px">';
      for(var j=0; j<12; j++)
      {
	 	if((j+1)==aktMonat)
		{
			MoHtml += '<OPTION VALUE="" selected></OPTION>';
		}
		else
		{
			MoHtml += '<OPTION VALUE=""></OPTION>';
		}
      }
      SelHtml += '</SELECT>';
	  document.open;
      document.write(MoHtml);
	  document.close;
   }

function fillBox(objSel, array, length)
{
	for(i=0; i<length; i++)
		{
			objSel.options[i].text = array[i].sText;
			objSel.options[i].value = array[i].sValue;
		}
}

  
  


//------------------------------------------------------------------------------------
// ceck date
// 
// author:	Michaela Schleich
// company:	mindfact interaktive medien ag
// date:	01.11.1999
// update:	14.01.2000
//
// input date (tt.mm.jjjj)
//
// returns FALSE if date is not a valid date
// returns TRUE if date valid
//------------------------------------------------------------------------------------
function checkDate(eDat)
{
// split the userdate
	eTag = eDat.substring(0,2);
	eMonat = eDat.substring(3,5);
	eJahr = eDat.substring(6,10);
	

	if(eJahr<aktJahr)
	{
		alert("Die Fälligkeit liegt in der Vergangenheit");
		return false;
	}
	else
	{
		if(eMonat<aktMonat)
		{
			alert("Die Fälligkeit liegt in der Vergangenheit");
			return false;
		}
		else
		{
			if(eTag<aktTag)
			{
				alert("Die Fälligkeit liegt in der Vergangenheit");
				return false;
			}
		}
	}
	
	
	if(eDat.length != 10 || isNaN(eTag) || isNaN(eMonat) || isNaN(eJahr))
		{
		alert("Bitte das Datum in Form von tt.mm.jjjj eingeben.");
		return false;
		}
	else
		{
		if(eMonat < 01 || eMonat > 12)
		{
		alert("Bitte überprüfen Sie den eingegeben Monat.");
		return false;
		}
		else
			{
			if(eTag > 31 || eTag<=00)
			{
			alert("Bitte ueberprüfen Sie den Tag des Datums.");
			return false;
			}
			else
			{

// if must be split, to long for browser interpreter
				if(((eTag == 31) && (eMonat == 02)) || ((eTag == 31) && (eMonat == 04)) || ((eTag == 31) && (eMonat == 06)))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
				}
				if((eTag == 30 && eMonat == 02) || (eTag == 31 && eMonat==9) || (eTag==31 && eMonat==11))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
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
					alert(eJahr + " ist kein Schaltjahr.");
					return false;
					}
					}
				}
			}
		}		
	}
	return true;
}

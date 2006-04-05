/*
* File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/opencms_timer.js,v $
* Date   : $Date: 2001/07/31 15:50:12 $
* Version: $Revision: 1.4 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// ===============================================
// JAVASCRIPT-FUNCTIONEN OPENCMS
//
// dynamic timer
//
// author:          m.schleich
// company:         mindfact interaktive medien ag
// date:            24.01.2000
// update:      
// update author:   
// ===============================================

// global varibles and objects
var mday=0;

// get date
    aktDat = new Date;
    aktTag= aktDat.getDate();
    aktMonat= aktDat.getMonth()+1;
    aktJahr= aktDat.getFullYear();

//to remeber the Date, which is slected by user
    userDat = new Date;
    userDay= userDat.getDate();
    userMonth= userDat.getMonth()+1;
    userYear= userDat.getFullYear();
    wday = 0;
    
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

for(var j=0; j<11; j++)
{
    help=aktJahr+j;
    aYear[j] = new YearEntry(help,help);
}

txt1='<tr><td align=right>1</td><td align=right>2</td><td align=right>3</td><td align=right>4</td><td align=right>5</td><td align=right>6</td><td align=right>7</td></tr>';
txt12='<tr><td align=right>8</td><td align=right>9</td><td align=right>10</td><td align=right>11</td><td align=right>12</td><td align=right>13</td><td align=right>14</td></tr>';
txt13='<tr><td align=right>15</td><td align=right>16</td><td align=right>17</td><td align=right>18</td><td align=right>19</td><td align=right>20</td><td align=right>21</td></tr>';
txt14='<tr><td align=right>22</td><td align=right>23</td><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td></tr>';
txt15a='<tr><td align=right>29</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt15b='<tr><td align=right>29</td><td align=right>30</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt15c='<tr><td align=right>29</td><td align=right>30</td><td align=right>31</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';


txt2='<tr><td align=right>&nbsp;</td><td align=right>1</td><td align=right>2</td><td align=right>3</td><td align=right>4</td><td align=right>5</td><td align=right>6</td></tr>';
txt22='<tr><td align=right>7</td><td align=right>8</td><td align=right>9</td><td align=right>10</td><td align=right>11</td><td align=right>12</td><td align=right>13</td></tr>';
txt23='<tr><td align=right>14</td><td align=right>15</td><td align=right>16</td><td align=right>17</td><td align=right>18</td><td align=right>19</td><td align=right>20</td></tr>';
txt24='<tr><td align=right>21</td><td align=right>22</td><td align=right>23</td><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td></tr>';
txt25a='<tr><td align=right>28</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt25b='<tr><td align=right>28</td><td align=right>29</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt25c='<tr><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt25d='<tr><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>31</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';


txt3='<tr><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>1</td><td align=right>2</td><td align=right>3</td><td align=right>4</td><td align=right>5</td></tr>';
txt32='<tr><td align=right>6</td><td align=right>7</td><td align=right>8</td><td align=right>9</td><td align=right>10</td><td align=right>11</td><td align=right>12</td></tr>';
txt33='<tr><td align=right>13</td><td align=right>14</td><td align=right>15</td><td align=right>16</td><td align=right>17</td><td align=right>18</td><td align=right>19</td></tr>';
txt34='<tr><td align=right>20</td><td align=right>21</td><td align=right>22</td><td align=right>23</td><td align=right>24</td><td align=right>25</td><td align=right>26</td></tr>';
txt35a='<tr><td align=right>27</td><td align=right>28</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt35b='<tr><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt35c='<tr><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt35d='<tr><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>31</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';


txt4='<tr><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>1</td><td align=right>2</td><td align=right>3</td><td align=right>4</td></tr>';
txt42='<tr><td align=right>5</td><td align=right>6</td><td align=right>7</td><td align=right>8</td><td align=right>9</td><td align=right>10</td><td align=right>11</td></tr>';
txt43='<tr><td align=right>12</td><td align=right>13</td><td align=right>14</td><td align=right>15</td><td align=right>16</td><td align=right>17</td><td align=right>18</td></tr>';
txt44='<tr><td align=right>19</td><td align=right>20</td><td align=right>21</td><td align=right>22</td><td align=right>23</td><td align=right>24</td><td align=right>25</td></tr>';
txt45a='<tr><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt45b='<tr><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt45c='<tr><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt45d='<tr><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>31</td><td align=right>&nbsp;</td></tr>';


txt5='<tr><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>1</td><td align=right>2</td><td align=right>3</td></tr>';
txt52='<tr><td align=right>4</td><td align=right>5</td><td align=right>6</td><td align=right>6</td><td align=right>8</td><td align=right>9</td><td align=right>10</td></tr>';
txt53='<tr><td align=right>11</td><td align=right>12</td><td align=right>13</td><td align=right>14</td><td align=right>15</td><td align=right>16</td><td align=right>17</td></tr>';
txt54='<tr><td align=right>18</td><td align=right>19</td><td align=right>20</td><td align=right>21</td><td align=right>22</td><td align=right>23</td><td align=right>24</td></tr>';
txt55a='<tr><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt55b='<tr><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt55c='<tr><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>&nbsp;</td></tr>';
txt55d='<tr><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td><td align=right>31</td></tr>';


txt6='<tr><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>1</td><td align=right>2</td></tr>';
txt62='<tr><td align=right>3</td><td align=right>4</td><td align=right>5</td><td align=right>6</td><td align=right>7</td><td align=right>8</td><td align=right>9</td></tr>';
txt63='<tr><td align=right>10</td><td align=right>11</td><td align=right>12</td><td align=right>13</td><td align=right>14</td><td align=right>15</td><td align=right>16</td></tr>';
txt64='<tr><td align=right>17</td><td align=right>18</td><td align=right>19</td><td align=right>20</td><td align=right>21</td><td align=right>22</td><td align=right>23</td></tr>';
txt65a='<tr><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt65b='<tr><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>&nbsp;</td></tr>';
txt65c='<tr><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td><td align=right>30</td></tr>';
txt66='<tr><td align=right>31</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';

txt7='<tr><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>1</td></tr>';
txt72='<tr><td align=right>2</td><td align=right>3</td><td align=right>4</td><td align=right>5</td><td align=right>6</td><td align=right>7</td><td align=right>8</td></tr>';
txt73='<tr><td align=right>9</td><td align=right>10</td><td align=right>11</td><td align=right>12</td><td align=right>13</td><td align=right>14</td><td align=right>15</td></tr>';
txt74='<tr><td align=right>16</td><td align=right>17</td><td align=right>18</td><td align=right>19</td><td align=right>20</td><td align=right>21</td><td align=right>22</td></tr>';
txt75a='<tr><td align=right>23</td><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>&nbsp;</td></tr>';
txt75b='<tr><td align=right>23</td><td align=right>24</td><td align=right>25</td><td align=right>26</td><td align=right>27</td><td align=right>28</td><td align=right>29</td></tr>';
txt76a='<tr><td align=right>30</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';
txt76b='<tr><td align=right>30</td><td align=right>31</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td><td align=right>&nbsp;</td></tr>';


// generates two selctboxes for year and month
function writeSel() // fill the year and month selectbox
   {
      var SelHtml = '';
      var MoHtml = '';
      if(ns)
      {
         SelHtml = '<select name="YEAR" width="50" STYLE="WIDTH:50px">';
         SelHtml += '<select name="YEAR" width="50" STYLE="WIDTH:50px" onChange="selectDays(1,TIMER.MONTH.options[TIMER.MONTH.selectedIndex].value,this.options[this.selectedIndex].value);">';
      }
      else
      {
        SelHtml = '<select name="YEAR" width="100" STYLE="WIDTH:50px" onChange="selectDays(1,TIMER.MONTH.options[TIMER.MONTH.selectedIndex].value,this.options[this.selectedIndex].value);">';
      }
      for(var j=0; j<11; j++)
      {
        SelHtml += '<OPTION VALUE=""></OPTION>';
      }
      SelHtml += '</SELECT>';
      
      
      MoHtml = '</td><td><select name="MONTH" width="100" STYLE="WIDTH:100px" onChange="selectDays(1,this.options[this.selectedIndex].value,TIMER.YEAR.options[TIMER.YEAR.selectedIndex].value);">';
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
        MoHtml += '</SELECT></td>';
            
      
      SelHtml=  new String( SelHtml+MoHtml );
      
      document.open();
      document.clear();
      document.write(SelHtml);
      document.close();
   }

// fills the two selctboxes for year and month
function fillBox(objSel, array, length)
{
    for(i=0; i<length; i++)
        {
            objSel.options[i].text = array[i].sText;
            objSel.options[i].value = array[i].sValue;
        }
}

// decides which layer with days is shown
function selectDays(day, month, year)
{
    mday = checkTimerDate(month, year);
    userDat.setFullYear(year);
    userDat.setMonth(month-1);
    userDat.setDate(1);
    wday = userDat.getUTCDay();
    if (wday==0){wday=7;}    //sunday is 0 in Javasicrtpt
    
    hidelyr('days');    
    
        switch (mday)
        {
            case 31:
            {
                switch (wday)
                {
                    case 1:
                    {
                        txt=txt1+txt12+txt13+txt14+txt15c;
                        break;
                    }
                    case 2:
                    {
                        txt=txt2+txt22+txt23+txt24+txt25d;
                        break;
                    }
                    case 3:
                    {
                        txt=txt3+txt32+txt33+txt34+txt35d;
                        break;
                    }
                    case 4:
                    {
                        txt=txt4+txt42+txt43+txt44+txt45d;
                        break;
                    }
                    case 5:
                    {
                        txt=txt5+txt52+txt53+txt54+txt55d;
                        break;
                    }
                    case 6:
                    {
                        txt=txt6+txt62+txt63+txt64+txt65c+txt66;
                        break;
                    }
                    case 7:
                    {
                        txt=txt7+txt72+txt73+txt74+txt75b+txt76b;
                        break;
                    }
                }
                break;
            }
            case 30:
            {
                switch (wday)
                {
                    case 1:
                    {
                        txt=txt1+txt12+txt13+txt14+txt15b;
                        break;
                    }
                    case 2:
                    {
                        txt=txt2+txt22+txt23+txt24+txt25c;
                        break;
                    }
                    case 3:
                    {
                        txt=txt3+txt32+txt33+txt34+txt35c;
                        break;
                    }
                    case 4:
                    {
                        txt=txt4+txt42+txt43+txt44+txt45c;
                        break;
                    }
                    case 5:
                    {
                        txt=txt5+txt52+txt53+txt54+txt55c;
                        break;
                    }
                    case 6:
                    {
                        txt=txt6+txt62+txt63+txt64+txt65c;
                        break;
                    }
                    case 7:
                    {
                        txt=txt7+txt72+txt73+txt74+txt75b+txt76a;
                        break;
                    }
                }
                break;
            }
            case 29:
            {
                switch (wday)
                {
                    case 1:
                    {
                        txt=txt1+txt12+txt13+txt14+txt15a;
                        break;
                    }
                    case 2:
                    {
                        txt=txt2+txt22+txt23+txt24+txt25b;
                        break;
                    }
                    case 3:
                    {
                        txt=txt3+txt32+txt33+txt34+txt35b;
                        break;
                    }
                    case 4:
                    {
                        txt=txt4+txt42+txt43+txt44+txt45b;
                        break;
                    }
                    case 5:
                    {
                        txt=txt5+txt52+txt53+txt54+txt55b;
                        break;
                    }
                    case 6:
                    {
                        txt=txt6+txt62+txt63+txt64+txt65b;
                        break;
                    }
                    case 7:
                    {
                        txt=txt7+txt72+txt73+txt74+txt75b;
                        break;
                    }
                }
                break;
            }
            case 28:
            {
                switch (wday)
                {
                    case 1:
                    {
                        txt=txt1+txt12+txt13+txt14;
                        break;
                    }
                    case 2:
                    {
                        txt=txt2+txt22+txt23+txt24+txt25a;
                        break;
                    }
                    case 3:
                    {
                        txt=txt3+txt32+txt33+txt34+txt35a;
                        break;
                    }
                    case 4:
                    {
                        txt=txt4+txt42+txt43+txt44+txt45a;
                        break;
                    }
                    case 5:
                    {
                        txt=txt5+txt52+txt53+txt54+txt55a;
                        break;
                    }
                    case 6:
                    {
                        txt=txt6+txt62+txt63+txt64+txt65a;
                        break;
                    }
                    case 7:
                    {
                        txt=txt7+txt72+txt73+txt74+txt75a;
                        break;
                    }
                }
                break;
            }
    }
    txt = '<table border=0 cellspacing=1 cellpadding=4><tr><td align=center>'+aWeekday[0]+'</td><td align=center>'+aWeekday[1]+'</td><td align=center>'+aWeekday[2]+'</td><td align=center>'+aWeekday[3]+'</td><td align=center>'+aWeekday[4]+'</td><td align=center>'+aWeekday[5]+'</td><td align=center>'+aWeekday[6]+'</td></tr><tr><td colspan=7><hr></td></tr>'+txt+'</table>';
    eval(lyrtxt);
    eval(layerzeigen_01+'days'+layerzeigen_02);
    userYear=year;
    if(month<10)
    {
        userMonth='0'+month;
    }
    else
    {
        userMonth=month;
    }
    changeDay(wday);

}

// changes the selected day
function changeDay(day)
{
    hilf=wday-1;
    hilf2=day-1;
    
    if(day>=wday && day<(mday+wday))
    {
        for(i=0; i<37; i++)
        {
            eval(imgonlyr+'images[i].src = "' + resourcesUri + 'empty.gif"');
        }
        eval(imgonlyr+'images[hilf2].src = "' + resourcesUri + 'circle.gif"');
        if((day-hilf)<10)
            {
                userDay='0'+new String(day-hilf);
            }
        else
        {
            userDay=day-hilf;
        }
        
    }
}
  


//------------------------------------------------------------------------------------
// ceck days
// 
// author:  Michaela Schleich
// company: mindfact interaktive medien ag
// date:    25.01.2000
// update:  
//
// input date (int month, int year)
//
// returns days per month
//------------------------------------------------------------------------------------
function checkTimerDate(month, year)
{
    var schaltjahr=false;
    
                if(month == 2)
                {
                    sj = year%4;
                    if(sj==0)
                    schaltjahr=true;

                    sj = year%100;
                    if(sj==0)
                    schaltjahr=false;
        
                    sj = year%400;
                    if(sj==0)
                    schaltjahr=true;
                }

                if(schaltjahr)
                {
                    return 29;
                }
                else
                {
                    if(month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12)
                    {
                        return 31;
                    }
                    else
                    {
                        if(month == 2)
                        {
                            return 28;
                        }
                        else
                        {
                            return 30;
                        }
                    }
                }

}

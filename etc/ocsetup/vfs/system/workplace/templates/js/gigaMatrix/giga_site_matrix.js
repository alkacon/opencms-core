
function siteMatrix(x, y) {

    // properties
	this.numOfSiteDomains = 0;
	this.numOfSiteTypes = 0;
	this.numOfSiteNodes = 0;
	this.siteDomains = new Array();
	this.siteTypes = new Array();
	this.siteNodes = new Array();
	this.siteDomainsIdx = new Array();
	this.siteTypesIdx = new Array();
	this.siteNodesIdx = new Array();
	
	defLayerColor = '#ffffff'; // this is a global var from warp_layer.js
	this.warpLayer =  new warpLayer(0,0,1,1); // create a layer for the matrix.
	setStyle(this.warpLayer.name, 'visibility', 'visible');
	this.warpLayer.move(x, y);
	
	// methods	

	this.addSiteDomain = _addSiteDomain;
	this.addSiteType = _addSiteType;
	this.addSiteNode = _addSiteNode;
	
	this.drawMatrix = _drawMatrix;
	this.getSiteNode = _getSiteNode;

}


function _addSiteNode(cmsSiteId, cmsSiteName, domainIdRef, siteTypeIdRef, siteEnabled) {

	this.siteNodesIdx[++this.numOfSiteNodes] = cmsSiteId;

	this.siteNodes[cmsSiteId] = new Array();
	this.siteNodes[cmsSiteId]["domainIdRef"] = domainIdRef;
	this.siteNodes[cmsSiteId]["siteTypeIdRef"] = siteTypeIdRef;
	this.siteNodes[cmsSiteId]["cmsSiteId"] = cmsSiteId;
	this.siteNodes[cmsSiteId]["cmsSiteName"] = cmsSiteId;
	this.siteNodes[cmsSiteId]["siteEnabled"] = siteEnabled;
}


function _getSiteNode(domainIdRef, siteTypeIdRef) {

	
	for(var n = 1; n <= this.numOfSiteNodes; n++) {

		if(this.siteNodes[this.siteNodesIdx[n]]["domainIdRef"] == domainIdRef) {
		
			if(this.siteNodes[this.siteNodesIdx[n]]["siteTypeIdRef"] == siteTypeIdRef) {
			
				return this.siteNodesIdx[n];
				break;
			}
		}
	
	}
	
	return '';
}


function _addSiteDomain(domainId, domainCnt, domainCountry) {


	this.siteDomainsIdx[++this.numOfSiteDomains] = domainId;
	this.siteDomains[domainId] = new Array();
	this.siteDomains[domainId]["domainId"] = domainId; // id
	this.siteDomains[domainId]["domainCnt"] = domainCnt; // content
	this.siteDomains[domainId]["idx"] = this.numOfSiteDomains; // index
	this.siteDomains[domainId]["domainCountry"] = domainCountry; // country desc. i.e 'Denmark'.
}


function _addSiteType(siteTypeId, siteTypeCnt) {

	this.siteTypesIdx[++this.numOfSiteTypes] = siteTypeId;

	this.siteTypes[siteTypeId] = new Array();
	this.siteTypes[siteTypeId]["siteTypeId"] = siteTypeId;
	this.siteTypes[siteTypeId]["siteTypeCnt"] = siteTypeCnt;

}


function _drawMatrix() {

	var tmpHtml = '';
	var tmpHtmlX = '';
	var tmpHtmlY = addTD( '<img src="../pics/sitedot.gif" width="100" height="1" alt="" border="0">' );;
	var checkChar = 'l';
	var checkCharStart = checkChar;
	var currSiteNodeId;
	var tmpNodeHtml = '';
	var menuColl = '';
	var menuCollNeg = '';
	var tmpAlt = '';
	
	
	for(var x = 1; x <= this.numOfSiteDomains; x++) { // create top headers
			
		tmpHtmlY += addTD( this.siteDomains[ this.siteDomainsIdx[x] ]["domainCnt"] + '<br><img src="../pics/sitedot.gif" width="40" height="1" alt="" border="0">', 'align=center class="headers"');
		
	}
	
	
	for(var y = 1; y <= this.numOfSiteTypes; y++) { // create matrix and side headers
	
		
		tmpHtmlX += addTD( this.siteTypes[ this.siteTypesIdx[y] ]["siteTypeCnt"], 'class="headers"');
		
		checkChar = checkCharStart;
			
		for(var x = 1; x <= this.numOfSiteDomains; x++) {
		
			currSiteNodeId = this.getSiteNode(this.siteDomainsIdx[x], this.siteTypesIdx[y])
			if(currSiteNodeId != '') {
			
				if(this.siteNodes[currSiteNodeId].siteEnabled) {
				
					tmpNodeHtml = 'on';
					menuColl = 'cM2';
					menuCollNeg = 'cM';
				}
				else {
				
					tmpNodeHtml = 'off';
					menuColl = 'cM';
					menuCollNeg = 'cM2';
					
				}
				
//				tmpAlt = this.siteTypes[ this.siteTypesIdx[y] ]["siteTypeCnt"] + ' - ' + this.siteDomains[ this.siteDomainsIdx[x] ]["domainCountry"]
				tmpAlt = this.siteNodes[currSiteNodeId].cmsSiteName;			
				tmpHtmlX += addTD( addLink("javascript:currSiteNodeId='" + currSiteNodeId + "';" + menuCollNeg + ".menus['m001'].showMenu(false,null);" + menuColl + ".menus['m001'].showMenu(true,null)" ,'<img src="../pics/sitedot_' + tmpNodeHtml + '.gif" width="20" height="20" border="0" alt="' + tmpAlt + '">'), 'align=center class="' + checkChar + 'grey"');
			}
			else {
			
				tmpHtmlX += addTD('&nbsp;', 'align=center class="' + checkChar + 'grey"');
			}
			
			
			
			if(checkChar == 'l') {
				checkChar = 'd';
			}
			else {
				checkChar = 'l';
			}
		
		}
		
		  tmpHtml += addTR(tmpHtmlX);
		  tmpHtmlX = '';
		  	
	}
	
	tmpHtml = addTR(tmpHtmlY) + tmpHtml;

	
	writeLayer(this.warpLayer.name, addTable(tmpHtml,'border=0 cellpadding=3 cellspacing=2 width=300'),0)
	

}

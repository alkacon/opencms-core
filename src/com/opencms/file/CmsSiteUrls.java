package com.opencms.file;

/**
 * This class is used to hold results from the table CMS_SITE_URLS
 */
public class CmsSiteUrls
{
	private int urlId;
	private String url;
	private int siteId;
	private int primaryUrl;
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:03:18)
 * @param urlId int
 * @param url java.lang.String
 * @param siteId int
 * @param primaryUrl int
 */
public CmsSiteUrls(int urlId, String url, int siteId, int primaryUrl)
{
	this.urlId=urlId;
	this.url=url;
	this.siteId=siteId;
	this.primaryUrl=primaryUrl;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @return int
 */
public int getPrimaryUrl() {
	return primaryUrl;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @return int
 */
public int getSiteId() {
	return siteId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @return java.lang.String
 */
public java.lang.String getUrl() {
	return url;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @return int
 */
public int getUrlId() {
	return urlId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @param newPrimaryUrl int
 */
public void setPrimaryUrl(int newPrimaryUrl) {
	primaryUrl = newPrimaryUrl;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @param newSiteId int
 */
public void setSiteId(int newSiteId) {
	siteId = newSiteId;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @param newUrl java.lang.String
 */
public void setUrl(java.lang.String newUrl) {
	url = newUrl;
}
/**
 * Insert the method's description here.
 * Creation date: (22-09-2000 13:02:42)
 * @param newUrlId int
 */
public void setUrlId(int newUrlId) {
	urlId = newUrlId;
}
}

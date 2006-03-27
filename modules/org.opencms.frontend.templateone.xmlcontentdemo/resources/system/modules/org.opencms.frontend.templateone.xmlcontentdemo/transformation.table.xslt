<?xml  version="1.0"  encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            
<xsl:template match="/">
  <table cellspacing="1" cellpadding="3" border="0" width="630">
    <xsl:copy>
	<xsl:copy-of select="table/colgroup"/>
    </xsl:copy>
    <xsl:apply-templates select="table"/>
  </table>
</xsl:template>

<xsl:template match="tr[position()=1]">
  <tr class="trow1">
    <xsl:copy-of select="td"/>
  </tr>
</xsl:template>
  
<xsl:template match="tr[(position()mod 2)=0]">
  <tr class="trow2">
    <xsl:copy-of select="td"/>
  </tr>
</xsl:template>
  
<xsl:template match="tr[((position()mod 2)=1) and (position()!=1)]">
  <tr class="trow3">
    <xsl:copy-of select="td"/>
  </tr>
</xsl:template>

</xsl:stylesheet>
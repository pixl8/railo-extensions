<cfsetting showdebugoutput="no">
<!---
<cffunction name="valueEquals">
	<cfargument name="left" required="yes">
	<cfargument name="right" required="yes">
    <cf_valueEquals left="#left#" right="#right#">
</cffunction>
 
<cf_valueEquals left="" right="">
--->


<!--- cfmapping --->
	<cfmapping logicalPath="/susi" relativepath="./urs" />
	<cf_valueEquals left="#expandPath("/susi")#" right="#request.currentPath#urs">
	
	<cfapplication name="changed">
	
	<cf_valueEquals left="#expandPath("/susi") eq request.currentPath&"urs"#" right="#false#">

<!--- mappingAdd --->
	<cfset mappingAdd("/abc","#request.currentPath#urs")>
	<cf_valueEquals left="#expandPath("/abc")#" right="#request.currentPath#urs">

<!--- render --->
	<cfset x=1>
	<cfset str=render('<cfset x=2>')>
	<cf_valueEquals left="#x#" right="#2#">
	<cfset str=render('<cfoutput>#x#</cfoutput>')>
	<cf_valueEquals left="#str#" right="#2#">
	<!---<cfset x=1>
	<cfset start=getTickCount()>
	<cfloop from="1" to="1000" index="i"><cfset str=render('<cfset x=2>')></cfloop>
	<cfdump var="#getTickCount()-start#" label="execution time">
	<cf_valueEquals left="#x#" right="#2#">--->
	
<!--- StructListNew --->
	<cfset sct=structListNew()>
	<cfset sct=structListNew(10)>
	<cfset sct.a=1>
	<cfset sct.b=2>
	<cfset sct.c=3>
	<cfset sct.d=4>
	<cf_valueEquals left="#structKeyList(sct)#" right="a,b,c,d">
	<cfset sct.e=5>
	<cfset sct.f=6>
	<cfset sct.g=7>
	<cfset sct.h=8>
	<cfset sct.i=9>
	<cfset sct.j=10>
	<cfset sct.k=11>
	<cfset sct.l=12>
	<cf_valueEquals left="#structKeyList(sct)#" right="c,d,e,f,g,h,i,j,k,l">
	<cf_valueEquals left="#sct['E']#" right="5">
	<cf_valueEquals left="#sct['e']#" right="5">
	
	<cfdump var="#sct#">
	
	
	
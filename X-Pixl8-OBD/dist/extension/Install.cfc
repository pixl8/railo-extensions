<cfcomponent>
	
    <cfset variables.previousJars=["obd-v01.jar","obd-v02.jar","pixl8-obd-v01.jar","pixl8-obd-v02.jar"]>
    <cfset variables.previousFLDs=["obd-v01.fld","obd-v02.fld","pixl8-obd-v01.fld","pixl8-obd-v02.fld"]>
    <cfset variables.previousTLDs=["obd-v01.tld","obd-v02.tld","pixl8-obd-v01.tld","pixl8-obd-v02.tld"]>
    <cfset variables.version="v01">
	


    <cffunction name="install" returntype="string" output="no"
    	hint="called from Railo to install application">
    	<cfargument name="error" type="struct">
        <cfargument name="path" type="string">
        <cfargument name="config" type="struct">
        
        
        <cfif server.railo.version LT "4.0.0.000">
        	<cfset error.common="to install this extension you need at least Railo version [4.0.0.000], your version is [#server.railo.version#]">
            <cfreturn>
        </cfif>
        
		<cfset removeOlderTLDs()>
		<cfset removeOlderFLDs()>
		<cfset removeOlderJars()>
		
        <!--- jar --->
         <cfadmin 
            action="updateJar"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            jar="#path#lib/pixl8-obd-v03.jar">
		
        <!--- fld --->
        <cfadmin 
            action="updateFLD"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            fld="#path#pixl8-obd-v03.fld">
		
        <!--- tld --->
        <cfadmin 
            action="updateTLD"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            tld="#path#pixl8-obd-v03.tld">
			
		<!--- testpage --->
		<cfdirectory action="copy" recurse="true"  directory="#path#testcases/" destination="#config.mixed.destination#">
        
        <cfreturn 'The OBD Compatibility Extension is now successful installed. You need to restart Railo before you can use the App.'>
        
    </cffunction>
    	
     <cffunction name="update" returntype="string" output="no"
    	hint="called from Railo to update a existing application">
		
       <cfreturn install(argumentCollection=arguments)>
    </cffunction>
    
    
    <cffunction name="uninstall" returntype="string" output="no"
    	hint="called from Railo to uninstall application">
    	<cfargument name="path" type="string">
        <cfargument name="config" type="struct">
                
		
		<cfset removeOlderTLDs()>
		<cfset removeOlderFLDs()>
		<cfset removeOlderJars()>
				
        <!--- remove FLDs --->
        <cfadmin 
            action="removeFLD"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            name="pixl8-obd-v03.fld">
			
        <!--- remove TLDs --->
        <cfadmin 
            action="removeTLD"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            name="pixl8-obd-v03.tld">
        
	   <!--- remove jar --->
        <cfadmin 
            action="removeJar"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            jar="pixl8-obd-v03.jar"> 
        
	<!--- remove testpage
		<cfset file="#config.mixed.destination#/test-mongodb.cfm">
        <cfif FileExists(file)>
        	<cffile action="delete" file="#file#">
        </cfif>--->
		
        <cfreturn 'The OBD Compatibility Extension is now successful removed'>
    </cffunction>
    
    <cffunction name="validate" returntype="string" output="no"
    	hint="called from Railo to install application">
    	<cfargument name="error" type="struct">
        <cfargument name="path" type="string">
        <cfargument name="config" type="struct">
        <cfargument name="step" type="numeric">
        
    </cffunction>  
	
	    
    <cffunction name="removeOlderTLDs" returntype="void" output="no" access="private">
    	<cfloop from="1" to="#arrayLen(variables.previousTLDs)#" index="i">
            <cftry>
                <cfadmin 
                    action="removeTLD"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"
                    tld="#variables.previousTLDs[i]#">
                <cfcatch></cfcatch>
            </cftry>
        </cfloop>
    </cffunction>
    
    <cffunction name="removeOlderFLDs" returntype="void" output="no" access="private">
    	<cfloop from="1" to="#arrayLen(variables.previousFLDs)#" index="i">
            <cftry>
                <cfadmin 
                    action="removeFLD"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"
                    fld="#variables.previousFLDs[i]#">
                <cfcatch></cfcatch>
            </cftry>
        </cfloop>
    </cffunction>
	
	<cffunction name="removeOlderJars" returntype="void" output="no" access="private">
    	<cfloop from="1" to="#arrayLen(variables.previousJars)#" index="i">
            <cftry>
                <cffile action="delete" file="#getLibFolder()#/#variables.previousJars[i]#">
                <cfcatch></cfcatch>
            </cftry>
        </cfloop>
    </cffunction>
	
	<cffunction name="getLibFolder" access="private">
		<cfset var cl=getPageContext().getClass().getClassLoader()>
        <cfset path="railo/loader/engine/CFMLEngine.class">
        <cfset var res = cl.getResource(path)>
        <cfset var strFile = createObject('java','java.net.URLDecoder').decode(res.getFile().trim(),"iso-8859-1")>
        <cfset var index=strFile.indexOf('!')>
        <cfif index!=-1><cfset strFile=strFile.substring(0,index)></cfif>
        <cfset strFile=GetDirectoryFromPath(strFile)>
		
		<cfif strFile.startsWith("file:")><cfset strFile=strFile.substring(5)></cfif>
        <cfif findNoCase("windows",server.os.name) and left(strFile,1) EQ "/">
			<cfset strFile=strFile.substring(1)>
		</cfif>
		
		<cfreturn strFile>
    </cffunction>
    
    
      
</cfcomponent>
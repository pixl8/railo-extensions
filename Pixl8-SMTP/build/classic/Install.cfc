<cfcomponent>
	
    <cfset variables.previousJars=[]>
    <cfset variables.previousFLDs=[]>
	


    <cffunction name="install" returntype="string" output="no"
    	hint="called from Lucee to install application">
    	<cfargument name="error" type="struct">
        <cfargument name="path" type="string">
        <cfargument name="config" type="struct">
        
		<cfset removeOlderFLDs()>
		<cfset removeOlderJars()>
		
        <!--- jar --->
        <cfdirectory action="list" directory="#path#jars/" listinfo="name" name="local.dir">
        <cfloop query="#dir#">
             <cfadmin 
                action="updateJar"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                jar="#path#jars/#dir.name#">
        </cfloop>
		
        <!--- install driver --->
        <cfadmin 
            action="updateContext"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            source="#path#event-gateway/SmtpGatewayDriver.cfc"
            destination="admin/gdriver/SmtpGatewayDriver.cfc">
            
        
        <!--- install listener --->
        <cfadmin 
            action="updateContext"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            source="#path#webcontext/components/uk/co/pixl8/smtp/SmtpGatewayListener.cfc"
            destination="components/uk/co/pixl8/smtp/SmtpGatewayListener.cfc">
        



        <cfreturn 'The Extension is now successful installed. You need to restart Lucee before you can use the App.'>
        
    </cffunction>
    	
     <cffunction name="update" returntype="string" output="no"
    	hint="called from Lucee to update a existing application">
		
       <cfreturn install(argumentCollection=arguments)>
    </cffunction>
    
    
    <cffunction name="uninstall" returntype="string" output="no"
    	hint="called from Lucee to uninstall application">
    	<cfargument name="path" type="string">
        <cfargument name="config" type="struct">
                
		
		<cfset removeOlderFLDs()>
		<cfset removeOlderJars()>
				
	   <!--- remove jar --->
        <cfdirectory action="list" directory="#path#jars/" listinfo="name" name="local.dir">
        <cfloop query="#dir#">
             <cfadmin 
                action="removeJar"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                jar="#dir.name#">
        </cfloop>
        
        <!--- remove driver --->
        <cfadmin 
            action="removeContext"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            destination="admin/gdriver/SmtpGatewayDriver.cfc">
            
        
        <!--- remove listener --->
        <cfadmin 
            action="removeContext"
            type="#request.adminType#"
            password="#session["password"&request.adminType]#"
            destination="components/uk/co/pixl8/smtp/SmtpGatewayListener.cfc">
        
		
        <cfreturn 'The Extension is now successful removed'>
    </cffunction>
    
    <cffunction name="validate" returntype="string" output="no"
    	hint="called from Lucee to install application">
    	<cfargument name="error" type="struct">
        <cfargument name="path" type="string">
        <cfargument name="config" type="struct">
        <cfargument name="step" type="numeric">
        
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
        <cfset path="lucee/loader/engine/CFMLEngine.class">
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
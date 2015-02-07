component extends="Gateway" {


    variables.name = "SmtpGateway";
    

    fields = array(
    
          field( "Server Port", "port", "25", true, "SMTP Server TCP Port", "text" ) 
        , field( "Host Name", "hostname", "SmtpGateway", false, "Host Name that will be set for the SMTP Server", "text" )
        , field( "Max Message Length", "maxMessageLength", 0, false, "The Maximum length of a message in bytes.  Larger messages will throw a TooMuchDataException.  Leave 0 for unlimited size.", "text" )
    );


    public function getCfcPath() {          return ""; }


    public function getListenerPath() {

        return "";
    }


    public function getLabel() {            return "Smtp Gateway" }


    public function getDescription() {      return "runs an SMTP service and accepts/processes messages via the Listener CFC" }


    public function getClass() {            return "railo.extension.gateway.smtp.SMTPGateway"; }

    
    public function getListenerCfcMode() {  return "required"; }


    /*/
    public function onBeforeUpdate( required cfcPath, required startupMode, required custom ) {

        // validate args and throw on failure
    }   //*/

}

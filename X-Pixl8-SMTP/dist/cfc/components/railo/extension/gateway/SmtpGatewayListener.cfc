component {

    variables.id        = "SmtpGatewayListener-#getTickCount()#";
    variables.logfile   = "SmtpGateway";


    /**
    * called by the smtp event gateway once for each recipient.
    * 
    * to reject a message, set the key in result.Reject to true, and optionally set result.Message and result.Code
    * all calls to this method must be rejected in order to reject a message.  if one call is accepted then the 
    * message is processed.
    */
    public function accept( String from, String to, String ipAddress, Struct result ) {

        //* example: to reject messages where to contains the substring 'Unkown'
        if ( arguments.to CT "Unknown" ) {

            result.Reject  = true;                                              // required to Reject

            result.Message = "Rejected [#to#] because it contains 'Unknown'";   // optional
            result.Code    = 530;                                               // optional
        }   //*/

        log text="#variables.id# -accept( #arguments.toString()# )" file=variables.logfile;
    }


    /**
    * called by the smtp event gateway after all the recipients have been processed and at least one combination
    * of from/to/ipAddress was accepted by the accept() method
    * 
    * data provides the following keys with more info: 
    * 
    *   ContentType
    *   MessageId
    *   Recipients
    *   ReplyTo
    *   SentDate
    *   Size
    *   Subject
    *   MimeMessage (a javax.mail.internet.MimeMessage object which can be used to retrieve info about the message)
    * 
    */
    public function deliver( Struct data ) {

        log text="#variables.id# -deliver- #arguments.data.toString()#" file=variables.logfile;
    }

}
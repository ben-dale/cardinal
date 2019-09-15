module cardinal {
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires javafx.controls;
    requires org.apache.commons.io;
    requires gson;
    requires java.xml;
    requires java.sql;
    opens uk.co.ridentbyte.model to gson;
    exports uk.co.ridentbyte;
    exports uk.co.ridentbyte.model;
}

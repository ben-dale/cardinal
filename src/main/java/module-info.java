module cardinal {
    requires javafx.controls;
    requires gson;
    requires java.xml;
    requires java.sql;
    requires java.net.http;
    requires junit;
    requires jdk.crypto.ec;
    requires jdk.crypto.cryptoki;
    requires org.assertj.core;
    opens uk.co.ridentbyte.model to gson;
    exports uk.co.ridentbyte;
    exports uk.co.ridentbyte.model;
}

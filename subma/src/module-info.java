module subma {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires java.sql;
    
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;
    requires org.apache.fontbox;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.codec;
    requires org.apache.commons.logging;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.commons.compress;
    requires org.apache.commons.collections4;
    requires org.apache.xmlbeans;
    requires com.github.virtuald.curvesapi;
    requires commons.math3;

    opens com.subsmanager to javafx.fxml, javafx.graphics;
    opens com.subsmanager.gui.controller to javafx.fxml;
    opens com.subsmanager.subscription.model to javafx.base;
    opens com.subsmanager.auth to javafx.base;
    opens com.subsmanager.catalog to javafx.base;
    opens com.subsmanager.coin to javafx.base;
    opens com.subsmanager.db to javafx.base, javafx.fxml;
    
    
}
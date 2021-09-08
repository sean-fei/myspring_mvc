package com.sean.springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.InputStream;

/**
 * @author yunfei_li@qq.com
 * @date 2021年09月08日 9:57
 */
public class XmlPaser {

    public static String getBasePackage(String xml) {
        try {
            SAXReader saxParser = new SAXReader();
            saxParser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            InputStream inputStream = XmlPaser.class.getClassLoader().getResourceAsStream(xml);
            // XML 对象文档
            Document document = saxParser.read(inputStream);
            Element rootElement = document.getRootElement();
            Element componentScan = rootElement.element("component-scan");
            Attribute attribute = componentScan.attribute("base-package");
            String basePackage = attribute.getText();
            return basePackage;
        } catch (DocumentException | SAXException e) {
            e.printStackTrace();
        } finally {

        }
        return "";
    }

}

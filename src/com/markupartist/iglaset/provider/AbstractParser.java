package com.markupartist.iglaset.provider;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractParser extends DefaultHandler {

	private StringBuilder stringBuilder = new StringBuilder();
	
	public abstract void onStartElement(String name, Attributes atts);
	public abstract void onEndElement(String name, String result);

    public void characters(char ch[], int start, int length) {
    	stringBuilder.append(ch, start, length);
    }	
    
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	onStartElement(name, atts);
    }
    
    public void endElement(String uri, String name, String qName) {
    	String resultRaw = stringBuilder.toString().trim();
    	String result = resultRaw.replace("\n", "").trim();
    	onEndElement(name, result);
    	stringBuilder.setLength(0);
    }

    
}

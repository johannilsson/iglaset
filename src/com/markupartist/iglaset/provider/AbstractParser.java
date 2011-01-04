package com.markupartist.iglaset.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public abstract class AbstractParser<T> extends DefaultHandler {

	private static final String TAG = AbstractParser.class.getSimpleName();
	private StringBuilder stringBuilder = new StringBuilder();
	
	public abstract void onStartElement(String name, Attributes atts);
	public abstract void onEndElement(String name, String result);
	protected abstract ArrayList<T> getContent();

	public ArrayList<T> parse(InputStream in) {
        InputSource inputSource = new InputSource(in);

        try {
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        SAXParser sp = spf.newSAXParser();
	        XMLReader xr = sp.getXMLReader();
	        xr.setContentHandler(this);
	        xr.parse(inputSource);
	    } catch (IOException e) {
	        Log.e(TAG, e.toString());
	    } catch (SAXException e) {
	        Log.e(TAG, e.toString());
	    } catch (ParserConfigurationException e) {
	        Log.e(TAG, e.toString());
	    }

		return getContent();
	}
	
    public void characters(char ch[], int start, int length) {
    	stringBuilder.append(ch, start, length);
    }	
    
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	onStartElement(name, atts);
    }
    
    public void endElement(String uri, String name, String qName) {
    	String result = stringBuilder.toString().trim();
    	onEndElement(name, result);
    	stringBuilder.setLength(0);
    }
    
}

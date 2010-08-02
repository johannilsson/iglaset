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

public class TagsParser extends DefaultHandler {
	
    private static final String TAG = TagsParser.class.getSimpleName();
	private ArrayList<Tag> tags;
	private Tag currentTag;
	private StringBuilder builder;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        tags = new ArrayList<Tag>();
        builder = new StringBuilder();
    }
    
    public ArrayList<Tag> parse(InputStream in) {    	
        try {
            InputSource inputSource = new InputSource(in);

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

        return this.tags;
    }
	
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	if(name.equals("tag")) {
    		currentTag = new Tag();
    		
    		String id = atts.getValue("id");
    		if(null != id) {
    			currentTag.setId(Integer.parseInt(id));
    		}
    	}
    }
    
    public void endElement(String uri, String name, String qName) {
    	if(name.equals("tag")) {
    		if(currentTag.getId() != Tag.UNDEFINED_ID) {
    			tags.add(currentTag);
    		}
    		currentTag = null;
    	} else if(name.equals("tag_type")) {
    		currentTag.setType(builder.toString().trim());
    	} else if(name.equals("name")) {
    		currentTag.setName(builder.toString().trim());
    	}
    	
    	builder.setLength(0);
    }

    public void characters(char ch[], int start, int length) {
    	if(null != currentTag) {
    		builder.append(ch, start, length);
    	}
    }
}

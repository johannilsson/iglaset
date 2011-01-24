package com.markupartist.iglaset.provider;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TagsParser extends AbstractParser<Tag> {
	
	private ArrayList<Tag> tags = new ArrayList<Tag>();
	private Tag currentTag;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        tags.clear();
    }

	@Override
	public void onStartElement(String name, Attributes atts) {
    	if(name.equals("tag")) {
    		currentTag = new Tag();
    		
    		String id = atts.getValue("id");
    		if(null != id) {
    			currentTag.setId(Integer.parseInt(id));
    		}
    	}
	}

	@Override
	public void onEndElement(String name, String result) {
    	if(name.equals("tag")) {
    		if(currentTag.getId() != Tag.UNDEFINED_ID) {
    			tags.add(currentTag);
    		}
    		currentTag = null;
    	} else if(name.equals("tag_type")) {
    		currentTag.setType(result);
    	} else if(name.equals("name")) {
    		currentTag.setName(result);
    	}
	}

	@Override
	protected ArrayList<Tag> getContent() {
		return tags;
	}
}

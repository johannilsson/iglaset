package com.markupartist.iglaset.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

/**
 * @author marco
 * 
 * Dummy cookie store to be able to connect to the iglaset API without using cookies. Without this
 * an error is returned when trying to login twice in a row. The cookie token method is not needed
 * since we provide the token with each API call.
 */
public class DummyCookieStore implements CookieStore {

	private List<Cookie> cookies = new ArrayList<Cookie>();
	
	@Override
	public void addCookie(Cookie cookie) {
	}

	@Override
	public void clear() {	
	}

	@Override
	public boolean clearExpired(Date date) {
		return false;
	}

	@Override
	public List<Cookie> getCookies() {
		return cookies;
	}
}
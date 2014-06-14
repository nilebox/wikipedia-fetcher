package ru.nilebox.wikipedia.fetcher.filter;

import org.w3c.dom.Document;

/**
 *
 * @author nile
 */
public interface WikipediaFilter {
	/*
	 * Method checks if the page is valid for query
	 */
	boolean validatePage(Document responseDoc, Document contentDoc);
	
	/*
	 * Method checks if the page reference is valid for query
	 */
	boolean validatePageReference(String text);
}

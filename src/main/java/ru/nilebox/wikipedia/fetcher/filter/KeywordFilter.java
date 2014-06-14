package ru.nilebox.wikipedia.fetcher.filter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import ru.nilebox.wikipedia.fetcher.utils.XMLUtils;

/**
 *
 * @author nile
 */
public class KeywordFilter implements WikipediaFilter {

	private static final XPath xpath = XPathFactory.newInstance().newXPath();
	private XPathExpression vcardPath;
	private XPathExpression infoboxPath;
	private String[] keywords;

	public KeywordFilter() throws XPathExpressionException {
		vcardPath = xpath.compile("//table[@class=\"infobox vcard\"]");
		infoboxPath = xpath.compile("//table[@class=\"infobox\"]");
	}

	public boolean validatePage(Document responseDoc, Document contentDoc) {
		String content = XMLUtils.toString(responseDoc);
		if (content.contains("#REDIRECT")) {
			return true;
		}

		try {
			String vcard = (String) vcardPath.evaluate(contentDoc, XPathConstants.STRING);
			if (vcard == null || vcard.length() == 0) {
				vcard = (String) infoboxPath.evaluate(contentDoc, XPathConstants.STRING);
			}
			if (vcard == null || vcard.length() == 0) {
				return false;
			}
			vcard = vcard.toLowerCase();

			for (String keyword : keywords) {
				if (vcard.contains(keyword.toLowerCase())) {
					return true;
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Error evaluating XPath", e);
		}
		return false;
	}

	public boolean validatePageReference(String text) {
		for (String keyword : keywords) {
				if (text.contains(keyword))
					return true;
		}
		return false;
	}
}

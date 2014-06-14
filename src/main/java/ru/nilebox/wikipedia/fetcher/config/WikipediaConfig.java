package ru.nilebox.wikipedia.fetcher.config;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 *
 * @author nile
 */
public class WikipediaConfig {
	private static final XPath xpath = XPathFactory.newInstance().newXPath();
	private String baseUrl = ConfigDefaults.BASE_URL;
	private XPathExpression contentPath;
	private XPathExpression descriptionPath;
	private XPathExpression redirectPath;	
	private XPathExpression referencesPath;	
	private XPathExpression referenceUrlPath;
	
	public WikipediaConfig() throws XPathExpressionException {
		contentPath = xpath.compile(ConfigDefaults.XPATH_CONTENT);
		descriptionPath = xpath.compile(ConfigDefaults.XPATH_DESCRIPTION);
		redirectPath = xpath.compile(ConfigDefaults.XPATH_REDIRECT_URL);
		referencesPath = xpath.compile(ConfigDefaults.XPATH_REFERENCES);
		referenceUrlPath = xpath.compile(ConfigDefaults.XPATH_REFERENCE_URL);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public XPathExpression getContentPath() {
		return contentPath;
	}

	public void setContentPath(XPathExpression contentPath) {
		this.contentPath = contentPath;
	}

	public XPathExpression getDescriptionPath() {
		return descriptionPath;
	}

	public void setDescriptionPath(XPathExpression descriptionPath) {
		this.descriptionPath = descriptionPath;
	}

	public XPathExpression getRedirectPath() {
		return redirectPath;
	}

	public void setRedirectPath(XPathExpression redirectPath) {
		this.redirectPath = redirectPath;
	}

	public XPathExpression getReferencesPath() {
		return referencesPath;
	}

	public void setReferencesPath(XPathExpression referencesPath) {
		this.referencesPath = referencesPath;
	}

	public XPathExpression getReferenceUrlPath() {
		return referenceUrlPath;
	}

	public void setReferenceUrlPath(XPathExpression referenceUrlPath) {
		this.referenceUrlPath = referenceUrlPath;
	}
	
}

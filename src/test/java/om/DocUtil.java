package om;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DocUtil {
	
	private Document doc;
	private Element element;

	public DocUtil(Document doc, Element element) {
		super();
		this.doc = doc;
		this.element = element;
	}
	
	public DocUtil(Document doc) {
		super();
		this.doc = doc;
	}

	public DocUtil addElement(String name) {
		Element newElement = doc.createElement(name);
		
		if (element == null) {
			doc.appendChild(newElement);
		} else {
			element.appendChild(newElement);
		}
		
		return new DocUtil(doc, newElement);
	}
	
	public DocUtil addAtribute(String key, String value) {
		Attr attr = doc.createAttribute(key);
		attr.setValue(value);
		element.setAttributeNode(attr);
		return this;
	}

	
	
	//Gettters and Setters
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

}

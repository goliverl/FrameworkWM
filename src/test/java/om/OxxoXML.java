package om;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType; 


public class OxxoXML {
	

	String requestString;
	

	@XmlTransient
	static class TPEDoc {
		String version;
		Header header;
		Request request;
		
		public TPEDoc() {}
		
		@XmlAttribute
		public void setVersion(String version) {
			this.version = version;
		}
		
		@XmlElement
		public void setHeader(Header header) {
			this.header = header;
		}
		
		@XmlElement
		public void setRequest(Request request){
			this.request = request;
		}
		
		public String getVersion() {
			return version;
		}

		public Header getHeader() {
			return header;
		}

		public Request getRequest() {
			return request;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(propOrder = {"application","entity","operation"})
	static class Header {
		@XmlAttribute(required = true)
		String application;
		@XmlAttribute(required = true)
		String entity;
		@XmlAttribute(required = true)
		String operation;	
		
		
		public void setApplication(String application) {
			this.application = application;
		}
		
		public void setEntity(String entity) {
			this.entity = entity;
		}
		
		public void setOperation(String operation) {
			this.operation = operation;
		}

		public String getApplication() {
			return application;
		}

		public String getEntity() {
			return entity;
		}

		public String getOperation() {
			return operation;
		}
		
		
	}

	static class Request {
		
	}

	
	public static String marshal(TPEDoc tpeDoc) throws JAXBException, IOException {
	    JAXBContext context = JAXBContext.newInstance(tpeDoc.getClass());
	    Marshaller mar= context.createMarshaller();
	   // mar.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
	   // mar.setProperty("com.sun.xml.bind.xmlHeaders",
	   //	      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    StringWriter sw = new StringWriter();
	    mar.marshal(tpeDoc, sw);
	    return sw.toString();
	}
	
	public static String marshal(Object document) throws JAXBException, IOException {
	    JAXBContext context = JAXBContext.newInstance(document.getClass());
	    Marshaller mar= context.createMarshaller();
	    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    StringWriter sw = new StringWriter();
	    mar.marshal(document, sw);
	    return sw.toString();
	}
	
}



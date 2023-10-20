package util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import utils.webmethods.ReadRequest;

public class RequestUtil { 
	 

	public static String getSimpleDataXml(String xmlResponse, String data) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
    	return runGetFolioRequestDoc.getElementsByTagName(data).item(0).getTextContent();
	}
	
	public static String getWmCodeXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0);
		
    	return eElement.getAttribute("value");
	}
	
	public static String getWmCodeJson(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);
		JSONObject doc = (JSONObject) json.get("TPEDoc");
		JSONObject response = (JSONObject) doc.get("response");
		JSONObject wmCode = (JSONObject) response.get("wmCode");
		return wmCode.get("value").toString();
	}
	
	public static String getFolioJson(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString);
		JSONObject doc = (JSONObject) json.get("TPEDoc");
		JSONObject header = (JSONObject) doc.get("header");
		System.out.println(header);
		return header.get("folio").toString();
	}
	 
	public static String getFolioXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("header").item(0);
		
    	return eElement.getAttribute("folio");
	}
	
	public static String getCreationDate(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("header").item(0);
		
    	return eElement.getAttribute("creationDate");
	}
	
	public static String getAttributeResponse(String xmlResponse, String tagName, String attributeName) {
		try {
			Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
			Element responseElement = (Element) runGetFolioRequestDoc.getElementsByTagName("response").item(0);
			Element eElement = (Element) responseElement.getElementsByTagName(tagName).item(0);
			
	    	return eElement.getAttribute(attributeName);
		} catch (NullPointerException e) {
			return null;
		}
		
	}
	
	public static String getpvDocId(String xmlResponse) {
		
		Document runGetRequestIbound = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetRequestIbound.getElementsByTagName("header").item(0);
		
    	return eElement.getAttribute("PV_DOC_ID");
	}
	
	
	public static void main(String[] args)  {
		String response = "<TPEDoc version=\"1.0\">\r\n" + 
				"<header application=\"OLS\" entity=\"IZZI\" operation=\"QRY01\" folio=\"2017931721\" source=\"POS\" plaza=\"10MON\" tienda=\"50QTE\" caja=\"1\" adDate=\"20200414\" pvDate=\"20200414000000\"/>\r\n" + 
				"<response>\r\n" + 
				"<service id=\"101655006\" idClient=\"0600034433\" accountId=\"60003443\" client=\"YVETTE AYME GUTIERREZ MARTIN\" payType=\"P\">\r\n" + 
				"<detail desc=\"Maximo a Pagar\" monto=\"1000000\" mov=\"max\"/>\r\n" + 
				"<detail desc=\"Minimo a Pagar\" monto=\"100\" mov=\"min\"/>\r\n" + 
				"<detail desc=\"subtotal\" monto=\"40000\" mov=\"+\"/>\r\n" + 
				"<detail desc=\"Total\" monto=\"40000\" mov=\"t\"/>\r\n" + 
				"</service>\r\n" + 
				"<wmCode value=\"101\" desc=\"Pago aplicado, gracias por su preferencia\"/>\r\n" + 
				"</response>\r\n" + 
				"</TPEDoc>";
			
		String data = getAttributeResponse(response, "service", "accountId");
		System.out.println("Data: " + data);
//		
//		String response = "<BODY bgcolor=#dddddd>\r\n" + 
//				"    <TABLE bgcolor=#dddddd border=1>\r\n" + 
//				"        <TR>\r\n" + 
//				"            <TD valign=\"top\">\r\n" + 
//				"                <B>responseQueryTdc</B>\r\n" + 
//				"            </TD>\r\n" + 
//				"            <TD>\r\n" + 
//				"                <TABLE bgcolor=#dddddd border=1>\r\n" + 
//				"                    <TR>\r\n" + 
//				"                        <TD valign=\"top\">\r\n" + 
//				"                            <B>queryList</B>\r\n" + 
//				"                        </TD>\r\n" + 
//				"                        <TD>\r\n" + 
//				"                            <TABLE>\r\n" + 
//				"                            </TABLE>\r\n" + 
//				"                        </TD>\r\n" + 
//				"                    </TR>\r\n" + 
//				"                    <TR>\r\n" + 
//				"                        <TD valign=\"top\">\r\n" + 
//				"                            <B>wmCode</B>\r\n" + 
//				"                        </TD>\r\n" + 
//				"                        <TD>\r\n" + 
//				"                            <TABLE bgcolor=#dddddd border=1>\r\n" + 
//				"                                <TR>\r\n" + 
//				"                                    <TD valign=\"top\">\r\n" + 
//				"                                        <B>value</B>\r\n" + 
//				"                                    </TD>\r\n" + 
//				"                                    <TD>101</TD>\r\n" + 
//				"                                </TR>\r\n" + 
//				"                                <TR>\r\n" + 
//				"                                    <TD valign=\"top\">\r\n" + 
//				"                                        <B>desc</B>\r\n" + 
//				"                                    </TD>\r\n" + 
//				"                                    <TD>Transacción exitosa</TD>\r\n" + 
//				"                                </TR>\r\n" + 
//				"                            </TABLE>\r\n" + 
//				"                        </TD>\r\n" + 
//				"                    </TR>\r\n" + 
//				"                </TABLE>\r\n" + 
//				"            </TD>\r\n" + 
//				"        </TR>\r\n" + 
//				"    </TABLE>\r\n" + 
//				"</BODY>";
//		
//		
//	
//		int wmCodeTagPos = response.indexOf("wmCode");
//		
//		String nextSub1 = response.substring(wmCodeTagPos);
//		
//		int valueTagPos = nextSub1.indexOf("value");
//		
//		String nextSub2 = nextSub1.substring(valueTagPos);
//		
//		String valueTag = "<TD>";
//		
//		int tagPos = nextSub2.indexOf(valueTag);
//		
//		String nextSub3 = nextSub2.substring(tagPos);
//		
//		int endTagPos = nextSub3.indexOf("</TD>");
//		
//		
//		
//		String str = nextSub3.substring(valueTag.length(), endTagPos);
		
		
//		String str = getValueHtmlResponse(response, "TD", "wmCode", "value");
//		
//		System.out.println(str);
//		
//		
//		
		System.out.println("test");
		
	}
	
	public static String getWmCodeHtmlResponse(String response) {
		return getValueHtmlResponse(response, "TD", "wmCode", "value");
	}
	
	public static String getValueHtmlResponse(String response, String tagWithValue, String... parentValues) {
		
		String actualSubString = response;
		String startTag = "<" + tagWithValue + ">";
		String endTag = "</" + tagWithValue + ">";
		
		for (String string : parentValues) {
			int pos = actualSubString.indexOf(string);
			actualSubString = actualSubString.substring(pos);
		}
		
		int tagPos = actualSubString.indexOf(startTag);
		String subString  = actualSubString.substring(tagPos); 
		int endTagPos = subString.indexOf(endTag);
		
		
		String str = subString.substring(startTag.length(), endTagPos);
		
		
		return str;
	}
	
}

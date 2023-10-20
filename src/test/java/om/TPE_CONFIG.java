package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import exceptions.ReempRequestException;
import modelo.TestCase;

public class TPE_CONFIG {

	Date fecha = new Date();// obtener fecha del sistema
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
	
	
	public String runGetQRY01="FAC\\QRY01.txt";
	public String requestCONFIG ="http://%s/invoke/TPE.POSCONF.Pub:request?xmlIn=";
	
	String creationDate;
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;
	
	String date = formatter.format(fecha);
	
	public  TPE_CONFIG (HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {

		this.data = data;
		this.testCase = testCase;
		this.db = db;
		
	}
	
	public String QRY01Merma() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestCONFIG, data.get("host"));
		String fechaTexto = formatter.format(fecha);

			OxxoRequest or = new OxxoRequest(url);
			
			or.addHeader("application", "POSCONF");
			or.addHeader("entity", "CONFIG");
			or.addHeader("operation", "QRY01");
			or.addHeader("source", "POS");	
			or.addHeader("plaza", data.get("plaza"));
			or.addHeader("tienda", data.get("tienda"));
			or.addHeader("caja", data.get("caja"));
			or.addHeader("pvDate", fechaTexto.substring(0, 8));
			or.addHeader("adDate", fechaTexto.substring(0, 8));
			
			
			Document doc = or.addNewDocument();
			//etiqueta
			Element configList = doc.createElement("configList");
			doc.appendChild(configList);
		    
			
			Element getConfig = doc.createElement("getConfig");
			configList.appendChild(getConfig);
			
			
			//atributos
			
		    Attr app = doc.createAttribute("application");
		    app.setValue(data.get("application"));
		    getConfig.setAttributeNode(app);
		    
		    Attr date = doc.createAttribute("date");
		    date.setValue(fechaTexto);
		    getConfig.setAttributeNode(date);
		    
		    Attr entity = doc.createAttribute("entity");
		    entity.setValue(data.get("configEntity"));
		    getConfig.setAttributeNode(entity);
		    
		    Attr proc_code = doc.createAttribute("proc_code");
		    entity.setValue("");
		    getConfig.setAttributeNode(proc_code);
		    
		    
		    String request = or.getRequestString();
			
			System.out.println(request);
			testCase.addTextEvidenceCurrentStep(request);
			
			String response = or.executeRequest();
			
			testCase.addTextEvidenceCurrentStep(response);
			
			return response;
			
			
		}
	
}
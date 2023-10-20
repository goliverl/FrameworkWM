package om;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class PR2 {

	public String runValidSKU = "PR2\\runValidSKU.txt";
	public String runInbound = "PR2\\runInbound.txt";
	public String runOutbound = "PR2\\runOutbound.txt";
	public String runAffect = "PR2\\runAffect.txt";
	public String runAck = "PR2\\runAck.txt";
	public String runValidSkuIS = "PR2\\getValidSku.txt";
	
	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	
	public PR2(HashMap<String, String> data, TestCase testCase,	SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
		
	/////////////////////////////////////////////////////////////////////////////////////////
	
    //							runValidSku                                                //
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	
public String ejecutarRunValidSKU() throws ReempRequestException, ClientProtocolException, IOException  {
		
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", GlobalVariables.HOST_PR2);
	datosRequest.put("DESTINATIONAPP", "POS");
	datosRequest.put("SENDER_ID", data.get("SENDER"));
	datosRequest.put("RECEIVER_ID", data.get("RECEIVER"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("SKU", data.get("ITEM"));
	
	String request = GetRequestFile.getRequestFile(runValidSKU, datosRequest);
	System.out.println(request);
	
	String response = GetRequest.executeGetRequest(request);
	
	testCase.addTextEvidenceCurrentStep(request);
	
	return response;	
	
}

public String getValidSkuXml() throws ReempRequestException, ClientProtocolException, IOException {
	   
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("DESTINATIONAPP", "POS");
	datosRequest.put("SENDER_ID", data.get("SENDER"));
	datosRequest.put("RECEIVER_ID", data.get("RECEIVER"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("SKU", data.get("SKU"));
	
	String request = GetRequestFile.getRequestFile(runValidSkuIS, datosRequest);
	System.out.println(request);
	return request;
}

public String ejecutarRunInbound() throws ReempRequestException, ClientProtocolException, IOException  {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", GlobalVariables.HOST_PR2);
	datosRequest.put("PV_DOC_ID", data.get("PV_DOC_ID"));
	datosRequest.put("SHIP_DATE", data.get("SHIP_DATE"));
	datosRequest.put("SENDER_ID", data.get("SENDER"));
	datosRequest.put("RECEIVER_ID", data.get("RECEIVER"));
	datosRequest.put("EXT_REF_NO", data.get("EXT_REF_NO"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("CTRL_CODE", data.get("CTRL_CODE"));
	datosRequest.put("ITEM", data.get("ITEM"));
	datosRequest.put("SHIP_QTY", data.get("SHIP_QTY"));
	
	String request = GetRequestFile.getRequestFile(runInbound, datosRequest);
	System.out.println(request);
	
	String response = GetRequest.executeGetRequest(request);
	
	testCase.addTextEvidenceCurrentStep(request);
	
	return response;	
	
}

public String ejecutarRunOutbound() throws ReempRequestException, ClientProtocolException, IOException  {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", GlobalVariables.HOST_PR2);
	datosRequest.put("SENDER_ID", data.get("RECEIVER"));

	
	String request = GetRequestFile.getRequestFile(runOutbound, datosRequest);
	System.out.println(request);
	
	String response = GetRequest.executeGetRequest(request);
	
	testCase.addTextEvidenceCurrentStep(request);
	
	return response;	
	
}
	
public String ejecutarRunAffect() throws ReempRequestException, ClientProtocolException, IOException  {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", GlobalVariables.HOST_PR2);
	datosRequest.put("SENDER_ID", data.get("SENDER"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("MOV_TYPE", data.get("MOV_TYPE"));
	datosRequest.put("PV_DOC_ID", data.get("PV_DOC_ID"));
	datosRequest.put("SHIP_DATE", data.get("SHIP_DATE"));
	datosRequest.put("RECEIVER_ID", data.get("RECEIVER"));
	datosRequest.put("EXT_REF_NO", data.get("EXT_REF_NO"));
	datosRequest.put("CTRL_CODE", data.get("CTRL_CODE"));
	
	String request = GetRequestFile.getRequestFile(runAffect, datosRequest);
	System.out.println(request);
	
	String response = GetRequest.executeGetRequest(request);
	
	testCase.addTextEvidenceCurrentStep(request);
	
	return response;	
	
}

public String ejecutarRunAck() throws ReempRequestException, ClientProtocolException, IOException  {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", GlobalVariables.HOST_PR2);
	datosRequest.put("SENDER_ID", data.get("RECEIVER"));
	datosRequest.put("IF_DATE", data.get("SHIP_DATE"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("RECEIVER_ID", data.get("SENDER"));
	datosRequest.put("NO_RECORDS", data.get("NO_RECORDS"));
	datosRequest.put("PV_DOC_ID", data.get("PV_DOC_ID"));
	
	String request = GetRequestFile.getRequestFile(runAck, datosRequest);
	System.out.println(request);
	
	String response = GetRequest.executeGetRequest(request);
	
	testCase.addTextEvidenceCurrentStep(request);
	
	return response;	
	
}
	
		
	
	
	
	
	
	
	
	
	
	
	
	
}

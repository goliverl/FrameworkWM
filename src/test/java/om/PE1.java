package om;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLResult;
//import utils.SqlUtil;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class PE1 {
	public String runGetFolioPath = "PE1\\RunGetFolio.txt";
	public String runGetFolioPeruPath = "PE1\\RunGetFolioPeru.txt";
	public String runGetFolioPEPath = "PE1\\RunGetFolioPE.txt";
	public String runGetAuthPath = "PE1\\RunGetAuth.txt";
	public String runGetAuthPeruPath = "PE1\\RunGetPeruAuth.txt";
	public String runGetAuthPEPath = "PE1\\RunGetAuthPE.txt";
	public String runGetAckPath = "PE1\\RunGetAck.txt";
	public String runGetAckPeruPath = "PE1\\RunGetPeruAck.txt";
	public String runGetAckPEPath = "PE1\\RunGetAckPE.txt";

	String creationDate;

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;

	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
// de
// la
// fecha

	public PE1(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;

	}

	public String ejecutarRunGetFolio() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));

		String request = GetRequestFile.getRequestFile(runGetFolioPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de folio proporciona un folio de forma exitosa:");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
	}

	public String ejecutarRunGetFolioPeru() throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));

		String request = GetRequestFile.getRequestFile(runGetFolioPeruPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}
	
	public String ejecutarRunGetFolioPE() throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));

		String request = GetRequestFile.getRequestFile(runGetFolioPEPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}

	public String ejecutarRunGetAuth(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();

		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));

		datosRequest.put("folio", folio);
		datosRequest.put("pv_date", date);
		datosRequest.put("ad_date", date.substring(0, 8));
		datosRequest.put("carrier", data.get("carrier"));
		datosRequest.put("phone", data.get("phone"));
		datosRequest.put("amount", data.get("amount"));

		String request = GetRequestFile.getRequestFile(runGetAuthPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de Autorización proporciona la autorización de la transacción exitosamente.");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
	}

	public String ejecutarRunGetAuthPeru(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));

		datosRequest.put("folio", folio);
		datosRequest.put("pv_date", data.get("pvDate"));
		datosRequest.put("ad_date", data.get("adDate"));
		datosRequest.put("carrier", data.get("carrier"));
		datosRequest.put("phone", data.get("phone"));
		datosRequest.put("amount", data.get("amount"));

		String request = GetRequestFile.getRequestFile(runGetAuthPeruPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}
	public String ejecutarRunGetAuthPE(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));

		datosRequest.put("folio", folio);
		datosRequest.put("pv_date", date);
		datosRequest.put("ad_date", date.substring(0, 8));
		datosRequest.put("carrier", data.get("carrier"));
		datosRequest.put("phone", data.get("phone"));
		datosRequest.put("amount", data.get("amount"));

		String request = GetRequestFile.getRequestFile(runGetAuthPEPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}


	public String ejecutarRunGetAckPeru(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("folio", folio);
		datosRequest.put("ack", data.get("ack"));

		String request = GetRequestFile.getRequestFile(runGetAckPeruPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}

	public String ejecutarRunGetAckPE(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("folio", folio);
		datosRequest.put("ack", data.get("ack"));

		String request = GetRequestFile.getRequestFile(runGetAckPEPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;
	}

	public String ejecutarRunGetAck(String folio) throws ReempRequestException, ClientProtocolException, IOException {
		String ackStepResponse = " ";
		String ackNegativo= "01";
		if(data.get("ack").equals(ackNegativo)) {
			ackStepResponse="Rechazada";
		}
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("folio", folio);
		datosRequest.put("ack", data.get("ack"));

		String request = GetRequestFile.getRequestFile(runGetAckPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de Confirmación ACK concluye con la transacción "+ackStepResponse+":");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
	}

	public void validarTelefono() {
		testCase.nextStep("Validar la configuracion del telefono ");
		compararTelefono();
	}

	public void compararTelefono() {
		String resp = null;
		String[][] array = null;
		String path = System.getProperty("user.dir") + "/Interfaces/PE1/PE1Telefonos.txt";
		boolean valido = false;
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			FileReader fr2 = new FileReader(path);
			BufferedReader br2 = new BufferedReader(fr2);

			String linea;
			String[] parts;
			int i = 0;
			int filas = (int) br2.lines().count();
			array = new String[filas][3];

			while ((linea = br.readLine()) != null) {

				parts = linea.split(",");
				array[i][0] = parts[0];
				array[i][1] = parts[1];
				array[i][2] = parts[2];
				i++;

			}

			fr.close();

		} catch (Exception e) {
			System.out.println("Excepcion leyendo fichero : " + e);
		}

		for (int i = 0; i < array.length; i++) {

			for (int j = 0; j < array[i].length; j++) {

				if (data.get("phone").equals(array[i][j])) {
					System.out.print("Número válido\n");
					resp = "Número: " + array[i][0] + " para pruebas de " + array[i][1] + ". \n"
							+ "Válido para la compañia: " + array[i][2];
					testCase.addTextEvidenceCurrentStep(resp);
					valido = true;
				}

			}

		}

		if (valido == false) {
			System.out.print("Número no válido\n");
			resp = "Número no válido:" + data.get("phone");
			testCase.addTextEvidenceCurrentStep(resp);
		}

	}

	public void validaCarrier(SQLUtil db) throws SQLException {

		String query_carrier = "select * from tpeuser.tae_carrier where carrier = '" + data.get("carrier") + "'";
		testCase.nextStep("Validar que el carrier sea correcto");
		SQLResult validaCarrierRes = db.executeQuery(query_carrier);

		System.out.println(query_carrier);
		boolean validaCarrierBool = validaCarrierRes.isEmpty(); // checa que el string contenga datos

		System.out.println(validaCarrierBool);
		if (validaCarrierBool == false) {
			System.out.print("El carrier es correcto \n");
			testCase.addQueryEvidenceCurrentStep(validaCarrierRes);
		} else {
			System.out.print("El carrier no es correcto \n");
			testCase.addTextEvidenceCurrentStep("El carrier: " + data.get("carrier") + " no es valido\n");
		}
	}

	public void validaConfigTienda(SQLUtil db) throws SQLException {

		testCase.nextStep("Validar que la tienda este configurada con el carrier solicitado");

		String query_routing = "select * from tpeuser.tae_routing where carrier = '" + data.get("carrier") + "'"
				+ " AND plaza = '" + data.get("plaza") + "'";
		SQLResult validaTiendaRes = db.executeQuery(query_routing);

		System.out.println(query_routing);
		boolean validaTiendaBool = validaTiendaRes.isEmpty(); // checa que el string contenga datos

		System.out.println(validaTiendaBool);
		if (validaTiendaBool == false) {
			testCase.addQueryEvidenceCurrentStep(validaTiendaRes);
			System.out.print("La tienda cuenta con el carrier ingresado \n");
		} else {
			testCase.addTextEvidenceCurrentStep("La plaza: " + data.get("plaza") + " no esta mapeada con el carrier: "
					+ data.get("carrier") + "\n");
			System.out.print("La tienda no cuenta con el carrier ingresado \n");
		}

	}

}

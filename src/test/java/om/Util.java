package om;

import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;

import exceptions.ReempRequestException;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GetRequestFile;
import utils.FTPUtil;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class Util {
	
	TestCase testCase;
	
	
	public Util(TestCase testCase) {
		super();
		this.testCase = testCase;
	}
	
	public Util() {
		super();
	}
	
	public static void main(String[] args) {
		Util u = new Util();
		String date = u.getFormattedDate("ddMMyyyy");
		String fileName = "switch_oxxo";
		fileName = fileName.concat(date+".log");
		System.out.println(fileName);
	}
	
	

	// #------------------------ Log Functions ------------------------------------#

		public void logHost(String host) {
			String mensaje = "BD consultada: "+host;
			System.out.println(mensaje);
			testCase.addTextEvidenceCurrentStep(mensaje);
		}
		
		public void logBold(String msj) {
			System.out.println(msj);
			testCase.addBoldTextEvidenceCurrentStep(msj);
		}
		
		public void log(String msj) {
			System.out.println(msj);
			testCase.addTextEvidenceCurrentStep(msj);
		}
		
		public void mensajeError(String mensajeError) {
			System.out.println(mensajeError);
			assertTrue(false,mensajeError);
		}
		
		public void muestraError(String query, String mensajeError) {
			System.out.println(mensajeError);
			log("Fallo al realizar la consulta: \r\n"+query);
			assertTrue(false,mensajeError);
		}
		
		
		public void printQuery(String query) {
			System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
			System.out.println(query+"\r\n");
			System.out.println("#---------------------------#\r\n");
		}
		
//		int contador = 0;
//		public void step(String step){
//			contador++;
//			System.out.println("\r\nStep "+contador+"-"+step);
//			addStep(step);
//		}
//		
//		public SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
//			SQLResult queryResult;
//			printQuery(query);
//			queryResult = executeQuery(obj, query);
//			return queryResult;
//		}

		
	// #------------------------ Utility Functions ------------------------------------#

		public String generaFecha() {
			String fecha = "";
			LocalDate date = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-uu");
			fecha = date.format(formatter);
			return fecha;
		}
		
		
		public String getFormattedDate(String dateFormat) {
			Date fecha = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			String date = formatter.format(fecha);
			
			return date;
		}
		
	// #------------------------ SQL Functions ------------------------------------#
		
		public void validaRespuestaQuery(SQLResult queryResult) {
			if (!queryResult.isEmpty()) {
				//imprime la cantidad de registros devueltos de la consulta ejecutada
				int registros = queryResult.getRowCount();
				//agrega el query y los registros a evidencia
				testCase.addQueryEvidenceCurrentStep(queryResult);
				System.out.println("Query ejecutado exitosamente, devolvio: "+registros+" registro(s)");
			}
		}
		
		
		
		
	// #------------------------ Web Functions ------------------------------------#
		
		public void seleccionaElemento(SeleniumUtil u, By xpath) {
			u.highLight(xpath);
			u.click(xpath);
			u.unhighLight(xpath);
		}
		
		public void insertaTexto(SeleniumUtil u, By xpath, String text) {
			u.highLight(xpath);
			u.sendKeys(xpath, text);
			u.unhighLight(xpath);
		}
		
		// #------------------------ FTP Functions ------------------------------------#
		
		public FTPClient getFTPClient(String ftpHost, String ftpPort, String ftpUser, String ftpPass) {
			int port = Integer.parseInt(ftpPort);
			FTPUtil ftp = new FTPUtil(ftpHost, port, ftpUser, ftpPass);
			FTPClient ftpClient = ftp.getClient();
			
			return ftpClient;
		}
		
		
		public void searchFTPFileByName(FTPClient ftpClient, String ftpPath, String fileName) throws IOException {
			log("Archivo a buscar :"+fileName+" en la ruta: "+ftpPath);
			ftpClient.changeWorkingDirectory(ftpPath);
			FTPFile[] files = ftpClient.listFiles();
			int filecount = 0;
			for (FTPFile file : files) {
				if (file.getName().equalsIgnoreCase(fileName)) {
						filecount++;
					}
			}
			if (filecount == 0) {
				mensajeError("No se encontro el archivo: "+fileName);
			}else {
				log("Existencia del archivo: "+fileName+" validada correctamente");
			}
		}
		
		public void searchFTPFileByExtension(FTPClient ftpClient, String ftpPath, String fileExtension) throws IOException {
			log("Archivos a buscar con extension: "+fileExtension+" en la ruta: "+ftpPath);
			ftpClient.changeWorkingDirectory(ftpPath);
			FTPFile[] files = ftpClient.listFiles();
			List<String> foundFiles = new ArrayList<String>();
			for (FTPFile file : files) {
				if (file.getName().endsWith(fileExtension)) {
						foundFiles.add(file.getName());
					}
			}
			int fileCount = foundFiles.size();
			if (fileCount == 0) {
				mensajeError("No se encontraron archivos con la extension: "+fileExtension);
			}else {
				log("Existencia de archivo(s) con extension "+fileExtension+" validado(s) correctamente, "
						+ "se encontraron: "+fileCount+" archivo(s): ");
				for (String archivo : foundFiles) {
					log(archivo);
				}
			}
		}
		
		// #------------------------ XML Functions ------------------------------------#
		
		public void validaWmCodeXML(String response, String wmCode, String code, String mensaje) {
			if (wmCode.equalsIgnoreCase(code)) {
				log(mensaje);
			}else {
				mensajeError("El wmCode: "+wmCode+" es diferente al esperado: "+code);
			}
		}
		
		public void logRequestAndResponse(String xmlPath, HashMap<String,String> datos) throws ReempRequestException, ClientProtocolException, IOException {
			String request = GetRequestFile.getRequestFile(xmlPath, datos);
			log("Request: \r\n"+request);
			String response = GetRequest.executeGetRequest(request);
			log("Response: \r\n"+response);
		}
		
		public String getXmlResponse(String xmlPath, HashMap<String,String> datos) throws ReempRequestException, ClientProtocolException, IOException {
			String request = GetRequestFile.getRequestFile(xmlPath, datos);
			log("Request: \r\n"+request);
			String response = GetRequest.executeGetRequest(request);
			log("Response: \r\n"+response);
			
			return response;
		}
		
		public String getXmlTagValue(String response, String tagName) {
			String tagValue = getSimpleDataXml(response, tagName);
			System.out.println("Recupera valor de "+tagName+": "+tagValue);
			
			return tagValue;
		}
		
		public HashMap<String,String> getXMLDates(String response){
			HashMap <String, String> dates = new HashMap<>();
			String creationDate = getXmlTagValue(response, "creationDate");
			String pvDate = creationDate;
			String adDate = pvDate.substring(0,8);
			dates.put("creationDate", creationDate);
			dates.put("pvDate", pvDate);
			dates.put("adDate", adDate);
		
			return dates;
		}
		
		public String getPvDate() {
			String pvDate = getFormattedDate("yyyyMMddHHmmss");
			return pvDate;
		}
		
		public String getAdDate() {
			String adDate = getFormattedDate("yyyyMMddHHmmss");
			adDate = adDate.substring(0,8);
			
			return adDate;
		}
		
	// #------------------------  My Functions ------------------------------------#
	
}

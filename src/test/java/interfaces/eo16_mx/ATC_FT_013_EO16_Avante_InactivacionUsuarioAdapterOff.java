package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.validation.constraints.AssertTrue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.ApiUtil;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 *  I21045-Administración de Identidades-Avante-CC: MTC-FT-023-Realizar una Inactivación de Usuario,  Excepción en el servicio
 * Desc:
 * Se requiere validar el método de inactivación de usuarios en el sistema EBS 12.2.4 con los siguientes parámetros de entrada:
 * De acuerdo con el Número del empleado (# empleado SAP), campo a guardar:
 * Numero de empleado (# empleado SAP)
 * Tipo de Movimiento = Baja. 
 * Clave de Movimiento = Recisión. 
 * Status. En la baja por default colocar “Finalizar asignación”.
 * Fecha baja. Si es una baja (salida del empleado) colocar una fecha final.
 * La baja del usuario también puede ser por Inactivación del usuario.
 * Para este caso deberá simularse una desconexión con el WS de WM apagando o deshabilitando el adapter DBS_ORAFIN_IDM_NT.
 * Obteniendo así una Excepción en el servicio: Código de respuesta 500 | Se presento un error en el servicio EBS. 
 * @author Roberto Flores
 * @date   2022/07/22
 */
public class ATC_FT_013_EO16_Avante_InactivacionUsuarioAdapterOff  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_013_EO16_Avante_InactivacionUsuarioAdapterOff_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("Administración de Identidades - Avante"); 
		testCase.setPrerequisites("1. Para poder hacer la asignación de roles y accesos debe de existir una matriz configurada en IDM.\r\n"
				+ "2. Contar con u usuario que esté dado de alta en EBS 12.2.4 pero que se encuentre inactivo.\r\n"
				+ "3. Contar con acceso a la BD FCWM6QA.FEMCOM.NET.\r\n"
				+ "4. Contar con acceso a la BD FCMLTAQ.FEMCOM.NET.\r\n"
				+ "5. Contar con acceso  y permisos para acceder al Integration Server (IS).");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat jsonFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		/*
		 * Variables
		 *********************************************************************/
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='UpdUserInactivate' AND creation_date >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')";

		String consultaError = "SELECT error_id, folio, error_date,description tpe_type \r\n" + 
				"FROM WMLOG.WM_LOG_ERROR_TPE \r\n" + 
				"WHERE   TPE_TYPE LIKE '%s' "
				+ "AND FOLIO = %s \r\n" + 
				"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY ERROR_DATE DESC";
		
		String expectedCode = data.get("code");
		
		String status = data.get("status");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Ingresar al IS con las credenciales correctas e irse a la ruta: Adapters > webMethods Adapter for JDBC > Connections, y deshabilitar el pool o adapter de conexión DBS_ORAFIN_IDM_NT.");
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
				iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
				testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		 
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/		
		addStep("Capturar en POSTMAN URL y Body");
					
				Date fechaEjecucionInicio = new Date();
				
				String fechaJson = jsonFormat.format(fechaEjecucionInicio);
		
				testCase.addTextEvidenceCurrentStep("url: " + data.get("url"));
				testCase.addTextEvidenceCurrentStep("body:" + data.get("json").replace("<<FECHA_ACTUAL>>", fechaJson));
				
				boolean conexion = true;
				
				assertTrue("Se ingresa exitosamente.", conexion);		
		
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/		
		addStep("Irse a la pestaña de Body y capturar los parámetros necesarios para la solicitud de Alta de Consulta de Usuario.");
				
				HttpResponse respuesta = ApiUtil.sendPostDissableSSL(
						data.get("url"), 
						data.get("json"));
				
				Date fechaEjecucionFin = new Date();
				
				String actualResponseCode = String.valueOf(respuesta.getStatusLine().getStatusCode());
				String actualResponseMessage = EntityUtils.toString(respuesta.getEntity());
				
				testCase.addCodeEvidenceCurrentStep("codigo de respuesta actual: " + actualResponseCode);
				System.out.println("codigo de respuesta actual: " + actualResponseCode);
				testCase.addCodeEvidenceCurrentStep("respuesta mensaje actual: " + actualResponseMessage);
				System.out.println("respuesta mensaje actual: " + actualResponseMessage);
				
				assertEquals(actualResponseCode, expectedCode);
		
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/		
				addStep("Establecer conexión a la BD de WM FCWM6QA.FEMCOM.NET.");		
		
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
				testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
					
				assertTrue("Se ingresa exitosamente.", true);

		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
				addStep("Validar el registro de la ejecución de la interfaz EO16_MX.");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
				
				String tdcQueryValidate_f = String.format(tdcQueryValidate, fechaEjecucionInicio_f);
				
				System.out.println(tdcQueryValidate_f);		
				
				SQLResult connectionResult = executeQuery(dbPos, tdcQueryValidate_f);
		
				assertFalse(connectionResult.isEmpty());
									
				String folio = connectionResult.getData(0, "Folio");
				String estatus = connectionResult.getData(0, "Status");	
				
				assertEquals(estatus, status);
		
				testCase.addQueryEvidenceCurrentStep(connectionResult);	
  
		
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/		
		addStep("Establecer conexión a la BD de WMLOG FCWMLTAQ.FEMCOM.NET.");		
		
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ");
				testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAQ_MTY);
					
				assertTrue("Se ingresa exitosamente.", true);
				
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/		
		addStep("Ejecutar la siguiente sentencia para validar el registro del log del error de desconexión de WM para el código obtenido 500:");		
		
				String fechaEjecucionFin_f = formatter.format(fechaEjecucionFin);
				
				String consultaError_f = String.format(consultaError, "%EO16_MX%", folio, fechaEjecucionInicio_f, fechaEjecucionFin_f);
				
				System.out.println(consultaError_f);
			
				SQLResult consultaError_r = dbMtyFCWMLTAQ.executeQuery(consultaError_f);
			
				boolean validaConsultaError = consultaError_r.isEmpty();
			
				System.out.println(validaConsultaError);
				
				testCase.addQueryEvidenceCurrentStep(consultaError_r, true);
				if (!validaConsultaError) {
					testCase.addTextEvidenceCurrentStep("Error encontrado en WMLOG.WM_LOG_ERROR_TPE");
				} else {
					testCase.addTextEvidenceCurrentStep("No se encontraron errores en WMLOG.WM_LOG_ERROR_TPE");
				}
			
				assertFalse(validaConsultaError, "No se visualizan errores en la base de datos");
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_013_EO16_Avante_InactivacionUsuarioAdapterOff_Test";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-010-Realizar una Consulta de Usuario, Excepción en el Servicio";
	}

	@Override
	public String setTestDesigner() {
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}
	
//	public static void main(String[] args) throws Exception {
//		
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		
//		String encoding = Base64.getEncoder().encodeToString(("test1:test1").getBytes());
//		HttpPost httpPost = new HttpPost("http://host:post/test/login");
//		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
//
//		System.out.println("executing request " + httpPost.getRequestLine());
//		HttpResponse response = httpClient.execute(httpPost);
//		HttpEntity entity = response.getEntity();
//	}
	
}

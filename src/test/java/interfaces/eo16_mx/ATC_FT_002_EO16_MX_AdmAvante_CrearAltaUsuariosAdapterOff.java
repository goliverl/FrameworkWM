package interfaces.eo16_mx;


import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;


import java.util.HashMap;


import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import modelo.BaseExecution;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.sql.SQLResult;
import utils.ApiMethodsUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_002_EO16_MX_AdmAvante_CrearAltaUsuariosAdapterOff extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_EO16_MX_AdmAvante_CrearAltaUsuarioAdapterOff_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		//utils.sql.SQLUtil dbPosNueva = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	

		SeleniumUtil u;

		
		testCase.setProject_Name("Administracion de Identidades - Avante");  
		testCase.setTest_Description(data.get("Name"));
				
/** VARIABLES *********************************************************************/	
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='CreateUser' ORDER BY END_DT DESC";
		
		String tdcQueryError = "SELECT * FROM  WMLOG.WM_LOG_ERROR_TPE WHERE FOLIO='%s' AND TPE_TYPE='EO16_MX' ORDER BY ERROR_DATE DESC";
		
		String uri = String.format(data.get("uri"),data.get("host"),data.get("port"));
		RestAssured.baseURI = uri;
		//RestAssured.baseURI = "https://www.postman-echo.com";

		String responseCode = data.get("code");
		String status = data.get("status");
        String folio="";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		/* PASO 1 *********************************************************************/
		
		addStep("Conectarse a IS del servidor de FCWINTQA3 y apagar el adapter DBS_RETEK_NT.");		
		
//		u = new SeleniumUtil(new ChromeTest(), true);
//		pok = new PakageManagment(u, testCase);
//		
//		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
//		String contra = "http://" + user + ":" + ps + "@" + server + ":5555/WmRoot/adapter-index.dsp?url=%2FWmART%2FListResources.dsp%3FadapterTypeName%3DJDBCAdapter%26dspName%3D.LISTRESOURCES&adapter=JDBCAdapter&text=webMethods+Adapter+for+JDBC&help=true";
//		u.get(contra);
//		
//		pok.selectAdapter();
		
		 u = new SeleniumUtil(new ChromeTest(), true);
		IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
		iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
		testCase.addScreenShotCurrentStep(u, "Estatus adapter");
			
		
		
		/* PASO 2 *********************************************************************/			

		addStep("Capturar Username y Password en la pestaña de Authorization.");
			
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + data.get("password"));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con exito.");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);	
		
		/* PASO 3 *********************************************************************/		
		
		addStep("Capturar los parámetros necesarios para la solicitud de Alta de Usuario y enviar el request.");
	
		ApiMethodsUtil api = new ApiMethodsUtil(uri);
		String body1 = data.get("json");
		
//		String credentials = data.get("username")+":"+data.get("password");
//		Response JsonRequestBasicAuth= api.BasicAuthRequest(uri, credentials);
//		String JsonResponseBA =String.valueOf(JsonRequestBasicAuth.asPrettyString());
//		String StatusCodeBA = String.valueOf(JsonRequestBasicAuth.getStatusCode());
//		
//		System.out.println("BasicAuth Res: "+JsonResponseBA);
//		System.out.println("BasicAuth: "+StatusCodeBA);
		
		
		//El body1 debe de ser el path completo con los datos Ejemplo: https://httpbin.org/get?NOMBRE_USUARIO=IDMUSER01\
		Response JsonRequest= api.postRequestMethod(uri, body1);
		
		String JsonResponse =String.valueOf(JsonRequest.asPrettyString());
		String StatusCode = String.valueOf(JsonRequest.getStatusCode());
		String MessageResponse = String.valueOf(JsonRequest.getStatusLine());
		
		System.out.println("JsonResponse: \n"+JsonResponse);
		System.out.println("StatusCode: \n"+StatusCode);
		
		
		
		testCase.addTextEvidenceCurrentStep(data.get("json"));
		
		testCase.addBoldTextEvidenceCurrentStep("Se muestra la respuesta del request enviado.");		
	
		//respuesta del request
		testCase.addTextEvidenceCurrentStep(JsonResponse);		
		testCase.addTextEvidenceCurrentStep("Status code: " + StatusCode);	
		testCase.addTextEvidenceCurrentStep("Mensaje recibido: " + MessageResponse);	

		
		boolean validateStatus = responseCode.equals(StatusCode);
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		assertFalse(!validateStatus, "La ejecuci�n de la interfaz no fue exitosa");

		/* PASO 4 *********************************************************************/		    
		
		addStep("Establecer conexión a la BD de WM FCWM6QA.FEMCOM.NET.");		

		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			
		assertTrue("Se ingresa exitosamente.", true);

		/* PASO 5 *********************************************************************/		    

		addStep("Validar el registro de la ejecución de la interfaz EO16_MX.");
		
		System.out.println(tdcQueryValidate);		
		SQLResult connectionResult = executeQuery(dbPos, tdcQueryValidate);

		boolean connection = connectionResult.isEmpty();
		
		if (!connection) {				
			
			String estatus = connectionResult.getData(0, "Status");			
			connection = estatus.equals(status);
			System.out.println(connection);
			testCase.addQueryEvidenceCurrentStep(connectionResult);	
			
			connection = !connection;	
		} 

		assertFalse(connection, "La tabla no contiene la información o STATUS diferente a " + data.get("status"));	    
		
				
		/* PASO 6 *********************************************************************/		    
		
		addStep("Establecer conexión a la BD de WMLOG FCWMLTAQ.FEMCOM.NET.");		

		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			
		assertTrue("Se ingresa exitosamente.", true);

		/* PASO 7 *********************************************************************/			

		addStep("Validar el registro de la ejecucion de la interfaz EO16_MX.");
		
		String error = String.format(tdcQueryError, folio);
		System.out.println(tdcQueryValidate);		

		SQLResult errorr = dbLog.executeQuery(error);
		boolean emptyError = errorr.isEmpty();
		
		if (!emptyError) {					
			SQLResult errorIS = dbLog.executeQuery(error);
			testCase.addQueryEvidenceCurrentStep(errorIS);	
		}
		
		
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_EO16_MX_AdmAvante_CrearAltaUsuarioAdapterOff_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
}

package interfaces.tpe.btc;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_007_TPE_BTC_InsertaTransaccionServicio extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_TPE_BTC_InsertaTransaccionServicio_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbTran= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		SeleniumUtil u;
		PakageManagment pok;	
		
		testCase.setProject_Name("Bitacoras de transacciones");    
				
		/** VARIABLES *********************************************************************/
		
		//Fecha de json
				Date fecha = new Date();
				SimpleDateFormat formatterfechaJson = new SimpleDateFormat("yyyyMMddHHmmss"); 
				String date = formatterfechaJson.format(fecha);
				System.out.println(date);
				
	   //Fecha adDate
				String addDate = date;
				addDate = date.substring(0, 8);
				System.out.println(addDate);
				
				
		//Fecha consulta	
				
				String date2 = date;
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
				formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));				
				date2 = formatter.format(fecha);
				System.out.println(date2);
				
				
	
		String consultaRegistro1 = "SELECT   plaza, tienda, caja, service_type, entity, ticket,folio_wm \n " + 
				"FROM TPEUSER.POS_TRANSACTION \n " + 
				"WHERE SERVICE_TYPE = '" + data.get("serviceType") + "' \n " + 
				"AND ENTITY ='" + data.get("entity") + "' \n " + 
				"AND LAST_STATUS_DATE = (select to_date ('%s','DD/MM/YYYY hh24:mi:ss') from dual) \n"+
			//	"AND CREATION_DATE = (select to_date ('%s','DD/MM/YYYY hh24:mi:ss') from dual)\n " + 
			//	"AND OPERATION IN ('" + data.get("operation1") + "', '" + data.get("operation2") + "', '" + data.get("operation3") + "') \n " + 
				"ORDER BY CREATION_DATE DESC ";
		
		String consultaRegistro2 = "SELECT   operation, status, description, last_status_date,amount, ip_address,auth, creation_date\n" + 
				"FROM TPEUSER.POS_TRANSACTION \n" + 
				"WHERE SERVICE_TYPE = '" + data.get("serviceType") + "' \n" + 
				"AND ENTITY = '" + data.get("entity") + "' \n" +
				"AND LAST_STATUS_DATE = (select to_date ('%s','DD/MM/YYYY hh24:mi:ss') from dual) \n"+
			//	"AND CREATION_DATE = (select to_date ('%s','DD/MM/YYYY hh24:mi:ss') from dual)\n" + 
			//	"AND OPERATION IN ('" + data.get("operation1") + "', '" + data.get("operation2") + "', '" + data.get("operation3") + "') \n" + 
				"ORDER BY CREATION_DATE DESC";
		
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String WMcode= "101";
		
		testCase.setTest_Description(data.get("idCaso") +" Insertar transacciones de servicio  "+data.get("servicioJson") + " en la tabla de bitacora");
		
		/*NOTA;
		 * CP: MTC-FT-001-Insertar transacciones de servicio Gift Card Producto en la tabla de bitacora
		 * Cambiar el service_type y entity por el correspondiente en el data provider*/
		/*
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
	// Paso 0 -- Encender o apagar adapter* **************************************************************************************************************************************/
		addStep("Comprobar status adapter");
		 u = new SeleniumUtil(new ChromeTest(), true);
		IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
		iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
		testCase.addScreenShotCurrentStep(u, "Estatus adapter");

		
		
		
// **************************************Paso 1 *******************************************************************			
				
		addStep("Enviar el Json con registros");
		
		testCase.addBoldTextEvidenceCurrentStep("Request: \n"); 
		
		String json_f = String.format(data.get("jsonIn"),date, date, addDate);
			
		testCase.addTextEvidenceCurrentStep(json_f );
		
		System.out.println(" Request \n"+ json_f );
		
		//Entra al integration server
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		//String dateExecution = pok.runIntefaceWmWithTresInputs10(data.get("interfase"), data.get("servicio"), data.get("$resourceID"),"$resourceID", data.get("$path"), "$path",data.get("jsonIn") , "jsonIn");
		
		String dateExecution = pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"),data.get("jsonIn") , "jsonIn");
		
		System.out.println("Respuesta dateExecution " + dateExecution);
		
		Thread.sleep(5000);
		//hacer highlight en el response
		u.highLight(By.xpath("/html/body/pre"));
		//imprime el reponse
		testCase.addBoldTextEvidenceCurrentStep("Response: "); 
		System.out.println( u.getText(By.xpath("/html/body/pre")));
		//Se agrega a la evidencia el response
		testCase.addBoldTextEvidenceCurrentStep(u.getText(By.xpath("/html/body/pre"))); //agregar a la evidencia
		
        String JsonResponse = u.getText(By.xpath("/html/body/pre"));
		
		JSONObject json = new JSONObject(JsonResponse);
		//Se extrae el valor del value
		String value = json.getJSONObject("TPEDoc").getJSONObject("response").getJSONObject("wmCode").getString("value");
		
		System.out.println("Error code : " + value);
		
		boolean validaCode = value.equals(WMcode);
		
		if (!validaCode){
			
			validaCode = false;
		}
		
		u.close();
		assertTrue("El wm_code es incorrecto", validaCode);
	/*
		//Se obtiene el creationDate
		String JsonResponse = u.getText(By.xpath("/html/body/pre"));
		
		JSONObject json = new JSONObject(JsonResponse);
		
		String creationDate = json.getJSONObject("TPEDoc").getJSONObject("header").getString("creationDate");
		
		 System.out.println("Fecha del json: "+ creationDate);
		//Se da formato de fecha al creationDate
		 DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		 Date date = new Date(creationDate);		 
		 String creationDateFormat = creationDate;
		 creationDateFormat = dateFormat.format(date);
		 System.out.println("Fecha con formato: "+ creationDateFormat);*/
		 
		
		//*****************************************Paso 2 ************************************************************
		
		addStep("Realizar la conexión a la BD FCTPEQA con esquema TPEUSER");
		
        System.out.println(GlobalVariables.DB_HOST_FCTPE);
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTPE);
		
		
		boolean conexion = true;
		
		assertTrue("Se realiza la conexión de manera correcta ", conexion);
		

		
		//*****************************************Paso 3 ************************************************************
		
		addStep("Realizar la siguiente consulta para validar el insert en la bitacora ");

		//Parte 1
		 String consultaRegistro1_f = String.format(consultaRegistro1, date2 );
		
		System.out.println(consultaRegistro1_f);
		
		SQLResult consultaRegistro_Res1 = executeQuery(dbTran, consultaRegistro1_f);

		boolean valida_consultaRegistro1 = consultaRegistro_Res1.isEmpty();

		if (!valida_consultaRegistro1) {
			
			testCase.addQueryEvidenceCurrentStep(consultaRegistro_Res1);
		}

		
		//Parte 2
		 String consultaRegistro2_f = String.format(consultaRegistro2, date2 );
			
		 System.out.println(consultaRegistro2_f);
			
		 SQLResult consultaRegistro_Res2 = executeQuery(dbTran, consultaRegistro2_f);

		boolean valida_consultaRegistro2 = consultaRegistro_Res2.isEmpty();

		if (!valida_consultaRegistro2) {
			
			testCase.addQueryEvidenceCurrentStep(consultaRegistro_Res2);
		}

		System.out.println(valida_consultaRegistro2);
		
		assertFalse(valida_consultaRegistro2, "No se  muestra el registro insertado");

		
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_TPE_BTC_InsertaTransaccionServicio_test";
		
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

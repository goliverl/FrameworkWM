package interfaces.tpe.btc;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
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
 *  I20017 Bitácora de Transacciones: MTC-FT-021-Insertar transacciones de servicio de Corresponsalias con tarjeta en la tabla de sumarizado
 * Desc:
 * Se valida la ejecucion de la operacion QRY02 perteneciente a la interfaz FEMSA_TPE_BTC con la finalidad de insertar transacciones de Corresponsalias con tarjeta en la tabla de sumarizado  
 * @author Roberto Flores
 * @date   2022/07/25
 */
public class ATC_FT_003_BTC_Bitacora_ErrorInsercionBitacora  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_BTC_Bitacora_ErrorInsercionBitacora_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("I20017 Bitácora de Transacciones"); 
		testCase.setTest_Description(data.get("name") + data.get("desc"));
		testCase.setPrerequisites("Contar con acceso a la BD FCTPEQA \r\n"
				+ "-Contar con información en las tablas para de bitacora\r\n"
				+ "Contar con acceso a la BD FCWMQA con esquema WMLOG");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		utils.sql.SQLUtil FCTPE = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil FCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		
		/*
		 * Variables
		 *********************************************************************/
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));		
		String server = data.get("server");
		
		
		String consultaRegistro1 = "SELECT   plaza, tienda, caja, service_type, entity, ticket,folio_wm \n " + 
				"FROM TPEUSER.POS_TRANSACTION \n " + 
				"WHERE SERVICE_TYPE = '" + data.get("serviceType") + "' \n " + 
				"AND ENTITY ='" + data.get("entity") + "' \n " + 
				"AND creation_date >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss') \n"+
				"ORDER BY CREATION_DATE DESC ";
		
		String consultaRegistro2 = "SELECT   operation, status, description, last_status_date,amount, ip_address,auth, creation_date\n" + 
				"FROM TPEUSER.POS_TRANSACTION \n" + 
				"WHERE SERVICE_TYPE = '" + data.get("serviceType") + "' \n" + 
				"AND ENTITY = '" + data.get("entity") + "' \n" +
				"AND creation_date >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss') \n"+
				"ORDER BY CREATION_DATE DESC";
		
		String qryWmLogRun = "select * \r\n"
				+ "from wmlog.WM_LOG_ERROR_TPE \r\n"
				+ "where TPE_TYPE = '%s' AND ERROR_CODE = 199 \r\n"
				+ "AND ERROR_DATE >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		SeleniumUtil u;
//		/****************************************************************************************************************************************
//		 * Paso 1, 2, 3, 4
//		 * **************************************************************************************************************************************/
//		addStep("Deshabilitar adapter");
//				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
//				IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("user"), PasswordUtil.decryptPassword(data.get("ps")), data.get("server") + ":" + data.get("port") );
//				iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
//				testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		 
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/		
		addStep("Enviar el Json con registros");
					
				Date fechaEjecucionInicio = new Date();
				
				PakageManagment pok;
				
				//Fecha de json pvDate
				Date fecha = new Date();
				SimpleDateFormat formatterfechaJson = new SimpleDateFormat("yyyyMMddHHmmss"); 
				String date = formatterfechaJson.format(fecha);
				System.out.println(date);
				
				//Fecha de json addDate
				String addDate = date;
				SimpleDateFormat formatterfechaJson2 = new SimpleDateFormat("yyyyMMdd"); 
				addDate = formatterfechaJson2.format(fecha);
				//addDate.substring(0, 8);
				System.out.println(addDate);
				
				testCase.addBoldTextEvidenceCurrentStep("Request: "); 
				String ConsultaReferencias_f = String.format(data.get("jsonIn"),date, date, addDate );
				testCase.addTextEvidenceCurrentStep(ConsultaReferencias_f);
				
				System.out.println(" Request \n"+ ConsultaReferencias_f
						);
				
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
				String desc = json.getJSONObject("TPEDoc").getJSONObject("response").getJSONObject("wmCode").getString("desc");
					
				System.out.println("Code : " + value);
				System.out.println("desc : " + desc);
				
				u.close();
				
				boolean codeCorrecto = true;
				
				if(value.equals("109"))
				{
					codeCorrecto = true;
					
				} else {
					
					codeCorrecto = false;
					
				}
				assertTrue(codeCorrecto, "El code es diferente al esperado");
		
				
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/		
		addStep("Realizar la conexión a la BD FCTPEQA con esquema TPEUSER");
					
				System.out.println(GlobalVariables.DB_HOST_FCTPE);
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA");
				testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTPE);
				
		
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/		
		addStep("Realizar la siguiente consulta para validar que no se inserto informacion en la bitacora ");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				//Parte 1
				String consultaRegistro1_f = String.format(consultaRegistro1, fechaEjecucionInicio_f);
				System.out.println(consultaRegistro1_f);
				SQLResult consultaRegistro1_r = executeQuery(FCTPE, consultaRegistro1_f);
		
				testCase.addQueryEvidenceCurrentStep(consultaRegistro1_r, true);
				
				//assertFalse(consultaRegistro1_r.isEmpty(), "No se encuentran registros en TPEUSER.POS_TRANSACTION");
		
				
				//Parte 2
				String consultaRegistro2_f = String.format(consultaRegistro2, fechaEjecucionInicio_f);
				System.out.println(consultaRegistro2_f);
				SQLResult consultaRegistro2_r = executeQuery(FCTPE, consultaRegistro2_f);
		
				testCase.addQueryEvidenceCurrentStep(consultaRegistro2_r, true);
				
				//assertFalse(consultaRegistro2_r.isEmpty(), "No se encuentran registros en TPEUSER.POS_TRANSACTION");
		
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/		
		addStep("Realizar la conexión a la BD FCWMLTAEQA con esquema WMLOG");
					
				System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAEQA");
				testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);	
	
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/		
		addStep("Realizar la siguiente consulta para validar el error");		
		
				
				String qryWmLogRun_f = String.format(qryWmLogRun, "%SE.CTR%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLTAEQA, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
					
				
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_003_BTC_Bitacora_ErrorInsercionBitacora_Test";
	}

	@Override
	public String setTestDescription() {
		return "ATC-FT-008-Realizar una Consulta de Usuario, Exitosa";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
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
	
	
	
}
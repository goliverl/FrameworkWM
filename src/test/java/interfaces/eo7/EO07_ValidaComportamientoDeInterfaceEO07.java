package interfaces.eo7;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class EO07_ValidaComportamientoDeInterfaceEO07 extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO07_001_Valida_Comportamiento_Interface_EO07(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Validar el comportamiento de la interface EO07 Cuando el archivo a procesar se encuentra vacio
		 * Validar el comportamiento de la interface EO07 Cuando el archivo contiene datos distintos a los que se esperaban
		 * Validar el comportamiento de la interface EO07 Cuando el nombre del archivos a procesar no cumple con la nomenclatura
		 * Validar el comportamiento de la interface EO07 Cuando la cuenta que se extrae del archivo a procesar no existe en la tabla CE_BANK_ACCOUNTS.
		 * Validar el comportamiento de la interface EO07 Cuando no se encuentre archivos en el buzón FTP
		 */
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String validaFTP =	"SELECT trim(OPERACION), trim(VALOR1), trim(VALOR2), trim(VALOR3), trim(VALOR4) ,trim(VALOR5), trim(VALOR6) "
				+ "FROM WMLOG.WM_INTERFASE_CONFIG "
				+ "WHERE INTERFASE = 'EO07'  "
				+ "AND OPERACION IN ('FTP')";

		// Paso 3
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'EO07'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'E'  ORDER BY START_DT DESC";
		
//		*******
//		Paso 4
	
		String ValidLogError=	"SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ "FROM WMLOG.WM_LOG_ERROR  "
				+ "WHERE RUN_ID='%s'";


//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='EO07' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************

		addStep("Validar la configuraci�n del servidor FTP en la tabla WM_INTERFASE_CONFIG de WMUSER");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(validaFTP);

		SQLResult ConsRegRes = dbPos.executeQuery(validaFTP);

		boolean ValidaDatBool = ConsRegRes.isEmpty(); 


		if (!ValidaDatBool) {
			testCase.addQueryEvidenceCurrentStep(ConsRegRes); 
		}

		System.out.println(ValidaDatBool); 
		assertFalse("No hay configuraci�n del servidor FTP en la tabla WM_INTERFASE_CONFIG de WMUSER para la interfaz EO07", ValidaDatBool);
																						

//*************************************************Paso 2***********************************************************************************************************************
		
		
		addStep("Ejecutar el servicio EO07.Pub:run solicitando la ejecuci�n del job runEO07 de CtrlM "
				+ "para procesar el archivo en el directorio FTP de la interface.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);


		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}
		
		
		
		
		
//**********************************************************Paso 3*************************************************************************************************************		
			
		addStep("Validar la insercion de los detalle de la ejecucion de la interface EO07 "
				+ "en la tabla WM_LOG_RUN de WMLOG con STATUS = 'E'.");
		String RUN_ID ="";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: "+ RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la informacion.", LogRequest);
		
		
		
//*********************************Paso 4*********************************************************

		addStep("Validar la insercion de los detalles del error generado durante la "
				+ "ejecucion de la interface EO07 en la tabla WM_LOG_ERROR de WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);// Pos
		String FormatLogErr= String.format(ValidLogError, RUN_ID);
		System.out.println(FormatLogErr);

		SQLResult verifComp = dbPos.executeQuery(FormatLogErr);

		boolean validResComp = verifComp.isEmpty();

		if (!validResComp) {

			testCase.addQueryEvidenceCurrentStep(verifComp);

		}

		System.out.println(validResComp);
		assertFalse("No se muestran registros ", validResComp);

		



	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO07_001_Valida_Comportamiento_Interface_EO07";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Valida el comportamiento de la interfaz EO07 con ciertas restricciones (archivo con datos distintos, archivo vacio, no se encuentra el archivo, etc.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
}

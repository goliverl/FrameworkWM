package interfaces.eo06;

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

public class ATC_FT_EO06_001_Procesar_Archivos_WU_Invalidos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO06_001_Procesar_Archivos_WU_Invalidos_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Procesar archivos WesternUnion con prefijo REM_WUW con detalle invalido
		 * Procesar archivos WesternUnion con prefijo REM_WUO con detalle inválido
		 * Procesar archivos WesternUnion inválidos
		 */
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String validaFTP="SELECT trim(OPERACION), trim(VALOR1), trim(VALOR2), trim(VALOR3), trim(VALOR4) ,trim(VALOR5), trim(VALOR6)  "
				+ "FROM WMUSER.WM_INTERFASE_CONFIG  "
				+ "WHERE INTERFASE = 'EO06'  "
				+ "AND ENTIDAD = 'WU' "
				+ "AND OPERACION IN ('SFTP','FS')  "
				+ "AND CATEGORIA = 'CONFIG'";

		// Paso 2
		String ValidArch= "SELECT ENTITY, VALUE1 AS PREFIJO, VALUE2, VALUE4 "
				+ "FROM WMUSER.BO_CONFIG "
				+ "WHERE APLICATION = 'EO06' "
				+ "AND ENTITY IN ('TELECOM', 'WU')";
		
				
		
//		*******
//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'EO06'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'E'  ORDER BY START_DT DESC";
		

//	Paso 5 
		 String ValidArchivo= "SELECT trim(OPERACION), trim(VALOR1), trim(VALOR2), trim(VALOR3), trim(VALOR4) ,trim(VALOR5), trim(VALOR6) "
			 		+ "FROM WMUSER.WM_INTERFASE_CONFIG  "
			 		+ "WHERE INTERFASE = 'EO06'  "
			 		+ "AND ENTIDAD IN ('WU_NV') "
			 		+ "AND OPERACION IN ('SFTP','FS')  "
			 		+ "AND CATEGORIA = 'CONFIG'"; 

//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='EO06' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************
		
		addStep("Validar la configuracion del servidor FTP en la tabla WM_INTERFASE_CONFIG de WMUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(validaFTP);

		SQLResult ConsRegRes = dbPos.executeQuery(validaFTP);

		boolean ValidaDatBool = ConsRegRes.isEmpty(); 


		if (!ValidaDatBool) {
			testCase.addQueryEvidenceCurrentStep(ConsRegRes); 
		}

		System.out.println(ValidaDatBool); 
		assertFalse("No hay configuraci�n del servidor FTP en la tabla WM_INTERFASE_CONFIG de WMUSER", ValidaDatBool);
																						

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Validar que existan archivos pendientes de procesar con el prefijo diferente a los "
				+ "configurados en la tabla BO_CONFIG de WMUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);// RMS
		System.out.println(ValidArch);
		SQLResult verif = dbPos.executeQuery(ValidArch);

		boolean validRes = verif.isEmpty();

		if (!validRes) {

			testCase.addQueryEvidenceCurrentStep(verif);

		}

		System.out.println(validRes);
		assertFalse("No existan archivos pendientes de procesar", validRes);

//**********************************************************Paso 3*************************************************************************************************************		
		
		addStep("Ejecutar el servicio EO06.Pub:run desde el Job EO06 de Ctrl-M.");

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
		
		
		
//*********************************************************Paso 4**************************************************************************************************
		
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'E'.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {

			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la informaci�n.", LogRequest);
		
		
		
//*********************************Paso 5*********************************************************
		 
		addStep("Validar que el archivo inv�lido procesado, es movido a la carpeta "
				+ "de archivos invalidos configurada en tabla WM_INTERFASE_CONFIG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);// Pos
		System.out.println(ValidArchivo);

		SQLResult verifComp = dbPos.executeQuery(ValidArchivo);

		boolean validResComp = verifComp.isEmpty();

		if (!validResComp) {

			testCase.addQueryEvidenceCurrentStep(verifComp);

		}

		System.out.println(validResComp);
		assertFalse("No se muestran registros ", validResComp);

		

//********************************************Paso 6**************************************************************************************************************************

//		Verificar la eliminaci�n del archivo procesado en el FileSystem configurado para la interface.

		//Pendiente hasta tener contrase�a para conexion al servidor ftp
//		FTP_SERVERHOST: 10.182.32.13
//		FTP_SERVERPORT: 22
//		FTP_USERNAME: wmuser
//		FTP_PASSWORD: ???????(Desconocido)
		
		
//		addStep("Comprobar que se envio el documento al servidor FTP: /u01/posuser/FEMSA_OXXO/TNDES/"
//				+ data.get("cr_plaza") + "/working/" + DOC);
//
//		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
//
//		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("cr_plaza") + "/working/" + DOC;
//		System.out.println("Ruta: " + ruta);
//
//		if (ftp.fileExists(ruta)) {
//
//			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
//					+ data.get("cr_plaza") + "/working/" + DOC);
//
//		} else {
//
//			System.out.println("No Existe");
//
//		}
//
//		assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);


	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Procesar archivos Western Union invalidos";
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

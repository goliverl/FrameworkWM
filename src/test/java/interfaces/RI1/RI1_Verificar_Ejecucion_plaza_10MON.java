package interfaces.RI1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;
import utils.FTPUtil;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class RI1_Verificar_Ejecucion_plaza_10MON extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RI1_Verificar_Ejecucion_Interfaz(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String validaDoc = "";

		 validaDoc =	"SELECT  \r\n" + 
				"      DISTINCT TH.FROM_LOCATION, TH.TO_PLAZA, TH.LOAD_WEEK \r\n" + 
				" FROM WMUSER.TRANSFER_HEAD TH, \r\n" + 
				" WMUSER.TRANSFER_DETAIL TD, \r\n" + 
				" XXFC.XXFC_PEDIDOS_EXT_PLAZAS XPEP, \r\n" + 
				" XXFC.XXFC_PEDIDOS_EXT_ITEMS XPEI \r\n" + 
				" WHERE     TH.LOAD_BATCH_ID = TD.LOAD_BATCH_ID \r\n" + 
				"       AND TH.TRANSACTION_ID = TD.TRANSACTION_ID \r\n" + 
				"       AND TH.LOAD_WEEK = TD.LOAD_WEEK \r\n" + 
				"       AND TH.TO_IMMEX = 1 \r\n" + 
				"       AND TH.RI1_STATUS = 'L' \r\n" + 
				"       AND TH.TO_PLAZA = XPEP.PLAZA \r\n" + 
				"       AND TH.TO_PLAZA = XPEI.PLAZA \r\n" + 
				"       AND TH.LOAD_WEEK IN \r\n" + 
				"              (TO_CHAR (SYSDATE, 'IW'), TO_CHAR (SYSDATE - 7, 'IW')) \r\n" + 
				"AND TH.TO_PLAZA='10MON'";
			/*	"       AND TD.ITEM = XPEI.ITEM \r\n" + 
				"       AND XPEI.TIPO_MOV = 2 \r\n" + 
				"       AND TO_DATE (TH.APPROVAL_DATE, 'YYYYMMDDHH24MISS') BETWEEN XPEP.FCH_VIG_INI \r\n" + 
				"                                                              AND XPEP.FCH_VIG_FIN \r\n" + 
				"       AND TO_DATE (TH.APPROVAL_DATE, 'YYYYMMDDHH24MISS') BETWEEN XPEI.FCH_VIG_INI \r\n" + 
				"                                                              AND XPEI.FCH_VIG_FIN \r\n" ;
		*/
		 
		 
	  //Paso 3
		
		String ValidaRegistroWMLOG = "SELECT RUN_ID ,STATUS  FROM WMLOG.WM_LOG_RUN WHERE INTERFACE='RI1main' AND STATUS='S'  AND ROWnum < 20 ORDER BY RUN_ID DESC ";
		
		String ValidaRegistroWMLOGThread = "SELECT RUN_ID ,STATUS  FROM WMLOG.WM_LOG_RUN WHERE INTERFACE='RI1main' AND STATUS='S'  AND ROWnum < 2 ORDER BY RUN_ID DESC ";
		

		
		//Paso 5
		
		String ValidaActualizacion = "SELECT RI1_STATUS, RI1_RUN_ID, RI1_PROC_DATE, RI1_FILE \r\n" + 
				"                                                             FROM TRANSFER_HEAD \r\n" + 
				"                                                             WHERE TO_PLAZA='10MON' AND TO_IMMEX=1  AND RI1_STATUS='E'\r\n" + 
				"                                                             AND RI1_RUN_ID=";
		
		
//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************************************************************************************************************************

		addStep("Validar información disponible en RMS. ");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		

		System.out.println(validaDoc);

		SQLResult validaDocRes = dbRms.executeQuery(validaDoc);

		String FROMLOCATION_Validacion = validaDocRes.getData(0, "FROM_LOCATION");
		String TO_PLAZA_Validacion = validaDocRes.getData(0, "TO_PLAZA");

		System.out.println("TH.FROM_LOCATION  = " + FROMLOCATION_Validacion); // la imprime
		System.out.println("TH.TO_PLAZA = " + TO_PLAZA_Validacion); // la imprime

		boolean ValidaDocBool = validaDocRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaDocBool) {

			testCase.addQueryEvidenceCurrentStep(validaDocRes); // Si no esta vacio, lo agrega a la evidencia
		}

		assertFalse("No se existen registros de la interfaz en RMS", ValidaDocBool); // Si esta vacio, imprime
																							// mensaje

		System.out.println(ValidaDocBool); // Si no, imprime la fechas

//*****************************************Paso 2*****************************************************************************************************************************
		
		addStep("Ejecutar  el servicio RI1.Pub:run de la interfaz, solicitando el job:  runRI1.sh  ");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		u.getDriver().manage().timeouts().pageLoadTimeout(15, TimeUnit.MINUTES);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		//Obtenermos la interface y le quitamos los espacios
		String Interface =data.get("interfase");
		System.out.println(Interface);
		Interface = Interface.replace(" ", "");
		System.out.println(Interface);
		
		//Obtenermos la interface y le quitamos los espacios
		String Servicio =data.get("servicio");
		System.out.println(Servicio);
		Servicio = Servicio.replace(" ", "");
		System.out.println(Servicio);
		
				
		String dateExecution = pok.runIntefaceWmOneButton10(Interface, Servicio);
		
		//System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(ValidaRegistroWMLOG);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println(successRun);
													// estatus S

		
		u.close();
		
		//*****************************************Paso 3*****************************************************************************************************************************
		
		addStep("Validar registro de la ejecución de la interfaz en las tablas de WMLOG. ");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		

		System.out.println(ValidaRegistroWMLOG);

		SQLResult ValidaWMLOG = dbLog.executeQuery(ValidaRegistroWMLOG);

		String StatusWMLOG = ValidaWMLOG.getData(0, "STATUS");
		String RUN_ID_WMLOG = ValidaWMLOG.getData(0, "RUN_ID");
		System.out.println("Wm_Log Status = " + StatusWMLOG);
		System.out.println("RUN_ID = " + RUN_ID_WMLOG);
		boolean ValidaWMLOGBool = ValidaWMLOG.isEmpty(); // checa que el string contenga datos

		if (!ValidaWMLOGBool) {

			testCase.addQueryEvidenceCurrentStep(ValidaWMLOG); // Si no esta vacio, lo agrega a la evidencia
		}

		assertFalse("No existen registros en las tablas WMLog", ValidaWMLOGBool); // Si esta vacio, imprime
																							// mensaje

		System.out.println(ValidaWMLOGBool); // Si no, imprime la fechas
		

		//*****************************************Paso 4*****************************************************************************************************************************
		/*
		 * addStep("Validar registro de la ejecución de la interfaz en las tablas de WMLOG_thread. "
		 * );
		 * 
		 * System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		 * 
		 * 
		 * System.out.println(ValidaRegistroWMLOG);
		 * 
		 * SQLResult ValidaWMLOGThread = dbLog.executeQuery(ValidaRegistroWMLOG);
		 * 
		 * String StatusWMLOG = ValidaWMLOGThread.getData(0, "STATUS"); String
		 * RUN_ID_WMLOG = ValidaWMLOGThread.getData(0, "RUN_ID");
		 * System.out.println("Wm_Log Status = " + StatusWMLOG);
		 * System.out.println("RUN_ID = " + StatusWMLOG); boolean ValidaWMLOGBool =
		 * ValidaWMLOGThread.isEmpty(); // checa que el string contenga datos
		 * 
		 * if (!ValidaWMLOGBool) {
		 * 
		 * testCase.addQueryEvidenceCurrentStep(ValidaWMLOG); // Si no esta vacio, lo
		 * agrega a la evidencia }
		 * 
		 * assertFalse("No existen registros en las tablas WMLog", ValidaWMLOGBool); //
		 * Si esta vacio, imprime // mensaje
		 * 
		 * System.out.println(ValidaWMLOGBool); // Si no, imprime la fechas
		 */
		

//		************************************************Paso 4**************************
		//Pendiente hasta tener contraseña para conexion al servidor ftp
/*
 * FTP_SERVERHOST: "10.80.1.108" FTP_SERVERPORT: "21" FTP_USERNAME: "posuser"
 * FTP_PASSWORD: "???????(Desconocido)"
 */
		
		//addStep("Verificar envió del archivo plano al servidor FTP de IMMEX. ");
		/*
		 * addStep("Comprobar que se envio el documento al servidor FTP: /u01/posuser/FEMSA_OXXO/TNDES/"
		 * + data.get("cr_plaza") + "/working/" + "");
		 */
		
		/*
		 * FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
		 * 
		 * String ruta = "/FEMSA_OXXO/IMMEX/";// + data.get("cr_plaza") + "/working/" +
		 * ""; System.out.println("Ruta: " + ruta);
		 * 
		 * if (ftp.fileExists(ruta)) {
		 * 
		 * testCase.
		 * addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/IMMEX/"
		 * + data.get("cr_plaza") + "/working/" + "");
		 * 
		 * } else {
		 * 
		 * System.out.println("No Existe");
		 * 
		 * }
		 * 
		 * assertFalse( "No Existen archivos en la ruta FTP: " +
		 * ruta,!ftp.fileExists(ruta));
		 * 
		 */
		
		//*****************************************Paso 5*****************************************************************************************************************************
		
				addStep("Validar actualización del estatus a E en la tabla TRANSFER_HEAD de los registros procesados en RMS.  ");

				System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
				

				
				ValidaActualizacion = ValidaActualizacion + RUN_ID_WMLOG;
				System.out.println(ValidaActualizacion);
				SQLResult ValidaEstatus = dbRms.executeQuery(ValidaActualizacion);

				
				boolean validaInterfazBool = ValidaEstatus.isEmpty();// revisa si esta vacio
				
				
				System.out.println(validaInterfazBool);
				if (!validaInterfazBool  ) {
					
					String RMS_STATUS = ValidaEstatus.getData(0, "RI1_STATUS");

					System.out.println("RI1_STATUS = " + RMS_STATUS); // la imprime

					//testCase.addQueryEvidenceCurrentStep(ValidaEstatus); // Si no esta vacio, lo agrega a la evidencia
					if ( RMS_STATUS == "E" ) {

						testCase.addQueryEvidenceCurrentStep(ValidaEstatus); 
					}
					
				}
				
				

				assertFalse("El estatus no es E", validaInterfazBool); // Si esta vacio, imprime
																									// mensaje

				System.out.println(validaInterfazBool); // Si no, imprime la fechas
				
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_RI1_Verificar_Ejecucion_Interfaz";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Verificar ejecución de la interfaz con la plaza.";
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


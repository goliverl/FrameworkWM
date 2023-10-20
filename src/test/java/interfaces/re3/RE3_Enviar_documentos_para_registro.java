package interfaces.re3;

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

public class RE3_Enviar_documentos_para_registro extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RE3_Enviar_documentos_para_registro(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/*
		 * Variables
		 ******************************************************************************************************************************************/


		// Paso 1

		String ValidarRegistroDiaActual ="SELECT TRANSLATE( UPPER(TO_CHAR(SYSDATE, 'DAY','NLS_DATE_LANGUAGE=SPANISH')),'ÁÉ','AE' ) as dia FROM DUAL"; //RMS
		
		String ValidarRegistroDiaActualvALOR1 ="SELECT sup_id, [day] FROM POSUSER.WM_EDI_SUPPLIER_DAYS \r\n" + 
				"where sup_id = 3357 and te_id = 'I'"; //Posuser
	
		 
		 //Paso 2   Comprobar el valor de CEDIS del proveedor registros en la tabla CEDIS 
			
			String ValidaRegistroCEDIS = "SELECT DISTINCT  C.RETEK_CR FROM POSUSER.WM_EDI_SUPPLIER_DAYS A, POSUSER.WM_EDI_SUPPLIER_CEDIS B, POSUSER.CEDIS C\r\n" + 
					" WHERE A.SUP_ID = B.SUP_ID  AND B.CEDIS_ID = C.ID\r\n" + 
					" AND A.SUP_ID = [ID] ";
			
			
			
	  //Paso 3 Comprobar que existan registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE de la BD RETEK para el ID igual a 3357
		
		String ValidaRegistroWM_EDI_MAP_SUPPLIER = "SELECT s.SUPPLIER FROM WMUSER.WM_EDI_MAP_SUPPLIER S, WMUSER.WM_EDI_INTERCHANGE I WHERE S.SUPPLIER = I.SUPPLIER AND S.STATUS = I.STATUS_TYPE AND S.ID = 3357";
		
		
		//Paso 4   Comprobar que existan registros en la tabla ITEM_SUPP_COUNTRY de la BD RETEK en donde SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]. 
		
		String ValidaRegistroITEM_SUPP_COUNTRY = "SELECT ITEM,	SUPPLIER\r\n" + 
				"FROM WMUSER.ITEM_SUPP_COUNTRY WHERE SUPPLIER =  [ID]";
		
		
		//Paso 5 Comprobar la FECHA limite para procesar los datos, en la tabla WM_EDI_INV_CONTROL de la BD RETEK. 
		
		String ValidaRegistroWM_EDI_INV_CONTROL = "SELECT DISTINCT TRUNC(LAST_SENT_DATE) LAST_SENT_DATE FROM WMUSER.WM_EDI_INV_CONTROL\r\n" + 
				"WHERE SUPPLIER =  [WM_EDI_MAP_SUPPLIER.SUPPLIER] \r\n" + 
				"AND CEDIS =[CEDIS.RETEK_CR] \r\n" + 
				"AND ITEM =  [ITEM_SUPP_COUNTRY.ITEM] ";
		
		//Paso 6 Comprobar que existan registros en la tabla VM_ITEM_LOC_SOH, de la BD POSUSER. 
		
		String ValidaRegistroVM_ITEM_LOC_SOH = "SELECT ITEM, ITEM_PARENT,AV_COST FROM WMUSER.VM_ITEM_LOC_SOH \r\n" + 
				"WHERE ITEM =  [ITEM_SUPP_COUNTRY.ITEM] \r\n" + 
				"AND LOC = [CEDIS.RETEK_CR] \r\n" + 
				"AND LOC_TYPE = 'W' \r\n" + 
				"AND TO_DATE(SOH_UPDATE_DATETIME, 'YYYY-MM-DD HH:MI:SS') >= TO_DATE('[DATE] 01:00:00', 'YYYY-MM-DD HH:MI:SS')";
		
		//Paso 7 Ejecutar el servicio RE3.Pub:run 
		
		
		
		//Paso 8 Confirmar que exista registro de la correcta ejecucion de la interfaz en la tabla  WM_LOG_RUN de la BD WMLOG para la interfaz RE3. 
		
		String ValidaRegistroWM_LOG_RUN= "SELECT RUN_ID ,STATUS  FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'RE3' AND TRUNC(END_DT) = TRUNC(SYSDATE) AND STATUS = 'S' ORDER BY RUN_ID DESC";
		
		//Paso 9 Confirmar que existan los registros de los documentos procesados en la tabla WM_LOG_THREAD de la BD WMLOG. 
		
		String ValidaRegistroWM_LOG_THREAD = "SELECT THREAD_ID , PARENT_ID ,NAME FROM WMLOG.WM_LOG_THREAD  WHERE PARENT_ID = [WM_LOG_RUN.RUN_ID] AND \r\n" + 
				"TRUNC(END_DT) = TRUNC(SYSDATE)  AND STATUS = 'S' ";
		
		//Paso 10 Comprobar que se INSERTEN los registros en la tabla EDI_OUTBOUND_DOCS de la BD RETEK en donde DATE_SENT es igual a la fecha actual, y SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]. 
		
		String ValidaRegistroEDI_OUTBOUND_DOCS = "SELECT DATE_SENT,SUPPLIER,DOC_NAME FROM WMUSER.EDI_OUTBOUND_DOCS WHERE TRUNC(DATE_SENT) = TRUNC(SYSDATE) \r\n" + 
				"AND RUN_ID = [WM_LOG_RUN.RUN_ID] AND SUPPLIER = [WM_EDI_MAP_SUPPLIER.SUPPLIER] AND STATUS = 'E' ";
		
		//Paso 11 Comprobar que se INSERTEN el registro en la tabla WM_EDI_CONTROL_ENVIO en la BD POSUSER en donde SUP_ID es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]. 
		
		String ValidaRegistroWM_EDI_CONTROL_ENVIO = "SELECT SUP_ID ,CEDIS,LAST_SENT_DATE ,TRANSACTION ,STATUS ,DOC_NAME FROM POSUSER.WM_EDI_CONTROL_ENVIO WHERE SUP_ID = [ID] AND \r\n" + 
				"TRANSACTION = 'INV' AND STATUS = 'E' AND TRUNC(LAST_SENT_DATE) = TRUNC(SYSDATE) ";
		
		//Paso 12 Comprobar que se recibieron los documentos en el servidor TN, verificando los archivos LOG. 
		
		//String ValidaRegistroWMLOG = "SELECT RUN_ID ,STATUS  FROM WMLOG.WM_LOG_RUN WHERE INTERFACE='RI1main' AND STATUS='S'  AND ROWnum < 20 ORDER BY RUN_ID DESC ";
		
		
//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************
	
		addStep("Validar que el ID este activo para su procesamiento en el día actual: ");
		
		System.out.println("Paso 1: Validar que el ID este activo para su procesamiento en el día actual");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(ValidarRegistroDiaActual);
		

		SQLResult SQLRegistrodiaActual = dbPos.executeQuery(ValidarRegistroDiaActual);
		
		boolean ValidaRegistrodiaActual =SQLRegistrodiaActual.isEmpty(); 
		
		if (!ValidaRegistrodiaActual) {
			
			testCase.addQueryEvidenceCurrentStep(SQLRegistrodiaActual); 
		}
		
	System.out.println(ValidaRegistrodiaActual); 
	
	assertFalse(  "No hay registros en la tabla para el dia actual", ValidaRegistrodiaActual);
	
	String DiaActual = SQLRegistrodiaActual.getData(0, "dia"); //extrae dia actual
	
	System.out.println(DiaActual.toUpperCase()); //imprime dia actual
	
	System.out.println("Paso 1: El campo del día actual debe tener el valor 1 ");

	addStep("Validar que el campo del día actual tenga en  valor 1 ");
		
		
				
		ValidarRegistroDiaActualvALOR1 = ValidarRegistroDiaActualvALOR1.replace("[day]", DiaActual.toLowerCase());
		
		System.out.println(ValidarRegistroDiaActualvALOR1);
		
		SQLResult SQLRegistrosDiarios = dbPos.executeQuery(ValidarRegistroDiaActualvALOR1);
		
		//lunes, martes, miercoles, jueves, viernes, sabado, domingo

		boolean ValidaDatBool = SQLRegistrosDiarios.isEmpty(); 
				
		
		if (!ValidaDatBool) {
			
			
			String RegistroActual = SQLRegistrosDiarios.getData(0, 1); //obtiene el valor del dia actual
					
			System.out.println("Valor de dia actual = " + RegistroActual); //imrpime su valor
			

			if (RegistroActual=="1") { //revisa que sea igual a 1 y lo agrgega a la evidencia
				
				testCase.addQueryEvidenceCurrentStep(SQLRegistrosDiarios); 
			}
					
			 
		}
		System.out.println(ValidaDatBool); 
		
		assertFalse("No hay registros en la tabla con el valor 1 ", ValidaDatBool);

																						

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Comprobar el valor de CEDIS del proveedor registros en la tabla CEDIS de la BD POSUSER. ");
		
		System.out.println("Paso 2");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String IDAProceso = data.get("ID");
		
		ValidaRegistroCEDIS = ValidaRegistroCEDIS.replace("[ID]", IDAProceso);
				
		System.out.println(ValidaRegistroCEDIS);

		SQLResult verif =  dbPos.executeQuery(ValidaRegistroCEDIS);
		
		String RETEK_CR="";
		
		boolean validRes = verif.isEmpty();

		if (!validRes) {
			RETEK_CR = verif.getData(0, "RETEK_CR");

			System.out.println("RETEK_CR = " + RETEK_CR);
			
			testCase.addQueryEvidenceCurrentStep(verif);

		}

		System.out.println(validRes);
		assertFalse("El campo RETEK_CR resulto vacio", validRes);
		
		//************************************************Paso 3********************* ***********************************************************************************************************
		
			addStep("Comprobar que existan registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE de la BD RETEK para el ID igual a ");
			
			System.out.println("Paso 3");
			
			System.out.println(GlobalVariables.DB_HOST_Rms);

			ValidaRegistroWM_EDI_MAP_SUPPLIER = ValidaRegistroWM_EDI_MAP_SUPPLIER.replace("[ID]", IDAProceso);
			
			System.out.println(ValidaRegistroWM_EDI_MAP_SUPPLIER);
			
			SQLResult ConsRegRes = dbRms.executeQuery(ValidaRegistroWM_EDI_MAP_SUPPLIER);

			ValidaDatBool = ConsRegRes.isEmpty();
			String supplier="";
			if (!ValidaDatBool) {
				supplier = ConsRegRes.getData(0, "SUPPLIER");

				System.out.println("supplier= " + supplier);

				testCase.addQueryEvidenceCurrentStep(ConsRegRes);
			}

			System.out.println(ValidaDatBool); 
			assertFalse("No hay registros en la tabla WM_EDI_MAP_SUPPLIER en la BD RMS,", ValidaDatBool);
																			
			//************************************************Paso 4********************* ***********************************************************************************************************
			
			addStep("Comprobar que existan registros en la tabla ITEM_SUPP_COUNTRY de la BD RETEK en donde SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER].  ");
			
			System.out.println("Paso 4");

			System.out.println(GlobalVariables.DB_HOST_Puser);


			ValidaRegistroITEM_SUPP_COUNTRY = ValidaRegistroITEM_SUPP_COUNTRY.replace("[ID]",supplier );
			
			System.out.println(ValidaRegistroITEM_SUPP_COUNTRY);

			SQLResult SQLResultITEM_SUPP_COUNTRY = dbRms.executeQuery(ValidaRegistroITEM_SUPP_COUNTRY);

			ValidaDatBool = SQLResultITEM_SUPP_COUNTRY.isEmpty(); // checa que el string contenga datos
			String ItemsResult="";
			if (!ValidaDatBool) {
				ItemsResult = SQLResultITEM_SUPP_COUNTRY.getData(0, "ITEM");

				System.out.println("Item a procesar= " + ItemsResult);

				testCase.addQueryEvidenceCurrentStep(SQLResultITEM_SUPP_COUNTRY); // Si no esta vacio, lo agrega a la evidencia
			}

			System.out.println(ValidaDatBool); // Si no, imprime la fechas
			assertFalse("No hay registros en la tabla ITEM_SUPP_COUNTRY ", ValidaDatBool);
																					
			//************************************************Paso 5********************* ***********************************************************************************************************
			
			addStep("Comprobar la FECHA limite para procesar los datos, en la tabla WM_EDI_INV_CONTROL de la BD RETEK.  ");
			
			System.out.println("Paso 5");

			System.out.println(GlobalVariables.DB_HOST_RMS_MEX);


			ValidaRegistroWM_EDI_INV_CONTROL = ValidaRegistroWM_EDI_INV_CONTROL.replace("[WM_EDI_MAP_SUPPLIER.SUPPLIER]",supplier );

			ValidaRegistroWM_EDI_INV_CONTROL = ValidaRegistroWM_EDI_INV_CONTROL.replace("[CEDIS.RETEK_CR]",RETEK_CR );

			ValidaRegistroWM_EDI_INV_CONTROL = ValidaRegistroWM_EDI_INV_CONTROL.replace("[ITEM_SUPP_COUNTRY.ITEM]",ItemsResult );
			
			System.out.println(ValidaRegistroWM_EDI_INV_CONTROL);

			SQLResult SQLResultEDI_INV_CONTROL = dbRms.executeQuery(ValidaRegistroWM_EDI_INV_CONTROL);

			ValidaDatBool = SQLResultEDI_INV_CONTROL.isEmpty(); // checa que el string contenga datos

			String LAST_SENT_DATE="";
			
			if (!ValidaDatBool) {
				
				LAST_SENT_DATE = SQLResultEDI_INV_CONTROL.getData(0, "LAST_SENT_DATE");

				System.out.println("LAST_SENT_DATE= " + LAST_SENT_DATE);

				testCase.addQueryEvidenceCurrentStep(SQLResultEDI_INV_CONTROL); // Si no esta vacio, lo agrega a la evidencia
			}

			System.out.println(ValidaDatBool); // Si no, imprime la fechas
			assertFalse("No hay registros en la tabla WM_EDI_INV_CONTROL", ValidaDatBool);
																
			//************************************************Paso 6********************* ***********************************************************************************************************
								
			addStep("Comprobar que existan registros en la tabla VM_ITEM_LOC_SOH, de la BD POSUSER.  ");
			
			System.out.println("Paso 6");

			System.out.println(GlobalVariables.DB_HOST_Rms);
			
			LAST_SENT_DATE=LAST_SENT_DATE.substring(0,10);
			System.out.println(LAST_SENT_DATE);
			ValidaRegistroVM_ITEM_LOC_SOH = ValidaRegistroVM_ITEM_LOC_SOH.replace("[DATE]",LAST_SENT_DATE );

			ValidaRegistroVM_ITEM_LOC_SOH = ValidaRegistroVM_ITEM_LOC_SOH.replace("[CEDIS.RETEK_CR]",RETEK_CR );

			ValidaRegistroVM_ITEM_LOC_SOH = ValidaRegistroVM_ITEM_LOC_SOH.replace("[ITEM_SUPP_COUNTRY.ITEM]",ItemsResult );
			
			System.out.println(ValidaRegistroVM_ITEM_LOC_SOH);

			SQLResult SQLResultVM_ITEM_LOC_SOH = dbRms.executeQuery(ValidaRegistroVM_ITEM_LOC_SOH);

			ValidaDatBool = SQLResultVM_ITEM_LOC_SOH.isEmpty(); // checa que el string contenga datos
			String ItemResult;
			if (!ValidaDatBool) {
				ItemResult = SQLResultVM_ITEM_LOC_SOH.getData(0, "ITEM");

				System.out.println("ItemResult= " + ItemResult);

				testCase.addQueryEvidenceCurrentStep(SQLResultVM_ITEM_LOC_SOH); // Si no esta vacio, lo agrega a la evidencia
			}

			System.out.println(ValidaDatBool); // Si no, imprime la fechas
			//assertFalse("No hay registros en la tabla VM_ITEM_LOC_SOH", ValidaDatBool);
																	

		
//*********************************************************Paso 7**************************************************************************************************
		addStep("Ejecutar el servicio RE3.Pub:run ");
		System.out.println("Paso 7: Ejecucion de servicio");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";
		

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);
		/*
		 * SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat); String run_id
		 * = is.getData(0, "RUN_ID"); String status1 = is.getData(0, "STATUS");// guarda
		 * el run id de la // ejecución
		 * 
		 * boolean valuesStatus = status1.equals(searchedStatus);// Valida si se //
		 * encuentra en // estatus R
		 * 
		 * while (valuesStatus) {
		 * 
		 * status1 = is.getData(0, "STATUS"); run_id = is.getData(0, "RUN_ID");
		 * valuesStatus = status1.equals(searchedStatus);
		 * 
		 * u.hardWait(3);
		 * 
		 * }
		 */
		
//*********************************Paso 8*********************************************************
	
		addStep("Confirmar que exista registro de la correcta ejecucion de la interfaz en la tabla  WM_LOG_RUN de la BD WMLOG para la interfaz RE3. ");
		
		System.out.println("Paso 8");
		
		String RunID = "";
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidaRegistroWM_LOG_RUN);
		
		SQLResult SQResultWM_LOG_RUN = dbLog.executeQuery(ValidaRegistroWM_LOG_RUN);

		boolean LogRequest = SQResultWM_LOG_RUN.isEmpty();

		if (!LogRequest) {
			RunID = SQResultWM_LOG_RUN.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(SQResultWM_LOG_RUN);

		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la información dentro del registro de ejecuciones.", LogRequest);
	
//********************************************Paso 9**************************************************************************************************************************

		addStep("Confirmar que existan los registros de los documentos procesados en la tabla WM_LOG_THREAD de la BD WMLOG. .");
		
		System.out.println("Paso 9");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		String THREAD_ID ="";
		
		ValidaRegistroWM_LOG_THREAD = ValidaRegistroWM_LOG_THREAD.replace("[WM_LOG_RUN.RUN_ID]",RunID );
		
		
		System.out.println(ValidaRegistroWM_LOG_THREAD);
		
		SQLResult ExecuteValidIns = dbRms.executeQuery(ValidaRegistroWM_LOG_THREAD);

		boolean ExecuteValidInsReq = ExecuteValidIns.isEmpty();

		if (!ExecuteValidInsReq) {
			THREAD_ID = ExecuteValidIns.getData(0, "THREAD_ID");
			System.out.println("THREAD_ID: "+ THREAD_ID);
			testCase.addQueryEvidenceCurrentStep(ExecuteValidIns);

		}

		System.out.println(ExecuteValidInsReq);
		assertFalse("No se muestran datos insertados", ExecuteValidInsReq);
		
//*********************************************************Paso 10 **********************************************************************************************

		addStep("Comprobar que se INSERTEN los registros en la tabla EDI_OUTBOUND_DOCS de la BD RETEK en donde DATE_SENT es igual a la fecha actual, y SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]. ");
		
		System.out.println("Paso 10");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		
		ValidaRegistroEDI_OUTBOUND_DOCS = ValidaRegistroEDI_OUTBOUND_DOCS.replace("[WM_LOG_RUN.RUN_ID]",RunID );

		ValidaRegistroEDI_OUTBOUND_DOCS = ValidaRegistroEDI_OUTBOUND_DOCS.replace("[WM_EDI_MAP_SUPPLIER.SUPPLIER]",supplier );

		
		System.out.println(ValidaRegistroEDI_OUTBOUND_DOCS);
		
		SQLResult SQLResultEDI_OUTBOUND_DOCS = dbPos.executeQuery(ValidaRegistroEDI_OUTBOUND_DOCS);

		boolean ResultadoVacio = SQLResultEDI_OUTBOUND_DOCS.isEmpty();

		if (!ResultadoVacio) {

			testCase.addQueryEvidenceCurrentStep(SQLResultEDI_OUTBOUND_DOCS);

		}

		System.out.println(ResultadoVacio);
		assertFalse("No se muestran datos insertados", ResultadoVacio);
	
		//*********************************************************Paso 11 **********************************************************************************************

				addStep("Comprobar que se INSERTEN el registro en la tabla WM_EDI_CONTROL_ENVIO en la BD POSUSER en donde SUP_ID es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]. ");
				
				System.out.println("Paso 11");
				
				System.out.println(GlobalVariables.DB_HOST_Puser);
				
				
				System.out.println(ValidaRegistroWM_EDI_CONTROL_ENVIO);
				SQLResult ExecuteUpdtFormat = dbPos.executeQuery(ValidaRegistroWM_EDI_CONTROL_ENVIO);

				boolean ExecuteValidUpdtReq = ExecuteUpdtFormat.isEmpty();

				if (!ExecuteValidUpdtReq) {

					testCase.addQueryEvidenceCurrentStep(ExecuteUpdtFormat);

				}

				System.out.println(ExecuteValidUpdtReq);
				assertFalse("No se muestran datos insertados", ExecuteValidUpdtReq);
				
			
				//*********************************************************Paso 12 **********************************************************************************************

				/*
				 * addStep("Comprobar que se recibieron los documentos en el servidor TN, verificando los archivos LOG. "
				 * ); System.out.println(GlobalVariables.DB_HOST_RMS_MEX); String
				 * ValidUpdtFormat = String.format(ValidUpdate, RunID, Doc_Name);
				 * System.out.println(ValidUpdtFormat); SQLResult ExecuteUpdtFormat =
				 * dbRms.executeQuery(ValidUpdtFormat);
				 * 
				 * boolean ExecuteValidUpdtReq = ExecuteUpdtFormat.isEmpty();
				 * 
				 * if (!ExecuteValidUpdtReq) {
				 * 
				 * testCase.addQueryEvidenceCurrentStep(ExecuteUpdtFormat);
				 * 
				 * }
				 * 
				 * System.out.println(ExecuteValidUpdtReq);
				 * assertFalse("No se muestran datos insertados", ExecuteValidUpdtReq);
				 */
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_RE3_Enviar_documentos_para_registro";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. RE3_Enviar_documentos_para_registro";
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

package interfaces.ol11_ol14_imx;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;


public class ATC_FT_OL11_OL14_IMX_001_Ejecucion_con_FlatFile_Bancomer extends BaseExecution{
	
	@Test(dataProvider = "data-provider") 
	public void ATC_FT_OL11_OL14_IMX_001_Ejecucion_con_FlatFile_Bancomer_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utiler�as
		 ********************************************************************************************************************************************/
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		/*
		/*
		 * Variables
		 ******************************************************************************************************************************************/

	
	
           //Paso 3
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'OL11_14_IMX'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S' AND ROWNUM <=1 ORDER BY START_DT DESC";
           //Paso 4 
		String validaInsercionArchivo = "SELECT * FROM WMUSER.BANK_ENVELOPE ;\r\n" + 
				"WHERE DOC_NAME = '"+data.get("DOC_NAME") +"'\r\n" + 
				"AND TRUNC (RECEIVED_DATE) = TRUNC (SYSDATE) \r\n" + 
				"AND INTERFASE = 'OL11_14'";
		//Paso 5
		String validaDetalleDoc = "SELECT * FROM WMUSER.BANK_INBOUND_DOCS \r\n" + 
				"WHERE BE_ID = (SELECT ID FROM BANK_ENVELOPE \r\n" + 
				"WHERE DOC_NAME = '"+data.get("DOC_NAME") +"'\r\n" + 
				"AND TRUNC (RECEIVED_DATE) = TRUNC (SYSDATE) \r\n" + 
				"AND INTERFASE = 'OL11_14')";
		
		//Paso 6
		String registroHeader = "SELECT statement_number, bank_account_num, statement_date, record_status_Flag, currency_code, org_id \r\n" + 
				"FROM CE.CE_STATEMENT_HEADERS_INT \r\n" + 
				"WHERE STATEMENT_NUMBER = '"+data.get("DOC_NAME") +"'";
		
		//Paso 7
		String registroLinea = "SELECT bank_account_num, statement_number, line_number, trx_code, trx_text, currency_code FROM XXFC.XXFC_STATEMENT_LINES_FILTER \r\n" + 
				"WHERE STATEMENT_NUMBER = '"+data.get("DOC_NAME") +"'";
		
//*****************************************************************************************************************************************		

		/* Pasos */
		
		//Paso 1 
		
		/*Tener la siguiente nomenclatura en el nombre del archivo a enviar:
          BBVI[CRSUP]DDMMAAHHMMSS(MXN/USD).DAT*/
	
//*****************************************************************************************************************************************
		//Paso 2
	/*	Enviar el archivo BBVI[CRSUP]DDMMAAHHMMSS(MXN/USD).DAT mediante FTP al folder /ns/OL11_OL14_IMX/Pub/run del servidor puerto 7787. */
		
//***************************************************************************************************************************************

//		Paso 3	************************	
		addStep("Validar que se inserte el detalle de la ejecucion de la interface en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.");
		
		System.out.println(ValidLog);
		
		SQLResult ValidLogResult = executeQuery(dbLog, ValidLog);
		
		
		boolean ValidaLog = ValidLogResult.isEmpty();
		
		if (!ValidaLog) {	
			
			testCase.addQueryEvidenceCurrentStep(ValidLogResult);		
			
		} 

		System.out.println(ValidaLog);

		assertFalse(ValidaLog, "No se inserto el detalle de la ejecucion de la interface en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.");
		
		
//Paso **********************************************************************************************************************************************
		addStep("Validar que se inserte la informacion del archivo enviado en la tabla BANK_ENVELOPE de WMUSER.");
		
        System.out.println(validaInsercionArchivo);
		
		SQLResult validaInsercionArchivoResult = executeQuery(dbPos,validaInsercionArchivo);
		
		
		boolean validaInsercionArchivoB = validaInsercionArchivoResult.isEmpty();
		
		if (!validaInsercionArchivoB) {	
			
			testCase.addQueryEvidenceCurrentStep(ValidLogResult);		
			
		} 

		System.out.println(validaInsercionArchivoB);
		
		assertFalse(validaInsercionArchivoB, "No se inserto la informacion del archivo enviado en la tabla BANK_ENVELOPE de WMUSER.");
		
//Paso 5******************************************************************************************************************************
		
addStep("Validar que se inserte el detalle del documento en la tabla BANK_INBOUND_DOCS de WMUSER.");
		
        System.out.println(validaInsercionArchivo);
		
		SQLResult validaDetalleDocResult = executeQuery(dbPos,validaDetalleDoc);
		
		
		boolean validaDetalleDocB = validaDetalleDocResult.isEmpty();
		
		if (!validaDetalleDocB) {	
			
			testCase.addQueryEvidenceCurrentStep(validaDetalleDocResult);		
			
		} 

		System.out.println(validaDetalleDocB);
		
		assertFalse(validaDetalleDocB, "No se inserto el detalle del documento en la tabla BANK_INBOUND_DOCS de WMUSER.");
		
//Paso 6 -***************************************************************************************************************************
		
addStep("Validar que se inserte el registro del Header del documento procesado con el nombre del archivo pero con el siguiente formato de fecha YYYYMMDD en la tabla CE_STATEMENT_HEADERS_INT de ORAFIN.");
		
        System.out.println(registroHeader);
		
		SQLResult registroHeaderResult = executeQuery(dbEbs,registroHeader);
		
		
		boolean validaregistroHeader = registroHeaderResult.isEmpty();
		
		if (!validaregistroHeader) {	
			
			testCase.addQueryEvidenceCurrentStep(registroHeaderResult);		
			
		} 

		System.out.println(validaregistroHeader);
		
		assertFalse(validaregistroHeader, "No se inserto el registro del Header del documento procesado en la tabla CE_STATEMENT_HEADERS_INT de ORAFIN.");
		
//Paso 7 ***************************************************************************************************************************
		
addStep("Validar que se inserte la informacion de las l�neas del documento en la tabla XXFC_STATEMENT_LINES_FILTER de ORAFIN.");
		
        System.out.println(registroLinea);
		
		SQLResult registroLineaResult = executeQuery(dbEbs,registroLinea);
		
		
		boolean validaregistroLinea = registroLineaResult.isEmpty();
		
		if (!validaregistroLinea) {	
			
			testCase.addQueryEvidenceCurrentStep(registroHeaderResult);		
			
		} 

		System.out.println(validaregistroLinea);
		
		assertFalse(validaregistroLinea, "No se inserto la informacion de las lineas del documento en la tabla XXFC_STATEMENT_LINES_FILTER de ORAFIN.");
		
		
	

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que cuando la interface reciba un FlatFile de Bancomer y Plaza Culiacan la procese de forma correcta.";
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


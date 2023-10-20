package interfaces.OL11_OL14_CO;


import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class OL11_OL14_CO_ProcesarFlateFileEgresoBancomer extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL11_OL14_CO_Procesar_FlatFile_de_Egreso_de_Bancomer(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_USER_EBS_COL,GlobalVariables.DB_PASSWORD_EBS_COL, GlobalVariables.DB_HOST_EBS_COL);
		
	    
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

				

		String validaEjecucion = "SELECT run_id,interface, start_dt, end_dt, status, server  FROM wmlog.wm_log_run\r\n" + 
				"WHERE interface like 'OL11_14' \r\n" + 
				"AND start_dt >= trunc(SYSDATE)  \r\n" + 
				"AND status = 'S'  \r\n" + 
				"ORDER BY start_dt  DESC";
				

		String validaRegistro = "SELECT * FROM WMUSER.BANK_ENVELOPE \r\n" + 
				"WHERE doc_name = '" + data.get(" archivo.DAT") + "'\r\n" + 
				"AND TRUNC (received_date) = TRUNC (SYSDATE) \r\n" + 
				"AND interfase = 'OL11_14_CO' \r\n" + 
				"AND status = 'E'";
		
		String validaDetalle = "SELECT * FROM WMUSER.BANK_ENVELOPE \r\n" + 
				"WHERE be_id = (\r\n" + 
				"SELECT id FROM bank_envelope \r\n" + 
				"WHERE doc_name = '" + data.get(" archivo.DAT") + "'\r\n" + 
				"AND TRUNC (received_date) = TRUNC (SYSDATE) \r\n" + 
				"AND interfase = 'OL11_14_CO') \r\n" + 
				"AND status = 'E'";
		
		String validaHeader = "SELECT statement_number, bank_account_num, statement_date, bank_name, bank_branch_name, control_begin_balance\r\n" + 
				"FROM ce_statement_headers_int \r\n" + 
				"WHERE statement_number = '" + data.get(" archivo.DAT") + "'";
				

		String validaineas = "SELECT bank_account_num, statement_number, line_number, trx_date, trx_text FROM xxfc_statement_lines_filter \r\n" + 
				"WHERE statement_number = " + data.get(" archivo.DAT") + "";
				


		
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */
		


//paso 1 ***************************************
		
		/*Tener la siguiente nomenclatura en el nombre del archivo a enviar:
BCOI[CRSUP]DDMMAAHHMMSS(MXN/USD).DAT*/
		
//Paso 2 ***************************************
		
		/*Enviar el archivo BCOI[CRSUP]DDMMAAHHMMSS(MXN/USD).DAT mediante FTP al folder /ns/OL11_OL14/Pub/run del servidor puerto 7787*/
		
//Paso 3 ***************************************
		
		addStep("Validar la ejecución de la interface en la tabla wm_log_run de WMLOG.");

		System.out.println(validaEjecucion);
		
		SQLResult validaEjecucionResult = executeQuery(dbLog, validaEjecucion); //dbPos
		
		String run_id = "";
		
		boolean ValidacionPaso1 = validaEjecucionResult.isEmpty();
		
		if (!ValidacionPaso1) {
			
			run_id = validaEjecucionResult.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + run_id);
			testCase.addQueryEvidenceCurrentStep(validaEjecucionResult);


		}

		assertFalse(ValidacionPaso1, "No se encontro el registro de la correcta ejecución de la interface en WMLOG.");

//paso 4 *********************************************

		addStep("Verificar que se ha insertado el registro del archivo en la tabla bank_envelope de WMINT, validar información general del documento en WMINT");

	
		System.out.println(validaRegistro);
		
		SQLResult validaRegistroResult = executeQuery(dbPos, validaRegistro); //dbPos
		
		
		boolean ValidacionPaso2 = validaRegistroResult.isEmpty();
		
		if (!ValidacionPaso2) {

			testCase.addQueryEvidenceCurrentStep(validaEjecucionResult);


		}

		assertFalse( ValidacionPaso2, "Deberá existir registro del archivo en la tabla bank_envelope.");

//Paso 5	******************************************

		addStep("Revisar que se insertó el detalle del documento en la tabla bank_inbound_docs de WMINT.");

		
		System.out.println(validaDetalle);
		
		SQLResult validaDetalleResult = executeQuery(dbPos, validaDetalle);
		
		boolean validacionPaso3 =validaDetalleResult.isEmpty();
		
		if (!validacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(validaDetalleResult);
		}

		assertFalse(validacionPaso3, "Deberá existir información del detalle de documento y banco procesado.");

//Paso 6	***********************************

		addStep("En la tabla ce_statement_headers_int de ORAFINCOL, validaremos que se tenga registro del header de documento"
				+ " mediante el nombre del archivo pero modificando la fecha en formato yyyymmdd dentro del mismo.");

		
		System.out.println(validaHeader);
		
		SQLResult validaHeaderResult = executeQuery(dbEbs, validaHeader);
		
		boolean validacionPaso4 = validaHeaderResult.isEmpty();
		
		if (!validacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(validaHeaderResult);
		}

		assertFalse(validacionPaso4, "El header del documento  no esta disponible en ce_statement_headers_int");
		
		//Paso 7	***********************************

				addStep("Verificar que las líneas del documento hayan sido insertadas en la tabla xxfc_statement_lines_filter de ORAFINCOL.");

				
				System.out.println(validaineas);
				
				SQLResult validaineasResult = executeQuery(dbEbs, validaineas);
				
				boolean validacionPaso5 = validaineasResult.isEmpty();
				
				if (!validacionPaso5) {

					testCase.addQueryEvidenceCurrentStep(validaineasResult);
				}

				assertFalse(validacionPaso5, "El header del documento  no esta disponible en ce_statement_headers_int");

				System.out.println(validacionPaso5);

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL11_OL14_CO_Procesar_FlatFile_de_Egreso_de_Bancomer";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Validar que la interface reciba un FlatFile de Bancomer, Plaza Bogotá y de tipo Egreso, esta la procese de forma correcta.";
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
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}


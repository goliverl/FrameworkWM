package interfaces.pr2col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;


import modelo.BaseExecution;
import om.PR2_CO;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class ATC_FT_007_PR2_CO_ValidaArticulosSalidaEntrada extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PR2_CO_ValidaArticulosSalidaEntrada_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		/**
		 * MTC-FT-010 PR2_CO Validacion de Articulos a transferir, Transferencias de Salida y Entrada a traves de la interface PR2_CO
		 */
		

		PR2_CO pr2_col = new PR2_CO(data, testCase, dbLog);

		/*
		 * Variables
		 *************************************************************************/
			
			String QueryTransaction = "SELECT ID, OPERATION, START_DT,END_DT,STATUS,CODE "
					+ "FROM POSUSER.TSF_TRANSACTION_COL "
					+ "WHERE START_DT >= trunc(sysdate) "
					+ "AND OPERATION = '%s' "
					+ "AND CODE = '000' "
					+ "AND ROWNUM = 1 "
					+ "ORDER BY START_DT DESC";
			

			// Inbound---------------------------------------------------------------------------------


			String tdcQueryPosTsfOnlineHead = "SELECT ID, PV_DOC_ID, SENDER_ID, RECEIVER_ID, SHIP_DATE, STATUS, RUN_ID "
					+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE  PV_DOC_ID = '"+ data.get("PV_DOC_ID") + "' AND SENDER_ID = '" + data.get("SENDER") + "' "
					+ "AND STATUS = 'R' " +
					" AND RECEIVED_DATE >= TRUNC(SYSDATE) "+
					" ORDER BY RECEIVED_DATE DESC";
			

			String tdcQueryPosTsfOnlineDet1 = "SELECT * FROM POSUSER.POS_TSF_ONLINE_DETL" + " where ID = '%s'"
					+ " AND PARTITION_DATE > = trunc(sysdate) "
					+ "ORDER BY PARTITION_DATE DESC";



			
			String Error ="SELECT ERROR_ID,FOLIO,ERROR_DATE,ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE  "
					+ "WHERE ERROR_DATE >= trunc(sysdate) "
					+ "AND TPE_TYPE LIKE '%PR2_CO%' "
					+ "AND ROWNUM >=5"
					+ "ORDER BY ERROR_DATE DESC";
			
		

		// -------------------------------------------------------------------------------------





		testCase.setProject_Name("Autonom�a Sistema OXXO Colombia");

		/*
		 * Pasos
		 *****************************************************************************/

		/*
		 *  Paso 1
		 **************************************************************************/
		
		addStep(" Ejecutar desde un navegador web el siguiente request para el servicio runValidSku desde el navegador para solicitar una validaci�n de art�culos. ");

		String validSKU = pr2_col.EjecutaRunValidSKU();
		System.out.println(" - validSKU: "+validSKU );
	
		testCase.addTextEvidenceCurrentStep(validSKU);
						
		
//		********************************** Paso 2 ********************************************************************* 
		addStep("Consultar la tabla TSF_TRANSACTION_COL para validar la ejecuci�n del servicio PR2_CO_validSKU el cual finaliz� con el code 000 y en estatus S exitosa, en la BD FCWM6QA.");
		String FormatTrans = String.format(QueryTransaction, "PR2_CO_validSKU");
		System.out.println(FormatTrans);
		SQLResult ValidEjec = dbPos.executeQuery(FormatTrans);
	
		boolean paso1 = ValidEjec.isEmpty();
		if (!paso1) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida la ejecuci�n del servicio PR2_CO_validSKU el cual finaliz� con el code 000 y en estatus S exitosa");
		}
		testCase.addQueryEvidenceCurrentStep(ValidEjec);
		System.out.println(paso1);
		assertFalse(paso1, "No se valida la ejecuci�n del servicio PR2_CO_validSKU");
		
			
		// ********************************************************* Paso 3 *****************************************************
		
		addStep("Ejecutar el siguiente request para el servicio runInbound desde el navegador para solicitar de transferencias de Salida (Inbound).");

		String validInbound= pr2_col.EjecutaRunInbound();
		System.out.println(" - validInbound: "+validInbound );
	
		testCase.addTextEvidenceCurrentStep(validInbound);
		
		
		
		//**************************************************** Paso 4 *************************************************************************
		addStep("Comprobar que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Inbound.");
		
			String FormatTransIn = String.format(QueryTransaction, "PR2_CO_Inbound");
			System.out.println(FormatTransIn);
			SQLResult ValidEjecIN = dbPos.executeQuery(FormatTransIn);
	
		boolean paso2 = ValidEjecIN.isEmpty();
		if (!paso2) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida la ejecuci�n del servicio PR2_CO_validSKU el cual finaliz� con el code 000 y en estatus S exitosa");
		}
		testCase.addQueryEvidenceCurrentStep(ValidEjecIN);
		System.out.println(paso2);
		assertFalse(paso2, "No se valida la ejecuci�n del servicio PR2_CO_Inbound");

			
		// ************************************** Paso 5 *********************************************
		addStep("Validar que se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD de la BD FCWM6QA con status igual a 'R'");
		
		System.out.println(tdcQueryPosTsfOnlineHead);
		SQLResult ValidInf = dbPos.executeQuery(tdcQueryPosTsfOnlineHead);
		String ID = "";
		boolean paso3 = ValidInf.isEmpty();
		if (!paso3) {
			testCase.addBoldTextEvidenceCurrentStep("se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD de la BD FCWM6QA con status igual a 'R'");
			ID = ValidInf.getData(0, "ID");
		}
		testCase.addQueryEvidenceCurrentStep(ValidInf);
		System.out.println(paso3);
		assertFalse(paso3, "No se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD ya que no se encontro registros");

		
		// ********************************************************* Paso 6 ************************************************
	
			addStep("Validar que se insert� el detalle de la informaci�n en la tabla POS_TSF_ONLINE_DETL de la BD FCWM6QA con status igual a 'R'");
			
			String FormatDTL = String.format(tdcQueryPosTsfOnlineDet1, ID);
			System.out.println(FormatDTL);
			
			SQLResult ValidDTL = dbPos.executeQuery(FormatDTL);
		
			boolean paso4 = ValidDTL.isEmpty();
			if (!paso4) {
				testCase.addBoldTextEvidenceCurrentStep("se insert� el detalle de la informaci�n en la tabla POS_TSF_ONLINE_DETL");
				ID = ValidInf.getData(0, "ID");
			}
			testCase.addQueryEvidenceCurrentStep(ValidDTL);
			System.out.println(paso4);
			assertFalse(paso4, "No se insert� el detalle de la informaci�n en la tabla POS_TSF_ONLINE_DETL");
		
//			********************** Paso 7 ******************************************************************
			addStep("Ejecutar el siguiente request para el servicio runOutbound desde el navegador para validar las de transferencias (Outbound).");

			String validOutbound= pr2_col.EjecutaRunOutbound();
			System.out.println(" - validOutbound: "+validOutbound );
		
			testCase.addTextEvidenceCurrentStep(validOutbound);

//			**********************************Paso 8 ******************************************************************
			addStep("Comprobar que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Outbound.");
					 
			String FormatTransOut = String.format(QueryTransaction, "PR2_CO_Outbound");
			System.out.println(FormatTransOut);
			SQLResult ValidEjecOut = dbPos.executeQuery(FormatTransOut);
		
			boolean paso5 = ValidEjecOut.isEmpty();
			if (!paso5) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Outbound");
			}
			testCase.addQueryEvidenceCurrentStep(ValidEjecOut);
			System.out.println(paso5);
			
			assertFalse(paso5, "No se registro la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Outbound");
			
			
//			******************************************Paso 8 *************************************************
			
			addStep("Desde un navegador web ejecutar el siguiente request para el servicio runAffect desde el navegador para marcar las transferencias recibidas.");

			String validAffect= pr2_col.EjecutaRunAffect();
			System.out.println(" - validAffect: "+validAffect );
		
			testCase.addTextEvidenceCurrentStep(validAffect);
			
			
//			********************************************* Paso 10 ********************************* 
			
			addStep(" Validar que tambi�n se muestre la informaci�n actualizada en la tablaPOS_TSF_ONLINE_HEAD");
			
			System.out.println(tdcQueryPosTsfOnlineHead);
			ValidInf = dbPos.executeQuery(tdcQueryPosTsfOnlineHead);
		
			 paso3 = ValidInf.isEmpty();
			if (!paso3) {
				testCase.addBoldTextEvidenceCurrentStep("se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD de la BD FCWM6QA con status igual a 'R'");
				ID = ValidInf.getData(0, "ID");
			}
			testCase.addQueryEvidenceCurrentStep(ValidInf);
			System.out.println(paso3);
			assertFalse(paso3, "No se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD ya que no se encontro registros");
			
//			********************************************* Paso 11**********************************************************
			addStep("Comprobar que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Affect.");
			
			String FormatTransAff = String.format(QueryTransaction, "PR2_CO_Affect");
			System.out.println(FormatTransAff);
			SQLResult ValidEjecAff = dbPos.executeQuery(FormatTransAff);
		
			boolean paso6 = ValidEjecAff.isEmpty();
			if (!paso6) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Affect");
			}
			testCase.addQueryEvidenceCurrentStep(ValidEjecAff);
			System.out.println(paso6);
			
			assertFalse(paso6, "No se registro la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_Affect");
			
			
//			***************************************** Paso 12 ***************************************************
			
			addStep("Desde un navegador web ejecutar el siguiente request para el servicio runACK desde el navegador para marcar las transferencias recibidas.");

			String validAck= pr2_col.EjecutaRunAck();
			System.out.println(" - validAck: "+validAck );
		
			testCase.addTextEvidenceCurrentStep(validAck);
			
//			**************************************** Paso 13 **********************************************
			
			 addStep("Validar que tambi�n se muestre la informaci�n actualizada en la tabla POS_TSF_ONLINE_HEAD de la BD FCWM6QA, con los �tems transferidos.  ");
				
				System.out.println(tdcQueryPosTsfOnlineHead);
				ValidInf = dbPos.executeQuery(tdcQueryPosTsfOnlineHead);
			
				 paso3 = ValidInf.isEmpty();
				if (!paso3) {
					testCase.addBoldTextEvidenceCurrentStep("se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD de la BD FCWM6QA con status igual a 'R'");
					ID = ValidInf.getData(0, "ID");
				}
				testCase.addQueryEvidenceCurrentStep(ValidInf);
				System.out.println(paso3);
				assertFalse(paso3, "No se insert� correctamente la informaci�n en la tabla POS_TSF_ONLINE_HEAD ya que no se encontro registros");
				
				
//			******************************************************************** Paso 14 **********************************************************************
				addStep("Comprobar que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_ACK.");
				
				String FormatTransAck = String.format(QueryTransaction, "PR2_CO_ACK");
				System.out.println(FormatTransAck);
				SQLResult ValidEjecAck = dbPos.executeQuery(FormatTransAck);
			
				boolean paso7 = ValidEjecAck.isEmpty();
				if (!paso7) {
					testCase.addBoldTextEvidenceCurrentStep("Se valida que se registre la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_ACK");
				}
				testCase.addQueryEvidenceCurrentStep(ValidEjecAck);
				System.out.println(paso7);
				
				assertFalse(paso7, "No se registro la transacci�n en la tabla TSF_TRANSACTION_COL de la BD FCWM6QA para la operaci�n PR2_CO_ACK");
				
//		****************************************************************** Paso 15 ***********************************************************
				
				addStep("Ejecutar la siguiente consulta en la base de datos FCWMLQA  para validar  que no se encuentren registros de error de la PR2_CO");
				 
				System.out.println(Error);
				SQLResult ValidError = dbLog.executeQuery(Error);
			
				 boolean paso9 = ValidError.isEmpty();
				if (paso9) {
					testCase.addBoldTextEvidenceCurrentStep("No se encontraron errores OK");
				
				}
				testCase.addQueryEvidenceCurrentStep(ValidInf);
				System.out.println(paso3);
				assertTrue(paso9, "Se encontraron errores en la ejecucion");	
				
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-010 PR2_CO Validaci�n de Art�culos a transferir, Transferencias de Salida y Entrada a trav�s de la interface PR2_CO";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_PR2_CO_ValidaArt�culosaTransferirSalidayEntrada_test";
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


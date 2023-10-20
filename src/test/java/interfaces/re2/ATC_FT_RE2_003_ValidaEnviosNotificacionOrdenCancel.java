package interfaces.re2;


import static org.testng.Assert.assertFalse;


import java.util.HashMap;

import org.testng.annotations.Test;


import modelo.BaseExecution;
import util.GlobalVariables;

import utils.sql.SQLResult;

public class ATC_FT_RE2_003_ValidaEnviosNotificacionOrdenCancel extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RE2_003_ValidaEnviosNotificacionOrdenCancel_test(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
        
		/**
		 * ALM
		 * Validar el envio de Notificaciones de Ordenes de compra Canceladas (runConfirm)
		 */
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String ConsCorreo = "SELECT t1.group_id,t1.group_name,t1.group_desc,t3.email,t3.user_id  "
				+ "FROM WMLOG.wm_log_group t1, WMLOG.wm_log_user_group t2, WMLOG.wm_log_user t3  "
				+ "WHERE t1.group_id = t2.group_id "
				+ "AND t2.user_id = t3.user_id "
				+ "AND t3.plaza IS NULL  "
				+ "AND t1.interface_name = 'RE2_Confirm' "
				+ " AND t1.group_name = 'LOGADMIN'";
		


		// Paso 2
	
		String OrdComp = "SELECT VENDOR_ORDER_NO,SUPPLIER,LOCATION,LOC_TYPE,WM_RUN_ID,WM_STATUS"
				+ " FROM RMS100.FEM_VMI_ORDERS_HEAD "
				+ "WHERE WM_STATUS = 'C' "
				+ "AND WM_RUN_ID = '"+data.get("Run_ID")+"'";
		
		
//	Paso 3
 
		String Update = "UPDATE RMS100.FEM_VMI_ORDERS_HEAD "
				+ "SET WM_STATUS = 'X' "
				+ "WHERE WM_STATUS = 'C' "
				+ "AND WM_RUN_ID = '"+data.get("Run_ID")+"'";
			

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************
		addStep("Consultar el correo al que se enviara la notificaci�n, en la tabla WM_LOG_USERS de la BD WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(ConsCorreo);

		SQLResult ConsRes = dbLog.executeQuery(ConsCorreo);

		boolean ValidaDatBool = ConsRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaDatBool) {

			testCase.addQueryEvidenceCurrentStep(ConsRes); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(ValidaDatBool); // Si no, imprime la fechas
		assertFalse(ValidaDatBool,"No hay registros en la tabla FEM_VMI_ORDERS_HEAD en la BD RETEK, donde WM_STATUS es igual a 'A' o 'R'");
																						

//*************************************************Paso 2***********************************************************************************************************************
			
		addStep("Consultar ordenes de compra con estatus C y run_id = "+data.get("Run_ID"));

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS

		System.out.println(OrdComp);

		SQLResult verif = dbRms.executeQuery(OrdComp);

		boolean validRes = verif.isEmpty();

		if (!validRes) {
			
			testCase.addQueryEvidenceCurrentStep(verif);

		}

		System.out.println(validRes);
		assertFalse(validRes,"No se muestran registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE");

//**********************************************************Paso 3*************************************************************************************************************		
	
//		SE REQUIERE REALIZAR UPDATE QUEDA PENDIENTE HASTA PODER REALIZAR EL PASO
//		ADEMAS EL CASO REQUIERE DE VERIFICAR LLEGADA DE UN CORREO ELECTRONICO 
//		AL  MOMENTO NO HAY FORMA DE HACER ESA VALIDACION 
		

//		addStep("Actualizar el estatus de la orden de compra a X para el env�o de la notificaci�n.");

//		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
//
//		System.out.println(Update);
//
//		Statement stmt = dbRms.createStatement();
//		
//		
//		stmt.executeUpdate(Update);

//		boolean validResComp = verifComp.isEmpty();
//
//		if (!validResComp) {
//
//			testCase.addQueryEvidenceCurrentStep(verifComp);
//
//		}
//
//		System.out.println(validResComp);
//		assertFalse("No se muestran registros en las tablas WM_EDI_FUNCTIONS y WM_EDI_SUPPLIER_FUNCTION", validResComp);
		
//*********************************************************Paso 4**************************************************************************************************
		//Validar la recepci�n de la notificaci�n de cancelaci�n v�a correo electr�nico.
		//Por el momento no hay forma de realizar esta validacion


	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar el envoo de Notificaciones de Ordenes de compra Canceladas (runConfirm)";
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

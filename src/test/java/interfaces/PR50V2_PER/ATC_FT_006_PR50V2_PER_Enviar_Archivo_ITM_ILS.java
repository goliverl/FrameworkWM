package interfaces.PR50V2_PER;

import static org.testng.Assert.assertFalse;

/*
 * I20016 Actualizacion tecnologica Webmethods 10 - Enviar archivo ITM a traves de la DS50-TN-PR50 a XPOS
 * 
 * Prueba de regresion  para comprobar la no afectacion en la 
 * funcionalidad principal de la interface FEMSA_PR50V2 para archivos ITM 
 * (Item Master) de bajada  (OUTBOUND) al ser migrada de WM9.9 a WM10.5
 * 
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_006_PR50V2_PER_Enviar_Archivo_ITM_ILS extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PR50V2_PER_Enviar_Archivo_ITM_ILS_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		

		/*
		 * Variables************************************************/
		String queryStatusChanging = "SELECT * \r\n"
				+ "FROM posuser.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'ITM'\r\n"
				+ "AND PV_CR_PLAZA = '%s'\r\n"
				+ "AND PV_CR_TIENDA = '%s'\r\n"
				+ "AND STATUS = 'P' \r\n"
				+ "ORDER BY PARTITION_DATE DESC";
		
		String path = " /u01/posuser/FEMSA_OXXO/POS/%s/%s/backup";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		/***************************Paso 1**************************/
		addStep("MTC-FT-010-2-C1 PR26 Generacion de archivo ITM de Item Master a traves de la interface FEMSA_PR26_PER");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar la DS50(PR50) desde un punto de venta XPOS .");
		
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta en la DB **OIWMQA** para validar el cambio de status de L a P");
		String formatStatusChanging = String.format(queryStatusChanging, data.get("Plaza"), data.get("Tienda"));
		SQLResult statusChanging = executeQuery(dbPos, formatStatusChanging);
		System.out.println(formatStatusChanging);
		boolean validateStatusChanging = statusChanging.isEmpty();
		if(!validateStatusChanging) {
			testCase.addQueryEvidenceCurrentStep(statusChanging);
		}
		assertFalse(validateStatusChanging, "No se obtuvieron registros");
		/***************************Paso 4**************************/
		addStep("Ingresar a la herramienta FileZilla mediante los siguientes datos:\r\n"
				+ "IP: 10.80.8.181\r\n"
				+ "User: posuser\r\n"
				+ "Pass: posuser");
		
		
		/***************************Paso 5**************************/
		addStep("Validar que  el archivo generado se encuentre en el buzón de la tienda <TIENDA>:");
		String formatPath = String.format(path, data.get("Plaza"), data.get("Tienda"));
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

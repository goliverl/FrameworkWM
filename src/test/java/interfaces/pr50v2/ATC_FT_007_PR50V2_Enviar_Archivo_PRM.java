package interfaces.pr50v2;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_007_PR50V2_Enviar_Archivo_PRM extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PR50V2_Enviar_Archivo_PRM_test(HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-008-C1 PR50_OUT Enviar archivo PRM a traves de la DS50-TN-PR50 a XPOS
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryFileStatus = "SELECT ID,POE_ID,TARGET_TYPE,PARTITION_DATE,DOC_TYPE,PV_CR_PLAZA,PV_CR_PLAZA,STATUS \r\n"
				+ "FROM POS_OUTBOUND_DOCS \r\n"
				+ "WHERE SENT_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_CR_PLAZA='%s' \r\n"
				+ "AND PV_CR_TIENDA IN ('%s') \r\n"
				+ "AND STATUS = 'L'";
		
		String filePath = "/u01/posuser/FEMSA_OXXO/POS/%s/%s/%s";
		
		String queryStatusChanging = "SELECT ID,POE_ID,TARGET_TYPE,PARTITION_DATE,DOC_TYPE,PV_CR_PLAZA,PV_CR_PLAZA,STATUS \r\n"
				+ "FROM POS_OUTBOUND_DOCS \r\n"
				+ "WHERE SENT_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_CR_PLAZA = '%s' \r\n"
				+ "AND PV_CR_TIENDA = '%s'";
		
		/***************************Paso 1**************************/
		addStep("Llamar a <MTC-FT-013-C1 PR26 Generacion de archivo PRM de catalogo de promociones a traves de la interface FEMSA_PR26>");
		
		
		/***************************Paso 2**************************/
		addStep("Validar el registro de el archivo PRM de la tienda <TIENDA> se muestre con STATUS= 'L'.");
		String formatFileStatus = String.format(queryFileStatus, data.get("Plaza"), data.get("Tienda"));
		SQLResult fileStatus = executeQuery(dbLog, formatFileStatus);
		System.out.println(formatFileStatus);
		boolean validateFileStatus = fileStatus.isEmpty();
		if(!validateFileStatus) {
			testCase.addQueryEvidenceCurrentStep(fileStatus);
		}
		assertFalse(validateFileStatus, "No se obtuvieron registros con status L");
		/***************************Paso 3**************************/
		addStep("Establecer la conexión con el servidor FTP de Buzones de tienda:\r\n"
				+ "IP: 10.182.92.13\r\n"
				+ "User: <USER>\r\n"
				+ "Pass: <PASS>");
		
		/***************************Paso 4**************************/
		addStep("Validar que  el archivo PRM se encuentre en el buzón de la tienda <TIENDA>:");
		String formatFilePath = String.format(filePath, data.get("Plaza"), data.get("Tienda"), data.get("Folder"));
		System.out.println("Buzon: " + formatFilePath);
		
		/***************************Paso 5**************************/
		addStep("Ejecutar la DS50(PR50) desde un punto de venta XPOS .");
		
		
		/***************************Paso 6**************************/
		addStep("Ejecutar la siguiente consulta en la DB **FCWM6QA** para validar el cambio de status de L a P:");
		String formatStatusChanging = String.format(queryStatusChanging, data.get("Plaza"), data.get("Tienda"));
		SQLResult statusChanging = executeQuery(dbLog, formatStatusChanging);
		System.out.println(formatStatusChanging);
		boolean validateStatusChanging = statusChanging.isEmpty();
		if(!validateStatusChanging) {
			testCase.addQueryEvidenceCurrentStep(statusChanging);
		}
		assertFalse(validateStatusChanging, "No se obtuvieron registros");
		/***************************Paso 7**************************/
		addStep("Validar que  el archivo generado se encuentre en el buzón de la tienda <TIENDA>: ");
		formatFilePath = String.format(filePath, data.get("Plaza"), data.get("Tienda"), data.get("Folder"));
		System.out.println("Buzon: " + formatFilePath);
		
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
		return "MTC-FT-008-PR50_OUT Enviar archivo PRM a traves de la DS50-TN-PR50 a XPOS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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

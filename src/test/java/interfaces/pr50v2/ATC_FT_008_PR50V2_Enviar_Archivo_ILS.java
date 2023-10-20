package interfaces.pr50v2;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
/**
 * Proyecto: Actualizacion Tecnologica Webmethods
 * CP: MTC-FT-008-PR50_OUT Enviar archivo ILS a traves de la DS50-TN-PR50 a XPOS
 * Descripcion:Prueba de regresión para comprobar la no afectacion en la funcionalidad principal 
 * de la interface FEMSA_PR50V2 para archivos ILS (Liga/Catalogo de Proveedores) de 
 * bajada (OUTBOUND) al ser migrada de WM9.9 a WM10.5
 * 
 * 
 * @reviewer Gilberto Martinez
 * @date 2023/15/02
 */
public class ATC_FT_008_PR50V2_Enviar_Archivo_ILS extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_PR50V2_Enviar_Archivo_ILS_test(HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-008-PR50_OUT Enviar archivo ILS a traves de la DS50-TN-PR50 a XPOS
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryFileStatus = "SELECT ID,POE_ID,DOC_TYPE,PV_CR_PLAZA,PV_CR_TIENDA,SENT_DATE \r\n"
				+ "FROM WMVIEW.POS_OUTBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE = 'ILS' \r\n"
				+ "AND PV_CR_PLAZA= '%s' \r\n"
				+ "AND PV_CR_TIENDA = '%s'\r\n"
				+ "AND SENT_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		String filePath = "/u01/posuser/FEMSA_OXXO/POS/%s/%s/%s";
		
		String queryStatusChanging = "SELECT * \r\n"
				+ "FROM POS_OUTBOUND_DOCS \r\n"
				+ "WHERE SENT_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND DOC_TYPE = 'ILS' \r\n"
				+ "AND DOC_NAME LIKE '%%s%'";
		
		/***************************Paso 1**************************/
		addStep("Llamar a <MTC-FT-019-C1 PR36 Generacion de archivo ILS de catalogo de proveedores a traves de la interface FEMSA_PR36>");
		
		
		/***************************Paso 2**************************/
		addStep("Validar el registro de el archivo ILS de la tienda se muestre con STATUS= 'L'.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String formatFileStatus = String.format(queryFileStatus, data.get("Plaza"), data.get("Tienda"));
		System.out.println(formatFileStatus);
		SQLResult fileStatus = executeQuery(dbPos, formatFileStatus);
		
		boolean validateFileStatus = fileStatus.isEmpty();
		if(!validateFileStatus) {
			testCase.addBoldTextEvidenceCurrentStep("Se encontro registro de el archivo ILS de la tienda <TIENDA> se muestre con STATUS= 'L'." );
		}
		System.out.println(validateFileStatus);
		testCase.addQueryEvidenceCurrentStep(fileStatus);
//		assertFalse(validateFileStatus, "No se obtuvieron registros");
		/***************************Paso 3**************************/
		addStep("Establecer la conexión con el servidor FTP de Buzones de tienda.\r\n"
				+ "IP: 10.182.92.120\r\n"
				+ "User: <USER>\r\n"
				+ "Pass: <PASS>");
		
		/***************************Paso 4**************************/
		addStep("Validar que  el archivo ILS se encuentre en el buzón de la tienda <TIENDA>:");
		String formatFilePath = String.format(filePath, data.get("Plaza"), data.get("Tienda"), data.get("Folder"));
		System.out.println("Buzon: " + formatFilePath);
		
		/***************************Paso 5**************************/
		addStep("Ejecutar la DS50(PR50) desde un punto de venta XPOS.");
		
		/***************************Paso 6**************************/
		String formatStatusChanging = String.format(queryStatusChanging, data.get("Plaza"));
		SQLResult statusChanging = executeQuery(dbLog, formatStatusChanging);
		System.out.println(formatStatusChanging);
		boolean validateStatusChanging = statusChanging.isEmpty();
		if(!validateStatusChanging) {
			testCase.addQueryEvidenceCurrentStep(statusChanging);
		}
		assertFalse(validateStatusChanging, "No se obtuvieron registros");
		
		/***************************Paso 7**************************/
		addStep("Validar que  el archivo ILS generado se encuentre en el buzón de la tienda <TIENDA>:");
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
		return "MTC-FT-008-PR50_OUT Enviar archivo ILS a traves de la DS50-TN-PR50 a XPOS";
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

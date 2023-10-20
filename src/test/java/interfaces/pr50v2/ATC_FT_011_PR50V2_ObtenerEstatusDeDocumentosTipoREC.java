package interfaces.pr50v2;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR50V2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;
import utils.sql.SQLResult;

public class ATC_FT_011_PR50V2_ObtenerEstatusDeDocumentosTipoREC extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_PR50V2_ObtenerEstatusDeDocumentosTipoREC_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		PR50V2 PR50V2Util = new PR50V2(data, testCase, dbPos);
		
		/**
		 * ALM
		 * Obtener el estatus de los documentos de tipo REC enviados en linea (runInboundQuery)
		 */

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String ValidaFolio = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,SOURCEAPP"
				+ " FROM POSUSER.POS_INBOUND_DOCS " + "WHERE SUBSTR(PV_DOC_NAME,4,10) = '" + data.get("plaza")
				+ "' || '" + data.get("tienda") + "' " + "AND PV_DOC_ID = '" + data.get("PV_DOC_ID") + "' "
				+ "AND DOC_TYPE  = 'REC' " + "AND SOURCEAPP = 'POSEL'";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface LIKE '%PR50%'" + " and  start_dt >= TRUNC(SYSDATE)" + " order by start_dt desc)"
				+ " where rownum = 1";

		String ValidEstatus = "select * from (SELECT RUN_ID, INTERFACE, start_dt, END_DT, STATUS "
				+ "FROM WMLOG.WM_LOG_RUN " + "WHERE INTERFACE LIKE '%PR50%' " + "and  start_dt >= TRUNC(SYSDATE)"
				+ "AND STATUS = 'S' ORDER BY START_DT DESC) where rownum <=1";

		String ConsEstatus = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,SOURCEAPP "
				+ "FROM posuser.POS_INBOUND_DOCS " + "where PV_DOC_ID = %s";
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//Paso 1 *************************	

		addStep("Validar que el folio a consultar exista en la tabla pos_inbound_docs.");
		System.out.println(GlobalVariables.DB_HOST_Puser);

		SQLResult FolResult = executeQuery(dbPos, ValidaFolio);
		System.out.println(ValidaFolio);

		boolean Res = FolResult.isEmpty();

		if (!Res) {
			testCase.addQueryEvidenceCurrentStep(FolResult);

		}

		System.out.println(Res);
		assertFalse(Res, "No se obtiene informaci�n de la consulta");

//paso 2 **************************

		addStep("Se envia una petici�n por HTTP al servicio: wm.tn:receive");

		String respuesta = PR50V2Util.EjecutarRunInboundREC();

		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(respuesta);
		
		
		
		String Doc_ID = runGetFolioRequestDoc.getElementsByTagName("PV_DOC_ID").item(0).getTextContent();
		String status = runGetFolioRequestDoc.getElementsByTagName("STATUS").item(0).getTextContent();

		addStep("Verificar la respuesta generada por el servicio.");

		

		boolean validationRequest = status.isEmpty();
		
		testCase.addTextEvidenceCurrentStep(respuesta);
		System.out.println("Status request: " + status);

		System.out.println("DOC_ID: " + Doc_ID);

		assertFalse(validationRequest, "No devuelve los datos correctamente");

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
		return "Obtener el estatus de los documentos de tipo REC enviados en linea (runInboundQuery)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
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

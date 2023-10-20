package interfaces.pr50v2;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR50V2;
import util.GlobalVariables;

import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;
import utils.sql.SQLResult;

public class ATC_FT_011_PR50V2_ObtenerEstatusDeDocumentosTipoTIC extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_PR50V2_ObtenerEstatusDeDocumentosTipoTIC_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/**
		 * ALM
		 * Obtener el estatus de los documentos de tipo TIC (runInboundQuery)
		 */
		
		PR50V2 PR50V2Util = new PR50V2(data, testCase, dbPos);
		String PID_ID = "";
		String plaza = "";
		String tienda = "";
		String DOC_TYP = "";
		String Sender_ID = "";
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String ValidaFolioTIC = "SELECT PID_ID,PV_NUM_CAJA,ITEM,PV_CVE_MVT,PV_FOLIO " + " FROM POSUSER.POS_TIC_DETL_F "
				+ " WHERE PV_FOLIO = '" + data.get("PV_FOLIO") + "'";

		String ObtTienPlaz = " SELECT SUBSTR(PV_DOC_NAME,4,5) as plaza, SUBSTR(PV_DOC_NAME,9,5) as tienda, SUBSTR(PV_DOC_NAME,4,10) as SENDER_ID, DOC_TYPE"
				+ " FROM posuser.POS_INBOUND_DOCS " + " WHERE ID = '%s'";

		String ValidFolPos = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME  "
				+ " FROM POSUSER.POS_INBOUND_DOCS " + " WHERE SUBSTR(PV_DOC_NAME,4,10) = '%s' || '%s' "
				+ " AND DOC_TYPE  = 'TIC' " + " AND ID = '%s' ";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//Paso 1 *************************	

		addStep("Validar que el folio exista en la tabla TIC en linea.");
		System.out.println(GlobalVariables.DB_HOST_Puser);

		SQLResult FolResult = executeQuery(dbPos, ValidaFolioTIC);
		System.out.println(ValidaFolioTIC);

		boolean Res = FolResult.isEmpty();

		if (!Res) {

			testCase.addQueryEvidenceCurrentStep(FolResult);
			PID_ID = FolResult.getData(0, "PID_ID");

			String FormObtPlaz = String.format(ObtTienPlaz, PID_ID);
			System.out.println(FormObtPlaz);
			SQLResult ObtPT = dbPos.executeQuery(FormObtPlaz);
			plaza = ObtPT.getData(0, "PLAZA");
			tienda = ObtPT.getData(0, "TIENDA");
			DOC_TYP = ObtPT.getData(0, "DOC_TYPE");
			Sender_ID = ObtPT.getData(0, "SENDER_ID");
		}

		System.out.println(Res);
		assertFalse(Res, "No se obtiene informaci�n de la consulta");

//paso 2 **************************

		addStep("Validar que el folio a consultar exista en la tabla pos_inbound_docs de POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);

		String FormValidFolPos = String.format(ValidFolPos, plaza, tienda, PID_ID);
		System.out.println(FormValidFolPos);
		SQLResult ValidaFolio = dbPos.executeQuery(FormValidFolPos);

		String PV_DOC_ID = "";
		boolean ResPos = ValidaFolio.isEmpty();

		if (!ResPos) {

			testCase.addQueryEvidenceCurrentStep(ValidaFolio);
			PV_DOC_ID = ValidaFolio.getData(0, "PV_DOC_ID");
		}

		System.out.println(ResPos);
		assertFalse(ResPos, "No se obtiene informaci�n de la consulta");

//		Paso 3**********************************************************************************************************

		addStep("Se envia una petici�n por HTTP al servicio: wm.tn:receive");

		String respuesta = PR50V2Util.EjecutarRunInbounTIC(Sender_ID, DOC_TYP, PV_DOC_ID);

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
		return "Obtener el estatus de los documentos de tipo TIC (runInboundQuery)";
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

package interfaces.pr2;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PR2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class PR2CancelacionDeFolio_ extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR2_CancelacionDeFolio_Test(HashMap<String, String> data) throws Exception {

		/*
		 * Comentarios ----11/08/20---- Monitorear: En el caso de ALM menciona que antes
		 * de ejecutar el servicio outbound se deben tener registros en estatus I, pero
		 * al finalizar el servicio que le antecede (inbound) los registros a procesar
		 * terminan en estatus R. ----
		 */

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,
				GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		PR2 pr2Util = new PR2(data, testCase, dbRms);
		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryOrafin = " SELECT ORACLE_CR, ORACLE_CR_DESC, ORACLE_CR_SUPERIOR, ESTADO, RETEK_CR  FROM  XXFC_MAESTRO_DE_CRS_V "
				+ " WHERE  ESTADO = 'A'" + " AND ORACLE_CR_SUPERIOR = '" + data.get("PLAZA") + "'"
				+ " AND ORACLE_CR = '" + data.get("TIENDA") + "'" + " AND ORACLE_CR_TYPE = 'T'";

		String tdcQueryItem = "SELECT * " + "FROM XXFC_ITEM_LOC_VIEW " + "WHERE ITEM =" + data.get("ITEM")
				+ " AND LOC = %s " + "AND STATUS IN ('A','I')";

		String tdcQueryWmLogValidSku = "SELECT * FROM( SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ "FROM wmlog.wm_log_run " + "WHERE interface = 'PR02_validSKU' " + " and start_dt >= trunc(sysdate) "
				+ "ORDER BY START_DT DESC) WHERE ROWNUM = 1";

		// Inbound---------------------------------------------------------------------------------

		String tdcQueryPosTsfOnlineHead1 = "SELECT ID, PV_DOC_ID, SENDER_ID,RECEIVER_ID,SHIP_DATE,STATUS,RUN_ID"
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE SENDER_ID ='" + data.get("SENDER") + "'"
				+ " AND RECEIVER_ID = '" + data.get("RECEIVER") + "'" + " AND EXT_REF_NO =" + data.get("EXT_REF_NO")
				+ " AND RECEIVED_DATE >= TRUNC(SYSDATE)" + " AND PV_DOC_ID = " + data.get("PV_DOC_ID");

		String tdcQueryWmLogInbound = "SELECT * FROM( SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ " FROM wmlog.wm_log_run" + " WHERE interface = 'PR02_Inbound'" + " and start_dt >= trunc(sysdate) "
				+ " ORDER BY START_DT DESC) WHERE ROWNUM = 1";

		String tdcQueryPosTsfOnlineHead2 = "SELECT ID, PV_DOC_ID, SENDER_ID, RECEIVER_ID, SHIP_DATE, STATUS, RUN_ID "
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE RUN_ID = %s" + " AND PV_DOC_ID = "
				+ data.get("PV_DOC_ID") + " AND SENDER_ID = '" + data.get("SENDER") + "'"
				+ " AND RECEIVED_DATE >= TRUNC(SYSDATE)" + " ORDER BY ID DESC";

		String tdcQueryPosTsfOnlineDet1 = "SELECT * FROM POSUSER.POS_TSF_ONLINE_DETL" + " where ID = %s"
				+ " AND PARTITION_DATE > = trunc(sysdate)";

		// Outbound----------------------------------------------------------------------------------

		/*
		 * String tdcQueryPosTsfOnlineHead3 =
		 * "SELECT ID, PV_DOC_ID, SENDER_ID,RECEIVER_ID,SHIP_DATE,STATUS,RUN_ID "+
		 * " FROM POSUSER.POS_TSF_ONLINE_HEAD" +
		 * " WHERE PV_DOC_ID = "+data.get("PV_DOC_ID")+ " AND RECEIVER_ID = '" +
		 * data.get("RECEIVER") +"'"+ " AND EXT_REF_NO = "+data.get("EXT_REF_NO") ;
		 * //" AND STATUS = 'I'";
		 * 
		 * String tdcQueryPosTsfOnlineDet2 =
		 * "SELECT * FROM POSUSER.POS_TSF_ONLINE_DETL WHERE ID = '%s'";
		 */

		String tdcQueryWmLogOutbound = "SELECT * FROM( SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ "FROM wmlog.wm_log_run " + "WHERE interface = 'PR02_Outbound' " + " and start_dt >= trunc(sysdate) "
				+ "ORDER BY START_DT DESC) WHERE ROWNUM = 1";

		// Affect----------------------------------------------------------------------------------
		String tdcQueryPosTsfOnlineHead4 = "SELECT ID, PV_DOC_ID, SENDER_ID,RECEIVER_ID,SHIP_DATE,STATUS,RUN_ID "
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE ID = %s" + " AND STATUS = 'R'";

		String tdcQueryWmLogAffect = "SELECT * FROM( SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ "FROM wmlog.wm_log_run " + "WHERE interface = 'PR02_AFFECT' " + " and start_dt >= trunc(sysdate) "
				+ "ORDER BY START_DT DESC) WHERE ROWNUM = 1";

		String tdcQueryPosTsfOnlineAffect = "SELECT * FROM POSUSER.POS_TSF_ONLINE_AFFECT " + " WHERE PV_DOC_ID ="
				+ data.get("PV_DOC_ID") + " AND RECEIVED_DATE >= TRUNC(SYSDATE)" + " AND MOV_TYPE = '"
				+ data.get("MOV_TYPE") + "'" + " AND  RUN_ID = %s";

		String tdcQueryPosTsfOnlineHead5 = "SELECT ID, PV_DOC_ID, SENDER_ID,RECEIVER_ID,SHIP_DATE,STATUS,RUN_ID "
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE ID = %s" + " AND STATUS = 'C'";

		String sku, pv_doc_id, query, receiver, inboundStatus = "R", statusWmlog = "S", statusAffect = "C", statusPuser,
				wmcode, wmcodeVal = "101";

		/*
		 * Pasos
		 *****************************************************************************/
		// Paso 1
		/*
		 * Valid Sku
		 **************************************************************************/
		addStep("Validar el CR que se utiliza para realizar la ejecución en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN.");
		System.out.println(tdcQueryOrafin);
		SQLResult orafin = dbEbs.executeQuery(tdcQueryOrafin);
		String retek_cr = orafin.getData(0, "RETEK_CR");
		System.out.println("Fecha minima= " + retek_cr);
		boolean paso1 = orafin.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(orafin);
		}
		assertFalse("No se encontro el CR de la plaza y/o tienda a utilizar", paso1);

		// Paso 2
		addStep("Comprobar que existan datos de los items que se estan procesando en la tabla  XXFC_ITEM_LOC_VIEW de RETEK.");
		query = String.format(tdcQueryItem, retek_cr);
		System.out.println("\n" + query);

		SQLResult rms = dbRms.executeQuery(query);

		boolean paso2 = rms.isEmpty();
		if (!paso2) {
			testCase.addQueryEvidenceCurrentStep(rms);
		}
		assertFalse("No existen datos de los items que se estan procesando", paso2);

		// Paso 3
		addStep("Llamar al servico PR02.Pub:runValidSku.");

		String responseRunValidSku = pr2Util.ejecutarRunValidSKU();
		System.out.print(responseRunValidSku);
		sku = RequestUtil.getSimpleDataXml(responseRunValidSku, "SKU");

		// Paso 4
		addStep("Verificar la respuesta generada por el servicio.");
		boolean skuValidation = sku.equals(data.get("ITEM"));
		testCase.addTextEvidenceCurrentStep(responseRunValidSku);
		assertTrue(skuValidation, "Los items indicado no pueden ser usados");

		// Paso 5
		addStep("Validar que la ejecución de la interface se registre correctamente en la tabla WM_LOG_RUN de la BD de WMLOG.");
		SQLResult vali = dbLog.executeQuery(tdcQueryWmLogValidSku);
		String statusSku = vali.getData(0, "STATUS");
		System.out.println(statusSku);
		boolean statusSku1 = statusSku.equals(statusWmlog);
		if (statusSku1) {
			testCase.addQueryEvidenceCurrentStep(vali);
		}

		assertTrue(statusSku1, "La ejecución de la interfaz no fue exitosa");
		/*
		 * Inbound
		 **************************************************************************/
		// Paso 6
		addStep("Comprobar que No existan registros en la tabla POS_TSF_ONLINE_HEAD en la BD POSUSER.");
		System.out.println("\n" + tdcQueryPosTsfOnlineHead1);
		SQLResult pos = dbPos.executeQuery(tdcQueryPosTsfOnlineHead1);

		boolean pos1 = pos.isEmpty();
		testCase.addQueryEvidenceCurrentStep(pos);
		assertTrue(pos1, "Existen registros en la tabla"); // cambiar a assertTrue

		// Paso 7
		addStep("Se inicia la ejecución del servicio: PR02.Pub:runInbound.");
		String responseRunInbound = pr2Util.ejecutarRunInbound();
		System.out.print(responseRunInbound);
		pv_doc_id = RequestUtil.getSimpleDataXml(responseRunInbound, "PV_DOC_ID");
		boolean inboundValidation = pv_doc_id.equals(data.get("PV_DOC_ID"));
		testCase.addTextEvidenceCurrentStep("\n" + responseRunInbound);

		assertTrue(inboundValidation);

		// Paso 8
		addStep("Validar que la interface se ejecutó correctamente en la tabla WM_LOG_RUN de la BD WMLOG.");
		System.out.println("\n" + tdcQueryWmLogInbound);
		SQLResult inbound = dbLog.executeQuery(tdcQueryWmLogInbound);
		String run_id_inbound = inbound.getData(0, "RUN_ID");
		String STATUS = inbound.getData(0, "STATUS");
		boolean inboundRun = statusWmlog.equals(STATUS);
		if (inboundRun) {
			testCase.addQueryEvidenceCurrentStep(inbound);
		}
		assertTrue(inboundRun, "La ejecución de la interfaz no fue exitosa");

		// Paso 9
		addStep("Validar que se insertó correctamente la información en la tabla: POS_TSF_ONLINE_HEAD de la BD POSUSER con estatus = R.");
		query = String.format(tdcQueryPosTsfOnlineHead2, run_id_inbound);
		SQLResult query1 = dbPos.executeQuery(query);
		System.out.println("\n" + query1);
		String id = query1.getData(0, "ID");
		String STATUS1 = query1.getData(0, "STATUS");
		boolean inboundRun1 =inboundStatus.equals(STATUS1);
		if(inboundRun1) {
			testCase.addQueryEvidenceCurrentStep(query1);
		}
		assertTrue(inboundRun1, "La información no se insertó en la tabla con estatus R");

		// Paso 10
		addStep("Validar que se insertó correctamente la información en la tabla: POS_TSF_ONLINE_DETL de la BD POSUSER.");
		query = String.format(tdcQueryPosTsfOnlineDet1, id);
		System.out.print("\n" + query);
		SQLResult query2 = dbPos.executeQuery(query);
		String id2 = query2.getData(0, "ID");
		boolean idPuserIn = id2.equals(id);
		if(idPuserIn) {
			testCase.addQueryEvidenceCurrentStep(query2);
		}
		assertTrue(idPuserIn, "La información no se insertó en la tabla POS_TSF_ONLINE_DETL");
		
		

		/*
		 * Affect
		 **************************************************************************/
		// Paso 13

		addStep("Validar que existen datos para ser procesados en la tabla POS_TSF_ONLINE_HEAD de POSUSER.");
		query = String.format(tdcQueryPosTsfOnlineHead4, id);
		SQLResult head = dbPos.executeQuery(query);
		boolean paso13 = head.isEmpty();
		testCase.addQueryEvidenceCurrentStep(head);
		assertFalse(paso13, "No existen datos para ser procesados en la tabla POS_TSF_ONLINE_HEAD");

		// Paso 14
		addStep("Ejecutar el servicio: PR02.Pub:runAffect y validar que el registro de salida contiene 101 en WM_CODE.");
		String responseRunAffect = pr2Util.ejecutarRunAffect();
		System.out.println(responseRunAffect);
		wmcode = RequestUtil.getSimpleDataXml(responseRunAffect, "WM_CODE");
		boolean affectValidation = wmcode.equals(wmcodeVal);
		testCase.addTextEvidenceCurrentStep("\n" + responseRunAffect);
		assertTrue(affectValidation, "La salida no contiene el código 101 en WM_CODE");

		// Paso 15
		addStep("Validar que la interfaz se ejecutó correctamente y sin errores en la tabla WM_LOG_RUN de la BD WMLOG.");
		System.out.println("\n" + tdcQueryWmLogAffect);
		SQLResult he = dbLog.executeQuery(tdcQueryWmLogAffect);
		String run_id_affect = he.getData(0, "RUN_ID");
		String STATUS3 = he.getData(0, "STATUS");
		boolean affectRun =statusWmlog.equals(STATUS3);
		testCase.addQueryEvidenceCurrentStep(he);
		assertTrue(affectRun, "La ejecución de la interfaz no fue exitosa");

		// Paso 16
		addStep("Comprobar que se inserte la información en la tabla POS_TSF_ONLINE_AFFECT de la BD POSUSER.");
		query = String.format(tdcQueryPosTsfOnlineAffect, run_id_affect);
		SQLResult posAffect = dbPos.executeQuery(query);
		boolean affectRun1 = posAffect.isEmpty();
		testCase.addQueryEvidenceCurrentStep(posAffect);
		assertFalse(affectRun1, "No se insertó la información en la tabla POS_TSF_ONLINE_AFFECT");

		// Paso 17
		addStep("Validar que la información se actualizó de estatus R a estatus C en la tabla POS_TSF_ONLINE_HEAD de POSUSER.");
		query = String.format(tdcQueryPosTsfOnlineHead5, id);
		System.out.println("\n" + query);
		SQLResult posrun = dbPos.executeQuery(query);
		statusPuser = posrun.getData(0, "STATUS");
		boolean statusPuserAffect = statusPuser.equals(statusAffect);
		testCase.addQueryEvidenceCurrentStep(posrun);
		assertTrue(statusPuserAffect, "La información no se actualizó a estatus C");
		
		/*
		 * Outbound
		 **************************************************************************/

		// Paso 11
		addStep("Ejecutar el servicio: PR02.Pub:runOutbound.");
		String responseRunOutbound = pr2Util.ejecutarRunOutbound();
		System.out.println(responseRunOutbound);
		receiver = RequestUtil.getSimpleDataXml(responseRunOutbound, "RECEIVER_ID");
		boolean outboundValidation = receiver.equals(data.get("RECEIVER"));
		testCase.addTextEvidenceCurrentStep("\n" + responseRunOutbound);
		assertTrue(outboundValidation);

		// Paso 12
		addStep("Validar que la interfaz se ejecutó sin errores en la tabla WM_LOG_RUN de la BD WMLOG.");
		System.out.println("\n" + tdcQueryWmLogOutbound);
		SQLResult out = dbLog.executeQuery(tdcQueryWmLogOutbound);
		String STATUSOUT = out.getData(0, "STATUS");
		boolean outboundRun = STATUSOUT.equals(statusWmlog);
		if(outboundRun) {
			testCase.addQueryEvidenceCurrentStep(out);
		}
		assertTrue(outboundRun, "La ejecución de la interfaz no fue exitosa");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return " ATC_FT_004_PR2_CancelacionDeFolio ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_004_PR2_CancelacionDeFolio";
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
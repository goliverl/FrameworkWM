package interfaces.pr2;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PR2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR2Workflow extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR2_Workflow_Test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbPuser = new SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_EBS,GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

		PR2 pr2Util = new PR2(data, testCase, dbRms);

		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryOrafin = " SELECT ORACLE_CR, ORACLE_CR_DESC, ORACLE_CR_SUPERIOR, ESTADO, RETEK_CR  FROM  XXFC_MAESTRO_DE_CRS_V "
				+ " WHERE ESTADO = 'A'" + " AND ORACLE_CR_SUPERIOR = '" + data.get("PLAZA") + "'" + " AND ORACLE_CR = '"
				+ data.get("TIENDA") + "'" + " AND ORACLE_CR_TYPE = 'T'";

		/*String tdcQueryItem = "SELECT * " + "FROM XXFC_ITEM_LOC_VIEW " + "WHERE ITEM =" + data.get("ITEM")
				+ " AND LOC = %s " + "AND STATUS IN ('A','I')";*/
		
		String tdcQueryItem = "SELECT ITEM,LOC,LOC_TYPE,UNIT_RETAIL,STATUS,STATUS_UPDATE_DATE " + "FROM RMS100.ITEM_LOC " + "WHERE ITEM =" + data.get("ITEM")
		        + " AND LOC = %s " + "AND STATUS IN ('A','I')";
		
		String tdcMapeo = "SELECT STORE,STORE_NAME,STORE_NAME10,TRANSFER_ZONE FROM STORE"
				+" WHERE STORE_NAME10 IN ('"+data.get("SENDER")+"','"+data.get("RECEIVER")+"')";

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

		String tdcQueryPosTsfOnlineDet2 = "SELECT * FROM POSUSER.POS_TSF_ONLINE_DETL WHERE ID = '%s'";

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
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE ID = %s" + " AND STATUS = 'I'";

		// Ack----------------------------------------------------------------------------------

		String tdcQueryWmLogAck = "SELECT * FROM( SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ "FROM wmlog.wm_log_run " + "WHERE interface = 'PR02_ACK' " + " and start_dt >= trunc(sysdate) "
				+ "ORDER BY START_DT DESC) WHERE ROWNUM = 1";

		String tdcQueryPosTsfOnlineHead7 = "SELECT ID,SENDER_ID,RECEIVER_ID,SHIP_DATE,RUN_ID, PHYSICAL_INV_DT, STATUS, ACK_DATE "
				+ " FROM POSUSER.POS_TSF_ONLINE_HEAD" + " WHERE SENDER_ID = '" + data.get("SENDER") + "'"
				+ " AND RECEIVER_ID = '" + data.get("RECEIVER") + "'" + " AND ID = %s"
				+ " AND ACK_DATE> = TRUNC(SYSDATE)" + " AND STATUS = 'E'";

		// Paso Consulta
		String sku, pv_doc_id, query, receiver, inboundStatusPuser = "R", statusWmlog = "S", statusAffect = "I",
				statusAck = "E", statusPuser, wmcode, wmcodeVal = "101";
		
		testCase.setProject_Name("Interface WM");

		/*
		 * Pasos
		 *****************************************************************************/

		/*
		 * Valid Sku
		 **************************************************************************/

		// Paso 1
		addStep("Validar el CR que se utiliza para realizar la ejecución en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN.");

		System.out.println(tdcQueryOrafin);	
		SQLResult resultQueryOrafin = dbEbs.executeQuery(tdcQueryOrafin);
		boolean orafin = resultQueryOrafin.isEmpty();
		String retek_cr = resultQueryOrafin.getData(0,"RETEK_CR");
		if(!resultQueryOrafin.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultQueryOrafin);
		}

		assertFalse(orafin, "No se encontro el CR de la plaza y/o tienda a utilizar");

		// Paso 2
		addStep("Comprobar que existan datos de los items que se estan procesando en la tabla  XXFC_ITEM_LOC_VIEW de RETEK.");

		query = String.format(tdcQueryItem, retek_cr);
		System.out.println("\n" + query);
		SQLResult resultItemLoc = dbRms.executeQuery(query);
		boolean retek = resultItemLoc.isEmpty();
		if(!resultItemLoc.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultItemLoc);
		}

		assertFalse(retek, "No existen datos de los items que se estan procesando");
		
		//Paso 1   
	    addStep("Validar que las tiendas esten mapeadas en el campo TRANSFER_ZONE de la tabla STORE de RETEK.");     	
	       
	       SQLResult resultStore = dbRms.executeQuery(tdcMapeo);
	       boolean mapeo = resultStore.isEmpty(); 
	       if(!resultStore.isEmpty()) {
				testCase.addQueryEvidenceCurrentStep(resultStore);
			}
	       
	   	assertFalse(mapeo,"No se encontraron datos");

		// Paso 3
		addStep("Llamar al servico PR02.Pub:runValidSku.");

		String responseRunValidSku = pr2Util.ejecutarRunValidSKU();
		System.out.print(responseRunValidSku);
		sku = RequestUtil.getSimpleDataXml(responseRunValidSku, "SKU");

		testCase.passStep();

		// Paso 4
		addStep("Verificar la respuesta generada por el servicio.");

		boolean skuValidation = sku.equals(data.get("ITEM"));
		testCase.addTextEvidenceCurrentStep(responseRunValidSku);

		assertTrue(skuValidation, "Los items indicado no pueden ser usados");

		// Paso 5
		addStep("Validar que la ejecución de la interface se registre correctamente en la tabla WM_LOG_RUN de la BD WMLOG.");
	
		SQLResult resultWmlog = dbLog.executeQuery(tdcQueryWmLogValidSku);
		String statusSku = resultWmlog.getData(0, "STATUS"); 
		boolean skuRun = statusSku.equals(statusWmlog);
		if(!resultWmlog.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultWmlog);
		}
		
		assertTrue(skuRun, "La ejecución de la interfaz no fue exitosa");

		/*
		 * Inbound
		 **************************************************************************/
		// Paso 6
		addStep("Comprobar que No existan registros en la tabla POS_TSF_ONLINE_HEAD en la BD POSUSER.");

		System.out.println("\n" + tdcQueryPosTsfOnlineHead1);
		SQLResult resultHead = dbPuser.executeQuery(tdcQueryPosTsfOnlineHead1);
		boolean emptyIn = resultHead.isEmpty();
		if(resultHead.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultHead);
		}

		assertTrue(emptyIn, "Existen registros en la tabla"); 

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
		
		SQLResult resultWmlog2 = dbLog.executeQuery(tdcQueryWmLogInbound);
		String statusInbound = resultWmlog2.getData(0, "STATUS"); 
		String run_id_inbound = resultWmlog2.getData(0, "RUN_ID"); 
		boolean inboundRun = statusWmlog.equals(statusInbound);	
		if(!resultWmlog2.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultWmlog2);
		}

		assertTrue(inboundRun, "La ejecución de la interfaz no fue exitosa");

		// Paso 9
		addStep("Validar que se insertó correctamente la información en la tabla: POS_TSF_ONLINE_HEAD de la BD POSUSER con estatus = R.");

		query = String.format(tdcQueryPosTsfOnlineHead2, run_id_inbound);
		System.out.println("\n" + query);		
		SQLResult resultHead2 = dbPuser.executeQuery(query);
		String statusHeadIn = resultHead2.getData(0, "STATUS"); 
		String id = resultHead2.getData(0, "ID"); 	
		boolean statusPuserIn = inboundStatusPuser.equals(statusHeadIn);
		if(!resultHead2.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultHead2);
		}

		assertTrue(statusPuserIn, "La información no se insertó en la tabla con estatus R");

		// Paso 10
		addStep("Validar que se insertó correctamente la información en la tabla: POS_TSF_ONLINE_DETL de la BD POSUSER.");

		query = String.format(tdcQueryPosTsfOnlineDet1, id);
		System.out.print("\n" + query);
		SQLResult resultDetl1 = dbPuser.executeQuery(query);
		String idPuser = resultDetl1.getData(0, "ID"); 
		boolean idPuserIn = idPuser.equals(id);
		if(!resultDetl1.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultDetl1);
		}
		
		assertTrue(idPuserIn, "La información no se insertó en la tabla POS_TSF_ONLINE_DETL");

		/*
		 * Affect
		 **************************************************************************/
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
		
		SQLResult resultWmlog3 = dbLog.executeQuery(tdcQueryWmLogAffect);
		String statusAff = resultWmlog3.getData(0, "STATUS"); 
		String run_id_affect = resultWmlog3.getData(0, "RUN_ID");		
		boolean affectRun = statusWmlog.equals(statusAff);
		if(!resultWmlog3.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultWmlog3);
		}

		assertTrue(affectRun, "La ejecución de la interfaz no fue exitosa");

		// Paso 16
		addStep("Comprobar que se inserte la información en la tabla POS_TSF_ONLINE_AFFECT de la BD POSUSER.");

		query = String.format(tdcQueryPosTsfOnlineAffect, run_id_affect);
		System.out.println("\n" + query);
		SQLResult resultAffect = dbPuser.executeQuery(query);
		boolean emptyAffect = resultAffect.isEmpty();
		if(!resultAffect.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultAffect);
		};

		assertFalse(emptyAffect, "No se insertó la información en la tabla POS_TSF_ONLINE_AFFECT");

		// Paso 17
		addStep("Validar que la información se actualizó de estatus R a estatus I en la tabla POS_TSF_ONLINE_HEAD de POSUSER.");
		
		query = String.format(tdcQueryPosTsfOnlineHead5, id);
		System.out.println("\n" + query);
		SQLResult resultHead3 = dbPuser.executeQuery(query);
		statusPuser = resultHead3.getData(0, "STATUS");
		boolean statusPuserAffect = statusPuser.equals(statusAffect);
		if(!resultHead3.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultHead3);
		}

		assertTrue(statusPuserAffect, "La información no se actualizó a estatus I");

		/*
		 * Outbound
		 **************************************************************************/

		addStep("Validar que existen datos para ser procesados: POS_TSF_ONLINE_DETL");
		
		query = String.format(tdcQueryPosTsfOnlineDet2, id);
		System.out.println("\n" + query);
		SQLResult resultDetl2 = dbPuser.executeQuery(query);
		boolean emptyOutDetl = resultDetl2.isEmpty();
		if(!resultDetl2.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultDetl2);
		}

		assertFalse(emptyOutDetl, "La información no se actualizo en la tabla POS_TSF_ONLINE_DETL");

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
		SQLResult resultWmlog4 = dbLog.executeQuery(tdcQueryWmLogOutbound);
		String statusOutbound = resultWmlog4.getData(0,"STATUS");
		boolean outboundRun = statusOutbound.equals(statusWmlog);
		if(!resultWmlog4.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultWmlog4);
		}

		assertTrue(outboundRun, "La ejecución de la interfaz no fue exitosa");

		/*
		 * Ack
		 **************************************************************************/

		// Paso 18
		addStep("Ejecutar el servicio: PR02.Pub:runACK.");

		String responseRunAck = pr2Util.ejecutarRunAck();
		System.out.println(responseRunAck);


		// Paso 19
		addStep("Validar que la interfaz se ejecutó correctamente y sin errores en la tabla WM_LOG_RUN de la BD WMLOG.");

		System.out.println("\n" + tdcQueryWmLogAck);
		SQLResult resultWmlog5 = dbLog.executeQuery(tdcQueryWmLogOutbound);		
		String statusWmlogAck = resultWmlog5.getData(0,"STATUS");
		boolean ackRun = statusWmlogAck.equals(statusWmlog);
		if(!resultWmlog5.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultWmlog5);
		}


		assertTrue(ackRun, "La ejecución de la interfaz no fue exitosa");

		// Paso 20
		addStep("Verificar que los campos PHYSICAL_INV_DT, STATUS Y ACK_DATE de la tabla POS_TSF_ONLINE_HEAD hayan sido actualizados y que el estatus haya cambiado a E.");

		query = String.format(tdcQueryPosTsfOnlineHead7, id);
		System.out.println("\n" + query);
		SQLResult resultHead4 = dbPuser.executeQuery(query);	
		statusPuser = resultHead4.getData(0, "STATUS");
		boolean ackPuser = statusPuser.equals(statusAck);
		if(!resultHead4.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(resultHead4);
		}
		

		assertTrue(ackPuser, "El estatus no cambio a E y los campos no fueron actualizados");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return " ATC_FT_005_PR2_Workflow ";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_005_PR2_Workflow";
	}
	

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
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

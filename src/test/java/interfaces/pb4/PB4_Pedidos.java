package interfaces.pb4;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class PB4_Pedidos extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PB4_Validar_Proceso_Datos_Plaza_Pedidos(HashMap<String, String> data) throws Exception {
		// *********************************************************Utilerías***************************************************************************************

		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbBi = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI, GlobalVariables.DB_USER_BI,GlobalVariables.DB_PASSWORD_BI);

		
		// Variables*************************************************************************************************************************************************
		
		//PASO 1
		String qryStatusI = "SELECT DISTINCT(A.PV_CR_PLAZA), A.PV_CR_TIENDA, A.PID_ID, A.STATUS " + 
				"FROM POSUSER.PID_INTERFACE A, POSUSER.POS_REC B, POSUSER.POS_REC_DETL C " + 
				"WHERE PV_CR_PLAZA = '" + data.get("plaza") + "'  AND DOC_TYPE = 'REC' " + 
				"AND STATUS = 'I' AND A.PID_ID = B.PID_ID AND A.PID_ID= C.PID_ID "; //POSUSER
		
		//PASO 2
		
		//separados
		
		String qry_XXFC_MAESTRO_DE_CRS_V1 = "SELECT oracle_cr, oracle_cr_desc, oracle_cr_superior, oracle_cr_type FROM XXFC_MAESTRO_DE_CRS_V " + 
				"WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'" + 
				"AND ORACLE_CR_TYPE = 'T'  AND ORACLE_CR = '" + data.get("tienda") + "'"; //EBS
		
		String qry_XXFC_MAESTRO_DE_CRS_V2 = "SELECT estado, retek_cr, retek_asesor, retek_asesor_nombre, retek_distrito FROM XXFC_MAESTRO_DE_CRS_V " +  //obtener retek_cr
				"WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' " + 
				"AND ORACLE_CR_TYPE = 'T'  AND ORACLE_CR = '" + data.get("tienda") + "'"; //EBS
		
		String qry_XXFC_MAESTRO_DE_CRS_V3 = "SELECT retek_plaza, retek_status, surh_cr, surh_flag, cr_flex_value_id, oracle_ef FROM XXFC_MAESTRO_DE_CRS_V " + 
				"WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' " + 
				"AND ORACLE_CR_TYPE = 'T'  AND ORACLE_CR = '" + data.get("tienda") + "'"; //EBS
		
		String qry_XXFC_MAESTRO_DE_CRS_V4 = "SELECT oracle_ef_desc, ef_flex_value_id, oracle_cia, oracle_cia_desc FROM XXFC_MAESTRO_DE_CRS_V " + 
				"WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' " + 
				"AND ORACLE_CR_TYPE = 'T'  AND ORACLE_CR = '" + data.get("tienda") + "'"; //EBS
		
		String qry_XXFC_MAESTRO_DE_CRS_V5 = "SELECT cia_flex_value_id, id_estado_financiero, id_centro_responsabilidad, id_compania, legacy_ef, legacy_cr FROM XXFC_MAESTRO_DE_CRS_V " + 
				"WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' " + 
				"AND ORACLE_CR_TYPE = 'T'  AND ORACLE_CR = '" + data.get("tienda") + "'"; //EBS
		
		
		
		
		//PASO 3 
		// consultas de error
				String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
				String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
						+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				
	    //PASO 4
		String tdcIntegrationServerFormat = "select*from(SELECT run_id, interface, start_dt, end_dt, status, server, att1 FROM WMLOG.WM_LOG_RUN " + 
				"WHERE INTERFACE = 'PB4' " + 
				"AND TRUNC(START_DT) = TRUNC(SYSDATE) " + 
				"AND STATUS = 'S' " + 
				"ORDER BY START_DT DESC) where rownum <=1 ";
		
		
		//PASO 5 
		
		String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD " + 
				"WHERE PARENT_ID = '%s'";
		
		String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD " + 
				"WHERE PARENT_ID = '%s'";
		
		//PASO 6
		String qry_pedido = "SELECT  l.id_lineasurtido, l.id_tienda , l.id_proveedor,  d.id_pedido, d.id_articulo,p.id_pedido, l.loaddate FROM BIODSMKT.LINEA_SURTIDO L, BIODSMKT.PEDIDO P,  BIODSMKT.PEDIDO_DETALLE D " + 
				"WHERE L.ID_TIENDA = P.ID_TIENDA " + 
				"AND P.ID_TIENDA = '%s' " + 
				"AND P.ID_PEDIDO = D.ID_PEDIDO " + 
				"AND TRUNC(L.LOADDATE) = TRUNC(SYSDATE) "
			  + "AND rownum <=3 ";
		
	/*	String qry_pedido2 = "SELECT l.dias_devisita, l.loaddate, p.id_pedido, p.id_tienda, p.id_proveedor, p.num_pedido FROM BIODSMKT.LINEA_SURTIDO L, BIODSMKT.PEDIDO P,  BIODSMKT.PEDIDO_DETALLE D " + 
				"WHERE L.ID_TIENDA = P.ID_TIENDA " + 
				"AND P.ID_TIENDA = '%s' " + 
				"AND P.ID_PEDIDO = D.ID_PEDIDO " + 
				"AND TRUNC(L.LOADDATE) = TRUNC(SYSDATE) ";
		
		String qry_pedido3 = "SELECT p.fecha, p.fecha_surtido, p.fecha_cierre, p.loaddate FROM BIODSMKT.LINEA_SURTIDO L, BIODSMKT.PEDIDO P,  BIODSMKT.PEDIDO_DETALLE D " + 
				"WHERE L.ID_TIENDA = P.ID_TIENDA " + 
				"AND P.ID_TIENDA = '%s'" + 
				"AND P.ID_PEDIDO = D.ID_PEDIDO" + 
				"AND TRUNC(L.LOADDATE) = TRUNC(SYSDATE) ";
		
		String qry_pedido4 = "SELECT d.id_pedido, d.id_articulo, d.inventario_teorico, d.num_sugerido FROM BIODSMKT.LINEA_SURTIDO L, BIODSMKT.PEDIDO P,  BIODSMKT.PEDIDO_DETALLE D " + 
				"WHERE L.ID_TIENDA = P.ID_TIENDA " + 
				"AND P.ID_TIENDA = '%s' " + 
				"AND P.ID_PEDIDO = D.ID_PEDIDO " + 
				"AND TRUNC(L.LOADDATE) = TRUNC(SYSDATE) ";
		
		String qry_pedido5 = "SELECT d.num_ordenado, d.num_recibido, d.loaddate FROM BIODSMKT.LINEA_SURTIDO L, BIODSMKT.PEDIDO P,  BIODSMKT.PEDIDO_DETALLE D " + 
				"WHERE L.ID_TIENDA = P.ID_TIENDA " + 
				"AND P.ID_TIENDA = '%s' " + 
				"AND P.ID_PEDIDO = D.ID_PEDIDO " + 
				"AND TRUNC(L.LOADDATE) = TRUNC(SYSDATE) ";*/
		//PASO 7
		
		String qryStatusE1= "SELECT pid_id, doc_type, pv_doc_name, if_id, target_id FROM POSUSER.PID_INTERFACE " + 
				"WHERE TARGET_ID = '%s' " + 
				"AND PV_CR_PLAZA = '" + data.get("plaza") + "'" + 
				"AND PV_CR_TIENDA = '" + data.get("tienda") + "' " + 
				"AND STATUS = 'E'";
		
		String qryStatusE2= "SELECT status, pv_cr_plaza, pv_cr_tienda, partition_date FROM POSUSER.PID_INTERFACE " + 
				"WHERE TARGET_ID = '%s' " + 
				"AND PV_CR_PLAZA = '" + data.get("plaza") + "'" + 
				"AND PV_CR_TIENDA = '" + data.get("tienda") + "'" + 
				"AND STATUS = 'E'";
		
		
		// Pasos *************************************************************************************************************************************************
		
		//PASO 1
		addStep("Que exista al menos un registro con STATUS = I y DOC_TYPE = REC en las tablas PID_INTERFACE, POS_REC, POS_REC_DETL para la Plaza en la BD de POSUSER.", 
				"Que existan registros para ser procesados.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		System.out.println(qryStatusI);

		SQLResult qryStatusI_result = dbPos.executeQuery(qryStatusI);

		boolean Valida_qryStatusI = qryStatusI_result.isEmpty();

		System.out.println(Valida_qryStatusI);

		if (!Valida_qryStatusI) {

			testCase.addQueryEvidenceCurrentStep(qryStatusI_result);

		}
		assertFalse("No hay registros por procesar ",Valida_qryStatusI);
		
		//PASO 2
		
		addStep("Que este registrada la plaza y la tienda en tabla XXFC_MAESTRO_DE_CRS_V", 
				"Que la plaza y tienda exista en el catalogo.");

		
		System.out.println(GlobalVariables.DB_HOST_EBS);

		//Parte 1

		System.out.println(qry_XXFC_MAESTRO_DE_CRS_V1);

		SQLResult qry_XXFC_MAESTRO_DE_CRS_V_result1 = dbEbs.executeQuery(qry_XXFC_MAESTRO_DE_CRS_V1);

		boolean Valida_qry_XXFC_MAESTRO_DE_CRS_VI1 = qry_XXFC_MAESTRO_DE_CRS_V_result1.isEmpty();

		System.out.println(Valida_qry_XXFC_MAESTRO_DE_CRS_VI1);

		if (!Valida_qry_XXFC_MAESTRO_DE_CRS_VI1) {

			testCase.addQueryEvidenceCurrentStep(qry_XXFC_MAESTRO_DE_CRS_V_result1);

		}
		
		//Parte 2
		System.out.println(qry_XXFC_MAESTRO_DE_CRS_V2);

		SQLResult qry_XXFC_MAESTRO_DE_CRS_V_result2 = dbEbs.executeQuery(qry_XXFC_MAESTRO_DE_CRS_V2);
		
		String retek_cr = qry_XXFC_MAESTRO_DE_CRS_V_result2.getData(0, "RETEK_CR");
		
		System.out.println("RETEK_CR = " + retek_cr);

		boolean Valida_qry_XXFC_MAESTRO_DE_CRS_VI2 = qry_XXFC_MAESTRO_DE_CRS_V_result2.isEmpty();

		System.out.println(Valida_qry_XXFC_MAESTRO_DE_CRS_VI2);

		if (!Valida_qry_XXFC_MAESTRO_DE_CRS_VI2) {

			testCase.addQueryEvidenceCurrentStep(qry_XXFC_MAESTRO_DE_CRS_V_result2);

		}
		
		//Parte 3

				System.out.println(qry_XXFC_MAESTRO_DE_CRS_V3);

				SQLResult qry_XXFC_MAESTRO_DE_CRS_V_result3 = dbEbs.executeQuery(qry_XXFC_MAESTRO_DE_CRS_V3);

				boolean Valida_qry_XXFC_MAESTRO_DE_CRS_VI3 = qry_XXFC_MAESTRO_DE_CRS_V_result3.isEmpty();

				System.out.println(Valida_qry_XXFC_MAESTRO_DE_CRS_VI3);

				if (!Valida_qry_XXFC_MAESTRO_DE_CRS_VI3) {

					testCase.addQueryEvidenceCurrentStep(qry_XXFC_MAESTRO_DE_CRS_V_result3);

				}
				
				//Parte 4

				System.out.println(qry_XXFC_MAESTRO_DE_CRS_V4);

				SQLResult qry_XXFC_MAESTRO_DE_CRS_V_result4 = dbEbs.executeQuery(qry_XXFC_MAESTRO_DE_CRS_V4);

				boolean Valida_qry_XXFC_MAESTRO_DE_CRS_VI4 = qry_XXFC_MAESTRO_DE_CRS_V_result4.isEmpty();

				System.out.println(Valida_qry_XXFC_MAESTRO_DE_CRS_VI4);

				if (!Valida_qry_XXFC_MAESTRO_DE_CRS_VI4) {

					testCase.addQueryEvidenceCurrentStep(qry_XXFC_MAESTRO_DE_CRS_V_result4);

				}
				
				//Parte 5

				System.out.println(qry_XXFC_MAESTRO_DE_CRS_V5);

				SQLResult qry_XXFC_MAESTRO_DE_CRS_V_result5 = dbEbs.executeQuery(qry_XXFC_MAESTRO_DE_CRS_V5);

				boolean Valida_qry_XXFC_MAESTRO_DE_CRS_VI5 = qry_XXFC_MAESTRO_DE_CRS_V_result5.isEmpty();

				System.out.println(Valida_qry_XXFC_MAESTRO_DE_CRS_VI5);

				if (!Valida_qry_XXFC_MAESTRO_DE_CRS_VI5) {

					testCase.addQueryEvidenceCurrentStep(qry_XXFC_MAESTRO_DE_CRS_V_result5);

				}
		
		
		assertFalse("No existe la plaza y/o tienda en el catalogo ",Valida_qry_XXFC_MAESTRO_DE_CRS_VI5);
		
		//PASO 3
		
		addStep("Se invoca el proceso PB4.Pub:run mediante la ejecucion del JOB runPB4", 
				"Que se ejecute correctamente la interfaz.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println(successRun);
													// estatus S

		if (!successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);

			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}
		}
		
		//PASO 4
		addStep("Se valida el STATUS E en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE='PB4'" , 
				"Que exista registro de la correcta ejecución de la interface PB4 ");

		boolean validateStatus = status.equals(status1);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);

		boolean log1 = log.isEmpty();

		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);

		}

		System.out.println(log1);
		
		//PASO 5
		addStep("Comprobar que existan threads de los documentos procesados en WM_LOG_THREAD en WMLOG", 
				" Existen registros de los threads procesados.");

		String consulta1 = String.format(qry_threads1, run_id);
		System.out.println("CONSULTA THREAD " + consulta1);
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		assertFalse("No se generaron threads en la tabla", threads1);
		
		//PASO 6
		addStep("Comprobar que se inserten los datos en las tablas  LINEA_SURTIDO, PEDIDO y PEDIDO_DETALLE  de la BD BI."
				, "Que se compruebe que los datos se insertaron o actualizaron correctamente ");
		
		//recibe el retek_cr
		
		//Parte 1
		
		
		String qry_pedido_format = String.format(qry_pedido, retek_cr);
		
		System.out.println(qry_pedido_format);


		SQLResult qry_pedido_result = dbBi.executeQuery(qry_pedido_format);

		boolean Valida_qry_pedido = qry_pedido_result.isEmpty();

		System.out.println(Valida_qry_pedido);

		if (!Valida_qry_pedido) {

			testCase.addQueryEvidenceCurrentStep(qry_pedido_result);

		}
	/*	//Parte 2
				System.out.println(qry_pedido2);
				
				String qry_pedido_format2 = String.format(qry_pedido2, retek_cr);


				SQLResult qry_pedido_resul2 = dbBi.executeQuery(qry_pedido_format2);
				
				
				boolean Valida_qry_pedido2 = qry_pedido_resul2.isEmpty();

				System.out.println(Valida_qry_pedido2);

				if (!Valida_qry_pedido2) {

					testCase.addQueryEvidenceCurrentStep(qry_pedido_resul2);

				}
				
		//Parte 3
				
				System.out.println(qry_pedido3);
				
				String qry_pedido_format3 = String.format(qry_pedido3, retek_cr);


				SQLResult qry_pedido_resul3 = dbBi.executeQuery(qry_pedido_format3);
				
				
				boolean Valida_qry_pedido3 = qry_pedido_resul2.isEmpty();

				System.out.println(Valida_qry_pedido3);

				if (!Valida_qry_pedido3) {

					testCase.addQueryEvidenceCurrentStep(qry_pedido_resul3);

				}	
				
          //Parte 4
				
				System.out.println(qry_pedido4);
				
				String qry_pedido_format4 = String.format(qry_pedido4, retek_cr);


				SQLResult qry_pedido_resul4 = dbBi.executeQuery(qry_pedido_format4);
				
				
				boolean Valida_qry_pedido4 = qry_pedido_resul4.isEmpty();

				System.out.println(Valida_qry_pedido4);

				if (!Valida_qry_pedido4) {

					testCase.addQueryEvidenceCurrentStep(qry_pedido_resul4);

				}	
				
            //Parte 5
				
				System.out.println(qry_pedido5);
				
				String qry_pedido_format5 = String.format(qry_pedido5, retek_cr);


				SQLResult qry_pedido_resul5 = dbBi.executeQuery(qry_pedido_format5);
				
				
				boolean Valida_qry_pedido5 = qry_pedido_resul5.isEmpty();

				System.out.println(Valida_qry_pedido5);

				if (!Valida_qry_pedido5) {

					testCase.addQueryEvidenceCurrentStep(qry_pedido_resul5);

				}	*/
					
		
		assertFalse("No se   insertaron o actualizaron correctamente los datos ",Valida_qry_pedido);
		
		//PASO 7
		
         addStep("Se valida el STATUS = E en la tabla PID_INTERFACE para la plaza y la tienda en la BD POSUSER.\r\n", 
         		"Que los campos de Status y Target_Id fueron actualizados. ");
		
         
         //Parte 1
		System.out.println(qryStatusE1);
		
		String qryStatusE_format1= String.format(qryStatusE1, run_id);

		SQLResult qryStatusE_result1 = dbPos.executeQuery(qryStatusE_format1);

		boolean Valida_qryStatusE1 = qryStatusE_result1.isEmpty();

		System.out.println(Valida_qryStatusE1);

		if (!Valida_qryStatusE1) {

			testCase.addQueryEvidenceCurrentStep(qryStatusE_result1);

		}
		
		 //Parte 2
		System.out.println(qryStatusE2);
		
		String qryStatusE_format2= String.format(qryStatusE2, run_id);

		SQLResult qryStatusE_result2 = dbPos.executeQuery(qryStatusE_format2);

		boolean Valida_qryStatusE2 = qryStatusE_result2.isEmpty();

		System.out.println(Valida_qryStatusE2);

		if (!Valida_qryStatusE2) {

			testCase.addQueryEvidenceCurrentStep(qryStatusE_result2);

		}
		
		assertFalse("No hay registros por procesar ",Valida_qryStatusE2);
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar que se procesan los datos para la plaza (PEDIDOS)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PB4_Validar_Proceso_Datos_Plaza_Pedidos";
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
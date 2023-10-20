package interfaces.pr36_col;


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

public class PR36Colombia extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR36_PR36Colombia(HashMap<String, String> data) throws Exception {
	
/* Utilerías *********************************************************************/		

//		SqlUtil dbPuser = new SqlUtil(GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser, GlobalVariables.DB_HOST_Puser);
//		SqlUtil dbRms = new SqlUtil(GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL, GlobalVariables.DB_HOST_RMS_COL);		
//		SqlUtil dbLog = new SqlUtil(GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA, GlobalVariables.DB_HOST_FCWMLQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		

	
		String tdcAvailablePlaza = "select LOCATION, SUBSTR(store_name10,1,5) PLAZA from"
			      +" ( "
			              +" SELECT DISTINCT"
			              +" TO_NUMBER(LOCATION) LOCATION"
			              +" FROM  (   SELECT DISTINCT LOCATION"
			              +" FROM repl_item_loc ril, item_loc_traits ilt, sups s"
			              +" WHERE source_wh IS NULL"
			              +" AND ilt.item (+) = ril.item"
			              +" AND ilt.loc (+) = ril.LOCATION"
			              +" AND ril.primary_repl_supplier = s.supplier"
			              +" AND ((ril.last_update_datetime > TO_DATE (" + "'"+data.get("StartDate")+"'"
			              + ", 'DD/MM/YYYY HH:MI:SS AM') AND ril.last_update_datetime <= TO_DATE ("+ "'"+data.get("EndDate")+"'"
			              + ", 'DD/MM/YYYY HH:MI:SS AM'))"
			                      +" OR (ilt.last_update_datetime > TO_DATE (" + "'"+data.get("StartDate")+"'"
			              + ", 'DD/MM/YYYY HH:MI:SS AM') AND ilt.last_update_datetime <= TO_DATE (" + "'"+data.get("EndDate")+"'"
			              + ", 'DD/MM/YYYY HH:MI:SS AM')))"
			                  +" AND ril.loc_type='S'"
			                  +" UNION ALL "
			                  +" SELECT DISTINCT LOCATION"
			                  +" FROM repl_item_loc ril, item_loc_traits ilt, wh"
			                  +" WHERE ilt.item (+) = ril.item"
			                  +" AND ilt.loc (+) = ril.LOCATION"
			                  +" AND wh = source_wh"
			                  +" AND primary_repl_supplier IS NULL"
			                  +" AND ((ril.last_update_datetime > TO_DATE (" + "'"+data.get("StartDate")+"'"
			               + ", 'DD/MM/YYYY HH:MI:SS AM') AND ril.last_update_datetime <= TO_DATE (" + "'"+data.get("EndDate")+"'"
			               + ", 'DD/MM/YYYY HH:MI:SS AM'))"
			                      +" OR (ilt.last_update_datetime > TO_DATE (" + "'"+data.get("StartDate")+"'"
			               + ", 'DD/MM/YYYY HH:MI:SS AM') AND ilt.last_update_datetime <= TO_DATE (" + "'"+data.get("EndDate")+"'"
			               + ", 'DD/MM/YYYY HH:MI:SS AM')))"
			          +" AND ril.loc_type='S'"
			                     +" ) "
			        +" ) a, store b"
			  +" where a.LOCATION=b.STORE"
			  +" order by PLAZA";
		
			//query2
			String lnPendientes = "SELECT LPAD (item, 16, ' ') item, primary_repl_supplier,"
				       +" loc_type, CLASS, subclass,"
				       +" sup_name," 
				         +" TO_DATE("+ "'" +data.get("StartDate")+"'"+ ", 'DD/MM/YYYY HH:MI:SS AM')" 			       
				+" FROM (SELECT DISTINCT ril.item, primary_repl_supplier, pct_tolerance,"
				                +" 'S' loc_type, dept, CLASS, subclass,"
				                +" store_reorderable_ind, sup_name,"
				               +" SUBSTR(st.description,1,INSTR(st.description,' ')-1) id_carrier,"
				               +" SUBSTR(st.description,INSTR(st.description,' ')+1) alias_carrier,"
				               +" pec.tiempo_entrega, pec.periodo_revision,"
				               +" pec.venta_unidades, pec.stock_pr,"
				               +" pec.fem_stock_prom, pec.fem_start, pec.fem_end, vp.venta_perdida,"
				               +" ( SELECT VPN FROM ITEM_SUPPLIER itms  WHERE itms.item = ril.item AND itms.supplier = primary_repl_supplier) as VPN,"                
				                +" s.fem_rfc_code,"
				                +" ril.dispatch_unit,"
				                +" ril.unit_cost,"
				                +" ril.total_gross_cost,"
				                +" ril.freight"                
				           +" FROM repl_item_loc ril,"
				               +" item_loc_traits ilt,"
				                +" sups s,"
				                +" sup_traits st,"
				                +" sup_traits_matrix stm,"
				                +" xxfc_parametros_envio_cent pec,"
				                +" xxfc_venta_perdida_bi vp"
				          +" WHERE source_wh IS NULL"
				          +" AND ilt.item(+) = ril.item"
				          +" AND ilt.loc(+) = ril.LOCATION"	           
				           +" AND ril.primary_repl_supplier = s.supplier"
				           +" AND ril.primary_repl_supplier = stm.supplier(+)"
				           +" AND stm.sup_trait = st.sup_trait(+)"
				           +" AND vp.item(+) = ril.item"
				           +" AND vp.location(+) = ril.location"
				           +" AND vp.supplier(+) = ril.primary_repl_supplier"
				            +" AND ( ril.last_update_datetime > TO_DATE ("+"'"+data.get("StartDate")+"'" + ", 'DD/MM/YYYY HH:MI:SS AM')"                 
				                 +" OR ilt.last_update_datetime > TO_DATE (" + "'" +data.get("StartDate")+ "'" + ", 'DD/MM/YYYY HH:MI:SS AM')"	                 
				                 +" OR pec.last_update_datetime > TO_DATE ("+ "'"+data.get("StartDate")+"'" + ", 'DD/MM/YYYY HH:MI:SS AM')"
				                +" ) "
				            +" AND ril.primary_repl_supplier = pec.supplier(+)"
				            +" AND pec.LOCATION(+) = ril.LOCATION"
				            +" AND pec.item(+) = ril.item"
				    +" UNION ALL"
				    +" SELECT DISTINCT ril.item, source_wh primary_repl_supplier,"
				                +" pct_tolerance, 'W' loc_type, dept, CLASS, subclass,"
				                + " store_reorderable_ind, wh_name sup_name,"
				                +" SUBSTR (st.description, 1, 2) id_carrier,"
				                +" SUBSTR (st.description, 4) alias_carrier,"
				                +" pec.tiempo_entrega, pec.periodo_revision,"
				                +" pec.venta_unidades, pec.stock_pr,"
				                +" pec.fem_stock_prom, pec.fem_start, pec.fem_end, vp.venta_perdida,"        
				                +"'0' as VPN,"
				                +" '0' as fem_rfc_code,"
				                +" ril.dispatch_unit,"
				                +" ril.unit_cost,"
				                +" ril.total_gross_cost,"
				                + " ril.freight"
				            +" FROM repl_item_loc ril,"
				             +" item_loc_traits ilt,"
				              +"  wh,"
				               +" sup_traits st,"
				                +" sup_traits_matrix stm,"
				                +" xxfc_parametros_envio_cent pec,"
				                +" xxfc_venta_perdida_bi vp,"
				                +" sups s"
				            +" WHERE ilt.item(+) = ril.item"
				             +" AND ilt.loc(+) = ril.LOCATION"
				                +" AND wh = source_wh"    
				                +" AND primary_repl_supplier IS NULL"		               
				                +" AND ril.source_wh = stm.supplier(+)"
				                +" AND stm.sup_trait = st.sup_trait(+)"
				                +" AND ril.primary_repl_supplier = pec.supplier(+)"
				                +" AND pec.item(+) = ril.item"
				                +" AND pec.LOCATION(+) = ril.LOCATION"
				                +" AND vp.item(+) = ril.item"
				                +" AND vp.location(+) = ril.location"
				                +" AND vp.supplier(+) = ril.primary_repl_supplier"
				                +" AND ( ril.last_update_datetime > TO_DATE ("+"'"+ data.get("StartDate")+"'" + ", 'DD/MM/YYYY HH:MI:SS AM')"
				                    +" OR ilt.last_update_datetime > TO_DATE ("+"'"+data.get("StartDate")+"'" + ", 'DD/MM/YYYY HH:MI:SS AM')"
				                  +" OR pec.last_update_datetime > TO_DATE ("+"'"+data.get("StartDate")+"'"+ ", 'DD/MM/YYYY HH:MI:SS AM')"
				                +" ) "
				+" )";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR36_CL'" 	
				 +" and  start_dt > To_Date ('%s', 'DD-MM-YYYY hh24:mi' )"
			     +" order by start_dt desc)"
				+ " where rownum = 1";	

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR36_CL' "
				//	+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " // FCWMLQA 
				+ " ORDER BY start_dt DESC";


		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM wm_log_thread "
				+ " WHERE parent_id = %s" ;
				//	+ " AND status = 'S' "
//				+ " AND att1 =" + data.get("plaza")
//				+ " AND att2 ="+ data.get("tienda"); // FCWMLQA
		
		String tdcQueryErrorId ="SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION  "
				+ " FROM WM_LOG_ERROR "
				+ " where RUN_ID='%s'";
		
		String tdcOutbound = "select  ID,doc_name,doc_type,sent_date,pv_cr_plaza,pv_cr_tienda,status"
				+ " from POSUSER.POS_OUTBOUND_DOCS"
				+ " where DOC_TYPE='ILS' "
				+ " and STATUS='L'"; //RMP
//				+ " AND pv_cr_plaza = '10MON'"
//				+ " AND pv_cr_tienda='50EDI'";
		
		
		String status = "S";
		
	
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String con ="http://"+user+":"+ps+"@"+server;
		String searchedStatus = "R";
		String run_id ;	
		
/**
* 
* **********************************Pasos del caso de Prueba *****************************************
* 
* 		
*/		

// 							Paso 1 ***********************************************************************************************
		
		
addStep("Obtener las plazas que serán procesadas (adpGetAvailableStores) con la siguiente consulta:");	

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);

		System.out.println(tdcAvailablePlaza);

//		SQLUtil.executeQuery(testCase, dbRms, tdcAvailablePlaza);
		
        SQLResult AvaiblePlaza = executeQuery(dbRms, tdcAvailablePlaza);


		boolean av = AvaiblePlaza.isEmpty();

		System.out.println(av);

		

assertFalse(av, "ERROR ,No se obtiene ninguna plaza");
		

//							Paso 2 ***********************************************************************************************

addStep("Obtener las lineas pendientes a procesar según la plaza consultada con la siguiente consulta:");



	System.out.println(GlobalVariables.DB_HOST_RMS_COL);

	System.out.println(lnPendientes);

//	SQLUtil.executeQuery(testCase, dbRms, lnPendientes);
	
    SQLResult ExeInpendientes = executeQuery(dbRms, lnPendientes);


	boolean in = ExeInpendientes.isEmpty();

	System.out.println(in);

	
	assertFalse(in, "ERROR ,No se obtiene ninguna Linea pendiente a procesar");



//							Paso 3 ***********************************************************************************************
/*addStep("Ejecucion de la interfase PR36");

String contra =   "http://"+user+":"+ps+"@"+server+":5555";
System.out.println(contra);
u.get(contra);

String dateExecution = pok.runIntefaceWM(data.get("interfase"),data.get("servicio"), null);

String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer,dateExecution);
String [] is =SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcIntegrationServerFormat, "STATUS","RUN_ID");
run_id = is[1];//guarda el run id de la ejecución
 
boolean valuesStatus = is[0].equals(searchedStatus);//Valida si se encuentra en estatus R
while (valuesStatus) {
 is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcIntegrationServerFormat, "STATUS","RUN_ID");
 valuesStatus = is[0].equals(searchedStatus);
 
 u.hardWait(2);
 
}
boolean successRun = is[0].equals(status);//Valida si se encuentra en estatus S
    if(!successRun){
   
   String error = String.format(tdcQueryErrorId, run_id);
   
   boolean emptyError = SQLUtil.isEmptyQuery(testCase, dbLog, error);
   
   if(!emptyError){  
   
    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
   
    testCase.addQueryEvidenceCurrentStep(dbLog, error);
   
   }
}


	

//			Paso 6	************************

addStep("Comprobar que se registra la ejecucion en WMLOG");



		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);

		String  fcwS  = SQLUtil.getColumn(testCase, dbLog,queryStatusLog, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);
		
		
		
		
		
		
assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");




// 		Paso 7	************************

addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(tdcQueryStatusThread);
		String regPlazaTienda = SQLUtil.getColumn(testCase, dbLog, queryStatusThread, "STATUS");
		boolean statusThread = status.equals(regPlazaTienda);
		System.out.println(statusThread);
				if(!statusThread){

						String error = String.format(tdcQueryErrorId, run_id);

						boolean emptyError = SQLUtil.isEmptyQuery(testCase, dbLog, error);

						if(!emptyError){  

							testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

							testCase.addQueryEvidenceCurrentStep(dbLog, error);

										}
									}
assertTrue(statusThread,"El registro de ejecución de la plaza y tienda no fue exitoso");






//							Paso 7 ***********************************************************************************************	
addStep("Ejecutar el siguiente query para validar que haya viajado el XML del artículo a la base de datos:");	

/**
 * agregar la conecion a esta consulta 
 */

	/* System.out.println(GlobalVariables.DB_HOST_PosUserChile);
	System.out.println(tdcOutbound);

	SQLUtil.executeQuery(testCase, dbPuser, tdcOutbound);

	boolean ou = tdcOutbound.isEmpty();

	System.out.println(ou);

	assertFalse(ou);
	
	assertFalse(ou, "Error,no se muestra info ");*/

	
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR36_PR36Colombia";
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


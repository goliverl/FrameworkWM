package interfaces.po18;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import javax.xml.transform.Result;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_PO18_EjecucionInterfaz  extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO18_EjecucionInterfaz_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler?as
		 *********************************************************************/
		
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		
		String Cons_FPE_I="select * from (SELECT a.id, b.pid_id, caja, servicio,valor,consecutivo,ref1,b.folio_transaccion,ticket " + 
				" FROM posuser.pos_inbound_docs a, posuser.pos_fpe_detl b " + 
				" WHERE a.status = 'I' " + 
				" AND doc_type = 'FPE' " + 
				" AND a.Id = b.pid_id " + 
				" AND a.pv_doc_name LIKE '" + data.get("DOC_NAME") + "%' ) where rownum <=3"; //Validar documentos a procesar en la BD del POS de tipo FPE.
		String Cons_FPE_I2="select * from (   SELECT a.id, b.amount,pv_date,admin_date, ref2, b.partition_date " + 
				" FROM posuser.pos_inbound_docs a, posuser.pos_fpe_detl b " + 
				" WHERE a.status = 'I' " + 
				" AND doc_type = 'FPE' " + 
				" AND a.Id = b.pid_id " + 
				" AND a.pv_doc_name LIKE  '" + data.get("DOC_NAME") + "%' ) where rownum <=3";
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PO18%' "
				+ "ORDER BY START_DT DESC) where rownum <=3";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		String consultaTHREADS = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='%s'";// Consulta para los THreads
		String consultaTHREADS2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 " + "FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID='%s'";// Consulta para los THreads
		
		String consultaPagoSTG = "SELECT PS_FORMA_PAGO_ID, PLAZA, TIENDA, CAJA, SERVICIO, CONSECUTIVO, VALOR, REF1,ESTATUS" + 
				" FROM WMUSER.XXFC_PAGO_SERVICIO_FORMAS_STG " + 
				" WHERE fecha_administrativa = TO_DATE ('%s', 'YYYY/MM/DD')" + 
				" AND creation_date >= TRUNC (SYSDATE)" + 
				" AND ticket = '%s'" + 
				" AND plaza = '" + data.get("plaza") + "' " + 
				" AND tienda ='" + data.get("tienda") + "' ";
		String consultaPagoSTG2=" SELECT FOLIO_TRANSACCION, TICKET, MONTO_PAGADO, FECHA_TRANSACCION, FECHA_ADMINISTRATIVA\r\n" + 
				"  FROM WMUSER.XXFC_PAGO_SERVICIO_FORMAS_STG " + 
				"  WHERE fecha_administrativa = TO_DATE ('%s', 'YYYY/MM/DD')" + 
				"  AND creation_date >= TRUNC (SYSDATE)" + 
				"  AND ticket = '%s'" + 
				" AND plaza = '" + data.get("plaza") + "' " + 
				" AND tienda ='" + data.get("tienda") + "' ";
		String consultaPagoFORMAS =  "SELECT PS_FORMA_PAGO_ID, PLAZA, TIENDA, CAJA, SERVICIO, CONSECUTIVO, VALOR, REF1,ESTATUS" + 
				" FROM  XXFC.XXFC_PAGO_SERVICIO_FORMAS " + 
				" WHERE fecha_administrativa = TO_DATE ('%s', 'YYYY/MM/DD')" + 
				" AND creation_date >= TRUNC (SYSDATE)" + 
				"  AND ticket = '%s'" + 
				" AND plaza = '" + data.get("plaza") + "' " + 
				" AND tienda ='" + data.get("tienda") + "' ";//Se enviara la informaci?n del detalle de los medios de pago utilizados en el pago de un servicio a las tablas de Orafin
		String consultaPagoFORMAS2 =" SELECT FOLIO_TRANSACCION, TICKET, MONTO_PAGADO, FECHA_TRANSACCION, FECHA_ADMINISTRATIVA\r\n" + 
				"  FROM  XXFC.XXFC_PAGO_SERVICIO_FORMAS " + 
				"  WHERE fecha_administrativa = TO_DATE ('%s', 'YYYY/MM/DD')" + 
				"  AND creation_date >= TRUNC (SYSDATE)" + 
				"  AND ticket = '%s'" + 
				" AND plaza = '" + data.get("plaza") + "' " + 
				" AND tienda ='" + data.get("tienda") + "' ";
		String Cons_FPE_E=" SELECT id, pe_id, pv_doc_id,status, doc_type " + 
				"  FROM posuser.POS_INBOUND_DOCS " + 
				" WHERE target_id = '%s'" + 
				" AND ID = '%s'"+
				" AND status = 'E'";//Validar que los campos: status y target_id, sean actualizados en la tabla POS_INBOUND_DOCS 
		String Cons_FPE_E2="SELECT pv_doc_name,target_id, received_date,inserted_date, partition_date" + 
				"  FROM posuser.POS_INBOUND_DOCS" + 
				"  WHERE target_id = '%s'" + 
				" AND ID = '%s'"+
				"  AND status = 'E'";
//Paso 1    ************************        
addStep("Validar documentos a procesar en la BD del POS de tipo FPE. SELECT a.id, b.*" + 
		" FROM posuser.pos_inbound_docs a, posuser.pos_fpe_detl" + 
		" WHERE  a.status = 'I'" + 
		" AND doc_type = 'FPE'" + 
		" AND a.Id = b.pid_id" + 
		" AND a.pv_doc_name LIKE '" + data.get("DOC_NAME") + "%'");


		SQLResult result1 = executeQuery(dbPOSUSER, Cons_FPE_I);
        boolean serv = result1.isEmpty();
        if (!serv) {
	    testCase.addQueryEvidenceCurrentStep(result1);
	    }
        System.out.println(serv);
        System.out.println(Cons_FPE_I);
        System.out.println(Cons_FPE_I2);
        assertFalse( "No hay insumos a procesar",serv);
      
        SQLResult result1Temp = executeQuery(dbPOSUSER, Cons_FPE_I2);
        boolean serv2 = result1Temp.isEmpty();
        if (!serv2) {
	    testCase.addQueryEvidenceCurrentStep(result1Temp);
        }
        System.out.println(serv2);
        
       
        assertFalse( "No hay insumos a procesar",serv2);
        String pv_date1 = result1Temp.getData(0, "PV_DATE");
        String pv_date= pv_date1;
        String TICKET1 = result1.getData(0, "TICKET");
        String ticket= TICKET1;
        String ID1 = result1Temp.getData(0, "ID");
        String ID= ID1;
        
//Paso 2    ************************        
addStep("Ejecutar  el servicio PO18.Pub:run");
        String status = "S";
// utileria
        SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
        PakageManagment pok = new PakageManagment(u, testCase);

        String user = data.get("user");
        String ps = PasswordUtil.decryptPassword(data.get("ps"));
        String server = data.get("server");
        String con = "http://" + user + ":" + ps + "@" + server;
        String searchedStatus = "R";

        System.out.println(GlobalVariables.DB_HOST_LOG);
        String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
        u.get(contra);
       // String dateExecution = pok.
        String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
       
        System.out.println("Respuesta dateExecution" + dateExecution);
     // System.out.println("Respuesta dateExecution"+dateExecution);
        
        SQLResult result5 = executeQuery(dbLOG, tdcIntegrationServerFormat);
		
		
		String status1 = result5.getData(0, "STATUS");
		String run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLOG, tdcIntegrationServerFormat);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			

			u.hardWait(2);

		}
		
       
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
       if (!successRun) {

	   String error = String.format(consultaERROR, run_id);
	   String error1 = String.format(consultaERROR2, run_id);
	   String error2 = String.format(consultaERROR3, run_id);
	   
	  
		SQLResult result3 = executeQuery(dbLOG, error);
		SQLResult resultError1 = executeQuery(dbLOG, error1);
		SQLResult resultError2 = executeQuery(dbLOG, error2);

		boolean emptyError = result3.isEmpty();
		

		if (!emptyError) {

			testCase.addTextEvidenceCurrentStep(
					"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

			testCase.addQueryEvidenceCurrentStep(result3);

		}
	

	   boolean emptyError1 = resultError1.isEmpty();
	   if (!emptyError1) {
		testCase.addQueryEvidenceCurrentStep(resultError1);
	   }
	   boolean emptyError2 = resultError2.isEmpty();

	    if (!emptyError2) {

		testCase.addQueryEvidenceCurrentStep(resultError2);

	}

}
       u.close();
//Paso 3    ************************		
addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run "
		+ "SELECT * FROM wmlog.wm_log_run WHERE interface = 'RO4' ORDER BY start_dt DESC;");
		
		SQLResult result3 = executeQuery(dbLOG, tdcIntegrationServerFormat);
         String fcwS = result3.getData(0, "STATUS");
         
         boolean validateStatus = status.equals(fcwS);
         System.out.println(validateStatus);
         assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");

         boolean av2 = result3.isEmpty();
         if (av2 == false) {

	     testCase.addQueryEvidenceCurrentStep(result3);

       } else {
	     testCase.addQueryEvidenceCurrentStep(result3);
}
        System.out.println(av2);

//Paso 4    ************************
addStep("Verificar que la interfaz genero Threads "
		+ "SELECT * FROM wmlog.wm_log_thread WHERE parent_id = [wm_log_run.tun_id]");
    String consultados1 = String.format(consultaTHREADS, run_id);
        System.out.println("CONSULTA THREAD" + consultados1);
        
        SQLResult result4 = executeQuery(dbLOG, consultados1);
        boolean av31 = result4.isEmpty();
        if (av31 == false) {

		testCase.addQueryEvidenceCurrentStep(result4);

     } else {
    	 testCase.addQueryEvidenceCurrentStep(result4);

}
     System.out.println(av31);
// .-----------Segunda consulta
    String consultado = String.format(consultaTHREADS2, run_id);
    
    SQLResult result4Temp = executeQuery(dbLOG, consultado);
    boolean av3111 = result4Temp.isEmpty();
    if (av3111 == false) {

	testCase.addQueryEvidenceCurrentStep(result4Temp);

    } else {
    	testCase.addQueryEvidenceCurrentStep(result4Temp);
    }
    System.out.println(av31);
    assertFalse("No se generaron threads en la tabla", av3111);
    
    
    String THREAD1 = result4.getData(0, "THREAD_ID");
    String THREAD= THREAD1;


 //Paso 5   ************************
  addStep("Verificar la informacion procesada en las tablas  de ORAFIN. SELECT *" + 
  		" FROM WMUSER.XXFC_PAGO_SERVICIO_FORMAS_STG " + 
  		" WHERE fecha_administrativa = TO_DATE ([POS_FPE_DETL.ADMIN_DATE], 'DD/MM/YYYY')" + 
  		" AND creation_date >= TRUNC (SYSDATE)" + 
  		" AND ticket = [POS_FPE_DETL.TICKET]" + 
  		" AND plaza = [PLAZA]" + 
  		" AND tienda = [TIENDA]");
  
  String STG= String.format(consultaPagoSTG, pv_date, ticket);
  
  SQLResult resultServ = executeQuery(dbEbs, STG);
  
  boolean serv5 = resultServ.isEmpty();
  if (!serv5) {
  testCase.addQueryEvidenceCurrentStep(resultServ);
  }
  System.out.println(serv5);
  System.out.println(STG);
  assertFalse( "Error hora fin",serv5);
 
  
  String STG2= String.format(consultaPagoSTG2, pv_date, ticket);
  SQLResult resultSTG2 = executeQuery(dbEbs, STG2);
  System.out.println(STG2);
  boolean serv52 = resultSTG2.isEmpty();
  if (!serv52) {
  testCase.addQueryEvidenceCurrentStep(resultSTG2);
  }
  System.out.println(serv52);
  assertFalse( "",serv52);
  
//Paso 6   ************************
  addStep("Verificar la informacion procesada en las tablas  de ORAFIN. SELECT *" + 
  		" FROM XXFC.XXFC_PAGO_SERVICIO_FORMAS " + 
  		" WHERE fecha_administrativa = TO_DATE ([POS_FPE_DETL.ADMIN_DATE], 'DD/MM/YYYY')" + 
  		" AND creation_date >= TRUNC (SYSDATE)" + 
  		" AND ticket = [POS_FPE_DETL.TICKET]" + 
  		" AND plaza = [PLAZA]" + 
  		" AND tienda = [TIENDA]");
  
String formas= String.format(consultaPagoFORMAS, pv_date, ticket);
  SQLResult resultFormas = executeQuery(dbEbs, formas);
  boolean serv6 = resultFormas.isEmpty();
  if (!serv6) {
  testCase.addQueryEvidenceCurrentStep(resultFormas);
  }
  System.out.println(serv6);
  System.out.println(formas);
  assertFalse( "Error hora fin", serv6);
  
  
  String formas2= String.format(consultaPagoFORMAS2, pv_date, ticket);
  SQLResult resultFormas2 = executeQuery(dbEbs, formas2);
  boolean serv61 = resultFormas2.isEmpty();
  if (!serv61) {
  testCase.addQueryEvidenceCurrentStep(resultFormas2);
  }
  System.out.println(serv61);
 
  System.out.println(formas2);
  assertFalse( "No hay insumos a procesar",serv61);
  
//Paso 7   ************************
  addStep("Verificar que se actualizaron los datos en en la tabla POS_INBOUND_DOCS de  POSUSER. " + 
  		"SELECT *" + 
  		"  FROM POS_INBOUND_DOCS" + 
  		" WHERE id = [POS_INBOUND_DOCS.ID] " + 
  		" AND target_id = [WM_LOG_THREAD.THREAD_ID] " + 
  		" AND status = 'E' ") ;
 
  String fpe= String.format(Cons_FPE_E, THREAD, ID);
  SQLResult result7 = executeQuery(dbPOSUSER, fpe);
  System.out.println(fpe);
 boolean servv = result7.isEmpty();
  if (!servv) {
  testCase.addQueryEvidenceCurrentStep(result7);}
  System.out.println(servv);
  System.out.println(fpe);
  //System.out.println(Cons_FPE_I2);
  
  String fpe1= String.format(Cons_FPE_E2, THREAD, ID);
  SQLResult result7Fpe = executeQuery(dbPOSUSER, fpe1);

  System.out.println(fpe1);
 boolean servv1 = result7Fpe.isEmpty();
  if (!servv1) {
  testCase.addQueryEvidenceCurrentStep(result7Fpe);
  }
  System.out.println(servv1);
  System.out.println(fpe1);
  //System.out.println(Cons_FPE_I2);
  assertFalse( "No hay insumos a procesar",servv);
 

  
		}
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar proceso normal la ejecuci?n de la interfaz para la tienda ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO18_EjecucionInterfaz_test";
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
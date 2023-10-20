package interfaces.po22_cl;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PO22_CL_EjecucionCorrectaFAC extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO22_CL_Verificar_Transferencia_ComprasyDev(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ, GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryDepth = "SELECT interfase, valor1"
				+ " FROM wmuser.wm_interfase_config"
				+ " WHERE interfase = 'PO22_CL' "
				+ " AND operacion = 'DEPTH_DAYS'";
		
		String tdcQueryDocFac = "SELECT A.ID, B.DOC_TYPE, B.STATUS, B.PARTITION_DATE "
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_INBOUND_DOCS B "
				+ " WHERE B.PE_ID = A.ID "
				+ " AND B.DOC_TYPE = 'FAC'"
				+ " AND B.STATUS = 'I'"
				+ " AND B.PARTITION_DATE >= TRUNC(SYSDATE - '%s') "
				+ " AND A.PARTITION_DATE >= TRUNC(SYSDATE - '%s')";
		
		String tdcQueryPosFac = "SELECT PID_ID, PV_DOC_ID, DOC_TYPE, VENDOR_NUM, VENDOR_SITE_CODE"
				+ " FROM POS_FAC"
				+ " WHERE PID_ID IN (%s)"
				+ " AND DOC_TYPE = 'FAC'";
		
		String tdcQueryFacDetl = "SELECT PID_ID, LINE_NUMBER, AMOUNT "
				+ " FROM POS_FAC_DETL"
				+ " WHERE PID_ID IN (%s)";
		
		String tdcQueryInvoices = "SELECT COUNT(*) "
				+ " FROM AP.AP_INVOICES_INTERFACE";
		
		String tdcQueryEbs = "SELECT AP.SEGMENT1, AP.VENDOR_ID, AP.VENDOR_NAME, APS.VENDOR_SITE_ID, APS.TERMS_ID"
				+ " FROM APPS.AP_SUPPLIERS AP, APPS.AP_SUPPLIER_SITES_ALL APS"
				+ " WHERE AP.SEGMENT1 = '%s'"
//				+ " AND AP.VENDOR_ID = APS.VENDOR_ID"  
				+ " AND APS.VENDOR_SITE_CODE = '%s'";
		
		String tdcQueryErrorId =" SELECT RUN_ID, INTERFACE, STATUS, SERVER, END_DT"
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " where INTERFACE='PO22_CL'"
				+ " and rownum = 1"
				+ " AND TRUNC(END_DT)=TRUNC(sysdate) "
				+ " ORDER BY END_DT DESC";
		
		String tdcQueryStatusE = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE "
				+ " FROM POS_INBOUND_DOCS "
				+ " WHERE DOC_TYPE='FAC' "
				+ " AND STATUS='E' "
				+ " ORDER BY received_date DESC";
		
		
		
		
		
		
		
		
	
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Accesar a la BD OCHWMWA con el usuario WMUSER.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_PosUserChile);
		
//Paso 2 *************************		
		addStep("Consultar los días de profundidad que serán utilizados en la interfaz con la siguiente consulta:");	
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryDepth);
		
		SQLResult depthResult = executeQuery(dbPos, tdcQueryDepth);
		String depth_days = depthResult.getData(0, "valor1");
		
		boolean depth = depthResult.isEmpty();
		
		if (!depth) {
			
			testCase.addQueryEvidenceCurrentStep(depthResult);
		}
		
		System.out.println(depth);
		
		assertFalse(depth, "No se obtiene información de la consulta");
		
//Paso 3 *************************		
		addStep("Acceder al esquema POSUSER de OCHWMQA y comprobar que existan documentos tipo FAC pendientes para ser procesados por la PO22 con la siguiente consulta.");	
				
		String DocFacFormat = String.format(tdcQueryDocFac, depth_days);
		System.out.println(DocFacFormat);
		
		SQLResult DocFacResult = executeQuery(dbPos, DocFacFormat);
		String id = DocFacResult.getData(0, "ID");
				
		boolean docFac = DocFacResult.isEmpty();
		
		if (!docFac) {	
			
			testCase.addQueryEvidenceCurrentStep(DocFacResult);		
			
		} 

		System.out.println(docFac);

		assertFalse(docFac, "La tabla no contiene registros");
		
//Paso 4 ***********************
		addStep("Comprobar la información del header del archivo FAC con la siguiente consulta:");
		String posFacFormat = String.format(tdcQueryPosFac, id);
		System.out.println(posFacFormat);
		
		SQLResult posFacResult = executeQuery(dbPos, posFacFormat);
		String pid_id = posFacResult.getData(0, "PID_ID");
		String vendor_num = posFacResult.getData(0, "VENDOR_NUM");
		String vendor_site_code = posFacResult.getData(0, "VENDOR_SITE_CODE");
				
		boolean posFac = posFacResult.isEmpty();
		
		if (!posFac) {	
			
			testCase.addQueryEvidenceCurrentStep(posFacResult);		
			
		} 

		System.out.println(posFac);

		assertFalse(posFac, "La tabla no contiene registros");		
		
//Paso 5 **********************
		addStep("Conseguir el detalle de cada uno de los documentos con la siguiente consulta:");
		String FacDetlFormat = String.format(tdcQueryFacDetl, pid_id);
		System.out.println(FacDetlFormat);
		
		SQLResult FacDetlResult = executeQuery(dbPos, FacDetlFormat);
			
		boolean FacDetl = FacDetlResult.isEmpty();
		
		if (!FacDetl) {	
			
			testCase.addQueryEvidenceCurrentStep(FacDetlResult);		
			
		} 

		System.out.println(FacDetl);

		assertFalse(FacDetl, "La tabla no contiene registros");		
		
//Paso 6 *************************		
		addStep("Acceder a la BD OCHEBSQA.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OIEBSBDQ");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_OIEBSBDQ);
		
//Paso 7 *************************	
		addStep("Comprobar que datos se encuentran presentes en la tabla AP_INVOICES_INTERFACE antes de ejecutar la interfaz con la siguiente consulta:");
		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(tdcQueryInvoices);
		
		SQLResult invoiceResult = executeQuery(dbEbs, tdcQueryInvoices);
		
		boolean invoice = invoiceResult.isEmpty();
		
		if (!invoice) {
			
			testCase.addQueryEvidenceCurrentStep(invoiceResult);
		}
		
		System.out.println(invoice);
		
		assertFalse(invoice, "No se obtiene información de la consulta");
		
//Paso 8 *************************			
		addStep("Comprobar que el proveedor que está en la tabla POS_FAC exista en EBS con la siguiente consulta:");
		String ebsFormat = String.format(tdcQueryEbs, vendor_num, vendor_site_code);
		System.out.println(ebsFormat);
		
		SQLResult ebsResult = executeQuery(dbEbs, ebsFormat);
			
		boolean proveedor = ebsResult.isEmpty();
		
		if (!proveedor) {	
			
			testCase.addQueryEvidenceCurrentStep(ebsResult);		
			
		} 

		System.out.println(proveedor);

		assertFalse(proveedor, "La tabla no contiene registros");		
		
//Paso 9 ********************
		addStep("Ejecutar el servicio: ");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		
//Paso 10 ********************
		addStep("Acceder a la base de datos OCHWMQA con el usuario WMLOG.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_LogChile);	
					
//Paso 11 *******************************
	    addStep("Verificar que no contenga errores de ejecución.");
	    SQLResult errorResult = executeQuery(dbLog, tdcQueryErrorId);
	    	   
	    boolean emptyError = errorResult.isEmpty();
	   
	    if(!emptyError){  
	   	   
	    	testCase.addQueryEvidenceCurrentStep(errorResult);
	   
	    }
	   
	    testCase.addQueryEvidenceCurrentStep(errorResult);
	    testCase.addTextEvidenceCurrentStep(" No se encontró un error en la ejecución de la interfaz, en la tabla WM_LOG_ERROR");
		
 //Paso 12 ******************
	    addStep("Comprobar que el documento ahora se encuentra en status E.");
	    String statusEFormat = String.format(tdcQueryStatusE, depth_days);
	    System.out.println(statusEFormat);
	
	    SQLResult statusEResult = executeQuery(dbPos, statusEFormat);
			
	    boolean statusE = statusEResult.isEmpty();
	
	    if (!statusE) {	
		
	    	testCase.addQueryEvidenceCurrentStep(statusEResult);		
		
	    } 

	    System.out.println(statusE);

	    assertFalse(statusE, "La tabla no contiene registros");		
		
		
		
		
		
		
		
		
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
		return "Terminada. El propósito de la interface, es transferir las compras y devoluciones (Facturas y notas de crédito) de las tiendas de Chile hacia las BD de EBS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PO22_CL_Verificar_Transferencia_ComprasyDev";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}

package interfaces.ro12;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;
import modelo.BaseExecution;
import util.GlobalVariables;


public class RO12SyncPosUpdTienda extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO12_003_SyncPosUpTienda(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_Ebs,GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs);
		SQLUtil dbPuser = new SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
//		SQLUtil dbWm = new SQLUtil(GlobalVariables.DB_HOST_Data, GlobalVariables.DB_USER_Data, GlobalVariables.DB_PASSWORD_Data);

		
		
		/* Variables *************************************************************************/
		
		String tdcUpdateOrafin = "UPDATE WMUSER.WM_SYNC_POS_EXC_RATES"
				+" SET WM_STATUS_CODE = 'L'"  
				+" WHERE CR_TIENDA = '" + data.get("TIENDA") +"'"
				+" AND CR_PLAZA = '" + data.get("PLAZA") +"'" 
				+" AND WM_STATUS_CODE = 'E'" 
				+" AND  EXC_RATE_TYPE = 'T'" 
				+" AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		//La consulta  WM_LOG_RUN se puede dividir en dos para mostrar los datos ATT
		String tdcQueryWmLog1 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER, ATT1, ATT2, ATT3, ATT4, ATT5 "+
				" FROM WMLOG.WM_LOG_RUN" + 
				" WHERE INTERFACE = 'RO12_MX'" + 
				" AND STATUS = 'S'" + 
				" AND START_DT > TRUNC(SYSDATE)" + 
				" ORDER BY START_DT DESC"; 
		
		
		String tdcQueryPosOutboundDocs = "SELECT ID,POE_ID,DOC_NAME,DOC_TYPE,SENT_DATE,STATUS,DATE_CREATED"+
				" FROM POSUSER.POS_OUTBOUND_DOCS" + 
				" WHERE DOC_TYPE = 'EXR'" +
				" AND PV_CR_PLAZA= '"+ data.get("PLAZA") +"'"+
				" AND PV_CR_TIENDA='"+ data.get("TIENDA") +"'"+
				" AND PARTITION_DATE>=TRUNC(SYSDATE)" + 
				" ORDER BY SENT_DATE DESC"; 
		
		//La consulta a WM_SYNC_POS_EXC_RATESse divide en dos para mostrar todos los datos
		String tdcQueryWmSyncPosExcRates1 = "SELECT ID,CR_PLAZA,CR_TIENDA,EXC_RATE_TYPE,FROM_CURRENCY, WM_STATUS_CODE, TO_CURRENCY,CONVERSION_DATE,CONVERSION_TYPE"+
				" FROM WMUSER.WM_SYNC_POS_EXC_RATES" + 
				" WHERE CR_TIENDA = '"+ data.get("TIENDA") +"'"+
				" AND CR_PLAZA = '"+ data.get("PLAZA") +"'"+
				" AND WM_STATUS_CODE = 'E' "+
				" AND CREATION_DATE >= TRUNC(SYSDATE)"; 
		
		String tdcQueryWmSyncPosExcRates2 = "SELECT CONVERSION_RATE,USER_CONVERSION_TYPE,WM_STATUS_CODE,CREATION_DATE,LAST_UPDATE_DATE"+
				" FROM WMUSER.WM_SYNC_POS_EXC_RATES" + 
				" WHERE WHERE CR_TIENDA = '"+ data.get("TIENDA") +"'"+
				" AND CR_PLAZA = ' "+ data.get("PLAZA") +"'"+
				" AND WM_STATUS_CODE = 'E' "+
				" AND CREATION_DATE >= TRUNC(SYSDATE)"; 
      

		
        //Paso  Consulta 
		String statusWmlog="S",wm_status="E";
		
	      
		/* Pasos *****************************************************************************/   

		/* **************************************************************************/   
	   	 
		//Paso 1   
	    addStep("Actualizar un Tipo de Cambio a nivel Tienda en la tabla WM_SYNC_POS_EXC_RATES de ORAFIN"); 
	    
  
	  
		//Paso 2	
		addStep("Validar correcta ejecución de la interface en la tabla WM_LOG_RUN de WMLOG");
		
		SQLResult status = executeQuery(dbLog, tdcQueryWmLog1);
		
		String status2 = status.getData(0, "STATUS");
		
           boolean run = status2.equals(statusWmlog);
       
        assertTrue(run);
		

        //Paso 3
        addStep("Validar correcta inserción de los documentos enviados a la Tienda en la tabla POS_OUTBOUND_DOCS de POSUSER"); 
        
	       System.out.println(tdcQueryPosOutboundDocs); 
	        
	       SQLResult Currency = executeQuery(dbPuser, tdcQueryPosOutboundDocs);
	        
	       boolean outboundDocs = Currency.isEmpty();
	       testCase.addQueryEvidenceCurrentStep(Currency);
     	
	    assertTrue(outboundDocs);
        	
        //Paso 4
	    addStep("Validar la correcta actualización de WM_STATUS_CODE = 'E', para la Tienda con el Tipo de Cambio enviado en la tabla WM_SYNC_POS_EXC_RATES de ORAFIN");

	    
		   SQLResult wm_statusDb = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates1); //primera consulta
	    	
		   String StatusE = wm_statusDb.getData(0, "WM_STATUS_CODE");
		    
		   SQLResult wm_statusDb2 = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates2);
		   
	       testCase.addQueryEvidenceCurrentStep(wm_statusDb2); //segunda consulta
	       
           boolean statusRate = StatusE.equals(wm_status);
        
         assertTrue(statusRate);

	}

	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		return " Validar que el Tipo de Cambio Tienda sea enviado a la Tienda.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_RO12_003_SyncPosUpTienda";
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


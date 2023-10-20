package interfaces.ol9;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class OL9PreRequisitos extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL9_OL9PreRequisitos(HashMap<String, String> data) throws Exception { 
		
		//BD
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEBSav = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_FCEBC4, GlobalVariables.DB_USER_EBS_FCEBC4, GlobalVariables.DB_PASSWORD_EBS_FCEBC4);
		
		 
		
		//Utileria
		String tdcQueryEstatusNull = "select trans_id,plaza,tienda,folio_transaccion,servicio,fecha_transaccion,fecha_recepcion,hora_transaccion,estatus "
				+ " from  XXFC.XXFC_PAGO_SERVICIOS_PRE" + 
				" WHERE SERVICIO =" +"'"+ data.get("service_id")+"'"+
				" AND PLAZA ="+"'" + data.get("PLAZA")+"'"+
				" AND TIENDA = "+"'"+ data.get("TIENDA")+"'"+
				" AND ESTATUS IS NULL";
		
		String tdcQueryStatusLog = "SELECT DOC_NAME, VENDOR_ID, CREATED_DATE, STATUS, REGISTROS "
				+ "FROM WM_OL9_OUTBOUND_DOCS" + 
				" where created_date = TRUNC (SYSDATE)" + 
				" and vendor_id = '%s'" + 
				" order by CREATED_DATE desc";
		
		String tdcQueryStatusData = "SELECT VENDOR_ID, SERVICE_ID, ATTRIBUTE6, CR_PLAZA "
				+ " FROM XXFC_SERVICES_VENDOR_COMM_DATA "
				+ " where SERVICE_ID =" +"'"+ data.get("service_id")+"'";
		
		testCase.setProject_Name("Configuracion de ambientes.");

		/////////////////// PASOS ///////////////////////////
			addStep("RESUMEN"); 
	
			// *********************************************************************************************************************************
			//** PASO 1 *************************************************************************************************************

			testCase.addBoldTextEvidenceCurrentStep("Conectarse a la base de datos EBS_FCEBC4.");
					
					testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_EBS_FCEBC4 + " usuario: "
							+ GlobalVariables.DB_USER_EBS_FCEBC4);
					testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + dbEBSav.getConn());
			
//** PASO 2 *************************************************************************************************************
				testCase.addBoldTextEvidenceCurrentStep("Verificar que en la tabla XXFC_PAGO_SERVICIOS_PRE exista información con estatus nulo o F");

				System.out.println(GlobalVariables.DB_HOST_EBS);
				System.out.println(tdcQueryEstatusNull);

				SQLResult nulls = dbEBSav.executeQuery(tdcQueryEstatusNull);
				boolean nul = nulls.isEmpty();

					if (!nul) {
			
						//testCase.addQueryEvidenceCurrentStep(nulls);    
						testCase.addTextEvidenceCurrentStep("-La tabla contiene información ");
			
			
						}else {
							
							testCase.addTextEvidenceCurrentStep("-La tabla no contiene registros");
						}

					
					
				

//** PASO 3 *************************************************************************************************************	
				testCase.addBoldTextEvidenceCurrentStep("Validar que el service_id contenga el email en la tabla XXFC_SERVICES_VENDOR_COMM_DATA");
				
				SQLResult validaEmail = executeQuery(dbEBSav, tdcQueryStatusData);
				
				String emailEmpty = validaEmail.getData(0, "ATTRIBUTE6");
				
				boolean statusDataEmpty = validaEmail.isEmpty();
				
				if(!statusDataEmpty) {
					boolean email = emailEmpty.isEmpty();
					testCase.addTextEvidenceCurrentStep("-El servicio se encuentra en la tabla");
					if(!email) {
						testCase.addTextEvidenceCurrentStep("-El email "+emailEmpty+" esta dado de alta");
					}
				}
				
	//** PASO 4 *************************************************************************************************************	
				testCase.addBoldTextEvidenceCurrentStep("Verificar la ejecución de la interfaz ha procesado un archivo el dia de hoy");
			
				String vendor_id = validaEmail.getData(0, "VENDOR_ID");
			String TdcQquery = String.format(tdcQueryStatusLog, vendor_id);	
			SQLResult wmol9 = executeQuery(dbEBSav, TdcQquery);
			 
			 boolean av = wmol9.isEmpty();
			 
			 if(av) {
				 testCase.addTextEvidenceCurrentStep("-Este Vendor id esta Disponible");
			 }else {
				  testCase.addTextEvidenceCurrentStep("-Este Vendor id ya se utilizo el día de hoy");
				  testCase.addTextEvidenceCurrentStep("-Nota: Para poder utiliar este vendor id, deberas elimnarlo de la tabla.");
			 }
				
				testCase.addBoldTextEvidenceCurrentStep("EVIDENCIAS");// *********************************************************************************************************************************
		//** PASO 1 *************************************************************************************************************

				addStep("Conectarse a la base de datos EBS_FCEBC4.");
				
				testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_EBS_FCEBC4 + " usuario: "
						+ GlobalVariables.DB_USER_EBS_FCEBC4);
				testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + dbEBSav.getConn());
		
		//** PASO 2 *************************************************************************************************************
		addStep("Verificar que en la tabla XXFC_PAGO_SERVICIOS_PRE exista información con estatus nulo o F");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println(tdcQueryEstatusNull);

		 nulls = dbEBSav.executeQuery(tdcQueryEstatusNull);
		 nul = nulls.isEmpty();

			if (!nul) {
	
				testCase.addQueryEvidenceCurrentStep(nulls);    
				testCase.addTextEvidenceCurrentStep("-La tabla contiene información ");
	
	
				}else {
					testCase.addQueryEvidenceCurrentStep(nulls);    
					testCase.addTextEvidenceCurrentStep("-La tabla no contiene registros");
				}

			
		
		
		//** PASO 3 *************************************************************************************************************	
			addStep("Validar que el service_id contenga el email en la tabla XXFC_SERVICES_VENDOR_COMM_DATA");
		
		 validaEmail = executeQuery(dbEBSav, tdcQueryStatusData);
		
		 emailEmpty = validaEmail.getData(0, "ATTRIBUTE6");
		
		 statusDataEmpty = validaEmail.isEmpty();
		
		if(!statusDataEmpty) {
			boolean email = emailEmpty.isEmpty();
			testCase.addQueryEvidenceCurrentStep(validaEmail);
			testCase.addTextEvidenceCurrentStep("-El servicio se encuentra en la tabla");
			if(!email) {
				testCase.addTextEvidenceCurrentStep("-El email "+emailEmpty+" esta dado de alta");
			}
		}
		
		//** PASO 4 *************************************************************************************************************	
		addStep("Verificar la ejecución de la interfaz ha procesado un archivo el dia de hoy");
	
		 vendor_id = validaEmail.getData(0, "VENDOR_ID");
		 TdcQquery = String.format(tdcQueryStatusLog, vendor_id);	
		 wmol9 = executeQuery(dbEBSav, TdcQquery);
	 
	 av = wmol9.isEmpty();
	 
	 if(av) {
		 testCase.addQueryEvidenceCurrentStep(wmol9);
		 testCase.addTextEvidenceCurrentStep("-Este Vendor id esta Disponible");
	 }else {
		 testCase.addQueryEvidenceCurrentStep(wmol9);
		 testCase.addTextEvidenceCurrentStep("-Este Vendor id ya se utilizo el día de hoy");
		 testCase.addTextEvidenceCurrentStep("-Nota: Para poder utiliar este vendor id, deberas elimnarlo de la tabla.");

	 }
		
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
		return "Construida. OL9PreRequisitos ";
	}

	@Override
	public String setTestDesigner() {
		return "tbd";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL9_OL9PreRequisitos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
		

}

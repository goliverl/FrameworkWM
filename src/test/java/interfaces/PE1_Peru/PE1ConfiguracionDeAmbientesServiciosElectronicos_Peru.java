package interfaces.PE1_Peru;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import util.GlobalVariables;
import util.SSHConnector;
import org.testng.annotations.Test;
import com.jcraft.jsch.JSchException;
import integrationServer.om.PakageManagment;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PE1ConfiguracionDeAmbientesServiciosElectronicos_Peru extends BaseExecution {

	private static final String USERNAME = PasswordUtil.decryptPassword("B154EF642C53C68266902033F242CC52");
	private static final String HOST = "10.184.40.109";
	private static final int PORT = 22;
	private static final String PASSWORD = PasswordUtil.decryptPassword("E1D7C6C1F1080EC9B01E3EC18DF1BC77");

	@Test(dataProvider = "data-provider")

	public void ATC_FT_003_PE01_Peru_Config_Ambientes_Serv_Electronicos(HashMap<String, String> data) throws Exception {

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);

		// consultas

		String query_carrier = "select * from tpeuser.tae_carrier where carrier = '" + data.get("carrier") + "'";

		String query_routing = "select * from tpeuser.tae_routing where carrier = '" + data.get("carrier") + "'"
				+ " AND plaza = '" + data.get("plaza") + "'";

		String query_switch = "select * from TPEUSER.TAE_ROUTING where carrier='" + data.get("carrier")
				+ "' AND plaza= '" + data.get("plaza") + "'";

		// consulta para verificar que no se registraron errores en la transaccion
		// Conectarse a base de datos
		addStep("RESUMEN.");

// Paso 1 *********************************************************************************

		System.out.println("Paso 1");

		testCase.addBoldTextEvidenceCurrentStep(
				"Verificar que el carrier sea valido verificando en la tabla tpeuser.tae_carrier.");
		SQLResult validaCarrierRes = db.executeQuery(query_carrier); // es la primera de peru

		System.out.println(query_carrier);
		boolean validaCarrierBool = validaCarrierRes.isEmpty(); // checa que el string contenga datos

		System.out.println(validaCarrierBool);
		if (validaCarrierBool == false) {
			System.out.print("El carrier es correcto \n");
			testCase.addTextEvidenceCurrentStep("-El carrier  '" + data.get("carrier") + "' es valido.");
			// testCase.addQueryEvidenceCurrentStep(validaCarrierRes);
		} else {
			System.out.print("El carrier no es correcto \n");
			testCase.addTextEvidenceCurrentStep("-El carrier: '" + data.get("carrier") + "' no es valido.");
		}

// Paso 2 *********************************************************************************

		System.out.println("Paso 2");

		testCase.addBoldTextEvidenceCurrentStep(
				"Validar que la tienda este configurada con el carrier solicitado en la tabla tpeuser.tae_routing.");

		SQLResult validaTiendaRes = db.executeQuery(query_routing); // es la primera de peru

		System.out.println(query_routing);
		boolean validaTiendaBool = validaTiendaRes.isEmpty(); // checa que el string contenga datos

		System.out.println(validaTiendaBool);
		if (validaTiendaBool == false) {
			// testCase.addQueryEvidenceCurrentStep(validaTiendaRes);
			testCase.addTextEvidenceCurrentStep("-La plaza: " + data.get("plaza") + " cuenta con el carrier ingresado "
					+ data.get("carrier") + ", por ende esta configurado.");
			System.out.print("La tienda cuenta con el carrier ingresado \n");
		} else {
			testCase.addTextEvidenceCurrentStep("La plaza: " + data.get("plaza") + " no esta mapeada con el carrier: "
					+ data.get("carrier") + "\n");
			System.out.print("La tienda no cuenta con el carrier ingresado \n");
		}

// Paso 3 *********************************************************************************


		testCase.addBoldTextEvidenceCurrentStep("Validar la configuracion del telefono.");
		testCase.addTextEvidenceCurrentStep(
				"Nota: Se consigue la configuracion de los telefonos mediante la lista de telefonos validos.");
		String resp = null;
		String[][] array = null;
		String path = System.getProperty("user.dir") + "/Interfaces/PE1/PE1Telefonos.txt";
		boolean valido = false;
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			FileReader fr2 = new FileReader(path);
			BufferedReader br2 = new BufferedReader(fr2);

			String linea;
			String[] parts;
			int i = 0;
			int filas = (int) br2.lines().count();
			array = new String[filas][3];

			while ((linea = br.readLine()) != null) {

				parts = linea.split(",");
				array[i][0] = parts[0];
				array[i][1] = parts[1];
				array[i][2] = parts[2];
				i++;

			}

			fr.close();

		} catch (Exception e) {
			System.out.println("Excepcion leyendo fichero : " + e);
		}

		for (int i = 0; i < array.length; i++) {

			for (int j = 0; j < array[i].length; j++) {

				if (data.get("phone").equals(array[i][j])) {
					System.out.print("Número válido\n");
					resp = "-El número ingesado: '" + array[i][0] + "' esta configurado para pruebas de " + array[i][1]
							+ ". \n" + " Es válido para la compañia: '" + array[i][2] + "'.";
					testCase.addTextEvidenceCurrentStep(resp);
					valido = true;
				}

			}

		}

		if (valido == false) {
			System.out.print("Número no válido\n");
			resp = "Número no válido:" + data.get("phone");
			testCase.addTextEvidenceCurrentStep(resp);
		}

// Paso 4  *********************************************************************************


		testCase.addBoldTextEvidenceCurrentStep(
				"Consultar el swtich configurado con el carrier y plaza ingresados, con la siguiente"
						+ "consulta en la tabla TPEUSER.TAE_ROUTING");

		SQLResult validaSwitch = db.executeQuery(query_switch); // es la segunda dbFCTPEQA

		System.out.println(query_switch);
		String carrierSwitch = validaSwitch.getData(0, "SWITCH");
		boolean validaSwitchBOl = validaSwitch.isEmpty(); // checa que el string contenga datos

		System.out.println(validaSwitchBOl);
		if (validaSwitchBOl == false) {
			System.out.print("Switch encontrado " + carrierSwitch);
			testCase.addTextEvidenceCurrentStep(
					"-El carrier y la plaza estan configurados con el siguiente switch=: " + carrierSwitch + ".");
		} else {
			System.out.print("Switch no encontrado");
			testCase.addTextEvidenceCurrentStep(
					"-No se encontro un switch configurado para el carrier y plaza introducidos.");
		}

// Paso 5 *********************************************************************************


		testCase.addBoldTextEvidenceCurrentStep(
				"Buscar el switch en Integration Server en el apartado de global variables.");
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String valueSwitch = pok.getIpSwitch(carrierSwitch, false);
		System.out.println("valueSwitch " + valueSwitch);

		if (!valueSwitch.isEmpty()) {
			testCase.addTextEvidenceCurrentStep("-La direccion del switch es: '" + valueSwitch + "'.");
			String ip = valueSwitch.substring(7, 20);
			String puerto = valueSwitch.substring(21, 25);
			System.out.println("direccion del switch: " + valueSwitch + "ip= " + ip + " puerto= " + puerto);
			ssh("", false);

		} else {
			testCase.addTextEvidenceCurrentStep("-El Switch no esta en global variables.");

		}
		testCase.addBoldTextEvidenceCurrentStep("Evidencia: ");
		// *********************************************************************************************FIN
		// RESUMEN*********************************************************************************************
		
		addStep("Conectarse a la base de datos DB.");

		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_Oiwmqa + " usuario: "
				+ GlobalVariables.DB_USER_Oiwmqa);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn()); // es la primera de peru
		testCase.setProject_Name("Configuracion de ambientes.");

// Paso 6 *********************************************************************************

		addStep(" Verificar que el carrier sea valido verificando con la siguiente consulta en la tabla tpeuser.tae_carrier. ");

		System.out.println(validaCarrierBool);
		if (validaCarrierBool == false) {
			System.out.print("El carrier es correcto \n");
			testCase.addTextEvidenceCurrentStep("-El carrier  '" + data.get("carrier") + "' es valido.");
			testCase.addQueryEvidenceCurrentStep(validaCarrierRes);
		} else {
			System.out.print("El carrier no es correcto \n");
			testCase.addTextEvidenceCurrentStep("-El carrier: '" + data.get("carrier") + "' no es valido.");
		}

// Paso 7 *********************************************************************************


		addStep(" Validar que la tienda este configurada con el carrier solicitado con la siguiente consulta en la tabla tpeuser.tae_routing. ");

		System.out.println(validaTiendaBool);
		if (validaTiendaBool == false) {
			testCase.addQueryEvidenceCurrentStep(validaTiendaRes);
			testCase.addTextEvidenceCurrentStep("-La plaza: " + data.get("plaza") + " cuenta con el carrier ingresado "
					+ data.get("carrier") + ", por ende esta configurado.");
			System.out.print("La tienda cuenta con el carrier ingresado \n");
		} else {
			testCase.addTextEvidenceCurrentStep("La plaza: " + data.get("plaza") + " no esta mapeada con el carrier: "
					+ data.get("carrier") + "\n");
			System.out.print("La tienda no cuenta con el carrier ingresado \n");
		}

// Paso 8 *********************************************************************************


		addStep(" Validar la configuracion del telefono. ");
		testCase.addTextEvidenceCurrentStep(
				"Nota: Se consigue la configuracion de los telefonos mediante la lista de telefonos validos.");

		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			FileReader fr2 = new FileReader(path);
			BufferedReader br2 = new BufferedReader(fr2);

			String linea;
			String[] parts;
			int i = 0;
			int filas = (int) br2.lines().count();
			array = new String[filas][3];

			while ((linea = br.readLine()) != null) {

				parts = linea.split(",");
				array[i][0] = parts[0];
				array[i][1] = parts[1];
				array[i][2] = parts[2];
				i++;

			}

			fr.close();

		} catch (Exception e) {
			System.out.println("Excepcion leyendo fichero : " + e);
		}

		for (int i = 0; i < array.length; i++) {

			for (int j = 0; j < array[i].length; j++) {

				if (data.get("phone").equals(array[i][j])) {
					System.out.print("Número válido\n");
					resp = "-El número ingesado: '" + array[i][0] + "' esta configurado para pruebas de " + array[i][1]
							+ ". \n" + " Es válido para la compañia: '" + array[i][2] + "'.";
					testCase.addTextEvidenceCurrentStep(resp);
					valido = true;
				}

			}

		}

		if (valido == false) {
			System.out.print("Número no válido\n");
			resp = "Número no válido:" + data.get("phone");
			testCase.addTextEvidenceCurrentStep(resp);
		}

// Paso 9 *********************************************************************************


		addStep("Conectarse a la base de datos Oiwmqa.");

		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_Oiwmqa + " usuario: "
				+ GlobalVariables.DB_USER_Oiwmqa);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());

		// paso 5
		addStep("Consultar el swtich configurado con el carrier y plaza ingresados, con la siguiente consulta en la tabla TPEUSER.TAE_ROUTING");

		System.out.println(validaSwitchBOl);
		if (validaSwitchBOl == false) {
			System.out.print("Switch encontrado " + carrierSwitch);
			testCase.addTextEvidenceCurrentStep(
					"-El carrier y la plaza estan configurados con el siguiente switch=: " + carrierSwitch + ".");
			testCase.addQueryEvidenceCurrentStep(validaSwitch);
		} else {
			System.out.print("Switch no encontrado");
			testCase.addTextEvidenceCurrentStep(
					"-No se encontro un switch configurado para el carrier y plaza introducidos.");
		}

// Paso 10  *********************************************************************************

		System.out.println("Paso 10");

		addStep("Buscar el switch en Integration Server en el apartado de global variables.");
		u.get(contra);
		valueSwitch = pok.getIpSwitch(carrierSwitch, true);
		System.out.println("valueSwitch " + valueSwitch);

		if (!valueSwitch.isEmpty()) {
			testCase.addTextEvidenceCurrentStep("-La direccion del switch es: '" + valueSwitch + "'.");
			String ip = valueSwitch.substring(7, 20);
			String puerto = valueSwitch.substring(21, 25);
			System.out.println("direccion del switch: " + valueSwitch + "ip= " + ip + " puerto= " + puerto);
			ssh("", false);

		} else {
			testCase.addTextEvidenceCurrentStep("-El Switch no esta en global variables.");

		}
		u.close();

	}

	public void ssh(String switchConf, boolean resumen) {
		if (resumen == true) {
			addStep("Establecer conexion con sevicio SSH para validar configuracion del channel.");

		} else {
			testCase.addBoldTextEvidenceCurrentStep(
					"Establecer conexion con sevicio SSH para validar configuracion del channel.");

		}
		String bancoToLowerCase = "";
		bancoToLowerCase = switchConf.toLowerCase();
		// testCase.addTextEvidenceCurrentStep("Banco a buscar:
		// '"+bancoToLowerCase+"'");
		System.out.println(bancoToLowerCase);

		// ssh
		try {
			SSHConnector sshConnector = new SSHConnector();
			sshConnector.connect(USERNAME, PASSWORD, HOST, PORT);
			if (sshConnector.isConected()) {
				testCase.addTextEvidenceCurrentStep("-La conexion ssh a la direccion " + HOST + " fue exitosa. ");
				if (resumen == true) {
					addStep("Buscar la configuracion del channel del banco '" + switchConf + "'.");

				} else {
					testCase.addBoldTextEvidenceCurrentStep(
							"Buscar la configuracion del channel del banco '" + switchConf + "'.");

				}
				String result = sshConnector.executeCommand(
						"cd /u01/wmuser/webMethods9/IntegrationServer/instances/default/jpos/deploy; cat 10_channel_"
								+ bancoToLowerCase + ".xml");
				System.out.println("respuesta= " + result);
				if (result == "") {
					testCase.addTextEvidenceCurrentStep("-No se encuentra el channel del banco configurado.");
					testCase.addTextEvidenceCurrentStep(
							"-El documento 10_channel_" + bancoToLowerCase + ".xml no existe");

				} else {
					testCase.addTextEvidenceCurrentStep("-Channel encontrado: ");
					testCase.addTextEvidenceCurrentStep("10_channel_" + bancoToLowerCase + ".xml.-");

					testCase.addTextEvidenceCurrentStep(result);

				}
				sshConnector.disconnect();

			} else {
				testCase.addTextEvidenceCurrentStep("-La conexion ssh a la direccion " + HOST
						+ " no fue exitosa, verifica que el dispositivo esta disponible o si estas autorizado para conectarte.");

			}

		} catch (JSchException ex) {
			ex.printStackTrace();

			System.out.println(ex.getMessage());
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();

			System.out.println(ex.getMessage());
		} catch (IOException ex) {
			ex.printStackTrace();

			System.out.println(ex.getMessage());
		}
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_003_PE01_Peru_Config_Ambientes_Serv_Electronicos";
	}

	@Override
	public String setTestDescription() {
		return "Construido. Valida la configuracion de ambientes para pruebas.";
	}

	@Override
	public String setTestDesigner() {
		return "Equipo Automatizacion QA";
	}

	@Override
	public String setTestInstanceID() {
		return "-1";
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
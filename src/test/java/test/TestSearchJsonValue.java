package test;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.restassured.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import util.GlobalVariables;
import utils.password.PasswordUtil;

public class TestSearchJsonValue {

	//public static Object finalresult = null;
	

	public static String jsonSearchKey(String object, String key) throws JSONException {
		System.out.println("############ NUEVA CADENA #############");
		String x = null;

		if (object.contains("{")) {

			JSONObject json = new JSONObject(object);
			System.out.println(json.length());
			System.out.println(json.toString());

			System.out.println("Si es json valido");
			if (json.opt(key) == null) {
				System.out.println("No hay valor en el array");
				for (int i = 0; i <= json.length() - 1; i++) {
					String cadena = json.get(json.names().get(i).toString()).toString();
					if (cadena.contains("{"))
						x = jsonSearchKey(cadena, key);
					if (x != null)
						break;
				}

			} else {
				System.out.println("si se encontro el valor del key en el json");
				return json.get(key).toString();
			}

		} else {
			System.out.println("No es un json valido");
		}
		return x;
	}

	public static void main(String[] args) throws JSONException {

		// PasswordUtil.generateEncryptPass();

		// JSONObject jo = new
		// JSONObject("{\"TPEDoc\":{\"response\":{\"wmCode\":{\"value\":\"199\",\"desc\":\"Excepcion
		// General\"}}}}");
		JSONObject jo = new JSONObject("{\"value\":\"199\",\"desc\":\"Excepcion General\"}");

		String x = "{\"TPE\": {\"header\":{\"application\":\"BTC\",\"operation\":\"QRY01\",\"source\":\"POS\",\"plaza\":\"10MO\",\"tienda\":\"50AMO\",\"caja\":\"2\",\"folio\":\"340521\",\"operator\":\"121321\",\"creationDate\":\"2021111230180227\",\"avDate\":\"20210830120000\",\"pvDate\":\"20210830\"},\"response\":{\"wmCode\":{\"value\":\"101\",\"action\":\"ok\",\"desc\":\"Transaccion Completa\"}}}}";


//	       System.out.println("#########################");
//	       System.out.println(jo.get(jo.names().get(0).toString()).toString());
//	       System.out.println(jo.get(jo.names().get(0).toString()).toString().contains("{"));

		System.out.println(jsonSearchKey(x, "pvDate"));

		// jo.getJSONObject("wmCode").getString("value");

//	       Response response = null;
//
//	       
//	       try {
//	            response = RestAssured.given().contentType(ContentType.JSON).body(request).post(parameter);
//	            list.add(r.getDataReport(request,response.getBody().asString(),response.getStatusCode(),parts,parameter));
//	        } catch (Exception ex) {
//	            System.out.println(ex.getMessage());
//	        }

	}

}
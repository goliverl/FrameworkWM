package test;

import org.apache.commons.codec.binary.Base64;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AuthRequest {
	
	@Test
	public void AutorizationUserRequest() {
		
		//uri a mandar el request
		RestAssured.baseURI = "https://www.postman-echo.com";
		
		RequestSpecification request = RestAssured.given();
		// el usuario y contraseña estan separados por :
		String credentials  = "postman:password";
		// se usa la clase base64 para encriptar los datos de arriba y que no se vayan directo a la network
		//transformandolo en bytes para encriptarla
		byte [] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
		//pasar a string para pasarla al request header
		String encodedCredentialsString = new String(encodedCredentials);
		//pasar las credenciales al header de postman Authorization
		request.header("Authorization","Basic "+encodedCredentialsString);
		//Pasar el body en este caso vacio para el caso de prueba
		String body = "";
		//crear el request y mandarlo al server
		request.header("Content-type", "application/json");
		
		Response response = request.body(body).get("/basic-auth");
		System.out.println("Response status code is: " + response.getStatusCode());
		response.prettyPrint();		
		
		 try {
	           // response = RestAssured.given().contentType(ContentType.JSON).body(request).post(parameter);
	         //   list.add(r.getDataReport(request,response.getBody().asString(),response.getStatusCode(),parts,parameter));
	        } catch (Exception ex) {
	            System.out.println(ex.getMessage());
	        }		
	}
}

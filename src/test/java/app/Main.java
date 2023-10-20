package app;

import start.Start;
import utils.password.PasswordUtil;

public class Main {

	public static void main(String[] args) {
		Start.generateEncryptPassword();
		System.out.println(PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B"));
	}

}

package hello;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;

import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;


@SpringBootApplication
public class App implements CommandLineRunner {

	@Autowired
	private VaultTemplate vaultTemplate;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void run(String [] strings) throws Exception {

		// You usually would not print a secret to stderr
		VaultResponse response =
			vaultTemplate.opsForKeyValue(
			  "secret", KeyValueBackend.KV_2).get("github");

		System.err.println("\n\nValue of github.oauth2.key");
		System.err.println(response.getData().get("github.oauth2.key") + "\n");

		// Let's encrypt some data using the Transit backend.
		VaultTransitOperations transitOperations =
			vaultTemplate.opsForTransit();

		// We need to setup transit first (assuming you didn't set up it yet).
		VaultSysOperations sysOperations =
			vaultTemplate.opsForSys();

		if ( ! sysOperations.getMounts().containsKey("transit/")) {

			sysOperations.mount("transit", VaultMount.create("transit"));
			transitOperations.createKey("foo-key");
		}

		// Encrypt a plain-text value
		String ciphertext =
			transitOperations.encrypt(
				"foo-key", "Secure message");

		System.err.println("\nEncrypted value");
		System.err.println(ciphertext);


		// Decrypt a plain-text value
		String plaintext =
			transitOperations.decrypt(
				"foo-key", ciphertext);

		System.err.println("\nDecrypted value");
		System.err.println(plaintext + "\n\n");
	}
}

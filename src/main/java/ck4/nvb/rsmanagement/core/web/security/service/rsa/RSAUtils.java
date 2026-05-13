package ck4.nvb.rsmanagement.core.web.security.service.rsa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {
  public static final String KEY_ALGORITHM = "RSA";

  public static final int DEFAULT_KEY_SIZE = 2048;

  public static PublicKey getPublicKey(String publicKeyFileName) throws Exception {
    byte[] bytes = readFile(publicKeyFileName);
    return getPublicKey(bytes);
  }

  public static PrivateKey getPrivateKey(String privateKeyFileName) throws Exception {
    byte[] bytes = readFile(privateKeyFileName);
    return getPrivateKey(bytes);
  }

  public static byte[] readFile(String fileName) throws IOException {
    return Files.readAllBytes(new File(fileName).toPath());
  }

  private static PublicKey getPublicKey(byte[] bytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    bytes = Base64.getDecoder().decode(bytes);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePublic(keySpec);
  }

  private static PrivateKey getPrivateKey(byte[] bytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    bytes = Base64.getDecoder().decode(bytes);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePrivate(keySpec);
  }

  public static void generateKey(
      String publicKeyFileName, String privateKeyFileName, String secret, int keySize)
      throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    SecureRandom secureRandom = new SecureRandom(secret.getBytes());
    keyPairGenerator.initialize(Math.max(keySize, DEFAULT_KEY_SIZE), secureRandom);
    KeyPair keyPair = keyPairGenerator.genKeyPair();

    // get public key and write
    byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
    publicKeyBytes = Base64.getEncoder().encode(publicKeyBytes);
    writeFile(publicKeyFileName, publicKeyBytes);

    // get private key and write
    byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
    privateKeyBytes = Base64.getEncoder().encode(privateKeyBytes);
    writeFile(privateKeyFileName, privateKeyBytes);
  }

  private static void writeFile(String fileName, byte[] content) throws IOException {
    File file = new File(fileName);
    File parent = file.getParentFile();
    if (!file.exists()) {
      if (!parent.exists()) {
        parent.mkdirs();
      }
      file.createNewFile();
    }
    Files.write(file.toPath(), content);
  }
}

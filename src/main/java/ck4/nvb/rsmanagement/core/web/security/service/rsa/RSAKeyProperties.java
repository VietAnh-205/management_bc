package ck4.nvb.rsmanagement.core.web.security.service.rsa;

import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rs.key")
public class RSAKeyProperties {

  private String publicKeyFile;
  private String privateKeyFile;
  private String secret;

  private PublicKey publicKey;
  private PrivateKey privateKey;

  @PostConstruct
  public void createRSAKey() throws Exception {
    try {
      this.publicKey = RSAUtils.getPublicKey(publicKeyFile);
      this.privateKey = RSAUtils.getPrivateKey(privateKeyFile);
    } catch (Exception e) { // if public key or private key is invalid, generate a new key pair
      RSAUtils.generateKey(publicKeyFile, privateKeyFile, secret, 0);
      this.publicKey = RSAUtils.getPublicKey(publicKeyFile);
      this.privateKey = RSAUtils.getPrivateKey(privateKeyFile);
    }
  }
}

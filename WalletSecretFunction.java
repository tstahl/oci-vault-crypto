import com.fasterxml.jackson.core.JsonProcessingException;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletSecretFunction{

    private static String dbPassword;
    private static SecretsClient secretsClient;

 public static void main(String[] args) {        
	String version = System.getenv("OCI_RESOURCE_PRINCIPAL_VERSION");
        BasicAuthenticationDetailsProvider provider = null;
        if( version == null ) {
        //    provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
              provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
	}
        else {
            try {
                provider = new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        secretsClient = new SecretsClient(provider);
        secretsClient.setRegion(Region.US_ASHBURN_1);

        String dbPasswordOcid = "ocid1.vaultsecret.oc1.iad.amaaaaaac3adhhqa5kpgmwnsmkqn2qi7nhzisbjikjtsxrdx725gxh7rug2q";

        dbPassword = new String(getSecret(dbPasswordOcid));

	System.out.println("SECRET DECODED64: " + dbPassword);
    } 

    public static byte[] getSecret(String secretOcid) {
        GetSecretBundleRequest getSecretBundleRequest = GetSecretBundleRequest
                .builder()
                .secretId(secretOcid)
                .stage(GetSecretBundleRequest.Stage.Current)
                .build();

        GetSecretBundleResponse getSecretBundleResponse = secretsClient
                .getSecretBundle(getSecretBundleRequest);

        Base64SecretBundleContentDetails base64SecretBundleContentDetails =
                (Base64SecretBundleContentDetails) getSecretBundleResponse.
                        getSecretBundle().getSecretBundleContent();

        byte[] secretValueDecoded = Base64.decodeBase64(base64SecretBundleContentDetails.getContent());

        return secretValueDecoded;
    }
}

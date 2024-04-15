package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * This EndPoint provides SSL sockets for http protocol.  This extends the ServerSocket to create SSLSockets for http.
 *
 * <table class="inner">
 * <tr class="header"><td>Parameter Name</td><td>Explanation</td><td>Default Value</td><td>Required</td></tr>
 * <tr class="row"><td>keystore</td><td>The keystore of the certificates used by this socket.  This contains both the certificate used to authenticate the server as well as authenticate clients.</td><td>None</td><td>Yes</td></tr>
 * <tr class="altrow"><td>storepass</td><td>Password for the keystore.</td><td>None</td><td>Yes</td></tr>
 * <tr class="row"><td>keypass</td><td>Password for the key with the given alias.</td><td>Defaults to storepass</td><td>No, but its a good idea</td></tr>
 * <tr class="altrow"><td>alias</td><td>The name of the certificate in the keystore used for server authentication.</td><td>sslkey</td><td>No</td></tr>
 * <tr class="row"><td>ciphers</td><td>A comma seperated list of cipher suites to use to encrypt the SSL socket.</td><td></td><td>No</td></tr>
 * <tr class="atlrow"><td>protocols</td><td>A comma seperated list of protocols to use in negotiating the SSL socket.</td><td></td><td>No</td></tr>
 * <tr class="row"><td>clientauth</td><td>Setting this to true will require clients authenticate to the server.</td><td>false</td><td>No</td></tr>
 * </table>
 */
@Slf4j
public class SSLServerSocketEndPoint extends ServerSocketEndPoint {

    private static final ConfigOption KEYSTORE_OPTION = new ConfigOption("keystore", true, "The keystore used by the SSL server.");
    private static final ConfigOption STOREPASS_OPTION = new ConfigOption("storepass", true, "The keystore password.");
    private static final ConfigOption KEYPASS_OPTION = new ConfigOption("keypass", false, "The password for the key in the keystore.");
    private static final ConfigOption ALIAS_OPTION = new ConfigOption("alias", "sslkey", "The alias to the key used by the SSL server.");
    private static final ConfigOption CIPHERS_OPTION = new ConfigOption("ciphers", false, "Comma seperated list of ciphers to use for the SSL server.");
    private static final ConfigOption PROTOCOLS_OPTION = new ConfigOption("protocols", false, "Comma seperated list of protocols for SSL server.");
    private static final ConfigOption CLIENT_AUTH_OPTION = new ConfigOption("clientauth", "false", "Require client authentication during SSL handshake.");


    public void initialize(String name, Server server) throws IOException {
        super.initialize(name, server);
        try {
            File keystoreFile = new File(KEYSTORE_OPTION.getProperty(server, endpointName));
            String storepass = STOREPASS_OPTION.getProperty(server, endpointName);
            String keypass = KEYPASS_OPTION.getProperty(server, endpointName);
            keypass = keypass == null ? storepass : keypass;
            KeyStore keystore = loadKeystoreFromFile(keystoreFile, storepass.toCharArray());

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(getKeyManagers(keystore, keypass.toCharArray(), ALIAS_OPTION.getProperty(server, endpointName)), getTrustManagers(keystore), null);
            factory = context.getServerSocketFactory();
        } catch (GeneralSecurityException e) {
            log.error("Security Exception while initializing. {}", e.getMessage());
            throw (IOException) new IOException().initCause(e);
        }
    }

    protected String getProtocol() {
        return "https";
    }

    protected ServerSocket createSocket(int port) throws IOException {
        ServerSocket serverSocket = super.createSocket(port);
        String cipherSuites = CIPHERS_OPTION.getProperty(server, getName());
        if (cipherSuites != null) {
            ((SSLServerSocket) serverSocket).setEnabledCipherSuites(cipherSuites.split(","));
        }

        String protocols = PROTOCOLS_OPTION.getProperty(server, getName());
        if (protocols != null) {
            ((SSLServerSocket) serverSocket).setEnabledProtocols(protocols.split(","));
        }

        boolean clientAuth = CLIENT_AUTH_OPTION.getBoolean(server, getName()).booleanValue();
        if (clientAuth) {
            ((SSLServerSocket) serverSocket).setNeedClientAuth(true);
        }
        return serverSocket;
    }

    private KeyStore loadKeystoreFromFile(File file, char[] password) throws IOException, GeneralSecurityException {
        KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream stream = new FileInputStream(file);
        keystore.load(stream, password);
        stream.close();

        return keystore;
    }

    private TrustManager[] getTrustManagers(KeyStore keystore) throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509");
        factory.init(keystore);

        return factory.getTrustManagers();
    }

    private KeyManager[] getKeyManagers(KeyStore keystore, char[] pwd, String alias) throws GeneralSecurityException {
        KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
        factory.init(keystore, pwd);
        KeyManager[] kms = factory.getKeyManagers();
        if (alias != null) {
            for (int i = 0; i < kms.length; i++) {
                if (kms[i] instanceof X509KeyManager)
                    kms[i] = new AliasForcingKeyManager((X509KeyManager) kms[i], alias);
            }
        }

        return kms;
    }

    private class AliasForcingKeyManager implements X509KeyManager {
        X509KeyManager baseKM = null;
        String alias = null;

        public AliasForcingKeyManager(X509KeyManager keyManager, String alias) {
            baseKM = keyManager;
            this.alias = alias;
        }

        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return baseKM.chooseClientAlias(keyType, issuers, socket);
        }

        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            String[] validAliases = baseKM.getServerAliases(keyType, issuers);
            if (validAliases != null) {
                for (int j = 0; j < validAliases.length; j++) {
                    if (validAliases[j].equals(alias)) return alias;
                }
            }
            return baseKM.chooseServerAlias(keyType, issuers, socket);  // use default if we can't find the alias.
        }

        public X509Certificate[] getCertificateChain(String alias) {
            return baseKM.getCertificateChain(alias);
        }

        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return baseKM.getClientAliases(keyType, issuers);
        }

        public PrivateKey getPrivateKey(String alias) {
            return baseKM.getPrivateKey(alias);
        }

        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return baseKM.getServerAliases(keyType, issuers);
        }
    }
}

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
 * This EndPoint provides SSL sockets for http protocol.  This extends the ServerSocket to create SSLSockets for https.
 */
@Slf4j
public class SSLServerSocketEndPoint extends ServerSocketEndPoint {

    private KeyStore keystore;
    private String storepass;
    private String keypass;
    private String alias;
    private String[] cipherSuites = null;
    private String[] protocols = null;
    private boolean clientAuth = false;

    public SSLServerSocketEndPoint(File keystoreFile, String alias, String storepass) throws GeneralSecurityException, IOException {
        super(143);
        this.alias = alias;
        this.storepass = storepass;
        this.keystore = loadKeystoreFromFile(keystoreFile, storepass.toCharArray());
    }

    public SSLServerSocketEndPoint(KeyStore keystore, String alias, String storepass) {
        super(143);
        this.keystore = keystore;
        this.alias = alias;
        this.storepass = storepass;
    }

    public SSLServerSocketEndPoint(KeyStore keystore, String alias, String storepass, String keypass) {
        super(143);
        this.keystore = keystore;
        this.alias = alias;
        this.storepass = storepass;
        this.keypass = keypass;
    }

    public void start(Server server) throws IOException {
        super.start(server);
        try {
            keypass = keypass == null ? storepass : keypass;
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(getKeyManagers(keystore, keypass.toCharArray(), alias), getTrustManagers(keystore), null);
            factory = context.getServerSocketFactory();
        } catch (GeneralSecurityException e) {
            log.error("Security Exception while initializing.", e);
            throw (IOException) new IOException().initCause(e);
        }

    }

    protected String getProtocol() {
        return "https";
    }

    protected ServerSocket createSocket(int port) throws IOException {
        ServerSocket serverSocket = super.createSocket(port);
        if (cipherSuites != null) {
            ((SSLServerSocket) serverSocket).setEnabledCipherSuites(cipherSuites);
        }

        if (protocols != null) {
            ((SSLServerSocket) serverSocket).setEnabledProtocols(protocols);
        }

        ((SSLServerSocket) serverSocket).setNeedClientAuth(clientAuth);
        return serverSocket;
    }

    private KeyStore loadKeystoreFromFile(File file, char[] password) throws IOException, GeneralSecurityException {
        InputStream stream = new FileInputStream(file);
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(stream, password);
            return keystore;
        } finally {
            stream.close();
        }
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

    public SSLServerSocketEndPoint protocols(String[] protocols) {
        this.protocols = protocols;
        return this;
    }

    public SSLServerSocketEndPoint cipherSuites(String[] suites) {
        cipherSuites = suites;
        return this;
    }

    public SSLServerSocketEndPoint clientAuth(boolean auth) {
        clientAuth = auth;
        return this;
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

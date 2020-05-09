package de.aaschmid.taskwarrior.client;

import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.util.io.pem.PemObject;
import de.aaschmid.taskwarrior.thirdparty.org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public class MySslContextFactory {

    private static final String DEFAULT_PROTOCOL = "TLS";
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String PEM_TYPE_PKCS1 = "RSA PRIVATE KEY";
    private static final String PEM_TYPE_PKCS8 = "PRIVATE KEY";
    private static final String KEY_ALGORITHM_RSA = "RSA";

    public static SSLContext createSslContext(
            String caCert, String privateKeyCert, String privateKey) {

        final String keystorePassword = UUID.randomUUID().toString();
        final KeyStore keyStore = createKeyStore(caCert, privateKeyCert, privateKey, keystorePassword);

        return createSslContext(DEFAULT_PROTOCOL, keyStore, keystorePassword);
    }

    private static KeyStore createKeyStore(
            String caCert, String privateKeyCert, String privateKey, String keystorePassword) {

        final PasswordProtection keyStoreProtection = new PasswordProtection(keystorePassword.toCharArray());

        KeyStore result;
        try {
            result = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, keyStoreProtection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not build keystore: %s", e.getMessage());
        }

        AtomicInteger idx = new AtomicInteger(0);
        createCertificatesFor(caCert).forEach(c -> {
            try {
                result.setCertificateEntry("ca_" + idx.getAndIncrement(), c);
            } catch (KeyStoreException e) {
                throw new TaskwarriorKeyStoreException(e, "Could not add CA certificate 'caCert' to keystore.");
            }
        });

        Certificate[] privateKeyCertsChain = createCertificatesFor(privateKeyCert).toArray(new Certificate[0]);
        PrivateKey privateKeyObject = createPrivateKeyFor(privateKey);
        try {
            result.setEntry("key", new KeyStore.PrivateKeyEntry(privateKeyObject, privateKeyCertsChain), keyStoreProtection);
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not create private cert 'privateKeyCert' and key 'privateKey' to keystore.");
        }

        return result;
    }

    private static List<Certificate> createCertificatesFor(String certFile) {
        List<Certificate> result = new ArrayList<>();
        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(certFile.getBytes()))) {
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            while (bis.available() > 0) {
                result.add(cf.generateCertificate(bis));
            }
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read certificates of 'certFile' via input stream.");
        } catch (CertificateException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not generate certificates for 'certFile'.");
        }
        return result;
    }

    private static PrivateKey createPrivateKeyFor(String privateKey) {
        try {
            byte[] bytes = privateKey.getBytes();

            PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
            PemObject privateKeyObject = pemReader.readPemObject();

            switch (privateKeyObject.getType()) {
                case PEM_TYPE_PKCS1:
                    return createPrivateKeyForPkcs1(privateKeyObject.getContent());
                case PEM_TYPE_PKCS8:
                    return createPrivateKeyForPkcs8(privateKeyObject.getContent());
                default:
                    throw new TaskwarriorKeyStoreException("Unsupported key algorithm '%s'.", privateKeyObject.getType());
            }
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read private key of 'privateKey' via input stream.");
        }
    }

    private static PrivateKey createPrivateKeyForPkcs1(byte[] privateKeyBytes) {
        RSAPrivateKey rsa = RSAPrivateKey.getInstance(privateKeyBytes);
        RSAPrivateCrtKeyParameters keyParameters = new RSAPrivateCrtKeyParameters(
                rsa.getModulus(),
                rsa.getPublicExponent(),
                rsa.getPrivateExponent(),
                rsa.getPrime1(),
                rsa.getPrime2(),
                rsa.getExponent1(),
                rsa.getExponent2(),
                rsa.getCoefficient());

        try {
            return new JcaPEMKeyConverter().getPrivateKey(PrivateKeyInfoFactory.createPrivateKeyInfo(keyParameters));
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Failed to encode PKCS#1 private key of 'privateKey'.");
        }
    }

    private static PrivateKey createPrivateKeyForPkcs8(byte[] privateKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorKeyStoreException(e, "Key factory could not be initialized for algorithm '%s'.", KEY_ALGORITHM_RSA);
        } catch (InvalidKeySpecException e) {
            throw new TaskwarriorKeyStoreException(e, "Invalid key spec for %s private key in 'privateKey'.", KEY_ALGORITHM_RSA);
        }
    }

    private static SSLContext createSslContext(String protocol, KeyStore keyStore, String keyStorePassword) {
        requireNonNull(protocol, "'protocol' must not be null.");
        requireNonNull(keyStore, "'keyStore' must not be null.");
        requireNonNull(keyStorePassword, "'keyStorePassword' must not be null.");

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(protocol);
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorSslContextException(e, "Cannot create SSL context for protocol '%s'.", protocol);
        }
        try {
            sslContext.init(loadKeyMaterial(keyStore, keyStorePassword), loadTrustMaterial(keyStore), null);
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new TaskwarriorSslContextException(e, "Could not init ssl context: %s", e.getMessage());
        }
        return sslContext;
    }

    private static KeyManager[] loadKeyMaterial(KeyStore keystore, String keyStorePassword)
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory result = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        result.init(keystore, keyStorePassword.toCharArray());
        return result.getKeyManagers();
    }

    private static TrustManager[] loadTrustMaterial(KeyStore truststore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory result = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        result.init(truststore);
        return result.getTrustManagers();
    }
}

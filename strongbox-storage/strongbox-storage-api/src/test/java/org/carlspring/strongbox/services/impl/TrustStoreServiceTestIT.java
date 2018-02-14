package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.certificates.KeyStoreManager;
import org.carlspring.strongbox.services.TrustStoreService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StorageApiTestConfig.class)
public class TrustStoreServiceTestIT
{

    @Inject
    private TrustStoreService trustStoreService;

    private Resource trustStore;

    @Inject
    private KeyStoreManager keyStoreManager;

    private InetAddress inetAddress;

    @Before
    public void before()
            throws Exception
    {
        inetAddress = InetAddress.getByName("repository.apache.org");
        trustStore = getTrustStoreResource();
        keyStoreManager.removeCertificates(trustStore.getFile(), "password".toCharArray(), inetAddress, 443);
    }

    @Test
    public void shouldAddSslCertificatesToTrustStore()
            throws Exception
    {
        Assert.assertFalse(keyStoreManager.listCertificates(trustStore.getFile(),
                                                            "password".toCharArray()).keySet().stream().filter(
                name -> name.contains("*.apache.org")).findAny().isPresent());

        trustStoreService.addSslCertificatesToTrustStore("https://repository.apache.org/snapshots/");

        Assert.assertTrue(keyStoreManager.listCertificates(trustStore.getFile(),
                                                           "password".toCharArray()).keySet().stream().filter(
                name -> name.contains("*.apache.org")).findAny().isPresent());
    }

    private Resource getTrustStoreResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.truststore.jks",
                                                                      "etc/ssl/truststore.jks");
    }

}
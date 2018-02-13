package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Przemyslaw Fusik
 */
public class BaseMavenGroupRepositoryComponentTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected static final String REPOSITORY_LEAF_E = "leaf-repo-e";

    protected static final String REPOSITORY_LEAF_L = "leaf-repo-l";

    protected static final String REPOSITORY_LEAF_Z = "leaf-repo-Z";

    protected static final String REPOSITORY_LEAF_D = "leaf-repo-d";

    protected static final String REPOSITORY_LEAF_G = "leaf-repo-g";

    protected static final String REPOSITORY_LEAF_K = "leaf-repo-k";

    protected static final File REPOSITORY_LEAF_L_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_LEAF_L);

    protected static final File REPOSITORY_LEAF_D_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_LEAF_D);

    protected static final File REPOSITORY_LEAF_G_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_LEAF_G);

    protected static final File REPOSITORY_LEAF_K_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_LEAF_K);

    protected static final String REPOSITORY_GROUP_A = "group-repo-a";

    protected static final String REPOSITORY_GROUP_B = "group-repo-b";

    protected static final String REPOSITORY_GROUP_C = "group-repo-c";

    protected static final String REPOSITORY_GROUP_F = "group-repo-f";

    protected static final String REPOSITORY_GROUP_H = "group-repo-h";

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_E));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_Z));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_G));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_C));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H));

        return repositories;
    }

    @Override
    public void createRepository(final Repository repository)
            throws RepositoryManagementStrategyException, JAXBException, IOException
    {
        MavenRepositoryConfiguration configuration = new MavenRepositoryConfiguration();
        configuration.setIndexingEnabled(true);

        repository.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setAllowsForceDeletion(true);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setRepositoryConfiguration(configuration);

        super.createRepository(repository);
    }

    protected void createLeaf(String repositoryId)
            throws Exception
    {
        Repository repository = new Repository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(new Random().nextInt(2) % 2 == 0 ?
                           RepositoryTypeEnum.HOSTED.getType() :
                           RepositoryTypeEnum.PROXY.getType());

        createRepository(repository);
    }

    protected void createGroup(String repositoryId,
                               String... leafs)
            throws Exception
    {
        Repository repository = new Repository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.GROUP.getType());
        repository.setGroupRepositories(new HashSet<>(Arrays.asList(leafs)));

        createRepository(repository);
    }

    @Before
    public void initialize()
            throws Exception
    {
        createLeaf(REPOSITORY_LEAF_E);
        createLeaf(REPOSITORY_LEAF_L);
        createLeaf(REPOSITORY_LEAF_Z);
        createLeaf(REPOSITORY_LEAF_D);
        createLeaf(REPOSITORY_LEAF_G);
        createLeaf(REPOSITORY_LEAF_K);

        createGroup(REPOSITORY_GROUP_C, REPOSITORY_LEAF_E, REPOSITORY_LEAF_Z);
        createGroup(REPOSITORY_GROUP_B, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_A, REPOSITORY_LEAF_G, REPOSITORY_GROUP_B);
        createGroup(REPOSITORY_GROUP_F, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_H, REPOSITORY_GROUP_F, REPOSITORY_LEAF_K);

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(REPOSITORY_LEAF_L_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(REPOSITORY_LEAF_G_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_D_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_K_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_L);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_G);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_D);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_K);

        /**
         <denied>
         <rule-set group-repository="group-repo-h">
         <rule pattern=".*(com|org)/artifacts/to/update/releases/update-group.*">
         <repositories>
         <repository>leaf-repo-d</repository>
         </repositories>
         </rule>
         </rule-set>
         </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             REPOSITORY_GROUP_H,
                             new String[]{ REPOSITORY_LEAF_D },
                             ".*(com|org)/artifacts/to/update/releases/update-group.*",
                             ROUTING_RULE_TYPE_DENIED);

        postInitializeInternally();
    }

    protected void postInitializeInternally()
            throws IOException
    {
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        Set<Repository> repositories = getRepositoriesToClean();
        addRepositoriesToClean(repositories);

        for (Repository repository : repositories)
        {
            getRepositoryIndexManager().closeIndexersForRepository(repository.getStorage().getId(), repository.getId());
        }

        removeRepositories(repositories);
    }

    protected void addRepositoriesToClean(final Set<Repository> repositories)
    {
    }

}

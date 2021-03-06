package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;

import org.springframework.stereotype.Component;

/**
 * @author stodorov
 */
@Component("MavenSnapshotVersionValidator")
public class MavenSnapshotVersionValidator
        implements MavenVersionValidator
{

    @Override
    public boolean supports(Repository repository)
    {
        return MavenVersionValidator.super.supports(repository) &&
               repository.getVersionValidators().contains(VersionValidatorType.SNAPSHOT);
    }

    /**
     * Matches versions:
     * 1.0-20131004
     * 1.0-20131004.115330
     * 1.0-20131004.115330-1
     * 1.0.8-20151025.032208-1
     * 1.0.8-alpha-1-20151025.032208-1
     */
    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isSnapshot(version) && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a SNAPSHOT artifact to a repository with a release policy!");
        }
        if (!isSnapshot(version) && repository.acceptsSnapshots() && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository with a SNAPSHOT policy!");
        }
    }

    public boolean isSnapshot(String version)
    {
        return version != null && ArtifactUtils.isSnapshot(version);
    }

}

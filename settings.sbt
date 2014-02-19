import SimpleSettings._

primarySettings := primary(
    name             = "tools"
  , companyName      = "Guidewire"
  , organization     = "com.guidewire.tools"
  , homepage         = "https://github.com/Guidewire/marathon-client"
  , vcsSpecification = "git@github.com:Guidewire/marathon-client.git"
)

mavenSettings := maven(
  license(
      name  = "The Apache Software License, Version 2.0"
    , url   = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  ),
  developer(
      id              = "David Hoyt"
    , name            = "David Hoyt"
    , email           = "dhoyt@guidewire.com"
    , url             = "http://www.hoytsoft.org/"
    , organization    = "Guidewire"
    , organizationUri = "http://www.guidewire.com/"
    , roles           = Seq("architect", "developer")
  ),
  developer(
      id              = "Olga Tikhonova"
    , name            = "Olga Tikhonova"
    , email           = "otikhonova@guidewire.com"
    , url             = "http://www.guidewire.com/"
    , organization    = "Guidewire"
    , organizationUri = "http://www.guidewire.com/"
    , roles           = Seq("architect", "developer")
  )
)

publishSettings := publishing(
    releaseCredentialsID  = "sonatype-nexus-staging"
  , releaseRepository     = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  , snapshotCredentialsID = "sonatype-nexus-snapshots"
  , snapshotRepository    = "https://oss.sonatype.org/content/repositories/snapshots"
  , signArtifacts         = true
)

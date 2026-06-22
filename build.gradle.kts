import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.qvsorrow"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    jvmToolchain(17)
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    api("com.squareup.okio:okio:3.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    // automaticRelease = false: the upload lands as a *pending* deployment in the
    // Central Portal. You then inspect it and press "Publish" manually in the UI.
    // Central releases are immutable (no delete/overwrite), so manual review on the
    // first release is the safe default. Flip to true once the pipeline is trusted.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)

    // Sign only when a GPG key is configured. This keeps `publishToMavenLocal`
    // working without GPG (for local consumer testing), while the real Central
    // release - run with signingInMemoryKey set in ~/.gradle/gradle.properties -
    // is still signed (Central requires signatures).
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates("io.github.qvsorrow", "binkode", "0.1.0")

    pom {
        name.set("binkode")
        description.set("bincode (de)serializer for kotlinx.serialization, backed by okio")
        url.set("https://github.com/QVSorrow/binkode")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("qvsorrow")
                name.set("Oles Nykytiuk")
            }
        }
        scm {
            url.set("https://github.com/QVSorrow/binkode")
            connection.set("scm:git:git://github.com/QVSorrow/binkode.git")
            developerConnection.set("scm:git:ssh://git@github.com/QVSorrow/binkode.git")
        }
    }
}

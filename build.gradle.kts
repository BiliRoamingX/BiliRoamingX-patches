import org.gradle.kotlin.dsl.support.listFilesOrdered
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    `maven-publish`
    signing
}

group = "app.revanced.bilibili"

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven { url = uri("https://jitpack.io") }
    maven {
        // A repository must be speficied for some reason. "registry" is a dummy.
        url = uri("https://maven.pkg.github.com/zjns/registry")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    // Used in JsonGenerator.
    implementation(libs.gson)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType(Jar::class) {
        exclude("app/revanced/generator")

        manifest {
            attributes["Name"] = "BiliRoamingX Patches"
            attributes["Description"] = "Patches for BiliRoamingX."
            attributes["Version"] = version
            attributes["Timestamp"] = System.currentTimeMillis().toString()
            attributes["Source"] = "git@github.com:zjns/BiliRoamingX-patches.git"
            attributes["Author"] = "Kofua"
            attributes["License"] = "GNU General Public License v3.0"
        }
    }

    register("buildDexJar") {
        description = "Build and add a DEX to the JAR file"
        group = "build"

        dependsOn(build)

        doLast {
            val d8 = File(System.getenv("ANDROID_HOME")).resolve("build-tools")
                .listFilesOrdered().last().resolve("d8").absolutePath

            val patchesJar = configurations.archives.get().allArtifacts.files.files.first().absolutePath
            val workingDirectory = layout.buildDirectory.dir("libs").get().asFile

            exec {
                workingDir = workingDirectory
                commandLine = listOf(d8, "--release", patchesJar)
            }

            exec {
                workingDir = workingDirectory
                commandLine = listOf("zip", "-u", patchesJar, "classes.dex")
            }
        }
    }

    register<JavaExec>("generatePatchesFiles") {
        description = "Generate patches files"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.generator.Main")
    }

    // Needed by gradle-semantic-release-plugin.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    publish {
        dependsOn("buildDexJar")
        dependsOn("generatePatchesFiles")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/zjns/BiliRoamingX-patches")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("revanced-patches-publication") {
            from(components["java"])

            pom {
                name = "BiliRoamingX Patches"
                description = "Patches for BiliRoamingX."

                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "zjns"
                        name = "Kofua"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/zjns/BiliRoamingX-patches.git"
                    developerConnection = "scm:git:git@github.com:zjns/BiliRoamingX-patches.git"
                    url = "https://github.com/zjns/BiliRoamingX-patches"
                }
            }
        }
    }
}

signing {
    useGpgCmd()

    sign(publishing.publications["revanced-patches-publication"])
}

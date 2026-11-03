plugins {
    id("java-library")
    id("maven-publish")
}

val mod_id: String = project.property("mod_id") as String
val mod_name: String = project.property("mod_name") as String
val mod_author: String = project.property("mod_author") as String
val minecraft_version: String = project.property("minecraft_version") as String
val minecraft_version_range: String = project.property("minecraft_version_range") as String
val fabric_version: String = project.property("fabric_version") as String
val fabric_loader_version: String = project.property("fabric_loader_version") as String
val license: String = project.property("license") as String
val mod_description: String = project.property("mod_description") as String
val credits: String = project.property("credits") as String
val neoforge_version: String = project.property("neoforge_version") as String
val neoforge_loader_version_range: String = project.property("neoforge_loader_version_range") as String
val java_version: Int = project.property("java_version").toString().toInt()

base {
    archivesName = "${mod_id}-${project.name}-${minecraft_version}"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    
    exclusiveContent {
        forRepositories(
                maven {
                    name = "ParchmentMC"
                    url = uri("https://maven.parchmentmc.org/")
                },
                maven {
                    name = "NeoForge"
                    url = uri("https://maven.neoforged.net/releases")
                }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }

    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
}

listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.getByName(variant).outgoing {
        capability("$group:${base.archivesName.get()}:$version")
        capability("$group:${mod_id}-${project.name}-${minecraft_version}:$version")
        capability("$group:${mod_id}:$version")
    }
    
    publishing.publications.configureEach {
        if (this is MavenPublication) {
            suppressPomMetadataWarningsFor(variant)
        }
    }
}

tasks.named<Jar>("sourcesJar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${mod_name}" }
    }
}

tasks.named<Jar>("jar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${mod_name}" }
    }

    manifest {
        attributes(
            mapOf(
                "Specification-Title"    to mod_name,
                "Specification-Vendor"   to mod_author,
                "Specification-Version"  to archiveVersion.get(),
                "Implementation-Title"   to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor"  to mod_author,
                "Built-On-Minecraft"     to minecraft_version
            )
        )
    }
}

tasks.processResources {
    val expandProps = mapOf(
        "version"                       to version,
        "group"                         to project.group,
        "minecraft_version"             to minecraft_version,
        "minecraft_version_range"       to minecraft_version_range,
        "fabric_version"                to fabric_version,
        "fabric_loader_version"         to fabric_loader_version,
        "mod_name"                      to mod_name,
        "mod_author"                    to mod_author,
        "mod_id"                        to mod_id,
        "license"                       to license,
        "description"                   to mod_description,
        "neoforge_version"              to neoforge_version,
        "neoforge_loader_version_range" to neoforge_loader_version_range,
        "credits"                       to credits,
        "java_version"                  to java_version
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }

    inputs.properties(expandProps)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"]) 
        }
    }
    
    repositories {
        val localMavenUrl = System.getenv("local_maven_url")

        if (localMavenUrl != null) {
            maven {
                url = uri(localMavenUrl)
            }
        }
    }
}

plugins {
    id("multiloader-loader")
    id("fabric-loom")
}

val minecraft_version: String by project
val parchment_minecraft: String by project
val parchment_version: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val mod_id: String by project

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchment_minecraft:$parchment_version@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
}

loom {
    val aw = project(":common").file("src/main/resources/$mod_id.accesswidener")

    if (aw.exists()) {
        accessWidenerPath = aw
    }

    mixin {
        defaultRefmapName.set("$mod_id.refmap.json")
    }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir = "runs/client"
        }
    
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir = "runs/server"
        }
    }
}

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

val neoforge_version: String by project
val parchment_minecraft: String by project
val parchment_version: String by project
val mod_id: String by project

neoForge {
    version = neoforge_version
    
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    
    parchment {
        minecraftVersion = parchment_minecraft
        mappingsVersion = parchment_version
    }
    
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            ideName = "NeoForge ${name.replaceFirstChar { it.uppercase() }} (${project.path})" 
        }
        
        create("client") {
            client()
        }
        
        create("data") {
            data()

            programArguments.addAll(
                "--mod", mod_id, 
                "--all", 
                "--output", file("src/generated/resources/").absolutePath, 
                "--existing", file("src/main/resources/").absolutePath
            )
        }
        
        create("server") {
            server()
        }
    }
    
    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

plugins {
    java
    `maven-publish`
}

group = "org.eclipse.mat"
version = "1.17.0-standalone"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf(
                "plugins/org.eclipse.mat.report/src",
                "plugins/org.eclipse.mat.api/src",
                "plugins/org.eclipse.mat.parser/src",
                "plugins/org.eclipse.mat.hprof/src"
            ))
            // 排除 UI / acquire / export 等不需要的源码
            exclude(
                "**/hprof/ui/HPROFPreferencePage.java",
                "**/hprof/ui/PreferenceInitializer.java",
                "**/hprof/acquire/**",
                "**/hprof/describer/HprofContentDescriber.java",
                "**/hprof/describer/HprofGZIPContentDescriber.java",
                "**/hprof/ExportHprof.java",
                "**/internal/apps/**",
                "**/internal/acquire/**"
            )
        }
        // .properties 文件（NLS 消息）也在 src 目录里，需要作为 resources 打包
        resources {
            setSrcDirs(listOf(
                "plugins/org.eclipse.mat.report/src",
                "plugins/org.eclipse.mat.api/src",
                "plugins/org.eclipse.mat.parser/src",
                "plugins/org.eclipse.mat.hprof/src"
            ))
            include("**/*.properties")
        }
    }
}

dependencies {
    // 零外部依赖 - 纯 Java API
}

tasks.jar {
    archiveBaseName.set("mat-standalone")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.eclipse.mat"
            artifactId = "mat-standalone"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

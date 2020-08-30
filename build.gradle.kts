val springBootVersion = "2.2.6.RELEASE"
val springVersion = "5.2.5.RELEASE"
val h2Version = "1.4.200"

plugins {
	`java-library`
	id("java")
}

group = "org.pensatocode.simplicity"
version = "2.0.2"
description = "A tiny framework for building fast and reliable RESTful APIs in Java."
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	jcenter()
	mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework:spring-web:${springVersion}")
    implementation("org.springframework:spring-beans:${springVersion}")
    implementation("org.springframework:spring-context:${springVersion}")

    api("org.springframework:spring-jdbc:${springVersion}")
    api("org.springframework.data:spring-data-commons:${springBootVersion}")

    // Database
//    implementation("postgresql:postgresql:9.1-901-1.jdbc4")
    implementation("com.h2database:h2:${h2Version}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springBootVersion}") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// https://guides.gradle.org/building-kotlin-jvm-libraries/
tasks {
    jar {
        manifest {
            attributes(
                    mapOf("Implementation-Title" to project.name,
                            "Implementation-Version" to project.version)
            )
        }
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }
}
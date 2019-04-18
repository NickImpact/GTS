/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.nickimpact.gts.api.dependencies;

import com.google.common.collect.ImmutableList;
import me.nickimpact.gts.api.dependencies.relocation.Relocation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

public enum Dependency {

	ASM(
			"org.ow2.asm",
			"asm",
			"6.2.1",
			"FGDbbDPMmchOXLMORrAX5NHMmn+8F0EB1vhIKbtkwIU="
	),
	ASM_COMMONS(
			"org.ow2.asm",
			"asm-commons",
			"6.2.1",
			"P1eNMe8w+UttH0SBL0H+T5inzUKvNTNfXUhmqzuQGGU="
	),
	JAR_RELOCATOR(
			"me.lucko",
			"jar-relocator",
			"1.3",
			"mmz3ltQbS8xXGA2scM0ZH6raISlt4nukjCiU2l9Jxfs="
	),

	SLF4J_SIMPLE(
			"org.slf4j",
			"slf4j-simple",
			"1.7.25",
			"CWbob/+lvlLT2ee4ndZ02YoD7tCkVPuvfBvZSTvZ2HQ="
	),
	SLF4J_API(
			"org.slf4j",
			"slf4j-api",
			"1.7.25",
			"GMSgCV1cHaa4F1kudnuyPSndL1YK1033X/OWHb3iW3k="
	),
	H2_DRIVER(
			"com.h2database",
			"h2",
			"1.4.198",
			"Mt1rFJy3IqpMLdTUCnSpzUHjKsWaTnVaZuV1NmDWHUY="
			// we don't apply relocations to h2 - it gets loaded via
			// an isolated classloader
	),
	HIKARI(
			"com{}zaxxer",
			"HikariCP",
			"3.3.1",
			"SIaA1yzGHOZNpZNoIt903f5ScJrIB3u8CT2cNkaLcy0=",
			Relocation.of("hikari", "com{}zaxxer{}hikari")
	),
	MARIADB_DRIVER(
			"org{}mariadb{}jdbc",
			"mariadb-java-client",
			"2.4.0",
			"G346tblA35aJS8q1a3dQVZdU7Q7isGMzhwftoz6MZqU=",
			Relocation.of("mariadb", "org{}mariadb{}jdbc")
	),
	MYSQL_DRIVER(
			"mysql",
			"mysql-connector-java",
			"5.1.47",
			"5PhASPOSsrN7r0ao1QjkuN2uKG0gnvmVueEYhSAcGSM=",
			Relocation.of("mysql", "com{}mysql")
	),
	MONGODB_DRIVER(
			"org.mongodb",
			"mongo-java-driver",
			"3.10.1",
			"IGjdjTH4VjqnqGUdVe8u+dKfzKkpCG1NR11TE8ieCdU=",
			Relocation.of("mongodb", "com{}mongodb"),
			Relocation.of("bson", "org{}bson")
	),
	CONFIGURATE_YAML(
			"me{}lucko{}configurate",
			"configurate-yaml",
			"3.5",
			"Dxr1o3EPbpOOmwraqu+cors8O/nKwJnhS5EiPkTb3fc=",
			Relocation.of("configurate", "ninja{}leaping{}configurate")
	),
	CONFIGURATE_HOCON(
			"me{}lucko{}configurate",
			"configurate-hocon",
			"3.5",
			"sOym1KPmQylGSfk90ZFqobuvoZfEWb7XMmMBwbHuxFw=",
			Relocation.of("configurate", "ninja{}leaping{}configurate"),
			Relocation.of("hocon", "com{}typesafe{}config")
	),
	HOCON_CONFIG(
			"com{}typesafe",
			"config",
			"1.3.3",
			"tfHWBx8VSNBb6C9Z+QOcfTeheHvY48Z34x7ida9KRiE=",
			Relocation.of("hocon", "com{}typesafe{}config")
	),
	CONFIGURATE_GSON(
			"me{}lucko{}configurate",
			"configurate-gson",
			"3.5",
			"Q3wp3xpqy41bJW3yUhbHOzm+NUkT4bUUBI2/AQLaa3c=",
			Relocation.of("configurate", "ninja{}leaping{}configurate")
	),
	CONFIGURATE_CORE(
			"me{}lucko{}configurate",
			"configurate-core",
			"3.5",
			"J+1WnX1g5gr4ne8qA7DuBadLDOsZnOZjwHbdRmVgF6c=",
			Relocation.of("configurate", "ninja{}leaping{}configurate")
	);

	private final List<URL> urls;
	private final String version;
	private final byte[] checksum;
	private final List<Relocation> relocations;

	private static final String MAVEN_CENTRAL_REPO = "https://repo1.maven.org/maven2/";
	private static final String LUCK_MIRROR_REPO = "https://nexus.lucko.me/repository/maven-central/";
	private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

	Dependency(String groupID, String artifactID, String version, String checksum, Relocation... relocations) {
		String path = String.format(MAVEN_FORMAT,
				rewriteEscaping(groupID).replace(".", "/"),
				rewriteEscaping(artifactID),
				version,
				rewriteEscaping(artifactID),
				version
		);

		try {
			this.urls = ImmutableList.of(
					new URL(LUCK_MIRROR_REPO + path),
					new URL(MAVEN_CENTRAL_REPO + path)
			);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e); // propagate
		}
		this.version = version;
		this.checksum = Base64.getDecoder().decode(checksum);
		this.relocations = ImmutableList.copyOf(relocations);
	}

	private static String rewriteEscaping(String s) {
		return s.replace("{}", ".");
	}

	public List<URL> getUrls() {
		return this.urls;
	}

	public String getVersion() {
		return this.version;
	}

	public byte[] getChecksum() {
		return this.checksum;
	}

	public List<Relocation> getRelocations() {
		return this.relocations;
	}
}

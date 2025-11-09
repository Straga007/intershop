package com.tomacat.sesecuritykeylock;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {

	private static Collection<GrantedAuthority> getAuthorities(Jwt jwt) {
		Map<String, List<String>> access = jwt.getClaim("access");
		List<GrantedAuthority> authorities = new ArrayList<>();

		for (Map.Entry<String, List<String>> accessEntry : access.entrySet()) {
			String key = accessEntry.getKey();
			accessEntry.getValue().stream()
					.map(it -> key + "_" + it)
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toCollection(() -> authorities));
		}
		return List.copyOf(authorities);
	}

	public static void main(String[] args) {
		// Пример готового токена.
		Jwt jwt = Jwt.withTokenValue("none")
				.header("alg", "none")
				.claim("iat", Instant.ofEpochSecond(1735678800))
				.claim("exp", Instant.ofEpochSecond(1735679100))
				.claim("access", Map.of(
						"root", List.of("user", "simple_user", "read_access"),
						"audit", List.of("read", "write"),
						"mail", List.of("read", "send"),
						"documents", List.of("read")
				))
				.build();

		Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = Application::getAuthorities;
		Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
		String joinedAuthorities = authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.sorted()
				.collect(Collectors.joining(","));
		System.out.println(joinedAuthorities);
	}

}

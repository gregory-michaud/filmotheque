package fr.eni.tp.filmotheque.configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	
	private final String SELECT_USER = "select email, password, 1 from membre where email=?";
	private final String SELECT_ROLES = "select m.email, r.role from MEMBRE m inner join ROLES r on r.IS_ADMIN = m.admin where m.email = ?";

	/**
	 * Récupération des membres de l'application via la base de données */
	@Bean
	UserDetailsManager userDetailsManager(DataSource dataSource) {
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
		jdbcUserDetailsManager.setUsersByUsernameQuery(SELECT_USER);
		jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(SELECT_ROLES);
		return jdbcUserDetailsManager;
	}
	
	/**
	* Restriction des URLs selon la connexion utilisateur et leurs rôles
	*/
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> {
			//permettre à tout le monde d'accéder à l'URL racine
			auth
				//Permettre aux visiteurs d'accéder à la liste des films
				.requestMatchers(HttpMethod.GET, "/films").permitAll()
				//Permettre aux visiteurs d'accéder au détail d'un film
				.requestMatchers(HttpMethod.GET, "/films/detail").permitAll()
				// Accès à la vue principale
				.requestMatchers("/").permitAll()
				.requestMatchers("/login").permitAll()
				// Permettre à tous d'afficher correctement les images et CSS
				.requestMatchers("/css/*").permitAll()
				.requestMatchers("/images/*").permitAll()
				// Il faut être connecté pour toutes autres URLs
				.anyRequest().authenticated();
		});
		//Customiser le formulaire
		http.formLogin(form -> {
			form.loginPage("/login").permitAll();
			form.defaultSuccessUrl("/session").permitAll();			
		});
		
		// /logout --> vider la session
		http.logout(logout -> 
			logout
			.invalidateHttpSession(true)
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
			.logoutSuccessUrl("/"));

		
		
		return http.build();

		
	}
	
	
	
	
}

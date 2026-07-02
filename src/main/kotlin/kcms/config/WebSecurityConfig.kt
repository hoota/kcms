package kcms.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Value("\${cms.auth.username:admin}")
    lateinit var authUsername: String

    @Value("\${cms.auth.password:qqq}")
    lateinit var authPassword: String

    // Настройка авторизации
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/kcms/**").authenticated() // Требовать авторизацию для /cms/**
            .anyRequest().permitAll() // Остальные запросы разрешены
            .and()
            .httpBasic() // Включить Basic Auth
            .and()
            .csrf().disable() // CSRF можно отключить для простоты (особенно для API)
    }

    // Настройка in-memory пользователя
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .inMemoryAuthentication()
            .withUser(authUsername)
            .password(passwordEncoder().encode(authPassword))
            .roles("ADMIN")
    }

    // Простой PasswordEncoder
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return NoOpPasswordEncoder.getInstance() // Для демо. Лучше использовать BCrypt в продакшене
    }
}

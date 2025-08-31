package kcms.config

import kiss.gossr.spring.CssHelper
import kiss.gossr.spring.RoutesHelper
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedMethods("POST", "GET", "DELETE", "PUT", "OPTIONS")
            // .allowedOrigins("http://4jav.movie")
            .allowedOriginPatterns("*")
            .allowCredentials(true)

    }

    @Bean
    fun routesHelper(
        handlerMapping: RequestMappingHandlerMapping,
        applicationContext: ApplicationContext
    ) = RoutesHelper(
        applicationContext, handlerMapping
    )

    @Bean
    fun cssHelper(
        handlerMapping: RequestMappingHandlerMapping,
        applicationContext: ApplicationContext
    ) = CssHelper(
        applicationContext,
        handlerMapping,
        devMode = System.getProperty("dev-mode") == "true"
    )

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
        return restTemplate
    }

}
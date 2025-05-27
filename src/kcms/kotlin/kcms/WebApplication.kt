package kcms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
	scanBasePackages = ["kcms"],
	// exclude = [ DataSourceAutoConfiguration::class ]
)
class WebApplication

fun main(args: Array<String>) {
	runApplication<WebApplication>(*args)
}

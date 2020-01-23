package red.binder.mildom.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MildomUserApplication

fun main(args: Array<String>) {
    runApplication<MildomUserApplication>(*args)
}

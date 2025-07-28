package com.paymentsystem.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentInitiationSystemApplication

fun main(args: Array<String>) {
	runApplication<PaymentInitiationSystemApplication>(*args)
}

package com.yi.labbooking

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(CliApplication().run(args.toList()))
}

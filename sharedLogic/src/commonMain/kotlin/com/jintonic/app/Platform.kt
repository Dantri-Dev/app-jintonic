package com.jintonic.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
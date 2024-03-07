package core

import org.instancio.Instancio

open class BaseTest {
    inline fun <reified T : Any> random(): T = Instancio.create(T::class.java)
}

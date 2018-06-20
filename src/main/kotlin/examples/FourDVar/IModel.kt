package FourDVar

import io.improbable.keanu.kotlin.DoubleOperators

interface IModel<DATA : DoubleOperators<DATA>> {
    fun step() : Collection<DATA>
    fun getState() : Collection<DATA>
    fun setState(state : Collection<DATA>)
}
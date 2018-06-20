package Lorenz4DVar

import FourDVar.IModel
import io.improbable.keanu.kotlin.DoubleOperators
import io.improbable.keanu.randomfactory.RandomFactory
import java.util.ArrayList

class LorenzModel<DOUBLE: DoubleOperators<DOUBLE>>(var x: DOUBLE, var y: DOUBLE, var z: DOUBLE,
                                                   val random: RandomFactory<DOUBLE>) : IModel<DOUBLE> {

    val SIGMA = 10.0
    val BETA = 2.66667
    val RHO = 28.0
    val DT = 0.01

    val MICROSTEPS_PER_STEP = 5
    val OBSERVATION_ERROR = 0.5

    constructor(startState : List<DOUBLE>, rand : RandomFactory<DOUBLE>) :
            this(startState[0], startState[1], startState[2], rand)


    override fun step(): Collection<DOUBLE> {
        val observations = ArrayList<DOUBLE>(MICROSTEPS_PER_STEP)

        for (i in 0 until MICROSTEPS_PER_STEP) {
            microStep()
            observations.add(observe())
        }

        return observations
    }


    fun microStep() {
        val dx = ((y - x) * SIGMA) * DT
        val dy = (x * (z - RHO) + y) * (-DT)
        val dz = (x * y - z * BETA) * DT
        x += dx
        y += dy
        z += dz
    }


    private fun observe(): DOUBLE {
        return random.nextGaussian(x, OBSERVATION_ERROR)
    }


    override fun getState(): Collection<DOUBLE> {
        return listOf(x, y, z)
    }


    override fun setState(state: Collection<DOUBLE>) {
        val it = state.iterator()
        x = it.next()
        y = it.next()
        z = it.next()
    }
}
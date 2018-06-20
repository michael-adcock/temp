package examples

import io.improbable.keanu.algorithms.variational.GradientOptimizer
import io.improbable.keanu.vertices.dbl.DoubleVertex
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import java.util.Arrays.asList
import io.improbable.keanu.kotlin.*
import io.improbable.keanu.network.BayesianNetwork


data class State(val x : DoubleVertex, val y : DoubleVertex, val z : DoubleVertex) {

//    fun valueList() : List<Double>() {
//        return asList(x.value, y.value, z.value)
//    }
}

fun main(args : Array<String>) {
    var state = State(
            GaussianVertex(20.0, 1.0),
            GaussianVertex(19.0, 1.0),
            GaussianVertex(50.0, 1.0))

    val observations = asList(19.670, 19.316, 18.179, 16.088, 15.144)

    for (step in 0..4) {
        state = lorenzTimestep(state)
        val observation = GaussianVertex(state.x, 1.0)
        observation.observe(observations[step])
        println("" + state.x.value.scalar() + " " + state.y.value.scalar() + " " + state.z.value.scalar())
    }

    var model = BayesianNetwork(state.x.connectedGraph) // can be x, y or z
    var optimiser = GradientOptimizer(model)
    optimiser.maxAPosteriori(100000)


    println(state.x.connectedGraph)
    println("\n" + state.x.value.scalar() + " " + state.y.value.scalar() + " " + state.z.value.scalar())
}


fun lorenzTimestep(state : State) : State {
    //intergrate one timestep
    var sigma = 10.0
    var beta = 8.0 / 3.0
    var rho = 28.0
    val dt = 0.01

    var dx = sigma * (state.y - state.x)
    var dy = state.x * (rho - state.z) - state.y
    var dz = state.x * state.y - beta * state.z

    return State(state.x + dx * dt, state.y + dy * dt, state.z + dz * dt)
}
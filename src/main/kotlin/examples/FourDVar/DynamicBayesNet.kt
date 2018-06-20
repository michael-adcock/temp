package FourDVar


import io.improbable.keanu.kotlin.ArithmeticDouble
import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.vertices.dbl.DoubleVertex

class DynamicBayesNet<T : DoubleVertex> {

    var startState : Collection<T>
    var observationVertices : Collection<DoubleVertex>
    var endState : Collection<DoubleVertex>
    var net : BayesianNetwork

    constructor(probabilisticModel: IModel<DoubleVertex>, startState : Collection<T>) {
        // generate Bayes' network
        this.startState = startState
        probabilisticModel.setState(startState)
        observationVertices = probabilisticModel.step()
        endState = probabilisticModel.getState()
        net = BayesianNetwork(startState.iterator().next().connectedGraph)
    }

    fun observe(observations: Iterable<ArithmeticDouble>) {
        val observation = observations.iterator()
        for (vertex in observationVertices) {
            if (!observation.hasNext()) throw(ArrayIndexOutOfBoundsException("wrong number of observations"))
            vertex.observe(observation.next().value)
        }
    }
}
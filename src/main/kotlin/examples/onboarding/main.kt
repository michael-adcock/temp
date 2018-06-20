package examples.onboarding

import io.improbable.keanu.algorithms.mcmc.MetropolisHastings
import io.improbable.keanu.algorithms.variational.GradientOptimizer
import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex

fun main(args : Array<String>) {
    var T = GaussianVertex(20.0, 5.0) // Prior for temp 20 C +- 5
    var thermo1 = GaussianVertex(T, 1.0)  // Reading with noise - sigms represents uncertainty
    var thermo2 = GaussianVertex(T, 1.0)  // Second reading

    //var tSum = thermo1 * thermo2 // can do arithmetic with distributions

    thermo1.observe(20.6) // observed real life value
    thermo2.observe(19.9)

    //tSum.dualNumber.partialDerivatives.withRespectTo(thermo1) // diffentiate

    println(T.connectedGraph)
    var model = BayesianNetwork(T.connectedGraph) // Explores all  connections in model
    var optimiser = GradientOptimizer(model) // Auto differerntial of model
    optimiser.maxAPosteriori(100) // non linerar optimiser      // can use before MCMC

    println(T.value) // most probable value
    val samples = MetropolisHastings.getPosteriorSamples(model, listOf(T), 10000) // MCMC
    println(samples.get(T).asList())
    println(samples.drop(1000).downSample(5).get(T).mode) //burn in // downsample elminates very close samples
}
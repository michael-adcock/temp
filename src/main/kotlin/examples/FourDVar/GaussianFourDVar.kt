package FourDVar

import io.improbable.keanu.algorithms.mcmc.MetropolisHastings
import io.improbable.keanu.algorithms.variational.GradientOptimizer
import io.improbable.keanu.kotlin.plus
import io.improbable.keanu.tensor.dbl.DoubleTensor
import io.improbable.keanu.vertices.dbl.DoubleVertex
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import java.util.*

class GaussianFourDVar {

    private val MAX_MAP_EVALUATIONS = 2000
    private val POSTERIOR_SAMPLE_COUNT = 50
    private val BEST_FIT_SIGMA = 1.0

    fun assimilate(dbNet: DynamicBayesNet<GaussianVertex>) {
        val bestFit = variationalBayes(dbNet)
        cycle(dbNet, bestFit)
    }

    private fun variationalBayes(dbNet: DynamicBayesNet<GaussianVertex>): HashMap<DoubleVertex, GaussianVertex> {
        val graphOptimizer = GradientOptimizer(dbNet.net)
        graphOptimizer.maxAPosteriori(MAX_MAP_EVALUATIONS)

        val endStateBestFit = HashMap<DoubleVertex, GaussianVertex>()
        for (vertex in dbNet.endState) {
            endStateBestFit[vertex] = GaussianVertex(vertex, BEST_FIT_SIGMA)
        }
        val endStateList = arrayListOf<DoubleVertex>()
        dbNet.endState.forEach { dv -> endStateList.add(dv) }

        val samples = MetropolisHastings.getPosteriorSamples(dbNet.net, endStateList, POSTERIOR_SAMPLE_COUNT)

        for (vertex in dbNet.endState) {
            var xbar = 0.0
            var x2bar = 0.0
            for (sample in samples[vertex].asList()) {
                xbar.plus(sample)
                x2bar.plus(sample * sample)
            }
            val n = samples[vertex].asList().size
            xbar.div(n * Math.sqrt((n - 1.0) / n))
            x2bar.div(n - 1)
            endStateBestFit.getValue(vertex).sigma.value = DoubleTensor.create(Math.sqrt(x2bar - xbar * xbar + 0.05), intArrayOf(1))
        }
        return endStateBestFit
    }

    private fun cycle(dbNet: DynamicBayesNet<GaussianVertex>, bestFit: Map<DoubleVertex, GaussianVertex>) {
        val endVertexIt = dbNet.endState.iterator()
        for (startVertex in dbNet.startState) {
            if (!endVertexIt.hasNext()) throw(ArrayIndexOutOfBoundsException("start and end states don't have the same dimension, strange."))
            val endVertex = endVertexIt.next()
            val bestFitVertex = bestFit.getValue(endVertex)
            startVertex.mu.value = bestFitVertex.mu.value
            startVertex.sigma.value = bestFitVertex.sigma.value
        }
        for (startVertex in dbNet.startState) {
            startVertex.setAndCascade(startVertex.mu.value)
        }
    }
}
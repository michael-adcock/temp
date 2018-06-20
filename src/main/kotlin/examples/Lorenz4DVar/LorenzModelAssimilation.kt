package Lorenz4DVar

import FourDVar.GaussianFourDVar
import FourDVar.DynamicBayesNet
import io.improbable.keanu.randomfactory.DoubleVertexFactory
import io.improbable.keanu.randomfactory.RandomDoubleFactory
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex

class LorenzModelAssimilation {

    var realWorldStartX = 20.0
    var realWorldStartY = 19.0
    var realWorldStartZ = 50.0
    var modelStartX = realWorldStartX + 0.1 // model state is arbitrarily perturbed from real state
    var modelStartY = realWorldStartY + 0.2
    var modelStartZ = realWorldStartZ - 0.3
    var initialStateUncertainty = 1.0


    fun run(NUMBER_OF_WINDOWS: Int) {

        val random = RandomDoubleFactory()
        val realWorldStartState = listOf(
                random.nextConstant(realWorldStartX),
                random.nextConstant(realWorldStartY),
                random.nextConstant(realWorldStartZ)
        )
        val realWorld = LorenzModel(realWorldStartState, random)

        val probabilistic = DoubleVertexFactory()
        val probStartState = listOf(
                probabilistic.nextGaussian(modelStartX, initialStateUncertainty),
                probabilistic.nextGaussian(modelStartY, initialStateUncertainty),
                probabilistic.nextGaussian(modelStartZ, initialStateUncertainty)
        )
        val probabilisticModel = LorenzModel(probStartState, probabilistic)

        val dynamicBayesNet = DynamicBayesNet<GaussianVertex>(probabilisticModel, probStartState)
        val fourDVar = GaussianFourDVar()

        for (windowNumber in 0 until NUMBER_OF_WINDOWS) {
            val observations = realWorld.step()
            dynamicBayesNet.observe(observations)
            fourDVar.assimilate(dynamicBayesNet)

            // Having performed inference during the assimilation step our startState is now the Gaussian approximation
            // of the end state given the observations, ready for the next assimilation cycle
            val it = dynamicBayesNet.startState.iterator()
            System.out.println(
                    "Real World:\t\t\t\t (${realWorld.x.value}, ${realWorld.y.value}, ${realWorld.z.value})\n" +
                            "Probabilistic Model:\t (${it.next().mu.value}, ${it.next().mu.value}, ${it.next().mu.value})\n")
        }
    }
}

fun main(args: Array<String>) {
    val assimilation = LorenzModelAssimilation()

    assimilation.run(100)
}
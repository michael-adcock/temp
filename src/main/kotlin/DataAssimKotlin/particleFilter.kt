package DataAssimKotlin

import StationSim.State
import StationSim.Station
import java.io.File
import java.util.*

fun main(args : Array<String>) {
    println("Starting...")


    // Read in truth data
    val file = File("simulation_outputs/truth_state_1.txt")
    var text: List<String> = file.bufferedReader().readLines()
    text = text.subList(1, text.lastIndex)


    //subset truth data by a given interval of steps
    val subsetInterval: Long = 200
    val subset = mutableListOf<State>()
    for (line in text) {
        var elements = line.split(",")
        if (elements[0].toLong() % subsetInterval == 0L) {
            subset.add(State(elements[0].toLong(), elements[1], elements[2].toInt(), elements[3].toDouble(),
                    elements[4].toDouble(), elements[5].toDouble(), elements[6], elements[7]))
        }
    }

    val numSimulations = 2

    var exitProbParams : MutableList<Array<DoubleArray?>> = mutableListOf()
    for(sim in 0 until numSimulations) {
        var exitProb = arrayOfNulls<DoubleArray>(3)
        for (i in 0 until 3) {
            val x = Random().nextDouble()
            exitProb[i] = doubleArrayOf(x, 1.0 - x)
        }
        exitProbParams.add(exitProb)
    }

    var simulations : MutableList<Station> = mutableListOf()
    for (exitProbs in exitProbParams) {
        val stationSim = Station(System.currentTimeMillis())
        stationSim.exitProbs = exitProbs
        stationSim.start()
        simulations.add(stationSim)
    }

    do {
        var checkCosts = false
        var costs : MutableMap<Station, Double> = mutableMapOf()
        for (sim in simulations) {
            if (!sim.schedule.step(sim)) {
                break
            }
            if (sim.schedule.steps % subsetInterval == 0L) {
                //costs.add(costFunction(sim))
                costs[sim] = costFunction(sim)
                checkCosts = true
            }
        }
        if (checkCosts) {
            val med = median(costs.values.toList())

            for ((sim, cost) in costs) {
                if (cost > med) {
                    //remove sim
                } else {
                    //clone sim
                }
            }
            checkCosts = false
        }
    }
        while (simulations[0].area.getAllObjects().size > 0 || simulations[0].schedule.steps < 10000)

    for (sim in simulations) {
        //clean up
        sim.finish()
    }
        System.exit(0)


}

fun costFunction (sim : Station) : Double {
    return 0.0
}

fun median(l: List<Double>) = l.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }
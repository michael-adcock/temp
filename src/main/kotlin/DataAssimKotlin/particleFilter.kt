package DataAssimKotlin

import StationSim.Entrance
import StationSim.Person
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

    // Hom many simulations will we use
    val numSimulations = 10

    // Randomly create staring exit probabilities for simulations
    var exitProbParams : MutableList<Array<DoubleArray?>> = mutableListOf()
    for(sim in 0 until numSimulations) {
        var exitProb = arrayOfNulls<DoubleArray>(3)
        for (i in 0 until 3) {
            val x = Random().nextDouble()
            exitProb[i] = doubleArrayOf(x, 1.0 - x)
        }
        exitProbParams.add(exitProb)
    }

    // Create the simulations
    var simulations : MutableList<Station> = mutableListOf()
    for (exitProbs in exitProbParams) {
        val stationSim = Station(System.currentTimeMillis())
        stationSim.exitProbs = exitProbs
        stationSim.start()
        simulations.add(stationSim)
    }

    // Main loop
    do {
        var checkCosts = false
        var costs : MutableMap<Station, Double> = mutableMapOf()


        // Run a step of each simulation
        for (sim in simulations) {
            if (!sim.schedule.step(sim)) {
                break
            }
            // Check if simulations have reached desired interval. If so, check performance with cost function
            // Store Sims and costs in a map
            if (sim.schedule.steps % subsetInterval == 0L) {
                //costs.add(costFunction(sim))
                costs[sim] = costFunction(sim, subset.filter { it.step == sim.schedule.steps })
                checkCosts = true
            }
        }

        // Remove worst performers, clone best performers
        if (checkCosts) {
            val med = median(costs.values.toList())
            for ((sim, cost) in costs) {
                if (cost > med) {
                    simulations.remove(sim)

                } else {
                    // Clone sim with adjusted exitProbs
                    val newSim = Station(System.currentTimeMillis())
                    // adjust exit probs slightly
                    for (probs in newSim.exitProbs) {
                        probs[0] += (Random().nextDouble() - 0.5) * 0.1
                    }

                    //Start sim and add to list
                    newSim.start()
                    simulations.add(newSim)

                    // set the state of the simulation
                    // Position of all active people
                    for (person in sim.area.getAllObjects()) {
                        if (person is Person) {
                            println("ok") ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            //val newPerson = Person(newSim.personSize, person.location, person.toString(), newSim, person.entrance.exitProbs, person.entrance)
                            newSim.area.setObjectLocation(person, person.location) // copy object needed ?
                        }
                    }

                    // List of finished people
                    for (person in sim.finishedPeople) {
                        if (person is Person) {
                            newSim.finishedPeople.add(person) // copy object needed ?
                        }
                    }

                    // Set correct num of remaining people to enter sim from each entrance
                    for ((i, entrance) in sim.entrances.withIndex()) {
                        if (entrance is Entrance) {
                            newSim.entrances[i].numPeople = entrance.numPeople
                        }
                    }
                    // Set correct number of people already added to sim
                    newSim.addedCount = sim.addedCount
                }
            }
        }
    } while (simulations[0].area.getAllObjects().size > 0 || simulations[0].schedule.steps < 10000)

    //clean up
    for (sim in simulations) {
        sim.finish()
    }
        System.exit(0)


}

fun costFunction (sim : Station, subset : List<State>) : Double {
    var cost = 0.0
    var found = true
    for (truth in subset) {
        for(person in sim.entrances)
            if (truth.person == person.toString()) {
                cost += Math.sqrt(((person.location.x - truth.x) * (person.location.x - truth.x)) -
                        ((person.location.y - truth.y) * (person.location.y - truth.y)))
                found = true
            }
        if (!found) {
            cost += 50
            found = false
        }
    }
    return cost
}

fun median(l: List<Double>) = l.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }
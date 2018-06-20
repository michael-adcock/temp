package DataAssimKotlin

import StationSim.State
import StationSim.Station
import StationSim.StationWithUI
import io.improbable.keanu.vertices.dbl.DoubleVertex
import java.io.File
import sim.engine.SimState
import sim.util.Double2D


//data class State(val x: DoubleVertex, )

fun main(args : Array<String>) {
    println("Starting...")

    val file = File("simulation_outputs/truth_state.txt")

    var text:List<String> = file.bufferedReader().readLines()
    text = text.subList(1, text.lastIndex)

    val subsetInterval:Long = 200
    val subset = mutableListOf<State>()
    for (line in text) {
        var elements = line.split(",")
        if (elements[0].toLong() % subsetInterval == 0L) {
            subset.add(State(elements[0].toLong(), elements[1], elements[2].toDouble(), elements[3].toDouble(),
                    elements[4].toDouble(), elements[5]))
        }
    }


    //Station.main(arrayOfNulls<String>(0))
    run {
        val stationSim = Station(System.currentTimeMillis())

        stationSim.start()
        do {
            if (stationSim.schedule.steps % subsetInterval == 0L) {
                for (person in stationSim.area.getAllObjects()) {
                    for (truth in subset) {
                        if (stationSim.schedule.steps == truth.step && person.toString() == truth.person) {
                            stationSim.area.setObjectLocation(person, Double2D(truth.x, truth.y))
                            println("Position Changed")
                        }
                    }
                }
            }
            if (!stationSim.schedule.step(stationSim)) {
                break
            }
            println("Step: " + stationSim.schedule.steps + "\tNumber of people in sim: " + stationSim.area.getAllObjects().size)
        }
        while (stationSim.area.getAllObjects().size > 0 && stationSim.schedule.steps < 3000)

        stationSim.finish()
        System.exit(0)
        println("Finished")

    }

}
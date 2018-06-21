package DataAssimKotlin

import StationSim.*
import io.improbable.keanu.vertices.dbl.DoubleVertex
import java.io.File
import sim.engine.SimState
import sim.util.Double2D


//data class State(val x: DoubleVertex, )

fun main(args : Array<String>) {
    println("Starting...")

    // Read in truth data
    val file = File("simulation_outputs/truth_state_1.txt")
    var text:List<String> = file.bufferedReader().readLines()
    text = text.subList(1, text.lastIndex)


    //subset truth data by a given interval of steps
    val subsetInterval:Long = 200
    val subset = mutableListOf<State>()
    for (line in text) {
        var elements = line.split(",")
        if (elements[0].toLong() % subsetInterval == 0L) {
            println(line)
            subset.add(State(elements[0].toLong(), elements[1], elements[2].toInt(), elements[3].toDouble(),
                    elements[4].toDouble(), elements[5].toDouble(), elements[6], elements[7]))
        }
    }

    // run our simulation and assamilate state of truth data exactly (except random
    run {
        val stationSim = Station(System.currentTimeMillis())

        stationSim.start()
        do {
            if (stationSim.schedule.steps % subsetInterval == 0L) {

                // remove all people from the sim
                stationSim.area.clear()
                stationSim.finishedPeople.clear()

                // Keep a count of how many people have been spawn by each entrance an total people spawned
                var entranceCounts : MutableMap<String, Int> = hashMapOf()
                var totalPeople = 0


                for (truth in subset) {
                   if (stationSim.schedule.steps == truth.step) {
                       totalPeople++

                       // update map for entrances
                       if (truth.entrance in entranceCounts.keys) {
                           entranceCounts[truth.entrance] = entranceCounts[truth.entrance]!! + 1
                       } else
                           entranceCounts[truth.entrance] = 1

                       // find entrance person was spawned from
                       var entrance = stationSim.entrances[0]
                       for (ent in stationSim.entrances) {
                           if (ent.toString() == truth.entrance) {
                               entrance = ent
                               println(entrance)
                           }
                       }

                       // create person and set location
                       val location = Double2D(truth.x, truth.y)
                       val person = Person(stationSim.personSize, location, truth.person, stationSim, entrance.exitProbs, entrance)

                       if (truth.finished == 1) {
                           // add people who have passed through exit
                           stationSim.finishedPeople.add(person)
                       } else {
                           // add people who are active in sim
                           stationSim.area.setObjectLocation(person, location)
                           println("Position Changed")
                           }
                   }
                }

                // reset values for people remaining to be spawned globally and per entrance
                stationSim.addedCount = totalPeople
                for (entrance in stationSim.entrances) {
                    entrance.numPeople = (stationSim.numPeople / stationSim.numEntrances) - entranceCounts[entrance.toString()]!!
                }
            }

            // next step of simulation
            if (!stationSim.schedule.step(stationSim)) {
                break
            }
            println("Step: " + stationSim.schedule.steps + "\tNumber of people in sim: " + stationSim.area.getAllObjects().size)
        }
        while (stationSim.area.getAllObjects().size > 0 && stationSim.schedule.steps < 10000)

        //clean up
        stationSim.finish()
        System.exit(0)
        println("Finished")

    }

}
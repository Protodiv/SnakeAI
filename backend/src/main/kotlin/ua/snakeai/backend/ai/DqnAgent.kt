package ua.snakeai.backend.ai

import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions
import java.io.File
import kotlin.random.Random

data class Transition(
    val state: DoubleArray,
    val action: Int,
    val reward: Double,
    val nextState: DoubleArray,
    val done: Boolean
)

class ReplayBuffer(private val maxSize: Int) {
    private val list = mutableListOf<Transition>()
    private var writeIndex = 0

    fun add(transition: Transition) {
        if (list.size < maxSize) {
            list.add(transition)
        } else {
            list[writeIndex] = transition
            writeIndex = (writeIndex + 1) % maxSize
        }
    }

    fun sample(batchSize: Int): List<Transition> {
        val size = list.size
        if (size == 0) return emptyList()
        return List(batchSize) { list.random() }
    }

    fun size(): Int = list.size
}

class DqnAgent(
    val name: String,
    learningRate: Double = 0.001,
    val gamma: Double = 0.99,
    val batchSize: Int = 64,
    memorySize: Int = 50000,
    var epsilon: Double = 1.0,
    val epsilonDecay: Double = 0.995,
    val epsilonMin: Double = 0.01
) {
    val replayBuffer = ReplayBuffer(memorySize)
    val network: MultiLayerNetwork

    init {
        val conf = NeuralNetConfiguration.Builder()
            .seed(12345)
            .updater(Adam(learningRate))
            .list()
            .layer(0, DenseLayer.Builder()
                .nIn(11)
                .nOut(256)
                .activation(Activation.RELU)
                .build())
            .layer(1, DenseLayer.Builder()
                .nIn(256)
                .nOut(128)
                .activation(Activation.RELU)
                .build())
            .layer(2, OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(128)
                .nOut(3)
                .activation(Activation.IDENTITY)
                .build())
            .build()

        network = MultiLayerNetwork(conf)
        network.init()
    }

    constructor(name: String, modelFile: File) : this(name) {
        val loaded = ModelSerializer.restoreMultiLayerNetwork(modelFile)
        this.network.setParams(loaded.params())
        this.epsilon = 0.01 // Use exploitation on loaded models
    }

    fun getQValues(state: DoubleArray): DoubleArray {
        val input = Nd4j.create(state, intArrayOf(1, 11))
        val output = network.output(input)
        return doubleArrayOf(output.getDouble(0, 0), output.getDouble(0, 1), output.getDouble(0, 2))
    }

    fun selectAction(state: DoubleArray, explore: Boolean = true): Pair<Int, Boolean> {
        if (explore && Random.nextDouble() < epsilon) {
            return Pair(Random.nextInt(3), true) // Random action
        }
        val qValues = getQValues(state)
        var maxIndex = 0
        var maxVal = qValues[0]
        for (i in 1..2) {
            if (qValues[i] > maxVal) {
                maxVal = qValues[i]
                maxIndex = i
            }
        }
        return Pair(maxIndex, false) // Greedy action
    }

    fun trainStep(): Double {
        if (replayBuffer.size() < batchSize) return 0.0

        val batch = replayBuffer.sample(batchSize)

        val statesArr = Array(batchSize) { batch[it].state }
        val nextStatesArr = Array(batchSize) { batch[it].nextState }

        val ndStates = Nd4j.create(statesArr)
        val ndNextStates = Nd4j.create(nextStatesArr)

        val qValuesCurrent = network.output(ndStates)
        val qValuesNext = network.output(ndNextStates)

        val targets = qValuesCurrent.dup()

        for (i in 0 until batchSize) {
            val transition = batch[i]
            val maxNextQ = qValuesNext.getRow(i.toLong()).max().getDouble(0)
            val targetQ = if (transition.done) {
                transition.reward
            } else {
                transition.reward + gamma * maxNextQ
            }
            targets.putScalar(longArrayOf(i.toLong(), transition.action.toLong()), targetQ)
        }

        network.fit(ndStates, targets)
        return network.score() // Returns loss score of the training step
    }

    fun decayEpsilon() {
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay
        }
    }

    fun save(file: File) {
        ModelSerializer.writeModel(network, file, true)
    }
}

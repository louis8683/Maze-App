package com.hfad.mazeapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.*

class MainActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var maze: MazeTable

    private val mazeWidth = 10
    private val mazeHeight = 10

    companion object {
        private var mSpeechRecognizer: SpeechRecognizer? = null
        private var mSpeechRecognizerIntent: Intent? = null
        private var mIslistening: Boolean = false
        private const val RESULT_SPEECH = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        maze = MazeTable(mazeHeight, mazeWidth, findViewById(R.id.table_maze), this)

        // setup the buttons
        findViewById<Button>(R.id.button_up).setOnClickListener { maze.move(MazeTable.UP) }
        findViewById<Button>(R.id.button_down).setOnClickListener { maze.move(MazeTable.DOWN) }
        findViewById<Button>(R.id.button_left).setOnClickListener { maze.move(MazeTable.LEFT) }
        findViewById<Button>(R.id.button_right).setOnClickListener { maze.move(MazeTable.RIGHT) }

        // Make sure speech recognition is available
        val textView = findViewById<TextView>(R.id.available)
        textView.text = "Speech Recognition Availability:" + SpeechRecognizer.isRecognitionAvailable(this).toString()

        // set onclick for the button
        val buttonListen = findViewById<Button>(R.id.button_listen)
        buttonListen.setOnClickListener{
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                startActivityForResult(intent, RESULT_SPEECH)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(this, "Oops! Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show()
            }
        }

        // Continuous Speech Recognition
        initSpeechRecognizer()
        // Button for backup manual listening
        val buttonListenContinuous = findViewById<Button>(R.id.button_start_continuous)
        buttonListenContinuous.setOnClickListener { mSpeechRecognizer?.startListening(mSpeechRecognizerIntent) }
    }

    // TODO: Does the recognizer still shows error when reopening the app without destroying it?
    override fun onStop() {
        super.onStop()
        mSpeechRecognizer?.destroy()
    }

    override fun onRestart() {
        super.onRestart()
        initSpeechRecognizer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSpeechRecognizer?.destroy()
    }

    // Override functions for RecognizerListener
    override fun onBeginningOfSpeech() {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {
        findViewById<TextView>(R.id.continuous_partial_result).text =
                "(Error:"+ error.toString() + ")"
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
    }
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onPartialResults(partialResults: Bundle?) {
        val strings = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (strings != null) {
            var outputString = ""
            for (string in strings) {
                if (textToCommand(string)) {
                    outputString = string
                    break
                }
            }
            findViewById<TextView>(R.id.continuous_partial_result).text = outputString
        }
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
    }
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onResults(results: Bundle?) {
        val strings = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (strings != null) {
            var outputString = ""
            for (string in strings) {
                if (textToCommand(string)) {
                    outputString = string
                    break
                }
            }
            findViewById<TextView>(R.id.continuous_partial_result).text = outputString
        }
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
    }
    override fun onRmsChanged(rmsdB: Float) {}

    private fun initSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizer?.setRecognitionListener(this)
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            RESULT_SPEECH -> {
                if(resultCode == Activity.RESULT_OK && null != data) {
                    val text : ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val textView = findViewById<TextView>(R.id.status)
                    textView.text = text[0]
                    textToCommand(text[0])
                }
            }
        }
    }

    private fun textToCommand(text: String): Boolean {
        val steps: Int =
            if (text.contains("二") || text.contains("兩") || text.contains("2")) 2
            else if (text.contains("三") || text.contains("3")) 3
            else if (text.contains("四") || text.contains("4")) 4
            else if (text.contains("五") || text.contains("5")) 5
            else if (text.contains("六") || text.contains("6")) 6
            else if (text.contains("七") || text.contains("7")) 7
            else if (text.contains("八") || text.contains("8")) 8
            else if (text.contains("九") || text.contains("9")) 9
            else 1

        val direction = when (true) {
            text.contains("上") -> MazeTable.UP
            text.contains("下") -> MazeTable.DOWN
            text.contains("左") -> MazeTable.LEFT
            text.contains("右") -> MazeTable.RIGHT
            else -> return false
        }

        maze.move(direction, steps)
        return true
    }
}

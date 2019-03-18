package com.hfad.mazeapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import org.w3c.dom.Text
import java.text.AttributedString

class MainActivity : AppCompatActivity(), RecognitionListener {

    private val UP = 1
    private val DOWN = 2
    private val LEFT = 3
    private val RIGHT = 4

    private val mazeWidth = 10
    private val mazeHeight = 10
    private val idForEachBlock = ArrayList<ArrayList<Int>>()
    private val currentPosition = Position(9, 9)
    private val RESULT_SPEECH = 1

    companion object {
        private var mSpeechRecognizer: SpeechRecognizer? = null
        private var mSpeechRecognizerIntent: Intent? = null
        private var mIslistening: Boolean = false
    }

    class Position (var row: Int, var col: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init table
        val table = findViewById<TableLayout>(R.id.table_maze)
        val tableRows = ArrayList<TableRow>(mazeHeight)
        for (i in 0 until mazeHeight) {
            tableRows.add(TableRow(this))
            table.addView(tableRows[i])
        }
        for (row in 0 until mazeHeight) {
            idForEachBlock.add(ArrayList())
            for (col in 0 until mazeWidth) {
                idForEachBlock[row].add(0)
                tableRows[row].addView(createDefaultBlock(col, row))
            }
        }

        // setup the buttons
        findViewById<Button>(R.id.button_up).setOnClickListener { move(UP) }
        findViewById<Button>(R.id.button_down).setOnClickListener { move(DOWN) }
        findViewById<Button>(R.id.button_left).setOnClickListener { move(LEFT) }
        findViewById<Button>(R.id.button_right).setOnClickListener { move(RIGHT) }

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
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onResults(results: Bundle?) {
        val strings = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (strings != null) {
            textToCommand(strings[0])
            findViewById<TextView>(R.id.continuous_partial_result).text = strings[0]
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

    private fun createDefaultBlock(col:Int, row:Int) : TextView {
        val textView = TextView(this)
        textView.text = " 0 "
        textView.id = View.generateViewId()
        idForEachBlock[row][col] = textView.id
        if (col == currentPosition.col && row == currentPosition.row)
            textView.setBackgroundColor(Color.BLUE)
        else
            textView.setBackgroundColor(Color.YELLOW)
        textView.setPadding(10,10,10,10)
        return textView
    }

    private fun move(command: Int, steps: Int = 1) {
        if (command == UP && currentPosition.row - steps < 0) return
        if (command == DOWN && currentPosition.row + steps > mazeHeight-1) return
        if (command == LEFT && currentPosition.col - steps < 0) return
        if (command == RIGHT && currentPosition.col + steps > mazeWidth-1) return

        val oldRow = currentPosition.row
        val oldCol = currentPosition.col
        val oldTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol])
        oldTextView.setBackgroundColor(Color.YELLOW)
        when (command) {
            UP -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow - steps][oldCol])
                newTextView?.setBackgroundColor(Color.BLUE)
                currentPosition.row -= steps
            }
            DOWN -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow + steps][oldCol])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.row += steps
            }
            LEFT -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol - steps])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.col -= steps
            }
            RIGHT -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol + steps])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.col += steps
            }
        }
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

    private fun textToCommand(text: String) {
        var isValidInput = true
        var steps = 1
        if (text.contains("二") || text.contains("兩") || text.contains("2")) steps = 2
        else if (text.contains("三") || text.contains("3")) steps = 3
        else if (text.contains("四") || text.contains("4")) steps = 4
        else if (text.contains("五") || text.contains("5")) steps = 5
        else if (text.contains("六") || text.contains("6")) steps = 6
        else if (text.contains("七") || text.contains("7")) steps = 7
        else if (text.contains("八") || text.contains("8")) steps = 8
        else if (text.contains("九") || text.contains("9")) steps = 9

        var direction = UP
        if (text.contains("上")) direction = UP
        else if (text.contains("下")) direction = DOWN
        else if (text.contains("左")) direction = LEFT
        else if (text.contains("右")) direction = RIGHT
        else isValidInput = false

        if (isValidInput) move(direction, steps)
    }
}

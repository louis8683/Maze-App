package com.hfad.mazeapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import org.w3c.dom.Text
import java.text.AttributedString

class MainActivity : AppCompatActivity() {

    private val UP = 1
    private val DOWN = 2
    private val LEFT = 3
    private val RIGHT = 4

    private val mazeWidth = 10
    private val mazeHeight = 10
    private val idForEachBlock = ArrayList<ArrayList<Int>>()
    private val currentPosition = Position(9, 9)
    private val RESULT_SPEECH = 1

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

        // listen button
        val textView = findViewById<TextView>(R.id.available)
        textView.text = "Speech Recognition Availability:" + SpeechRecognizer.isRecognitionAvailable(this).toString()

        val button = findViewById<Button>(R.id.button_listen)
        button.setOnClickListener{
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                startActivityForResult(intent, RESULT_SPEECH)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(this, "Oops! Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun move(command: Int) {
        if (command == UP && currentPosition.row == 0) return
        if (command == DOWN && currentPosition.row == mazeHeight-1) return
        if (command == LEFT && currentPosition.col == 0) return
        if (command == RIGHT && currentPosition.col == mazeWidth-1) return

        val oldRow = currentPosition.row
        val oldCol = currentPosition.col
        val oldTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol])
        oldTextView.setBackgroundColor(Color.YELLOW)
        when (command) {
            UP -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow - 1][oldCol])
                newTextView?.setBackgroundColor(Color.BLUE)
                currentPosition.row -= 1
            }
            DOWN -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow + 1][oldCol])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.row += 1
            }
            LEFT -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol - 1])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.col -= 1
            }
            RIGHT -> {
                val newTextView = findViewById<TextView>(idForEachBlock[oldRow][oldCol + 1])
                newTextView.setBackgroundColor(Color.BLUE)
                currentPosition.col += 1
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

                    when (textView.text.toString()) {
                        "往上", "上" -> move(UP)
                        "往下", "下" -> move(DOWN)
                        "往左", "左" -> move(LEFT)
                        "往右", "右" -> move(RIGHT)
                    }
                    textView.text = "Result:"
                    for (string in text) {
                        textView.text = textView.text.toString() + string
                    }
                }
            }
        }
    }
}

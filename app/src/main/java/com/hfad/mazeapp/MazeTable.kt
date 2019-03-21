package com.hfad.mazeapp

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

class MazeTable (private val mazeHeight: Int, private val mazeWidth: Int, tableLayout: TableLayout, private val context: Context){
    private val maze = listOf(
        listOf(1,0,1,1,0,0,0,0,0,0),
        listOf(1,0,0,1,1,1,1,1,1,1),
        listOf(1,1,1,1,0,0,0,0,0,1),
        listOf(0,0,0,0,0,1,1,1,1,1),
        listOf(0,1,0,0,0,1,0,0,0,0),
        listOf(0,1,1,1,1,1,0,0,0,0),
        listOf(0,0,0,1,0,0,0,0,0,0),
        listOf(0,1,0,1,1,1,1,1,1,1),
        listOf(0,1,0,1,0,0,0,0,0,1),
        listOf(0,1,1,1,0,0,0,0,0,1))

    companion object {
        const val UP = 1
        const val DOWN = 2
        const val LEFT = 3
        const val RIGHT = 4
    }


    private val idForEachBlock = ArrayList<ArrayList<Int>>()
    private val startPosition = Position(mazeWidth - 1, mazeHeight - 1)
    private val currentPosition = Position(mazeWidth - 1, mazeHeight - 1)
    private val endPosition = Position(0, 0)

    private class Position (var row: Int, var col: Int)
    private class BlockType {
        companion object {
            const val BLOCK_ROAD = 1
            const val BLOCK_WALL = 0
        }
    }

    init {
        // init table
        val tableRows = ArrayList<TableRow>(mazeHeight)
        for (i in 0 until mazeHeight) {
            tableRows.add(TableRow(context))
            tableLayout.addView(tableRows[i])
        }
        for (row in 0 until mazeHeight) {
            idForEachBlock.add(ArrayList())
            for (col in 0 until mazeWidth) {
                tableRows[row].addView(createDefaultBlock(col, row, maze[row][col]))
            }
        }
    }

    private fun createDefaultBlock(col:Int, row:Int, blockType: Int) : TextView {
        val textView = TextView(context)
        textView.text = " " + blockType.toString() + " "
        textView.id = View.generateViewId()
        idForEachBlock[row].add(textView.id)
        if (col == currentPosition.col && row == currentPosition.row)
            textView.setBackgroundColor(Color.BLUE)
        else if (col == endPosition.col && row == endPosition.row)
            textView.setBackgroundColor(Color.RED)
        else if (blockType == BlockType.BLOCK_ROAD)
            textView.setBackgroundColor(Color.GREEN)
        else
            textView.setBackgroundColor(Color.YELLOW)
        textView.setPadding(10,10,10,10)
        return textView
    }

    fun move(command: Int, steps: Int = 1) {
        if (command == UP && currentPosition.row - 1 < 0) return
        if (command == DOWN && currentPosition.row + 1 > mazeHeight-1) return
        if (command == LEFT && currentPosition.col - 1 < 0) return
        if (command == RIGHT && currentPosition.col + 1 > mazeWidth-1) return

        when (command) {
            UP -> {
                var tmpSteps = steps
                while (tmpSteps > 0 && maze[currentPosition.row-1][currentPosition.col] == BlockType.BLOCK_ROAD) {
                    reverseBlock(currentPosition.col, currentPosition.row)
                    currentPosition.row --
                    tmpSteps --
                    val newTextView = (context as Activity).findViewById<TextView>(idForEachBlock[currentPosition.row][currentPosition.col])
                    newTextView?.setBackgroundColor(Color.BLUE)
                }
            }
            DOWN -> {
                var tmpSteps = steps
                while (tmpSteps > 0 && maze[currentPosition.row+1][currentPosition.col] == BlockType.BLOCK_ROAD) {
                    reverseBlock(currentPosition.col, currentPosition.row)
                    currentPosition.row ++
                    tmpSteps --
                    val newTextView = (context as Activity).findViewById<TextView>(idForEachBlock[currentPosition.row][currentPosition.col])
                    newTextView?.setBackgroundColor(Color.BLUE)
                }
            }
            LEFT -> {
                var tmpSteps = steps
                while (tmpSteps > 0 && maze[currentPosition.row][currentPosition.col-1] == BlockType.BLOCK_ROAD) {
                    reverseBlock(currentPosition.col, currentPosition.row)
                    currentPosition.col --
                    tmpSteps --
                    val newTextView = (context as Activity).findViewById<TextView>(idForEachBlock[currentPosition.row][currentPosition.col])
                    newTextView?.setBackgroundColor(Color.BLUE)
                }
            }
            RIGHT -> {
                var tmpSteps = steps
                while (tmpSteps > 0 && maze[currentPosition.row][currentPosition.col+1] == BlockType.BLOCK_ROAD) {
                    reverseBlock(currentPosition.col, currentPosition.row)
                    currentPosition.col ++
                    tmpSteps --
                    val newTextView = (context as Activity).findViewById<TextView>(idForEachBlock[currentPosition.row][currentPosition.col])
                    newTextView?.setBackgroundColor(Color.BLUE)
                }
            }
        }
    }

    private fun reverseBlock(col:Int, row:Int) {
        val oldTextView = (context as Activity).findViewById<TextView>(idForEachBlock[row][col])
        if (col == endPosition.col && row == endPosition.row)
            oldTextView.setBackgroundColor(Color.RED)
        else if (maze[row][col] == BlockType.BLOCK_ROAD)
            oldTextView.setBackgroundColor(Color.GREEN)
        else
            oldTextView.setBackgroundColor(Color.YELLOW)
    }
}
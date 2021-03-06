package com.devtides.snake

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

class GameManager(context: Context, attibuteSet: AttributeSet) : SurfaceView(context, attibuteSet),
    SurfaceHolder.Callback {

    private val boardSize = 20
    private var pointSize = 0f
    private var w = Resources.getSystem().displayMetrics.widthPixels
    private var h = Resources.getSystem().displayMetrics.heightPixels
    private lateinit var apple: Point
    private val snake = arrayListOf<Point>()
    private val gameEngine = GameEngine(holder, this)
    private var movingDirection = Direction.LEFT
    private var updatedDirection = Direction.LEFT

    private var gameOver = false
    private var score = 0

    private val mpStart = MediaPlayer.create(context, R.raw.snake_start)
    private val mpApple = MediaPlayer.create(context, R.raw.snake_point)
    private val mpDie = MediaPlayer.create(context, R.raw.snake_die)

    init {
        holder.addCallback(this)
        pointSize = w * 0.9f / boardSize

        initGame()
    }

    fun initGame() {
        gameEngine.reset()
        gameOver = false
        snake.clear()
        val initialPoint = Point(Random().nextInt(boardSize), Random().nextInt(boardSize))
        snake.add(initialPoint)
        if (initialPoint.x < boardSize / 2) {
            movingDirection = Direction.RIGHT
            updatedDirection = Direction.RIGHT
        } else {
            movingDirection = Direction.LEFT
            updatedDirection = Direction.LEFT
        }

        generateNewApple()
        score = 0

        mpStart.start()
    }

    fun generateNewApple() {
        var valid = false
        while (!valid) {
            valid = true
            apple = Point(Random().nextInt(boardSize), Random().nextInt(boardSize))
            for (snakePoint: Point in snake) {
                if (apple.x == snakePoint.x && apple.y == snakePoint.y) {
                    valid = false
                    break
                }
            }
        }
    }

    fun move(direction: Direction) {
        if (!(movingDirection == Direction.UP && direction == Direction.DOWN) &&
            !(movingDirection == Direction.DOWN && direction == Direction.UP) &&
            !(movingDirection == Direction.LEFT && direction == Direction.RIGHT) &&
            !(movingDirection == Direction.RIGHT && direction == Direction.LEFT)
        ) {
            updatedDirection = direction
        }
    }

    fun update() {
        if(!gameOver && !checkCollision()) {
            val direction = updatedDirection

            val lastPoint = Point(snake[snake.size - 1].x, snake[snake.size - 1].y)

            if (snake.size > 1) {
                for (i in snake.size - 1 downTo 1) {
                    if (snake[i].x != snake[i - 1].x) {
                        snake[i].x = snake[i - 1].x
                    } else {
                        snake[i].y = snake[i - 1].y
                    }
                }
            }

            if (snake[0].x == apple.x && snake[0].y == apple.y) {
                snake.add(lastPoint)
                generateNewApple()
                gameEngine.increaseSpeed()
                updateScore()
                mpApple.start()
            }

            when (direction) {
                Direction.LEFT -> snake[0].x--
                Direction.RIGHT -> snake[0].x++
                Direction.UP -> snake[0].y--
                Direction.DOWN -> snake[0].y++
            }

            movingDirection = updatedDirection
        }
    }

    fun updateScore() {
        score++
        (context as MainActivity).updateScore(score)
    }

    fun checkCollision(): Boolean {
        when(updatedDirection) {
            Direction.UP -> {
                if(snake[0].y == 0) {
                    gameOver = true
                } else {
                    for(i in 1 until snake.size - 1) {
                        if(snake[0].x == snake[i].x && snake[0].y - 1 == snake[i].y) {
                            gameOver = true
                            break
                        }
                    }
                }
            }

            Direction.DOWN -> {
                if(snake[0].y == boardSize - 1) {
                    gameOver = true
                } else {
                    for(i in 1 until snake.size - 1) {
                        if(snake[0].x == snake[i].x && snake[0].y + 1 == snake[i].y) {
                            gameOver = true
                            break
                        }
                    }
                }
            }

            Direction.LEFT -> {
                if(snake[0].x == 0) {
                    gameOver = true
                } else {
                    for(i in 1 until snake.size - 1) {
                        if(snake[0].y == snake[i].y && snake[0].x - 1 == snake[i].x) {
                            gameOver = true
                            break
                        }
                    }
                }
            }

            Direction.RIGHT -> {
                if(snake[0].x == boardSize - 1) {
                    gameOver = true
                } else {
                    for(i in 1 until snake.size - 1) {
                        if(snake[0].y == snake[i].y && snake[0].x + 1 == snake[i].x) {
                            gameOver = true
                            break
                        }
                    }
                }
            }
        }

        if(gameOver) {
            (context as MainActivity).gameOver()
            mpDie.start()
        }

        return gameOver
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        drawBoard(canvas)
        drawApple(canvas)
        drawSnake(canvas)
    }

    fun drawBoard(canvas: Canvas?) {
        canvas?.drawRGB(255, 255, 255)
        val boardLeft = w * 0.05f
        val boardRight = w * 0.95f
        val boardTop = h * 0.1f
        val boardBottom = h * 0.1f + boardSize * pointSize

        val boardPaint = Paint()
        boardPaint.color = Color.BLACK

        canvas?.drawLine(boardLeft, boardTop, boardLeft, boardBottom, boardPaint)
        canvas?.drawLine(boardLeft, boardTop, boardRight, boardTop, boardPaint)
        canvas?.drawLine(boardLeft, boardBottom, boardRight, boardBottom, boardPaint)
        canvas?.drawLine(boardRight, boardTop, boardRight, boardBottom, boardPaint)
    }

    fun drawApple(canvas: Canvas?) {
        val applePaint = Paint()
        applePaint.color = Color.GREEN

        canvas?.drawRect(getPointRect(apple), applePaint)
    }

    fun drawSnake(canvas: Canvas?) {
        val snakePaint = Paint()
        snakePaint.color = Color.BLUE

        for (point: Point in snake) {
            canvas?.drawRect(getPointRect(point), snakePaint)
        }
    }

    fun getPointRect(point: Point): Rect {
        val left = (w * 0.05f + point.x * pointSize).toInt()
        val right = (left + pointSize).toInt()
        val top = (h * 0.1f + point.y * pointSize).toInt()
        val bottom = (top + pointSize).toInt()
        return Rect(left, top, right, bottom)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        gameEngine.surfaceHolder = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        try {
            gameEngine.setRunning(false)
            gameEngine.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        gameEngine.setRunning(true)
        gameEngine.start()
    }
}
package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private var _uiState = MutableStateFlow(value = GameUiState())

private var usedWords: MutableSet<String> = mutableSetOf()



class GameViewModel : ViewModel() {
    lateinit var currentWord: String
    private fun pickRandomWordShuffle(): String {
        currentWord = allWords.random()
        if(usedWords.contains(currentWord)) {
            return pickRandomWordShuffle()
        }
        else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }
    var userGuess by mutableStateOf("")
    init {
        resetGame()
    }
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }
        else {
            _uiState.update {currentState ->
                currentState.copy(isGuessedWordWrong = true)

            }
        }
        updateUserGuess("")
    }

    fun updateGameState(updatedScore: Int) {
        if(usedWords.size == MAX_NO_OF_WORDS) {
            //Last round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true,
                    revealAns = false
                )
            }
        }
        else {
            //Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                    revealAns = false
                )
            }
        }

    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordShuffle())
    }

    fun revealAns() {
        _uiState.update { currentState ->
            currentState.copy(
                revealAns = true
            )
        }
    }
}
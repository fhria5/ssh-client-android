package com.sshclient.ui

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

class TerminalEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    var onEnterKey: ((String) -> Unit)? = null

    init {
        setTextIsSelectable(true)
        isFocusable = true
        isFocusableInTouchMode = true
        inputType = android.text.InputType.TYPE_NULL
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        // Block standard input connection - we handle keys manually
        return null
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    onEnterKey?.invoke("\n")
                    return true
                }
                KeyEvent.KEYCODE_DEL -> {
                    onEnterKey?.invoke("\u007f") // DEL
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    onEnterKey?.invoke("\u001b[D") // ESC[D
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    onEnterKey?.invoke("\u001b[C") // ESC[C
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    onEnterKey?.invoke("\u001b[A") // ESC[A
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    onEnterKey?.invoke("\u001b[B") // ESC[B
                    return true
                }
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }
}
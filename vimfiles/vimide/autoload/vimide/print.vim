" Vimide Script.
" Author: keyhom (keyhom.c@gmail.com)
" License: Copyright (c) 2012 keyhom.c@gmail.com.
" 
"   This software is provided 'as-is', without any express or implied warranty.
"   In no event will the authors be held liable for any damages arising from
"   the use of this software.
"   
"   Permission is granted to anyone to use this software for any purpose
"   excluding commercial applications, and to alter it and redistribute it
"   freely, subject to the following restrictions:
"   
"     1. The origin of this software must not be misrepresented; you must not
"     claim that you wrote the original software. If you use this software
"     in a product, an acknowledgment in the product documentation would be
"     appreciated but is not required.
"   
"     2. Altered source versions must be plainly marked as such, and must not
"     be misrepresented as being the original software.
"   
"     3. This notice may not be removed or altered from any source
"     distribution.
" 

" ----------------------------------------------------------------------------
" Script Functions:
" ----------------------------------------------------------------------------

" Echos the supplied message at the supplied level with the specified
" highlight.
function! s:EchoLevel(message, level, highlight)
  " don't echo if the message is 0, which signals an execute failure.
  if type(a:message) == g:NUMBER_TYPE && a:message == 0
    return
  endif

  if g:VIdeLogLevel < a:level
    return
  endif

  if type(a:message) == g:LIST_TYPE
    let messages = a:message
  else
    let messages = split(a:message, '\n')
  endif

  exec 'echohl ' . a:highlight
  redraw
  if mode() == 'n'
    for line in messages
      echom line
    endfo
  else
    " if we aren't in normal mode then use regular 'echo' since echom messages
    " won't be displayed while the current mode is displayed in vim's command
    " line.
    echo join(messages, '\n') . '\n'
  endif
  echohl None
endfunction

" ----------------------------------------------------------------------------
" Functions:
" ----------------------------------------------------------------------------

" Echos in trace level.
function! vimide#print#EchoTrace(message, ...)
  if a:0 > 0
    call s:EchoLevel('(' . a:1 . 's) ' . a:message, 6, g:VIdeTraceHighlight)
  else
    call s:EchoLevel(a:message, 6, g:VIdeTraceHighlight)
  endif
endfunction

" Echos in debug level.
function! vimide#print#EchoDebug(message)
  call s:EchoLevel(a:message, 5, g:VIdeDebugHighlight)
endfunction

" Echos in info level.
function! vimide#print#EchoInfo(message)
  call s:EchoLevel(a:message, 4, g:VIdeInfoHighlight)
endfunction

" Echos in warning level.
function! vimide#print#EchoWarning(message)
  call s:EchoLevel(a:message, 3, g:VIdeWarningHighlight)
endfunction

" Echos in error level.
function! vimide#print#EchoError(message)
  call s:EchoLevel(a:message, 2, g:VIdeErrorHighlight)
endfunction

" Echos in fatal level.
function! vimide#print#EchoFatal(message)
  call s:EchoLevel(a:message, 1, g:VIdeFatalHighlight)
endfunction

" Echos a message using the info highlight regardless of what log level is
" set.
function! vimide#print#Echo(message)
  if a:message != '0' && g:VIdeLogLevel > 0
    exec 'echohl ' . g:VIdeInfoHighlight
    redraw
    for line in split(a:message, '\n')
      echom line
    endfo
    echohl None
  endif
endfunction

" vim:ft=vim

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
"
" Highlight:
"
" ----------------------------------------------------------------------------
" define Correction group based on Normal.
hi link Correction Normal
hi Correction gui=underline,bold term=underline,bold cterm=underline,bold

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_correct = 
      \ 'javaCorrect?project=<project>&file=<file>&line=<line>&offset=<offset>'

let s:command_correct_apply = s:command_correct . '&apply=<apply>'

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Corrects the code under the cursor.
"
" Corrent:
" ----------------------------------------------------------------------------
function! vimide#java#correct#Correct()
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#lang#SilentUpdate('java', 0)

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')

  let command = s:command_correct
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<line>', line('.'), '')
  let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')

  let window_name = file . '_correct'
  call vimide#window#TempWindowClear(window_name)

  let result = vimide#Execute(command)

  " error executing the command.
  if type(result) != g:DICT_TYPE && type(result) != g:STRING_TYPE
    return
  elseif type(result) == g:STRING_TYPE " no error on the current line.
    call vimide#print#Echo(result)
    return
  elseif len(result.corrections) == 0
    call vimide#print#EchoInfo('No Suggestions.')
    return
  endif

  let content = []
  call add(content, result.message)
  for correction in result.corrections
    call add(content, correction.index . '.' . result.offset . ': ' . correction.description)
    for line in split(correction.preview, '\n')
      call add(content, line != '' ? ("\t" . line) : line)
    endfor
  endfor

  call vimide#window#TempWindow(window_name, content)

  let filename = file
  let b:filename = filename
  augroup temp_window
    autocmd! BufWinLeave <buffer>
    call vimide#util#GoToBufferWindowRegister(filename)
  augroup END

  setlocal ft=java

  " exec "syntax match Normal /" . escape(getline(1), '^$/\') . "/"
  syntax match Correction /^[0-9]\+\.[0-9]\+:.*/

  nnoremap <silent> <buffer> <cr>
        \ :call vimide#java#correct#CorrectApply()<cr>

  redraw | echo ""
endfunction

" ----------------------------------------------------------------------------
" Applies the suggestion at the current line.
"
" CorrectApply:
" ----------------------------------------------------------------------------
function! vimide#java#correct#CorrectApply()
  let closeNeed = 0
  let line = getline('.')
  if line =~ '^[0-9]\+\.[0-9]\+:'
    let winnr = bufwinnr('%')
    let name = substitute(expand('%:p'), '_correct$', '', '')
    let file_winnr = bufwinnr(bufnr('^' . b:filename))
    if file_winnr != -1
      exec file_winnr . "winc w"
      call vimide#lang#SilentUpdate()

      let index = substitute(line, '^\([0-9]\+\)\..*', '\1', '')

      let project = vimide#project#impl#GetProject()
      let file = vimide#util#LegalPath(expand('%:p'), 2)
      let command = s:command_correct_apply
      let command = substitute(command, '<project>', project, '')
      let command = substitute(command, '<file>', file, '')
      let command = substitute(command, '<line>', line('.'), '')
      let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')
      let command = substitute(command, '<apply>', index, '')

      call vimide#lang#Refactor(command)
      call vimide#lang#UpdateSrcFile('java', 1)

      exec winnr . 'winc w'
      let closeNeed = 1
    else
      call vimide#print#EchoError(name . ' no longer found in an open window.')
    endif
  endif

  if closeNeed
    close
  endif
endfunction

" vim:ft=vim

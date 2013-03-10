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
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_constructor = 
      \ '/javaConstructor?project=<project>&file=<file>&offset=<offset>'

let s:command_properties = 
      \ '/javaBeanProperties?project=<project>&file=<file>&offset=<offset>&type=<type>'

" ----------------------------------------------------------------------------
"
" Autocmd Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Generates a constructor by selecting range or the element under the cursor.
"
" GenerateConstructor:
"   first - the first element line number.
"   last  - the last element line number.
"   bang  - the bang.
" ----------------------------------------------------------------------------
function! vimide#java#impl#GenerateConstructor(first, last, bang)
  if !vimide#project#impl#IsCurrentFileInProject()
    return
  endif

  call vimide#lang#SilentUpdate()

  let properties = a:last == 1 ? [] :
        \ vimide#java#util#GetSelectedFields(a:first, a:last)

  let project = vimide#project#impl#GetProject()
  let file = expand('%:p')

  let command = s:command_constructor
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', vimide#util#LegalPath(file, 2), '')
  let command = substitute(command, '<offset>', vimide#util#GetOffset(), '')

  if a:bang == ''
    let command .= '&super=1'
  endif

  if len(properties) > 0
    for prop in properties
      if '' != prop
        let command .= '&field=' . prop
      endif
    endfor
  endif

  let result = vimide#Execute(command)

  if type(result) == g:STRING_TYPE && result != ''
    call vimide#print#EchoError(result)
    return
  endif

  if result != '0'
    call vimide#util#Reload({'retab' : 1})
    write
  endif
endfunction

" vim:ft=vim

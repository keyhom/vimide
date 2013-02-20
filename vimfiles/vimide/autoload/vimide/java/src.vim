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

let s:command_java_format = "/javaSrcFormat?project=<project>&file=<file>&hoffset=<hoffset>&toffset=<toffset>"
let s:command_comment = "/javaDocComment?project=<project>&file=<file>&offset=<offset>"
let s:command_organize_imports = "/organizeImports?project=<project>&file=<file>&offset=<offset>"

" ----------------------------------------------------------------------------
"
" Autoload Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Formats the supplied src file.
"
" Format:
"   first - the start line of the src file.
"   last  - the end line of the src file.
" ----------------------------------------------------------------------------
function! vimide#java#src#Format(first, last)
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)

  if '' == project
    return
  endif

  " current the file location.
  let file = vimide#util#LegalPath(file)

  " save the file to supply the dirty commit.
  write

  " silent updated.
  " get relative file path.
  let command = s:command_java_format
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let begin = vimide#util#GetOffset(a:first, 1)
  let end = vimide#util#GetOffset(a:last, 1) + len(getline(a:last)) - 1
  let command = substitute(command, '<hoffset>', begin, '')
  let command = substitute(command, '<toffset>', end, '')

  let result = vimide#Execute(command)
  if result != '0'
    call vimide#util#Reload({'retab': 1})
    write
  endif
endfunction

" ----------------------------------------------------------------------------
" Add/Update the comments for the element under the cursor.
"
" Comment:
" ----------------------------------------------------------------------------
function! vimide#java#src#Comment()
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)

  if '' == project
    return
  endif

  " save the file to supply the dirty commit.
  write

  " current the file location.
  let file = vimide#util#LegalPath(file)
  let offset = vimide#util#GetCurrentElementOffset()

  " silent updated.
  let command = s:command_comment
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')

  let result = vimide#Execute(command)

  if '0' != result
    call vimide#util#Reload({'retab': 1})
    write
  endif
endfunction

" ----------------------------------------------------------------------------
" Organize imports by adding missing and clean up.
"
" OrganizeImports:
" ----------------------------------------------------------------------------
function! vimide#java#src#OrganizeImports(...)
  let file = expand('%:p')
  let project = vimide#project#impl#GetProject(file)

  if '' == project
    return
  endif

  " save the file to supply the dirty commit.
  write

  " current the file location.
  let file = vimide#util#LegalPath(file)
  let offset = vimide#util#GetCurrentElementOffset()

  let command = s:command_organize_imports
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')

  if a:0
    let command .= '&types=' . join(a:1, ',')
  endif

  let result = vimide#Execute(command)

  if type(result) == g:STRING_TYPE
    call vimide#print#EchoError(command)
    return
  endif

  if type(result) == g:DICT_TYPE
    call vimide#util#Reload({'pos' : [result.line, result.column]})
    call vimide#lang#UpdateSrcFile('java', 1)
    return
  endif

  if type(result) != g:LIST_TYPE
    return
  endif

  let chosen = []
  for choices in result
    let choice = vimide#java#src#ImportPrompt(choices)
    if choice == ''
      return
    endif
    call add(chosen, choice)
  endfor

  if len(chosen)
    call vimide#java#src#OrganizeImports(chosen)
  endif

endfunction

" ----------------------------------------------------------------------------
" Prompts the user to choose the class to import.
"
" ImportPrompt:
"   choices - the list to choose.
" ----------------------------------------------------------------------------
function! vimide#java#src#ImportPrompt(choices)
  let response = vimide#util#PromptList("Choose the class to import", a:choices)
  if response == -1
    return ''
  endif

  return get(a:choices, response)
endfunction

" vim:ft=vim

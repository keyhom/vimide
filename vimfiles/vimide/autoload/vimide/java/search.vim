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
" Global Variables:
"
" ----------------------------------------------------------------------------

if !exists("g:VIdeJavaDocSearchSingleResult")
  " possible values ('open', 'lopen')
  let g:VIdeJavaDocSearchSingleResult = 'open'
endif

if !exists('g:VIdeJavaSearchSingleResult')
  " possible values ('split', 'edit', 'lopen')
  let g:VIdeJavaSearchSingleResult = g:VIdeDefaultFileOpenAction
endif

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_search_src = "javaSearch"
let s:command_search_doc = "javaDocSearch"

let s:search_element = "<search>?project=<project>&file=<file>&offset=<offset>&length=<length>&<args>"
let s:search_pattern = "<search>"
let s:options = ['-p', '-t', '-x', '-s', '-i']
let s:contexts = ['all', 'declarations', 'implementors', 'references']
let s:scopes = ['all', 'project']
let s:types = [
      \ 'annotation',
      \ 'class',
      \ 'classOrEnum',
      \ 'classOrInterface',
      \ 'constructor',
      \ 'enum',
      \ 'field',
      \ 'interface',
      \ 'method',
      \ 'package',
      \ 'type']

" ----------------------------------------------------------------------------
"
" Script Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Executes a search session.
" Usage closely resebles vimide request client usage.
" When doing a non-pattern search the element under the cursor is searched
" for.
"   Search for declarations of element under the cursor
"     call s:Search("-x", "declarations")
"   Search for references of HashMap
"     call s:Search("-p", "HashM*", "-t", "class", "-x", "references")
" Or all the arguments can be passed in at once:
"   call s:Search("-p 'HashM*' -t class -x references")
"
" Search:
"   command - the search command.
"   [...]   - the arguments.
" ----------------------------------------------------------------------------
function! s:Search(command, ...)
  let argline = ""
  let index = 1
  while index <= a:0
    if index != 1
      let argline = argline . " "
    endif
    let argline .= a:{index}
    let index = index + 1
  endwhile

  " check if pattern supplied without -p.
  if argline !~ '^\s*-[a-z]' && argline !~ '^\s*$'
    let argline = '-p ' . argline
  endif

  let cmdOptions = vimide#util#ToOption(argline)

  let in_project = vimide#project#impl#IsCurrentFileInProject()
  let project = vimide#project#impl#GetProject()

  " element search
  if argline !~ '-p\>'
    if &ft != 'java'
      call vimide#print#EchoWarning("Element searches only supported in java search files.")
      return 0
    endif

    if !vimide#java#util#IsValidIdentifier(expand('<cword>'))
      call vimide#print#EchoError("Element under the cursor is not a valid java identifier.")
      return 0
    endif

    if !in_project
      " build a pattern search and execute it.
      return s:SearchAlternative('-p ' . s:BuildPattern() . ' ' . argline, 1)
    endif

    let position = vimide#util#GetCurrentElementPosition()
    let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
    let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

    let search_cmd = s:params_search_element
    let search_cmd = substitute(search_cmd, '<search>', a:command, '')
    let search_cmd = substitute(search_cmd, '<project>', project, '')
    let search_cmd = substitute(search_cmd, '<file>', file, '')
    let search_cmd = substitute(search_cmd, '<offset>', offset, '')
    let search_cmd = substitute(search_cmd, '<length>', length, '')
    let search_cmd = substitute(search_cmd, '<args>', argline, '')

    let result = vimide#Execute(search_cmd)
  else 
    " Pattern search
    let search_cmd = s:search_pattern
    let search_cmd = substitute(search_cmd, '<search>', a:command, '')
    " the http url
    let search_cmd .= '?' 

    if '' != project
      let search_cmd .= 'project=' . project 
    endif

    if '' != file
      let search_cmd .= '&file=' . file
    endif

    let search_cmd .= argline
    " quote the search pattern.
    let search_cmd = substitute(search_cmd, '\(.*-p\s\+\)\(.\{-}\)\(\s\|$\)\(.*\)', '\1"\2"\3\4', '')

    let result = vimide#Execute(search_cmd)

    if !in_project && filereadable(expand('%'))
      return result + s:SearchAlternative(argline, 0)
    endif
  endif

  return result
endfunction

" ----------------------------------------------------------------------------
" Executes a search and displays the results via quickfix.
"
" SearchAndDisplay:
"   type  - the type for source.
"   args  - the arguments.
" ----------------------------------------------------------------------------
function! vimide#java#search#SearchAndDisplay(type, args)
  " if running from a non java source file, no SilentUpdate needed.
  if &ft == 'java'
    call vimide#lang#SilentUpdate()
  endif

  let argline = a:args

  " check if just a pattern was supplied.
  if argline =~ '^\s*\w'
    let argline = '-p ' . argline
  endif

  let results = s:Search(a:type, argline)
  if type(results) != g:LIST_TYPE
    return
  endif

endfunction

" ----------------------------------------------------------------------------
" Custom command completion for JavaSearch.
"
" CommandCompleteJavaSearch:
"   argLead   - 
"   cmdLine   - 
"   cursorPos -
" ----------------------------------------------------------------------------
function! vimide#java#search#CommandCompleteJavaSearch(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  if cmdLine =~ '-s\s\+[a-z]*$'
    let scopes = deepcopy(s:scopes)
    call filter(scopes, 'v:val =~ "^' . argLead . '"')
    return scopes
  elseif cmdLine =~ '-t\s\+[a-z]*$'
    let types = deepcopy(s:types)
    call filter(types, 'v:val =~ "^' . argLead . '"')
    return types
  elseif cmdLine =~ '-x\s\+[a-z]*$'
    let contexts = deepcopy(s:contexts)
    call filter(contexts, 'v:val =~ "^' . argLead . '"')
    return contexts
  elseif cmdLine =~ '\s\+[-]\?$'
    let options = deepcopy(s:options)
    let index = 0
    for option in options
      if a:cmdLine =~ option
        call remove(options, index)
      else
        let index += 1
      endif
    endfor
    return options
  endif

  return []
endfunction

" vim:ft=vim
